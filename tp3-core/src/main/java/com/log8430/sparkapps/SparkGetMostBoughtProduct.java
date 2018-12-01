package com.log8430.sparkapps;

import java.net.URL;
import java.util.*;

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
					.mapToPair(p->new Tuple2<>(p.getProductname(), p));

			response = invoiceRDD
					.mapToPair(i -> new Tuple2<>(i.getProductname(), i.getQuantity()))
					.reduceByKey((x, y) -> x + y)
					.mapToPair(Tuple2::swap)
					.sortByKey(false)
					.mapToPair(Tuple2::swap)
					.join(productRDD)
					.first()._2.swap();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
