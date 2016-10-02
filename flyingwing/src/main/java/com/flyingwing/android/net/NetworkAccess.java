/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.4.10
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "UnusedAssignment", "WeakerAccess", "ForLoopReplaceableByForEach", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "ThrowFromFinallyBlock"})
public class NetworkAccess {
	
	public static final int CONNECTION_CONNECT_FAIL = 100;
	public static final int CONNECTION_CONNECTED = 101;
	public static final int CONNECTION_LOAD_FAIL = 102;
	public static final int CONNECTION_LOADED = 103;
	public static final int SPLIT_AUTO_MAX_QUANTITY = 0;
	public static final int SPLIT_BY_QUANTITY = 1;
	public static final int SPLIT_BY_LENGTH = 2;
	private static final String ONLY_READ_HEADER = "header";
	private static final String REQUEST_GET = "GET";
	private static final String REQUEST_PUT = "PUT";
	private static final String REQUEST_POST = "POST";
	private static final String REQUEST_DELETE = "DELETE";
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
	 * @param isSkipDataRead 若為true，完成連線後須調用ConnectionResult.disconnect()斷開連線
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, boolean isSkipDataRead, Handler handler){
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		if(isAvailable(context)){
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
	 * @param isSkipDataRead 若為true，完成連線後須調用ConnectionResult.disconnect()斷開連線
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, List<Map<String, Object>> contentList
			, boolean isSkipDataRead, Handler handler){
		Object[][] objectArray = new Object[contentList.size()][3];
		Map<String, Object> map;
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
	 */
	public static ConnectionResult connectUseHttpURLConnection(Context context, String httpUrl, List<Map<String, Object>> contentList
			, Handler handler){
		return connectUseHttpURLConnection(context, httpUrl, contentList, false, handler);
	}
	
	/**
	 * @param isSkipDataRead 若為true，完成連線後須調用HttpURLConnection.disconnect()斷開連線
	 */
	public static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, boolean isSkipDataRead){
		if(isAvailable(context)){
			return connectUseHttpURLConnection(context, httpUrl, objectArray, null, isSkipDataRead, null, null);
		}
		return null;
	}
	
	public static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray){
		return connectUseHttpURLConnection(context, httpUrl, objectArray, false);
	}
	
	/**
	 * HttpURLConnection MultiPort
	 * @param requestRangeIndex 連線後要求回傳的內容區間
	 * @param isSkipDataRead 若為true，完成連線後須調用HttpURLConnection.disconnect()斷開連線
	 */
	private static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, String requestRangeIndex, boolean isSkipDataRead){
		if(isAvailable(context)){
			return connectUseHttpURLConnection(context, httpUrl, objectArray, requestRangeIndex, isSkipDataRead, null, null);
		}
		return null;
	}
	
	/**
	 * @param requestRangeIndex 連線後要求回傳的內容區間
	 */
	private static HttpURLConnection connectUseHttpURLConnection(Context context, String httpUrl, Object[][] objectArray
			, String requestRangeIndex, boolean isSkipDataRead, ConnectionResult connectionResult, Handler handler){
		HttpURLConnection httpURLConnection = connectUseHttpURLConnectionImplementRequest(httpUrl, objectArray, requestRangeIndex);
		if(httpURLConnection == null || connectionResult == null){
			if(connectionResult != null){
				connectionResult.setStatusMessage("Connecting, Connect Fail Exception");
			}
			return httpURLConnection;
		}
		
		connectionResult.setConnectUrl(httpURLConnection.getURL().getPath());
		connectionResult.setRequestType(httpURLConnection.getRequestMethod());
		InputStream inputStreamError = httpURLConnection.getErrorStream();
		if(inputStreamError != null){
			connectionResult.setStatusMessage(inputStreamToString(inputStreamError, null, NETWORKSETTING.mBufferSize));
			
			if(handler != null){
				Message msg = new Message();
				msg.what = CONNECTION_CONNECT_FAIL;
				msg.obj = connectionResult;
				handler.sendMessage(msg);
			}
			return httpURLConnection;
		}
		try {
			connectionResult.setResponseCode(httpURLConnection.getResponseCode());
			connectionResult.setResponseMessage(httpURLConnection.getResponseMessage());
			if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK && 
					httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
				printInfo("Connect Fail StatusCode " + httpURLConnection.getResponseCode()
						, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
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
			printInfo("Connect, Exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
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
				connectionResult.setHttpURLConnection(httpURLConnection);
				connectionResult.setContent(httpURLConnection.getInputStream());
				return httpURLConnection;
			}
			deployDataForConnectionResult(httpURLConnection.getInputStream(), connectionResult);
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("LoadData, Exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
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
			
			if(httpURLConnection != null){
				httpURLConnection.connect();
			}
			return httpURLConnection;
//		} catch (MalformedURLException e) {
//		} catch (IOException e) {
		} catch (Exception e) {
			printInfo("Connecting, Exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
		}finally{
			printInfo(requestType + ", " + httpUrl, NetworkAccess.NETWORKSETTING.mIsPrintConnectionUrl);
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
			httpURLConnection.setConnectTimeout(NETWORKSETTING.mConnectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.mReadTimeout);
			httpURLConnection.setChunkedStreamingMode(NETWORKSETTING.mBufferSize);
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
			printInfo("HttpGetSetting, Exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
		}
		return null;
	}
	
	private static HttpURLConnection useHttpURLConnectionPost(URL httpUrl, Object[][] objectArray, String requestRangeIndex){
		String hyphens = "--";
		String boundary = "#!#!#!BOUNDARY!#!#!#";
		String breakLine = "\r\n";
		try {
			// System.setProperty() for APP
			System.setProperty("http.keepAlive", "true");
			HttpURLConnection httpURLConnection = (HttpURLConnection)httpUrl.openConnection();
			httpURLConnection.setRequestMethod(REQUEST_POST);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setConnectTimeout(NETWORKSETTING.mConnectTimeout);
			httpURLConnection.setReadTimeout(NETWORKSETTING.mReadTimeout);
			httpURLConnection.setChunkedStreamingMode(NETWORKSETTING.mBufferSize);
			/*
			 * HTTP/1.0 預設不保持連線，Header add Connection: Keep-Alive field 表示保持連線
			 * HTTP/1.1 預設保持連線，Header add Connection: Close field 表示不保持連線
			 */
			// HttpURLConnection.setRequestProperty() only for URLConnection
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
							" name=\"" + objectArray[i][0] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);
					
					dataOutputStream.write(((String)objectArray[i][1]).getBytes(charsetName));
				}else if(objectArray[i].length == 4){
					dataOutputStream.write(("Content-Disposition: form-data;" + 
							" name=\"" + objectArray[i][0] + "\";" + 
							" filename=\"" + objectArray[i][1] + "\"").getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.write(("Content-Type: " + objectArray[i][2]).getBytes(charsetName));
					dataOutputStream.writeBytes(breakLine);
					dataOutputStream.writeBytes(breakLine);
					
					if(objectArray[i][3] != null && objectArray[i][3] instanceof InputStream){
						InputStream is = (InputStream)objectArray[i][3];
						int progress;
						byte[] buffer = new byte[NETWORKSETTING.mBufferSize];
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
			printInfo("HttpPostSetting, Exception " + e, NetworkAccess.NETWORKSETTING.mIsPrintConnectException);
		}
		return null;
	}
	
	private static Object deployData(InputStream is, String contentType){
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
	
	private static void deployDataForConnectionResult(InputStream is, ConnectionResult connectionResult){
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
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			objectArray = null;
			e.printStackTrace();
		}
		return object;
	}
	
	public static boolean isAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
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
	
	public static class MultiPortManager {
		
		private boolean[] portStatusArray;
		private long[][] portLengthArray;
		private Object[] portContentArray;
		
		public interface MultiPortDownLoadComplete {
			void connectFail(HttpURLConnection httpURLConnection);
			void connected(HttpURLConnection httpURLConnection);
			void loadFail(HttpURLConnection httpURLConnection);
			void loading(HttpURLConnection httpURLConnection, int count, Object objectPort);
			void loadExpired(HttpURLConnection httpURLConnection, int count);
			void loaded(HttpURLConnection httpURLConnection, int sum, Object object);
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
						multiPortComplete.loadExpired(null, count);
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
					if(httpURLConnection == null){
						return;
					}
					
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
			return count == portStatusArray.length;
		}
	}
}