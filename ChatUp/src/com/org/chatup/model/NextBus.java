package com.org.chatup.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NextBus {

	private final String query;
	//Provided by app
	private final String location_lat;
	private final String location_long;
	ArrayList<String> agencies;
	HashMap<String, String> agenciesNames;
	
	ArrayList<String> routes;
	HashMap<String, String> routesNames;

	ArrayList<String> stops;
	HashMap<String, Stop> stopsNames;

	public NextBus(String query, String location_lat, String location_long) {
		this.query = query.toLowerCase().trim();
		this.location_lat = location_lat;
		this.location_long = location_long;
	}

	
	public String getNextBus(){
		String[] message = query.split("\\s+");
		System.out.println(message.length);
		if(message.length == 0){
			return help("");
		}
		if(message.length == 1){
			String agencyFound = getAgencyList(message);
			if(message[0] == null || agencyFound == null){
				return help(getAgencyList());
			}
			
			loadRoutesList(agencyFound);		
			return help(getRoutesList());
		}
		if(message.length == 2){
			String agencyFound = getAgencyList(message);
			if(message[0] == null || agencyFound == null){
				return help(getAgencyList());
			}
			
			String routeFound = getRoutesList(agencyFound, message);		
			if(message[1] == null || routeFound == null){
				return help(getRoutesList());
			}
			
			Stop nearestStop = getNearestStop(agencyFound, routeFound, message);
			String predictionsResult = getPredictions(nearestStop, agencyFound, routeFound);
			if (predictionsResult.trim().equals("")){
				predictionsResult = "No Prediction Found";
			}
			return agenciesNames.get(agencyFound) + " " + 
					routesNames.get(routeFound) + " " + 
					nearestStop.title + "\n" + 
					predictionsResult;
		}else if(message.length >= 3){
			String agencyFound = getAgencyList(message);
			if(message[0] == null || agencyFound == null){
				return help(getAgencyList());
			}
			
			String routeFound = getRoutesList(agencyFound, message);		
			if(message[1] == null || routeFound == null){
				return help(getRoutesList());
			}
			
			String stopFound = getStopsList(agencyFound, routeFound, message);
			if(message[2] == null || stopFound == null){
				return help(getStopsList());
			}
			Stop stopAtLocation = getStop(message);
			String predictionsResult = getPredictions(stopAtLocation, agencyFound, routeFound);
			if (predictionsResult.trim().equals("")){
				predictionsResult = "No Prediction Found";
			}
			return agenciesNames.get(agencyFound) + " " + 
			routesNames.get(routeFound) + " " + 
					stopAtLocation.title + "\n" + 
					predictionsResult;
		}else{
			return help("");
		}
	}

	private String help(String message){
		return message + "Please use the following syntax: [agency tag] [route tag] or [agency tag] [route tag] [stop tag]";
	}
	
	private String getAgencyList(String[] message){
		String agencyListString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList";
			System.out.println("Requesting: " + url);
			agencyListString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document agencyList = null;
		try {
			agencyList = loadXMLFromString(agencyListString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agencies = new ArrayList<String>();
		agenciesNames = new HashMap<String, String>();
		NodeList agencyTagsList = agencyList.getElementsByTagName("agency");
		for (int i = 0; i < agencyTagsList.getLength(); i++) {
			Node node = agencyTagsList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				agencies.add(node.getAttributes().getNamedItem("tag").getNodeValue());
				agenciesNames.put(node.getAttributes().getNamedItem("tag").getNodeValue(), node.getAttributes().getNamedItem("title").getNodeValue());
			}
		}
		for(String agency:agencies){
			if(message[0].trim().toLowerCase().equals(agency.toLowerCase())){
				return agency;
			}
		}
		return null;
	}
	
	private String getAgencyList(){
		String result = "Agencies\n";
		for(String agency:agencies){
			result += agency + " - " + agenciesNames.get(agency) + "\n";
		}
		return result;
	}
	
	private String getRoutesList(String agency, String[] message){
		String routesString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=" + agency;
			System.out.println("Requesting: " + url);
			routesString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document routesList = null;
		try {
			routesList = loadXMLFromString(routesString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		routes = new ArrayList<String>();
		routesNames = new HashMap<String, String>();

		NodeList routesTagList = routesList.getElementsByTagName("route");
		for (int i = 0; i < routesTagList.getLength(); i++) {
			Node node = routesTagList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				//System.out.println(node.getAttributes().getNamedItem("tag").getNodeValue());
				routes.add(node.getAttributes().getNamedItem("tag").getNodeValue());
				routesNames.put(node.getAttributes().getNamedItem("tag").getNodeValue(), node.getAttributes().getNamedItem("title").getNodeValue());
			}
		}
		for(String route:routes){
			if(message[1].trim().toLowerCase().equals(route.toLowerCase())){
				return route;
			}
		}
		return null;
	}
	
	private void loadRoutesList(String agency){
		String routesString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=" + agency;
			System.out.println("Requesting: " + url);
			routesString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document routesList = null;
		try {
			routesList = loadXMLFromString(routesString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		routes = new ArrayList<String>();
		routesNames = new HashMap<String, String>();

		NodeList routesTagList = routesList.getElementsByTagName("route");
		for (int i = 0; i < routesTagList.getLength(); i++) {
			Node node = routesTagList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				//System.out.println(node.getAttributes().getNamedItem("tag").getNodeValue());
				routes.add(node.getAttributes().getNamedItem("tag").getNodeValue());
				routesNames.put(node.getAttributes().getNamedItem("tag").getNodeValue(), node.getAttributes().getNamedItem("title").getNodeValue());
			}
		}
	}
	
	private String getRoutesList(){
		String result = "Routes\n";
		for(String route:routes){
			result += route + " - " + routesNames.get(route) + "\n";
		}
		return result;
	}
	
	
	private String getStopsList(String agency, String route, String[] message){
		String stopsString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=" + route;
			System.out.println("Requesting: " + url);
			stopsString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(stopsString);
		Document stopsList = null;
		try {
			stopsList = loadXMLFromString(stopsString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stops = new ArrayList<String>();
		stopsNames = new HashMap<String, Stop>();

		NodeList stopsTagList = stopsList.getElementsByTagName("stop");
		for (int i = 0; i < stopsTagList.getLength(); i++) {
			Node node = stopsTagList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getParentNode().getNodeName().equals("route")) {
				stops.add(node.getAttributes().getNamedItem("tag").getNodeValue());
				stopsNames.put(node.getAttributes().getNamedItem("tag").getNodeValue(),
						new Stop(node.getAttributes().getNamedItem("tag").getNodeValue(),
								node.getAttributes().getNamedItem("lat").getNodeValue(), 
								node.getAttributes().getNamedItem("lon").getNodeValue(), 
								node.getAttributes().getNamedItem("title").getNodeValue())); 
			}
		}
		for(String stop:stops){
			if(message[2].trim().toLowerCase().equals(stop.toLowerCase())){
				return stop;
			}
		}
		return null;
	}
	
	private String getStopsList(){
		String result = "Stops\n";
		for(String stop:stops){
			result += stop + " - " + stopsNames.get(stop).title + "\n";
		}
		return result;
	}
	
	private Stop getNearestStop(String agency, String route, String[] message){
		String stopsString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=" + route;
			System.out.println("Requesting: " + url);
			stopsString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(stopsString);
		Document stopsList = null;
		try {
			stopsList = loadXMLFromString(stopsString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stops = new ArrayList<String>();
		stopsNames = new HashMap<String, Stop>();

		NodeList stopsTagList = stopsList.getElementsByTagName("stop");
		for (int i = 0; i < stopsTagList.getLength(); i++) {
			Node node = stopsTagList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getParentNode().getNodeName().equals("route")) {
				stops.add(node.getAttributes().getNamedItem("tag").getNodeValue());
				stopsNames.put(node.getAttributes().getNamedItem("tag").getNodeValue(),
						new Stop(node.getAttributes().getNamedItem("tag").getNodeValue(),
								node.getAttributes().getNamedItem("lat").getNodeValue(), 
								node.getAttributes().getNamedItem("lon").getNodeValue(), 
								node.getAttributes().getNamedItem("title").getNodeValue())); 
			}
		}
		Stop nearestStop = null;
		Double min = Double.MAX_VALUE;
		for (String stop:stops){
			System.out.println(stop);
			Stop s = stopsNames.get(stop);
			System.out.println(s);
			Double currDistance = distance(Double.parseDouble(s.lat), 
									Double.parseDouble(s.lng),
									Double.parseDouble(location_lat),
									Double.parseDouble(location_long), 0, 0);
			if(currDistance < min){
				min = currDistance;
				nearestStop = s;
			}
			System.out.println();
		}
		System.out.println(nearestStop);
		return nearestStop;		
	}
	
	private Stop getStop(String[] message){
		Stop stopFound = null;
		for (String stop:stops){
			if(stop.equals(message[2].trim().toLowerCase())){
				stopFound = stopsNames.get(stop);
			}
		}
		System.out.println(stopFound);
		return stopFound;		
	}
	
	private String getPredictions(Stop nearestStop, String agency, String route){
		
		String predictionsString = "";
		try {
			String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=" + agency + "&r=" + route + "&s=" + nearestStop.tag;
			System.out.println("Requesting: " + url);
			predictionsString = readUrl(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(predictionsString);
		Document predictionsList = null;
		try {
			predictionsList = loadXMLFromString(predictionsString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> predictions = new ArrayList<String>();

		NodeList predictionsTagList = predictionsList.getElementsByTagName("prediction");
		for (int i = 0; i < predictionsTagList.getLength(); i++) {
			Node node = predictionsTagList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String totalTime = timeConversion(Integer.parseInt(node.getAttributes().getNamedItem("seconds").getNodeValue()));
				predictions.add(totalTime);
				System.out.println(totalTime);
			}
		}
		String predictionsResult = "";
		for(String i:predictions){
			predictionsResult += i + "\n";
		}
		return predictionsResult;
	}
	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read); 

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public Document loadXMLFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}
	public static double distance(double lat1, double lon1, double lat2, double lon2,
			double el1, double el2) {

		final int R = 6371; // Radius of the earth

		Double latDistance = deg2rad(lat2 - lat1);
		Double lonDistance = deg2rad(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;
		distance = Math.pow(distance, 2) + Math.pow(height, 2);
		return Math.sqrt(distance);
	}
	

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	
	private static String timeConversion(int totalSeconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
	    int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
	    int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
	    int hours = totalMinutes / MINUTES_IN_AN_HOUR;
	    
	    if(hours == 0){
	    	if(minutes == 0){
	    		return seconds + " seconds";
	    	}else{
	    		return minutes + " minutes " + seconds + " seconds";
	    	}
	    }else{
		    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	    }
	}
}


class Stop{
	public String tag;
	public String lat;
	public String lng;
	public String title;
	public Stop(String tag, String lat, String lng, String title) {
		super();
		this.tag = tag;
		this.lat = lat;
		this.lng = lng;
		this.title = title;
	}
	
	public String toString(){
		return title + " " + lat + "," + lng;
	}

}