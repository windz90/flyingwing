/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.net;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings("unused")
public class NetworkUtils {

	// 取得手機Wifi網路卡的MAC值
	public static String getWifiMAC(@NonNull Context context){
		if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){
			WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if(wifiInfo != null){
				return wifiInfo.getMacAddress();
			}
		}
		return null;
	}

	/**
	 * 內含BroadcastReceiver，建議於Activity流程內unregisterReceiver。
	 */
	public static void setWifiEnabled(@NonNull final Context context, final boolean isEnable, final String wifiStateText, @NonNull final Handler handler){
		final WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager.isWifiEnabled() == isEnable){
			handler.sendEmptyMessage(1);
			return;
		}
		if(context.checkCallingPermission(android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			return;
		}

		if(!TextUtils.isEmpty(wifiStateText)){
			Toast.makeText(context, wifiStateText, Toast.LENGTH_LONG).show();
		}
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if((isEnable && intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0) == WifiManager.WIFI_STATE_ENABLED) || 
						(!isEnable && intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0) == WifiManager.WIFI_STATE_DISABLED)){
					context.unregisterReceiver(this);
					handler.sendEmptyMessage(1);
				}
			}
		}, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		new Thread(new Runnable() {
			@Override
			public void run() {
				// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
				wifiManager.setWifiEnabled(isEnable);
			}
		}).start();
	}

	public static boolean isAvailable(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	public static boolean isConnected(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	/**@deprecated */
	public static boolean isAnyAvailable(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//noinspection deprecation
		NetworkInfo[] networkInfoArray = connectivityManager.getAllNetworkInfo();
		if(networkInfoArray != null){
			for(NetworkInfo networkInfo : networkInfoArray){
				if(networkInfo.isAvailable()){
					return true;
				}
			}
		}
		return false;
	}

	/**@deprecated */
	public static boolean isConnectedSelectedType(@NonNull Context context, int connectivityManagerType) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//noinspection deprecation
		NetworkInfo networkInfo = connectivityManager.getNetworkInfo(connectivityManagerType);
		return networkInfo != null && networkInfo.isAvailable();
	}

	/**@deprecated */
	@SuppressWarnings("deprecation")
	public static boolean isConnectedMobileNetwork(@NonNull Context context) {
		return isConnectedSelectedType(context, ConnectivityManager.TYPE_MOBILE);
	}

	/**@deprecated */
	@SuppressWarnings("deprecation")
	public static boolean isConnectedWifi(@NonNull Context context) {
		return isConnectedSelectedType(context, ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * 內含BroadcastReceiver，建議於Activity流程內unregisterReceiver。
	 */
	public static void wifiScan(@NonNull final Context context, @NonNull final Handler handler){
		final WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager.isWifiEnabled() || 
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED || 
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED || 
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || 
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Message msg = handler.obtainMessage();
					// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
					msg.obj = wifiManager.getScanResults();
					context.unregisterReceiver(this);
					handler.sendMessage(msg);
				}
			}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
			wifiManager.startScan();
		}
	}

	public static boolean wifiConnect(@NonNull Context context, @NonNull String ssId, String bssId, String password, boolean isUseExistWifiConfiguration){
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if(!wifiManager.isWifiEnabled() ||
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED ||
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			return false;
		}

		if(ssId.charAt(0) != '"'){
			ssId = "\"" + ssId;
		}
		if(ssId.charAt(ssId.length() - 1) != '"'){
			ssId = ssId + "\"";
		}
		WifiConfiguration wifiConfiguration;
		boolean isEnable;
		if(isUseExistWifiConfiguration){
			// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
			List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
			if(list != null){
				int size = list.size();
				for(int i=0; i<size; i++){
					wifiConfiguration = list.get(i);
					if(wifiConfiguration.SSID.equals(ssId)){
						wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
						// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
						wifiManager.disconnect();
						isEnable = wifiManager.enableNetwork(wifiConfiguration.networkId, true);
						return isEnable && wifiManager.reconnect();
					}
				}
			}
		}

		wifiConfiguration = new WifiConfiguration();
		wifiConfiguration.SSID = ssId;
		if(!TextUtils.isEmpty(bssId)){
			wifiConfiguration.BSSID = bssId;
		}
		if(!TextUtils.isEmpty(password)){
			wifiConfiguration.preSharedKey = "\"" + password + "\"";
		}
		wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
		// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
		wifiManager.disconnect();
		isEnable = wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfiguration), true);
		wifiManager.saveConfiguration();
		return isEnable && wifiManager.reconnect();
	}

	public static boolean wifiConnect(@NonNull Context context, @NonNull ScanResult scanResult, String password, boolean isUseExistWifiConfiguration){
		return wifiConnect(context, scanResult.SSID, scanResult.BSSID, password, isUseExistWifiConfiguration);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static String getLocalIpAddress(){
		NetworkInterface networkInterface;
		InetAddress inetAddress;
		try {
			for(Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();){
				networkInterface = enumeration.nextElement();
				if(networkInterface.isLoopback()){
					continue;
				}
				for(Enumeration<InetAddress> enumerationInetAddress = networkInterface.getInetAddresses(); enumerationInetAddress.hasMoreElements();){
					inetAddress = enumerationInetAddress.nextElement();
					if(inetAddress != null && !inetAddress.isLoopbackAddress()){
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static InetAddress getBroadcastAddressFromNetworkInterface(){
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		NetworkInterface networkInterface;
		InetAddress inetAddress;
		try {
			for(Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();){
				networkInterface = enumeration.nextElement();
				if(networkInterface.isLoopback()){
					continue;
				}
				for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){
					inetAddress = interfaceAddress.getBroadcast();
					if(inetAddress != null){
						return inetAddress;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InetAddress getBroadcastAddressFromMask(@NonNull Context context){
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if(!wifiManager.isWifiEnabled() || 
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			return null;
		}

		// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		if(dhcpInfo != null){
			int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
			byte[] quads = new byte[4];
			for(int i=0; i<quads.length; i++){
				quads[i] = (byte)((broadcast >> i * 8) & 0xFF);
			}
			try {
				return InetAddress.getByAddress(quads);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}