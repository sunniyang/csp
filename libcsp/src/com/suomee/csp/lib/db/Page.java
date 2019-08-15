package com.suomee.csp.lib.db;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {
	private List<T> records;
	private int total;
	public List<T> getRecords() {
		return records;
	}
	public void setRecords(List<T> records) {
		this.records = records;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	
	public Page() {
		this.records = new ArrayList<T>();
		this.total = 0;
	}
}
