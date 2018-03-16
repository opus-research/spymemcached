package net.spy.memcached;

import java.util.concurrent.TimeUnit;

import net.spy.memcached.internal.GetFuture;


/**
 * This test assumes a binary server is running on localhost:11211.
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

	public void testGATTimeout() throws Exception {
		if (isMembase()) {
			assertNull(client.get("gatkey"));
			assert client.set("gatkey", 1, "gatvalue").get().booleanValue();
			assert client.getAndTouch("gatkey", 2).getValue().equals("gatvalue");
			Thread.sleep(1300);
			assert client.get("gatkey").equals("gatvalue");
			Thread.sleep(2000);
			assertNull(client.getAndTouch("gatkey", 3));
		}
	}

	public void testTouchTimeout() throws Exception {
		if (isMembase()) {
			assertNull(client.get("touchkey"));
			assert client.set("touchkey", 1, "touchvalue").get().booleanValue();
			assert client.touch("touchkey", 2).get().booleanValue();
			Thread.sleep(1300);
			assert client.get("touchkey").equals("touchvalue");
			Thread.sleep(2000);
			assertFalse(client.touch("touchkey", 3).get().booleanValue());
		}
	}
	
	@Override
	public void testSyncGetTimeouts() throws Exception {
		final String key="timeoutTestKey";
		final String value="timeoutTestValue";
		// Shutting down the default client to get one with a short timeout.
		assertTrue("Couldn't shut down within five seconds",
			client.shutdown(5, TimeUnit.SECONDS));

		initClient(new BinaryConnectionFactory() {
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
}
