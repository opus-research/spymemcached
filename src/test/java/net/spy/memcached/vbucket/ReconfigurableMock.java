/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket;

import net.spy.memcached.vbucket.config.Bucket;

/**
 * A ReconfigurableMock.
 */
public class ReconfigurableMock implements Reconfigurable {
  public void reconfigure(Bucket bucket) {
  }
}
