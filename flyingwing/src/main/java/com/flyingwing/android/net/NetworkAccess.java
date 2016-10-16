/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.5.0
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.List;
import java.util.Locale;
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

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "UnusedAssignment", "WeakerAccess", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "ThrowFromFinallyBlock"})
public class NetworkAccess {

	public static final int CONNECTION_CONNECT_FAIL = 100;
	public static final int CONNECTION_CONNECTED = 101;
	public static final int CONNECTION_LOAD_FAIL = 102;
	public static final int CONNECTION_LOADED = 103;
	public static final int SPLIT_AUTO_MAX_QUANTITY = 0;
	public static final int SPLIT_BY_QUANTITY = 1;
	public static final int SPLIT_BY_LENGTH = 2;
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

	private static void printInfo(String info, boolean isPrint){
		if(isPrint){
			System.out.println(info);
		}
	}

	/**
	 * @param isSkipDataRead 若為true，完成連線後須調用{@link ConnectionResult#disconnect()}斷開連線
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, HttpURLConnection httpURLConnection, boolean isSkipDataRead, Handler handler){
		ConnectionResult connectionResult = getInitConnectResult();
		if(isAvailable(context)){
			connectUseHttpURLConnection(httpURLConnection, isSkipDataRead, connectionResult, handler);
		}
		return connectionResult;
	}

	public static ConnectionResult connectUseHttpURLConnection(Context context, HttpURLConnection httpURLConnection, Handler handler){
		ConnectionResult connectionResult = getInitConnectResult();
		if(isAvailable(context)){
			connectUseHttpURLConnection(httpURLConnection, false, connectionResult, handler);
		}
		return connectionResult;
	}

	/**
	 * 完成連線後須調用{@link HttpURLConnection#disconnect()}斷開連線
	 */
	public static void connectControlHttpURLConnection(Context context, HttpURLConnection httpURLConnection, Handler handler){
		if(isAvailable(context)){
			connectUseHttpURLConnection(httpURLConnection, true, null, handler);
		}
	}

