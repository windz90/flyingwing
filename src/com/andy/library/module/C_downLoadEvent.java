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
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.1.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_downLoadEvent {
	
	public static final int STYLE_TEST = 1;
	
	public static ConnectionResult updateTestData(Context context, String post1, String post2, String post3, String post4){
		String urlPost = "http://test.com";
		String[][] postData = new String[][]{{"post1", post1}, {"post2", post2}, {"post3", post3}
		, {"post4", post4}};
		ConnectionResult connectionResult = C_networkAccess.connectUseHttpClient(context, urlPost, postData);
		return connectionResult;
	}
	
	public static ConnectionResult prepareData(Context context, Handler handler, int style, String[] valueArray){
		ConnectionResult connectionResult = null;
		for(int i=0; i<valueArray.length; i++){
			if(valueArray[i] == null){
				valueArray[i] = "";
			}
		}
		if(style == STYLE_TEST){
			String post1 = valueArray[0];
			String post2 = valueArray[1];
			String post3 = valueArray[2];
			String post4 = valueArray[3];
			connectionResult = updateTestData(context, post1, post2, post3, post4);
		}
		return connectionResult;
	}
	
	public static final int RUN_FOREGROUND = 0x0A1;
	public static final int RUN_BACKGROUND_ALLOW = 0x0A2;
	public static final int RUN_BACKGROUND = 0x0A3;
	public static final int RUN_BACKGROUND_QUIET = 0x0A4;
	
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
		public void connectFail(ConnectionResult connectionResult){};
		public void connected(ConnectionResult connectionResult){};
		public abstract void loadFail(ConnectionResult connectionResult);
		public abstract void loaded(ConnectionResult connectionResult);
		public void onCancelForegroundWait(DialogInterface dialog){};
	}
	
	public static void baseConnection(Context context, Looper looper, final DownLoadComplete complete, final int style, final String...valueArray){
		if(!C_networkAccess.isConnect(context)){
			reply(noNetworkConnection(context, RUN_BACKGROUND_QUIET), complete);
			return;
		}
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, complete);
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
		reply(connecting(context, handler, style, valueArray), complete);
	}
	
	public static void baseConnection(Context context, final DownLoadComplete complete, final int style, final String...valueArray){
		baseConnection(context, null, complete, style, valueArray);
	}
	
	public static void customConnection(final Context context, Looper looper, final DownLoadSetting setting, final DownLoadComplete complete
			, final int style, final String...valueArray){
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, complete);
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
		if(!C_networkAccess.isConnect(context)){
			Message msg = runOptionDispatch(noNetworkConnection(context, setting.runOption), setting.runOption, setting.isDialogDismiss);
			if(setting.isUseHandler){
				if(handler == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handler.sendMessage(msg);
				}
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
			final Handler handlerCopy = handler;
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					Message msg = runOptionDispatch(connecting(context, handlerCopy, style, valueArray), setting.runOption, setting.isDialogDismiss);
					if(setting.isUseHandler){
						if(handlerCopy == null){
							Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
						}else{
							handlerCopy.sendMessage(msg);
						}
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
			Message msg = runOptionDispatch(connecting(context, handler, style, valueArray), setting.runOption, setting.isDialogDismiss);
			if(setting.isUseHandler){
				if(handler == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handler.sendMessage(msg);
				}
			}else{
				reply(msg, complete);
			}
		}
	}
	
	public static void customConnection(final Context context, final DownLoadSetting setting, final DownLoadComplete complete
			, final int style, final String...valueArray){
		customConnection(context, null, setting, complete, style, valueArray);
	}
	
	public static void syncConnection(final Context context, Looper looper, final DownLoadComplete complete, final int style
			, final String...valueArray){
		Handler handler = new Handler(looper, new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				reply(msg, complete);
				return false;
			}
		});
		if(!C_networkAccess.isConnect(context)){
			handler.sendMessage(noNetworkConnection(context, 0));
			return;
		}
		handler.sendMessage(connecting(context, handler, style, valueArray));
	}
	
	public static void asyncConnection(final Context context, Looper looper, final int runOption, boolean isShow, final boolean isDismiss
			, String text, final DownLoadComplete complete, final int style, final String...valueArray){
		Handler handler = null;
		try {
			Callback callback = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					reply(msg, complete);
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
		if(!C_networkAccess.isConnect(context)){
			if(handler == null){
				Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
			}else{
				handler.sendMessage(runOptionDispatch(noNetworkConnection(context, runOption), runOption, isDismiss));
			}
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
		final Handler handlerCopy = handler;
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(handlerCopy == null){
					Log.w("Handler", "Can't create handler inside thread that has not called Looper.prepare()");
				}else{
					handlerCopy.sendMessage(runOptionDispatch(connecting(context, handlerCopy, style, valueArray), runOption, isDismiss));
				}
			}
		});
		thread.start();
	}
	
	public static void asyncConnection(final Context context, final int runOption, boolean isShow, final boolean isDismiss
			, String text, final DownLoadComplete complete, final int style, final String...valueArray){
		asyncConnection(context, null, runOption, isShow, isDismiss, text, complete, style, valueArray);
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
	
	private static Message connecting(final Context context, Handler handler, final int style, final String...valueArray){
		ConnectionResult connectionResult = prepareData(context, handler, style, valueArray);
		Message msg = new Message();
		msg.obj = connectionResult;
		if(connectionResult == null || connectionResult.getStatusMessage().contains("Connect Fail ")){
			msg.what = C_networkAccess.CONNECTION_LOAD_FAIL;
		}else{
			msg.what = C_networkAccess.CONNECTION_LOADED;
		}
		return msg;
	}
	
	private static Message runOptionDispatch(Message msg, int runOption, boolean isDismiss){
		if((runOption == RUN_FOREGROUND || runOption == RUN_BACKGROUND_ALLOW)){
			if((isDismiss || msg.what == C_networkAccess.CONNECTION_LOAD_FAIL) && 
					C_progressDialog.hasInstance() && C_progressDialog.getInstanceDialog().isShowing()){
				C_progressDialog.dismissInstance();
			}else if(runOption == RUN_FOREGROUND){
				msg.what = -1;
				return msg;
			}
		}
		return msg;
	}
	
	private static void reply(Message msg, DownLoadComplete complete){
		if(msg.what == 0 || msg.what == C_networkAccess.CONNECTION_CONNECT_FAIL){
			complete.connectFail((ConnectionResult)msg.obj);
			complete.loadFail((ConnectionResult)msg.obj);
		}else if(msg.what == C_networkAccess.CONNECTION_CONNECTED){
			complete.connected((ConnectionResult)msg.obj);
		}else if(msg.what == C_networkAccess.CONNECTION_LOAD_FAIL){
			complete.loadFail((ConnectionResult)msg.obj);
		}else if(msg.what == C_networkAccess.CONNECTION_LOADED){
			complete.loaded((ConnectionResult)msg.obj);
		}
	}
}