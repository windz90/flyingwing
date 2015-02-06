package com.andy.library.module;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.4.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_networkAccess {
	
	public static final int SPLIT_AUTO_MAX_QUANTITY = 0;
	public static final int SPLIT_BY_QUANTITY = 1;
	public static final int SPLIT_BY_LENGTH = 2;
	public static final int CONNECTION_CONNECT_FAIL = 0x101;
	public static final int CONNECTION_CONNECTED = 0x102;
	public static final int CONNECTION_LOAD_FAIL = 0x103;
	public static final int CONNECTION_LOADED = 0x104;
	private static final String ONLY_READ_HEADER = "header";
	private static final String REQUEST_GET = "GET";
	private static final String REQUEST_POST = "POST";
	private static final NetworkSetting NETWORKSETTING = new NetworkSetting();
	
	public static class NetworkSetting {
		private int connectTimeout = 20000;
		private int readTimeout = 65000;
		private int bufferSize = 8192;
		private boolean isPrintConnectionUrl = true;
		private boolean isPrintConnectException = true;
	}
	
	public static void setConnectTimeout(int connectTimeout){
		C_networkAccess.NETWORKSETTING.connectTimeout = connectTimeout;
	}
	
	public static int getConnectTimeout(){
		return C_networkAccess.NETWORKSETTING.connectTimeout;
	}
	
	public static void setReadTimeout(int readTimeout){
		C_networkAccess.NETWORKSETTING.readTimeout = readTimeout;
	}
	
	public static int getReadTimeout(){
		return C_networkAccess.NETWORKSETTING.readTimeout;
	}
	
	public static void setBufferSize(int bufferSize){
		C_networkAccess.NETWORKSETTING.bufferSize = bufferSize;
	}
	
	public static int getBufferSize(){
		return C_networkAccess.NETWORKSETTING.bufferSize;
	}
	
	public static void setPrintConnectionUrl(boolean isPrintConnectionUrl){
		C_networkAccess.NETWORKSETTING.isPrintConnectionUrl = isPrintConnectionUrl;
	}
	
	public static boolean isPrintConnectionUrl(){
		return C_networkAccess.NETWORKSETTING.isPrintConnectionUrl;
	}
	
	public static void setPrintConnectException(boolean isPrintConnectException){
		C_networkAccess.NETWORKSETTING.isPrintConnectException = isPrintConnectException;
	}
	
	public static boolean isPrintConnectException(){
		return C_networkAccess.NETWORKSETTING.isPrintConnectException;
	}
	
	private static void printInfo(String info, boolean isPrint){
		if(isPrint){
			System.out.println(info);
		}
	}
	
	/**
	 * @param context
	 * @param httpUrl
	 * @param objectArray
	 * @param isSkipDataRead 若為true，完成連線後須自行調用HttpURLConnection.disconnect()斷開連線
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, boolean isSkipDataRead, Handler handler){
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		if(isConnect(context)){
			connectUseHttpURLConnection(context, httpUrl, objectArray, null, isSkipDataRead, connectionResult, handler);
		}
		return connectionResult;
	}
	
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray, Handler handler){
		return connectUseHttpURLConnection(context, httpUrl, objectArray, false, handler);
	}
	
	/**
	 * String key : map.get("0");<br>
	 * String value : map.get("1");<br>
	 * String MIME Type : map.get("2");<br>
	 * InputStream is : map.get("3");<br>
	 * @param context
	 * @param httpUrl
	 * @param contentList
	 * @param isSkipDataRead 若為true，完成連線後須自行調用HttpURLConnection.disconnect()斷開連線
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, List<Map<String, Object>> contentList
			, boolean isSkipDataRead, Handler handler){
		Object[][] objectArray = new Object[contentList.size()][3];
		Map<String, Object> map = new HashMap<String, Object>();
		for(int i=0; i<contentList.size(); i++){
			map = contentList.get(i);
			objectArray[i][0] = map.get("0");
			objectArray[i][1] = map.get("1");
			objectArray[i][2] = map.get("2");
			objectArray[i][3] = map.get("3");
		}
		return connectUseHttpURLConnection(context, httpUrl, objectArray, isSkipDataRead, handler);
	}
	
	/**
	 * String key : map.get("0");<br>
	 * String value : map.get("1");<br>
	 * String MIME Type : map.get("2");<br>
	 * InputStream is : map.get("3");<br>
	 * @param context
	 * @param httpUrl
	 * @param contentList
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, List<Map<String, Object>> contentList
			, Handler handler){
		return connectUseHttpURLConnection(context, httpUrl, contentList, false, handler);
	}
	
	/**
	 * HttpURLConnection MultiPort
	 * @param context
	 * @param httpUrl
	 * @param objectArray
	 * @param requestRangeIndex 連線後要求回傳的內容區間
	 * @param isSkipDataRead 若為true，完成連線後須自行調用HttpURLConnection.disconnect()斷開連線
	 * @return
	 */
	private static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, String requestRangeIndex, boolean isSkipDataRead){
		if(isConnect(context)){
			return connectUseHttpURLConnection(context, httpUrl, objectArray, requestRangeIndex, isSkipDataRead, null, null);
		}
		return null;
	}
	
	/**
	 * HttpURLConnection MultiPort
	 * @param context
	 * @param httpUrl
	 * @param objectArray
	 * @param requestRangeIndex 連線後要求回傳的內容區間
	 * @param isSkipDataRead
	 * @param connectionResult
	 * @param handler
	 * @return
	 */
	private static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, String requestRangeIndex, boolean isSkipDataRead, ConnectionResult connectionResult, Handler handler){
		HttpURLConnection httpURLConnection = connectUseHttpURLConnectionImplementRequest(httpUrl, objectArray
				, requestRangeIndex);
		
		if(httpURLConnection == null || connectionResult == null){
			if(connectionResult != null){
				connectionResult.setStatusMessage("Connecting, Connect Fail Exception");
			}
			return httpURLConnection;
		}
		connectionResult.setConnectUrl(httpURLConnection.getURL().getPath());
		connectionResult.setRequestType(httpURLConnection.getRequestMethod());
		try {
			connectionResult.setResponseCode(httpURLConnection.getResponseCode());
			connectionResult.setResponseMessage(httpURLConnection.getResponseMessage());
			if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK && 
					httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
				printInfo("Connect Fail StatusCode " + httpURLConnection.getResponseCode()
						, C_networkAccess.NETWORKSETTING.isPrintConnectException);
				connectionResult.setStatusMessage("Connect Fail StatusCode " + httpURLConnection.getResponseCode());
				
				if(handler != null){
					Message msg = new Message();
					msg.what = CONNECTION_CONNECT_FAIL;
					msg.obj = connectionResult;
					handler.sendMessage(msg);
				}
				return httpURLConnection;
			}
			
			connectionResult.setStatusMessage("Connect Success");
			connectionResult.setContentEncoding(httpURLConnection.getContentEncoding());
			connectionResult.setContentLength(httpURLConnection.getContentLength());
			connectionResult.setContentType(httpURLConnection.getContentType());
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
				printInfo("Connect ok StatusCode " + httpURLConnection.getResponseCode(), false);
			}else if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
				printInfo("Connecting StatusCode " + httpURLConnection.getResponseCode(), false);
			}
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("Connect, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
			connectionResult.setStatusMessage("Connect, Connect Fail Exception " + e);
		}
		
		if(handler != null){
			Message msg = new Message();
			msg.what = CONNECTION_CONNECTED;
			msg.obj = connectionResult;
			handler.sendMessage(msg);
		}
		
		try {
			if(isSkipDataRead){
				connectionResult.setContent(httpURLConnection.getInputStream());
				return httpURLConnection;
			}
			deployDataForConnectionResult(httpURLConnection.getInputStream(), connectionResult);
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("LoadData, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
			connectionResult.setStatusMessage("LoadData, Connect Fail Exception " + e);
		}
		httpURLConnection.disconnect();
		return httpURLConnection;
	}
	
	public static HttpURLConnection connectUseHttpURLConnectionImplementRequest(String httpUrl, Object[][] objectArray, String requestRangeIndex){
		String requestType = "";
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection httpURLConnection;
			if(objectArray == null){
				// HttpGet方法
				requestType = REQUEST_GET;
				httpURLConnection = useHttpURLConnectionGet(url, requestRangeIndex);
			}else{
				// HttpPost方法
				requestType = REQUEST_POST;
				httpURLConnection = useHttpURLConnectionPost(url, objectArray, requestRangeIndex);
			}
			
			httpURLConnection.connect();
			return httpURLConnection;
//		} catch (MalformedURLException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("Connecting, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
		}finally{
			printInfo(requestType + ", " + httpUrl, C_networkAccess.NETWORKSETTING.isPrintConnectionUrl);
		}
		return null;
	}
	
	private static HttpURLConnection useHttpURLConnectionGet(URL httpUrl, String requestRangeIndex){
		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection)httpUrl.openConnection();
			httpURLConnection.setRequestMethod(REQUEST_GET);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(false);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setConnectTimeout(NETWORKSETTING.connectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.readTimeout);
			httpURLConnection.setChunkedStreamingMode(NETWORKSETTING.bufferSize);
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			if(requestRangeIndex != null && requestRangeIndex.trim().length() > 0){
				if(requestRangeIndex.equals(ONLY_READ_HEADER)){
					httpURLConnection.setRequestProperty("Range", "bytes=" + "0-0");
				}else{
					httpURLConnection.setRequestProperty("Range", "bytes=" + requestRangeIndex);
				}
			}
			return httpURLConnection;
