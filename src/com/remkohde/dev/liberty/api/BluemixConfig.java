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
	private String cloudantDBUsername = null;
	private String cloudantDBPassword = null;
	private String cloudantDBHost = null;
	private String cloudantDBPort = null;
	private static String cloudantDatabaseName = "alchemydatanews_searchresults";
	
	protected BluemixConfig() {
		loadVcapServices();
		parseAlchemyApikey();
		parseCloudantCredentials();
	}
	
	public static BluemixConfig getInstance() {
		if(instance==null) {		
			synchronized (BluemixConfig.class) {
				if (instance != null) {
					return instance;
				}
				instance = new BluemixConfig();
			} 
		}
		return instance;
	}
	
	/**
	 * AlchemyAPI
	 * @return
	 */
	public String getAlchemyApikey() {
		return alchemyApikey;
	}
	/**
	 * CloudantDB
	 * @return
	 */
	public String getCloudantDBUsername() {
		return cloudantDBUsername;
	}
	public String getCloudantDBPassword() {
		return cloudantDBPassword;
	}
	public String getCloudantDBHost() {
		return cloudantDBHost;
	}
	public String getCloudantDBPort() {
		return cloudantDBPort;
	}
	public String getCloudantDatabaseName() {
		return cloudantDatabaseName;
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
		if(alchemyAPIConfig==null){
			throw new RuntimeException("Could not find alchemy_api key in VCAP_SERVICES env variable");
		}
		JsonObject alchemyAPIConfig1 = (JsonObject) alchemyAPIConfig.get(0); 
		JsonObject alchemyAPICredentials = alchemyAPIConfig1.getAsJsonObject("credentials");
		this.alchemyApikey = alchemyAPICredentials.get("apikey").getAsString();
		System.out.println("AlchemyApikey: "+alchemyApikey);		
	}
	
	private void parseCloudantCredentials() {	
		JsonArray cloudantDBConfig = vcapServices.getAsJsonArray("cloudantNoSQLDB");
		if(cloudantDBConfig==null){
			throw new RuntimeException("Could not find cloudantNoSQLDB key in VCAP_SERVICES env variable");
		}
		JsonObject cloudantDBConfig1 = (JsonObject) cloudantDBConfig.get(0); 
		JsonObject cloudantDBCredentials = cloudantDBConfig1.getAsJsonObject("credentials");
		this.cloudantDBUsername = cloudantDBCredentials.get("username").getAsString();
		this.cloudantDBPassword = cloudantDBCredentials.get("password").getAsString();
		this.cloudantDBHost = cloudantDBCredentials.get("host").getAsString();
		this.cloudantDBPort = cloudantDBCredentials.get("port").getAsString();
		System.out.println("Cloudant credentials: "+cloudantDBUsername+":"+cloudantDBPassword+"@"+cloudantDBHost+":"+cloudantDBPort);
	}
}