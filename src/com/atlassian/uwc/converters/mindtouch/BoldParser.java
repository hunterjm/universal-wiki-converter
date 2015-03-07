package com.atlassian.uwc.converters.mindtouch;

import com.atlassian.uwc.converters.xml.SimpleParser;

/**
 * Used to replace tags with Confluence italic syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class BoldParser extends SimpleParser {
	/**
	 * assigns appropriate delimiter when creating
	 */
	public BoldParser () {
		this.delim = "ASTERISKTOKEN"; //uses SimpleParser's methods
	}
}
