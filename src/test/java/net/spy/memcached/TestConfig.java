package net.spy.memcached;

public class TestConfig {
	public static final String TYPE_MEMCACHED = "memcached";
	public static final String TYPE_MEMBASE = "membase";
	public static final String TYPE_COUCHBASE = "couchbase";

	public static final String IPV4_ADDR = System.getProperty("server.address", "127.0.0.1");
	public static final String IPV6_ADDR = "::ffff:" + System.getProperty("server.address", "127.0.0.1");
	public static final String TYPE = System.getProperty("server.type", "memcached").toLowerCase();

	private TestConfig(){
		// Empty
	}

	public static final boolean isMemcached() {
		return TYPE.equals(TYPE_MEMCACHED);
	}
	
	public static final boolean isMembase() {
		return TYPE.equals(TYPE_MEMBASE);
	}
	
	public static final boolean isCouchbase() {
		return TYPE.equals(TYPE_COUCHBASE);
	}
}
