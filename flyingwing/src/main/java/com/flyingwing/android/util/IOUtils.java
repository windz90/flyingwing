/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 4.0.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.MemoryFile;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess", "ForLoopReplaceableByForEach", "Convert2Diamond", "UnusedReturnValue"})
public class IOUtils {

	public static final int IO_BUFFER_SIZE = 1024 * 16;

	public static final String SP_MAP_HEAD = "/!#/spMapHead/#!/";
	public static final String SP_MAP_ITEM_LEFT_BORDER = "(!#/";
	public static final String SP_MAP_ITEM_RIGHT_BORDER = "/#!)=";
	public static final String SP_MAP_DELIMITER = "/!#/-/#!/";

	// Java block begin -----

	@SuppressWarnings("UnusedAssignment")
	public static boolean inputStreamWriteOutputStream(InputStream inputStream, OutputStream outputStream, int bufferSize){
		if(inputStream == null || outputStream == null){
			return false;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		byte[] buffer;
		try {
			buffer = new byte[bufferSize];
			try {
				int progress;
				while((progress = inputStream.read(buffer)) != -1){
					outputStream.write(buffer, 0, progress);
				}
				outputStream.flush();
			} finally {
				outputStream.close();
				inputStream.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			buffer = null;
			e.printStackTrace();
		}
		return false;
	}

	public static boolean inputStreamWriteOutputStream(InputStream inputStream, OutputStream outputStream){
		return inputStreamWriteOutputStream(inputStream, outputStream, IO_BUFFER_SIZE);
	}

	public static boolean byteArrayWriteOutStream(byte[] byteArray, OutputStream outputStream){
		if(byteArray == null || outputStream == null){
			return false;
		}
		try {
			try {
				outputStream.write(byteArray, 0, byteArray.length);
				outputStream.flush();
			} finally {
				outputStream.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("UnusedAssignment")
	public static boolean stringWriteOutStream(String string, OutputStream outputStream, Charset charset){
		if(string == null || outputStream == null){
			return false;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.println("charset get failed, using default charset " + charset.displayName());
		}
		byte[] byteArray;
		try {
			byteArray = string.getBytes(charset);
			try {
				outputStream.write(byteArray, 0, byteArray.length);
				outputStream.flush();
			} finally {
				outputStream.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			byteArray = null;
			e.printStackTrace();
		}
		return false;
	}

	public static boolean stringWriteOutStream(String string, OutputStream outputStream){
		return stringWriteOutStream(string, outputStream, null);
	}

	@SuppressWarnings("UnusedAssignment")
	public static byte[] inputStreamToByteArray(InputStream inputStream, int bufferSize){
		if(inputStream == null){
			return null;
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		ByteArrayOutputStream byteArrayOutputStream;
		byte[] buffer, byteArray = null;
		try {
			buffer = new byte[bufferSize];
			byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				int progress;
				while((progress = inputStream.read(buffer)) != -1){
					byteArrayOutputStream.write(buffer, 0, progress);
				}
				byteArrayOutputStream.flush();
				byteArray = byteArrayOutputStream.toByteArray();
			} finally {
				byteArrayOutputStream.close();
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			buffer = null;
			byteArrayOutputStream = null;
			byteArray = null;
			e.printStackTrace();
		}
		return byteArray;
	}

	public static byte[] inputStreamToByteArray(InputStream inputStream){
		return inputStreamToByteArray(inputStream, IO_BUFFER_SIZE);
	}

	@SuppressWarnings("UnusedAssignment")
	public static String inputStreamToString(InputStream inputStream, Charset charset, int bufferSize){
		if(inputStream == null){
			return null;
		}
		if(charset == null){
			charset = Charset.forName("ISO-8859-1");
//			System.out.println("charset get failed, using default charset " + charset.displayName());
		}
		if(bufferSize < 8192){
			bufferSize = 8192;
		}
		BufferedReader bufferedReader;
		String line;
		StringBuilder stringBuilder = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset), bufferSize);
			stringBuilder = new StringBuilder();// StringBuffer is thread safe, StringBuilder is faster but not thread safe.
			try {
				while((line = bufferedReader.readLine()) != null){
					stringBuilder.append(line).append("\n");
				}
			} finally {
				bufferedReader.close();
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			stringBuilder = null;
			line = null;
			bufferedReader = null;
			e.printStackTrace();
		}
		return stringBuilder == null ? null : stringBuilder.toString();
	}

	public static String inputStreamToString(InputStream inputStream, Charset charset){
		return inputStreamToString(inputStream, charset, IO_BUFFER_SIZE);
	}

	public static String inputStreamToString(InputStream inputStream){
		return inputStreamToString(inputStream, null, IO_BUFFER_SIZE);
	}

	@SuppressWarnings("UnusedAssignment")
	public static byte[] fileToByteArray(File file){
		if(file == null){
			return null;
		}
		InputStream inputStream;
		byte[] byteArray = null;
		try {
			inputStream = new FileInputStream(file);
			byteArray = new byte[(int)file.length()];
			try {
				int progress = 0;
				while(progress < byteArray.length){
					progress = progress + inputStream.read(byteArray, progress, byteArray.length - progress);
				}
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			byteArray = null;
			inputStream = null;
			e.printStackTrace();
		}
		return byteArray;
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

	public static boolean fileCreate(File file, boolean targetIsFile){
		if(targetIsFile){
			if(file.exists() && file.isFile()){
				return true;
			}
			File fileParent = file.getParentFile();
			try {
				return file.canWrite() && (fileParent != null && !fileParent.exists() ? fileParent.mkdirs() && file.createNewFile() : file.createNewFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			if(file.exists() && file.isDirectory()){
				return true;
			}
			return file.canWrite() && file.mkdirs();
		}
		return false;
	}

	public static boolean filePathCheck(File file){
		File fileParent = file.getParentFile();
		boolean isPassParent = true;
		if(fileParent != null && !fileParent.exists()){
			isPassParent = filePathCheck(fileParent);
		}
		return isPassParent && file.isDirectory() || (file.canWrite() && file.mkdir());
	}

	public static boolean fileDeleteAll(File file){
		File[] files = file.listFiles();
		if(files != null){
			for(int i=0; i<files.length; i++){
				if(files[i].isDirectory()){
					fileDeleteAll(files[i]);
				}else if(!files[i].delete()){
					System.out.println("Delete file failed, path:\n" + files[i].getPath());
				}
			}
		}
		return file.delete();
	}

	// Java block end -----

	/**
	 * The file is located at APK [/res/raw/], file cannot exist in subdirectory.
	 * In the past, maximum file size of raw directory was limited to 1MB.
	 */
	public static byte[] readFileFromRawResource(Resources resources, int resourceId){
		return inputStreamToByteArray(resources.openRawResource(resourceId));
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static byte[] readFileFromAssets(Context context, String fileName){
		try {
			return inputStreamToByteArray(context.getApplicationContext().getAssets().open(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static byte[] readFileFromAssets(Resources resources, String fileName){
		try {
			return inputStreamToByteArray(resources.getAssets().open(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static @Nullable OutputStream getWriteFileFromInternalAppOperatingMode(Context context, String fileName, int operatingMode){
		fileName = fileName.replace(File.separator, "_");
		try {
			return context.getApplicationContext().openFileOutput(fileName, operatingMode);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static @Nullable OutputStream getWriteFileFromInternalAppPrivateMode(Context context, String fileName){
		return getWriteFileFromInternalAppOperatingMode(context, fileName, Context.MODE_PRIVATE);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static @Nullable InputStream getReadFileFromInternalApp(Context context, String fileName){
		fileName = fileName.replace(File.separator, "_");
		try {
			return context.getApplicationContext().openFileInput(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static boolean writeFileFromInternalAppOperatingMode(Context context, InputStream inputStream, String fileName, int operatingMode, int bufferSize){
		OutputStream outputStream = getWriteFileFromInternalAppOperatingMode(context, fileName, operatingMode);
		if(outputStream == null){
			return false;
		}
		return inputStreamWriteOutputStream(inputStream, outputStream, bufferSize);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static boolean writeFileFromInternalAppOperatingMode(Context context, InputStream inputStream, String fileName, int operatingMode){
		return writeFileFromInternalAppOperatingMode(context, inputStream, fileName, operatingMode, IO_BUFFER_SIZE);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static boolean writeFileFromInternalAppPrivateMode(Context context, InputStream inputStream, String fileName, int bufferSize){
		return writeFileFromInternalAppOperatingMode(context, inputStream, fileName, Context.MODE_PRIVATE, bufferSize);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static boolean writeFileFromInternalAppPrivateMode(Context context, InputStream inputStream, String fileName){
		return writeFileFromInternalAppOperatingMode(context, inputStream, fileName, Context.MODE_PRIVATE, IO_BUFFER_SIZE);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static @Nullable byte[] readFileFromInternalApp(Context context, String fileName, int bufferSize){
		InputStream inputStream = getReadFileFromInternalApp(context, fileName);
		if(inputStream == null){
			return null;
		}
		return inputStreamToByteArray(inputStream, bufferSize);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static @Nullable byte[] readFileFromInternalApp(Context context, String fileName){
		return readFileFromInternalApp(context, fileName, IO_BUFFER_SIZE);
	}

	/**
	 * The file is located at [/data/data/packageName/files/], file cannot exist in subdirectory.
	 */
	public static boolean deleteFileFromInternalApp(Context context, String fileName){
		return context.getApplicationContext().deleteFile(fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static @Nullable File getWriteFileFromInternalAppFilesDir(Context context, String directoryPath, String fileName){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getFilesDir().getPath();
		File file = new File(internalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static File getReadFileFromInternalAppFilesDir(Context context, String directoryPath, String fileName){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getFilesDir().getPath();
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static boolean writeFileFromInternalAppFilesDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend, int bufferSize){
		File file = getWriteFileFromInternalAppFilesDir(context, directoryPath, fileName);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static boolean writeFileFromInternalAppFilesDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend){
		return writeFileFromInternalAppFilesDir(context, inputStream, directoryPath, fileName, isAppend, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static boolean writeFileFromInternalAppFilesDir(Context context, InputStream inputStream, String directoryPath, String fileName){
		return writeFileFromInternalAppFilesDir(context, inputStream, directoryPath, fileName, false, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static @Nullable byte[] readFileFromInternalAppFilesDir(Context context, String directoryPath, String fileName){
		File file = getReadFileFromInternalAppFilesDir(context, directoryPath, fileName);
		return fileToByteArray(file);
	}

	/**
	 * The file path is located in [/data/data/packageName/files/].
	 */
	public static boolean deleteFileFromInternalAppFilesDir(Context context, String directoryPath, String fileName){
		File file = getReadFileFromInternalAppFilesDir(context, directoryPath, fileName);
		return file.delete();
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static @Nullable File getWriteFileFromInternalAppCacheDir(Context context, String directoryPath, String fileName){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getCacheDir().getPath();
		File file = new File(internalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static File getReadFileFromInternalAppCacheDir(Context context, String directoryPath, String fileName){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getCacheDir().getPath();
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static boolean writeFileFromInternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend
			, int bufferSize){
		File file = getWriteFileFromInternalAppCacheDir(context, directoryPath, fileName);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static boolean writeFileFromInternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend){
		return writeFileFromInternalAppCacheDir(context, inputStream, directoryPath, fileName, isAppend, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static boolean writeFileFromInternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName){
		return writeFileFromInternalAppCacheDir(context, inputStream, directoryPath, fileName, false, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static byte[] readFileFromInternalAppCacheDir(Context context, String directoryPath, String fileName){
		File file = getReadFileFromInternalAppCacheDir(context, directoryPath, fileName);
		return fileToByteArray(file);
	}

	/**
	 * The file path is located in [/data/data/packageName/cache/].
	 */
	public static boolean deleteFileFromInternalAppCacheDir(Context context, String directoryPath, String fileName){
		File file = getReadFileFromInternalAppCacheDir(context, directoryPath, fileName);
		return file.delete();
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static @Nullable File getWriteFileFromInternalAppDirectoryOperatingMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName
			, int operatingMode){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getDir(firstLayerDirectoryName, operatingMode).getPath();
		File file = new File(internalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static @Nullable File getWriteFileFromInternalAppDirectoryPrivateMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName){
		return getWriteFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static File getReadFileFromInternalAppDirectoryOperatingMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName
			, int operatingMode){
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		String internalPath = context.getApplicationContext().getDir(firstLayerDirectoryName, operatingMode).getPath();
		return new File(internalPath + directoryPath + fileName);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static File getReadFileFromInternalAppDirectoryPrivateMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName){
		return getReadFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean writeFileFromInternalAppDirectoryOperatingMode(Context context, InputStream inputStream, String firstLayerDirectoryName, String directoryPath
			, String fileName, int operatingMode, boolean isAppend, int bufferSize){
		File file = getWriteFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, operatingMode);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean writeFileFromInternalAppDirectoryOperatingMode(Context context, InputStream inputStream, String firstLayerDirectoryName, String directoryPath
			, String fileName, int operatingMode, boolean isAppend){
		return writeFileFromInternalAppDirectoryOperatingMode(context, inputStream, firstLayerDirectoryName, directoryPath, fileName, operatingMode, isAppend, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean writeFileFromInternalAppDirectoryOperatingMode(Context context, InputStream inputStream, String firstLayerDirectoryName, String directoryPath
			, String fileName, int operatingMode){
		return writeFileFromInternalAppDirectoryOperatingMode(context, inputStream, firstLayerDirectoryName, directoryPath, fileName, operatingMode, false
				, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean writeFileFromInternalAppDirectoryPrivateMode(Context context, InputStream inputStream, String firstLayerDirectoryName, String directoryPath
			, String fileName, boolean isAppend){
		return writeFileFromInternalAppDirectoryOperatingMode(context, inputStream, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE, isAppend
				, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean writeFileFromInternalAppDirectoryPrivateMode(Context context, InputStream inputStream, String firstLayerDirectoryName, String directoryPath
			, String fileName){
		return writeFileFromInternalAppDirectoryOperatingMode(context, inputStream, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE, false
				, IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static byte[] readFileFromInternalAppDirectoryOperatingMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName
			, int operatingMode){
		File file = getReadFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, operatingMode);
		return fileToByteArray(file);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static byte[] readFileFromInternalAppDirectoryPrivateMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName){
		return readFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE);
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean deleteFileFromInternalAppDirectoryOperatingMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName
			, int operatingMode){
		File file = getReadFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, operatingMode);
		return file.delete();
	}

	/**
	 * The file path is located in [/data/data/packageName/(Prefix)app_ + directoryName/], file must have at least one level of directory.
	 */
	public static boolean deleteFileFromInternalAppDirectoryPrivateMode(Context context, String firstLayerDirectoryName, String directoryPath, String fileName){
		return deleteFileFromInternalAppDirectoryOperatingMode(context, firstLayerDirectoryName, directoryPath, fileName, Context.MODE_PRIVATE);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static @Nullable File getWriteFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return null;
		}
		// If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>.
		File file = context.getExternalFilesDir(type);
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static @Nullable File getReadFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName){
		String externalStorageState = Environment.getExternalStorageState();
		boolean externalMounted = externalStorageState.equals(Environment.MEDIA_MOUNTED) || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		if(!externalMounted){
			return null;
		}
		// If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>.
		File file = context.getExternalFilesDir(type);
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean writeFileFromExternalAppFilesDir(Context context, InputStream inputStream, String type, String directoryPath, String fileName, boolean isAppend
			, int bufferSize){
		File file = getWriteFileFromExternalAppFilesDir(context, type, directoryPath, fileName);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean writeFileFromExternalAppFilesDir(Context context, InputStream inputStream, String type, String directoryPath, String fileName, boolean isAppend){
		return writeFileFromExternalAppFilesDir(context, inputStream, type, directoryPath, fileName, isAppend, IO_BUFFER_SIZE);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean writeFileFromExternalAppFilesDir(Context context, InputStream inputStream, String type, String directoryPath, String fileName){
		return writeFileFromExternalAppFilesDir(context, inputStream, type, directoryPath, fileName, false, IO_BUFFER_SIZE);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean writeFileFromExternalAppFilesDir(Context context, InputStream inputStream, String type, String directoryPath, String fileName, boolean isAppend
			, int bufferSize, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		return writeFileFromExternalAppFilesDir(context, inputStream, type, directoryPath, fileName, isAppend, bufferSize);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static @Nullable byte[] readFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName){
		File file = getReadFileFromExternalAppFilesDir(context, type, directoryPath, fileName);
		return fileToByteArray(file);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static @Nullable byte[] readFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		return readFileFromExternalAppFilesDir(context, type, directoryPath, fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean deleteFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return false;
		}
		// If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>.
		File file = context.getExternalFilesDir(type);
		if(file == null){
			return false;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath + fileName);
		return file.delete();
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/files/type/].
	 */
	public static boolean deleteFileFromExternalAppFilesDir(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		return deleteFileFromExternalAppFilesDir(context, type, directoryPath, fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static @Nullable File getWriteFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return null;
		}
		// If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>.
		File file = context.getExternalCacheDir();
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static @Nullable File getReadFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName){
		String externalStorageState = Environment.getExternalStorageState();
		boolean externalMounted = externalStorageState.equals(Environment.MEDIA_MOUNTED) || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		if(!externalMounted){
			return null;
		}
		// If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>.
		File file = context.getExternalCacheDir();
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean writeFileFromExternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend
			, int bufferSize){
		File file = getWriteFileFromExternalAppCacheDir(context, directoryPath, fileName);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean writeFileFromExternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend){
		return writeFileFromExternalAppCacheDir(context, inputStream, directoryPath, fileName, isAppend, IO_BUFFER_SIZE);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean writeFileFromExternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName){
		return writeFileFromExternalAppCacheDir(context, inputStream, directoryPath, fileName, false, IO_BUFFER_SIZE);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean writeFileFromExternalAppCacheDir(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend
			, int bufferSize, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		return writeFileFromExternalAppCacheDir(context, inputStream, directoryPath, fileName, isAppend, bufferSize);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static @Nullable byte[] readFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName){
		File file = getReadFileFromExternalAppCacheDir(context, directoryPath, fileName);
		return fileToByteArray(file);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN < Build.VERSION_CODES.KITKAT, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static @Nullable byte[] readFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		return readFileFromExternalAppCacheDir(context, directoryPath, fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean deleteFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return false;
		}
		// If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>.
		File file = context.getExternalCacheDir();
		if(file == null){
			return false;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath + fileName);
		return file.delete();
	}

	/**
	 * If Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT, require android.permission.WRITE_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/Android/data/packageName/cache/].
	 */
	public static boolean deleteFileFromExternalAppCacheDir(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		return deleteFileFromExternalAppCacheDir(context, directoryPath, fileName);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static @Nullable File getWriteFileFromExternalPublic(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return null;
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStoragePublicDirectory(type);
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require android.permission.READ_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@SuppressLint("InlinedApi")
	@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
	public static @Nullable File getReadFileFromExternalPublic(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		String externalStorageState = Environment.getExternalStorageState();
		boolean externalMounted = externalStorageState.equals(Environment.MEDIA_MOUNTED) || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		if(!externalMounted){
			return null;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		// If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStoragePublicDirectory(type);
		if(file == null){
			return null;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalPublic(Context context, InputStream inputStream, String type, String directoryPath, String fileName, boolean isAppend
			, int bufferSize, Handler handlerNoPermissions){
		File file = getWriteFileFromExternalPublic(context, type, directoryPath, fileName, handlerNoPermissions);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalPublic(Context context, InputStream inputStream, String type, String directoryPath, String fileName, boolean isAppend
			, Handler handlerNoPermissions){
		return writeFileFromExternalPublic(context, inputStream, type, directoryPath, fileName, isAppend, IO_BUFFER_SIZE, handlerNoPermissions);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalPublic(Context context, InputStream inputStream, String type, String directoryPath, String fileName
			, Handler handlerNoPermissions){
		return writeFileFromExternalPublic(context, inputStream, type, directoryPath, fileName, false, IO_BUFFER_SIZE, handlerNoPermissions);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@SuppressLint("InlinedApi")
	@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
	public static @Nullable byte[] readFileFromExternalPublic(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		File file = getReadFileFromExternalPublic(context, type, directoryPath, fileName, handlerNoPermissions);
		return fileToByteArray(file);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/type/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean deleteFileFromExternalPublic(Context context, String type, String directoryPath, String fileName, Handler handlerNoPermissions){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return false;
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStoragePublicDirectory(type);
		if(file == null){
			return false;
		}
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath + fileName);
		return file.delete();
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static @Nullable File getWriteFileFromExternalStorage(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return null;
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStorageDirectory();
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath);
		if(!file.exists() && !file.mkdirs()){
			System.out.println("Directory create failed, path:\n" + file.getPath());
			return null;
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require android.permission.READ_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@SuppressLint("InlinedApi")
	@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
	public static @Nullable File getReadFileFromExternalStorage(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		String externalStorageState = Environment.getExternalStorageState();
		boolean externalMounted = externalStorageState.equals(Environment.MEDIA_MOUNTED) || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		if(!externalMounted){
			return null;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
				ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return null;
		}
		// If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStorageDirectory();
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		return new File(externalPath + directoryPath + fileName);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalStorage(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend, int bufferSize
			, Handler handlerNoPermissions){
		File file = getWriteFileFromExternalStorage(context, directoryPath, fileName, handlerNoPermissions);
		if(file == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, new FileOutputStream(file, isAppend), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalStorage(Context context, InputStream inputStream, String directoryPath, String fileName, boolean isAppend
			, Handler handlerNoPermissions){
		return writeFileFromExternalStorage(context, inputStream, directoryPath, fileName, isAppend, IO_BUFFER_SIZE, handlerNoPermissions);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean writeFileFromExternalStorage(Context context, InputStream inputStream, String directoryPath, String fileName, Handler handlerNoPermissions){
		return writeFileFromExternalStorage(context, inputStream, directoryPath, fileName, false, IO_BUFFER_SIZE, handlerNoPermissions);
	}

	/**
	 * If Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN, require android.permission.READ_EXTERNAL_STORAGE.<br>
	 * The file path is located in [/externalPath/].
	 */
	@SuppressLint("InlinedApi")
	@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
	public static @Nullable byte[] readFileFromExternalStorage(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		File file = getReadFileFromExternalStorage(context, directoryPath, fileName, handlerNoPermissions);
		return fileToByteArray(file);
	}

	/**
	 * android.permission.WRITE_EXTERNAL_STORAGE<br>
	 * The file path is located in [/externalPath/].
	 */
	@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	public static boolean deleteFileFromExternalStorage(Context context, String directoryPath, String fileName, Handler handlerNoPermissions){
		boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(!externalMounted){
			return false;
		}
		if(ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
			if(handlerNoPermissions != null){
				Message message = handlerNoPermissions.obtainMessage();
				message.obj = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
				handlerNoPermissions.sendMessage(message);
			}
			return false;
		}
		// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
		File file = Environment.getExternalStorageDirectory();
		String externalPath = file.toString();
		boolean isNamePrefixContainSeparator = fileName.charAt(0) == File.separatorChar;
		if(TextUtils.isEmpty(directoryPath)){
			directoryPath = isNamePrefixContainSeparator ? "" : File.separator;
		}else{
			if(directoryPath.charAt(0) != File.separatorChar){
				directoryPath = File.separator + directoryPath;
			}
			if(directoryPath.indexOf(externalPath) == 0){
				externalPath = "";
			}
			if(!isNamePrefixContainSeparator && directoryPath.charAt(directoryPath.length() - 1) != File.separatorChar){
				directoryPath = directoryPath + File.separator;
			}
		}
		file = new File(externalPath + directoryPath + fileName);
		return file.delete();
	}

	public static @Nullable Uri getInsertUriFromMediaStore(Context context, String directoryPath, String fileName, String intentType, boolean isPending){
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
		contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
		contentValues.put(MediaStore.MediaColumns.MIME_TYPE, intentType);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
			contentValues.put(MediaStore.MediaColumns.IS_PENDING, isPending ? 1 : 0);
			contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directoryPath);
		}else{
			contentValues.put(MediaStore.MediaColumns.DATA, directoryPath);
		}
		ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
		if(intentType.toLowerCase().startsWith("image")){
			return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
		}else if(intentType.toLowerCase().startsWith("audio")){
			return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
		}else if(intentType.toLowerCase().startsWith("video")){
			return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
		}else{
			return contentResolver.insert(Uri.parse("content://media/external/files"), contentValues);
		}
	}


	public static @Nullable Uri getQueryUriFromMediaStore(Context context, String directoryPath, String fileName, String intentType){
		String[] projection = new String[]{MediaStore.MediaColumns._ID};
		String selection;
		String[] selectionArgs;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
			selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? and " + MediaStore.MediaColumns.TITLE + "=?";
			selectionArgs = new String[]{directoryPath, fileName};
		}else{
			selection = MediaStore.MediaColumns.DATA + "=?";
			selectionArgs = new String[]{directoryPath + fileName};
		}
		Uri uri;
		ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
		if(intentType.toLowerCase().startsWith("image")){
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}else if(intentType.toLowerCase().startsWith("audio")){
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		}else if(intentType.toLowerCase().startsWith("video")){
			uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		}else{
			uri = Uri.parse("content://media/external/files");
		}
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
		if(cursor != null && cursor.moveToFirst()){
			uri = ContentUris.withAppendedId(uri, cursor.getLong(0));
			cursor.close();
			return uri;
		}
		return null;
	}

	public static int editUriFromMediaStore(Context context, Uri uri, String directoryPathNew, String fileNameNew, String intentTypeNew, boolean isPendingNew){
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameNew);
		contentValues.put(MediaStore.MediaColumns.TITLE, fileNameNew);
		contentValues.put(MediaStore.MediaColumns.MIME_TYPE, intentTypeNew);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
			contentValues.put(MediaStore.MediaColumns.IS_PENDING, isPendingNew ? 1 : 0);
			contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directoryPathNew);
		}else{
			contentValues.put(MediaStore.MediaColumns.DATA, directoryPathNew);
		}
		ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
		return contentResolver.update(uri, contentValues, null, null);
	}

	public static boolean insertUriFromMediaStore(Context context, InputStream inputStream, String directoryPath, String fileName, String intentType, boolean isPending){
		Uri uri = getInsertUriFromMediaStore(context, directoryPath, fileName, intentType, isPending);
		if(uri == null){
			return false;
		}
		try {
			return inputStreamWriteOutputStream(inputStream, context.getApplicationContext().getContentResolver().openOutputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeUriFromMediaStore(Context context, InputStream inputStream, String directoryPath, String fileName, String intentType){
		Uri uri = getQueryUriFromMediaStore(context, directoryPath, fileName, intentType);
		if(uri == null){
			return false;
		}
		try {
			ParcelFileDescriptor parcelFileDescriptor = context.getApplicationContext().getContentResolver().openFileDescriptor(uri, "w");
			if(parcelFileDescriptor != null){
				// Native Fd
				// parcelFileDescriptor.detachFd()
				inputStreamWriteOutputStream(inputStream, new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
				parcelFileDescriptor.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static @Nullable byte[] readUriFromMediaStore(Context context, String directoryPath, String fileName, String intentType){
		Uri uri = getQueryUriFromMediaStore(context, directoryPath, fileName, intentType);
		if(uri == null){
			return null;
		}
		try {
			ParcelFileDescriptor parcelFileDescriptor = context.getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
			if(parcelFileDescriptor != null){
				// Native Fd
				// parcelFileDescriptor.detachFd()
				byte[] bytes = inputStreamToByteArray(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
				parcelFileDescriptor.close();
				return bytes;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int deleteUriFromMediaStore(Context context, InputStream inputStream, String directoryPath, String fileName, String intentType){
		Uri uri = getQueryUriFromMediaStore(context, directoryPath, fileName, intentType);
		if(uri == null){
			return -1;
		}
		try {
			return context.getApplicationContext().getContentResolver().delete(uri, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * {@link MemoryFile#close()} Closes the memory file. If there are no other open references to the memory file, it will be deleted.
	 */
	public static boolean writeMemoryFile(MemoryFile memoryFile, InputStream inputStream, int bufferSize){
		return inputStreamWriteOutputStream(inputStream, memoryFile.getOutputStream(), bufferSize);
	}

	/**
	 * {@link MemoryFile#close()} Closes the memory file. If there are no other open references to the memory file, it will be deleted.
	 */
	public static boolean writeMemoryFile(MemoryFile memoryFile, InputStream inputStream){
		return inputStreamWriteOutputStream(inputStream, memoryFile.getOutputStream(), IO_BUFFER_SIZE);
	}

	/**
	 * {@link MemoryFile#close()} Closes the memory file. If there are no other open references to the memory file, it will be deleted.
	 */
	public static byte[] readMemoryFile(MemoryFile memoryFile, int bufferSize){
		return inputStreamToByteArray(memoryFile.getInputStream(), bufferSize);
	}

	/**
	 * {@link MemoryFile#close()} Closes the memory file. If there are no other open references to the memory file, it will be deleted.
	 */
	public static byte[] readMemoryFile(MemoryFile memoryFile){
		return inputStreamToByteArray(memoryFile.getInputStream(), IO_BUFFER_SIZE);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static synchronized void changeSharedPreferences(SharedPreferences sp, SharedPreferences.Editor spEdit, String key, Object value, boolean toggleMode){
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
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static synchronized boolean writeSharedPreferencesCommitSync(SharedPreferences sp, String key, Object value, boolean toggleMode){
		SharedPreferences.Editor spEdit = sp.edit();
		changeSharedPreferences(sp, spEdit, key, value, toggleMode);
		return spEdit.commit();
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean writeSharedPreferencesCommitSync(Context context, String spName, String key, Object value, boolean toggleMode){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return writeSharedPreferencesCommitSync(sp, key, value, toggleMode);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean writeSharedPreferencesCommitSync(SharedPreferences sp, String key, Object value){
		return writeSharedPreferencesCommitSync(sp, key, value, false);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean writeSharedPreferencesCommitSync(Context context, String spName, String key, Object value){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return writeSharedPreferencesCommitSync(sp, key, value, false);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static synchronized void writeSharedPreferencesApplySync(SharedPreferences sp, String key, Object value, boolean toggleMode){
		SharedPreferences.Editor spEdit = sp.edit();
		changeSharedPreferences(sp, spEdit, key, value, toggleMode);
		spEdit.apply();
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesApplySync(Context context, String spName, String key, Object value, boolean toggleMode){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesApplySync(sp, key, value, toggleMode);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesApplySync(SharedPreferences sp, String key, Object value){
		writeSharedPreferencesApplySync(sp, key, value, false);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesApplySync(Context context, String spName, String key, Object value){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesApplySync(sp, key, value, false);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesCommitAsync(final SharedPreferences sp, final String key, final Object value, final boolean toggleMode
			, final Handler handler){
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSuccess = writeSharedPreferencesCommitSync(sp, key, value, toggleMode);
				if(handler != null){
					Message msg = Message.obtain();
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

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesCommitAsync(Context context, String spName, String key, Object value, boolean toggleMode, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesCommitAsync(sp, key, value, toggleMode, handler);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void writeSharedPreferencesCommitAsync(SharedPreferences sp, String key, Object value, Handler handler){
		writeSharedPreferencesCommitAsync(sp, key, value, false, handler);
	}

	public static void writeSharedPreferencesCommitAsync(Context context, String spName, String key, Object value, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesCommitAsync(sp, key, value, false, handler);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean removeSharedPreferencesCommitSync(SharedPreferences sp, String key){
		return writeSharedPreferencesCommitSync(sp, key, null, true);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean removeSharedPreferencesCommitSync(Context context, String spName, String key){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return writeSharedPreferencesCommitSync(sp, key, null, true);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void removeSharedPreferencesCommitAsync(SharedPreferences sp, String key, Handler handler){
		writeSharedPreferencesCommitAsync(sp, key, null, true, handler);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static void removeSharedPreferencesCommitAsync(Context context, String spName, String key, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesCommitAsync(sp, key, null, true, handler);
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean clearAndDeleteSharedPreferencesFile(Context context, String spName){
		boolean isCommit = context.getSharedPreferences(spName, Context.MODE_PRIVATE).edit().clear().commit();
		return new File(context.getFilesDir().getParent() + "/shared_prefs/" + spName + ".xml").delete() && isCommit;
	}

	/**
	 * The file path is located in [/data/data/packageName/shared_prefs/].
	 */
	public static boolean clearAndDeleteAllSharedPreferencesFile(Context context){
		File fileDir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
		String[] filePaths = fileDir.list();
		if(filePaths == null){
			return false;
		}
		String fileName;
		SharedPreferences.Editor spEdit;
		File file;
		Boolean isDeleteAll = null;
		for(int i=0; i<filePaths.length; i++){
			if(filePaths[i].lastIndexOf(".xml") < filePaths[i].length()){
				fileName = filePaths[i].substring(0, filePaths[i].lastIndexOf(".xml"));
				spEdit = context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();
				if(spEdit.clear().commit()){
					isDeleteAll = isDeleteAll == null ? true : isDeleteAll;
				}else{
					isDeleteAll = false;
					System.out.println("Commit shared preferences failed, file name:\n" + fileName);
				}
			}
			file = new File(fileDir.getPath() + filePaths[i]);
			if(file.delete()){
				isDeleteAll = isDeleteAll == null ? true : isDeleteAll;
			}else{
				isDeleteAll = false;
				System.out.println("Delete file failed, path:\n" + file.getPath());
			}
		}
		return isDeleteAll != null && isDeleteAll;
	}

	private static synchronized void changeSharedPreferencesMap(SharedPreferences sp, SharedPreferences.Editor spEdit, String mapSaveKey, Map<String, String> map
			, boolean toggleMode){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spOldKey = sp.getString(spMapHeadKey, "");
		Set<String> setOldKey = new HashSet<String>();
		if(!TextUtils.isEmpty(spOldKey)){
			String[] spOldKeyArray = spOldKey.split(SP_MAP_DELIMITER);
			Collections.addAll(setOldKey, spOldKeyArray);
		}

		Iterator<Map.Entry<String, String>> iteratorEntry;
		Map.Entry<String, String> entry;
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
						spNewKey = Utils.getStringSymbolCombine(spNewKey, entry.getKey(), SP_MAP_DELIMITER, false);
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

	public static synchronized boolean writeSharedPreferencesMapCommitSync(SharedPreferences sp, String mapSaveKey, Map<String, String> map, boolean toggleMode){
		SharedPreferences.Editor spEdit = sp.edit();
		changeSharedPreferencesMap(sp, spEdit, mapSaveKey, map, toggleMode);
		return spEdit.commit();
	}

	public static boolean writeSharedPreferencesMapCommitSync(Context context, String spName, String mapSaveKey, Map<String, String> map
			, boolean toggleMode){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return writeSharedPreferencesMapCommitSync(sp, mapSaveKey, map, toggleMode);
	}

	public static boolean writeSharedPreferencesMapCommitSync(SharedPreferences sp, String mapSaveKey, Map<String, String> map){
		return writeSharedPreferencesMapCommitSync(sp, mapSaveKey, map, false);
	}

	public static boolean writeSharedPreferencesMapCommitSync(Context context, String spName, String mapSaveKey, Map<String, String> map){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return writeSharedPreferencesMapCommitSync(sp, mapSaveKey, map, false);
	}

	public static void writeSharedPreferencesMapCommitAsync(final SharedPreferences sp, final String mapSaveKey, final Map<String, String> map
			, final boolean toggleMode, final Handler handler){
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSuccess = writeSharedPreferencesMapCommitSync(sp, mapSaveKey, map, toggleMode);
				if(handler != null){
					Message msg = Message.obtain();
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

	public static void writeSharedPreferencesMapCommitAsync(Context context, String spName, String mapSaveKey, Map<String, String> map, boolean toggleMode
			, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesMapCommitAsync(sp, mapSaveKey, map, toggleMode, handler);
	}

	public static void writeSharedPreferencesMapCommitAsync(SharedPreferences sp, String mapSaveKey, Map<String, String> map, Handler handler){
		writeSharedPreferencesMapCommitAsync(sp, mapSaveKey, map, false, handler);
	}

	public static void writeSharedPreferencesMapCommitAsync(Context context, String spName, String mapSaveKey, Map<String, String> map, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		writeSharedPreferencesMapCommitAsync(sp, mapSaveKey, map, false, handler);
	}

	private static void removeSharedPreferencesMap(SharedPreferences sp, SharedPreferences.Editor spEdit, String mapSaveKey){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spKey = sp.getString(spMapHeadKey, "");
		if(!TextUtils.isEmpty(spKey)){
			String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
			for(int i=0; i<spKeyArray.length; i++){
				spEdit.remove(spMapHeadKey + spKeyArray[i]);
			}
		}
		spEdit.remove(spMapHeadKey);
	}

	public static synchronized boolean removeSharedPreferencesMapCommitSync(SharedPreferences sp, String mapSaveKey){
		SharedPreferences.Editor spEdit = sp.edit();
		removeSharedPreferencesMap(sp, spEdit, mapSaveKey);
		return spEdit.commit();
	}

	public static boolean removeSharedPreferencesMapCommitSync(Context context, String spName, String mapSaveKey){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return removeSharedPreferencesMapCommitSync(sp, mapSaveKey);
	}

	public static void removeSharedPreferencesMapCommitAsync(final SharedPreferences sp, final String mapSaveKey, final Handler handler){
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSuccess = removeSharedPreferencesMapCommitSync(sp, mapSaveKey);
				if(handler != null){
					Message msg = Message.obtain();
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

	public static void removeSharedPreferencesMapCommitAsync(Context context, String spName, String mapSaveKey, Handler handler){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		removeSharedPreferencesMapCommitAsync(sp, mapSaveKey, handler);
	}

	public static Map<String, String> readSharedPreferencesMapSync(SharedPreferences sp, String mapSaveKey){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spKey = sp.getString(spMapHeadKey, "");
		if(TextUtils.isEmpty(spKey)){
			return new HashMap<String, String>();
		}

		String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
		String spValue;
		Map<String, String> map = new HashMap<String, String>(spKeyArray.length);
		for(int i=0; i<spKeyArray.length; i++){
			if(sp.contains(spMapHeadKey + spKeyArray[i])){
				spValue = sp.getString(spMapHeadKey + spKeyArray[i], "");
				map.put(spKeyArray[i], spValue);
			}
		}
		return map;
	}

	public static Map<String, String> readSharedPreferencesMapSync(Context context, String spName, String mapSaveKey){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return readSharedPreferencesMapSync(sp, mapSaveKey);
	}

	public static String readSharedPreferencesMapInItemSync(SharedPreferences sp, String mapSaveKey, int location){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spKey = sp.getString(spMapHeadKey, "");
		if(TextUtils.isEmpty(spKey)){
			return null;
		}

		String[] spKeyArray = spKey.split(SP_MAP_DELIMITER);
		return sp.getString(spMapHeadKey + spKeyArray[location], null);
	}

	public static String readSharedPreferencesMapInItemSync(Context context, String spName, String mapSaveKey, int location){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return readSharedPreferencesMapInItemSync(sp, mapSaveKey, location);
	}

	public static String readSharedPreferencesMapInItemSync(SharedPreferences sp, String mapSaveKey, String mapSaveItemKey){
		final String spMapHeadKey = SP_MAP_HEAD + mapSaveKey;

		String spKey = sp.getString(spMapHeadKey, "");
		if(TextUtils.isEmpty(spKey)){
			return null;
		}

		return sp.getString(spMapHeadKey + mapSaveItemKey, null);
	}

	
	public static String readSharedPreferencesMapInItemSync(Context context, String spName, String mapSaveKey, String mapSaveItemKey){
		SharedPreferences sp = context.getApplicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
		return readSharedPreferencesMapInItemSync(sp, mapSaveKey, mapSaveItemKey);
	}
}