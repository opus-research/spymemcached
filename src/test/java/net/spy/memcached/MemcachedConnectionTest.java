/**
 * Copyright (C) 2006-2009 Dustin Sallings
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached;

import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

/**
 * Test stuff that can be tested within a MemcachedConnection separately.
 */
public class MemcachedConnectionTest extends TestCase {

  public void testDebugBuffer() throws Exception {
    String input = "this is a test _";
    ByteBuffer bb = ByteBuffer.wrap(input.getBytes());
    String s = MemcachedConnection.dbgBuffer(bb, input.length());
    assertEquals("this is a test \\x5f", s);
  }
  
  /**
   * Verifies that every exception is handled in the IO thread
   * in a safe way and operation can continue no matter what.
   */
  public void testSafeExceptionHandling() throws IOException {
    MockDefaultConnectionFactory cf = new MockDefaultConnectionFactory();
	MemcachedClient client = new MemcachedClient(cf, AddrUtil.getAddresses(TestConfig.IPV4_ADDR
	           + ":" + TestConfig.PORT_NUMBER));
	 
	  
    client.set("key1", 0, 1);
    assertEquals(1, client.get("key1"));

    client.set("key2", 0, 2);
    assertEquals(2, client.get("key2"));

    // throws
    client.set("key3", 0, 3);
    assertEquals(3, client.get("key3"));
  
    client.set("key4", 0, 4);
    assertEquals(4, client.get("key4"));
  }
}
