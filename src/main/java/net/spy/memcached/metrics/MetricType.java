package net.spy.memcached.metrics;

/**
 * Defines the type of metric collection to use.
 *
 * More detailed types provide more insight, but can come with more
 * overhead during collection.
 */
public enum MetricType {

  /**
   * No metrics collection.
   *
   * If the "OFF" type is chosen, no metrics will be registered
   * and collected.
   */
  OFF,

  /**
   * Metrics useful for performance-related tracing.
   *
   * These metrics provide insight into the application performance
   * and show how the operations flow in and out of the library.
   */
  PERFORMANCE,

  /**
   * Metrics useful for debugging.
   *
   * These metrics (in addition to the "PERFORMANCE" metrics) provide
   * more insight into the state of the library (for example node states),
   * but it comes with larger aggregation overhead. Use during development
   * and debug sessions.
   */
  DEBUG
}
