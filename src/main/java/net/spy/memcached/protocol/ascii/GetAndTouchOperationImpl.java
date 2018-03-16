/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.ascii;

import net.spy.memcached.ops.GetAndTouchOperation;

/**
 * Implementation of the get and touch operation.
 */
public class GetAndTouchOperationImpl extends BaseGetOpImpl implements
    GetAndTouchOperation {

  public GetAndTouchOperationImpl(String c, int e,
      GetAndTouchOperation.Callback cb, String k) {
    super(c, e, cb, k);
  }

}
