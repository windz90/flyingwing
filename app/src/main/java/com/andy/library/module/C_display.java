/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.2.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class C_display {
	
	public static final int DISPLAY_METRICS_FROM_WINDOW_MANAGER = 0;
	public static final int DISPLAY_METRICS_FROM_RESOURCES = 1;
	public static final int DISPLAY = 2;
	
	public interface EventCallBack{
		public void completed(int visibleHe);
	}
	
	public static Display getDisplayFromActivity(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		return display;
	}
	
	public static Display getDisplayFromContext(Context context){
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		Display display = windowManager.getDefaultDisplay();
		return display;
	}
	
	@SuppressLint("NewApi")
	public static Point getDisplaySize(Display display){
		// API 13
		Point point = new Point();
		if(Build.VERSION.SDK_INT >= 13){
			display.getSize(point);
		}
		return point;
	}
	
	public static DisplayMetrics getDisplayMetricsFromWindowManager(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}
	
	public static DisplayMetrics getDisplayMetricsFromWindowManager(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}
	
	public static DisplayMetrics getDisplayMetricsFromResources(Context context){
		return context.getResources().getDisplayMetrics();
	}
	
	public static DisplayMetrics getDisplayMetrics(Context context, int flag){
		if(flag == DISPLAY_METRICS_FROM_RESOURCES){
			return getDisplayMetricsFromResources(context);
		}
		return getDisplayMetricsFromWindowManager(context);
	}
	
	public static boolean equalsDisplayMetrics(DisplayMetrics dm1, DisplayMetrics dm2){
		return dm1 != null 
				&& dm2 != null 
				&& dm1.widthPixels == dm2.widthPixels 
				&& dm1.heightPixels == dm2.heightPixels 
				&& dm1.density == dm2.density 
				&& dm1.scaledDensity == dm2.scaledDensity 
				&& dm1.densityDpi == dm2.densityDpi 
				&& dm1.xdpi == dm2.xdpi 
				&& dm1.ydpi == dm2.ydpi;
	}
	
	public static boolean equalsDisplayMetrics(Context context){
		DisplayMetrics dm1 = getDisplayMetricsFromWindowManager(context);
		DisplayMetrics dm2 = getDisplayMetricsFromResources(context);
		return equalsDisplayMetrics(dm1, dm2);
	}
	
	public static void printDisplayMetrics(DisplayMetrics displayMetrics){
		System.out.println("widthPixels " + displayMetrics.widthPixels + "\n" + 
				"heightPixels " + displayMetrics.heightPixels + "\n" + 
				"density " + displayMetrics.density + "\n" + 
				"scaledDensity " + displayMetrics.scaledDensity + "\n" + 
				"densityDpi " + displayMetrics.densityDpi + "\n" + 
				"xdpi " + displayMetrics.xdpi + "\n" + 
				"ydpi " + displayMetrics.ydpi);
	}
	
	public static void printDisplayMetrics(Context context, int flag){
		printDisplayMetrics(getDisplayMetrics(context, flag));
	}
	
	public static int getAbsWidth(int width, int height){
		return width < height ? width : height;
	}
	
	public static int getAbsHeight(int width, int height){
		return width < height ? height : width;
	}
	
	public static void setVisibleHeightWaitOnDraw(final Activity activity, final EventCallBack callBack){
        activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).post(new Runnable() {
			
			@Override
			public void run() {
				callBack.completed(measureVisibleHeightForOnDraw(activity));
			}
		});
	}
	
	public static int measureVisibleWidthForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
//		rect.left = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getLeft();
//		rect.right = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getRight();
		return Math.abs(rect.left - rect.right);
	}
	
	public static int measureVisibleWidthForOnDraw(Activity activity){
		return measureVisibleWidthForOnDraw(activity.getWindow().getDecorView());
	}
	
	public static int measureVisibleHeightForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
