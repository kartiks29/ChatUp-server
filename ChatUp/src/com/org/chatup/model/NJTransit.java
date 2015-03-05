package com.org.chatup.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.joda.time.DateTime;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.org.chatup.web.RequestHandler;

public class NJTransit {
	
	private final String TABLE_NAME = "njtransit";
	private final String query;
	
	private Map<String, String> stationList = new HashMap<String, String>();
	private Connection connection = null;
	
	public NJTransit(String query) {
		this.query = query;
	}
	
	
	public void openDbConnection(String databaseName, String username, String password) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		connection = (Connection) DriverManager
		          .getConnection("jdbc:mysql://localhost/" + databaseName + "?"
		              + "user=" + username 
		              + "&password=" + password
		              );
	}
	
	public void closeDbConnection(Connection connect) throws SQLException {
		connect.close();
	}
	
	
	public void insertStationList(final int batchSize) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		truncateTable(TABLE_NAME);
		openDbConnection(RequestHandler.DATABASE_NAME, RequestHandler.USER_NAME, RequestHandler.PASSWORD);
		String INSERT_QUERY = "INSERT INTO " + TABLE_NAME +" (st_code, st_name, last_updated) VALUES (?, ?, ?)";
		
		PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(INSERT_QUERY);
		int count = 0;
		
		for(Map.Entry<String, String> entry : stationList.entrySet()) {
			preparedStatement.setString(1, entry.getKey());
			preparedStatement.setString(2, entry.getValue());
			
			Date dt = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTime = sdf.format(dt);
			
			preparedStatement.setString(3, currentTime);
			preparedStatement.addBatch();
			
			if(++count % batchSize == 0) {
				preparedStatement.executeBatch();
		    }
		}
		preparedStatement.executeBatch(); // Insert remaining records
		preparedStatement.close();
		closeDbConnection(connection);
	}
	
	
	public Map<String, String> getStationCodes(String station) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		openDbConnection(RequestHandler.DATABASE_NAME, RequestHandler.USER_NAME, RequestHandler.PASSWORD);
		final String SELECT_QUERY = "SELECT st_code, st_name FROM " + TABLE_NAME + " WHERE st_name LIKE '%" + station + "%';";
		Map<String, String> stationCodes = new HashMap<String, String>();
		
		PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(SELECT_QUERY);
		ResultSet resultSet = (ResultSet) preparedStatement.executeQuery();
		
		while(resultSet.next()) {
			stationCodes.put(resultSet.getString(1), resultSet.getString(2));
			
		}
		preparedStatement.close();
		closeDbConnection(connection);
		
		return stationCodes;
	}
	
	
	public void truncateTable(String tableName) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		openDbConnection(RequestHandler.DATABASE_NAME, RequestHandler.USER_NAME, RequestHandler.PASSWORD);
		
		String TRUNCATE_QUERY = "TRUNCATE TABLE " + tableName;
		
		PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(TRUNCATE_QUERY);
		preparedStatement.execute();
		preparedStatement.close();
		
		closeDbConnection(connection);
	}
	
	
	public void scrapeStationList() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		
		HtmlPage page = webClient.getPage(new URL("http://www.njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainTo"));
		HtmlSelect select = (HtmlSelect) page.getElementByName("selOrigin");
		List<HtmlOption> options = select.getOptions();
		
		for(HtmlOption option : options) {
			if(option.getValueAttribute().length() > 0) {
				stationList.put(option.getValueAttribute(), option.getText());
			}
		}
	}
	
	
	public HtmlPage scrapeTimingPage(String fromStation, String toStation) throws FailingHttpStatusCodeException, IOException {
		final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		WebRequest requestSettings = new WebRequest(new URL("http://njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom"), HttpMethod.POST);
		requestSettings.setRequestParameters(new ArrayList());
		requestSettings.getRequestParameters().add(new NameValuePair("selOrigin", fromStation));
		requestSettings.getRequestParameters().add(new NameValuePair("selDestination", toStation));
		requestSettings.getRequestParameters().add(new NameValuePair("OriginDescription", ""));
		requestSettings.getRequestParameters().add(new NameValuePair("DestDescription", ""));
		
		return webClient.getPage(requestSettings);
	}
	
	
	public String scrapeTrainTimings(String fromStation, String toStation) throws FailingHttpStatusCodeException, IOException {
		
		HtmlPage webPage = scrapeTimingPage(fromStation, toStation);
		HtmlTable table = (HtmlTable) webPage.getFirstByXPath("/html/body/div[1]/div[2]/table/tbody/tr/td[2]/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td[1]/table/tbody/tr[1]/td/table/tbody/tr/td/div/table");
		
		String temp = "";
		int count = 0;
		
		for(HtmlTableRow row : table.getRows().subList(1, table.getRowCount())) {
				String timing = row.getCell(0).asText()
						.replaceAll("\n", " ")
						;
				String time = timing.substring(0, 8);
				int hr = Integer.parseInt(time.substring(0, 2));
				int min = Integer.parseInt(time.substring(3, 5));
				if("PM".equals(time.substring(6, 8)) && hr < 12) {
					hr += 12;
				}
				
				DateTime currentTime = new DateTime();
				if((currentTime.getHourOfDay() < hr) || 
						(currentTime.getHourOfDay() == hr && currentTime.getMinuteOfHour() <= min) ) {
					
					temp += timing + "\n";
					
						for(HtmlTableCell cell : row.getCells().subList(1, row.getCells().size())) {
							temp += cell.asText() + "\n";
						}
						count++;
				}

				temp += "\n";
				
				if(count > 2)
					break;
		}
		
		return temp.trim();
	}
	
	
	public String getTimings() throws FailingHttpStatusCodeException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		String result = "";
		Map<String, String> fromStationCodes = new HashMap<String, String>();
		Map<String, String> toStationCodes = new HashMap<String, String>();
		
		if(query == null)
			return result;
		
		try {
			String[] queryAsWords = query.split("\\bto\\b");
			if(queryAsWords.length == 2) {
				
				fromStationCodes = getStationCodes(queryAsWords[0].trim());
				toStationCodes = getStationCodes(queryAsWords[1].trim());
				
				/**
				 * Check if both ORIGIN and DESTINATION are valid
				 */
				if(fromStationCodes.size() == 0) {
					result = "Enter valid Origin station";
					return result;
				} else if(toStationCodes.size() == 0) {
					result = "Enter valid Destination station";
					return result;
				}
				
				/**
				 * Check if ORIGIN and DESTINATION are same
				 * retainAll() removes all keys in `toStationCodes` from `fromStationCodes`
				 */
				Set<String> intersectionSet = new HashSet<String>(fromStationCodes.keySet());
				intersectionSet.retainAll(toStationCodes.keySet());
				if(intersectionSet.size() != 0) {
					result = "Enter different source and destination";
					return result;
				}
				
				
				for(String from : fromStationCodes.keySet()) {
					for(String to : toStationCodes.keySet()) {
						
						if(result.trim().length() > 0) {
							result += "\n";
						}
						
						result += fromStationCodes.get(from).toUpperCase() + " -> "
								+ toStationCodes.get(to).toUpperCase() + "\n" 
								+ scrapeTrainTimings(from, to) 
								+ "\n\n"
								;
					}
				}
				
			} else {
				result = "Use this syntax: Origin_station to Destination_station";
				return result;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
