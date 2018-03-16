/**
 * Copyright (C) 2006-2009 Dustin Sallings
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

package net.spy.memcached.transcoders;

import net.spy.memcached.CachedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transcoder that serializes and unserializes longs.
 */
public final class LongTranscoder implements Transcoder<Long> {
  private static final Logger LOG =
    LoggerFactory.getLogger(LongTranscoder.class);

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
      LOG.error(
          "Unexpected flags for long:  " + d.getFlags() + " wanted " + FLAGS);
      return null;
    }
  }

  public int getMaxSize() {
    return CachedData.MAX_SIZE;
  }
}
