package net.spy.memcached;

public class MultiNodeFailureBaseBucketTest extends AbstractMultiNodeFailure {

  @Override
  protected String getBucketType() {
    return BASE_BUCKET_TYPE;
  }
}
