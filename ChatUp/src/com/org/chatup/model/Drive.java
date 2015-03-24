package com.org.chatup.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Drive {
	private String ACCESS_TOKEN;
	private static final String SINGLE_QUOTE = "'";
	private final String queryType = "?q=fullText contains ";
	private String mimeType = "mimeType = 'application/vnd.google-apps.document'";
	private String query;
	private final String DRIVE_ENDPOINT = "https://www.googleapis.com/drive/v2/files";
	
	public Drive(String query, String access_token) {
		this.ACCESS_TOKEN = access_token;
		this.query = query.replaceAll("[^\\s\\w]", "");
	}
	
	public String getFiles() throws IOException, JSONException {
		String url = DRIVE_ENDPOINT + queryType + SINGLE_QUOTE + query + SINGLE_QUOTE + " and " + mimeType + "&access_token=" + ACCESS_TOKEN;
		String response = null;
//		System.out.println(url.replace(" ", "%20"));
		JSONObject json = readJsonFromUrl(url.replace(" ", "%20"));
//		System.out.println(json);
	    JSONArray files = json.getJSONArray("items");
	    if(files.length() < 1) {
	    	response = "Sorry, nothing found";
	    } else {
	    	response = readFiles(files);
	    }
	    
	    return response.trim();
	}
	
	
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
	
	public String readStringFromUrlAuth(String url, String authToken) throws ClientProtocolException, IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet(url);
		
		if(authToken != null) {
			httpRequest.setHeader("Authorization", "Bearer " + authToken);
		}
		
		HttpResponse httpResponse = httpClient.execute(httpRequest);
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), Charset.forName("UTF-8")));
		String text = readAll(rd);
		
		return text;
	}

	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		String jsonText = readStringFromUrlAuth(url, null);
		JSONObject json = new JSONObject(jsonText);
		
		return json;
	  }
	
	private String readFiles(JSONArray files) throws JSONException, MalformedURLException, IOException {
		String title;
		String fileContents;
		String searchResult;
		StringBuilder result = new StringBuilder();
		
		for(int i = 0; i < files.length(); i++) {
			JSONObject file = files.getJSONObject(i);
			title = file.getString("title");
//			System.out.println("File: " + title + "\n");
			if(file.has("exportLinks")) {
				if(file.getJSONObject("exportLinks").has("text/plain")) {
					fileContents = readStringFromUrlAuth(file.getJSONObject("exportLinks").getString("text/plain"), ACCESS_TOKEN);
					
					searchResult = searchQuery(fileContents);
					if(searchResult.trim().length() > 0) {
						result.append("File: " + title + "\n");
						result.append(searchQuery(fileContents) + "\n\n");
					}
				}
			}
		}
		
		return result.toString();
	}
	
	private String searchQuery(String fileContents) {
		StringBuilder sb = new StringBuilder();
		String[] words = query.split(" ");
		for(String word : words) {
			sb.append(word + "|");
		}
		if(words.length > 1) {
			sb.append(query + "|");
		}
		System.out.println(sb);
		StringBuilder result = new StringBuilder();
//		Pattern pattern = Pattern.compile("[\\W].*\\b(" + sb.substring(0, sb.length() - 1) + ")\\b.*[\\W]", Pattern.CASE_INSENSITIVE);
//		Pattern pattern = Pattern.compile("[\\w\\s]*(" + sb.substring(0, sb.length() - 1) + ")[\\w\\s]*[\\W]", Pattern.CASE_INSENSITIVE);
		Pattern pattern = Pattern.compile("[\\w,‘’“”'&\": -]*(" + sb.substring(0, sb.length() - 1) + ")[\\w,‘’“”'&\": -]*[\\W]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(fileContents);
		
		int count = 0;
		while(matcher.find() && count < 5) {
			result.append("--  " + matcher.group().trim() + "\n");
			count++;
		}
		
		return result.toString().trim();
	}
	
	
	
	
	
	
}
