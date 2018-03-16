package net.spy.memcached.tapmessage;

import java.nio.ByteBuffer;

import net.spy.memcached.compat.SpyObject;

/**
 * The BaseMessage implements the header of a tap message. This class cannot be instantiated.
 * Tap stream messages are created with the RequestMessage and ResponseMessage classes.
 */
public class BaseMessage extends SpyObject {
	protected static final int MAGIC_OFFSET = 0;
	protected static final int OPCODE_OFFSET = 1;
	protected static final int KEYLENGTH_OFFSET = 2;
	protected static final int EXTRALENGTH_OFFSET = 4;
	protected static final int DATATYPE_OFFSET = 5;
	protected static final int VBUCKET_OFFSET = 6;
	protected static final int TOTALBODY_OFFSET = 8;
	protected static final int OPAQUE_OFFSET = 12;
	protected static final int CAS_OFFSET = 16;
	public static final int HEADER_LENGTH = 24;

	protected TapMagic magic;
	protected TapOpcode opcode;
	protected short keylength;
	protected byte extralength;
	protected byte datatype;
	protected short vbucket;
	protected int totalbody;
	protected int opaque;
	protected long cas;

	protected BaseMessage() {
		// Empty
	}

	/**
	 * Sets the value of the tap messages magic field.
	 * @param m The new value for the magic field.
	 */
	public final void setMagic(TapMagic m) {
		magic = m;
	}

	/**
	 * Gets the value of the tap messages magic field.
	 * @return The value of the magic field.
	 */
	public final TapMagic getMagic() {
		return magic;
	}

	/**
	 * Sets the value of the tap messages opcode field
	 * @param o The new value of the opcode field.
	 */
	public final void setOpcode(TapOpcode o) {
		opcode = o;
	}

	/**
	 * Gets the value of the tap messages opaque field.
	 * @return The value of the opaque field.
	 */
	public final TapOpcode getOpcode() {
		return opcode;
	}

	/**
	 * Gets the value of the tap messages key length field.
	 * @return The value of the key length field.
	 */
	public final short getKeylength() {
		return keylength;
	}

	/**
	 * Sets the value of the tap messages data type field.
	 * @param b The new value for the data type field.
	 */
	public final void setDatatype(byte d) {
		datatype = d;
	}

	/**
	 * Gets the value of the tap messages data type field.
	 * @return The value of the data type field.
	 */
	public final byte getDatatype() {
		return datatype;
	}

	/**
	 * Sets the value of the tap messages extra length field.
	 * @param i The new value for the extra length field.
	 */
	public final void setExtralength(byte e) {
		extralength = e;
	}

	/**
	 * Gets the value of the tap messages extra length field.
	 * @return The value of the extra length field.
	 */
	public final byte getExtralength() {
		return extralength;
	}

	/**
	 * Sets the value of the tap messages vbucket field.
	 * @param vb The new value for the vbucket field.
	 */
	public final void setVbucket(short vb) {
		vbucket = vb;
	}

	/**
	 * Gets the value of the tap messages vbucket field.
	 * @return The value of the vbucket field.
	 */
	public final short getVbucket() {
		return vbucket;
	}

	/**
	 * Sets the value of the tap messages total body field.
	 * @param l The new value for the total body field.
	 */
	public final void setTotalbody(int t) {
		totalbody = t;
	}

	/**
	 * Gets the value of the tap messages total body field.
	 * @return The value of the total body field.
	 */
	public final int getTotalbody() {
		return totalbody;
	}

	/**
	 * Sets the value of the tap messages opaque field.
	 * @param op The new value for the opaque field.
	 */
	public final void setOpaque(int op) {
		opaque = op;
	}

	/**
	 * Gets the value of the tap messages opaque field.
	 * @return The value of the opaque field.
	 */
	public final int getOpaque() {
		return opaque;
	}

	/**
	 * Sets the value of the tap messages cas field.
	 * @param cas The new value for the cas field.
	 */
	public final void setCas(long c) {
		cas = c;
	}

	/**
	 * Gets the value of the tap messages cas field.
	 * @return The value of the cas field.
	 */
	public final long getCas() {
		return cas;
	}

	/**
	 * Gets the length of the entire message.
	 * @return The length of the message.
	 */
	public final int getMessageLength() {
		return HEADER_LENGTH + getTotalbody();
	}

	/**
	 * Creates a ByteBuffer representation of the message.
	 * @return The ByteBuffer representation of the message.
	 */
	public ByteBuffer getBytes() {
		ByteBuffer bb = ByteBuffer.allocate(HEADER_LENGTH + getTotalbody());
		bb.put(magic.magic);
		bb.put(opcode.opcode);
		bb.putShort(keylength);
		bb.put(extralength);
		bb.put(datatype);
		bb.putShort(vbucket);
		bb.putInt(totalbody);
		bb.putInt(opaque);
		bb.putLong(cas);
		return bb;
	}

	protected short decodeShort(byte[] data, int i) {
		return (short)((data[i] & 0xff) << 8 | (data[i + 1] & 0xff));
	}

	protected int decodeInt(byte[] data, int i) {
		return (data[i] & 0xff) << 24
			| (data[i + 1] & 0xff) << 16
			| (data[i + 2] & 0xff) << 8
			| (data[i + 3] & 0xff);
		  }

	protected long decodeLong(byte[] data, int i) {
		return (data[i] & 0xffL) << 56
			| (data[i + 1] & 0xffL) << 48
			| (data[i + 2] & 0xffL) << 40
			| (data[i + 3] & 0xffL) << 32
			| (data[i + 4] & 0xffL) << 24
			| (data[i + 5] & 0xffL) << 16
			| (data[i + 6] & 0xffL) << 8
			| (data[i + 7] & 0xffL);
	}
}
