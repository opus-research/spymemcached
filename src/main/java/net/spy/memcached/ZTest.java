package net.spy.memcached;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.internal.HttpFuture;
import net.spy.memcached.protocol.couchdb.View;

public class ZTest {
	public static void main(String args[]) throws Exception {
		List<URI> base = new LinkedList<URI>();
		base.add(URI.create("http://localhost:8091/pools"));
		//base.add(URI.create("http://10.2.1.11:8091/pools"));
		MembaseClient c = new MembaseClient(base, "default", "");
		System.out.println(c.set("key", 0, "ppp").get(1, TimeUnit.MINUTES));
		/*while(true) {
			try {
				HttpFuture<View> v = c.asyncGetView("$dev_first", "all");
				System.out.println(v.getStatus().getMessage());
			} catch (Exception e) {
				System.out.println("Caught Exception: " + e.getMessage());
			}
			Thread.sleep(1000);
		}*/
	}
}
