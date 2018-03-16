package net.spy.memcached.protocol.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.TapOperation;
import net.spy.memcached.tapmessage.BaseMessage;
import net.spy.memcached.tapmessage.FieldDoesNotExistException;
import net.spy.memcached.tapmessage.Opcode;
import net.spy.memcached.tapmessage.ResponseMessage;
import net.spy.memcached.tapmessage.Util;

public abstract class TapOperationImpl extends OperationImpl implements TapOperation {
	private static final int TAP_FLAG_ACK = 0x1;
	
	private int bytesProcessed;
	private int bodylen;
	private byte[] header;
	private byte[] message;
	
	static final int CMD=0;
	
	private final String keyFilter;
	private final String valueFilter;
	
	protected TapOperationImpl(OperationCallback cb, String keyFilter, String valueFilter) {
		super(CMD, generateOpaque(), cb);
		this.header = new byte[BaseMessage.HEADER_LENGTH];
		this.message = null;
		this.keyFilter = keyFilter;
		this.valueFilter = valueFilter;
	}

	public abstract void initialize();

	@Override
	public void readFromBuffer(ByteBuffer data) throws IOException {
		while (data.remaining() > 0) {
			if (bytesProcessed < BaseMessage.HEADER_LENGTH) {
				header[bytesProcessed] = data.get();
				bytesProcessed++;
			} else {
				if (message == null) {
					bodylen = (int) Util.fieldToValue(header, BaseMessage.TOTAL_BODY_INDEX, BaseMessage.TOTAL_BODY_FIELD_LENGTH);
					message = new byte[BaseMessage.HEADER_LENGTH + bodylen];
					System.arraycopy(header, 0, message, 0, BaseMessage.HEADER_LENGTH);
				}
				
				if (bytesProcessed < message.length) {
					message[bytesProcessed] = data.get();
					bytesProcessed++;
				}
				if (bytesProcessed >= message.length) {
					ResponseMessage response = new ResponseMessage(message);

					if (response.getOpcode() != Opcode.OPAQUE && response.getOpcode() != Opcode.NOOP) {
						if (response.getFlags() == TAP_FLAG_ACK) {
							((Callback)getCallback()).gotAck(response.getOpcode(), response.getOpaque());
						}
						if (filter(response)) {
							((Callback)getCallback()).gotData(response);
						}
					}
					message = null;
					bytesProcessed = 0;
				}
			}
		}
	}
	
	private boolean filter(ResponseMessage tapMessage) {
		if (keyFilter != null) {
			if (!Pattern.matches(keyFilter, tapMessage.getKey())) {
				return false;
			}
		}
		try {
			if (valueFilter != null) {
				if (!Pattern.matches(valueFilter, new String(tapMessage.getValue()))) {
					return false;
				}
			}
		} catch (FieldDoesNotExistException e) {
			// There is no value for this packet so continue
		}
		return true;
	}
}
