package com.log8430.model;

public class InvoiceItem {
	public Product product;
	public int quantity;

	public InvoiceItem(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public InvoiceItem() {
	}

	@Override
	public String toString() {
		return product.toString() + ", Quantity: " + quantity + "\n";
	}
}
