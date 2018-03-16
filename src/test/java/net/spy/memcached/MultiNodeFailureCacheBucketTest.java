package net.spy.memcached;

public class MultiNodeFailureCacheBucketTest extends AbstractMultiNodeFailure {

  @Override
  protected String getBucketType() {
    return CACHE_BUCKET_TYPE;
  }
}
