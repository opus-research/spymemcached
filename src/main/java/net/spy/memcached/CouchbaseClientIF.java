package net.spy.memcached;

import java.util.List;

import net.spy.memcached.internal.HttpFuture;
import net.spy.memcached.protocol.couch.ViewResponse;
import net.spy.memcached.protocol.couch.Query;
import net.spy.memcached.protocol.couch.View;

public interface CouchbaseClientIF {

	// View Access
	public HttpFuture<View> asyncGetView(final String designDocumentName, final String viewName);

	public HttpFuture<List<View>> asyncGetViews(final String designDocumentName);

	public View getView(final String designDocumentName, final String viewName);

	public List<View> getViews(final String designDocumentName);

	// Query
	public HttpFuture<ViewResponse> asyncQuery(View view, Query query);

	public HttpFuture<ViewResponse> asyncQueryAndExcludeDocs(View view, Query query);

	public HttpFuture<ViewResponse> asyncQueryAndReduce(View view, Query query);

	public ViewResponse query(View view, Query query);

	public ViewResponse queryAndExcludeDocs(View view, Query query);

	public ViewResponse queryAndReduce(View view, Query query);
}
