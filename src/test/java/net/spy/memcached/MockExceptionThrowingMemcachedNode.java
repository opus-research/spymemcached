package net.spy.memcached;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;


import net.spy.memcached.ops.Operation;
import net.spy.memcached.protocol.TCPMemcachedNodeImpl;

/**
 * This Mock allows to pass a configurable set of operations and then fail.
 */
public class MockExceptionThrowingMemcachedNode extends TCPMemcachedNodeImpl {

  private int timesCalled = 0;
  
  private int throwAtTimes = 5;
  
  public MockExceptionThrowingMemcachedNode(SocketAddress sa, SocketChannel c, int bufSize,
	      BlockingQueue<Operation> rq, BlockingQueue<Operation> wq,
	      BlockingQueue<Operation> iq, Long opQueueMaxBlockTimeNs, long dt) {
	    // This mock never does auth.
	    super(sa, c, bufSize, rq, wq, iq, opQueueMaxBlockTimeNs, false, dt);
	  }
	  
	public void addOp(Operation op) throws IllegalStateException {
		   timesCalled++;

		   super.addOp(op);
		   if(timesCalled == throwAtTimes) {
			   throw new OutOfMemoryError("Simulated Out-of-memory exception thrown.");
		   }
	}
	  
	protected void optimize() {}

}