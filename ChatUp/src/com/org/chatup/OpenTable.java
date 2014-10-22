package com.org.chatup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

import com.csvreader.CsvReader;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.google.code.geocoder.model.LatLngBounds;
import com.thoughtworks.selenium.webdriven.commands.GetAlert;



/**
 * Servlet implementation class OpenTable
 * The servlet returns top 5 restaurants nearby along with timings where table is available
 */
@WebServlet("/restaurant")
public class OpenTable extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String zip=null;
	String city=null;
	int count=0;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OpenTable() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String query = request.getParameter("query");
		String lat = request.getParameter("lat");
		String lon = request.getParameter("lon");
		String time = request.getParameter("time");
		ArrayList<JSONObject> results = null;
		if(query.matches("eat")){
			getZipAndCity(lat, lon);
			results=getRestaurants(time);
			JSONObject obj= new JSONObject();
			try {
				obj.put("query", query);
				JSONArray result = new JSONArray(results);
				obj.put("results", result);
			} catch (JSONException e) {
				writer.print("No results found for this query");
			}
			writer.print(obj);
		}
	}
	
	public static void main(String[] args) {
		OpenTable ot=new OpenTable();
		ot.getZipAndCity("40.4943747", "-74.4400266");
	}
	/**
	 * Method to get search results using OpenTable API
	 * @param time 
	 * @return ArrayList<SearchResponse>
	 */
	public ArrayList<JSONObject> getRestaurants(String time){

		ArrayList<JSONObject> respList=new ArrayList<JSONObject>();	
		try {

			//call the API. The API key has to be generated. 
			URL url = new URL("https://opentable.herokuapp.com/api/restaurants?city="+ URLEncoder.encode(city));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			//add request header
			con.setRequestMethod("GET");
			InputStream is=con.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
			StringBuilder responseStrBuilder = new StringBuilder();
			String respString;
			while ((respString = streamReader.readLine()) != null)
				responseStrBuilder.append(respString);
			JSONObject resp= new JSONObject(responseStrBuilder.toString());

			// parse json
			JSONArray jsonMainNode = resp.optJSONArray("restaurants");

			for(int i=0; i < jsonMainNode.length(); i++)
			{
				if(count<5){
					JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
					JSONObject data=new JSONObject();
					JSONArray times;
					data.put("name", jsonChildNode.optString("name"));
						times= getTimings(jsonChildNode.optString("reserve_url"),time);
 
					if(times!=null && times.length()>0){

						data.put("timeAvailable", times);
						respList.add(data);
						count++;
					}				
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return respList;		

	}

	private JSONArray getTimings(String reserveUrl, String currTime){

		System.out.println(reserveUrl);
		JSONArray times=new JSONArray();
  
    	DateTimeFormatter fmt = DateTimeFormat.forPattern("h:mm aa");   	
    	DateTime curr=fmt.parseDateTime(currTime);
    	
		WebDriver driver = new FirefoxDriver();		
		try{
			driver.navigate().to(reserveUrl);

			Select select = new Select(driver.findElement(By.id("SearchNav_OTDateSearch_cboHourList")));
			
			List<WebElement> options=select.getOptions();
			for(WebElement option:options){
				DateTime optTime= fmt.parseDateTime(option.getAttribute("value"));
				if(curr.isBefore(optTime) || curr.isEqual(optTime) ){
					select.selectByVisibleText(option.getText());
					break;
				}
			}
			

			WebElement searchButton = driver.findElement(By.id("SearchNav_btnFindTable"));
			searchButton.click();

			List<WebElement> avTimes = driver.findElements(By.className("ResultTimes"));
			if(!avTimes.get(0).getText().isEmpty()){
				String[] strArray=avTimes.get(0).getText().split("\n");
				for (String time : strArray) {
					if(!time.isEmpty() && !time.equals("")){
						String[] strArr=time.trim().split(" ");
						if(!strArr[0].trim().equals(""))
						times.put(strArr[0].trim()); 
					}
				}					
			}	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			driver.close();
		}		
		return times;
	}

	private void getZipAndCity(String latitude, String longitude) {
		
		// Read from zipcodes database and find zip using latitude and longitude
	//		try {
	//
	//			CsvReader zips = new CsvReader("E:\\graduate\\badri\\zip_codes_states.csv");		
	//			zips.readHeaders();
	//			while (zips.readRecord())
	//			{
	//				String zipcode = zips.get("zip");
	//				String lat = zips.get("latitude");
	//				String lon = zips.get("longitude"); 
	//				String cityname = zips.get("city");				
	//				if(lat.contains(latitude) && lon.contains(longitude)){
	//					zip=zipcode;
	//					city=cityname;
	//					break;
	//				}
	//			}
	//			zips.close();			
	//		} catch (FileNotFoundException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}

		try{
			Geocoder geocoder= new Geocoder();
			LatLng location=new LatLng(latitude, longitude);
			GeocoderRequest geocoderRequest=new GeocoderRequestBuilder().setLocation(location).getGeocoderRequest();
		    GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		    
		    for (int i = 0 ; i < geocoderResponse.getResults().size() ; ++i)
	          {
	            GeocoderResult super_var1 = geocoderResponse.getResults().get(i);
	            for (int j = 0 ; j < super_var1.getAddressComponents().size() ; ++j)
	            {
	              GeocoderAddressComponent super_var2 = super_var1.getAddressComponents().get(j);
	              for (int k = 0 ; k < super_var2.getTypes().size() ; ++k)
	              {
	                //find city
	                if(super_var2.getTypes().get(k).equals("locality"))
	                {
	                  //put the city name in the form
	                  city = super_var2.getLongName();
	                }
	              }
	            }
	          }
		    System.out.println("city..."+city);
		}catch(Exception e){
			e.printStackTrace();
		}
		

	}
}

