package net.spy.memcached;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import net.spy.memcached.internal.OperationFuture;

public class Test {
  public static void main(String args[]) throws Exception {
    //MembaseClient client = new MembaseClient(Arrays.asList(URI.create("http://localhost:8091/pools")), "default", "");
    MemcachedClient client = new MemcachedClient(new InetSocketAddress("localhost", 9000));
    
    List<OperationFuture<Boolean>> futures = new LinkedList<OperationFuture<Boolean>>();
    for (int i = 0; i < 100; i++) {
      futures.add(client.set("key" + i, 0, "value"));
    }
    Thread.sleep(5000);
    futures.get(0).get().booleanValue();
    client.shutdown();
  }
}
