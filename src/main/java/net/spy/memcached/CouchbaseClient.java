package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;

import net.spy.memcached.internal.HttpFuture;
import net.spy.memcached.protocol.couchdb.DocParserUtils;
import net.spy.memcached.protocol.couchdb.HttpCallback;
import net.spy.memcached.protocol.couchdb.HttpOperation;
import net.spy.memcached.protocol.couchdb.View;
import net.spy.memcached.vbucket.ConfigurationException;

public class CouchbaseClient extends MembaseClient {
	
	private CouchbaseConnection cconn;
	private final String bucketName;

	public CouchbaseClient(List<URI> baseList, String bucketName, String pwd)
			throws IOException, ConfigurationException {
		this(baseList, bucketName, bucketName, pwd);
	}
	
	public CouchbaseClient(List<URI> baseList, String bucketName, String usr, String pwd)
			throws IOException, ConfigurationException {
		super(new CouchbaseConnectionFactory(baseList, bucketName, usr, pwd));
		this.bucketName = bucketName;
		CouchbaseConnectionFactory cf = (CouchbaseConnectionFactory)connFactory;
		List<InetSocketAddress> addrs = AddrUtil.getAddresses(cf.getVBucketConfig().getServers());
		
		//-- TODO: Hack! NS_Server sends us addrs like ip:11210, but we want ip:5984.
		List<InetSocketAddress> conv = new LinkedList<InetSocketAddress>();
		while (!addrs.isEmpty()) { conv.add(addrs.remove(0)); }
		while (!conv.isEmpty()) { addrs.add(new InetSocketAddress(conv.remove(0).getHostName(), 5984)); }
		//-- End Hack --
		
		cconn = cf.createCouchDBConnection(addrs);
	}
	
	public View getView(String designDocumentName, String viewName)
			throws InterruptedException, ExecutionException {
		Collection<View> views = getViews(designDocumentName);
		for (View v : views) {
			if (v.getViewName().equals(viewName)) {
				return v;
			}
		}
		getLogger().warn("No view for design document " + designDocumentName + " was found");
		return null;
	}
	
	public List<View> getViews(String ddn)
			throws InterruptedException, ExecutionException {
		String uri = "/" + bucketName + "/_design/" + ddn;
		String designDocJson = asyncHttpGet(uri).get();
		System.out.println(designDocJson);
		List<View> views;
		try {
			views = DocParserUtils.parseDesignDocumentForViews(bucketName, ddn, designDocJson);
		} catch (ParseException e) {
			getLogger().error(e.getMessage());
			return null;
		}
		return views;
	}

	private HttpFuture<String> asyncHttpGet(String uri) {
		final CountDownLatch couchLatch = new CountDownLatch(1);
		final HttpFuture<String> crv =
			new HttpFuture<String>(couchLatch, operationTimeout);
		
		HttpRequest request = new BasicHttpRequest("GET", uri, HttpVersion.HTTP_1_1);
		HttpOperation op = new HttpOperation(request, new HttpCallback() {
			@Override
			public void complete(HttpResponse response) {
				try {
					crv.set(EntityUtils.toString(response.getEntity()));
				} catch (org.apache.http.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				couchLatch.countDown();
			}
		});
		crv.setOperation(op);
		addOp(op);
		return crv;
	}
	
	public void addOp(final HttpOperation op) {
		cconn.checkState();
		cconn.addOp(op);
	}

	@Override
	public void shutdown() {
		shutdown(-1, TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean shutdown(long duration, TimeUnit units) {
		try {
			super.shutdown(duration, units);
			cconn.shutdown();
			return true;
		} catch (IOException e) {
			getLogger().error("Error shutting down CouchbaseClient");
			return false;
		}
	}
}
