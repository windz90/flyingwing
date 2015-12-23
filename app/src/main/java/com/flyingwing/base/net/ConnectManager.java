/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.2.5
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.net;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.flyingwing.base.widget.CustomProgressDialog;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("unused")
public abstract class ConnectManager {
	
	public static final int RUN_MODE_FOREGROUND_SINGLE_TAG = 0x0A1;
	public static final int RUN_MODE_BACKGROUND_ALLOW = 0x0A2;
	public static final int RUN_MODE_BACKGROUND = 0x0A3;
	
	public static abstract class ConnectAction {
		
		protected Context mContext;
		protected int mFlag;
		protected String[] mStringArray;
		protected Object[] mObjectArray;
		protected Handler mHandler;
		
		public ConnectAction(Context context, int flag, String...stringArray){
			mContext = context;
			mFlag = flag;
			mStringArray = stringArray;
		}
		
		public ConnectAction(Context context, int flag, Object...objectArray){
			mContext = context;
			mFlag = flag;
			mObjectArray = objectArray;
		}
		
		public Context getContext(){
			return mContext;
		}
		
		public void setConnectStatusHandler(Handler handler){
			mHandler = handler;
		}
		
		public Handler getConnectStatusHandler(){
			return mHandler;
		}
		
		public abstract NetworkAccess.ConnectionResult runConnectAction();
		
		public abstract HttpURLConnection runHttpURLConnection();
	}
	
	public static class ConnectSetting {
		
		private static boolean sSyncConnectRunning;
		
		private boolean mUseThread, mUseHandler, mDialogShow, mDialogDismiss, mSyncLock;
		private int mRunMode;
		private String mHintText;
		private ExecutorService mExecutorService;
		
		public static void setSyncConnectRunning(boolean isSyncConnectRunning){
			sSyncConnectRunning = isSyncConnectRunning;
		}
		
		public static boolean isSyncConnectRunning(){
			return sSyncConnectRunning;
		}
		
