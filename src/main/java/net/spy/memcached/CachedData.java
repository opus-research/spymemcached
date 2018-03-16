/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached;

import java.util.Arrays;

/**
 * Cached data with its attributes.
 */
public final class CachedData {

  /**
   * The maximum size that should be considered storing in a server.
   */
  /*
   * though memcached no longer has a maximum size, rather than remove this
   * entirely just bump it up for now
   */
  public static final int MAX_SIZE = 20 * 1024 * 1024;

  private final int flags;
  private final byte[] data;

  /**
   * Get a CachedData instance for the given flags and byte array.
   *
   * @param f the flags
   * @param d the data
   * @param maxSize the maximum allowable size.
   */
  public CachedData(int f, byte[] d, int maxSize) {
    super();
    if (d.length > maxSize) {
      throw new IllegalArgumentException("Cannot cache data larger than "
          + maxSize + " bytes (you tried to cache a " + d.length
          + " byte object)");
    }
    flags = f;
    data = d;
  }

  /**
   * Get the stored data.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Get the flags stored along with this value.
   */
  public int getFlags() {
    return flags;
  }

  @Override
  public String toString() {
    return "{CachedData flags=" + flags + " data=" + Arrays.toString(data)
        + "}";
  }
}
