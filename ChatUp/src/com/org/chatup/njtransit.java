package com.org.chatup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class njtransit
 */
@WebServlet("/njtransit")
public class njtransit extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("NJTransit GET method");
		
		DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
		String now = dateFormat.format(System.currentTimeMillis());
		System.out.println(now);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		Document document;
		Pattern pattern;
		Matcher matcher;
		String st_code2="", st_code1="", st1="", st2="";
		
		//	Get the JSON request in String format
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while( (str = br.readLine()) != null ){
			sb.append(str);
		}
			
		//	Get the actual command in String
		String param="";
		try {
			JSONObject jsonReq = new JSONObject(sb.toString());
			param = (String) jsonReq.get("request");
			param = param.trim().toLowerCase();
					
		} catch (JSONException e1) {
				e1.printStackTrace();
			}
				
		ArrayList<String> cmd = new ArrayList<>();
		for(String t : param.split(" ")) {
			cmd.add(t);
		}
		
		
		//	Get station code mappings and get correct code from command (received from Android)
		HashMap<String, String> selOrigin = new HashMap<String, String>();
		HashMap<String, String> selDest = new HashMap<String, String>();
		
		Document doc = Jsoup.connect("http://www.njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainTo").userAgent("Mozilla").timeout(10*1000).get();
		Elements div = doc.select("div.divCenter").select("div.content").select("table").select("tbody").select("tr").select("td").get(1).select("table").select("tr").get(1).select("td").select("table").select("tbody").select("tr").get(1).select("td");
		div = div.select("div.AccordionSchedules").select("div.AccordionPanel").select("div.AccordionPanelContent").select("form").select("table");
		
		Elements origin = div.select("tr").get(1).select("td").get(1).select("span.notranslate").select("select > option");
		for(Element option : origin) {
			selOrigin.put(option.text(), option.val());
		}
		Iterator it = selOrigin.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			if(pairs.getKey().toString().toLowerCase().contains(cmd.get(0))) {
				st1 = pairs.getKey();
				st_code1 = selOrigin.get(st1);
			}
		}
		
		Elements dest = div.select("tr").get(2).select("td").get(1).select("span.notranslate").select("select > option");
		for(Element option : dest) {
			selDest.put(option.text(), option.val());
		}
		it = selDest.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			if(pairs.getKey().toString().toLowerCase().contains(cmd.get(1))) {
				st2 = pairs.getKey();
				st_code2 = selDest.get(st2);
			}
		}
		
		document = Jsoup.connect("http://njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom")
			    .data("selOrigin", st_code1) // Fill the first input field. 103_NEC
			    .data("selDestination", st_code2) // Fill the second input field. 105_BNTN
			    .data("OriginDescription", "") // You need to keep it unmodified!
			    .data("DestDescription", "")
			    .post();
		
		pattern = Pattern.compile("<span>(.+:.+)(..M)");
		matcher = pattern.matcher(document.toString());
		String temp = "";
		int i = 0;
		while(matcher.find() && i < 5) {
			
			String time = matcher.group().toString().substring(6);
			
			try {
				DateTime nowt = new DateTime();
				int hr = Integer.parseInt(time.substring(0, 2));
				int min = Integer.parseInt(time.substring(3, 5));
				if("PM".equals(time.substring(6))) {
					hr += 12;
				}
				DateTime njt = new DateTime(nowt.getYear(), nowt.getMonthOfYear(), nowt.getDayOfMonth(), hr, min);
				
				if(njt.compareTo(nowt) >= 0) {
					temp += st1 + ": " + time + "  ";
					if(matcher.find()) {
						temp += st2 + ": " + matcher.group().toString().substring(6) + "\\n";
					}
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		temp = "{\"response\":\"" + temp + "\"}";
		System.out.println(temp);
		writer.print(temp);
		writer.flush();
	}
}
