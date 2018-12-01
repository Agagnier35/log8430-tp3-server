package com.log8430.sparkapps;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.log8430.dao.SparkTools;
import com.log8430.model.converter.InvoiceItemConverter;
import com.log8430.model.converter.ProductConverter;
import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.entity.Product;
import com.log8430.model.json.InvoiceItemJSON;
import com.log8430.model.json.InvoiceJSON;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

public class SparkCreateInvoiceJob implements Runnable {
	private InvoiceJSON invoiceJSON;
	private URL mainJar;

	public SparkCreateInvoiceJob(InvoiceJSON invoiceJSON, URL mainJar) {
		this.invoiceJSON = invoiceJSON;
		this.mainJar = mainJar;
	}

	@Override
	public void run() {
		try (SparkTools st = new SparkTools(mainJar)) {//auto-closeable
			JavaSparkContext sc = st.getSc();

			List<Product> products = invoiceJSON.getItems().stream()
					.map(InvoiceItemJSON::getProductJSON)
					.map(ProductConverter::convert)
					.collect(Collectors.toList());

			System.out.println("--------------SAVE PRODUCTS---------------");
			JavaRDD<Product> productJavaRDD = sc.parallelize(products);
			javaFunctions(productJavaRDD)
					.writerBuilder(SparkTools.KEYSPACE_NAME, SparkTools.PRODUCT_TABLE_NAME, mapToRow(Product.class))
					.saveToCassandra();

			List<InvoiceItem> invoices = invoiceJSON.getItems().stream()
					.map(InvoiceItemConverter::convert)
					.collect(Collectors.toList());

			System.out.println("--------------SAVE INVOICE---------------");
			JavaRDD<InvoiceItem> invoicesRdd = sc.parallelize(invoices);
			javaFunctions(invoicesRdd)
					.writerBuilder(SparkTools.KEYSPACE_NAME, SparkTools.INVOICE_TABLE_NAME, mapToRow(InvoiceItem.class))
					.saveToCassandra();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
