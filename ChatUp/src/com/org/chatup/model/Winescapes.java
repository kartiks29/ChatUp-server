package com.org.chatup.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class Winescapes {
	
	private final String query;
	String API_KEY = "chatup780480", EMAIL = "vlnvv14@gmail.com", WINE_NAME = "", region = "", 
			varietal = "", country = "", type = "", vintage = "", json = "";
	
	ArrayList<String> regionList, countryList, varietalList, typeList;
	
	String END_POINT = "http://winescapes.net/api/?api_key="
			+ API_KEY + 
			"&cmd=WS_SEARCH" + 
			"&email_id=" 
			+ EMAIL + 
			"&ws_winename="
			;
	
	public Winescapes(String query) {
		this.query = query;
		//	Get list of all regions, countries, etc. from winescape.net
		this.regionList = region();
		this.countryList = country();
		this.varietalList = varietal();
		this.typeList = type();
	}
	
	
	
	
	
	public String calculate() throws JSONException, IOException {
		ArrayList<String> cat = new ArrayList<>();
		String result = "";
		
		for(String t : query.split(" "))
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
		WINE_NAME = query;
		
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
			Scanner scanner = new Scanner(WINE_NAME);
			Scanner in = scanner.useDelimiter("[^0-9]+");
			vintage = in.nextInt() + "";
			WINE_NAME = WINE_NAME.replaceAll("[0-9]", "").trim();
			//System.out.println("Vintage: " + vintage);
		}
		System.out.println("WineName: " + WINE_NAME);

		
		if(("".compareTo(country) == 0) && ("".compareTo(region) == 0) && ("".compareTo(type) == 0) && ("".compareTo(varietal) == 0) && ("".compareTo(vintage) == 0)) {
		
			json = Jsoup.connect("http://winescapes.net/api/?api_key=" + API_KEY + "&cmd=" + "WS_SEARCH" + "&email_id=" + EMAIL + "&ws_winename=" + WINE_NAME).ignoreContentType(true).execute().body();
		}
		else {
			
			System.out.println("Inside!");
			
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
		
		int count = 0;
		
		JSONArray jsonArr = new JSONArray();
		String failure_msg = "";

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
				jsonObj.put("wine_name", jObj.get("wine_name"));
				jsonObj.put("verietal", jObj.get("verietal"));
				jsonObj.put("vintage", jObj.get("vintage"));
				jsonObj.put("upCount", jObj.get("upCount"));
				jsonObj.put("downCount", jObj.get("downCount"));
				jsonObj.put("is_available", jObj.get("is_available"));
				jsonArr.put(jsonObj);

				count++;
				// Display only 3 results
				if(count == 3)
					break;
			}
		}
		
		
		//	API returned ERROR if failure_msg NOT EMPTY
		if("".compareTo(failure_msg) != 0)
			result += failure_msg;
		
		else {
			//	wine_list is EMPTY
			if(jsonArr.toString().length() <= 2) {
				result += "No wines found for this category";
			}
			
			else {
				//	Parse and return the result in JSON
				for (int i = 0; i < count; i++) {

                    JSONObject jObj = jsonArr.getJSONObject(i);
                    result += (i + 1) +
                    		". " + jObj.get("wine_name") + "\n" +
                    		"Varietal: " + jObj.get("verietal") + "\n" +
                    		"Vintage: " + jObj.get("vintage") + "\n" +
                    		"Up Count: " + jObj.get("upCount") + "\n" +
                    		"Down Count: " + jObj.get("downCount") + "\n" +
                    		"Availability: " + jObj.get("is_available") + "\n"
                    		;
                    
                    if(i < count - 1)
                    	result += "\n";
                }
/*				
	            try {
					writer.print(new JSONObject().put("response", result));
					System.out.println("Result: " + (new JSONObject().put("response", result)).getString("response"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
*/					
			}
		}
		
		return result;
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
