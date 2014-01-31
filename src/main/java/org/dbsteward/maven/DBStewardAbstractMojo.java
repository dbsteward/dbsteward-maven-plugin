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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

/**
 * DBSteward common Mojo bits
 *
 * @author nicholas.kiraly
 */
public abstract class DBStewardAbstractMojo extends AbstractMojo {

  /**
   * DBSteward relative or absolute binary - should be available on your path
   * example values: dbsteward if you installed DBSteward via PEAR
   * /home/dev/DBSteward/bin/dbsteward your DBSteward working copy bin, if you
   * git cloned it
   */
  @Parameter(defaultValue = "dbsteward", property = "dbstewardBinaryPath", required = true)
  protected File dbstewardBinaryPath;

  /**
   * DBSteward --outputdir value specification. You generally want to leave this
   * the project.build.directory so your DBSteward output files end up in
   * /target/
   */
  @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
  protected File outputDir;

  /**
   * Run DBSteward binary with the specified parameters
   *
   * @param args
   * @throws org.codehaus.plexus.util.cli.CommandLineException
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  protected void runDbsteward(String... args) throws MojoExecutionException {
    Commandline commandLine = new Commandline();
    commandLine.setExecutable(dbstewardBinaryPath.getAbsolutePath());

    Arg outputdir_arg = commandLine.createArg();
    outputdir_arg.setValue("--outputdir=" + outputDir);

    if (!outputDir.exists()) {
      getLog().warn("outputdir " + outputDir + " does not exist, creating");
      outputDir.mkdir();
    }

    for (String arg : args) {
      Arg _arg = commandLine.createArg();
      _arg.setValue(arg);
    }
    PluginLogStreamConsumer pluginInfoStream = new PluginLogStreamConsumer(getLog());
    StringStreamConsumer errorStream = new StringStreamConsumer();

    try {
      int returnCode = CommandLineUtils.executeCommandLine(commandLine, pluginInfoStream, errorStream, 10);
      if (returnCode != 0) {
        throw new MojoExecutionException("Unexpected DBSteward Error. Error Buffer = " + errorStream.getOutput());
      }
    } catch (CommandLineException cle) {
      getLog().error("DBSteward Execution Exception: " + cle.getMessage(), cle);
    }
  }

}
