package com.andy.library.module;

import java.util.concurrent.ExecutorService;

import com.andy.library.R;
import com.andy.library.module.C_networkAccess.ConnectionResult;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.0.6
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_downLoadEvent {
	
	public static final int STYLE_STORE_BY_NEIGHBOR = 8;
	public static final int STYLE_STORE_BY_ZONE = 9;
	
	public static final int RUN_FOREGROUND = 0xA1;
	public static final int RUN_BACKGROUND_ALLOW = 0xA2;
	public static final int RUN_BACKGROUND = 0xA3;
	public static final int RUN_BACKGROUND_QUIET = 0xA4;
	
	public static class DownLoadSetting{
		
		private boolean isUseThread, isUseHandler, isDialogShow, isDialogDismiss;
		private int runOption;
		private String hintText;
		private ExecutorService executorService;
		
		public DownLoadSetting(){}
		
		public DownLoadSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runOption
				, String hintText, ExecutorService executorService){
			this.isUseThread = isUseThread;
			this.isUseHandler = isUseHandler;
			this.isDialogShow = isDialogShow;
			this.isDialogDismiss = isDialogDismiss;
			this.runOption = runOption;
			this.hintText = hintText;
			this.executorService = executorService;
		}
		
		public DownLoadSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runOption
				, String hintText){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runOption, hintText, null);
		}
		
		public DownLoadSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runOption
				, ExecutorService executorService){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runOption, null, executorService);
		}
		
		public DownLoadSetting(boolean isUseThread, boolean isUseHandler, boolean isDialogShow, boolean isDialogDismiss, int runOption){
			this(isUseThread, isUseHandler, isDialogShow, isDialogDismiss, runOption, null, null);
		}
		
		public void setUseThread(boolean isUseThread){
			this.isUseThread = isUseThread;
		}
		
		public void setUseHandler(boolean isUseHandler){
			this.isUseHandler = isUseHandler;
		}
		
		public void setDialogShow(boolean isDialogShow){
			this.isDialogShow = isDialogShow;
		}
		
		public void setDialogDismiss(boolean isDialogDismiss){
			this.isDialogDismiss = isDialogDismiss;
		}
		
		public void setExecutorService(ExecutorService executorService){
			this.executorService = executorService;
		}
		
		public void setRunOption(int runOption){
			this.runOption = runOption;
		}
		
		public int getRunOption(){
			return runOption;
		}
		
		public void setHintText(String hintText){
			this.hintText = hintText;
		}
		
		public String getHintText(){
			return hintText;
		}
		
		public ExecutorService getExecutorService(){
			return executorService;
		}
		
		public boolean isUseThread(){
			return isUseThread;
		}
		
		public boolean isUseHandler(){
			return isUseHandler;
		}
		
		public boolean isDialogShow(){
			return isDialogShow;
		}
		
		public boolean isDialogDismiss(){
			return isDialogDismiss;
		}
	}
	
	public static abstract class DownLoadComplete{
		public abstract void loaded(ConnectionResult connectionResult);
		public abstract void loadFail(ConnectionResult connectionResult);
		public void onCancelForegroundWait(DialogInterface dialog){};
	}
	
	public static ConnectionResult updateStoreByNeighborData(Context context, String latit, String longit, String limit
			, String offset){
		String urlPost = "http://www.century21.com.tw/api/json/neighbor_store.aspx";
		String[][] postData = new String[][]{{"longitude", longit}, {"latitude", latit}, {"limit", limit}
		, {"offset", offset}};
		ConnectionResult connectionResult = C_networkAccess.connectUseHttpClient(context, urlPost, postData);
		return connectionResult;
	}
	
	public static ConnectionResult updateStoreByZoneData(Context context, String city, String district, String keyword
			, String limit, String offset){
		String urlPost = "http://www.century21.com.tw/api/json/zone_store2.aspx";
		String[][] postData = new String[][]{{"city", city}, {"district", district}, {"keyword", keyword}
		, {"limit", limit}, {"offset", offset}};
		ConnectionResult connectionResult = C_networkAccess.connectUseHttpClient(context, urlPost, postData);
		return connectionResult;
	}
	
	public static ConnectionResult prepareData(Context context, int style, String[] valueArray){
		ConnectionResult connectionResult = null;
		for(int i=0; i<valueArray.length; i++){
			if(valueArray[i] == null){
				valueArray[i] = "";
			}
		}
		if(style == STYLE_STORE_BY_NEIGHBOR){
			String latit = valueArray[0];
			String longit = valueArray[1];
			String limit = valueArray[2];
			String offset = valueArray[3];
			connectionResult = updateStoreByNeighborData(context, latit, longit, limit, offset);
		}else if(style == STYLE_STORE_BY_ZONE){
			String city = valueArray[0];
			String district = valueArray[1];
			String keyword = valueArray[2];
			String limit = valueArray[3];
			String offset = valueArray[4];
			connectionResult = updateStoreByZoneData(context, city, district, keyword, limit, offset);
		}
		return connectionResult;
	}
	
	public static void baseConnection(Context context, final DownLoadComplete complete, final int style, final String...valueArray){
		if(!C_networkAccess.isConnect(context)){
			reply(noNetworkConnection(context, RUN_BACKGROUND_QUIET), complete);
			return;
		}
		reply(connecting(context, style, valueArray), complete);
	}
	
	public static void customConnection(final Context context, final DownLoadSetting setting, final DownLoadComplete complete, final int style
			, final String...valueArray){
		final Handler handler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, complete);
				return false;
			}
		});
		if(!C_networkAccess.isConnect(context)){
			Message msg = runOptionDispatch(noNetworkConnection(context, setting.runOption), setting.runOption, setting.isDialogDismiss);
			if(setting.isUseHandler){
				handler.sendMessage(msg);
			}else{
				reply(msg, complete);
			}
			return;
		}
		
		if((setting.runOption == RUN_FOREGROUND || setting.runOption == RUN_BACKGROUND_ALLOW) && 
				setting.isDialogShow && context instanceof Activity){
			C_progressDialog.getInstance((Activity)context, setting.hintText);
			C_progressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					C_progressDialog.dismissInstance();
					complete.onCancelForegroundWait(dialog);
				}
			});
		}else if(setting.hintText != null){
			C_progressDialog.setInstanceMessage(setting.hintText);
		}
		if(setting.isUseThread){
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					Message msg = runOptionDispatch(connecting(context, style, valueArray), setting.runOption, setting.isDialogDismiss);
					if(setting.isUseHandler){
						handler.sendMessage(msg);
					}else{
						reply(msg, complete);
					}
				}
			};
			if(setting.executorService == null){
				new Thread(runnable).start();
			}else{
				setting.executorService.submit(runnable);
			}
		}else{
			Message msg = runOptionDispatch(connecting(context, style, valueArray), setting.runOption, setting.isDialogDismiss);
			if(setting.isUseHandler){
				handler.sendMessage(msg);
			}else{
				reply(msg, complete);
			}
		}
	}
	
	public static void syncConnection(final Context context, Looper looper, final DownLoadComplete complete, final int style
			, final String...valueArray){
		if(!C_networkAccess.isConnect(context)){
			Handler handler = new Handler(looper, new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					reply(noNetworkConnection(context, 0), complete);
					return false;
				}
			});
			handler.sendEmptyMessage(0);
			return;
		}
		
		Message msg = connecting(context, style, valueArray);
		Handler handler = new Handler(looper, new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, complete);
				return false;
			}
		});
		handler.sendMessage(msg);
	}
	
	@Deprecated
	public static void syncConnection(final Context context, final DownLoadComplete complete, final int style
			, final String...valueArray){
		if(!C_networkAccess.isConnect(context)){
			HandlerThread handlerThread = new HandlerThread("noNetworkToast");
			handlerThread.start();
			Handler handler = new Handler(handlerThread.getLooper(), new Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					reply(noNetworkConnection(context, 0), complete);
					return false;
				}
			});
			handler.sendEmptyMessage(0);
			handlerThread.quit();
			return;
		}
		
		Message msg = connecting(context, style, valueArray);
		HandlerThread handlerThread = new HandlerThread("complete");
		handlerThread.start();
		Handler handler = new Handler(handlerThread.getLooper(), new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, complete);
				return false;
			}
		});
		handler.sendMessage(msg);
		handlerThread.quit();
	}
	
	public static void asyncConnection(final Context context, final int runOption, boolean isShow, final boolean isDismiss
			, String text, final DownLoadComplete complete, final int style, final String...valueArray){
		final Handler handler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, complete);
				return false;
			}
		});
		if(!C_networkAccess.isConnect(context)){
			handler.sendMessage(runOptionDispatch(noNetworkConnection(context, runOption), runOption, isDismiss));
			return;
		}
		
		if((runOption == RUN_FOREGROUND || runOption == RUN_BACKGROUND_ALLOW) && isShow && context instanceof Activity){
			C_progressDialog.getInstance((Activity)context, text);
			C_progressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					C_progressDialog.dismissInstance();
					complete.onCancelForegroundWait(dialog);
				}
			});
		}else if(text != null){
			C_progressDialog.setInstanceMessage(text);
		}
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				handler.sendMessage(runOptionDispatch(connecting(context, style, valueArray), runOption, isDismiss));
			}
		});
		thread.start();
	}
	
	public static void asyncConnection(final Activity activity, final int runOption, final DownLoadComplete complete, final int style
			, final String...valueArray){
		asyncConnection(activity, runOption, true, true, null, complete, style, valueArray);
	}
	
	public static void asyncConnection(final Activity activity, final int runOption, boolean isShow, final boolean isDismiss
			, final DownLoadComplete complete, final int style, final String...valueArray){
		asyncConnection(activity, runOption, isShow, isDismiss, null, complete, style, valueArray);
	}
	
	public static void asyncConnection(final Activity activity, final int runOption, String text, final DownLoadComplete complete
			, final int style, final String...valueArray){
		asyncConnection(activity, runOption, true, true, text, complete, style, valueArray);
	}
	
	private static Message noNetworkConnection(Context context, int runOption){
		ConnectionResult connectionResult = new ConnectionResult();
		connectionResult.setStatusMessage("Connect Fail No Network Connection");
		if(runOption != RUN_BACKGROUND_QUIET){
			Toast toast = Toast.makeText(context, context.getString(R.string.noNetworkConnection), Toast.LENGTH_SHORT);
			toast.show();
		}
		Message msg = new Message();
		msg.obj = connectionResult;
		return msg;
	}
	
	private static Message connecting(final Context context, final int style, final String...valueArray){
		ConnectionResult connectionResult;
		connectionResult = prepareData(context, style, valueArray);
		Message msg = new Message();
		msg.obj = connectionResult;
		if(connectionResult != null && !connectionResult.getStatusMessage().contains("Connect Fail ")){
			msg.what = 1;
		}
		return msg;
	}
	
	private static Message runOptionDispatch(Message msg, int runOption, boolean isDismiss){
		if((runOption == RUN_FOREGROUND || runOption == RUN_BACKGROUND_ALLOW)){
			if((isDismiss || msg.what == 0) && C_progressDialog.hasInstance() && C_progressDialog.getInstanceDialog().isShowing()){
				C_progressDialog.dismissInstance();
			}else if(runOption == RUN_FOREGROUND){
				msg.what = -1;
				return msg;
			}
		}
		return msg;
	}
	
	private static void reply(Message msg, DownLoadComplete complete){
		if(msg.what == 1){
			complete.loaded((ConnectionResult)msg.obj);
		}else if(msg.what == 0){
			complete.loadFail((ConnectionResult)msg.obj);
		}
	}
}