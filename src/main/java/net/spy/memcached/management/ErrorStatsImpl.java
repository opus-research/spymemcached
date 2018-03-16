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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import net.spy.memcached.ops.ErrorCode;

/**
 * This is the implementation for the OperationStatusMX bean that handles
 * reporting to a JMX monitor. This bean handles information on the types
 * of errors that are being received by the application.
 */
public class ErrorStatsImpl extends NotificationBroadcasterSupport
  implements ErrorStatsMXBean {

  public long getSuccessfulOps() {
    return Stats.ERROR_CODES.get(ErrorCode.SUCCESS).get();
  }

  public long getNotFoundErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_NOT_FOUND).get();
  }

  public long getExistsErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_EXISTS).get();
  }

  public long getInvalidErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_EXISTS).get();
  }

  public long getNotStoredErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_NOT_STORED).get();
  }

  public long getBadDeltaErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_DELTA_BADVAL).get();
  }

  public long getNotMyVbucketErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_NOT_MY_VBUCKET).get();
  }

  public long getAuthErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_AUTH_ERROR).get();
  }

  public long getAuthContinueErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_AUTH_CONTINUE).get();
  }

  public long getUnknownCommandErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_UNKNOWN_COMMAND).get();
  }

  public long getNoMemoryErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_NO_MEM).get();
  }

  public long getNotSupportedErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_NOT_SUPPORTED).get();
  }

  public long getInternalErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_INTERNAL).get();
  }

  public long getBusyErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_BUSY).get();
  }

  public long getTemporaryFailureErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.ERR_TEMP_FAIL).get();
  }

  public long getUnknownErrorErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.UNKNOWN_ERROR).get();
  }

  public long getCancelledErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.CANCELLED).get();
  }

  public long getTimoutErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.TIMED_OUT).get();
  }

  public long getExceptionErrors() {
    return Stats.ERROR_CODES.get(ErrorCode.EXCEPTION).get();
  }

  public void reset() {
    for (Map.Entry<ErrorCode, AtomicLong> ec: Stats.ERROR_CODES.entrySet()) {
      ec.getValue().set(0);
    }
    sendNotification(new Notification("ErrorCode", this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Reset error code stats"));
  }

  public void disable() {
    Stats.trackErrors = false;
    sendNotification(new AttributeChangeNotification(this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Error Tracking disabled", "track_errors", "boolean",
        Stats.trackErrors, false));
  }

  public void enable() {
    Stats.trackErrors = true;
    sendNotification(new AttributeChangeNotification(this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Error Tracking enabled", "track_errors", "boolean",
        Stats.trackErrors, true));
  }
}
