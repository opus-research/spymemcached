package net.spy.memcached.couch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import net.spy.memcached.CouchbaseClient;
import net.spy.memcached.TestConfig;
import net.spy.memcached.couch.CouchbaseClientTest.PutOperation.PutCallback;
import net.spy.memcached.internal.HttpFuture;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.protocol.couchdb.HttpOperationImpl;
import net.spy.memcached.protocol.couchdb.RowWithDocs;
import net.spy.memcached.protocol.couchdb.ViewResponseNoDocs;
import net.spy.memcached.protocol.couchdb.Query;
import net.spy.memcached.protocol.couchdb.ViewResponseReduced;
import net.spy.memcached.protocol.couchdb.RowReduced;
import net.spy.memcached.protocol.couchdb.RowNoDocs;
import net.spy.memcached.protocol.couchdb.View;
import net.spy.memcached.protocol.couchdb.ViewResponseWithDocs;
import net.spy.memcached.vbucket.ConfigurationException;

import junit.framework.TestCase;

public class CouchbaseClientTest extends TestCase {
	protected TestingClient client = null;
	private static final String SERVER_URI = "http://" + TestConfig.IPV4_ADDR
			+ "/pools";
	private static final Map<String, Object> items;
	public static final String DESIGN_DOC_W_REDUCE = "doc_with_view";
	public static final String DESIGN_DOC_WO_REDUCE = "doc_without_view";
	public static final String VIEW_NAME_W_REDUCE = "view_with_reduce";
	public static final String VIEW_NAME_WO_REDUCE = "view_without_reduce";

