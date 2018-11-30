package com.log8430.sparkapps;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.fpm.*;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import com.log8430.dao.SparkTools;
import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.entity.Product;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;

import static com.log8430.dao.SparkTools.INVOICE_TABLE_NAME;
import static com.log8430.dao.SparkTools.KEYSPACE_NAME;

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

			MappingManager manager = st.getManager();
			Mapper<Product> productRepository = manager.mapper(Product.class);

			StringBuilder b = new StringBuilder();

			JavaRDD<InvoiceItem> invoiceRDD = javaFunctions(sc)
					.cassandraTable(KEYSPACE_NAME, INVOICE_TABLE_NAME, mapRowTo(InvoiceItem.class));

			JavaRDD<List<Product>> products = sc.parallelize(mapInvoiceToItems(invoiceRDD, productRepository));
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

			response = b.toString();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	private static List<List<Product>> mapInvoiceToItems(JavaRDD<InvoiceItem> invoiceRDD, Mapper<Product> productRepository) {
		List<List<Product>> productsMultiples = new ArrayList<>();
		invoiceRDD.foreach(i -> {
			List<Product> multiples = new ArrayList<>();
			Product product = productRepository.get(i.getProductname());
			for (int j = 0; j < i.getQuantity(); j++) {
				multiples.add(product);
			}
			productsMultiples.add(multiples);
		});
		return productsMultiples;
	}
}
