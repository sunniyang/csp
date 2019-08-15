package com.suomee.csp.lib.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author yangguang
 * 配置读取接口
 * 配置文件默认的路径在src下，confs.xml文件中的config标签指定了配置文件的根路径
 * 配置文件格式：
 * <a>
 *   <b>
 *   xxx
 *   </b>
 *   
 *   <c>
 *   xxx1
 *   xxx2
 *   xxx3
 *   </c>
 *   
 *   <d>
 *   key1=v1
 *   key2=v2
 *   key3=v3
 *   </d>
 * </a>
 * 
 * <a1>
 *   yyy
 * </a1>
 * 获取方法（假如文件名为sytem.xml）：
 * Configs.getConfig("system").getValue("/a/b") = xxx
 * Configs.getConfig("system").getList("/a/c") = List[xxx1, xxx2, xxx3]
 * Configs.getConfig("system").getMap("/a/d") = Map[key1->v1, key2->v2, key3->v3]
 */
public class Configs {
	private static Configs instance = null;
	private static final Object mutex = new Object();
	static Configs getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new Configs();
				}
			}
		}
		return instance;
	}
	
	private Map<String, Config> configs;
	
	private Configs() {
		this.configs = new HashMap<String, Config>();
	}
	
	public static Configs.Config parse(String content) throws Exception {
		if (content == null || content.isEmpty()) {
			return null;
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		InputStream contentInput = null;
		try {
			contentInput = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>" + content + "</root>").getBytes("UTF-8"));
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(contentInput);
			
			TreeMap<String, Element> elements = new TreeMap<String, Element>();
			
			Element root = null;
			NodeList nodes = doc.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element)nodes.item(i);
					if (el.getTagName().equals("root")) {
						root = el;
						break;
					}
				}
			}
			if (root == null) {
				return null;
			}
			elements.put("/", root);
			
			Map<String, List<String>> lists = new HashMap<String, List<String>>();
			Map<String, Map<String, String>> maps = new HashMap<String, Map<String, String>>();
			while (!elements.isEmpty()) {
				Map.Entry<String, Element> entry = elements.firstEntry();
				String path = entry.getKey();
				Element el = entry.getValue();
				elements.remove(path);
				
				List<String> list = new ArrayList<String>();
				Map<String, String> map = new HashMap<String, String>();
				nodes = el.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element cel = (Element)node;
						list.add(cel.getTagName());
						map.put(cel.getTagName(), "");
						String cpath = path.equals("/") ? path + cel.getTagName() : path + "/" + cel.getTagName();
						elements.put(cpath, cel);
						continue;
					}
					if (node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.CDATA_SECTION_NODE) {
						continue;
					}
					String text = node.getTextContent().trim();
					if (text.isEmpty()) {
						continue;
					}
					if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
						list.add(text);
						map.put(text, "");
						continue;
					}
					String[] textLines = text.split("\r|\n");
					for (String textLine : textLines) {
						textLine = textLine.trim();
						if (textLine.isEmpty()) {
							continue;
						}
						list.add(textLine);
						int eIndex = textLine.indexOf("=");
						if (eIndex < 0) {
							//list.add(textLine);
							map.put(textLine, "");
						}
						else {
							String k = textLine.substring(0, eIndex);
							String v = textLine.substring(eIndex + 1);
							//list.add(textLine.substring(0, eIndex));
							map.put(k, v);
						}
					}
				}
				if (list.size() > 0) {
					List<String> ls = lists.get(path);
					if (ls == null) {
						ls = new LinkedList<String>();
						lists.put(path, ls);
					}
					ls.addAll(list);
				}
				if (map.size() > 0) {
					Map<String, String> ms = maps.get(path);
					if (ms == null) {
						ms = new HashMap<String, String>();
						maps.put(path, ms);
					}
					ms.putAll(map);
				}
			}
			return new Configs.Config(lists, maps);
		}
		finally {
			try {
				if (contentInput != null) {
					contentInput.close();
				}
			}
			catch (Exception e) {}
		}
	}
	
	public static void addConfig(String name, Map<String, List<String>> lists, Map<String, Map<String, String>> maps) {
		Configs.getInstance().configs.put(name, new Config(lists, maps));
	}
	
	public static Config getConfig() {
		return getConfig("default");
	}
	
	public static Config getConfig(String name) {
		if (name == null) {
			return Config.EMPTY;
		}
		Config config = Configs.getInstance().configs.get(name);
		if (config == null) {
			return Config.EMPTY;
		}
		return config;
	}
	
	public static Collection<Config> getAllConfigs() {
		return Configs.getInstance().configs.values();
	}
	
	public static class Config {
		private static Config EMPTY = new Config(null, null);
		
		private Map<String, List<String>> lists;
		private Map<String, Map<String, String>> maps;
		
		private Config(Map<String, List<String>> lists, Map<String, Map<String, String>> maps) {
			if (lists == null) {
				lists = new HashMap<String, List<String>>();
			}
			if (maps == null) {
				maps = new HashMap<String, Map<String, String>>();
			}
			this.lists = lists;
			this.maps = maps;
		}
		
		public boolean has(String key) {
			if (key == null) {
				return false;
			}
			if (this.lists.containsKey(key)) {
				return true;
			}
			if (this.maps.containsKey(key)) {
				return true;
			}
			return false;
		}
		
		public String getValue(String key) {
			if (key == null) {
				return null;
			}
			List<String> list = this.lists.get(key);
			if (list == null) {
				return null;
			}
			return list.get(0);
		}
		
		public List<String> getList(String key) {
			if (key == null) {
				return null;
			}
			return this.lists.get(key);
		}
		
		public Set<String> getSet(String key) {
			if (key == null) {
				return null;
			}
			Map<String, String> set = this.maps.get(key);
			if (set == null) {
				return null;
			}
			return set.keySet();
		}
		
		public Map<String, String> getMap(String key) {
			if (key == null) {
				return null;
			}
			return this.maps.get(key);
		}
		
		public Map<String, List<String>> getLists() {
			return lists;
		}
		
		public Map<String, Map<String, String>> getMaps() {
			return maps;
		}
	}
}
