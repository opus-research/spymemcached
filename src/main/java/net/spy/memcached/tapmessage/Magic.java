package net.spy.memcached.tapmessage;

/**
 * The Magic enum contains a list all of the different magic that can be passed in a tap message in the flag field.
 */
public enum Magic {
	/**
	 * Defines a binary tap packet
	 */
	PROTOCOL_BINARY_REQ((byte) 0x80),

	PROTOCOL_BINARY_RES((byte) 0x81);

	/**
	 * The magic value
	 */
	public byte magic;

	/**
	 * Defines the magic value
	 * @param magic - The new magic value
	 */
	Magic(byte magic) {
		this.magic = magic;
	}
}
