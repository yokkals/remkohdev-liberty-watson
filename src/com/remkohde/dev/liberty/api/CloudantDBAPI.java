package com.remkohde.dev.liberty.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("/cloudant/data")
public class CloudantDBAPI {

	public CloudantDBAPI(){
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@QueryParam("try1") String try1) 
	throws Exception {	
		
		return Response.ok("Okay dan...").build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveJsonObject()
			throws Exception {
	    
		System.out.println("====save data to cloudant");
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("test", "Hello World");
		
		JsonArray jsonArrayResponse = new JsonArray();
		jsonArrayResponse.add(jsonObject);
		
		return Response.ok(jsonArrayResponse.toString()).build();	
	}
	
}
