package com.org.chatup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Servlet implementation class winescapes
 */
@WebServlet("/winescapes")
public class winescapes extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Scanner scanner;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();

		writer.print("Winescpaes GET method");
		writer.flush();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		
		String varietal = "", country =  "", region = "", type = "", vintage = "";
		String API_KEY = "chatup780480", EMAIL = "vlnvv14@gmail.com", WINE_NAME = "";
		
		//	Get list of all regions, countries, etc. from winescape.net
		ArrayList<String> regionList = region(), countryList = country(), varietalList = varietal(), typeList = type();

		//	Get the JSON request in String format
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while( (str = br.readLine()) != null ){
		    sb.append(str);
		}
		
		//	Get the actual command in String
		String param ="", json = "";
		try {
			JSONObject jsonReq = new JSONObject(sb.toString());
			param = (String) jsonReq.get("request");
			param = param.trim().toLowerCase();
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}		
		
		ArrayList<String> cat = new ArrayList<>();
		for(String t : param.split(" "))
			cat.add(t);

		//	Supporting all combinations (multiple categories)
		
		for(int i = 0; i < cat.size(); i++) {
			String word = cat.get(i), t = "";
			
			if("".equals(region)) {
				region = containsWord(regionList, word);
				
				// check for 2 words match
				if(i < cat.size() - 1) {
					t = containsBoth(regionList, word + " " + cat.get(i + 1));
					if(t != "")
						region = t;
				}
				
				if(! "".equals(region)) {
					continue;
				}
			}
			if("".equals(varietal)) {
				varietal = containsWord(varietalList, word);
				
				if(i < cat.size() - 1) {
					t = containsBoth(varietalList, word + " " + cat.get(i + 1));
					if(t != "")
						varietal = t;
				}
				if(! "".equals(varietal)) { 
					continue;
				}
				
			}
			if("".equals(country)) {
				country = containsWord(countryList, word);
				
				if(i < cat.size() - 1) {
					t = containsBoth(countryList, word + " " + cat.get(i + 1));
					if(t != "")
						country = t;
				}
				
				if(! "".equals(country)) {
					continue;
				}
			}
			if("".equals(type)) {
				type = containsWord(typeList, word);
				
				if(i < cat.size() - 1) {
					t = containsBoth(typeList, word + " " + cat.get(i + 1));
					if(t != "")
						type = t;
				}
				
				if(! "".equals(type)) {
					continue;
				}
			}
		}
		
		//	Extract WINE NAME from REQUEST string
		WINE_NAME = param;
		
		WINE_NAME = WINE_NAME.replace(country, "");
		//System.out.println("Country: " + country);
		
		WINE_NAME = WINE_NAME.replace(type, "");
		//System.out.println("Type: " + type);
		
		WINE_NAME = WINE_NAME.replace(region, "");
		//System.out.println("Region: " + region);
		
		WINE_NAME = WINE_NAME.replace(varietal, "");
		//System.out.println("Varietal: " + varietal);
		
		//	Extract VINTAGE
		if(WINE_NAME.matches(".*\\d.*")) {
			scanner = new Scanner(WINE_NAME);
			Scanner in = scanner.useDelimiter("[^0-9]+");
			vintage = in.nextInt() + "";
			WINE_NAME = WINE_NAME.replaceAll("[0-9]", "").trim();
			//System.out.println("Vintage: " + vintage);
		}
		//System.out.println("WineName: " + WINE_NAME);

		
		if(("".compareTo(country) == 0) && ("".compareTo(region) == 0) && ("".compareTo(type) == 0) && ("".compareTo(varietal) == 0) && ("".compareTo(vintage) == 0)) {
		
			json = Jsoup.connect("http://winescapes.net/api/?api_key=" + API_KEY + "&cmd=" + "WS_SEARCH" + "&email_id=" + EMAIL + "&ws_winename=" + WINE_NAME).ignoreContentType(true).execute().body();
		}
		else {
			//	Build URL based on parameters
			if("".equals(WINE_NAME.trim())) {
				WINE_NAME = "%20";
			}
			
			//	Base URL
			String url = "http://winescapes.net/api/?api_key=" + API_KEY + "&cmd=" + "WS_ADVANCED_SEARCH" +
						 "&email_id=" + EMAIL + "&ws_winename=" + WINE_NAME + "&ws_country=" + country + 
						 "&ws_region=" + region + "&ws_varietal=" + varietal + "&ws_winetype=" + type + 
						 "&ws_vintage=" + vintage;
			
			//System.out.println("URL: " + url);
			json = Jsoup.connect(url).ignoreContentType(true).execute().body();
		}
		
		JSONArray jsonArr = new JSONArray();
		String failure_msg = "";

		try {
			JSONObject jObject = new JSONObject(json);
			
			//	failure_msg NOT EMPTY if API returns error
			if("FAIL".compareTo(jObject.getString("status")) == 0)
				failure_msg = jObject.getString("response_text");
			
			//	SUCCESS. Now check for 
			else {
				JSONArray jArray = jObject.getJSONArray("wine_list");
				for(int i = 0; i < jArray.length(); i++) {
					JSONObject jsonObj = new JSONObject();
					JSONObject jObj = jArray.getJSONObject(i);
					jsonObj.put("wine_name", jObj.getString("wine_name"));
					jsonArr.put(jsonObj);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		//	API returned ERROR if failure_msg NOT EMPTY
		if("".compareTo(failure_msg) != 0)
			writer.print(failure_msg);
		
		else {
			//	wine_list is EMPTY
			if(jsonArr.toString().length() <= 2) {
				writer.print("No wines found for this category");
			}
			
			else {
				//	Parse and return the result in JSON
				String result = "";
	            
				try {
	            	for (int i = 0; i < jsonArr.length(); i++) {

	                    JSONObject jObj = jsonArr.getJSONObject(i);
	                    result += (i + 1) + ". " + jObj.getString("wine_name") + "\\n";
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
				
	            result = "{\"response\":\"" + result + "\"}";
				writer.print(result);
			}
		}

		writer.flush();
	}
	
	
	private String containsWord(ArrayList<String> list, String word) {
		int length;
		
		for(int i = 0; i < list.size(); i++) {
			String temp = list.get(i);

			if(temp.length() >= word.length()) {
				length = word.length();
			}
			else {
				length = temp.length();
			}
			
			if(temp.substring(0, length).compareTo(word.substring(0, length)) == 0) {
				if(temp.compareTo(word) == 0) {
					return temp;
				}	
			}
		}
		return "";
	}
	
	private String containsBoth(ArrayList<String> list, String word) {
		
		for(String temp : list) {
			if(temp.equals(word)) {
				return temp;
			}
		}
		
		return "";
	}
	
	
	
	
	//	Get Variety list
	protected ArrayList<String> varietal() {
		
		ArrayList<String> varietal = new ArrayList<>();
		
		try {
			String json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=GET_LOV_VARIETAL").ignoreContentType(true).execute().body();
			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("varietal");
			
			for(int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				varietal.add(jObj.getString("lov_value").trim().toLowerCase());
			}
			
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		
		return varietal;
	}
	
	//	Get Type list
	protected ArrayList<String> type() {
		
		ArrayList<String> type = new ArrayList<>();
		
		try {
			String json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=GET_LOV_TYPE").ignoreContentType(true).execute().body();
			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("type");
			
			for(int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				type.add(jObj.getString("lov_value").trim().toLowerCase());
			}
			
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		
		return type;
	}
	
	//	Get Region list
	protected ArrayList<String> region() {
		
		ArrayList<String> region = new ArrayList<>();
		
		try {
			String json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=GET_LOV_REGION").ignoreContentType(true).execute().body();
			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("region");
			
			for(int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				region.add(jObj.getString("lov_value").trim().toLowerCase());
				//System.out.println(jObj.getString("lov_value").trim().toLowerCase());
			}
			
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		
		return region;
	}
	
	//	Get Country list
	protected ArrayList<String> country() {
		
		ArrayList<String> country = new ArrayList<>();
		
		try {
			String json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=GET_LOV_COUNTRY").ignoreContentType(true).execute().body();
			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("country");
			
			for(int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				country.add(jObj.getString("lov_value").trim().toLowerCase());
			}
			
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		
		return country;
	}

}
