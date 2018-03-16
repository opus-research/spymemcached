/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

import java.util.List;

import net.spy.memcached.internal.HttpFuture;
import net.spy.memcached.internal.ViewFuture;
import net.spy.memcached.protocol.couch.ViewResponseNoDocs;
import net.spy.memcached.protocol.couch.Query;
import net.spy.memcached.protocol.couch.ViewResponseReduced;
import net.spy.memcached.protocol.couch.View;
import net.spy.memcached.protocol.couch.ViewResponseWithDocs;

/**
 * This interface is provided as a helper for testing clients of the
 * CouchbaseClient.
 */
public interface CouchbaseClientIF extends MembaseClientIF {

  // View Access
  HttpFuture<View> asyncGetView(final String designDocumentName,
      final String viewName);

  HttpFuture<List<View>> asyncGetViews(final String designDocumentName);

  View getView(final String designDocumentName, final String viewName);

  List<View> getViews(final String designDocumentName);

  // Query
  ViewFuture asyncQuery(View view, Query query);

  HttpFuture<ViewResponseNoDocs> asyncQueryAndExcludeDocs(View view,
      Query query);

  HttpFuture<ViewResponseReduced> asyncQueryAndReduce(View view,
      Query query);

  ViewResponseWithDocs query(View view, Query query);

  ViewResponseNoDocs queryAndExcludeDocs(View view, Query query);

  ViewResponseReduced queryAndReduce(View view, Query query);
}
