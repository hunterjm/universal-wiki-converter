package com.atlassian.uwc.converters.mindtouch;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.ui.ContentPermission;

public class SecurityParser extends DefaultXmlParser {

	/**
	 * type of security tag
	 */
	public enum Type {
		/**
		 * Make sure we are in the right section
		 */
		SECURITY,
		/**
		 * corresponds with page permissions
		 */
		RESTRICTION,
		/**
		 * corresponds with user/group permissions
		 */
		GRANT,
		/**
		 * corresponds with what role is granted
		 */
		ROLE,
		/**
		 * corresponds with who the grant is for
		 */
		USER,
		/**
		 * corresponds with the grant user
		 */
		USERNAME,
		/**
		 * corresponds with who the grant is for
		 */
		GROUP,
		/**
		 * corresponds with the grant user
		 */
		GROUPNAME;
		/**
		 * @param qName
		 * @return determines what type is associated with the given qName.
		 */
		static Type getType(String qName) {
			if ("security".equals(qName)) return SECURITY;
			if ("restriction".equals(qName)) return RESTRICTION;
			if ("grant".equals(qName)) return GRANT;
			if ("role".equals(qName)) return ROLE;
			if ("user".equals(qName)) return USER;
			if ("username".equals(qName)) return USERNAME;
			if ("group".equals(qName)) return GROUP;
			if ("groupname".equals(qName)) return GROUPNAME;
			return null;
		}
	}
	
	public enum PagePerms {
		PUBLIC,
		SEMI_PUBLIC,
		PRIVATE;
		static PagePerms getPerm(String s) {
			if("Private".equals(s)) return PRIVATE;
			if("Semi-Public".equals(s)) return SEMI_PUBLIC;
			return PUBLIC;
		}
		static String toString(PagePerms p) {
			if(PRIVATE.equals(p)) return "Private";
			if(SEMI_PUBLIC.equals(p)) return "Semi-Public";
			return "Public";
		}
	}
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Keeps track of which tag/element we're currently examining, and any enclosing tags.
	 */
	private static Stack<String> nested;
	
	private static String parent = "";
	private static Type type;
	private static PagePerms pagePerm;
	private static ContentPermission.Type perm;
	private static String name;
	private static boolean group = false;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		if (type == null) return;
		if (getPage() == null) return;
		if(!getNested().isEmpty()) this.parent = getNested().peek();
		getNested().push(qName);
		switch (type) {
		case USER:
			if(this.parent.equals("grant"))
				this.group = false;
			break;
		case GROUP:
			if(this.parent.equals("grant"))
				this.group = true;
			break;
		}
		this.type = type;
	}
	
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		if (type == null) return;
		if (getPage() == null) return;
		switch (type) {
		case GRANT:
			// Add page restrictions
			if(this.parent.equals("security") && (this.pagePerm == PagePerms.SEMI_PUBLIC && this.perm == ContentPermission.Type.EDIT) || this.pagePerm == PagePerms.PRIVATE)
				getPage().addPermission(new ContentPermission(this.perm, this.name, this.group));
			// If it is private, and we are adding an edit restriction, also add it as a view restriction
			if(this.parent.equals("security") && this.pagePerm == PagePerms.PRIVATE && this.perm == ContentPermission.Type.EDIT)
				getPage().addPermission(new ContentPermission(ContentPermission.Type.VIEW, this.name, this.group));
			this.perm = null;
			this.name = null;
			this.group = false;
			break;
		}
		this.type = null;
		getNested().pop();
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		if (getPage() == null) return;
		String content = String.copyValueOf(ch, start, length);
		switch(this.type) {
		case RESTRICTION:
			if(this.parent.equals("security"))
				this.pagePerm = PagePerms.getPerm(content);
			break;
		case ROLE:
			if(this.parent.equals("grant")) {
				if(content.equals("Contributor") || content.equals("Admin")) this.perm = ContentPermission.Type.EDIT;
				else this.perm = ContentPermission.Type.VIEW;
			}
			break;
		case USERNAME:
			if(this.parent.equals("user"))
				this.name = content;
			break;
		case GROUPNAME:
			if(this.parent.equals("group"))
				this.name = content;
			break;
		}
	}
	
	/**
	 * @return stack of nested tags
	 */
	private Stack<String> getNested() {
		if (nested == null)
			nested = new Stack<String>();
		return nested;
	}
}