	static {
		items = new HashMap<String, Object>();
		int d = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++, d++) {
					String type = new String(new char[] { 'a' + 26 });
					String small = (new Integer(j)).toString();
					String large = (new Integer(k)).toString();
					String doc = generateDoc(type, small, large);
					items.put("key" + d, doc);
				}
			}
		}
	}

	protected void initClient() throws Exception {
		List<URI> uris = new LinkedList<URI>();
		uris.add(URI.create(SERVER_URI));
		client = new TestingClient(uris, "default", "");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initClient();
		String docUri = "/default/_design/" + DESIGN_DOC_W_REDUCE;
		String view = "{\"language\":\"javascript\",\"views\":{\""
				+ VIEW_NAME_W_REDUCE + "\":{\"map\":\"function (doc) {  "
				+ "emit(doc._id, 1)}\",\"reduce\":\"_sum\" }}}";
		client.asyncHttpPut(docUri, view);

		docUri = "/default/_design/" + DESIGN_DOC_WO_REDUCE;
		view = "{\"language\":\"javascript\",\"views\":{\""
				+ VIEW_NAME_WO_REDUCE + "\":{\"map\":\"function (doc) {  "
				+ "emit(doc._id, 1)}\"}}}";
		for (Entry<String, Object> item : items.entrySet()) {
			client.set(item.getKey(), 0, (String) item.getValue()).get();
		}
		client.asyncHttpPut(docUri, view);
		Thread.sleep(1000);
	}

	@Override
	protected void tearDown() throws Exception {
		// Shut down, start up, flush, and shut down again. Error tests have
		// unpredictable timing issues.
		client.shutdown();
		client = null;
		initClient();
		assertTrue(client.flush().get());
		client.shutdown();
		client = null;
		super.tearDown();
	}

	private static String generateDoc(String type, String small, String large) {
		return "{\"type\":\"" + type + "\"" + "\"small range\":\"" + small
				+ "\"" + "\"large range\":\"" + large + "\"}";
	}

	public void testQueryWithDocs() throws Exception {
		Query query = new Query();
		View view = client.getView(DESIGN_DOC_W_REDUCE, VIEW_NAME_W_REDUCE);
		ViewResponseWithDocs response = client.query(view, query).get();

		Iterator<RowWithDocs> itr = response.iterator();
		while (itr.hasNext()) {
			RowWithDocs row = itr.next();
			if (items.containsKey(row.getId())) {
				assert items.get(row.getId()).equals(row.getDoc());
			}
		}
		assert items.size() == response.size();
	}

	public void testViewNoDocs() throws Exception {
		Query query = new Query();
		View view = client.getView(DESIGN_DOC_W_REDUCE, VIEW_NAME_W_REDUCE);
		HttpFuture<ViewResponseNoDocs> future = client.queryAndExcludeDocs(
				view, query);
		ViewResponseNoDocs response = future.get();

		Iterator<RowNoDocs> itr = response.iterator();
		while (itr.hasNext()) {
			RowNoDocs row = itr.next();
			if (!items.containsKey(row.getId())) {
				fail("Got an item that I shouldn't have gotten.");
			}
		}
		assert response.size() == items.size() : "Failed: "
				+ future.getStatus().getMessage();
	}

	public void testReduce() throws Exception {
		Query query = new Query();
		View view = client.getView(DESIGN_DOC_W_REDUCE, VIEW_NAME_W_REDUCE);
		HttpFuture<ViewResponseReduced> future = client.queryAndReduce(view,
				query);
		ViewResponseReduced reduce = future.get();

		Iterator<RowReduced> itr = reduce.iterator();
		while (itr.hasNext()) {
			RowReduced row = itr.next();
			assert row.getKey() == null;
			assert Integer.valueOf(row.getValue()) == items.size() : "Failed: "
					+ future.getStatus().getMessage();
		}
	}

	public void testReduceWhenNoneExists() throws Exception {
		Query query = new Query();
		View view = client.getView(DESIGN_DOC_WO_REDUCE, VIEW_NAME_WO_REDUCE);
		try {
			client.queryAndReduce(view, query);
		} catch (RuntimeException e) {
			return; // Pass, no reduce exists.
		}
		fail("No view exists and this query still happened");
	}

	class TestingClient extends CouchbaseClient {

		public TestingClient(List<URI> baseList, String bucketName, String pwd)
				throws IOException, ConfigurationException {
			super(baseList, bucketName, pwd);
		}

		public HttpFuture<String> asyncHttpPut(String uri, String document)
				throws UnsupportedEncodingException {
			final CountDownLatch couchLatch = new CountDownLatch(1);
			final HttpFuture<String> crv = new HttpFuture<String>(couchLatch,
					operationTimeout);

			HttpRequest request = new BasicHttpEntityEnclosingRequest("PUT",
					uri, HttpVersion.HTTP_1_1);
			StringEntity entity = new StringEntity(document);
			((BasicHttpEntityEnclosingRequest) request).setEntity(entity);
			HttpOperationImpl op = new PutOperationImpl(request,
					new PutCallback() {
						String json;

						@Override
						public void receivedStatus(OperationStatus status) {
							crv.set(json, status);
						}

						@Override
						public void complete() {
							couchLatch.countDown();
						}

						@Override
						public void getData(String response) {
							json = response;
						}
					});
			crv.setOperation(op);
			addOp(op);
			return crv;
		}

	}

	interface PutOperation {
		interface PutCallback extends OperationCallback {
			void getData(String response);
		}
	}

	class PutOperationImpl extends HttpOperationImpl implements PutOperation {

		public PutOperationImpl(HttpRequest r, PutCallback cb) {
			super(r, cb);
		}

		@Override
		public void handleResponse(HttpResponse response) {
			String json = getEntityString(response);
			int errorcode = response.getStatusLine().getStatusCode();
			if (errorcode == HttpURLConnection.HTTP_OK) {
				((PutCallback) callback).getData(json);
				callback.receivedStatus(new OperationStatus(true, "OK"));
			} else {
				callback.receivedStatus(new OperationStatus(false, Integer
						.toString(errorcode)));
			}
			callback.complete();
		}

	}
}
