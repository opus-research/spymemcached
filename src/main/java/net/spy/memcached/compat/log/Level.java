/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.compat.log;

/**
 * Levels for logging.
 */
public enum Level {

  /**
   * Debug level.
   */
  DEBUG,
  /**
   * Info level.
   */
  INFO,
  /**
   * Warning level.
   */
  WARN,
  /**
   * Error level.
   */
  ERROR,
  /**
   * Fatal level.
   */
  FATAL;

  /**
   * Get a string representation of this level.
   */
  @Override
  public String toString() {
    return ("{LogLevel:  " + name() + "}");
  }

}
