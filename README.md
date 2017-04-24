dbsteward-maven-plugin
======================
DBSteward maven plugin for deploying and upgrading your DBSteward-defined database

dbsteward/dbsteward-maven-plugin/master Build Status: [![dbsteward/dbsteward-maven-plugin/master Build Status](https://travis-ci.org/dbsteward/dbsteward-maven-plugin.png?branch=master)](https://travis-ci.org/dbsteward/dbsteward-maven-plugin)


Usage Guide
===========
Here is a crash course for getting up and running building databases from DBSteward definitions as part of your maven build process.


## Building a Database
1) Check out the plugin code and mvn install the artifact to your m2 repo
```bash
[ nkiraly@bludgeon ~/dbsteward-maven-plugin ]
$ mvn install
```


2) Define the plugin as a dependency in your pom.xml
```XML
  <dependencies>
    <dependency>
      <groupId>org.dbsteward.maven</groupId>
      <artifactId>dbsteward-maven-plugin</artifactId>
      <version>1.4.1-SNAPSHOT</version>
    </dependency>
  </dependencies>
```


3) Define the plugin as part of the build process in your pom.xml, specifying your DBSteward definition file inline in the plugin configuration:
```XML
  <build>
    <plugins>
      <plugin>
        <groupId>org.dbsteward.maven</groupId>
        <artifactId>dbsteward-maven-plugin</artifactId>
        <version>1.4.1-SNAPSHOT</version>
        <configuration>
          <sqlFormat>pgsql8</sqlFormat>
          <definitionFile>example.xml</definitionFile>
          <dbHost>localhost</dbHost>
          <dbPort>5432</dbPort>
          <dbName>someapp</dbName>
          <dbUsername>dbsteward_ci</dbUsername>
          <dbPassword>password1</dbPassword>
          <dbBootstrap>postgres</dbBootstrap>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
For more detailed examples, see [example1](https://github.com/dbsteward/dbsteward-maven-plugin/blob/master/example1/pom.xml)


4) Run the plugin sql-compile goal to build your database creation SQL file:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:sql-compile
```


5) Run the plugin db-create goal to build your database on the specified server:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:db-create
```


Note: Steps 4 and 5 can be combined to compile and run your sql guaranteed fresh:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:sql-compile dbsteward:db-create
```




## Upgrading a Database
Follow Steps 1 and 2 from Building a Database

3) Define the plugin as part of the build process in your pom.xml, specifying your old (previous) and new (current) definition file in the plugin configuration:
```XML
  <build>
    <plugins>
      <plugin>
        <groupId>org.dbsteward.maven</groupId>
        <artifactId>dbsteward-maven-plugin</artifactId>
        <version>1.4.1-SNAPSHOT</version>
        <configuration>
          <sqlFormat>pgsql8</sqlFormat>
          <oldDefinitionFile>example1.xml</oldDefinitionFile>
          <newDefinitionFile>example2.xml</newDefinitionFile>
          <dbHost>localhost</dbHost>
          <dbPort>5432</dbPort>
          <dbName>someapp</dbName>
          <dbUsername>dbsteward_ci</dbUsername>
          <dbPassword>password1</dbPassword>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
For more detailed examples, see [example2](https://github.com/dbsteward/dbsteward-maven-plugin/blob/master/example2/pom.xml)


4) Run the plugin sql-diff goal to build your database upgrade SQL files:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:sql-diff
```


5) Run the plugin db-upgrade goal to upgrade your database on the specified server:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:db-upgrade
```


Note: Steps 4 and 5 can be combined to compile and run your sql guaranteed fresh:
```bash
[ nkiraly@bludgeon ~/that-project-tho ]
$ mvn dbsteward:sql-diff dbsteward:db-upgrade
```


For examples of managing Slony replicated databases with dbsteward-maven-plugin, see [example3](https://github.com/dbsteward/dbsteward-maven-plugin/blob/master/example3/pom.xml) and [example4](https://github.com/dbsteward/dbsteward-maven-plugin/blob/master/example3/pom.xml)
