package net.spy.memcached.protocol.binary;

import java.util.Collection;
import java.util.Collections;

import net.spy.memcached.ops.GetOperation;
import net.spy.memcached.ops.GetlOperation;
import net.spy.memcached.ops.GetsOperation;
import net.spy.memcached.ops.OperationStatus;

class GetOperationImpl extends OperationImpl
	implements GetOperation, GetsOperation, GetlOperation {

	static final int GET_CMD=0;
	static final int GETL_CMD=0x94;
	
	/**
	 * Length of the extra header stuff for a GET response.
	 */
	static final int EXTRA_HDR_LEN=4;

	private final String key;
	private final int exp;
	private final boolean hasExp;

	public GetOperationImpl(String k, GetOperation.Callback cb) {
		super(GET_CMD, generateOpaque(), cb);
		key=k;
		exp=0;
		hasExp=false;
	}

	public GetOperationImpl(String k, GetsOperation.Callback cb) {
		super(GET_CMD, generateOpaque(), cb);
		key=k;
		exp=0;
		hasExp=false;
	}
	
	public GetOperationImpl(String k, int e, GetlOperation.Callback cb) {
		super(GETL_CMD, generateOpaque(), cb);
		key=k;
		exp=e;
		hasExp=true;
	}

	@Override
	public void initialize() {
		if (hasExp) {
			prepareBuffer(key, 0, EMPTY_BYTES, 0, exp);
		} else {
			prepareBuffer(key, 0, EMPTY_BYTES);
		}
	}

	@Override
	protected void decodePayload(byte[] pl) {
		final int flags=decodeInt(pl, 0);
		final byte[] data=new byte[pl.length - EXTRA_HDR_LEN];
		System.arraycopy(pl, EXTRA_HDR_LEN, data, 0, pl.length-EXTRA_HDR_LEN);
		// Assume we're processing a get unless the cast fails.
		try {
			GetOperation.Callback cb=(GetOperation.Callback)getCallback();
			cb.gotData(key, flags, data);
		} catch(ClassCastException e) {
			try {
				GetsOperation.Callback cb=(GetsOperation.Callback)getCallback();
				cb.gotData(key, flags, responseCas, data);
			} catch (ClassCastException e1) {
				//getl return the key and value. So we need to strip off the key
				byte[] value = new byte[data.length-key.length()];
				System.arraycopy(data, key.length(), value, 0, data.length-key.length());
				GetlOperation.Callback cb=(GetlOperation.Callback)getCallback();
				cb.gotData(key, flags, value);
			}
		}
		getCallback().receivedStatus(STATUS_OK);
	}

	@Override
	protected OperationStatus getStatusForErrorCode(int errCode, byte[] errPl) {
		return errCode == ERR_NOT_FOUND ? NOT_FOUND_STATUS : null;
	}

	public Collection<String> getKeys() {
		return Collections.singleton(key);
	}

}
