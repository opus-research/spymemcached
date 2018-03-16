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
		if (TestConfig.defaultToIPV4()) {
			return "/" + TestConfig.IPV4_ADDR + ":11211";
		}
		return "/" + TestConfig.IPV6_ADDR + ":11211";
	}

}
