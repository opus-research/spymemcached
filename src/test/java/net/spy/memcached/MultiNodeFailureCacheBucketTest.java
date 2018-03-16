package net.spy.memcached;

/**
 * User: vitaly.rudenya
 * Date: 07.07.11
 * Time: 10:09
 */
public class MultiNodeFailureCacheBucketTest extends AbstractMultiNodeFailure {
    @Override
    protected String getBucketType() {
        return CACHE_BUCKET_TYPE;
    }
}
