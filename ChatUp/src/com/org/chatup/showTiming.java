package com.org.chatup;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Servlet implementation class webScraper
 */
@WebServlet("/webScraper")
public class showTiming extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int  nth = -1;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//	Determine if user is searching for a movie or entering the movie number
		//	Update nth
		
		//scrape("The Rifleman", nth);
		scrape("Get Smart", nth);
		//scrape("aksjbkvjsd", nth);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	protected void scrape(String title, int nth) {
		WebDriver driver = new FirefoxDriver();

		driver.navigate().to("http://xfinitytv.comcast.net/tv-listings");
		WebElement searchBox = driver.findElement(By.cssSelector("div.mod-search-field-wrapper").cssSelector("input[type=text]"));
		WebElement searchButton = driver.findElement(By.cssSelector("button.button.search.font-icon-search"));
		searchBox.sendKeys(title);
		searchButton.click();
		
		WebElement div = driver.findElement(By.cssSelector("div#searchList > *:first-child"));
		
		
		String res = div.getAttribute("id").toString();
		
		if(res.equals("noResults")) {
			System.out.println("Your search did not match any results");
		}
		else {
			List<WebElement> titleMatches = driver.findElements(By.cssSelector("div#searchList > *:first-child > *"));
			
			if(nth == -1) {		//	1st time: user searched movie name
				
				if(titleMatches.size() > 1) {
					int count = 1;
					for(WebElement temp : titleMatches) {
						String t = temp.findElement(By.cssSelector("div.entity_info > h3 > a")).getAttribute("name");
						System.out.println(count + ". " + t);
						count++;
					}
					// Temp
					titleMatches.get(0).findElement(By.cssSelector("div.entity_info > h3 > a")).click();
					String timing = driver.findElement(By.cssSelector("#actionPanelTvListings > ul > li > div.details-info > div.record-time")).getText();					
					System.out.println(timing);
				}
				else {
					System.out.println("Only one match");
					titleMatches.get(0).findElement(By.cssSelector("div.entity_info > h3 > a")).click();
					
					getShowTiming();
				}
			}
			
			else {
				if((nth > 0) && (nth < titleMatches.size())) {
					titleMatches.get(nth).findElement(By.cssSelector("div.entity_info > h3 > a")).click();
					
					getShowTiming();
				}
				else {
					System.out.println("Invalid option selected");
				}
			}
		}
		
		driver.close();
	}
	
	protected void getShowTiming() {
		System.out.println("Inside getShowTiming");
	}
}
