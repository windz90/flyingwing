/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.1.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach", "Convert2Diamond", "UnusedReturnValue"})
public class NetworkUtils {

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

	@RequiresApi(api = Build.VERSION_CODES.M)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAvailableByInternetForStrict(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		Network network = connectivityManager.getActiveNetwork();
		if(network == null){
			return false;
		}
		NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
		return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyAvailableByInternet(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			Network[] networks = connectivityManager.getAllNetworks();
			NetworkCapabilities networkCapabilities;
			for(int i=0; i<networks.length; i++){
				networkCapabilities = connectivityManager.getNetworkCapabilities(networks[i]);
				if(networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
					return true;
				}
			}
			NetworkInfo networkInfo;
			for(int i=0; i<networks.length; i++){
				networkInfo = connectivityManager.getNetworkInfo(networks[i]);
				if(networkInfo != null && networkInfo.isAvailable()){
					return true;
				}
			}
			return false;
		}
		NetworkInfo[] networkInfoArray = connectivityManager.getAllNetworkInfo();
		for(int i=0; i<networkInfoArray.length; i++){
			if(networkInfoArray[i].isAvailable()){
				return true;
			}
		}
		return false;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyAvailableByInternetForStrict(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		Network[] networks = connectivityManager.getAllNetworks();
		NetworkCapabilities networkCapabilities;
		for(int i=0; i<networks.length; i++){
			networkCapabilities = connectivityManager.getNetworkCapabilities(networks[i]);
			if(networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)){
				return true;
			}
		}
		return false;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedByInternet(@NonNull Context context){
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
			return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyConnectedByInternet(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		NetworkCapabilities networkCapabilities;
		Network[] networks = connectivityManager.getAllNetworks();
		for(int i=0; i<networks.length; i++){
			networkCapabilities = connectivityManager.getNetworkCapabilities(networks[i]);
			if(networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
					|| networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))){
				return true;
			}
		}
		return false;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedByNetworkType(@NonNull Context context, int networkType) {
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
			return networkCapabilities != null && networkCapabilities.hasTransport(networkType);
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == networkType;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedByMobileNetwork(@NonNull Context context) {
		return isConnectedByNetworkType(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? NetworkCapabilities.TRANSPORT_CELLULAR : ConnectivityManager.TYPE_MOBILE);
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedByWifi(@NonNull Context context) {
		return isConnectedByNetworkType(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? NetworkCapabilities.TRANSPORT_WIFI : ConnectivityManager.TYPE_WIFI);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyConnectedByNetworkType(@NonNull Context context, int networkType) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		NetworkCapabilities networkCapabilities;
		Network[] networks = connectivityManager.getAllNetworks();
		for(int i=0; i<networks.length; i++){
			networkCapabilities = connectivityManager.getNetworkCapabilities(networks[i]);
			if(networkCapabilities != null && networkCapabilities.hasTransport(networkType)){
				return true;
			}
		}
		return false;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyConnectedByMobileNetwork(@NonNull Context context) {
		return isAnyConnectedByNetworkType(context, NetworkCapabilities.TRANSPORT_CELLULAR);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAnyConnectedByWifi(@NonNull Context context) {
		return isAnyConnectedByNetworkType(context, NetworkCapabilities.TRANSPORT_WIFI);
	}

	/**
	 * @return if connect speed 400 kbps up
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedFastInternet(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		NetworkInfo networkInfo;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			Network network = connectivityManager.getActiveNetwork();
			if(network == null){
				return false;
			}
			NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
			if(networkCapabilities == null){
				return false;
			}
			if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
				return true;
			}
			if(!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
				return false;
			}
			networkInfo = connectivityManager.getNetworkInfo(network);
			if(networkInfo == null){
				return false;
			}
		}else{
			networkInfo = connectivityManager.getActiveNetworkInfo();
			if(networkInfo == null || !networkInfo.isConnected()){
				return false;
			}
			int type = networkInfo.getType();
			switch(type){
				case ConnectivityManager.TYPE_WIFI:
				case ConnectivityManager.TYPE_ETHERNET:
					return true;
			}
			if(type != ConnectivityManager.TYPE_MOBILE){
				return false;
			}
		}
		switch(networkInfo.getSubtype()){
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			// 2G
			case TelephonyManager.NETWORK_TYPE_GSM:
			case TelephonyManager.NETWORK_TYPE_IDEN: // ~25 kbps, API level 8.
			case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
				return false;
			// 3G
			case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2 Mbps, API level 11.
			case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps, API level 9.
			case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20 Mbps, API level 13.
			case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
			// 4G
			case TelephonyManager.NETWORK_TYPE_LTE: // ~ 10+ Mbps, API level 11.
			case TelephonyManager.NETWORK_TYPE_IWLAN:
			default:
				return true;
		}
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE<br>
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link WifiManager#getConnectionInfo()})
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static WifiInfo getWifiConnectionInfo(@NonNull Context context, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.ACCESS_WIFI_STATE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}

		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null){
			return null;
		}
		// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
		// For Android 10 (API level 29) and higher
		// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
		return wifiManager.getConnectionInfo();
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE
	 */
	@SuppressLint("HardwareIds")
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static String getWifiMAC(@NonNull Context context, Handler handlerNoPermissions){
		WifiInfo wifiInfo = getWifiConnectionInfo(context, handlerNoPermissions);
		if(wifiInfo == null){
			return null;
		}
		return wifiInfo.getMacAddress();
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static String getWifiIP(@NonNull Context context, Handler handlerNoPermissions){
		WifiInfo wifiInfo = getWifiConnectionInfo(context, handlerNoPermissions);
		if(wifiInfo == null){
			return null;
		}
		int ipAddress = wifiInfo.getIpAddress();
		return String.format(Locale.getDefault(), "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static DhcpInfo getWifiDHCPInfo(@NonNull Context context, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.ACCESS_WIFI_STATE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}

		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null){
			return null;
		}
		// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
		return wifiManager.getDhcpInfo();
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static String getWifiGatewayIP(@NonNull Context context, Handler handlerNoPermissions){
		DhcpInfo dhcpInfo = getWifiDHCPInfo(context, handlerNoPermissions);
		if(dhcpInfo == null){
			return null;
		}
		int ipAddress = dhcpInfo.gateway;
		return String.format(Locale.getDefault(), "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}

	public static boolean isWifiEnabled(@NonNull Context context) {
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		return wifiManager != null && wifiManager.isWifiEnabled();
	}

	/**
	 * android.permission.CHANGE_WIFI_STATE<br>
	 * {@link IntentFilter#addAction(String)}, {@link WifiManager#WIFI_STATE_CHANGED_ACTION}<br>
	 * {@link WifiManager#EXTRA_WIFI_STATE}<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * Restriction on enabling and disabling Wi-Fi<br>
	 * {@link WifiManager#setWifiEnabled(boolean)}<br>
	 * This method was deprecated in API level 29. The method always returns false.<br>
	 */
	@RequiresPermission(android.Manifest.permission.CHANGE_WIFI_STATE)
	public static void setWifiEnabled(@NonNull Context context, final boolean isEnable, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.CHANGE_WIFI_STATE};
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int extra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
				if((isEnable && extra == WifiManager.WIFI_STATE_ENABLED) || (!isEnable && extra == WifiManager.WIFI_STATE_DISABLED)){
					context.getApplicationContext().unregisterReceiver(this);
				}
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		}
		final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null){
			return;
		}
		new Thread(new Runnable() {
			@RequiresPermission(android.Manifest.permission.CHANGE_WIFI_STATE)
			@Override
			public void run() {
				// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
				// This method was deprecated in API level 29
				wifiManager.setWifiEnabled(isEnable);
			}
		}).start();
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE ({@link WifiManager#getScanResults()})<br>
	 * android.permission.CHANGE_WIFI_STATE<br>
	 * android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION ({@link WifiManager#getScanResults()})<br>
	 * {@link IntentFilter#addAction(String)}, {@link WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}<br><br>
	 *
	 * {@link WifiManager#startScan()}<br>
	 * This method was deprecated in Android 9 (API level 28)<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link WifiManager#getScanResults()}, {@link WifiManager#startScan()})
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE})
	public static void wifiScan(@NonNull final Context context, final BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null || !wifiManager.isWifiEnabled()){
			return;
		}
		List<String> list = new ArrayList<String>(4);
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.ACCESS_WIFI_STATE);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.CHANGE_WIFI_STATE);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
			if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
				list.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
			}else if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
				list.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
			}
		}
		if(list.size() > 0){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = list.toArray(new String[0]);
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				context.getApplicationContext().unregisterReceiver(this);
				// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
				// <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> or
				// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
				// For Android 10 (API level 29) and higher
				// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
				List<ScanResult> list = wifiManager.getScanResults();
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
		// <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
		// This method was deprecated in Android 9 (API level 28)
		// For Android 10 (API level 29) and higher
		// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
		wifiManager.startScan();
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE<br>
	 * android.permission.CHANGE_WIFI_STATE<br>
	 * {@link IntentFilter#addAction(String)}, {@link WifiManager#NETWORK_STATE_CHANGED_ACTION}, {@link ConnectivityManager#CONNECTIVITY_ACTION}<br>
	 * NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link WifiManager#getConfiguredNetworks()})<br>
	 * Restrictions on direct access to configured Wi-Fi networks<br>
	 * The {@link WifiManager#getConfiguredNetworks()} method always returns an empty list.<br>
	 * Note: If a carrier app calls {@link WifiManager#getConfiguredNetworks()}, the system returns a list containing only the networks that the carrier configured.
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION}, conditional = true)
	public static boolean wifiConnect(@NonNull Context context, @NonNull String ssId, String bssId, String password, boolean isUseExistWifiConfiguration
			, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null || !wifiManager.isWifiEnabled()){
			return false;
		}
		List<String> list = new ArrayList<String>(2);
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.ACCESS_WIFI_STATE);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.CHANGE_WIFI_STATE);
		}
		if(list.size() > 0){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = list.toArray(new String[0]);
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}

		/*
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (networkInfo.isConnected()) {
					context.getApplicationContext().unregisterReceiver(this);
				}
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));// ConnectivityManager.CONNECTIVITY_ACTION
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
			// For Android 10 (API level 29) and higher
			// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
			// Restrictions on direct access to configured Wi-Fi networks
			// The getConfiguredNetworks() method always returns an empty list.
			// Note: If a carrier app calls getConfiguredNetworks(), the system returns a list containing only the networks that the carrier configured.
			List<WifiConfiguration> listWifiConfiguration = wifiManager.getConfiguredNetworks();
			if(listWifiConfiguration != null){
				int size = listWifiConfiguration.size();
				for(int i=0; i<size; i++){
					wifiConfiguration = listWifiConfiguration.get(i);
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

	@RequiresPermission(allOf = {android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION}, conditional = true)
	public static boolean wifiConnect(@NonNull Context context, @NonNull ScanResult scanResult, String password, boolean isUseExistWifiConfiguration
			, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		return wifiConnect(context, scanResult.SSID, scanResult.BSSID, password, isUseExistWifiConfiguration, broadcastReceiver, handlerNoPermissions);
	}

	@Nullable
	public static BluetoothAdapter getBluetoothAdapter(Context context){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
			BluetoothManager bluetoothManager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
			if(bluetoothManager == null){
				return null;
			}
			return bluetoothManager.getAdapter();
		}else{
			return BluetoothAdapter.getDefaultAdapter();
		}
	}

	public static String getBluetoothMAC(@NonNull Context context){
		return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), "bluetooth_address");
	}

	public static boolean isBluetoothSupported(@NonNull Context context){
		return context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static boolean isBluetoothLeSupported(@NonNull Context context){
		return context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}

	public static boolean isBluetoothSupported2(@NonNull Context context){
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		return bluetoothAdapter != null;
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public static boolean isBluetoothEnabled(@NonNull Context context, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.BLUETOOTH};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}

		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN (BluetoothAdapter.disable())<br>
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothAdapter#ACTION_STATE_CHANGED}<br>
	 * {@link BluetoothAdapter#EXTRA_STATE}<br>
	 * {@link BluetoothAdapter#EXTRA_PREVIOUS_STATE}
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void setBluetoothEnabled(@NonNull Context context, boolean isEnable, Integer requestCode, BroadcastReceiver broadcastReceiver
			, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.BLUETOOTH};
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}
		final BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter == null || bluetoothAdapter.isEnabled() == isEnable){
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// BluetoothAdapter.EXTRA_PREVIOUS_STATE
				int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
				if((isEnable && extra == BluetoothAdapter.STATE_ON) || (!isEnable && extra == BluetoothAdapter.STATE_OFF)){
					context.getApplicationContext().unregisterReceiver(this);
				}
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}
		if(isEnable){
			Intent intent = new Intent();
			intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			if(requestCode != null && context instanceof Activity){
				((Activity)context).startActivityForResult(intent, requestCode);
			}else{
				context.startActivity(intent);
			}
		}else{
			new Thread(new Runnable() {
				@RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
				@Override
				public void run() {
					// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
					bluetoothAdapter.disable();
				}
			}).start();
		}
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN (BluetoothAdapter.disable())<br>
	 * {@link BluetoothAdapter#EXTRA_STATE}<br>
	 * {@link BluetoothAdapter#EXTRA_PREVIOUS_STATE}
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void setBluetoothEnabled(@NonNull Activity activity, boolean isEnable, int requestCode, Handler handlerNoPermissions){
		setBluetoothEnabled(activity, isEnable, requestCode, null, handlerNoPermissions);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN (BluetoothAdapter.disable())<br>
	 * {@link BluetoothAdapter#EXTRA_STATE}<br>
	 * {@link BluetoothAdapter#EXTRA_PREVIOUS_STATE}
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void setBluetoothEnabled(@NonNull Context context, boolean isEnable, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		setBluetoothEnabled(context, isEnable, null, broadcastReceiver, handlerNoPermissions);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothAdapter#ACTION_STATE_CHANGED}<br>
	 * {@link BluetoothAdapter#EXTRA_STATE}<br>
	 * {@link BluetoothAdapter#EXTRA_PREVIOUS_STATE}
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void setBluetoothEnabledForQuiet(Context context, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.BLUETOOTH};
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}
		final BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter == null || bluetoothAdapter.isEnabled()){
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// BluetoothAdapter.EXTRA_PREVIOUS_STATE
				int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
				if((isEnable && extra == BluetoothAdapter.STATE_ON) || (!isEnable && extra == BluetoothAdapter.STATE_OFF)){
					context.getApplicationContext().unregisterReceiver(this);
				}
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}
		new Thread(new Runnable() {
			@RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
			@Override
			public void run() {
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothAdapter.enable();
			}
		}).start();
	}

	/**
	 * For Android 5.0 (API level 21) and higher<br>
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION ({@link BluetoothLeScanner#startScan(ScanCallback)})<br>
	 * BluetoothDevice bluetoothDevice = result.getDevice()<br>
	 * BluetoothLeScanner.stopScan(scanCallback)
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void bluetoothLeScan(@NonNull Context context, boolean isStartScan, ScanCallback scanCallback, Handler handlerNoPermissions){
		List<String> list = new ArrayList<String>(4);
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH_ADMIN);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
			if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
				list.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
			}else if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
				list.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
			}
		}
		if(list.size() > 0){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = list.toArray(new String[0]);
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}

		/*
		new ScanCallback(){
			@Override
			public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
				super.onScanResult(callbackType, result);
				BluetoothDevice bluetoothDevice = result.getDevice();
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothLeScanner.stopScan(scanCallback);
			}

			@Override
			public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
				super.onBatchScanResults(results);
				int size = results.size();
				BluetoothDevice bluetoothDevice;
				for(int i=0; i<size; i++){
					bluetoothDevice = results.get(i).getDevice();
				}
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothLeScanner.stopScan(scanCallback);
			}

			@Override
			public void onScanFailed(int errorCode) {
				super.onScanFailed(errorCode);
			}
		};
		*/
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
			BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
			if(bluetoothLeScanner != null){
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothLeScanner.stopScan(scanCallback);
				if(isStartScan){
					// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
					// <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> or
					// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
					bluetoothLeScanner.startScan(scanCallback);
				}
			}
		}
	}

	/**
	 * For Android 4.3 (API level 18) and higher to 5.0 (API level 21) below<br>
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * BluetoothAdapter.stopLeScan(leScanCallback)<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link BluetoothAdapter#startLeScan(BluetoothAdapter.LeScanCallback)})
	 */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static boolean bluetoothLeScan(@NonNull Context context, boolean isStartScan, BluetoothAdapter.LeScanCallback leScanCallback, Handler handlerNoPermissions){
		List<String> list = new ArrayList<String>(2);
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH_ADMIN);
		}
		if(list.size() > 0){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = list.toArray(new String[0]);
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}

		/*
		new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothAdapter.stopLeScan(leScanCallback);
			}
		};
		*/
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
			// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
			bluetoothAdapter.stopLeScan(leScanCallback);
			// For Android 10 (API level 29) and higher
			// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
			return isStartScan && bluetoothAdapter.startLeScan(leScanCallback);
		}
		return false;
	}

