package com.log8430.model.json;

import java.io.Serializable;

public class ProductJSON implements Serializable {
	private String name;
	private double price;

	public ProductJSON() {
	}

	public ProductJSON(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Name: " + name + ", price: " + price;
	}
}
