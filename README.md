dbsteward-maven-plugin
======================
DBSteward maven plugin for deploying and upgrading your DBSteward-defined database



Usage Guide
===========
Here is a crash course for getting up and running building databases from DBSteward definitions as part of your maven build process.



## Building a Database
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
        <dependencies>
          <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>9.2-1004-jdbc4</version>
          </dependency>
        </dependencies>
        <configuration>
          <definitionFile>example.xml</definitionFile>
          <driver>org.postgresql.Driver</driver>
          <url>jdbc:postgresql://localhost:5432:example</url>
          <username>dbsteward_ci</username>
          <password>password1</password>
          <createDBName>example</createDBName>
          <createDBUrl>jdbc:postgresql://localhost:5432:postgres</createDBUrl>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
For more detailed examples, see https://github.com/nkiraly/dbsteward-maven-plugin/blob/master/example1/pom.xml


4) Run the plugin sql-compile goal to build your database creation SQL file:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:sql-compile
```


5) Run the plugin db-create goal to build your database at the specified jdbc url:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:db-create
```


Note: Steps 4 and 5 can be combined to compile and run your sql guaranteed fresh:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:sql-compile dbsteward:db-create
```




## Upgrading a Database
Follow Steps 1 and 2 from Building a Database

3) Define the plugin as part of the build process in your pom.xml, specifying your old (previous) and new (current) defintion file in the plugin configuration:
```XML
  <build>
    <plugins>
      <plugin>
        <groupId>org.dbsteward.maven</groupId>
        <artifactId>dbsteward-maven-plugin</artifactId>
        <dependencies>
          <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>9.2-1004-jdbc4</version>
          </dependency>
        </dependencies>
        <configuration>
          <oldDefinitionFile>example1.xml</oldDefinitionFile>
          <newDefinitionFile>example2.xml</newDefinitionFile>
          <driver>org.postgresql.Driver</driver>
          <url>jdbc:postgresql://localhost:5432:example</url>
          <username>dbsteward_ci</username>
          <password>password1</password>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
For more detailed examples, see https://github.com/nkiraly/dbsteward-maven-plugin/blob/master/example2/pom.xml


4) Run the plugin sql-diff goal to build your database upgrade SQL files:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:sql-diff
```


5) Run the plugin db-upgrade goal to upgrade your database at the specified jdbc url:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:db-upgrade
```


Note: Steps 4 and 5 can be combined to compile and run your sql guaranteed fresh:
```bash
[ nkiraly@generati ~/that-project-tho ]
$ mvn dbsteward:sql-diff dbsteward:db-upgrade
```

