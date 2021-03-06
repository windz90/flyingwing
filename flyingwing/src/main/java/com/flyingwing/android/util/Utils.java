/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 4.0.5
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import androidx.annotation.FloatRange;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

@SuppressWarnings({"unused", "IfCanBeSwitch", "ForLoopReplaceableByForEach", "Convert2Diamond"})
public class Utils {

	public static final char[] HEX_CHARS_SAMPLE = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

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

	public static String byteArrayToHexString(byte[] bytes){
		char[] hexChars = new char[bytes.length * 2];
		int value;
		for(int i=0; i<bytes.length; i++){
			value = bytes[i] & 0xFF;
			hexChars[i * 2] = HEX_CHARS_SAMPLE[value >>> 4];
			hexChars[i * 2 + 1] = HEX_CHARS_SAMPLE[value & 0x0F];
		}
		return new String(hexChars);
	}

	public static Object removeNull(Object object, Object replace){
		if(object == null){
			return replace;
		}
		return object;
	}

	public static String removeNull(Object object, String replace){
		if(object == null){
			return replace;
		}
		return object.toString();
	}

	public static String removeNull(Object object){
		if(object == null){
			return "";
		}
		return object.toString();
	}

	public static String removeEmptyString(String original, String replace){
		if(original == null || original.length() == 0){
			return replace;
		}
		return original;
	}

	public static String trimAndMergeLines(String string){
		if(string != null){
			string = string.replace("\n", "").replace("\r", "").trim();
		}
		return string;
	}

