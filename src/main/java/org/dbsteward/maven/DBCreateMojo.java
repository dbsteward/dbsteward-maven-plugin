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
import org.codehaus.mojo.sql.SqlExecMojo;
import org.codehaus.plexus.util.FileUtils;

/**
 * Create the specified database and load it with the compiled SQL of the
 * specified definition files.
 *
 * SQLCompileMojo (sql-compile goal) is a prerequisite to this running
 * successfully
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "db-create", defaultPhase = LifecyclePhase.INSTALL)
public class DBCreateMojo extends SqlExecMojo {

  /**
   * Relative or absolute path to DBSteward database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.definitionFile}", property = "definitionFile", required = true)
  private File definitionFile;

  /**
   * DBSteward --outputdir value specification. You generally want to leave this
   * the project.build.directory so your DBSteward output files end up in
   * /target/
   *
   * NOTE: this is cloned from DBStewardAbstractMojo as this does not inherit
   * from it but the value needs to be consistent
   */
  @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
  private File outputDir;

  /**
   * Database name to create. If omitted, CREATE DATABASE will not be issued.
   */
  @Parameter(property = "createDBName")
  private String createDBName;

  /**
   * Database connection to use to create the database
   */
  @Parameter(property = "createDBUrl")
  private String createDBUrl;

  /**
   * Create the database specified by overriding SqlExecMojo.execute() to do
   * db-create goal
   *
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    super.setSqlCommand("");
    String saveUrl = super.getUrl();

    if (createDBName != null && createDBName.length() > 0) {
      getLog().info("Creating database: " + createDBName);
      super.setUrl(createDBUrl);
      super.setSqlCommand("CREATE DATABASE " + createDBName);
      super.execute(); // initiate the code-setup sql execution
    }

    // calculate build file path to load as srcFile
    String buildSqlPath = FileUtils.basename(definitionFile.getPath(), "xml");
    // look for definitionFile's output file in the output dir
    buildSqlPath = outputDir + File.separator + buildSqlPath;
    // kill trailing . left by basename
    buildSqlPath = buildSqlPath.substring(0, buildSqlPath.length() - 1);
    buildSqlPath = buildSqlPath + "_build.sql";
    File buildSqlFile = new File(buildSqlPath);
    if (!buildSqlFile.exists()) {
      throw new MojoExecutionException("DBSteward output build file " + buildSqlPath + "does not exist. Did you forget to run sql-compile before this db-create goal?");
    }

    File[] buildFiles = {buildSqlFile};

    getLog().info("Executing database script " + buildSqlFile + " on " + saveUrl);
    super.setUrl(saveUrl);
    super.setSqlCommand("");
    super.setSrcFiles(buildFiles);
    super.execute(); // initiate the code-setup sql execution
  }

  //
  // SqlExecMojo override options specified into SqlExecMojo private variables
  //
  @Parameter(property = "url")
  private String url;

  @Override
  public void setUrl(String url) {
    super.setUrl(url);
  }

  @Parameter(property = "driver")
  private String driver;

  @Override
  public void setDriver(String driver) {
    super.setDriver(driver);
  }

  @Parameter(property = "username")
  private String username;

  @Override
  public void setUsername(String username) {
    super.setUsername(username);
  }

  @Parameter(property = "password")
  private String password;

  @Override
  public void setPassword(String password) {
    super.setPassword(password);
  }

}
