package net.spy.memcached.tapmessage;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * A tap request message that is used to start tap streams, perform sasl authentication, and
 * maintain the health of tap streams.
 */
public class RequestMessage extends BaseMessage{

	private boolean hasBackfill;
	private boolean hasVBucketList;
	private boolean hasFlags;

	private List<TapFlag> flagList;
	private short[] vblist; 
	private String name;
	private long backfilldate;

	/**
	 * Create a tap request message. These messages are used to start tap streams.
	 */
	public RequestMessage() {
		flagList = new LinkedList<TapFlag>();
		vblist = new short[0];
		name = UUID.randomUUID().toString();
		backfilldate = -1;
		totalbody += name.length();
		keylength = (short)name.length();
	}

	/**
	 * Sets the flags for the tap stream. These flags decide what kind of tap stream
	 * will be received.
	 * @param f The flags to use for this tap stream.
	 */
	public void setFlags(TapFlag f) {
		if (!flagList.contains(f)) {
			if (!hasFlags) {
				hasFlags = true;
				extralength += 4;
				totalbody += 4;
			}
			if (f.equals(TapFlag.BACKFILL)) {
				hasBackfill = true;
				totalbody += 8;
			}if (f.equals(TapFlag.LIST_VBUCKETS)
					|| f.equals(TapFlag.TAKEOVER_VBUCKETS)) {
				hasVBucketList = true;
				totalbody += 2;
			}
			flagList.add(f);
		}
	}

	/**
	 * Returns the flags for this message.
	 * @return An int value of flags set for this tap message.
	 */
	public List<TapFlag> getFlags() {
		return flagList;
	}

	/**
	 * Stream all keys inserted into the server after a given date.
	 * @param date - The date to stream keys from. Null to stream all keys.
	 */
	public void setBackfill(long date) {
		backfilldate = date;
	}

	/**
	 * Sets a list of vbuckets to stream keys from.
	 * @param vbs - A list of vbuckets.
	 */
	public void setVbucketlist(short[] vbs) {
		int oldSize = (vblist.length + 1) * 2;
		int newSize = (vbs.length + 1) * 2;
		totalbody =+ newSize - oldSize;
		vblist = vbs;
	}

	/**
	 * Sets a name for this tap stream. If the tap stream fails this name can be used to try to restart
	 * the tap stream from where it last left off.
	 * @param s The name for the tap stream.
	 */
	public void setName(String n) {
		if (n.length() > 65535) {
			throw new IllegalArgumentException("Tap name too long");
		}
		totalbody += n.length() - name.length();
		keylength = (short)n.length();
		name = n;
	}

	/**
	 * Encodes the message into binary.
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

		if (hasFlags) {
			int flag = 0;
			for (int i = 0; i < flagList.size(); i++) {
				flag |= flagList.get(i).flag;
			}
			bb.putInt(flag);
		}
		bb.put(name.getBytes());
		if (hasBackfill) {
			bb.putLong(backfilldate);
		}
		if (hasVBucketList) {
			bb.putShort((short)vblist.length);
			for (int i = 0; i < vblist.length; i++) {
				bb.putShort(vblist[i]);
			}
		}

		return (ByteBuffer) bb.flip();
	}
}
