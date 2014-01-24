dbsteward-maven-plugin
======================

DBSteward maven plugin for deploying and upgrading your DBSteward-defined database



Usage Guide
===========

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


3) Define the plugin as part of the build process in your pom.xml, specifying your DBSteward definition file inline in the plugin defintion:
```XML
  <build>
    <plugins>
      <plugin>
        <groupId>org.dbsteward.maven</groupId>
        <artifactId>dbsteward-maven-plugin</artifactId>
        <configuration>
          <definitionFile>example1.xml</definitionFile>
        </configuration>
      </plugin>
    </plugins>
  </build>
```


4) Run the plugin compile goal to build your database creation SQL file:
```bash
mvn org.dbsteward.maven:dbsteward-maven-plugin:compile
```

