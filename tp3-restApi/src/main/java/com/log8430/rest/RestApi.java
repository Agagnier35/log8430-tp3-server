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
import com.log8430.util.SparkThreadExceptionHandler;
import com.log8430.util.SparkThreadFactory;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("/")
public class RestApi extends ResourceConfig {

	public static final String MAIN_JAR_LOCATION = "/WEB-INF/lib/tp3-core-1.0-SNAPSHOT.jar";

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
			URL mainJar = ServletContextHolder.context.getResource(MAIN_JAR_LOCATION);
			if (mainJar == null) {
				throw new Exception("Missing main jar");
			}

			SparkCreateInvoiceJob job = new SparkCreateInvoiceJob(invoiceJSON, mainJar);
			Throwable t = runSparkThread(job);

			if (t != null) {
				return Response.status(500).entity(t.getStackTrace()).build();
			}

			return Response.status(201).entity(invoiceJSON).build();
		} catch (Exception e) {
			return Response.status(500).entity(e.getStackTrace()).build();
		}
	}

	@GET
	@Path("/top")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMostBoughtProduct() {
		System.out.println("GET top");
		try {
			URL mainJar = ServletContextHolder.context.getResource(MAIN_JAR_LOCATION);
			if (mainJar == null) {
				throw new Exception("Missing main jar");
			}

			SparkGetMostBoughtProduct job = new SparkGetMostBoughtProduct(mainJar);
			Throwable t = runSparkThread(job);

			if (t != null) {
				return Response.status(500).entity(t.getStackTrace()).build();
			}
			System.out.println(job.response);
			return Response.status(200).entity(job.response).build();
		} catch (Exception e) {
			return Response.status(500).entity(e.getStackTrace()).build();
		}
	}

	private Throwable runSparkThread(Runnable job) {
		try {
			System.out.println("------------T Start----------------");

			SparkThreadExceptionHandler handler = new SparkThreadExceptionHandler();
			SparkThreadFactory factory = new SparkThreadFactory(handler);

			Thread t = factory.newThread(job);
			t.start();
			t.join();//blocks and wait
			System.out.println("------------T stop----------------");
			return handler.t;
		} catch (Exception e) {
			return e;
		}
	}
}
