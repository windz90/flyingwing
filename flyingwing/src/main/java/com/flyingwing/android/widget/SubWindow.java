/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.4.8
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
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

import com.flyingwing.android.R;
import com.flyingwing.android.util.DisplayUtils;
import com.flyingwing.android.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess", "ForLoopReplaceableByForEach", "Convert2Diamond"})
public class SubWindow {

	public static boolean sIsInstanceShow;

	public interface ClickAction {
		void action(View v, int which, Bundle bundle);
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
		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		alertDialogBuilder.show();
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
	public static void popupMessage(Context context, final View anchorView, Drawable drawableBackground, boolean isSetLocation, int gravity, int x, int y
			, int width, int height, TextViewAttribute[] textViewAttributes, String[] strArray, final ClickAction clickAction){
		Resources res = context.getResources();

		int space;
		LayoutParams linLayPar;

		LinearLayout linLay;
		ScrollView scrollView;
		LinearLayout scrollLinLay;
		TextView[] textViews = new TextView[strArray.length];

		space = res.getDimensionPixelSize(R.dimen.dip8);
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

		linLayPar = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextViewAttribute textViewAttribute = new TextViewAttribute();
		for(int i=0; i<textViews.length; i++){
			if(i < textViewAttributes.length - 1){
				textViewAttribute = textViewAttributes[i];
			}
			textViews[i] = new TextView(context);
			textViews[i].setLayoutParams(linLayPar);
			textViews[i].setGravity(textViewAttribute.getGravity());
			if(textViewAttribute.getTextColor() != null){
				textViews[i].setTextColor(textViewAttribute.getTextColor());
			}
			textViews[i].setTextSize(textViewAttribute.getTextSize());
			textViews[i].setTypeface(textViewAttribute.getTypeface());
			textViews[i].setEllipsize(textViewAttribute.getEllipsize());
			textViews[i].setMaxLines(textViewAttribute.getMaxLines());

			if(i < strArray.length && !TextUtils.isEmpty(strArray[i])){
				textViews[i].setText(strArray[i]);
			}else{
				textViews[i].setVisibility(View.GONE);
			}
			scrollLinLay.addView(textViews[i]);
		}

		PopupWindow popupWindow = new PopupWindow(context);
		popupWindow.setBackgroundDrawable(drawableBackground);
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setContentView(linLay);

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				if(clickAction != null){
					clickAction.action(anchorView, DialogInterface.BUTTON_NEGATIVE, null);
				}
			}
		});

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		if(isSetLocation){
			popupWindow.showAtLocation(anchorView, gravity, x, y);
		}else{
			popupWindow.showAsDropDown(anchorView);
		}
	}

	public static void popupMessage(Context context, View anchorView, boolean isSetLocation, int gravity, int x, int y, int width, int height
			, TextViewAttribute[] textViewAttributes, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), isSetLocation, gravity, x, y, width, height
				, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, boolean isSetLocation, int gravity, int x, int y
			, TextViewAttribute[] textViewAttributes, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, isSetLocation, gravity, x, y, ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.WRAP_CONTENT, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, boolean isSetLocation, int gravity, int x, int y, TextViewAttribute[] textViewAttributes
			, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), isSetLocation, gravity, x, y
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int gravity, int x, int y, int width, int height
			, TextViewAttribute[] textViewAttributes, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, true, gravity, x, y, width, height, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int gravity, int x, int y, int width, int height, TextViewAttribute[] textViewAttributes
			, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), true, gravity, x, y, width, height
				, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int gravity, int x, int y, TextViewAttribute[] textViewAttributes
			, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, true, gravity, x, y, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
				, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int gravity, int x, int y, TextViewAttribute[] textViewAttributes, String[] strArray
			, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), true, gravity, x, y
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int width, int height, TextViewAttribute[] textViewAttributes
			, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, width, height, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int width, int height, TextViewAttribute[] textViewAttributes, String[] strArray
			, final ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0, width, height
				, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, TextViewAttribute[] textViewAttributes, String[] strArray
			, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.WRAP_CONTENT, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, TextViewAttribute[] textViewAttributes, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, final View anchorView, Drawable drawableBackground, boolean isSetLocation, int gravity, int x, int y
			, int width, int height, String[] strArray, final ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		TextViewAttribute[] textViewAttributes = new TextViewAttribute[3];
		textViewAttributes[0] = new TextViewAttribute();
		textViewAttributes[0].setGravity(Gravity.CENTER_VERTICAL);
		textViewAttributes[0].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
		textViewAttributes[0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textViewAttributes[0].setEllipsize(TruncateAt.END);
		textViewAttributes[0].setMaxLines(2);
		textViewAttributes[1] = new TextViewAttribute();
		textViewAttributes[1].setGravity(textViewAttributes[0].getGravity());
		textViewAttributes[1].setTextSize(Utils.getTextSize(Utils.SIZE_TITLE, isBigScreen));
		textViewAttributes[1].setTypeface(textViewAttributes[0].getTypeface());
		textViewAttributes[1].setEllipsize(textViewAttributes[0].getEllipsize());
		textViewAttributes[1].setMaxLines(textViewAttributes[0].getMaxLines());
		textViewAttributes[2] = new TextViewAttribute();
		textViewAttributes[2].setTextSize(Utils.getTextSize(Utils.SIZE_TEXT, isBigScreen));
		popupMessage(context, anchorView, drawableBackground, isSetLocation, gravity, x, y, width, height, textViewAttributes, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int gravity, int x, int y, int width, int height
			, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, true, gravity, x, y, width, height, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int gravity, int x, int y, int width, int height, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), true, gravity, x, y, width, height
				, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int gravity, int x, int y, String[] strArray
			, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, true, gravity, x, y, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
				, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int gravity, int x, int y, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, gravity, x, y
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, int width, int height, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, width, height, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, int width, int height, String[] strArray, final ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0, width, height
				, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, Drawable drawableBackground, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.WRAP_CONTENT, strArray, clickAction);
	}

	public static void popupMessage(Context context, View anchorView, String[] strArray, ClickAction clickAction){
		popupMessage(context, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, strArray, clickAction);
	}

	public static void alertBuilderConfirm(Context context, String title, String message, String textPositive, String textNegative, final ClickAction clickAction){
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
		alertDialogBuilder.setPositiveButton(textPositive, click);
		alertDialogBuilder.setNegativeButton(textNegative, click);
		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				click.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}
		});
		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		alertDialogBuilder.show();
	}

	public static void alertBuilderConfirm(Context context, String title, String message, final ClickAction clickAction){
		alertBuilderConfirm(context, title, message, context.getString(R.string.ok), context.getString(R.string.cancel), clickAction);
	}

	public static void alertBuilderConfirm(Context context, String message, final ClickAction clickAction){
		alertBuilderConfirm(context, null, message, context.getString(R.string.ok), context.getString(R.string.cancel), clickAction);
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
		alertDialogBuilder.setMessage(activity.getString(R.string.confirm_ask));
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

	public static void popupWindow(Context context, View contentView, final View anchorView, Drawable drawableBackground, boolean isSetLocation, int gravity
			, int x, int y, int width, int height, final ClickAction clickAction){
		PopupWindow popupWindow = new PopupWindow(context);
		popupWindow.setBackgroundDrawable(drawableBackground);
		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setContentView(contentView);

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				if(clickAction != null){
					clickAction.action(anchorView, DialogInterface.BUTTON_NEGATIVE, null);
				}
			}
		});

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		if(isSetLocation){
			popupWindow.showAtLocation(anchorView, gravity, x, y);
		}else{
			popupWindow.showAsDropDown(anchorView);
		}
	}

	public static void popupWindow(Context context, View contentView, View anchorView, Drawable drawableBackground, int gravity, int x, int y, int width, int height
			, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, drawableBackground, true, gravity, x, y, width, height, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, int gravity, int x, int y, int width, int height, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), true, gravity, x, y, width, height
				, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, Drawable drawableBackground, int gravity, int x, int y, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, drawableBackground, true, gravity, x, y, ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.WRAP_CONTENT, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, int gravity, int x, int y, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), true, gravity, x, y
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, Drawable drawableBackground, int width, int height, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, width, height, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, int width, int height, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0
				, width, height, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, Drawable drawableBackground, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, drawableBackground, false, Gravity.NO_GRAVITY, 0, 0, ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.WRAP_CONTENT, clickAction);
	}

	public static void popupWindow(Context context, View contentView, View anchorView, ClickAction clickAction){
		popupWindow(context, contentView, anchorView, ContextCompat.getDrawable(context, R.drawable.popup_background_mtrl_mult), false, Gravity.NO_GRAVITY, 0, 0
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, clickAction);
	}

	public static void alertBuilderInput(final Context context, String title, String message, View contentView, final EditText editText, final int minLength
			, final int maxLength, final int imeOptions, final String inputContentKey, final String[] excludeArray, final String[] excludeHintArray
			, boolean isOutsideCancel, final ClickAction clickAction){
		final Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isFillScreenDip480 = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH_480);

		LinearLayout linearLayout;

		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

		if(contentView == null){
			contentView = editText;
		}

		linearLayout.addView(contentView);

		final DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.softInputSwitch(context, editText, false);
				if(which != DialogInterface.BUTTON_POSITIVE){
					dialog.dismiss();
					clickAction.action(null, which, null);
					return;
				}
				String inputName = editText.getText().toString();
				if(inputName.trim().length() < minLength || (inputName.length() > maxLength && maxLength > 0)){
					Utils.setToast(context, res.getString(R.string.char_length_hint, "" + minLength, "" + maxLength));
					return;
				}
				Bundle bundle = new Bundle();
				if(excludeArray == null || excludeArray.length == 0){
					dialog.dismiss();
					bundle.putString(inputContentKey, inputName);
					clickAction.action(null, which, bundle);
					return;
				}
				for(int i=0; i<excludeArray.length; i++){
					if(inputName.equals(excludeArray[i])){
						if(excludeHintArray != null){
							if(i < excludeHintArray.length && !TextUtils.isEmpty(excludeHintArray[i])){
								Utils.setToast(context, excludeHintArray[i]);
							}else if(excludeHintArray.length == 1 && !TextUtils.isEmpty(excludeHintArray[0])){
								Utils.setToast(context, excludeHintArray[0]);
							}
						}
						break;
					}
					if(i == excludeArray.length - 1){
						dialog.dismiss();
						bundle.putString(inputContentKey, inputName);
						clickAction.action(null, which, bundle);
					}
				}
			}
		};
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setView(linearLayout);
		alertDialogBuilder.setPositiveButton(res.getString(R.string.submit), null); // disable click button auto dismiss
		alertDialogBuilder.setNegativeButton(res.getString(R.string.cancel), null);
		final AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCanceledOnTouchOutside(isOutsideCancel);
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utils.softInputSwitch(context, editText, true);
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onClick.onClick(alertDialog, AlertDialog.BUTTON_POSITIVE);
					}
				});
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onClick.onClick(alertDialog, AlertDialog.BUTTON_NEGATIVE);
					}
				});
			}
		});

		Window window = alertDialog.getWindow();
		if(window != null){
			WindowManager.LayoutParams windowManagerLayoutParams = window.getAttributes();
			windowManagerLayoutParams.x = 0;
			windowManagerLayoutParams.y = 0;
			windowManagerLayoutParams.width = contentView.getLayoutParams().width;
			window.setAttributes(windowManagerLayoutParams);
		}

		alertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, DialogInterface.BUTTON_NEGATIVE, null);
				}
			}
		});

		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == imeOptions && clickAction != null) {
					onClick.onClick(alertDialog, DialogInterface.BUTTON_POSITIVE);
				}
				return false;
			}
		});

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		alertDialog.show();
	}

	public static void alertBuilderInput(Context context, String title, String message, int inputType, int maxLines, int minLength, int maxLength, int imeOptions
			, String editDefault, String editHint, String inputContentKey, String[] excludeArray, String[] excludeHintArray, boolean isOutsideCancel
			, ClickAction clickAction){
		Resources res = context.getResources();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		boolean isFillScreenDip480 = DisplayUtils.isFillScreen(dm, DisplayUtils.LIMIT_DIP_WIDTH_480);

		int itemWi, itemHe;
		LayoutParams linearLayoutParams;

		itemWi = res.getDimensionPixelSize(R.dimen.dip280);
		itemHe = res.getDimensionPixelSize(R.dimen.toolbarHeight);
		linearLayoutParams = new LayoutParams(itemWi, itemHe);
		EditText editText = new EditText(context);
		editText.setLayoutParams(linearLayoutParams);
		editText.setGravity(Gravity.CENTER_VERTICAL);
		editText.setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isFillScreenDip480));
		editText.setInputType(inputType);
		if(maxLines == 1){
			editText.setSingleLine(true);
		}else if(maxLines > 0){
			editText.setMaxLines(maxLines);
		}
		if(maxLength > 0){
			editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
		}
		editText.setImeOptions(imeOptions);

		editText.setText(editDefault);
		editText.setSelection(editText.getText().length());

		editText.setHint(editHint);

		alertBuilderInput(context, title, message, editText, editText, minLength, maxLength, imeOptions, inputContentKey, excludeArray, excludeHintArray
				, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, int maxLines, int minLength, int maxLength, int imeOptions
			, String editDefault, String editHint, String inputContentKey, String[] excludeArray, String[] excludeHintArray, boolean isOutsideCancel
			, ClickAction clickAction){
		alertBuilderInput(context, title, message, new EditText(context).getInputType(), maxLines, minLength, maxLength, imeOptions, editDefault, editHint
				, inputContentKey, excludeArray, excludeHintArray, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, int maxLines, int minLength, int maxLength, String editDefault
			, String editHint, String inputContentKey, String[] excludeArray, String[] excludeHintArray, boolean isOutsideCancel, ClickAction clickAction){
		EditText editText = new EditText(context);
		alertBuilderInput(context, title, message, editText.getInputType(), maxLines, minLength, maxLength, editText.getImeOptions(), editDefault, editHint
				, inputContentKey, excludeArray, excludeHintArray, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(final Context context, String title, String message, int inputType, int maxLines, int minLength, int maxLength
			, int imeOptions, String editDefault, String editHint, String inputContentKey, boolean isOutsideCancel, final ClickAction clickAction){
		alertBuilderInput(context, title, message, inputType, maxLines, minLength, maxLength, imeOptions, editDefault, editHint, inputContentKey, null, null
				, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, int maxLines, int minLength, int maxLength, int imeOptions
			, String editDefault, String editHint, String inputContentKey, boolean isOutsideCancel, ClickAction clickAction){
		alertBuilderInput(context, title, message, new EditText(context).getInputType(), maxLines, minLength, maxLength, imeOptions, editDefault, editHint
				, inputContentKey, null, null, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, int maxLines, int minLength, int maxLength, String editDefault
			, String editHint, String inputContentKey, boolean isOutsideCancel, ClickAction clickAction){
		EditText editText = new EditText(context);
		alertBuilderInput(context, title, message, editText.getInputType(), maxLines, minLength, maxLength, editText.getImeOptions(), editDefault, editHint
				, inputContentKey, null, null, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, int imeOptions, String editDefault, String editHint, String inputContentKey
			, boolean isOutsideCancel, ClickAction clickAction){
		alertBuilderInput(context, title, message, new EditText(context).getInputType(), -1, -1, -1, imeOptions, editDefault, editHint, inputContentKey, null, null
				, isOutsideCancel, clickAction);
	}

	public static void alertBuilderInput(Context context, String title, String message, String editDefault, String editHint, String inputContentKey
			, boolean isOutsideCancel, ClickAction clickAction){
		EditText editText = new EditText(context);
		alertBuilderInput(context, title, message, editText.getInputType(), -1, -1, -1, editText.getImeOptions(), editDefault, editHint, inputContentKey, null, null
				, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(final Context context, int width, int height, String title, TextViewAttribute[] textViewAttributes
			, final String[][] strArray, boolean isOutsideCancel, final ClickAction clickAction){
		Resources res = context.getResources();

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

		space = res.getDimensionPixelSize(R.dimen.dip8);
		itemWi = width - space * 2;
		itemHe = LayoutParams.WRAP_CONTENT;
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		TextViewAttribute textViewAttribute = new TextViewAttribute();
		for(int i=0; i<buttons.length; i++){
			if(i < textViewAttributes.length -1){
				textViewAttribute = textViewAttributes[i];
			}
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(textViewAttribute.getGravity());
			if(textViewAttribute.getTextColor() != null){
				buttons[i].setTextColor(textViewAttribute.getTextColor());
			}
			buttons[i].setTextSize(textViewAttribute.getTextSize());
			buttons[i].setTypeface(textViewAttribute.getTypeface());
			buttons[i].setEllipsize(textViewAttribute.getEllipsize());
			buttons[i].setMaxLines(textViewAttribute.getMaxLines());
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

		Window window = alertDialog.getWindow();
		if(window != null){
			WindowManager.LayoutParams windowLayPar = window.getAttributes();
			windowLayPar.x = 0;
			windowLayPar.y = 0;
			windowLayPar.width = width;
			windowLayPar.height = height;
			window.setBackgroundDrawableResource(android.R.color.transparent);
			window.setAttributes(windowLayPar);
		}

		alertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, DialogInterface.BUTTON_NEGATIVE, null);
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

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		alertDialog.show();
	}

	public static void alertMenuUseButton(Context context, DisplayMetrics displayMetrics, String title, TextViewAttribute[] textViewAttributes
			, String[][] strArray, boolean isOutsideCancel, ClickAction clickAction){
		int width = (int)(displayMetrics.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		alertMenuUseButton(context, width, height, title, textViewAttributes, strArray, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(Context context, String title, TextViewAttribute[] textViewAttributes, String[][] strArray
			, boolean isOutsideCancel, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		alertMenuUseButton(context, displayMetrics, title, textViewAttributes, strArray, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(Context context, DisplayMetrics displayMetrics, int width, int height, String title, String[][] strArray
			, boolean isOutsideCancel, ClickAction clickAction){
		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		TextViewAttribute[] textViewAttributes = new TextViewAttribute[1];
		textViewAttributes[0] = new TextViewAttribute();
		textViewAttributes[0].setGravity(Gravity.CENTER);
		textViewAttributes[0].setTextColor(ContextCompat.getColorStateList(context, R.color.selector_textcolor_item));
		textViewAttributes[0].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
		textViewAttributes[0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textViewAttributes[0].setEllipsize(TruncateAt.END);
		textViewAttributes[0].setMaxLines(2);
		alertMenuUseButton(context, width, height, title, textViewAttributes, strArray, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(Context context, int width, int height, String title, String[][] strArray, boolean isOutsideCancel
			, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		alertMenuUseButton(context, displayMetrics, width, height, title, strArray, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(Context context, DisplayMetrics displayMetrics, String title, String[][] strArray, boolean isOutsideCancel
			, ClickAction clickAction){
		int width = (int)(displayMetrics.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		alertMenuUseButton(context, width, height, title, strArray, isOutsideCancel, clickAction);
	}

	public static void alertMenuUseButton(Context context, String title, String[][] strArray, boolean isOutsideCancel, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		alertMenuUseButton(context, displayMetrics, title, strArray, isOutsideCancel, clickAction);
	}

	public static RelativeLayout getTopBarWithTextView(Context context, DisplayMetrics displayMetrics, int width, int height
			, int btnWidth, int space){
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
				textViews[i].setId(R.id.topLayoutLeftView);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				relLayPar.setMargins(0, space, space, space);
			}else if(i == 1){
				textViews[i].setId(R.id.topLayoutCenterView);
				relLayPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
				relLayPar.addRule(RelativeLayout.RIGHT_OF, R.id.topLayoutLeftView);
			}else{
				textViews[i].setId(R.id.topLayoutRightView);
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
		relLayPar.addRule(RelativeLayout.LEFT_OF, R.id.topLayoutRightView);

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
				imageViews[i].setId(R.id.topLayoutLeftView);
				relLayPar = new RelativeLayout.LayoutParams(btnWidth, height - space * 2);
				relLayPar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				relLayPar.setMargins(0, space, space, space);
			}else{
				imageViews[i].setId(R.id.topLayoutRightView);
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
			relLayPar.addRule(RelativeLayout.RIGHT_OF, R.id.topLayoutLeftView);
			textViews[i] = new TextView(context);
			textViews[i].setId(R.id.topLayoutCenterView);
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
		relLayPar.addRule(RelativeLayout.LEFT_OF, R.id.topLayoutRightView);

		return relLay;
	}

	public static RelativeLayout getTopBarWithTextView(Context context, int width, int height, int btnWidth, int space){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		return getTopBarWithTextView(context, displayMetrics, width, height, btnWidth, space);
	}

	public static RelativeLayout getTopBarWithSideImageView(Context context, int width, int height, int btnWidth, int space){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		return getTopBarWithSideImageView(context, displayMetrics, width, height, btnWidth, space);
	}

	public static void dialogMenuUseButton(final Context context, DisplayMetrics displayMetrics, int width, int height, TextViewAttribute[] textViewAttributes
			, String title, final String[][] strArray, boolean isOutsideCancel, final ClickAction clickAction){
		Resources res = context.getResources();

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

		TextViewAttribute textViewAttribute = textViewAttributes.length > 0 ? textViewAttributes[0] : new TextViewAttribute();
		space = res.getDimensionPixelSize(R.dimen.dip8);
		itemWi = width - space * 2;
		itemHe = (int)(61.5f * 0.75f * displayMetrics.density);
		linLayPar = new LayoutParams(itemWi, itemHe);
		linLayPar.setMargins(space, 0, space, 0);
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(context);
			textView.setLayoutParams(linLayPar);
			textView.setPadding(space, 0, 0, 0);
			textView.setGravity(textViewAttribute.getGravity());
			if(textViewAttribute.getTextColor() != null){
				textView.setTextColor(textViewAttribute.getTextColor());
			}
			textView.setTextSize(textViewAttribute.getTextSize());
			textView.setTypeface(textViewAttribute.getTypeface());
			scrollLinLay.addView(textView);

			textView.setText(title);
		}

		linLayPar = new LayoutParams(itemWi, LayoutParams.WRAP_CONTENT);
		linLayPar.setMargins(space, 0, space, 0);
		for(int i=0; i<buttons.length; i++){
			if(i + 1 < textViewAttributes.length -1){
				textViewAttribute = textViewAttributes[i + 1];
			}
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(textViewAttribute.getGravity());
			if(textViewAttribute.getTextColor() != null){
				buttons[i].setTextColor(textViewAttribute.getTextColor());
			}
			buttons[i].setTextSize(textViewAttribute.getTextSize());
			buttons[i].setTypeface(textViewAttribute.getTypeface());
			buttons[i].setEllipsize(textViewAttribute.getEllipsize());
			buttons[i].setMaxLines(textViewAttribute.getMaxLines());
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

		Window window = dialog.getWindow();
		if(window != null){
			WindowManager.LayoutParams windowLayPar = window.getAttributes();
			windowLayPar.x = 0;
			windowLayPar.y = 0;
			windowLayPar.width = width;
			windowLayPar.height = height;
			window.setBackgroundDrawableResource(android.R.color.transparent);
			window.setAttributes(windowLayPar);
		}

		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, DialogInterface.BUTTON_NEGATIVE, null);
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

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		dialog.show();
	}

	public static void dialogMenuUseButton(Context context, DisplayMetrics displayMetrics, TextViewAttribute[] textViewAttributes, String title
			, String[][] strArray, boolean isOutsideCancel, ClickAction clickAction){
		int width = (int)(displayMetrics.widthPixels * 0.89f);
		int height = LayoutParams.WRAP_CONTENT;
		dialogMenuUseButton(context, displayMetrics, width, height, textViewAttributes, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseButton(Context context, TextViewAttribute[] textViewAttributes, String title, String[][] strArray
			, boolean isOutsideCancel, final ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseButton(context, displayMetrics, textViewAttributes, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseButton(Context context, DisplayMetrics displayMetrics, int width, int height, String title, String[][] strArray
			, boolean isOutsideCancel, ClickAction clickAction){
		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		TextViewAttribute[] textViewAttributes = new TextViewAttribute[2];
		textViewAttributes[0] = new TextViewAttribute();
		textViewAttributes[0].setGravity(Gravity.CENTER_VERTICAL);
		textViewAttributes[0].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
		textViewAttributes[0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textViewAttributes[1] = new TextViewAttribute();
		textViewAttributes[1].setGravity(Gravity.CENTER);
		textViewAttributes[1].setTextColor(ContextCompat.getColorStateList(context, R.color.selector_textcolor_item));
		textViewAttributes[1].setTextSize(textViewAttributes[0].getTextSize());
		textViewAttributes[1].setTypeface(textViewAttributes[0].getTypeface());
		textViewAttributes[1].setEllipsize(TruncateAt.END);
		textViewAttributes[1].setMaxLines(2);
		dialogMenuUseButton(context, displayMetrics, width, height, textViewAttributes, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseButton(Context context, int width, int height, String title, String[][] strArray, boolean isOutsideCancel
			, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseButton(context, displayMetrics, width, height, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseButton(Context context, DisplayMetrics displayMetrics, String title, String[][] strArray, boolean isOutsideCancel
			, ClickAction clickAction){
		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		TextViewAttribute[] textViewAttributes = new TextViewAttribute[2];
		textViewAttributes[0] = new TextViewAttribute();
		textViewAttributes[0].setGravity(Gravity.CENTER_VERTICAL);
		textViewAttributes[0].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
		textViewAttributes[0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textViewAttributes[1] = new TextViewAttribute();
		textViewAttributes[1].setGravity(Gravity.CENTER);
		textViewAttributes[1].setTextColor(ContextCompat.getColorStateList(context, R.color.selector_textcolor_item));
		textViewAttributes[1].setTextSize(textViewAttributes[0].getTextSize());
		textViewAttributes[1].setTypeface(textViewAttributes[0].getTypeface());
		textViewAttributes[1].setEllipsize(TruncateAt.END);
		textViewAttributes[1].setMaxLines(2);
		dialogMenuUseButton(context, displayMetrics, textViewAttributes, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseButton(Context context, String title, String[][] strArray, boolean isOutsideCancel, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseButton(context, displayMetrics, title, strArray, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, DisplayMetrics displayMetrics, View topBar, int width, int height, String title, int[] selectedArray
			, ListAdapter adp, final boolean isMulti, boolean isOutsideCancel, final ClickAction clickAction) {
		Resources res = context.getResources();

		int itemWi, itemHe, space;
		LayoutParams linLayPar;

		LinearLayout linLay;
		final ListView listView;

		linLay = new LinearLayout(context);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setBackgroundResource(android.R.color.white);
		linLay.setGravity(Gravity.CENTER_HORIZONTAL);

		space = res.getDimensionPixelSize(R.dimen.dip8);
		if(topBar == null){
			itemWi = res.getDimensionPixelSize(R.dimen.dip72);
			itemHe = (int)(61.5f * 1.0f * displayMetrics.density);
			topBar = getTopBarWithTextView(context, displayMetrics, width, itemHe, itemWi, space);

			topBar.setBackgroundColor(0xFFC0C0C0);
			topBar.findViewById(R.id.topLayoutLeftView).setBackgroundResource(android.R.color.white);

			if(topBar.findViewById(R.id.topLayoutLeftView) instanceof TextView){
				((TextView)topBar.findViewById(R.id.topLayoutLeftView)).setText(res.getString(R.string.cancel));
			}
		}

		linLay.addView(topBar);

		if(topBar.findViewById(R.id.topLayoutCenterView) instanceof TextView){
			((TextView)topBar.findViewById(R.id.topLayoutCenterView)).setText(title);
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
		listView.setDividerHeight((int)(1 * displayMetrics.density));

		linLay.addView(listView);

		if(isMulti){
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}else{
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		listView.setAdapter(adp);
		for(int i=0; i<selectedArray.length; i++){
			listView.setItemChecked(selectedArray[i], true);
			if(!isMulti){
				listView.setSelection(selectedArray[i]);
			}
		}

		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linLay);
		dialog.setCanceledOnTouchOutside(isOutsideCancel);

		Window window = dialog.getWindow();
		if(window != null){
			WindowManager.LayoutParams windowLayPar = window.getAttributes();
			windowLayPar.x = 0;
			windowLayPar.y = 0;
			windowLayPar.width = width;
			windowLayPar.height = height;
			window.setBackgroundDrawableResource(android.R.color.transparent);
			window.setAttributes(windowLayPar);
		}

		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(clickAction != null){
					clickAction.action(null, DialogInterface.BUTTON_NEGATIVE, null);
				}
			}
		});

		if(topBar.findViewById(R.id.topLayoutLeftView) != null){
			topBar.findViewById(R.id.topLayoutLeftView).setOnClickListener(new OnClickListener() {

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
				if(!isMulti){
					dialog.dismiss();
				}
			}
		});

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		dialog.show();
	}

	public static void dialogMenuUseListView(Context context, DisplayMetrics displayMetrics, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, int resourceId, int[] viewIdArray, boolean isMulti, boolean isOutsideCancel, ClickAction clickAction) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> hashMap;
		for(int i=0; i<strArray.length; i++){
			hashMap = new HashMap<String, String>(2);
			hashMap.put("strArray", strArray[i]);
			list.add(hashMap);
		}
		SimpleAdapter adp = new SimpleAdapter(context, list, resourceId, new String[]{"strArray"}, viewIdArray);
		dialogMenuUseListView(context, displayMetrics, topBar, width, height, title, selectedArray, adp, isMulti, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray, int[] selectedArray
			, int resourceId, int[] viewIdArray, boolean isMulti, boolean isOutsideCancel, ClickAction clickAction) {
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseListView(context, displayMetrics, topBar, width, height, title, strArray, selectedArray, resourceId, viewIdArray, isMulti, isOutsideCancel
				, clickAction);
	}

	public static void dialogMenuUseListView(Context context, DisplayMetrics displayMetrics, View topBar, int width, int height, String title, String[] strArray
			, int[] selectedArray, boolean isMulti, boolean isOutsideCancel, ClickAction clickAction) {
		int resourceId;
		int[] viewIdArray;
		if(isMulti){
			resourceId = android.R.layout.simple_list_item_multiple_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}else{
			resourceId = android.R.layout.simple_list_item_single_choice;
			viewIdArray = new int[]{android.R.id.text1};
		}
		dialogMenuUseListView(context, displayMetrics, topBar, width, height, title, strArray, selectedArray, resourceId, viewIdArray, isMulti, isOutsideCancel
				, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, int width, int height, String title, String[] strArray, int[] selectedArray
			, boolean isMulti, boolean isOutsideCancel, ClickAction clickAction) {
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseListView(context, displayMetrics, topBar, width, height, title, strArray, selectedArray, isMulti, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, DisplayMetrics displayMetrics, View topBar, String title, String[] strArray, int[] selectedArray
			, boolean isMulti, boolean isOutsideCancel, ClickAction clickAction) {
		int width, height;
		if(displayMetrics.widthPixels < displayMetrics.heightPixels){
			width = (int)(displayMetrics.widthPixels * 0.79f);
			height = (int)(displayMetrics.widthPixels * 0.95f);
		}else{
			width = (int)(displayMetrics.heightPixels * 0.79f);
			height = (int)(displayMetrics.heightPixels * 0.95f);
		}
		dialogMenuUseListView(context, displayMetrics, topBar, width, height, title, strArray, selectedArray, isMulti, isOutsideCancel, clickAction);
	}

	public static void dialogMenuUseListView(Context context, View topBar, String title, String[] strArray, int[] selectedArray, boolean isMulti
			, boolean isOutsideCancel, ClickAction clickAction) {
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		dialogMenuUseListView(context, displayMetrics, topBar, title, strArray, selectedArray, isMulti, isOutsideCancel, clickAction);
	}

	public static void popupMenuUseButton(Context context, final View view, int width, int height, TextViewAttribute[] textViewAttributes, String title
			, final String[][] strArray, final ClickAction clickAction){
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

		TextViewAttribute textViewAttribute = textViewAttributes.length > 0 ? textViewAttributes[0] : new TextViewAttribute();
		if(!TextUtils.isEmpty(title)){
			textView = new TextView(context);
			textView.setLayoutParams(linLayPar);
			textView.setGravity(textViewAttribute.getGravity());
			textView.setTextSize(textViewAttribute.getTextSize());
			textView.setTypeface(textViewAttribute.getTypeface());
			textView.setText(title);
			scrollLinLay.addView(textView);
		}

		for(int i=0; i<buttons.length; i++){
			if(i + 1 < textViewAttributes.length -1){
				textViewAttribute = textViewAttributes[i + 1];
			}
			buttons[i] = new Button(context);
			buttons[i].setLayoutParams(linLayPar);
			buttons[i].setPadding(0, 0, 0, 0);
			buttons[i].setGravity(textViewAttribute.getGravity());
			if(textViewAttribute.getTextColor() != null){
				buttons[i].setTextColor(textViewAttribute.getTextColor());
			}
			buttons[i].setTextSize(textViewAttribute.getTextSize());
			buttons[i].setTypeface(textViewAttribute.getTypeface());
			buttons[i].setEllipsize(textViewAttribute.getEllipsize());
			buttons[i].setMaxLines(textViewAttribute.getMaxLines());
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

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				if(clickAction != null){
					clickAction.action(view, DialogInterface.BUTTON_NEGATIVE, null);
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

		if(context instanceof Activity && ((Activity)context).isFinishing()){
			return;
		}
		popupWindow.showAsDropDown(view);
	}

	public static void popupMenuUseButton(Context context, DisplayMetrics displayMetrics, View view, TextViewAttribute[] textViewAttributes, String title
			, String[][] strArray, ClickAction clickAction){
		int width = displayMetrics.widthPixels / 2;
		int height = LayoutParams.WRAP_CONTENT;
		popupMenuUseButton(context, view, width, height, textViewAttributes, title, strArray, clickAction);
	}

	public static void popupMenuUseButton(Context context, View view, TextViewAttribute[] textViewAttributes, String title, String[][] strArray
			, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		popupMenuUseButton(context, displayMetrics, view, textViewAttributes, title, strArray, clickAction);
	}

	public static void popupMenuUseButton(Context context, DisplayMetrics displayMetrics, View view, int width, int height, String title, String[][] strArray
			, ClickAction clickAction){
		boolean isBigScreen = DisplayUtils.isFillScreen(displayMetrics, DisplayUtils.LIMIT_DIP_WIDTH);

		TextViewAttribute[] textViewAttributes = new TextViewAttribute[2];
		textViewAttributes[0] = new TextViewAttribute();
		textViewAttributes[0].setGravity(Gravity.CENTER);
		textViewAttributes[0].setTextSize(Utils.getTextSize(Utils.SIZE_SUBJECT, isBigScreen));
		textViewAttributes[0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textViewAttributes[1] = new TextViewAttribute();
		textViewAttributes[1].setGravity(textViewAttributes[0].getGravity());
		textViewAttributes[1].setTextColor(ContextCompat.getColorStateList(context, R.color.selector_textcolor_item));
		textViewAttributes[1].setTextSize(textViewAttributes[0].getTextSize());
		textViewAttributes[1].setTypeface(textViewAttributes[0].getTypeface());
		textViewAttributes[1].setEllipsize(TruncateAt.END);
		textViewAttributes[1].setMaxLines(2);
		popupMenuUseButton(context, view, width, height, textViewAttributes, title, strArray, clickAction);
	}

	public static void popupMenuUseButton(Context context, View view, int width, int height, String title, String[][] strArray, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		popupMenuUseButton(context, displayMetrics, view, width, height, title, strArray, clickAction);
	}

	public static void popupMenuUseButton(Context context, DisplayMetrics displayMetrics, View view, String title, String[][] strArray
			, ClickAction clickAction){
		int width = displayMetrics.widthPixels / 2;
		int height = LayoutParams.WRAP_CONTENT;
		popupMenuUseButton(context, displayMetrics, view, width, height, title, strArray, clickAction);
	}

	public static void popupMenuUseButton(Context context, View view, String title, String[][] strArray, ClickAction clickAction){
		DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetricsFromWindowManager(context);
		popupMenuUseButton(context, displayMetrics, view, title, strArray, clickAction);
	}
}