	private static void connectUseHttpURLConnection(HttpURLConnection httpURLConnection, boolean isSkipDataRead, ConnectionResult connectionResult
			, Handler handler){
		if(httpURLConnection == null){
			if(connectionResult != null){
				connectionResult.setStatusMessage("Connection open failed");
			}
			return;
		}

		try {
			httpURLConnection.connect();
			if(connectionResult != null){
				connectionResult.setConnectUrl(httpURLConnection.getURL().getPath());
				connectionResult.setRequestType(httpURLConnection.getRequestMethod());
			}

			InputStream inputStreamError = httpURLConnection.getErrorStream();
			if(inputStreamError != null){
				if(connectionResult != null){
					connectionResult.setStatusMessage(inputStreamToString(inputStreamError, null, NETWORKSETTING.mBufferSize));
				}
				if(handler != null){
					Message msg = new Message();
					msg.what = CONNECTION_CONNECT_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
				return;
			}

			if(connectionResult != null){
				connectionResult.setResponseCode(httpURLConnection.getResponseCode());
				connectionResult.setResponseMessage(httpURLConnection.getResponseMessage());
			}
			if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK &&
					httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
				printInfo("Connection connect failed, StatusCode " + httpURLConnection.getResponseCode()
						, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
				if(connectionResult != null){
					connectionResult.setStatusMessage("Connection connect failed, StatusCode " + httpURLConnection.getResponseCode());
				}
				if(handler != null){
					Message msg = new Message();
					msg.what = CONNECTION_CONNECT_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
				return;
			}

			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
				printInfo("Connection connect OK, StatusCode " + httpURLConnection.getResponseCode(), false);
			}else if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
				printInfo("Connection connecting, StatusCode " + httpURLConnection.getResponseCode(), false);
			}

			if(connectionResult != null){
				connectionResult.setStatusMessage("Connect success");
				connectionResult.setContentEncoding(httpURLConnection.getContentEncoding());
				connectionResult.setContentLength(httpURLConnection.getContentLength());
				connectionResult.setContentType(httpURLConnection.getContentType());
			}
		} catch (Exception e) {
			printInfo("Connection connect failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
			if(connectionResult != null){
				connectionResult.setStatusMessage("Connection connect failed, exception " + e);
			}
		}

		if(handler != null){
			Message msg = new Message();
			msg.what = CONNECTION_CONNECTED;
			msg.obj = connectionResult;
			handler.sendMessage(msg);
		}

		if(connectionResult != null){
			try {
				if(isSkipDataRead){
					connectionResult.setHttpURLConnection(httpURLConnection);
					connectionResult.setContent(httpURLConnection.getInputStream());
					return;
				}
				loadDataForConnectionResult(httpURLConnection.getInputStream(), connectionResult);
			} catch (Exception e) {
				printInfo("Connection loading failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
				connectionResult.setStatusMessage("Connection loading failed, exception " + e);
			}
		}
		if(!isSkipDataRead){
			httpURLConnection.disconnect();
		}
	}

	public static boolean isAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	public static HttpURLConnection openHttpURLConnectionWithHttps(String httpMethod, String strUrl, String[][] headerArrays, Object[][] contentArrays
			, SSLContext sslContext, HostnameVerifier hostnameVerifier){
		HttpURLConnection httpURLConnection = null;
		try {
			if(TextUtils.isEmpty(httpMethod)){
				httpMethod = HTTP_METHOD_GET;
			}
			if(HTTP_METHOD_GET.equals(httpMethod)){
				for(int i=0; i<contentArrays.length; i++){
					strUrl = strUrl + (i == 0 ? "?" : "&") + contentArrays[i][0] + "=" + contentArrays[i][1];
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
			printInfo("Connection open failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
		}finally{
			printInfo(httpMethod + ", " + strUrl, NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl);
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
					printInfo("Connection https certificate file init failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
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
			printInfo("Connection https security init failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
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

			if(contentArrays == null || contentArrays.length == 0 || contentArrays[0].length < 2){
				return true;
			}
			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			String charsetName = Charset.forName("UTF-8").displayName();
			for(Object[] contentArray : contentArrays){
				dataOutputStream.writeBytes(hyphens + boundary + breakLine);
				if (contentArray.length == 2) {
					dataOutputStream.write(("Content-Disposition: form-data;" +
							" name=\"" + contentArray[0] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);

					dataOutputStream.write(((String) contentArray[1]).getBytes(charsetName));
				} else if (contentArray.length == 4) {
					dataOutputStream.write(("Content-Disposition: form-data;" +
							" name=\"" + contentArray[0] + "\";" +
							" filename=\"" + contentArray[1] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.write(("Content-Type: " + contentArray[2]).getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);

					if (contentArray[3] != null && contentArray[3] instanceof InputStream) {
						InputStream inputStream = (InputStream) contentArray[3];
						int progress;
						byte[] buffer = new byte[NETWORKSETTING.mBufferSize];
						while ((progress = inputStream.read(buffer)) != -1) {
							dataOutputStream.write(buffer, 0, progress);
						}
						dataOutputStream.flush();
						inputStream.close();
					}
				}
				dataOutputStream.writeBytes(breakLine);
			}
			dataOutputStream.writeBytes(hyphens + boundary + hyphens);
			dataOutputStream.flush();
			dataOutputStream.close();
		} catch (Exception e) {
			printInfo("Connection prepare failed, exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
			httpURLConnection = null;
		}
		return httpURLConnection != null;
	}

	public static ConnectionResult getInitConnectResult(){
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setStatusMessage("Connection check failed, no network connection");
		return connectionResult;
	}

	private static Object loadData(InputStream is, String contentType){
		if(is == null){
			return null;
		}
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setContentType(contentType);
		Object object;
		if(checkContentTypeIsText(connectionResult)){
			object = inputStreamToString(is, connectionResult.getContentCharset(), NETWORKSETTING.mBufferSize);
		}else{
			object = inputStreamToByteArray(is, NETWORKSETTING.mBufferSize);
		}
		return object;
	}

	private static void loadDataForConnectionResult(InputStream is, ConnectionResult connectionResult){
		if(is == null){
			return;
		}
		Object object;
		if(checkContentTypeIsText(connectionResult)){
			object = inputStreamToString(is, connectionResult.getContentCharset(), NETWORKSETTING.mBufferSize);
		}else{
			object = inputStreamToByteArray(is, NETWORKSETTING.mBufferSize);
		}
		if(object instanceof String && object.equals("OutOfMemoryError")){
			connectionResult.setStatusMessage("Connection loading failed, OutOfMemoryError");
			object = null;
		}
		connectionResult.setContent(object);
	}

	public static boolean checkContentTypeIsText(ConnectionResult connectionResult){
		boolean isText = false;
		Charset charset = null;
		String contentType = connectionResult.getContentType();
		if(contentType != null && contentType.trim().length() > 0){
			String[] values = contentType.split(";");
			// 取得網頁文字編碼
			for(String value : values){
				value = value.trim();
				if(value.contains("text/")){
					isText = true;
				}
				if(value.toLowerCase(Locale.ENGLISH).startsWith("charset=")){
					charset = Charset.forName(value.substring("charset=".length()));
					connectionResult.setContentCharset(charset);
				}
				if(isText && charset != null){
					break;
				}
			}
		}
		return isText;
	}

	public static String inputStreamToString(InputStream is, Charset charset, int bufferSize){
		if(is == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
			printInfo("charset get fail, using default, ", false);
		}
		printInfo("charset = " + charset.displayName(), false);
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		BufferedReader reader;
		StringBuilder stringBuilder = new StringBuilder();// StringBuilder速度較快但不支援多執行緒同步
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(is, charset), bufferSize);
			try {
				while((line = reader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				is.close();
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			reader = null;
			stringBuilder = null;
			line = null;
			stringBuilder = new StringBuilder("OutOfMemoryError");
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public static Object inputStreamToByteArray(InputStream is, int bufferSize){
		if(is == null){
			return null;
		}
		Object byteArray = null;
		int progress;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		ByteArrayOutputStream baos;
		try {
			buffer = new byte[bufferSize];
			baos = new ByteArrayOutputStream();
			try {
				while((progress = is.read(buffer)) != -1){
					baos.write(buffer, 0, progress);
				}
				baos.flush();
			} finally {
				is.close();
			}
			try {
				byteArray = baos.toByteArray();
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			byteArray = null;
			buffer = null;
			baos = null;
			byteArray = "OutOfMemoryError";
			e.printStackTrace();
		}
		return byteArray;
	}

	public static class ConnectionResult {

		private int mResponseCode;
		private String mResponseMessage;
		private String mRequestType;
		private String mConnectUrl;
		private String mStatusMessage;
		private String mContentType;
		private String mContentEncoding;
		private long mContentLength;
		private Charset mContentCharset;
		private Object mContent;
		private HttpURLConnection mHttpURLConnection;

		public void setResult(String requestType, String connectUrl, int responseCode, String responseMessage
				, String statusMessage, Object content, String contentEncoding, long contentLength, String contentType
				, Charset contentCharset){
			mRequestType = requestType;
			mConnectUrl = connectUrl;
			mResponseCode = responseCode;
			mResponseMessage = responseMessage;
			mStatusMessage = statusMessage;
			mContent = content;
			mContentEncoding = contentEncoding;
			mContentLength = contentLength;
			mContentType = contentType;
			mContentCharset = contentCharset;
		}

		public void setData(Object content, String contentEncoding, long contentLength, String contentType, Charset contentCharset){
			mContent = content;
			mContentEncoding = contentEncoding;
			mContentLength = contentLength;
			mContentType = contentType;
			mContentCharset = contentCharset;
		}

		public void setRequestType(String requestType){
			mRequestType = requestType;
		}

		public void setConnectUrl(String connectUrl){
			mConnectUrl = connectUrl;
		}

		public void setResponseCode(int responseCode){
			mResponseCode = responseCode;
		}

		public void setResponseMessage(String responseMessage){
			mResponseMessage = responseMessage;
		}

		public void setStatusMessage(String statusMessage){
			mStatusMessage = statusMessage;
		}

		public void setContent(Object content){
			mContent = content;
		}

		public void setContentEncoding(String contentEncoding){
			mContentEncoding = contentEncoding;
		}

		public void setContentLength(long contentLength){
			mContentLength = contentLength;
		}

		public void setContentType(String contentType){
			mContentType = contentType;
		}

		public void setContentCharset(Charset contentCharset){
			mContentCharset = contentCharset;
		}

		public String getRequestType(){
			return mRequestType;
		}

		public String getConnectUrl(){
			return mConnectUrl;
		}

		public int getResponseCode(){
			return mResponseCode;
		}

		public String getResponseMessage(){
			return mResponseMessage;
		}

		public String getStatusMessage(){
			return mStatusMessage;
		}

		public Object getContent(){
			return mContent;
		}

		public String getContentEncoding(){
			return mContentEncoding;
		}

		public long getContentLength(){
			return mContentLength;
		}

		public String getContentType(){
			return mContentType;
		}

		public Charset getContentCharset(){
			return mContentCharset;
		}

		public String getConnectionInfo(){
			String connectionInfo = mResponseCode + ", " + mResponseMessage + ", " + mRequestType + ", " + mStatusMessage +
					", \n" + mConnectUrl +
					", \n" + mContentEncoding + ", " + mContentLength +
					", \n" + mContentType;
			return connectionInfo;
		}

		private void setHttpURLConnection(HttpURLConnection httpURLConnection){
			mHttpURLConnection = httpURLConnection;
		}

		public void disconnect(){
			if(mHttpURLConnection != null){
				mHttpURLConnection.disconnect();
				mHttpURLConnection = null;
			}
		}
	}
}