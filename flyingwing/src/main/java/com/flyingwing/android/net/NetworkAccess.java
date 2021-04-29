/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.6.6
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public class NetworkAccess {

	public static final int CONNECTION_NO_NETWORK = 100;
	public static final int CONNECTION_CONNECT_FAIL = 101;
	public static final int CONNECTION_CONNECTED = 102;
	public static final int CONNECTION_LOAD_FAIL = 103;
	public static final int CONNECTION_LOADED = 104;
	public static final int OUTPUT_TYPE_BYTES = 0;
	public static final int OUTPUT_TYPE_STRING = 1;
	public static final int OUTPUT_TYPE_FILE = 2;
	public static final int OUTPUT_TYPE_SKIP = 3;
	public static final String[] ONLY_READ_HEADER = new String[]{"Range", "bytes=0-0"};
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_DELETE = "DELETE";
	public static final String HTTP_METHOD_HEAD = "HEAD";
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
	public static final String HTTP_METHOD_TRACE = "TRACE";
	public static final String CONTENT_TYPE_PLAIN = "text/plain";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private static final NetworkSetting NETWORKSETTING = new NetworkSetting();

	public static class NetworkSetting {
		private int mConnectTimeout = 20000;
		private int mReadTimeout = 65000;
		private int mBufferSize = 1024 * 16;
		private boolean mIsPrintConnectionUrl = true;
		private boolean mIsPrintConnectException = true;
		private boolean mIsPrintConnectionRequest = false;
		private boolean mIsPrintConnectionResponse = false;
	}

	public static void setConnectTimeout(int connectTimeout){
		NetworkAccess.NETWORKSETTING.mConnectTimeout = connectTimeout;
	}

	public static int getConnectTimeout(){
		return NetworkAccess.NETWORKSETTING.mConnectTimeout;
	}

	public static void setReadTimeout(int readTimeout){
		NetworkAccess.NETWORKSETTING.mReadTimeout = readTimeout;
	}

	public static int getReadTimeout(){
		return NetworkAccess.NETWORKSETTING.mReadTimeout;
	}

	public static void setBufferSize(int bufferSize){
		NetworkAccess.NETWORKSETTING.mBufferSize = bufferSize;
	}

	public static int getBufferSize(){
		return NetworkAccess.NETWORKSETTING.mBufferSize;
	}

	public static void setPrintConnectionUrl(boolean isPrintConnectionUrl){
		NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl = isPrintConnectionUrl;
	}

	public static boolean isPrintConnectionUrl(){
		return NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl;
	}

	public static void setPrintConnectException(boolean isPrintConnectException){
		NetworkAccess.NETWORKSETTING.mIsPrintConnectException = isPrintConnectException;
	}

	public static boolean isPrintConnectException(){
		return NetworkAccess.NETWORKSETTING.mIsPrintConnectException;
	}

	public static void setPrintConnectRequest(boolean isPrintConnectRequest){
		NetworkAccess.NETWORKSETTING.mIsPrintConnectionRequest = isPrintConnectRequest;
	}

	public static boolean isPrintConnectRequest(){
		return NetworkAccess.NETWORKSETTING.mIsPrintConnectionRequest;
	}

	public static void setPrintConnectResponse(boolean isPrintConnectResponse){
		NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse = isPrintConnectResponse;
	}

	public static boolean isPrintConnectResponse(){
		return NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse;
	}

	private static void printInfo(String info){
		System.out.println(info);
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAvailableByInternet(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			Network network = connectivityManager.getActiveNetwork();
			if(network == null){
				return false;
			}
			NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
			return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	private static ConnectionResult getNetworkCheckConnectResult(@NonNull Context context){
		ConnectionResult connectionResult = new ConnectionResult();
		if(!isAvailableByInternet(context)){
			connectionResult.setStatusCode(CONNECTION_NO_NETWORK);
			connectionResult.setStatusMessage("Connection check failed, no network connection");
		}
		return connectionResult;
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttps(@NonNull String strUrl, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		HttpURLConnection httpURLConnection = null;
		try {
			URL url = new URL(strUrl);
			httpURLConnection = (HttpURLConnection) url.openConnection();

			if(httpURLConnection instanceof HttpsURLConnection){
				SSLSocketFactory sslSocketFactory;
				if(sslContext == null){
					sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
				}else{
					sslSocketFactory = sslContext.getSocketFactory();
				}
				((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sslSocketFactory);
				if(hostnameVerifier != null){
					((HttpsURLConnection) httpURLConnection).setHostnameVerifier(hostnameVerifier);
				}
			}
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection open failed, exception " + e + ", " + strUrl);
			}
		}
		return httpURLConnection;
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttps(@NonNull String strUrl){
		return openHttpURLConnectionWithHttps(strUrl, null, null);
	}

	public static SSLContext getSSLContext(InputStream inputStreamServer, String keyStoreTypeServer, String keyStorePasswordServer
			, InputStream inputStreamClient, String keyStoreTypeClient, String keyStorePasswordClient){
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			if(inputStreamServer != null){
				try {
					KeyStore keyStoreServer = KeyStore.getInstance(keyStoreTypeServer == null ? KeyStore.getDefaultType() : keyStoreTypeServer);
					if(TextUtils.isEmpty(keyStorePasswordServer)){
						// 憑證處理
						CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
						Certificate certificate = certificateFactory.generateCertificate(inputStreamServer);

						keyStoreServer.load(null, null);
						// 載入Server憑證到金鑰庫
						keyStoreServer.setCertificateEntry("trust", certificate);
					}else{
						// 載入Server公鑰及密碼到金鑰庫
						keyStoreServer.load(inputStreamServer, keyStorePasswordServer.toCharArray());
					}

					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					// 載入Server金鑰庫到信任管理器
					trustManagerFactory.init(keyStoreServer);

					KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					if(inputStreamClient != null){
						KeyStore keyStoreClient = KeyStore.getInstance(keyStoreTypeClient == null ? KeyStore.getDefaultType() : keyStoreTypeClient);
						char[] keyStorePasswordClientChars = keyStorePasswordClient == null ? null : keyStorePasswordClient.toCharArray();
						// 載入Client公鑰及密碼到金鑰庫
						keyStoreClient.load(inputStreamClient, keyStorePasswordClientChars);

						// 載入Client金鑰庫到金鑰管理器
						keyManagerFactory.init(keyStoreClient, keyStorePasswordClientChars);
					}

					sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
					return sslContext;
				} catch (GeneralSecurityException | IOException e) {
					if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
						printInfo("Connection https certificate file init failed, exception " + e);
					}
				} finally {
					try {
						inputStreamServer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			// This SSL no verify and trust any certificate, very dangerous.
			sslContext.init(null, new TrustManager[]{new X509TrustManager() {
				@SuppressLint("TrustAllX509TrustManager")
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) {}

				@SuppressLint("TrustAllX509TrustManager")
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) {}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}}, new SecureRandom());
		} catch (GeneralSecurityException e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection https security init failed, exception " + e);
			}
		}
		return null;
	}

	/**
	 * This SSL no verify and trust any certificate, very dangerous.
	 */
	public static SSLContext getSSLContextNoCertificate(){
		return getSSLContext(null, null, null, null, null
				, null);
	}

	public static HostnameVerifier getHostnameVerifierWhiteList(final String[] allowHostnameArray, final boolean isOnly){
		return new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				if(allowHostnameArray != null){
					for(String allowHostname : allowHostnameArray){
						if(hostname.equals(allowHostname)){
							return true;
						}
					}
				}
				return !isOnly && HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
			}
		};
	}

	public static HostnameVerifier getHostnameVerifierBlackList(final String[] banHostnameArray, final boolean isOnly){
		return new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				if(banHostnameArray != null){
					for(String allowHostname : banHostnameArray){
						if(hostname.equals(allowHostname)){
							return false;
						}
					}
				}
				return isOnly || HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
			}
		};
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(@NonNull String strUrl, String httpMethod, String contentType
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		Object[] contentArray;
		if((TextUtils.isEmpty(httpMethod) || HTTP_METHOD_GET.equals(httpMethod)) && contentArrays != null && contentArrays.length > 0){
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(strUrl);
			boolean isContains = strUrl.contains("?");
			for(int i=0; i<contentArrays.length; i++){
				contentArray = contentArrays[i];
				if(contentArray == null || contentArray.length == 0 || contentArray[0] == null){
					continue;
				}
				stringBuilder.append(i == 0 && !isContains ? "?" : "&").append(contentArray[0]).append("=");
				if(contentArray.length > 1){
					stringBuilder.append(contentArray[contentArray.length - 1]);
				}
			}
			strUrl = stringBuilder.toString();
			contentArrays = null;
		}
		HttpURLConnection httpURLConnection = openHttpURLConnectionWithHttps(strUrl, sslContext, hostnameVerifier);
		boolean doOutput = false;
		if(contentArrays != null && contentArrays.length > 0){
			for(int i=0; i<contentArrays.length; i++){
				contentArray = contentArrays[i];
				if(contentArray == null || contentArray.length == 0 || contentArray[0] == null || contentArray.length < 2){
					continue;
				}
				doOutput = true;
				if(!TextUtils.isEmpty(contentType)){
					break;
				}
				if(contentArray[contentArray.length - 1] instanceof InputStream && TextUtils.isEmpty(contentType)){
					contentType = CONTENT_TYPE_MULTIPART;
					break;
				}
			}
		}
		if(TextUtils.isEmpty(contentType)){
			contentType = CONTENT_TYPE_URLENCODED;
		}
		if(!setHttpURLConnectionFieldsWithContentArrays(httpURLConnection, httpMethod, true, doOutput, false, true, contentType, headerArrays
				, contentArrays)){
			httpURLConnection = null;
		}
		return httpURLConnection;
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, httpMethod, null, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(@NonNull String strUrl, String httpMethod, String contentType
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, httpMethod, contentType, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, httpMethod, null, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, httpMethod, CONTENT_TYPE_URLENCODED, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, httpMethod, CONTENT_TYPE_MULTIPART, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(@NonNull String strUrl, String httpMethod, String contentType
			, String[][] headerArrays, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		if((TextUtils.isEmpty(httpMethod) || HTTP_METHOD_GET.equals(httpMethod)) && content != null){
			content = null;
		}
		HttpURLConnection httpURLConnection = openHttpURLConnectionWithHttps(strUrl, sslContext, hostnameVerifier);
		boolean doOutput = !TextUtils.isEmpty(content);
		if(TextUtils.isEmpty(contentType)){
			boolean isJson = false;
			try {
				JSONTokener jsonTokener = new JSONTokener(content);
				Object object = jsonTokener.nextValue();
				if(object instanceof JSONArray || object instanceof JSONObject){
					isJson = true;
				}
			} catch (Exception ignored) {}
			contentType = isJson ? CONTENT_TYPE_JSON : CONTENT_TYPE_PLAIN;
		}
		if(!setHttpURLConnectionFieldsWithContentStringUseFixedStreaming(httpURLConnection, httpMethod, true, doOutput, false, true, contentType
				, headerArrays, content)){
			httpURLConnection = null;
		}
		return httpURLConnection;
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, httpMethod, null, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(@NonNull String strUrl, String httpMethod, String contentType
			, String[][] headerArrays, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, httpMethod, contentType, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, httpMethod, null, headerArrays, content, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, httpMethod, CONTENT_TYPE_PLAIN, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(@NonNull String strUrl, String httpMethod
			, String[][] headerArrays, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, httpMethod, CONTENT_TYPE_JSON, headerArrays, content, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysGet(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_GET, null, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysPost(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_POST, null, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysPut(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_PUT, null, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysDelete(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_DELETE, null, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysGet(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_GET, null, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysPost(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_POST, null, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysPut(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_PUT, null, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysDelete(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArrays(strUrl, HTTP_METHOD_DELETE, null, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedGet(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_GET, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedPost(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_POST, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedPut(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_PUT, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedDelete(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_DELETE, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedGet(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_GET, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedPost(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_POST, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedPut(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_PUT, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncodedDelete(@NonNull String strUrl, String[][] headerArrays
			, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysUrlEncoded(strUrl, HTTP_METHOD_DELETE, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataGet(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_GET, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataPost(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_POST, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataPut(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_PUT, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataDelete(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_DELETE, headerArrays, contentArrays, sslContext
				, hostnameVerifier);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataGet(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_GET, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataPost(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_POST, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataPut(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_PUT, headerArrays, contentArrays, null
				, null);
	}

	/**
	 * @param contentArrays <br>
	 * contentArrays[][2] = String key, String value<br>
	 * contentArrays[][3] = String key, String fileName, Object object<br>
	 * contentArrays[][4] = String key, String fileName, String contentType(MIME Type), Object object
	 */
	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormDataDelete(@NonNull String strUrl
			, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentArraysMultipartFormData(strUrl, HTTP_METHOD_DELETE, headerArrays, contentArrays, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringGet(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_GET, null, headerArrays, content, sslContext
				, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringPost(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_POST, null, headerArrays, content, sslContext
				, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringPut(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_PUT, null, headerArrays, content, sslContext
				, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringDelete(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_DELETE, null, headerArrays, content, sslContext
				, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringGet(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_GET, null, headerArrays, content, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringPost(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_POST, null, headerArrays, content, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringPut(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_PUT, null, headerArrays, content, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentStringDelete(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentString(strUrl, HTTP_METHOD_DELETE, null, headerArrays, content, null
				, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainGet(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_GET, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainPost(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_POST, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainPut(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_PUT, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainDelete(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_DELETE, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainGet(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_GET, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainPost(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_POST, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainPut(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_PUT, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlainDelete(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentPlain(strUrl, HTTP_METHOD_DELETE, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONGet(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_GET, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONPost(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_POST, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONPut(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_PUT, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONDelete(@NonNull String strUrl, String[][] headerArrays
			, String content, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_DELETE, headerArrays, content, sslContext, hostnameVerifier);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONGet(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_GET, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONPost(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_POST, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONPut(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_PUT, headerArrays, content, null, null);
	}

	public static @Nullable HttpURLConnection openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSONDelete(@NonNull String strUrl, String[][] headerArrays
			, String content){
		return openHttpURLConnectionWithHttpsAndSetFieldsWithContentJSON(strUrl, HTTP_METHOD_DELETE, headerArrays, content, null, null);
	}

	public static boolean setHttpURLConnectionFieldsWithContentArrays(HttpURLConnection httpURLConnection, String httpMethod, boolean doInput, boolean doOutput
			, boolean useCaches, boolean keepAlive, String contentType, String[][] headerArrays, Object[][] contentArrays){
		if(httpURLConnection == null){
			return false;
		}
		String hyphens = "--";
		String boundary = "#!#!#!BOUNDARY!#!#!#";
		String breakLine = "\r\n";

		if(!TextUtils.isEmpty(contentType) && contentType.contains(CONTENT_TYPE_MULTIPART)){
			contentType = contentType + "; boundary=" + boundary;
		}
		if(setHttpURLConnectionFields(httpURLConnection, httpMethod, doInput, doOutput, useCaches, keepAlive, contentType, headerArrays)){
			if(!doOutput || contentArrays == null || contentArrays.length == 0){
				return true;
			}
			if(TextUtils.isEmpty(contentType) || contentType.contains(CONTENT_TYPE_URLENCODED)){
				return setHttpURLConnectionContentForUrlEncodedUseFixedStreaming(httpURLConnection, contentArrays);
			}else if(contentType.contains(CONTENT_TYPE_MULTIPART)){
				return setHttpURLConnectionContentForMultipartFormDataUseChunkStreaming(httpURLConnection, 0, hyphens, boundary, breakLine, contentArrays);
			}
		}
		return false;
	}

	public static boolean setHttpURLConnectionFieldsWithContentArrays(HttpURLConnection httpURLConnection, String httpMethod, String contentType
			, String[][] headerArrays, Object[][] contentArrays){
		if((TextUtils.isEmpty(httpMethod) || HTTP_METHOD_GET.equals(httpMethod)) && contentArrays != null){
			contentArrays = null;
		}
		boolean doOutput = contentArrays == null || contentArrays.length == 0;
		return setHttpURLConnectionFieldsWithContentArrays(httpURLConnection, httpMethod, true, doOutput, false, true, contentType
				, headerArrays, contentArrays);
	}

	public static boolean setHttpURLConnectionFieldsWithContentStringUseFixedStreaming(HttpURLConnection httpURLConnection, String httpMethod, boolean doInput
			, boolean doOutput, boolean useCaches, boolean keepAlive, String contentType, String[][] headerArrays, String content){
		if(!setHttpURLConnectionFields(httpURLConnection, httpMethod, doInput, doOutput, useCaches, keepAlive, contentType, headerArrays)){
			return false;
		}
		try {
			if(content != null){
				httpURLConnection.setFixedLengthStreamingMode(content.length());
				DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
				dataOutputStream.write(content.getBytes(DEFAULT_CHARSET));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo("Request content:\n" + content);
				}
				dataOutputStream.flush();
				dataOutputStream.close();
			}
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set content failed, exception " + e + ", " + httpMethod + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static boolean setHttpURLConnectionFieldsWithContentStringUseFixedStreaming(HttpURLConnection httpURLConnection, String httpMethod, String contentType
			, String[][] headerArrays, String content){
		if((TextUtils.isEmpty(httpMethod) || HTTP_METHOD_GET.equals(httpMethod)) && content != null){
			content = null;
		}
		boolean doOutput = !TextUtils.isEmpty(content);
		return setHttpURLConnectionFieldsWithContentStringUseFixedStreaming(httpURLConnection, httpMethod, true, doOutput, false, true
				, contentType, headerArrays, content);
	}

	public static boolean setHttpURLConnectionFieldsWithContentStringUseChunkStreaming(HttpURLConnection httpURLConnection, int chunkLength, String httpMethod
			, boolean doInput, boolean doOutput, boolean useCaches, boolean keepAlive, String contentType, String[][] headerArrays, String content){
		if(!setHttpURLConnectionFields(httpURLConnection, httpMethod, doInput, doOutput, useCaches, keepAlive, contentType, headerArrays)){
			return false;
		}
		try {
			if(content != null){
				httpURLConnection.setChunkedStreamingMode(chunkLength);
				DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
				dataOutputStream.write(content.getBytes(DEFAULT_CHARSET));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo("Request content:\n" + content);
				}
				dataOutputStream.flush();
				dataOutputStream.close();
			}
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set content failed, exception " + e + ", " + httpMethod + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static boolean setHttpURLConnectionFieldsWithContentStringUseChunkStreaming(HttpURLConnection httpURLConnection, int chunkLength, String httpMethod
			, String contentType, String[][] headerArrays, String content){
		if((TextUtils.isEmpty(httpMethod) || HTTP_METHOD_GET.equals(httpMethod)) && content != null){
			content = null;
		}
		boolean doOutput = !TextUtils.isEmpty(content);
		return setHttpURLConnectionFieldsWithContentStringUseChunkStreaming(httpURLConnection, chunkLength, httpMethod, true, doOutput, false
				, true, contentType, headerArrays, content);
	}

	public static boolean setHttpURLConnectionFields(HttpURLConnection httpURLConnection, String httpMethod, boolean doInput, boolean doOutput, boolean useCaches
			, boolean keepAlive, String contentType, String[][] headerArrays){
		try {
			// System.setProperty() for APP
			System.setProperty("http.keepAlive", "true");
			try {
				if(TextUtils.isEmpty(httpMethod)){
					httpMethod = HTTP_METHOD_GET;
				}
				httpURLConnection.setRequestMethod(httpMethod);
			} catch (ProtocolException e1) {
				try {
					expandHttpURLConnectionMethodsByReflection(httpMethod);
					httpURLConnection.setRequestMethod(httpMethod);
				} catch (Exception e2) {
					setHttpURLConnectionRequestMethodByReflection(httpURLConnection, httpMethod);
				}
			}
			httpURLConnection.setDoInput(doInput);
			httpURLConnection.setDoOutput(doOutput);
			httpURLConnection.setUseCaches(useCaches);
			httpURLConnection.setConnectTimeout(NETWORKSETTING.mConnectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.mReadTimeout);
			httpURLConnection.setRequestProperty("Charset", DEFAULT_CHARSET.name());
			httpURLConnection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET.name());
			if(!useCaches){
				httpURLConnection.addRequestProperty("Cache-Control", "no-cache");
			}
			/*
			 * HTTP/1.0 預設不保持連線，Header set key: "Connection" value: "Keep-Alive" 表示保持連線
			 * HTTP/1.1 預設保持連線，Header set key: "Connection" value: "Close" 表示不保持連線
			 */
			httpURLConnection.setRequestProperty("Connection", keepAlive ? "Keep-Alive" : "Close");
			if(!TextUtils.isEmpty(contentType) && !contentType.contains("charset")){
				contentType = contentType + "; charset=" + DEFAULT_CHARSET.name();
			}
			httpURLConnection.setRequestProperty("Content-Type", contentType);
			if(headerArrays != null && headerArrays.length > 0){
				for(int i=0; i<headerArrays.length; i++){
					if(headerArrays[i].length < 2 || headerArrays[i][0] == null || headerArrays[i][1] == null){
						continue;
					}
					httpURLConnection.setRequestProperty(headerArrays[i][0], headerArrays[i][1]);
				}
			}
			if(NETWORKSETTING.mIsPrintConnectionRequest){
				printMap(httpURLConnection.getRequestProperties(), "Request");
			}
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set fields failed, exception " + e + ", " + httpMethod + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static void setHttpURLConnectionFields(HttpURLConnection httpURLConnection, String httpMethod, String contentType, String[][] headerArrays) {
		boolean doOutput = !httpMethod.equals(HTTP_METHOD_GET);
		setHttpURLConnectionFields(httpURLConnection, httpMethod, true, doOutput, false, true, contentType, headerArrays);
	}

	@SuppressWarnings("JavaReflectionMemberAccess")
	public static void expandHttpURLConnectionMethodsByReflection(String httpMethod) throws ReflectiveOperationException {
		// Reflection反射調用屬性
		Field fieldModifiers = Field.class.getDeclaredField("modifiers");
		Field fieldStaticMethods = HttpURLConnection.class.getDeclaredField("methods");
		fieldModifiers.setAccessible(true);
		// remove final modifiers flag
		fieldModifiers.setInt(fieldStaticMethods, fieldModifiers.getInt(fieldModifiers) & ~Modifier.FINAL);
		fieldModifiers.setAccessible(false);

		fieldStaticMethods.setAccessible(true);
		String[] methods = (String[]) fieldStaticMethods.get(null);// static field
		if(methods != null){
			Set<String> setMethods = new LinkedHashSet<>(Arrays.asList(methods));
			setMethods.add(httpMethod);
			methods = setMethods.toArray(new String[0]);
			fieldStaticMethods.set(null, methods);
		}
		fieldStaticMethods.setAccessible(false);
	}

	public static void setHttpURLConnectionRequestMethodByReflection(HttpURLConnection httpURLConnection, String httpMethod) throws ReflectiveOperationException {
		Object object;
		String className = httpURLConnection.getClass().getName();
		if((className.contains("$") && className.substring(className.lastIndexOf("$")).equals("HttpsURLConnectionImpl"))
				|| className.substring(className.lastIndexOf(".")).equals("HttpsURLConnectionImpl")){
			// Reflection反射調用屬性
			Field field = httpURLConnection.getClass().getDeclaredField("delegate");
			field.setAccessible(true);
			object = field.get(httpURLConnection);
			field.setAccessible(false);
		}else{
			object = httpURLConnection;
		}
		if(object == null){
			return;
		}

		try {
			// Reflection反射調用方法
			Method method = object.getClass().getDeclaredMethod("setRequestMethod", java.lang.String.class);
			method.setAccessible(true);
			method.invoke(object, httpMethod);
			method.setAccessible(false);
		} catch (Exception e) {
			Field field = object.getClass().getDeclaredField("method");
			field.setAccessible(true);
			field.set(object, httpMethod);
			field.setAccessible(false);
		}
	}

	public static boolean setHttpURLConnectionContentForUrlEncodedUseFixedStreaming(HttpURLConnection httpURLConnection, Object[][] contentArrays){
		if(contentArrays == null || contentArrays.length == 0){
			return false;
		}
		try {
			Object[] contentArray;
			String pairs = null;
			for(int i=0; i<contentArrays.length; i++){
				contentArray = contentArrays[i];
				if(contentArray == null || contentArray.length == 0 || contentArray[0] == null){
					continue;
				}
				pairs = TextUtils.isEmpty(pairs) ? "" : pairs + "&";
				pairs = pairs + contentArray[0] + "=";
				if(contentArray.length > 1){
					pairs = pairs + contentArray[contentArray.length - 1];
				}
			}
			if(pairs != null){
				httpURLConnection.setFixedLengthStreamingMode(pairs.length());
				DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
				dataOutputStream.write(pairs.getBytes(DEFAULT_CHARSET));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo("Request content:\n" + pairs);
				}
				dataOutputStream.flush();
				dataOutputStream.close();
			}
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set content failed, exception " + e + ", " + httpURLConnection.getRequestMethod() + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static boolean setHttpURLConnectionContentForUrlEncodedUseChunkStreaming(HttpURLConnection httpURLConnection, int chunkLength, Object[][] contentArrays){
		if(contentArrays == null || contentArrays.length == 0){
			return false;
		}
		try {
			Object[] contentArray;
			httpURLConnection.setChunkedStreamingMode(chunkLength);
			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			String pairs;
			boolean isFirstPair = true;
			if(NETWORKSETTING.mIsPrintConnectionRequest){
				printInfo("Request content:");
			}
			for(int i=0; i<contentArrays.length; i++){
				contentArray = contentArrays[i];
				if(contentArray == null || contentArray.length == 0 || contentArray[0] == null){
					continue;
				}
				if(isFirstPair){
					isFirstPair = false;
					pairs = contentArray[0] + "=";
				}else{
					pairs = "&" + contentArray[0] + "=";
				}
				if(contentArray.length > 1){
					pairs = pairs + contentArray[contentArray.length - 1];
				}
				dataOutputStream.write(pairs.getBytes(DEFAULT_CHARSET));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo(pairs);
				}
			}
			dataOutputStream.flush();
			dataOutputStream.close();
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set content failed, exception " + e + ", " + httpURLConnection.getRequestMethod() + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static boolean setHttpURLConnectionContentForMultipartFormDataUseChunkStreaming(HttpURLConnection httpURLConnection, int chunkLength, String hyphens
			, String boundary, String breakLine, Object[][] contentArrays){
		if(contentArrays == null || contentArrays.length == 0){
			return false;
		}
		try {
			Object[] contentArray;
			Charset charset = DEFAULT_CHARSET;
			String line;
			httpURLConnection.setChunkedStreamingMode(chunkLength);
			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			if(NETWORKSETTING.mIsPrintConnectionRequest){
				printInfo("Request content:");
			}
			for(int i=0; i<contentArrays.length; i++){
				contentArray = contentArrays[i];
				if(contentArray.length == 2 && contentArray[0] == null){
					continue;
				}
				if(contentArray.length == 3 && contentArray[0] == null){
					continue;
				}
				if(contentArray.length == 4 && contentArray[0] == null){
					continue;
				}
				line = hyphens + boundary +
						breakLine;
				dataOutputStream.write(line.getBytes(charset));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo(line);
				}

				if(contentArray.length == 2){
					line = "Content-Disposition: form-data; name=\"" + contentArray[0] + "\""
							+ breakLine + breakLine;
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}
				}else if (contentArray.length == 3){
					line = "Content-Disposition: form-data; name=\"" + contentArray[0] + "\"; filename=\"" + contentArray[1] + "\""
							+ breakLine
							+ "Content-Type: application/x-object"
							+ breakLine + breakLine;
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}
				}else if (contentArray.length == 4){
					line = "Content-Disposition: form-data; name=\"" + contentArray[0] + "\"; filename=\"" + contentArray[1] + "\""
							+ breakLine
							+ "Content-Type: " + contentArray[2]
							+ breakLine + breakLine;
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}
				}
				if(contentArray[contentArray.length - 1] == null){
					line = "null";
					dataOutputStream.write(line.getBytes(charset));
				}else{
					if(contentArray[contentArray.length - 1] instanceof String){
						line = (String) contentArray[contentArray.length - 1];
						dataOutputStream.write(line.getBytes(charset));
					}else if(contentArray[contentArray.length - 1] instanceof InputStream){
						InputStream inputStream = (InputStream) contentArray[contentArray.length - 1];
						int progress;
						byte[] buffer = new byte[NETWORKSETTING.mBufferSize];
						while ((progress = inputStream.read(buffer)) != -1) {
							dataOutputStream.write(buffer, 0, progress);
						}
						dataOutputStream.flush();
						inputStream.close();
						line = "write stream";
					}else if(contentArray[contentArray.length - 1] instanceof byte[]){
						dataOutputStream.write((byte[]) contentArray[contentArray.length - 1]);
						dataOutputStream.flush();
						line = "write bytes, length " + ((byte[]) contentArray[contentArray.length - 1]).length;
					}
				}
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo(line);
				}

				dataOutputStream.writeBytes(breakLine);
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo(breakLine);
				}
			}
			line = hyphens + boundary + hyphens;
			dataOutputStream.write(line.getBytes(charset));
			if(NETWORKSETTING.mIsPrintConnectionRequest){
				printInfo(line);
			}
			dataOutputStream.flush();
			dataOutputStream.close();
			return true;
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection set content failed, exception " + e + ", " + httpURLConnection.getRequestMethod() + ", " + httpURLConnection.getURL().toString());
			}
		}
		return false;
	}

	public static Object[][] filterNotNullContentArrays(Object[][] contentArrays){
		Object[] contentArray;
		List<Object[]> list = new ArrayList<>();
		for(int i=0; i<contentArrays.length; i++){
			contentArray = contentArrays[i];
			if(contentArray == null || contentArray.length == 0 || contentArray[0] == null || contentArray[contentArray.length - 1] == null){
				continue;
			}
			list.add(contentArray);
		}
		if(list.size() == contentArrays.length){
			return contentArrays;
		}
		return list.toArray(new Object[0][0]);
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static ConnectionResult connectUseHttpURLConnectionOutputBytes(@NonNull Context context, HttpURLConnection httpURLConnection){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() != CONNECTION_NO_NETWORK){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_BYTES, null, null, connectionResult);
		}
		return connectionResult;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static ConnectionResult connectUseHttpURLConnectionOutputString(@NonNull Context context, HttpURLConnection httpURLConnection){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() != CONNECTION_NO_NETWORK){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_STRING, null, null, connectionResult);
		}
		return connectionResult;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static ConnectionResult connectUseHttpURLConnectionOutputString(@NonNull Context context, HttpURLConnection httpURLConnection, Charset charset){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() != CONNECTION_NO_NETWORK){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_STRING, charset, null, connectionResult);
		}
		return connectionResult;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static ConnectionResult connectUseHttpURLConnectionOutputFile(@NonNull Context context, HttpURLConnection httpURLConnection, File fileOutput){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() != CONNECTION_NO_NETWORK){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_FILE, null, fileOutput, connectionResult);
		}
		return connectionResult;
	}

	/**
	 * 僅完成連線，不自動下載回傳內容，成功後{@link ConnectionResult#getStatusCode()}的狀態為{@link #CONNECTION_CONNECTED}
	 * 完成連線後須調用{@link HttpURLConnection#disconnect()}斷開連線
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static ConnectionResult connectUseHttpURLConnectionOutputSkip(@NonNull Context context, HttpURLConnection httpURLConnection){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() != CONNECTION_NO_NETWORK){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_SKIP, null, null, connectionResult);
		}
		return connectionResult;
	}

	private static void connectUseHttpURLConnection(HttpURLConnection httpURLConnection, int outputType, Charset charset, File fileOutput
			, ConnectionResult connectionResult){
		if(httpURLConnection == null){
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
				connectionResult.setStatusMessage("Connection open failed");
			}
			return;
		}

		String strUrl = httpURLConnection.getURL().toString();
		if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl){
			printInfo("Connection connect start, " + httpURLConnection.getRequestMethod() + ", " + strUrl);
		}
		try {
			httpURLConnection.connect();
			if(charset == null){
				charset = getCharsetInContentType(httpURLConnection.getContentType());
				if(charset == null && outputType == OUTPUT_TYPE_STRING){
					charset = DEFAULT_CHARSET;
				}
			}
			if(connectionResult != null){
				connectionResult.setHttpURLConnection(httpURLConnection);
				connectionResult.setContentCharset(charset);
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printMap(httpURLConnection.getHeaderFields(), "Response");
			}

			InputStream inputStreamError = httpURLConnection.getErrorStream();
			if(inputStreamError != null){
				String errorMessage = inputStreamToString(inputStreamError, charset, NETWORKSETTING.mBufferSize, connectionResult);
				if(connectionResult != null){
					connectionResult.setErrorMessage(errorMessage);
				}
			}

			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
					printInfo("Connection connect OK, ResponseCode " + httpURLConnection.getResponseCode() + ", " + httpURLConnection.getRequestMethod()
							+ ", " + strUrl);
				}
			}else if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
					printInfo("Connection connect partial, ResponseCode " + httpURLConnection.getResponseCode() + ", " + httpURLConnection.getRequestMethod()
							+ ", " + strUrl);
				}
			}else{
				if(connectionResult != null){
					connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
					connectionResult.setStatusMessage("Connection connect failed, ResponseCode " + httpURLConnection.getResponseCode());
				}
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
					printInfo("Connection connect failed, ResponseCode " + httpURLConnection.getResponseCode() + ", " + httpURLConnection.getRequestMethod()
							+ ", " + strUrl);
				}
				httpURLConnection.disconnect();
				return;
			}

			if(connectionResult != null){
				try {
					connectionResult.setContentLength(Long.parseLong(httpURLConnection.getHeaderField("content-length")));
				} catch (Exception ignored) {}
			}
		} catch (Exception e) {
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
				connectionResult.setStatusMessage("Connection connect failed, exception " + e);
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection connect failed, exception " + e + ", " + httpURLConnection.getRequestMethod() + ", " + strUrl);
			}
			return;
		}
		if(outputType == OUTPUT_TYPE_SKIP){
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECTED);
				connectionResult.setStatusMessage("Connection connected");
			}
			return;
		}
		if(connectionResult == null){
			httpURLConnection.disconnect();
			return;
		}

		try {
			if(outputType == OUTPUT_TYPE_BYTES) {
				connectionResult.setContentBytes(inputStreamToByteArray(httpURLConnection.getInputStream(), NETWORKSETTING.mBufferSize, connectionResult));
			}else if(outputType == OUTPUT_TYPE_STRING){
				connectionResult.setContentString(inputStreamToString(httpURLConnection.getInputStream(), charset, NETWORKSETTING.mBufferSize
						, connectionResult));
			}else if(outputType == OUTPUT_TYPE_FILE && fileOutput != null){
				if(!inputStreamWriteOutputStream(httpURLConnection.getInputStream(), new FileOutputStream(fileOutput), NETWORKSETTING.mBufferSize
						, connectionResult)){
					if(fileOutput.delete()){
						System.out.println("not delete file " + fileOutput.getPath());
					}
				}
				connectionResult.setContentOutputFile(fileOutput);
			}
			if(connectionResult.getStatusCode() != CONNECTION_LOAD_FAIL){
				connectionResult.setStatusCode(CONNECTION_LOADED);
				connectionResult.setStatusMessage("Connection loaded");
			}
		} catch (Exception e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
		}
		if(connectionResult.getStatusCode() == CONNECTION_LOAD_FAIL && NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
			printInfo(connectionResult.getStatusMessage() + ", " + httpURLConnection.getRequestMethod() + ", " + strUrl);
		}
		httpURLConnection.disconnect();
	}

	public static Charset getCharsetInContentType(String contentType){
		if(contentType != null && contentType.trim().length() > 0){
			String[] values = contentType.split(";");
			// 取得網頁文字編碼
			for(String value : values){
				value = value.trim();
				if(value.toLowerCase().startsWith("charset=")){
					return Charset.forName(value.substring("charset=".length()));
				}
			}
		}
		return null;
	}

	private static void printMap(Map<String, List<String>> map, String prefix){
		if(map == null || map.size() == 0){
			return;
		}
		Iterator<Map.Entry<String, List<String>>> iteratorEntry = map.entrySet().iterator();
		Map.Entry<String, List<String>> entry;
		List<String> list;
		Iterator<String> iterator;
		while(iteratorEntry.hasNext()){
			entry = iteratorEntry.next();
			list = entry.getValue();
			iterator = list.iterator();
			while(iterator.hasNext()){
				printInfo(prefix + " key: " + entry.getKey() + " value: " + iterator.next());
			}
		}
	}

	@SuppressWarnings("UnusedAssignment")
	private static byte[] inputStreamToByteArray(InputStream inputStream, int bufferSize, ConnectionResult connectionResult){
		if(inputStream == null){
			return null;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		ByteArrayOutputStream byteArrayOutputStream;
		byte[] buffer, byteArray = null;
		try {
			buffer = new byte[bufferSize];
			byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				int progress;
				while((progress = inputStream.read(buffer)) != -1){
					byteArrayOutputStream.write(buffer, 0, progress);
				}
				byteArrayOutputStream.flush();
				byteArray = byteArrayOutputStream.toByteArray();
			} finally {
				byteArrayOutputStream.close();
				inputStream.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read bytes, length " + byteArray.length);
			}
		} catch (IOException e) {
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		} catch (OutOfMemoryError e) {
			buffer = null;
			byteArrayOutputStream = null;
			byteArray = null;
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
				connectionResult.setStatusMessage("Connection loading failed, error " + e);
			}
		}
		return byteArray;
	}

	@SuppressWarnings("UnusedAssignment")
	public static String inputStreamToString(InputStream inputStream, Charset charset, int bufferSize, ConnectionResult connectionResult){
		if(inputStream == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.println("charset get failed, using default charset " + charset.displayName());
		}
//		System.out.println("charset = " + charset.displayName());
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		BufferedReader bufferedReader;
		String line;
		StringBuilder stringBuilder = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset), bufferSize);
			stringBuilder = new StringBuilder();// StringBuffer is thread safe, StringBuilder is faster but not thread safe.
			try {
				while((line = bufferedReader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				bufferedReader.close();
				inputStream.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read string, length " + stringBuilder.length());
				printInfo(stringBuilder.toString());
			}
		} catch (IOException e) {
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		} catch (OutOfMemoryError e) {
			stringBuilder = null;
			line = null;
			bufferedReader = null;
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
				connectionResult.setStatusMessage("Connection loading failed, error " + e);
			}
		}
		return stringBuilder == null ? null : stringBuilder.toString();
	}

	@SuppressWarnings("UnusedAssignment")
	private static boolean inputStreamWriteOutputStream(InputStream inputStream, OutputStream outputStream, int bufferSize, ConnectionResult connectionResult){
		if(inputStream == null || outputStream == null){
			return false;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		try {
			buffer = new byte[bufferSize];
			try {
				int progress;
				while((progress = inputStream.read(buffer)) != -1){
					outputStream.write(buffer, 0, progress);
				}
				outputStream.flush();
			} finally {
				outputStream.close();
				inputStream.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read stream and output");
			}
			return true;
		} catch (IOException e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
		} catch (OutOfMemoryError e) {
			buffer = null;
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, error " + e);
		}
		return false;
	}

	public static class ConnectionResult {

		private long mContentLength = -1;
		private Charset mContentCharset;
		private byte[] mContentBytes;
		private String mContentString;
		private File mContentOutputFile;
		private int mStatusCode;
		private String mStatusMessage;
		private String mErrorMessage;
		private HttpURLConnection mHttpURLConnection;

		public void setContentLength(long contentLength){
			mContentLength = contentLength;
		}

		public void setContentCharset(Charset contentCharset){
			mContentCharset = contentCharset;
		}

		public void setContentBytes(byte[] bytes){
			mContentBytes = bytes;
		}

		public void setContentString(String string){
			mContentString = string;
		}

		private void setContentOutputFile(File fileOutput){
			mContentOutputFile = fileOutput;
		}

		public void setStatusCode(int statusCode){
			mStatusCode = statusCode;
		}

		public void setStatusMessage(String statusMessage){
			mStatusMessage = statusMessage;
		}

		public void setErrorMessage(String errorMessage){
			mErrorMessage = errorMessage;
		}

		private void setHttpURLConnection(HttpURLConnection httpURLConnection){
			mHttpURLConnection = httpURLConnection;
		}

		public long getContentLength(){
			return mContentLength;
		}

		public Charset getContentCharset(){
			return mContentCharset;
		}

		public byte[] getContentBytes(){
			return mContentBytes;
		}

		public String getContentString(){
			if(mContentString == null){
				if(mContentBytes == null){
					return null;
				}
				return new String(mContentBytes, mContentCharset == null ? DEFAULT_CHARSET : mContentCharset);
			}
			return mContentString;
		}

		public File getContentOutputFile(){
			return mContentOutputFile;
		}

		public int getStatusCode(){
			return mStatusCode;
		}

		public String getStatusMessage(){
			return mStatusMessage;
		}

		public String getErrorMessage(){
			return mErrorMessage;
		}

		public HttpURLConnection getHttpURLConnection(){
			return mHttpURLConnection;
		}
	}
}