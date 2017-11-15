/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.3.7
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
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "WeakerAccess", "SameParameterValue"})
public class DisplayUtils {

	public static final int LIMIT_DIP_WIDTH_320 = 320;
	public static final int LIMIT_DIP_WIDTH_360 = 360;
	public static final int LIMIT_DIP_WIDTH_411 = 411;
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

	public static final float RATIO_GOLDEN = 1.618f;
	public static final float RATIO_PLATINUM = 1.732f;
	public static final float RATIO_SILVER = 2.414f;
	public static final float RATIO_BRONZE = 3.303f;

	public interface MeasureCallback {
		void completed(int statusBarHe, int usableHe);
	}

	public static Display getDisplayFromActivity(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		return display;
	}

	public static @Nullable Display getDisplayFromContext(Context context){
		WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		if(windowManager == null){
			return null;
		}
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

	public static @Nullable DisplayMetrics getDisplayMetricsFromWindowManager(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		if(windowManager == null){
			return null;
		}
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}

	public static DisplayMetrics getDisplayMetricsFromWindowManager(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static @Nullable DisplayMetrics getRealDisplayMetricsFromWindowManager(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		if(windowManager == null){
			return null;
		}
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

	public static @Nullable DisplayMetrics getDisplayMetrics(Context context, int flag){
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

	/**
	 * Not contain StatusBar
	 */
	public static void measureUsableHeightWaitOnDraw(final View view, final MeasureCallback measureCallback){
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					//noinspection deprecation
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				Rect rect = new Rect();
				view.getWindowVisibleDisplayFrame(rect);
				measureCallback.completed(rect.top, Math.abs(rect.top - rect.bottom));
			}
		});
	}

	/**
	 * Not contain StatusBar
	 */
	public static void measureUsableHeightWaitOnDraw(Activity activity, MeasureCallback measureCallback){
		measureUsableHeightWaitOnDraw(activity.getWindow().getDecorView(), measureCallback);
	}

	/**
	 * Not contain StatusBar
	 */
	public static int measureUsableHeightForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		return Math.abs(rect.top - rect.bottom);
	}

	/**
	 * Not contain StatusBar
	 */
	public static int measureUsableHeightForOnDraw(Activity activity){
		return measureUsableHeightForOnDraw(activity.getWindow().getDecorView());
	}

	public static int measureStatusBarHeightForOnDraw(View view){
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}

