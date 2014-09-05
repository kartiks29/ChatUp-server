package com.org.chatup;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Servlet implementation class loginPage
 */
@WebServlet("/loginPage")
public class loginPage extends HttpServlet {
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		
		String url = "http://www.google.com/search?hl=en&btnI=1&q=" + request.getParameter("loginPage");
		Document document = Jsoup.connect(url).userAgent("Mozilla").timeout(10*1000).get();	//	10s timeout
		
		writer.print(document.baseUri());
		writer.flush();
	}
}
