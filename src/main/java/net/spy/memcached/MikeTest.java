package net.spy.memcached;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class MikeTest {
  public static void main(String args[]) throws Exception {
    MikeClient client = new MikeClient(/*new BinaryConnectionFactory(),*/ Arrays.asList(new InetSocketAddress("localhost", 11211)));
    System.out.println("Set: " + client.set("key", 0, "value").getStatus().getMessage());
    CASValue<Object> v = client.getAndLock("key", 20);
    System.out.println(v.getCas());
    System.out.println("Set: " + client.set("key", 0, "bad").getStatus().getMessage());
    System.out.println("Item shouldn't be changed: " + (String)client.get("key"));
    System.out.println("Unlock: " + client.unlock("key", v.getCas()).getStatus().getMessage());
    System.out.println("Set: " + client.set("key", 0, "good").getStatus().getMessage());
    System.out.println("The final result: " + (String)client.get("key"));
    client.flush().get();
    client.shutdown();
  }
}
