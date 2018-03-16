/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * Callback that's invoked with the response of an operation.
 */
public interface OperationCallback {

  /**
   * Method invoked with the status when the operation is complete.
   *
   * @param status the result of the operation
   */
  void receivedStatus(OperationStatus status);

  /**
   * Called whenever an operation completes.
   */
  void complete();
}
