package com.remkohde.dev.liberty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


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
		
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("startdate", startdate);
		jsonObject.addProperty("enddate", enddate);
		jsonObject.addProperty("searchterm", searchterm);
		jsonObject.addProperty("count", count);
		jsonArrayResponse.add(jsonObject);

		return Response.ok(jsonArrayResponse.toString()).build();
	}

}
