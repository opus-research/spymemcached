package net.spy.memcached;

import java.net.URI;
import java.util.Arrays;

public class MembaseClientTest extends BinaryClientTest {
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
		return "localhost/127.0.0.1:11210";
	}
	
	@Override
	protected void initClient(ConnectionFactory cf) throws Exception{
		client=new MembaseClient(cf, Arrays.asList(URI.create("http://localhost:8091/pools")),
				"default","default", "");
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
}
