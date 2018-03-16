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
public final class LongTranscoder extends SpyObject
  implements Transcoder<Long> {

  private static final int FLAGS = SerializingTranscoder.SPECIAL_LONG;

  private final TranscoderUtils tu = new TranscoderUtils(true);

  public boolean asyncDecode(CachedData d) {
    return false;
  }

  public CachedData encode(java.lang.Long l) {
    return new CachedData(FLAGS, tu.encodeLong(l), getMaxSize());
  }

  public Long decode(CachedData d) {
    if (FLAGS == d.getFlags()) {
      return tu.decodeLong(d.getData());
    } else {
      getLogger().error(
          "Unexpected flags for long:  " + d.getFlags() + " wanted " + FLAGS);
      return null;
    }
  }

  public int getMaxSize() {
    return CachedData.MAX_SIZE;
  }

}
