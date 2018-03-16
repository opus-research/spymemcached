/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.transcoders;

import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;

/**
 * Transcoder that serializes and unserializes longs.
 */
public final class IntegerTranscoder extends SpyObject implements
    Transcoder<Integer> {

  private static final int FLAGS = SerializingTranscoder.SPECIAL_INT;

  private final TranscoderUtils tu = new TranscoderUtils(true);

  public boolean asyncDecode(CachedData d) {
    return false;
  }

  public CachedData encode(java.lang.Integer l) {
    return new CachedData(FLAGS, tu.encodeInt(l), getMaxSize());
  }

  public Integer decode(CachedData d) {
    if (FLAGS == d.getFlags()) {
      return tu.decodeInt(d.getData());
    } else {
      return null;
    }
  }

  public int getMaxSize() {
    return CachedData.MAX_SIZE;
  }

}
