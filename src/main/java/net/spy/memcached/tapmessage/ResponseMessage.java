package net.spy.memcached.tapmessage;

import java.util.List;

/**
 * A representation of a tap stream message sent from a tap stream server.
 */
public class ResponseMessage extends BaseMessage{
	// Offsets are given from the end of the header
	private static final int ENGINE_PRIVATE_OFFSET = 24;
	private static final int FLAGS_OFFSET = 26;
	private static final int TTL_OFFSET = 28;
	private static final int RESERVED1_OFFSET = 29;
	private static final int RESERVED2_OFFSET = 30;
	private static final int RESERVED3_OFFSET = 31;
	private static final int ITEM_FLAGS_OFFSET = 32;
	private static final int ITEM_EXPIRY_OFFSET = 36;
	private static final int KEY_OFFSET = 40;

	private final short engineprivate;
	private final List<TapFlag> flags;
	private final byte ttl;
	private final byte reserved1;
	private final byte reserved2;
	private final byte reserved3;
	private final int itemflags;
	private int itemexpiry;
	private final String key;
	private final byte[] value;

	/**
	 * Creates a ResponseMessage from binary data.
	 * @param buffer The binary data sent from the tap stream server.
	 */
	public ResponseMessage(byte[] b) {
		magic = TapMagic.getMagicByByte(b[MAGIC_OFFSET]);
		opcode = TapOpcode.getOpcodeByByte(b[OPCODE_OFFSET]);
		keylength = decodeShort(b, KEYLENGTH_OFFSET);
		extralength = b[EXTRALENGTH_OFFSET];
		datatype = b[DATATYPE_OFFSET];
		vbucket = decodeShort(b, VBUCKET_OFFSET);
		totalbody = decodeInt(b, TOTALBODY_OFFSET);
		opaque = decodeInt(b, OPAQUE_OFFSET);
		cas = decodeLong(b, CAS_OFFSET);
		engineprivate = decodeShort(b, ENGINE_PRIVATE_OFFSET);
		flags = TapFlag.getFlags(decodeShort(b, FLAGS_OFFSET));
		ttl = b[TTL_OFFSET];
		reserved1 = b[RESERVED1_OFFSET];
		reserved2 = b[RESERVED2_OFFSET];
		reserved3 = b[RESERVED3_OFFSET];
		if (!opcode.equals(TapOpcode.OPAQUE)) {
			if (opcode.equals(TapOpcode.MUTATION)) {
				itemflags = decodeInt(b, ITEM_FLAGS_OFFSET);
				itemexpiry = decodeInt(b, ITEM_EXPIRY_OFFSET);
			} else {
				itemflags = 0;
				itemexpiry = 0;
			}
			byte[] keybytes = new byte[keylength];
			System.arraycopy(b, KEY_OFFSET, keybytes, 0, keylength);
			key = new String(keybytes);
			value = new byte[b.length - keylength - KEY_OFFSET];
			System.arraycopy(b, (keylength + KEY_OFFSET), value, 0, value.length);
		} else {
			itemflags = 0;
			itemexpiry = 0;
			key = null;
			value = null;
		}
	}

	/**
	 * Gets the value of the engine private field if the field exists in the message.
	 * @return The engine private data.
	 */
	public long getEnginePrivate() {
		return engineprivate;
	}

	/**
	 * Gets the value of the flags field if the field exists in the message.
	 * @return The flags data.
	 */
	public List<TapFlag> getFlags() {
		return flags;
	}

	/**
	 * Gets the value of the time to live field if the field exists in the message.
	 * @return The time to live value;
	 */
	public int getTTL() {
		return ttl;
	}

	/**
	 * Gets the value of the reserved1 field if the field exists in the message.
	 * @return The reserved1 data.
	 */
	protected int getReserved1() {
		return reserved1;
	}

	/**
	 * Gets the value of the reserved2 field if the field exists in the message.
	 * @return The reserved2 data.
	 */
	protected int getReserved2() {
		return reserved2;
	}

	/**
	 * Gets the value of the reserved3 field if the field exists in the message.
	 * @return The reserved3 data.
	 */
	protected int getReserved3() {
		return reserved3;
	}

	/**
	 * Gets the value of the items flag field if the field exists in the message.
	 * @return The items flag data.
	 */
	public int getItemFlags() {
		return itemflags;
	}

	/**
	 * Gets the value of the item expiry field if the field exists in the message.
	 * @return The item expiry data.
	 */
	public long getItemExpiry() {
		return itemexpiry;
	}

	/**
	 * Gets the value of the key field if the field exists in the message.
	 * @return The key data.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value of the value field if the field exists in the message.
	 * @return The value data.
	 */
	public byte[] getValue() {
		return value;
	}
}
