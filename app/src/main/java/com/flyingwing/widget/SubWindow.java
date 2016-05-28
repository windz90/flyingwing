/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.3.14
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.flyingwing.R;
import com.flyingwing.util.DisplayUtils;
import com.flyingwing.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach", "Convert2Diamond"})
public class SubWindow {

	public static boolean sIsInstanceShow;

	public interface ClickAction {
		void action(View v, int clickIndex, Bundle bundle);
	}

	public static void alertBuilderMessage(Context context, String title, String message, final DialogInterface.OnClickListener click) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton(context.getString(R.string.close), click);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (click != null) {
					click.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
				}
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

	public static void alertBuilderMessage(Context context, String message, DialogInterface.OnClickListener click) {
		alertBuilderMessage(context, null, message, click);
	}

	public static void alertBuilderMessage(Context context, String title, String message) {
		alertBuilderMessage(context, title, message, null);
	}

	public static void alertBuilderMessage(Context context, String message) {
		alertBuilderMessage(context, null, message, null);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void popupMessage(Context context, final View view, boolean isSetLocation, int gravity, int x, int y, int width, int height
			, String[] strArray, final ClickAction clickAction){
		Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH);

		int space;
		LayoutParams linLayPar;

		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		View viewSpace;
		TextView[] textViews = new TextView[4];

		space = res.getDimensionPixelSize(R.dimen.dip5);
		linLayPar = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setLayoutParams(linLayPar);
		linLay.setPadding(space, space, space, space);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);

		linLayPar = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		scrollView = new ScrollView(context);
		scrollView.setLayoutParams(linLayPar);
		linLay.addView(scrollView);

		scrollLinLay = new LinearLayout(context);
		scrollLinLay.setOrientation(LinearLayout.VERTICAL);
		scrollLinLay.setLayoutParams(linLayPar);
		scrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		scrollView.addView(scrollLinLay);

