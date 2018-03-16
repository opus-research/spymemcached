package net.spy.memcached;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPerformance {
  private MemcachedClient memcache;

  public static void main(String[] args) throws Exception {
      new TestPerformance().run();
  }

  private void run() throws Exception {
    Properties systemProperties = System.getProperties();
    systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
    System.setProperties(systemProperties);

    Logger.getLogger("net.spy.memcached").setLevel(Level.ALL);
      memcache = new MemcachedClient(AddrUtil.getAddresses("localhost:11219"));

      long time1 = System.nanoTime();
      addLotsOfData();

      long time2 = System.nanoTime();
      System.out.println("Time to add data = " + ((time2 - time1) / 1000000.0) + "ms");

      memcache.waitForQueues(86400, TimeUnit.SECONDS);
      long time3 = System.nanoTime();
      System.out.println("Time for queues to drain = " + ((time3 - time2) / 1000000.0) + "ms");
      memcache.set(Integer.toString(0), 86400, "Hello this is a test " + 0);
      memcache.shutdown(2000, TimeUnit.MILLISECONDS);
  }

  private void addLotsOfData() {
      for (int i = 0; i < 25000; i++) {
          memcache.set(Integer.toString(i), 86400, "Hello this is a test " + i);
      }
  }
}