//		} catch (ProtocolException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("HttpGetSetting, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
		}
		return null;
	}
	
	private static HttpURLConnection useHttpURLConnectionPost(URL httpUrl, Object[][] objectArray, String requestRangeIndex){
		String hyphens = "--";
		String boundary = "*****abcde*****";
		String breakLine = "\r\n";
		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection)httpUrl.openConnection();
			httpURLConnection.setRequestMethod(REQUEST_POST);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setConnectTimeout(NETWORKSETTING.connectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.readTimeout);
			httpURLConnection.setChunkedStreamingMode(NETWORKSETTING.bufferSize);
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			if(requestRangeIndex != null && requestRangeIndex.trim().length() > 0){
				if(requestRangeIndex.equals(ONLY_READ_HEADER)){
					httpURLConnection.setRequestProperty("Range", "bytes=" + "0-0");
				}else{
					httpURLConnection.setRequestProperty("Range", "bytes=" + requestRangeIndex);
				}
			}
			
			if(objectArray == null || objectArray.length == 0 || objectArray[0].length < 2){
				return httpURLConnection;
			}
			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			String charsetName = Charset.forName("UTF-8").displayName();
			for(int i=0; i<objectArray.length; i++){
				dataOutputStream.writeBytes(hyphens + boundary + breakLine);
				if(objectArray[i].length == 2){
					dataOutputStream.write(("Content-Disposition: form-data;" + 
							" name=\"" + (String)objectArray[i][0] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);
					
					dataOutputStream.write(((String)objectArray[i][1]).getBytes(charsetName));
				}else if(objectArray[i].length == 4){
					dataOutputStream.write(("Content-Disposition: form-data;" + 
							" name=\"" + (String)objectArray[i][0] + "\";" + 
							" filename=\"" + (String)objectArray[i][1] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.write(("Content-Type: " + (String)objectArray[i][2]).getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);
					
					if(objectArray[i][3] != null && objectArray[i][3] instanceof InputStream){
						InputStream is = (InputStream)objectArray[i][3];
						int progress;
						byte[] buffer = new byte[1024 * 8];
						while((progress = is.read(buffer)) != -1){
							dataOutputStream.write(buffer, 0, progress);
						}
						dataOutputStream.flush();
						is.close();
					}
				}
				dataOutputStream.writeBytes(breakLine);
			}
			dataOutputStream.writeBytes(hyphens + boundary + hyphens);
			dataOutputStream.flush();
			dataOutputStream.close();
			return httpURLConnection;
//		} catch (ProtocolException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("HttpPostSetting, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
		}
		return null;
	}
	
	/**
	 * HttpGet
	 * @param context
	 * @param httpUrl
	 * @param isSkipDataRead
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, boolean isSkipDataRead, Handler handler){
		return connectUseHttpClient(context, httpUrl, null, null, isSkipDataRead, handler);
	}
	
	/**
	 * HttpGet
	 * @param context
	 * @param httpUrl
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, Handler handler){
		return connectUseHttpClient(context, httpUrl, null, null, false, handler);
	}
	
	/**
	 * HttpPost UrlEncodedFormEntity
	 * @param context
	 * @param httpUrl
	 * @param httpPostData
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, String[][] httpPostData, Handler handler){
		return connectUseHttpClient(context, httpUrl, httpPostData, null, false, handler);
	}
	
	/**
	 * HttpPost UrlEncodedFormEntity
	 * @param context
	 * @param httpUrl
	 * @param httpPostData
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, String[][] httpPostData){
		return connectUseHttpClient(context, httpUrl, httpPostData, null, false, null);
	}
	
	/**
	 * HttpPost UrlEncodedFormEntity
	 * @param context
	 * @param httpUrl
	 * @param httpPostData
	 * @param isSkipDataRead
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, String[][] httpPostData
			, boolean isSkipDataRead, Handler handler){
		return connectUseHttpClient(context, httpUrl, httpPostData, null, isSkipDataRead, handler);
	}
	
	/**
	 * HttpPost InputStreamEntity
	 * @param context
	 * @param httpUrl
	 * @param is
	 * @param isSkipDataRead
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, InputStream is, boolean isSkipDataRead
			, Handler handler){
		return connectUseHttpClient(context, httpUrl, null, is, isSkipDataRead, handler);
	}
	
	/**
	 * HttpPost InputStreamEntity
	 * @param context
	 * @param httpUrl
	 * @param is
	 * @param handler
	 * @return
	 */
	public static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, InputStream is, Handler handler){
		return connectUseHttpClient(context, httpUrl, null, is, false, handler);
	}
	
	private static ConnectionResult connectUseHttpClient(final Context context, String httpUrl, String[][] httpPostData
			, InputStream is, boolean isSkipDataRead, Handler handler){
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		if(isConnect(context)){
			connectUseHttpClientImplementConnection(httpUrl, getHttpPost(httpUrl, httpPostData, is), connectionResult, isSkipDataRead, handler);
		}
		return connectionResult;
	}
	
	public static ConnectionResult connectUseHttpClientImplementConnection(String httpUrl, HttpPost httpPost
			, ConnectionResult connectionResult, boolean isSkipDataRead, Handler handler){
		HttpResponse httpResponse = null;
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, NETWORKSETTING.connectTimeout);
		HttpConnectionParams.setSoTimeout(httpParams, NETWORKSETTING.readTimeout);
		HttpConnectionParams.setSocketBufferSize(httpParams, NETWORKSETTING.bufferSize);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register (new Scheme ("http", PlainSocketFactory.getSocketFactory(), 80));
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLSocketFactory sslSocketFactory = new SSLSocketFactory(keyStore);
			schemeRegistry.register (new Scheme ("https", sslSocketFactory, 443));
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		
		HttpClient httpClient = new DefaultHttpClient(threadSafeClientConnManager, httpParams);
		httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
		connectionResult.setConnectUrl(httpUrl);
		connectionResult.setStatusMessage("Connect Success");
		
		try {
			if(httpPost == null){
				// HttpGet方法
				connectionResult.setRequestType(REQUEST_GET);
				HttpGet httpGet = new HttpGet(httpUrl);
				httpResponse = httpClient.execute(httpGet);
			}else{
				// HttpPost方法
				connectionResult.setRequestType(REQUEST_POST);
				httpResponse = httpClient.execute(httpPost);
			}
//		} catch (ClientProtocolException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("Connecting, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
			connectionResult.setStatusMessage("Connecting, Connect Fail Exception " + e);
		}finally{
			printInfo(connectionResult.getRequestType() + ", " + httpUrl, C_networkAccess.NETWORKSETTING.isPrintConnectionUrl);
		}
		
		if(httpResponse == null){
			return connectionResult;
		}
		StatusLine statusLine = httpResponse.getStatusLine();
		if(statusLine == null){
			return connectionResult;
		}
		connectionResult.setResponseCode(statusLine.getStatusCode());
		connectionResult.setResponseMessage(statusLine.getReasonPhrase());
		if(statusLine.getStatusCode() != HttpStatus.SC_OK){
			printInfo("Connect Fail StatusCode " + statusLine.getStatusCode(), C_networkAccess.NETWORKSETTING.isPrintConnectException);
			connectionResult.setStatusMessage("Connect Fail StatusCode " + statusLine.getStatusCode());
			
			if(handler != null){
				Message msg = new Message();
				msg.what = CONNECTION_CONNECT_FAIL;
				msg.obj = connectionResult;
				handler.sendMessage(msg);
			}
			return connectionResult;
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		Header header = httpEntity.getContentEncoding();
		connectionResult.setContentEncoding(header == null ? null : header.getValue());
		connectionResult.setContentLength(httpEntity.getContentLength());
		header = httpEntity.getContentType();
		connectionResult.setContentType(header == null ? null : header.getValue());
		
		if(handler != null){
			Message msg = new Message();
			msg.what = CONNECTION_CONNECTED;
			msg.obj = connectionResult;
			handler.sendMessage(msg);
		}
		
		try{
			if(isSkipDataRead){
				connectionResult.setContent(httpEntity.getContent());
				return connectionResult;
			}
			deployDataForConnectionResult(httpEntity.getContent(), connectionResult);
//		} catch (IllegalStateException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("LoadData, Exception " + e, C_networkAccess.NETWORKSETTING.isPrintConnectException);
			connectionResult.setStatusMessage("LoadData, Connect Fail Exception " + e);
		}
		printInfo("Connect ok StatusCode " + httpResponse.getStatusLine().getStatusCode(), false);
		return connectionResult;
	}
	
	private static HttpPost getHttpPost(String httpUrl, String[][] httpPostData, InputStream is){
		/*
		 * AbstractHttpEntity, BasicHttpEntity, BufferedHttpEntity, ByteArrayEntity, EntityTemplate
		 * , FileEntity, HttpEntityWrapper, InputStreamEntity, SerializableEntity, StringEntity
		 */
		HttpPost httpPost = null;
		if(httpPostData != null){
			httpPost = getUrlEncodedFormEntity(httpUrl, httpPostData);
		}else if(is != null){
			httpPost = getInputStreamEntity(httpUrl, is);
		}
		return httpPost;
	}
	
	public static HttpPost getUrlEncodedFormEntity(String httpUrl, String[][] httpPostData){
		if(httpPostData == null){
			return null;
		}
		HttpPost httpPost = new HttpPost(httpUrl);
		List<NameValuePair> putNvpArrayList = new ArrayList<NameValuePair>();
		for(int i=0; i<httpPostData.length; i++){
			putNvpArrayList.add(new BasicNameValuePair(httpPostData[i][0], httpPostData[i][1]));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(putNvpArrayList, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return httpPost;
	}
	
	public static HttpPost getInputStreamEntity(String httpUrl, InputStream is){
		if(is == null){
			return null;
		}
		HttpPost httpPost = new HttpPost(httpUrl);
		httpPost.addHeader("Content-Type", "application/octet-stream");
		
		int progress;
		byte[] buffer = new byte[1024 * 8];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			try {
				while((progress = is.read(buffer)) != -1){
					baos.write(buffer, 0, progress);
				}
				baos.flush();
			} finally {
				is.close();
			}
			try {
				byte[] byteArray = baos.toByteArray();
				httpPost.setEntity(new InputStreamEntity(new ByteArrayInputStream(byteArray), byteArray.length));
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			buffer = null;
			e.printStackTrace();
		}
		return httpPost;
	}
	
	private static Object deployData(InputStream is, String contentType){
		if(is == null){
			return null;
		}
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setContentType(contentType);
		Object object;
		if(checkContentTypeIsText(connectionResult)){
			object = inputStreamToString(is, connectionResult.getContentCharset(), 1024 * 16);
		}else{
			object = inputStreamToByteArray(is, 1024 * 16);
		}
		return object;
	}
	
	private static void deployDataForConnectionResult(InputStream is, ConnectionResult connectionResult){
		if(is == null){
			return;
		}
		Object object;
		if(checkContentTypeIsText(connectionResult)){
			object = inputStreamToString(is, connectionResult.getContentCharset(), 1024 * 16);
		}else{
			object = inputStreamToByteArray(is, 1024 * 16);
		}
		if(object instanceof String && object.equals("OutOfMemoryError")){
			connectionResult.setStatusMessage("LoadData, Connect Fail OutOfMemoryError");
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
					if(connectionResult != null){
						connectionResult.setContentCharset(charset);
					}
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
					stringBuilder.append(line + "\n");
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
	
	public static Object objectCompose(Object...objectArray){
		if(objectArray == null || objectArray.length == 0){
			return null;
		}
		Object object = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				for(int i=0; i<objectArray.length; i++){
					if(objectArray[i] != null){
						oos.writeObject(objectArray[i]);
						oos.flush();
					}
				}
			} finally {
				oos.close();
			}
			try {
				byte[] byteArray = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
				ObjectInputStream ois = new ObjectInputStream(bais);
				bais.close();
				
				object = ois.readObject();
				ois.close();
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			objectArray = null;
			e.printStackTrace();
		}
		return object;
	}
	
	public static boolean isConnectedSelectedType(Context context, int connectivityManagerType) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getNetworkInfo(connectivityManagerType);
		if(networkInfo != null){
			State state = networkInfo.getState();
			if(state == State.CONNECTED){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConnectedMobileNetwork(Context context) {
		if(isConnectedSelectedType(context, ConnectivityManager.TYPE_MOBILE)){
			return true;
		}
		return false;
	}
	
	public static boolean isConnectedWIFI(Context context) {
		if(isConnectedSelectedType(context, ConnectivityManager.TYPE_WIFI)){
			return true;
		}
		return false;
	}
	
	public static boolean isConnect(Context context) {
		boolean isConnect = false;
		ConnectivityManager connectManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectManager.getActiveNetworkInfo() != null){
			isConnect = connectManager.getActiveNetworkInfo().isAvailable();
		}
//		NetworkInfo[] networkInfo = ContyManager.getAllNetworkInfo();
//		if(networkInfo != null){
//			for(int i=0; i<networkInfo.length; i++){
//				if(networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
//					return true;
//				}
//			}
//		}
		return isConnect;
	}
	
	public static class ConnectionResult {
		
		private int responseCode;
		private String responseMessage;
		private String requestType;
		private String connectUrl;
		private String statusMessage;
		private String contentType;
		private String contentEncoding;
		private long contentLength;
		private Charset contentCharset;
		private Object content;
		
		public void setResult(String requestType, String connectUrl, int responseCode, String responseMessage
				, String statusMessage, Object content, String contentEncoding, long contentLength, String contentType
				, Charset contentCharset){
			this.requestType = requestType;
			this.connectUrl = connectUrl;
			this.responseCode = responseCode;
			this.responseMessage = responseMessage;
			this.statusMessage = statusMessage;
			this.content = content;
			this.contentEncoding = contentEncoding;
			this.contentLength = contentLength;
			this.contentType = contentType;
			this.contentCharset = contentCharset;
		}
		
		public void setData(Object content, String contentEncoding, long contentLength, String contentType, Charset contentCharset){
			this.content = content;
			this.contentEncoding = contentEncoding;
			this.contentLength = contentLength;
			this.contentType = contentType;
			this.contentCharset = contentCharset;
		}
		
		public void setRequestType(String requestType){
			this.requestType = requestType;
		}
		
		public void setConnectUrl(String connectUrl){
			this.connectUrl = connectUrl;
		}
		
		public void setResponseCode(int responseCode){
			this.responseCode = responseCode;
		}
		
		public void setResponseMessage(String responseMessage){
			this.responseMessage = responseMessage;
		}
		
		public void setStatusMessage(String statusMessage){
			this.statusMessage = statusMessage;
		}
		
		public void setContent(Object content){
			this.content = content;
		}
		
		public void setContentEncoding(String contentEncoding){
			this.contentEncoding = contentEncoding;
		}
		
		public void setContentLength(long contentLength){
			this.contentLength = contentLength;
		}
		
		public void setContentType(String contentType){
			this.contentType = contentType;
		}
		
		public void setContentCharset(Charset contentCharset){
			this.contentCharset = contentCharset;
		}
		
		public String getRequestType(){
			return requestType;
		}
		
		public String getConnectUrl(){
			return connectUrl;
		}
		
		public int getResponseCode(){
			return responseCode;
		}
		
		public String getResponseMessage(){
			return responseMessage;
		}
		
		public String getStatusMessage(){
			return statusMessage;
		}
		
		public Object getContent(){
			return content;
		}
		
		public String getContentEncoding(){
			return contentEncoding;
		}
		
		public long getContentLength(){
			return contentLength;
		}
		
		public String getContentType(){
			return contentType;
		}
		
		public Charset getContentCharset(){
			return contentCharset;
		}
		
		public String getConnectionInfo(){
			String connectionInfo = responseCode + ", " + responseMessage + ", " + requestType + ", " + statusMessage + 
					", \n" + connectUrl + 
					", \n" + contentEncoding + ", " + contentLength + 
					", \n" + contentType;
			return connectionInfo;
		}
	}
	
	public static class MultiPortManager {
		
		private boolean[] portStatusArray;
		private long[][] portLengthArray;
		private Object[] portContentArray;
		
		public interface MultiPortDownLoadComplete {
			public void connectFail(HttpURLConnection httpURLConnection);
			public void connected(HttpURLConnection httpURLConnection);
			public void loadFail(HttpURLConnection httpURLConnection);
			public void loading(HttpURLConnection httpURLConnection, int count, Object objectPort);
			public void loadExpired(HttpURLConnection httpURLConnection, int count);
			public void loaded(HttpURLConnection httpURLConnection, int sum, Object object);
		}
		
		public MultiPortManager(){}
		
		public void startDownLoad(final Context context, final String httpUrl, final Object[][] objectArray
				, final int multiPortMode, final int multiPortValue, final MultiPortDownLoadComplete multiPortComplete){
			final Handler handler = new Handler(new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					HttpURLConnection httpURLConnection = (HttpURLConnection)msg.obj;
					try {
						if(httpURLConnection == null || (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK && 
								httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)){
							multiPortComplete.connectFail(httpURLConnection);
							return false;
						}
					} catch (IOException e) {
						multiPortComplete.connectFail(httpURLConnection);
						return false;
					}
					multiPortComplete.connected(httpURLConnection);
					multiPortDownLoad(context, httpUrl, objectArray, httpURLConnection
							, multiPortMode, multiPortValue, multiPortComplete);
					return false;
				}
			});
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					HttpURLConnection httpURLConnection = connectUseHttpURLConnection(context, httpUrl, objectArray
							, ONLY_READ_HEADER, false);
					Message msg = new Message();
					msg.obj = httpURLConnection;
					handler.sendMessage(msg);
				}
			});
			thread.start();
		}
		
		public void multiPortDownLoad(Context context, final String httpUrl, final Object[][] objectArray
				, final HttpURLConnection httpURLConnection, int multiPortMode, int multiPortValue
				, final MultiPortDownLoadComplete multiPortComplete){
			String acceptRanges = httpURLConnection.getHeaderField("Accept-Ranges");
			String contentRange = httpURLConnection.getHeaderField("Content-Range");
			String eTag = httpURLConnection.getHeaderField("ETag");
			long allLength = 0, portLength = 0;
			int portQuantity = 0;
			if(contentRange != null && contentRange.contains("/") && !contentRange.endsWith("/")){
				allLength = Long.parseLong(contentRange.substring(contentRange.indexOf("/") + 1));
			}
			if(acceptRanges != null && acceptRanges.equals("bytes") && eTag != null){
				switch (multiPortMode) {
				case SPLIT_AUTO_MAX_QUANTITY:
					break;
				case SPLIT_BY_QUANTITY:
					if(multiPortValue > 0){
						portQuantity = multiPortValue;
						portLength = allLength / portQuantity;
					}
					break;
				case SPLIT_BY_LENGTH:
					if(multiPortValue > 20){
						portLength = multiPortValue;
						portQuantity = (int)(allLength / portLength) + 1;
					}
					break;
				}
				if(portQuantity > 20 || portQuantity < 1){
					portQuantity = 20;
					portLength = allLength / portQuantity;
				}
				if(portLength < 21){
					portQuantity = 1;
					portLength = allLength;
				}
				
				multiPortDispatch(context, httpUrl, objectArray, eTag, portQuantity, portLength, multiPortComplete);
			}else{
				final Handler handler = new Handler(new Callback() {
					
					@Override
					public boolean handleMessage(Message msg) {
						multiPortComplete.loaded(httpURLConnection, 1, msg.obj);
						return false;
					}
				});
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Message msg = new Message();
						try {
							msg.obj = deployData(httpURLConnection.getInputStream(), httpURLConnection.getContentType());
						} catch (IOException e) {
							e.printStackTrace();
						}
						httpURLConnection.disconnect();
						handler.sendMessage(msg);
					}
				});
				thread.start();
			}
		}
		
		private void multiPortDispatch(final Context context, final String httpUrl, final Object[][] objectArray
				, final String eTag, final int portQuantity, final long portLength
				, final MultiPortDownLoadComplete multiPortComplete){
			final Handler handler = new Handler(new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					multiPortDownLoadSub(context, httpUrl, objectArray, eTag, msg.what, multiPortComplete);
					return false;
				}
			});
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					portStatusArray = new boolean[portQuantity];
					portLengthArray = new long[portQuantity][2];
					portContentArray = new Object[portQuantity];
					for(int i=0; i<portLengthArray.length; i++){
						if(i == 0){
							portLengthArray[i][0] = i;
							portLengthArray[i][1] = portLength - 1;
						}else{
							portLengthArray[i][0] = portLength * i;
							portLengthArray[i][1] = portLength * (i + 1) - 1;
						}
						handler.sendEmptyMessage(i);
					}
				}
			});
			thread.start();
		}
		
		private void multiPortDownLoadSub(final Context context, final String httpUrl, final Object[][] objectArray
				, final String eTag, final int count, final MultiPortDownLoadComplete multiPortComplete){
			final Handler handler = new Handler(new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					Object[] objects = (Object[])msg.obj;
					HttpURLConnection httpURLConnection = (HttpURLConnection)objects[0];
					if(httpURLConnection == null){
						multiPortComplete.loadExpired(httpURLConnection, count);
						return false;
					}
					String portETag = httpURLConnection.getHeaderField("ETag");
					if(portETag == null || !eTag.equals(portETag)){
						multiPortComplete.loadExpired(httpURLConnection, count);
						return false;
					}
					portContentArray[count] = objects[1];
					portStatusArray[count] = true;
					
					if(isMultiPortDownloaded()){
						multiPortComplete.loaded(httpURLConnection, portStatusArray.length, objectCompose(portContentArray));
					}else{
						if(portContentArray[count] != null){
							multiPortComplete.loading(httpURLConnection, count, objectCompose(portContentArray));
						}else{
							multiPortComplete.loadFail(httpURLConnection);
						}
					}
					return false;
				}
			});
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					Object[] objects = new Object[2];
					HttpURLConnection httpURLConnection = connectUseHttpURLConnection(context, httpUrl, objectArray
							, portLengthArray[count][0] + "-" + portLengthArray[count][1], false);
					objects[0] = httpURLConnection;
					try {
						objects[1] = deployData(httpURLConnection.getInputStream(), httpURLConnection.getContentType());
					} catch (IOException e) {
						e.printStackTrace();
					}
					httpURLConnection.disconnect();
					Message msg = new Message();
					msg.obj = objects;
					handler.sendMessage(msg);
				}
			});
			thread.start();
		}
		
		private boolean isMultiPortDownloaded(){
			int count = 0;
			for(int i=0; i<portStatusArray.length; i++){
				if(portStatusArray[i]){
					count++;
				}
			}
			if(count == portStatusArray.length){
				return true;
			}
			return false;
		}
	}
}