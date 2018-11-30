package com.log8430.model.converter;

import com.log8430.model.entity.Product;
import com.log8430.model.json.ProductJSON;

public class ProductConverter {
	public static Product convert(ProductJSON json){
		return new Product(json.getName(), json.getPrice());
	}
}
