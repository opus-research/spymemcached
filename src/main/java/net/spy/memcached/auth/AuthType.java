/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.spy.memcached.auth;

/**
 *
 */
enum AuthType {

  CRAM_MD5("CRAM-MD5"),
  PLAIN("PLAIN");

  private final String type;

  AuthType(String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }
  
}
