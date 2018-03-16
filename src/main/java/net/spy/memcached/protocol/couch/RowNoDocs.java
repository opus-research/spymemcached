/**
 * Copyright (C) 2009-2011 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached.protocol.couch;

/**
 * Holds a row in a view result that contains the fields
 * id, key, and value.
 */
public class RowNoDocs implements ViewRow {
  private final String id;
  private final String key;
  private final String value;

  public RowNoDocs(String id, String key, String value) {
    // The id can be the string "null" so convert it to null
    if (id != null && id.equals("null")) {
      this.id = null;
    } else {
      this.id = id;
    }
    // The key can be the string "null" so convert it to null
    if (key != null && key.equals("null")) {
      this.key = null;
    } else {
      this.key = key;
    }
    // The value can be the string "null" so convert it to null
    if (value != null && value.equals("null")) {
      this.value = null;
    } else {
      this.value = value;
    }
  }

  public String getId() {
    return id;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public Object getDocument() {
    throw new UnsupportedOperationException("This view result doesn't contain "
        + "documents");
  }
}
