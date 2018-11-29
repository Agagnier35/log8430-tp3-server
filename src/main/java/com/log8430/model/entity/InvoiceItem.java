package com.log8430.model.entity;

import java.util.UUID;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static com.log8430.dao.CassandraTools.INVOICE_TABLE_NAME;
import static com.log8430.dao.CassandraTools.KEYSPACE_NAME;

@Table(keyspace = KEYSPACE_NAME, name = INVOICE_TABLE_NAME)
public class InvoiceItem {
	@PartitionKey
	private UUID id;
	private String productName;
	private int quantity;

	public InvoiceItem() {
	}

	public InvoiceItem(UUID id, String productName, int quantity) {
		this.id = id;
		this.productName = productName;
		this.quantity = quantity;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
