package com.log8430.model;

import java.util.ArrayList;
import java.util.List;

public class Invoice {
	public List<InvoiceItem> items = new ArrayList<>();

	public Invoice() {
	}

	@Override
	public String toString(){
		return "Items: \n" + items;
	}
}
