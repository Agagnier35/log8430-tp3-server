package com.log8430.model.json;

import java.io.Serializable;

public class MostBoughtProductJSON implements Serializable {
	private static final long serialVersionUID = 7058444383647416343L;
	private ProductJSON productJSON;
	private int nbOfTimes;

	public MostBoughtProductJSON(ProductJSON productJSON, int nbOfTimes) {
		this.productJSON = productJSON;
		this.nbOfTimes = nbOfTimes;
	}

	public MostBoughtProductJSON() {
	}

	public ProductJSON getProductJSON() {
		return productJSON;
	}

	public void setProductJSON(ProductJSON productJSON) {
		this.productJSON = productJSON;
	}

	public int getNbOfTimes() {
		return nbOfTimes;
	}

	public void setNbOfTimes(int nbOfTimes) {
		this.nbOfTimes = nbOfTimes;
	}

	@Override
	public String toString() {
		return productJSON.toString() + "bought " + nbOfTimes + " times.";
	}
}
