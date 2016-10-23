/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.2.10
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.flyingwing.android.widget.CustomProgressDialog;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ConnectManager {

	public static final int RUN_MODE_BACKGROUND = 0;
	public static final int RUN_MODE_BACKGROUND_ALLOW = 1;
	public static final int RUN_MODE_FOREGROUND_SINGLE_TAG = 2;

	public static abstract class ConnectAction {

		protected Context mContext;
		protected int mFlag;
		protected String[] mStringArray;
		protected Object[] mObjectArray;

		public ConnectAction(@NonNull Context context, int flag, String...stringArray){
			mContext = context;
			mFlag = flag;
			mStringArray = stringArray;
		}

		public ConnectAction(@NonNull Context context, int flag, Object...objectArray){
			mContext = context;
			mFlag = flag;
			mObjectArray = objectArray;
		}

		public Context getContext(){
			return mContext;
		}

		public abstract NetworkAccess.ConnectionResult runConnectAction(Handler handler);

		public abstract HttpURLConnection runHttpURLConnection();
	}

	public static class ConnectSetting {

		private static boolean sSyncConnectRunning;

		private boolean mIsUseThread, mIsUseHandler, mIsDialogShow, mIsDialogDismiss, mIsSyncLock;
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
			mIsUseThread = isUseThread;
			mIsUseHandler = isUseHandler;
			mIsDialogShow = isDialogShow;
			mIsDialogDismiss = isDialogDismiss;
			mIsSyncLock = isSyncLock;
			mRunMode = runMode;
			mHintText = hintText;
			mExecutorService = executorService;
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, String hintText
				, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, hintText, isSyncLock, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, String hintText){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, hintText, false, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, boolean isSyncLock
				, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, isSyncLock, executorService);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, false, executorService);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, isSyncLock, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runMode, null, false, null);
		}

		public void setUseThread(boolean isUseThread){
			mIsUseThread = isUseThread;
		}

		public boolean isUseThread(){
			return mIsUseThread;
		}

		public void setUseHandler(boolean isUseHandler){
			mIsUseHandler = isUseHandler;
		}

		public boolean isUseHandler(){
			return mIsUseHandler;
		}

		public void setDialogShow(boolean isDialogShow){
			mIsDialogShow = isDialogShow;
		}

		public boolean isDialogShow(){
			return mIsDialogShow;
		}

		public void setDialogDismiss(boolean isDialogDismiss){
			mIsDialogDismiss = isDialogDismiss;
		}

		public boolean isDialogDismiss(){
			return mIsDialogDismiss;
		}

		public void setSyncLock(boolean isSyncLock){
			mIsSyncLock = isSyncLock;
		}

		public boolean isSyncLock(){
			return mIsSyncLock;
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
		public void onConnectionNoNetwork(NetworkAccess.ConnectionResult connectionResult){}
		public abstract void onConnectionStatusChange(int connectStatus, NetworkAccess.ConnectionResult connectionResult);
		public void onCancelForegroundWait(DialogInterface dialog){}
	}

	public static void customConnection(Looper looper, @NonNull final ConnectAction connectAction, @NonNull final ConnectSetting connectSetting
			, final ConnectListener connectListener){
		final Handler handler = getHandler(looper, connectSetting, connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			if(connectSetting.isUseHandler()){
				noNetworkConnection(handler);
			}else{
				noNetworkConnection(connectSetting, connectListener);
			}
			return;
		}

		if((connectSetting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG || connectSetting.mRunMode == RUN_MODE_BACKGROUND_ALLOW) &&
				connectSetting.mIsDialogShow){
			CustomProgressDialog.getInstance(connectAction.getContext(), connectSetting.mHintText).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					if(connectListener != null){
						connectListener.onCancelForegroundWait(dialog);
					}
				}
			});
		}else if(connectSetting.mHintText != null){
			CustomProgressDialog.setInstanceMessage(connectSetting.mHintText);
		}

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				NetworkAccess.ConnectionResult connectionResult;
				if(connectSetting.isSyncLock()){
					ConnectSetting.sSyncConnectRunning = true;
					connectionResult = connectSynchronized(connectAction, connectSetting.isUseHandler() ? handler : null);
					ConnectSetting.sSyncConnectRunning = false;
				}else{
					connectionResult = connect(connectAction, connectSetting.isUseHandler() ? handler : null);
				}
				if(!connectSetting.isUseHandler() && connectionResult != null){
					report(connectionResult.getStatusCode(), connectionResult, connectSetting, connectListener);
				}
			}
		};
		if(connectSetting.isUseThread()){
			if(connectSetting.mExecutorService == null){
				new Thread(runnable).start();
			}else{
				connectSetting.mExecutorService.submit(runnable);
			}
		}else{
			runnable.run();
		}
	}

	public static void customConnection(@NonNull ConnectAction connectAction, @NonNull ConnectSetting connectSetting, ConnectListener connectListener){
		customConnection(null, connectAction, connectSetting, connectListener);
	}

	public static void baseConnection(Looper looper, @NonNull ConnectAction connectAction, ConnectListener connectListener){
		ConnectSetting connectSetting = new ConnectSetting(false, false, false, false, RUN_MODE_BACKGROUND);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			noNetworkConnection(connectSetting, connectListener);
			return;
		}
		NetworkAccess.ConnectionResult connectionResult = connect(connectAction, null);
		if(connectionResult != null){
			report(connectionResult.getStatusCode(), connectionResult, connectSetting, connectListener);
		}
	}

	public static void baseConnection(@NonNull ConnectAction connectAction, ConnectListener connectListener){
		baseConnection(null, connectAction, connectListener);
	}

	public static void syncConnection(Looper looper, @NonNull ConnectAction connectAction, final ConnectListener connectListener){
		Handler handler = getHandler(looper, new ConnectSetting(false, true, false, false, RUN_MODE_BACKGROUND), connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			noNetworkConnection(handler);
			return;
		}
		connect(connectAction, handler);
	}

	public static void asyncConnection(Looper looper, @NonNull final ConnectAction connectAction, int runMode, boolean isShow, boolean isDismiss, String text
			, final ConnectListener connectListener){
		final Handler handler = getHandler(looper, new ConnectSetting(true, true, isShow, isDismiss, runMode), connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			noNetworkConnection(handler);
			return;
		}

		if((runMode == RUN_MODE_FOREGROUND_SINGLE_TAG || runMode == RUN_MODE_BACKGROUND_ALLOW) && isShow){
			CustomProgressDialog.getInstance(connectAction.getContext(), text).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					if(connectListener != null){
						connectListener.onCancelForegroundWait(dialog);
					}
				}
			});
		}else if(text != null){
			CustomProgressDialog.setInstanceMessage(text);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				connect(connectAction, handler);
			}
		}).start();
	}

	public static void asyncConnection(@NonNull ConnectAction connectAction, int runMode, boolean isShow, boolean isDismiss, String text
			, ConnectListener connectListener){
		asyncConnection(null, connectAction, runMode, isShow, isDismiss, text, connectListener);
	}

	public static void asyncConnection(@NonNull ConnectAction connectAction, int runMode, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, true, true, null, connectListener);
	}

	public static void asyncConnection(@NonNull ConnectAction connectAction, int runMode, boolean isShow, boolean isDismiss, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, isShow, isDismiss, null, connectListener);
	}

	public static void asyncConnection(@NonNull ConnectAction connectAction, int runMode, String text, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, true, true, text, connectListener);
	}

	public static Handler getHandler(Looper looper, @NonNull final ConnectSetting connectSetting, final ConnectListener connectListener){
		Callback callback = new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if(msg.obj == null || !(msg.obj instanceof NetworkAccess.ConnectionResult)){
					return false;
				}
				report(msg.what, (NetworkAccess.ConnectionResult) msg.obj, connectSetting, connectListener);
				return false;
			}
		};
		if(looper == null){
			return new Handler(callback);
		}else{
			return new Handler(looper, callback);
		}
	}

	private static void noNetworkConnection(Handler handler){
		NetworkAccess.ConnectionResult connectionResult = new NetworkAccess.ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		Message msg = Message.obtain(handler);
		msg.obj = connectionResult;
		handler.sendMessage(msg);
	}

	private static void noNetworkConnection(@NonNull ConnectSetting connectSetting, ConnectListener connectListener){
		NetworkAccess.ConnectionResult connectionResult = new NetworkAccess.ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		report(NetworkAccess.CONNECTION_NO_NETWORK, connectionResult, connectSetting, connectListener);
	}

	private static NetworkAccess.ConnectionResult connect(@NonNull ConnectAction connectAction, Handler handler){
		return connectAction.runConnectAction(handler);
	}

	private static synchronized NetworkAccess.ConnectionResult connectSynchronized(@NonNull ConnectAction connectAction, Handler handler){
		return connect(connectAction, handler);
	}

	private static void report(int connectStatus, NetworkAccess.ConnectionResult connectionResult, @NonNull ConnectSetting connectSetting
			, ConnectListener connectListener){
		// RUN_FOREGROUND_SINGLE_TAG若顯式Tag被使用者取消，後續不進行任何處理
		if(connectSetting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG && !CustomProgressDialog.isInstanceShowing()){
			return;
		}
		if((connectSetting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG || connectSetting.mRunMode == RUN_MODE_BACKGROUND_ALLOW) &&
				CustomProgressDialog.isInstanceShowing() && connectSetting.mIsDialogDismiss){
			CustomProgressDialog.clearInstance();
		}
		if(connectListener == null){
			return;
		}
		if(connectStatus == NetworkAccess.CONNECTION_NO_NETWORK){
			connectListener.onConnectionNoNetwork(connectionResult);
			connectListener.onConnectionStatusChange(connectStatus, connectionResult);
		}else{
			connectListener.onConnectionStatusChange(connectStatus, connectionResult);
		}
	}
}