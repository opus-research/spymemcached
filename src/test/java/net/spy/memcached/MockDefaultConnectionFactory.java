package net.spy.memcached;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;


public class MockDefaultConnectionFactory extends DefaultConnectionFactory {

	public MockDefaultConnectionFactory() {
		 super(DEFAULT_OP_QUEUE_LEN, DEFAULT_READ_BUFFER_SIZE);
	}
	
	public MemcachedNode createMemcachedNode(SocketAddress sa, SocketChannel c, int bufSize) {
		return new MockExceptionThrowingMemcachedNode(sa, c, bufSize,
		          createReadOperationQueue(),
		          createWriteOperationQueue(),
		          createOperationQueue(),
		          getOpQueueMaxBlockTime(),
		          getOperationTimeout());  
	}
	
}
