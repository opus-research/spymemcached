package net.spy.memcached;

/**
 * User: vitaly.rudenya
 * Date: 07.07.11
 * Time: 10:11
 */
public class MultiNodeFailureBaseBucketTest extends AbstractMultiNodeFailure {
    @Override
    protected String getBucketType() {
        return BASE_BUCKET_TYPE;
    }
}
