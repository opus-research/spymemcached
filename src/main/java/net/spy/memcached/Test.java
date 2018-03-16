package net.spy.memcached;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

import net.spy.memcached.transcoders.SerializingTranscoder;

public class Test {
	public static void main(String args[]) throws Exception {
		Random r=new Random();
		SerializingTranscoder st=new SerializingTranscoder(Integer.MAX_VALUE);

		st.setCompressionThreshold(Integer.MAX_VALUE);

		byte data[]=new byte[21*1024*1024];
		r.nextBytes(data);
		MemcachedClient client = new MemcachedClient(new BinaryConnectionFactory(), Arrays.asList(new InetSocketAddress("localhost", 11211)));
		if (client.set("bigassthing", 60, data, st).getStatus().isSuccess() != false){
			System.out.println("Didn't fail setting bigass thing.");
		} else {
			System.err.println("Successful failure setting bigassthing.  Ass size " + data.length + " bytes doesn't fit.");
		}

		// But I should still be able to do something.
		System.out.println(client.set("k", 5, "Blah").get().booleanValue());
		System.out.println(client.get("k"));
	}
}
