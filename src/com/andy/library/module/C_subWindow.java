package com.andy.library.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.andy.library.R;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 2.3.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_subWindow {
	
	public interface ClickAction{
		public void action(View v, int clickIndex, Bundle bundle);
	}
	
	public static void alertBuilderOnlyMessage(Context context, String title, String message, final DialogInterface.OnClickListener click) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton(context.getString(R.string.close), click);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				click.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}
		});
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				alertDialogBuilder.show();
			}
		}else{
			alertDialogBuilder.show();
		}
	}
	
	public static void alertBuilderOnlyMessage(Context context, String message, DialogInterface.OnClickListener click) {
		alertBuilderOnlyMessage(context, null, message, click);
	}
	
	public static void alertBuilderOnlyMessage(Context context, String message) {
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		alertBuilderOnlyMessage(context, null, message, click);
	}
	
	public static void alertBuilderOnlyMessage(Context context, String title, String message) {
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		alertBuilderOnlyMessage(context, title, message, click);
	}
	
	public static void alertBuilderConfirm(final Context context, String title, String message, final ClickAction clickAction){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// DialogInterface.BUTTON_POSITIVE
				// DialogInterface.BUTTON_NEGATIVE
				clickAction.action(null, which, null);
			}
		};
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton(context.getString(R.string.ok), click);
		alertDialogBuilder.setNegativeButton(context.getString(R.string.cancel), click);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				click.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}
		});
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				alertDialogBuilder.show();
			}
		}else{
			alertDialogBuilder.show();
		}
	}
	
	public static void alertBuilderConfirm(final Context context, String title, final ClickAction clickAction){
		alertBuilderConfirm(context, title, context.getString(R.string.confirm), clickAction);
	}
	
	public static void alertBuilderQuit(final Activity activity, final Class<? extends Activity> quitCalss){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					Intent intent = new Intent(activity, quitCalss);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("quit", true);
					activity.startActivity(intent);
					activity.finish();
				}
			}
		};
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
		alertDialogBuilder.setTitle(activity.getString(R.string.quit));
		alertDialogBuilder.setMessage(activity.getString(R.string.confirm));
		alertDialogBuilder.setPositiveButton(activity.getString(R.string.ok), click);
		alertDialogBuilder.setNegativeButton(activity.getString(R.string.cancel), click);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				click.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}
		});
		if(!activity.isFinishing()){
			alertDialogBuilder.show();
		}
	}
	
	public static void alertMenuUseButton(final Context context, int width, int height, String title, final String[][] strArray
			, boolean isOutsideCancel, final ClickAction click){
		Resources res = context.getResources();
		
		int itemWi, itemHe, space;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = Utils.isFillScreen(dm, Utils.LIMIT_DIP_WIDTH);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		Button[] button = new Button[strArray.length];
		
		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(context);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(context);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		space = (int)(5 * dm.density);
		itemWi = width - space * 2;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		for(int i=0; i<button.length; i++){
			button[i] = new Button(context);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.color.selector_textcolor_item);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			}
			button[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			button[i].setText(strArray[i][1]);
			button[i].setEllipsize(TruncateAt.END);
			button[i].setMaxLines(2);
			scrollLinLay.addView(button[i]);
			
			TextPaint textPaint = button[i].getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setView(linLay, 0, 0, 0, 0);
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), onClick);
		alertDialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = alertDialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		alertDialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				alertDialog.show();
			}
		}else{
			alertDialog.show();
		}
		
		for(int i=0; i<button.length; i++){
			final int count = i;
			button[i].setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putString("itemId", strArray[count][0]);
					bundle.putString("itemTitle", strArray[count][1]);
					click.action(v, count, bundle);
					alertDialog.dismiss();
				}
			});
		}
	}
	
	public static void alertMenuUseButton(final Context context, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		alertMenuUseButton(context, width, height, title, strArray, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseButton(final Context context, int width, int height, String title, final String[][] strArray
			, boolean isOutsideCancel, final ClickAction click){
		Resources res = context.getResources();
		
		int itemWi, itemHe, space;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = Utils.isFillScreen(dm, Utils.LIMIT_DIP_WIDTH);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		final Button[] button = new Button[strArray.length + 1];
		TextView textView;
		
		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(context);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(context);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		space = (int)(5 * dm.density);
		itemWi = width - space * 2;
		itemHe = (int)(61.5f * 0.75f * dm.density);
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(context);
			textView.setLayoutParams(linLayPar);
			textView.setPadding(space, 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setTextColor(res.getColor(android.R.color.black));
			textView.setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			textView.setText(title);
			scrollLinLay.addView(textView);
			
			TextPaint textPaint = textView.getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLayPar.setMargins(space, 0, space, 0);
		for(int i=0; i<button.length; i++){
			button[i] = new Button(context);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.color.selector_textcolor_item);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			}
			button[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			if(i < button.length - 1){
				button[i].setText(strArray[i][1]);
			}else{
				button[i].setText(res.getString(R.string.cancel));
			}
			button[i].setEllipsize(TruncateAt.END);
			button[i].setMaxLines(2);
			scrollLinLay.addView(button[i]);
			
			TextPaint textPaint = button[i].getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = dialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				dialog.show();
			}
		}else{
			dialog.show();
		}
		
		for(int i=0; i<button.length; i++){
			final int count = i;
			button[i].setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(count < button.length - 1){
						Bundle bundle = new Bundle();
						bundle.putString("itemId", strArray[count][0]);
						bundle.putString("itemTitle", strArray[count][1]);
						click.action(v, count, bundle);
					}
					dialog.dismiss();
				}
			});
		}
	}
	
	public static void dialogMenuUseButton(final Context context, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		dialogMenuUseButton(context, width, height, title, strArray, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, int[] selectedArray, String title
			, ListAdapter adp, final boolean isMult, boolean isOutsideCancel, final ClickAction click) {
		Resources res = context.getResources();
		
		int itemWi, itemHe, space;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = Utils.isFillScreen(dm, Utils.LIMIT_DIP_WIDTH);
		
		LinearLayout linLay;
		LinearLayout linLayDetailHoriz;
		final ListView listView;
		
		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		space = (int)(5 * dm.density);
		if(topBar == null){
			TextView[] topView = new TextView[3];
			
			itemWi = width;
			linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
			linLayDetailHoriz = new LinearLayout(context);
			linLayDetailHoriz.setOrientation(LinearLayout.HORIZONTAL);
			linLayDetailHoriz.setBackgroundColor(0xFFC0C0C0);
			linLayDetailHoriz.setLayoutParams(linLayPar);
			linLayDetailHoriz.setPadding(space, 0, space, 0);
			linLayDetailHoriz.setGravity(Gravity.CENTER);
			
			for(int i=0; i<topView.length; i++){
				itemHe = (int)(61.5f * 1.0f * dm.density);
				topView[i] = new TextView(context);
				if(i == 0){
					linLayPar = new LayoutParams(itemHe, itemHe - space * 2);
					topView[i].setTag("left");
					topView[i].setBackgroundResource(android.R.color.white);
				}else if(i == 1){
					linLayPar = new LayoutParams((int)(itemWi - itemHe * 2 - space * 2), itemHe);
					topView[i].setTag("center");
				}else{
					linLayPar = new LayoutParams(itemHe, itemHe - space * 2);
				}
				topView[i].setLayoutParams(linLayPar);
				topView[i].setGravity(Gravity.CENTER);
				topView[i].setTextColor(res.getColor(android.R.color.black));
				if(i == 1){
					topView[i].setTextSize(Utils.getTextSize(Utils.SIZE_TITLE, isBigScreen));
					TextPaint txtPaint = topView[i].getPaint();
					txtPaint.setFakeBoldText(true);
				}else{
					topView[i].setTextSize(Utils.getTextSize(Utils.SIZE_TEXT, isBigScreen));
				}
				topView[i].setEllipsize(TruncateAt.END);
				topView[i].setMaxLines(2);
				linLayDetailHoriz.addView(topView[i]);
			}
			
			topBar = linLayDetailHoriz;
		}
		
		linLay.addView(topBar);
		
		if(topBar.findViewWithTag("left") instanceof TextView){
			((TextView)topBar.findViewWithTag("left")).setText(res.getString(R.string.cancel));
		}
		
		if(topBar.findViewWithTag("center") instanceof TextView){
			((TextView)topBar.findViewWithTag("center")).setText(title);
		}
		
		itemWi = width - space * 2;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, space);
		listView = new ListView(context);
		listView.setLayoutParams(linLayPar);
		listView.setFastScrollEnabled(true);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0x00000000);
		listView.setDivider(new ColorDrawable(0xFF808080));
		listView.setDividerHeight((int)(1 * dm.density));
		
		linLay.addView(listView);
		
		if(isMult){
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}else{
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		listView.setAdapter(adp);
		for(int i=0; i<selectedArray.length; i++){
			listView.setItemChecked(selectedArray[i], true);
			if(!isMult){
				listView.setSelection(selectedArray[i]);
			}
		}
		
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = dialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
		dialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				dialog.show();
			}
		}else{
			dialog.show();
		}
		
		if(topBar.findViewWithTag("left") instanceof View){
			topBar.findViewWithTag("left").setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putString("itemId", "" + position);
				bundle.putString("itemTitle", parent.getAdapter().getItem(position).toString());
				bundle.putBoolean("itemStatus", listView.isItemChecked(position));
				click.action(view, position, bundle);
				if(!isMult){
					dialog.dismiss();
				}
			}
		});
	}
	
	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, int resourceId, int[] viewIdArray, boolean isMult, boolean isOutsideCancel, ClickAction click) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for(int i=0; i<strArray.length; i++){
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("strArray", strArray[i]);
			list.add(hashMap);
		}
		SimpleAdapter adp = new SimpleAdapter(context, list, resourceId, new String[]{"strArray"}, viewIdArray);
		dialogMenuUseListView(context, topBar, width, height, selectedArray, title, adp, isMult, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, boolean isMult, boolean isOutsideCancel, ClickAction click) {
		int resourceId;
		int[] viewIdArray;
		if(isMult){
			resourceId = android.R.layout.simple_list_item_multiple_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}else{
			resourceId = android.R.layout.simple_list_item_single_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}
		dialogMenuUseListView(context, topBar, width, height, title, strArray, selectedArray, resourceId, viewIdArray, isMult, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseListView(Context context, View topBar, String title, String[] strArray, int[] selectedArray
			, boolean isMult, boolean isOutsideCancel, ClickAction click) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.79f);
		int height = (int)(dm.heightPixels * 0.7f);
		dialogMenuUseListView(context, topBar, width, height, title, strArray, selectedArray, isMult, isOutsideCancel, click);
	}
	
	public static void popupMenuUseButton(Context context, View view, int width, int height, String title, final String[][] strArray
			, final ClickAction click){
		Resources res = context.getResources();
		
		int itemWi;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = Utils.isFillScreen(dm, Utils.LIMIT_DIP_WIDTH);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		TextView textView;
		Button[] button = new Button[strArray.length];
		
		itemWi = width;
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setLayoutParams(linLayPar);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(context);
		scrollView.setLayoutParams(linLayPar);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(context);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setLayoutParams(linLayPar);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(context);
			textView.setLayoutParams(linLayPar);
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(res.getColor(android.R.color.black));
			textView.setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			textView.setText(title);
			scrollLinLay.addView(textView);
		}
		
		for(int i=0; i<button.length; i++){
			button[i] = new Button(context);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.color.selector_textcolor_item);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(android.R.color.black));
				e.printStackTrace();
			}
			button[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			button[i].setText(strArray[i][1]);
			button[i].setEllipsize(TruncateAt.END);
			button[i].setMaxLines(2);
			scrollLinLay.addView(button[i]);
			
			TextPaint textPaint = button[i].getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
		final PopupWindow popupWindow = new PopupWindow(context);
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(colorDrawable);
		popupWindow.setContentView(linLay);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				popupWindow.showAsDropDown(view);
			}
		}else{
			popupWindow.showAsDropDown(view);
		}
		
		for(int i=0; i<button.length; i++){
			final int count = i;
			button[i].setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putString("itemId", strArray[count][0]);
					bundle.putString("itemTitle", strArray[count][1]);
					click.action(v, count, bundle);
					popupWindow.dismiss();
				}
			});
		}
	}
	
	public static void popupMenuUseButton(Context context, View view, String title, final String[][] strArray, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels / 2;
		int height = LayoutParams.WRAP_CONTENT;
		popupMenuUseButton(context, view, width, height, title, strArray, click);
	}
}