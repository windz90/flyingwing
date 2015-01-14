package com.andy.library.module.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 2.3.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_progressDialog{
	
	private static C_progressDialog progress;
	
	private Context context;
	private Dialog dialog;
	private LinearLayout linLay;
	private TextView textView;
	
	public C_progressDialog(Context context, String message){
		this.context = context;
		
		int itemWi;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		
		linLay = new LinearLayout(context);
		textView = new TextView(context);
		
		itemWi = (int)(dm.widthPixels * 0.8f);
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLay.setLayoutParams(linLayPar);
		linLay.setOrientation(LinearLayout.HORIZONTAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER);
		
		ProgressBar loadingBar = new ProgressBar(context);
		
		linLayPar = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(linLayPar);
		textView.setTextColor(0xFFC0C0C0);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		
		linLay.addView(loadingBar);
		linLay.addView(textView);
		
		textView.setText(message);
		
		dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(false);
		
		itemWi = (int)((dm.widthPixels / dm.heightPixels < 1 ? dm.widthPixels : dm.heightPixels) * 0.89f);
		WindowManager.LayoutParams windowLayPar = dialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = itemWi;
//		dialog.getWindow().setBackgroundDrawableResource(R.color.Transparent);
		dialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				dialog.show();
			}
		}else{
			dialog.show();
		}
	}
	
	public C_progressDialog(Context context){
		this(context, null);
	}
	
	public Dialog getDialog(){
		return dialog;
	}
	
	public void setCancelable(boolean isCancel){
		dialog.setCancelable(isCancel);
	}
	
	public void setMessage(final String message){
		if(textView == null){
			return;
		}
		if(context instanceof Activity){
			((Activity)context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					textView.setText(message);
				}
			});
		}else{
			textView.setText(message);
		}
	}
	
	public void appendMessage(final String message){
		if(textView == null){
			return;
		}
		if(context instanceof Activity){
			((Activity)context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					textView.append(message);
				}
			});
		}else{
			textView.append(message);
		}
	}
	
	public String getMessage(){
		if(textView == null){
			return null;
		}
		return textView.getText().toString();
	}
	
	public void dismiss(){
		if(dialog != null){
			dialog.dismiss();
			textView = null;
			linLay = null;
			dialog = null;
			context = null;
		}
	}
	
	public static C_progressDialog getInstance(Context context, String message){
		if(progress == null){
			progress = new C_progressDialog(context, message);
		}
		return progress;
	}
	
	public static C_progressDialog getInstance(Context context){
		return getInstance(context, null);
	}
	
	public static boolean hasInstance(){
		return progress == null ? false : true;
	}
	
	public static void setInstanceCancelable(boolean isCancel){
		if(progress == null){
			return;
		}
		progress.setCancelable(isCancel);
	}
	
	public static void setInstanceMessage(String message){
		if(progress == null){
			return;
		}
		progress.setMessage(message);
	}
	
	public static void appendInstanceMessage(String message){
		if(progress == null){
			return;
		}
		progress.appendMessage(message);
	}
	
	public static String getInstanceMessage(){
		if(progress == null){
			return null;
		}
		return progress.getMessage();
	}
	
	public static Dialog getInstanceDialog(){
		if(progress == null){
			return null;
		}
		return progress.getDialog();
	}
	
	public static void dismissInstance(){
		if(progress == null){
			return;
		}
		progress.dismiss();
		progress = null;
	}
}