package com.remkohde.dev.liberty;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/watson/news/search")
/**
 * @see http://docs.alchemyapi.com/
 */
public class WatsonNewsSearchServlet {
	
	
	public WatsonNewsSearchServlet() {

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
		    @QueryParam("startdate") String startdate, @QueryParam("enddate") String enddate, 
			@QueryParam("searchterm") String searchterm, @QueryParam("count") String count) 
	throws Exception {		
		
		return Response.ok("okay").build();
	}
}
