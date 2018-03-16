/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket;

/**
 * An unchecked exception that signals that a configuration error
 * has occured while communicating with Membase.
 */
public class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -9180877058910807939L;

  public ConfigurationException() {
    super();
  }

  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationException(Throwable cause) {
    super(cause);
  }
}
