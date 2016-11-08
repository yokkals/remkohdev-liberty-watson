package com.remkohde.dev.liberty.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BluemixConfig {

	private static BluemixConfig instance = null;
	
	private JsonObject vcapServices = null;
	private String alchemyApikey = null;

	protected BluemixConfig() {
		loadVcapServices();
		parseAlchemyApikey();
	}
	
	public static BluemixConfig getInstance() {
		if(instance == null) {
			instance = new BluemixConfig();
		}
		return instance;
	}
	
	/**
	 * AlchemyApikey
	 * @return
	 */
	public String getAlchemyApikey() {
		return alchemyApikey;
	}
	
	/**
	 * Method to load the Bluemix configuration from either the Bluemix system 
	 * variable VCAP_SERVICES and if not available from Bluemix, for instance
	 * because running on localhost, load the configuration from a local 
	 * properties file ~/WebContent/WEB-INF/classes/bluemix.json
	 */
	private void loadVcapServices(){
		try {
			String vcapServices = System.getenv("VCAP_SERVICES");
			if (vcapServices != null) {
				this.vcapServices = (JsonObject) new JsonParser().parse(vcapServices);
			}else{
				InputStream in = getClass().getClassLoader().getResourceAsStream("bluemix.json");				
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));		
				StringBuilder sb = new StringBuilder();
				String inputStr;
				while((inputStr = streamReader.readLine()) != null){
				    sb.append(inputStr);
				}			
				this.vcapServices = (JsonObject) new JsonParser().parse(sb.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}
	
	/**
	 * Load the AlchemyAPI apikey from the Bluemix configuration.
	 */
	private void parseAlchemyApikey() {		
		JsonArray alchemyAPIConfig = vcapServices.getAsJsonArray("alchemy_api");
		JsonObject alchemyAPIConfig1 = (JsonObject) alchemyAPIConfig.get(0); 
		JsonObject alchemyAPICredentials = alchemyAPIConfig1.getAsJsonObject("credentials");
		this.alchemyApikey = alchemyAPICredentials.get("apikey").getAsString();
		System.out.println("AlchemyApikey: "+alchemyApikey);		
	}
}