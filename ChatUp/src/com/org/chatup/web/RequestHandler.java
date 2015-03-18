package com.org.chatup.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.org.chatup.model.NJTransit;
import com.org.chatup.model.Open311;
import com.org.chatup.model.OpenTable;
import com.org.chatup.model.Winescapes;

/**
 * Servlet implementation class RequestHandler
 */
@WebServlet("/RequestHandler")
public class RequestHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String GOOGLE_SERVER_KEY = "AIzaSyBY8TsKmH4RKjRIFRg5_DlE6p66BCaCsLM";
	static final String MESSAGE_KEY = "message";
	
	
	
	
	
	public static final String DATABASE_NAME = "chatup";
//	private final String USER_NAME = "chatup_admin";
	public static final String USER_NAME = "root";
//	private final String PASSWORD = "chatup_admin";
	public static final String PASSWORD = "chatup";
	private final String TABLE_NAME = "gcm_users";
	private Connection connection = null;
	
	
	
	
	
	
	
       
    public RequestHandler() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter output = response.getWriter();
		output.write("Hello there!");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String result = "Try again in sometime";
		JSONObject requestJson, jObject = new JSONObject();
		
		try {
			System.out.println("Request received");
			requestJson = Helper.getEncodedJson(request);
					//.getJSONObject("request");
			
			if(requestJson != null) {
				String urlTitle = "";
				String message = "";
				urlTitle = requestJson.getJSONObject("data").getString("urlTitle");
				message = requestJson.getJSONObject("data").getString("message");
				
				switch(urlTitle) {
				
				case "Winescapes":
					Winescapes winescapes = new Winescapes(message);
					
					result = winescapes.calculate();
					
					break;
					
				case "NJTransit":
					NJTransit njTransit = new NJTransit(message);
/*					
					try {
						njTransit.scrapeStationList();
						njTransit.insertStationList(100);
					} catch (InstantiationException | IllegalAccessException
							| ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
*/					
					
					try {
						result = njTransit.getTimings().trim();
						if(result.length() == 0) {
							result = "Could not find any trains. Try again in sometime";
						}
					} catch (FailingHttpStatusCodeException e) {
						System.out.println("FailingHttpStatusCodeException");
						e.printStackTrace();
						
					} catch (InstantiationException e) {
						System.out.println("InstantiationException");
						e.printStackTrace();
						
					} catch (IllegalAccessException e) {
						System.out.println("IllegalAccessException");
						e.printStackTrace();
						
					} catch (ClassNotFoundException e) {
						System.out.println("ClassNotFoundException");
						e.printStackTrace();
					}
					break;
					
				case "Opentable":
					OpenTable opentable = new OpenTable(message);
					result = opentable.test();
					break;
				
				case "Open311":
					Open311 open311 = new Open311(message);
					result = open311.sendRequest();
					break;

				default:
					
					break;
				}
				
				jObject.accumulate("data",
								new JSONObject().accumulate("urlTitle", urlTitle).accumulate("message", result)
								);
				
				String regId = "";
				//regId = getRegId("vlnvv14@gmail.com");
				//regId = getRegId(requestJson.getString("GCMregId"));
				regId = requestJson.getString("GCMregId");
//				System.out.println("regId: " + regId);
				
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message msg = new Message.Builder().timeToLive(3600)
						.delayWhileIdle(true)
						.addData(MESSAGE_KEY, jObject.toString())
						.build()
						;
//				System.out.println(jObject.toString());
				Result status = sender.send(msg, regId, 5);
				request.setAttribute("pushStatus", status.toString());
				if(status.getErrorCodeName() != null) {
					System.out.println(status.getErrorCodeName());
				}
			}
			
		} catch (JSONException e) {
			System.out.println("Caught Exception");
			e.printStackTrace();
		}		
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
	
	public String getRegId(String email) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		openDbConnection(DATABASE_NAME, USER_NAME, PASSWORD);
		final String SELECT_QUERY = "SELECT gcm_regid FROM " + TABLE_NAME + " WHERE gcm_regid='" + email + "'";
		
		PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(SELECT_QUERY);
		ResultSet resultSet = (ResultSet) preparedStatement.executeQuery();
		
		String regId = "";
		if(resultSet.next()) {
			regId = resultSet.getString(1);
		}
		preparedStatement.close();
		closeDbConnection(connection);
		
		return regId;
	}
}
