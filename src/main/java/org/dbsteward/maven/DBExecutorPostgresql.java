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
import org.apache.maven.plugin.logging.Log;

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

  public void createDatabase(String name) {
    this.log.warn("@TODO: create database " + name);
  }

  public void executeFile(File f) {
    this.log.warn("@TODO: execute file " + f);
  }
}
