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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.spy.memcached.compat.SpyObject;

/**
 * A class that enables the user to enable or disable statistics through
 * the use of JMX MBeans.
 */
public class JMXMonitor extends SpyObject {

  private final MBeanServer mbs;
  private final String clientStatsName = "net.spy.memcached:type=Clients";
  private final String operationStatsName =
    "net.spy.memcached:type=Operations";
  private final String errorStatsName = "net.spy.memcached:type=Errors";
  private final ClientStatsImpl clientsmxbean;
  private final OperationStatsImpl operationsmxbean;
  private final ErrorStatsImpl errorsmxbean;

  public JMXMonitor() {
    mbs = ManagementFactory.getPlatformMBeanServer();
    clientsmxbean = new ClientStatsImpl();
    operationsmxbean = new OperationStatsImpl();
    errorsmxbean = new ErrorStatsImpl();
  }

  public boolean enableClientMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(clientStatsName);
      mbs.registerMBean(clientsmxbean, mxbeanName);
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }

  public boolean enableOperationMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(operationStatsName);
      mbs.registerMBean(operationsmxbean, mxbeanName);
      Stats.trackOps = true;
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }

  public boolean enableErrorMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(errorStatsName);
      mbs.registerMBean(errorsmxbean, mxbeanName);
      Stats.trackErrors = true;
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }

  public boolean disableClientMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(clientStatsName);
      mbs.unregisterMBean(mxbeanName);
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }

  public boolean disableOperationMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(operationStatsName);
      mbs.unregisterMBean(mxbeanName);
      Stats.trackOps = false;
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }

  public boolean disableErrorMonitoring() {
    try {
      ObjectName mxbeanName = new ObjectName(errorStatsName);
      mbs.unregisterMBean(mxbeanName);
      Stats.trackErrors = false;
    } catch (Exception e) {
      getLogger().error(e.getMessage());
      return false;
    }
    return true;
  }
}
