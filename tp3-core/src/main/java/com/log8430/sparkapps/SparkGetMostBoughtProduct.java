package com.log8430.sparkapps;

import java.net.URL;

import org.apache.spark.api.java.*;

import com.log8430.dao.SparkTools;
import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.entity.Product;

import scala.Tuple2;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;
import static com.log8430.dao.SparkTools.*;

public class SparkGetMostBoughtProduct implements Runnable {
	public Tuple2<Product, Integer> response;
	private URL mainJar;

	public SparkGetMostBoughtProduct(URL mainJar) {
		this.mainJar = mainJar;
	}

	@Override
	public void run() {
		try (SparkTools st = new SparkTools(mainJar)) {
			JavaSparkContext sc = st.getSc();

			JavaRDD<InvoiceItem> invoiceRDD = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, INVOICE_TABLE_NAME, mapRowTo(InvoiceItem.class));
			JavaPairRDD<String, Product> productRDD = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, PRODUCT_TABLE_NAME, mapRowTo(Product.class))
					.mapToPair(p -> new Tuple2<>(p.getProductname(), p));

			Tuple2<String, Integer> topProductName =
					invoiceRDD
							.mapToPair(i -> new Tuple2<>(i.getProductname(), i.getQuantity()))
							.reduceByKey((x, y) -> x + y)
							.mapToPair(Tuple2::swap)
							.sortByKey(false)
							.mapToPair(Tuple2::swap)
							.first();

			Tuple2<String, Product> topProduct = productRDD.filter(t -> t._1.equals(topProductName._1)).first();

			response = new Tuple2<>(topProduct._2, topProductName._2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
