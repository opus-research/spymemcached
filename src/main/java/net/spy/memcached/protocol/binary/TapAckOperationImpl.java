package net.spy.memcached.protocol.binary;

import java.nio.ByteBuffer;
import java.util.Collection;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.tapmessage.Magic;
import net.spy.memcached.tapmessage.Opcode;
import net.spy.memcached.tapmessage.RequestMessage;

public class TapAckOperationImpl extends TapOperationImpl {
	private final Opcode opcode;
	private final int opaque;

	TapAckOperationImpl(Opcode opcode, int opaque, OperationCallback cb) {
		super(cb, null, null);
		this.opcode = opcode;
		this.opaque = opaque;
	}

	@Override
	public void initialize() {
		RequestMessage message = new RequestMessage();
		message.setMagic(Magic.PROTOCOL_BINARY_RES);
		message.setOpcode(opcode);
		message.setOpaque(opaque);
		setBuffer(message.getBytes());
	}

	@Override
	public void readFromBuffer(ByteBuffer data) {
		// Do Nothing
	}

	@Override
	public Collection<String> getKeys() {
		return null;
	}

	@Override
	public void streamClosed(OperationState state) {
		transitionState(state);
	}
}
