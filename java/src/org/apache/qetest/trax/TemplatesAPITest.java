/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2000, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 *
 * TemplatesAPITest.java
 *
 */
package org.apache.qetest.trax;

import org.apache.qetest.*;
import org.apache.qetest.xsl.*;

// Import all relevant TRAX packages
import javax.xml.transform.*;
import javax.xml.transform.stream.*;    // We assume Features.STREAM for some tests

// Needed SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;

// Needed DOM classes
import org.w3c.dom.Node;
import org.w3c.dom.Document;

// javax JAXP classes for parser pluggability
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// java classes
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

//-------------------------------------------------------------------------

/**
 * Basic API coverage test for the Templates class of TRAX.
 * @author shane_curcuru@lotus.com
 */
public class TemplatesAPITest extends XSLProcessorTestBase
{

    /**
     * Cheap-o filename for various output files.
     *
     */
    protected OutputNameManager outNames;

    /** Cheap-o filename set for both API tests and exampleSimple. */
    protected XSLTestfileInfo simpleTest = new XSLTestfileInfo();

    /** Name of a stylesheet with xsl:output HTML. */
    protected String outputFormatXSL = null;

    /** Cache the relevant system property. */
    protected String saveXSLTProp = null;

    /** Allow user to override our default of Xalan 2.x processor classname. */
    public static final String XALAN_CLASSNAME =
        "org.apache.xalan.processor.TransformerFactoryImpl";

    /** NEEDSDOC Field PROCESSOR_CLASSNAME          */
    protected String PROCESSOR_CLASSNAME = "processorClassname";

    /** NEEDSDOC Field processorClassname          */
    protected String processorClassname = XALAN_CLASSNAME;

    /** NEEDSDOC Field TRAX_PROCESSOR_XSLT          */
    public static final String TRAX_PROCESSOR_XSLT = "javax.xml.transform.TransformerFactory";

    /** Known outputFormat property name from outputFormatTest  */
    public static final String OUTPUT_FORMAT_NAME = "cdata-section-elements";

    /** Known outputFormat property value from outputFormatTest  */
    public static final String OUTPUT_FORMAT_VALUE = "cdataHere";

    /** NEEDSDOC Field TRAX_SUBDIR          */
    public static final String TRAX_SUBDIR = "trax";

    /** Default ctor initializes test name, comment, numTestCases. */
    public TemplatesAPITest()
    {

        numTestCases = 1;  // REPLACE_num
        testName = "TemplatesAPITest";
        testComment = "Basic API coverage test for the Templates class of TRAX";
    }

    /**
     * Initialize this test - Set names of xml/xsl test files, cache system property.  
     *
     * NEEDSDOC @param p
     *
     * NEEDSDOC ($objectName$) @return
     */
    public boolean doTestFileInit(Properties p)
    {

        // Used for all tests; just dump files in xapi subdir
        File outSubDir = new File(outputDir + File.separator + TRAX_SUBDIR);

        if (!outSubDir.mkdirs())
            reporter.logWarningMsg("Could not create output dir: "
                                   + outSubDir);

        outNames = new OutputNameManager(outputDir + File.separator + TRAX_SUBDIR
                                         + File.separator + testName, ".out");

        // Used for API coverage and exampleSimple
        String testBasePath = inputDir + File.separator + TRAX_SUBDIR
                              + File.separator;
        String goldBasePath = goldDir + File.separator + TRAX_SUBDIR
                              + File.separator;

        simpleTest.xmlName = testBasePath + "TransformerAPIParam.xml";
        simpleTest.inputName = testBasePath + "TransformerAPIParam.xsl";
        simpleTest.goldName = goldBasePath + "TransformerAPIParam.out";
        outputFormatXSL = testBasePath + "TransformerAPIOutputFormat.xsl";

        // Cache trax system property
        saveXSLTProp = System.getProperty(TRAX_PROCESSOR_XSLT);

        reporter.logInfoMsg(TRAX_PROCESSOR_XSLT + " property is: "
                            + saveXSLTProp);

        // Check if user wants to use a processor other than Xalan 2.x
        processorClassname = testProps.getProperty(PROCESSOR_CLASSNAME,
                                                   XALAN_CLASSNAME);

        // @todo fix: user should be able to specify -processorClassname 
        //  on the command line to override the system properties

        reporter.logInfoMsg(PROCESSOR_CLASSNAME + " property is: "
                            + processorClassname);
        reporter.logInfoMsg(TRAX_PROCESSOR_XSLT + " property is: "
                            + System.getProperty(TRAX_PROCESSOR_XSLT));

        return true;
    }

