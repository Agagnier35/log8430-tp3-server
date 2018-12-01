package com.log8430.sparkapps;

import java.net.URL;
import java.util.*;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.fpm.*;

import com.log8430.dao.SparkTools;
import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.entity.Product;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;
import static com.log8430.dao.SparkTools.*;

public class SparkGetMostBoughtProduct implements Runnable {
	public String response;
	private URL mainJar;

	public SparkGetMostBoughtProduct(URL mainJar) {
		this.mainJar = mainJar;
	}

	@Override
	public void run() {
		try (SparkTools st = new SparkTools(mainJar)) {
			JavaSparkContext sc = st.getSc();

			StringBuilder b = new StringBuilder();
			b.append("CANCER MOST BOUGHT:\n\n\n\n\n");

			JavaRDD<InvoiceItem> invoiceRDD = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, INVOICE_TABLE_NAME, mapRowTo(InvoiceItem.class));
			List<Product> products = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, PRODUCT_TABLE_NAME, mapRowTo(Product.class)).collect();

			JavaRDD<List<Product>> productsRDD = sc.parallelize(mapInvoiceToItems(invoiceRDD, products));
			FPGrowth fpg = new FPGrowth()
					.setMinSupport(0.2)
					.setNumPartitions(10);
			FPGrowthModel<Product> model = fpg.run(productsRDD);

			for (FPGrowth.FreqItemset<Product> itemset : model.freqItemsets().toJavaRDD().collect()) {
				b.append("[" + itemset.javaItems() + "], " + itemset.freq()).append("\n");
			}

			double minConfidence = 0.8;
			for (AssociationRules.Rule<Product> rule
					: model.generateAssociationRules(minConfidence).toJavaRDD().collect()) {
				b.append(rule.javaAntecedent() + " => " + rule.javaConsequent() + ", " + rule.confidence()).append("\n");
			}
			System.out.println(b.toString());
			response = b.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<List<Product>> mapInvoiceToItems(JavaRDD<InvoiceItem> invoiceRDD, List<Product> products) {
		List<List<Product>> productsMultiples = new ArrayList<>();
		invoiceRDD.foreach(i -> {
			List<Product> multiples = new ArrayList<>();
			Optional<Product> matchingProduct = products.stream()
					.filter(p -> p.getProductname().equals(i.getProductname()))
					.findFirst();
			matchingProduct.ifPresent(p -> {
				for (int j = 0; j < i.getQuantity(); j++) {
					multiples.add(p);
				}
				productsMultiples.add(multiples);
			});
		});
		return productsMultiples;
	}
}
