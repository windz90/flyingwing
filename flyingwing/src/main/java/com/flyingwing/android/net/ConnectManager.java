/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.3.0
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

	public static final int RUN_MODE_BACKGROUND = 0;
	public static final int RUN_MODE_BACKGROUND_ALLOW = 1;
	public static final int RUN_MODE_FOREGROUND_SINGLE_TAG = 2;

	public interface ConnectAction {
		/**
		 * Return a {@link Message} for {@link ConnectListener#onConnectionStatusChange(int, Object)}<br>
		 * int connectStatus = {@link Message#what}<br>
		 * Object object = {@link Message#obj}
		 */
		Message onConnectAction();
	}

	public static abstract class ConnectListener {
		public abstract void onConnectionStatusChange(int connectStatus, Object object);
		public void onCancelForegroundWait(DialogInterface dialog){}
	}

	public static class ConnectSetting {

		private static boolean sSyncConnectRunning;

		protected boolean mIsUseThread, mIsUseHandler, mIsDialogShow, mIsDialogDismiss, mIsSyncLock;
		protected int mRunMode;
		protected String mHintText;
		protected ExecutorService mExecutorService;

		public static void setSyncConnectRunning(boolean isSyncConnectRunning){
			sSyncConnectRunning = isSyncConnectRunning;
		}

		public static boolean isSyncConnectRunning(){
			return sSyncConnectRunning;
		}

		public ConnectSetting(){}

		public ConnectSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runMode, String hintText
				, boolean isSyncLock, ExecutorService executorService){
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

	public static class ConnectSettingHalfSync extends ConnectSetting {
		public ConnectSettingHalfSync(){
			mIsUseThread = false;
			mIsUseHandler = true;
			mIsDialogShow = false;
			mIsDialogDismiss = false;
			mIsSyncLock = false;
			mRunMode = RUN_MODE_BACKGROUND;
			mExecutorService = null;
		}
	}

	public static class ConnectSettingAsync extends ConnectSetting {
		public ConnectSettingAsync(int runMode, boolean isShow, boolean isDismiss, String text){
			mIsUseThread = true;
			mIsUseHandler = true;
			mIsDialogShow = isShow;
			mIsDialogDismiss = isDismiss;
			mIsSyncLock = false;
			mRunMode = runMode;
			mHintText = text;
			mExecutorService = null;
		}
		public ConnectSettingAsync(int runMode, boolean isShow, boolean isDismiss){
			this(runMode, isShow, isDismiss, null);
		}
		public ConnectSettingAsync(int runMode, String text){
			this(runMode, true, true, null);
		}
	}

	public static void connection(@NonNull final Context context, final Looper looper, @NonNull final ConnectAction connectAction
			, @NonNull final ConnectSetting connectSetting, final ConnectListener connectListener){
		if((connectSetting.mRunMode == RUN_MODE_FOREGROUND_SINGLE_TAG || connectSetting.mRunMode == RUN_MODE_BACKGROUND_ALLOW) &&
				connectSetting.mIsDialogShow){
			CustomProgressDialog.getInstance(context, connectSetting.mHintText).show();
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
				Message msg;
				if(connectSetting.mIsSyncLock){
					ConnectSetting.sSyncConnectRunning = true;
					msg = onConnectActionSynchronized(connectAction);
					ConnectSetting.sSyncConnectRunning = false;
				}else{
					msg = connectAction.onConnectAction();
				}
				if(msg != null){
					if(connectSetting.mIsUseHandler){
						getHandler(context, looper, connectSetting, connectListener).sendMessage(msg);
					}else{
						report(msg, connectSetting, connectListener);
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

	public static void connection(@NonNull Context context, @NonNull ConnectAction connectAction, @NonNull ConnectSetting connectSetting
			, ConnectListener connectListener){
		connection(context, null, connectAction, connectSetting, connectListener);
	}

	public static Handler getHandler(@NonNull Context context, Looper looper, @NonNull final ConnectSetting connectSetting, final ConnectListener connectListener){
		Callback callback = new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if(msg.obj == null){
					return false;
				}
				report(msg, connectSetting, connectListener);
				return false;
			}
		};
		if(looper == null){
			return new Handler(context.getMainLooper(), callback);
		}else{
			return new Handler(looper, callback);
		}
	}

	private static synchronized Message onConnectActionSynchronized(@NonNull ConnectAction connectAction){
		return connectAction.onConnectAction();
	}

	private static void report(@NonNull Message msg, @NonNull ConnectSetting connectSetting, ConnectListener connectListener){
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
		connectListener.onConnectionStatusChange(msg.what, msg.obj);
	}
}