package com.atlassian.uwc.converters.xml;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Subclasses that set the delim field will replace tags with that delim.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class SimpleParser extends DefaultXmlParser {
	protected String delim = "";
	private static final String SIMPLEPARSERTOKEN = "SIMPLEPARSERTOKEN";

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput(SIMPLEPARSERTOKEN);
	}
	
	Pattern startSpace = Pattern.compile("^(\\s+)");
	Pattern endSpace = Pattern.compile("(\\s+)$");
	public void endElement(String uri, String localName, String qName) {
		
		String output = getOutput();
		clearOutput();
		
		int tokenStart = output.lastIndexOf(SIMPLEPARSERTOKEN);
		if(tokenStart != -1) {
			
			// Replace space at beginning
			String line = output.substring(tokenStart + 17);
			output = output.substring(0, tokenStart);
			String prepend = "";
			Matcher startWhitespace = startSpace.matcher(line);
			if(startWhitespace.find()) {
				prepend = startWhitespace.group(1);
				line = startWhitespace.replaceAll("");
			}
			output += prepend + SIMPLEPARSERTOKEN + line;
			
		}
		
		// Replace space at end
		String append = "";
		Matcher endWhitespace = endSpace.matcher(output);
		if(endWhitespace.find()) {
			append = endWhitespace.group(1);
			output = endWhitespace.replaceAll("");
		}
		
		if(output.endsWith(SIMPLEPARSERTOKEN)) {
			appendOutput(replaceLastToken(output, append));
		} else {
			appendOutput(replaceLastToken(output, delim) + delim + append);
		}
	}
	
	private String replaceLastToken(String output, String replacement) {
		StringBuilder b = new StringBuilder(output);
		b.replace(
				output.lastIndexOf(SIMPLEPARSERTOKEN), 
				output.lastIndexOf(SIMPLEPARSERTOKEN) + SIMPLEPARSERTOKEN.length(), 
				replacement);
		return b.toString();
	}

}
