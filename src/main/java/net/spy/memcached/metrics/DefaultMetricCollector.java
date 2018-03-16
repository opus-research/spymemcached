package net.spy.memcached.metrics;

import com.codahale.metrics.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class DefaultMetricCollector extends AbstractMetricCollector {

  public static final String DEFAULT_REPORTER_TYPE = "console";
  public static final String DEFAULT_REPORTER_INTERVAL = "30";
  public static final String DEFAULT_REPORTER_OUTDIR = "";
  private MetricRegistry registry;

  private ConcurrentHashMap<String, Counter> counters;
  private ConcurrentHashMap<String, Meter> meters;
  private ConcurrentHashMap<String, Histogram> histograms;

  public DefaultMetricCollector() {
    registry = new MetricRegistry();
    counters = new ConcurrentHashMap<String, Counter>();
    meters = new ConcurrentHashMap<String, Meter>();
    histograms = new ConcurrentHashMap<String, Histogram>();

    initReporter();
  }

  private void initReporter() {
    String reporterType =
      System.getProperty("net.spy.metrics.reporter.type", DEFAULT_REPORTER_TYPE);
    String reporterInterval =
      System.getProperty("net.spy.metrics.reporter.interval", DEFAULT_REPORTER_INTERVAL);
    String reporterDir =
      System.getProperty("net.spy.metrics.reporter.outdir", DEFAULT_REPORTER_OUTDIR);

    if(reporterType.equals("console")) {
      final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.SECONDS)
        .build();
      reporter.start(Integer.parseInt(reporterInterval), TimeUnit.SECONDS);
    } else if (reporterType.equals("jmx")) {
      final JmxReporter reporter = JmxReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.SECONDS)
        .build();
      reporter.start();
    } else if (reporterType.equals("csv")) {
      final CsvReporter reporter = CsvReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.SECONDS)
        .build(new File(reporterDir));
      reporter.start(Integer.parseInt(reporterInterval), TimeUnit.SECONDS);
    } else if (reporterType.equals("slf4j")) {
      final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.SECONDS)
        .outputTo(LoggerFactory.getLogger(MetricCollector.class))
        .build();
      reporter.start(Integer.parseInt(reporterInterval), TimeUnit.SECONDS);
    } else {
        throw new IllegalStateException("Unknown Metrics Reporter Type: " + reporterType);
    }
  }

  @Override
  public void addCounter(String name) {
    if (!counters.containsKey(name)) {
      counters.put(name, registry.counter(name));
    }
  }

  @Override
  public void removeCounter(String name) {
    if (!counters.containsKey(name)) {
      registry.remove(name);
      counters.remove(name);
    }
  }

  @Override
  public void incrementCounter(String name, int amount) {
    if (counters.containsKey(name)) {
      counters.get(name).inc(amount);
    }
  }

  @Override
  public void decrementCounter(String name, int amount) {
    if (counters.containsKey(name)) {
      counters.get(name).dec(amount);
    }
  }

  @Override
  public void addMeter(String name) {
    if (!meters.containsKey(name)) {
      meters.put(name, registry.meter(name));
    }
  }

  @Override
  public void removeMeter(String name) {
    if (meters.containsKey(name)) {
      meters.remove(name);
    }
  }

  @Override
  public void markMeter(String name) {
    if (meters.containsKey(name)) {
      meters.get(name).mark();
    }
  }

  @Override
  public void addHistogram(String name) {
    if (!histograms.containsKey(name)) {
      histograms.put(name, registry.histogram(name));
    }
  }

  @Override
  public void removeHistogram(String name) {
    if (histograms.containsKey(name)) {
      histograms.remove(name);
    }
  }

  @Override
  public void updateHistogram(String name, int amount) {
    if (histograms.containsKey(name)) {
      histograms.get(name).update(amount);
    }
  }
}
