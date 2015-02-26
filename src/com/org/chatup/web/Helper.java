package com.org.chatup.web;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public final class Helper {
	
	public Helper() {
	}
	
	public static JSONObject getEncodedJson(HttpServletRequest request) {
		StringBuffer jsonBuilder = new StringBuffer();
		String line = null;
		
		try {
		  BufferedReader reader = request.getReader();
		  while ((line = reader.readLine()) != null)
			  jsonBuilder.append(line);
		  
		  return new JSONObject(jsonBuilder.toString());
		  
		} catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
	}
}
