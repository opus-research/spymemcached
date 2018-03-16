/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * Operation status indicating an operation was cancelled.
 */
public class CancelledOperationStatus extends OperationStatus {

  public CancelledOperationStatus() {
    super(false, "cancelled");
  }
}
