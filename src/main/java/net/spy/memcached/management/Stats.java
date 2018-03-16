/**
 * Copyright (C) 2009-2012 Couchbase, Inc.
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

package net.spy.memcached.management;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.spy.memcached.ops.ErrorCode;
import net.spy.memcached.ops.OperationStatus;

/**
 * Contains statistics on client operations.
 */
public class Stats {

  protected static long sequenceNumber = 1;

  protected static boolean trackOps = false;
  protected static boolean trackErrors = false;

  //Operation stats
  protected static final AtomicLong TOTAL_OPS = new AtomicLong(0);
  protected static final AtomicLong ADD_OPS = new AtomicLong(0);
  protected static final AtomicLong APPEND_OPS = new AtomicLong(0);
  protected static final AtomicLong CAS_OPS = new AtomicLong(0);
  protected static final AtomicLong DECR_OPS = new AtomicLong(0);
  protected static final AtomicLong DELETE_OPS = new AtomicLong(0);
  protected static final AtomicLong GET_OPS = new AtomicLong(0);
  protected static final AtomicLong GETS_OPS = new AtomicLong(0);
  protected static final AtomicLong INCR_OPS = new AtomicLong(0);
  protected static final AtomicLong PREPEND_OPS = new AtomicLong(0);
  protected static final AtomicLong SET_OPS = new AtomicLong(0);
  protected static final AtomicLong STATS_OPS = new AtomicLong(0);
  protected static final AtomicLong REPLACE_OPS = new AtomicLong(0);

  // Error stats
  protected static final Map<ErrorCode, AtomicLong> ERROR_CODES =
    new HashMap<ErrorCode, AtomicLong>();

  static {
    // Defining all of these here makes our hashmap is thread-safe since
    // we will never add another record
    ERROR_CODES.put(ErrorCode.SUCCESS, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_NOT_FOUND, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_EXISTS, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_INVAL, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_NOT_STORED, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_DELTA_BADVAL, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_NOT_MY_VBUCKET, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_AUTH_ERROR, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_AUTH_CONTINUE, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_UNKNOWN_COMMAND, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_NO_MEM, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_NOT_SUPPORTED, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_INTERNAL, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_BUSY, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.ERR_TEMP_FAIL, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.UNKNOWN_ERROR, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.CANCELLED, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.TIMED_OUT, new AtomicLong(0));
    ERROR_CODES.put(ErrorCode.EXCEPTION, new AtomicLong(0));
  }

  // Connection/Threading Stats
  protected static final AtomicLong CLIENTS = new AtomicLong(0);
  protected static final AtomicLong CONNS = new AtomicLong(0);

  public static void incrTotalClients() {
    CLIENTS.incrementAndGet();
  }

  public static void decrTotalClients() {
    CLIENTS.decrementAndGet();
  }

  public static void incrTotalConnections() {
    CONNS.incrementAndGet();
  }

  public static void decrTotalConnections() {
    CONNS.decrementAndGet();
  }

  public static void incrTotalAdd(OperationStatus status) {
    if (trackOps) {
      ADD_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalAppend(OperationStatus status) {
    if (trackOps) {
      APPEND_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalCas(OperationStatus status) {
    if (trackOps) {
      CAS_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalDecr(OperationStatus status) {
    if (trackOps) {
      DECR_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalDelete(OperationStatus status) {
    if (trackOps) {
      DELETE_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalGet(OperationStatus status) {
    if (trackOps) {
      GET_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalGets(OperationStatus status) {
    if (trackOps) {
      GETS_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalIncr(OperationStatus status) {
    if (trackOps) {
      INCR_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalPrepend(OperationStatus status) {
    if (trackOps) {
      PREPEND_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalSet(OperationStatus status) {
    if (trackOps) {
      SET_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalStats(OperationStatus status) {
    if (trackOps) {
      STATS_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public static void incrTotalReplace(OperationStatus status) {
    if (trackOps) {
      REPLACE_OPS.incrementAndGet();
      TOTAL_OPS.incrementAndGet();
    }

    processError(status.getErrorCode());
  }

  public void reset() {
    if (trackOps) {
      throw new IllegalStateException("Cannot reset stats while stats tracking"
          + "is enabled");
    }
    // Reset everything to 0
  }

  private static void processError(ErrorCode e) {
    if (trackErrors) {
      ERROR_CODES.get(e).incrementAndGet();
    }
  }
}
