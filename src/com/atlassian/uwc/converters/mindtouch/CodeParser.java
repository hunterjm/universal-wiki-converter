package com.atlassian.uwc.converters.mindtouch;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;


/**
 * Used to replace tags with Confluence code macro syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class CodeParser extends DefaultXmlParser {
	private static final String UNDERSCORETOKEN = "UNDERSCORETOKEN";
	private static final String ASTERISKTOKEN = "ASTERISKTOKEN";
	private static final String DASHTOKEN = "DASHTOKEN";
	private static final String PLUSTOKEN = "PLUSTOKEN";
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String lang = "php";
		if(qName.equals("pre")) {
			lang = attributes.getValue("class");
			if(lang == null) lang = "php";
			// Some fiddling with values
			if(lang.equals("js")) lang = "javascript";
			if(lang.equals("xml") || lang.equals("html")) lang = "html/xml";
		}
		appendOutput("{code:language=" + lang + "}");
	}
	
	public void endElement(String uri, String localName, String qName) {
		appendOutput("{code}");
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		appendOutput(String.copyValueOf(ch, start, length)
				.replaceAll("\\\\(\\{|\\}|\\[|\\])", "$1")
				.replace("_", UNDERSCORETOKEN)
				.replace("*", ASTERISKTOKEN)
				.replace("-", DASHTOKEN)
				.replace("+", PLUSTOKEN));
	}
	
}
