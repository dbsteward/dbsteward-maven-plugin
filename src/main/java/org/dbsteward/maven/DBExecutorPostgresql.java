package org.dbsteward.maven;

/**
 * This software is licensed under the BSD (2 Clause) license.
 * http://opensource.org/licenses/BSD-2-Clause
 *
 * Copyright (c) 2014, Nicholas J Kiraly, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Postgresql Database Executor Definition
 *
 * pgsql8 sqlFormat glue to create and upgrade databases
 *
 * READ THIS!
 *
 * Before you ask why this does not use sql-maven-plugin or similar mojos which
 * utilize JDBC drivers, let me point out some things:
 *
 * 1) DBSteward postgresql compiler generates dollar-quoted function
 * definitions, and while JDBC postgresql drivers did not always understand
 * dollar-quoting, they still cut up statements at the semi-colon, making
 * loading of pl/pgsql functions with semicolons in the their bodies impossible.
 *
 * 2) DBSteward slony deploy and upgrade management is accomplished via slonik
 * EXECUTE SCRIPT and other CLI / secondary script magicks for which there is no
 * alternative so I would rather be consistent with CLI tools than using JDBC or
 * sql-maven-plugin.
 *
 * @author nicholas.kiraly
 */
public class DBExecutorPostgresql implements DBExecutor {

  private Log log;

  protected String host;
  protected String port;
  protected String name;
  protected String username;
  protected String password;
  protected String bootstrap;

  protected String dbEncoding = "UTF8";
  protected String dbTemplate = "template0";

  public void setPluginLog(Log log) {
    this.log = log;
  }

  public void setConnectionInfo(String host, String port, String name, String username, String password, String boostrap) {
    this.host = host;
    this.port = port;
    this.name = name;
    this.username = username;
    this.password = password;
    this.bootstrap = boostrap;
  }

  protected void executeTool(File tool, String... args) throws MojoExecutionException {
    Commandline commandLine = new Commandline();
    commandLine.setExecutable(tool.getPath());

    for (String arg : args) {
      Arg _arg = commandLine.createArg();
      _arg.setValue(arg);
    }

    PluginLogStreamConsumer pluginInfoStream = new PluginLogStreamConsumer(this.log, PluginLogLevel.LOG_LEVEL_INFO);
    StringStreamConsumer errorStream = new StringStreamConsumer();

    try {
      int returnCode = CommandLineUtils.executeCommandLine(commandLine, pluginInfoStream, errorStream, 30);
      if (returnCode != 0) {
        throw new MojoExecutionException("Unexpected Tool Return Code " + returnCode + " - Error Buffer = " + errorStream.getOutput());
      }
    } catch (CommandLineException cle) {
      this.log.error("Tool Execution Exception: " + cle.getMessage(), cle);
      throw new MojoExecutionException("Tool Execution Exception: " + cle.getMessage());
    }
  }

  protected int executeToolWithConsumers(File tool, StreamConsumer infoStreamConsumer, StreamConsumer errorStreamConsumer, String... args) throws MojoExecutionException, CommandLineException {
    Commandline commandLine = new Commandline();
    commandLine.setExecutable(tool.getPath());

    for (String arg : args) {
      Arg _arg = commandLine.createArg();
      _arg.setValue(arg);
    }

    return CommandLineUtils.executeCommandLine(commandLine, infoStreamConsumer, errorStreamConsumer, 10);
  }

  public void createDatabase(String name) throws MojoExecutionException {
    File createdb = new File("createdb");
    String[] args = {
      "--host=" + this.host,
      "--port=" + this.port,
      "--username=" + this.username,
      name,
      "--encoding=" + this.dbEncoding,
      "--template=" + this.dbTemplate
    };
    executeTool(createdb, args);
  }

  public void executeFile(String name, File f) throws MojoExecutionException {
    File psql = new File("psql");
    String[] args = {
      "--variable=ON_ERROR_STOP=1",
      "--host=" + this.host,
      "--port=" + this.port,
      "--username=" + this.username,
      "--dbname=" + name,
      "--file=" + f
    };
    executeTool(psql, args);
  }

  @Override
  public List<String> listDatabases() throws MojoExecutionException {
    File psql = new File("psql");
    String[] args = {
      "--host=" + this.host,
      "--port=" + this.port,
      "--username=" + this.username,
      "--dbname=" + this.bootstrap,
      "--list"
    };

    StringStreamConsumer infoStreamConsumer = new StringStreamConsumer();
    StringStreamConsumer errorStreamConsumer = new StringStreamConsumer();
    try {
      int returnCode = this.executeToolWithConsumers(psql, infoStreamConsumer, errorStreamConsumer, args);
      if (returnCode != 0) {
        throw new MojoExecutionException("Unexpected Tool Return Code " + returnCode + " - Error Buffer = " + errorStreamConsumer.getOutput());
      }
    } catch (CommandLineException cle) {
      this.log.error("Tool Execution Exception: " + cle.getMessage(), cle);
      throw new MojoExecutionException("Tool Execution Exception: " + cle.getMessage());
    }

    List<String> list = new ArrayList<>();
    /*
     List of databases
     Name           |    Owner     | Encoding | Collate | Ctype | Access privileges
     -------------------------+--------------+----------+---------+-------+-------------------
     dbsteward_phpunit       | deployment   | UTF8     | C       | C     |
     postgres                | pgsql        | UTF8     | C       | C     |
     someapp                 | dbsteward_ci | UTF8     | C       | C     |
     template0               | pgsql        | UTF8     | C       | C     | =c/pgsql         +
     |              |          |         |       | pgsql=CTc/pgsql
     template1               | pgsql        | UTF8     | C       | C     | =c/pgsql         +
     |              |          |         |       | pgsql=CTc/pgsql
     (5 rows)

     */

    String[] lines = infoStreamConsumer.getOutput().split("\n");
    for (String line : lines) {
      if (line.contains("List of databases")) {
        continue;
      }
      if (line.contains("Access privileges")) {
        continue;
      }
      if (line.contains("+------")) {
        continue;
      }
      if (line.contains(" rows)")) {
        continue;
      }
      String[] line_chunks = line.split("\\|");
      String dbname = line_chunks[0].trim();
      // if the first col is not empty, its a database name
      if (dbname.length() > 0) {
        this.log.debug("dbname:" + dbname);
        list.add(dbname);
      } else {
        this.log.debug("dbline:" + line);
      }
    }

    return list;
  }

  @Override
  public boolean databaseExists(String name) throws MojoExecutionException {
    List<String> list = this.listDatabases();
    if (list.contains(name)) {
      return true;
    }
    return false;
  }

}
