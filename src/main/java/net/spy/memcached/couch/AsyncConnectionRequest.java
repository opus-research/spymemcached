/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.couch;

import org.apache.http.nio.NHttpClientConnection;

/**
 * An AsyncConnectionRequest.
 */
public class AsyncConnectionRequest {

  private volatile boolean completed;
  private volatile NHttpClientConnection conn;

  public AsyncConnectionRequest() {
    super();
  }

  public boolean isCompleted() {
    return this.completed;
  }

  public void setConnection(NHttpClientConnection newConn) {
    if (this.completed) {
      return;
    }
    this.completed = true;
    synchronized (this) {
      this.conn = newConn;
      notifyAll();
    }
  }

  public NHttpClientConnection getConnection() {
    return this.conn;
  }

  public void cancel() {
    if (this.completed) {
      return;
    }
    this.completed = true;
    synchronized (this) {
      notifyAll();
    }
  }

  public void waitFor() throws InterruptedException {
    if (this.completed) {
      return;
    }
    synchronized (this) {
      while (!this.completed) {
        wait();
      }
    }
  }

}
