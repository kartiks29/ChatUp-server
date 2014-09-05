package com.org.chatup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

import net.sourceforge.htmlunit.corejs.javascript.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

/**
 * Servlet implementation class channelMap
 */
@WebServlet("/channelMap")
public class channelMap extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("ChannelMap GET method");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String st_code="38", param="", result="";
		
		//	Get the JSON request in String format
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while( (str = br.readLine()) != null ){
			sb.append(str);
		}
			
		//	Get the actual command in String
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
		HashMap<String, String> channelMap = new HashMap<String, String>();
		
		try(BufferedReader brr = new BufferedReader(new FileReader("/Users/Vicky/Desktop/channelMapping.txt"))) {
	        StringBuilder sbb = new StringBuilder();
	        String line = brr.readLine();

	        while (line != null) {
	            sbb.append(line);
	            sbb.append(System.lineSeparator());
	            line = brr.readLine();
	        }
	        String everything = sbb.toString();
	        //System.out.println(everything);
	        String[] t = everything.split(",");
	        for(int i = 0; i < t.length - 1; i++) {
	        	String pair = t[i];
	        	//System.out.println(pair);
	        	String ch = pair.split(":")[0];
	        	String no = pair.split(":")[1];
	        	channelMap.put(ch, no);
	        }
	    }
/*
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);    
        
        String url = "http://www.optimum.com/lineup.jsp?regionId=" + st_code;
        HtmlPage page = webClient.getPage(url);
        Document doc = Jsoup.parse(page.asXml());
        
        Elements div = doc.select("div#main").select("div#content").select("div.padborders").select("div.columns");
		String temp = "";
        Elements origin = div.get(0).select("li.channelIo");
		for(Element option : origin) {
			String num = option.text().split(" ")[0];
			String name = option.text().replace(num, "");
			channelMap.put(name, num);
			temp += name + ":" + num + ",";
			System.out.format("%-30s%5s", name, num + "\n");
		}
		
		File file = new File("/Users/Vicky/Desktop/channelMapping.txt");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(temp);
		bw.close();
*/		
		
	    
		//	Match command (from Android) to channel mapping
		Iterator it = channelMap.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			
			if(pairs.getKey().toString().toLowerCase().trim().contains(param)) {	// Complete match
				result += "#" + pairs.getValue().toString().toLowerCase() + " : " + pairs.getKey().toString() + "\\n";
			}
		}
		
		result = "{\"response\":\"" + result + "\"}";
		writer.print(result);
        writer.flush();
	}

}
