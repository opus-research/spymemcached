/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket;

import java.util.Observer;
import java.util.Observable;

/**
 * A BucketObserverMock.
 */
public class BucketObserverMock implements Observer {
  private boolean updateCalled = false;

  public void update(Observable o, Object arg) {
    updateCalled = true;
  }

  public boolean isUpdateCalled() {
    return updateCalled;
  }
}
