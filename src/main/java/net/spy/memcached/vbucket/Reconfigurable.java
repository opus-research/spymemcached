package net.spy.memcached.vbucket;

import net.spy.memcached.vbucket.config.Bucket;

/**
 * Interface provides a method for receiving configuration updates
 * @author alexander.sokolovsky.a@gmail.com
 */
public interface Reconfigurable {
    /**
     * Call on a configuration update
     * @param bucket updated vbucket configuration
     */
    void reconfigure(Bucket bucket);
}
