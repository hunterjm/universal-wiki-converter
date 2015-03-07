package com.atlassian.uwc.converters.mindtouch;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.ui.Page;

/**
 * Subclasses that set the delim field will replace tags with that delim.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class EscapeParser extends DefaultXmlParser {
	private static final String UNDERSCORETOKEN = "UNDERSCORETOKEN";
	private static final String ASTERISKTOKEN = "ASTERISKTOKEN";
	private static final String DASHTOKEN = "DASHTOKEN";
	private static final String PLUSTOKEN = "PLUSTOKEN";
	
	public void endElement(String uri, String localName, String qName) {
		String output = getOutput();
		clearOutput();
		output = output
				.replaceAll("(_|\\*|\\+|-)", "\\\\$1")
				.replace(UNDERSCORETOKEN, "_")
				.replace(ASTERISKTOKEN, "*")
				.replace(DASHTOKEN, "-")
				.replace(PLUSTOKEN, "+")
				.replace("UWCULTOKEN", "*")
				.replace("UWCOLTOKEN", "#");
		appendOutput(output);
	}

}
