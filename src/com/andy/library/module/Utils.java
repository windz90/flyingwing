package com.andy.library.module;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.os.RemoteException;
import android.os.Handler.Callback;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.2.11
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class Utils {
	
	public static final int LIMIT_DIP_WIDTH = 600;
	public static final int LIMIT_DIP_WIDTH_480 = 480;
	public static final int SIZE_SUBJECT_S = 0;
	public static final int SIZE_TAB_S = 1;
	public static final int SIZE_TITLE_S = 2;
	public static final int SIZE_BULLET_S = 3;
	public static final int SIZE_TEXT_S = 4;
	public static final int SIZE_SUBJECT = 5;
	public static final int SIZE_TAB = 6;
	public static final int SIZE_TITLE = 7;
	public static final int SIZE_BULLET = 8;
	public static final int SIZE_TEXT = 9;
	public static final int SIZE_SUBJECT_L = 10;
	public static final int SIZE_TAB_L = 11;
	public static final int SIZE_TITLE_L = 12;
	public static final int SIZE_BULLET_L = 13;
	public static final int SIZE_TEXT_L = 14;
	
	public static final String ASSETS_PATH = "file:///android_asset/";
	public static final String SP_KEY_STATUSBAR_HEIGHT = "statusBarHe";
	public static final String SP_MAP_HEAD = "~_spMapKey_~";
	public static final String SP_MAP_HEAD_KEY_DELIMITER = "~_K,K_~";
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
	
	public static String inputStreamToString(InputStream is, Charset charset, int bufferSize){
		if(is == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.print("charset get fail, using default, ");
		}
//		System.out.println("charset = " + charset.displayName());
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset), bufferSize);
		StringBuilder stringBuilder = new StringBuilder();// StringBuilder速度較快但不支援多執行緒同步
		String line;
		try {
			try {
				while((line = reader.readLine()) != null){
					stringBuilder.append(line + "\n");
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
			stringBuilder = new StringBuilder();
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
	
	public static String inputStreamToString(InputStream is, Charset charset){
		return inputStreamToString(is, charset, 1024 * 10);
	}
	
	public static byte[] inputStreamToByteArray(InputStream is, int bufferSize){
		if(is == null){
			return null;
		}
		byte[] byteArray = null;
		int progress;
		byte[] buffer = new byte[bufferSize];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
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
		return inputStreamToByteArray(is, 1024 * 10);
	}
	
	public static OutputStream byteArrayToOutputStream(byte[] byteArray, OutputStream os, int bufferSize){
		if(byteArray == null || os == null){
			return null;
		}
		int progress;
		byte[] buffer = new byte[bufferSize];
		InputStream is = new ByteArrayInputStream(byteArray);
		try {
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
		return byteArrayToOutputStream(byteArray, os, 1024 * 10);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		byte[] buffer = new byte[bufferSize];
		try {
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
		return inputStreamWriteOutputStream(is, os, 1024 * 10);
	}
	
	public static boolean ByteArrayWriteOutStream(byte[] byteArray, OutputStream os, int bufferSize){
		if(byteArray == null || os == null){
			return false;
		}
		InputStream bais = new ByteArrayInputStream(byteArray);
		return inputStreamWriteOutputStream(bais, os, bufferSize);
	}
	
	public static boolean ByteArrayWriteOutStream(byte[] byteArray, OutputStream os){
		return ByteArrayWriteOutStream(byteArray, os, 1024 * 10);
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
				file.mkdirs();
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
		return writeSDCardFile(is, directory, fileName, 1024 * 10);
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
			
			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
			while(fileChannelRead.read(byteBuffer) != -1){
				byteBuffer.flip();
				fileChannelWrite.write(byteBuffer);
				byteBuffer.clear();
			}
			
			fileChannelRead.close();
			fileInputStream.close();
			fileChannelWrite.close();
			fileOutputStream.close();
			return file.isFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
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
			byte[] buffer = new byte[bufferSize];
			while(randomAccessFileRead.read(buffer) != -1){
				randomAccessFileWrite.write(buffer);
			}
			randomAccessFileRead.close();
			randomAccessFileWrite.close();
			return file.isFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
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
			
			mappedByteBufferRead = null;
			mappedByteBufferWrite = null;
			fileChannelRead.close();
			fileInputStream.close();
			fileChannelWrite.close();
			randomAccessFile.close();
			return file.isFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void deleteAll(File file){
		File[] fileArray = file.listFiles();
		if(fileArray != null){
			for(int i=0; i<fileArray.length; i++){
				if(fileArray[i].isDirectory()){
					deleteAll(fileArray[i]);
				}else{
					fileArray[i].delete();
				}
			}
		}
		file.delete();
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
					stringBuilder.append("&#"+((int)c)+";");
				}else{
					stringBuilder.append(c);
				}
			}
		}
		return stringBuilder.toString();
	}
	
	/**
	 * 反射類別資料，包含ClassLoader、extends Superclass、implements Interface、Field、Constructor、Method、InnerClass
	 * @param class1 Instance or Class.forName("className")
	 * @return
	 */
	public static void reflectionClassInfo(Class<?> class1){
		String info;
		info = class1.getName();
		Log.v("ClassName", info);
		
		info = class1.getClassLoader().getClass().getName();
		Log.v("Reflection ClassLoader", info);
		
		info = class1.getEnclosingClass().getName();
		Log.i("Reflection OuterClass", info);
		
		info = class1.getSuperclass().getName();
		Log.v("Reflection extends Superclass", info);
		
		Log.i("Reflection implements Interface", "****Interface****");
		Class<?>[] interfaceArray = class1.getInterfaces();
		for(int i=0; i<interfaceArray.length; i++){
			info = interfaceArray[i].getName();
			Log.v("Interface", info);
		}
		
		Log.i("Reflection Field", "****Field****");
		Field[] fieldArray = class1.getDeclaredFields();
		for(int i=0; i<fieldArray.length; i++){
			info = fieldArray[i].toGenericString();
			Log.v("Field", info);
		}
		
		Log.i("Reflection Constructor", "****Constructor****");
		Constructor<?>[] constructorArray = class1.getDeclaredConstructors();
		for(int i=0; i<constructorArray.length; i++){
			info = constructorArray[i].toGenericString();
			Log.v("Constructor", info);
		}
		
		Log.i("Reflection Method", "****Method****");
		Method[] methodArray = class1.getDeclaredMethods();
		for(int i=0; i<methodArray.length; i++){
			info = methodArray[i].toGenericString();
			Log.v("Method", info);
		}
		
		Log.i("Reflection InnerClass", "****InnerClass****");
		Class<?>[] classArray = class1.getDeclaredClasses();
		for(int i=0; i<classArray.length; i++){
			info = classArray[i].getName();
			Log.v("InnerClass", info);
		}
	}
	
	public static void setTextSize(Activity activity, TextView textView, int unit, float size){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		DisplayMetrics dmRes = activity.getResources().getDisplayMetrics();
		if(dm.scaledDensity == dmRes.scaledDensity){
			textView.setTextSize(unit, size);
		}else{
			if(size != textView.getTextSize()){
				textView.getPaint().setTextSize(TypedValue.applyDimension(unit, size, dm));
				if (textView.getLayout() != null) {
					// Reflection反射調用private方法
					try {
						Method method = textView.getClass().getDeclaredMethod("nullLayouts");
						method.setAccessible(true);
						method.invoke(textView);
						method.setAccessible(false);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					textView.requestLayout();
					textView.invalidate();
				}
			}
		}
	}
	
	public static void setTextSize(Activity activity, TextView textView, float size){
		setTextSize(activity, textView, TypedValue.COMPLEX_UNIT_SP, size);
	}
	
	public static void setTextSizeMethod(Activity activity, TextView textView, int unit, float size){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		DisplayMetrics dmRes = activity.getResources().getDisplayMetrics();
		if(dm.scaledDensity == dmRes.scaledDensity){
			textView.setTextSize(unit, size);
		}else{
			// Reflection反射調用private方法
			try {
				Method method = textView.getClass().getDeclaredMethod("setRawTextSize", float.class);
				method.setAccessible(true);
				method.invoke(textView, TypedValue.applyDimension(unit, size, dm));
				method.setAccessible(false);
			} catch (NoSuchMethodException e) {
				setTextSize(activity, textView, unit, size);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				setTextSize(activity, textView, unit, size);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				setTextSize(activity, textView, unit, size);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				setTextSize(activity, textView, unit, size);
				e.printStackTrace();
			}
		}
	}
	
	public static void setTextSizeMethod(Activity activity, TextView textView, float size){
		setTextSizeMethod(activity, textView, TypedValue.COMPLEX_UNIT_SP, size);
	}
	
	public static int getTextSize(Activity activity, int flag){
		int textSize = 15;
		if(C_display.isBigScreen(activity, LIMIT_DIP_WIDTH)){
			switch (flag) {
			case SIZE_SUBJECT_L:textSize = 26;break;
			case SIZE_TAB_L:textSize = 25;break;
			case SIZE_TITLE_L:textSize = 24;break;
			case SIZE_BULLET_L:textSize = 23;break;
			case SIZE_TEXT_L:textSize = 22;break;
			case SIZE_SUBJECT:textSize = 21;break;
			case SIZE_TAB:textSize = 20;break;
			case SIZE_TITLE:textSize = 19;break;
			case SIZE_BULLET:textSize = 18;break;
			case SIZE_TEXT:textSize = 17;break;
			case SIZE_SUBJECT_S:textSize = 16;break;
			case SIZE_TAB_S:textSize = 15;break;
			}
		}else{
			switch (flag) {
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
			}
		}
		return textSize;
	}
	
	public static int getVisibleHeightSP(Activity activity, String SPname){
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int visibleHe = dm.heightPixels - getStatusBarHeightSP(activity, SPname);
		return visibleHe;
	}
	
	public static int getStatusBarHeightSP(Activity activity, String SPname){
		SharedPreferences sp = activity.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		return sp.getInt(Utils.SP_KEY_STATUSBAR_HEIGHT, 0);
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
	
	public static SharedPreferences.Editor getNotYetCommitSharedPreferencesEditor(Context context, String SPname, String key, Object value
			, boolean toggleMode){
		SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		SharedPreferences.Editor spEdit = sp.edit();
		spEdit.commit();
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
	
	public static boolean putSharedPreferences(final Context context, final String SPname, final String key, final Object value
			, final boolean toggleMode){
		return getNotYetCommitSharedPreferencesEditor(context, SPname, key, value, toggleMode).commit();
	}
	
	public static boolean putSharedPreferences(final Context context, final String SPname, final String key, final Object value){
		return getNotYetCommitSharedPreferencesEditor(context, SPname, key, value, false).commit();
	}
	
	public static boolean removeSharedPreferences(Context context, String SPname, String key){
		return putSharedPreferences(context, SPname, key, null, true);
	}
	
	public static void putSharedPreferences(final Context context, final String SPname, final String key, final Object value
			, final boolean toggleMode, final Handler handler){
		final SharedPreferences.Editor spEdit = getNotYetCommitSharedPreferencesEditor(context, SPname, key, value, toggleMode);
		
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
	
	public static void putSharedPreferences(Context context, String SPname, String key, Object value, Handler handler){
		putSharedPreferences(context, SPname, key, value, false, handler);
	}
	
	public static void removeSharedPreferences(Context context, String SPname, String key, Handler handler){
		putSharedPreferences(context, SPname, key, null, true, handler);
	}
	
	public static void putSharedPreferencesMap(Context context, String SPname, String mapSaveKey, Map<String, String> map
			, final Handler handler){
		putSharedPreferencesMap(context, SPname, mapSaveKey, map, false, handler);
	}
	
	public static void putSharedPreferencesMap(final Context context, final String SPname, final String mapSaveKey
			, final Map<String, String> map, final boolean toggleMode, final Handler handler){
		final SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		final SharedPreferences.Editor spEdit = sp.edit();
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				putSharedPreferencesMap(context, SPname, mapSaveKey, map, toggleMode, sp, spEdit);
				
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
	
	private static void putSharedPreferencesMap(Context context, String SPname, String mapSaveKey, Map<String, String> map
			, boolean toggleMode, SharedPreferences sp, SharedPreferences.Editor spEdit){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spOldKey = sp.getString(spMapHeadKey, "");
		Set<String> oldKeySet = new HashSet<String>();
		if(!TextUtils.isEmpty(spOldKey)){
			String[] spOldKeyArray = spOldKey.split(SP_MAP_HEAD_KEY_DELIMITER);
			for(int i=0; i<spOldKeyArray.length; i++){
				oldKeySet.add(spOldKeyArray[i]);
			}
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
						spNewKey = getStringSymbolCombine(spNewKey, entry.getKey(), false);
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
		
		if(oldKeySet.size() > 0){
			Iterator<String> iterator = oldKeySet.iterator();
			for(int i=0; i<oldKeySet.size(); i++){
				spEdit.remove(spMapHeadKey + iterator.next());
			}
		}
	}
	
	public static void removeSharedPreferencesMap(Context context, String SPname, final String mapSaveKey, final Handler handler){
		SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		final SharedPreferences.Editor spEdit = sp.edit();
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		if(!TextUtils.isEmpty(spKey)){
			String[] spKeyArray = spKey.split(SP_MAP_HEAD_KEY_DELIMITER);
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
	
	public static Map<String, String> getSharedPreferencesMap(Context context, String SPname, String mapSaveKey){
		SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;
		
		String spKey = sp.getString(spMapHeadKey, "");
		Map<String, String> map = new HashMap<String, String>();
		if(spKey == null){
			return null;
		}else if(spKey.length() == 0){
			return map;
		}
		
		String[] spKeyArray = spKey.split(SP_MAP_HEAD_KEY_DELIMITER);
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
	
	@Deprecated
	public static void putSharedPreferencesMapOld(Context context, String SPname, final String saveKey, Map<String, String> map
			, boolean toggleMode, final Callback callback){
		SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		final SharedPreferences.Editor spEdit = sp.edit();
		Iterator<Entry<String, String>> entryIterator = map.entrySet().iterator();
		Entry<String, String> entry;
        for(int i=0; i<map.size(); i++){
        	entry = entryIterator.next();
    		if(toggleMode && sp.contains(saveKey + "_" + "map" + "_" + i + "_" + "key")){
	        	spEdit.remove(saveKey + "_" + "map" + "_" + i + "_" + "key");
	        	spEdit.remove(saveKey + "_" + "map" + "_" + i + "_" + "value");
    		}else{
            	spEdit.putString(saveKey + "_" + "map" + "_" + i + "_" + "key", entry.getKey());
            	spEdit.putString(saveKey + "_" + "map" + "_" + i + "_" + "value", entry.getValue());
    		}
        }
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean isSuccessfully = spEdit.commit();
				if(callback != null){
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putBoolean(saveKey, isSuccessfully);
					msg.what = 1;
					msg.setData(bundle);
					callback.handleMessage(msg);
				}
			}
		});
		thread.start();
	}
	
	@Deprecated
	public static void putSharedPreferencesMapOld(Context context, String SPname, String saveKey, Map<String, String> map
			, final Callback callback){
		removeSharedPreferencesMapOld(context, SPname, saveKey, callback);
		putSharedPreferencesMapOld(context, SPname, saveKey, map, false, callback);
	}
	
	@Deprecated
	public static void removeSharedPreferencesMapOld(Context context, String SPname, String saveKey, final Callback callback){
		putSharedPreferencesMapOld(context, SPname, saveKey, getSharedPreferencesMap(context, SPname, saveKey), true, callback);
	}
	
	@Deprecated
	public static Map<String, String> getSharedPreferencesMapOld(Context context, String SPname, String saveKey){
		SharedPreferences sp = context.getSharedPreferences(SPname, Context.MODE_PRIVATE);
		int i = 0;
		String key, value;
		Map<String, String> map = new HashMap<String, String>();
		while(sp.contains(saveKey + "_" + "map" + "_" + i + "_" + "key")){
			key = sp.getString(saveKey + "_" + "map" + "_" + i + "_" + "key", "");
			value = sp.getString(saveKey + "_" + "map" + "_" + i + "_" + "value", "");
			map.put(key, value);
			i++;
		}
		return map;
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
		return getStringSymbolCombine(body, sub, SP_MAP_HEAD_KEY_DELIMITER, isAllowRepeat);
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
	
	public static void printListItem(List<?> list){
		for(int i=0; i<list.size(); i++){
			System.out.println("count " + i + ":" + list.get(i).toString());
		}
	}
	
	public static void printListItem(List<? extends Map<String, ?>> list, String...keyArray){
		for(int i=0; i<list.size(); i++){
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
	
	public static String[][] getMapToArray(Map<String, ?> map){
		return getMapToArray(map, false);
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
		return replace;
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
	
	/**
	 * intentType<br>
	 * "text/plain"<br>
	 * "image/jpeg"<br>
	 * "image/*"<br>
	 * "video/*"
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
	
	public static Intent getActionSendIntentForAPP(Activity activity, String packageName, String className, String intentType, String subject
			, String text, Uri streamUri){
		PackageManager packageManager = activity.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(resolveInfoList != null && resolveInfoList.size() > 0){
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
			Utils.setToast(activity, packageName + " Not installed", Toast.LENGTH_SHORT);
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
	
	public static void contentSelection(Activity activity, String intentType, String title, int requestCode){
		Intent intent = new Intent();
		intent.setType(intentType);
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(Intent.createChooser(intent, title), requestCode);
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
	
	// 取得螢幕亮度模式
	/**
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1　為自動調節螢幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0　為手動調節螢幕亮度
	 */
	public static int getScreenBrightnessMode(Activity activity){
		int screenBrightnessMode = 0;
		try {
			screenBrightnessMode = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return screenBrightnessMode;
	}
	
	// 取得系統亮度
	public static int getScreenBrightnessForSystem(Activity activity){
		int screenBrightness = 0;
		try {
			screenBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
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
	public static void setScreenBrightnessMode(Activity activity, int screenBrightnessMode){
		Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessMode);
	}
	
	// 設定系統亮度
	/**
	 * brightness = 0 - 255
	 */
	public static void setScreenBrightnessForSystem(Activity activity, int screenBrightness){
		Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightness);
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
	public static void switchSoftInput(Context context, View view, boolean status){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(view != null){
			if(status){
				imm.showSoftInput(view, 0);
			}else{
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}
	
	// 自動切換鍵盤開關
	public static void toggleSoftInput(Context context, View view){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(view != null){
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	/**
	 * 判斷螢幕尺寸<br>
	 * Configuration.SCREENLAYOUT_SIZE_SMALL<br>
	 * Configuration.SCREENLAYOUT_SIZE_NORMAL<br>
	 * Configuration.SCREENLAYOUT_SIZE_LARGE<br>
	 * Configuration.SCREENLAYOUT_SIZE_XLARGE
	 * @param context
	 * @param screenLayoutSize
	 * @return isLeast
	 */
	public static boolean isLayoutSizeAtLeast(Context context, int screenLayoutSize) {
		if (android.os.Build.VERSION.SDK_INT >= 11) {// honeycomb
			// test screen size, use reflection because isLayoutSizeAtLeast is only available since 11
			Configuration configuration = context.getResources().getConfiguration();
			try {
				Method method = configuration.getClass().getMethod("isLayoutSizeAtLeast", int.class);
				boolean isLeast = (Boolean)method.invoke(configuration, screenLayoutSize);
				return isLeast;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
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
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getMacAddress();
	}
	
	// 判斷Activity是否在前端執行
	public static boolean isRunningActivity(Context context){
		// <uses-permission android:name="android.permission.GET_TASKS"/>
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		String runningClassName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		if(context.getClass().getName().equals(runningClassName)){
			return true;
		}else{
			return false;
		}
	}
	
	// 取得系統記憶體資訊
	@SuppressLint("NewApi")
	public static void logActivityManagerMemoryInfo(Context context){
		// ActivityManager.MemoryInfo：系统可用記憶體資訊
		// ActivityManager.RecentTaskInfo：最近的任務資訊
		// ActivityManager.RunningAppProcessInfo：正在執行的程式資訊
		// ActivityManager.RunningServiceInfo：正在執行的服務資訊
		// ActivityManager.RunningTaskInfo：正在运行的任務資訊
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo activityMemoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(activityMemoryInfo);
		
		// Log.v()，v代表verbose，在Logcat中輸出顏色是黑色，沒有代表性意義，通常用來紀錄詳細一點的訊息
		// Log.d()，d代表debug，在Logcat中輸出顏色是藍色，代表程式debug的訊息
		// Log.i()，i代表information，在Logcat中輸出顏色是綠色，代表提示性的訊息
		// Log.w()，w代表warning，在Logcat中輸出顏色是橘色，代表警告的訊息
		// Log.e()，e代表error，在Logcat中輸出顏色是紅色，代表錯誤的訊息
		Log.v("ActivityManager", "App的記憶體限制：" + activityManager.getMemoryClass() + "MB");
		Log.v("ActivityManager", "largeHeapApp的記憶體限制：" + activityManager.getLargeMemoryClass() + "MB");
		Log.v("ActivityManager.MemoryInfo", "系統剩餘記憶體：" + (activityMemoryInfo.availMem >> 10) + "KB");
		Log.v("ActivityManager.MemoryInfo", "系統目前是否執行低記憶體模式：" + activityMemoryInfo.lowMemory);
		Log.v("ActivityManager.MemoryInfo", "系統記憶體低於" + (activityMemoryInfo.threshold >> 10) + "KB時執行低記憶體模式");
		if(Build.VERSION.SDK_INT >= 16){
			Log.v("ActivityManager.MemoryInfo", "系統總記憶體" + (activityMemoryInfo.totalMem >> 10) + "KB");
		}
	}
	
	// 取得App記憶體資訊
	public static void logAppMemoryHeap(){
		Log.v("Debug", "AppNavtiveHeap已分配的記憶體剩餘量：" + (Debug.getNativeHeapFreeSize() >> 10) + "KB");
		Log.v("Debug", "AppNavtiveHeap已分配的記憶體：" + (Debug.getNativeHeapSize() >> 10) + "KB");
		Log.v("Debug", "AppNavtiveHeap已使用的記憶體：" + (Debug.getNativeHeapAllocatedSize() >> 10) + "KB");
		Runtime runtime = Runtime.getRuntime();
		Log.v("Runtime", "AppHeap已分配的記憶體剩餘量：" + (runtime.freeMemory() >> 10) + "KB");
		Log.v("Runtime", "AppHeap已分配的記憶體：" + (runtime.totalMemory() >> 10) + "KB");
		Log.v("Runtime", "AppHeap最大可分配的記憶體：" + (runtime.maxMemory() >> 10) + "KB");
	}
	
	// 取得App記憶體詳細資訊
	public static void logDebugMemoryInfo(){
		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);
		Log.v("Debug.MemoryInfo", "dalvikPrivateDirty:" + memoryInfo.dalvikPrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "dalvikPss:" + memoryInfo.dalvikPss + "KB");
		Log.v("Debug.MemoryInfo", "dalvikSharedDirty:" + memoryInfo.dalvikSharedDirty + "KB");
		Log.v("Debug.MemoryInfo", "nativePrivateDirty:" + memoryInfo.nativePrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "nativePss:" + memoryInfo.nativePss + "KB");
		Log.v("Debug.MemoryInfo", "nativeSharedDirty:" + memoryInfo.nativeSharedDirty + "KB");
		Log.v("Debug.MemoryInfo", "otherPrivateDirty:" + memoryInfo.otherPrivateDirty + "KB");
		Log.v("Debug.MemoryInfo", "otherPss:" + memoryInfo.otherPss + "KB");
		Log.v("Debug.MemoryInfo", "otherSharedDirty:" + memoryInfo.otherSharedDirty + "KB");
	}
	
	// 取得各App系統資訊
	public static void logAppMemoryInfoArray(Context context){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcessList = activityManager.getRunningAppProcesses();
		for(int i=0; i<appProcessList.size(); i++){
			RunningAppProcessInfo appProcessInfo = (RunningAppProcessInfo)appProcessList.get(i);
			int[] pids = new int[]{appProcessInfo.pid};
			Debug.MemoryInfo[] debugMemoryInfoArray = activityManager.getProcessMemoryInfo(pids);
			Log.v("AppMemoryInfo", "processName:" + appProcessInfo.processName + 
					" pid:" + appProcessInfo.pid + " uid:" + appProcessInfo.uid + 
					" memorySize:" + debugMemoryInfoArray[0].dalvikPrivateDirty + "KB");
		}
	}
	
	// 取得軟體資訊
	public static PackageInfo getPackageInfo(Context context, int flags){
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), flags);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo;
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
	public static Signature getKeystoreSignature(Context context){
		PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
		for(Signature signature : packageInfo.signatures){
			return signature;
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
				}else{
					// Custom Install
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
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// 取得手機資訊
	public static List<Map<String, String>> getPhoneInfo(Context context){
		// <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
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
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		// 行動通訊類型
		String[] phoneTypeArray = {"NONE", "GSM", "CDMA", "SIP"};
		String phoneType = phoneTypeArray[telephonyManager.getPhoneType()];
		hashMap.put("phoneType", phoneType);
		infoList.add(hashMap);
		
		return infoList;
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
	
	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	public static void clearViewGroupAllView(ViewGroup viewGroup, boolean isIndicatesGC){
		List<View> list = new ArrayList<View>();
		Utils.getViewGroupAllView(viewGroup, list);
		for(int i=0; i<list.size(); i++){
			Utils.clearDrawable(list.get(i), true, true, false);
		}
		View view;
		for(int i=0; i<list.size(); i++){
			view = list.get(i);
			view.clearFocus();
			view = null;
		}
		list.clear();
		list = null;
		if(isIndicatesGC){
			System.gc();
		}
	}
	
	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	public static void activityFinishClear(Activity activity, boolean isIndicatesGC){
		clearViewGroupAllView((ViewGroup)activity.getWindow().getDecorView(), isIndicatesGC);
	}
	
	public static void getViewGroupAllView(View view, List<View> list){
		list.add(view);
		for(int i=0; i<list.size(); i++){
			if(list.get(i) instanceof ViewGroup){
				ViewGroup viewGroup = (ViewGroup)list.get(i);
				for(int j=0; j<viewGroup.getChildCount(); j++){
					list.add(viewGroup.getChildAt(j));
				}
			}
		}
	}
	
	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	@TargetApi(16)
	public static void clearDrawable(View view, boolean foreground, boolean background, boolean isIndicatesGC){
		// drawable instanceof ColorDrawable
		// drawable instanceof ShapeDrawable
		// drawable instanceof BitmapDrawable
		// drawable instanceof NinePatchDrawable
		// drawable instanceof ClipDrawable
		// drawable instanceof LayerDrawable
		// drawable instanceof StateListDrawable
		// drawable instanceof AnimationDrawable
		Drawable drawable;
		BitmapDrawable bd;
		Bitmap bitmap = null;
		
		if(foreground){
			if(view instanceof ImageView){
				drawable = ((ImageView)view).getDrawable();
				if(drawable != null){
					((ImageView)view).setImageDrawable(null);
					if(drawable instanceof BitmapDrawable){
						bd = (BitmapDrawable)drawable;
						if(bd != null){
							bitmap = bd.getBitmap();
							bd.setCallback(null);
							bd = null;
//							recycleBitmap(bitmap, false);
						}
					}else{
						drawable.setCallback(null);
					}
					drawable = null;
				}
			}else if(view instanceof ImageButton){
				drawable = ((ImageButton)view).getDrawable();
				if(drawable != null){
					((ImageButton)view).setImageDrawable(null);
					if(drawable instanceof BitmapDrawable){
						bd = (BitmapDrawable)drawable;
						if(bd != null){
							bitmap = bd.getBitmap();
							bd.setCallback(null);
							bd = null;
//							recycleBitmap(bitmap, false);
						}
					}else{
						drawable.setCallback(null);
					}
					drawable = null;
				}
			}else if(view instanceof TextView || view instanceof Button){
				Drawable[] drawableArray = ((TextView)view).getCompoundDrawables();
				((TextView)view).setCompoundDrawables(null, null, null, null);
				for(int i=0; i<drawableArray.length; i++){
					if(drawableArray[i] != null){
						if(drawableArray[i] instanceof BitmapDrawable){
							bd = (BitmapDrawable)drawableArray[i];
							if(bd != null){
								bitmap = bd.getBitmap();
								bd.setCallback(null);
								bd = null;
//								recycleBitmap(bitmap, false);
							}
						}else{
							drawableArray[i].setCallback(null);
						}
						drawableArray[i] = null;
					}
				}
			}
		}
		
		if(background){
			drawable = view.getBackground();
			if(drawable != null){
				view.setBackground(null);
//				view.setBackgroundDrawable(null);
				if(drawable instanceof BitmapDrawable){
					bd = (BitmapDrawable)drawable;
					if(bd != null){
						bitmap = bd.getBitmap();
						bd.setCallback(null);
						bd = null;
//						recycleBitmap(bitmap, false);
					}
				}else{
					drawable.setCallback(null);
				}
				drawable = null;
			}
		}
		
		if(bitmap != null){
			bitmap = null;
		}
		if(isIndicatesGC){
			System.gc();
		}
	}
	
	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	public static void recycleBitmap(Bitmap bitmap, boolean isIndicatesGC){
		if(bitmap != null){
			bitmap.recycle();
			bitmap = null;
			if(isIndicatesGC){
				System.gc();
			}
		}
	}
}