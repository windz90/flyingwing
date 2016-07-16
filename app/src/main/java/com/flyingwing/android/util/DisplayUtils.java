/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.3.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.util;

import android.annotation.TargetApi;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable"})
public class DisplayUtils {

	public static final int LIMIT_DIP_WIDTH_320 = 320;
	public static final int LIMIT_DIP_WIDTH_360 = 360;
	public static final int LIMIT_DIP_WIDTH_480 = 480;
	public static final int LIMIT_DIP_WIDTH_540 = 540;
	public static final int LIMIT_DIP_WIDTH_600 = 600;
	public static final int LIMIT_DIP_WIDTH_720 = 720;
	public static final int LIMIT_DIP_WIDTH_800 = 800;
	public static final int LIMIT_DIP_WIDTH_960 = 960;
	public static final int LIMIT_DIP_WIDTH = LIMIT_DIP_WIDTH_480;

	public static final int DISPLAY_METRICS_FROM_WINDOW_MANAGER = 0;
	public static final int DISPLAY_METRICS_FROM_RESOURCES = 1;
	public static final int DISPLAY = 2;

	public interface EventCallback {
		void completed(int visibleHe);
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static Point getDisplaySize(Display display){
		Point point = new Point();
		display.getSize(point);
		return point;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Point getRealDisplaySize(Display display){
		Point point = new Point();
		display.getRealSize(point);
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static DisplayMetrics getRealDisplayMetricsFromWindowManager(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
		return displayMetrics;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static DisplayMetrics getRealDisplayMetricsFromWindowManager(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
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

	public static void measureVisibleHeightWaitOnDraw(final View view, final EventCallback eventCallback){
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					//noinspection deprecation
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				eventCallback.completed(measureVisibleHeightForOnDraw(view));
			}
		});
	}

	public static void measureVisibleHeightWaitOnDraw(Activity activity, EventCallback eventCallback){
		measureVisibleHeightWaitOnDraw(activity.getWindow().getDecorView(), eventCallback);
	}

	public static int measureVisibleWidthForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		return Math.abs(rect.left - rect.right);
	}

	public static int measureVisibleWidthForOnDraw(Activity activity){
		return measureVisibleWidthForOnDraw(activity.getWindow().getDecorView());
	}

	/**
	 * Not contain StatusBar
	 */
	public static int measureVisibleHeightForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		return Math.abs(rect.top - rect.bottom);
	}

	public static int measureVisibleHeightForOnDraw(Activity activity){
		return measureVisibleHeightForOnDraw(activity.getWindow().getDecorView());
	}

	/**
	 * Not contain StatusBar and TitleBar
	 */
	public static int measureContentHeightForOnDraw(Activity activity){
		Rect rect = new Rect();
		rect.top = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		rect.bottom = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getBottom();
		return Math.abs(rect.top - rect.bottom);
	}

	public static int measureStatusBarHeightForOnDraw(Activity activity){
		return getHeightPixels(getDisplayMetricsFromWindowManager(activity), false) - measureVisibleHeightForOnDraw(activity);
	}

