/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.4.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "ForLoopReplaceableByForEach", "IfCanBeSwitch", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "UnusedAssignment"})
public class Utils {
	
	public static final int LIMIT_DIP_WIDTH_320 = 320;
	public static final int LIMIT_DIP_WIDTH_360 = 360;
	public static final int LIMIT_DIP_WIDTH_480 = 480;
	public static final int LIMIT_DIP_WIDTH_540 = 540;
	public static final int LIMIT_DIP_WIDTH_600 = 600;
	public static final int LIMIT_DIP_WIDTH_720 = 720;
	public static final int LIMIT_DIP_WIDTH = LIMIT_DIP_WIDTH_600;
	public static final int SIZE_TEXT_S = 0;
	public static final int SIZE_BULLET_S = 1;
	public static final int SIZE_TITLE_S = 2;
	public static final int SIZE_TAB_S = 3;
	public static final int SIZE_SUBJECT_S = 4;
	public static final int SIZE_TEXT = 5;
	public static final int SIZE_BULLET = 6;
	public static final int SIZE_TITLE = 7;
	public static final int SIZE_TAB = 8;
	public static final int SIZE_SUBJECT = 9;
	public static final int SIZE_TEXT_L = 10;
	public static final int SIZE_BULLET_L = 11;
	public static final int SIZE_TITLE_L = 12;
	public static final int SIZE_TAB_L = 13;
	public static final int SIZE_SUBJECT_L = 14;
	public static final int SIZE_TEXT_XL = 15;
	public static final int SIZE_BULLET_XL = 16;
	public static final int SIZE_TITLE_XL = 17;
	public static final int SIZE_TAB_XL = 18;
	public static final int SIZE_SUBJECT_XL = 19;
	
	public static final String ASSETS_PATH = "file:///android_asset/";
	public static final String SP_KEY_STATUS_BAR_HEIGHT = "statusBarHe";
	public static final String SP_MAP_HEAD = "/!#/spMapHead/#!/";
	public static final String SP_MAP_ITEM_LEFT_BORDER = "(!#/";
	public static final String SP_MAP_ITEM_RIGHT_BORDER = "/#!)=";
	public static final String SP_MAP_DELIMITER = "/!#/-/#!/";
	public static final String REG_EXP_INT = "^-?\\d+$";
	public static final String REG_EXP_INT_POS = "^\\d+$";
	public static final String REG_EXP_INT_NEG = "^-\\d+$";
	public static final String REG_EXP_FLOAT = "^(-?\\d+)(\\.\\d+)?$";
	public static final String REG_EXP_FLOAT_POS = "^\\d+(\\.\\d+)?$";
	public static final String REG_EXP_FLOAT_NEG = "^-\\d+(\\.\\d+)?$";
	public static final String REG_EXP_FLOAT_STRICT = "^(-?\\d+)(\\.\\d+){1}$";
	public static final String REG_EXP_FLOAT_STRICT_POS = "^\\d+(\\.\\d+){1}$";
	public static final String REG_EXP_FLOAT_STRICT_NEG = "^-\\d+(\\.\\d+){1}$";
	public static final String REG_EXP_EMAIL = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
//	final String[][] MIME_TABLE = {
//			{".3gp", "video/3gpp"}, 
//			{".apk", "application/vnd.android.package-archive"}, 
//			{".asf", "video/x-ms-asf"}, 
//			{".avi", "video/x-msvideo"}, 
//			{".bin", "application/octet-stream"}, 
//			{".bmp", "image/bmp"}, 
//			{".c", "text/plain"}, 
//			{".class", "application/octet-stream"}, 
//			{".conf", "text/plain"}, 
//			{".cpp", "text/plain"}, 
//			{".doc", "application/msword"}, 
//			{".exe", "application/octet-stream"}, 
//			{".gif", "image/gif"}, 
//			{".gtar", "application/x-gtar"}, 
//			{".gz", "application/x-gzip"}, 
//			{".h", "text/plain"}, 
//			{".htm", "text/html"}, 
//			{".html", "text/html"}, 
//			{".jar", "application/java-archive"}, 
//			{".java", "text/plain"}, 
//			{".jpeg", "image/jpeg"}, 
//			{".jpg", "image/jpeg"}, 
//			{".js", "application/x-javascript"}, 
//			{".log", "text/plain"}, 
//			{".m3u", "audio/x-mpegurl"}, 
//			{".m4a", "audio/mp4a-latm"}, 
//			{".m4b", "audio/mp4a-latm"}, 
//			{".m4p", "audio/mp4a-latm"}, 
//			{".m4u", "video/vnd.mpegurl"}, 
//			{".m4v", "video/x-m4v"}, 
//			{".mov", "video/quicktime"}, 
//			{".mp2", "audio/x-mpeg"}, 
//			{".mp3", "audio/x-mpeg"}, 
//			{".mp4", "video/mp4"}, 
//			{".mpc", "application/vnd.mpohun.certificate"}, 
//			{".mpe", "video/mpeg"}, 
//			{".mpeg", "video/mpeg"}, 
//			{".mpg", "video/mpeg"}, 
//			{".mpg4", "video/mp4"}, 
//			{".mpga", "audio/mpeg"}, 
//			{".msg", "application/vnd.ms-outlook"}, 
//			{".ogg", "audio/ogg"}, 
//			{".pdf", "application/pdf"}, 
//			{".png", "image/png"}, 
//			{".pps", "application/vnd.ms-powerpoint"}, 
//			{".ppt", "application/vnd.ms-powerpoint"}, 
//			{".prop", "text/plain"}, 
//			{".rar", "application/x-rar-compressed"}, 
//			{".rc", "text/plain"}, 
//			{".rmvb", "audio/x-pn-realaudio"}, 
//			{".rtf", "application/rtf"}, 
//			{".sh", "text/plain"}, 
//			{".tar", "application/x-tar"}, 
//			{".tgz", "application/x-compressed"}, 
//			{".txt", "text/plain"}, 
//			{".wav", "audio/x-wav"}, 
//			{".wma", "audio/x-ms-wma"}, 
//			{".wmv", "audio/x-ms-wmv"}, 
//			{".wps", "application/vnd.ms-works"}, 
//			{".xml", "text/plain"}, 
//			{".z", "application/x-compress"}, 
//			{".zip", "application/zip"}, 
//			{"", "*/*"}
//	};
	
