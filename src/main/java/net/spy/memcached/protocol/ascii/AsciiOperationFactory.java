package net.spy.memcached.protocol.ascii;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import net.spy.memcached.ops.BaseOperationFactory;
import net.spy.memcached.ops.CASOperation;
import net.spy.memcached.ops.ConcatenationOperation;
import net.spy.memcached.ops.ConcatenationType;
import net.spy.memcached.ops.DeleteOperation;
import net.spy.memcached.ops.FlushOperation;
import net.spy.memcached.ops.GetAndTouchOperation;
import net.spy.memcached.ops.GetOperation;
import net.spy.memcached.ops.GetlOperation;
import net.spy.memcached.ops.GetsOperation;
import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.MultiGetOperationCallback;
import net.spy.memcached.ops.Mutator;
import net.spy.memcached.ops.MutatorOperation;
import net.spy.memcached.ops.NoopOperation;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.SASLAuthOperation;
import net.spy.memcached.ops.SASLMechsOperation;
import net.spy.memcached.ops.SASLStepOperation;
import net.spy.memcached.ops.StatsOperation;
import net.spy.memcached.ops.StoreOperation;
import net.spy.memcached.ops.StoreType;
import net.spy.memcached.ops.TapOperation;
import net.spy.memcached.ops.VersionOperation;
import net.spy.memcached.tapmessage.TapOpcode;
import net.spy.memcached.tapmessage.RequestMessage;

/**
 * Operation factory for the ascii protocol.
 */
public class AsciiOperationFactory extends BaseOperationFactory {

	public DeleteOperation delete(String key, OperationCallback cb) {
		return new DeleteOperationImpl(key, cb);
	}

	public FlushOperation flush(int delay, OperationCallback cb) {
		return new FlushOperationImpl(delay, cb);
	}

	public GetAndTouchOperation getAndTouch(String key, int expiration,
			GetAndTouchOperation.Callback cb) {
		throw new UnsupportedOperationException("Get and touch is not supported " +
				"for ASCII protocol");
	}

	public GetOperation get(String key, GetOperation.Callback cb) {
		return new GetOperationImpl(key, cb);
	}

	public GetOperation get(Collection<String> keys, GetOperation.Callback cb) {
		return new GetOperationImpl(keys, cb);
	}

	public GetlOperation getl(String key, int exp, GetlOperation.Callback cb) {
		return new GetlOperationImpl(key, exp, cb);
	}

	public GetsOperation gets(String key, GetsOperation.Callback cb) {
		 return new GetsOperationImpl(key, cb);
	}

	public MutatorOperation mutate(Mutator m, String key, int by,
			long exp, int def, OperationCallback cb) {
		return new MutatorOperationImpl(m, key, by, cb);
	}

	public StatsOperation stats(String arg, StatsOperation.Callback cb) {
		return new StatsOperationImpl(arg, cb);
	}

	public StoreOperation store(StoreType storeType, String key, int flags,
			int exp, byte[] data, OperationCallback cb) {
		return new StoreOperationImpl(storeType, key, flags, exp, data, cb);
	}

	public KeyedOperation touch(String key, int expiration, OperationCallback cb) {
		throw new UnsupportedOperationException("Touch is not supported for " +
				"ASCII protocol");
	}

	public VersionOperation version(OperationCallback cb) {
		return new VersionOperationImpl(cb);
	}

	public NoopOperation noop(OperationCallback cb) {
		return new VersionOperationImpl(cb);
	}

	public CASOperation cas(StoreType type, String key, long casId, int flags,
			int exp, byte[] data, OperationCallback cb) {
		return new CASOperationImpl(key, casId, flags, exp, data, cb);
	}

	public ConcatenationOperation cat(ConcatenationType catType,
			long casId,
			String key, byte[] data, OperationCallback cb) {
		return new ConcatenationOperationImpl(catType, key, data, cb);
	}

	@Override
	protected Collection<? extends Operation> cloneGet(KeyedOperation op) {
		Collection<Operation> rv=new ArrayList<Operation>();
		GetOperation.Callback callback = new MultiGetOperationCallback(
				op.getCallback(), op.getKeys().size());
		for(String k : op.getKeys()) {
			rv.add(get(k, callback));
		}
		return rv;
	}

	public SASLMechsOperation saslMechs(OperationCallback cb) {
		throw new UnsupportedOperationException("SASL is not supported for " +
				"ASCII protocol");
	}

	public SASLStepOperation saslStep(String[] mech, byte[] challenge,
			String serverName, Map<String, ?> props, CallbackHandler cbh,
			OperationCallback cb) {
		throw new UnsupportedOperationException("SASL is not supported for " +
				"ASCII protocol");
	}

	public SASLAuthOperation saslAuth(String[] mech, String serverName,
			Map<String, ?> props, CallbackHandler cbh, OperationCallback cb) {
		throw new UnsupportedOperationException("SASL is not supported for " +
				"ASCII protocol");
	}

	@Override
	public TapOperation tapBackfill(String id, long date, OperationCallback cb) {
		throw new UnsupportedOperationException("Tap is not supported for ASCII" +
				" protocol");
	}

	@Override
	public TapOperation tapCustom(String id, RequestMessage message,
			OperationCallback cb) {
		throw new UnsupportedOperationException("Tap is not supported for ASCII" +
				" protocol");
	}

	@Override
	public TapOperation tapAck(TapOpcode opcode, int opaque, OperationCallback cb) {
		throw new UnsupportedOperationException("Tap is not supported for ASCII" +
				" protocol");
	}

	@Override
	public TapOperation tapDump(String id, OperationCallback cb) {
		throw new UnsupportedOperationException("Tap is not supported for ASCII" +
		" protocol");
	}

}
