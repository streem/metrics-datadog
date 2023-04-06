package pro.streem.metrics.datadog.transport;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import pro.streem.metrics.datadog.model.DatadogCounter;
import pro.streem.metrics.datadog.model.DatadogGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Uses dogstatsd UDP protocol to push metrics to datadog. Note that datadog doesn't support
 * time in the UDP protocol. So all metrics are against current time.
 * <p/>
 * Also dogstatsd UDP doesn't support batching of metrics, so it pushes metrics as it receives
 * rather than batching.
 *
 * @see <a href="http://docs.datadoghq.com/guides/dogstatsd">dogstatsd</a>
 */
public class UdpTransport implements Transport {

  private static final Logger LOG = LoggerFactory.getLogger(UdpTransport.class);
  private final StatsDClient statsd;
  private final Map lastSeenCounters = new HashMap<String, Long>();

  private UdpTransport(String prefix, String statsdHost, int port, boolean isRetryingLookup, String[] globalTags) {
    final Callable<SocketAddress> socketAddressCallable;

    if(isRetryingLookup) {
      socketAddressCallable = volatileAddressResolver(statsdHost, port);
    } else {
      socketAddressCallable = staticAddressResolver(statsdHost, port);
    }

    statsd = new NonBlockingStatsDClient(
            prefix,
            Integer.MAX_VALUE,
            globalTags,
            new StatsDClientErrorHandler() {
              public void handle(Exception e) {
                LOG.error(e.getMessage(), e);
              }
            },
            socketAddressCallable
    );
  }

  public void close() throws IOException {
    statsd.stop();
  }

  public static class Builder {
    String prefix = null;
    String statsdHost = "localhost";
    int port = 8125;
    boolean isLookupRetrying = false;

    public Builder withPrefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder withStatsdHost(String statsdHost) {
      this.statsdHost = statsdHost;
      return this;
    }

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public Builder withRetryingLookup(boolean isRetrying) {
      this.isLookupRetrying = isRetrying;
      return this;
    }

    public UdpTransport build() {
      return new UdpTransport(prefix, statsdHost, port, isLookupRetrying, new String[0]);
    }
  }

  public Request prepare() throws IOException {
    return new DogstatsdRequest(statsd, lastSeenCounters);
  }

  public static class DogstatsdRequest implements Transport.Request {
    private final StatsDClient statsdClient;
    private final Map<String, Long> lastSeenCounters;

    public DogstatsdRequest(StatsDClient statsdClient, Map<String, Long> lastSeenCounters) {
      this.statsdClient = statsdClient;
      this.lastSeenCounters = lastSeenCounters;
    }

    /**
     * statsd has no notion of batch request, so gauges are pushed as they are received
     */
    public void addGauge(DatadogGauge gauge) {
      if (gauge.getPoints().size() > 1) {
        LOG.debug("Gauge " + gauge.getMetric() + " has more than one data point, " +
            "will pick the first point only");
      }
      double value = gauge.getPoints().get(0).get(1).doubleValue();
      String[] tags = gauge.getTags().toArray(new String[gauge.getTags().size()]);
      statsdClient.gauge(gauge.getMetric(), value, tags);
    }

    /**
     * statsd has no notion of batch request, so counters are pushed as they are received
     */
    public void addCounter(DatadogCounter counter) {
      if (counter.getPoints().size() > 1) {
        LOG.debug("Counter " + counter.getMetric() + " has more than one data point, " +
            "will pick the first point only");
      }
      long value = counter.getPoints().get(0).get(1).longValue();
      String[] tags = counter.getTags().toArray(new String[counter.getTags().size()]);
      StringBuilder sb = new StringBuilder("");
      for (int i=tags.length - 1; i>=0; i--) {
        sb.append(tags[i]);
        if (i > 0) {
          sb.append(",");
        }
      }

      String metric = counter.getMetric();
      String finalMetricsSeenName = metric + ":" + sb.toString();
      long finalValue = value;
      if (lastSeenCounters.containsKey(finalMetricsSeenName)) {
        // If we've seen this counter before then calculate the difference
        // by subtracting the new value from the old. StatsD expects a relative
        // counter, not an absolute!
        finalValue = Math.max(0, value - lastSeenCounters.get(finalMetricsSeenName));
      }
      // Store the last value we saw so that the next addCounter call can make
      // the proper relative value
      lastSeenCounters.put(finalMetricsSeenName, value);

      statsdClient.count(metric, finalValue, tags);
    }

    /**
     * For statsd the metrics are pushed as they are received. So there is nothing do in send.
     */
    public void send() {
    }
  }

  // Visible for testing.
  static Callable<SocketAddress> staticAddressResolver(final String host, final int port) {
    try {
      return NonBlockingStatsDClient.staticAddressResolution(host, port);
    } catch(final Exception e) {
      LOG.error("Error during constructing statsd address resolver.", e);
      throw new RuntimeException(e);
    }
  }

  // Visible for testing.
  static Callable<SocketAddress> volatileAddressResolver(final String host, final int port) {
    return NonBlockingStatsDClient.volatileAddressResolution(host, port);
  }
}
