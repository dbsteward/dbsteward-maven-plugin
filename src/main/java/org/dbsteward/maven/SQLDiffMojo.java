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
import java.util.Collection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Difference the specified DBSteward definition files, outputting the SQL
 * statements to upgrade old to new
 *
 * @author nicholas.kiraly
 */
@Mojo(name = "sql-diff", defaultPhase = LifecyclePhase.COMPILE)
public class SQLDiffMojo extends DBStewardAbstractMojo {

  /**
   * Relative or absolute path to old (previous) database definition XML file
   */
  @Parameter(property = "oldDefinitionFile", required = true)
  protected File oldDefinitionFile;

  /**
   * Relative or absolute path to new (current) database definition XML file
   */
  @Parameter(property = "newDefinitionFile", required = true)
  protected File newDefinitionFile;

  /**
   * Compile upgrade SQL files specified in plugin config
   * 
   * @throws MojoExecutionException
   */
  @Override
  public void execute() throws MojoExecutionException {
    // do common setup from DBStewardAbstractMojo
    super.execute();

    getLog().info("Diffing DBSteward definitions");
    getLog().info("  Old: " + oldDefinitionFile.getPath());
    getLog().info("  New: " + newDefinitionFile.getPath());

    String[] args = {
      "--oldxml=" + oldDefinitionFile.getPath(),
      "--newxml=" + newDefinitionFile.getPath()
    };
    runDbsteward(args);

    // confirm output before returning
    // calculate upgrade files path
    String upgradeSqlFilePrefix = FilenameUtils.getBaseName(newDefinitionFile.getPath());
    upgradeSqlFilePrefix = upgradeSqlFilePrefix + "_upgrade";

    // look for files matching prefix in outputDir
    IOFileFilter fileFilter = new WildcardFileFilter(upgradeSqlFilePrefix + "*.*");
    Collection<File> upgradeSqlFiles = FileUtils.listFiles(outputDir.getAbsoluteFile(), fileFilter, null);

    if (upgradeSqlFiles.isEmpty()) {
      throw new MojoExecutionException("DBSteward outputDir " + outputDir + " not found to contain any upgrade files. Check DBSteward execution output.");
    }

    // store in upgradeSqlFileNameCSL for reference by build chain
    String upgradeSqlFileNameCSL = StringUtils.join(upgradeSqlFiles.iterator(), ",");
    proj.getProperties().setProperty("project.dbsteward.output.upgradeSqlFileNameCSL", upgradeSqlFileNameCSL);
  }

}
