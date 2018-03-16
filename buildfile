# -*- mode: ruby -*-
# Generated by Buildr 1.2.10, change to your liking
# Version number for this release
VERSION_NUMBER = `git describe`.strip
# Version number for the next release
NEXT_VERSION = VERSION_NUMBER
# Group identifier for your projects
GROUP = "spy"
COPYRIGHT = "2006-2011  Dustin Sallings, Matt Ingenthron"

PROJECT_NAME = "spymemcached"

TEST_SERVER_V4 = ENV['SPYMC_TEST_SERVER_V4'] || "127.0.0.1"
TEST_SERVER_V6 = ENV['SPYMC_TEST_SERVER_V6'] || ENV['SPYMC_TEST_SERVER_V4'] || "::1"

SERVER_TYPE=ENV['SPYMC_SERVER_TYPE'] || "memcached"

puts "Using server at ipv4 #{TEST_SERVER_V4}"
puts "Using server at ipv6 #{TEST_SERVER_V6}"
puts "Server is type #{SERVER_TYPE}"

def compute_released_verions
  h = {}
  `git tag`.reject{|i| i =~ /pre|rc/}.map{|v| v.strip}.each do |v|
    a=v.split('.')
    h[a[0..1].join('.')] = v
  end
  require 'set'
  rv = Set.new h.values
  rv << VERSION_NUMBER
  rv
end

RELEASED_VERSIONS=compute_released_verions.sort.reverse

# Specify Maven 2.0 remote repositories here, like this:
repositories.release_to = 'sftp://ingenthr@cb-web01.couchbase.com/var/www/domains/membase.org/files/htdocs/maven2'
repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://bleu.west.spy.net/~dustin/m2repo/"
repositories.remote << "https://repository.jboss.org/nexus/content/repositories/releases/"

require 'buildr/java/emma'

plugins=[
  'spy:m1compat:rake:1.0',
  'spy:site:rake:1.2.4',
  'spy:git_tree_version:rake:1.0',
  'spy:build_info:rake:1.1.1'
]

plugins.each do |spec|
  artifact(spec).tap do |plugin|
    plugin.invoke
    load plugin.name
  end
end

desc "Java memcached client"
define "spymemcached" do

  test.options[:java_args] = "-ea"
  test.include "*Test"
  if SERVER_TYPE == 'memcached' then
    test.exclude '*VBucketMemcachedClientTest', '*BucketMonitorTest',
              '*ConfigurationProviderHTTPTest', '*CouchbaseClientTest',
              '*MembaseClientTest'
  end
  if SERVER_TYPE == 'membase' then
    test.exclude '*CouchbaseClientTest'
  end
  test.using :fork=>:each, :properties=>{ 'server.address_v4'=>TEST_SERVER_V4,
               'server.address_v6'=>TEST_SERVER_V6,
               'server.type'=>SERVER_TYPE }

  TREE_VER=tree_version
  puts "Tree version is #{TREE_VER}"

  project.version = VERSION_NUMBER
  project.group = GROUP
  compile.options.target = '1.5'
  manifest["Implementation-Vendor"] = COPYRIGHT
  manifest['Copyright'] = COPYRIGHT
  compile.with "log4j:log4j:jar:1.2.15", "jmock:jmock:jar:1.2.0",
               "junit:junit:jar:4.4", "org.jboss.netty:netty:jar:3.1.5.GA",
               "org.springframework:spring-beans:jar:3.0.3.RELEASE",
               "org.codehaus.jettison:jettison:jar:1.1",
               "commons-codec:commons-codec:jar:1.5",
               "org.easymock:easymock:jar:2.4",
               "org.easymock:easymockclassextension:jar:2.4",
               "junit-addons:junit-addons:jar:1.4",
               "cglib:cglib:jar:2.2.2", "asm:asm:jar:3.3.1",
               "org.apache.httpcomponents:httpcore:jar:4.1.1",
               "org.apache.httpcomponents:httpcore-nio:jar:4.1.1"

  # Gen build
  gen_build_info "net.spy.memcached", "git"
  compile.from "target/generated-src"
  resources.from "target/generated-rsrc"

  package(:jar).with :manifest =>
    manifest.merge("Main-Class" => "net.spy.memcached.BuildInfo")

  package :sources
  package :javadoc
  javadoc.using(:windowtitle => "javadocs for spymemcached #{TREE_VER}",
                :doctitle => "Javadocs for spymemcached #{TREE_VER}",
                :use => true,
                :charset => 'utf-8',
                :overview => 'src/main/java/net/spy/memcached/overview.html',
                :group => { 'Core' => 'net.spy.memcached' },
                :link => 'http://java.sun.com/j2se/1.5.0/docs/api/')

  emma.exclude 'net.spy.memcached.test.*'
  emma.exclude 'net.spy.memcached.BuildInfo'

end
# vim: syntax=ruby et ts=2
