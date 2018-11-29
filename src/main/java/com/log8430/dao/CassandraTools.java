package com.log8430.dao;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.spark.connector.cql.CassandraConnector;

public class CassandraTools {
	public static final String KEYSPACE_NAME = "Invoices";
	public static final String INVOICE_TABLE_NAME = "InvoiceItem";
	public static final String PRODUCT_TABLE_NAME = "Product";
	private static MappingManager manager;
	private static Session session;
	private static JavaSparkContext sc;

	static {
		String sparkIp = System.getProperty("spark.host.address");
		String cassIp = System.getProperty("cassandra.host.address");
		SparkConf conf = new SparkConf()
				.setAppName("FreqAssociation")
				.setMaster("spark://" + sparkIp + ":7077")
				.set("spark.cassandra.connection.host", cassIp)
				.set("spark.cassandra.connection.port", "9042");
		sc = new JavaSparkContext(conf);

		System.out.println("----------------CREATE SPARK CONF-------------------");

		CassandraConnector connector = CassandraConnector.apply(sc.getConf());
		session = connector.openSession();
		createKeyspace(KEYSPACE_NAME, "SimpleStrategy", 1);
		createProductTable();
		createInvoiceItemTable();
		manager = new MappingManager(session);

		System.out.println("----------------CONNECTED CASS-------------------");
	}

	public static void stop() {
		session.close();
		sc.close();
	}

	private static void createKeyspace(String keyspaceName, String replicationStrategy, int replicationFactor) {
		System.out.println("----------------CREATE KEYSPACE-------------------");
		StringBuilder sb =
				new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
						.append(keyspaceName).append(" WITH replication = {")
						.append("'class':'").append(replicationStrategy)
						.append("','replication_factor':").append(replicationFactor)
						.append("};");

		String query = sb.toString();
		session.execute(query);
	}

	private static void createInvoiceItemTable() {
		System.out.println("----------------CREATE INVOICEITEM T-------------------");
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append(getFullTableName(INVOICE_TABLE_NAME)).append("(")
				.append("id uuid PRIMARY KEY, ")
				.append("productName text,")
				.append("quantity int);");

		String query = sb.toString();
		session.execute(query);
	}

	private static void createProductTable() {
		System.out.println("----------------CREATE PRODUCT T-------------------");
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append(getFullTableName(PRODUCT_TABLE_NAME)).append("(")
				.append("productName text PRIMARY KEY,")
				.append("price double);");

		String query = sb.toString();
		session.execute(query);
	}

	private static String getFullTableName(String tableName) {
		return KEYSPACE_NAME + "." + tableName;
	}

	public static JavaSparkContext getSc() {
		return sc;
	}

	public static MappingManager getManager() {
		return manager;
	}
}
