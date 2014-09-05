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
public class webScraper extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		WebDriver driver = new FirefoxDriver();
		
		//driver.navigate().to("http://testing-ground.scraping.pro/login");
		//WebElement userName_editbox = driver.findElement(By.id("usr"));
        //WebElement password_editbox = driver.findElement(By.id("pwd"));
        //WebElement submit_button = driver.findElement(By.xpath("//input[@value='Login']"));
 
        //userName_editbox.sendKeys("admin");
        //password_editbox.sendKeys("12345");
        //submit_button.click();
		
        //String text = driver.findElement(By.xpath("//div[@id='case_login']/h3")).getText();
/*		
		driver.navigate().to("http://www.optimum.com/lineup.jsp?regionId=" + "38");
		String text = driver.findElement(By.cssSelector("div#main").cssSelector("div#content").cssSelector("div.padborders").cssSelector("div.columns")).getText();
	
		System.out.println(text);
        driver.close();
*/
		driver.navigate().to("http://xfinitytv.comcast.net/tv-listings");
		WebElement searchBox = driver.findElement(By.cssSelector("div.mod-search-field-wrapper").cssSelector("input[type=text]"));
		WebElement searchButton = driver.findElement(By.cssSelector("button.button.search.font-icon-search"));
		//searchBox.sendKeys("The Rifleman");
		searchBox.sendKeys("aksjbkvjsd");
		searchButton.click();
		
		WebElement div = driver.findElement(By.cssSelector("div#searchList > *:first-child"));
		String res = div.getAttribute("id").toString();
		
		if(res.equals("noResults")) {
			System.out.println("Your search did not match any results");
		}
		else {
			
		}
		
		driver.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
