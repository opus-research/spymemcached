package net.spy.memcached;

/**
 * Test the test protocol over IPv6.
 */
public class AsciiIPV6ClientTest extends AsciiClientTest {

	@Override
	protected void initClient(ConnectionFactory cf) throws Exception {
		client=new MemcachedClient(cf,
			AddrUtil.getAddresses(TestConfig.IPV6_ADDR + ":11211"));
	}

	@Override
	protected String getExpectedVersionSource() {
		// If no ipv6 address is given and we're not using localhost then we default to ipv4
		if (TestConfig.IPV4_ADDR.equals(System.getProperty(TestConfig.IPV6_PROP, "::1"))) {
			return "/" + TestConfig.IPV4_ADDR + ":11211";
		}
		return "/" + TestConfig.IPV6_ADDR + ":11211";
	}

}