	public static String inputStreamToString(InputStream is, Charset charset, int bufferSize){
		if(is == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.print("charset get fail, using default, ");
		}
//		System.out.println("charset = " + charset.displayName());
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		BufferedReader reader;
		StringBuilder stringBuilder = new StringBuilder();// StringBuilder速度較快但不支援多執行緒同步
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(is, charset), bufferSize);
			try {
				while((line = reader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				is.close();
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			reader = null;
			stringBuilder = null;
			line = null;
			stringBuilder = new StringBuilder();
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
	
	public static String inputStreamToString(InputStream is, Charset charset){
		return inputStreamToString(is, charset, 1024 * 16);
	}
	
	public static byte[] inputStreamToByteArray(InputStream is, int bufferSize){
		if(is == null){
			return null;
		}
		byte[] byteArray = null;
		int progress;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		ByteArrayOutputStream baos;
		try {
			buffer = new byte[bufferSize];
			baos = new ByteArrayOutputStream();
			try {
				while((progress = is.read(buffer)) != -1){
					baos.write(buffer, 0, progress);
				}
				baos.flush();
			} finally {
				is.close();
			}
			try {
				byteArray = baos.toByteArray();
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			byteArray = null;
			buffer = null;
			baos = null;
			e.printStackTrace();
		}
		return byteArray;
	}
	
	public static byte[] inputStreamToByteArray(InputStream is){
		return inputStreamToByteArray(is, 1024 * 16);
	}
	
	public static OutputStream byteArrayToOutputStream(byte[] byteArray, OutputStream os, int bufferSize){
		if(byteArray == null || os == null){
			return null;
		}
		int progress;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		InputStream is;
		try {
			buffer = new byte[bufferSize];
			is = new ByteArrayInputStream(byteArray);
			try {
				while((progress = is.read(buffer)) != -1){
					os.write(buffer, 0, progress);
				}
				os.flush();
			} finally {
				is.close();
				is = null;
			}
			return os;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			byteArray = null;
			os = null;
			buffer = null;
			is = null;
			e.printStackTrace();
		}
		return os;
	}
	
	public static OutputStream byteArrayToOutputStream(byte[] byteArray, OutputStream os){
		return byteArrayToOutputStream(byteArray, os, 1024 * 16);
	}
	
	public static byte[] fileToByteArray(File file){
		if(file == null){
			return null;
		}
		byte[] byteArray = null;
		try {
			InputStream is = new FileInputStream(file);
			int progress = 0;
			byteArray = new byte[(int)file.length()];
			try {
				while(progress < byteArray.length){
					progress = progress + is.read(byteArray, progress, byteArray.length - progress);
				}
			} finally {
				is.close();
				is = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			file = null;
			byteArray = null;
			e.printStackTrace();
		}
		return byteArray;
	}
	
	public static boolean inputStreamWriteOutputStream(InputStream is, OutputStream os, int bufferSize){
		if(is == null || os == null){
			return false;
		}
		int progress;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		try {
			buffer = new byte[bufferSize];
			try {
				while((progress = is.read(buffer)) != -1){
					os.write(buffer, 0, progress);
				}
				os.flush();
			} finally {
				is.close();
				os.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			os = null;
			buffer = null;
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean inputStreamWriteOutputStream(InputStream is, OutputStream os){
		return inputStreamWriteOutputStream(is, os, 1024 * 16);
	}
	
	public static boolean ByteArrayWriteOutStream(byte[] byteArray, OutputStream os, int bufferSize){
		if(byteArray == null || os == null){
			return false;
		}
		InputStream bais = new ByteArrayInputStream(byteArray);
		return inputStreamWriteOutputStream(bais, os, bufferSize);
	}
	
	public static boolean ByteArrayWriteOutStream(byte[] byteArray, OutputStream os){
		return ByteArrayWriteOutStream(byteArray, os, 1024 * 16);
	}
	
	public static boolean writeSDCardFile(InputStream is, String directory, String fileName, int bufferSize){
		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			file = new File(sdCardPath + directory);
			if(!file.exists()){
				if(!file.mkdirs()){
					System.out.println("directory already existed");
				}
			}
			file = new File(sdCardPath + directory + fileName);
			try {
				return inputStreamWriteOutputStream(is, new FileOutputStream(file, false), bufferSize);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean writeSDCardFile(InputStream is, String directory, String fileName){
		return writeSDCardFile(is, directory, fileName, 1024 * 16);
	}
	
	public static byte[] readSDCardFile(String directory, String fileName){
		// <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			file = new File(sdCardPath + directory + fileName);
			return fileToByteArray(file);
		}
		return null;
	}
	
	public static boolean copyUseFileChannel(String pathRead, String pathWrite, int bufferSize){
		try {
			File file = new File(pathRead);
			FileInputStream fileInputStream = new FileInputStream(file);
			file = new File(pathWrite);
			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			FileChannel fileChannelRead = fileInputStream.getChannel();
			FileChannel fileChannelWrite = fileOutputStream.getChannel();
			
			if(bufferSize < 8192){
				bufferSize = 8192;
			}
			try {
				ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
				while(fileChannelRead.read(byteBuffer) != -1){
					byteBuffer.flip();
					fileChannelWrite.write(byteBuffer);
					byteBuffer.clear();
				}
			} finally {
				fileChannelRead.close();
				fileInputStream.close();
				fileChannelWrite.close();
				fileOutputStream.close();
			}
			
			return file.isFile();
		} catch (IOException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean copyUseRandomAccessFile(String pathRead, String pathWrite, int bufferSize){
		try {
			File file = new File(pathRead);
			RandomAccessFile randomAccessFileRead = new RandomAccessFile(file, "r");
			file = new File(pathWrite);
			RandomAccessFile randomAccessFileWrite = new RandomAccessFile(file, "rw");
			if(bufferSize < 8192){
				bufferSize = 8192;
			}
			try {
				byte[] buffer = new byte[bufferSize];
				while(randomAccessFileRead.read(buffer) != -1){
					randomAccessFileWrite.write(buffer);
				}
			} finally {
				randomAccessFileRead.close();
				randomAccessFileWrite.close();
			}
			return file.isFile();
		} catch (IOException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean copyUseMappedByteBuffer(String pathRead, String pathWrite, long bufferSize){
		try {
			File file = new File(pathRead);
			FileInputStream fileInputStream = new FileInputStream(file);
			file = new File(pathWrite);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			FileChannel fileChannelRead = fileInputStream.getChannel();
			FileChannel fileChannelWrite = randomAccessFile.getChannel();
			
			long progress = 0;
			MappedByteBuffer mappedByteBufferRead, mappedByteBufferWrite;
			if(bufferSize < 8192){
				bufferSize = 8192;
			}
			try {
				while(progress < fileChannelRead.size()){
					if(fileChannelRead.size() - progress < bufferSize){
						bufferSize = fileChannelRead.size() - progress;
					}
					mappedByteBufferRead = fileChannelRead.map(FileChannel.MapMode.READ_ONLY, progress, bufferSize);
					mappedByteBufferWrite = fileChannelWrite.map(FileChannel.MapMode.READ_WRITE, progress, bufferSize);
					mappedByteBufferWrite.put(mappedByteBufferRead);
					progress = progress + bufferSize;
					mappedByteBufferRead.clear();
					mappedByteBufferWrite.clear();
				}
			} finally {
				mappedByteBufferRead = null;
				mappedByteBufferWrite = null;
				fileChannelRead.close();
				fileInputStream.close();
				fileChannelWrite.close();
				randomAccessFile.close();
			}
			
			return file.isFile();
		} catch (IOException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean fileCreate(File file, boolean targetIsFile, boolean isEnforce){
		if(file.exists()){
			if(targetIsFile){
				if(file.isFile()){
					return true;
				}
				if(isEnforce && file.canWrite() && file.delete()){
					try {
						return file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				if(file.isDirectory()){
					return true;
				}
				if(isEnforce && file.canWrite() && file.delete()){
					return file.mkdirs();
				}
			}
			return targetIsFile ? file.isFile() : file.isDirectory();
		}

		if(!file.getParentFile().exists() || !file.getParentFile().isDirectory()){
			if(filePathLayersCheck(file.getParentFile(), isEnforce)){
				if(!file.getParentFile().mkdirs()){
					return false;
				}
			}else{
				return false;
			}
		}

		if(targetIsFile){
			try {
				return file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			return file.mkdirs();
		}
		return false;
	}
	
	public static boolean filePathLayersCheck(File file, boolean isEnforce){
		if(file.exists()){
			return file.isDirectory() ||
					isEnforce && file.canWrite() && file.delete() && 
							(file.mkdirs() || filePathLayersCheck(file.getParentFile(), true));
		}
		return file.mkdirs() || filePathLayersCheck(file.getParentFile(), isEnforce);
	}
	
	public static boolean fileDeleteAll(File file){
		File[] fileArray = file.listFiles();
		if(fileArray != null){
			for(int i=0; i<fileArray.length; i++){
				if(fileArray[i].isDirectory()){
					fileDeleteAll(fileArray[i]);
				}else if(!fileArray[i].delete()){
					System.out.println("not delete file " + fileArray[i].getPath());
				}
			}
		}
		return file.delete();
	}
	
	public static boolean isFillScreen(DisplayMetrics displayMetrics, int limitDipWidth){
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;
		int displayAbsWidth = width < height ? width : height;
		return displayAbsWidth / displayMetrics.density + 0.5f >= limitDipWidth;
	}
	
	public static boolean isFillScreen(Context context, int limitDipWidth){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return isFillScreen(displayMetrics, limitDipWidth);
	}
	
	public static int getVisibleHeightSP(Context context, String spName){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		int visibleHe = displayMetrics.heightPixels - getStatusBarHeightSP(context, spName);
		return visibleHe;
	}
	
	public static int getStatusBarHeightSP(Context context, String spName){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getInt(Utils.SP_KEY_STATUS_BAR_HEIGHT, 0);
	}
	
	public static int getVisibleHeight(DisplayMetrics displayMetrics){
		int visibleHe = displayMetrics.heightPixels - getStatusBarHeight(0);
		return visibleHe;
	}

	public static int getStatusBarHeight(int defValue){
		Resources res = Resources.getSystem();
		int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
		if(resourceId > 0){
			return res.getDimensionPixelSize(resourceId);
		}
		return defValue;
	}
	
	public static int getVisibleHeight(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return getVisibleHeight(displayMetrics);
	}

	public static TypedValue getAttribute(Context context, int attrResource){
		TypedValue typedValue = new TypedValue();
		Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(attrResource, typedValue, true)){
				return typedValue;
			}
		}
		return null;
	}

	public static int getAttributeResource(Context context, int attrResource, int defResource){
		TypedValue typedValue = new TypedValue();
		Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(attrResource, typedValue, true)){
				return typedValue.resourceId;
			}
		}
		return defResource;
	}

	public static int getAttributePixels(Context context, DisplayMetrics displayMetrics, int attrResource, int defValue){
		TypedValue typedValue = new TypedValue();
		Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(attrResource, typedValue, true)){
				return TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
			}
		}
		return defValue;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context, DisplayMetrics displayMetrics, int defValue){
		return getAttributePixels(context, displayMetrics, android.R.attr.actionBarSize, defValue);
	}

	public static boolean isOnViewVisible(View parentsView, View view){
		if(view.getVisibility() != View.VISIBLE){
			return false;
		}
		Rect rectHit = new Rect();
		parentsView.getHitRect(rectHit);
		return view.getLocalVisibleRect(rectHit);
	}

	public static boolean isViewVisible(View parentsView, View view, int xOffset, int yOffset){
		if(view.getVisibility() != View.VISIBLE){
			return false;
		}
		Rect rectScroll = new Rect();
		parentsView.getDrawingRect(rectScroll);
		rectScroll.left += xOffset;
		rectScroll.right += xOffset;
		rectScroll.top += yOffset;
		rectScroll.bottom += yOffset;
		boolean isViewVisible = rectScroll.left <= view.getLeft() && rectScroll.right >= view.getLeft() + view.getWidth() && 
				rectScroll.top <= view.getTop() && rectScroll.bottom >= view.getTop() + view.getHeight();
		return isViewVisible;
	}

	@SuppressWarnings("deprecation")
	public static void clearWebViewCookie(Context context){
		CookieManager cookieManager;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookies(null);
			cookieManager.flush();
		}else{
			CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
			cookieSyncManager.startSync();
			cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			cookieSyncManager.stopSync();
		}
	}

	public static void setToast(Context context, CharSequence text, int gravity, int duration){
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(gravity, 0, 0);
		toast.show();
	}

	public static void setToast(Context context, CharSequence text, int duration){
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public static void setToast(Context context, CharSequence text){
		setToast(context, text, Toast.LENGTH_SHORT);
	}

	public static void setTextSize(Context context, TextView textView, int unit, float size){
		DisplayMetrics dmWin = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dmWin);
		DisplayMetrics dmRes = context.getResources().getDisplayMetrics();
		if(dmWin.scaledDensity == dmRes.scaledDensity){
			textView.setTextSize(unit, size);
		}else{
			if(size != textView.getTextSize()){
				textView.getPaint().setTextSize(TypedValue.applyDimension(unit, size, dmWin));
				if (textView.getLayout() != null) {
					// Reflection反射調用private方法
					try {
						Method method = textView.getClass().getDeclaredMethod("nullLayouts");
						method.setAccessible(true);
						method.invoke(textView);
						method.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					textView.requestLayout();
					textView.invalidate();
				}
			}
		}
	}
	
	public static void setTextSize(Context context, TextView textView, float size){
		setTextSize(context, textView, TypedValue.COMPLEX_UNIT_SP, size);
	}
	
	public static void setTextSizeMethod(Context context, TextView textView, int unit, float size){
		DisplayMetrics dmWin = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dmWin);
		DisplayMetrics dmRes = context.getResources().getDisplayMetrics();
		if(dmWin.scaledDensity == dmRes.scaledDensity){
			textView.setTextSize(unit, size);
		}else{
			// Reflection反射調用private方法
			try {
				Method method = textView.getClass().getDeclaredMethod("setRawTextSize", float.class);
				method.setAccessible(true);
				method.invoke(textView, TypedValue.applyDimension(unit, size, dmWin));
				method.setAccessible(false);
			} catch (Exception e) {
				setTextSize(context, textView, unit, size);
				e.printStackTrace();
			}
		}
	}
	
	public static void setTextSizeMethod(Context context, TextView textView, float size){
		setTextSizeMethod(context, textView, TypedValue.COMPLEX_UNIT_SP, size);
	}
	
	private static int getTextSize(int flag){
		int textSize = 15;
		switch (flag) {
			case SIZE_SUBJECT_XL:textSize = 29;break;
			case SIZE_TAB_XL:textSize = 28;break;
			case SIZE_TITLE_XL:textSize = 27;break;
			case SIZE_BULLET_XL:textSize = 26;break;
			case SIZE_TEXT_XL:textSize = 25;break;
			case SIZE_SUBJECT_L:textSize = 24;break;
			case SIZE_TAB_L:textSize = 23;break;
			case SIZE_TITLE_L:textSize = 22;break;
			case SIZE_BULLET_L:textSize = 21;break;
			case SIZE_TEXT_L:textSize = 20;break;
			case SIZE_SUBJECT:textSize = 19;break;
			case SIZE_TAB:textSize = 18;break;
			case SIZE_TITLE:textSize = 17;break;
			case SIZE_BULLET:textSize = 16;break;
			case SIZE_TEXT:textSize = 15;break;
			case SIZE_SUBJECT_S:textSize = 14;break;
			case SIZE_TAB_S:textSize = 13;break;
			// Not recommended
			case SIZE_TITLE_S:textSize = 12;break;
			case SIZE_BULLET_S:textSize = 11;break;
			case SIZE_TEXT_S:textSize = 10;break;
		}
		return textSize;
	}
	
	public static int getTextSize(int flag, boolean isBigScreen, int offsetSize){
		int textSize = getTextSize(flag);
		if(isBigScreen){
			textSize = textSize + offsetSize;
		}
		return textSize;
	}
	
	public static int getTextSize(int flag, boolean isBigScreen){
		return getTextSize(flag, isBigScreen, 3);
	}
	
	public static ColorStateList getColorStateList(Resources res, int colorResource, int defaultColor){
		ColorStateList colorStateList;
		XmlPullParser xpp = res.getXml(colorResource);
		try {
			colorStateList = ColorStateList.createFromXml(res, xpp);
		} catch (XmlPullParserException | IOException e) {
			colorStateList = ColorStateList.valueOf(defaultColor);
			e.printStackTrace();
		}
		return colorStateList;
	}
	
	public static float getTextWidths(Paint paint, String text){
		/*
		 * 1.
		 * width = paint.measureText(text);
		 * 
		 * 2.
		 * width = Layout.getDesiredWidth(text, textPaint);
		 * 
		 * 3.
		 * width = new StaticLayout(text, textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true).getLineWidth(0);
		 * 
		 * 4.minimal bounds
		 * Rect rect = new Rect();
		 * paint.getTextBounds(text, 0, text.length(), rect);
		 * width = rect.width();
		 */
		
		float width = 0;
		if(TextUtils.isEmpty(text)){
			return 0;
		}
		
		int length = text.length();
		float[] widths = new float[length];
		paint.getTextWidths(text, widths);
		for(int i=0; i<length; i++){
			width = width + (float) Math.ceil(widths[i]);
		}
		return width;
	}
	
	public static float getTextBaselineY(Paint paint){
		FontMetrics fontMetrics = paint.getFontMetrics();
		float baselineY = (fontMetrics.bottom - fontMetrics.top) / 2 + fontMetrics.bottom;
		return baselineY;
	}

	public static String neatString(String string){
		if(string != null){
			string = string.replace("\r\n", "").replace("\n", "").trim();
		}
		return string;
	}

	public static Object removeNull(Object object, Object replace){
		if(object == null){
			object = replace;
		}
		return object;
	}

	public static String removeNull(Object object, String replace){
		if(object == null || object.toString().length() == 0){
			object = replace;
		}
		return object.toString();
	}

	public static String removeNull(Object object){
		return removeNull(object, "");
	}

	public static String halfWidthToFullWidth(String text){
		StringBuilder stringBuilder = new StringBuilder();
		char word;
		for(int i=0; i<text.length(); i++){
			word = text.charAt(i);
			// 半形ASCII 33~126 與 全形ASCII 65281~65374 對應之 ASCII 皆相差 65248
			if(word > 32 && word < 127){
				word = (char)((int)word + 65248);
			}
			stringBuilder.append(word);
		}
		return stringBuilder.toString();
	}

	public static String getXmlEscapeText(String xml) {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < xml.length(); i++){
			char c = xml.charAt(i);
			switch(c){
				case '<': stringBuilder.append("&lt;"); break;
				case '>': stringBuilder.append("&gt;"); break;
				case '\"': stringBuilder.append("&guot;"); break;
				case '&': stringBuilder.append("&amp;"); break;
				case '\'': stringBuilder.append("&apos;"); break;
				default:
					if(c>0x7e) {
						stringBuilder.append("&#").append((int)c).append(";");
					}else{
						stringBuilder.append(c);
					}
			}
		}
		return stringBuilder.toString();
	}
	
	public static String getStringSymbolCombine(String body, String sub, String delimiter, boolean isAllowRepeat){
		if(TextUtils.isEmpty(sub)){
			return body;
		}
		if(body == null){
			body = "";
		}

		StringBuilder stringBuilder = new StringBuilder(body);
		String[] subArray = sub.split(delimiter);
		String start, end, startSub, middleSub, endSub;
		try {
			start = stringBuilder.substring(0, stringBuilder.indexOf(delimiter)) + delimiter;
		} catch (Exception e) {
			start = stringBuilder.toString();
		}
		for(int i=0; i<subArray.length; i++){
			try {
				end = stringBuilder.substring(stringBuilder.lastIndexOf(delimiter));
			} catch (Exception e) {
				end = "";
			}
			startSub = subArray[i] + delimiter;
			middleSub = delimiter + subArray[i] + delimiter;
			endSub = delimiter + subArray[i];
			if(isAllowRepeat || !(stringBuilder.indexOf(middleSub) > -1 || start.equals(startSub) || end.equals(endSub) ||
					start.equals(subArray[i]))){
				if(stringBuilder.length() == 0){
					stringBuilder.append(subArray[i]);
				}else{
					stringBuilder.append(endSub);
				}
			}
		}
		return stringBuilder.toString();
	}

	public static String getStringSymbolCombine(String body, String sub, boolean isAllowRepeat){
		return getStringSymbolCombine(body, sub, SP_MAP_DELIMITER, isAllowRepeat);
	}

	public static String findStringTarget(String text, String target, String borderFront, String borderRear){
		if(text == null || target == null || borderFront == null || borderRear == null){
			return null;
		}
		int indexRear = text.indexOf(target);
		if(indexRear < 0){
			return null;
		}
		indexRear = text.indexOf(borderRear, indexRear);
		if(indexRear < 0){
			return null;
		}
		int indexFront = text.lastIndexOf(borderFront, indexRear - 1);
		if(indexFront < 0){
			return null;
		}
		return text.substring(indexFront, indexRear + 1);
	}

	public static List<String> filterDigitInString(String text, boolean isAddAloneExistKeyword, String...jointKeywords){
		List<String> list = new ArrayList<String>();
		StringBuilder stringBuilder = new StringBuilder();
		boolean isContainDigit = false;
		char charFind, charKeyword;
		int index = 0, indexKeywordArray, indexKeyword;
		int length = text.length();
		while (index < length) {
			charFind = text.charAt(index);
			if(Character.isDigit(charFind)){
				stringBuilder.append(charFind);
				isContainDigit = true;
				index++;
				continue;
			}
			
			findKeywords :{
				indexKeywordArray = 0;
				indexKeyword = 0;
				while (indexKeywordArray < jointKeywords.length) {
					charFind = text.charAt(index + indexKeyword);
					charKeyword = jointKeywords[indexKeywordArray].charAt(indexKeyword);
					while (indexKeyword < jointKeywords[indexKeywordArray].length() && charFind == charKeyword) {
						if(indexKeyword == jointKeywords[indexKeywordArray].length() - 1){
							stringBuilder.append(jointKeywords[indexKeywordArray]);
							break findKeywords;
						}
						indexKeyword++;
					}
					indexKeyword = 0;
					indexKeywordArray++;
				}
				
				// index not found digit and keyword
				if(stringBuilder.length() > 0){
					if(isContainDigit || isAddAloneExistKeyword){
						list.add(stringBuilder.toString());
					}
					stringBuilder = new StringBuilder();
					isContainDigit = false;
				}
			}
			index += indexKeyword + 1;
		}
		return list;
	}

	public static List<String> filterDigitInString(String text, String...jointKeywords){
		return filterDigitInString(text, false, jointKeywords);
	}
	
	@SuppressLint("CommitPrefEdits")
	public static SharedPreferences.Editor getNotYetCommitSharedPreferencesEditor(Context context, String spName, String key, Object value
			, boolean toggleMode) {
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		SharedPreferences.Editor spEdit = sp.edit();
		if(toggleMode && sp.contains(key)){
			spEdit.remove(key);
		}else if(value != null){
			if(value instanceof Boolean){
				spEdit.putBoolean(key, Boolean.parseBoolean(value.toString()));
			}else if(value instanceof Integer){
				spEdit.putInt(key, Integer.parseInt(value.toString()));
			}else if(value instanceof Long){
				spEdit.putLong(key, Long.parseLong(value.toString()));
			}else if(value instanceof Float){
				spEdit.putFloat(key, Float.parseFloat(value.toString()));
			}else if(value instanceof String){
				spEdit.putString(key, value.toString());
			}
		}
		return spEdit;
	}
	
	public static boolean putSharedPreferences(final Context context, final String spName, final String key, final Object value
			, final boolean toggleMode){
		return getNotYetCommitSharedPreferencesEditor(context, spName, key, value, toggleMode).commit();
	}
	
	public static boolean putSharedPreferences(final Context context, final String spName, final String key, final Object value){
		return getNotYetCommitSharedPreferencesEditor(context, spName, key, value, false).commit();
	}
	
	public static void putSharedPreferences(final Context context, final String spName, final String key, final Object value
			, final boolean toggleMode, final Handler handler){
		final SharedPreferences.Editor spEdit = getNotYetCommitSharedPreferencesEditor(context, spName, key, value, toggleMode);
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean isSuccess = spEdit.commit();
				if(handler != null){
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putBoolean(key, isSuccess);
					msg.what = 1;
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		});
		thread.start();
	}
	
	public static void putSharedPreferences(Context context, String spName, String key, Object value, Handler handler){
		putSharedPreferences(context, spName, key, value, false, handler);
	}
	
	public static boolean removeSharedPreferences(Context context, String spName, String key){
		return putSharedPreferences(context, spName, key, null, true);
	}
	
	public static void removeSharedPreferences(Context context, String spName, String key, Handler handler){
		putSharedPreferences(context, spName, key, null, true, handler);
	}
	
	public static void putSharedPreferencesMap(Context context, String spName, String mapSaveKey, Map<String, String> map
			, final Handler handler){
		putSharedPreferencesMap(context, spName, mapSaveKey, map, false, handler);
	}
	
	public static void putSharedPreferencesMap(final Context context, final String spName, final String mapSaveKey
			, final Map<String, String> map, final boolean toggleMode, final Handler handler){
		final SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final SharedPreferences.Editor spEdit = sp.edit();
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				putSharedPreferencesMap(context, spName, mapSaveKey, map, toggleMode, sp, spEdit);
				
				boolean isSuccess = spEdit.commit();
				if(handler != null){
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putBoolean(mapSaveKey, isSuccess);
					msg.what = 1;
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		});
		thread.start();
	}
	
	private static void putSharedPreferencesMap(Context context, String spName, String mapSaveKey, Map<String, String> map
			, boolean toggleMode, SharedPreferences sp, SharedPreferences.Editor spEdit){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spOldKey = sp.getString(spMapHeadKey, "");
		Set<String> oldKeySet = new HashSet<String>();
		if(!TextUtils.isEmpty(spOldKey)){
			String[] spOldKeyArray = spOldKey.split(SP_MAP_DELIMITER);
			Collections.addAll(oldKeySet, spOldKeyArray);
		}
		
		Iterator<Entry<String, String>> entryIterator;
		Entry<String, String> entry;
		String spNewKey = null;
		if(map != null){
			spNewKey = "";
			entryIterator = map.entrySet().iterator();
			try {
				while(entryIterator.hasNext()){
					entry = entryIterator.next();
					
					if(toggleMode && sp.contains(spMapHeadKey + entry.getKey())){
						spEdit.remove(spMapHeadKey + entry.getKey());
					}else{
						spEdit.putString(spMapHeadKey + entry.getKey(), entry.getValue());
						spNewKey = getStringSymbolCombine(spNewKey, entry.getKey(), SP_MAP_DELIMITER, false);
					}
					if(oldKeySet.size() > 0){
						oldKeySet.remove(entry.getKey());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		spEdit.putString(spMapHeadKey, spNewKey);
		
		int size = oldKeySet.size();
		if(size > 0){
			Iterator<String> iterator = oldKeySet.iterator();
			for(int i=0; i<size; i++){
				spEdit.remove(spMapHeadKey + iterator.next());
			}
		}
	}
	
	public static void removeSharedPreferencesMap(Context context, String spName, final String mapSaveKey, final Handler handler){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final SharedPreferences.Editor spEdit = sp.edit();
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		if(!TextUtils.isEmpty(spKey)){
			String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
			for(int i=0; i<spKeyArray.length; i++){
				spEdit.remove(spMapHeadKey + spKeyArray[i]);
			}
		}
		spEdit.remove(spMapHeadKey);
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean isSuccess = spEdit.commit();
				if(handler != null){
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putBoolean(mapSaveKey, isSuccess);
					msg.what = 1;
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		});
		thread.start();
	}
	
	public static Map<String, String> getSharedPreferencesMap(Context context, String spName, String mapSaveKey){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		Map<String, String> map = new HashMap<String, String>();
		if(TextUtils.isEmpty(spKey)){
			return map;
		}
		
		String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
		String spValue;
		for(int i=0; i<spKeyArray.length; i++){
			if(!sp.contains(spMapHeadKey + spKeyArray[i])){
				continue;
			}
			spValue = sp.getString(spMapHeadKey + spKeyArray[i], null);
			map.put(spKeyArray[i], spValue);
		}
		return map;
	}
	
	public static String getSharedPreferencesMapInItem(Context context, String spName, String mapSaveKey, int location){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		if(spKey.length() == 0){
			return null;
		}
		
		String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
		String spValue = sp.getString(spMapHeadKey + spKeyArray[location], null);
		return spValue;
	}
	
	public static String getSharedPreferencesMapInItem(Context context, String spName, String mapSaveKey, String mapSaveItemKey){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		if(spKey.length() == 0){
			return null;
		}
		
		String spValue = sp.getString(spMapHeadKey + mapSaveItemKey, null);
		return spValue;
	}
	
	public static String[][] getMapToArray(Map<String, ?> map, boolean isReview){
		String[][] strArray = new String[map.size()][2];
		Iterator<? extends Entry<String, ?>> entryIterator = map.entrySet().iterator();
		Entry<String, ?> entry;
		for(int i=0; i<map.size(); i++){
			entry = entryIterator.next();
			strArray[i][0] = entry.getKey();
			strArray[i][1] = entry.getValue().toString();
			if(isReview){
				System.out.println("count " + i + ":" + strArray[i][0] + ":" + strArray[i][1]);
			}
		}
		return strArray;
	}
	
	public static String[][] getMapToArray(Map<String, ?> map){
		return getMapToArray(map, false);
	}
	
	public static void printListItem(List<?> list){
		int size = list.size();
		for(int i=0; i<size; i++){
			System.out.println("count " + i + ":" + list.get(i).toString());
		}
	}
	
	public static void printListItem(List<? extends Map<String, ?>> list, String...keyArray){
		int size = list.size();
		for(int i=0; i<size; i++){
			for(int j=0; j<keyArray.length; j++){
				System.out.println("count " + i + ":" + keyArray[j] + ":" + list.get(i).get(keyArray[j]).toString());
			}
		}
	}
	
	public static void printArrayItem(Object[] array){
		for(int i=0; i<array.length; i++){
			System.out.println("count " + i + ":" + array[i].toString());
		}
	}
	
	public static JSONArray newJSONArray(String data){
		try {
			return new JSONArray(data);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}
	
	public static JSONArray getJSONArrayInJA(JSONArray jsonArray, int index){
		JSONArray jsonArraySub = jsonArray.optJSONArray(index);
		if(jsonArraySub == null){
			jsonArraySub = new JSONArray();
		}
		return jsonArraySub;
	}
	
	public static JSONObject getJSONObjectInJA(JSONArray jsonArray, int index){
		JSONObject jsonObjectSub = jsonArray.optJSONObject(index);
		if(jsonObjectSub == null){
			jsonObjectSub = new JSONObject();
		}
		return jsonObjectSub;
	}
	
	public static List<String> getJSONArrayToList(JSONArray jsonArray){
		List<String> list = new ArrayList<String>();
		for(int i=0; i<jsonArray.length(); i++){
			list.add(jsonArray.optString(i));
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> reflectionJSONArrayToList(JSONArray jsonArray){
		// Reflection反射調用private屬性
		try {
			Field field = jsonArray.getClass().getDeclaredField("values");
			field.setAccessible(true);
			List<Object> list = (List<Object>)field.get(jsonArray);
			field.setAccessible(false);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject newJSONObject(String data){
		try {
			return new JSONObject(data);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}
	
	public static JSONArray getJSONArrayInJO(JSONObject jsonObject, String key){
		JSONArray jsonArraySub = jsonObject.optJSONArray(key);
		if(jsonArraySub == null){
			jsonArraySub = new JSONArray();
		}
		return jsonArraySub;
	}
	
	public static JSONObject getJSONObjectInJO(JSONObject jsonObject, String key){
		JSONObject jsonObjectSub = jsonObject.optJSONObject(key);
		if(jsonObjectSub == null){
			jsonObjectSub = new JSONObject();
		}
		return jsonObjectSub;
	}
	
	public static String[][] getJSONObjectToArray(JSONObject jsonObject){
		String key;
		JSONArray jsonArrayKey = jsonObject.names();
		String[][] element = new String[jsonArrayKey.length()][2];
		for(int i=0; i<jsonArrayKey.length(); i++){
			key = jsonArrayKey.optString(i);
			element[i][0] = key;
			element[i][1] = jsonObject.optString(key);
		}
		return element;
	}
	
	public static Map<String, String> getJSONObjectToMap(JSONObject jsonObject){
		String key;
		JSONArray jsonArrayKey = jsonObject.names();
		Map<String, String> map = new HashMap<String, String>();
		for(int i=0; i<jsonArrayKey.length(); i++){
			key = jsonArrayKey.optString(i);
			map.put(key, jsonObject.optString(key));
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> reflectionJSONObjectToMap(JSONObject jsonObject){
		// Reflection反射調用private屬性
		try {
			Field field = jsonObject.getClass().getDeclaredField("nameValuePairs");
			field.setAccessible(true);
			Map<String, Object> map = (Map<String, Object>)field.get(jsonObject);
			field.setAccessible(false);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 反射類別資料，包含ClassLoader、DeclaringClass、EnclosingClass、extends Superclass、EnumConstant、implements Interface、Field、Constructor、Method、InnerClass
	 * @param class1 Instance or Class.forName("className")
	 */
	public static void reflectionClassInfo(Class<?> class1){
		String info;
		info = class1.getName();
		Log.v("ClassName", info);

		ClassLoader classLoader = class1.getClassLoader();
		if(classLoader != null){
			info = classLoader.getClass().getName();
			Log.v("ClassLoader", info);
		}

		Class<?> classMember = class1.getDeclaringClass();
		if(classMember != null){
			info = classMember.getName();
			Log.v("MemberClass", info);
		}

		Class<?> classOuter = class1.getEnclosingClass();
		if(classOuter != null){
			info = classOuter.getName();
			Log.v("OuterClass", info);
		}

		Class<?> classSuper = class1.getSuperclass();
		if(classSuper != null){
			info = classSuper.getName();
			Log.v("extends Superclass", info);
		}

		Class<?>[] interfaces = class1.getInterfaces();
		if(interfaces.length > 0){
			Log.i("Reflection", "**** Implements Interface count:" + interfaces.length + " ****");
			for(int i=0; i<interfaces.length; i++){
				info = interfaces[i].getName();
				Log.v("Interface", info);
			}
		}

		Object[] enumConstants = class1.getEnumConstants();
		if(enumConstants != null && enumConstants.length > 0){
			Log.i("Reflection", "**** Enum Constant count:" + enumConstants.length + " ****");
			for(int i=0; i<enumConstants.length; i++){
				info = enumConstants[i].getClass().getName();
				Log.v("Enum Constant", info);
			}
		}

		Field[] fields = class1.getDeclaredFields();
		if(fields.length > 0){
			Log.i("Reflection", "**** Field count:" + fields.length + " ****");
			for(int i=0; i<fields.length; i++){
				info = fields[i].toGenericString();
				Log.v("Field", info);
			}
		}

		Constructor<?>[] constructors = class1.getDeclaredConstructors();
		if(constructors.length > 0){
			Log.i("Reflection", "**** Constructor count:" + constructors.length + " ****");
			for(int i=0; i<constructors.length; i++){
				info = constructors[i].toGenericString();
				Log.v("Constructor", info);
			}
		}

		Method[] methods = class1.getDeclaredMethods();
		if(methods.length > 0){
			Log.i("Reflection", "**** Method count:" + methods.length + " ****");
			for(int i=0; i<methods.length; i++){
				info = methods[i].toGenericString();
				Log.v("Method", info);
			}
		}

		Class<?>[] classes = class1.getDeclaredClasses();
		if(classes.length > 0){
			Log.i("Reflection", "**** InnerClass count:" + classes.length + " ****");
			for(int i=0; i<classes.length; i++){
				info = classes[i].getName();
				Log.v("InnerClass", info);
			}
		}
	}
	
	/**
	 * MIME Type<br>
	 * "text/plain; charset=utf-8"<br>
	 * "text/*"<br>
	 * "image/jpeg"<br>
	 * "image/*"<br>
	 * "audio/x-mpeg"<br>
	 * "audio/*"<br>
	 * "video/mp4"<br>
	 * "video/*"<br>
	 * "application/zip"<br>
	 * "application/octet-stream"<br>
	 * "multipart/mixed"<br>
	 * "multipart/related"<br>
	 * "multipart/alternative"<br>
	 * "message/rfc822"
	 * 
	 * @param streamUri Uri.fromFile(file)
	 */
	public static Intent getActionSendIntent(String intentType, String subject, String text, Uri streamUri){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if(!TextUtils.isEmpty(intentType)){
			intent.setType(intentType);
		}
		if(!TextUtils.isEmpty(subject)){
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if(!TextUtils.isEmpty(text)){
			intent.putExtra(Intent.EXTRA_TEXT, text);
		}
		if(streamUri != null && !TextUtils.isEmpty(streamUri.getPath())){
			intent.putExtra(Intent.EXTRA_STREAM, streamUri);
		}
		return intent;
	}
	
	public static Intent getActionSendIntentForAPP(Context context, String packageName, String className, String intentType, String subject
			, String text, Uri streamUri){
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(listResolveInfo != null && listResolveInfo.size() > 0){
			intent = new Intent(Intent.ACTION_SEND);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.setType(intentType);
//			intent.setClassName("com.facebook.katana", "com.facebook.katana.ShareLinkActivity");
//			intent.setClassName("com.twitter.android", "com.twitter.android.PostActivity");
			intent.setClassName(packageName, className);
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT, text);
			intent.putExtra(Intent.EXTRA_STREAM, streamUri);
		}else{
			Utils.setToast(context, packageName + " Not installed");
			Uri uriMarket = Uri.parse("market://details?id=" + packageName);
			intent = new Intent(Intent.ACTION_VIEW, uriMarket);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		}
		return intent;
	}
	
	public static Intent getActionSendtoIntentForEmail(String mailToUri, String subject, String text){
		if(!TextUtils.isEmpty(mailToUri)){
			return null;
		}
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse(mailToUri));
		if(!TextUtils.isEmpty(subject)){
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if(!TextUtils.isEmpty(text)){
			intent.putExtra(Intent.EXTRA_TEXT, text);
		}
		return intent;
	}
	
	public static Intent getActionSendIntentForEmail(String intentType, String[] email, String[] cc, String[] bcc, String subject, String text
			, Uri streamUri){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if(!TextUtils.isEmpty(intentType)){
			intent.setType(intentType);
		}
		if(email != null && email.length > 0){
			intent.putExtra(Intent.EXTRA_EMAIL, email);
		}
		if(cc != null && cc.length > 0){
			intent.putExtra(Intent.EXTRA_CC, cc);
		}
		if(bcc != null && bcc.length > 0){
			intent.putExtra(Intent.EXTRA_BCC, bcc);
		}
		if(!TextUtils.isEmpty(subject)){
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if(!TextUtils.isEmpty(text)){
			intent.putExtra(Intent.EXTRA_TEXT, text);
		}
		if(streamUri != null && !TextUtils.isEmpty(streamUri.getPath())){
			intent.putExtra(Intent.EXTRA_STREAM, streamUri);
		}
		return intent;
	}
	
	public static Intent getActionSendMultipleIntentForEmail(String intentType, String[] email, String[] cc, String[] bcc, String subject, String text
			, ArrayList<Uri> streamUriList){
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if(!TextUtils.isEmpty(intentType)){
			intent.setType(intentType);
		}
		if(email != null && email.length > 0){
			intent.putExtra(Intent.EXTRA_EMAIL, email);
		}
		if(cc != null && cc.length > 0){
			intent.putExtra(Intent.EXTRA_CC, cc);
		}
		if(bcc != null && bcc.length > 0){
			intent.putExtra(Intent.EXTRA_BCC, bcc);
		}
		if(!TextUtils.isEmpty(subject)){
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if(!TextUtils.isEmpty(text)){
			intent.putExtra(Intent.EXTRA_TEXT, text);
		}
		if(streamUriList != null && streamUriList.size() > 0){
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, streamUriList);
		}
		return intent;
	}
	
	public static Intent getLauncherIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return intent;
	}

	public static Intent getLauncherIntent(Context context, Class<?> targetClass){
		return getLauncherIntent(context.getPackageName(), targetClass.getName());
	}
	
	public static Intent getBackTaskIntent(Context context, Class<? extends Activity> targetClass){
		Intent intent = new Intent(context, targetClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(Context context, Class<? extends Activity> targetCalss){
		Intent intent = new Intent(context, targetCalss);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}
	
	public static Intent getContentSelectionIntent(String intentType, boolean allowMultiple){
		Intent intent = new Intent();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		}else{
			intent.setAction(Intent.ACTION_GET_CONTENT);
		}
		intent.setType(intentType);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
		}
		return intent;
	}
	
	public static void callContentSelection(final Activity activity, String intentType, boolean allowMultiple, final int requestCode
			, final String title, String failInfo){
		final Intent intent = getContentSelectionIntent(intentType, allowMultiple);
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		if(listResolveInfo != null && listResolveInfo.size() > 0){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					if(title == null){
						activity.startActivityForResult(intent, requestCode);
					}else{
						activity.startActivityForResult(Intent.createChooser(intent, title), requestCode);
					}
				}
			}).start();
		}else{
			Utils.setToast(activity, failInfo);
		}
	}
	
	public static void callContentSelection(Activity activity, String intentType, boolean allowMultiple, int requestCode){
		callContentSelection(activity, intentType, allowMultiple, requestCode, null, "No application");
	}
	
	public static void callImageCrop(final Activity activity, Uri uriSrc, Uri uriDst, int aspectX, int aspectY, int outputX, int outputY
			, String outputFormat, boolean circleCrop, boolean noFaceDetection, boolean returnData, final int requestCode, String failInfo){
		final Intent intent = new Intent("com.android.camera.action.CROP");
		intent.putExtra("crop", "true");
		// 設置剪裁圖片來源
		intent.setDataAndType(uriSrc, "image/*");
		// 設置剪裁圖片輸出路徑
		if(!returnData && uriDst != null){
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uriDst);
		}
		
		// 設置裁剪框的的寬高比
		if(aspectX > 0){
			intent.putExtra("aspectX", aspectX);
		}
		if(aspectY > 0){
			intent.putExtra("aspectY", aspectY);
		}
		
		// 固定裁剪後圖片的寬高值
		if(outputX > 0){
			intent.putExtra("outputX", outputX);
		}
		if(outputY > 0){
			intent.putExtra("outputY", outputY);
		}
		
		// 設置剪裁後圖片格式 Bitmap.CompressFormat
		intent.putExtra("outputFormat", outputFormat);
		// 圓形裁剪框
		intent.putExtra("circleCrop", circleCrop);
		// 取消人臉識別
		intent.putExtra("noFaceDetection", noFaceDetection);
		// 是否直接返回圖片
		intent.putExtra("return-data", returnData);
		
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		if(listResolveInfo != null && listResolveInfo.size() > 0){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					activity.startActivityForResult(intent, requestCode);
				}
			}).start();
		}else{
			Utils.setToast(activity, failInfo);
		}
	}
	
	public static void callImageCrop(Activity activity, Uri uriSrc, Uri uriDst, int aspectX, int aspectY, int outputX, int outputY
			, String outputFormat, boolean circleCrop, boolean noFaceDetection, boolean returnData, int requestCode){
		callImageCrop(activity, uriSrc, uriDst, aspectX, aspectY, outputX, outputY, outputFormat, circleCrop, noFaceDetection, returnData, requestCode
				, "No application");
	}
	
	public static void callImageCrop(Activity activity, Uri uriSrc, Uri uriDst, String outputFormat, boolean circleCrop, boolean noFaceDetection
			, boolean returnData, int requestCode){
		callImageCrop(activity, uriSrc, uriDst, 0, 0, 0, 0, outputFormat, circleCrop, noFaceDetection, returnData, requestCode);
	}
	
	public static void callImageCrop(Activity activity, Uri uriSrc, Uri uriDst, boolean circleCrop, boolean noFaceDetection
			, boolean returnData, int requestCode){
		callImageCrop(activity, uriSrc, uriDst, 0, 0, 0, 0, Bitmap.CompressFormat.PNG.toString(), circleCrop, noFaceDetection, returnData, requestCode);
	}
	
	public static void callImageCrop(Activity activity, Uri uriSrc, Uri uriDst, String outputFormat, boolean returnData, int requestCode){
		callImageCrop(activity, uriSrc, uriDst, 0, 0, 0, 0, outputFormat, false, false, returnData, requestCode);
	}
	
	public static void callImageCrop(Activity activity, Uri uriSrc, Uri uriDst, boolean returnData, int requestCode){
		callImageCrop(activity, uriSrc, uriDst, 0, 0, 0, 0, Bitmap.CompressFormat.PNG.toString(), false, false, returnData, requestCode);
	}
	
	public static void callGoogleMapsNavigation(Context context, String sLatit, String sLongit, String dLatit, String dLongit){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://maps.google.com/maps?f=d&saddr=" + sLatit + "," + sLongit +
				"&daddr=" + dLatit + "," + dLongit + "&hl=zh-TW"));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		context.startActivity(intent);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void callAppSettings(Context context){
		Uri packageUri = Uri.fromParts("package", context.getPackageName(), null);
		/*
		 * Mobile網路設定頁
		 * Settings.ACTION_WIRELESS_SETTINGS
		 * WIFI網路設定頁
		 * Settings.ACTION_WIFI_SETTINGS
		 */
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
		context.startActivity(intent);
	}
	
	public static String queryFilePathFromUri(Context context, Uri uri){
		String path = null;
		Cursor cursor = null;
		try {
			String[] projection = new String[]{MediaStore.MediaColumns.DATA};
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
				path = cursor.getString(columnIndex);
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(cursor != null){
				cursor.close();
			}
		}
		return path;
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String[] getFilesPathFromIntentUri(Context context, Intent intent){
		Uri[] uris = getIntentUris(intent);
		String[] paths = new String[uris.length];
		for(int i=0; i<uris.length; i++){
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
				if(uris[i].getScheme().equals("content")){
					paths[i] = queryFilePathFromUri(context, uris[i]);
				}else{
					paths[i] = uris[i].getPath();
				}
				continue;
			}
			
			if(!DocumentsContract.isDocumentUri(context, uris[i])){
				paths[i] = queryFilePathFromUri(context, uris[i]);
				continue;
			}
			
			String authority = uris[i].getAuthority();
			String docId = DocumentsContract.getDocumentId(uris[i]);
			if(authority.equals("com.android.providers.downloads.documents")){
				uris[i] = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
				paths[i] = queryFilePathFromUri(context, uris[i]);
				continue;
			}
			
			String[] divide = docId.split(":");
			String type = divide[0];
			if(authority.equals("com.android.externalstorage.documents")){
				if(type.equals("primary")){
					paths[i] = Environment.getExternalStorageDirectory() + File.separator + divide[1];
				}
			}else if(authority.equals("com.android.providers.media.documents")){
				if(type.equals("image")){
					uris[i] = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
					paths[i] = queryFilePathFromUri(context, uris[i]);
				}else if(type.equals("audio")){
					uris[i] = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
					paths[i] = queryFilePathFromUri(context, uris[i]);
				}else if(type.equals("video")){
					uris[i] = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
					paths[i] = queryFilePathFromUri(context, uris[i]);
				}
			}
		}
		return paths;
	}
	
	@SuppressWarnings("ResourceType")
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static Uri[] getIntentUrisWithPath(Context context, Intent intent){
		Uri[] uris = getIntentUris(intent);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
			return uris;
		}
		
		for(int i=0; i<uris.length; i++){
			if(DocumentsContract.isDocumentUri(context, uris[i])){
				int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				context.getContentResolver().takePersistableUriPermission(uris[i], takeFlags);
			}
		}
		return uris;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static Uri[] getIntentUris(Intent intent){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
			return new Uri[]{intent.getData()};
		}
		ClipData clipData = intent.getClipData();
		if(clipData == null || clipData.getItemCount() == 0){
			return new Uri[]{intent.getData()};
		}
		Uri[] uris = new Uri[clipData.getItemCount()];
		for(int i=0; i<uris.length; i++){
			uris[i] = clipData.getItemAt(i).getUri();
		}
		return uris;
	}
	
	// 取得螢幕亮度模式
	/**
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1　為自動調節螢幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0　為手動調節螢幕亮度
	 */
	public static int getScreenBrightnessMode(Context context){
		int screenBrightnessMode = 0;
		try {
			screenBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return screenBrightnessMode;
	}
	
	// 取得系統亮度
	public static int getScreenBrightnessForSystem(Context context){
		int screenBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
		return screenBrightness;
	}
	
	// 取得目前亮度
	public static int getScreenBrightnessForActivity(Activity activity){
		WindowManager.LayoutParams windowManagerLayoutParams = activity.getWindow().getAttributes();
		if(windowManagerLayoutParams.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE){
			return getScreenBrightnessForSystem(activity);
		}
		return (int)(windowManagerLayoutParams.screenBrightness * 255.0f);
	}
	
	// 設定螢幕亮度模式
	/**
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1　為自動調節螢幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0　為手動調節螢幕亮度
	 */
	public static void setScreenBrightnessMode(Context context, int screenBrightnessMode){
		Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessMode);
	}
	
	// 設定系統亮度
	/**
	 * brightness = 0 - 255
	 */
	public static void setScreenBrightnessForSystem(Context context, int screenBrightness){
		Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightness);
	}
	
	// 設定目前亮度
	/**
	 * brightness = 0 - 255
	 */
	public static void setScreenBrightnessForActivity(Activity activity, int screenBrightness){
		WindowManager.LayoutParams windowManagerLayoutParams = activity.getWindow().getAttributes();
		windowManagerLayoutParams.screenBrightness = screenBrightness / 255.0f;
		activity.getWindow().setAttributes(windowManagerLayoutParams);
	}
	
	// 控制鍵盤開關
	public static void softInputSwitch(Context context, View view, boolean isShow){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(view != null){
			if(isShow){
				imm.showSoftInput(view, 0);
			}else{
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}
	
	public static void softInputSwitch(Window window, boolean isShow){
		if(window != null){
			if(isShow){
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}else{
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}
	}
	
	// 自動切換鍵盤開關
	public static void softInputToggle(Context context, View view){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(view != null){
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	// 取得Android機器ID
	public static String getAndroidID(Context context){
		// Android API 2.2 當時的部份設備有bug，會產生相同的ANDROID_ID:9774d56d682e549c
		String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		// 硬體的唯一值 API 9
//		String buildSerial = android.os.Build.SERIAL;
		// 製造商
//		String manufacturer = android.os.Build.MANUFACTURER;
		// 機器名稱
//		String model = android.os.Build.MODEL;
		// 機器型號
//		String device = android.os.Build.DEVICE;
		/*
		 * UUID(Universally Unique Identifier)全局唯一識別字，是指在一台機器上生成的數字，它保證對在同一時空中的所有機器都是唯一的。
		 * 按照開放軟體基金會(OSF)制定的標準計算，用到了乙太網卡位址、 納秒級時間、晶片ID碼和許多可能的數位。
		 * 由以下幾部分的組合：當前日期和時間（UUID的第一個部分與時間有關，如果你在生成一個UUID之後，過幾秒又生成一個UUID，則第一個部分不同，其餘相同）
		 * ，時鐘序列，全局唯一的IEEE機器識別號（如果有網卡，從網卡獲得，沒有網卡以其他方式獲得），UUID的唯一缺陷在於生成的結果字串會比較長。
		 */
//		String uuid = UUID.randomUUID().toString();
		return androidID;
	}
	
	// 取得手機Wifi網路卡的MAC值
	public static String getWifiMAC(Context context){
		// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
		if(context.checkCallingPermission(android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
			return null;
		}
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getMacAddress();
	}
	
	// 判斷此Activity是否正在前端執行
	@SuppressWarnings("deprecation")
	public static boolean isRunningTopActivity(Context context){
		// <uses-permission android:name="android.permission.GET_TASKS"/>
		if(context.checkCallingPermission(android.Manifest.permission.GET_TASKS) == PackageManager.PERMISSION_DENIED){
			return false;
		}
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		String runningClassName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		return context.getClass().getName().equals(runningClassName);
	}
	
	public static boolean isRunningApp(Context context, String packageName){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(packageName.equals(runningAppProcessInfo.processName)){
				return true;
			}
			for(String packageNameItem : runningAppProcessInfo.pkgList){
				if(packageName.equals(packageNameItem)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isRunningAppProcess(Context context, int processId){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(processId == runningAppProcessInfo.pid){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isRunningAppProcess(Context context, String processName){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(processName.equals(runningAppProcessInfo.processName)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isRunningService(Context context, Class<?> serviceClass){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceClass.getName().equals(runningServiceInfo.service.getClassName())){
				return true;
			}
		}
		return false;
	}
	
	// 列印所有執行中的AppProcess系統資訊
	public static void logRunningAppProcessInfo(Context context){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
		int size = runningAppProcessInfoList.size();
		int[] pids = new int[size];
		Log.v("RunningAppProcess", "Count:" + size);
		
		RunningAppProcessInfo runningAppProcessInfo;
		Debug.MemoryInfo[] debugMemoryInfoArray;
		for(int i=0; i<size; i++){
			runningAppProcessInfo = runningAppProcessInfoList.get(i);
			pids[i] = runningAppProcessInfo.pid;
		}
		for(int i=0; i<size; i++){
			runningAppProcessInfo = runningAppProcessInfoList.get(i);
			debugMemoryInfoArray = activityManager.getProcessMemoryInfo(pids);
			Log.v("RunningAppProcess", "processName:" + runningAppProcessInfo.processName +
					" pid:" + runningAppProcessInfo.pid + " uid:" + runningAppProcessInfo.uid);
			logDebugMemoryInfo(debugMemoryInfoArray[i]);
		}
	}
	
	// 列印系統記憶體資訊
	@SuppressLint("NewApi")
	public static void logActivityManagerMemoryInfo(Context context){
		// ActivityManager.MemoryInfo：系统可用記憶體資訊
		// ActivityManager.RecentTaskInfo：最近的任務資訊
		// ActivityManager.RunningAppProcessInfo：正在執行的程式資訊
		// ActivityManager.RunningServiceInfo：正在執行的服務資訊
		// ActivityManager.RunningTaskInfo：正在執行的任務資訊
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo activityMemoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(activityMemoryInfo);
		
		// Log.v()，v代表verbose，在Logcat中輸出顏色是黑色，沒有代表性意義，通常用來紀錄詳細一點的訊息
		// Log.d()，d代表debug，在Logcat中輸出顏色是藍色，代表程式debug的訊息
		// Log.i()，i代表information，在Logcat中輸出顏色是綠色，代表提示性的訊息
		// Log.w()，w代表warning，在Logcat中輸出顏色是橘色，代表警告的訊息
		// Log.e()，e代表error，在Logcat中輸出顏色是紅色，代表錯誤的訊息
		Log.v("ActivityManager", "AppProcess的記憶體限制：" + activityManager.getMemoryClass() + "MB");
		Log.v("ActivityManager", "AppLargeHeapProcess的記憶體限制：" + activityManager.getLargeMemoryClass() + "MB");
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統剩餘記憶體：" + (activityMemoryInfo.availMem >> 10) + "KB");
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統目前是否執行低記憶體模式：" + activityMemoryInfo.lowMemory);
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統記憶體低於" + (activityMemoryInfo.threshold >> 10) + "KB時執行低記憶體模式");
		if(Build.VERSION.SDK_INT >= 16){
			Log.v("ActivityManager", "MemoryInfo系統總記憶體" + (activityMemoryInfo.totalMem >> 10) + "KB");
		}
	}
	
	/**
	 *  列印此App單一Process記憶體資訊<br>
	 *  NavtiveHeap: C & C++ heap space<br>
	 *  DalvikVMHeap: Java heap space
	 */
	public static void logProcessMemoryHeap(){
		// Java heap space
		Runtime runtime = Runtime.getRuntime();
		Log.v("Runtime", "ProcessDalvikVMHeap已分配的記憶體量：" + (runtime.totalMemory() >> 10) + "KB");
		Log.v("Runtime", "ProcessDalvikVMHeap已分配未使用的記憶體量：" + (runtime.freeMemory() >> 10) + "KB");
		Log.v("Runtime", "ProcessDalvikVMHeap最大可分配的記憶體量：" + (runtime.maxMemory() >> 10) + "KB");
		// C & C++ heap space
		Log.v("Debug", "ProcessNavtiveHeap已分配的記憶體量：" + (Debug.getNativeHeapAllocatedSize() >> 10) + "KB");
		Log.v("Debug", "ProcessNavtiveHeap已分配未使用的記憶體量：" + (Debug.getNativeHeapFreeSize() >> 10) + "KB");
		Log.v("Debug", "ProcessNavtiveHeap目前的記憶體量：" + (Debug.getNativeHeapSize() >> 10) + "KB");
	}
	
	// 列印此App單一Process記憶體詳細資訊
	public static void logDebugMemoryInfo(Debug.MemoryInfo debugMemoryInfo){
		if(debugMemoryInfo == null){
			debugMemoryInfo = new Debug.MemoryInfo();
			Debug.getMemoryInfo(debugMemoryInfo);
		}
		// Java heap space
		Log.v("Debug.MemoryInfo", "dalvikPrivateDirty:" + debugMemoryInfo.dalvikPrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "dalvikPss:" + debugMemoryInfo.dalvikPss + "KB");
		Log.v("Debug.MemoryInfo", "dalvikSharedDirty:" + debugMemoryInfo.dalvikSharedDirty + "KB");
		// C & C++ heap space
		Log.v("Debug.MemoryInfo", "nativePrivateDirty:" + debugMemoryInfo.nativePrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "nativePss:" + debugMemoryInfo.nativePss + "KB");
		Log.v("Debug.MemoryInfo", "nativeSharedDirty:" + debugMemoryInfo.nativeSharedDirty + "KB");
		
		Log.v("Debug.MemoryInfo", "otherPrivateDirty:" + debugMemoryInfo.otherPrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "otherPss:" + debugMemoryInfo.otherPss + "KB");
		Log.v("Debug.MemoryInfo", "otherSharedDirty:" + debugMemoryInfo.otherSharedDirty + "KB");
	}
	
	public static void logDebugMemoryInfo(){
		logDebugMemoryInfo(null);
	}
	
	// 取得軟體資訊
	public static PackageInfo getPackageInfo(Context context, String packageName, int flags){
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, flags);
			return packageInfo;
		} catch (NameNotFoundException ignored) {}
		return null;
	}
	
	public static PackageInfo getPackageInfo(Context context, int flags){
		return getPackageInfo(context, context.getPackageName(), flags);
	}
	
	public static int getVersionCode(Context context){
		PackageInfo packageInfo = getPackageInfo(context, 0);
		if(packageInfo != null){
			return packageInfo.versionCode;
		}
		return -1;
	}
	
	public static String getVersionName(Context context){
		PackageInfo packageInfo = getPackageInfo(context, 0);
		if(packageInfo != null){
			return packageInfo.versionName;
		}
		return null;
	}
	
	// 取得金鑰簽名
	public static Signature getKeystoreSignature(Context context, String packageName){
		PackageInfo packageInfo = getPackageInfo(context, packageName, PackageManager.GET_SIGNATURES);
		if(packageInfo != null){
			for(Signature signature : packageInfo.signatures){
				if(signature != null){
					return signature;
				}
			}
		}
		return null;
	}
	
	// 取得已安裝的軟體資訊
	public static List<PackageInfo> getInstallPackageInfo(Context context, boolean isContainSystemDefaultInstall){
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
		if(!isContainSystemDefaultInstall){
			List<PackageInfo> linkedList = new LinkedList<PackageInfo>(packageInfoList);
			for(int i=0; i<packageInfoList.size(); i++){
				if ((packageInfoList.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0){
					// System Default Install
					linkedList.remove(i);
				}
			}
			packageInfoList = new ArrayList<PackageInfo>(linkedList);
		}
		return packageInfoList;
	}
	
	// 寫入聯絡人資訊
	public static boolean saveContentProvider(Context context, String[] infoArray){
		// <uses-permission android:name="android.permission.WRITE_CONTACTS"/>
		// 某些機種需要讀取權限
		// <uses-permission android:name="android.permission.READ_CONTACTS"/>
		if(context.checkCallingPermission(android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED){
			return false;
		}
		try {
			String[] contentArray = new String[8];
			for(int i=0; i<contentArray.length; i++){
				if(infoArray.length > i && infoArray[i] != null){
					contentArray[i] = infoArray[i];
				}else{
					contentArray[i] = "";
				}
			}
			ArrayList<ContentProviderOperation> contentList = new ArrayList<ContentProviderOperation>();
			int rawContactInsertIndex = contentList.size();
			contentList.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME, null)
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
					.withValue(StructuredName.DISPLAY_NAME, contentArray[0])
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
					.withValue(Organization.COMPANY, contentArray[1])
					.withValue(Organization.TITLE, contentArray[2])
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
					.withValue(Email.DATA, contentArray[3])
					.withValue(Email.TYPE, Email.TYPE_WORK)
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, contentArray[4])
					.withValue(Phone.TYPE, Phone.TYPE_WORK)
					.withValue(Phone.LABEL, "Company Tel")
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, contentArray[5])
					.withValue(Phone.TYPE, Phone.TYPE_FAX_WORK)
					.withValue(Phone.LABEL, "Company Fax")
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, contentArray[6])
					.withValue(Phone.TYPE, Phone.TYPE_MOBILE)
					.withValue(Phone.LABEL, "Phone")
					.build());
			
			contentList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, contentArray[7])
					.withValue(Phone.TYPE, Phone.TYPE_HOME)
					.withValue(Phone.LABEL, "Home Tel")
					.build());
			
			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentList);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// 取得手機資訊
	public static List<Map<String, String>> getPhoneInfo(Context context){
		// <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
		if(context.checkCallingPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
			return null;
		}

		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		
		List<Map<String, String>> infoList = new ArrayList<Map<String,String>>();
		Map<String, String> hashMap = new HashMap<String, String>();
		
		// MSISDN(Mobile Subscriber ISDN number) 手機號碼(行動設備PSTN/ISDN號碼)
		// MSISDN for GSM
		String MSISDN = telephonyManager.getLine1Number();
		hashMap.put("MSISDN", MSISDN);
		infoList.add(hashMap);
		
		// IMEI(International Mobile Equipment Identity number) or MEID or ESN 手機序號(國際移動設備辨識碼)
		// IMEI for GSM, MEID or ESN for CDMA
		String IMEI = telephonyManager.getDeviceId();
		hashMap.put("IMEI", IMEI);
		infoList.add(hashMap);
		
		// IMSI(International Mobile Subscriber Identity) SIM卡號碼(國際行動用戶辨識碼)
		// IMSI for GSM
		String IMSI = telephonyManager.getSubscriberId();
		hashMap.put("IMSI", IMSI);
		infoList.add(hashMap);
		
		// ICCID(Integrate Circuit Card Identity) SIM卡序號(積體電路卡辨識碼)
		String ICCID = telephonyManager.getSimSerialNumber();
		hashMap.put("ICCID", ICCID);
		infoList.add(hashMap);
		
		// 手機漫遊狀態
		String roamingStatus = telephonyManager.isNetworkRoaming() ? "漫遊中" : "非漫遊";
		hashMap.put("roamingStatus", roamingStatus);
		infoList.add(hashMap);
		
		// 電信網路國別
		String networkCountry = telephonyManager.getNetworkCountryIso();
		hashMap.put("networkCountryISO", networkCountry);
		infoList.add(hashMap);
		
		// 電信公司代號
		String networkOperator = telephonyManager.getNetworkOperator();
		hashMap.put("networkOperator", networkOperator);
		infoList.add(hashMap);
		
		// 電信公司名稱
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		hashMap.put("networkOperatorName", networkOperatorName);
		infoList.add(hashMap);
		
		// SIM卡狀態資訊
		int simState = telephonyManager.getSimState();
		hashMap.put("simState", "" + simState);
		infoList.add(hashMap);
		
		if(simState == TelephonyManager.SIM_STATE_READY){
			// SIM卡國別
			String simCountry = telephonyManager.getSimCountryIso();
			hashMap.put("simCountryISO", simCountry);
			infoList.add(hashMap);
			
			// SIM卡供應商代號
			String simOperator = telephonyManager.getSimOperator();
			hashMap.put("simOperator", simOperator);
			infoList.add(hashMap);
			
			// SIM卡供應商名稱
			String simOperatorName = telephonyManager.getSimOperatorName();
			hashMap.put("simOperatorName", simOperatorName);
			infoList.add(hashMap);
		}
		
		// 行動網路類型
		// Reflection反射調用hide方法
		try {
			Method method = telephonyManager.getClass().getDeclaredMethod("getNetworkTypeName", int.class);
			method.setAccessible(true);
			String networkTypeName = (String)method.invoke(telephonyManager, telephonyManager.getNetworkType());
			method.setAccessible(false);
			hashMap.put("networkTypeName", networkTypeName);
			infoList.add(hashMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 行動通訊類型
		String[] phoneTypeArray = {"NONE", "GSM", "CDMA", "SIP"};
		String phoneType = phoneTypeArray[telephonyManager.getPhoneType()];
		hashMap.put("phoneType", phoneType);
		infoList.add(hashMap);
		
		return infoList;
	}
	
	public static boolean executeCommand(boolean isPrint, String...cmdArray){
		Process process = null;
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;
		boolean isExecuteFinished = false;
		try {
			try {
				process = Runtime.getRuntime().exec("/system/bin/su");
			} catch (Exception ignored) {}
			try {
				process = Runtime.getRuntime().exec("/system/xbin/su");
			} catch (Exception ignored) {}
//			process = Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","reboot now"});
			
			if(process != null){
				dataInputStream = new DataInputStream(process.getInputStream());
				if(isPrint){
					System.out.println("Get superUser permission command result\n" + inputStreamToString(dataInputStream, null));
				}
			}
			
			if(cmdArray != null && cmdArray.length > 0){
				int offset = 0;
				if(process == null){
					process = Runtime.getRuntime().exec(cmdArray[0]);
					offset = 1;
				}
				dataOutputStream = new DataOutputStream(process.getOutputStream());
				for(int i=0; i<cmdArray.length - offset; i++){
					dataOutputStream.writeBytes(cmdArray[i + offset] + "\n");
				}
				dataOutputStream.flush();
				
				process.waitFor();
				isExecuteFinished = true;
				
				dataInputStream = new DataInputStream(process.getInputStream());
				if(isPrint){
					System.out.println("Execute command result\n" + inputStreamToString(dataInputStream, null));
				}
			}
		} catch (Exception e) {
			if(isPrint){
				e.printStackTrace();
			}
		} finally {
			try {
				if(dataInputStream != null){
					dataInputStream.close();
				}
				if(dataOutputStream != null){
					dataOutputStream.close();
				}
				if(process != null){
					process.destroy();
				}
			} catch (Exception e) {
				if(isPrint){
					e.printStackTrace();
				}
			}
		}
		return isExecuteFinished;
	}
	
	public static boolean isRooted(Context context){
		/*
		 * Release-Keys 正式開發者簽名
		 * Test-Keys 第三方開發者自訂簽名
		 * 一般來說Release-Keys會比Test-Keys更安全，但不一定總是如此
		 */
		String buildTags = android.os.Build.TAGS;
		if(buildTags != null && buildTags.contains("test-keys")){
			return true;
		}
		
		// 檢查是否安裝su權限管理程式Superuser，不一定會安裝在/system/app/
		File file = new File("/system/app/Superuser.apk");
		if(file.isFile()){
			return true;
		}
		
		// 檢查是否安裝su權限管理程式Superuser，未安裝不一定代表沒有root
		if(getPackageInfo(context, "com.noshufou.android.su", PackageManager.GET_ACTIVITIES) != null){
			return true;
		}
		
		// 檢查是否安裝su權限管理程式SuperSU，不一定會安裝在/system/app/
		file = new File("/system/app/SuperSU.apk");
		if(file.isFile()){
			return true;
		}
		
		// 檢查是否安裝su權限管理程式SuperSU，未安裝不一定代表沒有root
		if(getPackageInfo(context, "eu.chainfire.supersu", PackageManager.GET_ACTIVITIES) != null){
			return true;
		}
		
		// 檢查各路徑是否含有su檔
		String[] pathArray = new String[]{"/system/bin/", "/system/xbin/", "/system/bin/failsafe/"
				, "/system/sd/xbin/", "/sbin/", "/data/local/"
				, "/data/local/bin/", "/data/local/xbin/"};
		for(String path : pathArray){
			file = new File(path + "su");
			if(file.isFile()){
				return true;
			}
		}
		return false;
	}
	
	// 計算兩點距離API版
	public static double getDistance1(double lat1, double lon1, double lat2, double lon2) {
		float[] results=new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
	
	// 計算兩點距離簡算版
	public static double getDistance2(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
					* Math.cos(Math.toRadians(theta));
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		double miles = dist * 60 * 1.1515;
		return miles;
	}
	
	public static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public static float rotate(MotionEvent event){
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		double radians = Math.atan2(y, x);
		return (float) Math.toDegrees(radians);
	}
	
	public static void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	public static void getViewGroupAllView(View view, List<View> list){
		list.add(view);
		if(view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			View viewChild;
			for(int i=0; i<viewGroup.getChildCount(); i++){
				viewChild = viewGroup.getChildAt(i);
				if(viewChild instanceof ViewGroup){
					getViewGroupAllView(viewChild, list);
				}else{
					list.add(viewChild);
				}
			}
		}
	}
	
	public static List<View> getViewGroupAllView(View view){
		List<View> list = new ArrayList<View>();
		getViewGroupAllView(view, list);
		return list;
	}
	
	public static List<View> findViewByClass(View view, Class<?> targetClass, boolean isPrintClassName){
		List<View> list = new ArrayList<View>();
		List<View> listMatch = new ArrayList<View>();
		getViewGroupAllView(view, list);
		int size = list.size();
		for(int i=0; i<size; i++){
			if(isPrintClassName){
				System.out.println(i + " " + list.get(i).getClass().getName());
			}
			if(targetClass.isInstance(list.get(i))){
				listMatch.add(list.get(i));
			}
		}
		list.clear();
		return listMatch;
	}
	
	public static List<View> findViewByClass(View view, Class<?> targetClass){
		return findViewByClass(view, targetClass, false);
	}
	
	public static void clearViewGroup(ViewGroup viewGroup, boolean isIndicatesGC){
		List<View> list = new ArrayList<View>();
		getViewGroupAllView(viewGroup, list);
		View view;
		int size = list.size();
		for(int i=0; i<size; i++){
			view = list.get(i);
			if(view != null){
				clearViewInsideDrawable(view, true, true, false);
				view.clearFocus();
			}
		}
		list.clear();
		if(isIndicatesGC){
			System.gc();
		}
	}
	
	public static void activityFinishClear(Activity activity, boolean isIndicatesGC){
		clearViewGroup((ViewGroup)activity.getWindow().getDecorView(), isIndicatesGC);
	}
	
	@SuppressWarnings("deprecation")
	public static void clearViewInsideDrawable(View view, boolean foreground, boolean background, boolean isIndicatesGC){
		/*
		Known Direct Subclasses
		AnimatedVectorDrawable, BitmapDrawable, ClipDrawable, ColorDrawable, DrawableContainer, GradientDrawable, InsetDrawable, LayerDrawable
		, NinePatchDrawable, PictureDrawable, RotateDrawable, RoundedBitmapDrawable, ScaleDrawable, ShapeDrawable, VectorDrawable
		Known Indirect Subclasses
		AnimatedStateListDrawable, AnimationDrawable, LevelListDrawable, PaintDrawable, RippleDrawable, StateListDrawable, TransitionDrawable
		*/
		if(foreground){
			if(view instanceof ImageView){
				clearDrawable(((ImageView)view).getDrawable());
				((ImageView)view).setImageDrawable(null);
			}else if(view instanceof TextView){
				Drawable[] drawables = ((TextView)view).getCompoundDrawables();
				((TextView)view).setCompoundDrawables(null, null, null, null);
				for(int i=0; i<drawables.length; i++){
					clearDrawable(drawables[i]);
				}
			}
		}
		
		if(background){
			clearDrawable(view.getBackground());
			view.setBackgroundDrawable(null);
		}
		
		if(isIndicatesGC){
			System.gc();
		}
	}
	
	public static void clearDrawable(Drawable drawable){
		BitmapDrawable bd;
		if(drawable != null){
			if(drawable instanceof BitmapDrawable){
				bd = (BitmapDrawable)drawable;
				bd.setCallback(null);
				recycleBitmap(bd.getBitmap(), false);
			}else{
				drawable.setCallback(null);
			}
		}
	}
	
	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	public static void recycleBitmap(Bitmap bitmap, boolean isIndicatesGC){
		if(bitmap != null){
			bitmap.recycle();
			if(isIndicatesGC){
				System.gc();
			}
		}
	}
}