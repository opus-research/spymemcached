package net.spy.memcached;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.SyncRequest;
import net.spy.memcached.internal.SyncResponse;

public class MembaseClientTest extends BinaryClientTest {
	@Override
	protected void initClient() throws Exception {
		initClient(new MembaseConnectionFactory(Arrays.asList(URI.create("http://localhost:8091/pools")),
				"default","default", ""));
	}

	@Override
	protected String getExpectedVersionSource() {
		return "localhost/127.0.0.1:11210";
	}

	@Override
	protected void initClient(ConnectionFactory cf) throws Exception{
		client=new MembaseClient((MembaseConnectionFactory)cf);
	}

	@Override
	public void testAvailableServers() {
		// MembaseClient tracks hostname and ip address of servers need to
		// make sure the available server list is 2 * (num servers)
		try {
			Thread.sleep(10); // Let the client warm up
		} catch (InterruptedException e) {
		}
		assert client.getAvailableServers().size() == 2;
	}

	@Override
	public void testSyncGetTimeouts() throws Exception {
		final String key="timeoutTestKey";
		final String value="timeoutTestValue";
		// Shutting down the default client to get one with a short timeout.
		assertTrue("Couldn't shut down within five seconds",
			client.shutdown(5, TimeUnit.SECONDS));

		initClient(new MembaseConnectionFactory(Arrays.asList(URI.create("http://localhost:8091/pools")),
				"default","default", "") {
			@Override
			public long getOperationTimeout() {
				return 2;
			}

			@Override
			public int getTimeoutExceptionThreshold() {
				return 1000000;
			}
		});

		Thread.sleep(100); // allow connections to be established

		int j = 0;
		boolean set = false;
		do {
			set = client.set(key, 0, value).get();
			j++;
		} while (!set && j < 10);
		assert set == true;

		int i = 0;
		GetFuture<Object> g = null;
		try {
			for(i = 0; i < 1000000; i++) {
				g = client.asyncGet(key);
				g.get();
			}
			throw new Exception("Didn't get a timeout.");
		} catch(Exception e) {
			assert !g.getStatus().isSuccess();
			System.out.println("Got a timeout at iteration " + i + ".");
		}
		Thread.sleep(100); // let whatever caused the timeout to pass
		try {
			if (value.equals(client.asyncGet(key).get(30, TimeUnit.SECONDS))) {
			System.out.println("Got the right value.");
		} else {
			throw new Exception("Didn't get the expected value.");
		}
		} catch (java.util.concurrent.TimeoutException timeoutException) {
		        debugNodeInfo(client.getNodeLocator().getAll());
			throw new Exception("Unexpected timeout after 30 seconds waiting", timeoutException);
		}
	}

	public void testSync() throws Exception {
		if (isMembase()) {
			client = new MembaseClient(Arrays.asList(new URI(
					"http://localhost:8091/pools")), "default", "default", "");
			Collection<SyncRequest> keys = new LinkedList<SyncRequest>();

			for (int i = 0; i < 20; i++) {
				keys.add(new SyncRequest("key" + i));
			}

			for (int i = 0; i < 10; i++) {
				client.set("key" + i, 0, "value" + i);
			}

			Collection<SyncResponse> resp = ((MembaseClient)client).asyncSync(keys, 0, false,
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
