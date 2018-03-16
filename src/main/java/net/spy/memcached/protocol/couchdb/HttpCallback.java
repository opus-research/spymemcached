package net.spy.memcached.protocol.couchdb;

import org.apache.http.HttpResponse;

public interface HttpCallback {
	public void complete(HttpResponse response);
}
