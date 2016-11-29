/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.5.10
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.MemoryFile;
import android.os.Message;
import android.os.PowerManager;
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
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
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
import java.util.StringTokenizer;

@SuppressWarnings({"unused", "WeakerAccess", "IfCanBeSwitch", "ForLoopReplaceableByForEach", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "ThrowFromFinallyBlock"})
public class Utils {

	public static final int SIZE_TEXT_S = 10;
	public static final int SIZE_BULLET_S = 11;
	public static final int SIZE_TITLE_S = 12;
	// Above is not recommended
	public static final int SIZE_TAB_S = 13;
	public static final int SIZE_SUBJECT_S = 14;
	public static final int SIZE_TEXT = 15;
	public static final int SIZE_BULLET = 16;
	public static final int SIZE_TITLE = 17;
	public static final int SIZE_TAB = 18;
	public static final int SIZE_SUBJECT = 19;
	public static final int SIZE_TEXT_L = 20;
	public static final int SIZE_BULLET_L = 21;
	public static final int SIZE_TITLE_L = 22;
	public static final int SIZE_TAB_L = 23;
	public static final int SIZE_SUBJECT_L = 24;
	public static final int SIZE_TEXT_XL = 25;
	public static final int SIZE_BULLET_XL = 26;
	public static final int SIZE_TITLE_XL = 27;
	public static final int SIZE_TAB_XL = 28;
	public static final int SIZE_SUBJECT_XL = 29;

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

