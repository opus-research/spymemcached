/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc
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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 *, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.CASOperation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.StoreOperation;
import net.spy.memcached.ops.StoreType;

class StoreOperationImpl extends SingleKeyOperationImpl implements
    StoreOperation, CASOperation {

  private static final int SET = 0x01;
  private static final int ADD = 0x02;
  private static final int REPLACE = 0x03;

  static final int SETQ = 0x11;
  static final int ADDQ = 0x12;
  static final int REPLACEQ = 0x13;

  // 4-byte flags, 4-byte expiration
  static final int EXTRA_LEN = 8;

  private final StoreType storeType;
  private final int flags;
  private final int exp;
  private final long cas;
  private final byte[] data;

  private static int cmdMap(StoreType t) {
    int rv;
    switch (t) {
    case set:
      rv = SET;
      break;
    case add:
      rv = ADD;
      break;
    case replace:
      rv = REPLACE;
      break;
    default:
      rv = -1;
    }
    // Check fall-through.
    assert rv != -1 : "Unhandled store type:  " + t;
    return rv;
  }

  public StoreOperationImpl(StoreType t, String k, int f, int e, byte[] d,
      long c, OperationCallback cb) {
    super(cmdMap(t), generateOpaque(), k, cb);
    flags = f;
    exp = e;
    data = d;
    cas = c;
    storeType = t;
  }

  @Override
  public void initialize() {
    prepareBuffer(key, cas, data, flags, exp);
  }

  public byte[] getBytes() {
    return data;
  }

  public long getCasValue() {
    return cas;
  }

  public int getExpiration() {
    return exp;
  }

  public int getFlags() {
    return flags;
  }

  public byte[] getData() {
    return data;
  }

  public StoreType getStoreType() {
    return storeType;
  }
}
