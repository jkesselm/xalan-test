/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
package org.apache.qetest.dtm;

import java.io.StringReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.xpath.objects.XMLStringFactoryImpl;

import org.apache.xml.dtm.*;
import org.apache.xml.dtm.ref.*;


/**
 * Unit test for DTMManager/DTM
 *
 * Loads an XML document from a file (or, if no filename is supplied,
 * an internal string), then dumps its contents. Replaces the old
 * version, which was specific to the ultra-compressed implementation.
 * (Which, by the way, we probably ought to revisit as part of our ongoing
 * speed/size performance evaluation.)
 *
 * %REVIEW% Extend to test DOM2DTM, incremental, DOM view of the DTM, 
 * whitespace-filtered, indexed/nonindexed, ...
 * */
public class TestDTMIterator {

static final String[] TYPENAME=
  { "NULL",
    "ELEMENT",
    "ATTRIBUTE",
    "TEXT",
    "CDATA_SECTION",
    "ENTITY_REFERENCE",
    "ENTITY",
    "PROCESSING_INSTRUCTION",
    "COMMENT",
    "DOCUMENT",
    "DOCUMENT_TYPE",
    "DOCUMENT_FRAGMENT",
    "NOTATION",
    "NAMESPACE"
  };

  public static void main(String argv[])
  {
  	System.out.println("\nHELLO THERE AND WELCOME TO THE WACKY WORLD OF ITERATORS \n");
    try
    {
		// Pick our input source
		Source source=null;
		if(argv.length<1)
		{
			String defaultSource=
				"<?xml version=\"1.0\"?>\n"+
				"<Document>"+
				"<A><B><C><D><E><F/></E></D></C></B></A><Aa/>"+
				"</Document>";

			source=new StreamSource(new StringReader(defaultSource));
		}
		else if (argv.length>1 &&  "X".equalsIgnoreCase(argv[1]))
		{
			// XNI stream startup goes here
			// Remember to perform Schema validation, to obtain PSVI annotations
		}
		else
		{
			// Read from a URI via whatever mechanism the DTMManager prefers
			source=new StreamSource(argv[0]);
		}
	
      // Get a DTM manager, and ask it to load the DTM "uniquely",
      // with no whitespace filtering, nonincremental, but _with_
      // indexing (a fairly common case, and avoids the special
      // mode used for RTF DTMs).
      DTMManager manager= new DTMManagerDefault().newInstance(new XMLStringFactoryImpl());
      DTM dtm=manager.getDTM(source, true, null, false, true);

	  // Get the root node and then the first child.
	  int dtmRoot = dtm.getDocument();
	  int child = dtm.getFirstChild(dtmRoot);
	  int sndChild = dtm.getFirstChild(child);
      
	  // Get a Iterator for CHILD:: axis and set startNode <Document>
      DTMAxisIterator iter = dtm.getAxisIterator(Axis.CHILD);
      iter.setStartNode(child);

	  System.out.println("#### First Iterator for <Document>\n");			   
	  printNode(dtm, iter, child, " ");
	  // Iterate the axis and print out node info.
      for (int nextNode = iter.next(); DTM.NULL != nextNode;
              nextNode = iter.next())
		  printNode(dtm, iter, nextNode, " ");

	  // Get a second Iterator of Descendants, and get the last node.	  
	  DTMAxisIterator iter2 = dtm.getAxisIterator(Axis.DESCENDANT);
	  iter2.setStartNode(sndChild);


	  System.out.println("#### Second Iterator\n");
	  printNode(dtm, iter, sndChild, " ");
	  // Iterate the axis and print out node info.
      for (int nextNode = iter2.next(); DTM.NULL != nextNode;
              nextNode = iter2.next())
		  printNode(dtm, iter2, nextNode, " ");
	  int lastNode = iter2.getLast();


	  // Get a third itertor of Ancestors starting from the last descendant
	  // of pervious iterator.
	  DTMAxisIterator revIter = dtm.getAxisIterator(Axis.ANCESTOR);
	  revIter.setStartNode(lastNode);

	  System.out.println("#### Third Iterator\n");
	  printNode(dtm, iter, lastNode, " ");
	  // Iterate the axis and print out node info.
      for (int nextNode = revIter.next(); DTM.NULL != nextNode;
              nextNode = revIter.next())
		  printNode(dtm, revIter, nextNode, " ");

    }
    catch(Exception e)
      {
        e.printStackTrace();
      }
  }
  
  static void printNode(DTM dtm, DTMAxisIterator iter, int nodeHandle, String indent)
  {
    // Briefly display this node
    // Don't bother displaying namespaces or attrs; we do that at the
    // next level up.
    // %REVIEW% Add namespace info, type info, ...

    // Formatting hack -- suppress quotes when value is null, to distinguish
    // it from "null".
    String value=dtm.getNodeValue(nodeHandle);
    String vq=(value==null) ? "" : "\"";

    System.out.println(indent+
		       "Node "+nodeHandle+": "+
		       TYPENAME[dtm.getNodeType(nodeHandle)]+" \""+
		       dtm.getNodeNameX(nodeHandle)+ " : " +
			   dtm.getNodeName(nodeHandle)+
		       "\" expandedType="+dtm.getExpandedTypeID(nodeHandle)+

		       "\n"+
		       indent+
			   "\tQName Info: "+
			   //"Prefix= " +dtm.getPrefix(kid)+
			   " LocalName="+"\""+dtm.getLocalName(nodeHandle)+"\""+
			   " URI= "+"\""+dtm.getNamespaceURI(nodeHandle)+"\""+

		       "\n"+
		       indent+
			   "\tIterator Info: "+
			   " StartNode= "+"\""+iter.getStartNode()+"\""+
			   " Postion="+"\""+iter.getPosition()+"\""+
			   " LastNode="+"\""+iter.getLast()+"\""+
			   " Reverse Axis? = "+"\""+iter.isReverse()+"\""+

		       "\n"+
		       indent+
		       "\tParent=" + dtm.getParent(nodeHandle) +
		       " FirstChild=" + dtm.getFirstChild(nodeHandle) +
		       " NextSib=" + dtm.getNextSibling(nodeHandle)+
			   " Level=" + dtm.getLevel(nodeHandle)+"\n"+
		       
		       indent+
		       "\tValue=" + vq + value + vq
		       ); 
  }
  
}