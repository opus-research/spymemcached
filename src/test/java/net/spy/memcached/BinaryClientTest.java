package net.spy.memcached;


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
			assert client.set("gatkey", 2, "gatvalue").get().booleanValue();
			Thread.sleep(1000);
			assert client.getAndTouch("gatkey", 3).equals("gatvalue");
			Thread.sleep(2000);
			assert client.get("gatkey").equals("gatvalue");
			Thread.sleep(1100);
			assertFalse(client.getAndTouch("gatkey", 3).equals("gatvalue"));
		}
	}
}