    /**
     * Cleanup this test - reset the cached system property trax.processor.xslt.  
     *
     * NEEDSDOC @param p
     *
     * NEEDSDOC ($objectName$) @return
     */
    public boolean doTestFileClose(Properties p)
    {

        if (saveXSLTProp == null)
        {
            System.getProperties().remove(TRAX_PROCESSOR_XSLT);
        }
        else
        {
            System.getProperties().put(TRAX_PROCESSOR_XSLT, saveXSLTProp);
        }

        return true;
    }

    /**
     * TRAX Templates: cover newTransformer(), 
     * getOutputProperties() APIs and basic functionality.
     *
     * NEEDSDOC ($objectName$) @return
     */
    public boolean testCase1()
    {
        reporter.testCaseInit("TRAX Templates: cover APIs and basic functionality");

        TransformerFactory factory = null;
        try
        {
            factory = TransformerFactory.newInstance();
        }
        catch (Exception e)
        {
            reporter.checkFail(
                "Problem creating Processor; cannot continue testcase");
            reporter.logThrowable(reporter.ERRORMSG, e,
                                  "Problem creating Processor");
            return true;
        }

        try
        {
            // Cover APIs newTransformer(), getOutputProperties()
            Templates templates =
                factory.newTemplates(new StreamSource(simpleTest.inputName));
            Transformer transformer = templates.newTransformer();

            reporter.check((transformer != null), true,
                           "newTransformer() is non-null for "
                           + simpleTest.inputName);

            Properties outputFormat = templates.getOutputProperties();

            reporter.check((outputFormat != null), true,
                           "getOutputProperties() is non-null for "
                           + simpleTest.inputName);
            reporter.logHashtable(reporter.STATUSMSG, outputFormat,
                                  "getOutputProperties for " + simpleTest.inputName);
        }
        catch (Exception e)
        {
            reporter.checkErr("newTransformer/getOutputProperties threw: "
                              + e.toString());
            reporter.logThrowable(reporter.STATUSMSG, e,
                                  "newTransformer/getOutputProperties threw:");
        }

        try
        {
            Templates templates2 =
                factory.newTemplates(new StreamSource(outputFormatXSL));
            Properties outputFormat2 = templates2.getOutputProperties();

            reporter.check((outputFormat2 != null), true,
                           "getOutputProperties() is non-null for "
                           + outputFormatXSL);
            reporter.logHashtable(reporter.STATUSMSG, outputFormat2,
                                  "getOutputProperties for " + outputFormatXSL);

            String tmp = outputFormat2.getProperty(OUTPUT_FORMAT_NAME);
            reporter.check(tmp, OUTPUT_FORMAT_VALUE, "outputProperty " + OUTPUT_FORMAT_NAME + " has known value ?" + tmp + "?");
            // HACK: check for another value instead; should cdata-section-elements come back?
            tmp = outputFormat2.getProperty("omit-xml-declaration");
            reporter.check(tmp, "yes", "outputProperty omit-xml-declaration has known value ?" + tmp + "?");
        }
        catch (Exception e)
        {
            reporter.checkErr("outputFormat() is html... threw: "
                              + e.toString());
            reporter.logThrowable(reporter.STATUSMSG, e,
                                  "outputFormat() is html... threw:");
        }
        reporter.logTraceMsg("Functionality of Transformers covered in TransformerAPITest, elsewhere");
        reporter.testCaseClose();

        return true;
    }


    /**
     * Convenience method to print out usage information - update if needed.  
     *
     * NEEDSDOC ($objectName$) @return
     */
    public String usage()
    {

        return ("Common [optional] options supported by TemplatesAPITest:\n"
                + "(Note: assumes inputDir=.\\tests\\api)\n"
                + "-processorClassname classname.of.processor  (to override setPlatformDefaultProcessor to Xalan 2.x)\n"
                + super.usage());
    }


    /**
     * Main method to run test from the command line - can be left alone.  
     * @param args command line argument array
     */
    public static void main(String[] args)
    {

        TemplatesAPITest app = new TemplatesAPITest();

        app.doMain(args);
    }
}