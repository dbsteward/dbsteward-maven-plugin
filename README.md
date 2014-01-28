dbsteward-maven-plugin
======================

DBSteward maven plugin for deploying and upgrading your DBSteward-defined database



Usage Guide
===========


## Building a Database
To get up and running building DBSteward definitions as part of your maven build process, here is a crash course.

1) Check out the plugin code and mvn install the artifact to your m2 repo
```bash
[ nkiraly@generati ~/dbsteward-maven-plugin ]
$ mvn install
```


2) Define the plugin as a dependency in your pom.xml
```XML
  <dependencies>
    <dependency>
      <groupId>org.dbsteward.maven</groupId>
      <artifactId>dbsteward-maven-plugin</artifactId>
      <version>1.3.7-SNAPSHOT</version>
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
        <configuration>
          <definitionFile>example1.xml</definitionFile>
          <dbName>someapp_example</dbName>
        </configuration>
      </plugin>
    </plugins>
  </build>
```


4a) Run the plugin sql-compile goal to build your database creation SQL file:
```bash
[ nkiraly@generati ~/that-project-tho ]
mvn org.dbsteward.maven:dbsteward-maven-plugin:sql-compile
```
### -OR-
4b) Run the plugin db-create goal to build your database on the specified server:
```bash
[ nkiraly@generati ~/that-project-tho ]
mvn org.dbsteward.maven:dbsteward-maven-plugin:db-create
```
The db-create goal runs the sql-compile mojo implicitly.



## Upgrading a Database
Follow Steps 1 and 2 from Building a Database

3) Define the plugin as part of the build process in your pom.xml, specifying your old (previous) and new (current) defintion file in the plugin configuration:
```XML
  <build>
    <plugins>
      <plugin>
        <groupId>org.dbsteward.maven</groupId>
        <artifactId>dbsteward-maven-plugin</artifactId>
        <configuration>
          <oldDefinitionFile>example1.xml</oldDefinitionFile>
          <newDefinitionFile>example2.xml</newDefinitionFile>
          <dbName>someapp_example</dbName>
        </configuration>
      </plugin>
    </plugins>
  </build>
```


4a) Run the plugin sql-diff goal to build your database upgrade SQL files:
```bash
[ nkiraly@generati ~/that-project-tho ]
mvn org.dbsteward.maven:dbsteward-maven-plugin:sql-diff
```
### -OR-
4b) Run the plugin db-upgrade goal to upgrade your database on the specified server:
```bash
[ nkiraly@generati ~/that-project-tho ]
mvn org.dbsteward.maven:dbsteward-maven-plugin:db-upgrade
```
The db-upgrade goal runs the sql-diff mojo implicitly.

