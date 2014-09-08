package com.org.chatup;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class ikea
 */
@WebServlet("/ikea")
public class ikea extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
/*		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		
		//String query = request.getParameter("query");
		String query = "mattress";
		ArrayList<String> prodName = new ArrayList<String>();
		
		Document doc = Jsoup.connect("http://www.ikea.com/us/en/search/?query=" + query).userAgent("Mozilla").timeout(10*1000).get();
		Elements div = doc.select("div#allContent").select("div#mainPadding").select("div#main").select("div.rightContent").select("div#productsContainer").select("table#productsTable");
		
		Iterator<Element> it = div.select("tbody").select("tr").iterator();
		
		while(it.hasNext()) {

			Element row = it.next();
			for(Element td : row.select("td")) {
				String temp = td.select("div.productContainer").select("div.parentContainer").select("div.productPadding").select("a").select("span.prodName.prodNameTro").text();
				if(temp.length() != 0) {
					prodName.add(temp);
				}
			}
		}
		
		String result = "";
		for(String temp : prodName) {
			result += (temp + "\\n");
		}
		
		result = "{\"response\":\"" + result + "\"}";
		//System.out.println(result);
		
		writer.print(result);
		writer.flush();
*/
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();

		writer.print("Ikea POST method");
		writer.flush();
	}

}
