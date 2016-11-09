package com.remkohde.dev.liberty.nosql;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.CouchDbException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.remkohde.dev.liberty.api.BluemixConfig;

public class CloudantClientMgr {

	private static CloudantClient cloudant = null;
	private static Database db = null;

	private CloudantClientMgr() {
	}
	
	private static void initClient() {
		if (cloudant==null) {
			synchronized (CloudantClientMgr.class) {
				if (cloudant != null) {
					return;
				}
				cloudant = createClient();
			} // end synchronized
		}
	}

	private static CloudantClient createClient() {		
		String user = BluemixConfig.getInstance().getCloudantDBUsername();
		String password = BluemixConfig.getInstance().getCloudantDBPassword();	
		try {
			System.out.print("=====Create CloudantClient");
			CloudantClient client = ClientBuilder.account(user)
					.username(user)
					.password(password)
					.build();
			return client;
		} catch (CouchDbException e) {
			throw new RuntimeException("Unable to connect to repository", e);
		} catch (Exception e){
			throw new RuntimeException("Exception ...", e);
		}
	} 

	public static Database getDB() {
		if (cloudant==null) {
			initClient();
		}
		if (db == null) {
			try {
				db = cloudant.database(BluemixConfig.getInstance().getCloudantDatabaseName(), true);
			} catch (Exception e) {
				throw new RuntimeException("DB Not found", e);
			}
		}
		return db;
	}
	
}
