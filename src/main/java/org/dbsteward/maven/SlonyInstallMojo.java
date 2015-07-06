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
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Install slony into the defined nodes for replication for the defined database
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "slony-install", defaultPhase = LifecyclePhase.DEPLOY)
public class SlonyInstallMojo extends DBStewardAbstractMojo {
  
  protected SlonyExecutor slonyExecutor;
  
  /**
   * Relative or absolute path to DBSteward database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.definitionFile}", property = "definitionFile", required = true)
  protected File definitionFile;
  
  /**
   * Relative or absolute path to DBSteward database definition slony configuration XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.slonyDefinitionFile}", property = "slonyDefinitionFile", required = true)
  protected File slonyDefinitionFile;
  
  /**
   * Slony Replica Set IDs to process
   */
  @Parameter(defaultValue = "${project.dbsteward.slonyReplicaSetIds}", property = "slonyReplicaSetIds", required = true)
  protected String[] slonyReplicaSetIds;

  /**
   * Compile creation sql specified in plugin config
   * Install slony replication for nodes specified in slonyDefinitionFile
   * 
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    // do common setup from DBStewardAbstractMojo
    super.execute();
    
    this.slonyExecutor = new SlonyExecutor();
    slonyExecutor.setPluginLog(getLog());

    getLog().info("Compiling DBSteward definition with slony file");
    getLog().info(" Path:" + definitionFile.getPath());
    getLog().info(" Path:" + slonyDefinitionFile.getPath());

    String[] args = {
      "--generateslonik",
      "--requireslonyid",
      "--xml=" + definitionFile.getPath(),
      "--xml=" + slonyDefinitionFile.getPath()
    };
    runDbsteward(args);

    // confirm sql output file for execution
    String buildSqlFileName = FilenameUtils.getBaseName(definitionFile.getPath());
    buildSqlFileName = outputDir + File.separator + buildSqlFileName + "_build.sql";
    // confirm definitionFile's output file in the output dir
    File buildSqlFile = new File(buildSqlFileName);
    if (!buildSqlFile.exists()) {
      throw new MojoExecutionException("DBSteward output build file " + buildSqlFileName + " does not exist. Check DBSteward execution output.");
    }
    
    // confirm slonnik output files for execution
    for(String replicaSetId : this.slonyReplicaSetIds) {
      String replicaSetSlonikFilePrefix = FilenameUtils.getBaseName(definitionFile.getPath());
      replicaSetSlonikFilePrefix += "_slony_replica_set_" + replicaSetId;
      
      String replicaSetCreateNodesSlonikFileName = outputDir + File.separator + replicaSetSlonikFilePrefix + "_create_nodes.slonik";
      // confirm slonik output file in the output dir
      File replicaSetCreateNodesSlonikFile = new File(replicaSetCreateNodesSlonikFileName);
      if (!replicaSetCreateNodesSlonikFile.exists()) {
        throw new MojoExecutionException("DBSteward output slonik create nodes file " + replicaSetCreateNodesSlonikFileName + " does not exist. Check DBSteward execution output.");
      }
      
      String replicaSetSubscribeSlonikFileName = outputDir + File.separator + replicaSetSlonikFilePrefix + "_subscribe.slonik";
      // confirm slonik output file in the output dir
      File replicaSetSubscribeSlonikFile = new File(replicaSetSubscribeSlonikFileName);
      if (!replicaSetSubscribeSlonikFile.exists()) {
        throw new MojoExecutionException("DBSteward output slonik subscribe file " + replicaSetSubscribeSlonikFileName + " does not exist. Check DBSteward execution output.");
      }
      
      //TODO: create database replicas that do not exist before building cluster with slonik commands
      
      // execute the create nodes and subscribe scripts
      slonyExecutor.executeFile(replicaSetCreateNodesSlonikFile);
      slonyExecutor.executeFile(replicaSetSubscribeSlonikFile);
    }
  }

}
