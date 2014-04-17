package com.andy.library.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.andy.library.R;
import com.andy.library.view.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 2.2.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_subWindow {
	
	public interface ClickAction{
		public void action(View v, int clickIndex, Bundle bundle);
	}
	
	public static void alertBuilderOnlyMessage(Activity activity, String title, String message, final DialogInterface.OnClickListener click) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton(activity.getString(R.string.close), click);
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
	
	public static void alertBuilderOnlyMessage(Activity activity, String message, DialogInterface.OnClickListener click) {
		alertBuilderOnlyMessage(activity, null, message, click);
	}
	
	public static void alertBuilderOnlyMessage(Activity activity, String message) {
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		alertBuilderOnlyMessage(activity, null, message, click);
	}
	
	public static void alertBuilderOnlyMessage(Activity activity, String title, String message) {
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		alertBuilderOnlyMessage(activity, title, message, click);
	}
	
	public static void alertBuilderQuit(final Activity activity){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					Intent intent = new Intent(activity, Main.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("quit", true);
					activity.startActivity(intent);
					activity.finish();
				}
			}
		};
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
		alertDialogBuilder.setTitle(activity.getString(R.string.quit));
		alertDialogBuilder.setMessage(activity.getString(R.string.sendConfirm));
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
	
	public static void alertBuilderConfirm(final Activity activity, String title, String message, final ClickAction clickAction){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// DialogInterface.BUTTON_POSITIVE
				// DialogInterface.BUTTON_NEGATIVE
				clickAction.action(null, which, null);
			}
		};
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
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
	
	public static void alertBuilderConfirm(final Activity activity, String title, final ClickAction clickAction){
		alertBuilderConfirm(activity, title, activity.getString(R.string.sendConfirm), clickAction);
	}
	
	public static void alertMenuUseButton(final Activity activity, int width, int height, String title, final String[][] strArray
			, boolean isOutsideCancel, final ClickAction click){
		Resources res = activity.getResources();
		int itemWi, itemHe;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		Button[] button = new Button[strArray.length];
		
		linLay = new LinearLayout(activity);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(R.color.White);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(activity);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(activity);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		itemWi = width;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi, itemHe);
		for(int i=0; i<button.length; i++){
			button[i] = new Button(activity);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.drawable.selector_textcolor_custom_adapter);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			}
			Utils.setTextSizeMethod(activity, button[i], Utils.getTextSize(activity, Utils.SIZE_SUBJECT));
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
		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle(title);
		alertDialog.setView(linLay, 0, 0, 0, 0);
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.cancel), onClick);
		alertDialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = alertDialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		alertDialog.getWindow().setBackgroundDrawableResource(R.color.Transparent);
		alertDialog.getWindow().setAttributes(windowLayPar);
		if(!activity.isFinishing()){
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
	
	public static void alertMenuUseButton(final Activity activity, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		alertMenuUseButton(activity, width, height, title, strArray, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseButton(final Activity activity, int width, int height, String title, final String[][] strArray
			, boolean isOutsideCancel, final ClickAction click){
		Resources res = activity.getResources();
		int itemWi, itemHe;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		final Button[] button = new Button[strArray.length + 1];
		TextView textView;
		
		linLay = new LinearLayout(activity);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(R.color.White);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(activity);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(activity);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		itemWi = width;
		itemHe = (int)(61.5f * 0.75f * dm.density);
		linLayPar = new LayoutParams(itemWi, itemHe);
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(activity);
			textView.setLayoutParams(linLayPar);
			textView.setPadding((int)(10 * dm.density), 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setTextColor(res.getColor(R.color.Black));
			Utils.setTextSizeMethod(activity, textView, Utils.getTextSize(activity, Utils.SIZE_SUBJECT));
			textView.setText(title);
			scrollLinLay.addView(textView);
			
			TextPaint textPaint = textView.getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		for(int i=0; i<button.length; i++){
			button[i] = new Button(activity);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.drawable.selector_textcolor_custom_adapter);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			}
			Utils.setTextSizeMethod(activity, button[i], Utils.getTextSize(activity, Utils.SIZE_SUBJECT));
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
		
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = dialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		dialog.getWindow().setBackgroundDrawableResource(R.color.Transparent);
		dialog.getWindow().setAttributes(windowLayPar);
		if(!activity.isFinishing()){
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
	
	public static void dialogMenuUseButton(final Activity activity, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		dialogMenuUseButton(activity, width, height, title, strArray, isOutsideCancel, click);
	}
	
	public static void dialogMenuUseListView(Activity activity, View topBar, int width, int height, String title, final String[] strArray
			, int[] selectedArray, final boolean isMult, boolean isOutsideCancel, final ClickAction click) {
		Resources res = activity.getResources();
		int itemWi, itemHe, space;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		LinearLayout linLay;
		LinearLayout linLayDetailHoriz;
		final ListView listView;
		
		linLay = new LinearLayout(activity);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(R.color.White);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		space = (int)(10 * dm.density);
		if(topBar == null){
			TextView[] topView = new TextView[3];
			
			itemWi = width;
			linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
			linLayDetailHoriz = new LinearLayout(activity);
			linLayDetailHoriz.setOrientation(LinearLayout.HORIZONTAL);
			linLayDetailHoriz.setBackgroundResource(R.color.WhiteGray);
			linLayDetailHoriz.setLayoutParams(linLayPar);
			linLayDetailHoriz.setPadding(space, 0, space, 0);
			linLayDetailHoriz.setGravity(Gravity.CENTER);
			
			for(int i=0; i<topView.length; i++){
				itemHe = (int)(61.5f * 1.0f * dm.density);
				topView[i] = new TextView(activity);
				if(i == 0){
					linLayPar = new LayoutParams(itemHe, itemHe - space * 2);
					topView[i].setTag("left");
					topView[i].setBackgroundResource(R.color.White);
				}else if(i == 1){
					linLayPar = new LayoutParams((int)(itemWi - itemHe * 2 - space * 2), itemHe);
					topView[i].setTag("center");
				}else{
					linLayPar = new LayoutParams(itemHe, itemHe - space * 2);
				}
				topView[i].setLayoutParams(linLayPar);
				topView[i].setGravity(Gravity.CENTER);
				topView[i].setTextColor(res.getColor(R.color.Black));
				if(i == 1){
					Utils.setTextSizeMethod(activity, topView[i], Utils.getTextSize(activity, Utils.SIZE_TITLE));
					TextPaint txtPaint = topView[i].getPaint();
					txtPaint.setFakeBoldText(true);
				}else{
					Utils.setTextSizeMethod(activity, topView[i], Utils.getTextSize(activity, Utils.SIZE_TEXT));
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
		
		itemWi = width;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi - space * 2, itemHe);
		linLayPar.setMargins(space, 0, space, space);
		listView = new ListView(activity);
		listView.setLayoutParams(linLayPar);
		listView.setFastScrollEnabled(true);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0x00000000);
		listView.setDivider(res.getDrawable(R.color.Gray));
		listView.setDividerHeight((int)(1 * dm.density));
		
		linLay.addView(listView);
		
		int resource;
		int[] viewId;
		if(isMult){
			resource = R.layout.list_item_multiple_choice;
			viewId = new int[]{R.id.multiText1};
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}else{
			resource = R.layout.list_item_single_choice;
			viewId = new int[]{R.id.singleText1};
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for(int i=0; i<strArray.length; i++){
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("strArray", strArray[i]);
			list.add(hashMap);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(activity, list, resource, new String[]{"strArray"}, viewId);
		listView.setAdapter(simpleAdapter);
		for(int i=0; i<selectedArray.length; i++){
			listView.setItemChecked(selectedArray[i], true);
			if(!isMult){
				listView.setSelection(selectedArray[i]);
			}
		}
		
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(isOutsideCancel);
		
		WindowManager.LayoutParams windowLayPar = dialog.getWindow().getAttributes();
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.width = width;
		windowLayPar.height = height;
		dialog.getWindow().setBackgroundDrawableResource(R.color.White);
		dialog.getWindow().setAttributes(windowLayPar);
		if(!activity.isFinishing()){
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putString("itemId", "" + position);
				bundle.putString("itemTitle", strArray[position]);
				bundle.putBoolean("itemStatus", listView.isItemChecked(position));
				click.action(view, position, bundle);
				if(!isMult){
					dialog.dismiss();
				}
			}
		});
	}
	
	public static void dialogMenuUseListView(Activity activity, View topBar, String title, final String[] strArray, int[] selectedArray
			, final boolean isMult, boolean isOutsideCancel, final ClickAction click) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.79f);
		int height = (int)(dm.heightPixels * 0.7f);
		dialogMenuUseListView(activity, topBar, width, height, title, strArray, selectedArray, isMult, isOutsideCancel, click);
	}
	
	public static void popupMenuUseButton(Activity activity, View view, int width, int height, String title, final String[][] strArray
			, final ClickAction click){
		Resources res = activity.getResources();
		int itemWi;
		LinearLayout.LayoutParams linLayPar;
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		TextView textView;
		Button[] button = new Button[strArray.length];
		
		itemWi = width;
		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLay = new LinearLayout(activity);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(R.color.White);
		linLay.setLayoutParams(linLayPar);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);
		
		scrollView = new ScrollView(activity);
		scrollView.setLayoutParams(linLayPar);
		linLay.addView(scrollView);
		
		scrollLinLay = new LinearLayout(activity);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setLayoutParams(linLayPar);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);
		
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(activity);
			textView.setLayoutParams(linLayPar);
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(res.getColor(R.color.Black));
			Utils.setTextSizeMethod(activity, textView, Utils.getTextSize(activity, Utils.SIZE_SUBJECT));
			textView.setText(title);
			scrollLinLay.addView(textView);
		}
		
		for(int i=0; i<button.length; i++){
			button[i] = new Button(activity);
			button[i].setLayoutParams(linLayPar);
			button[i].setPadding(0, 0, 0, 0);
			button[i].setGravity(Gravity.CENTER);
			XmlPullParser xpp = res.getXml(R.drawable.selector_textcolor_custom_adapter);
			try {
				ColorStateList colorList = ColorStateList.createFromXml(res, xpp);
				button[i].setTextColor(colorList);
			} catch (XmlPullParserException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			} catch (IOException e) {
				button[i].setTextColor(res.getColor(R.color.Black));
				e.printStackTrace();
			}
			Utils.setTextSizeMethod(activity, button[i], Utils.getTextSize(activity, Utils.SIZE_SUBJECT));
			button[i].setText(strArray[i][1]);
			button[i].setEllipsize(TruncateAt.END);
			button[i].setMaxLines(2);
			scrollLinLay.addView(button[i]);
			
			TextPaint textPaint = button[i].getPaint();
			textPaint.setFakeBoldText(true);
		}
		
		ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
		final PopupWindow popupWindow = new PopupWindow(activity);
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(colorDrawable);
		popupWindow.setContentView(linLay);
		if(!activity.isFinishing()){
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
	
	public static void popupMenuUseButton(Activity activity, View view, String title, final String[][] strArray, final ClickAction click){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels / 2;
		int height = LayoutParams.WRAP_CONTENT;
		popupMenuUseButton(activity, view, width, height, title, strArray, click);
	}
}