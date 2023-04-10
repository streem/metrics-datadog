package pro.streem.metrics.datadog;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("default")
public class DefaultMetricNameFormatterFactory implements MetricNameFormatterFactory {
  public MetricNameFormatter build() {
    return new DefaultMetricNameFormatter();
  }
}
