/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

/**
 * Holds a row in a view result that contains the fields
 * id, key, value, and doc.
 */
public class RowWithDocs extends RowNoDocs {
  private Object doc;

  public RowWithDocs(String id, String key, String value, Object doc) {
    super(id, key, value);
    // The doc can be the string "null" so convert it to null
    if (doc != null && doc.equals("null")) {
      this.doc = null;
    } else {
      this.doc = doc;
    }
  }

  public Object getDoc() {
    return doc;
  }
}