	/**
	 * For Android 4.3 (API level 18) and higher<br>
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * BluetoothAdapter.stopLeScan(leScanCallback)<br><br>
	 *
	 * For Android 5.0 (API level 21) and higher<br>
	 * android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION ({@link BluetoothLeScanner#startScan(ScanCallback)})<br>
	 * BluetoothDevice bluetoothDevice = result.getDevice()<br>
	 * BluetoothLeScanner.stopScan(scanCallback)<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link BluetoothAdapter#startLeScan(BluetoothAdapter.LeScanCallback)})
	 */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN})
	public static void bluetoothLeScan(@NonNull Context context, boolean isStartScan, ScanCallback scanCallback, BluetoothAdapter.LeScanCallback leScanCallback
			, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			if(scanCallback != null){
				bluetoothLeScan(context, isStartScan, scanCallback, handlerNoPermissions);
			}
		}else{
			if(leScanCallback != null){
				bluetoothLeScan(context, isStartScan, leScanCallback, handlerNoPermissions);
			}
		}
	}

	public static boolean bluetoothLeRefresh(@NonNull BluetoothGatt bluetoothGatt){
		boolean isRefresh = false;
		// Reflection????????????hide??????
		try {
			//noinspection JavaReflectionMemberAccess
			@SuppressLint("DiscouragedPrivateApi")
			Method method = bluetoothGatt.getClass().getDeclaredMethod("refresh");
			method.setAccessible(true);
			isRefresh = (boolean) method.invoke(bluetoothGatt);
			method.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isRefresh;
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void bluetoothLeSubscribe(@NonNull BluetoothGatt bluetoothGatt, @NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic
			, @NonNull UUID uuidDescriptor, boolean isEnable){
		bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, isEnable);
		BluetoothGattDescriptor bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(uuidDescriptor);
		if(bluetoothGattDescriptor != null){
			bluetoothGattDescriptor.setValue(isEnable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void bluetoothLeSubscribeEnable(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, UUID uuidDescriptor){
		bluetoothLeSubscribe(bluetoothGatt, bluetoothGattCharacteristic, uuidDescriptor, true);
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void bluetoothLeSubscribeDisable(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, UUID uuidDescriptor){
		bluetoothLeSubscribe(bluetoothGatt, bluetoothGattCharacteristic, uuidDescriptor, false);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * android.permission.ACCESS_COARSE_LOCATION<br>
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothDevice#ACTION_FOUND}<br>
	 * BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)<br>
	 * BluetoothAdapter.cancelDiscovery(); (android.permission.BLUETOOTH_ADMIN)<br><br>
	 *
	 * For Android 10 (API level 29) and higher<br>
	 * android.permission.ACCESS_FINE_LOCATION ({@link BluetoothAdapter#startDiscovery()})
	 */
	@RequiresPermission(allOf = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN
			, android.Manifest.permission.ACCESS_COARSE_LOCATION})
	public static void bluetoothDiscovery(@NonNull Context context, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		List<String> list = new ArrayList<String>(3);
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.BLUETOOTH_ADMIN);
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
			list.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
		}
		if(list.size() > 0){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = list.toArray(new String[0]);
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
				bluetoothAdapter.cancelDiscovery();
				context.getApplicationContext().unregisterReceiver(this);
			}
		};
		*/
		if(broadcastReceiver != null){
			// <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter.isDiscovering()){
			// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
			bluetoothAdapter.cancelDiscovery();
		}
		// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
		// For Android 10 (API level 29) and higher
		// <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
		bluetoothAdapter.startDiscovery();
	}

	/**
	 * callback use BroadcastReceiver or activityForResult<br>
	 * Discoverability will auto enable bluetooth<br>
	 * android.Manifest.permission.BLUETOOTH<br>
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothAdapter#ACTION_SCAN_MODE_CHANGED}<br>
	 * {@link BluetoothAdapter#EXTRA_SCAN_MODE}<br>
	 * {@link BluetoothAdapter#EXTRA_PREVIOUS_SCAN_MODE}
	 * @param seconds default 120 sec
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public static void setBluetoothDiscoverability(@NonNull Context context, @IntRange(from=0, to=3600) int seconds, BroadcastReceiver broadcastReceiver
			, int activityForResultRequestCode, boolean isActivityForResult, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.BLUETOOTH};
				handlerNoPermissions.sendMessage(message);
			}
			return;
		}
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		// <uses-permission android:name="android.permission.BLUETOOTH"/>
		if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
			return;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE
				if(intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
					context.getApplicationContext().unregisterReceiver(this);
				}
			}
		};
		*/
		Intent intent = new Intent();
		intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
		if(isActivityForResult && context instanceof Activity){
			((Activity)context).startActivityForResult(intent, activityForResultRequestCode);
		}else{
			if(broadcastReceiver != null){
				context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
			}
			context.startActivity(intent);
		}
	}

	/**
	 * Use BroadcastReceiver<br>
	 * Discoverability will auto enable bluetooth<br>
	 * android.Manifest.permission.BLUETOOTH
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothAdapter#ACTION_SCAN_MODE_CHANGED}
	 * @param seconds default 120 sec
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public static void setBluetoothDiscoverability(@NonNull Context context, @IntRange(from=0, to=3600) int seconds, BroadcastReceiver broadcastReceiver
			, Handler handlerNoPermissions){
		setBluetoothDiscoverability(context, seconds, broadcastReceiver, 0, false, handlerNoPermissions);
	}

	/**
	 * Use startActivityForResult<br>
	 * Discoverability will auto enable bluetooth<br>
	 * android.Manifest.permission.BLUETOOTH
	 * @param seconds default 120 sec
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public static void setBluetoothDiscoverability(@NonNull Context context, @IntRange(from=0, to=3600) int seconds, int activityForResultRequestCode
			, Handler handlerNoPermissions){
		setBluetoothDiscoverability(context, seconds, null, activityForResultRequestCode, true, handlerNoPermissions);
	}

	/**
	 * android.permission.BLUETOOTH_ADMIN<br>
	 * {@link IntentFilter#addAction(String)}, {@link BluetoothDevice#ACTION_FOUND}<br>
	 * {@link BluetoothDevice#EXTRA_BOND_STATE}<br>
	 * BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
	public static boolean bluetoothPairCreate(@NonNull Context context, BluetoothDevice bluetoothDevice, BroadcastReceiver broadcastReceiver, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.BLUETOOTH_ADMIN};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}

		/*
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0) == BluetoothDevice.BOND_BONDED){
					context.getApplicationContext().unregisterReceiver(this);
				}
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			}
		};
		*/
		if(broadcastReceiver != null){
			context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			// <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
			return bluetoothDevice.createBond();
		}else{
			// Reflection????????????hide??????
			try {
				Method method = bluetoothDevice.getClass().getMethod("createBond");
				return (boolean)method.invoke(bluetoothDevice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean bluetoothPairRemove(BluetoothDevice bluetoothDevice){
		// Reflection????????????hide??????
		try {
			//noinspection JavaReflectionMemberAccess
			Method method = bluetoothDevice.getClass().getMethod("removeBond");
			return (boolean)method.invoke(bluetoothDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean bluetoothPairCancelProcess(BluetoothDevice bluetoothDevice){
		// Reflection????????????hide??????
		try {
			//noinspection JavaReflectionMemberAccess
			Method method = bluetoothDevice.getClass().getMethod("cancelBondProcess");
			return (boolean)method.invoke(bluetoothDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean setBluetoothDevicePin(BluetoothDevice bluetoothDevice, byte[] bytes){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			bluetoothDevice.setPin(bytes);
		}else{
			// Reflection????????????hide??????
			try {
				Method method = bluetoothDevice.getClass().getMethod("setPin", byte[].class);
				return (boolean)method.invoke(bluetoothDevice, new Object[]{bytes});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean bluetoothCancelPairingUserInput(BluetoothDevice bluetoothDevice){
		// Reflection????????????hide??????
		try {
			//noinspection JavaReflectionMemberAccess
			Method method = bluetoothDevice.getClass().getMethod("cancelPairingUserInput");
			return (boolean)method.invoke(bluetoothDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * android.permission.BLUETOOTH_ADMIN
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
	public static boolean bluetoothPairCreateForQuiet(@NonNull Context context, BluetoothDevice bluetoothDevice, byte[] bytes, BroadcastReceiver broadcastReceiver
			, Handler handlerNoPermissions){
		return setBluetoothDevicePin(bluetoothDevice, bytes)
				&& bluetoothPairCreate(context, bluetoothDevice, broadcastReceiver, handlerNoPermissions)
				&& bluetoothCancelPairingUserInput(bluetoothDevice);
	}

	/**
	 * android.permission.ACCESS_WIFI_STATE
	 */
	@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
	public static InetAddress getBroadcastAddressFromWifiMask(@NonNull Context context, Handler handlerNoPermissions){
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null || !wifiManager.isWifiEnabled()){
			return null;
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.ACCESS_WIFI_STATE};
				handlerNoPermissions.sendMessage(message);
			}
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

	@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
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

	@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
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
}