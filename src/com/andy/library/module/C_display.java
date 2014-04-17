package com.andy.library.module;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Window;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.1.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_display {
	
	private static int visibleHe;
	
	public interface EventCallBack{
		public void completed(int visibleHe);
	}
	
	public static Display getDisplay(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		return display;
	}
	
	@SuppressLint("NewApi")
	public static Point getDisplaySize(Activity activity){
		// API 13
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		return point;
	}
	
	public static DisplayMetrics getDisplayMetrics(Activity activity){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm;
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
				callBack.completed(drawVisibleHeight(activity));
			}
		});
	}
	
	public static int drawVisibleHeight(Activity activity){
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//		rect.top = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//		rect.bottom = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getBottom();
		visibleHe = Math.abs(rect.top - rect.bottom);
		return visibleHe;
	}
	
	public static int getVisibleHeight(Activity activity){
		return getHeightPixels(activity, false) - getStatusBarHeight(activity);
	}
	
	public static int getStatusBarHeight(Activity activity){
		return getHeightPixels(activity, false) - visibleHe;
	}
	
	@SuppressLint("InlinedApi")
	public static int getActionBarHeight(Context context){
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		return context.getResources().getDimensionPixelSize(typedValue.resourceId);
	}
	
	public static boolean isOrientationPortrait(Activity activity){
		if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isBigScreen(Activity activity, int limitDipWidth){
		DisplayMetrics dm = getDisplayMetrics(activity);
		int width, height;
		if(Build.VERSION.SDK_INT >= 13){
			Point point = getDisplaySize(activity);
			width = point.x;
			height = point.y;
		}else{
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		int displayAbsWidth = getAbsWidth(width, height);
		if(displayAbsWidth / dm.density + 0.5f > limitDipWidth){
			return true;
		}
		return false;
	}
	
	public static int getWidthPixels(Activity activity, boolean isAbs){
		int width, height;
		if(Build.VERSION.SDK_INT >= 13){
			Point point = getDisplaySize(activity);
			width = point.x;
			height = point.y;
		}else{
			DisplayMetrics dm = getDisplayMetrics(activity);
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		if(isAbs){
			return getAbsWidth(width, height);
		}else{
			return width;
		}
	}
	
	public static int getHeightPixels(Activity activity, boolean isAbs){
		int width, height;
		if(Build.VERSION.SDK_INT >= 13){
			Point point = getDisplaySize(activity);
			width = point.x;
			height = point.y;
		}else{
			DisplayMetrics dm = getDisplayMetrics(activity);
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		if(isAbs){
			return getAbsHeight(width, height);
		}else{
			return height;
		}
	}
	
	public static int getWidthDip(Activity activity, boolean isAbs){
		DisplayMetrics dm = getDisplayMetrics(activity);
		int width, height;
		if(Build.VERSION.SDK_INT >= 13){
			Point point = getDisplaySize(activity);
			width = point.x;
			height = point.y;
		}else{
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		if(isAbs){
			return (int)(getAbsWidth(width, height) / dm.density + 0.5f);
		}else{
			return (int)(width / dm.density + 0.5f);
		}
	}
	
	public static int getHeightDip(Activity activity, boolean isAbs){
		DisplayMetrics dm = getDisplayMetrics(activity);
		int width, height;
		if(Build.VERSION.SDK_INT >= 13){
			Point point = getDisplaySize(activity);
			width = point.x;
			height = point.y;
		}else{
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		if(isAbs){
			return (int)(getAbsHeight(width, height) / dm.density + 0.5f);
		}else{
			return (int)(height / dm.density + 0.5f);
		}
	}
	
	public static int getDip(Activity activity, float pixels){
		DisplayMetrics dm = getDisplayMetrics(activity);
		return (int)(pixels / dm.density + 0.5f);
	}
	
	public static int getPixels(Activity activity, float dip){
		DisplayMetrics dm = getDisplayMetrics(activity);
		return (int)(dip * dm.density + 0.5f);
	}
	
	public static double getScreenInch(Activity activity){
		DisplayMetrics dm = getDisplayMetrics(activity);
		double widthInch = dm.widthPixels / dm.xdpi;
		double heightInch = dm.heightPixels / dm.ydpi;
		return Math.sqrt(Math.pow(widthInch, 2) + Math.pow(heightInch, 2));
	}
	
	// dpi驗算證明
	public double getScreenDpi(Activity activity){
		DisplayMetrics dm = getDisplayMetrics(activity);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		return diagonalPixels / getScreenInch(activity);
	}
	
	/**
	 * 取得XY平均真實螢幕密度
	 * @param activity
	 * @return
	 */
	public static float getRealXYDensity(Activity activity){
		DisplayMetrics dm = getDisplayMetrics(activity);
		return (dm.xdpi + dm.ydpi) / 2 / 160;
	}
	
	public static float getDipScaleX(Activity activity){
		DisplayMetrics dm = getDisplayMetrics(activity);
		float realDensityX = dm.xdpi / 160;
		float realDipX = getWidthPixels(activity, true) / realDensityX;
		return getWidthDip(activity, true) / realDipX;
	}
	
	public static float getDipScaleY(Activity activity){
		DisplayMetrics dm = getDisplayMetrics(activity);
		float realDensityY = dm.ydpi / 160;
		float realDipY = getHeightPixels(activity, true) / realDensityY;
		return getHeightDip(activity, true) / realDipY;
	}
}