		linLayPar = new LayoutParams(LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.dip10));
		viewSpace = new View(context);
		viewSpace.setLayoutParams(linLayPar);

		linLayPar = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		for(int i=0; i<textViews.length; i++){
			textViews[i] = new TextView(context);
			textViews[i].setLayoutParams(linLayPar);
			if(i < 2){
				textViews[i].setGravity(Gravity.CENTER_VERTICAL);
			}
			textViews[i].setTextColor(Color.BLACK);
			if(i == 0){
				textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			}else if(i == 1){
				textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_TITLE, isBigScreen));
			}else{
				textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_TEXT, isBigScreen));
			}
			if(i < 2){
				textViews[i].getPaint().setFakeBoldText(true);
				textViews[i].setEllipsize(TruncateAt.END);
				textViews[i].setMaxLines(2);
			}

			if(i < strArray.length && !TextUtils.isEmpty(strArray[i])){
				textViews[i].setText(strArray[i]);
			}else{
				textViews[i].setVisibility(View.GONE);
			}
		}

		scrollLinLay.addView(textViews[0]);
		scrollLinLay.addView(textViews[1]);
		scrollLinLay.addView(viewSpace);
		scrollLinLay.addView(textViews[2]);
		scrollLinLay.addView(textViews[3]);

		if((textViews[0].getVisibility() == View.GONE && textViews[1].getVisibility() == View.GONE) ||
				(textViews[2].getVisibility() == View.GONE && textViews[3].getVisibility() == View.GONE)){
			viewSpace.setVisibility(View.GONE);
		}

		PopupWindow popupWindow = new PopupWindow(context);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.drawable.dialog_holo_light_frame));
		}else{
			popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.drawable.dialog_frame));
		}
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setContentView(linLay);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				if(isSetLocation){
					popupWindow.showAtLocation(view, gravity, x, y);
				}else{
					popupWindow.showAsDropDown(view);
				}
			}
		}else if(isSetLocation){
			popupWindow.showAtLocation(view, gravity, x, y);
		}else{
			popupWindow.showAsDropDown(view);
		}

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				if(clickAction != null){
					clickAction.action(view, -1, null);
				}
			}
		});
	}

	public static void popupMessage(Context context, View view, boolean isSetLocation, int gravity, int x, int y, String[] strArray
			, ClickAction clickAction){
		popupMessage(context, view, isSetLocation, gravity, x, y, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, strArray, clickAction);
	}

	public static void popupMessage(Context context, View view, int width, int height, String[] strArray, ClickAction clickAction){
		popupMessage(context, view, false, Gravity.NO_GRAVITY, 0, 0, width, height, strArray, clickAction);
	}

	public static void popupMessage(Context context, View view, String[] strArray, ClickAction clickAction){
		popupMessage(context, view, false, Gravity.NO_GRAVITY, 0, 0, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, strArray, clickAction);
	}

	public static void alertBuilderConfirm(final Context context, String title, String message, final ClickAction clickAction){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// DialogInterface.BUTTON_POSITIVE
				// DialogInterface.BUTTON_NEGATIVE
				if(clickAction != null){
					clickAction.action(null, which, null);
				}
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

	public static void alertBuilderConfirm(final Context context, String message, final ClickAction clickAction){
		alertBuilderConfirm(context, context.getString(R.string.confirm), message, clickAction);
	}

	public static void alertBuilderQuit(final Activity activity, final Class<? extends Activity> quitClass){
		final DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					Intent intent = new Intent(activity, quitClass);
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
			, boolean isOutsideCancel, final ClickAction clickAction){
		Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH);

		int itemWi, itemHe, space;
		LayoutParams linLayPar;

		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		Button[] buttons = new Button[strArray.length];

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

		space = res.getDimensionPixelSize(R.dimen.dip5);
		itemWi = width - space * 2;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		for(int i=0; i<buttons.length; i++){
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(Gravity.CENTER);
			ColorStateList colorList = ContextCompat.getColorStateList(context, R.color.selector_textcolor_item);
			buttons[i].setTextColor(colorList);
			buttons[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			buttons[i].getPaint().setFakeBoldText(true);
			buttons[i].setEllipsize(TruncateAt.END);
			buttons[i].setMaxLines(2);
			scrollLinLay.addView(buttons[i]);

			buttons[i].setText(strArray[i][1]);
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

		alertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, -1, null);
				}
			}
		});

		for(int i=0; i<buttons.length; i++){
			final int count = i;
			buttons[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(clickAction != null){
						Bundle bundle = new Bundle();
						bundle.putString("itemId", strArray[count][0]);
						bundle.putString("itemTitle", strArray[count][1]);
						clickAction.action(v, count, bundle);
					}
					alertDialog.dismiss();
				}
			});
		}
	}

	public static void alertMenuUseButton(final Context context, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction clickAction){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		alertMenuUseButton(context, width, height, title, strArray, isOutsideCancel, clickAction);
	}

	public static RelativeLayout getTopBarWithTextView(Context context, DisplayMetrics displayMetrics, int width, int height
			, int btnWidth, int space){
		Resources res = context.getResources();

		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		RelativeLayout.LayoutParams relLayPar;

		RelativeLayout relLay;
		TextView[] textViews = new TextView[3];

		relLay = new RelativeLayout(context);
		relLay.setId(R.id.topLayout);
		relLay.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
		relLay.setPadding(space, 0, space, 0);

		for(int i=0; i<textViews.length; i++){
			textViews[i] = new TextView(context);
			if(i == 0){
				textViews[i].setId(R.id.topLeft);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				relLayPar.setMargins(0, space, space, space);
			}else if(i == 1){
				textViews[i].setId(R.id.topCenter);
				relLayPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
				relLayPar.addRule(RelativeLayout.RIGHT_OF, R.id.topLeft);
			}else{
				textViews[i].setId(R.id.topRight);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				relLayPar.setMargins(space, space, 0, space);
			}
			textViews[i].setLayoutParams(relLayPar);
			textViews[i].setGravity(Gravity.CENTER);
			textViews[i].setTextColor(Color.BLACK);
			if(i == 1){
				textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_TITLE, isBigScreen));
				textViews[i].getPaint().setFakeBoldText(true);
			}else{
				textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_TEXT, isBigScreen));
			}
			textViews[i].setEllipsize(TruncateAt.END);
			textViews[i].setMaxLines(2);
			relLay.addView(textViews[i]);
		}

		// 偏移位置設定
		relLayPar = (RelativeLayout.LayoutParams)textViews[1].getLayoutParams();
		relLayPar.addRule(RelativeLayout.LEFT_OF, R.id.topRight);

		return relLay;
	}

	public static RelativeLayout getTopBarWithSideImageView(Context context, DisplayMetrics displayMetrics, int width, int height
			, int btnWidth, int space){
		Resources res = context.getResources();

		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		RelativeLayout.LayoutParams relLayPar;

		RelativeLayout relLay;
		ImageView[] imageViews = new ImageView[2];
		TextView[] textViews = new TextView[1];

		relLay = new RelativeLayout(context);
		relLay.setId(R.id.topLayout);
		relLay.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
		relLay.setPadding(space, 0, space, 0);

		for(int i=0; i<imageViews.length; i++){
			imageViews[i] = new ImageView(context);
			if(i == 0){
				imageViews[i].setId(R.id.topLeft);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				relLayPar.setMargins(0, space, space, space);
			}else{
				imageViews[i].setId(R.id.topRight);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				relLayPar.setMargins(space, space, 0, space);
			}
			imageViews[i].setLayoutParams(relLayPar);
			imageViews[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
			relLay.addView(imageViews[i]);
		}

		for(int i=0; i<textViews.length; i++){
			relLayPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			relLayPar.addRule(RelativeLayout.RIGHT_OF, R.id.topLeft);
			textViews[i] = new TextView(context);
			textViews[i].setId(R.id.topCenter);
			textViews[i].setLayoutParams(relLayPar);
			textViews[i].setGravity(Gravity.CENTER);
			textViews[i].setTextColor(Color.BLACK);
			textViews[i].setTextSize(Utils.getTextSize(Utils.SIZE_TITLE, isBigScreen));
			textViews[i].getPaint().setFakeBoldText(true);
			textViews[i].setEllipsize(TruncateAt.END);
			textViews[i].setMaxLines(2);
			relLay.addView(textViews[i]);
		}

		// 偏移位置設定
		relLayPar = (RelativeLayout.LayoutParams)textViews[0].getLayoutParams();
		relLayPar.addRule(RelativeLayout.LEFT_OF, R.id.topRight);

		return relLay;
	}

	public static RelativeLayout getTopBarWithTextView(Context context, int width, int height, int btnWidth, int space){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return getTopBarWithTextView(context, displayMetrics, width, height, btnWidth, space);
	}

	public static RelativeLayout getTopBarWithSideImageView(Context context, int width, int height, int btnWidth, int space){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return getTopBarWithSideImageView(context, displayMetrics, width, height, btnWidth, space);
	}

	public static void dialogMenuUseButton(final Context context, int width, int height, String title, final String[][] strArray
			, boolean isOutsideCancel, final ClickAction clickAction){
		Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH);

		int itemWi, itemHe, space;
		LayoutParams linLayPar;

		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		final Button[] buttons = new Button[strArray.length + 1];
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

		space = res.getDimensionPixelSize(R.dimen.dip5);
		itemWi = width - space * 2;
		itemHe = (int)(61.5f * 0.75f * dm.density);
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(context);
			textView.setLayoutParams(linLayPar);
			textView.setPadding(space, 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setTextColor(Color.BLACK);
			textView.setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			textView.getPaint().setFakeBoldText(true);
			scrollLinLay.addView(textView);

			textView.setText(title);
		}

		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLayPar.setMargins(space, 0, space, 0);
		for(int i=0; i<buttons.length; i++){
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(Gravity.CENTER);
			ColorStateList colorList = ContextCompat.getColorStateList(context, R.color.selector_textcolor_item);
			buttons[i].setTextColor(colorList);
			buttons[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			buttons[i].getPaint().setFakeBoldText(true);
			buttons[i].setEllipsize(TruncateAt.END);
			buttons[i].setMaxLines(2);
			scrollLinLay.addView(buttons[i]);

			if(i < buttons.length - 1){
				buttons[i].setText(strArray[i][1]);
			}else{
				buttons[i].setText(res.getString(R.string.cancel));
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
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				dialog.show();
			}
		}else{
			dialog.show();
		}

		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, -1, null);
				}
			}
		});

		for(int i=0; i<buttons.length; i++){
			final int count = i;
			buttons[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(clickAction != null && count < strArray.length - 1){
						Bundle bundle = new Bundle();
						bundle.putString("itemId", strArray[count][0]);
						bundle.putString("itemTitle", strArray[count][1]);
						clickAction.action(v, count, bundle);
					}
					dialog.dismiss();
				}
			});
		}
	}

	public static void dialogMenuUseButton(final Context context, String title, final String[][] strArray, boolean isOutsideCancel
			, final ClickAction clickAction){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = (int)(dm.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		dialogMenuUseButton(context, width, height, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, int[] selectedArray, String title
			, ListAdapter adp, final boolean isMult, boolean isOutsideCancel, final ClickAction clickAction) {
		Resources res = context.getResources();

		int itemWi, itemHe, space;
		LayoutParams linLayPar;

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);

		LinearLayout linLay;
		final ListView listView;

		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);

		space = res.getDimensionPixelSize(R.dimen.dip5);
		if(topBar == null){
			itemWi = res.getDimensionPixelSize(R.dimen.dip72);
			itemHe = (int)(61.5f * 1.0f * dm.density);
			topBar = getTopBarWithTextView(context, dm, width, itemHe, itemWi, space);

			topBar.setBackgroundColor(0xFFC0C0C0);
			topBar.findViewById(R.id.topLeft).setBackgroundResource(android.R.color.white);

			if(topBar.findViewById(R.id.topLeft) instanceof TextView){
				((TextView)topBar.findViewById(R.id.topLeft)).setText(res.getString(R.string.cancel));
			}
		}

		linLay.addView(topBar);

		if(topBar.findViewById(R.id.topCenter) instanceof TextView){
			((TextView)topBar.findViewById(R.id.topCenter)).setText(title);
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
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.getWindow().setAttributes(windowLayPar);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				dialog.show();
			}
		}else{
			dialog.show();
		}

		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, -1, null);
				}
			}
		});

		if(topBar.findViewById(R.id.topLeft) != null){
			topBar.findViewById(R.id.topLeft).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(clickAction != null){
					Bundle bundle = new Bundle();
					bundle.putString("itemId", "" + position);
					bundle.putString("itemTitle", parent.getAdapter().getItem(position).toString());
					bundle.putBoolean("itemStatus", listView.isItemChecked(position));
					clickAction.action(view, position, bundle);
				}
				if(!isMult){
					dialog.dismiss();
				}
			}
		});
	}

	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, int resourceId, int[] viewIdArray, boolean isMult, boolean isOutsideCancel, ClickAction clickAction) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for(int i=0; i<strArray.length; i++){
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("strArray", strArray[i]);
			list.add(hashMap);
		}
		SimpleAdapter adp = new SimpleAdapter(context, list, resourceId, new String[]{"strArray"}, viewIdArray);
		dialogMenuUseListView(context, topBar, width, height, selectedArray, title, adp, isMult, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, boolean isMult, boolean isOutsideCancel, ClickAction clickAction) {
		int resourceId;
		int[] viewIdArray;
		if(isMult){
			resourceId = android.R.layout.simple_list_item_multiple_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}else{
			resourceId = android.R.layout.simple_list_item_single_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}
		dialogMenuUseListView(context, topBar, width, height, title, strArray, selectedArray, resourceId, viewIdArray, isMult, isOutsideCancel
				, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, String title, String[] strArray, int[] selectedArray
			, boolean isMult, boolean isOutsideCancel, ClickAction clickAction) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width, height;
		if(dm.widthPixels < dm.heightPixels){
			width = (int)(dm.widthPixels * 0.79f);
			height = (int)(dm.widthPixels * 0.95f);
		}else{
			width = (int)(dm.heightPixels * 0.79f);
			height = (int)(dm.heightPixels * 0.95f);
		}
		dialogMenuUseListView(context, topBar, width, height, title, strArray, selectedArray, isMult, isOutsideCancel, clickAction);
	}

	public static void popupMenuUseButton(Context context, final View view, int width, int height, String title, final String[][] strArray
			, final ClickAction clickAction){
		Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isBigScreen = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH);

		int itemWi;
		LayoutParams linLayPar;

		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		TextView textView;
		Button[] buttons = new Button[strArray.length];

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
			textView.setTextColor(Color.BLACK);
			textView.setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			textView.setText(title);
			scrollLinLay.addView(textView);
		}

		for(int i=0; i<buttons.length; i++){
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(Gravity.CENTER);
			ColorStateList colorList = ContextCompat.getColorStateList(context, R.color.selector_textcolor_item);
			buttons[i].setTextColor(colorList);
			buttons[i].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
			buttons[i].getPaint().setFakeBoldText(true);
			buttons[i].setEllipsize(TruncateAt.END);
			buttons[i].setMaxLines(2);
			scrollLinLay.addView(buttons[i]);

			buttons[i].setText(strArray[i][1]);
		}

		final PopupWindow popupWindow = new PopupWindow(context);
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
		popupWindow.setContentView(linLay);
		if(context instanceof Activity){
			if(!((Activity)context).isFinishing()){
				popupWindow.showAsDropDown(view);
			}
		}else{
			popupWindow.showAsDropDown(view);
		}

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				if(clickAction != null){
					clickAction.action(view, -1, null);
				}
			}
		});

		for(int i=0; i<buttons.length; i++){
			final int count = i;
			buttons[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(clickAction != null && count < strArray.length - 1){
						Bundle bundle = new Bundle();
						bundle.putString("itemId", strArray[count][0]);
						bundle.putString("itemTitle", strArray[count][1]);
						clickAction.action(v, count, bundle);
					}
					popupWindow.dismiss();
				}
			});
		}
	}

	public static void popupMenuUseButton(Context context, View view, String title, final String[][] strArray, final ClickAction clickAction){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels / 2;
		int height = LayoutParams.WRAP_CONTENT;
		popupMenuUseButton(context, view, width, height, title, strArray, clickAction);
	}
}