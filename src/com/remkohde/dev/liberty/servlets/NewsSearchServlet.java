package com.remkohde.dev.liberty.servlets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
		
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		String searchdate = format1.format(now.getTimeInMillis());
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
		// [ {"publicationDate": 2016-10-01, "sentimentScore": 0.1234},
		//   {"publicationDate": 2016-10-01, "sentimentScore": 0.2345} ]
		String sentimentScores = parseToD3jsFormat(alchemyResults, searchdate, startdate, enddate, searchterm, count);
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

	public String parseToD3jsFormat(String alchemyResults, String searchdate, String startdate, String enddate,
			String searchterm, String count) {
		JsonArray response = null;
		try {
			
			Gson gson = new Gson();
			JsonArray resultArray = gson.fromJson(alchemyResults, JsonArray.class);
			JsonObject resultJsonObject = resultArray.get(0).getAsJsonObject();
			JsonArray docs = resultJsonObject.getAsJsonArray("docs");
			
			// create D3js array format
			JsonArray allSearchResultsArray = new JsonArray();
			for(int i=0; i< docs.size(); i++) {
				  JsonObject row = new JsonObject();
			      JsonObject doc = docs.get(i).getAsJsonObject();
			      // doc.source.enriched.url.enrichedTitle.docSentiment.score
			      // doc.source.enriched.url.enrichedTitle.docSentiment.type
			      // doc.source.enriched.url.publicationDate
			      JsonObject url = doc.get("source").getAsJsonObject()
			    		  .get("enriched").getAsJsonObject()
			    		  .get("url").getAsJsonObject();
			      JsonObject docSentiment = url.get("enrichedTitle").getAsJsonObject()
			    		  .get("docSentiment").getAsJsonObject();
			      String docSentimentScore = docSentiment.get("score").getAsString();
			      String docSentimentType = docSentiment.get("type").getAsString();	
			      JsonObject publicationDate = url.get("publicationDate").getAsJsonObject();
			      String publicationDateDate = publicationDate.get("date").getAsString();
			      
			      // From "Oct 29, 2014 12:00:00 AM" to "2014-10-29"
			      SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
			      SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
			      Date dte = format1.parse(publicationDateDate);
		    	  String pubDate = format2.format(dte);
			      
			      row.addProperty("publicationDate", pubDate);
			      row.addProperty("sentimentScore", docSentimentScore);
			      allSearchResultsArray.add(row);
			}
			
			// get uniquedates
			SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
			Date startdte = format3.parse(startdate);
			Date enddte = format3.parse(enddate);
			Vector<String> uniqueDates = new Vector<String>();
			for(int i=0; i< allSearchResultsArray.size(); i++) {
				String pubDate = allSearchResultsArray.get(i).getAsJsonObject().get("publicationDate").getAsString();
				Date pubdte = format3.parse(pubDate);
				if(pubdte.before(startdte) || pubdte.after(enddte)){
					continue;
				}
				if(! uniqueDates.contains(pubDate)){
					uniqueDates.addElement(pubDate);
				}
			}
			// sort uniquedates
			Collections.sort(uniqueDates);
			
			// aggregate sentimentScores per unique pubDate
			JsonArray jsonD3jsArray = new JsonArray();
			int i1 = 0;
			for(String uniqueDate : uniqueDates){
				i1++;
				double sentiments = 0.0;
				int i2=0;
				for(int i=0; i< allSearchResultsArray.size(); i++) {
					String pubDate = allSearchResultsArray.get(i).getAsJsonObject().get("publicationDate").getAsString();
					if(uniqueDate.compareTo(pubDate)==0){
						i2++;
						sentiments += allSearchResultsArray.get(i).getAsJsonObject().get("sentimentScore").getAsDouble();
					}
				}
				double avgSentiment = sentiments/i2;
				JsonObject aggregate = new JsonObject();
				aggregate.addProperty("publicationDate", uniqueDate);
				aggregate.addProperty("sentimentScore", avgSentiment);
				jsonD3jsArray.add(aggregate);
			}
			
			// create response object
			JsonObject result = new JsonObject();
			result.addProperty("searchdate", searchdate);
			result.addProperty("startdate", startdate);
			result.addProperty("enddate", enddate);
			result.addProperty("searchterm", searchterm);
			result.addProperty("count", count);
			result.add("result", jsonD3jsArray);
			
			response = new JsonArray();
			response.add(result);
			
		}catch(ParseException pe){
	    	  pe.printStackTrace();
	    }
		return response.toString();
	}

}
