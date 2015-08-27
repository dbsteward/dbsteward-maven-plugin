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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 *
 * @author nicholas.kiraly
 */
public class SlonyExecutor {

  private Log log;

  public void setPluginLog(Log log) {
    this.log = log;
  }
  
  protected void executeTool(Commandline commandLine, File tool, String... args) throws MojoExecutionException {
    if ( commandLine == null ) {
      commandLine = new Commandline();
    }
    commandLine.setExecutable(tool.getPath());

    for (String arg : args) {
      Arg _arg = commandLine.createArg();
      _arg.setValue(arg);
    }
    PluginLogStreamConsumer pluginInfoStream = new PluginLogStreamConsumer(this.log, PluginLogLevel.LOG_LEVEL_INFO);
    CommandLineUtils.StringStreamConsumer errorStream = new CommandLineUtils.StringStreamConsumer();

    try {
      int returnCode = CommandLineUtils.executeCommandLine(commandLine, pluginInfoStream, errorStream, 0);
      if (returnCode != 0) {
        throw new MojoExecutionException("Unexpected Tool Return Code " + returnCode + " - Error Buffer = " + errorStream.getOutput());
      }
    } catch (CommandLineException cle) {
      this.log.error("Tool Execution Exception: " + cle.getMessage(), cle);
      throw new MojoExecutionException("Tool Execution Exception: " + cle.getMessage());
    }
  }
  
  public void executeFile(File f) throws MojoExecutionException {
    File slonik = new File("slonik");
    String[] args = {
      f.toString()
    };

    Commandline slonikCommandLine = new Commandline();
    // set slonik command working directory to the dir the slonik file f is in
    // in case if a slonik file #includes another slonik file
    // or does an EXECUTE SCRIPT ( FILENAME = ) using a relative path
    slonikCommandLine.setWorkingDirectory(f.getParentFile());

    // NOTICE: if slons aren't running or aren't doing their job,
    // CommandLineUtils.executeCommandLine() may hang for a long time waiting for commands to complete
    executeTool(slonikCommandLine, slonik, args);
  }

}
