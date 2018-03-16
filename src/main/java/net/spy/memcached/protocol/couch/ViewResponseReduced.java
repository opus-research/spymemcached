/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import java.util.Collection;
import java.util.Iterator;

/**
 * Holds the response of a view query where the map and reduce
 * function were called.
 */
public class ViewResponseReduced implements ViewResponse<RowReduced> {

  private final Collection<RowReduced> rows;
  private final Collection<RowError> errors;

  public ViewResponseReduced(final Collection<RowReduced> r,
      final Collection<RowError> e) {
    rows = r;
    errors = e;
  }

  public Collection<RowError> getErrors() {
    return errors;
  }

  public int size() {
    return rows.size();
  }

  @Override
  public Iterator<RowReduced> iterator() {
    return rows.iterator();
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (RowReduced r : rows) {
      s.append(r.getKey() + " : " + r.getValue() + "\n");
    }
    return s.toString();
  }
}
