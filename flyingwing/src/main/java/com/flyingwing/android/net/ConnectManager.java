/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.2.7
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
import android.util.Log;

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

		public abstract NetworkAccess.ConnectionResult runConnectAction(Handler handler);

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

	public static void baseConnection(Looper looper, ConnectAction connectAction, ConnectListener connectListener){
		Handler handler = getHandler(looper, connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			reply(noNetworkConnection(handler), connectListener);
			return;
		}
		reply(connect(connectAction, handler), connectListener);
	}

	public static void baseConnection(ConnectAction connectAction, ConnectListener connectListener){
		baseConnection(null, connectAction, connectListener);
	}

	public static void customConnection(Looper looper, final ConnectAction connectAction, final ConnectSetting connectSetting
			, final ConnectListener connectListener){
		final Handler handler = getHandler(looper, connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			Message msg = runModeHandle(noNetworkConnection(handler), connectSetting.mRunMode, connectSetting.mDialogDismiss);
			if(connectSetting.mUseHandler){
				handler.sendMessage(msg);
			}else{
				reply(msg, connectListener);
			}
			return;
		}

		if((connectSetting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG || connectSetting.mRunMode == RUN_MODE_BACKGROUND_ALLOW) &&
				connectSetting.mDialogShow){
			CustomProgressDialog.getInstance(connectAction.getContext(), connectSetting.mHintText).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					connectListener.onCancelForegroundWait(dialog);
				}
			});
		}else if(connectSetting.mHintText != null){
			CustomProgressDialog.setInstanceMessage(connectSetting.mHintText);
		}

		if(connectSetting.mUseThread){
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					Message msg;
					if(connectSetting.mSyncLock){
						ConnectSetting.sSyncConnectRunning = true;
						msg = connectSync(connectAction, handler);
						ConnectSetting.sSyncConnectRunning = false;
					}else{
						msg = connect(connectAction, handler);
					}
					msg = runModeHandle(msg, connectSetting.mRunMode, connectSetting.mDialogDismiss);
					if(connectSetting.mUseHandler){
						handler.sendMessage(msg);
					}else{
						reply(msg, connectListener);
					}
				}
			};
			if(connectSetting.mExecutorService == null){
				new Thread(runnable).start();
			}else{
				connectSetting.mExecutorService.submit(runnable);
			}
		}else{
			Message msg = runModeHandle(connect(connectAction, handler), connectSetting.mRunMode, connectSetting.mDialogDismiss);
			if(connectSetting.mUseHandler){
				if(handler == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handler.sendMessage(msg);
				}
			}else{
				reply(msg, connectListener);
			}
		}
	}

	public static void customConnection(ConnectAction connectAction, ConnectSetting connectSetting, ConnectListener connectListener){
		customConnection(null, connectAction, connectSetting, connectListener);
	}

	public static void syncConnection(Looper looper, ConnectAction connectAction, final ConnectListener connectListener){
		Handler handler = getHandler(looper, connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			handler.sendMessage(noNetworkConnection(handler));
			return;
		}
		handler.sendMessage(connect(connectAction, handler));
	}

	public static void asyncConnection(Looper looper, final ConnectAction connectAction, final int runMode, boolean isShow
			, final boolean isDismiss, String text, final ConnectListener connectListener){
		final Handler handler = getHandler(looper, connectListener);
		if(!NetworkAccess.isAvailable(connectAction.getContext())){
			handler.sendMessage(runModeHandle(noNetworkConnection(handler), runMode, isDismiss));
			return;
		}

		if((runMode == RUN_MODE_FOREGROUND_SINGLE_TAG || runMode == RUN_MODE_BACKGROUND_ALLOW) && isShow){
			CustomProgressDialog.getInstance(connectAction.getContext(), text).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					connectListener.onCancelForegroundWait(dialog);
				}
			});
		}else if(text != null){
			CustomProgressDialog.setInstanceMessage(text);
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				handler.sendMessage(runModeHandle(connect(connectAction, handler), runMode, isDismiss));
			}
		});
		thread.start();
	}

	public static void asyncConnection(ConnectAction connectAction, int runMode, boolean isShow, boolean isDismiss, String text
			, ConnectListener connectListener){
		asyncConnection(null, connectAction, runMode, isShow, isDismiss, text, connectListener);
	}

	public static void asyncConnection(ConnectAction connectAction, int runMode, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, true, true, null, connectListener);
	}

	public static void asyncConnection(ConnectAction connectAction, int runMode, boolean isShow, boolean isDismiss, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, isShow, isDismiss, null, connectListener);
	}

	public static void asyncConnection(ConnectAction connectAction, int runMode, String text, ConnectListener connectListener){
		asyncConnection(connectAction, runMode, true, true, text, connectListener);
	}

	public static Handler getHandler(final Looper looper, final ConnectListener connectListener){
		Callback callback = new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, connectListener);
				return false;
			}
		};
		if(looper == null){
			return new Handler(callback);
		}else{
			return new Handler(looper, callback);
		}
	}

	private static Message noNetworkConnection(Handler handler){
		NetworkAccess.ConnectionResult connectionResult = new NetworkAccess.ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		Message msg = Message.obtain(handler);
		msg.obj = connectionResult;
		return msg;
	}

	private static Message connect(ConnectAction connectAction, Handler handler){
		NetworkAccess.ConnectionResult connectionResult = connectAction.runConnectAction(handler);
		Message msg = Message.obtain(handler);
		msg.obj = connectionResult;
		msg.what = connectionResult.getStatusCode();
		if(msg.what != NetworkAccess.CONNECTION_LOAD_FAIL && msg.what != NetworkAccess.CONNECTION_LOADED){
			// NetworkAccess.CONNECTION_CONNECT_FAIL、NetworkAccess.CONNECTION_CONNECTED已即時回調，故此時的回調略過不處理。
			msg.what = -1;
		}
		return msg;
	}

	private static synchronized Message connectSync(ConnectAction connectAction, Handler handler){
		return connect(connectAction, handler);
	}

	private static Message runModeHandle(Message msg, int runMode, boolean isDismiss){
		// RUN_FOREGROUND_SINGLE_TAG若顯式Tag被使用者取消，後續不進行任何處理
		if(runMode == RUN_MODE_FOREGROUND_SINGLE_TAG && msg.what != 0 &&
				(!CustomProgressDialog.hasInstanceDialog() || !CustomProgressDialog.isInstanceShowing())){
			msg.what = -1;
			return msg;
		}
		if(runMode == RUN_MODE_FOREGROUND_SINGLE_TAG || runMode == RUN_MODE_BACKGROUND_ALLOW){
			if(CustomProgressDialog.hasInstanceDialog() && CustomProgressDialog.isInstanceShowing()){
				if(isDismiss || msg.what == 0 || msg.what == NetworkAccess.CONNECTION_LOAD_FAIL){
					CustomProgressDialog.clearInstance();
				}
			}
		}
		return msg;
	}

	private static void reply(Message msg, ConnectListener connectListener){
		if(msg.what == 0 || msg.what == NetworkAccess.CONNECTION_CONNECT_FAIL){
			if(msg.what == 0){
				connectListener.noNetworkConnection((NetworkAccess.ConnectionResult)msg.obj);
			}
			connectListener.connectFail((NetworkAccess.ConnectionResult)msg.obj);
			connectListener.loadFail((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_CONNECTED){
			connectListener.connected((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_LOAD_FAIL){
			connectListener.loadFail((NetworkAccess.ConnectionResult)msg.obj);
		}else if(msg.what == NetworkAccess.CONNECTION_LOADED){
			connectListener.loaded((NetworkAccess.ConnectionResult)msg.obj);
		}
	}
}