package com.remkohde.dev.liberty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Servlet implementation class NewsSearchServlet
 */
@WebServlet(name = "com.remkohde.dev.liberty.NewsSearchServlet",
			loadOnStartup=1,
        	urlPatterns = "/news")
public class NewsSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsSearchServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		String startdate = (String) request.getParameter("startdate");
		String enddate = (String) request.getParameter("enddate");
		String searchterm = (String) request.getParameter("searchterm");
		String count = (String) request.getParameter("count");
	
		String alchemyResults = callAlchemyNewsApi(startdate, enddate, searchterm, count);
		
		request.setAttribute("alchemyResults", alchemyResults);
		request.getRequestDispatcher("pages/response.jsp").include(request, response);		
		request.getRequestDispatcher("pages/newsanalysis_result.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	private String callAlchemyNewsApi(String startdate, String enddate, String searchterm, String count){
		
		String response = "";
		try {
			String params = "startdate="+startdate+"&enddate="+enddate+"&searchterm="+searchterm+"&count="+count;
			String urlString = "http://localhost:9080/JavaCloudantDBApp/api/watson/news?"+params;
			URL url = new URL(urlString);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
	
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
	
			String output;
			
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				response += output;
			}
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

}
