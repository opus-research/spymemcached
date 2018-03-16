package net.spy.memcached;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ZTest {
	private static final String VALUE = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final int MGETSIZE = 100;
	private static final int NUMKEYS = 1000;
	
	public static void main(String args[]) throws Exception {
		MemcachedClient client = new MemcachedClient(Arrays.asList(URI.create("http://10.2.1.11:8091/pools"), URI.create("http://10.2.1.67:8091/pools")), "default", "");
		for (int i = 0; i < NUMKEYS; i++) {
			client.set("key" + i, 0, VALUE).get().booleanValue();
		}

		long start = System.currentTimeMillis();

		while ((System.currentTimeMillis() - start) < (480 * 1000)) {
			Collection<String> keys = new ArrayList<String>(NUMKEYS);
			for (int i = 0; i < MGETSIZE; i++) {
				int keynum = (int)(Math.random() * NUMKEYS);
				keys.add("key" + keynum);
			}
			try {
			client.getBulk(keys);
			} catch (Exception e) {
				
			}
		}
		client.shutdown();
	}
}