	public static int measureStatusBarHeightForOnDraw(Activity activity){
		return measureStatusBarHeightForOnDraw(activity.getWindow().getDecorView());
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

	public static int getUsableHeight(Context context){
		return getHeightPixels(getDisplayMetricsFromWindowManager(context), false) - getStatusBarHeight(0);
	}

	public static int getUsableHeight(DisplayMetrics displayMetrics){
		return getHeightPixels(displayMetrics, false) - getStatusBarHeight(0);
	}

	public static int getStatusBarHeight(int defInt){
		Resources res = Resources.getSystem();
		int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
		if(resourceId > 0){
			// displayMetricsFromResources
			return res.getDimensionPixelSize(resourceId);
		}
		return defInt;
	}

	public static int getNavigationBarHeight(Context context, int defInt){
		Resources res = Resources.getSystem();
		int orientation = context.getResources().getConfiguration().orientation;
		int resourceId = res.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
		if(resourceId > 0){
			// displayMetricsFromResources
			return res.getDimensionPixelSize(resourceId);
		}
		return defInt;
	}

	public static boolean hasNavigationBar(boolean defBoolean){
		Resources res = Resources.getSystem();
		int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
		if(resourceId > 0){
			return res.getBoolean(resourceId);
		}
		return defBoolean;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static int measureNavigationBarHeight(Context context, DisplayMetrics displayMetrics){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(context);
		if(realDisplayMetrics == null){
			return -1;
		}
		return realDisplayMetrics.heightPixels - displayMetrics.heightPixels;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static int measureNavigationBarHeight(Context context){
		DisplayMetrics realDisplayMetrics = getRealDisplayMetricsFromWindowManager(context);
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		if(realDisplayMetrics == null || displayMetrics == null){
			return -1;
		}
		return realDisplayMetrics.heightPixels - displayMetrics.heightPixels;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context, int defInt){
		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
				// displayMetricsFromResources
				return context.getResources().getDimensionPixelSize(typedValue.resourceId);
			}
		}
		return defInt;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context, DisplayMetrics displayMetrics, int defInt){
		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
				return TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
			}
		}
		return defInt;
	}

	public static boolean isViewAnyPartVisibleInParentView(View parentView, View view){
		if(view.getVisibility() != View.VISIBLE){
			return false;
		}
		Rect rectHit = new Rect();
		parentView.getHitRect(rectHit);
		return view.getLocalVisibleRect(rectHit);
	}

	public static boolean isViewFullVisibleInParentView(View parentView, View view, int xOffset, int yOffset){
		if(view.getVisibility() != View.VISIBLE){
			return false;
		}
		Rect rectDrawing = new Rect();
		parentView.getDrawingRect(rectDrawing);
		rectDrawing.left += xOffset;
		rectDrawing.right += xOffset;
		rectDrawing.top += yOffset;
		rectDrawing.bottom += yOffset;
		return rectDrawing.left <= view.getLeft() && rectDrawing.right >= view.getLeft() + view.getWidth() &&
				rectDrawing.top <= view.getTop() && rectDrawing.bottom >= view.getTop() + view.getHeight();
	}

	public static boolean isOrientationPortrait(Context context){
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	public static boolean isOrientationLandscape(Context context){
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
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

	public static Rect getAppropriateSize(int intrinsicWidth, int intrinsicHeight, int minWidth, int minHeight, int maxWidth, int maxHeight, boolean isConflictLimitWidth){
		if(minWidth > maxWidth && minHeight > maxHeight){
			throw new IllegalArgumentException("minWidth:" + minWidth + " > maxWidth:" + maxWidth + " minHeight:" + minHeight + " > maxHeight:" + maxHeight);
		}else if(minWidth > maxWidth){
			throw new IllegalArgumentException("minWidth:" + minWidth + " > maxWidth:" + maxWidth);
		}else if(minHeight > maxHeight){
			throw new IllegalArgumentException("minHeight:" + minHeight + " > maxHeight:" + maxHeight);
		}
		float rateHeight = (intrinsicHeight + 0f) / intrinsicWidth;
		Rect rect = new Rect();
		rect.right = maxWidth;
		rect.bottom = (int)(rect.right * rateHeight);
		if(rect.bottom > maxHeight){
			rect.bottom = maxHeight;
			rect.right = (int)(rect.bottom * ((intrinsicWidth + 0f) / intrinsicHeight));
			if(rect.right < minWidth && isConflictLimitWidth){
				rect.right = minWidth;
				rect.bottom = (int)(rect.right * rateHeight);
			}
		}else if(rect.bottom < minHeight && !isConflictLimitWidth){
			rect.bottom = minHeight;
			rect.right = (int)(rect.bottom * ((intrinsicWidth + 0f) / intrinsicHeight));
		}
		return rect;
	}

	public static Rect getAppropriateSize(int intrinsicWidth, int intrinsicHeight, int maxWidth, int maxHeight, boolean isConflictLimitWidth){
		return getAppropriateSize(intrinsicWidth, intrinsicHeight, 0, 0, maxWidth, maxHeight, isConflictLimitWidth);
	}

	public static int getDipToPixels(DisplayMetrics displayMetrics, float dip){
		return (int)(dip * displayMetrics.density + 0.5f);
	}

	public static int getDipToPixels(Context context, int flag, float dip){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getDipToPixels(displayMetrics, dip);
	}

	public static int getDipToPixels(Context context, float dip){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getDipToPixels(displayMetrics, dip);
	}

	public static int getPixelsToDip(DisplayMetrics displayMetrics, float pixels){
		return (int)(pixels / displayMetrics.density + 0.5f);
	}

	public static int getPixelsToDip(Context context, int flag, float pixels){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getPixelsToDip(displayMetrics, pixels);
	}

	public static int getPixelsToDip(Context context, float pixels){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getPixelsToDip(displayMetrics, pixels);
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

	public static int getWidthDip(DisplayMetrics displayMetrics, boolean isAbs){
		return getPixelsToDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getWidthDip(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getPixelsToDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getWidthDip(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getPixelsToDip(displayMetrics, getWidthPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(DisplayMetrics displayMetrics, boolean isAbs){
		return getPixelsToDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		return getPixelsToDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static int getHeightDip(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		return getPixelsToDip(displayMetrics, getHeightPixels(displayMetrics, isAbs));
	}

	public static float getMillimeter(float pixels, float dpi){
		return pixels / (dpi * (1f / 25.4f));
	}

	public static float getWidthMillimeter(DisplayMetrics displayMetrics, boolean isAbs){
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getWidthMillimeter(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		if(displayMetrics == null){
			return -1f;
		}
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getWidthMillimeter(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		if(displayMetrics == null){
			return -1f;
		}
		return getMillimeter(getWidthPixels(displayMetrics, isAbs), displayMetrics.xdpi);
	}

	public static float getHeightMillimeter(DisplayMetrics displayMetrics, boolean isAbs){
		return getMillimeter(getHeightPixels(displayMetrics, isAbs), displayMetrics.ydpi);
	}

	public static float getHeightMillimeter(Context context, int flag, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetrics(context, flag);
		if(displayMetrics == null){
			return -1f;
		}
		return getMillimeter(getHeightPixels(displayMetrics, isAbs), displayMetrics.ydpi);
	}

	public static float getHeightMillimeter(Context context, boolean isAbs){
		DisplayMetrics displayMetrics = getDisplayMetricsFromWindowManager(context);
		if(displayMetrics == null){
			return -1f;
		}
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
		if(realDisplayMetrics == null){
			return -1f;
		}
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
		if(realDisplayMetrics == null){
			return -1f;
		}
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

	public static float getRatioGoldenReverse(float value){
		return value - value / RATIO_GOLDEN;
	}

	public static float getRatioPlatinumReverse(float value){
		return value - value / RATIO_PLATINUM;
	}

	public static float getRatioSilverReverse(float value){
		return value - value / RATIO_SILVER;
	}

	public static float getRatioBronzeReverse(float value){
		return value - value / RATIO_BRONZE;
	}
}