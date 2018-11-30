package com.log8430.model.entity;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static com.log8430.dao.SparkTools.INVOICE_TABLE_NAME;
import static com.log8430.dao.SparkTools.KEYSPACE_NAME;

@Table(keyspace = KEYSPACE_NAME, name = INVOICE_TABLE_NAME)
public class InvoiceItem implements Serializable {
	private static final long serialVersionUID = -291357288377562126L;
	@PartitionKey
	private UUID id;
	private String productname;
	private int quantity;

	public InvoiceItem() {
	}

	public InvoiceItem(UUID id, String productname, int quantity) {
		this.id = id;
		this.productname = productname;
		this.quantity = quantity;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
