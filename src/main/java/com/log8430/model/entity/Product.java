package com.log8430.model.entity;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static com.log8430.dao.CassandraTools.KEYSPACE_NAME;
import static com.log8430.dao.CassandraTools.PRODUCT_TABLE_NAME;

@Table(keyspace = KEYSPACE_NAME, name = PRODUCT_TABLE_NAME)
public class Product {
	@PartitionKey
	private String productName;
	private double price;

	public Product() {
	}

	public Product(String productName, double price) {
		this.productName = productName;
		this.price = price;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
