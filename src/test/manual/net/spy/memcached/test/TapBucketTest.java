package net.spy.memcached.test;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.TapClient;
import net.spy.memcached.tapmessage.ResponseMessage;

/**
 * In order to successfully run this test you must create a Membase bucket
 * on your server named "bucket", with password "password". The bucket must
 * also be empty. Having keys not generated by this test in you bucket can
 * cause this test to fail.
 *
 * This test tests that a tap stream is capable of connecting to a named
 * bucket and functions properly. The test puts 500 items into the bucket
 * and then checks to see that they are all streamed out of the bucket by
 * using a tap backfill stream.
 */
public class TapBucketTest {

	public static void main(String[] args) throws Exception {
		boolean failed = false;

		MemcachedClient mc = new MemcachedClient(
				Arrays.asList(new URI("http://localhost:8091/pools")),
				"bucket", "bucket", "password");
		TapClient tc = new TapClient(
				Arrays.asList(new URI("http://localhost:8091/pools")),
				"bucket", "bucket", "password");
		tc.tapBackfill(null, 5, TimeUnit.SECONDS);

		HashMap<String, Boolean> items = new HashMap<String, Boolean>();
		for (int i = 0; i < 500; i++) {
			mc.set("key" + i, 0, "value" + i);
			items.put("key" + i + ",value" + i, new Boolean(false));
		}

		while(tc.hasMoreMessages()) {
			ResponseMessage m;
			if ((m = tc.getNextMessage()) != null) {
				String key = m.getKey() + "," + new String(m.getValue());
				if (items.containsKey(key)) {
					items.put(key, new Boolean(true));
				} else {
					System.err.println("Error - Item: " + key + " was found" +
							", but shoult not have been found");
					failed = true;
				}
			}
		}

		for (Entry<String, Boolean> kv : items.entrySet()) {
			if (!kv.getValue().booleanValue()) {
				System.err.println("Error - Item: " + kv.getKey() + " was not" +
						" sent by the tap stream");
				failed = true;
			}
		}
		mc.flush();
		mc.shutdown();
		tc.shutdown();

		if (failed) {
			System.err.println("\n\nTest Failed\n\n\n");
		} else {
			System.err.println("\n\nTest Passed\n\n\n");
		}
	}
}
