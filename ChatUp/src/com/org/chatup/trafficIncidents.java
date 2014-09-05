package com.org.chatup;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class trafficIncidents
 */
@WebServlet("/trafficIncidents")
public class trafficIncidents extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		double lat = 40.7577, lon = -73.9857;
		
		double R = 6371;  // earth radius in km

		double radius = 50; // km

		double x1 = lon - Math.toDegrees(radius/R/Math.cos(Math.toRadians(lat)));

		double x2 = lon + Math.toDegrees(radius/R/Math.cos(Math.toRadians(lat)));

		double y1 = lat + Math.toDegrees(radius/R);

		double y2 = lat - Math.toDegrees(radius/R);
		
		System.out.println(x1);
		System.out.println(y1);
		System.out.println(x2);
		System.out.println(y2);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
