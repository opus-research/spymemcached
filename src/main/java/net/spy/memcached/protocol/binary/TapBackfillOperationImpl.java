package net.spy.memcached.protocol.binary;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.TapOperation;
import net.spy.memcached.tapmessage.Flag;
import net.spy.memcached.tapmessage.Magic;
import net.spy.memcached.tapmessage.Opcode;
import net.spy.memcached.tapmessage.RequestMessage;

public class TapBackfillOperationImpl extends TapOperationImpl implements TapOperation {
	private final String id;
	private final Date date;
	
	TapBackfillOperationImpl(String id, Date date, String keyFilter, String valueFilter, 
			OperationCallback cb) {
		super(cb, keyFilter, valueFilter);
		this.id = id;
		this.date = date;
	}

	@Override
	public void initialize() {
		RequestMessage message = new RequestMessage();
		message.setMagic(Magic.PROTOCOL_BINARY_REQ);
		message.setOpcode(Opcode.REQUEST);
		message.setFlags(Flag.BACKFILL);
		message.setFlags(Flag.SUPPORT_ACK);
		if (id != null) {
			message.setName(id);
		} else {
			message.setName(UUID.randomUUID().toString());
		}
		
		message.setBackfill(date);
		setBuffer(message.getBytes());
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
