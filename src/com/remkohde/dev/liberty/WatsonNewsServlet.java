package com.remkohde.dev.liberty;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyDataNews;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Documents;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentsResult;


@Path("/watson/news")
/**
 * REST service to search AlchemyData News API.
 * @see http://docs.alchemyapi.com/
 */
public class WatsonNewsServlet {

	public WatsonNewsServlet() {
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@QueryParam("startdate") String startdate, @QueryParam("enddate") String enddate, 
			@QueryParam("searchterm") String searchterm, @QueryParam("count") String count) 
	throws Exception {
		
		JsonArray jsonArrayResponse = new JsonArray();
		
		DocumentsResult result =  this.getAlchemyDataNews() ;
		Documents docs = result.getDocuments();
		
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("startdate", startdate);
		jsonObject.addProperty("enddate", enddate);
		jsonObject.addProperty("searchterm", searchterm);
		jsonObject.addProperty("count", count);
		jsonArrayResponse.add(jsonObject);

		return Response.ok(jsonArrayResponse.toString()).build();
	}
	
	
	public DocumentsResult getAlchemyDataNews() {
		// call alchemyAPI Data News
				AlchemyDataNews service = new AlchemyDataNews("289dfc336c56524c0512211fdaca41e71f7145c9");

			    Map<String, Object> params = new HashMap<String, Object>();

			    String[] fields =
			        new String[] { "enriched.url.title", "enriched.url.url", "enriched.url.author", "enriched.url.publicationDate",
			            "enriched.url.enrichedTitle.entities", "enriched.url.enrichedTitle.docSentiment"};
			    params.put(AlchemyDataNews.RETURN, StringUtils.join(fields, ","));
			    params.put(AlchemyDataNews.START, "1440720000");
			    params.put(AlchemyDataNews.END, "1441407600");
			    params.put(AlchemyDataNews.COUNT, 7);

			    // Query on adjacent nested fields:
			    params.put("q.enriched.url.enrichedTitle.entities.entity", "|text=IBM,type=company|");
			    params.put("q.enriched.url.enrichedTitle.docSentiment.type", "positive");
			    params.put("q.enriched.url.enrichedTitle.taxonomy.taxonomy_.label", "technology and computing");

			    //Map<String, Object>
			    DocumentsResult result = service.getNewsDocuments(params).execute();

			    System.out.println(result);
			    
			    return result;
	}

}
