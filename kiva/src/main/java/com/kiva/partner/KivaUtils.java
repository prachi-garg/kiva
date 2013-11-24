package com.kiva.partner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;

import org.apache.http.HttpResponse;

final class KivaUtils {

	private KivaUtils() {
	}

	static String getResponse(HttpResponse response) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		String line;
		StringBuilder content = new StringBuilder();
		while ((line = br.readLine()) != null) {
			content = content.append(line);
		}
		return content.toString();
	}
	
	/** 
	 * 	NOTE : some names have international accents, e.g. Banco Pérola
	 * 	replacing the accents with english here
	 *  as this was causing the insert into salesforce  to fail..
	 *  
	 * @param name partner name
	 * @return normalized name
	 */
	static String normalizeName(String name) {
		return Normalizer.normalize(name,
				Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "");
	}
}
