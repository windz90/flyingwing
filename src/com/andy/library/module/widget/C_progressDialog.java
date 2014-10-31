package com.andy.library.module.widget;

import com.andy.library.R;

import android.app.Activity;
import android.app.Dialog;
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
 * @version 2.3.2
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_progressDialog{
	
	private static C_progressDialog progress;
	
	private Activity activity;
	private Dialog dialog;
	private LinearLayout linLay;
	private TextView textView;
	
	public C_progressDialog(Activity activity, String message){
		this.activity = activity;
		
		int itemWi;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		linLay = new LinearLayout(activity);
		textView = new TextView(activity);
		
		itemWi = (int)(dm.widthPixels * 0.8f);
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLay.setLayoutParams(linLayPar);
		linLay.setOrientation(LinearLayout.HORIZONTAL);
		linLay.setBackgroundResource(R.color.White);
		linLay.setGravity(Gravity.CENTER);
		
		ProgressBar loadingBar = new ProgressBar(activity);
		
		linLayPar = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(linLayPar);
		textView.setTextColor(activity.getResources().getColor(R.color.WhiteGray));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		
		linLay.addView(loadingBar);
		linLay.addView(textView);
		
		textView.setText(message);
		
		dialog = new Dialog(activity);
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
		if(!activity.isFinishing()){
			dialog.show();
		}
	}
	
	public C_progressDialog(Activity activity){
		this(activity, null);
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
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				textView.setText(message);
			}
		});
	}
	
	public void appendMessage(final String message){
		if(textView == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				textView.append(message);
			}
		});
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
			activity = null;
		}
	}
	
	public static C_progressDialog getInstance(Activity activity, String message){
		if(progress == null){
			progress = new C_progressDialog(activity, message);
		}
		return progress;
	}
	
	public static C_progressDialog getInstance(Activity activity){
		return getInstance(activity, null);
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