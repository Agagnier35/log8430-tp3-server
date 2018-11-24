package com.log8430.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.log8430.model.*;

/**
 * Root resource (exposed at "hello" path)
 */
@Path("/")
public class RestApi {

	@POST
	@Path("/invoice")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createInvoice(Invoice invoice) {
		System.out.println("POST: " + invoice.toString());

		return Response.status(201).entity(invoice).build();
	}

	@GET
	@Path("/top")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MostBoughtProduct> getMostBoughtProduct(){
		System.out.println("GET top");

		List<MostBoughtProduct> productList = new ArrayList<>();

		Product product1 = new Product("Swag", 999.99);
		Product product2 = new Product("Swag", 999.99);
		Product product3 = new Product("Swag", 999.99);

		MostBoughtProduct top1 = new MostBoughtProduct(product1, 9999);
		MostBoughtProduct top2 = new MostBoughtProduct(product2, 9998);
		MostBoughtProduct top3 = new MostBoughtProduct(product3, 9997);

		productList.add(top1);
		productList.add(top2);
		productList.add(top3);

		return productList;
	}
}
