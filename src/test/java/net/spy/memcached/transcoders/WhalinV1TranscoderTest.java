/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.transcoders;

import java.util.Arrays;

import net.spy.memcached.CachedData;

/**
 * A WhalinV1TranscoderTest.
 */
public class WhalinV1TranscoderTest extends BaseTranscoderCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setTranscoder(new WhalinV1Transcoder());
  }

  @Override
  public void testByteArray() throws Exception {
    byte[] a = { 'a', 'b', 'c' };

    CachedData cd = getTranscoder().encode(a);
    byte[] decoded = (byte[]) getTranscoder().decode(cd);
    assertNotNull(decoded);
    assertTrue(Arrays.equals(a, decoded));
  }

  @Override
  protected int getStringFlags() {
    // Flags are not used by this transcoder.
    return 0;
  }

}
