/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

/**
 * An enum containing the two possible values for the stale
 * parameter.
 */
public enum Stale {
  OK {
    public String toString() {
      return "ok";
    }
  },

  UPDATE_AFTER {
    public String toString() {
      return "update_after";
    }
  }
}
