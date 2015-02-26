package com.org.chatup.model;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;

public class OpenTable {

	private String query;
	
	public OpenTable(String query) {
		this.query = query;
	}
	
	public static String test() {
		// Setup firefox binary to start in Xvfb        
	    String Xport = System.getProperty(
	            "lmportal.xvfb.id", ":100");
	    final File firefoxPath = new File(System.getProperty(
	            "lmportal.deploy.firefox.path", "/usr/bin/firefox"));
	    FirefoxBinary firefoxBinary = new FirefoxBinary(firefoxPath);
	    firefoxBinary.setEnvironmentProperty("DISPLAY", Xport);

	    // Start Firefox driver
	    WebDriver driver = new FirefoxDriver(firefoxBinary, null);
	    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	    driver.get("http://www.google.com/");

	/*        
	    // Take snapshot of browser
	    File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
	    FileUtils.copyFile(srcFile, new File("ffsnapshot.png"));
	*/
	    String result = "Temp";
	    result = driver.getTitle();
	    
	    driver.quit();
	    
	    return result;
	}
}
