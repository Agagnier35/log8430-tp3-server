package com.log8430.model.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InvoiceJSON implements Serializable {
	private List<InvoiceItemJSON> items = new ArrayList<>();

	public InvoiceJSON() {
	}

	public List<InvoiceItemJSON> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItemJSON> items) {
		this.items = items;
	}

	@Override
	public String toString(){
		return "Items: \n" + items;
	}
}
