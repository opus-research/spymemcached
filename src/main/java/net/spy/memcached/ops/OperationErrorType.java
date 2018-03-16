/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * Error classification.
 */
public enum OperationErrorType {
  /**
   * General error.
   */
  GENERAL,
  /**
   * Error that occurred because the client did something stupid.
   */
  CLIENT,
  /**
   * Error that occurred because the server did something stupid.
   */
  SERVER;
}
