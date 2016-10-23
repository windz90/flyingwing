/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.5.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "WeakerAccess", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "ThrowFromFinallyBlock"})
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
	private static final String HTTP_METHOD_GET = "GET";
	private static final String HTTP_METHOD_POST = "POST";
	private static final String HTTP_METHOD_HEAD = "HEAD";
	private static final String HTTP_METHOD_PUT = "PUT";
	private static final String HTTP_METHOD_DELETE = "DELETE";
	private static final String HTTP_METHOD_TRACE = "TRACE";
	private static final String HTTP_METHOD_OPTIONS = "OPTIONS";
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

	public static ConnectionResult connectUseHttpURLConnectionOutputBytes(Context context, HttpURLConnection httpURLConnection, Handler handler){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() == 0){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_BYTES, null, null, connectionResult, handler);
		}
		return connectionResult;
	}

	public static ConnectionResult connectUseHttpURLConnectionOutputString(Context context, HttpURLConnection httpURLConnection, Handler handler){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() == 0){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_STRING, null, null, connectionResult, handler);
		}
		return connectionResult;
	}

	public static ConnectionResult connectUseHttpURLConnectionOutputString(Context context, HttpURLConnection httpURLConnection, Charset charset, Handler handler){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() == 0){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_STRING, charset, null, connectionResult, handler);
		}
		return connectionResult;
	}

	public static ConnectionResult connectUseHttpURLConnectionOutputFile(Context context, HttpURLConnection httpURLConnection, File fileOutput, Handler handler){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() == 0){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_FILE, null, fileOutput, connectionResult, handler);
		}
		return connectionResult;
	}

	/**
	 * 完成連線後須調用{@link HttpURLConnection#disconnect()}斷開連線
	 */
	public static ConnectionResult connectUseHttpURLConnectionOutputSkip(Context context, HttpURLConnection httpURLConnection, Handler handler){
		ConnectionResult connectionResult = getNetworkCheckConnectResult(context);
		if(connectionResult.getStatusCode() == 0){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_SKIP, null, null, connectionResult, handler);
		}
		return connectionResult;
	}

	/**
	 * 完成連線後須調用{@link HttpURLConnection#disconnect()}斷開連線
	 */
	public static void connectControlHttpURLConnection(Context context, HttpURLConnection httpURLConnection, Handler handler){
		if(isAvailable(context)){
			connectUseHttpURLConnection(httpURLConnection, OUTPUT_TYPE_SKIP, null, null, null, handler);
		}
	}

	private static void connectUseHttpURLConnection(HttpURLConnection httpURLConnection, int outputType, Charset charset, File fileOutput
			, ConnectionResult connectionResult, Handler handler){
		if(httpURLConnection == null){
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
				connectionResult.setStatusMessage("Connection open failed");
			}
			return;
		}

		try {
			httpURLConnection.connect();
			if(connectionResult != null){
				connectionResult.setHttpURLConnection(httpURLConnection);
			}

			InputStream inputStreamError = httpURLConnection.getErrorStream();
			if(inputStreamError != null){
				if(connectionResult != null){
					connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
					connectionResult.setStatusMessage(new String(inputStreamToByteArray(inputStreamError, NETWORKSETTING.mBufferSize, connectionResult)
							, Charset.forName("UTF-8")));
				}
				if(handler != null){
					Message msg = Message.obtain(handler);
					msg.what = CONNECTION_CONNECT_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
				return;
			}

			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
					printInfo("Connection connect OK, StatusCode " + httpURLConnection.getResponseCode());
				}
			}else if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
					printInfo("Connection connecting, StatusCode " + httpURLConnection.getResponseCode());
				}
			}else{
				if(connectionResult != null){
					connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
					connectionResult.setStatusMessage("Connection connect failed, StatusCode " + httpURLConnection.getResponseCode());
				}
				if(handler != null){
					Message msg = Message.obtain(handler);
					msg.what = CONNECTION_CONNECT_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
				if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
					printInfo("Connection connect failed, StatusCode " + httpURLConnection.getResponseCode());
				}
				return;
			}

			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECTED);
				connectionResult.setStatusMessage("Connection connected");
				try {
					connectionResult.setContentLength(Long.parseLong(httpURLConnection.getHeaderField("content-length")));
				} catch (Exception ignored) {}
				connectionResult.setContentCharset(getCharset(httpURLConnection.getContentType()));
			}
			if(handler != null){
				Message msg = Message.obtain(handler);
				msg.what = CONNECTION_CONNECTED;
				msg.obj = connectionResult;
				handler.sendMessage(msg);
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printMap(httpURLConnection.getHeaderFields(), "Response");
			}
		} catch (Exception e) {
			if(connectionResult != null){
				connectionResult.setStatusCode(CONNECTION_CONNECT_FAIL);
				connectionResult.setStatusMessage("Connection connect failed, exception " + e);
			}
			if(handler != null){
				Message msg = Message.obtain(handler);
				msg.what = CONNECTION_CONNECT_FAIL;
				msg.obj = connectionResult;
				handler.sendMessage(msg);
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection connect failed, exception " + e);
			}
			return;
		}
		if(outputType == OUTPUT_TYPE_SKIP){
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
				connectionResult.setContentString(inputStreamToString(httpURLConnection.getInputStream()
						, charset == null ? connectionResult.getContentCharset() : charset, NETWORKSETTING.mBufferSize, connectionResult));
			}else if(outputType == OUTPUT_TYPE_FILE && fileOutput != null){
				inputStreamWriteOutputStream(httpURLConnection.getInputStream(), new FileOutputStream(fileOutput), NETWORKSETTING.mBufferSize, connectionResult);
				connectionResult.setContentOutputFile(fileOutput);
			}
			if(connectionResult.getStatusCode() == CONNECTION_LOAD_FAIL){
				if(handler != null){
					Message msg = Message.obtain(handler);
					msg.what = CONNECTION_LOAD_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
			}else{
				connectionResult.setStatusCode(CONNECTION_LOADED);
				connectionResult.setStatusMessage("Connection loaded");
				if(handler != null){
					Message msg = Message.obtain(handler);
					msg.what = CONNECTION_LOADED;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			if(handler != null){
				Message msg = Message.obtain(handler);
				msg.what = CONNECTION_LOAD_FAIL;
				msg.obj = connectionResult;
				handler.sendMessage(msg);
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection loading failed, exception " + e);
			}
		}
		httpURLConnection.disconnect();
	}

	public static boolean isAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	private static ConnectionResult getNetworkCheckConnectResult(Context context){
		ConnectionResult connectionResult = new ConnectionResult();
		if(!isAvailable(context)){
			connectionResult.setStatusCode(CONNECTION_NO_NETWORK);
			connectionResult.setStatusMessage("Connection check failed, no network connection");
		}
		return connectionResult;
	}

	public static HttpURLConnection openHttpURLConnectionWithHttps(String httpMethod, String strUrl, String[][] headerArrays, Object[][] contentArrays
			, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		HttpURLConnection httpURLConnection = null;
		try {
			if(TextUtils.isEmpty(httpMethod)){
				httpMethod = HTTP_METHOD_GET;
			}
			if(HTTP_METHOD_GET.equals(httpMethod) && contentArrays != null){
				boolean isContains = strUrl.contains("?");
				for(int i=0; i<contentArrays.length; i++){
					strUrl = strUrl + (i == 0 && !isContains ? "?" : "&") + contentArrays[i][0] + "=" + contentArrays[i][1];
				}
				contentArrays = null;
			}

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

			if(!prepareHttpURLConnection(httpURLConnection, httpMethod, headerArrays, contentArrays)){
				httpURLConnection = null;
			}
		} catch (Exception e) {
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection open failed, exception " + e);
			}
		}finally{
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl){
				printInfo(httpMethod + ", " + strUrl);
			}
		}
		return httpURLConnection;
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsGet(String strUrl, String[][] headerArrays, Object[][] contentArrays
			, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_GET, strUrl, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsPost(String strUrl, String[][] headerArrays, Object[][] contentArrays
			, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_POST, strUrl, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsHead(String strUrl, String[][] headerArrays, Object[][] contentArrays
			, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_HEAD, strUrl, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttps(String httpMethod, String strUrl, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttps(httpMethod, strUrl, headerArrays, contentArrays, null, null);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsGet(String strUrl, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_GET, strUrl, headerArrays, contentArrays, null, null);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsPost(String strUrl, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_POST, strUrl, headerArrays, contentArrays, null, null);
	}

	public static HttpURLConnection openHttpURLConnectionWithHttpsHead(String strUrl, String[][] headerArrays, Object[][] contentArrays){
		return openHttpURLConnectionWithHttps(HTTP_METHOD_HEAD, strUrl, headerArrays, contentArrays, null, null);
	}

	/**
	 * @param contentList <br>
	 * String key : map.get("0");<br>
	 * String value : map.get("1");<br>
	 * String MIME Type : map.get("2");<br>
	 * InputStream stream : map.get("3");
	 */
	public static HttpURLConnection openHttpURLConnectionWithHttps(String httpMethod, String strUrl, List<String[]> headerList
			, List<Map<String, Object>> contentList, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		String[][] headerArrays = null;
		Object[][] contentArrays = null;
		int size;
		if(headerList != null && headerList.size() > 0){
			size = headerList.size();
			headerArrays = new String[headerList.size()][2];
			for(int i=0; i<size; i++){
				headerArrays[i] = headerList.get(i);
			}
		}

		if(contentList != null && contentList.size() > 0){
			size = contentList.size();
			contentArrays = new Object[contentList.size()][4];
			Map<String, Object> map;
			for(int i=0; i<size; i++){
				map = contentList.get(i);
				contentArrays[i][0] = map.get("0");
				contentArrays[i][1] = map.get("1");
				contentArrays[i][2] = map.get("2");
				contentArrays[i][3] = map.get("3");
			}
		}
		return openHttpURLConnectionWithHttps(httpMethod, strUrl, headerArrays, contentArrays, sslContext, hostnameVerifier);
	}

	/**
	 * @param contentList <br>
	 * String key : map.get("0");<br>
	 * String value : map.get("1");<br>
	 * String MIME Type : map.get("2");<br>
	 * InputStream stream : map.get("3");
	 */
	public static HttpURLConnection openHttpURLConnectionWithHttps(String httpMethod, String strUrl, List<String[]> headerList
			, List<Map<String, Object>> contentList){
		return openHttpURLConnectionWithHttps(httpMethod, strUrl, headerList, contentList, null, null);
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
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

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
		return getSSLContext(null, null, null, null, null, null);
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

	public static boolean prepareHttpURLConnection(HttpURLConnection httpURLConnection, String httpMethod, String[][] headerArrays, Object[][] contentArrays){
		String hyphens = "--";
		String boundary = "#!#!#!BOUNDARY!#!#!#";
		String breakLine = "\r\n";
		try {
			// System.setProperty() for APP
			System.setProperty("http.keepAlive", "true");
			httpURLConnection.setRequestMethod(httpMethod);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(!httpMethod.equals(HTTP_METHOD_GET));
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setConnectTimeout(NETWORKSETTING.mConnectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.mReadTimeout);
			httpURLConnection.setChunkedStreamingMode(NETWORKSETTING.mBufferSize);
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			/*
			 * HTTP/1.0 預設不保持連線，Header add Connection: Keep-Alive field 表示保持連線
			 * HTTP/1.1 預設保持連線，Header add Connection: Close field 表示不保持連線
			 */
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			if(headerArrays != null && headerArrays.length > 0){
				for(String[] headerArray : headerArrays){
					httpURLConnection.setRequestProperty(headerArray[0], headerArray[1]);
				}
			}
			if(NETWORKSETTING.mIsPrintConnectionRequest){
				printMap(httpURLConnection.getRequestProperties(), "Request");
			}

			if(contentArrays == null || contentArrays.length == 0 || contentArrays[0].length < 2){
				return true;
			}
			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			Charset charset = Charset.forName("UTF-8");
			String line;
			for(Object[] contentArray : contentArrays){
				line = hyphens + boundary +
						breakLine;
				dataOutputStream.write(line.getBytes(charset));
				if(NETWORKSETTING.mIsPrintConnectionRequest){
					printInfo(line);
				}

				if(contentArray.length == 2){
					line = "Content-Disposition: form-data; name=\"" + contentArray[0] + "\"" +
							breakLine + breakLine;
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}

					line = (String) contentArray[1];
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}
				}else if (contentArray.length == 4){
					line = "Content-Disposition: form-data; name=\"" + contentArray[0] + "\"; filename=\"" + contentArray[1] + "\"" +
							breakLine +
							"Content-Type: " + contentArray[2] +
							breakLine + breakLine;
					dataOutputStream.write(line.getBytes(charset));
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}

					if(contentArray[3] != null && contentArray[3] instanceof InputStream){
						InputStream inputStream = (InputStream) contentArray[3];
						int progress;
						byte[] buffer = new byte[NETWORKSETTING.mBufferSize];
						while ((progress = inputStream.read(buffer)) != -1) {
							dataOutputStream.write(buffer, 0, progress);
						}
						dataOutputStream.flush();
						inputStream.close();
						line = "write stream";
					}else if(contentArray[3] != null && contentArray[3] instanceof byte[]){
						dataOutputStream.write((byte[]) contentArray[3]);
						dataOutputStream.flush();
						line = "write bytes, length " + ((byte[]) contentArray[3]).length;
					}else{
						line = "";
					}
					if(NETWORKSETTING.mIsPrintConnectionRequest){
						printInfo(line);
					}
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
		} catch (Exception e) {
			httpURLConnection = null;
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				printInfo("Connection prepare failed, exception " + e);
			}
		}
		return httpURLConnection != null;
	}

	public static Charset getCharset(String contentType){
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
	private static byte[] inputStreamToByteArray(InputStream is, int bufferSize, ConnectionResult connectionResult){
		if(is == null){
			return null;
		}
		byte[] byteArray = null;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		ByteArrayOutputStream baos;
		try {
			baos = new ByteArrayOutputStream();
			try {
				int progress;
				byte[] buffer = new byte[bufferSize];
				while((progress = is.read(buffer)) != -1){
					baos.write(buffer, 0, progress);
				}
				baos.flush();
				byteArray = baos.toByteArray();
			} finally {
				baos.close();
				is.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read bytes, length " + byteArray.length);
			}
		} catch (IOException e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		} catch (OutOfMemoryError e) {
			baos = null;
			byteArray = null;
			is = null;
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			}
		}
		return byteArray;
	}

	@SuppressWarnings("UnusedAssignment")
	public static String inputStreamToString(InputStream is, Charset charset, int bufferSize, ConnectionResult connectionResult){
		if(is == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.print("charset get fail, using default, ");
		}
//		System.out.println("charset = " + charset.displayName());
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		BufferedReader reader;
		StringBuilder stringBuilder = new StringBuilder();// StringBuilder速度較快但不支援多執行緒同步
		try {
			reader = new BufferedReader(new InputStreamReader(is, charset), bufferSize);
			try {
				String line;
				while((line = reader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				reader.close();
				is.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read string, length " + stringBuilder.length());
				printInfo(stringBuilder.toString());
			}
		} catch (IOException e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		} catch (OutOfMemoryError e) {
			stringBuilder = null;
			reader = null;
			is = null;
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			}
		}
		return stringBuilder == null ? null : stringBuilder.toString();
	}

	@SuppressWarnings("UnusedAssignment")
	private static boolean inputStreamWriteOutputStream(InputStream is, OutputStream os, int bufferSize, ConnectionResult connectionResult){
		if(is == null || os == null){
			return false;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		try {
			try {
				int progress;
				byte[] buffer = new byte[bufferSize];
				while((progress = is.read(buffer)) != -1){
					os.write(buffer, 0, progress);
				}
				os.flush();
			} finally {
				os.close();
				is.close();
			}
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectionResponse){
				printInfo("read stream and output");
			}
			return true;
		} catch (IOException e) {
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		} catch (OutOfMemoryError e) {
			os = null;
			is = null;
			connectionResult.setStatusCode(CONNECTION_LOAD_FAIL);
			connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			if(NetworkAccess.NETWORKSETTING.mIsPrintConnectException){
				connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			}
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
				return new String(mContentBytes, mContentCharset == null ? Charset.forName("UTF-8") : mContentCharset);
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

		public HttpURLConnection getHttpURLConnection(){
			return mHttpURLConnection;
		}
	}
}