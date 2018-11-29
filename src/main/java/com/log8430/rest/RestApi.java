package com.log8430.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.fpm.*;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.log8430.dao.CassandraTools;
import com.log8430.model.converter.InvoiceItemConverter;
import com.log8430.model.converter.ProductConverter;
import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.entity.Product;
import com.log8430.model.json.InvoiceItemJSON;
import com.log8430.model.json.InvoiceJSON;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.*;
import static com.log8430.dao.CassandraTools.*;

/**
 * Root resource (exposed at "hello" path)
 */
@Path("/")
public class RestApi extends ResourceConfig {
	private JavaSparkContext sc;
	private Mapper<Product> productRepository;

	public RestApi(){
		register(JacksonFeature.class);
	}

	private void initSpark() {
		System.out.println("----------------STARTED BUILD RESTAPI-------------------");
		sc = CassandraTools.getSc();
		MappingManager manager = CassandraTools.getManager();
		this.productRepository = manager.mapper(Product.class);
		System.out.println("----------------FINISHED BUILD RESTAPI-------------------");
	}

	private void stopSpark() {
		CassandraTools.stop();
	}

	@POST
	@Path("/invoice")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createInvoice(InvoiceJSON invoiceJSON) {
		System.out.println("POST: " + invoiceJSON.toString());
		try {
			initSpark();

			List<Product> products = invoiceJSON.getItems().stream()
					.map(InvoiceItemJSON::getProductJSON)
					.map(ProductConverter::convert)
					.collect(Collectors.toList());

			JavaRDD<Product> productJavaRDD = sc.parallelize(products);
			javaFunctions(productJavaRDD)
					.writerBuilder(KEYSPACE_NAME, PRODUCT_TABLE_NAME, mapToRow(Product.class))
					.saveToCassandra();

			List<InvoiceItem> invoices = invoiceJSON.getItems().stream()
					.map(InvoiceItemConverter::convert)
					.collect(Collectors.toList());

			JavaRDD<InvoiceItem> invoicesRdd = sc.parallelize(invoices);
			javaFunctions(invoicesRdd)
					.writerBuilder(KEYSPACE_NAME, INVOICE_TABLE_NAME, mapToRow(InvoiceItem.class))
					.saveToCassandra();

			return Response.status(201).entity(invoiceJSON).build();
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		} finally {
			stopSpark();
		}
	}

	@GET
	@Path("/top")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMostBoughtProduct() {
		System.out.println("GET top");
		try {
			initSpark();

			StringBuilder b = new StringBuilder();

			JavaRDD<InvoiceItem> invoiceRDD = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, INVOICE_TABLE_NAME, mapRowTo(InvoiceItem.class));

			JavaRDD<List<Product>> products = sc.parallelize(mapInvoiceToItems(invoiceRDD));
			FPGrowth fpg = new FPGrowth()
					.setMinSupport(0.2)
					.setNumPartitions(10);
			FPGrowthModel<Product> model = fpg.run(products);

			for (FPGrowth.FreqItemset<Product> itemset : model.freqItemsets().toJavaRDD().collect()) {
				b.append("[" + itemset.javaItems() + "], " + itemset.freq()).append("\n");
			}

			double minConfidence = 0.8;
			for (AssociationRules.Rule<Product> rule
					: model.generateAssociationRules(minConfidence).toJavaRDD().collect()) {
				b.append(rule.javaAntecedent() + " => " + rule.javaConsequent() + ", " + rule.confidence()).append("\n");
			}

			return Response.status(200).entity(b.toString()).build();
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		} finally {
			stopSpark();
		}
	}

	private List<List<Product>> mapInvoiceToItems(JavaRDD<InvoiceItem> invoiceRDD) {
		List<List<Product>> productsMultiples = new ArrayList<>();
		invoiceRDD.foreach(i -> {
			List<Product> multiples = new ArrayList<>();
			Product product = productRepository.get(i.getProductName());
			for (int j = 0; j < i.getQuantity(); j++) {
				multiples.add(product);
			}
			productsMultiples.add(multiples);
		});
		return productsMultiples;
	}
}
