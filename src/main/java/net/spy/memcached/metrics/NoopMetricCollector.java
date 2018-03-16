package net.spy.memcached.metrics;

public final class NoopMetricCollector extends AbstractMetricCollector {

  @Override
  public void addCounter(String name) {
    return;
  }

  @Override
  public void removeCounter(String name) {
    return;
  }

  @Override
  public void incrementCounter(String name, int amount) {
    return;
  }

  @Override
  public void decrementCounter(String name, int amount) {
    return;
  }

  @Override
  public void addMeter(String name) {
    return;
  }

  @Override
  public void removeMeter(String name) {
    return;
  }

  @Override
  public void markMeter(String name) {
    return;
  }

  @Override
  public void addHistogram(String name) {
    return;
  }

  @Override
  public void removeHistogram(String name) {
    return;
  }

  @Override
  public void updateHistogram(String name, int amount) {
    return;
  }

}
