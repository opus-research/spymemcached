/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import net.spy.memcached.ops.GetOperation;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;
import junit.framework.TestCase;

/**
 * An ErrorCodeTest.
 */
public class ErrorCodeTest extends TestCase {

  public void testErrorCodes() throws Exception {
    HashMap<Byte, String> errMap = new HashMap<Byte, String>();
    OperationFactory opFact = new BinaryOperationFactory();

    errMap.put(new Byte((byte) 0x01), "NOT FOUND");
    errMap.put(new Byte((byte) 0x02), "EXISTS");
    errMap.put(new Byte((byte) 0x03), "2BIG");
    errMap.put(new Byte((byte) 0x04), "INVAL");
    errMap.put(new Byte((byte) 0x05), "NOT STORED");
    errMap.put(new Byte((byte) 0x06), "DELTA BAD VAL");
    errMap.put(new Byte((byte) 0x07), "NOT MY VBUCKET");
    errMap.put(new Byte((byte) 0x81), "UNKNOWN COMMAND");
    errMap.put(new Byte((byte) 0x82), "NO MEM");
    errMap.put(new Byte((byte) 0x83), "NOT SUPPORTED");
    errMap.put(new Byte((byte) 0x84), "INTERNAL ERROR");
    errMap.put(new Byte((byte) 0x85), "BUSY");
    errMap.put(new Byte((byte) 0x86), "TEMP FAIL");

    int opaque = 0;
    for (final Entry<Byte, String> err : errMap.entrySet()) {
      byte[] b = new byte[24 + err.getValue().length()];
      b[0] = (byte) 0x81;
      b[7] = err.getKey();
      b[11] = (byte) err.getValue().length();
      b[15] = (byte) ++opaque;
      System.arraycopy(err.getValue().getBytes(), 0, b, 24, err.getValue()
          .length());

      GetOperation op = opFact.get("key", new GetOperation.Callback() {
        public void receivedStatus(OperationStatus s) {
          assert !s.isSuccess();
          assert err.getValue().equals(s.getMessage());
        }

        public void gotData(String k, int flags, byte[] data) {

        }

        public void complete() {
        }
      });
      ByteBuffer bb = ByteBuffer.wrap(b);
      bb.flip();
      op.readFromBuffer(bb);
    }
  }
}
