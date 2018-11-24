package com.log8430.model;

public class Product {
	public String name;
	public double price;

	public Product(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public Product() {
	}

	@Override
	public String toString() {
		return "Name: " + name + ", price: " + price;
	}
}
