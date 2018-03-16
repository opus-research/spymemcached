package net.spy.memcached;

import java.util.concurrent.Future;

import net.spy.memcached.transcoders.Transcoder;

public interface MembaseClientIF extends MemcachedClientIF {

	Future<CASValue<Object>> asyncGetAndLock(final String key, int exp);

	<T> Future<CASValue<T>> asyncGetAndLock(final String key, int exp,
			final Transcoder<T> tc);

	public <T> CASValue<T> getAndLock(String key, int exp, Transcoder<T> tc);

	CASValue<Object> getAndLock(String key, int exp);
}
