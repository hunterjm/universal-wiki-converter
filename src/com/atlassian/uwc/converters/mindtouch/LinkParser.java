package com.atlassian.uwc.converters.mindtouch;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class LinkParser extends DefaultXmlParser {

	private static String target = "";
	private static String alias = "";
	private static String parent = "";
	private NoSvnFilter nosvnfilter = new NoSvnFilter(); 
	Logger log = Logger.getLogger(this.getClass());
	public enum Type {
		ANCHOR,
		INTERNAL,
		EXTERNAL;
		public static Type getType(Attributes attributes) {
			String href = attributes.getValue("href");
			if(href != null && href.startsWith("#")) return ANCHOR;
			String val = attributes.getValue("rel");
			if (val == null) return null;
			if (val.contains("internal") || val.contains("custom")) return INTERNAL;
			if (val.contains("external")) return EXTERNAL;
			return null;
		}
	}
	Pattern hrefend = Pattern.compile("[^\\/]+$");
	private static boolean isImage = false;
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String href = attributes.getValue("href");
		Type type = Type.getType(attributes);
		if (href == null || type == null) {
			log.warn("Link href/type is undefined.");
			return;
		}
		// There are some really weird inline editor related links we need to skip
		int len = attributes.getLength();
		// Loop through all attributes
		for(int i = 0; i < len; i++) {
			String sAttrName = attributes.getLocalName(i);
			if(
				sAttrName.equalsIgnoreCase("onclick") ||
				sAttrName.equalsIgnoreCase("onmouseover") ||
				sAttrName.equalsIgnoreCase("onmouseout")
			) {
				log.warn("Link has a " + sAttrName + " attribute, which is not supported.");
				return;
			}
		}
		switch (type) {
		case INTERNAL:
			isImage = isImage(href);
			if (isImage) {
				getImageTarget(href);
			}
			else {
				Matcher endFinder = hrefend.matcher(href);
				target = endFinder.find()?endFinder.group():href;
				target = findInternalLink(target, href, getPage());
				if (target.contains("_")) target = fixUnderscores(URLDecoder.decode(target), href, getPage());
			}
			break;
		case EXTERNAL:
		case ANCHOR:
			target = href;
		}
	}
	
	protected String findInternalLink(String input, String href, Page page) {
		
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File("/Users/jhunter/tmp/exported_mindtouch_links.xml"));
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile(String.format("/links/link[@href=%s]/text()", escape(href)));
			
			String result = expr.evaluate(doc);
			log.debug("Found Link for " + href + ": " + result);
			if(!result.isEmpty()) return result;
			
		} catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
        	se.printStackTrace();
        } catch(XPathExpressionException xpe) {
        	xpe.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		
		// Fallback
		return input;
		
	}
	
	private String escape(String s) {
		Matcher matcher = Pattern.compile("['\"]").matcher(s);
		StringBuilder buffer = new StringBuilder("concat(");
		int start = 0;
		while (matcher.find()) {
			buffer.append("'")
				.append(s.substring(start, matcher.start()))
				.append("',");
			buffer.append("'".equals(matcher.group()) ? "\"'\"," : "'\"',");
			start = matcher.end();
		}
		if (start == 0) {
			return "'" + s + "'";
		}
		return buffer.append("'")
				.append(s.substring(start))
				.append("'")
				.append(")")
				.toString();
	}
	
	Pattern parents = Pattern.compile("http://[^\\/]+\\/(.*)$", Pattern.DOTALL);
	protected String fixUnderscores(String input, String href, Page page) {
		if (getProperties() != null && getProperties().containsKey("exportdir")) {
			String exportdir = getProperties().getProperty("exportdir", null);
			if (exportdir == null) return input;
			File export = new File(exportdir);
			if (!export.exists() || !export.isDirectory()) {
				log.warn("exportdir does not exist or is not a directory. " + exportdir);
				return input;
			}
			Matcher parentFinder = parents.matcher(href);
			if (parentFinder.find()) {
				String parentString = parentFinder.group(1);
				String[] parentArray = parentString.split("/");
				File[] exportfiles = export.listFiles(nosvnfilter);
				File rootFile = null;
				//get list of export pages to work with (ignore mindtouch root page, if it's there)
				boolean foundMindtouchAsRoot = false;
				for (File file : exportfiles) {
					if (file.isDirectory() && file.getName().endsWith("_RedWiki_subpages")) {
						foundMindtouchAsRoot = true;
						rootFile = file;
						break; //should only be one
					}
				}
				File[] rootFiles;
				if (!foundMindtouchAsRoot) rootFiles = exportfiles;
				else rootFiles = rootFile.listFiles(nosvnfilter);
				//walk the tree to get the leaf file for the input/href/page 
				File leafFile = getFile(parentArray, rootFiles);
				if (leafFile == null) return input.replace("_", " ");
				return fixUnderscores(input, leafFile);
			}
			else return input;
			
		}
		return input.replace("_", " ");
	}
	
	private File getFile(String[] ancestors, File[] thisdir) {
		if (thisdir == null) return null;
		String current = ancestors[0];
		File found = null;
		//find the file associated with the top ancestor
		for (File file : thisdir) {
			String cleaned = current.replaceAll("_", "");
			cleaned = cleaned.replaceAll("[%]\\w\\w", ""); //hack: remove url encoding
			String regex = "^\\d+\\Q"+cleaned+"\\E\\.xml$";
			String noUS = file.getName().replaceAll("_", "");
			if (noUS.matches(regex)) { 
				found = file;
				break;
			}
		}
		if (ancestors.length == 1) return found;
		if (found == null) return null;
		//get branch of ancestors
		int len = ancestors.length;
		String[] branch = new String[len-1];
		System.arraycopy(ancestors, 1, branch, 0, branch.length);
		//get subpages dir
		File subdir = getSubpagesDir(found);
		//recurse - walk the tree
		return getFile(branch, subdir.listFiles(nosvnfilter));
	}
	
	private File getSubpagesDir(File input) {
		String dir = input.getAbsolutePath().replaceFirst("\\.xml$", "_subpages");
		return new File(dir);
	}

	Pattern untilUS = Pattern.compile("([^_]*)(_)");
	protected String fixUnderscores(String input, File file) {
		//places where input has underscore, but file doesn't need spaces
		String current = file.getName().replaceFirst("^\\d+_", "");
		current = current.replaceFirst("\\.xml$", "");
		Matcher usFinder = untilUS.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (usFinder.find()) {
			found = true;
			String part = usFinder.group();
			if (current.startsWith(part)) {
				if (current.length()>=part.length()) {
					current = current.substring(part.length());
					continue;
				}
				else break;
			}
			String replacement = usFinder.group(1) + " ";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			usFinder.appendReplacement(sb, replacement);
			int minus = (usFinder.group(1)).length();
			if (current.length()>=minus) {
				current = current.substring(minus);
			}
		}
		if (found) {
			usFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern image = new ImageParser().filename;
	protected boolean isImage(String input) {
		Matcher imageFinder = image.matcher(input);
		return imageFinder.find();
	}

	protected String getImageTarget(String input) {
		Matcher imageFinder = image.matcher(input);
		if (imageFinder.find()) {
			target = imageFinder.group(1);
			parent = imageFinder.group(2);
			return target;
		}
		target = input;
		return input;
	}

	public void endElement(String uri, String localName, String qName) {
		if(target == "" || alias == "") return;
		if (target.equals(alias)) alias = "";
		else alias += "|";
		String link;
		if (isImage) {
			if (parent == null || getPage() == null || parent.equals(getPage().getName()))
				target = "^" + target;
			else     
				target = parent + "^" + target;
		}
		link = "[" + replaceTokens(alias) + replaceTokens(target) + "]";
		appendOutput(link);
		alias = target = "";
		isImage = false;
	}
	
	private String replaceTokens(String s) {
		
		return s.replace("_", "UNDERSCORETOKEN")
				.replace("*", "ASTERISKTOKEN")
				.replace("-", "DASHTOKEN")
				.replace("+", "PLUSTOKEN");
		
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		alias += String.copyValueOf(ch, start, length);
	}
}
