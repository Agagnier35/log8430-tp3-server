package com.log8430.dao;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import com.datastax.driver.core.Session;
import com.datastax.spark.connector.cql.CassandraConnector;

public class SparkTools implements AutoCloseable {
	public static final String KEYSPACE_NAME = "invoices";
	public static final String INVOICE_TABLE_NAME = "invoiceitem";
	public static final String PRODUCT_TABLE_NAME = "product";
	public static final String APP_NAME = "FreqAssociation";
	private Session session;
	private JavaSparkContext sc;

	public SparkTools(URL mainJar) throws Exception {
		System.out.println("----------------CREATE SPARK CONF-------------------");

		String sparkIp = System.getProperty("spark.host.address");
		String cassIp = System.getProperty("cassandra.host.address");

		//This is the hacky way, the right way would be to build one uber-jar, but it doesnt work well with tomcat's classLoader
		File jardir = new File(mainJar.toURI()).getParentFile();
		String[] jars = Arrays.stream(jardir.listFiles())
				.map(this::mapFileToPath)
				.toArray(String[]::new);

		SparkConf conf = new SparkConf()
				.setAppName(APP_NAME)
				.setMaster("spark://" + sparkIp + ":7077")
				.set("spark.cassandra.connection.host", cassIp)
				.set("spark.cassandra.connection.port", "9042")
				.set("spark.local.dir", "/tmp")
				.setJars(jars);
		sc = new JavaSparkContext(conf);

		CassandraConnector connector = CassandraConnector.apply(sc.getConf());
		session = connector.openSession();
		createKeyspace(KEYSPACE_NAME, "SimpleStrategy", 1);
		createProductTable();
		createInvoiceItemTable();

		System.out.println("----------------END SPARK CONF-------------------");
	}

	private String mapFileToPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return "";
		}
	}

	@Override
	public void close() {
		session.close();
		sc.close();
		System.out.println("----------------CLOSED SPARK-------------------");
	}

	private void createKeyspace(String keyspaceName, String replicationStrategy, int replicationFactor) {
		StringBuilder sb =
				new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
						.append(keyspaceName).append(" WITH replication = {")
						.append("'class':'").append(replicationStrategy)
						.append("','replication_factor':").append(replicationFactor)
						.append("};");

		String query = sb.toString();
		session.execute(query);
	}

	private void createInvoiceItemTable() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append(getFullTableName(INVOICE_TABLE_NAME)).append("(")
				.append("id uuid PRIMARY KEY, ")
				.append("productname text,")
				.append("quantity int);");

		String query = sb.toString();
		session.execute(query);
	}

	private void createProductTable() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append(getFullTableName(PRODUCT_TABLE_NAME)).append("(")
				.append("productname text PRIMARY KEY,")
				.append("price double);");

		String query = sb.toString();
		session.execute(query);
	}

	private static String getFullTableName(String tableName) {
		return KEYSPACE_NAME + "." + tableName;
	}

	public JavaSparkContext getSc() {
		return sc;
	}
}
