/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

import java.util.List;

/**
 * A ConfigDifference.
 */
public class ConfigDifference {

  /**
   * List of server names that were added.
   */
  private List<String> serversAdded;

  /**
   * List of server names that were removed.
   */
  private List<String> serversRemoved;

  /**
   * Number of vbuckets that changed. -1 if the total number changed.
   */
  private int vbucketsChanges;

  /**
   * True if the sequence of servers changed.
   */
  private boolean sequenceChanged;

  public List<String> getServersAdded() {
    return serversAdded;
  }

  protected void setServersAdded(List<String> newServersAdded) {
    this.serversAdded = newServersAdded;
  }

  public List<String> getServersRemoved() {
    return serversRemoved;
  }

  protected void setServersRemoved(List<String> newServersRemoved) {
    this.serversRemoved = newServersRemoved;
  }

  public int getVbucketsChanges() {
    return vbucketsChanges;
  }

  protected void setVbucketsChanges(int newVbucketsChanges) {
    this.vbucketsChanges = newVbucketsChanges;
  }

  public boolean isSequenceChanged() {
    return sequenceChanged;
  }

  protected void setSequenceChanged(boolean newSequenceChanged) {
    this.sequenceChanged = newSequenceChanged;
  }
}
