package net.spy.memcached.protocol.ascii;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;

/**
 * Operation to reset a timeout in Membase server.
 */
public class TouchOperationImpl extends OperationImpl
	implements KeyedOperation {
	private static final int OVERHEAD = 32;
	private static final String CMD = "touch";

	private final String key;
	private final int exp;

	private static final OperationStatus STORED=
		new OperationStatus(true, "STORED");
	private static final OperationStatus NOT_FOUND=
		new OperationStatus(false, "NOT_FOUND");
	private static final OperationStatus TEMP_FAIL =
		new OperationStatus(false, "Temporary Error");

	public TouchOperationImpl(String k, int e, OperationCallback cb) {
		super(cb);
		key=k;
		exp=e;
	}

	@Override
	public void handleLine(String line) {
		getLogger().debug("Delete of %s returned %s", key, line);
		getCallback().receivedStatus(matchStatus(line, STORED, NOT_FOUND, TEMP_FAIL));
		transitionState(OperationState.COMPLETE);
	}

	@Override
	public void initialize() {
		ByteBuffer b=ByteBuffer.allocate(
			KeyUtil.getKeyBytes(key).length + OVERHEAD);
		setArguments(b, CMD, key, exp);
		b.flip();
		setBuffer(b);
	}

	@Override
	public Collection<String> getKeys() {
		return Collections.singleton(key);
	}
}
