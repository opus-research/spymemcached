/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

/**
 * A ConfigParseException.
 */
public class ConfigParsingException extends RuntimeException {

  private static final long serialVersionUID = -8393032485475738369L;

  public ConfigParsingException() {
    super();
  }

  public ConfigParsingException(String message) {
    super(message);
  }

  public ConfigParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigParsingException(Throwable cause) {
    super(cause);
  }
}
