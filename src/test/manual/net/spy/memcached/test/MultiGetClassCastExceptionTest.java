package net.spy.memcached.test;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;

/**
 * Tests to make sure that we don't get ClassCastException's when
 * doing multi-get's.
 *
 * Thanks to Ilkinulas for writing this test.
 */
public class MultiGetClassCastExceptionTest {

	private MemcachedClient client = null;

	public static void main(String[] args) throws Exception {
		MultiGetClassCastExceptionTest test = new MultiGetClassCastExceptionTest();
		test.initialize();
		int numberOfWorkers = 5;
		for (int i=0; i<numberOfWorkers; i++) {
			Worker worker = test.new Worker(test.getClient());
			worker.start();
		}
		Thread.sleep(3000);
		test.shutdown(30);
	}

	public MemcachedClient getClient() {
		return client;
	}

	private void initialize() throws Exception {
		InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 11311);
		List<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
		addrs.add(addr);
		this.client = new MemcachedClient(new BinaryConnectionFactory(),addrs);
//		URI server = new URI("http://localhost:8091/pools");
//		ArrayList<URI> servers = new ArrayList();
//		servers.add(server);
//		this.client = new MemcachedClient(servers, "default", "");
		Thread.sleep(1000);
		System.err.println("Memcached client ready...");
	}

	private void shutdown(int seconds) {
	    getClient().shutdown(seconds, TimeUnit.SECONDS);
	    System.err.println("Done.");
	}

	public class Worker extends Thread {
		MemcachedClient client = null;

		public Worker(MemcachedClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				doWork();
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void doWork() throws Exception {
			String key = getRandomKey();
			CASValue<Object> casValue = this.client.gets(key);
			if (casValue == null) {
				Data data = new Data();
				data.incr();
				Future<Boolean> addFuture = this.client.add(key, 24*60*60 , data);
				addFuture.get();
			} else {
				Data data = (Data)casValue.getValue();
				long cas = casValue.getCas();
				data.incr();
				this.client.cas(key, cas, data);
			}
		}

		private String getRandomKey() {
			Random random = new Random();
			return "KEY_" +random.nextInt(100000);
		}
	}

	public class Data implements Serializable {
		private static final long serialVersionUID = -5406442657418041232L;

		long value = 0;
		long casId = 0;

		public void incr(){
			this.value++;
		}

		public long getCasId() {
			return casId;
		}

		public void setCasId(long casId) {
			this.casId = casId;
		}
	}
}
