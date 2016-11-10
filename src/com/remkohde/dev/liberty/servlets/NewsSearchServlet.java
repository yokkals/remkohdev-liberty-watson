package com.remkohde.dev.liberty.servlets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.cloudant.client.api.Database;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.remkohde.dev.liberty.nosql.CloudantClientMgr;

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
		
		String hosturl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+""+request.getContextPath();
		
		String startdate = (String) request.getParameter("startdate");
		String enddate = (String) request.getParameter("enddate");
		String searchterm = (String) request.getParameter("searchterm");
		String count = (String) request.getParameter("count");
	
		// Search AlchemyData News
		String alchemyResults = getAlchemyNewsApi(hosturl, startdate, enddate, searchterm, count);
		//request.getSession().setAttribute("alchemyResults", alchemyResults);
		
		// Save to CloudantDB		
		String cloudantResults = postCloudantDbApi(hosturl, alchemyResults, startdate, enddate, searchterm, count);
		// request.getSession().setAttribute("cloudantResults", cloudantResults);
		
		// Parse to D3js format: 
		// [ {"publicationDate": 2016-10-01, "sentiment": 0.1234},
		//   {"publicationDate": 2016-10-01, "sentiment": 0.2345} ]
		String sentimentScores = parseToD3jsFormat(alchemyResults, startdate, enddate, searchterm, count);
		request.getSession().setAttribute("result", sentimentScores);
		
		request.getRequestDispatcher("pages/newsanalysis_result.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * Calls the internal application API that calls the AlchemyData News API.
	 * 
	 * @param hosturl
	 * @param startdate
	 * @param enddate
	 * @param searchterm
	 * @param count
	 * @return
	 */
	private String getAlchemyNewsApi(String hosturl, String startdate, String enddate, String searchterm, String count){		
		String params = "startdate="+startdate+"&enddate="+enddate+"&searchterm="+searchterm+"&count="+count;
		String urlString = hosturl+"/api/watson/news";
		
		String response = callApi("get", urlString, params);
		return response;
	}
	
	/**
	 * Calls the internal application API that runs the Cloudant Client
	 * 
	 * @param hosturl
	 * @param jsonobj
	 * @param startdate
	 * @param enddate
	 * @param searchterm
	 * @param count
	 * @return
	 * @throws IOException
	 */
	private String postCloudantDbApi(String hosturl, String jsonobj, 
			String startdate, String enddate, String searchterm, String count) 
	 throws IOException {	
		String params = "jsonobj="+jsonobj;
		String urlString = hosturl+"/api/cloudant/data";
		
		Database db = CloudantClientMgr.getDB();
		
		String id = String.valueOf(System.currentTimeMillis());
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		String searchdate = format1.format(now.getTimeInMillis());
		
		Gson gson = new Gson();
		JsonArray jsonObject1 = gson.fromJson(jsonobj, JsonArray.class);
		
		System.out.println("===== Creating new document with id : " + id);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("_id", id);
		data.put("results", jsonObject1);		
		data.put("searchdate", searchdate);
		data.put("startdate", startdate);
		data.put("enddate", enddate);
		data.put("searchterm", searchterm);
		data.put("count", count);
		db.save(data);
		
		// attach the attachment object
		HashMap<String, Object> obj = db.find(HashMap.class, id);
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", obj.get("_id") + "");
		jsonObject.addProperty("results", obj.get("results") + "");
		
		return jsonObject.toString();
	}
	
	private String callApi(String method, String urlString, String params){
		
		String response = "";
		HttpURLConnection conn = null;
		try {
			if(method.toLowerCase()=="get"){
				
				URL url = new URL(urlString+"?"+params);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				
			}else if(method.toLowerCase()=="post"){
				
				byte[] postData       = params.getBytes(StandardCharsets.UTF_8);
				int    postDataLength = postData.length;
				URL    url            = new URL(urlString);
				conn= (HttpURLConnection) url.openConnection();           
				conn.setDoOutput(true);
				//conn.setInstanceFollowRedirects(false);
				conn.setRequestMethod("POST");
				//conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
				//conn.setRequestProperty( "charset", "utf-8");
				conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
				//conn.setUseCaches( false );
				try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
				   wr.write( postData );
				}
				
			}else{
				// 
				return "Illegal HTTP Method";
			}
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));	
			String output;			
			while ((output = br.readLine()) != null) {
				response += output;
			}
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("AlchemyData News response: "+response);
		return response;
	}

	public String parseToD3jsFormat(String alchemyResults, String startdate, String enddate,
			String searchterm, String count) {
		
		Gson gson = new Gson();
		//System.out.println("=====json: "+alchemyResults);
		// [{"result":[{"docs":{"result":{"docs":[{
		JsonArray jsonObject1 = gson.fromJson(alchemyResults, JsonArray.class);
		
		// create response
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("result", jsonObject1);
		jsonObject.addProperty("startdate", startdate);
		jsonObject.addProperty("enddate", enddate);
		jsonObject.addProperty("searchterm", searchterm);
		jsonObject.addProperty("count", count);
				
		JsonArray jsonArrayResponse = new JsonArray();
		jsonArrayResponse.add(jsonObject);
		
		return jsonArrayResponse.toString();
	}

}
