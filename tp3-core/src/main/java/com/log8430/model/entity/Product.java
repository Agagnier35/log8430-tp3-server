package com.log8430.model.entity;

import java.io.Serializable;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static com.log8430.dao.SparkTools.KEYSPACE_NAME;
import static com.log8430.dao.SparkTools.PRODUCT_TABLE_NAME;

@Table(keyspace = KEYSPACE_NAME, name = PRODUCT_TABLE_NAME)
public class Product implements Serializable {
	private static final long serialVersionUID = 5527502868188108934L;
	@PartitionKey
	private String productname;
	private double price;

	public Product() {
	}

	public Product(String productname, double price) {
		this.productname = productname;
		this.price = price;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
