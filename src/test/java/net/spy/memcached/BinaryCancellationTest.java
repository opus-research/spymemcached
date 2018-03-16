/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

/**
 * Test cancellation in the binary protocol.
 */
public class BinaryCancellationTest extends CancellationBaseCase {

  @Override
  protected void initClient() throws Exception {
    initClient(new BinaryConnectionFactory() {
      @Override
      public FailureMode getFailureMode() {
        return FailureMode.Retry;
      }
    });
  }

}
