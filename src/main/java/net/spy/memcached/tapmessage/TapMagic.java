/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.tapmessage;

/**
 * The Magic enum contains a list all of the different magic that can be passed
 * in a tap message in the flag field.
 */
public enum TapMagic {
  /**
   * Defines a tap binary request packet.
   */
  PROTOCOL_BINARY_REQ((byte) 0x80),

  /**
   * Defines a tap binary response packet.
   */
  PROTOCOL_BINARY_RES((byte) 0x81);

  /**
   * The magic value.
   */
  private byte magic;

  /**
   * Defines the magic value.
   * 
   * @param magic - The new magic value
   */
  TapMagic(byte magic) {
    this.magic = magic;
  }
  
  public byte getMagic() {
    return magic;
  }
}
