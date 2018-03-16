#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-6-openjdk
export PATH=$JAVA_HOME/bin:$PATH

CP=${CP}:.

for aFile in `find lib -name "*.jar"`
do
    CP=$aFile:$CP
done 

echo "which java.."
which java

echo "java -version.."
$JAVA_HOME/bin/java -version

echo "PATH..."
echo $PATH

echo "CLASSPATH.."
echo $CLASSPATH

echo "compiling files.."
$JAVA_HOME/bin/javac -classpath ${CP} cache/prototype/*.java
