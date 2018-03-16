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

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 * This is the implementation for the OperationStatusMX bean that handles
 * reporting to a JMX monitor. This bean handles information on the types
 * of operations (get, set, ...) that are being done by this application.
 */
public class OperationStatsImpl extends NotificationBroadcasterSupport
    implements OperationStatsMXBean {

  public long getTotalOps() {
    return Stats.TOTAL_OPS.get();
  }

  public long getTotalAdd() {
    return Stats.ADD_OPS.get();
  }

  public long getTotalAppend() {
    return Stats.APPEND_OPS.get();
  }

  public long getTotalCas() {
    return Stats.CAS_OPS.get();
  }

  public long getTotalDecr() {
    return Stats.DECR_OPS.get();
  }

  public long getTotalDelete() {
    return Stats.DELETE_OPS.get();
  }

  public long getTotalGet() {
    return Stats.GET_OPS.get();
  }

  public long getTotalGets() {
    return Stats.GET_OPS.get();
  }

  public long getTotalIncr() {
    return Stats.INCR_OPS.get();
  }

  public long getTotalPrepend() {
    return Stats.GET_OPS.get();
  }

  public long getTotalSet() {
    return Stats.SET_OPS.get();
  }

  public long getTotalStats() {
    return Stats.STATS_OPS.get();
  }

  public long getTotalReplace() {
    return Stats.REPLACE_OPS.get();
  }

  public void reset() {
    Stats.TOTAL_OPS.get();
    Stats.ADD_OPS.get();
    Stats.APPEND_OPS.get();
    Stats.CAS_OPS.get();
    Stats.DECR_OPS.get();
    Stats.DELETE_OPS.get();
    Stats.GET_OPS.get();
    Stats.GET_OPS.get();
    Stats.INCR_OPS.get();
    Stats.GET_OPS.get();
    Stats.SET_OPS.get();
    Stats.STATS_OPS.get();
    Stats.REPLACE_OPS.get();

    sendNotification(new Notification("ErrorCode", this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Reset error code stats"));
  }

  public void disable() {
    Stats.trackErrors = false;
    sendNotification(new AttributeChangeNotification(this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Operation Tracking disabled", "track_operations", "boolean",
        Stats.trackOps, false));
  }

  public void enable() {
    Stats.trackErrors = true;
    sendNotification(new AttributeChangeNotification(this,
        Stats.sequenceNumber++, System.currentTimeMillis(),
        "Operation Tracking enabled", "track_operations", "boolean",
        Stats.trackOps, true));
  }
}
