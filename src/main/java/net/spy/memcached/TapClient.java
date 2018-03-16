package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.TapOperation;
import net.spy.memcached.tapmessage.TapOpcode;
import net.spy.memcached.tapmessage.RequestMessage;
import net.spy.memcached.tapmessage.ResponseMessage;

public class TapClient {
	private boolean vBucketAware;
	private BlockingQueue<ResponseMessage> rqueue;
	private HashMap<Operation, TapConnectionProvider> omap;
	private List<InetSocketAddress> addrs;
	private List<URI> baseList;
	private String bucketName;
	private String usr;
	private String pwd;
	private long messagesRead;

	/**
	 * Creates a non-vBucket aware tap client.
	 *
	 * @param ia the addresses of each node in the cluster.
	 */
	public TapClient(InetSocketAddress... ia) {
		this(Arrays.asList(ia));
	}

	/**
	 * Creates a non-vBucket aware tap client.
	 *
	 * @param addrs a list of addresses containing each node in the cluster.
	 */
	public TapClient(List<InetSocketAddress> addrs) {
		this.rqueue = new LinkedBlockingQueue<ResponseMessage>();
		this.omap = new HashMap<Operation, TapConnectionProvider>();
		this.vBucketAware = false;
		this.addrs = addrs;
		this.baseList = null;
		this.bucketName = null;
		this.usr = null;
		this.pwd = null;
		this.messagesRead = 0;
	}

	public TapClient(final List<URI> baseList, final String bucketName,
			final String usr, final String pwd) {
		for (URI bu : baseList) {
			if (!bu.isAbsolute()) {
				throw new IllegalArgumentException("The base URI must be absolute");
			}
		}
		this.rqueue = new LinkedBlockingQueue<ResponseMessage>();
		this.omap = new HashMap<Operation, TapConnectionProvider>();
		this.vBucketAware = true;
		this.addrs = null;
		this.baseList = baseList;
		this.bucketName = bucketName;
		this.usr = usr;
		this.pwd = pwd;
		this.messagesRead = 0;
	}

