package net.spy.memcached.tapmessage;

import java.util.Date;

import net.spy.memcached.compat.log.Logger;
import net.spy.memcached.compat.log.LoggerFactory;


public class MessageBuilder {
	private Logger LOG = LoggerFactory.getLogger(MessageBuilder.class);
	
	RequestMessage message;
	
	public MessageBuilder() {
		this.message = new RequestMessage();
		message.setMagic(Magic.PROTOCOL_BINARY_REQ);
		message.setOpcode(Opcode.REQUEST);
	}
	
	public void doBackfill(Date date) {
		message.setBackfill(date);
		message.setFlags(Flag.BACKFILL);
	}
	
	public void doDump() {
		message.setFlags(Flag.DUMP);
	}
	
	public void specifyVbuckets(int[] vbucketlist) {
		message.setVbucketlist(vbucketlist);
		message.setFlags(Flag.LIST_VBUCKETS);
	}
	
	public void supportAck() {
		//message.setFlags(Flag.SUPPORT_ACK);
		LOG.info("ACK not supported");
	}
	
	public void keysOnly() {
		message.setFlags(Flag.KEYS_ONLY);
	}
	
	public void takeoverVbuckets(int[] vbucketlist) {
		message.setVbucketlist(vbucketlist);
		message.setFlags(Flag.TAKEOVER_VBUCKETS);
	}
	
	public RequestMessage getMessage() {
		return message;
	}
}
