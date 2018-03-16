package net.spy.memcached.protocol.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.internal.SyncRequest;
import net.spy.memcached.internal.SyncResponse;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.SyncOperation;

public class SyncOperationImpl extends MultiKeyOperationImpl
	implements SyncOperation {

	private static final int SYNC_EXTRA_LENGTH = 4;
	private static final int SYNC_KEYS_FIELD_LENGTH = 2;
	private static final int SYNC_KEY_MIN_LENGTH = 12;

	static final int CMD=0x96;

	private final Collection<SyncRequest> syncs;
	private final Map<Integer, String> keys=new HashMap<Integer, String>();
	private final int replicaCount;
	private final int persist;
	private final int mutation;
	private final int pandm;

	private final int terminalOpaque=generateOpaque();

	protected SyncOperationImpl(Collection<SyncRequest> reqs, int replicaCount,
			boolean persist, boolean mutation, boolean pandm,
			OperationCallback cb) {
		super(-1, -1, cb);

		this.replicaCount = replicaCount;
		this.persist = persist ? 1 : 0;
		this.mutation = mutation ? 1 : 0;
		this.pandm = pandm ? 1 : 0;
		syncs = reqs;
		for(SyncRequest s : reqs) {
			vbmap.put(s.getKey(), new Short((short)0));
			keys.put(new Integer(generateOpaque()), s.getKey());
		}
	}

	@Override
	public void initialize() {
		int size=MIN_RECV_PACKET + SYNC_KEYS_FIELD_LENGTH + SYNC_EXTRA_LENGTH;
		for(SyncRequest s : syncs) {
			size += KeyUtil.getKeyBytes(s.getKey()).length + SYNC_KEY_MIN_LENGTH;
		}

		// set up the initial header stuff
		ByteBuffer bb=ByteBuffer.allocate(size);

		// Custom header
		bb.put(REQ_MAGIC);
		bb.put((byte)CMD);
		bb.putShort((short)0); // keylength
		bb.put((byte)SYNC_EXTRA_LENGTH); // extralen
		bb.put((byte)0); // data type
		bb.putShort((short)0); // reserved
		bb.putInt(size - MIN_RECV_PACKET);
		bb.putInt(0); // opaque makes no sense
		bb.putLong(0); // cas

		bb.putInt(getFlags()); // flags
		bb.putShort((short)syncs.size()); // number of keys

		for(SyncRequest s : syncs) {
			byte[] key = KeyUtil.getKeyBytes(s.getKey());
			int vbucket = vbmap.get(s.getKey());
			bb.putLong(s.getCas());
			bb.putShort((short)vbucket);
			bb.putShort((short)key.length);
			bb.put(key);
		}

		bb.flip();
		setBuffer(bb);
	}

	@Override
	protected void finishedPayload(byte[] pl) throws IOException {
		if(responseOpaque == terminalOpaque) {
			getCallback().receivedStatus(STATUS_OK);
			transitionState(OperationState.COMPLETE);
		} else if(errorCode != 0) {
			getCallback().receivedStatus(new OperationStatus(false, "SYNC only supports replication check"));
		} else {
			final int numKeys=decodeShort(pl, 0);

			int offset = SYNC_KEYS_FIELD_LENGTH;
			Callback cb=(Callback)getCallback();
			for (int i = 0; i < numKeys; i++) {
				long cas = decodeLong(pl, offset);
				int keylen = decodeShort(pl, offset+=10);
				int eventid = pl[offset+=2];

				byte[] key = new byte[keylen];
				System.arraycopy(pl, offset + 1, key, 0, key.length);
				offset += keylen+1;

				OperationStatus status;
				switch(eventid) {
					case 1:
						status = new OperationStatus(true,"Persisted");
					case 2:
						status = new OperationStatus(true,"Modified");
					case 3:
						status = new OperationStatus(true,"Deleted");
					case 4:
						status = new OperationStatus(true,"Replicated");
					case 5:
						status = new OperationStatus(false,"Invalid Key");
					case 6:
						status = new OperationStatus(false,"Invalid CAS");
					default:
						status = new OperationStatus(false, "Unknown Status");
				}
				cb.gotData(new SyncResponse(new String(key), cas, status));
			}
			transitionState(OperationState.COMPLETE);
		}
		resetInput();
	}

	@Override
	protected boolean opaqueIsValid() {
		return responseOpaque == terminalOpaque
			|| keys.containsKey(responseOpaque);
	}

	private int getFlags() {
		return (replicaCount << 4
			| persist << 3
			| mutation << 2
			| pandm << 1);
	}

	public Collection<String> getKeys() {
		return keys.values();
	}

}
