package com.kiva.partner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Get a listing of Kiva Partners from the Kiva API
 * 
 * @author prachi
 */
public class KivaClient {
	private static final Logger LOGGER = Logger.getLogger(KivaClient.class);
	private static final String KIVA_URL = "http://api.kivaws.org/v1/partners.json";

	public List<Partner> getPartners() {
		List<Partner> allPartners = new ArrayList<Partner>();
		try {
			String partnerData = fetchPartner();

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(partnerData);
			JSONArray partners = (JSONArray) jsonObject.get("partners");

			@SuppressWarnings("unchecked")
			Iterator<JSONObject> it = partners.iterator();
			while (it.hasNext()) {
				JSONObject jo = (JSONObject) it.next();

				LOGGER.info("ADD: " + "id: " + jo.get("id") + ", name: " + jo.get("name")
						+ ", status: " + jo.get("status"));

				int id = Integer.valueOf(jo.get("id").toString());

				// some names have international accents, e.g. Banco Pérola
				// replacing the accents with english here
				// as this was causing the insert into salesforce to fail..
				String name = KivaUtils.normalizeName(jo.get("name")
						.toString());
				String status = jo.get("status").toString();

				Partner p = new Partner(id, name, status);
				allPartners.add(p);
			}
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return allPartners;
	}

	private String fetchPartner() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(KIVA_URL);
		String content = "";
		HttpResponse response;
		try {
			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			content = KivaUtils.getResponse(response);
			LOGGER.info("FETCH: " + content);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return content.toString();
	}
}
