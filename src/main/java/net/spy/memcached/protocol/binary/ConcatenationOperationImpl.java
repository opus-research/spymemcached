package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.ConcatenationOperation;
import net.spy.memcached.ops.ConcatenationType;
import net.spy.memcached.ops.OperationCallback;

class ConcatenationOperationImpl extends SingleKeyOperationImpl
	implements ConcatenationOperation {

	private static final byte APPEND=0x0e;
	private static final byte PREPEND=0x0f;

	private final long cas;
	private final ConcatenationType catType;
	private final byte[] data;

	private static byte cmdMap(ConcatenationType t) {
		byte rv=(byte) 0xFF;
		switch(t) {
			case append: rv=APPEND; break;
			case prepend: rv=PREPEND; break;
		}
		// Check fall-through.
		assert rv != -1 : "Unhandled store type:  " + t;
		return rv;
	}

	public ConcatenationOperationImpl(ConcatenationType t, String k,
			byte[] d, long c, OperationCallback cb) {
		super(cmdMap(t), generateOpaque(), k, cb);
		data=d;
		cas=c;
		catType=t;
	}

	@Override
	public void initialize() {
		prepareBuffer(key, cas, data);
	}

	public long getCasValue() {
		return cas;
	}

	public byte[] getData() {
		return data;
	}

	public ConcatenationType getStoreType() {
		return catType;
	}

}