	/**
	 * Gets the next tap message from the queue of received tap messages.
	 *
	 * @return The tap message at the head of the queue or null if the
	 * queue is empty for more than one second.
	 */
	public ResponseMessage getNextMessage() {
		try {
			messagesRead++;
			return rqueue.poll(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			shutdown();
			return null;
		}
	}

	/**
	 * Decides whether the client has received tap messages or will
	 * receive more messages in the future.
	 *
	 * @return true if the client has tap responses or expects to have
	 * responses in the future. False otherwise.
	 */
	public boolean hasMoreMessages() {
		if (!rqueue.isEmpty()) {
			return true;
		} else {
			synchronized(omap) {
				Iterator<Operation> itr = omap.keySet().iterator();
				while (itr.hasNext()) {
					Operation op = itr.next();
					if (op.getState().equals(OperationState.COMPLETE) ||
							op.isCancelled() || op.hasErrored()) {
						omap.get(op).shutdown();
						omap.remove(op);
					}
				}
				if (omap.size() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Allows the user to specify as custom tap message.
	 *
	 * @param id The named tap id that can be used to resume a disconnected
	 * tap stream
	 * @param message The custom tap message that will be used to initiate the
	 * tap stream.
	 * @return The operation that controls the tap stream.
	 * @throws ConfigurationException If a bad configuration was recieved from the
	 * Membase cluster.
	 * @throws IOException If there are errors connecting to the cluster.
	 */
	public Operation tapCustom(String id, RequestMessage message,
			String keyFilter, String valueFilter)
		throws ConfigurationException, IOException {
		final TapConnectionProvider conn;
		if (vBucketAware) {
			conn = new TapConnectionProvider(baseList, bucketName, usr, pwd);
		} else {
			conn = new TapConnectionProvider(addrs);
		}

		final CountDownLatch latch=new CountDownLatch(1);
		final Operation op=conn.opFact.tapCustom(id, message,
				new TapOperation.Callback() {

			public void receivedStatus(OperationStatus status) {
			}
			public void gotData(ResponseMessage tapMessage) {
				rqueue.add(tapMessage);
				messagesRead++;
			}
			public void gotAck(TapOpcode opcode, int opaque) {
				tapAck(conn, opcode, opaque, this);
			}
			public void complete() {
				latch.countDown();
			}});
		synchronized(omap) {
			omap.put(op, conn);
		}
		conn.addOp(op);
		return op;
	}

	/**
	 * Specifies a tap stream that will send all key-value mutations that take
	 * place in the future.
	 *
	 * @param id The named tap id that can be used to resume a disconnected
	 * tap stream
	 * @param runTime The amount of time to do backfill for. Set to 0 for
	 * infinite backfill.
	 * @return The operation that controls the tap stream.
	 * @throws ConfigurationException If a bad configuration was recieved from the
	 * Membase cluster.
	 * @throws IOException If there are errors connecting to the cluster.
	 */
	public Operation tapBackfill(String id, final int runTime) throws IOException,
			ConfigurationException {
		return tapBackfill(id, -1, runTime);
	}

	/**
	 * Specifies a tap stream that will send all key-value mutations that took
	 * place after a specific date.
	 *
	 * @param id The named tap id that can be used to resume a disconnected
	 * tap stream
	 * @param date The date to begin sending key mutations from. Specify -1
	 * to send all future key-value mutations.
	 * @param runTime The amount of time to do backfill for. Set to 0 for
	 * infinite backfill.
	 * @return The operation that controls the tap stream.
	 * @throws ConfigurationException If a bad configuration was recieved from the
	 * Membase cluster.
	 * @throws IOException If there are errors connecting to the cluster.
	 */
	public Operation tapBackfill(String id, long date, final int runTime) 
			throws IOException, ConfigurationException {
		final TapConnectionProvider conn;
		if (vBucketAware) {
			conn = new TapConnectionProvider(baseList, bucketName, usr, pwd);
		} else {
			conn = new TapConnectionProvider(addrs);
		}

		final CountDownLatch latch=new CountDownLatch(1);
		final Operation op=conn.opFact.tapBackfill(id, date,
				new TapOperation.Callback() {

			public void receivedStatus(OperationStatus status) {
			}
			public void gotData(ResponseMessage tapMessage) {
				rqueue.add(tapMessage);
				messagesRead++;
			}
			public void gotAck(TapOpcode opcode, int opaque) {
				tapAck(conn, opcode, opaque, this);
			}
			public void complete() {
				latch.countDown();
			}});
		synchronized(omap) {
			omap.put(op, conn);
		}
		conn.addOp(op);

		if (runTime > 0) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(runTime * 1000);
					} catch (InterruptedException e) {
						/* Not caught because if we are interrupted early
						 * the stream should close anyways.
						 */
					}
					conn.shutdown();
					synchronized(omap) {
						omap.remove(op);
					}
				}
			};
			new Thread(r).start();
		}
		return op;
	}

	private void tapAck(TapConnectionProvider conn, TapOpcode opcode, int opaque,
			OperationCallback cb) {
		final Operation op=conn.opFact.tapAck(opcode, opaque, cb);
		conn.addOp(op);
	}

	/**
	 * Shuts down all tap streams that are currently running.
	 */
	public void shutdown() {
		synchronized(omap) {
			Iterator<Operation> itr = omap.keySet().iterator();
			while (itr.hasNext()) {
				omap.get(itr.next()).shutdown();
			}
		}
	}

	/**
	 * The number of messages read by all of the tap streams created
	 * with this client. This will include a count of all tap
	 * response types.
	 * @return The number of messages read
	 */
	public long getMessagesRead() {
		return messagesRead;
	}
}
