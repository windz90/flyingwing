/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.3.2
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

import java.util.concurrent.ExecutorService;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectManager {

	public static final int CONNECTION_TYPE_ALWAYS_QUIET = 0;
	public static final int CONNECTION_TYPE_ALLOW_QUIET = 1;
	public static final int CONNECTION_TYPE_ALLOW_CANCEL = 2;

	public static class ConnectSetting {

		private static boolean sSyncConnectRunning;

		private boolean mIsUseThread, mIsUseHandler, mIsDialogShow, mIsDialogDismiss, mIsSyncLock;
		private int mConnectionType;
		private String mLabelText;
		private ExecutorService mExecutorService;

		public static void setSyncConnectRunning(boolean isSyncConnectRunning){
			sSyncConnectRunning = isSyncConnectRunning;
		}

		public static boolean isSyncConnectRunning(){
			return sSyncConnectRunning;
		}

		public ConnectSetting(){}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType, String hintText
				, boolean isSyncLock, ExecutorService executorService){
			mIsUseThread = isUseThread;
			mIsUseHandler = isUseHandler;
			mIsDialogShow = isDialogShow;
			mIsDialogDismiss = isDialogDismiss;
			mIsSyncLock = isSyncLock;
			mConnectionType = connectionType;
			mLabelText = hintText;
			mExecutorService = executorService;
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType, String hintText
				, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, hintText, isSyncLock, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType, String hintText){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, hintText, false, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType, boolean isSyncLock
				, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, null, isSyncLock, executorService);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType
				, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, null, false, executorService);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType, boolean isSyncLock){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, null, isSyncLock, null);
		}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int connectionType){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, connectionType, null, false, null);
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

		public void setConnectionType(int runMode){
			mConnectionType = runMode;
		}

		public int getConnectionType(){
			return mConnectionType;
		}

		public void setNoticeText(String hintText){
			this.mLabelText = hintText;
		}

		public String getHintText(){
			return mLabelText;
		}

		public void setExecutorService(ExecutorService executorService){
			mExecutorService = executorService;
		}

		public ExecutorService getExecutorService(){
			return mExecutorService;
		}
	}

	public static class ConnectSettingSyncBegin extends ConnectSetting {
		public ConnectSettingSyncBegin(){
			setUseThread(false);
			setUseHandler(true);
			setDialogShow(false);
			setDialogDismiss(false);
			setSyncLock(false);
			setConnectionType(CONNECTION_TYPE_ALWAYS_QUIET);
			setNoticeText(null);
			setExecutorService(null);
		}
	}

	public static class ConnectSettingSyncEnd extends ConnectSetting {
		public ConnectSettingSyncEnd(){
			setUseThread(true);
			setUseHandler(false);
			setDialogShow(false);
			setDialogDismiss(false);
			setSyncLock(false);
			setConnectionType(CONNECTION_TYPE_ALWAYS_QUIET);
			setNoticeText(null);
			setExecutorService(null);
		}
	}

	public static class ConnectSettingAsync extends ConnectSetting {
		public ConnectSettingAsync(int connectionType, boolean isShow, boolean isDismiss, String text){
			setUseThread(true);
			setUseHandler(true);
			setDialogShow(isShow);
			setDialogDismiss(isDismiss);
			setSyncLock(false);
			setConnectionType(connectionType);
			setNoticeText(text);
			setExecutorService(null);
		}
		public ConnectSettingAsync(int connectionType, boolean isShow, boolean isDismiss){
			this(connectionType, isShow, isDismiss, null);
		}
		public ConnectSettingAsync(int connectionType, String text){
			this(connectionType, true, true, text);
		}
	}

	public static abstract class ConnectListener {
		/**
		 * Return a {@link Message} for {@link ConnectListener#onConnectionResponse(Message)}<br>
		 */
		public abstract Message onConnectionRequest();
		public abstract void onConnectionResponse(Message msg);
		public void onCancelForegroundWait(DialogInterface dialog){}
	}

	public static void connection(@NonNull final Context context, final Looper looper, @NonNull final ConnectSetting connectSetting
			, @NonNull final ConnectListener connectListener){
		if((connectSetting.mConnectionType == CONNECTION_TYPE_ALLOW_CANCEL || connectSetting.mConnectionType == CONNECTION_TYPE_ALLOW_QUIET) &&
				connectSetting.mIsDialogShow){
			CustomProgressDialog.getInstance(context, connectSetting.mLabelText).show();
			CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					CustomProgressDialog.clearInstance();
					connectListener.onCancelForegroundWait(dialog);
				}
			});
		}else if(connectSetting.mLabelText != null){
			CustomProgressDialog.setInstanceMessage(connectSetting.mLabelText);
		}

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
				if(msg != null){
					if(connectSetting.mIsUseHandler){
						getHandler(context, looper, connectSetting, connectListener).sendMessage(msg);
					}else{
						responseManage(msg, connectSetting, connectListener);
					}
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

	public static Handler getHandler(@NonNull Context context, Looper looper, @NonNull final ConnectSetting connectSetting
			, @NonNull final ConnectListener connectListener){
		Callback callback = new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if(msg.obj == null){
					return false;
				}
				responseManage(msg, connectSetting, connectListener);
				return false;
			}
		};
		if(looper == null){
			return new Handler(context.getMainLooper(), callback);
		}else{
			return new Handler(looper, callback);
		}
	}

	private static synchronized Message requestSynchronized(@NonNull ConnectListener connectListener){
		return connectListener.onConnectionRequest();
	}

	private static void responseManage(@NonNull Message msg, @NonNull ConnectSetting connectSetting, @NonNull ConnectListener connectListener){
		// RUN_FOREGROUND若Tag被使用者取消，後續不進行任何處理
		if(connectSetting.mConnectionType == CONNECTION_TYPE_ALLOW_CANCEL && !CustomProgressDialog.isInstanceShowing()){
			return;
		}
		if((connectSetting.mConnectionType == CONNECTION_TYPE_ALLOW_CANCEL || connectSetting.mConnectionType == CONNECTION_TYPE_ALLOW_QUIET) &&
				CustomProgressDialog.isInstanceShowing() && connectSetting.mIsDialogDismiss){
			CustomProgressDialog.clearInstance();
		}
		connectListener.onConnectionResponse(msg);
	}
}