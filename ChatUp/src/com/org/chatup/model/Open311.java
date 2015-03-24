package com.org.chatup.model;

public class Open311 {
	private final String query;
	
	public Open311(String message) {
		this.query = message;
	}
	
	public String sendRequest() {
		return "success";
	}
}