	@SuppressWarnings("UnusedAssignment")
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
		try {
			reader = new BufferedReader(new InputStreamReader(is, charset), bufferSize);
			try {
				String line;
				while((line = reader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				reader.close();
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			stringBuilder = null;
			reader = null;
			is = null;
			e.printStackTrace();
		}
		return stringBuilder == null ? null : stringBuilder.toString();
	}

	public static String inputStreamToString(InputStream is, Charset charset){
		return inputStreamToString(is, charset, 1024 * 16);
	}

	public static String inputStreamToString(InputStream is){
		return inputStreamToString(is, null, 1024 * 16);
	}

	@SuppressWarnings("UnusedAssignment")
	public static byte[] inputStreamToByteArray(InputStream is, int bufferSize){
		if(is == null){
			return null;
		}
		byte[] byteArray = null;
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		ByteArrayOutputStream baos;
		try {
			baos = new ByteArrayOutputStream();
			try {
				int progress;
				byte[] buffer = new byte[bufferSize];
				while((progress = is.read(buffer)) != -1){
					baos.write(buffer, 0, progress);
				}
				baos.flush();
				byteArray = baos.toByteArray();
			} finally {
				baos.close();
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			baos = null;
			byteArray = null;
			is = null;
			e.printStackTrace();
		}
		return byteArray;
	}

	public static byte[] inputStreamToByteArray(InputStream is){
		return inputStreamToByteArray(is, 1024 * 16);
	}

	@SuppressWarnings("UnusedAssignment")
	public static OutputStream byteArrayToOutputStream(byte[] byteArray, OutputStream os, int bufferSize){
		if(byteArray == null || os == null){
			return null;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		InputStream is;
		try {
			is = new ByteArrayInputStream(byteArray);
			try {
				int progress;
				byte[] buffer = new byte[bufferSize];
				while((progress = is.read(buffer)) != -1){
					os.write(buffer, 0, progress);
				}
				os.flush();
			} finally {
				is.close();
			}
			return os;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			is = null;
			os = null;
			byteArray = null;
			e.printStackTrace();
		}
		return os;
	}

	public static OutputStream byteArrayToOutputStream(byte[] byteArray, OutputStream os){
		return byteArrayToOutputStream(byteArray, os, 1024 * 16);
	}

	@SuppressWarnings("UnusedAssignment")
	public static byte[] fileToByteArray(File file){
		if(file == null){
			return null;
		}
		byte[] byteArray = null;
		InputStream is;
		try {
			is = new FileInputStream(file);
			byteArray = new byte[(int)file.length()];
			try {
				int progress = 0;
				while(progress < byteArray.length){
					progress = progress + is.read(byteArray, progress, byteArray.length - progress);
				}
			} finally {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			byteArray = null;
			is = null;
			file = null;
			e.printStackTrace();
		}
		return byteArray;
	}

	@SuppressWarnings("UnusedAssignment")
	public static boolean inputStreamWriteOutputStream(InputStream is, OutputStream os, int bufferSize){
		if(is == null || os == null){
			return false;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		try {
			try {
				int progress;
				byte[] buffer = new byte[bufferSize];
				while((progress = is.read(buffer)) != -1){
					os.write(buffer, 0, progress);
				}
				os.flush();
			} finally {
				os.close();
				is.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			os = null;
			is = null;
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

	public static boolean writeMemoryFile(String name, int length, boolean allowPurging, InputStream is, int bufferSize){
		try {
			MemoryFile memoryFile = new MemoryFile(name, length);
			memoryFile.allowPurging(allowPurging);
			boolean isSuccess = inputStreamWriteOutputStream(is, memoryFile.getOutputStream(), bufferSize);
			memoryFile.close();
			return isSuccess;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeMemoryFile(String name, int length, boolean allowPurging, InputStream is){
		return writeMemoryFile(name, length, allowPurging, is, 1024 * 16);
	}

	public static byte[] readMemoryFile(String name, int length, int bufferSize){
		try {
			MemoryFile memoryFile = new MemoryFile(name, length);
			byte[] bytes = inputStreamToByteArray(memoryFile.getInputStream(), bufferSize);
			memoryFile.close();
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readMemoryFile(String name, int length){
		return readMemoryFile(name, length, 1024 * 16);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeSDCardFile(Context context, InputStream is, String directory, String fileName, int bufferSize, Handler handlerNoPermissions){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(!sdCardExist){
			return false;
		}
		if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}

		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		// 取得sdCard路徑
		File file = Environment.getExternalStorageDirectory();
		String sdCardPath = file.toString() + File.separator;
		if(directory.indexOf(sdCardPath) == 0){
			sdCardPath = "";
		}
		file = new File(sdCardPath + directory);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("directory cannot create");
			return false;
		}
		file = new File(sdCardPath + directory + fileName);
		try {
			return inputStreamWriteOutputStream(is, new FileOutputStream(file, false), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeSDCardFile(Context context, InputStream is, String directory, String fileName, Handler handlerNoPermissions){
		return writeSDCardFile(context, is, directory, fileName, 1024 * 16, handlerNoPermissions);
	}

	/**
	 * android.permission.READ_EXTERNAL_STORAGE
	 */
	public static byte[] readSDCardFile(Context context, String directory, String fileName, Handler handlerNoPermissions){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(!sdCardExist){
			return null;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}

		// 取得sdCard路徑
		// <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStorageDirectory();
		String sdCardPath = file.toString() + File.separator;
		if(directory.indexOf(sdCardPath) == 0){
			sdCardPath = "";
		}
		file = new File(sdCardPath + directory + fileName);
		return fileToByteArray(file);
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
				fileChannelWrite.close();
				fileChannelRead.close();
				fileOutputStream.close();
				fileInputStream.close();
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
				randomAccessFileWrite.close();
				randomAccessFileRead.close();
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

			if(bufferSize < 8192){
				bufferSize = 8192;
			}
			try {
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
					mappedByteBufferWrite.clear();
					mappedByteBufferRead.clear();
				}
			} finally {
				fileChannelWrite.close();
				fileChannelRead.close();
				randomAccessFile.close();
				fileInputStream.close();
			}

			return file.isFile();
		} catch (IOException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean fileCreate(File file, boolean targetIsFile, boolean isDeleteIncorrect){
		if(file.exists()){
			if(targetIsFile){
				if(file.isFile()){
					return true;
				}
				if(isDeleteIncorrect && file.canWrite() && file.delete()){
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
				if(isDeleteIncorrect && file.canWrite() && file.delete()){
					return file.mkdirs();
				}
			}
			return targetIsFile ? file.isFile() : file.isDirectory();
		}

		if(!file.getParentFile().exists() || !file.getParentFile().isDirectory()){
			if(filePathLayersCheck(file.getParentFile(), isDeleteIncorrect)){
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

	public static boolean filePathLayersCheck(File file, boolean isDeleteIncorrect){
		if(file.exists()){
			return file.isDirectory() ||
					isDeleteIncorrect && file.canWrite() && file.delete() &&
							(file.mkdirs() || filePathLayersCheck(file.getParentFile(), true));
		}
		return file.mkdirs() || filePathLayersCheck(file.getParentFile(), isDeleteIncorrect);
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

	public static String trimAndMergeLines(String string){
		if(string != null){
			string = string.replace("\n", "").replace("\r", "").trim();
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

	public static String findStringTarget(String text, String target, String prefix, String suffix){
		if(text == null || target == null || prefix == null || suffix == null){
			return null;
		}
		int indexSuffix = text.indexOf(target);
		if(indexSuffix < 0){
			return null;
		}
		indexSuffix = text.indexOf(suffix, indexSuffix);
		if(indexSuffix < 0){
			return null;
		}
		int indexPrefix = text.lastIndexOf(prefix, indexSuffix - 1);
		if(indexPrefix < 0){
			return null;
		}
		return text.substring(indexPrefix, indexSuffix + 1);
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

	public static String[][] getMapToArray(Map<String, ?> map, boolean isReview){
		String[][] strArray = new String[map.size()][2];
		Iterator<? extends Entry<String, ?>> iteratorEntry = map.entrySet().iterator();
		Entry<String, ?> entry;
		for(int i=0; i<map.size(); i++){
			entry = iteratorEntry.next();
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

	public static Object removeJSONArrayItem(JSONArray jsonArray, int index){
		if(jsonArray != null && jsonArray.length() > index){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				return jsonArray.remove(index);
			}
			List list = reflectionJSONArrayToList(jsonArray);
			if(list != null && list.size() > index){
				return list.remove(index);
			}
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
		Map<String, String> map = new HashMap<String, String>(jsonArrayKey.length());
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

	public static Object reflectionField(Object objectClass, String fieldName){
		// Reflection反射調用屬性
		try {
			Field field = objectClass.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Object objectField = field.get(objectClass);
			field.setAccessible(false);
			return objectField;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		return dist * 60 * 1.1515;
	}

	public static float getSpacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

	public static float getRotate(MotionEvent event){
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		double radians = Math.atan2(y, x);
		return (float)Math.toDegrees(radians);
	}

	public static void setMidPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public static float getRoundAngle(float centerX, float centerY, float pointX, float pointY, float angleOffset) {
		// half round 180 angle, left to positive, right to negative
		float angle = (float) Math.toDegrees(Math.atan2(centerX - pointX, centerY - pointY));
		// full round angle, left increment
		if(angle < 0){
			angle += 360;
		}
		// change round angle, right increment
		angle = 360 - angle;

		// offset angle
		angle += 360 - angleOffset;
		if(angle >= 360){
			angle -= 360;
		}
		return angle;
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

	public static void executeCommand(boolean isPrintResult, String...command){
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		try {
			Process process = processBuilder.start();
			if(isPrintResult){
				System.out.println(Utils.inputStreamToString(process.getInputStream()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void executeCommand(boolean isPrintResult, String command){
		StringTokenizer stringTokenizer = new StringTokenizer(command);
		String[] commandPart = new String[stringTokenizer.countTokens()];
		for(int i=0; i<commandPart.length; i++){
			commandPart[i] = stringTokenizer.nextToken();
		}
		ProcessBuilder processBuilder = new ProcessBuilder(commandPart);
		try {
			Process process = processBuilder.start();
			if(isPrintResult){
				System.out.println(Utils.inputStreamToString(process.getInputStream()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void executeMultiCommands(boolean isPrintResult, String[]...commandArray){
		ProcessBuilder processBuilder;
		for(String[] command : commandArray){
			processBuilder = new ProcessBuilder(command);
			try {
				Process process = processBuilder.start();
				if(isPrintResult){
					System.out.println(Utils.inputStreamToString(process.getInputStream()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void executeMultiCommands(boolean isPrintResult, String...commandArray){
		StringTokenizer stringTokenizer;
		String[] commandPart;
		ProcessBuilder processBuilder;
		for(String command : commandArray){
			stringTokenizer = new StringTokenizer(command);
			commandPart = new String[stringTokenizer.countTokens()];
			for(int i=0; i<commandPart.length; i++){
				commandPart[i] = stringTokenizer.nextToken();
			}
			processBuilder = new ProcessBuilder(commandPart);
			try {
				Process process = processBuilder.start();
				if(isPrintResult){
					System.out.println(Utils.inputStreamToString(process.getInputStream()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 取得Android機器ID
	public static String getAndroidID(Context context){
		// BuildSerial 硬體的唯一值 API 9
//		String buildSerial = android.os.Build.SERIAL;

		/*
		 * UUID(Universally Unique Identifier)全局唯一識別字，是指在一台機器上生成的數字，它保證對在同一時空中的所有機器都是唯一的。
		 * 按照開放軟體基金會(OSF)制定的標準計算，用到了乙太網卡位址、 納秒級時間、晶片ID碼和許多可能的數位。
		 * 由以下幾部分的組合：當前日期和時間（UUID的第一個部分與時間有關，如果你在生成一個UUID之後，過幾秒又生成一個UUID，則第一個部分不同，其餘相同）
		 * ，時鐘序列，全局唯一的IEEE機器識別號（如果有網卡，從網卡獲得，沒有網卡以其他方式獲得），UUID的唯一缺陷在於生成的結果字串會比較長。
		 */
//		String uuid = UUID.randomUUID().toString();

		// AndroidID Android API 2.2 當時的部份設備有bug，會產生相同的ANDROID_ID:9774d56d682e549c
		return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public static String getAssetsPathForWebViewOnly(String filePath){
		return "file:///android_asset/" + filePath;
	}

	public static String getRawPath(Context context, int rawResourceId){
		return "android.resource://" + context.getPackageName() + "/" + rawResourceId;
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

	public static int getAttributePixels(Context context, DisplayMetrics displayMetrics, int attrResource, int defInt){
		TypedValue typedValue = new TypedValue();
		Theme theme = context.getTheme();
		if(theme != null){
			if(theme.resolveAttribute(attrResource, typedValue, true)){
				return TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
			}
		}
		return defInt;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context, DisplayMetrics displayMetrics, int defInt){
		return getAttributePixels(context, displayMetrics, android.R.attr.actionBarSize, defInt);
	}

	public static int getUsableHeightSP(Context context, DisplayMetrics displayMetrics, String spName){
		return displayMetrics.heightPixels - getStatusBarHeightSP(context, spName);
	}

	public static int getUsableHeightSP(Context context, String spName){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return getUsableHeightSP(context, displayMetrics, spName);
	}

	public static int getStatusBarHeightSP(Context context, String spName){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getInt(SP_KEY_STATUS_BAR_HEIGHT, 0);
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
		return rectScroll.left <= view.getLeft() && rectScroll.right >= view.getLeft() + view.getWidth() &&
				rectScroll.top <= view.getTop() && rectScroll.bottom >= view.getTop() + view.getHeight();
	}

	public static void clearWebViewCookie(Context context){
		CookieManager cookieManager;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookies(null);
			cookieManager.flush();
		}else{
			//noinspection deprecation
			CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
			//noinspection deprecation
			cookieSyncManager.startSync();
			cookieManager = CookieManager.getInstance();
			//noinspection deprecation
			cookieManager.removeAllCookie();
			//noinspection deprecation
			cookieSyncManager.stopSync();
		}
	}

	public static void setTextSizeFix(Context context, TextView textView, int unit, float textSize){
		DisplayMetrics displayMetricsFromWindowManager = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetricsFromWindowManager);
		DisplayMetrics displayMetricsFromResources = context.getResources().getDisplayMetrics();
		if(displayMetricsFromWindowManager.scaledDensity == displayMetricsFromResources.scaledDensity){
			textView.setTextSize(unit, textSize);
			return;
		}
		// Reflection反射調用private方法
		try {
			Method method = textView.getClass().getDeclaredMethod("setRawTextSize", float.class);
			method.setAccessible(true);
			method.invoke(textView, TypedValue.applyDimension(unit, textSize, displayMetricsFromWindowManager));
			method.setAccessible(false);
		} catch (Exception e) {
			float sizeRaw = TypedValue.applyDimension(unit, textSize, displayMetricsFromWindowManager);
			if(sizeRaw == textView.getTextSize()){
				return;
			}
			textView.getPaint().setTextSize(sizeRaw);
			// Adjust layout
			textView.setEllipsize(textView.getEllipsize());
			e.printStackTrace();
		}
	}

	public static void setTextSizeFix(Context context, TextView textView, float textSize){
		setTextSizeFix(context, textView, TypedValue.COMPLEX_UNIT_SP, textSize);
	}

	public static float getTextSize(float textSize, boolean isBigScreen, float offsetSize){
		if(isBigScreen){
			textSize = textSize + offsetSize;
		}
		return textSize;
	}

	public static float getTextSize(float textSize, boolean isBigScreen){
		return getTextSize(textSize, isBigScreen, 3);
	}

	/**
	 * @param textView {@link TextView#getLayoutParams()}.{@link ViewGroup.LayoutParams#width} or {@link View#getWidth()} must have actual width value.
	 */
	public static float adjustSingleLineTextSizeFitWidth(Context context, TextView textView, String text){
		int width = 0;
		if(textView.getLayoutParams().width > 0){
			width = textView.getLayoutParams().width;
		}else if(textView.getWidth() > 0){
			width = textView.getWidth();
		}
		if(width == 0){
			return 0;
		}

		Paint paint = textView.getPaint();
		int length = text.length();
		int widthDiff = (int) (width - paint.measureText(text, 0, length));
		if(widthDiff == 0){
			return 0;
		}
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		paint.setTextSize(paint.getTextSize() + (int) (widthDiff / displayMetrics.scaledDensity));
		widthDiff = (int) (width - paint.measureText(text, 0, length));
		paint.setTextSize(paint.getTextSize() + (int) (widthDiff / displayMetrics.scaledDensity));

		while (paint.measureText(text, 0, length) < width) {
			paint.setTextSize(paint.getTextSize() + 1);
		}
		while (paint.measureText(text, 0, length) > width) {
			paint.setTextSize(paint.getTextSize() - 1);
		}
		// Adjust layout
		textView.setEllipsize(textView.getEllipsize());
		return paint.getTextSize();
	}

	public static float getTextWidths(Paint textViewPaint, String text){
		/*
		 * 1.
		 * width = TextView.getPaint().measureText(text);
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
		textViewPaint.getTextWidths(text, widths);
		for(int i=0; i<length; i++){
			width = width + (float)Math.ceil(widths[i]);
		}
		return width;
	}

	public static float getTextWidths(TextView textView, String text){
		return getTextWidths(textView.getPaint(), text);
	}

	/**
	 * Inaccurate, not recommended
	 */
	public static float getTextWidths(Context context, float textSize, String text){
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Paint paint = new Paint();
		paint.setTextSize(textSize * displayMetrics.scaledDensity);
		return getTextWidths(paint, text);
	}

	public static float getTextBaselineY(Paint paint, int canvasHeight){
		FontMetrics fontMetrics = paint.getFontMetrics();
		return canvasHeight / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
	}

	public static float getTextStaticLayoutVerticalCenterOffsetY(Paint paint){
		FontMetrics fontMetrics = paint.getFontMetrics();
		return (fontMetrics.descent - fontMetrics.ascent) / 2 + fontMetrics.descent;
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
		Set<String> setOldKey = new HashSet<String>();
		if(!TextUtils.isEmpty(spOldKey)){
			String[] spOldKeyArray = spOldKey.split(SP_MAP_DELIMITER);
			Collections.addAll(setOldKey, spOldKeyArray);
		}

		Iterator<Entry<String, String>> iteratorEntry;
		Entry<String, String> entry;
		String spNewKey = null;
		if(map != null){
			spNewKey = "";
			iteratorEntry = map.entrySet().iterator();
			try {
				while(iteratorEntry.hasNext()){
					entry = iteratorEntry.next();

					if(toggleMode && sp.contains(spMapHeadKey + entry.getKey())){
						spEdit.remove(spMapHeadKey + entry.getKey());
					}else{
						spEdit.putString(spMapHeadKey + entry.getKey(), entry.getValue());
						spNewKey = getStringSymbolCombine(spNewKey, entry.getKey(), SP_MAP_DELIMITER, false);
					}
					if(setOldKey.size() > 0){
						setOldKey.remove(entry.getKey());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		spEdit.putString(spMapHeadKey, spNewKey);

		int size = setOldKey.size();
		if(size > 0){
			Iterator<String> iterator = setOldKey.iterator();
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
		if(TextUtils.isEmpty(spKey)){
			return new HashMap<String, String>();
		}

		String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
		String spValue;
		Map<String, String> map = new HashMap<String, String>(spKeyArray.length);
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
		return sp.getString(spMapHeadKey + spKeyArray[location], null);
	}

	public static String getSharedPreferencesMapInItem(Context context, String spName, String mapSaveKey, String mapSaveItemKey){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spKey = sp.getString(spMapHeadKey, "");
		if(spKey.length() == 0){
			return null;
		}

		return sp.getString(spMapHeadKey + mapSaveItemKey, null);
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
		if(listResolveInfo == null || listResolveInfo.size() == 0){
			setToast(context, packageName + " Not installed");
			Uri uri = Uri.parse("market://details?id=" + packageName);
			intent = new Intent(Intent.ACTION_VIEW, uri);
			return intent;
		}
		intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setType(intentType);
		// intent.setClassName("com.facebook.katana", "com.facebook.katana.ShareLinkActivity");
		// intent.setClassName("com.twitter.android", "com.twitter.android.PostActivity");
		intent.setClassName(packageName, className);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.putExtra(Intent.EXTRA_STREAM, streamUri);
		return intent;
	}

	public static Intent getActionSendToIntentForEmail(String mailToUri, String subject, String text){
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

	public static Intent getLauncherIntent(Intent intent){
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return intent;
	}

	public static Intent getLauncherIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getLauncherIntent(intent);
	}

	public static Intent getLauncherIntent(Context context, Class<?> targetClass){
		return getLauncherIntent(new Intent(context, targetClass));
	}

	public static Intent getBackTaskIntent(Intent intent){
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	public static Intent getBackTaskIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getBackTaskIntent(intent);
	}

	public static Intent getBackTaskIntent(Context context, Class<? extends Activity> targetClass){
		return getBackTaskIntent(new Intent(context, targetClass));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(Intent intent){
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getBackDifferentTaskIntent(intent);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(Context context, Class<? extends Activity> targetClass){
		return getBackDifferentTaskIntent(new Intent(context, targetClass));
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

	/**
	 * Call startActivityForResult
	 */
	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, int aspectX, int aspectY, int outputX, int outputY, String outputFormat
			, boolean circleCrop, boolean noFaceDetection, boolean returnData){
		Intent intent = new Intent("com.android.camera.action.CROP");
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

		return intent;
	}

	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, String outputFormat, boolean circleCrop, boolean noFaceDetection, boolean returnData){
		return getImageCropIntent(uriSrc, uriDst, 0, 0, 0, 0, outputFormat, circleCrop, noFaceDetection, returnData);
	}

	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, boolean circleCrop, boolean noFaceDetection, boolean returnData){
		return getImageCropIntent(uriSrc, uriDst, 0, 0, 0, 0, Bitmap.CompressFormat.PNG.toString(), circleCrop, noFaceDetection, returnData);
	}

	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, String outputFormat, boolean returnData){
		return getImageCropIntent(uriSrc, uriDst, 0, 0, 0, 0, outputFormat, false, false, returnData);
	}

	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, boolean returnData){
		return getImageCropIntent(uriSrc, uriDst, 0, 0, 0, 0, Bitmap.CompressFormat.PNG.toString(), false, false, returnData);
	}

	public static void finishAPP(Activity activity){
		// Force kill process
//		android.os.Process.killProcess(android.os.Process.myPid());
		// Finish current task
//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
//			activity.finishAffinity();
//		}
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	public static int intentMatchAppCount(Context context, Intent intent){
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return listResolveInfo == null ? 0 : listResolveInfo.size();
	}

	public static void callMatchApp(Context context, Intent intent, String failInfo){
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(listResolveInfo == null || listResolveInfo.size() == 0){
			setToast(context, failInfo);
			return;
		}
		context.startActivity(intent);
	}

	public static void callMatchAppWaitResult(Object objectThis, Intent intent, String failInfo, int onActivityResultRequestCode){
		if(objectThis instanceof Activity){
			Activity activity = (Activity) objectThis;
			if(intentMatchAppCount(activity, intent) == 0){
				setToast(activity, failInfo);
				return;
			}
			activity.startActivityForResult(intent, onActivityResultRequestCode);
		}else if(objectThis instanceof android.support.v4.app.Fragment){
			android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) objectThis;
			if(intentMatchAppCount(fragment.getActivity(), intent) == 0){
				setToast(fragment.getActivity(), failInfo);
				return;
			}
			fragment.startActivityForResult(intent, onActivityResultRequestCode);
		}else if(objectThis instanceof android.app.Fragment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			if(intentMatchAppCount(fragment.getActivity(), intent) == 0){
				setToast(fragment.getActivity(), failInfo);
				return;
			}
			fragment.startActivityForResult(intent, onActivityResultRequestCode);
		}
	}

	public static void callContentSelectionWaitResult(final Object objectThis, String intentType, boolean allowMultiple, final int onActivityResultRequestCode
			, final String title, final String failInfo){
		final Intent intent = getContentSelectionIntent(intentType, allowMultiple);
		new Thread(new Runnable() {

			@Override
			public void run() {
				callMatchAppWaitResult(objectThis, title == null ? intent : Intent.createChooser(intent, title), failInfo, onActivityResultRequestCode);
			}
		}).start();
	}

	public static void callContentSelectionWaitResult(Object objectThis, String intentType, boolean allowMultiple, int onActivityResultRequestCode){
		callContentSelectionWaitResult(objectThis, intentType, allowMultiple, onActivityResultRequestCode, null, "No application");
	}

	public static void callAppStore(Context context, String packageName, String failInfo){
		Uri uri = Uri.parse("market://details?id=" + packageName);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		callMatchApp(context, intent, failInfo);
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

	public static String[] getFilesPathFromIntentUri(Context context, Intent intent){
		Uri[] uris = getIntentUris(intent);
		String[] paths = new String[uris.length];
		for(int i=0; i<uris.length; i++){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
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

			if(uris[i].getScheme().equals("content")){
				paths[i] = queryFilePathFromUri(context, uris[i]);
			}else{
				paths[i] = uris[i].getPath();
			}
		}
		return paths;
	}

	@SuppressWarnings("ResourceType")
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

	// 設定螢幕亮度模式
	/**
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1　為自動調節螢幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0　為手動調節螢幕亮度
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setScreenBrightnessMode(Context context, int screenBrightnessMode){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessMode);
	}

	// 取得螢幕亮度模式
	/**
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1　為自動調節螢幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0　為手動調節螢幕亮度
	 */
	public static int getScreenBrightnessMode(Context context){
		try {
			return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// 設定系統亮度
	/**
	 * brightness = 0 - 255
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setScreenBrightnessForSystem(Context context, int screenBrightness){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightness);
	}

	// 取得系統亮度
	public static int getScreenBrightnessForSystem(Context context){
		return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
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

	// 取得目前亮度
	public static int getScreenBrightnessForActivity(Activity activity){
		WindowManager.LayoutParams windowManagerLayoutParams = activity.getWindow().getAttributes();
		if(windowManagerLayoutParams.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE){
			return getScreenBrightnessForSystem(activity);
		}
		return (int)(windowManagerLayoutParams.screenBrightness * 255.0f);
	}

	/**
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setTouchPointState(Context context, boolean isShow){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		Settings.System.putInt(context.getContentResolver(), "show_touches", isShow ? 1 : 0);
	}

	public static int getTouchPointState(Context context){
		try {
			return Settings.System.getInt(context.getContentResolver(), "show_touches");
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@TargetApi(Build.VERSION_CODES.M)
	public static boolean checkWriteSettingsPermissionWaitResult(Object objectThis, int onActivityResultRequestCode){
		if(objectThis instanceof Activity){
			Activity activity = (Activity) objectThis;
			if(!Settings.System.canWrite(activity)){
				Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}else if(objectThis instanceof android.support.v4.app.Fragment){
			android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) objectThis;
			if(!Settings.System.canWrite(fragment.getActivity())){
				Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + fragment.getActivity().getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragment.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}else if(objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			if(!Settings.System.canWrite(fragment.getActivity())){
				Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + fragment.getActivity().getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragment.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.M)
	public static boolean checkSystemAlertOverlayPermissionWaitResult(Object objectThis, int onActivityResultRequestCode){
		if(objectThis instanceof Activity){
			Activity activity = (Activity) objectThis;
			if(!Settings.canDrawOverlays(activity)){
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
				activity.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}else if(objectThis instanceof android.support.v4.app.Fragment){
			android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) objectThis;
			if(!Settings.canDrawOverlays(fragment.getActivity())){
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + fragment.getActivity().getPackageName()));
				fragment.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}else if(objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			if(!Settings.canDrawOverlays(fragment.getActivity())){
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + fragment.getActivity().getPackageName()));
				fragment.startActivityForResult(intent, onActivityResultRequestCode);
				return false;
			}
		}
		return true;
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

	public static Notification getNotification(Context context, String ticker, String contentTitle, String contentText, String contentInfo, int color, int smallIcon
			, Bitmap bitmapLargeIcon, Long when, NotificationCompat.Style style, Intent intent, int onActivityResultRequestCode, int pendingIntentFlag
			, boolean isAutoCancel, boolean isOngoing, int notificationDefault, int notificationPriority, int notificationVisibility, Notification notificationPublic){
		NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(context);
		notificationCompatBuilder.setTicker(ticker);
		notificationCompatBuilder.setContentTitle(contentTitle);
		notificationCompatBuilder.setContentText(contentText);
		notificationCompatBuilder.setContentInfo(contentInfo);
		notificationCompatBuilder.setColor(color);
		notificationCompatBuilder.setSmallIcon(smallIcon);
		if(bitmapLargeIcon != null){
			notificationCompatBuilder.setLargeIcon(bitmapLargeIcon);
		}
		if(when != null){
			notificationCompatBuilder.setWhen(when);
		}
		if(style != null){
			notificationCompatBuilder.setStyle(style);
		}
		if(intent == null){
			intent = new Intent();
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, onActivityResultRequestCode, intent, pendingIntentFlag);
		notificationCompatBuilder.setContentIntent(pendingIntent);
		notificationCompatBuilder.setAutoCancel(isAutoCancel);
		notificationCompatBuilder.setOngoing(isOngoing);
		notificationCompatBuilder.setDefaults(notificationDefault);
		notificationCompatBuilder.setPriority(notificationPriority);
		notificationCompatBuilder.setVisibility(notificationVisibility);
		notificationCompatBuilder.setPublicVersion(notificationPublic);

		return notificationCompatBuilder.build();
	}

	public static Notification getNotification(Context context, String ticker, String contentTitle, String contentText, String contentInfo, int color, int smallIcon
			, Bitmap bitmapLargeIcon, Long when, NotificationCompat.Style style, Intent intent, int onActivityResultRequestCode, int pendingIntentFlag
			, boolean isAutoCancel, boolean isOngoing, int notificationDefault, int notificationVisibility, Notification notificationPublic){
		return getNotification(context, ticker, contentTitle, contentText, contentInfo, color, smallIcon, bitmapLargeIcon, when, style, intent, onActivityResultRequestCode
				, pendingIntentFlag, isAutoCancel, isOngoing, notificationDefault, NotificationCompat.PRIORITY_DEFAULT, notificationVisibility, notificationPublic);
	}

	public static Notification getNotification(Context context, String ticker, String contentTitle, String contentText, String contentInfo, int color, int smallIcon
			, Bitmap bitmapLargeIcon, Long when, NotificationCompat.Style style, Intent intent, int onActivityResultRequestCode, int pendingIntentFlag
			, boolean isAutoCancel, boolean isOngoing, int notificationDefault, int notificationPriority){
		return getNotification(context, ticker, contentTitle, contentText, contentInfo, color, smallIcon, bitmapLargeIcon, when, style, intent, onActivityResultRequestCode
				, pendingIntentFlag, isAutoCancel, isOngoing, notificationDefault, notificationPriority, NotificationCompat.VISIBILITY_PUBLIC, null);
	}

	public static Notification getNotification(Context context, String ticker, String contentTitle, String contentText, String contentInfo, int color, int smallIcon
			, Bitmap bitmapLargeIcon, Long when, NotificationCompat.Style style, Intent intent, int onActivityResultRequestCode, int pendingIntentFlag
			, boolean isAutoCancel, boolean isOngoing, int notificationDefault){
		return getNotification(context, ticker, contentTitle, contentText, contentInfo, color, smallIcon, bitmapLargeIcon, when, style, intent, onActivityResultRequestCode
				, pendingIntentFlag, isAutoCancel, isOngoing, notificationDefault, NotificationCompat.PRIORITY_DEFAULT, NotificationCompat.VISIBILITY_PUBLIC, null);
	}

	public static void sendNotification(Context context, String tag, int id, Notification notification, boolean isCancelExisted){
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
		if(isCancelExisted){
			notificationManager.cancel(tag, id);
		}
		notificationManager.notify(tag, id, notification);
	}

	public static String[] checkNeedRequestPermissions(Context context, String...permissions){
		StringBuilder stringBuilder = new StringBuilder();
		for(int i=0; i<permissions.length; i++){
			if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_DENIED){
				if(stringBuilder.length() > 0){
					stringBuilder.append("\n");
				}
				stringBuilder.append(permissions[i].trim());
			}
		}
		return stringBuilder.length() == 0 ? null : stringBuilder.toString().split("\n");
	}

	public static String[] checkNeedRationaleRequestPermissions(Activity activity, String...permissions){
		StringBuilder stringBuilder = new StringBuilder();
		for(int i=0; i<permissions.length; i++){
			if(ContextCompat.checkSelfPermission(activity, permissions[i]) == PackageManager.PERMISSION_DENIED){
				if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])){
					if(stringBuilder.length() > 0){
						stringBuilder.append("\n");
					}
					stringBuilder.append(permissions[i].trim());
				}
			}
		}
		return stringBuilder.length() == 0 ? new String[0] : stringBuilder.toString().split("\n");
	}

	/**
	 * Activity reported to<br>
	 * {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}<br>
	 * android.support.v4.app.Fragment reported to<br>
	 * {@link android.support.v4.app.Fragment#onRequestPermissionsResult(int, String[], int[])}<br>
	 * android.app.Fragment reported to<br>
	 * {@link android.app.Fragment#onRequestPermissionsResult(int, String[], int[])}
	 */
	public static void requestPermissionsWaitResult(Object objectThis, int onRequestPermissionsResultRequestCode, boolean isCheckRequest, String...permissions){
		String[] needRequestPermissions;
		if(objectThis instanceof Activity){
			Activity activity = (Activity) objectThis;
			needRequestPermissions = isCheckRequest ? checkNeedRequestPermissions(activity, permissions) : null;
			ActivityCompat.requestPermissions(activity, needRequestPermissions == null ? permissions : needRequestPermissions, onRequestPermissionsResultRequestCode);
		}else if(objectThis instanceof android.support.v4.app.Fragment){
			android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) objectThis;
			needRequestPermissions = isCheckRequest ? checkNeedRequestPermissions(fragment.getActivity(), permissions) : null;
			fragment.requestPermissions(needRequestPermissions == null ? permissions : needRequestPermissions, onRequestPermissionsResultRequestCode);
		}else if(objectThis instanceof android.app.Fragment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			needRequestPermissions = isCheckRequest ? checkNeedRequestPermissions(fragment.getActivity(), permissions) : null;
			fragment.requestPermissions(needRequestPermissions == null ? permissions : needRequestPermissions, onRequestPermissionsResultRequestCode);
		}
	}

	public static boolean isScreenOn(Context context, boolean isCheckDisplayState, boolean isCheckInteractive, boolean isMustDisplayStateOn){
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
			if(!isCheckDisplayState && isCheckInteractive){
				return powerManager.isInteractive();
			}
			DisplayManager displayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
			for(Display display : displayManager.getDisplays()){
				if(display.getDisplayId() == Display.INVALID_DISPLAY){
					continue;
				}
				if(isMustDisplayStateOn){
					if(isCheckDisplayState && !isCheckInteractive && display.getState() == Display.STATE_ON){
						return true;
					}
					if(isCheckDisplayState && isCheckInteractive && display.getState() == Display.STATE_ON && powerManager.isInteractive()){
						return true;
					}
				}else{
					if(isCheckDisplayState && !isCheckInteractive && display.getState() != Display.STATE_OFF){
						return true;
					}
					if(isCheckDisplayState && isCheckInteractive && display.getState() != Display.STATE_OFF && powerManager.isInteractive()){
						return true;
					}
				}
			}
			return false;
		}
		//noinspection deprecation
		return powerManager.isScreenOn();
	}

	public static boolean isScreenOn(Context context, boolean isMustDisplayStateOn){
		return isScreenOn(context, true, true, isMustDisplayStateOn);
	}

	// 判斷此Activity是否正在前端執行
	/**@deprecated */
	@SuppressWarnings("deprecation")
	@RequiresPermission(android.Manifest.permission.GET_TASKS)
	public static boolean isRunningTopActivity(Context context){
		// <uses-permission android:name="android.permission.GET_TASKS"/>
		if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.GET_TASKS) == PackageManager.PERMISSION_DENIED){
			return false;
		}
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
		String runningClassName = list.get(0).topActivity.getClassName();
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

	public static boolean isRunningAppOnState(Context context, String packageName, int importance/* RunningAppProcessInfo.importance */){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(packageName.equals(runningAppProcessInfo.processName) && runningAppProcessInfo.importance == importance){
				return true;
			}
			for(String packageNameItem : runningAppProcessInfo.pkgList){
				if(packageName.equals(packageNameItem) && runningAppProcessInfo.importance == importance){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRunningAppOnForeground(Context context, String packageName){
		return isRunningAppOnState(context, packageName, RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
	}

	// Click Home key or App switch key
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static boolean isRunningAppOnKeep(Context context, String packageName){
		return isRunningAppOnState(context, packageName, RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE);
	}

	// Click Back key finish app
	public static boolean isRunningAppOnBackground(Context context, String packageName){
		return isRunningAppOnState(context, packageName, RunningAppProcessInfo.IMPORTANCE_BACKGROUND);
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
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			Log.v("ActivityManager", "AppLargeHeapProcess的記憶體限制：" + activityManager.getLargeMemoryClass() + "MB");
		}
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統剩餘記憶體：" + (activityMemoryInfo.availMem >> 10) + "KB");
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統目前是否執行低記憶體模式：" + activityMemoryInfo.lowMemory);
		Log.v("ActivityManager", "ActivityManager.MemoryInfo 系統記憶體低於" + (activityMemoryInfo.threshold >> 10) + "KB時執行低記憶體模式");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
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
			return context.getPackageManager().getPackageInfo(packageName, flags);
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

	public static boolean isRooted(Context context){
		/*
		 * Release-Keys 正式開發者簽名
		 * Test-Keys 第三方開發者自訂簽名
		 * 一般來說Release-Keys會比Test-Keys更安全，但不一定總是如此
		 */
		String buildTags = Build.TAGS;
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

	/**
	 * 寫入聯絡人資訊<br>
	 * android.permission.WRITE_CONTACTS<br>
	 * 某些機種需要讀取權限<br>
	 * android.permission.READ_CONTACTS
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_CONTACTS)
	public static boolean saveContentProvider(Context context, String[] infoArray, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_CONTACTS};
				handlerNoPermissions.sendMessage(message);
			}
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
			// <uses-permission android:name="android.permission.WRITE_CONTACTS"/>
			// 某些機種需要讀取權限
			// <uses-permission android:name="android.permission.READ_CONTACTS"/>
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

	/**
	 * 取得手機資訊<br>
	 * android.permission.READ_PHONE_STATE
	 */
	@RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
	public static Map<String, String> getPhoneInfo(Context context, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_PHONE_STATE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}

		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

		Map<String, String> hashMap = new HashMap<String, String>();

		// <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
		// MSISDN(Mobile Subscriber ISDN number) 手機號碼(行動設備PSTN/ISDN號碼)
		// MSISDN for GSM
		String MSISDN = telephonyManager.getLine1Number();
		hashMap.put("MSISDN", MSISDN);

		// IMEI(International Mobile Equipment Identity number) or MEID or ESN 手機序號(國際移動設備辨識碼)
		// IMEI for GSM, MEID or ESN for CDMA
		String IMEI = telephonyManager.getDeviceId();
		hashMap.put("IMEI", IMEI);

		// IMSI(International Mobile Subscriber Identity) SIM卡號碼(國際行動用戶辨識碼)
		// IMSI for GSM
		String IMSI = telephonyManager.getSubscriberId();
		hashMap.put("IMSI", IMSI);

		// ICCID(Integrate Circuit Card Identity) SIM卡序號(積體電路卡辨識碼)
		String ICCID = telephonyManager.getSimSerialNumber();
		hashMap.put("ICCID", ICCID);

		// 手機漫遊狀態
		String roamingStatus = telephonyManager.isNetworkRoaming() ? "漫遊中" : "非漫遊";
		hashMap.put("roamingStatus", roamingStatus);

		// 電信網路國別
		String networkCountry = telephonyManager.getNetworkCountryIso();
		hashMap.put("networkCountryISO", networkCountry);

		// 電信公司代號
		String networkOperator = telephonyManager.getNetworkOperator();
		hashMap.put("networkOperator", networkOperator);

		// 電信公司名稱
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		hashMap.put("networkOperatorName", networkOperatorName);

		// SIM卡狀態資訊
		int simState = telephonyManager.getSimState();
		hashMap.put("simState", "" + simState);

		if(simState == TelephonyManager.SIM_STATE_READY){
			// SIM卡國別
			String simCountry = telephonyManager.getSimCountryIso();
			hashMap.put("simCountryISO", simCountry);

			// SIM卡供應商代號
			String simOperator = telephonyManager.getSimOperator();
			hashMap.put("simOperator", simOperator);

			// SIM卡供應商名稱
			String simOperatorName = telephonyManager.getSimOperatorName();
			hashMap.put("simOperatorName", simOperatorName);
		}

		// 行動網路類型
		// Reflection反射調用hide方法
		try {
			Method method = telephonyManager.getClass().getDeclaredMethod("getNetworkTypeName", int.class);
			method.setAccessible(true);
			String networkTypeName = (String)method.invoke(telephonyManager, telephonyManager.getNetworkType());
			method.setAccessible(false);
			hashMap.put("networkTypeName", networkTypeName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 行動通訊類型
		String[] phoneTypeArray = {"NONE", "GSM", "CDMA", "SIP"};
		String phoneType = phoneTypeArray[telephonyManager.getPhoneType()];
		hashMap.put("phoneType", phoneType);

		return hashMap;
	}

	public static void getViewGroupAllView(View view, List<View> list){
		list.add(view);
		if(view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			View viewChild;
			for(int i=0; i<viewGroup.getChildCount(); i++){
				viewChild = viewGroup.getChildAt(i);
				list.add(viewChild);
				if(viewChild instanceof ViewGroup){
					getViewGroupAllView(viewChild, list);
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

	public static void clearViewGroupInsideDrawable(ViewGroup viewGroup, boolean isIndicatesGC){
		List<View> list = new ArrayList<View>();
		getViewGroupAllView(viewGroup, list);
		View view;
		int size = list.size();
		for(int i=size - 1; i>=0; i--){
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

	public static void activityFinishClearDrawable(Activity activity, boolean isIndicatesGC){
		clearViewGroupInsideDrawable((ViewGroup)activity.getWindow().getDecorView(), isIndicatesGC);
	}

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
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				view.setBackground(null);
			}else{
				//noinspection deprecation
				view.setBackgroundDrawable(null);
			}
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