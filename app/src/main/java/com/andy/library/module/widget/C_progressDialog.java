/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.3.5
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

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

@SuppressWarnings("unused")
public class C_progressDialog {
	
	private static C_progressDialog sProgress;
	
	private Context mContext;
	private Dialog mDialog;
	private LinearLayout mLinLay;
	private TextView mTextView;
	
	public C_progressDialog(Context context, String message){
		mContext = context;
		
		int itemWi;
		LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		
		mLinLay = new LinearLayout(context);
		mTextView = new TextView(context);
		
		itemWi = (int)(dm.widthPixels * 0.8f);
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		mLinLay.setLayoutParams(linLayPar);
		mLinLay.setOrientation(LinearLayout.HORIZONTAL);
		mLinLay.setBackgroundResource(android.R.color.white);
		mLinLay.setGravity(Gravity.CENTER);
		
		ProgressBar loadingBar = new ProgressBar(context);
		
		linLayPar = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mTextView.setLayoutParams(linLayPar);
		mTextView.setTextColor(0xFFC0C0C0);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		
		mLinLay.addView(loadingBar);
		mLinLay.addView(mTextView);
		
		mTextView.setText(message);
		
		mDialog = new Dialog(context);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(mLinLay);
		mDialog.setCanceledOnTouchOutside(false);
		
		itemWi = (int)((dm.widthPixels / dm.heightPixels < 1 ? dm.widthPixels : dm.heightPixels) * 0.89f);
		WindowManager.LayoutParams windowLayPar = mDialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = itemWi;
//		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		mDialog.getWindow().setAttributes(windowLayPar);
	}
	
	public C_progressDialog(Context context){
		this(context, null);
	}
	
	public Dialog getDialog(){
		return mDialog;
	}
	
	public void setCancelable(boolean isCancel){
		mDialog.setCancelable(isCancel);
	}
	
	public void setMessage(final String message){
		if(mTextView == null){
			return;
		}
		if(mContext instanceof Activity){
			((Activity)mContext).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mTextView.setText(message);
				}
			});
		}else{
			mTextView.setText(message);
		}
	}
	
	public void appendMessage(final String message){
		if(mTextView == null){
			return;
		}
		if(mContext instanceof Activity){
			((Activity)mContext).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mTextView.append(message);
				}
			});
		}else{
			mTextView.append(message);
		}
	}
	
	public String getMessage(){
		return mTextView == null ? null : mTextView.getText().toString();
	}
	
	public boolean isShowing(){
		return mDialog != null && mDialog.isShowing();
	}
	
	public void show(){
		if(mDialog == null || mDialog.isShowing()){
			return;
		}
		if(mContext instanceof Activity){
			((Activity)mContext).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(!((Activity)mContext).isFinishing()){
						mDialog.show();
					}
				}
			});
		}else{
			mDialog.show();
		}
	}
	
	public void show(String message){
		setMessage(message);
		show();
	}
	
	public void hide(){
		if(mDialog != null){
			mDialog.hide();
		}
	}
	
	public void dismiss(){
		if(mDialog != null){
			mDialog.dismiss();
		}
	}
	
	public void clear(){
		if(mDialog != null){
			mDialog.dismiss();
			mTextView = null;
			mLinLay = null;
			mDialog = null;
			mContext = null;
		}
	}
	
	public static C_progressDialog getInstance(Context context, String message){
		if(sProgress == null){
			sProgress = new C_progressDialog(context, message);
		}else{
			sProgress.setMessage(message);
		}
		return sProgress;
	}
	
	public static C_progressDialog getInstance(Context context){
		return getInstance(context, null);
	}
	
	public static C_progressDialog getInstance(){
		return sProgress;
	}
	
	public static boolean hasInstance(){
		return sProgress != null;
	}
	
	public static Dialog getInstanceDialog(){
		return sProgress == null ? null : sProgress.getDialog();
	}
	
	public static void setInstanceCancelable(boolean isCancel){
		if(sProgress != null){
			sProgress.setCancelable(isCancel);
		}
	}
	
	public static void setInstanceMessage(String message){
		if(sProgress != null){
			sProgress.setMessage(message);
		}
	}
	
	public static void appendInstanceMessage(String message){
		if(sProgress != null){
			sProgress.appendMessage(message);
		}
	}
	
	public static String getInstanceMessage(){
		return sProgress == null ? null : sProgress.getMessage();
	}
	
	public static boolean isInstanceShowing(){
		return sProgress != null && sProgress.isShowing();
	}
	
	public static void showInstance(){
		if(sProgress != null){
			sProgress.show();
		}
	}
	
	public static void showInstance(String message){
		if(sProgress != null){
			sProgress.show(message);
		}
	}
	
	public static void hideInstance(){
		if(sProgress != null){
			sProgress.hide();
		}
	}
	
	public static void dismissInstance(){
		if(sProgress != null){
			sProgress.dismiss();
		}
	}
	
	public static void clearInstance(){
		if(sProgress != null){
			sProgress.clear();
			sProgress = null;
		}
	}
}