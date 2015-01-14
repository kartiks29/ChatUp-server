package com.org.chatup.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.org.chatup.model.NJTransit;
import com.org.chatup.model.Winescapes;

/**
 * Servlet implementation class RequestHandler
 */
@WebServlet("/RequestHandler")
public class RequestHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RequestHandler() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String result = "Try again in sometime";
		JSONObject requestJson, jObject = new JSONObject();;
		
		requestJson = getEncodedJson(request);
		
		if(requestJson != null) {
			String urlTitle = "";
			String message = "";
			try {
				urlTitle = requestJson.getString("urlTitle");
				message = requestJson.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			switch(urlTitle) {
			
			case "Winescapes":
				Winescapes winescapes = new Winescapes();
				break;
				
			case "NJTransit":
				NJTransit njTransit = new NJTransit(message);
				
				try {
					result = njTransit.getTimings().trim();
					if(result.length() == 0) {
						result = "Could not find any trains. Try again in sometime";
					}
				} catch (FailingHttpStatusCodeException e) {
					// TODO Auto-generated catch block
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
				break;
				
			default:
				
				break;
			}
			
			try {
				jObject.put("response", result);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(jObject);
	}
	
	
	public JSONObject getEncodedJson(HttpServletRequest request) {
		StringBuffer jsonBuilder = new StringBuffer();
		String line = null;
		
		try {
		  BufferedReader reader = request.getReader();
		  while ((line = reader.readLine()) != null)
			  jsonBuilder.append(line);
		  
		  return new JSONObject(jsonBuilder.toString());
		  
		} catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
	}
}
