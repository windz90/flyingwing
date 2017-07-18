/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.4.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectManager {

	public static class ConnectSetting {

		private static boolean sSyncConnectRunning;

		private boolean mIsUseThread, mIsUseHandler, mIsSyncLock;
		private ExecutorService mExecutorService;

		public static void setSyncConnectRunning(boolean isSyncConnectRunning){
			sSyncConnectRunning = isSyncConnectRunning;
		}

		public static boolean isSyncConnectRunning(){
			return sSyncConnectRunning;
		}

		public ConnectSetting(){}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isSyncLock, ExecutorService executorService){
			mIsUseThread = isUseThread;
			mIsUseHandler = isUseHandler;
			mIsSyncLock = isSyncLock;
			mExecutorService = executorService;
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isSyncLock){
			this(isUseThread, isUseHandler, isSyncLock, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, ExecutorService executorService){
			this(isUseThread, isUseHandler, false, executorService);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler){
			this(isUseThread, isUseHandler, false, null);
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

		public void setSyncLock(boolean isSyncLock){
			mIsSyncLock = isSyncLock;
		}

		public boolean isSyncLock(){
			return mIsSyncLock;
		}

		public void setExecutorService(ExecutorService executorService){
			mExecutorService = executorService;
		}

		public ExecutorService getExecutorService(){
			return mExecutorService;
		}
	}

	public static class ConnectSettingSync extends ConnectSetting {
		public ConnectSettingSync(){
			setUseThread(false);
			setUseHandler(false);
			setSyncLock(false);
			setExecutorService(null);
		}
	}

	public static class ConnectSettingSyncBegin extends ConnectSetting {
		public ConnectSettingSyncBegin(){
			setUseThread(false);
			setUseHandler(true);
			setSyncLock(false);
			setExecutorService(null);
		}
	}

	public static class ConnectSettingSyncEnd extends ConnectSetting {
		public ConnectSettingSyncEnd(){
			setUseThread(true);
			setUseHandler(false);
			setSyncLock(false);
			setExecutorService(null);
		}
	}

	public static class ConnectSettingAsync extends ConnectSetting {
		public ConnectSettingAsync(){
			setUseThread(true);
			setUseHandler(true);
			setSyncLock(false);
			setExecutorService(null);
		}
	}

	public static abstract class ConnectListener {

		public static final int CONNECTION_TYPE_ALLOW_QUIET = 0;
		public static final int CONNECTION_TYPE_ALWAYS_QUIET = 1;
		public static final int CONNECTION_TYPE_ALLOW_CANCEL = 2;

		private int mConnectionType;

		/**
		 * Return a {@link Message} for {@link ConnectListener#onConnectionResponse(Message)}<br>
		 */
		public abstract Message onConnectionRequest();
		public abstract void onConnectionResponse(Message msg);

		public void setConnectionType(int connectionType){
			mConnectionType = connectionType;
		}

		public int getConnectionType(){
			return mConnectionType;
		}
	}

	public static void connection(@NonNull final Context context, final Looper looper, @NonNull final ConnectSetting connectSetting
			, @NonNull final ConnectListener connectListener){
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Message msg;
				if(connectSetting.mIsSyncLock){
					ConnectSetting.sSyncConnectRunning = true;
					msg = requestSynchronized(connectListener);
					ConnectSetting.sSyncConnectRunning = false;
				}else{
					msg = connectListener.onConnectionRequest();
				}
				if(msg == null){
					return;
				}
				if(connectSetting.mIsUseHandler){
					new Handler(looper == null ? context.getApplicationContext().getMainLooper() : looper, new Callback() {
						@Override
						public boolean handleMessage(Message msg) {
							if(msg.obj == null){
								return false;
							}
							connectListener.onConnectionResponse(msg);
							return false;
						}
					}).sendMessage(msg);
				}else{
					connectListener.onConnectionResponse(msg);
				}
			}
		};
		if(connectSetting.mIsUseThread){
			if(connectSetting.mExecutorService == null){
				new Thread(runnable).start();
			}else{
				connectSetting.mExecutorService.submit(runnable);
			}
		}else{
			runnable.run();
		}
	}

	public static void connection(@NonNull Context context, @NonNull ConnectSetting connectSetting, @NonNull ConnectListener connectListener){
		connection(context, null, connectSetting, connectListener);
	}

	private static synchronized Message requestSynchronized(@NonNull ConnectListener connectListener){
		return connectListener.onConnectionRequest();
	}
}