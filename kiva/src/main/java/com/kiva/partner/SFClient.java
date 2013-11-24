package com.kiva.partner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Post partner data to Salesforce
 * 
 * @author prachi
 */
public class SFClient {

	private static final Logger LOGGER = Logger.getLogger(SFClient.class);

	private static final String TOKEN_URL = "https://login.salesforce.com/services/oauth2/token";
	private static final String CLIENT_ID = "3MVG9A2kN3Bn17hsgK1.mIZBpgLuZ16QalIlYH51zknh3WQunl6YknUfaeNK.kkji2yORhiGyLQMbf1piNoYM";
	private static final String CLIENT_SECRET = "8084534462677289057";
	private static final String USER_NAME = "prachi_garg@yahoo.com";
	private static final String PASSWORD = "vishprachi9SmWrySU1yXVlk3oN6HPPPtsHr";
	private static final String SOBJECT_LEAD_URL = "/services/data/v26.0/sobjects/Lead/";

	/**
	 * NOTE: Using password based authentication
	 */
	private Map<String, String> getAccessToken() {
		Map<String, String> tokenMap = new HashMap<String, String>();
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost postRequest = new HttpPost(TOKEN_URL);
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("grant_type", "password"));
			urlParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
			urlParameters.add(new BasicNameValuePair("client_secret",
					CLIENT_SECRET));
			urlParameters.add(new BasicNameValuePair("username", USER_NAME));
			urlParameters.add(new BasicNameValuePair("password", PASSWORD));

			postRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			String content = KivaUtils.getResponse(response);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(content);
			String accessToken = (String) jsonObject.get("access_token");
			String instanceUrl = (String) jsonObject.get("instance_url");
			tokenMap.put("accessToken", accessToken);
			tokenMap.put("instanceUrl", instanceUrl);

			LOGGER.info("ACCESS_TOKEN : " + accessToken);
			LOGGER.info("INSTANCE_URL : " + instanceUrl);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return tokenMap;
	}

	/**
	 * inserts parters as Leads into Salesforce
	 * 
	 * @param partners
	 */
	public void insertLead(List<Partner> partners) {

		Map<String, String> tokenMap = getAccessToken();
		String accessToken = tokenMap.get("accessToken");
		String instanceUrl = tokenMap.get("instanceUrl");
		String url = instanceUrl + SOBJECT_LEAD_URL;

		HttpClient httpClient = new DefaultHttpClient();
		try {
			// create the post request
			HttpPost postRequest = new HttpPost(url);
			postRequest.addHeader("Authorization", "Bearer " + accessToken);
			postRequest.addHeader("Content-Type", "application/json");

			// post partners one at a time
			for (Partner partner : partners) {
				httpPostPartner(httpClient, postRequest, partner);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	private void httpPostPartner(HttpClient httpClient, HttpPost postRequest,
			Partner partner) throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		String partnerJson = getJsonObject(partner);
		StringEntity params = new StringEntity(partnerJson);
		postRequest.setEntity(params);

		HttpResponse response = httpClient.execute(postRequest);
		if (response.getStatusLine().getStatusCode() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}
		LOGGER.info("LEAD : " + KivaUtils.getResponse(response));
	}

	@SuppressWarnings("unchecked")
	private String getJsonObject(Partner partner) {

		JSONObject json = new JSONObject();
		String name = partner.getName();
		// LastName field in SalesForce as char limit of 80
		if (name.length() > 80) {
			name = name.substring(0, 80);
		}
		json.put("LastName", name);
		json.put("Company", "Kiva");
		json.put("Id__c", partner.getId());
		json.put("Name__c", partner.getName());
		json.put("Status__c", partner.getStatus());

		LOGGER.info("POST: " + "id: " + json.get("Id__c") + ", name :"
				+ json.get("Name__c").toString());
		return json.toString();
	}
}
