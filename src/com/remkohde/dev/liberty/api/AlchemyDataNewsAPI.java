package com.remkohde.dev.liberty.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyDataNews;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentsResult;


@Path("/watson/news")
/**
 * REST service to search AlchemyData News API.
 * @see http://docs.alchemyapi.com/
 */
public class AlchemyDataNewsAPI {
	
	public AlchemyDataNewsAPI() {
		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			@QueryParam("startdate") String startdate, 
			@QueryParam("enddate") String enddate, 
			@QueryParam("searchterm") String searchterm, 
			@QueryParam("count") String count) 
	throws Exception {		
		
		// format arguments
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");	
		// startdate
		Calendar startdate1 = Calendar.getInstance();
		startdate1.setTime(format1.parse(startdate));
		long startdate2 = startdate1.getTimeInMillis()/1000;
		// enddate
		Calendar enddate1 = Calendar.getInstance();
		enddate1.setTime(format1.parse(enddate));
		long enddate2 = enddate1.getTimeInMillis()/1000;
		// count
		int cnt = Integer.valueOf(count).intValue();
		
		// get alchemydata news 
		DocumentsResult result =  this.getAlchemyDataNews(startdate2, enddate2, searchterm, cnt) ;
		
		// serialize results to json
		Gson gson = new Gson();
		String json = gson.toJson(result);

		// create response
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("docs", json);
		jsonObject.addProperty("startdate", startdate);
		jsonObject.addProperty("enddate", enddate);
		jsonObject.addProperty("searchterm", searchterm);
		jsonObject.addProperty("count", count);
		
		JsonArray jsonArrayResponse = new JsonArray();
		jsonArrayResponse.add(jsonObject);

		return Response.ok(jsonArrayResponse.toString()).build();	
	}
	
	/**
	 * 
	 * @param startdate
	 * @param enddate
	 * @param searchTerm
	 * @param entityType
	 * @param count
	 * @return
	 */
	public DocumentsResult getAlchemyDataNews(long startdate, long enddate, String searchTerm, int count) {		

		// Configure the AlchemyAPI Data News
		AlchemyDataNews service = new AlchemyDataNews(BluemixConfig.getInstance().getAlchemyApikey());

	    Map<String, Object> params = new HashMap<String, Object>();

	    String[] fields =
	        new String[] { "enriched.url.title", "enriched.url.url", "enriched.url.author", "enriched.url.publicationDate",
	            "enriched.url.enrichedTitle.entities", "enriched.url.enrichedTitle.docSentiment"};
	    params.put(AlchemyDataNews.RETURN, StringUtils.join(fields, ","));
	    params.put(AlchemyDataNews.START, startdate);
	    params.put(AlchemyDataNews.END, enddate);
	    params.put(AlchemyDataNews.COUNT, count);

	    // Query on adjacent nested fields:
	    params.put("q.enriched.url.enrichedTitle.keywords.keyword.text", searchTerm);
	    //params.put("q.enriched.url.enrichedTitle.entities.entity", "|text=IBM,type=company|");
	    //params.put("q.enriched.url.enrichedTitle.docSentiment.type", "positive");
	    //params.put("q.enriched.url.enrichedTitle.taxonomy.taxonomy_.label", "technology and computing");

	    DocumentsResult result = service.getNewsDocuments(params).execute();
	    
	    return result;
	}

	


}
