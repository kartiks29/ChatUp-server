package com.org.chatup;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class favicon
 */
@WebServlet("/favicon")
public class favicon extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter writer = response.getWriter();
		
		String reqUrl = "";
		reqUrl = request.getParameter("org");
		if(reqUrl != "") {
			String url = "http://www.google.com/search?&q=" + reqUrl;
			Document document = manuelRedirectHandler(url);
			
			String title = document.title();	//	Title
			String baseurl = document.baseUri();	// URL
			String favicon = "http://g.etfv.co/"+ baseurl;	// Favicon
			//System.out.println(title+"\t"+baseurl+"\t"+favicon);
			
			if((title.toLowerCase().contains("winescapes")) || (title.toLowerCase().contains("ikea")) || (title.toLowerCase().contains("transit")) || (title.toLowerCase().contains("optimum") || (title.toLowerCase().contains("comcast")) || (title.toLowerCase().contains("opentable")))) {
				writer.print(title + ";" + favicon + ";" + baseurl);
			}
			else {
				writer.print("Not Supported");
			}
		}
		writer.flush();
	}
	
	private static Document manuelRedirectHandler(String url) throws IOException
	{
		Document document = Jsoup.connect(url.replaceAll(" ", "%20")).userAgent("Mozilla").timeout(10*1000).followRedirects(true).get();
	    String title = document.title();

	    if (title.contains("Google Search"))
	    {
	    	Elements div = document.select("li[class=g]");
	    	String redirectUrl = div.get(0).select("h3[class=r] > a[href]").attr("href").toString();
	        redirectUrl = "https://google.com" + redirectUrl;
	    	
	        return manuelRedirectHandler(redirectUrl);
	    }
	    return document;
	}
}
