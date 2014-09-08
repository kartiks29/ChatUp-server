package com.org.chatup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Servlet implementation class webScraper
 */
@WebServlet("/showTiming")
public class showTiming extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		HttpSession session = request.getSession();
		
		//	Get the JSON request in String format
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str, resp = "";
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
		
		if(session.isNew()) {	//	User entered movie name
			session.setAttribute("movieTitle", param);
			resp = "1st case\\n" + scrape(param, -1);
		}
		else {
			if (param.matches("[0-9]+") && param.length() > 0) {
				int nth = Integer.parseInt(param);
				//session.removeAttribute("movieTitle");
				resp = scrape(session.getAttribute("movieTitle").toString(), nth);
			}
		}

		System.out.println(resp);
		resp = "{\"response\":\"" + resp + "\"}";
		
		writer.print(resp);
		writer.flush();
	}
	
	protected String scrape(String title, int nth) {
		WebDriver driver = new FirefoxDriver();

		driver.navigate().to("http://xfinitytv.comcast.net/tv-listings");
		WebElement searchBox = driver.findElement(By.cssSelector("div.mod-search-field-wrapper").cssSelector("input[type=text]"));
		WebElement searchButton = driver.findElement(By.cssSelector("button.button.search.font-icon-search"));
		searchBox.sendKeys(title);
		searchButton.click();
		
		WebElement div = driver.findElement(By.cssSelector("div#searchList > *:first-child"));
		String res = div.getAttribute("id").toString();
		String retval = "";
		
		if(res.equals("noResults")) {
			retval = "Your search did not match any results";
		}
		else {
			List<WebElement> titleMatches = driver.findElements(By.cssSelector("div#searchList > *:first-child > *"));
				
			if(titleMatches.size() > 1) {
				int count = 1;
				
				for(WebElement temp : titleMatches) {
					String t = temp.findElement(By.cssSelector("div.entity_info > h3 > a")).getAttribute("name");
					retval += (count++) + ". " + t + "\\n";
				}
				
				if(nth > 0){
					titleMatches.get(nth - 1).findElement(By.cssSelector("div.entity_info > h3 > a")).click();
					retval = driver.findElement(By.cssSelector("#actionPanelTvListings > ul > li > div.details-info > div.record-time")).getText();
				}
				else if(nth == 0)
					retval = "Invalid choice";
			}
			else {
				System.out.println("Only one match");
				titleMatches.get(0).findElement(By.cssSelector("div.entity_info > h3 > a")).click();
				retval = driver.findElement(By.cssSelector("#actionPanelTvListings > ul > li > div.details-info > div.record-time")).getText();
			}
		}
		
		driver.close();
		return retval;
	}
}
