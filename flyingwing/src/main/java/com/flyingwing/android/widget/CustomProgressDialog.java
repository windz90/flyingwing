/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.3.9
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CustomProgressDialog {

	private static final class StaticNestedClass {
		@SuppressLint("StaticFieldLeak")
		private static final CustomProgressDialog INSTANCE = new CustomProgressDialog();
	}

	private Context mContext;
	private Dialog mDialog;
	private RelativeLayout mRelativeLayout;
	private TextView mTextView;

	public void createDialog(Context context, String message){
		mContext = context;

		int itemWi, itemHe, space;
		RelativeLayout.LayoutParams relativeLayoutParams;

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);

		itemWi = (int)(dm.widthPixels * 0.8f);
		relativeLayoutParams = new RelativeLayout.LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		mRelativeLayout = new RelativeLayout(context);
		mRelativeLayout.setBackgroundResource(android.R.color.white);
		mRelativeLayout.setLayoutParams(relativeLayoutParams);
		mRelativeLayout.setGravity(Gravity.CENTER);

		relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		ProgressBar progressBar = new ProgressBar(context);
		progressBar.setId(android.R.id.progress);
		progressBar.setLayoutParams(relativeLayoutParams);
		mRelativeLayout.addView(progressBar);

		relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		relativeLayoutParams.addRule(RelativeLayout.RIGHT_OF, progressBar.getId());
		relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mTextView = new TextView(context);
		mTextView.setId(android.R.id.content);
		mTextView.setLayoutParams(relativeLayoutParams);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		mRelativeLayout.addView(mTextView);

		if(TextUtils.isEmpty(message)){
			mTextView.setVisibility(View.GONE);
		}

		mTextView.setText(message);

		mDialog = new Dialog(context);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(mRelativeLayout);
		mDialog.setCanceledOnTouchOutside(false);

		Window window = mDialog.getWindow();
		if(window != null){
			itemWi = (int)((dm.widthPixels / dm.heightPixels < 1 ? dm.widthPixels : dm.heightPixels) * 0.89f);
			WindowManager.LayoutParams windowManagerLayoutParams = window.getAttributes();
			windowManagerLayoutParams.x = 0;
			windowManagerLayoutParams.y = 0;
			windowManagerLayoutParams.width = itemWi;
//			window.setBackgroundDrawableResource(android.R.color.transparent);
			window.setAttributes(windowManagerLayoutParams);
		}
	}

	public void createDialog(Context context){
		createDialog(context, null);
	}

	public void setDialog(Dialog dialog){
		mDialog = dialog;
	}

	public Dialog getDialog(){
		return mDialog;
	}
	
	public TextView getTextView(){
		return mTextView;
	}

	public void setCancelable(boolean isCancel){
		mDialog.setCancelable(isCancel);
	}

	public void setMessage(final String message){
		if(mTextView == null){
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayout.findViewById(android.R.id.progress).getLayoutParams();
				if(TextUtils.isEmpty(message)){
					relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					mTextView.setVisibility(View.GONE);
				}else{
					relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
					mTextView.setVisibility(View.VISIBLE);
				}
				mTextView.setText(message);
			}
		};
		if(mContext instanceof Activity){
			((Activity)mContext).runOnUiThread(runnable);
		}else{
			runnable.run();
		}
	}

	public void appendMessage(final String message){
		if(mTextView == null){
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayout.findViewById(android.R.id.progress).getLayoutParams();
				if(mTextView.getText().length() == 0 && TextUtils.isEmpty(message)){
					relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					mTextView.setVisibility(View.GONE);
				}else{
					relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
					mTextView.setVisibility(View.VISIBLE);
				}
				mTextView.append(message);
			}
		};
		if(mContext instanceof Activity){
			((Activity)mContext).runOnUiThread(runnable);
		}else{
			runnable.run();
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
			mRelativeLayout = null;
			mDialog = null;
			mContext = null;
		}
	}

	public static CustomProgressDialog getInstance(Context context, String message){
		if(StaticNestedClass.INSTANCE.getDialog() == null){
			StaticNestedClass.INSTANCE.createDialog(context, message);
		}else{
			StaticNestedClass.INSTANCE.setMessage(message);
		}
		return StaticNestedClass.INSTANCE;
	}

	public static CustomProgressDialog getInstance(Context context){
		return getInstance(context, null);
	}

	public static boolean hasInstanceDialog(){
		return StaticNestedClass.INSTANCE.getDialog() != null;
	}

	public static Dialog getInstanceDialog(){
		return StaticNestedClass.INSTANCE.getDialog();
	}

	public static TextView getInstanceTextView(){
		return StaticNestedClass.INSTANCE.getTextView();
	}

	public static void setInstanceCancelable(boolean isCancel){
		StaticNestedClass.INSTANCE.setCancelable(isCancel);
	}

	public static void setInstanceMessage(String message){
		StaticNestedClass.INSTANCE.setMessage(message);
	}

	public static void appendInstanceMessage(String message){
		StaticNestedClass.INSTANCE.appendMessage(message);
	}

	public static String getInstanceMessage(){
		return StaticNestedClass.INSTANCE.getMessage();
	}

	public static boolean isInstanceShowing(){
		return StaticNestedClass.INSTANCE.isShowing();
	}

	public static void showInstance(Context context, String message){
		getInstance(context, message).show();
	}

	public static void showInstance(Context context){
		getInstance(context, null).show();
	}

	public static void hideInstance(){
		StaticNestedClass.INSTANCE.hide();
	}

	public static void dismissInstance(){
		StaticNestedClass.INSTANCE.dismiss();
	}

	public static void clearInstance(){
		StaticNestedClass.INSTANCE.clear();
	}
}