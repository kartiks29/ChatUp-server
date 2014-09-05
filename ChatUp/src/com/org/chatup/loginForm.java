package com.org.chatup;

import java.awt.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class loginForm
 */
@WebServlet("/loginForm")
public class loginForm extends HttpServlet {
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		
		//String url = "https://sso.cisco.com/autho/forms/CDClogin.html";
		//String url = "https://login.comcast.net/login";
		String url = "https://www.netflix.com/login?locale=en-US";
		//String url = "http://winescapes.net/web/index.html#";	//	Not working
		//String url = "https://idmsa.apple.com/IDMSWebAuth/login.html?appIdKey=af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3&path=/signin/?referrer%3D/account/manage&sslEnabled=true";
		//String url = "https://www.optimum.net/login";	//	Not working
		Document document = Jsoup.connect(url).userAgent("Mozilla").timeout(10*1000).get();	//	10s timeout
		
		Element form  = getForm(document);	//	Get form with type="password"
		Elements hiddenInputType, inputType;
		
		if(form != null) {
			hiddenInputType = getHiddenInputs(form);
			inputType = getInputs(form);
		}
		
		
		
		writer.print(document);
		writer.flush();
	}
	
	private Element getForm(Document document) {
		Elements forms = document.select("form");	//	Get all forms from the page 
		Element form = null;
		
		for(int i = 0; i < forms.size(); i++) {
			if(! forms.get(i).select("[type=password]").isEmpty()) {	//	Get all <input> of type="password"
				
				if(! forms.get(i).select("[method=post]").isEmpty()) {
					form = forms.get(i);
					return form;
				}
				else {
					continue;
				}
					
				
				//	continue:	Apple, Cisco
				//	break:		Winescapes, Netflix
				//	one form:	Comcast
				
				//	Check for method="post" => Cisco, Apple are cleared
			}
		}
		return null;
	}

	private Elements getHiddenInputs(Element form) {
		Elements hiddenInputType = null;
		hiddenInputType = form.select("[type=hidden]");
		System.out.println("Hidden\n" + hiddenInputType + "\n\n");
		
		return hiddenInputType;
	}
	
	private Elements getInputs(Element form) {
		Elements InputType = null;
		InputType = form.select("input");
		InputType = InputType.not("[type=hidden]");
		System.out.println("Not hidden\n" + InputType + "\n\n");
		
		return InputType;
	}
}
