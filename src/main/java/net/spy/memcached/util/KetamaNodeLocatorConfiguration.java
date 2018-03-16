/**
 * Copyright (C) 2006-2009 Dustin Sallings
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

package net.spy.memcached.util;

import net.spy.memcached.MemcachedNode;

/**
 * Defines the set of all configuration dependencies required for the
 * KetamaNodeLocator algorithm to run.
 */
public interface KetamaNodeLocatorConfiguration {

  /**
   * Returns a uniquely identifying key, suitable for hashing by the
   * KetamaNodeLocator algorithm.
   *
   * @param node The MemcachedNode to use to form the unique identifier
   * @param repetition The repetition number for the particular node in question
   *          (0 is the first repetition)
   * @return The key that represents the specific repetition of the node
   */
  String getKeyForNode(MemcachedNode node, int repetition);

  /**
   * Returns the number of discrete hashes that should be defined for each node
   * in the continuum.
   *
   * @return a value greater than 0
   */
  int getNodeRepetitions();
}
