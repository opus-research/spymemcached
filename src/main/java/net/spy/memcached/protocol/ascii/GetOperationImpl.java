/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.ascii;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.spy.memcached.ops.GetOperation;

/**
 * Operation for retrieving data.
 */
class GetOperationImpl extends BaseGetOpImpl implements GetOperation {

  private static final String CMD = "get";

  public GetOperationImpl(String key, GetOperation.Callback c) {
    super(CMD, c, Collections.singleton(key));
  }

  public GetOperationImpl(Collection<String> k, GetOperation.Callback c) {
    super(CMD, c, new HashSet<String>(k));
  }
}
