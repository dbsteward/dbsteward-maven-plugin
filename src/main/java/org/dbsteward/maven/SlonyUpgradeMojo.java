package org.dbsteward.maven;

/**
 * This software is licensed under the BSD (2 Clause) license.
 * http://opensource.org/licenses/BSD-2-Clause
 *
 * Copyright (c) 2015, Nicholas J Kiraly, All rights reserved.
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Upgrade a slony cluster to the new database definition specified
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "slony-upgrade", defaultPhase = LifecyclePhase.DEPLOY)
public class SlonyUpgradeMojo extends DBStewardAbstractMojo {
  
  protected SlonyExecutor slonyExecutor;
  
  /**
   * Relative or absolute path to old (previous) database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.oldDefinitionFile}", property = "oldDefinitionFile", required = true)
  protected File oldDefinitionFile;
  
  /**
   * Relative or absolute path to old (previous) database slony configuration XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.oldSlonyDefinitionFile}", property = "oldSlonyDefinitionFile", required = true)
  protected File oldSlonyDefinitionFile;
  
  /**
   * Relative or absolute path to new (current) database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.newDefinitionFile}", property = "newDefinitionFile", required = true)
  protected File newDefinitionFile;
  
  /**
   * Relative or absolute path to new (current) database slony configuration XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.newSlonyDefinitionFile}", property = "newSlonyDefinitionFile", required = true)
  protected File newSlonyDefinitionFile;
  
  /**
   * Slony Replica Set IDs to process
   */
  @Parameter(defaultValue = "${project.dbsteward.slonyReplicaSetIds}", property = "slonyReplicaSetIds", required = true)
  protected String[] slonyReplicaSetIds;
  
  /**
   * Slony Configuration for node connections etc
   */
  @Parameter(property = "slonyConfiguration", required = true)
  protected Map slonyConfiguration;

  /**
   * Difference the two database definitions specified in plugin config
   * Upgrade the database cluster with slonik execute script et al
   * 
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    // do common setup from DBStewardAbstractMojo
    super.execute();
    
    this.slonyExecutor = new SlonyExecutor();
    slonyExecutor.setPluginLog(getLog());

    getLog().info("Diffing DBSteward definitions for slony upgrade");
    getLog().info("  Old: " + oldDefinitionFile.getPath());
    getLog().info("  Old: " + oldSlonyDefinitionFile.getPath());
    getLog().info("  New: " + newDefinitionFile.getPath());
    getLog().info("  New: " + newSlonyDefinitionFile.getPath());

    String[] args = {
      "--generateslonik",
      "--requireslonyid",
      "--oldxml=" + oldDefinitionFile.getPath(),
      "--oldxml=" + oldSlonyDefinitionFile.getPath(),
      "--newxml=" + newDefinitionFile.getPath(),
      "--newxml=" + newSlonyDefinitionFile.getPath()
    };
    runDbsteward(args);

    getLog().info("Upgrading slony replicated database " + dbName + " on " + dbHost + ":" + dbPort);
    
    // confirm slonik output files and execute them
    for (String replicaSetId : this.slonyReplicaSetIds) {

      // calculate upgrade file prefix
      String upgradeReplicaSetSlonikFilePrefix = outputDir + File.separator
                + FilenameUtils.getBaseName(newDefinitionFile.getPath())
                + "_upgrade_slony_replica_set_" + replicaSetId;

      String upgradeStage1SlonikFileName = upgradeReplicaSetSlonikFilePrefix + "_stage1.slonik";
      // confirm stage 1 slonik output file is present
      File upgradeStage1SlonikFile = new File(upgradeStage1SlonikFileName);
      if (!upgradeStage1SlonikFile.exists()) {
        throw new MojoExecutionException("DBSteward output slonik stage 1 file " + upgradeStage1SlonikFileName + " does not exist. Check DBSteward execution output.");
      }

      String upgradeStage2SQLFileName = upgradeReplicaSetSlonikFilePrefix + "_stage2_data1.sql";
      // confirm stage 2 SQL DML output file is present
      File upgradeStage2SQLFile = new File(upgradeStage2SQLFileName);
      if (!upgradeStage2SQLFile.exists()) {
        throw new MojoExecutionException("DBSteward output SQL DML stage 2 file " + upgradeStage2SQLFileName + " does not exist. Check DBSteward execution output.");
      }

      String upgradeStage3SlonikFileName = upgradeReplicaSetSlonikFilePrefix + "_stage3.slonik";
      // confirm stage 3 slonik output file is present
      File upgradeStage3SlonikFile = new File(upgradeStage3SlonikFileName);
      if (!upgradeStage3SlonikFile.exists()) {
        throw new MojoExecutionException("DBSteward output slonik stage 3 file " + upgradeStage3SlonikFileName + " does not exist. Check DBSteward execution output.");
      }

      String upgradeStage4SQLFileName = upgradeReplicaSetSlonikFilePrefix + "_stage4_data1.sql";
      // confirm stage 4 SQL DML output file is present
      File upgradeStage4SQLFile = new File(upgradeStage4SQLFileName);
      if (!upgradeStage4SQLFile.exists()) {
        throw new MojoExecutionException("DBSteward output SQL DML stage 4 file " + upgradeStage4SQLFileName + " does not exist. Check DBSteward execution output.");
      }

      getLog().info("Replica Set ID " + replicaSetId + " upgrade stage1: slonik execute " + upgradeStage1SlonikFile);
      this.slonyExecutor.executeFile(upgradeStage1SlonikFile);

      getLog().info("Replica Set ID " + replicaSetId + " upgrade stage2: psql execute " + upgradeStage2SQLFile);
      this.dbExecutor.executeFile(dbName, upgradeStage2SQLFile);

      getLog().info("Replica Set ID " + replicaSetId + " upgrade stage3: slonik execute " + upgradeStage3SlonikFile);
      this.slonyExecutor.executeFile(upgradeStage3SlonikFile);

      getLog().info("Replica Set ID " + replicaSetId + " upgrade stage4: psql execute " + upgradeStage4SQLFile);
      this.dbExecutor.executeFile(dbName, upgradeStage4SQLFile);

    }
    
  }

}

