package net.spy.memcached;

import org.couchbase.mock.CouchbaseMock;

/**
 * User: vitaly.rudenya
 * Date: 07.07.11
 * Time: 10:11
 */
public class MultiNodeFailureBaseBucketTest extends AbstractMultiNodeFailure {
    @Override
    protected CouchbaseMock.BucketType getBucketType() {
        return CouchbaseMock.BucketType.BASE;
    }
}
