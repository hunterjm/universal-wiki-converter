package com.atlassian.uwc.converters.mindtouch;

import java.io.File;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.ui.Attachment;
import com.atlassian.uwc.ui.Page;

public class ImageParser extends DefaultXmlParser {
	Logger log = Logger.getLogger(this.getClass());
	
	Pattern filename = Pattern.compile("[@]api/deki/files/([0-9]+)/=([^\\/\\?]+?)(?:\\?size=([^\\/\\?]+))?(?:\\?parent=([^\\/]+))?$");
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String output = "";
		String src = attributes.getValue("src");
		if (src == null) {
			log.warn("Image src is undefined.");
			return;
		}
		String type = attributes.getValue("class");
		if(type != null && type.contains("edit")) {
			log.warn("Skipping over edit img.");
			return;
		}
		Matcher filenameFinder = filename.matcher(src);
		if (filenameFinder.find()) {
			String filenameString = filenameFinder.group(2);
			String parent = filenameFinder.group(4);
			parent = URLDecoder.decode(parent);
			if (parent != null)
				output = createImgSyntax(filenameString, parent);
			else 
				output = createImgSyntax(filenameString);
		}
		else { //not dekiwiki image syntax, just return the src in ! chars
			output = createImgSyntax(src);
		}
		appendOutput(output);
	}
	private String createImgSyntax(String filenameString) {
		return "!" + filenameString + "!";
	}
	private String createImgSyntax(String filenameString, String parent) {
		if (getPage() != null && getPage().getName() != null && getPage().getName().equals(parent))
			return createImgSyntax(filenameString);
		return "!" + parent + "^" + filenameString + "!";
	}
}
