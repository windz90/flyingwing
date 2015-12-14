package com.flyingwing.base.module;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.flyingwing.base.module.database.DataBaseHelper;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

public class UtilsOld {
	
	public static int IO_BUFFER_SIZE = 1024;
	
	public static boolean isConnect(Context ctx) {
		boolean flag = false;
		ConnectivityManager cwjManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cwjManager.getActiveNetworkInfo() != null)
			flag = cwjManager.getActiveNetworkInfo().isAvailable();
		return flag;
	}
	
	public static void checkDBVersion(Context c, String DataBaseName, String DataBaseURL, String txtURL) {
		try {
			List<Map<String, String>> dbVer= new ArrayList<Map<String, String>>();
			
			SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(c);
//			SharedPreferences.Editor newsVer = null;
//			SharedPreferences.Editor notifyVer = null;
			
			String DataBaseVersion = share.getString(DataBaseName, "0");
			
			String dbVerFromTXT = null;
			dbVer = getDBver(c, txtURL);
			for(int i = 0; i < dbVer.size();i++){
				Map<String, String> detail = dbVer.get(i);
				String name = detail.get("dbName");
				if(name.equals(DataBaseName)){
					dbVerFromTXT = detail.get("dbVer");
				}
			}
			
			float dbVersion = Float.parseFloat(DataBaseVersion);
			float dbVerTXT = Float.parseFloat(dbVerFromTXT);
			
			System.out.println("DataBaseName="+DataBaseName+"/dbVersion="+dbVersion+"/dbVerTXT="+dbVerTXT);
			
			if(dbVersion < dbVerTXT){
				loadDataBase(c, DataBaseName, DataBaseURL);
			}
		} catch (Exception e) {
			Log.e("checkDBVersion error:", e.toString());
		}
	}
	
	public static List<Map<String, String>> getDBver(Context c, String txtURL){
		List<Map<String, String>> list=new LinkedList<Map<String, String>>();
		
       try{
    	   if(isConnect(c)){
	            HashMap<String, String> hashMap = null;
	            InputStream ins = new BufferedInputStream(new URL(txtURL).openStream(), IO_BUFFER_SIZE);
	            BufferedReader br = new BufferedReader(new InputStreamReader(ins,"UTF-8"));
	            String inputLine;
	            while ((inputLine = br.readLine()) != null) {
	            	hashMap = new HashMap<String, String>();
	                String[] token = inputLine.split(":");
	                hashMap.put("dbName", token[0]);
	                hashMap.put("dbVer", token[1]);
//	                System.out.println(token[0] + ":" + token[1]);
	                list.add(hashMap);
	            }
    	   }
        }catch(Exception e){
        	Log.e("getDBver",e.toString());
        }
		return list;
	}
	
	public static void loadDataBase(Context c, String DataBaseName, String DataBaseURL){
		try {
			URL dataBaseUrl = new URL(DataBaseURL);
			URLConnection dataBaseConn = dataBaseUrl.openConnection();
			try{
				dataBaseConn.setReadTimeout(10000);
				dataBaseConn.setConnectTimeout(10000);
				dataBaseConn.connect();
			}catch (Exception e) {
				System.out.println("connect Exception " + e);
			}
			
			InputStream dataBaseOnCloud = dataBaseConn.getInputStream();
			String outFileName = DataBaseHelper.DATABASE_TEMP_FILE_PATH + DataBaseName;
			
			OutputStream dataBaseInPhone = new FileOutputStream(outFileName);
	
			byte[] buffer = new byte[1024];
			int length;
			while ((length = dataBaseOnCloud.read(buffer)) > 0) {
				dataBaseInPhone.write(buffer, 0, length);
			}
			dataBaseInPhone.flush();
			dataBaseInPhone.close();
			dataBaseOnCloud.close();
			
			copyDB(c, DataBaseName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyDB(Context c, String DataBaseName) throws IOException {
		c.deleteDatabase(DataBaseName);
		
		InputStream myInput= new FileInputStream(DataBaseHelper.DATABASE_TEMP_FILE_PATH + DataBaseName);
		String outFileName = DataBaseHelper.DATABASE_PATH + DataBaseName;
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		File file = new File(DataBaseHelper.DATABASE_TEMP_FILE_PATH + DataBaseName);
		file.delete();
		
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public static String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
	
   public static Bitmap getUrlBitmap(String url){
        Bitmap bitmap=null;
		try {
			byte[] data = getImageFromURL(url);			
			bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		catch(Exception e){
			
		} 
		return bitmap;
    }
	
    public static Bitmap getUrlBitmapBonus(String streamURL, float wiRatio){
		InputStream in = null;
//		BufferedOutputStream out = null;
		Bitmap bitmap = null;
        
		try{
	    	//fileName=Utils.escapeConvert(fileName);
	    	
	    	System.out.println("" + streamURL);
    		
	        in = new BufferedInputStream(new URL(streamURL).openStream(), IO_BUFFER_SIZE);
//			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
//			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);	        
//			Utils.copy(in, out);
//			out.flush();
			
//			byte[] data = dataStream.toByteArray();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			if(wiRatio >= 1.5){
				options.inSampleSize = 2;
			}else{
				options.inSampleSize = 3;
			}
			bitmap = BitmapFactory.decodeStream(in, null, options);
			in.close();
			in = null;
		}
		catch(Exception e){
			bitmap = null;
			in = null;
			System.out.println(e);
		}
		
		return bitmap;
    }
    
    public static String escapeConvert(String s){
    	if(s.indexOf(" ")!=-1){
    		s=s.replaceAll(" ", "%20");
    	}
    	if(s.indexOf("(")!=-1){
    		s=s.replaceAll("\\(", "%28");
    	}
    	if(s.indexOf(")")!=-1){
    		s=s.replaceAll("\\)", "%29");
    	}
    	return s;
    }
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}
	
    public static String encodeURI(String s){
        String encode="";
        try{
            encode= URLEncoder.encode(s,"UTF-8");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return encode;
    }
    
	public static Map<String, Double> getAddress2latLng(String address) {
		
		Map<String, Double> latLngMap=new HashMap<String, Double>();		
		try {
			JSONObject result=getGeocodingJson(address);
			JSONObject location=result.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");						
			latLngMap.put("lat", location.getDouble("lat"));
			latLngMap.put("lng", location.getDouble("lng"));
		} catch (Exception e) {
			Log.e("getAddress2latLng error", e.toString());
		}
		return latLngMap;
	}    
    
    
	public static JSONObject getGeocodingJson(String address) {
		String jsonurl = "http://maps.google.com/maps/api/geocode/json?address="+UtilsOld.encodeURI(address)+"&sensor=false";
		JSONObject jsonObj=null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(jsonurl);
			//List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("address", Utils.encodeURI(address)));
			//params.add(new BasicNameValuePair("sensor", "false"));
			//httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);

			String resStr = "";
			if (response.getStatusLine().getStatusCode() == 200) {
				resStr = EntityUtils.toString(response.getEntity());				
				jsonObj = new JSONObject(resStr.trim());
				
			} 
			else {
				
				// vtext.setTag("Error State:"+hr.getStatusLine().toString());
			}
			
		} catch (Exception e) {
			Log.e("getGeocodingJson error", e.toString());
		}
		return jsonObj;
	}
	
	public static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	public static void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}	
		
	
	public static PointF changeMarkedMapPoint(PointF screenPoint, float _zoomScale){
		PointF newMapPoint=new PointF();
		float newPointX=(screenPoint.x)/_zoomScale;
		float newPointY=(screenPoint.y)/_zoomScale;
		newMapPoint.set(newPointX, newPointY);
		return newMapPoint;
	}	
	
	public static PointF zoomAfterLeftTopPoint(PointF nowLeftTopPoint, PointF middlePoint, float zoomScale, float newScale){
		PointF newLeftTopPoint=new PointF();
		float newLeftTopX=nowLeftTopPoint.x-(middlePoint.x-nowLeftTopPoint.x)*(newScale-zoomScale)/zoomScale;
		float newLeftTopY=nowLeftTopPoint.y-(middlePoint.y-nowLeftTopPoint.y)*(newScale-zoomScale)/zoomScale;
		newLeftTopPoint.set(newLeftTopX, newLeftTopY);
		return newLeftTopPoint;
	}
	
	public static Criteria getGpsCriteria(){
		Criteria criteria=new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}
	
	public static String getGpsProvider(LocationManager locationManager){
		return locationManager.getBestProvider(getGpsCriteria(), true);
	}
	
	public static Map<String, Double> getGpsLocation(Context cxt){
		Map<String, Double> latLngMap=new HashMap<String, Double>();		
		LocationManager locationManager=(LocationManager)cxt.getSystemService(Context.LOCATION_SERVICE);
		String provider=getGpsProvider(locationManager);
		
		Location location=locationManager.getLastKnownLocation(provider);			
				
		if(location!=null){
			double lat=location.getLatitude();
			double lng=location.getLongitude();
			latLngMap.put("lat", lat);
			latLngMap.put("lng", lng);
		}
		
		return latLngMap;
	}	
	
	public static boolean isGPSEnable(Context context) {
		String str = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (str != null) {
			return str.contains("gps");
		}
		else{
			return false;
		}
	}
	
	public static void GPSswitch(Context cx) {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(cx, 0, gpsIntent, 0).send();
		}catch(CanceledException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isWiFiActive(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static Bitmap getImageFromAssetFile(Activity ac, String fileName){   
		Bitmap image = null;   
		try {
			AssetManager am = ac.getAssets();
			InputStream is1 = am.open(fileName);
			
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = 1;
			
			image = BitmapFactory.decodeStream(is1, null, options);						
			is1.close();
			//am.close();
						
			
		}
		catch(Exception e){   
			Log.e("取出檔案錯誤", e.toString());
		}   
		return image;
	}
	
    public static boolean copyToFile(String strURL, File destFile) {
    	byte[] data = null;
        try {
        	data = UtilsOld.getImageFromURL(strURL);        	
        	return copyToFile(data, destFile);
        } 
        catch (Exception e) {
            return false;
        }
    }
    
    public static boolean copyToFile(byte[] data, File destFile) {    	
    	InputStream inputStream=null;
        try {
        	inputStream=new ByteArrayInputStream(data);        	
        	return copyToFile(inputStream, destFile);
        } 
        catch (Exception e) {
            return false;
        }
    }    
		
    public static boolean copyToFile(InputStream inputStream, File destFile) {    	 
        try {        	
            if (destFile.exists()) {
            	destFile.delete();
            	//return true;                
            }
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }    
	
	public static byte[] getImageFromURL(String urlPath) {
		byte[] data = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlPath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(6000);
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				data = inputStreamToByteArray(is);
			} else {
				data = null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			conn.disconnect();
		}
		return data;
	}
	
	public static byte[] inputStreamToByteArray(InputStream is){
		byte[] byteArray = null;
		if(is != null){
			int progress;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				try {
					while((progress = is.read(buffer)) != -1){
						baos.write(buffer, 0, progress);
					}
					baos.flush();
					byteArray = baos.toByteArray();
				} finally {
					baos.close();
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return byteArray;
	}
}