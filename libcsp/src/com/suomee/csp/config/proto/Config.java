package com.suomee.csp.config.proto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Config implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, List<String>> lists;
	private Map<String, Map<String, String>> maps;
	public Map<String, List<String>> getLists() {
		return lists;
	}
	public void setLists(Map<String, List<String>> lists) {
		this.lists = lists;
	}
	public Map<String, Map<String, String>> getMaps() {
		return maps;
	}
	public void setMaps(Map<String, Map<String, String>> maps) {
		this.maps = maps;
	}
}
