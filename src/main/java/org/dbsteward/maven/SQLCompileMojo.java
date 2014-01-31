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
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Compile the specified DBSteward definition files to SQL statements
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "sql-compile", defaultPhase = LifecyclePhase.COMPILE)
public class SQLCompileMojo extends DBStewardAbstractMojo {

  /**
   * Relative or absolute path to DBSteward database definition XML file
   */
  @Parameter(defaultValue = "${project.dbsteward.definitionFile}", property = "definitionFile", required = true)
  protected File definitionFile;

  // import maven session execution instances
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    MavenProject proj = (MavenProject) getPluginContext().get("project");
    getLog().info("Compiling DBSteward definition");
    getLog().info(" Path:" + definitionFile.getPath());

    String[] args = {
      "--xml=" + definitionFile.getPath()
    };
    runDbsteward(args);

    // confirm output before leaving
    // calculate build file path
    String buildSqlPath = FileUtils.basename(definitionFile.getPath(), "xml");
    buildSqlPath = outputDir + File.separator + buildSqlPath;
    // kill trailing . left by basename
    buildSqlPath = buildSqlPath.substring(0, buildSqlPath.length() - 1);
    buildSqlPath = buildSqlPath + "_build.sql";
    // confirm definitionFile's output file in the output dir
    File buildSqlFile = new File(buildSqlPath);
    if (!buildSqlFile.exists()) {
      throw new MojoExecutionException("DBSteward output build file " + buildSqlPath + "does not exist. Check DBSteward execution output.");
    }
    proj.getProperties().setProperty("project.dbsteward.output.buildFileName", buildSqlPath);
    //throw new MojoExecutionException("project = " + this.project.getName());
  }

}
