/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package net.spy.memcached;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.couch.AsyncConnectionManager;
import net.spy.memcached.couch.AsyncConnectionRequest;
import net.spy.memcached.couch.RequestHandle;
import net.spy.memcached.protocol.couchdb.HttpOperation;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.BufferingNHttpEntity;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.NHttpRequestExecutionHandler;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.protocol.HttpContext;

public class CouchbaseNode extends SpyObject {

	private final InetSocketAddress addr;
	private final AsyncConnectionManager connMgr;

	private final long opQueueMaxBlockTime;
	private final long defaultOpTimeout;

	private final BlockingQueue<HttpOperation> writeQ;

	public CouchbaseNode(InetSocketAddress a, AsyncConnectionManager mgr,
			BlockingQueue<HttpOperation> wQ, long maxBlockTime,
			long operationTimeout) {
		addr = a;
		connMgr = mgr;
		writeQ = wQ;
		opQueueMaxBlockTime = maxBlockTime;
		defaultOpTimeout = operationTimeout;
	}

	public void init() throws IOReactorException {
		// Start the I/O reactor in a separate thread
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					connMgr.execute();
				} catch (InterruptedIOException ex) {
					System.err.println("Interrupted");
				} catch (IOException e) {
					System.err.println("I/O error: " + e.getMessage());
					e.printStackTrace();
				}
				System.out.println("I/O reactor terminated");
			}

		});
		t.start();
	}

	public void doWrites() {
		HttpOperation op;
		while ((op = writeQ.poll()) != null) {
			if (!op.isTimedOut() && !op.isCancelled()) {
				AsyncConnectionRequest connRequest = connMgr
						.requestConnection();
				try {
					connRequest.waitFor();
				} catch (InterruptedException e) {
					getLogger().warn(
							"Interrupted while trying to get a connection."
									+ " Cancelling op");
					op.cancel();
					return;
				}

				NHttpClientConnection conn = connRequest.getConnection();
				if (conn == null) {
					System.err.println("Failed to obtain connection");
					return;
				}

				HttpContext context = conn.getContext();
				RequestHandle handle = new RequestHandle(connMgr, conn);
				context.setAttribute("request-handle", handle);
				context.setAttribute("operation", op);
				conn.requestOutput();
			}
		}
	}

	public boolean hasWriteOps() {
		return !writeQ.isEmpty();
	}

	public void addOp(HttpOperation op) {
		try {
			if (!writeQ.offer(op, opQueueMaxBlockTime, TimeUnit.MILLISECONDS)) {
				throw new IllegalStateException("Timed out waiting to add "
						+ op + "(max wait=" + opQueueMaxBlockTime + "ms)");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting to add "
					+ op);
		}
	}

	public void shutdown() throws IOException {
		shutdown(0, TimeUnit.MILLISECONDS);
	}

	public void shutdown(long time, TimeUnit unit) throws IOException {
		if (unit != TimeUnit.MICROSECONDS) {
			connMgr.shutdown(TimeUnit.MILLISECONDS.convert(time, unit));
		} else {
			connMgr.shutdown(time);
		}
	}

	static class MyHttpRequestExecutionHandler implements
			NHttpRequestExecutionHandler {

		public MyHttpRequestExecutionHandler() {
			super();
		}

		public void initalizeContext(final HttpContext context,
				final Object attachment) {
		}

		public void finalizeContext(final HttpContext context) {
			RequestHandle handle = (RequestHandle) context
					.removeAttribute("request-handle");
			if (handle != null) {
				handle.cancel();
			}
		}

		public HttpRequest submitRequest(final HttpContext context) {
			HttpOperation op = (HttpOperation) context
					.getAttribute("operation");
			if (op == null)
				return null;
			return op.getRequest();
		}

		public void handleResponse(final HttpResponse response,
				final HttpContext context) {
			System.out.println("Handling Response");

			RequestHandle handle = (RequestHandle) context
					.removeAttribute("request-handle");
			HttpOperation op = (HttpOperation) context
					.removeAttribute("operation");
			System.out.println(response.getEntity().isChunked());
			if (!response.getEntity().isChunked()) {
				if (handle != null) {
					handle.completed();
					if (!op.isTimedOut() && !op.hasErrored()
							&& !op.isCancelled()) {
						op.getCallback().complete(response);
					}
				}
			}
		}

		@Override
		public ConsumingNHttpEntity responseEntity(HttpResponse response,
				HttpContext context) throws IOException {
			System.out.println("Dealing with Entity");
			return new BufferingNHttpEntity(response.getEntity(),
					new HeapByteBufferAllocator());
		}

	}

	static class EventLogger extends SpyObject implements EventListener {

		public void connectionOpen(final NHttpConnection conn) {
			getLogger().info("Connection open: " + conn);
		}

		public void connectionTimeout(final NHttpConnection conn) {
			getLogger().info("Connection timed out: " + conn);
		}

		public void connectionClosed(final NHttpConnection conn) {
			getLogger().info("Connection closed: " + conn);
		}

		public void fatalIOException(final IOException ex,
				final NHttpConnection conn) {
			getLogger().info("I/O error: " + ex.getMessage());
		}

		public void fatalProtocolException(final HttpException ex,
				final NHttpConnection conn) {
			getLogger().info("HTTP error: " + ex.getMessage());
		}

	}

}
