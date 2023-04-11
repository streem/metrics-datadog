# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.1] - Unreleased

[2.0.1]: https://github.com/streem/metrics-datadog/compare/1.1.13...HEAD

### Changed

* Renamed Java package from `org.coursera` to `pro.streem`
* Updated from `javax.validation` to `jakarta.validation`
* Update minimum Java version from 8 to 11 since that's required by Dropwizard 4

### Fixed

* Updated `dropwizard-metrics` library from `1.3.4` to `4.0.0` which does not contain any vulnerabilities, see https://mvnrepository.com/artifact/io.dropwizard/dropwizard-metrics/4.0.0

