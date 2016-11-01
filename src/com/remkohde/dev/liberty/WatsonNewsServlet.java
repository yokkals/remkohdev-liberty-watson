package com.remkohde.dev.liberty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyDataNews;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Documents;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentsResult;


@Path("/watson/news")
/**
 * REST service to search AlchemyData News API.
 * @see http://docs.alchemyapi.com/
 */
public class WatsonNewsServlet {
	private JsonObject bluemixConf = null;
	private String alchemyApikey = null;
	
	public WatsonNewsServlet() {
		loadBluemixProperties();
		loadAlchemyApikey();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@QueryParam("startdate") String startdate, @QueryParam("enddate") String enddate, 
			@QueryParam("searchterm") String searchterm, @QueryParam("count") String count) 
	throws Exception {		
		JsonArray jsonArrayResponse = new JsonArray();
		
		DocumentsResult result =  this.getAlchemyDataNews(searchterm, "company") ;
		Documents docs = result.getDocuments();
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("startdate", startdate);
		jsonObject.addProperty("enddate", enddate);
		jsonObject.addProperty("searchterm", searchterm);
		jsonObject.addProperty("count", count);
		jsonArrayResponse.add(jsonObject);

		return Response.ok(jsonArrayResponse.toString()).build();
	}
	
	
	public DocumentsResult getAlchemyDataNews(String searchTerm, String entityType) {		
		// Configure the AlchemyAPI Data News
		AlchemyDataNews service = new AlchemyDataNews(alchemyApikey);

	    Map<String, Object> params = new HashMap<String, Object>();

	    String[] fields =
	        new String[] { "enriched.url.title", "enriched.url.url", "enriched.url.author", "enriched.url.publicationDate",
	            "enriched.url.enrichedTitle.entities", "enriched.url.enrichedTitle.docSentiment"};
	    params.put(AlchemyDataNews.RETURN, StringUtils.join(fields, ","));
	    params.put(AlchemyDataNews.START, "1440720000");
	    params.put(AlchemyDataNews.END, "1441407600");
	    params.put(AlchemyDataNews.COUNT, 7);

	    // Query on adjacent nested fields:
	    params.put("q.enriched.url.enrichedTitle.entities.entity", "|text="+searchTerm+",type="+entityType+"|");
	    params.put("q.enriched.url.enrichedTitle.docSentiment.type", "positive");
	    params.put("q.enriched.url.enrichedTitle.taxonomy.taxonomy_.label", "technology and computing");

	    //Map<String, Object>
	    DocumentsResult result = service.getNewsDocuments(params).execute();

	    System.out.println(result);
	    
	    return result;
	}
	
	/**
	 * Method to load the Bluemix configuration from either the Bluemix system 
	 * variable VCAP_SERVICES and if not available from Bluemix, for instance
	 * because running on localhost, load the configuration from a local 
	 * properties file ~/WebContent/WEB-INF/classes/bluemix.json
	 */
	private void loadBluemixProperties(){
		try {
			String vcapServices = System.getenv("VCAP_SERVICES");
			if (vcapServices != null) {
				this.bluemixConf = (JsonObject) new JsonParser().parse(vcapServices);
			}else{
				InputStream in = getClass().getClassLoader().getResourceAsStream("bluemix.json");				
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));		
				StringBuilder sb = new StringBuilder();
				String inputStr;
				while((inputStr = streamReader.readLine()) != null){
				    sb.append(inputStr);
				}			
				this.bluemixConf = (JsonObject) new JsonParser().parse(sb.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}
	
	/**
	 * Load the AlchemyAPI apikey from the Bluemix configuration.
	 */
	private void loadAlchemyApikey() {		
		JsonArray alchemyAPIConfig = bluemixConf.getAsJsonArray("alchemy_api");
		JsonObject alchemyAPIConfig1 = (JsonObject) alchemyAPIConfig.get(0); 
		JsonObject alchemyAPICredentials = alchemyAPIConfig1.getAsJsonObject("credentials");
		this.alchemyApikey = alchemyAPICredentials.get("apikey").getAsString();
		System.out.println("AlchemyApikey: "+alchemyApikey);		
	}

}
