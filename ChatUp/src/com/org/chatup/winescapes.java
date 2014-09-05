package com.org.chatup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		String varietal = "", country =  "", region = "", type = "";
		
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
		String param="", json = "";
		try {
			JSONObject jsonReq = new JSONObject(sb.toString());
			param = (String) jsonReq.get("request");
			param = param.trim().toLowerCase();
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}		
		
		ArrayList<String> cat = new ArrayList<>();
		for(String t : param.split(" ")) {
			//System.out.println(t);
			cat.add(t);
		}

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
				
				//System.out.println("Region: " + region);
			}
			if("".equals(varietal)) {
				varietal = containsWord(varietalList, word);
				//System.out.println("Varietal: " + varietal);
				
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
				//System.out.println("Country: " + country);
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
				//System.out.println("Type: " + type);
			}
		}
			
			//System.out.println("Region: " + region);
			//System.out.println("Country: " + country);
			//System.out.println("Varietal: " + varietal);
			//System.out.println("Type: " + type);
		
		
		
		
		if(param.equals("budget")) {
			System.out.println("Budget");
			json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=WINE_PRICE_LIST&wineName=&region=&wineType=&verietal=&price=15&vintage=&country=&lat=40.4862157&lng=-74.45181880000001&userID=280&page=1").ignoreContentType(true).execute().body();
		}
		else {
			json = Jsoup.connect("http://winescapes.net/web/uploadImage.php?command=LATEST_WINE&wineName=&country=" + country + "&region=" + region + "&wineType=" + type + "&varietal=" + varietal + "&vintage=&advanceSearch=true&userID=280&search=true&lat=40.4862157&lng=-74.45181880000001").ignoreContentType(true).execute().body();
		}
		
		JSONArray jsonArr = new JSONArray();
		String json_new = "";
		
		try {
			json_new = "{'x':" + json + "}";
			JSONObject jObject = new JSONObject(json_new);
			JSONArray jArray = jObject.getJSONArray("x");
			
			for(int i = 0; i < jArray.length(); i++) {
				JSONObject jsonObj = new JSONObject();
				JSONObject jObj = jArray.getJSONObject(i);
				jsonObj.put("wine_name", jObj.getString("wine_name"));
				jsonArr.put(jsonObj);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(jsonArr.toString().length() <= 2) {
			writer.print("No wines found for this category");
		}
		else {
			
			if((region.equals("")) && (varietal.equals("")) && (country.equals("")) && (type.equals("")) && (!param.equals("budget"))) {
				
				//	Need to return popular keywords
				String popular = "{\"response\":" + "\"Popular keywords are:\\n1. Chardonnay\\n2. Italy\\n3. Napa Valley"
						+ "\\n4. Red" + "\"}";
				writer.print(popular);
			}
			else {
				//	Parse and return the result in JSON
				String result = jsonArr.toString(), result_new = "{'x':" + result + "}";
	            JSONObject jObject;
	            JSONArray jArray;
	            try {
	                jObject = new JSONObject(result_new);
	                jArray = jObject.getJSONArray("x");
	                result = "";

	                for (int i = 0; i < jArray.length(); i++) {

	                    JSONObject jObj = jArray.getJSONObject(i);
	                    result += (i + 1) + ". " + jObj.getString("wine_name") + "\\n";
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
				
	            result = result.replace("\"", "\\\"");
				result = "{\"response\":\"" + result + "\"}";
				//System.out.println(result);
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
