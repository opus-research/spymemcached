package net.spy.memcached;

import org.junit.Test;

/**
 * Tests the optional metric gathering.
 */
public class MetricsTest {

  @Test
  public void foo() throws Exception {
    ConnectionFactory cf = new BinaryConnectionFactory();
    MemcachedClient client = new MemcachedClient(cf, AddrUtil.getAddresses(TestConfig.IPV4_ADDR
      + ":" + TestConfig.PORT_NUMBER));

    while(true) {
      client.get("abc");
    }

    //client.shutdown();
  }

}
