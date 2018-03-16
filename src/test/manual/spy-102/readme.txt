conf -> contains log4j and a properties file used by the driver.
You can modify the properties file to reflect the host names in your cluster.

bin -> driver.sh contains a unix shell script

lib -> The lib folder contained libraries downloaded from
http://www.couchbase.com/develop/java/current along with log4j.jar. I didn’t include them here.

cache -> this is actually the source folder

build2.sh contains the scripts to build the driver classes.
driver2.sh to run it
You may want to update the JAVA_HOME in the build.sh and driver.sh files

You'll have to create a non-default bucket (Mbucket1, say, that is
referred to in the scripts)
