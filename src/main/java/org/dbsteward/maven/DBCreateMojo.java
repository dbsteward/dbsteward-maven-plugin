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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Create the specified database and load it with the compiled SQL of the
 * specified definition files.
 *
 * SQLCompileMojo (sql-compile goal) is a prerequisite to this running
 * successfully.
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "db-create", defaultPhase = LifecyclePhase.DEPLOY)
public class DBCreateMojo extends DBStewardAbstractMojo {

  /**
   * Relative or absolute path to DBSteward database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.definitionFile}", property = "definitionFile", required = true)
  private File definitionFile;

  /**
   * Goal should skip database creation
   */
  @Parameter(defaultValue = "${project.dbsteward.skipDBCreate}", property = "skipDBCreate")
  private boolean skipDBCreate;

  /**
   * Create the database specified in plugin config
   *
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    super.execute();

    // get buildSqlFileName stored by SQLCompileMojo
    String buildSqlFileName = proj.getProperties().getProperty("project.dbsteward.output.buildSqlFileName");
    if (buildSqlFileName == null || buildSqlFileName.length() == 0) {
      throw new MojoExecutionException("project.dbsteward.output.buildSqlFileName is not set. Did you run sql-compile before this goal?");
    }
    // check file exists
    File buildSqlFile = new File(buildSqlFileName);
    if (!buildSqlFile.exists()) {
      throw new MojoExecutionException("project.dbsteward.output.buildSqlFileName " + buildSqlFileName + "does not exist. Did you run sql-compile before this goal?");
    }

    if (skipDBCreate) {
      // don't create database - optional skip has been specified
      getLog().warn("skipDBCreate specified. Skipping database creation.");
    } else {
      getLog().info("Creating database: " + dbName + " on " + dbHost + ":" + dbPort);
      dbExecutor.createDatabase(dbName);
    }

    getLog().info("Loading database " + dbName + " on " + dbHost + ":" + dbPort);
    getLog().info("Executing script: " + buildSqlFile);
    dbExecutor.executeFile(buildSqlFile);
  }

}
