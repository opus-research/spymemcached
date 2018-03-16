/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

/**
 * A CancelFailureModeTest.
 */
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A CancellationFailureModeTest.
 */
public class CancelFailureModeTest extends ClientBaseCase {
  private String serverList;

  @Override
  protected void setUp() throws Exception {
    serverList =
        TestConfig.IPV4_ADDR + ":11211 " + TestConfig.IPV4_ADDR + ":11311";
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    serverList = TestConfig.IPV4_ADDR + ":11211";
    super.tearDown();
  }

  @Override
  protected void initClient(ConnectionFactory cf) throws Exception {
    client = new MemcachedClient(cf, AddrUtil.getAddresses(serverList));
  }

  @Override
  protected void initClient() throws Exception {
    initClient(new DefaultConnectionFactory() {
      @Override
      public FailureMode getFailureMode() {
        return FailureMode.Cancel;
      }
    });
  }

  @Override
  protected void flushPause() throws InterruptedException {
    Thread.sleep(100);
  }

  public void testQueueingToDownServer() throws Exception {
    Future<Boolean> f = client.add("someKey", 0, "some object");
    try {
      boolean b = f.get();
      fail("Should've thrown an exception, returned " + b);
    } catch (ExecutionException e) {
      // probably OK
    }
    assertTrue(f.isCancelled());
  }
}
