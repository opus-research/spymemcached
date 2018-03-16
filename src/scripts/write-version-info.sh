#!/bin/bash
#
# Licensed to Cloudera, Inc. under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# Cloudera, Inc. licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script writes the current version information to a generated source
# file. It is intended to be run from the base directory of the project.
#
# Arguments are:
#    path to the root of the build directory
#    the version number
#    the git hash of the current checkout. If empty, auto-detect.
#
# e.g., $ write-version-info.sh ./build/ 1.0.0

buildroot=$1
version=$2
specifiedgithash=$3

outputdir=${buildroot}/src/net/spy/memcached
buildinfofile=${outputdir}/BuildInfo.java
changesfile=${outputdir}/changelog.txt

signature=$specifiedgithash
if [ -z "$signature" ]; then
  signature=`git log -1 --pretty=format:%H`
fi

host=`hostname`
compiledate=`date`
treeversion=`git describe`

mkdir -p ${outputdir}
git log > ${changesfile}
cat > ${buildinfofile} <<EOF
// generated by src/scripts/write-version-info.sh

package net.spy.memcached;

import java.net.URL;
import java.util.Properties;

import java.io.InputStream;
import java.io.FileNotFoundException;

public final class BuildInfo extends Properties {
  public static final String VERSION="${version}";
  public static final String GIT_HASH="${signature}";
  public static final String TREE_VERSION="${treeversion}";
  public static final String COMPILE_USER="${USER}";
  public static final String COMPILE_HOST="${host}";
  public static final String COMPILE_DATE="${compiledate}";

  /**
   * Get an instance of BuildInfo that describes the spy.jar build.
   */
  public BuildInfo() {
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(256);
    sb.append("Spymemcached ");
    sb.append(VERSION);
    sb.append("\n\nTree Version: ");
    sb.append(TREE_VERSION);
    sb.append("\nLast Commit ID: ");
    sb.append(GIT_HASH);
    sb.append("\n\nCompiled by ");
    sb.append(COMPILE_USER);
    sb.append("@");
    sb.append(COMPILE_HOST);
    sb.append(" on ");
    sb.append(COMPILE_DATE);
    return sb.toString();
  }

  public URL getFile(String rel) throws FileNotFoundException {
    ClassLoader cl=getClass().getClassLoader();
    URL u=cl.getResource(rel);
    if(u == null) {
      throw new FileNotFoundException("Can't find " + rel);
    }
    return(u);
  }

  public static void main(String args[]) throws Exception {
    BuildInfo bi=new BuildInfo();
    String cl="%" + "CHANGELOG" + "%";

    System.out.println(bi);

    // If there was a changelog, let it be shown.
    if(!cl.equals("net/spy/memcached/changelog.txt")) {
      if(args.length > 0 && args[0].equals("-c")) {
        System.out.println(" -- Changelog:\n");

        URL u=bi.getFile("net/spy/memcached/changelog.txt");
        InputStream is=u.openStream();
        try {
          byte data[]=new byte[8192];
          int bread=0;
          do {
            bread=is.read(data);
            if(bread > 0) {
              System.out.write(data, 0, bread);
            }
          } while(bread != -1);
        } finally {
          is.close();
        }
      } else {
        System.out.println("(add -c to see the recent changelog)");
      }
    }
  }
}
EOF

