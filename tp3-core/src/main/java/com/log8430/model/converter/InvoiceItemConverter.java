package com.log8430.model.converter;

import java.util.UUID;

import com.log8430.model.entity.InvoiceItem;
import com.log8430.model.json.InvoiceItemJSON;

public class InvoiceItemConverter {
	public static InvoiceItem convert(InvoiceItemJSON json) {
		return new InvoiceItem(UUID.randomUUID(), json.getProductJSON().getName(), json.getQuantity());
	}
}
