package com.log8430.model.json;

public class InvoiceItemJSON {
	private ProductJSON productJSON;
	private int quantity;

	public InvoiceItemJSON() {
	}

	public ProductJSON getProductJSON() {
		return productJSON;
	}

	public void setProductJSON(ProductJSON productJSON) {
		this.productJSON = productJSON;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return productJSON.toString() + ", Quantity: " + quantity + "\n";
	}
}
