package net.spy.memcached;

public class TestConfig {
	public static final String IPV4_ADDR = System.getProperty("server.address", "127.0.0.1");
	public static final String IPV6_ADDR = "::ffff:" + System.getProperty("server.address", "127.0.0.1");
}
