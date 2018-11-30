package com.log8430.rest;

import java.net.URL;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.log8430.model.json.InvoiceJSON;
import com.log8430.sparkapps.SparkCreateInvoiceJob;
import com.log8430.sparkapps.SparkGetMostBoughtProduct;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("/")
public class RestApi extends ResourceConfig {

	public static final String MAIN_JAR_LOCATION = "/WEB-INF/uberJar/tp3-core.jar";

	public RestApi() {
		register(JacksonFeature.class);
	}

	@POST
	@Path("/invoice")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createInvoice(InvoiceJSON invoiceJSON) {
		System.out.println("POST: " + invoiceJSON.toString());

		try {
			URL mainJar = ServletContextHolder.context.getResource("/WEB-INF/lib/tp3-core-1.0-SNAPSHOT.jar");
			if(mainJar == null){
				throw new Exception("Missing main jar");
			}

			Thread t = new Thread(new SparkCreateInvoiceJob(invoiceJSON, mainJar));
			System.out.println("------------T Start----------------");
			t.start();

			t.join();//blocks and wait
			System.out.println("------------T stop----------------");

			return Response.status(201).entity(invoiceJSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/top")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMostBoughtProduct() {
		System.out.println("GET top");
		try {
			URL mainJar = ServletContextHolder.context.getResource(MAIN_JAR_LOCATION);
			SparkGetMostBoughtProduct job = new SparkGetMostBoughtProduct(mainJar);
			Thread t = new Thread(job);
			System.out.println("------------T Start----------------");
			t.start();

			t.join();//blocks and wait
			System.out.println("------------T stop----------------");

			return Response.status(200).entity(job.response).build();
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		}
	}
}
