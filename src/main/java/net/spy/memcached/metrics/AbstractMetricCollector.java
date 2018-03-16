package net.spy.memcached.metrics;


public abstract class AbstractMetricCollector  implements MetricCollector {

  @Override
  public void decrementCounter(String name) {
    decrementCounter(name, 1);
  }

  @Override
  public void incrementCounter(String name) {
    incrementCounter(name, 1);
  }

}
