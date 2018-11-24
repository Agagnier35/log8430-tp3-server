package com.log8430.model;

public class MostBoughtProduct {
	public Product product;
	public int nbOfTimes;

	public MostBoughtProduct(Product product, int nbOfTimes) {
		this.product = product;
		this.nbOfTimes = nbOfTimes;
	}

	public MostBoughtProduct() {
	}

	@Override
	public String toString() {
		return product.toString() + "bought " + nbOfTimes + " times.";
	}
}
