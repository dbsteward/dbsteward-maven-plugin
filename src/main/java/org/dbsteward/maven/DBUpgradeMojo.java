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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Difference and upgrade the specified database with the compiled SQL diff of
 * the specifies DBSteward definition files.
 *
 * SQLDiffMojo (sql-diff goal) is a prerequisite to this running successfully
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "db-upgrade", defaultPhase = LifecyclePhase.DEPLOY)
public class DBUpgradeMojo extends DBStewardAbstractMojo {

  /**
   * Relative or absolute path to old (previous) database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.oldDefinitionFile}", property = "oldDefinitionFile", required = true)
  protected File oldDefinitionFile;

  /**
   * Relative or absolute path to new (current) database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.newDefinitionFile}", property = "newDefinitionFile", required = true)
  protected File newDefinitionFile;

  /**
   * Upgrade the database specified in plugin config
   *
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    super.execute();

    // get upgrade file names  stored by SQLDiffMojo
    String upgradeSqlFileNameCSL = proj.getProperties().getProperty("project.dbsteward.output.upgradeSqlFileNameCSL");
    if (upgradeSqlFileNameCSL == null || upgradeSqlFileNameCSL.length() == 0) {
      throw new MojoExecutionException("project.dbsteward.output.upgradeFileNameCSL is not set. Did you run sql-diff before this goal?");
    }
    String[] upgradeSqlFileNames = StringUtils.split(upgradeSqlFileNameCSL, ",");
    List<File> upgradeSqlFiles = new ArrayList<>();
    // check that upgrade files named exist
    for (String upgradeSqlFileName : upgradeSqlFileNames) {
      File uf = new File(upgradeSqlFileName);
      if (!uf.exists()) {
        throw new MojoExecutionException("project.dbsteward.output.upgradeFileNameCSL item " + upgradeSqlFileName + " does not exist. Did you run sql-diff before this goal?");
      }
      upgradeSqlFiles.add(uf);
    }

    getLog().info("Upgrading database " + dbName + " on " + dbHost + ":" + dbPort);
    for (File upgradeSqlFile : upgradeSqlFiles) {
      getLog().info("Executing upgrade script: " + upgradeSqlFile);
      dbExecutor.executeFile(dbName, upgradeSqlFile);
    }
  }

}
