/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.qetest.xsl;

import org.apache.qetest.FileBasedTest;
import org.apache.qetest.Logger;
import org.apache.qetest.QetestUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Properties;
import java.util.Vector;

/**
 * Execute an instance of an org.apache.qetest.xsl.XSLProcessorTestBase.
 *
 * Cheap-o (for now) way to run qetest or Xalan tests directly
 * from an Ant build file.  Current usage:
 * <code>
 * &lt;taskdef name="QetestTask" classname="org.apache.qetest.xsl.XSLTestAntTask"/>
 *  &lt;target name="test">
 *      &lt;QetestTask
 *          test="Minitest"
 *          loggingLevel="50"
 *          consoleLoggingLevel="40"
 *          inputDir="../tests/api"
 *          goldDir="../tests/api-gold"
 *          outputDir="../tests/minitest"
 *          logFile="../tests/minitest/log.xml"
 *          flavor="trax"
 *       />
 * </code>
 * To be improved: I'd like to basically convert XSLTestHarness
 * into an Ant task, so you can run multiple tests at once.
 * Other obvious improvements include an AntLogger implementation
 * of Logger and better integration with the Project and
 * the various ways build scripts use properties.
 * Also, various properties should really have default values.
 *
 * Blatantly ripped off from org.apache.tools.ant.taskdefs.Java Ant 1.3
 *
 * @author <a href="mailto:shane_curcuru@lotus.com">Shane Curcuru</a>
 * @author Stefano Mazzocchi <a href="mailto:stefano@apache.org">stefano@apache.org</a>
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class XSLTestAntTask extends Task
{

    /** Representation of command line to run for our test.  */
    protected CommandlineJava commandLineJava = new CommandlineJava();

    /** If we should fork another JVM to execute the test.  */
    protected boolean fork = false;

    /** If forking, current dir to set new JVM in.  */
    protected File dir = null;

    /** Alternate Ant output file to use.  */
    protected File out;

    /** 
     * If Ant errors/problems should throw a BuildException.  
     * Note: This does not fail if the test fails, only on a 
     * serious error or problem running the test.
     */
    protected boolean failOnError = false;


    //-----------------------------------------------------
    //-------- Implementations for test-related parameters --------
    //-----------------------------------------------------

    /**
     * Test parameter: Name of test class to execute.
     *
     * Replacement for Java's setClassname property; accepts the
     * name of a specific Test subclass.  Note that we use
     * {@link org.apache.qetest.QetestUtils.testClassForName(String, String[], String) QetestUtils.testClassForName}
     * to actually get the FQCN of the class to run; this allows
     * users to just specify the name of the class itself
     * (e.g. SystemIdTest) and have it work properly.
     * We search the following default packages in order if needed:
     * <ul>
     * <li>org.apache.qetest.xsl</li>
     * <li>org.apache.qetest.trax</li>
     * <li>org.apache.qetest.xalanj2</li>
     * <li>org.apache.qetest.xalanj1</li>
     * <li>org.apache.qetest</li>
     * </ul>
     *
     * @param testClassname FQCN or just bare classname
     * of test to run
     */
    public void setTest(String testClassname)
    {

        String[] testPackages = { "org.apache.qetest.xsl",
                                  "org.apache.qetest.trax",
                                  "org.apache.qetest.xalanj2",
                                  "org.apache.qetest.xalanj1",
                                  "org.apache.qetest" };

        //@todo update to not be so roundabout
        Class clazz =
            QetestUtils.testClassForName(testClassname, testPackages,
                                         "org.apache.qetest.xsl.Minitest");


        // Note the wisdom of defaulting to the Minitest 
        //  is not obvious even to me; but it's something
        commandLineJava.setClassname(clazz.getName());
    }


    /**
     * Test parameter: Set the loggingLevel used in this test.
     *
     * @param ll loggingLevel passed to test for all
     * non-console output; 0=very little output, 99=lots
     * @see org.apache.qetest.Reporter#setLoggingLevel(int)
     */
    public void setLoggingLevel(int ll)
    {

        // Is this really the simplest way to stuff data into 
        //  objects in the 'proper Ant way'?
        commandLineJava.createArgument().setLine("-"
                                                 + Logger.OPT_LOGGINGLEVEL
                                                 + " "
                                                 + Integer.toString(ll));
    }


    /**
     * Test parameter: Set the consoleLoggingLevel used in this test.
     *
     * @param ll loggingLevel used just for console output; here,
     * the default log going to Ant's console
     * @see org.apache.qetest.ConsoleLogger
     */
    public void setConsoleLoggingLevel(int ll)
    {
        commandLineJava.createArgument().setLine(
            "-ConsoleLogger.loggingLevel " + Integer.toString(ll));
    }


    /**
     * Test parameter: inputDir, root of input files tree (required).
     *
     * //@todo this should have a default, since without a valid
     * value most tests will just return an error
     * @param d Path to look for input files in: should be the
     * root of the applicable tests/api, tests/conf, etc. tree
     * @see org.apache.qetest.FileBasedTest#OPT_INPUTDIR
     */
    public void setInputDir(Path d)
    {

        commandLineJava.createArgument().setValue(
            "-" + FileBasedTest.OPT_INPUTDIR);
        commandLineJava.createArgument().setPath(d);
    }


    /**
     * Test parameter: outputDir, dir to put outputs in.
     * @param d where the test will put it's output files
     * @see org.apache.qetest.FileBasedTest#OPT_OUTPUTDIR
     */
    public void setOutputDir(Path d)
    {

        commandLineJava.createArgument().setValue(
            "-" + FileBasedTest.OPT_OUTPUTDIR);
        commandLineJava.createArgument().setPath(d);
    }


    /**
     * Test parameter: goldDir, root of gold files tree.
     * @param d Path to look for gold files in: should be the
     * root of the applicable tests/api-gold, tests/conf-gold, etc. tree
     * @see org.apache.qetest.FileBasedTest#OPT_GOLDDIR
     */
    public void setGoldDir(Path d)
    {

        commandLineJava.createArgument().setValue(
            "-" + FileBasedTest.OPT_GOLDDIR);
        commandLineJava.createArgument().setPath(d);
    }


    /**
     * Test parameter: logFile, where to put XMLFileLogger output.
     * @param f File(name) to send our 'official' results to via
     * an {@link org.apache.qetest.XMLFileLogger XMLFileLogger}
     */
    public void setLogFile(File f)
    {
        commandLineJava.createArgument().setValue("-" + Logger.OPT_LOGFILE);
        commandLineJava.createArgument().setFile(f);  // Check if this is what the test is expecting
    }


    //-----------------------------------------------------
    //-------- Implementations from Java task --------
    //-----------------------------------------------------

    /**
     * Execute this task.
     *
     * Basically just calls the
     * {@link #executeJava() executeJava() worker method} to do
     * all the work of executing the task.  Then checks the
     * failOnError member to see if we should throw an exception.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException
    {

        int err = -1;


        if ((err = executeJava()) != 0)
        {
            if (failOnError)
            {
                throw new BuildException("QetestAntTask execution returned: "
                                         + err, location);
            }
            else
            {
                log("QetestAntTask Result: " + err, Project.MSG_ERR);
            }
        }
    }


    /**
     * Worker method to do the execution and return a return code.
     *
     * @return the return code from the execute java class if it
     * was executed in a separate VM (fork = "yes").
     *
     * @throws BuildException
     */
    public int executeJava() throws BuildException
    {

        String classname = commandLineJava.getClassname();


        if (classname == null)
        {
            throw new BuildException("Classname must not be null.");
        }

        if (fork)
        {
            log("Forking " + commandLineJava.toString(), Project.MSG_VERBOSE);

            return run(commandLineJava.getCommandline());
        }
        else
        {
            if (commandLineJava.getVmCommand().size() > 1)
            {
                log("JVM args ignored when same JVM is used.",
                    Project.MSG_WARN);
            }

            if (dir != null)
            {
                log("Working directory ignored when same JVM is used.",
                    Project.MSG_WARN);
            }

            log("Running in same VM "
                + commandLineJava.getJavaCommand().toString(), Project.MSG_VERBOSE);
            run(commandLineJava);

            return 0;
        }
    }


    /**
     * Set the classpath to be used for this test.
     *
     * @param s classpath used for running the test
     */
    public void setClasspath(Path s)
    {
        createClasspath().append(s);
    }


    /**
     * Creates a nested classpath element
     *
     * @return classpath element to set for this test
     */
    public Path createClasspath()
    {
        return commandLineJava.createClasspath(project).createPath();
    }


    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     *
     * @param r reference to the CLASSPATH
     */
    public void setClasspathRef(Reference r)
    {
        createClasspath().setRefid(r);
    }


    /**
     * Creates a nested arg element.
     *
     * @return Argument to send to our test
     */
    public Commandline.Argument createArg()
    {
        return commandLineJava.createArgument();
    }


    /**
     * Set the forking flag.
     *
     * @param s true if we should fork; false otherwise
     */
    public void setFork(boolean s)
    {
        this.fork = s;
    }


    /**
     * Creates a nested jvmarg element.
     *
     * @return Argument to send to our JVM if forking
     */
    public Commandline.Argument createJvmarg()
    {
        return commandLineJava.createVmArgument();
    }


    /**
     * Set the command used to start the VM (only if fork==false).
     *
     * @param s vm command used
     */
    public void setJvm(String s)
    {
        commandLineJava.setVm(s);
    }


    /**
     * Add a nested sysproperty element.
     *
     * @param sysp to send to our test/JVM
     */
    public void addSysproperty(Environment.Variable sysp)
    {
        commandLineJava.addSysproperty(sysp);
    }


    /**
     * Throw a BuildException if process returns non 0.
     *
     * @param fail if we should fail on serious errors
     */
    public void setFailonerror(boolean fail)
    {
        failOnError = fail;
    }


    /**
     * The working directory of the process, if forked.
     *
     * @param d current directory for test, if forked
     */
    public void setDir(File d)
    {
        this.dir = d;
    }


    /**
     * File the output of the process is redirected to.
     *
     * @param out output file for Ant output (not just test output)
     */
    public void setOutput(File out)
    {
        this.out = out;
    }


    /**
     * -mx or -Xmx depending on VM version
     *
     * @param max max Java memory to use for test execution
     */
    public void setMaxmemory(String max)
    {

        if (Project.getJavaVersion().startsWith("1.1"))
        {
            createJvmarg().setValue("-mx" + max);
        }
        else
        {
            createJvmarg().setValue("-Xmx" + max);
        }
    }


    /**
     * Executes the given classname with the given arguments as if
     * it was a command line application.
     * Explicitly adds test-specific args from our members.
     *
     * @param command object to execute
     *
     * @throws BuildException thrown if IOException thrown internally
     */
    private void run(CommandlineJava command) throws BuildException
    {

        ExecuteJava exe = new ExecuteJava();


        exe.setJavaCommand(command.getJavaCommand());
        exe.setClasspath(command.getClasspath());
        exe.setSystemProperties(command.getSystemProperties());

        if (out != null)
        {
            try
            {
                exe.setOutput(new PrintStream(new FileOutputStream(out)));
            }
            catch (IOException io)
            {
                throw new BuildException(io, location);
            }
        }

        exe.execute(project);
    }


    /**
     * Executes the given classname with the given arguments in a separate VM.
     *
     * @param command line args to execute
     *
     * @return status from VM execution
     *
     * @throws BuildException thrown if IOException thrown internally
     */
    private int run(String[] command) throws BuildException
    {

        FileOutputStream fos = null;


        try
        {
            Execute exe = null;


            if (out == null)
            {
                exe = new Execute(
                    new LogStreamHandler(
                    this, Project.MSG_INFO, Project.MSG_WARN), null);
            }
            else
            {
                fos = new FileOutputStream(out);
                exe = new Execute(new PumpStreamHandler(fos), null);
            }

            exe.setAntRun(project);

            if (dir == null)
            {
                dir = project.getBaseDir();
            }
            else if (!dir.exists() ||!dir.isDirectory())
            {
                throw new BuildException(
                    dir.getAbsolutePath() + " is not a valid directory",
                    location);
            }

            exe.setWorkingDirectory(dir);
            exe.setCommandline(command);

            try
            {
                return exe.execute();
            }
            catch (IOException e)
            {
                throw new BuildException(e, location);
            }
        }
        catch (IOException io)
        {
            throw new BuildException(io, location);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException io){}
            }
        }
    }


    /**
     * Executes the given classname with the given arguments as if it
     * was a command line application.
     *
     * @param classname of Java class to execute
     * @param args for Java class
     *
     * @throws BuildException not thrown
     */
    protected void run(String classname, Vector args) throws BuildException
    {

        CommandlineJava cmdj = new CommandlineJava();


        cmdj.setClassname(classname);

        for (int i = 0; i < args.size(); i++)
        {
            cmdj.createArgument().setValue((String) args.elementAt(i));
        }

        run(cmdj);
    }


    /**
     * Clear out the arguments to this java task.
     */
    public void clearArgs()
    {
        commandLineJava.clearJavaArgs();
    }
}