package com.org.chatup.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String USERS_TABLE = "USERS";
	
	public static final String DATABASE_NAME = "chatup";
//	private final String USER_NAME = "chatup_admin";
	public static final String USER_NAME = "root";
//	private final String PASSWORD = "chatup_admin";
	public static final String PASSWORD = "chatup";
	private Connection connection = null;
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject registerJson = Helper.getEncodedJson(request);
		JSONObject jObject = new JSONObject();
		String regId = "", result = "OK";
		
		if(registerJson != null) {
			try {
				System.out.print("Registering new device: ");
				regId = registerJson.getString("regId");
				System.out.println(regId);
				
				try {
					String email = registerJson.getString("email");
					String name = registerJson.getString("name");
					saveToDb(name, email, regId);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				result = "regId not saved. Register again";
				e.printStackTrace();
			}
		}
		
		try {
			jObject.put("response", result);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		PrintWriter writer = response.getWriter();
		writer.print(jObject);
		writer.close();
	}
	
	
	public void saveToDb(String name, String email, String regId) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		openDbConnection(DATABASE_NAME, USER_NAME, PASSWORD);
		PreparedStatement preparedStatement = null;
		final String INSERT_QUERY = "INSERT INTO " + USERS_TABLE +" (name, email, is_active, last_active, gcm_regId) VALUES (?, ?, ?, ?, ?)";
		
		try {
			preparedStatement = (PreparedStatement) connection.prepareStatement(INSERT_QUERY);
			
			preparedStatement.setString(1, name);
			Date dt = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTime = sdf.format(dt);
			preparedStatement.setString(2, email);
			preparedStatement.setBoolean(3, true);
			preparedStatement.setString(4, currentTime);
			preparedStatement.setString(5, regId);
			
			preparedStatement.addBatch();
			preparedStatement.executeBatch(); // Insert remaining records
			preparedStatement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
			connection.rollback();
			throw e;
			
		} finally {
			try { if(connection != null) { connection.close(); } } catch (SQLException e) { throw e;}
			try { if(preparedStatement != null) { preparedStatement.close(); } } catch (SQLException e) { throw e;}
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
}
