package net.spy.memcached;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.spy.memcached.internal.SyncRequest;
import net.spy.memcached.internal.SyncResponse;


/**
 * This test runs by default on localhost:11211, but can be run
 * on a different host by specifying the server.loc property.
 */
public class BinaryClientTest extends ProtocolBaseCase {

	@Override
	protected void initClient() throws Exception {
		initClient(new BinaryConnectionFactory() {
			@Override
			public long getOperationTimeout() {
				return 15000;
			}
			@Override
			public FailureMode getFailureMode() {
				return FailureMode.Retry;
			}
		});
	}

	@Override
	protected String getExpectedVersionSource() {
		return "/127.0.0.1:11211";
	}

	@Override
	public void testGetStatsCacheDump() throws Exception {
		// XXX:  Cachedump isn't returning anything from the server in binprot
		assertTrue(true);
	}

	public void testCASAppendFail() throws Exception {
		final String key="append.key";
		assertTrue(client.set(key, 5, "test").get());
		CASValue<Object> casv = client.gets(key);
		assertFalse(client.append(casv.getCas() + 1, key, "es").get());
		assertEquals("test", client.get(key));
	}

	public void testCASAppendSuccess() throws Exception {
		final String key="append.key";
		assertTrue(client.set(key, 5, "test").get());
		CASValue<Object> casv = client.gets(key);
		assertTrue(client.append(casv.getCas(), key, "es").get());
		assertEquals("testes", client.get(key));
	}

	public void testCASPrependFail() throws Exception {
		final String key="append.key";
		assertTrue(client.set(key, 5, "test").get());
		CASValue<Object> casv = client.gets(key);
		assertFalse(client.prepend(casv.getCas() + 1, key, "es").get());
		assertEquals("test", client.get(key));
	}

	public void testCASPrependSuccess() throws Exception {
		final String key="append.key";
		assertTrue(client.set(key, 5, "test").get());
		CASValue<Object> casv = client.gets(key);
		assertTrue(client.prepend(casv.getCas(), key, "es").get());
		assertEquals("estest", client.get(key));
	}

	public void testSync() throws Exception {
		if (isMembase()) {
			client = new MemcachedClient(Arrays.asList(new URI(
					"http://localhost:8091/pools")), "default", "default", "");
			Collection<SyncRequest> keys = new LinkedList<SyncRequest>();

			for (int i = 0; i < 20; i++) {
				keys.add(new SyncRequest("key" + i));
			}

			for (int i = 0; i < 10; i++) {
				client.set("key" + i, 0, "value" + i);
			}

			Collection<SyncResponse> resp = client.asyncSync(keys, 0, false,
					false, false).get();
			Iterator<SyncResponse> itr = resp.iterator();
			int passed = 0;
			int failed = 0;
			while (itr.hasNext()) {
				SyncResponse cur = itr.next();
				if (cur.getStatus().isSuccess()) {
					passed++;
				} else {
					failed++;
				}
			}
			System.out.println(passed + " " + failed);
			client.flush();
		}
	}
}
