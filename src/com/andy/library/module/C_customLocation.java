package com.andy.library.module;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 2.1.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_customLocation implements LocationListener {
	
	public static final int NETWORK = 10;
	public static final int GPS = 11;
	public static final int BEST = 12;
	
	public boolean autoPointCenter;
	public String provider, nowAddress = "正在解析目前地址資訊，請稍後再次點擊";
	
	private String className;
	private Context context;
	private LocationManager locationManager;
	private MapController mapController;
	private Runnable onFirstFix, onFailFix, geocodeCompletedNextAction;
	private List<Address> addressList;
	private GeoPoint geoPoint;
	private double longit, latit;
	
	public C_customLocation(Context context, MapController mapController){
		this.context = context;
		className = context.getClass().getSimpleName();
		this.mapController = mapController;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(!provider.equals(LocationManager.GPS_PROVIDER) && isGPSEnable(context)){
			GPSswitch(context);
			System.out.println("gps close");
		}
		getLocation(location);
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		System.out.println(className + " onProviderDisabled");
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		System.out.println(className + " onProviderEnabled");
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		System.out.println(className + " onStatusChanged " + provider + " " + status);
	}
	
	public void enableLocation(int providerPlan, long minTime, float minDistance){
		Location location;
		try{
			// 選擇定位模式
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			switch (providerPlan) {
			case NETWORK:
				provider = LocationManager.NETWORK_PROVIDER;
				location = locationManager.getLastKnownLocation(provider);
				break;
			case GPS:
				provider = LocationManager.GPS_PROVIDER;
				location = locationManager.getLastKnownLocation(provider);
				if(location == null){
					providerPlan = NETWORK;
					provider = LocationManager.NETWORK_PROVIDER;
					location = locationManager.getLastKnownLocation(provider);
				}
				break;
			case BEST:
			default:
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setAltitudeRequired(true);
				criteria.setBearingRequired(true);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				provider = locationManager.getBestProvider(criteria, true);
				location = locationManager.getLastKnownLocation(provider);
				break;
			}
			
			if(!provider.equals(LocationManager.GPS_PROVIDER) && isGPSEnable(context)){
				GPSswitch(context);
				System.out.println("gps close");
			}
			
			if(location != null){
				if(geoPoint == null){
					// 首次解析緯經度及定位
					latit = location.getLatitude();
					longit = location.getLongitude();
					geoPoint = new GeoPoint((int)(latit * 1E6), (int)(longit * 1E6));
					if(onFirstFix != null){
						Thread thread = new Thread(onFirstFix);
						thread.start();
					}
				}
				
				getLocation(location);
				// 啟動定位追蹤器
				locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
				return;
			}
			
			if(onFailFix != null){
				Thread thread = new Thread(onFailFix);
				thread.start();
				onFailFix = null;
			}
			
			provider = null;
			System.out.println(className + " 定位失敗");
			Toast.makeText(context, "定位失敗", Toast.LENGTH_SHORT).show();
		}catch (Exception e) {
			System.out.println(className + " 定位失敗1 " + e);
			Toast.makeText(context, "定位失敗", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void disableLocation(){
		if(provider != null){
			if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
					locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				try{
					locationManager.removeUpdates(this);
					provider = null;
					System.out.println(className + " 已移除定位追蹤器");
				}catch (Exception e) {
					System.out.println(className + " 移除定位追蹤器失敗 " + e);
				}
			}else{
				System.out.println(className + " 並未啟動定位追蹤器，無須移除");
			}
		}
	}
	
	public void getLocation(final Location location){
		try{
			// 解析緯經度及定位
			latit = location.getLatitude();
			longit = location.getLongitude();
			geoPoint = new GeoPoint((int)(latit * 1E6), (int)(longit * 1E6));
			
			if(autoPointCenter == true && mapController != null){
				mapController.animateTo(geoPoint);
			}
//			System.out.println("getLocation " + provider + " " + latit + "," + longit);
			
			final Handler handler = new Handler(new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					Thread thread = new Thread(geocodeCompletedNextAction);
					thread.start();
					return false;
				}
			});
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try{
						// 解析地址
						Geocoder geocoder = new Geocoder(context);
						addressList = geocoder.getFromLocation(latit, longit, 5);
						if(addressList.size() > 0){
							Address address = addressList.get(0);
							nowAddress = address.getAddressLine(0);
						}else{
							nowAddress = "找不到目前地址資訊";
						}
//						System.out.println("getLocation " + provider + " " + nowAddress);
					}catch (Exception e) {
						nowAddress = "無法取得目前地址資訊";
						System.out.println(className + " 無法取得目前位置2 " + e);
					}
					if(geocodeCompletedNextAction != null){
						handler.sendEmptyMessage(0);
					}
				}
			});
			thread.start();
		}catch (Exception e) {
			System.out.println(className + " 經緯度解析失敗2 " + e);
			Toast.makeText(context, "經緯度解析失敗", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setOnFirstFix(Runnable onFirstFix){
		this.onFirstFix = onFirstFix;
	}
	
	public void setOnFailFix(Runnable onFailFix){
		this.onFailFix = onFailFix;
	}
	
	public void setGeocodeCompletedNextAction(Runnable geocodeCompletedNextAction){
		this.geocodeCompletedNextAction = geocodeCompletedNextAction;
	}
	
	public List<Address> getAddressList(){
		return addressList;
	}
	
	public GeoPoint getGeoPoint(){
		return geoPoint;
	}
	
	public double getLatitude(){
		return latit;
	}
	
	public double getLongitude(){
		return longit;
	}
	
	public static boolean isGPSEnable(Context context) {
		String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider != null && provider.contains(LocationManager.GPS_PROVIDER)){
			return true;
		}
		return false;
	}
	
	public static void GPSswitch(Context context) {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
		}catch(CanceledException e) {
			System.out.println("GPS開關失敗 CanceledException " + e);
		}
	}
	
	// 計算兩點距離API版
	public static double getDistance1(double lat1, double lon1, double lat2, double lon2) {
		float[] results=new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
	
	// 計算兩點距離簡算版
	public static double getDistance2(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
					+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
					* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		double miles = dist * 60 * 1.1515;
		return miles;
	}
	// 將角度轉換為弧度
	public static double deg2rad(double degree) {
		return degree / 180 * Math.PI;
	}
	// 將弧度轉換為角度
	public static double rad2deg(double radian) {
		return radian * 180 / Math.PI;
	}
}