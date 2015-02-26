package com.org.chatup.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.org.chatup.Config;
import com.org.chatup.model.NJTransit;
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
	
	private final String USERS_TABLE = "USERS";
	private DataSource dataSource = null;
	
	
    @Override
    public void init() throws ServletException {
    	super.init();
    	dataSource = Config.getInstance(getServletContext()).getDataSource();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String result = "Try again in sometime";
		JSONObject requestJson, jObject = new JSONObject();
		
		try {
			System.out.println("Request received");
			requestJson = Helper.getEncodedJson(request);
			
			if(requestJson != null) {
				String urlTitle = "";
				String message = "";
				urlTitle = requestJson.getJSONObject("data").getString("urlTitle");
				message = requestJson.getJSONObject("data").getString("message");
				
				switch(urlTitle) {
				
				case "Winescapes":
					Winescapes winescapes = new Winescapes(dataSource, message);
					
					result = winescapes.calculate();
					
					break;
					
				case "NJTransit":
					NJTransit njTransit = new NJTransit(dataSource, message);
/*					
					try {
						njTransit.scrapeStationList();
						njTransit.insertStationList(100);
					} catch (InstantiationException | IllegalAccessException
							| ClassNotFoundException | SQLException e) {
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
/*
				case "OpenTable":
					OpenTable openTable = new OpenTable(message);
					result = openTable.test();
					break;
*/					
				default:
					
					break;
				}
				
				jObject.accumulate("data",
								new JSONObject().accumulate("urlTitle", urlTitle).accumulate("message", result)
								);
								
				String regId = "";
				//regId = getRegId("vlnvv14@gmail.com");
				try {
					// Need to change this
					regId = getRegId(requestJson.getString("GCMregId"));
				} catch (SQLException e) {
					e.printStackTrace();
				}

				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message msg = new Message.Builder().timeToLive(3600)
						.delayWhileIdle(true)
						.addData(MESSAGE_KEY, jObject.toString())
						.build()
						;
				
				System.out.println(jObject.toString() + "\n\n");
				
				Result status = sender.send(msg, regId, 5);
				request.setAttribute("pushStatus", status.toString());
				if(status.getErrorCodeName() != null) {
					System.out.println("GCM Error: " + status.getErrorCodeName());
				}
			}
		} catch (JSONException e) {
			System.out.println("Caught Exception");
			e.printStackTrace();
		}		
	}
	
	
	public String getRegId(String email) throws SQLException {
		
		final String SELECT_QUERY = "SELECT gcm_regId FROM " + USERS_TABLE + " WHERE gcm_regId='" + email + "'";
		String regId = "";
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(SELECT_QUERY);
			resultSet = (ResultSet) preparedStatement.executeQuery();
			
			if(resultSet.next()) {
				regId = resultSet.getString(1);
				
				updateLastActiveTime(regId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			connection.rollback();
			throw e;
			
		} finally {
			try { if(connection != null) { connection.close(); } } catch (SQLException e) { throw e;}
			try { if(preparedStatement != null) { preparedStatement.close(); } } catch (SQLException e) { throw e;}
			try { if(resultSet != null) { resultSet.close(); } } catch (SQLException e) { throw e;}
		}
		System.out.println("regId: " + regId);
		return regId;
	}
	
	public void updateLastActiveTime(String regId) throws SQLException {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(dt);
		final String UPDATE_QUERY = "UPDATE " + USERS_TABLE + " SET last_active = ? WHERE gcm_regId = ?";
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(UPDATE_QUERY);
			preparedStatement.setString(1, currentTime);
			preparedStatement.setString(2, regId);
			preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			connection.rollback();
			throw e;
			
		} finally {
			try { if(connection != null) { connection.close(); } } catch (SQLException e) { throw e;}
			try { if(preparedStatement != null) { preparedStatement.close(); } } catch (SQLException e) { throw e;}
		}
	}
}
