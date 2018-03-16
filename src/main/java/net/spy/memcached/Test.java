package net.spy.memcached;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import net.spy.memcached.tapmessage.ResponseMessage;

public class Test {
	public static void main(String args[]) throws Exception {
		TapClient tc = new TapClient(Arrays.asList(URI.create("http://10.2.1.58:8091/pools")), "default", "default", "");
		//MemcachedClient client = new MemcachedClient(new BinaryConnectionFactory(), Arrays.asList(new InetSocketAddress("10.2.1.58", 11211)));
		MemcachedClient client = new MemcachedClient(Arrays.asList(URI.create("http://10.2.1.58:8091/pools")), "default", "default", "");
		HashMap<String, Boolean> items = new HashMap<String, Boolean>();
		for (int i = 0; i < 25; i++) {
			//client.set("key" + i, 0, "value" + i).get();
			items.put("key" + i + ",value" + i, new Boolean(false));
		}

		tc.tapDump(null);

		int count = 0;
		while(tc.hasMoreMessages()) {
			ResponseMessage m;
			if ((m = tc.getNextMessage()) != null) {
				String key = m.getKey() + "," + new String(m.getValue());
				if (items.containsKey(key)) {
					items.put(key, new Boolean(true));
					count++;
					System.out.println(key + " " + count);
					
				} else {
					System.exit(1);
				}
			}
		}
	}
}