	public static int getVisibleHeight(Context context){
		return getHeightPixels(getDisplayMetricsFromWindowManager(context), false) - getStatusBarHeight(0);
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

	public static int getNavigationBarHeight(int defValue){
		Resources res = Resources.getSystem();
		int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
		if(resourceId > 0){
			// displayMetricsFromResources
			return res.getDimensionPixelSize(resourceId);
		}
		return defValue;
	}

	public static int getActionBarHeight(Context context, int defValue){
		TypedValue typedValue = new TypedValue();
		if(context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
			// displayMetricsFromResources
			return context.getResources().getDimensionPixelSize(typedValue.resourceId);
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
		// statusBar沒有單獨配置空間時
		if(visibleDisplayFrameHe == displayHe){
			return visibleDisplayFrameHe;
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

	public static boolean isOrientationPortrait(Context context){
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	public static boolean isFillScreen(DisplayMetrics displayMetrics, int limitDipWidth){
		int displayAbsWidth = getAbsWidth(displayMetrics.widthPixels, displayMetrics.heightPixels);
		return displayAbsWidth / displayMetrics.density + 0.5f >= limitDipWidth;
	}

	public static boolean isFillScreen(Context context, int flag, int limitDipWidth){
		return isFillScreen(getDisplayMetrics(context, flag), limitDipWidth);
	}

	public static boolean isFillScreen(Context context, int limitDipWidth){
		return isFillScreen(getDisplayMetricsFromWindowManager(context), limitDipWidth);
	}

	public static int getPixels(DisplayMetrics displayMetrics, float dip){
		return (int)(dip * displayMetrics.density + 0.5f);
	}

	public static int getPixels(Context context, int flag, float dip){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getPixels(displayMetrics, dip);
	}

	public static int getPixels(Context context, float dip){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getPixels(displayMetrics, dip);
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
		return getWidthPixels(getDisplayMetricsFromWindowManager(context), isAbs);
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
		return getHeightPixels(getDisplayMetricsFromWindowManager(context), isAbs);
	}

	public static int getDip(DisplayMetrics displayMetrics, float pixels){
		return (int)(pixels / displayMetrics.density + 0.5f);
	}

	public static int getDip(Context context, int flag, float pixels){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDip(displayMetrics, pixels);
	}

	public static int getDip(Context context, float pixels){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getDip(displayMetrics, pixels);
	}

	public static int getWidthDip(DisplayMetrics displayMetrics, boolean isAbs){
		return getDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getWidthDip(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getWidthDip(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(DisplayMetrics displayMetrics, boolean isAbs){
		return getDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static float getMillimeter(float pixels, float dpi){
		return pixels / (dpi * (1f / 25.4f));
	}

	public static float getWidthMillimeter(DisplayMetrics displayMetrics, boolean isAbs){
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getWidthMillimeter(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getWidthMillimeter(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getHeightMillimeter(DisplayMetrics displayMetrics, boolean isAbs){
		return getMillimeter(getHeightPixels(displayMetrics, isAbs), displayMetrics.ydpi);
	}

	public static float getHeightMillimeter(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getMillimeter(getHeightPixels(displayMetrics, isAbs), displayMetrics.ydpi);
	}

	public static float getHeightMillimeter(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getMillimeter(getHeightPixels(displayMetrics, isAbs), displayMetrics.ydpi);
	}

	public static double getScreenInch(int realWidthPixels, int realHeightPixels, float xdpi, float ydpi){
		float widthInch = realWidthPixels / xdpi;
		float heightInch = realHeightPixels / ydpi;
		return Math.sqrt(Math.pow((double) widthInch, 2) + Math.pow((double) heightInch, 2));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static double getScreenInch(Context context){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(context);
		return getScreenInch(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels, realDisplayMetrics.xdpi, realDisplayMetrics.ydpi);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static double getScreenInch(Activity activity){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(activity);
		return getScreenInch(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels, realDisplayMetrics.xdpi, realDisplayMetrics.ydpi);
	}

	/**
	 * 取得螢幕每英吋點數(Pixels驗算DPI)
	 */
	public static double getScreenDPIFromPixels(int realWidthPixels, int realHeightPixels, float xdpi, float ydpi){
		double diagonalPixels = Math.sqrt(Math.pow((double)realWidthPixels, 2) + Math.pow((double)realHeightPixels, 2));
		return diagonalPixels / getScreenInch(realWidthPixels, realHeightPixels, xdpi, ydpi);
	}

	/**
	 * 取得螢幕每英吋點數(Pixels驗算DPI)
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static double getScreenDPIFromPixels(Context context){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(context);
		return getScreenDPIFromPixels(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels, realDisplayMetrics.xdpi, realDisplayMetrics.ydpi);
	}

	/**
	 * 取得螢幕每英吋點數(Pixels驗算DPI)
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static double getScreenDPIFromPixels(Activity activity){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(activity);
		return getScreenDPIFromPixels(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels, realDisplayMetrics.xdpi, realDisplayMetrics.ydpi);
	}

	/**
	 * 取得螢幕每英吋點數(DPI)
	 */
	public static float getScreenDPI(DisplayMetrics displayMetrics){
		return (displayMetrics.xdpi + displayMetrics.ydpi) / 2;
	}

	/**
	 * 取得螢幕每英吋點數(DPI)
	 */
	public static float getScreenDPI(Context context, int flag){
		return getScreenDPI(getDisplayMetrics(context, flag));
	}

	/**
	 * 取得XY DPI計算的每英吋螢幕像素密度
	 */
	public static float getDensityFromXYDPI(DisplayMetrics displayMetrics){
		return (displayMetrics.xdpi + displayMetrics.ydpi) / 2 / 160;
	}

	/**
	 * 取得XY DPI計算的每英吋螢幕像素密度
	 */
	public static float getDensityFromXYDPI(Context context, int flag){
		return getDensityFromXYDPI(getDisplayMetrics(context, flag));
	}

	/**
	 * 取得X DPI計算的標準Dip縮放比例
	 */
	public static float getDipScaleRateFromXDPI(DisplayMetrics displayMetrics){
		float realDensityX = displayMetrics.xdpi / 160;
		float realDipX = getWidthPixels(displayMetrics, true) / realDensityX;
		return getWidthDip(displayMetrics, true) / realDipX;
	}

	/**
	 * 取得X DPI計算的標準Dip縮放比例
	 */
	public static float getDipScaleRateFromXDPI(Context context, int flag){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDipScaleRateFromXDPI(displayMetrics);
	}

	/**
	 * 取得Y DPI計算的標準Dip縮放比例
	 */
	public static float getDipScaleRateFromYDPI(DisplayMetrics displayMetrics){
		float realDensityY = displayMetrics.ydpi / 160;
		float realDipY = getHeightPixels(displayMetrics, true) / realDensityY;
		return getHeightDip(displayMetrics, true) / realDipY;
	}

	/**
	 * 取得Y DPI計算的標準Dip縮放比例
	 */
	public static float getDipScaleRateFromYDPI(Context context, int flag){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDipScaleRateFromYDPI(displayMetrics);
	}

	/**
	 * 取得XY DPI計算的平均標準Dip縮放比例
	 */
	public static float getDipScaleRateFromXYDPI(DisplayMetrics displayMetrics){
		float realDensityX = getDipScaleRateFromXDPI(displayMetrics);
		float realDensityY = getDipScaleRateFromYDPI(displayMetrics);
		return (realDensityX + realDensityY) / 2;
	}

	/**
	 * 取得XY DPI計算的平均標準Dip縮放比例
	 */
	public static float getDipScaleRateFromXYDPI(Context context, int flag){
		return getDipScaleRateFromXYDPI(getDisplayMetrics(context, flag));
	}
}