//		rect.top = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//		rect.bottom = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getBottom();
		return Math.abs(rect.top - rect.bottom);
	}
	
	public static int measureVisibleHeightForOnDraw(Activity activity){
		return measureVisibleHeightForOnDraw(activity.getWindow().getDecorView());
	}
	
	public static int measureStatusBarHeightForOnDraw(Activity activity){
		return getHeightPixels(activity, false) - measureVisibleHeightForOnDraw(activity);
	}
	
	public static int getVisibleHeight(Context context){
		return getHeightPixels(context, false) - getStatusBarHeight(0);
	}
	
	public static int getVisibleHeight(DisplayMetrics displayMetrics){
		return getHeightPixels(displayMetrics, false) - getStatusBarHeight(0);
	}
	
	public static int getStatusBarHeight(int defValue){
		Resources res = Resources.getSystem();
		int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
		if(resourceId > 0){
			// displayMetricsFromResources
			return res.getDimensionPixelSize(resourceId);
		}
		return defValue;
	}
	
	// 比較動態測量與查詢ResourceID取得的高度資料，返回大於零且較小的值
	public static int compareVisibleHeight(int visibleDisplayFrameHe, int displayHe, int statusBarHe){
		if(statusBarHe == 0){
			if(visibleDisplayFrameHe == 0){
				return displayHe;
			}
			return visibleDisplayFrameHe < displayHe ? visibleDisplayFrameHe : displayHe;
		}
		if(visibleDisplayFrameHe == 0){
			return displayHe - statusBarHe;
		}
		return visibleDisplayFrameHe < displayHe - statusBarHe ? visibleDisplayFrameHe : displayHe - statusBarHe;
	}
	
	public static int compareVisibleHeight(DisplayMetrics displayMetrics, View view){
		int visibleDisplayFrameHe = measureVisibleHeightForOnDraw(view);
		int displayHe = getHeightPixels(displayMetrics, false);
		int statusBarHe = getStatusBarHeight(0);
		return compareVisibleHeight(visibleDisplayFrameHe, displayHe, statusBarHe);
	}
	
	public static int compareVisibleHeight(Activity activity, DisplayMetrics displayMetrics){
		return compareVisibleHeight(displayMetrics, activity.getWindow().getDecorView());
	}
	
	public static int compareVisibleHeight(Activity activity){
		return compareVisibleHeight(getDisplayMetricsFromWindowManager(activity), activity.getWindow().getDecorView());
	}
	
	public static int compareVisibleHeight(Context context, View view){
		return compareVisibleHeight(getDisplayMetricsFromWindowManager(context), view);
	}
	
	public static int getActionBarHeight(Context context, int defValue){
		TypedValue typedValue = new TypedValue();
		if(context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
			// displayMetricsFromResources
			return context.getResources().getDimensionPixelSize(typedValue.resourceId);
		}
		return defValue;
	}
	
	public static boolean isOrientationPortrait(Context context){
		if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isFillScreen(DisplayMetrics displayMetrics, int limitDipWidth){
		int displayAbsWidth = getAbsWidth(displayMetrics.widthPixels, displayMetrics.heightPixels);
		if(displayAbsWidth / displayMetrics.density + 0.5f < limitDipWidth){
			return false;
		}
		return true;
	}
	
	public static boolean isFillScreen(Context context, int flag, int limitDipWidth){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return isFillScreen(displayMetrics, limitDipWidth);
	}
	
	public static boolean isFillScreen(Context context, int limitDipWidth){
		return isFillScreen(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, limitDipWidth);
	}
	
	public static int getWidthPixels(DisplayMetrics displayMetrics, boolean isAbs){
		if(isAbs){
			return getAbsWidth(displayMetrics.widthPixels, displayMetrics.heightPixels);
		}else{
			return displayMetrics.widthPixels;
		}
	}
	
	public static int getWidthPixels(Display display, boolean isAbs){
		Point point = getDisplaySize(display);
		if(isAbs){
			return getAbsWidth(point.x, point.y);
		}else{
			return point.x;
		}
	}
	
	public static int getWidthPixels(Context context, int flag, boolean isAbs){
		if(flag == DISPLAY_METRICS_FROM_RESOURCES){
			return getWidthPixels(getDisplayMetricsFromResources(context), isAbs);
		}
		if(flag == DISPLAY){
			return getWidthPixels(getDisplayFromContext(context), isAbs);
		}
		return getWidthPixels(getDisplayMetricsFromWindowManager(context), isAbs);
	}
	
	public static int getWidthPixels(Context context, boolean isAbs){
		return getWidthPixels(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, isAbs);
	}
	
	public static int getHeightPixels(DisplayMetrics displayMetrics, boolean isAbs){
		if(isAbs){
			return getAbsHeight(displayMetrics.widthPixels, displayMetrics.heightPixels);
		}else{
			return displayMetrics.heightPixels;
		}
	}
	
	public static int getHeightPixels(Display display, boolean isAbs){
		Point point = getDisplaySize(display);
		if(isAbs){
			return getAbsHeight(point.x, point.y);
		}else{
			return point.y;
		}
	}
	
	public static int getHeightPixels(Context context, int flag, boolean isAbs){
		if(flag == DISPLAY_METRICS_FROM_RESOURCES){
			return getHeightPixels(getDisplayMetricsFromResources(context), isAbs);
		}
		if(flag == DISPLAY){
			return getHeightPixels(getDisplayFromContext(context), isAbs);
		}
		return getHeightPixels(getDisplayMetricsFromWindowManager(context), isAbs);
	}
	
	public static int getHeightPixels(Context context, boolean isAbs){
		return getHeightPixels(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, isAbs);
	}
	
	public static int getWidthDip(DisplayMetrics displayMetrics, boolean isAbs){
		return (int)(getWidthPixels(displayMetrics, isAbs) / displayMetrics.density + 0.5f);
	}
	
	public static int getWidthDip(Context context, int flag, boolean isAbs){
		return getWidthDip(getDisplayMetrics(context, flag), isAbs);
	}
	
	public static int getWidthDip(Context context, boolean isAbs){
		return getWidthDip(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, isAbs);
	}
	
	public static int getHeightDip(DisplayMetrics displayMetrics, boolean isAbs){
		return (int)(getHeightPixels(displayMetrics, isAbs) / displayMetrics.density + 0.5f);
	}
	
	public static int getHeightDip(Context context, int flag, boolean isAbs){
		return getHeightDip(getDisplayMetrics(context, flag), isAbs);
	}
	
	public static int getHeightDip(Context context, boolean isAbs){
		return getHeightDip(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, isAbs);
	}
	
	public static int getDip(Context context, int flag, float pixels){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return (int)(pixels / displayMetrics.density + 0.5f);
	}
	
	public static int getDip(Context context, float pixels){
		return getDip(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, pixels);
	}
	
	public static int getPixels(Context context, int flag, float dip){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return (int)(dip * displayMetrics.density + 0.5f);
	}
	
	public static int getPixels(Context context, float dip){
		return getPixels(context, DISPLAY_METRICS_FROM_WINDOW_MANAGER, dip);
	}
	
	public static double getScreenInch(DisplayMetrics displayMetrics){
		double widthInch = displayMetrics.widthPixels / displayMetrics.xdpi;
		double heightInch = displayMetrics.heightPixels / displayMetrics.ydpi;
		return Math.sqrt(Math.pow(widthInch, 2) + Math.pow(heightInch, 2));
	}
	
	public static double getScreenInch(Context context, int flag){
		return getScreenInch(getDisplayMetrics(context, flag));
	}
	
	// dpi驗算證明
	public double getScreenDpi(DisplayMetrics displayMetrics){
		double diagonalPixels = Math.sqrt(Math.pow(displayMetrics.widthPixels, 2) + Math.pow(displayMetrics.heightPixels, 2));
		return diagonalPixels / getScreenInch(displayMetrics);
	}
	
	public double getScreenDpi(Context context, int flag){
		return getScreenDpi(getDisplayMetrics(context, flag));
	}
	
	/**
	 * 取得XY平均真實螢幕密度
	 * @param displayMetrics
	 * @return
	 */
	public static float getRealXYDensity(DisplayMetrics displayMetrics){
		return (displayMetrics.xdpi + displayMetrics.ydpi) / 2 / 160;
	}
	
	/**
	 * 取得XY平均真實螢幕密度
	 * @param context
	 * @param flag
	 * @return
	 */
	public static float getRealXYDensity(Context context, int flag){
		return getRealXYDensity(getDisplayMetrics(context, flag));
	}
	
	public static float getDipScaleX(DisplayMetrics displayMetrics){
		float realDensityX = displayMetrics.xdpi / 160;
		float realDipX = getWidthPixels(displayMetrics, true) / realDensityX;
		return getWidthDip(displayMetrics, true) / realDipX;
	}
	
	public static float getDipScaleX(Context context, int flag){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDipScaleX(displayMetrics);
	}
	
	public static float getDipScaleY(DisplayMetrics displayMetrics){
		float realDensityY = displayMetrics.ydpi / 160;
		float realDipY = getHeightPixels(displayMetrics, true) / realDensityY;
		return getHeightDip(displayMetrics, true) / realDipY;
	}
	
	public static float getDipScaleY(Context context, int flag){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDipScaleY(displayMetrics);
	}
}