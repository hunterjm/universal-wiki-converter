package com.atlassian.uwc.ui;

import java.util.Hashtable;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.mindtouch.LinkParser.Type;

public class ContentPermission {

	private Type type;
	private String name;
	private boolean group = false;
	
	public enum Type {
		VIEW,
		EDIT;
		public static Type getType(String type) {
			if(type == "View") return VIEW;
			if(type == "Edit") return EDIT;
			return null;
		}
		public static String toString(Type type) {
			if(type == VIEW) return "View";
			if(type == EDIT) return "Edit";
			return null;
		}
	}
	
	public ContentPermission() {
		
	}
	
	public ContentPermission(Hashtable table) {
		this.type = Type.getType((String) table.get("type"));
		this.name = (String) table.get("userName");
		if(this.name == null) {
			this.name = (String) table.get("groupName");
			this.group = true;
		}
	}
	
	public ContentPermission(Type type, String name, boolean group) {
		this.type = type;
		this.name = name;
		this.group = group;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isGroup() {
		return this.group;
	}
	
	public Hashtable toTable() {
		Hashtable table = new Hashtable();
		table.put("type", Type.toString(this.type));
		if(this.group == true) table.put("groupName", this.name);
		else table.put("userName", this.name);
		return table;
	}

}
