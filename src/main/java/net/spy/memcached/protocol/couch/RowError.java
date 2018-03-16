/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

/**
 * Holds the information for row in a view result that
 * describes an error.
 */
public class RowError {
  private final String from;
  private final String reason;

  public RowError(String from, String reason) {
    this.from = from;
    this.reason = reason;
  }

  public String getFrom() {
    return from;
  }

  public String getReason() {
    return reason;
  }
}
