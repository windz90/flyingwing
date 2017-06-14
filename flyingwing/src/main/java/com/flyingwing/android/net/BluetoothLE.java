/*
 * Copyright 2017 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 4.3
 */

package com.flyingwing.android.net;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.annotation.Size;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLE {

	private static final class StaticNestedClass {
		private static final BluetoothLE INSTANCE = new BluetoothLE();
	}

	public static final int STATE_NOT_SUPPORTED = -5300;
	public static final int STATE_NOT_ENABLED = -5200;
	public static final int STATE_DISCONNECTED = -5100;
	public static final int STATE_DISCONNECTING = -5000;
	public static final int STATE_CONNECTING = 5000;
	public static final int STATE_CONNECTED = 5100;

	public static final int STATE_BUSY = 5200;
	public static final int STATE_UNABLE_CONN = 5300;
	public static final int STATE_IDLE = 5400;

	public static final int KEY_ADDRESS = 1;
	public static final int KEY_ADDRESS_NOT = 2;
	public static final int KEY_BLE_STATE = 3;
	public static final int KEY_BLE_STATE_NOT = 4;
	public static final int KEY_GATT_STATUS = 5;
	public static final int KEY_GATT_STATUS_NOT = 6;
	public static final int KEY_UUID = 7;
	public static final int KEY_UUID_NOT = 8;

	public static final String ATTRIBUTE_MUST = "attributeMust";
	public static final String ATTRIBUTE_MUST_NOT = "attributeMustNot";

	public static final int GATT_ON_CONNECTION_STATE_CHANGE = 1;
	public static final int GATT_ON_SERVICES_DISCOVERED = 2;
	public static final int GATT_ON_CHARACTERISTIC_READ = 3;
	public static final int GATT_ON_CHARACTERISTIC_WRITE = 4;
	public static final int GATT_ON_CHARACTERISTIC_CHANGED = 5;
	public static final int GATT_ON_DESCRIPTOR_READ = 6;
	public static final int GATT_ON_DESCRIPTOR_WRITE = 7;
	public static final int GATT_ON_RELIABLE_WRITE_COMPLETED = 8;
	public static final int GATT_ON_READ_REMOTE_RSSI = 9;
	public static final int GATT_ON_MTU_CHANGED = 10;

	private final String CONNECTION_TAG_DEFAULT = "put";

	private ArrayMap<String, ArrayMap<String, BLEConnection>> mArrayMap2Connection;
	private ArrayMap<String, ArrayMap<String, List<BLEGattCallback>>> mArrayMap2ListGattCallback;
	private List<BLEGattCallback> mListBLEGattCallback;
	private BluetoothGattCallback mBluetoothGattCallbackInternal;
	private int mReconnectCountMax = 2;
	private int mRetryPeriodSeconds = 300;

	public static BluetoothLE getInstance(){
		return BluetoothLE.StaticNestedClass.INSTANCE;
	}

	public BluetoothLE(){}

	@Nullable
	public BluetoothAdapter getBluetoothAdapter(Context context){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
			BluetoothManager bluetoothManager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
			return bluetoothManager.getAdapter();
		}else{
			return BluetoothAdapter.getDefaultAdapter();
		}
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public synchronized void connect(final Context context, final String address, final String connectionTag, boolean isResetReconnectCount, boolean isForceReconnect){
		if(!BluetoothAdapter.checkBluetoothAddress(address)){
			return;
		}
		if(mArrayMap2Connection == null){
			mArrayMap2Connection = new ArrayMap<>();
		}
		ArrayMap<String, BLEConnection> arrayMapAddress = mArrayMap2Connection.get(address);
		if(arrayMapAddress == null){
			arrayMapAddress = new ArrayMap<>(2);
			mArrayMap2Connection.put(address, arrayMapAddress);
		}
		BLEConnection bleConnection = arrayMapAddress.get(connectionTag);
		if(bleConnection == null){
			bleConnection = new BLEConnection();
			arrayMapAddress.put(connectionTag, bleConnection);
		}
		if(bleConnection.arrayMap == null){
			bleConnection.arrayMap = new ArrayMap<>();
		}
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		if(bluetoothAdapter == null || !context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_SUPPORTED));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_NOT_SUPPORTED);
			return;
		}
		if(!bluetoothAdapter.isEnabled()){
			bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_ENABLED));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_NOT_ENABLED);

			if("0".equals(bleConnection.arrayMap.get("retryStatus"))){
				bleConnection.arrayMap.put("retryStatus", "1");
				final ArrayMap<String, String> arrayMapCopy = bleConnection.arrayMap;
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@RequiresPermission(android.Manifest.permission.BLUETOOTH)
					@Override
					public void run() {
						if("1".equals(arrayMapCopy.get("retryStatus"))){
							arrayMapCopy.put("retryStatus", "2");
							arrayMapCopy.put("retryStatus", "0");
							connect(context, address, connectionTag, false, false);
						}
					}
				}, mRetryPeriodSeconds * 1000L);
			}
			return;
		}
		if(mBluetoothGattCallbackInternal == null){
			mBluetoothGattCallbackInternal = getBluetoothGattCallbackInternal(context);
		}
		String state = bleConnection.arrayMap.get("state");
		if(TextUtils.isEmpty(state) || Integer.parseInt(state) < STATE_CONNECTING){
			if(bleConnection.bluetoothGatt == null){
				BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
				bleConnection.bluetoothGatt = bluetoothDevice.connectGatt(context, false, mBluetoothGattCallbackInternal);
				if(bleConnection.bluetoothGatt == null){
					bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_SUPPORTED));
					sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
							, -1, STATE_NOT_SUPPORTED);
					return;
				}
			}
			bleConnection.arrayMap.put("state", Integer.toString(STATE_CONNECTING));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_CONNECTING);
			if(isResetReconnectCount){
				bleConnection.arrayMap.put("reconnectCount", "0");
			}
			if(!bleConnection.bluetoothGatt.connect()){
				bleConnection.arrayMap.put("state", Integer.toString(STATE_DISCONNECTED));
				sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
						, -1, STATE_DISCONNECTED);
			}
		}else if(Integer.parseInt(state) >= STATE_CONNECTING && isForceReconnect){
			if(bleConnection.bluetoothGatt == null){
				BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
				bleConnection.bluetoothGatt = bluetoothDevice.connectGatt(context, false, mBluetoothGattCallbackInternal);
				if(bleConnection.bluetoothGatt == null){
					bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_SUPPORTED));
					sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
							, -1, STATE_NOT_SUPPORTED);
					return;
				}
			}
			bleConnection.arrayMap.put("state", Integer.toString(STATE_DISCONNECTING));
			if(isResetReconnectCount){
				bleConnection.arrayMap.put("reconnectCount", "0");
			}
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_DISCONNECTING);
			bleConnection.bluetoothGatt.disconnect();
		}
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public void connect(Context context, String address, boolean isResetReconnectCount, boolean isForceReconnect){
		connect(context, address, CONNECTION_TAG_DEFAULT, isResetReconnectCount, isForceReconnect);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public void connect(Context context, String address, String connectionTag){
		connect(context, address, connectionTag, true, false);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public void connect(Context context, String address){
		connect(context, address, CONNECTION_TAG_DEFAULT, true, false);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	private synchronized void disconnect(final Context context, final String address, final String connectionTag, boolean isDisconnected){
		if(!BluetoothAdapter.checkBluetoothAddress(address)){
			return;
		}
		if(mArrayMap2Connection == null || mArrayMap2Connection.size() == 0){
			return;
		}
		ArrayMap<String, BLEConnection> arrayMapAddress = mArrayMap2Connection.get(address);
		if(arrayMapAddress == null || arrayMapAddress.size() == 0){
			return;
		}
		BLEConnection bleConnection = arrayMapAddress.get(connectionTag);
		if(bleConnection == null || bleConnection.bluetoothGatt == null){
			return;
		}
		if(bleConnection.arrayMap == null){
			bleConnection.arrayMap = new ArrayMap<>();
		}
		BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
		if(bluetoothAdapter == null || !context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_SUPPORTED));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_NOT_SUPPORTED);
			return;
		}
		if(!bluetoothAdapter.isEnabled()){
			bleConnection.arrayMap.put("state", Integer.toString(STATE_NOT_ENABLED));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_NOT_ENABLED);
			return;
		}
		if(isDisconnected){
			bleConnection.bluetoothGatt.close();// release BluetoothGatt resource, this instance can not be used, must re-call connectGatt() get new BluetoothGatt
			bleConnection.bluetoothGatt = null;
		}else{
			bleConnection.arrayMap.put("state", Integer.toString(STATE_DISCONNECTING));
			bleConnection.arrayMap.put("reconnectCount", Integer.toString(mReconnectCountMax));
			sendGattCallbackOnConnectionStateChange(address, connectionTag, bleConnection.arrayMap.get("state"), bleConnection.bluetoothGatt
					, -1, STATE_DISCONNECTING);
			bleConnection.bluetoothGatt.disconnect();
		}
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public void disconnect(final Context context, final String address, final String connectionTag){
		disconnect(context, address, connectionTag, false);
	}

	/**
	 * android.permission.BLUETOOTH<br>
	 */
	@RequiresPermission(android.Manifest.permission.BLUETOOTH)
	public void disconnect(Context context, String address){
		disconnect(context, address, CONNECTION_TAG_DEFAULT, false);
	}

	private BluetoothGattCallback getBluetoothGattCallbackInternal(final Context context){
		return new BluetoothGattCallback() {
			@RequiresPermission(android.Manifest.permission.BLUETOOTH)
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int gattStatus, int gattState) {
				super.onConnectionStateChange(gatt, gattStatus, gattState);
				final String address = gatt.getDevice().getAddress();
				final String connectionTag = findConnectionTag(address, gatt);

				final ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					switch (gattState) {
						case BluetoothGatt.STATE_DISCONNECTED:
							if(Integer.toString(mReconnectCountMax).equals(arrayMap.get("reconnectCount"))){
								disconnect(context, address, connectionTag, true);
							}
							arrayMap.put("state", Integer.toString(STATE_DISCONNECTED));
							break;
						case BluetoothGatt.STATE_DISCONNECTING:
							arrayMap.put("state", Integer.toString(STATE_DISCONNECTING));
							break;
						case BluetoothGatt.STATE_CONNECTING:
							arrayMap.put("state", Integer.toString(STATE_CONNECTING));
							break;
						case BluetoothGatt.STATE_CONNECTED:
							arrayMap.put("state", Integer.toString(STATE_CONNECTED));
							arrayMap.put("reconnectCount", "0");
							arrayMap.put("retryStatus", "0");
							break;
					}
				}
				sendGattCallbackOnConnectionStateChange(address, connectionTag, arrayMap == null ? null : arrayMap.get("state"), gatt, gattStatus, gattState);

				if(gattState == BluetoothGatt.STATE_DISCONNECTED || (gattState == BluetoothGatt.STATE_CONNECTED && gattStatus != BluetoothGatt.GATT_SUCCESS)){
					if(arrayMap == null){
						return;
					}
					final int reconnectCount = TextUtils.isEmpty(arrayMap.get("reconnectCount")) ? 0 : Integer.parseInt(arrayMap.get("reconnectCount"));
					final Runnable runnable = new Runnable() {
						@RequiresPermission(android.Manifest.permission.BLUETOOTH)
						@Override
						public void run() {
							String reconnectCountUpdate = Integer.toString(reconnectCount < mReconnectCountMax ? reconnectCount + 1 : 0);
							if("2".equals(arrayMap.get("retryStatus"))){
								arrayMap.put("retryStatus", "0");
							}
							arrayMap.put("reconnectCount", reconnectCountUpdate);
							connect(context, address, connectionTag, false, false);
						}
					};
					if(reconnectCount < mReconnectCountMax && mReconnectCountMax > -1){
						new Handler(Looper.getMainLooper()).postDelayed(runnable, 500);
					}else if("0".equals(arrayMap.get("retryStatus")) && mRetryPeriodSeconds > -1){
						arrayMap.put("retryStatus", "1");
						new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
							@RequiresPermission(android.Manifest.permission.BLUETOOTH)
							@Override
							public void run() {
								if("1".equals(arrayMap.get("retryStatus"))){
									arrayMap.put("retryStatus", "2");
									runnable.run();
								}
							}
						}, mRetryPeriodSeconds * 1000L);
					}
				}
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int gattStatus) {
				super.onServicesDiscovered(gatt, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnServicesDiscovered(address, connectionTag, gatt, gattStatus);
			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int gattStatus) {
				super.onCharacteristicRead(gatt, characteristic, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnCharacteristicRead(address, connectionTag, gatt, characteristic, gattStatus);
			}

			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int gattStatus) {
				super.onCharacteristicWrite(gatt, characteristic, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnCharacteristicWrite(address, connectionTag, gatt, characteristic, gattStatus);
			}

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				super.onCharacteristicChanged(gatt, characteristic);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(STATE_IDLE));
				}
				sendGattCallbackOnCharacteristicChanged(address, connectionTag, gatt, characteristic);
			}

			@Override
			public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int gattStatus) {
				super.onDescriptorRead(gatt, descriptor, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnDescriptorRead(address, connectionTag, gatt, descriptor, gattStatus);
			}

			@Override
			public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int gattStatus) {
				super.onDescriptorWrite(gatt, descriptor, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnDescriptorWrite(address, connectionTag, gatt, descriptor, gattStatus);
			}

			@Override
			public void onReliableWriteCompleted(BluetoothGatt gatt, int gattStatus) {
				super.onReliableWriteCompleted(gatt, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnReliableWriteCompleted(address, connectionTag, gatt, gattStatus);
			}

			@Override
			public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int gattStatus) {
				super.onReadRemoteRssi(gatt, rssi, gattStatus);
				String address = gatt.getDevice().getAddress();
				String connectionTag = findConnectionTag(address, gatt);
				ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
				if(arrayMap != null){
					arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
				}
				sendGattCallbackOnReadRemoteRssi(address, connectionTag, gatt, rssi, gattStatus);
			}

			@Override
			public void onMtuChanged(BluetoothGatt gatt, int mtu, int gattStatus) {
				super.onMtuChanged(gatt, mtu, gattStatus);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
					String address = gatt.getDevice().getAddress();
					String connectionTag = findConnectionTag(address, gatt);
					ArrayMap<String, String> arrayMap = getInfoMap(address, connectionTag);
					if(arrayMap != null){
						arrayMap.put("state", Integer.toString(gattStatus == BluetoothGatt.GATT_SUCCESS ? STATE_IDLE : STATE_UNABLE_CONN));
					}
					sendGattCallbackOnMtuChanged(address, connectionTag, gatt, mtu, gattStatus);
				}
			}
		};
	}

	public String findConnectionTag(String address, BluetoothGatt bluetoothGatt){
		if(mArrayMap2Connection == null || mArrayMap2Connection.size() == 0){
			return null;
		}
		ArrayMap<String, BLEConnection> arrayMapAddress = mArrayMap2Connection.get(address);
		if(arrayMapAddress == null || arrayMapAddress.size() == 0){
			return null;
		}
		int size = arrayMapAddress.size();
		for(int i=0; i<size; i++){
			if(arrayMapAddress.valueAt(i).bluetoothGatt == bluetoothGatt){
				return arrayMapAddress.keyAt(i);
			}
		}
		return null;
	}

	public ArrayMap<String, String> findInfoMap(String address, BluetoothGatt bluetoothGatt){
		if(mArrayMap2Connection == null || mArrayMap2Connection.size() == 0){
			return null;
		}
		ArrayMap<String, BLEConnection> arrayMapAddress = mArrayMap2Connection.get(address);
		if(arrayMapAddress == null || arrayMapAddress.size() == 0){
			return null;
		}
		int size = arrayMapAddress.size();
		for(int i=0; i<size; i++){
			if(arrayMapAddress.valueAt(i).bluetoothGatt == bluetoothGatt){
				return arrayMapAddress.valueAt(i).arrayMap;
			}
		}
		return null;
	}

	private BLEConnection getBLEConnection(String address, String connectionTag){
		if(mArrayMap2Connection == null || mArrayMap2Connection.size() == 0){
			return null;
		}
		ArrayMap<String, BLEConnection> arrayMapAddress = mArrayMap2Connection.get(address);
		if(arrayMapAddress == null || arrayMapAddress.size() == 0){
			return null;
		}
		return arrayMapAddress.get(connectionTag);
	}

	public BluetoothGatt getBluetoothGatt(String address, String connectionTag){
		BLEConnection bleConnection = getBLEConnection(address, connectionTag);
		if(bleConnection != null){
			return bleConnection.bluetoothGatt;
		}
		return null;
	}

	public BluetoothGatt getBluetoothGatt(String address){
		return getBluetoothGatt(address, CONNECTION_TAG_DEFAULT);
	}

	public ArrayMap<String, String> getInfoMap(String address, String connectionTag){
		BLEConnection bleConnection = getBLEConnection(address, connectionTag);
		if(bleConnection != null){
			if(bleConnection.arrayMap == null){
				bleConnection.arrayMap = new ArrayMap<>();
			}
			return bleConnection.arrayMap;
		}
		return null;
	}

	public ArrayMap<String, String> getInfoMap(String address){
		return getInfoMap(address, CONNECTION_TAG_DEFAULT);
	}

	public boolean setDeviceState(String address, String connectionTag, int state){
		BLEConnection bleConnection = getBLEConnection(address, connectionTag);
		if(bleConnection != null){
			if(bleConnection.arrayMap == null){
				bleConnection.arrayMap = new ArrayMap<>();
			}
			bleConnection.arrayMap.put("state", Integer.toString(state));
			return true;
		}
		return false;
	}

	public boolean setDeviceState(String address, int state){
		return setDeviceState(address, CONNECTION_TAG_DEFAULT, state);
	}

	public int getDeviceState(String address, String connectionTag){
		BLEConnection bleConnection = getBLEConnection(address, connectionTag);
		if(bleConnection != null){
			if(bleConnection.arrayMap != null){
				String state = bleConnection.arrayMap.get("state");
				if(!TextUtils.isEmpty(state)){
					return Integer.parseInt(state);
				}
			}
		}
		return STATE_DISCONNECTED;
	}

	public int getDeviceState(String address){
		return getDeviceState(address, CONNECTION_TAG_DEFAULT);
	}

	/**
	 * @param filters Filter specific conditions.<br>
	 *                   filters[0] : key<br>
	 *                   {@link BluetoothLE#KEY_ADDRESS} or <br>
	 *                   {@link BluetoothLE#KEY_ADDRESS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_UUID} or <br>
	 *                   {@link BluetoothLE#KEY_UUID_NOT}<br>
	 *                   filters[1] : value<br>
	 *                   filters[2] : attribute<br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST} or <br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST_NOT}
	 */
	private synchronized boolean syncGattCallback(int type, String address, String connectionTag, BluetoothGattCallback bluetoothGattCallback, @Size(3) String[]... filters){
		if(type == 1){
			if(bluetoothGattCallback == null){
				return false;
			}
			if(mArrayMap2ListGattCallback == null){
				mArrayMap2ListGattCallback = new ArrayMap<>();
			}
			ArrayMap<String, List<BLEGattCallback>> arrayMapAddress = mArrayMap2ListGattCallback.get(address);
			if(arrayMapAddress == null){
				arrayMapAddress = new ArrayMap<>(2);
				mArrayMap2ListGattCallback.put(address, arrayMapAddress);
			}
			List<BLEGattCallback> listBLEGattCallback = arrayMapAddress.get(connectionTag);
			if(listBLEGattCallback == null){
				listBLEGattCallback = new ArrayList<>(4);
				arrayMapAddress.put(connectionTag, listBLEGattCallback);
			}
			int size = listBLEGattCallback.size();
			if(size == 0){
				listBLEGattCallback.add(new BLEGattCallback(bluetoothGattCallback, filters));
				return true;
			}
			BLEGattCallback bleGattCallbackItem;
			for(int i=0; i<size; i++){
				bleGattCallbackItem = listBLEGattCallback.get(i);
				if(bleGattCallbackItem.bluetoothGattCallback == bluetoothGattCallback){
					break;
				}
				if(i == size - 1){
					return listBLEGattCallback.add(new BLEGattCallback(bluetoothGattCallback, filters));
				}
			}
		}else{
			if(bluetoothGattCallback == null && type != 0){
				return false;
			}
			if(mArrayMap2ListGattCallback == null || mArrayMap2ListGattCallback.size() == 0){
				return false;
			}
			if(type == -4){
				mArrayMap2ListGattCallback.clear();
				return true;
			}
			ArrayMap<String, List<BLEGattCallback>> arrayMapAddress = mArrayMap2ListGattCallback.get(address);
			if(arrayMapAddress == null || arrayMapAddress.size() == 0){
				return false;
			}
			if(type == -3){
				arrayMapAddress.clear();
				return true;
			}
			mListBLEGattCallback = arrayMapAddress.get(connectionTag);
			if(mListBLEGattCallback == null || mListBLEGattCallback.size() == 0){
				return false;
			}
			if(type == -2){
				mListBLEGattCallback.clear();
				return true;
			}
			if(type == 0){
				return true;
			}else if(type == -1){
				BLEGattCallback bleGattCallbackItem;
				int size = mListBLEGattCallback.size();
				for(int i=0; i<size; i++){
					bleGattCallbackItem = mListBLEGattCallback.get(i);
					if(bleGattCallbackItem.bluetoothGattCallback == bluetoothGattCallback){
						return mListBLEGattCallback.remove(bleGattCallbackItem);
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param filters Filter specific conditions.<br>
	 *                   filters[0] : key<br>
	 *                   {@link BluetoothLE#KEY_ADDRESS} or <br>
	 *                   {@link BluetoothLE#KEY_ADDRESS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_UUID} or <br>
	 *                   {@link BluetoothLE#KEY_UUID_NOT}<br>
	 *                   filters[1] : value<br>
	 *                   filters[2] : attribute<br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST} or <br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST_NOT}
	 */
	public boolean addGattCallback(String address, String connectionTag, BluetoothGattCallback bluetoothGattCallback, @Size(3) String[]... filters){
		return bluetoothGattCallback != null && syncGattCallback(1, address, connectionTag, bluetoothGattCallback, filters);
	}

	public boolean addGattCallback(String address, String connectionTag, BluetoothGattCallback bluetoothGattCallback){
		return bluetoothGattCallback != null && syncGattCallback(1, address, connectionTag, bluetoothGattCallback);
	}

	/**
	 * @param filters Filter specific conditions.<br>
	 *                   filters[0] : key<br>
	 *                   {@link BluetoothLE#KEY_ADDRESS} or <br>
	 *                   {@link BluetoothLE#KEY_ADDRESS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE} or <br>
	 *                   {@link BluetoothLE#KEY_BLE_STATE_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS} or <br>
	 *                   {@link BluetoothLE#KEY_GATT_STATUS_NOT} or <br>
	 *                   {@link BluetoothLE#KEY_UUID} or <br>
	 *                   {@link BluetoothLE#KEY_UUID_NOT}<br>
	 *                   filters[1] : value<br>
	 *                   filters[2] : attribute<br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST} or <br>
	 *                   {@link BluetoothLE#ATTRIBUTE_MUST_NOT}
	 */
	public boolean putGattCallback(String address, BluetoothGattCallback bluetoothGattCallback, @Size(3) String[]... filters){
		return bluetoothGattCallback != null && syncGattCallback(1, address, CONNECTION_TAG_DEFAULT, bluetoothGattCallback, filters);
	}

	public boolean putGattCallback(String address, BluetoothGattCallback bluetoothGattCallback){
		return bluetoothGattCallback != null && syncGattCallback(1, address, CONNECTION_TAG_DEFAULT, bluetoothGattCallback);
	}

	private List<BLEGattCallback> getGattCallback(String address, String connectionTag){
		return syncGattCallback(0, address, connectionTag, null) ? mListBLEGattCallback : null;
	}

	private List<BLEGattCallback> getGattCallback(String address){
		return syncGattCallback(0, address, CONNECTION_TAG_DEFAULT, null) ? mListBLEGattCallback : null;
	}

	public boolean removeGattCallback(String address, String connectionTag, BluetoothGattCallback bluetoothGattCallback){
		return syncGattCallback(-1, address, connectionTag, bluetoothGattCallback);
	}

	public boolean removeGattCallback(String address, BluetoothGattCallback bluetoothGattCallback){
		return syncGattCallback(-1, address, CONNECTION_TAG_DEFAULT, bluetoothGattCallback);
	}

	public boolean clearConnectionTagLayerGattCallback(String address, String connectionTag){
		return syncGattCallback(-2, address, connectionTag, null);
	}

	public boolean clearConnectionTagLayerGattCallback(String address){
		return syncGattCallback(-2, address, CONNECTION_TAG_DEFAULT, null);
	}

	public boolean clearAddressLayerGattCallback(String address){
		return syncGattCallback(-3, address, null, null);
	}

	public boolean clearGattCallback(){
		return syncGattCallback(-4, null, null, null);
	}

	private void sendGattCallback(String address, String connectionTag, int gattCallback, String bleState, BluetoothGatt gatt, int gattStatus, int gattState
			, BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, int rssi, int mtu){
		List<BLEGattCallback> listBLEGattCallback = getGattCallback(address, connectionTag);
		if(listBLEGattCallback == null || listBLEGattCallback.size() == 0){
			return;
		}
		BLEGattCallback bleGattCallback;
		int size = listBLEGattCallback.size();
		for(int i=0; i<size; i++){
			bleGattCallback = listBLEGattCallback.get(i);
			if(bleGattCallback.listFilter == null || bleGattCallback.listFilter.size() == 0){
				switch (gattCallback) {
					case GATT_ON_CONNECTION_STATE_CHANGE:
						bleGattCallback.bluetoothGattCallback.onConnectionStateChange(gatt, gattStatus, gattState);
						break;
					case GATT_ON_SERVICES_DISCOVERED:
						bleGattCallback.bluetoothGattCallback.onServicesDiscovered(gatt, gattStatus);
						break;
					case GATT_ON_CHARACTERISTIC_READ:
						bleGattCallback.bluetoothGattCallback.onCharacteristicRead(gatt, characteristic, gattStatus);
						break;
					case GATT_ON_CHARACTERISTIC_WRITE:
						bleGattCallback.bluetoothGattCallback.onCharacteristicWrite(gatt, characteristic, gattStatus);
						break;
					case GATT_ON_CHARACTERISTIC_CHANGED:
						bleGattCallback.bluetoothGattCallback.onCharacteristicChanged(gatt, characteristic);
						break;
					case GATT_ON_DESCRIPTOR_READ:
						bleGattCallback.bluetoothGattCallback.onDescriptorRead(gatt, descriptor, gattStatus);
						break;
					case GATT_ON_DESCRIPTOR_WRITE:
						bleGattCallback.bluetoothGattCallback.onDescriptorWrite(gatt, descriptor, gattStatus);
						break;
					case GATT_ON_RELIABLE_WRITE_COMPLETED:
						bleGattCallback.bluetoothGattCallback.onReliableWriteCompleted(gatt, gattStatus);
						break;
					case GATT_ON_READ_REMOTE_RSSI:
						bleGattCallback.bluetoothGattCallback.onReadRemoteRssi(gatt, rssi, gattStatus);
						break;
					case GATT_ON_MTU_CHANGED:
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							bleGattCallback.bluetoothGattCallback.onMtuChanged(gatt, mtu, gattStatus);
						}
						break;
				}
				continue;
			}
			switch (gattCallback) {
				case GATT_ON_CONNECTION_STATE_CHANGE:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, bleState, Integer.toString(gattStatus), null)){
						bleGattCallback.bluetoothGattCallback.onConnectionStateChange(gatt, gattStatus, gattState);
					}
					break;
				case GATT_ON_SERVICES_DISCOVERED:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), null)){
						bleGattCallback.bluetoothGattCallback.onServicesDiscovered(gatt, gattStatus);
					}
					break;
				case GATT_ON_CHARACTERISTIC_READ:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), characteristic.getUuid())){
						bleGattCallback.bluetoothGattCallback.onCharacteristicRead(gatt, characteristic, gattStatus);
					}
					break;
				case GATT_ON_CHARACTERISTIC_WRITE:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), characteristic.getUuid())){
						bleGattCallback.bluetoothGattCallback.onCharacteristicWrite(gatt, characteristic, gattStatus);
					}
					break;
				case GATT_ON_CHARACTERISTIC_CHANGED:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, null, characteristic.getUuid())){
						bleGattCallback.bluetoothGattCallback.onCharacteristicChanged(gatt, characteristic);
					}
					break;
				case GATT_ON_DESCRIPTOR_READ:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), descriptor.getUuid())){
						bleGattCallback.bluetoothGattCallback.onDescriptorRead(gatt, descriptor, gattStatus);
					}
					break;
				case GATT_ON_DESCRIPTOR_WRITE:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), descriptor.getUuid())){
						bleGattCallback.bluetoothGattCallback.onDescriptorWrite(gatt, descriptor, gattStatus);
					}
					break;
				case GATT_ON_RELIABLE_WRITE_COMPLETED:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), null)){
						bleGattCallback.bluetoothGattCallback.onReliableWriteCompleted(gatt, gattStatus);
					}
					break;
				case GATT_ON_READ_REMOTE_RSSI:
					if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), null)){
						bleGattCallback.bluetoothGattCallback.onReadRemoteRssi(gatt, rssi, gattStatus);
					}
					break;
				case GATT_ON_MTU_CHANGED:
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						if(filter(bleGattCallback.listFilter, bleGattCallback.listFilter.size(), address, null, Integer.toString(gattStatus), null)){
							bleGattCallback.bluetoothGattCallback.onMtuChanged(gatt, mtu, gattStatus);
						}
					}
					break;
			}
		}
	}

	private void sendGattCallbackOnConnectionStateChange(String address, String connectionTag, String bleState, BluetoothGatt gatt, int gattStatus, int gattState){
		sendGattCallback(address, connectionTag, GATT_ON_CONNECTION_STATE_CHANGE, bleState, gatt, gattStatus, gattState, null, null, 0, 0);
	}

	private void sendGattCallbackOnServicesDiscovered(String address, String connectionTag, BluetoothGatt gatt, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_SERVICES_DISCOVERED, null, gatt, gattStatus, 0, null, null, 0, 0);
	}

	private void sendGattCallbackOnCharacteristicRead(String address, String connectionTag, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic
			, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_CHARACTERISTIC_READ, null, gatt, gattStatus, 0, characteristic, null, 0, 0);
	}

	private void sendGattCallbackOnCharacteristicWrite(String address, String connectionTag, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic
			, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_CHARACTERISTIC_WRITE, null, gatt, gattStatus, 0, characteristic, null, 0, 0);
	}

	private void sendGattCallbackOnCharacteristicChanged(String address, String connectionTag, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
		sendGattCallback(address, connectionTag, GATT_ON_CHARACTERISTIC_CHANGED, null, gatt, 0, 0, characteristic, null, 0, 0);
	}

	private void sendGattCallbackOnDescriptorRead(String address, String connectionTag, BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_DESCRIPTOR_READ, null, gatt, gattStatus, 0, null, descriptor, 0, 0);
	}

	private void sendGattCallbackOnDescriptorWrite(String address, String connectionTag, BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_DESCRIPTOR_WRITE, null, gatt, gattStatus, 0, null, descriptor, 0, 0);
	}

	private void sendGattCallbackOnReliableWriteCompleted(String address, String connectionTag, BluetoothGatt gatt, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_RELIABLE_WRITE_COMPLETED, null, gatt, gattStatus, 0, null, null, 0, 0);
	}

	private void sendGattCallbackOnReadRemoteRssi(String address, String connectionTag, BluetoothGatt gatt, int rssi, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_READ_REMOTE_RSSI, null, gatt, gattStatus, 0, null, null, rssi, 0);
	}

	private void sendGattCallbackOnMtuChanged(String address, String connectionTag, BluetoothGatt gatt, int mtu, int gattStatus){
		sendGattCallback(address, connectionTag, GATT_ON_MTU_CHANGED, null, gatt, gattStatus, 0, null, null, 0, mtu);
	}

	private boolean filter(List<String[]> listFilter, int sizeFilter, String address, String bleState, String gattStatus, UUID uuid){
		boolean isMatch = false;
		for(int j=0; j<sizeFilter; j++){
			if(listFilter.get(j).length == 3){
				switch (Integer.parseInt(listFilter.get(j)[0])) {
					case KEY_ADDRESS:
						if(!address.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_ADDRESS_NOT:
						if(address.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_BLE_STATE:
						if(!bleState.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_BLE_STATE_NOT:
						if(bleState.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_GATT_STATUS:
						if(!gattStatus.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_GATT_STATUS_NOT:
						if(gattStatus.equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_UUID:
						if(!uuid.toString().equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
					case KEY_UUID_NOT:
						if(uuid.toString().equals(listFilter.get(j)[2])){
							if(listFilter.get(j)[1].equals(ATTRIBUTE_MUST)){
								return false;
							}else{
								continue;
							}
						}
						break;
				}
			}
			isMatch = true;
			if(j < sizeFilter - 1){
				return true;
			}
		}
		return isMatch;
	}

	public void setReconnectCountMax(int reconnectCountMax){
		mReconnectCountMax = reconnectCountMax;
	}

	public int getReconnectCountMax(){
		return mReconnectCountMax;
	}

	public void enableReconnect(){
		mReconnectCountMax = 2;
	}

	public void disableReconnect(){
		mReconnectCountMax = -1;
	}

	public void setRetryPeriodSecondsForReconnectCountReset(int retryPeriodSeconds){
		mRetryPeriodSeconds = retryPeriodSeconds;
	}

	public int getRetryPeriodSecondsForReconnectCountReset(){
		return mRetryPeriodSeconds;
	}

	public void enableReconnectCountMaxReset(){
		mRetryPeriodSeconds = 300;
	}

	public void disableReconnectCountMaxReset(){
		mRetryPeriodSeconds = -1;
	}

	private class BLEConnection {

		private BluetoothGatt bluetoothGatt;
		private ArrayMap<String, String> arrayMap;

		BLEConnection(){}

		BLEConnection(BluetoothGatt bluetoothGatt, ArrayMap<String, String> arrayMap){
			this.bluetoothGatt = bluetoothGatt;
			this.arrayMap = arrayMap;
		}
	}

	private class BLEGattCallback {

		private BluetoothGattCallback bluetoothGattCallback;
		private List<String[]> listFilter;

		/**
		 * @param filters Filter specific conditions.<br>
		 *                   filters[0] : key<br>
		 *                   {@link BluetoothLE#KEY_ADDRESS} or <br>
		 *                   {@link BluetoothLE#KEY_ADDRESS_NOT} or <br>
		 *                   {@link BluetoothLE#KEY_BLE_STATE} or <br>
		 *                   {@link BluetoothLE#KEY_BLE_STATE_NOT} or <br>
		 *                   {@link BluetoothLE#KEY_GATT_STATUS} or <br>
		 *                   {@link BluetoothLE#KEY_GATT_STATUS_NOT} or <br>
		 *                   {@link BluetoothLE#KEY_UUID} or <br>
		 *                   {@link BluetoothLE#KEY_UUID_NOT}<br>
		 *                   filters[1] : value<br>
		 *                   filters[2] : attribute<br>
		 *                   {@link BluetoothLE#ATTRIBUTE_MUST} or <br>
		 *                   {@link BluetoothLE#ATTRIBUTE_MUST_NOT}
		 */
		@SuppressWarnings("ForLoopReplaceableByForEach")
		BLEGattCallback(BluetoothGattCallback bluetoothGattCallback, @Size(3) String[]... filters){
			this.bluetoothGattCallback = bluetoothGattCallback;
			if(filters.length > 0){
				listFilter = new ArrayList<>();
				for(int i=0; i<filters.length; i++){
					listFilter.add(new String[]{filters[i][0], filters[i][1], filters[i][2]});
				}
			}
		}
	}
}