/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

/**
 * Thrown by {@link MemcachedClient} when any internal operations timeout.
 *
 * @author Ray Krueger
 * @see net.spy.memcached.ConnectionFactory#getOperationTimeout()
 */
public class OperationTimeoutException extends RuntimeException {

  private static final long serialVersionUID = 1479557202445843619L;

  public OperationTimeoutException(String message) {
    super(message);
  }

  public OperationTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
