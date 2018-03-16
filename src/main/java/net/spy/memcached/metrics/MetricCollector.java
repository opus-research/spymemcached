package net.spy.memcached.metrics;


public interface MetricCollector {

  void addCounter(String name);
  void removeCounter(String name);
  void incrementCounter(String name);
  void incrementCounter(String name, int amount);
  void decrementCounter(String name);
  void decrementCounter(String name, int amount);

  void addMeter(String name);
  void removeMeter(String name);
  void markMeter(String name);

  void addHistogram(String name);
  void removeHistogram(String name);
  void updateHistogram(String name, int amount);

}
