package net.spy.memcached;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;

public class Temp {
	public static void main(String args[]) throws Exception {
		/*MemcachedClient client = new MemcachedClient(//new BinaryConnectionFactory(),
				Arrays.asList(new InetSocketAddress("localhost", 11211)));
		System.out.println(client.set("key", 0, "value").get().booleanValue());
		System.out.println(client.get("key"));
		System.out.println(client.getAndLock("key", 2).getValue());
		System.out.println(client.set("key", 0, "value").get().booleanValue());
		Thread.sleep(3000);
		System.out.println(client.set("key", 0, "value").get().booleanValue());
		client.shutdown();
		*/
		MemcachedClient client = new MemcachedClient(Arrays.asList(
				new URI("http://10.2.1.58:8091/pools")), "default", "default", "");
		//client.asyncGet
	}
}