		public ConnectSetting(){}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, String hintText, boolean isSyncLock, ExecutorService executorService){
			mUseThread = isUseThread;
			mUseHandler = isUseHandler;
			mDialogShow = isDialogShow;
			mDialogDismiss = isDialogDismiss;
			mSyncLock = isSyncLock;
			mRunMode = runMode;
			mHintText = hintText;
			mExecutorService = executorService;
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, String hintText, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, hintText, isSyncLock, null);
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, String hintText){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, hintText, false, null);
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, boolean isSyncLock, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, isSyncLock, executorService);
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, false, executorService);
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode
				, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, isSyncLock, null);
		}
		
		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, false, null);
		}
		
		public void setUseThread(boolean isUseThread){
			mUseThread = isUseThread;
		}
		
		public boolean isUseThread(){
			return mUseThread;
		}
		
		public void setUseHandler(boolean isUseHandler){
			mUseHandler = isUseHandler;
		}
		
		public boolean isUseHandler(){
			return mUseHandler;
		}
		
		public void setDialogShow(boolean isDialogShow){
			mDialogShow = isDialogShow;
		}
		
		public boolean isDialogShow(){
			return mDialogShow;
		}
		
		public void setDialogDismiss(boolean isDialogDismiss){
			mDialogDismiss = isDialogDismiss;
		}
		
		public boolean isDialogDismiss(){
			return mDialogDismiss;
		}
		
		public void setSyncLock(boolean isSyncLock){
			mSyncLock = isSyncLock;
		}
		
		public boolean isSyncLock(){
			return mSyncLock;
		}
		
		public void setRunMode(int runMode){
			mRunMode = runMode;
		}
		
		public int getRunMode(){
			return mRunMode;
		}
		
		public void setHintText(String hintText){
			this.mHintText = hintText;
		}
		
		public String getHintText(){
			return mHintText;
		}
		
		public void setExecutorService(ExecutorService executorService){
			mExecutorService = executorService;
		}
		
		public ExecutorService getExecutorService(){
			return mExecutorService;
		}
	}
	
	public static abstract class ConnectListener {
		public void noNetworkConnection(NetworkAccess.ConnectionResult connectionResult){}
		public void connectFail(NetworkAccess.ConnectionResult connectionResult){}
		public void connected(NetworkAccess.ConnectionResult connectionResult){}
		public abstract void loadFail(NetworkAccess.ConnectionResult connectionResult);
		public abstract void loaded(NetworkAccess.ConnectionResult connectionResult);
		public void onCancelForegroundWait(DialogInterface dialog){}
	}
	
	public static void baseConnection(Looper looper, ConnectAction action, final ConnectListener listener){
		if(!NetworkAccess.isAvailable(action.getContext())){
			reply(noNetworkConnection(), listener);
			return;
		}
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, listener);
					return false;
				}
			};
			if(looper == null){
				handler = new Handler(callback);
			}else{
				handler = new Handler(looper, callback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		action.setConnectStatusHandler(handler);
		reply(connecting(action), listener);
	}
	
	public static void baseConnection(ConnectAction action, ConnectListener listener){
		baseConnection(null, action, listener);
	}
	
	public static void customConnection(Looper looper, final ConnectAction action, final ConnectSetting setting
			, final ConnectListener listener){
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, listener);
					return false;
				}
			};
			if(looper == null){
				handler = new Handler(callback);
			}else{
				handler = new Handler(looper, callback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!NetworkAccess.isAvailable(action.getContext())){
			Message msg = runModeHandle(noNetworkConnection(), setting.mRunMode, setting.mDialogDismiss);
			if(setting.mUseHandler){
				if(handler == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handler.sendMessage(msg);
				}
			}else{
				reply(msg, listener);
			}
			return;
		}
		
		if((setting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG || setting.mRunMode == RUN_MODE_BACKGROUND_ALLOW) && 
				setting.mDialogShow){
			CustomProgressDialog.getInstance(action.getContext(), setting.mHintText).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					listener.onCancelForegroundWait(dialog);
				}
			});
		}else if(setting.mHintText != null){
			CustomProgressDialog.setInstanceMessage(setting.mHintText);
		}
		
		action.setConnectStatusHandler(handler);
		if(setting.mUseThread){
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					Message msg;
					if(setting.mSyncLock){
						ConnectSetting.sSyncConnectRunning = true;
						msg = connectingSync(action);
						ConnectSetting.sSyncConnectRunning = false;
					}else{
						msg = connecting(action);
					}
					msg = runModeHandle(msg, setting.mRunMode, setting.mDialogDismiss);
					if(setting.mUseHandler){
						if(action.getConnectStatusHandler() == null){
							Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
						}else{
							action.getConnectStatusHandler().sendMessage(msg);
						}
					}else{
						reply(msg, listener);
					}
				}
			};
			if(setting.mExecutorService == null){
				new Thread(runnable).start();
			}else{
				setting.mExecutorService.submit(runnable);
			}
		}else{
			Message msg = runModeHandle(connecting(action), setting.mRunMode, setting.mDialogDismiss);
			if(setting.mUseHandler){
				if(handler == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handler.sendMessage(msg);
				}
			}else{
				reply(msg, listener);
			}
		}
	}
	
	public static void customConnection(ConnectAction action, ConnectSetting setting, ConnectListener listener){
		customConnection(null, action, setting, listener);
	}
	
	public static void syncConnection(Looper looper, ConnectAction action, final ConnectListener listener){
		Handler handler = new Handler(looper, new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, listener);
				return false;
			}
		});
		action.setConnectStatusHandler(handler);
		if(!NetworkAccess.isAvailable(action.getContext())){
			handler.sendMessage(noNetworkConnection());
			return;
		}
		handler.sendMessage(connecting(action));
	}
	
	public static void asyncConnection(Looper looper, final ConnectAction action, final int runMode, boolean isShow
			, final boolean isDismiss, String text, final ConnectListener listener){
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, listener);
					return false;
				}
			};
			if(looper == null){
				handler = new Handler(callback);
			}else{
				handler = new Handler(looper, callback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!NetworkAccess.isAvailable(action.getContext())){
			if(handler == null){
				Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
			}else{
				handler.sendMessage(runModeHandle(noNetworkConnection(), runMode, isDismiss));
			}
			return;
		}
		
		if((runMode == RUN_MODE_FOREGROUND_SINGLE_TAG || runMode == RUN_MODE_BACKGROUND_ALLOW) && isShow){
			CustomProgressDialog.getInstance(action.getContext(), text).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					listener.onCancelForegroundWait(dialog);
				}
			});
		}else if(text != null){
			CustomProgressDialog.setInstanceMessage(text);
		}
		
		action.setConnectStatusHandler(handler);
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(action.getConnectStatusHandler() == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					action.getConnectStatusHandler().sendMessage(runModeHandle(connecting(action), runMode, isDismiss));
				}
			}
		});
		thread.start();
	}
	
	public static void asyncConnection(ConnectAction action, int runMode, boolean isShow, boolean isDismiss, String text
			, ConnectListener listener){
		asyncConnection(null, action, runMode, isShow, isDismiss, text, listener);
	}
	
	public static void asyncConnection(ConnectAction action, int runMode, ConnectListener listener){
		asyncConnection(action, runMode, true, true, null, listener);
	}
	
	public static void asyncConnection(ConnectAction action, int runMode, boolean isShow, boolean isDismiss, ConnectListener listener){
		asyncConnection(action, runMode, isShow, isDismiss, null, listener);
	}
	
	public static void asyncConnection(ConnectAction action, int runMode, String text, ConnectListener listener){
		asyncConnection(action, runMode, true, true, text, listener);
	}
	
	private static Message noNetworkConnection(){
		NetworkAccess.ConnectionResult connectionResult = new NetworkAccess.ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		Message msg = new Message();
		msg.obj = connectionResult;
		return msg;
	}
	
	private static Message connecting(ConnectAction action){
		NetworkAccess.ConnectionResult connectionResult = action.runConnectAction();
		Message msg = new Message();
		msg.obj = connectionResult;
		if(connectionResult.getStatusMessage().contains("Connect Fail StatusCode ")){
			// NetworkAccess.CONNECTION_CONNECT_FAIL已即時回調，故此時的回調略過不處理。
			msg.what = -1;
		}else if(connectionResult.getStatusMessage().contains("Connect Fail ")){
			msg.what = NetworkAccess.CONNECTION_LOAD_FAIL;
		}else{
			msg.what = NetworkAccess.CONNECTION_LOADED;
		}
		return msg;
	}
	
	private static synchronized Message connectingSync(ConnectAction action){
		return connecting(action);
	}
	
	private static Message runModeHandle(Message msg, int runMode, boolean isDismiss){
		// RUN_FOREGROUND_SINGLE_TAG若顯式Tag被使用者取消，後續不進行任何處理
		if(runMode == RUN_MODE_FOREGROUND_SINGLE_TAG && msg.what != 0 && 
				(!CustomProgressDialog.hasInstance() || !CustomProgressDialog.isInstanceShowing())){
			msg.what = -1;
			return msg;
		}
		if(runMode == RUN_MODE_FOREGROUND_SINGLE_TAG || runMode == RUN_MODE_BACKGROUND_ALLOW){
			if(CustomProgressDialog.hasInstance() && CustomProgressDialog.isInstanceShowing()){
				if(isDismiss || msg.what == 0 || msg.what == NetworkAccess.CONNECTION_LOAD_FAIL){
					CustomProgressDialog.clearInstance();
				}
			}
		}
		return msg;
	}
	
	private static void reply(Message msg, ConnectListener listener){
		if(msg.what == 0 || msg.what == NetworkAccess.CONNECTION_CONNECT_FAIL){
			if(msg.what == 0){
				listener.noNetworkConnection((NetworkAccess.ConnectionResult)msg.obj);
			}
			listener.connectFail((NetworkAccess.ConnectionResult)msg.obj);
			listener.loadFail((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_CONNECTED){
			listener.connected((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_LOAD_FAIL){
			listener.loadFail((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_LOADED){
			listener.loaded((NetworkAccess.ConnectionResult)msg.obj);
		}
	}
}