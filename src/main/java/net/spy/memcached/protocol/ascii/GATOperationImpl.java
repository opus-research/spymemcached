package net.spy.memcached.protocol.ascii;

import net.spy.memcached.ops.GATOperation;
import net.spy.memcached.ops.GetlOperation;

public class GATOperationImpl extends BaseGetOpImpl
	implements GATOperation{

	public GATOperationImpl(String c, int e, GetlOperation.Callback cb,
			String k) {
		super(c, e, cb, k);
	}

}