	public static String halfWidthToFullWidth(String text){
		StringBuilder stringBuilder = new StringBuilder();
		char word;
		for(int i=0; i<text.length(); i++){
			word = text.charAt(i);
			// ??????ASCII 33~126 ??? ??????ASCII 65281~65374 ????????? ASCII ????????? 65248
			if(word > 32 && word < 127){
				word = (char) ((int) word + 65248);
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
						stringBuilder.append("&#").append((int) c).append(";");
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
		return getStringSymbolCombine(body, sub, IOUtils.SP_MAP_DELIMITER, isAllowRepeat);
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

	public static String getMapToString(Map<String, ?> map){
		Iterator<? extends Entry<String, ?>> iteratorEntry = map.entrySet().iterator();
		if(!iteratorEntry.hasNext()){
			return "{}";
		}

		Entry<String, ?> entry;
		String key;
		Object value;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append('{');
		for(;;){
			entry = iteratorEntry.next();
			key = entry.getKey();
			value = entry.getValue();
			stringBuilder.append(key);
			stringBuilder.append('=');
			stringBuilder.append(value.toString());
			if(!iteratorEntry.hasNext()){
				return stringBuilder.append('}').toString();
			}
			stringBuilder.append(',').append(' ');
		}
	}

	public static String getMapsToString(Map<String, ?>[] maps){
		if(maps.length == 0){
			return "[]";
		}
		Map<String, ?> map;
		Iterator<? extends Entry<String, ?>> iteratorEntry;
		Entry<String, ?> entry;
		String key;
		Object value;
		StringBuilder stringBuilder = new StringBuilder();
		for(int i=0; i<maps.length; i++){
			map = maps[i];
			if(i == 0){
				stringBuilder.append('[').append('{');
			}else{
				stringBuilder.append(',').append(' ').append('{');
			}
			iteratorEntry = map.entrySet().iterator();
			if(!iteratorEntry.hasNext()){
				stringBuilder.append('}');
				continue;
			}

			for(;;){
				entry = iteratorEntry.next();
				key = entry.getKey();
				value = entry.getValue();
				stringBuilder.append(key);
				stringBuilder.append('=');
				stringBuilder.append(value.toString());
				if(!iteratorEntry.hasNext()){
					stringBuilder.append('}');
					break;
				}
				stringBuilder.append(',').append(' ');
			}
		}
		return stringBuilder.append(']').toString();
	}

	public static String getListMapToString(List<Map<String, ?>> list){
		int size = list.size();
		if(size == 0){
			return "[]";
		}
		Map<String, ?> map;
		Iterator<? extends Entry<String, ?>> iteratorEntry;
		Entry<String, ?> entry;
		String key;
		Object value;
		StringBuilder stringBuilder = new StringBuilder();
		for(int i=0; i<size; i++){
			map = list.get(i);
			if(i == 0){
				stringBuilder.append('[').append('{');
			}else{
				stringBuilder.append(',').append(' ').append('{');
			}
			iteratorEntry = map.entrySet().iterator();
			if(!iteratorEntry.hasNext()){
				stringBuilder.append('}');
				continue;
			}

			for(;;){
				entry = iteratorEntry.next();
				key = entry.getKey();
				value = entry.getValue();
				stringBuilder.append(key);
				stringBuilder.append('=');
				stringBuilder.append(value.toString());
				if(!iteratorEntry.hasNext()){
					stringBuilder.append('}');
					break;
				}
				stringBuilder.append(',').append(' ');
			}
		}
		return stringBuilder.append(']').toString();
	}

	public static String[][] getMapToArray(Map<String, ?> map, boolean isReview){
		String[][] strArray = new String[map.size()][2];
		Iterator<? extends Entry<String, ?>> iteratorEntry = map.entrySet().iterator();
		Entry<String, ?> entry;
		int size = map.size();
		for(int i=0; i<size; i++){
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
		Object value;
		int size = list.size();
		for(int i=0; i<size; i++){
			for(int j=0; j<keyArray.length; j++){
				value = list.get(i).get(keyArray[j]);
				if(value != null){
					System.out.println("count " + i + ":" + keyArray[j] + ":" + value.toString());
				}
			}
		}
	}

	public static void printArrayItem(Object[] array){
		for(int i=0; i<array.length; i++){
			System.out.println("count " + i + ":" + array[i].toString());
		}
	}

	public static void setReflectionField(Object objectInstance, String fieldName, Object value){
		// Reflection??????????????????
		try {
			Field field = objectInstance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(objectInstance, value);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object swapReflectionField(Object objectInstance, String fieldName, Object value){
		// Reflection??????????????????
		try {
			Field field = objectInstance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Object objectField = field.get(objectInstance);
			field.set(objectInstance, value);
			field.setAccessible(false);
			return objectField;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getReflectionField(Object objectInstance, String fieldName){
		// Reflection??????????????????
		try {
			Field field = objectInstance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Object objectField = field.get(objectInstance);
			field.setAccessible(false);
			return objectField;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object invokeReflectionMethod(Object objectInstance, String methodName, Class<?>[] parameterTypes, Object... args){
		Object object = null;
		// Reflection??????????????????
		try {
			Method method = objectInstance.getClass().getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			object = method.invoke(objectInstance, args);
			method.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * ???????????????????????????ClassLoader???DeclaringClass???EnclosingClass???extends Superclass???EnumConstant???implements Interface???Field???Constructor???Method???InnerClass
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
	 * Can also be use API {@link Location#distanceBetween(double, double, double, double, float[])} like:<br>
	 * Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results)<br>
	 * return results[0]
	 */
	public static double getDistanceSimpleFromLatitudeAndLongitude(double latitude1, double longitude1, double latitude2, double longitude2) {
		double theta = longitude1 - longitude2;
		double dist = Math.sin(Math.toRadians(latitude1)) * Math.sin(Math.toRadians(latitude2))
				+ Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
				* Math.cos(Math.toRadians(theta));
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		return dist * 60 * 1.1515;
	}

	/**
	 * @param roundRadiusRatio from=0.0, to=100.0
	 */
	public static float getCornerRadiusFromRatio(int width, int height, @FloatRange(from=0.0, to=100.0) float roundRadiusRatio){
		float radiusMax = Math.max(width, height) * 0.5f;
		if(roundRadiusRatio == 100f){
			return radiusMax;
		}
		float radiusMin = radiusMax / 100f;
		if(roundRadiusRatio == 1f){
			return radiusMin;
		}
		return radiusMin * roundRadiusRatio;
	}

	public static void executeCommand(boolean isPrintResult, String...command){
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		try {
			Process process = processBuilder.start();
			if(isPrintResult){
				System.out.println(IOUtils.inputStreamToString(process.getInputStream()));
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
				System.out.println(IOUtils.inputStreamToString(process.getInputStream()));
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
					System.out.println(IOUtils.inputStreamToString(process.getInputStream()));
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
					System.out.println(IOUtils.inputStreamToString(process.getInputStream()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		// Reflection????????????private??????
		try {
			Field field = jsonArray.getClass().getDeclaredField("values");
			field.setAccessible(true);
			List<Object> list = (List<Object>) field.get(jsonArray);
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
			List<Object> list = reflectionJSONArrayToList(jsonArray);
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
		String[][] element = new String[jsonObject.length()][2];
		if(jsonArrayKey != null){
			for(int i=0; i<jsonArrayKey.length(); i++){
				key = jsonArrayKey.optString(i);
				element[i][0] = key;
				element[i][1] = jsonObject.optString(key);
			}
		}
		return element;
	}

	public static Map<String, String> getJSONObjectToMap(JSONObject jsonObject){
		String key;
		JSONArray jsonArrayKey = jsonObject.names();
		Map<String, String> map = new HashMap<String, String>(jsonObject.length());
		if(jsonArrayKey != null){
			for(int i=0; i<jsonArrayKey.length(); i++){
				key = jsonArrayKey.optString(i);
				map.put(key, jsonObject.optString(key));
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> reflectionJSONObjectToMap(JSONObject jsonObject){
		// Reflection????????????private??????
		try {
			Field field = jsonObject.getClass().getDeclaredField("nameValuePairs");
			field.setAccessible(true);
			Map<String, Object> map = (Map<String, Object>) field.get(jsonObject);
			field.setAccessible(false);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final String JSON_ARRAY = "jsonArray";
	public static final String JSON_OBJECT = "jsonObject";
	public static final String JSON_BOOLEAN = "jsonBoolean";
	public static final String JSON_INT = "jsonInt";
	public static final String JSON_LONG = "jsonLong";
	public static final String JSON_DOUBLE = "jsonDouble";
	public static final String JSON_STRING = "jsonString";

	/**
	 * @param pairs <br>
	 * pairs[][3] = String key(null), Object value, String jsonType<br>
	 */
	public static int findJsonArrayItem(JSONArray jsonArray, Object[]...pairs){
		if(jsonArray == null || jsonArray.length() == 0){
			return -1;
		}
		int length = jsonArray.length();
		for(int i=0; i<pairs.length; i++){
			if(pairs[i].length < 3){
				continue;
			}
			for(int j=0; j<length; j++){
				if(pairs[i][2].equals(JSON_ARRAY)){
					JSONArray jsonArrayItem = jsonArray.optJSONArray(j);
					if(jsonArrayItem == null || (!TextUtils.isEmpty((String) pairs[i][1]) && !jsonArrayItem.toString().equals(pairs[i][1]))){
						if(j == length - 1){
							return -1;
						}else{
							continue;
						}
					}
					if(i == pairs.length - 1){
						return j;
					}
					int lengthNew = pairs.length - i - 1;
					Object[][] pairsNew = new Object[lengthNew][];
					System.arraycopy(pairs, i + 1, pairsNew, 0, lengthNew);
					int position = findJsonArrayItem(jsonArrayItem, pairsNew);
					if(position > -1){
						return position;
					}
					if(j == length - 1){
						return -1;
					}
				}else if(pairs[i][2].equals(JSON_OBJECT)){
					JSONObject jsonObjectItem = jsonArray.optJSONObject(j);
					if(jsonObjectItem == null || (!TextUtils.isEmpty((String) pairs[i][1]) && !jsonObjectItem.toString().equals(pairs[i][1]))){
						if(j == length - 1){
							return -1;
						}else{
							continue;
						}
					}
					if(i == pairs.length - 1){
						return j;
					}
					int lengthNew = pairs.length - i - 1;
					Object[][] pairsNew = new Object[lengthNew][];
					System.arraycopy(pairs, i + 1, pairsNew, 0, lengthNew);
					if(findJsonObjectItem(jsonObjectItem, pairsNew)){
						return j;
					}
					if(j == length - 1){
						return -1;
					}
				}else if(pairs[i][2].equals(JSON_BOOLEAN)){
					if(jsonArray.optBoolean(j) == (boolean) pairs[i][1]){
						return j;
					}
				}else if(pairs[i][2].equals(JSON_INT)){
					if(jsonArray.optInt(j) == (int) pairs[i][1]){
						return j;
					}
				}else if(pairs[i][2].equals(JSON_LONG)){
					if(jsonArray.optLong(j) == (long) pairs[i][1]){
						return j;
					}
				}else if(pairs[i][2].equals(JSON_DOUBLE)){
					if(jsonArray.optDouble(j) == (double) pairs[i][1]){
						return j;
					}
				}else if(pairs[i][2].equals(JSON_STRING)){
					if(jsonArray.optString(j).equals(pairs[i][1])){
						return j;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * @param pairs <br>
	 * pairs[][2] = String key, String value<br>
	 * pairs[][3] = String key, Object value, String jsonType<br>
	 */
	public static boolean findJsonObjectItem(JSONObject jsonObject, Object[]...pairs){
		if(jsonObject == null || jsonObject.length() == 0){
			return false;
		}
		for(int i=0; i<pairs.length; i++){
			if(pairs[i].length == 2 || pairs[i][2].equals(JSON_STRING)){
				if(!jsonObject.optString((String) pairs[i][0]).equals(pairs[i][1])){
					return false;
				}
				if(i == pairs.length - 1){
					return true;
				}
			}else if(pairs[i][2].equals(JSON_ARRAY)){
				JSONArray jsonArrayItem = jsonObject.optJSONArray((String) pairs[i][0]);
				if(i == pairs.length - 1){
					if(jsonArrayItem == null){
						return false;
					}
					return TextUtils.isEmpty((String) pairs[i][1]) || jsonArrayItem.toString().equals(pairs[i][1]);
				}
				int lengthNew = pairs.length - i - 1;
				Object[][] pairsNew = new Object[lengthNew][];
				System.arraycopy(pairs, i + 1, pairsNew, 0, lengthNew);
				return findJsonArrayItem(jsonArrayItem, pairsNew) > -1;
			}else if(pairs[i][2].equals(JSON_OBJECT)){
				JSONObject jsonObjectItem = jsonObject.optJSONObject((String) pairs[i][0]);
				if(i == pairs.length - 1){
					if(jsonObjectItem == null){
						return false;
					}
					return TextUtils.isEmpty((String) pairs[i][1]) || jsonObjectItem.toString().equals(pairs[i][1]);
				}
				int lengthNew = pairs.length - i - 1;
				Object[][] pairsNew = new Object[lengthNew][];
				System.arraycopy(pairs, i + 1, pairsNew, 0, lengthNew);
				return findJsonObjectItem(jsonObjectItem, pairsNew);
			}else if(pairs[i][2].equals(JSON_BOOLEAN)){
				if(jsonObject.optBoolean((String) pairs[i][0]) != (boolean) pairs[i][1]){
					return false;
				}
				if(i == pairs.length - 1){
					return true;
				}
			}else if(pairs[i][2].equals(JSON_INT)){
				if(jsonObject.optInt((String) pairs[i][0]) != (int) pairs[i][1]){
					return false;
				}
				if(i == pairs.length - 1){
					return true;
				}
			}else if(pairs[i][2].equals(JSON_LONG)){
				if(jsonObject.optLong((String) pairs[i][0]) != (long) pairs[i][1]){
					return false;
				}
				if(i == pairs.length - 1){
					return true;
				}
			}else if(pairs[i][2].equals(JSON_DOUBLE)){
				if(jsonObject.optDouble((String) pairs[i][0]) != (double) pairs[i][1]){
					return false;
				}
				if(i == pairs.length - 1){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Android ID(SSAID)????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????<br>
	 * ???Android 2.2 (API level 8)????????????????????????bug?????????????????????ANDROID_ID:9774d56d682e549c
	 */
	@SuppressLint("HardwareIds")
	public static String getAndroidID(Context context){
		// BuildSerial ?????????????????? Android 2.3 (API level 9)
//		String buildSerial = android.os.Build.SERIAL;

		/*
		 * UUID(Universally Unique Identifier)?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		 * ???????????????????????????????????????????????????ID????????????????????????ID???
		 * ???????????????????????????(OSF)?????????????????????????????????????????????????????? ????????????????????????ID??????????????????????????????
		 * UUID????????????????????????????????????????????????????????????????????????????????????IEEE?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		 * ???UUID??????????????????????????????????????????????????????????????????????????????????????????UUID?????????????????????????????????UUID?????????????????????????????????????????????
		 */
//		String uuid = UUID.randomUUID().toString();

		return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public static String getAssetsPathForWebViewOnly(String filePath){
		return "file:///android_asset/" + filePath;
	}

	public static String getRawPath(Context context, int rawResourceId){
		return "android.resource://" + context.getApplicationContext().getPackageName() + "/" + rawResourceId;
	}

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
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
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

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static Intent getActionSendIntentForAPP(Context context, String packageName, String className, String intentType, String subject, String text, Uri streamUri){
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		if(intent == null){
			intent = getLauncherIntent(packageName, className);
		}
		// For Android 11 (API level 30) or higher
		// Limited package visibility
		// To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.
		// To view all installed apps, require <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>.
		@SuppressLint("QueryPermissionsNeeded")
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(listResolveInfo.size() == 0){
			intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + packageName));
			return intent;
		}
		intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
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

	public static Intent getActionSendToIntentForEmail(String[] email, String[] cc, String[] bcc, String subject, String text){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SENDTO);// No attachment
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
		return intent;
	}

	public static Intent getActionSendIntentForEmail(String intentType, String[] email, String[] cc, String[] bcc, String subject, String text, Uri streamUri){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);// Single attachment
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
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);// Multiple attachments
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

	public static Intent getIntentFromHistoryStackToFront(Intent intent){
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		return intent;
	}

	public static Intent getIntentFromHistoryStackToFront(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getIntentFromHistoryStackToFront(intent);
	}

	public static Intent getIntentFromHistoryStackToFront(Context context, Class<? extends Activity> targetClass){
		return getIntentFromHistoryStackToFront(new Intent(context, targetClass));
	}

	public static Intent getBackHistoryStackIntent(Intent intent){
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	public static Intent getBackHistoryStackIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getBackHistoryStackIntent(intent);
	}

	public static Intent getBackHistoryStackIntent(Context context, Class<? extends Activity> targetClass){
		return getBackHistoryStackIntent(new Intent(context, targetClass));
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(Intent intent){
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(String packageName, String className){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(packageName, className));
		return getBackDifferentTaskIntent(intent);
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static Intent getBackDifferentTaskIntent(Context context, Class<? extends Activity> targetClass){
		return getBackDifferentTaskIntent(new Intent(context, targetClass));
	}

	/**
	 * Storage Access Framework (SAF)<br>
	 * Call {@link Activity#startActivityForResult(Intent, int, Bundle)}<br>
	 * Uri uri = {@link Intent#getData()}<br>
	 * Write<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openFileDescriptor(Uri, String)}<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openOutputStream(Uri)}<br>
	 * Delete<br>
	 * {@link DocumentsContract#deleteDocument(ContentResolver, Uri)}
	 *
	 * @param isOnlyShowOpenable Filter to only show results that can be "opened", such as a file (as opposed to a virtual file, list of contacts or timezones).
	 */
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static Intent getFileCreateIntent(String intentType, String fileName, Uri contentUriInitialPath, boolean isOnlyShowOpenable){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
		if(isOnlyShowOpenable){
			// Filter to only show results that can be "opened", such as a file (as opposed to a virtual file, list of contacts or timezones).
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		intent.setType(intentType);
		intent.putExtra(Intent.EXTRA_TITLE, fileName);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, contentUriInitialPath);
		}
		return intent;
	}

	/**
	 * Storage Access Framework (SAF)<br>
	 * Call {@link Activity#startActivityForResult(Intent, int, Bundle)}<br>
	 * Uri uri = {@link Intent#getData()}<br>
	 * Write<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openFileDescriptor(Uri, String)}<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openOutputStream(Uri)}<br>
	 * Read<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openFileDescriptor(Uri, String)}<br>
	 * {@link Context#getContentResolver()}, {@link ContentResolver#openInputStream(Uri)}<br>
	 * Delete<br>
	 * {@link DocumentsContract#deleteDocument(ContentResolver, Uri)}
	 *
	 * @param isOnlyShowOpenable Filter to only show results that can be "opened", such as a file (as opposed to a virtual file, list of contacts or timezones).
	 */
	public static Intent getFileSelectorIntent(String intentType, Uri contentUriInitialPath, boolean isOnlyShowOpenable, boolean allowMultiple){
		Intent intent = new Intent();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		}else{
			intent.setAction(Intent.ACTION_GET_CONTENT);
		}
		if(isOnlyShowOpenable){
			// Filter to only show results that can be "opened", such as a file (as opposed to a virtual file, list of contacts or timezones).
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		intent.setType(intentType);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, contentUriInitialPath);
		}
		return intent;
	}

	/**
	 * Storage Access Framework (SAF)<br>
	 * Call {@link Activity#startActivityForResult(Intent, int, Bundle)}<br>
	 * Uri uri = {@link Intent#getData()}<br>
	 * {@link DocumentFile#fromTreeUri(Context, Uri)}, {@link DocumentFile#listFiles}
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public static Intent getDirectorySelectorIntent(String intentType, Uri contentUriInitialPath){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setType(intentType);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, contentUriInitialPath);
		}
		return intent;
	}

	/**
	 * Storage Access Framework (SAF)<br>
	 * Get persistable uri permission (even if the device restart), you can save uri.toString() for next access.
	 */
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static void keepContentUriPermissionFromIntent(Context context, Intent intent){
		Uri[] uris = getIntentUris(intent);
		if(uris.length == 0){
			return;
		}
		for(int i=0; i<uris.length; i++){
			if(DocumentsContract.isDocumentUri(context, uris[i])){
				int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				context.getApplicationContext().getContentResolver().takePersistableUriPermission(uris[i], takeFlags);
			}
		}
	}

	/**
	 * Call {@link Activity#startActivityForResult(Intent, int, Bundle)}
	 */
	public static Intent getImageCropIntent(Uri uriSrc, Uri uriDst, int aspectX, int aspectY, int outputX, int outputY, String outputFormat
			, boolean circleCrop, boolean noFaceDetection, boolean returnData){
		Intent intent = new Intent();
		intent.setAction("com.android.camera.action.CROP");
		intent.putExtra("crop", "true");
		// ????????????????????????
		intent.setDataAndType(uriSrc, "image/*");
		// ??????????????????????????????
		if(!returnData && uriDst != null){
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uriDst);
		}

		// ??????????????????????????????
		if(aspectX > 0){
			intent.putExtra("aspectX", aspectX);
		}
		if(aspectY > 0){
			intent.putExtra("aspectY", aspectY);
		}

		// ?????????????????????????????????
		if(outputX > 0){
			intent.putExtra("outputX", outputX);
		}
		if(outputY > 0){
			intent.putExtra("outputY", outputY);
		}

		// ??????????????????????????? Bitmap.CompressFormat
		intent.putExtra("outputFormat", outputFormat);
		// ???????????????
		intent.putExtra("circleCrop", circleCrop);
		// ??????????????????
		intent.putExtra("noFaceDetection", noFaceDetection);
		// ????????????????????????
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

	public static Activity getActivityFromViewContext(Context contextFromView){
		while(contextFromView instanceof ContextWrapper){
			if(contextFromView instanceof Activity){
				return (Activity) contextFromView;
			}
			contextFromView = ((ContextWrapper) contextFromView).getBaseContext();
		}
		return null;
	}

	public static Activity getActivityFromView(View view){
		Context contextFromView = view.getContext();
		return getActivityFromViewContext(contextFromView);
	}

	public static void callNavigateUpTo(Activity activity){
		String className = NavUtils.getParentActivityName(activity);
		if(activity.getIntent().getAction() != null){
			className = activity.getIntent().getAction();
		}
		if(className != null){
			NavUtils.navigateUpTo(activity, getBackHistoryStackIntent(activity.getPackageName(), className));
		}
	}

	public static void finishAPP(Activity activity){
		// Force kill process
//		android.os.Process.killProcess(android.os.Process.myPid());
		// Finish current task
//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
//			activity.finishAffinity();
//		}
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static int intentMatchAppCount(Context context, Intent intent){
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		// For Android 11 (API level 30) or higher
		// Limited package visibility
		// To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.
		// To view all installed apps, require <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>.
		@SuppressLint("QueryPermissionsNeeded")
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return listResolveInfo.size();
	}

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static int callMatchApp(Context context, Intent intent){
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		// For Android 11 (API level 30) or higher
		// Limited package visibility
		// To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.
		// To view all installed apps, require <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>.
		@SuppressLint("QueryPermissionsNeeded")
		List<ResolveInfo> listResolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(listResolveInfo.size() == 0){
			return 0;
		}
		context.startActivity(intent);
		return listResolveInfo.size();
	}

	private static int callMatchAppWaitResult(Object objectThis, Intent intent, int onRequestCodeForActivity){
		int count;
		Activity activity;
		if(objectThis instanceof Activity){
			activity = (Activity) objectThis;
			count = intentMatchAppCount(activity, intent);
			if(count > 0){
				activity.startActivityForResult(intent, onRequestCodeForActivity);
				return count;
			}
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			activity = fragment.getActivity();
			if(activity != null){
				count = intentMatchAppCount(activity, intent);
				if(count > 0){
					fragment.startActivityForResult(intent, onRequestCodeForActivity);
					return count;
				}
			}
		}else if(objectThis instanceof androidx.fragment.app.Fragment){
			androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) objectThis;
			androidx.fragment.app.FragmentActivity fragmentActivity = fragment.getActivity();
			if(fragmentActivity != null){
				count = intentMatchAppCount(fragmentActivity, intent);
				if(count > 0){
					fragment.startActivityForResult(intent, onRequestCodeForActivity);
					return count;
				}
			}
		}else if(objectThis instanceof Context){
			activity = getActivityFromViewContext((Context) objectThis);
			if(activity != null){
				count = intentMatchAppCount(activity, intent);
				if(count > 0){
					activity.startActivityForResult(intent, onRequestCodeForActivity);
					return count;
				}
			}
		}
		return -1;
	}

	public static int callMatchAppWaitResult(Activity activity, Intent intent, int onRequestCodeForActivity){
		return callMatchAppWaitResult((Object) activity, intent, onRequestCodeForActivity);
	}

	public static int callMatchAppWaitResult(android.app.Fragment fragment, Intent intent, int onRequestCodeForActivity){
		return callMatchAppWaitResult((Object) fragment, intent, onRequestCodeForActivity);
	}

	public static int callMatchAppWaitResult(androidx.fragment.app.Fragment fragment, Intent intent, int onRequestCodeForActivity){
		return callMatchAppWaitResult((Object) fragment, intent, onRequestCodeForActivity);
	}

	public static int callMatchAppWaitResult(Context contextFromView, Intent intent, int onRequestCodeForActivity){
		return callMatchAppWaitResult((Object) contextFromView, intent, onRequestCodeForActivity);
	}

	private static void callFileSelectorWaitResult(final Object objectThis, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, final int onRequestCodeForActivity, final String title){
		final Intent intent = getFileSelectorIntent(intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple);
		new Thread(new Runnable() {

			@Override
			public void run() {
				callMatchAppWaitResult(objectThis, title == null ? intent : Intent.createChooser(intent, title), onRequestCodeForActivity);
			}
		}).start();
	}

	public static void callFileSelectorWaitResult(Activity activity, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity, String title){
		callFileSelectorWaitResult((Object) activity, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, title);
	}

	public static void callFileSelectorWaitResult(android.app.Fragment fragment, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity, String title){
		callFileSelectorWaitResult((Object) fragment, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, title);
	}

	public static void callFileSelectorWaitResult(androidx.fragment.app.Fragment fragment, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable
			, boolean allowMultiple, int onRequestCodeForActivity, String title){
		callFileSelectorWaitResult((Object) fragment, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, title);
	}

	public static void callFileSelectorWaitResult(Context contextFromView, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity, String title){
		callFileSelectorWaitResult((Object) contextFromView, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, title);
	}

	public static void callFileSelectorWaitResult(Activity activity, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity){
		callFileSelectorWaitResult((Object) activity, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, null);
	}

	public static void callFileSelectorWaitResult(android.app.Fragment fragment, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity){
		callFileSelectorWaitResult((Object) fragment, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, null);
	}

	public static void callFileSelectorWaitResult(androidx.fragment.app.Fragment fragment, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable
			, boolean allowMultiple, int onRequestCodeForActivity){
		callFileSelectorWaitResult((Object) fragment, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, null);
	}

	public static void callFileSelectorWaitResult(Context contextFromView, String intentType, Uri uriPickerInitial, boolean isOnlyShowOpenable, boolean allowMultiple
			, int onRequestCodeForActivity){
		callFileSelectorWaitResult((Object) contextFromView, intentType, uriPickerInitial, isOnlyShowOpenable, allowMultiple, onRequestCodeForActivity, null);
	}

	public static int callAppStore(Context context, String packageName){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + packageName));
		return callMatchApp(context, intent);
	}

	public static void callGoogleMapsNavigation(Context context, String sLatit, String sLongit, String dLatit, String dLongit){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://maps.google.com/maps?f=d&saddr=" + sLatit + "," + sLongit +
				"&daddr=" + dLatit + "," + dLongit + "&hl=zh-TW"));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		context.startActivity(intent);
	}

	@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
	public static void callAppSettings(Context context){
		/*
		 * Mobile???????????????
		 * Settings.ACTION_WIRELESS_SETTINGS
		 * WIFI???????????????
		 * Settings.ACTION_WIFI_SETTINGS
		 */
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.fromParts("package", context.getApplicationContext().getPackageName(), null));
		context.startActivity(intent);
	}

	public static String queryFilePathFromContentUri(Context context, Uri contentUri){
		String path = null;
		Cursor cursor = null;
		try {
			String[] projection;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
				projection = new String[]{MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns.DISPLAY_NAME};
			}else{
				projection = new String[]{MediaStore.MediaColumns.DATA};
			}
			cursor = context.getApplicationContext().getContentResolver().query(contentUri, projection, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
					path = cursor.getString(columnIndex);
					columnIndex = cursor.getColumnIndexOrThrow(projection[1]);
					path = path + File.separator + cursor.getString(columnIndex);
				}else{
					path = cursor.getString(columnIndex);
				}
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

	public static Uri[] getIntentUris(Intent intent){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			ClipData clipData = intent.getClipData();
			if(clipData != null){
				Uri[] uris = new Uri[clipData.getItemCount()];
				for(int i=0; i<uris.length; i++){
					uris[i] = clipData.getItemAt(i).getUri();
				}
				return uris;
			}
		}
		if(intent.getData() != null){
			return new Uri[]{intent.getData()};
		}
		return new Uri[0];
	}

	public static String[] getFilesPathFromIntentUri(Context context, Intent intent){
		Uri[] uris = getIntentUris(intent);
		String[] paths = new String[uris.length], divide;
		String authority, docId, scheme;
		for(int i=0; i<uris.length; i++){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				if(!DocumentsContract.isDocumentUri(context, uris[i])){
					paths[i] = queryFilePathFromContentUri(context, uris[i]);
					continue;
				}

				authority = uris[i].getAuthority();
				if(authority == null){
					continue;
				}
				docId = DocumentsContract.getDocumentId(uris[i]);
				if(authority.equals("com.android.providers.downloads.documents")){
					uris[i] = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
					paths[i] = queryFilePathFromContentUri(context, uris[i]);
					continue;
				}

				divide = docId.split(":");
				if(authority.equals("com.android.externalstorage.documents")){
					if(divide[0].equals("primary")){
						paths[i] = Environment.getExternalStorageDirectory() + File.separator + divide[1];
					}
				}else if(authority.equals("com.android.providers.media.documents")){
					if(divide[0].equals("image")){
						uris[i] = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
						paths[i] = queryFilePathFromContentUri(context, uris[i]);
					}else if(divide[0].equals("audio")){
						uris[i] = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
						paths[i] = queryFilePathFromContentUri(context, uris[i]);
					}else if(divide[0].equals("video")){
						uris[i] = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(divide[1]));
						paths[i] = queryFilePathFromContentUri(context, uris[i]);
					}
				}
			}

			scheme = uris[i].getScheme();
			if(scheme != null && scheme.equals("content")){
				paths[i] = queryFilePathFromContentUri(context, uris[i]);
			}else{
				paths[i] = uris[i].getPath();
			}
		}
		return paths;
	}

	// ????????????????????????
	/**
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0??????????????????????????????
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1??????????????????????????????
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setScreenBrightnessMode(Context context, int screenBrightnessMode, int onRequestCodeForActivity){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkWriteSettingsPermissionWaitResult(context, onRequestCodeForActivity)){
			Settings.System.putInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessMode);
		}
	}

	// ??????????????????
	/**
	 * brightness = 0 - 255
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setScreenBrightnessForSystem(Context context, int screenBrightness, int onRequestCodeForActivity){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkWriteSettingsPermissionWaitResult(context, onRequestCodeForActivity)){
			Settings.System.putInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightness);
		}
	}

	// ??????????????????
	/**
	 * brightness = 0 - 255
	 */
	public static void setScreenBrightnessForActivity(Activity activity, int screenBrightness){
		WindowManager.LayoutParams windowManagerLayoutParams = activity.getWindow().getAttributes();
		windowManagerLayoutParams.screenBrightness = screenBrightness / 255.0f;
		activity.getWindow().setAttributes(windowManagerLayoutParams);
	}

	// ???????????????????????????????????????
	/**
	 * show_touches = 0 ?????????????????????
	 * show_touches = 1 ??????????????????
	 * android.permission.WRITE_SETTINGS
	 */
	public static void setTouchPointState(Context context, boolean isShow, int onRequestCodeForActivity){
		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkWriteSettingsPermissionWaitResult(context, onRequestCodeForActivity)){
			Settings.System.putInt(context.getApplicationContext().getContentResolver(), "show_touches", isShow ? 1 : 0);
		}
	}

	// ????????????????????????
	/**
	 * SCREEN_BRIGHTNESS_MODE_MANUAL = 0??????????????????????????????
	 * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1??????????????????????????????
	 */
	public static int getScreenBrightnessMode(Context context){
		return Settings.System.getInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
	}

	// ??????????????????
	/**
	 * brightness = 0 - 255
	 */
	public static int getScreenBrightnessForSystem(Context context){
		return Settings.System.getInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
	}

	// ??????????????????
	/**
	 * brightness = 0 - 255
	 */
	public static int getScreenBrightnessForActivity(Activity activity){
		WindowManager.LayoutParams windowManagerLayoutParams = activity.getWindow().getAttributes();
		if(windowManagerLayoutParams.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE){
			return getScreenBrightnessForSystem(activity);
		}
		return (int) (windowManagerLayoutParams.screenBrightness * 255.0f);
	}

	// ???????????????????????????????????????
	/**
	 * show_touches = 0 ?????????????????????
	 * show_touches = 1 ??????????????????
	 */
	public static int getTouchPointState(Context context){
		return Settings.System.getInt(context.getApplicationContext().getContentResolver(), "show_touches", -1);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private static boolean checkWriteSettingsPermissionWaitResult(Object objectThis, int onRequestCodeForActivity){
		Activity activity;
		if(objectThis instanceof Activity){
			activity = (Activity) objectThis;
			if(Settings.System.canWrite(activity)){
				return true;
			}
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
			intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivityForResult(intent, onRequestCodeForActivity);
		}else if(objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			activity = fragment.getActivity();
			if(activity != null){
				if(Settings.System.canWrite(activity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragment.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}else if(objectThis instanceof androidx.fragment.app.Fragment){
			androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) objectThis;
			androidx.fragment.app.FragmentActivity fragmentActivity = fragment.getActivity();
			if(fragmentActivity != null){
				if(Settings.System.canWrite(fragmentActivity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + fragmentActivity.getApplicationContext().getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragment.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}else if(objectThis instanceof Context){
			activity = getActivityFromViewContext((Context) objectThis);
			if(activity != null){
				if(Settings.System.canWrite(activity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}
		return false;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkWriteSettingsPermissionWaitResult(Activity activity, int onRequestCodeForActivity){
		return checkWriteSettingsPermissionWaitResult((Object) activity, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkWriteSettingsPermissionWaitResult(android.app.Fragment fragment, int onRequestCodeForActivity){
		return checkWriteSettingsPermissionWaitResult((Object) fragment, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkWriteSettingsPermissionWaitResult(androidx.fragment.app.Fragment fragment, int onRequestCodeForActivity){
		return checkWriteSettingsPermissionWaitResult((Object) fragment, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkWriteSettingsPermissionWaitResult(Context contextFromView, int onRequestCodeForActivity){
		return checkWriteSettingsPermissionWaitResult((Object) contextFromView, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private static boolean checkSystemAlertOverlayPermissionWaitResult(Object objectThis, int onRequestCodeForActivity){
		Activity activity;
		if(objectThis instanceof Activity){
			activity = (Activity) objectThis;
			if(Settings.canDrawOverlays(activity)){
				return true;
			}
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
			activity.startActivityForResult(intent, onRequestCodeForActivity);
		}else if(objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			activity = fragment.getActivity();
			if(activity != null){
				if(Settings.canDrawOverlays(activity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
				fragment.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}else if(objectThis instanceof androidx.fragment.app.Fragment){
			androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) objectThis;
			androidx.fragment.app.FragmentActivity fragmentActivity = fragment.getActivity();
			if(fragmentActivity != null){
				if(Settings.canDrawOverlays(fragmentActivity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + fragmentActivity.getApplicationContext().getPackageName()));
				fragment.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}else if(objectThis instanceof Context){
			activity = getActivityFromViewContext((Context) objectThis);
			if(activity != null){
				if(Settings.canDrawOverlays(activity)){
					return true;
				}
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
				activity.startActivityForResult(intent, onRequestCodeForActivity);
			}
		}
		return false;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkSystemAlertOverlayPermissionWaitResult(Activity activity, int onRequestCodeForActivity){
		return checkSystemAlertOverlayPermissionWaitResult((Object) activity, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkSystemAlertOverlayPermissionWaitResult(android.app.Fragment fragment, int onRequestCodeForActivity){
		return checkSystemAlertOverlayPermissionWaitResult((Object) fragment, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkSystemAlertOverlayPermissionWaitResult(androidx.fragment.app.Fragment fragment, int onRequestCodeForActivity){
		return checkSystemAlertOverlayPermissionWaitResult((Object) fragment, onRequestCodeForActivity);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkSystemAlertOverlayPermissionWaitResult(Context contextFromView, int onRequestCodeForActivity){
		return checkSystemAlertOverlayPermissionWaitResult((Object) contextFromView, onRequestCodeForActivity);
	}

	// ??????????????????
	public static void softInputSwitch(Context context, View view, boolean isShow){
		if(context == null || view == null){
			return;
		}
		InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(inputMethodManager == null){
			return;
		}
		if(isShow){
			inputMethodManager.showSoftInput(view, 0);
		}else{
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static void softInputSwitch(Window window, boolean isShow){
		if(window == null){
			return;
		}
		if(isShow){
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}else{
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}

	// ????????????????????????
	public static void softInputToggle(Context context, View view){
		InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(inputMethodManager == null){
			return;
		}
		inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	public static NotificationCompat.Builder createNotificationCompatBuilder(Context context, String channelId, String ticker, String contentTitle, String contentText
			, String contentInfo, String subText, Integer color, int smallIcon, Bitmap bitmapLargeIcon, Long when, PendingIntent pendingIntent){
		NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(context, channelId);
		// According to high notification priority or channel importance, Android 3.0 (API level 11) to 4.4 (API level 19) use ticker, Android 5.0 (API level 21) and higher use head up.
		notificationCompatBuilder.setTicker(ticker);
		notificationCompatBuilder.setContentTitle(contentTitle);
		notificationCompatBuilder.setContentText(contentText);
		notificationCompatBuilder.setContentInfo(contentInfo);
		notificationCompatBuilder.setSubText(subText);
		if(color != null){
			notificationCompatBuilder.setColor(color);
		}
		notificationCompatBuilder.setSmallIcon(smallIcon);
		notificationCompatBuilder.setLargeIcon(bitmapLargeIcon);
		if(when != null){
			notificationCompatBuilder.setWhen(when);
		}
		if(pendingIntent == null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1){
			pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		}
		notificationCompatBuilder.setContentIntent(pendingIntent);
		return notificationCompatBuilder;
	}

	public static NotificationCompat.Builder createNotificationCompatBuilder(Context context, String channelId, String contentTitle, String contentText
			, String contentInfo, String subText, Integer color, int smallIcon, Bitmap bitmapLargeIcon, Long when, PendingIntent pendingIntent){
		return createNotificationCompatBuilder(context, channelId, null, contentTitle, contentText, contentInfo, subText, color, smallIcon, bitmapLargeIcon, when
				, pendingIntent);
	}

	public static NotificationCompat.Builder configureNotificationCompatBuilder(NotificationCompat.Builder notificationCompatBuilder, String groupKey
			, Boolean isGroupSummary, @NotificationCompat.GroupAlertBehavior Integer groupAlertBehavior, String category, Boolean isAutoCancel, Boolean isOngoing
			, Boolean colorize, Boolean isOnlyAlertOnce, Integer notificationVisibility, Notification notificationPublic){
		notificationCompatBuilder.setGroup(groupKey);
		if(isGroupSummary != null){
			notificationCompatBuilder.setGroupSummary(isGroupSummary);
		}
		if(groupAlertBehavior != null){
			notificationCompatBuilder.setGroupAlertBehavior(groupAlertBehavior);
		}
		notificationCompatBuilder.setCategory(category);
		if(isAutoCancel != null){
			notificationCompatBuilder.setAutoCancel(isAutoCancel);
		}
		if(isOngoing != null){
			notificationCompatBuilder.setOngoing(isOngoing);
		}
		if(colorize != null){
			notificationCompatBuilder.setColorized(colorize);
		}
		if(isOnlyAlertOnce != null){
			notificationCompatBuilder.setOnlyAlertOnce(isOnlyAlertOnce);
		}
		if(notificationVisibility != null){
			notificationCompatBuilder.setVisibility(notificationVisibility);
		}
		notificationCompatBuilder.setPublicVersion(notificationPublic);
		return notificationCompatBuilder;
	}

	public static NotificationCompat.Builder configureNotificationCompatBuilder(NotificationCompat.Builder notificationCompatBuilder, NotificationCompat.Style style
			, NotificationCompat.Action notificationCompatAction1, NotificationCompat.Action notificationCompatAction2, NotificationCompat.Action notificationCompatAction3){
		notificationCompatBuilder.setStyle(style);
		if(notificationCompatAction1 != null){
			notificationCompatBuilder.addAction(notificationCompatAction1);
		}
		if(notificationCompatAction2 != null){
			notificationCompatBuilder.addAction(notificationCompatAction2);
		}
		if(notificationCompatAction3 != null){
			notificationCompatBuilder.addAction(notificationCompatAction3);
		}
		return notificationCompatBuilder;
	}

	public static NotificationCompat.Builder configureNotificationCompatBuilder(NotificationCompat.Builder notificationCompatBuilder, Integer notificationDefault
			, Integer notificationPriority){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
			if(notificationDefault != null){
				notificationCompatBuilder.setDefaults(notificationDefault);
			}
			if(notificationPriority != null){
				notificationCompatBuilder.setPriority(notificationPriority);
			}
		}
		return notificationCompatBuilder;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static NotificationChannelGroup createNotificationChannelGroup(String groupId, String groupName, String groupDescription){
		NotificationChannelGroup notificationChannelGroup = new NotificationChannelGroup(groupId, groupName);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
			notificationChannelGroup.setDescription(groupDescription);
		}
		return notificationChannelGroup;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static void setCreatedNotificationChannelGroups(Context context, List<NotificationChannelGroup> listNotificationChannelGroup){
		NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
		if(notificationManager != null){
			notificationManager.createNotificationChannelGroups(listNotificationChannelGroup);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static NotificationChannel createNotificationChannel(String channelId, String channelName, String channelDescription, int channelImportance, String channelGroupId
			, Boolean isShowBadge){
		NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);
		notificationChannel.setDescription(channelDescription);
		notificationChannel.setGroup(channelGroupId);
		if(isShowBadge != null){
			notificationChannel.setShowBadge(isShowBadge);
		}
		return notificationChannel;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static void setCreatedNotificationChannels(Context context, List<NotificationChannel> listNotificationChannel){
		NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
		if(notificationManager != null){
			notificationManager.createNotificationChannels(listNotificationChannel);
		}
	}

	public static void notifyNotification(Context context, String tag, int id, Notification notification){
		NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
		if(notificationManager != null){
			notificationManager.notify(tag, id, notification);
		}
	}

	public static void notifyNotification(Context context, String tag, int id, NotificationCompat.Builder notificationCompatBuilder){
		notifyNotification(context, tag, id, notificationCompatBuilder.build());
	}

	public static void cancelNotification(Context context, String tag, int id){
		NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
		if(notificationManager != null){
			notificationManager.cancel(tag, id);
		}
	}

	public static String[] checkNeedRequestPermissions(Context context, String...permissions){
		StringBuilder stringBuilder = new StringBuilder();
		for(int i=0; i<permissions.length; i++){
			if(ContextCompat.checkSelfPermission(context.getApplicationContext(), permissions[i]) == PackageManager.PERMISSION_DENIED){
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
			if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), permissions[i]) == PackageManager.PERMISSION_DENIED){
				if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])){
					if(stringBuilder.length() > 0){
						stringBuilder.append("\n");
					}
					stringBuilder.append(permissions[i].trim());
				}
			}
		}
		return stringBuilder.length() == 0 ? null : stringBuilder.toString().split("\n");
	}

	/**
	 * Activity reported to<br>
	 * {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}<br>
	 * androidx.fragment.app.Fragment reported to<br>
	 * {@link androidx.fragment.app.Fragment#onRequestPermissionsResult(int, String[], int[])}<br>
	 * android.app.Fragment reported to<br>
	 * {@link android.app.Fragment#onRequestPermissionsResult(int, String[], int[])}
	 */
	private static boolean requestPermissionsWaitResult(Object objectThis, int onRequestCodeForPermissions, boolean isCheckRequest, String...permissions){
		Activity activity;
		if(objectThis instanceof Activity){
			activity = (Activity) objectThis;
			if(isCheckRequest){
				permissions = checkNeedRequestPermissions(activity, permissions);
			}
			if(permissions != null && permissions.length > 0){
				ActivityCompat.requestPermissions(activity, permissions, onRequestCodeForPermissions);
				return true;
			}
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && objectThis instanceof android.app.Fragment){
			android.app.Fragment fragment = (android.app.Fragment) objectThis;
			activity = fragment.getActivity();
			if(activity != null){
				if(isCheckRequest){
					permissions = checkNeedRequestPermissions(activity, permissions);
				}
				if(permissions != null && permissions.length > 0){
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
						fragment.requestPermissions(permissions, onRequestCodeForPermissions);
					}else{
						ActivityCompat.requestPermissions(activity, permissions, onRequestCodeForPermissions);
					}
					return true;
				}
			}
		}else if(objectThis instanceof androidx.fragment.app.Fragment){
			androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) objectThis;
			androidx.fragment.app.FragmentActivity fragmentActivity = fragment.getActivity();
			if(fragmentActivity != null){
				if(isCheckRequest){
					permissions = checkNeedRequestPermissions(fragmentActivity, permissions);
				}
				if(permissions != null && permissions.length > 0){
					fragment.requestPermissions(permissions, onRequestCodeForPermissions);
					return true;
				}
			}
		}else if(objectThis instanceof Context){
			activity = getActivityFromViewContext((Context) objectThis);
			if(activity != null){
				if(isCheckRequest){
					permissions = checkNeedRequestPermissions(activity, permissions);
				}
				if(permissions != null && permissions.length > 0){
					ActivityCompat.requestPermissions(activity, permissions, onRequestCodeForPermissions);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean requestPermissionsWaitResult(Activity activity, int onRequestCodeForPermissions, boolean isCheckRequest, String...permissions){
		return requestPermissionsWaitResult((Object) activity, onRequestCodeForPermissions, isCheckRequest, permissions);
	}

	public static boolean requestPermissionsWaitResult(android.app.Fragment fragment, int onRequestCodeForPermissions, boolean isCheckRequest, String...permissions){
		return requestPermissionsWaitResult((Object) fragment, onRequestCodeForPermissions, isCheckRequest, permissions);
	}

	public static boolean requestPermissionsWaitResult(androidx.fragment.app.Fragment fragment, int onRequestCodeForPermissions, boolean isCheckRequest
			, String...permissions){
		return requestPermissionsWaitResult((Object) fragment, onRequestCodeForPermissions, isCheckRequest, permissions);
	}

	public static boolean requestPermissionsWaitResult(Context contextFromView, int onRequestCodeForPermissions, boolean isCheckRequest, String...permissions){
		return requestPermissionsWaitResult((Object) contextFromView, onRequestCodeForPermissions, isCheckRequest, permissions);
	}

	public static boolean isMainThread(){
		// Looper.getMainLooper().getThread() == Thread.currentThread();
		// Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
		return Looper.getMainLooper() == Looper.myLooper();
	}

	public static void runMainThread(Runnable runnable){
		if(isMainThread()){
			runnable.run();
		}else{
			new Handler(Looper.getMainLooper()).post(runnable);
		}
	}

	public static void runMainThread(Handler.Callback callback){
		if(isMainThread()){
			callback.handleMessage(Message.obtain());
		}else{
			new Handler(Looper.getMainLooper(), callback).sendEmptyMessage(0);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
	public static boolean isScreenOn(Context context, boolean isOnlyAllowDisplayStateOn){
		DisplayManager displayManager = (DisplayManager) context.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
		if(displayManager == null){
			return false;
		}
		for(Display display : displayManager.getDisplays()){
			if(display.getDisplayId() == Display.INVALID_DISPLAY){
				continue;
			}
			if(isOnlyAllowDisplayStateOn){
				if(display.getState() == Display.STATE_ON){
					return true;
				}
			}else{
				if(display.getState() != Display.STATE_OFF){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isDeviceInteractive(Context context){
		PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if(powerManager == null){
			return false;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
			return powerManager.isInteractive();
		}
		//noinspection deprecation
		return powerManager.isScreenOn();
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
	public static boolean isScreenOnAndInteractive(Context context, boolean isOnlyAllowDisplayStateOn){
		PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if(powerManager == null){
			return false;
		}
		DisplayManager displayManager = (DisplayManager) context.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
		if(displayManager == null){
			return false;
		}
		for(Display display : displayManager.getDisplays()){
			if(display.getDisplayId() == Display.INVALID_DISPLAY){
				continue;
			}
			if(powerManager.isInteractive()){
				if(isOnlyAllowDisplayStateOn){
					return display.getState() == Display.STATE_ON;
				}else{
					return display.getState() != Display.STATE_OFF;
				}
			}
		}
		return false;
	}

	// ?????????Activity????????????????????????
	/**@deprecated */
	@RequiresPermission(android.Manifest.permission.GET_TASKS)
	public static boolean isRunningTopActivity(Context context){
		// <uses-permission android:name="android.permission.GET_TASKS"/>
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.GET_TASKS) == PackageManager.PERMISSION_DENIED){
			return false;
		}
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
		List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
		ComponentName componentName = list.get(0).topActivity;
		String runningClassName = componentName == null ? null : componentName.getClassName();
		return context.getClass().getName().equals(runningClassName);
	}

	public static boolean isRunningApp(Context context, String packageName){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
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
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
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
	@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
	public static boolean isRunningAppOnKeep(Context context, String packageName){
		return isRunningAppOnState(context, packageName, RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE);
	}

	// Click Back key finish app
	public static boolean isRunningAppOnBackground(Context context, String packageName){
		return isRunningAppOnState(context, packageName, RunningAppProcessInfo.IMPORTANCE_BACKGROUND);
	}

	public static boolean isRunningAppProcess(Context context, int processId){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(processId == runningAppProcessInfo.pid){
				return true;
			}
		}
		return false;
	}

	public static boolean isRunningAppProcess(Context context, String processName){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
		for(RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()){
			if(processName.equals(runningAppProcessInfo.processName)){
				return true;
			}
		}
		return false;
	}

	public static boolean isRunningService(Context context, Class<?> serviceClass){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return false;
		}
		for(RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceClass.getName().equals(runningServiceInfo.service.getClassName())){
				return true;
			}
		}
		return false;
	}

	// ????????????????????????AppProcess????????????
	public static void logRunningAppProcessInfo(Context context){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return;
		}
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

	// ???????????????????????????
	public static void logActivityManagerMemoryInfo(Context context){
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager == null){
			return;
		}
		// ActivityManager.MemoryInfo??????????????????????????????
		// ActivityManager.RecentTaskInfo????????????????????????
		// ActivityManager.RunningAppProcessInfo??????????????????????????????
		// ActivityManager.RunningServiceInfo??????????????????????????????
		// ActivityManager.RunningTaskInfo??????????????????????????????
		ActivityManager.MemoryInfo activityMemoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(activityMemoryInfo);

		// Log.v()???v??????verbose??????Logcat??????????????????????????????????????????????????????????????????????????????????????????
		// Log.d()???d??????debug??????Logcat???????????????????????????????????????debug?????????
		// Log.i()???i??????information??????Logcat???????????????????????????????????????????????????
		// Log.w()???w??????warning??????Logcat????????????????????????????????????????????????
		// Log.e()???e??????error??????Logcat????????????????????????????????????????????????
		Log.v("ActivityManager", "AppProcess?????????????????????" + activityManager.getMemoryClass() + "MB");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			Log.v("ActivityManager", "AppLargeHeapProcess?????????????????????" + activityManager.getLargeMemoryClass() + "MB");
		}
		Log.v("ActivityManager", "ActivityManager.MemoryInfo ????????????????????????" + (activityMemoryInfo.availMem >> 10) + "KB");
		Log.v("ActivityManager", "ActivityManager.MemoryInfo ?????????????????????????????????????????????" + activityMemoryInfo.lowMemory);
		Log.v("ActivityManager", "ActivityManager.MemoryInfo ?????????????????????" + (activityMemoryInfo.threshold >> 10) + "KB???????????????????????????");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			Log.v("ActivityManager", "MemoryInfo??????????????????" + (activityMemoryInfo.totalMem >> 10) + "KB");
		}
	}

	/**
	 *  ?????????App??????Process???????????????<br>
	 *  NavtiveHeap: C &amp; C++ heap space<br>
	 *  DalvikVMHeap: Java heap space
	 */
	public static void logProcessMemoryHeap(){
		// Java heap space
		Runtime runtime = Runtime.getRuntime();
		Log.v("Runtime", "ProcessDalvikVMHeap???????????????????????????" + (runtime.totalMemory() >> 10) + "KB");
		Log.v("Runtime", "ProcessDalvikVMHeap????????????????????????????????????" + (runtime.freeMemory() >> 10) + "KB");
		Log.v("Runtime", "ProcessDalvikVMHeap?????????????????????????????????" + (runtime.maxMemory() >> 10) + "KB");
		// C & C++ heap space
		Log.v("Debug", "ProcessNavtiveHeap???????????????????????????" + (Debug.getNativeHeapAllocatedSize() >> 10) + "KB");
		Log.v("Debug", "ProcessNavtiveHeap????????????????????????????????????" + (Debug.getNativeHeapFreeSize() >> 10) + "KB");
		Log.v("Debug", "ProcessNavtiveHeap????????????????????????" + (Debug.getNativeHeapSize() >> 10) + "KB");
	}

	// ?????????App??????Process?????????????????????
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

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static PackageInfo getPackageInfo(Context context, String packageName, int flags){
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		try {
			// For Android 11 (API level 30) or higher
			// Limited package visibility
			// To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.
			// To view all installed apps, require <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>.
			return packageManager.getPackageInfo(packageName, flags);
		} catch (NameNotFoundException ignored) {}
		return null;
	}

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static PackageInfo getPackageInfo(Context context, int flags){
		return getPackageInfo(context, context.getApplicationContext().getPackageName(), flags);
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

	// ??????????????????
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

	/**
	 * For Android 11 (API level 30) or higher<br>
	 * Limited package visibility<br>
	 * Affected methods include the following: {@link PackageManager#queryIntentActivities(Intent, int)}, {@link PackageManager#queryIntentServices(Intent, int)}, {@link PackageManager#queryBroadcastReceivers(Intent, int)},
	 * {@link PackageManager#getPackageInfo(String, int)}, {@link PackageManager#getInstalledPackages(int)} (int)}, {@link PackageManager#getInstalledApplications(int)},
	 * {@link Intent#resolveActivity(PackageManager)}, {@link Intent#resolveActivityInfo(PackageManager, int)}.<br>
	 * To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.<br>
	 * To view all installed apps, require android.permission.QUERY_ALL_PACKAGES.
	 */
	public static List<PackageInfo> getInstallPackageInfo(Context context, boolean isContainSystemDefaultInstall){
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		// For Android 11 (API level 30) or higher
		// Limited package visibility
		// To view other packages, the app need to declare the <queries> element to increase the visibility of the target package.
		// To view all installed apps, require <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>.
		@SuppressLint("QueryPermissionsNeeded")
		List<PackageInfo> listPackageInfo = packageManager.getInstalledPackages(0);
		if(!isContainSystemDefaultInstall){
			List<PackageInfo> linkedList = new LinkedList<PackageInfo>(listPackageInfo);
			PackageInfo packageInfo;
			for(Iterator<PackageInfo> iterator=listPackageInfo.iterator(); iterator.hasNext();){
				packageInfo = iterator.next();
				// Remove system default install
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0){
					iterator.remove();
				}
			}
			listPackageInfo = new ArrayList<PackageInfo>(linkedList);
		}
		return listPackageInfo;
	}

	public static boolean isRooted(Context context){
		/*
		 * Release-Keys ?????????????????????
		 * Test-Keys ??????????????????????????????
		 * ????????????Release-Keys??????Test-Keys????????????????????????????????????
		 */
		String buildTags = Build.TAGS;
		if(buildTags != null && buildTags.contains("test-keys")){
			return true;
		}

		// ??????????????????su??????????????????Superuser????????????????????????/system/app/
		File file = new File("/system/app/Superuser.apk");
		if(file.isFile()){
			return true;
		}

		// ??????????????????su??????????????????Superuser?????????????????????????????????root
		if(getPackageInfo(context, "com.noshufou.android.su", PackageManager.GET_ACTIVITIES) != null){
			return true;
		}

		// ??????????????????su??????????????????SuperSU????????????????????????/system/app/
		file = new File("/system/app/SuperSU.apk");
		if(file.isFile()){
			return true;
		}

		// ??????????????????su??????????????????SuperSU?????????????????????????????????root
		if(getPackageInfo(context, "eu.chainfire.supersu", PackageManager.GET_ACTIVITIES) != null){
			return true;
		}

		// ???????????????????????????su???
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
	 * ?????????????????????<br>
	 * android.permission.WRITE_CONTACTS<br>
	 * ??????????????????????????????<br>
	 * android.permission.READ_CONTACTS
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_CONTACTS)
	public static boolean writeContactsFromContentResolver(Context context, String[] infoArray, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED){
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
			// ??????????????????????????????
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

			context.getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentList);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ??????????????????<br>
	 * android.permission.READ_PHONE_STATE<br>
	 * For Android 10 (API level 29) and higher<br>
	 * Restriction on non-resettable device identifiers<br>
	 * android.permission.READ_PRIVILEGED_PHONE_STATE<br>
	 * Affected methods include the following: {@link Build#getSerial()}, {@link TelephonyManager#getImei()}, {@link TelephonyManager#getDeviceId()},
	 * {@link TelephonyManager#getMeid()}, {@link TelephonyManager#getSimSerialNumber()}, {@link TelephonyManager#getSubscriberId()}.<br>
	 * Caution: Third-party apps installed from the Google Play Store cannot declare privileged permissions.
	 */
	@SuppressLint("HardwareIds")
	@RequiresPermission(allOf = {android.Manifest.permission.READ_PHONE_STATE})
	public static Map<String, String> getPhoneInfo(Context context, Handler handlerNoPermissions){
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_PHONE_STATE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}

		TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		if(telephonyManager == null){
			return null;
		}

		Map<String, String> hashMap = new HashMap<String, String>();

		// <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
		// MSISDN(Mobile Subscriber ISDN number) ????????????(????????????PSTN/ISDN??????)
		// MSISDN for GSM
		String MSISDN = telephonyManager.getLine1Number();
		hashMap.put("MSISDN", MSISDN);

		// IMEI(International Mobile Equipment Identity number) or MEID or ESN ????????????(???????????????????????????)
		// For Android 10 (API level 29) and higher
		// Restriction on non-resettable device identifiers
		// <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"/>
		// Caution: Third-party apps installed from the Google Play Store cannot declare privileged permissions.
		String IMEI;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			IMEI = telephonyManager.getImei();
		}else{
			// IMEI for GSM, MEID or ESN for CDMA
			// This method was deprecated in API level 26. Use getImei() which returns IMEI for GSM or getMeid() which returns MEID for CDMA.
			IMEI = telephonyManager.getDeviceId();
		}
		hashMap.put("IMEI", IMEI);

		// For Android 10 (API level 29) and higher
		// Restriction on non-resettable device identifiers
		// <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"/>
		// Caution: Third-party apps installed from the Google Play Store cannot declare privileged permissions.
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			String MEID = telephonyManager.getMeid();
			hashMap.put("MEID", MEID);
		}

		// IMSI(International Mobile Subscriber Identity) SIM?????????(???????????????????????????)
		// IMSI for GSM
		// For Android 10 (API level 29) and higher
		// Restriction on non-resettable device identifiers
		// <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"/>
		// Caution: Third-party apps installed from the Google Play Store cannot declare privileged permissions.
		String IMSI = telephonyManager.getSubscriberId();
		hashMap.put("IMSI", IMSI);

		// ICCID(Integrate Circuit Card Identity) SIM?????????(????????????????????????)
		// For Android 10 (API level 29) and higher
		// Restriction on non-resettable device identifiers
		// <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"/>
		// Caution: Third-party apps installed from the Google Play Store cannot declare privileged permissions.
		String ICCID = telephonyManager.getSimSerialNumber();
		hashMap.put("ICCID", ICCID);

		// ??????????????????
		String roamingStatus = telephonyManager.isNetworkRoaming() ? "?????????" : "?????????";
		hashMap.put("roamingStatus", roamingStatus);

		// ??????????????????
		String networkCountry = telephonyManager.getNetworkCountryIso();
		hashMap.put("networkCountryISO", networkCountry);

		// ??????????????????
		String networkOperator = telephonyManager.getNetworkOperator();
		hashMap.put("networkOperator", networkOperator);

		// ??????????????????
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		hashMap.put("networkOperatorName", networkOperatorName);

		// SIM???????????????
		int simState = telephonyManager.getSimState();
		hashMap.put("simState", "" + simState);

		if(simState == TelephonyManager.SIM_STATE_READY){
			// SIM?????????
			String simCountry = telephonyManager.getSimCountryIso();
			hashMap.put("simCountryISO", simCountry);

			// SIM??????????????????
			String simOperator = telephonyManager.getSimOperator();
			hashMap.put("simOperator", simOperator);

			// SIM??????????????????
			String simOperatorName = telephonyManager.getSimOperatorName();
			hashMap.put("simOperatorName", simOperatorName);
		}

		// ??????????????????
		// Reflection????????????hide??????
		try {
			@SuppressLint("DiscouragedPrivateApi")
			Method method = telephonyManager.getClass().getDeclaredMethod("getNetworkTypeName", int.class);
			method.setAccessible(true);
			String networkTypeName = (String)method.invoke(telephonyManager, telephonyManager.getNetworkType());
			method.setAccessible(false);
			hashMap.put("networkTypeName", networkTypeName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ??????????????????
		String[] phoneTypeArray = {"NONE", "GSM", "CDMA", "SIP"};
		String phoneType = phoneTypeArray[telephonyManager.getPhoneType()];
		hashMap.put("phoneType", phoneType);

		return hashMap;
	}
}