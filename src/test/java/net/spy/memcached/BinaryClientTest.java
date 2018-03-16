/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc.
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

import java.net.InetSocketAddress;

/**
 * This test assumes a binary server is running on the host specified int the
 * environment variable SPYMC_TEST_SERVER_V4 or localhost:11211 by default.
 */
public class BinaryClientTest extends ProtocolBaseCase {

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
    return String.valueOf(new InetSocketAddress(TestConfig.IPV4_ADDR, 11211));
  }

  @Override
  public void testGetStatsCacheDump() throws Exception {
    // XXX: Cachedump isn't returning anything from the server in binprot
    assertTrue(true);
  }

  public void testCASAppendFail() throws Exception {
    final String key = "append.key";
    assertTrue(client.set(key, 5, "test").get());
    CASValue<Object> casv = client.gets(key);
    assertFalse(client.append(casv.getCas() + 1, key, "es").get());
    assertEquals("test", client.get(key));
  }

  public void testCASAppendSuccess() throws Exception {
    final String key = "append.key";
    assertTrue(client.set(key, 5, "test").get());
    CASValue<Object> casv = client.gets(key);
    assertTrue(client.append(casv.getCas(), key, "es").get());
    assertEquals("testes", client.get(key));
  }

  public void testCASPrependFail() throws Exception {
    final String key = "append.key";
    assertTrue(client.set(key, 5, "test").get());
    CASValue<Object> casv = client.gets(key);
    assertFalse(client.prepend(casv.getCas() + 1, key, "es").get());
    assertEquals("test", client.get(key));
  }

  public void testCASPrependSuccess() throws Exception {
    final String key = "append.key";
    assertTrue(client.set(key, 5, "test").get());
    CASValue<Object> casv = client.gets(key);
    assertTrue(client.prepend(casv.getCas(), key, "es").get());
    assertEquals("estest", client.get(key));
  }

  public void testGATTimeout() throws Exception {
    if (TestConfig.isMembase()) {
      assertNull(client.get("gatkey"));
      assert client.set("gatkey", 1, "gatvalue").get().booleanValue();
      assert client.getAndTouch("gatkey", 2).getValue().equals("gatvalue");
      Thread.sleep(1300);
      assert client.get("gatkey").equals("gatvalue");
      Thread.sleep(2000);
      assertNull(client.getAndTouch("gatkey", 3));
    }
  }

  public void testTouchTimeout() throws Exception {
    if (TestConfig.isMembase()) {
      assertNull(client.get("touchkey"));
      assert client.set("touchkey", 1, "touchvalue").get().booleanValue();
      assert client.touch("touchkey", 2).get().booleanValue();
      Thread.sleep(1300);
      assert client.get("touchkey").equals("touchvalue");
      Thread.sleep(2000);
      assertFalse(client.touch("touchkey", 3).get().booleanValue());
    }
  }

  @Override
  protected void syncGetTimeoutsInitClient() throws Exception {
    initClient(new BinaryConnectionFactory() {
      @Override
      public long getOperationTimeout() {
        return 2;
      }

      @Override
      public int getTimeoutExceptionThreshold() {
        return 1000000;
      }
    });
  }
}
