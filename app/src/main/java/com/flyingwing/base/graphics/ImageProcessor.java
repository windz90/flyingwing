/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.5.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.graphics;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings({"unused", "UnnecessaryLocalVariable", "ForLoopReplaceableByForEach", "Convert2Diamond", "TryFinallyCanBeTryWithResources", "UnusedAssignment"})
public class ImageProcessor {

	public static final int LOAD_RESULT_FAIL = -1;
	public static final int LOAD_RESULT_REMOTE = 1;
	public static final int LOAD_RESULT_LOCAL = 2;
	public static final int LOAD_RESULT_CACHE = 3;
	
	private static final ImageSetting IMAGESETTING = new ImageSetting();
	private static final ThreadPoolExecutor EXECUTOR_SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(IMAGESETTING.mThreadPoolSum);
	private static final Map<String, SoftReference<Bitmap>> BUFFER_MAP = new HashMap<String, SoftReference<Bitmap>>();
	private static final Map<String, String> SAMPLE_MAP = new HashMap<String, String>();
	private static final String SAMPLE_WORD = "_SampleSize";
	
	public interface OnLoadImageListener {
		void onLoadImageComplete(int loadResultFlag, String streamURL, Bitmap bitmap);
	}
	
	public static class ImageSetting {
		private Bitmap.Config mBitmapConfig = Bitmap.Config.ARGB_8888;
		private boolean mInNativeAlloc = true;
		private boolean mIsPrintLoadException = true;
		private int mBufferSize = 1024 * 16;
		private int mThreadPoolSum = 3;
	}
	
	public static void setBitmapConfig(Bitmap.Config bitmapConfig){
		ImageProcessor.IMAGESETTING.mBitmapConfig = bitmapConfig;
	}
	
	public static Bitmap.Config getBitmapConfig(){
		return IMAGESETTING.mBitmapConfig;
	}
	
	public static void setInNativeAlloc(boolean inNativeAlloc){
		ImageProcessor.IMAGESETTING.mInNativeAlloc = inNativeAlloc;
	}
	
	public static boolean getInNativeAlloc(){
		return IMAGESETTING.mInNativeAlloc;
	}
	
	public static void setInputBufferSize(int inputBufferSize){
		ImageProcessor.IMAGESETTING.mBufferSize = inputBufferSize;
	}
	
	public static int getInputBufferSize(){
		return IMAGESETTING.mBufferSize;
	}
	
	public static void setThreadPoolSum(int threadPoolSum){
		ImageProcessor.IMAGESETTING.mThreadPoolSum = threadPoolSum;
	}
	
	public static int getThreadPoolSum(){
		return IMAGESETTING.mThreadPoolSum;
	}
	
	public static void setPrintLoadStreamException(boolean isPrintLoadStreamException){
		ImageProcessor.IMAGESETTING.mIsPrintLoadException = isPrintLoadStreamException;
	}
	
	public static boolean isPrintLoadStreamException(){
		return IMAGESETTING.mIsPrintLoadException;
	}
	
	public static BlockingQueue<Runnable> getThreadPoolExecutorQueue(){
		return EXECUTOR_SERVICE.getQueue();
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, int inSampleSize, boolean isUseBuffer){
		// 讀取APK中/res/raw/底下被記錄在R.java的資源
		// 此目錄僅支援讀取1MB以下的檔案
		Bitmap bitmap;
		if(isUseBuffer){
			bitmap = getBufferBitmap("Raw" + File.separator + resource, SAMPLE_WORD, inSampleSize);
			if(bitmap != null){
				return bitmap;
			}
		}
		try {
			InputStream is = res.openRawResource(resource);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, "Raw" + File.separator + resource, SAMPLE_WORD, inSampleSize);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			bitmap = null;
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, int inSampleSize){
		return getRawBitmap(res, resource, inSampleSize, true);
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, float specifiedSize, boolean isUseBuffer){
		int scale = 1;
		try {
			InputStream is = res.openRawResource(resource);
			try {
				scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getRawBitmap(res, resource, scale, isUseBuffer);
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, float specifiedSize){
		return getRawBitmap(res, resource, specifiedSize, true);
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, int inSampleSize, boolean isUseBuffer){
		// 讀取APK中/assets/底下的檔案
		// 此目錄僅支援讀取1MB以下的檔案
		Bitmap bitmap;
		if(isUseBuffer){
			bitmap = getBufferBitmap("Assets" + File.separator + imageName, SAMPLE_WORD, inSampleSize);
			if(bitmap != null){
				return bitmap;
			}
		}
		AssetManager assetManager = context.getAssets();
		try {
			InputStream is = assetManager.open(imageName);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, "Assets" + File.separator + imageName, SAMPLE_WORD, inSampleSize);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			assetManager = null;
			bitmap = null;
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, int inSampleSize){
		return readAssetsBitmap(context, imageName, inSampleSize, true);
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, float specifiedSize, boolean isUseBuffer){
		AssetManager assetManager = context.getAssets();
		int scale = 1;
		try {
			InputStream is = assetManager.open(imageName);
			try {
				scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readAssetsBitmap(context, imageName, scale, isUseBuffer);
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, float specifiedSize){
		return readAssetsBitmap(context, imageName, specifiedSize, true);
	}
	
	public static void writeInsidePrivateImage(Context context, Bitmap bitmap, int quality, String imageName){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		try {
			FileOutputStream fileOutStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
				fileOutStream.flush();
			} finally {
				fileOutStream.close();
				fileOutStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, int inSampleSize, boolean isUseBuffer){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		Bitmap bitmap;
		if(isUseBuffer){
			bitmap = getBufferBitmap(imageName, SAMPLE_WORD, inSampleSize);
			if(bitmap != null){
				return bitmap;
			}
		}
		try {
			InputStream is = context.openFileInput(imageName);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, imageName, SAMPLE_WORD, inSampleSize);
			}
			return bitmap;
		} catch (Exception | OutOfMemoryError ignored) {}
		return null;
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, int inSampleSize){
		return readInsidePrivateImage(context, imageName, inSampleSize, true);
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, float specifiedSize, boolean isUseBuffer){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		int scale = 1;
		try {
			InputStream is = context.openFileInput(imageName);
			try {
				scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readInsidePrivateImage(context, imageName, scale, isUseBuffer);
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, float specifiedSize){
		return readInsidePrivateImage(context, imageName, specifiedSize, true);
	}
	
	public static boolean deleteInsidePrivateImage(Context context, String imageName){
		// 使用context.deleteFile處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		deleteBufferBitmap(imageName, SAMPLE_WORD);
		return context.deleteFile(imageName);
	}
	
	public static void writeInsideImageFile(Context context, int mode, Bitmap bitmap, int quality, String rootFolderName, String path, String imageName){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		File file = new File(insidePath + path);
		if(!file.exists()){
			if(!file.mkdirs()){
				System.out.println("directory already existed");
			}
		}
		file = new File(insidePath + path + imageName);
		try {
			FileOutputStream fileOutStream = new FileOutputStream(file, false);
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
				fileOutStream.flush();
			} finally {
				fileOutStream.close();
				fileOutStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, int inSampleSize
			, boolean isUseBuffer){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		Bitmap bitmap;
		if(isUseBuffer){
			bitmap = getBufferBitmap(insidePath + path + imageName, SAMPLE_WORD, inSampleSize);
			if(bitmap != null){
				return bitmap;
			}
		}
		File file = new File(insidePath + path + imageName);
		if(!file.isFile()){
			return null;
		}
		try {
			InputStream is = new FileInputStream(file);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, inSampleSize);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			file = null;
			bitmap = null;
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, int inSampleSize){
		return readInsideImageFile(context, mode, rootFolderName, path, imageName, inSampleSize, true);
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, float specifiedSize
			, boolean isUseBuffer){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		File file = new File(insidePath + path + imageName);
		if(!file.isFile()){
			return null;
		}
		InputStream is;
		int scale = 1;
		try {
			is = new FileInputStream(file);
			try {
				scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bitmap bitmap;
		try {
			if(isUseBuffer){
				bitmap = getBufferBitmap(insidePath + path + imageName, SAMPLE_WORD, scale);
				if(bitmap != null){
					return bitmap;
				}
			}
			
			is = new FileInputStream(file);
			try {
				bitmap = getAgileBitmap(is, scale);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, scale);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			file = null;
			is = null;
			bitmap = null;
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, float specifiedSize){
		return readInsideImageFile(context, mode, rootFolderName, path, imageName, specifiedSize, true);
	}
	
	public static boolean deleteInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		deleteBufferBitmap(insidePath + path + imageName, SAMPLE_WORD);
		File file = new File(insidePath + path + imageName);
		return file.delete();
	}
	
	public static void writeSDCardImageFile(Bitmap bitmap, int quality, String directory, String imageName){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
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
			file = new File(sdCardPath + directory + imageName);
			try {
				FileOutputStream fileOutStream = new FileOutputStream(file, false);
				try {
					bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
					fileOutStream.flush();
				} finally {
					fileOutStream.close();
					fileOutStream = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, int inSampleSize, boolean isUseBuffer){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			Bitmap bitmap;
			if(isUseBuffer){
				bitmap = getBufferBitmap(sdCardPath + directory + imageName, SAMPLE_WORD, inSampleSize);
				if(bitmap != null){
					return bitmap;
				}
			}
			file = new File(sdCardPath + directory + imageName);
			if(!file.isFile()){
				return null;
			}
			try {
				InputStream is = new FileInputStream(file);
				try {
					bitmap = getAgileBitmap(is, inSampleSize);
				} finally {
					is.close();
					is = null;
				}
				if(isUseBuffer){
					setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, inSampleSize);
				}
				return bitmap;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				file = null;
				bitmap = null;
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, int inSampleSize){
		return readSDCardImageFile(directory, imageName, inSampleSize, true);
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, float specifiedSize, boolean isUseBuffer){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			file = new File(sdCardPath + directory + imageName);
			if(!file.isFile()){
				return null;
			}
			InputStream is;
			int scale = 1;
			try {
				is = new FileInputStream(file);
				try {
					scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
				} finally {
					is.close();
					is = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Bitmap bitmap;
			try {
				if(isUseBuffer){
					bitmap = getBufferBitmap(sdCardPath + directory + imageName, SAMPLE_WORD, scale);
					if(bitmap != null){
						return bitmap;
					}
				}
				
				is = new FileInputStream(file);
				try {
					bitmap = getAgileBitmap(is, scale);
				} finally {
					is.close();
					is = null;
				}
				if(isUseBuffer){
					setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, scale);
				}
				return bitmap;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				file = null;
				is = null;
				bitmap = null;
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, float specifiedSize){
		return readSDCardImageFile(directory, imageName, specifiedSize, true);
	}
	
	public static boolean deleteSDCardImageFile(Context context, String directory, String imageName){
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			deleteBufferBitmap(sdCardPath + directory + imageName, SAMPLE_WORD);
			file = new File(sdCardPath + directory + imageName);
			return file.delete();
		}
		return false;
	}
	
	public static void writeImageFile(File file, Bitmap bitmap, int quality){
		try {
			FileOutputStream fileOutStream = new FileOutputStream(file, false);
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
				fileOutStream.flush();
			} finally {
				fileOutStream.close();
				fileOutStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeImageFile(String filePath, Bitmap bitmap, int quality){
		writeImageFile(new File(filePath), bitmap, quality);
	}
	
	public static Bitmap readImageFile(File file, int inSampleSize, boolean isUseBuffer){
		if(!file.isFile()){
			return null;
		}
		Bitmap bitmap;
		try {
			if(isUseBuffer){
				bitmap = getBufferBitmap(file.toString(), SAMPLE_WORD, inSampleSize);
				if(bitmap != null){
					return bitmap;
				}
			}
			
			InputStream is = new FileInputStream(file);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			if(isUseBuffer){
				setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, inSampleSize);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			file = null;
			bitmap = null;
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap readImageFile(String filePath, int inSampleSize, boolean isUseBuffer){
		return readImageFile(new File(filePath), inSampleSize, isUseBuffer);
	}
	
	public static Bitmap readImageFile(File file, int inSampleSize){
		return readImageFile(file, inSampleSize, true);
	}
	
	public static Bitmap readImageFile(String filePath, int inSampleSize){
		return readImageFile(new File(filePath), inSampleSize, true);
	}
	
	public static Bitmap readImageFile(File file, float specifiedSize, boolean isUseBuffer){
		int scale = 1;
		try {
			InputStream is = new FileInputStream(file);
			try {
				scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readImageFile(file, scale, isUseBuffer);
	}
	
	public static Bitmap readImageFile(String filePath, float specifiedSize, boolean isUseBuffer){
		return readImageFile(new File(filePath), specifiedSize, isUseBuffer);
	}
	
	public static Bitmap readImageFile(File file, float specifiedSize){
		return readImageFile(file, specifiedSize, true);
	}
	
	public static Bitmap readImageFile(String filePath, float specifiedSize){
		return readImageFile(new File(filePath), specifiedSize, true);
	}
	
	public static byte[] loadStream(String streamURL) throws IOException{
		streamURL = streamURL.replace(" ", "%20");
		streamURL = streamURL.replace("\\(", "%28");
		streamURL = streamURL.replace("\\)", "%29");
		URL url = new URL(streamURL);
		InputStream inputStream = url.openStream();
		byte[] bytes = inputStreamToByteArray(inputStream, IMAGESETTING.mBufferSize);
		inputStream.close();
		return bytes;
	}
	
	public static boolean isAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
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
		} catch (Exception e) {
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
		return inputStreamToByteArray(is, IMAGESETTING.mBufferSize);
	}
	
	public static void setBufferBitmap(Bitmap bitmap, String path, String sampleWord, int inSampleSize){
		if(path != null && path.trim().length() > 0){
			BUFFER_MAP.put(path + sampleWord + inSampleSize, new SoftReference<Bitmap>(bitmap));
			if(SAMPLE_MAP.get(path) == null || SAMPLE_MAP.get(path).length() == 0){
				SAMPLE_MAP.put(path, "" + inSampleSize);
			}else if(!isMatchBufferSample(path, sampleWord, inSampleSize)){
				SAMPLE_MAP.put(path, SAMPLE_MAP.get(path) + "," + inSampleSize);
			}
		}
	}
	
	public static Bitmap getBufferBitmap(String path, String sampleWord, int inSampleSize){
		if(path != null && path.trim().length() > 0){
			if(inSampleSize != -1 && BUFFER_MAP.containsKey(path + sampleWord + inSampleSize)){
				// 取得Map暫存於記憶體中的圖片
				SoftReference<Bitmap> softReference = BUFFER_MAP.get(path + sampleWord + inSampleSize);
				if(softReference != null){
					return softReference.get();
				}
			}
		}
		return null;
	}
	
	public static void deleteBufferBitmap(String path, String sampleWord){
		if(path != null && path.trim().length() > 0){
			String sampleStr = SAMPLE_MAP.get(path);
			SAMPLE_MAP.remove(path);
			if(sampleStr != null && sampleStr.trim().length() > 0){
				String[] sampleArray = sampleStr.split(",");
				for(int i=0; i<sampleArray.length; i++){
					BUFFER_MAP.remove(path + sampleWord + sampleArray[i]);
				}
			}
		}
	}
	
	public static void deleteBufferBitmap(String path){
		deleteBufferBitmap(path, SAMPLE_WORD);
	}
	
	public static boolean isMatchBufferSample(String path, String sampleWord, int inSampleSize){
		String sampleStr = SAMPLE_MAP.get(path);
		if(sampleStr != null && sampleStr.trim().length() > 0){
			String[] sampleArray = sampleStr.split(",");
			for(int i=0; i<sampleArray.length; i++){
				// 尋找符合的採樣值
				if(sampleArray[i].equals("" + inSampleSize)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static int getBufferSample(String path, String sampleWord, float specifiedSize){
		if(path == null || path.trim().length() == 0){
			return -1;
		}
		String sampleStr = SAMPLE_MAP.get(path);
		if(sampleStr == null || sampleStr.trim().length() == 0){
			return -1;
		}
		
		String[] sampleArray = sampleStr.split(",");
		int width, height;
		for(int i=0; i<sampleArray.length; i++){
			int bufferSample = Integer.parseInt(sampleArray[i]);
			SoftReference<Bitmap> softReference = BUFFER_MAP.get(path + sampleWord + bufferSample);
			if(softReference == null){
				continue;
			}
			Bitmap bitmap = softReference.get();
			if(bitmap == null){
				continue;
			}
			
			width = bitmap.getWidth() * bufferSample;
			height = bitmap.getHeight() * bufferSample;
			return getImageSpecifiedSizeNarrowScale(new float[]{width, height}, specifiedSize);
		}
		return -1;
	}
	
	public static Bitmap getImageAsync(final Context context, final String streamURL, final float specifiedSize, final int quality
			, final String imageName, final ThreadPoolExecutor threadPoolExecutor, final OnLoadImageListener onLoadImageListener){
		if(streamURL == null || streamURL.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onLoadImageComplete(LOAD_RESULT_FAIL, streamURL, null);
			}
			return null;
		}
		
		final Handler handlerDownload = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(msg.what == 0 && onLoadImageListener != null){
					onLoadImageListener.onLoadImageComplete(LOAD_RESULT_FAIL, streamURL, null);
				}else if(msg.what == 1 && onLoadImageListener != null){
					onLoadImageListener.onLoadImageComplete(LOAD_RESULT_REMOTE, streamURL, (Bitmap)msg.obj);
				}
				return false;
			}
		});
		final Runnable runnableDownload = new Runnable() {
			
			@Override
			public void run() {
				Bitmap bitmap = null;
				try {
					byte[] byteArray = loadStream(streamURL);
					InputStream is = new ByteArrayInputStream(byteArray);
					int scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
					try {
						is.reset();
					} catch (IOException e1) {
						try {
							is.close();
							is = null;
						} catch (Exception ignored) {}
						is = new ByteArrayInputStream(byteArray);
						e1.printStackTrace();
					}
					try {
						bitmap = getAgileBitmap(is, scale);
					} finally {
						is.close();
						is = null;
					}
					
					if(imageName != null){
						String imageNameCopy = imageName.trim().replace(File.separator, "_");
						if(imageNameCopy.length() > 0){
							// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
							setBufferBitmap(bitmap, imageNameCopy, SAMPLE_WORD, scale);
							// 將下載的圖片儲存於本地端
							writeInsidePrivateImage(context, bitmap, quality, imageNameCopy);
						}
					}
				} catch (Exception e) {
					if(IMAGESETTING.mIsPrintLoadException){e.printStackTrace();}
				}
				
				if(bitmap != null){
					Message msg = new Message();
					msg.what = 1;
					msg.obj = bitmap;
					handlerDownload.sendMessage(msg);
					return;
				}
				handlerDownload.sendEmptyMessage(0);
			}
		};
		
		Handler handlerNone = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(isAvailable(context)){
					if(threadPoolExecutor == null){
						EXECUTOR_SERVICE.submit(runnableDownload);
					}else{
						threadPoolExecutor.submit(runnableDownload);
					}
				}
				return false;
			}
		});
		return getImageAsyncLocalOnly(context, streamURL, specifiedSize, imageName, onLoadImageListener, handlerNone);
	}
	
	public static Bitmap getImageAsync(final Context context, final String streamURL, final float specifiedSize, final int quality
			, final String imageName, final OnLoadImageListener onLoadImageListener){
		return getImageAsync(context, streamURL, specifiedSize, quality, imageName, null, onLoadImageListener);
	}
	
	public static Bitmap getImageAsyncLocalOnly(final Context context, final String streamURL, final float specifiedSize, String imageName
			, final OnLoadImageListener onLoadImageListener, final Handler handlerNone){
		if(streamURL == null || streamURL.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onLoadImageComplete(LOAD_RESULT_FAIL, streamURL, null);
			}
			return null;
		}
		if(imageName == null){
			handlerNone.sendEmptyMessage(0);
			return null;
		}
		final String imageNameCopy = imageName.trim().replace(File.separator, "_");
		if(imageNameCopy.length() == 0){
			handlerNone.sendEmptyMessage(0);
			return null;
		}
		
		Bitmap bitmap = getBufferBitmap(imageNameCopy, SAMPLE_WORD, getBufferSample(imageNameCopy, SAMPLE_WORD, specifiedSize));
		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
		if(bitmap != null){
			if(onLoadImageListener != null){
				onLoadImageListener.onLoadImageComplete(LOAD_RESULT_CACHE, streamURL, bitmap);
			}
			return bitmap;
		}
		
		final Handler handlerRead = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(onLoadImageListener != null){
					onLoadImageListener.onLoadImageComplete(LOAD_RESULT_LOCAL, streamURL, (Bitmap) msg.obj);
				}
				return false;
			}
		});
		Thread threadRead = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Bitmap bitmap = null;
				// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
				try{
					InputStream is = context.openFileInput(imageNameCopy);
					int scale;
					try {
						scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
					} finally {
						is.close();
						is = null;
					}
					is = context.openFileInput(imageNameCopy);
					try {
						bitmap = getAgileBitmap(is, scale);
					} finally {
						is.close();
						is = null;
					}
					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, imageNameCopy, SAMPLE_WORD, scale);
				} catch (Exception e) {
					if(IMAGESETTING.mIsPrintLoadException){e.printStackTrace();}
				}
				
				// 若此圖片已儲存在本地端，則回傳圖片
				if(bitmap != null){
					Message msg = new Message();
					msg.obj = bitmap;
					handlerRead.sendMessage(msg);
					return;
				}
				handlerNone.sendEmptyMessage(0);
			}
		});
		threadRead.start();
		return null;
	}
	
	public static Bitmap getImageAsyncRemoteOnly(Context context, final String streamURL, final float specifiedSize, final ThreadPoolExecutor threadPoolExecutor
			, final OnLoadImageListener onLoadImageListener){
		if(streamURL == null || streamURL.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onLoadImageComplete(LOAD_RESULT_FAIL, streamURL, null);
			}
			return null;
		}
		
		final Bitmap bitmap = getBufferBitmap(streamURL, SAMPLE_WORD, getBufferSample(streamURL, SAMPLE_WORD, specifiedSize));
		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
        if(bitmap != null){
			if(onLoadImageListener != null){
				onLoadImageListener.onLoadImageComplete(LOAD_RESULT_CACHE, streamURL, bitmap);
			}
            return bitmap;
        }
		
		final Handler handlerDownload = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(msg.what == 0 && onLoadImageListener != null){
					onLoadImageListener.onLoadImageComplete(LOAD_RESULT_FAIL, streamURL, null);
				}else if(msg.what == 1 && onLoadImageListener != null){
					onLoadImageListener.onLoadImageComplete(LOAD_RESULT_REMOTE, streamURL, (Bitmap)msg.obj);
				}
				return false;
			}
		});
		Runnable runnableDownload = new Runnable() {
			
			@Override
			public void run() {
				Bitmap bitmap = null;
				try {
					byte[] byteArray = loadStream(streamURL);
					InputStream is = new ByteArrayInputStream(byteArray);
					int scale = getImageSpecifiedSizeNarrowScale(is, specifiedSize);
					try {
						is.reset();
					} catch (IOException e1) {
						try {
							is.close();
							is = null;
						} catch (IOException ignored) {}
						is = new ByteArrayInputStream(byteArray);
						e1.printStackTrace();
					}
					try {
						bitmap = getAgileBitmap(is, scale);
					} finally {
						is.close();
						is = null;
					}
					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, streamURL, SAMPLE_WORD, scale);
				} catch (Exception e) {
					if(IMAGESETTING.mIsPrintLoadException){e.printStackTrace();}
				}
				
				if(bitmap != null){
					Message msg = new Message();
					msg.what = 1;
					msg.obj = bitmap;
					handlerDownload.sendMessage(msg);
					return;
				}
				handlerDownload.sendEmptyMessage(0);
			}
		};
		
		if(isAvailable(context)){
			if(threadPoolExecutor == null){
				EXECUTOR_SERVICE.submit(runnableDownload);
			}else{
				threadPoolExecutor.submit(runnableDownload);
			}
		}
		return null;
	}
	
	public static Bitmap getImageAsyncRemoteOnly(Context context, final String streamURL, final float specifiedSize, final OnLoadImageListener onLoadImageListener){
		return getImageAsyncRemoteOnly(context, streamURL, specifiedSize, null, onLoadImageListener);
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Bitmap getAgileBitmap(InputStream is, int inSampleSize){
		// 圖片設定
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 設定是否讓BitmapFactory.Options只讀取圖片寬高資料而不實際將圖片載入Bitmap
		options.inJustDecodeBounds = false;
		// 設定匯入後圖片的寬高縮小比例，預設1為原始寬高
		options.inSampleSize = inSampleSize;
		// 設定圖片ARGB屬性佔用記憶體空間，預設Bitmap.Config.ARGB_8888為各佔8Bit
		options.inPreferredConfig = IMAGESETTING.mBitmapConfig;
		// SDK 11 開始可設定是否讓圖片內容允許變動
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			options.inMutable = true;
		}
		// 直接把不使用的記憶體歸給JVM，回收動作不佔用JVM的記憶體
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			try {
				BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(options, IMAGESETTING.mInNativeAlloc);
			} catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException ignored) {}
		}
		// 設定是否系統記憶體不足時先行回收部分的記憶體，但回收動作仍會佔用JVM的記憶體
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
			options.inPurgeable = true;
			options.inInputShareable = true;
		}
		Bitmap bitmap = null;
		try {
			// BitmapFactory.decodeStream調用JNI>>nativeDecodeAsset()匯入圖片，避開JAVA層createBitmap的記憶體佔用
			bitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (OutOfMemoryError e) {
			bitmap = null;
			e.printStackTrace();
		} finally {
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					is = null;
				}
			}
		}
		return bitmap;
	}
	
	public static float[] getImageSize(InputStream is){
		// 圖片設定
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 是否讓BitmapFactory.Options只讀取圖片寬高資料而不實際將圖片載入Bitmap
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, options);
		
		float[] imageSize = new float[]{options.outWidth, options.outHeight};
		return imageSize;
	}
	
	public static int getImageSpecifiedSizeNarrowScale(float[] imageSize, float specifiedSize){
		if(specifiedSize == 0){
			return 1;
		}
		float differenceWi = Math.abs(imageSize[0] - specifiedSize);
		float differenceHe = Math.abs(imageSize[1] - specifiedSize);
		float narrowScale;
		if(differenceWi < differenceHe){
			narrowScale = imageSize[0] / specifiedSize;
		}else{
			narrowScale = imageSize[1] / specifiedSize;
		}
		if(narrowScale < 1){
			narrowScale = 1;
		}
		BigDecimal bigDecimal = new BigDecimal("" + narrowScale).setScale(0, BigDecimal.ROUND_HALF_UP);
		return bigDecimal.intValue();
	}
	
	public static int getImageSpecifiedSizeNarrowScale(InputStream is, float specifiedSize){
		return getImageSpecifiedSizeNarrowScale(getImageSize(is), specifiedSize);
	}
	
	public static Bitmap convertMappedBitmap(Bitmap bitmap, int newWidth, int newHeight, Bitmap.Config config, File tempFile){
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
			FileChannel fileChannel = randomAccessFile.getChannel();
			
			// 設定映射緩衝區永遠不比圖像小，避免圖像資料進出緩衝區時發生緩衝區不足的錯誤
			long size;
			if(newWidth == bitmap.getWidth() && newHeight == bitmap.getHeight()){
				size = bitmap.getRowBytes() * bitmap.getHeight();
			}else{
				int bufferWidth = newWidth > bitmap.getWidth() ? newWidth : bitmap.getWidth();
				int bufferHeight = newHeight > bitmap.getHeight() ? newHeight : bitmap.getHeight();
				int pixelByte = (bitmap.getRowBytes() * bitmap.getHeight()) / bitmap.getWidth() / bitmap.getHeight();
				size = pixelByte * (long)bufferWidth * (long)bufferHeight;
			}
			try {
				MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
				bitmap.copyPixelsToBuffer(mappedByteBuffer);
				bitmap.recycle();
				bitmap = null;
				System.gc();
				
				if(newWidth > 0 && newHeight > 0){
					/*
					 * Bitmap CreateType
					 * 1.
					 * Bitmap.createBitmap(display, width, height, config)
					 * Bitmap.createBitmap(width, height, config)
					 * 2.
					 * Bitmap.createBitmap(display, colors, offset, stride, width, height, config)
					 * Bitmap.createBitmap(colors, offset, stride, width, height, config)
					 * Bitmap.createBitmap(display, colors, width, height, config)
					 * Bitmap.createBitmap(colors, width, height, config)
					 * 3.
					 * Bitmap.createBitmap(source, x, y, width, height, m, filter);
					 * Bitmap.createBitmap(source, x, y, width, height)
					 * Bitmap.createBitmap(src)
					 */
					bitmap = Bitmap.createBitmap(newWidth, newHeight, config);
					mappedByteBuffer.position(0);
					bitmap.copyPixelsFromBuffer(mappedByteBuffer);
				}
				mappedByteBuffer = null;
			} finally {
				fileChannel.close();
				randomAccessFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			bitmap = null;
			e.printStackTrace();
		} finally {
			if(tempFile != null){
				if(!tempFile.delete()){
					System.out.println("not delete file " + tempFile.getPath());
				}
			}
		}
		return bitmap;
	}
	
	public static Bitmap convertMappedBitmap(Bitmap bitmap, int newWidth, int newHeight, File tempFile){
		Bitmap.Config config = bitmap.getConfig();
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}
	
	public static Bitmap convertMappedBitmapUseInsidePrivate(Context context, Bitmap bitmap, int newWidth, int newHeight){
		Bitmap.Config config = bitmap.getConfig();
		File tempFile = new File(context.getFilesDir().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}
	
	public static Bitmap convertMappedBitmapUseExternalStorage(Bitmap bitmap, int newWidth, int newHeight){
		Bitmap.Config config = bitmap.getConfig();
		File tempFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}
	
	public static Canvas getCanvas(Bitmap bitmap){
		Canvas canvas = new Canvas(bitmap);
		// 設定抗鋸齒、濾波處理、抖動處理
		PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		canvas.setDrawFilter(paintFlagsDrawFilter);
		return canvas;
	}
	
	public static Paint getPaint(float strokeWidth){
		// 設定抗鋸齒、濾波處理、抖動處理
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		paint.setStrokeWidth(strokeWidth);
		// 設定Paint抗鋸齒
//		paint.setAntiAlias(true);
		// 設定Paint濾波處理
//		paint.setFilterBitmap(true);
		// 設定Paint抖動處理
//		paint.setDither(true);
		return paint;
	}
	
	public static Paint getPaint(float strokeWidth, int alpha, int red, int green, int blue){
		Paint paint = getPaint(strokeWidth);
		paint.setARGB(alpha, red, green, blue);
		return paint;
	}
	
	public static Paint getClearPaint(int strokeWidth){
		Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		clearPaint.setAntiAlias(true);
		clearPaint.setStrokeWidth(strokeWidth);
		clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		return clearPaint;
	}
	
	public static ColorMatrix getColorMatrix(float baseRed, float baseGreen, float baseBlue, float baseAlpha
			, float offsetRed, float offsetGreen, float offsetBlue, float offsetAlpha){
		ColorMatrix colorMatrix = new ColorMatrix();
		// 設定顏色矩陣R, G, B, A, offset
		float[] color = new float[]{
				baseRed, 0, 0, 0, offsetRed// sumRed
				, 0, baseGreen, 0, 0, offsetGreen// sumGreen
				, 0, 0, baseBlue, 0, offsetBlue// sumBlue
				, 0, 0, 0, baseAlpha, offsetAlpha};// sumAlpha
		colorMatrix.set(color);
		return colorMatrix;
	}
	
	public static void setPaintColorFilter(Paint paint, float baseRed, float baseGreen, float baseBlue, float baseAlpha
			, float offsetRed, float offsetGreen, float offsetBlue, float offsetAlpha){
		paint.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(baseRed, baseGreen, baseBlue, baseAlpha, offsetRed, offsetGreen, offsetBlue, offsetAlpha)));
	}
	
	public static void setPaintColorFilter(Paint paint, float offsetRed, float offsetGreen, float offsetBlue, float offsetAlpha){
		setPaintColorFilter(paint, 1.0f, 1.0f, 1.0f, 1.0f, offsetRed, offsetGreen, offsetBlue, offsetAlpha);
	}
	
	public static void setPaintColorFilter(Paint paint, ColorMatrix colorMatrix){
		paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}
	
	/**
	 * 設定顏色亮度相對偏移量
	 */
	public static ColorMatrix setBrightnessRelativeOffset(float brightnessRelativeOffset){
		return getColorMatrix(1.0f, 1.0f, 1.0f, 1.0f, brightnessRelativeOffset, brightnessRelativeOffset, brightnessRelativeOffset, 0);
	}
	
	/**
	 * 設定顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix setBrightnessRelativeScale(float brightnessRelativeScale){
		// colorMatrix.setScale(brightnessRelativeScale, brightnessRelativeScale, brightnessRelativeScale, 1.0f);
		return getColorMatrix(brightnessRelativeScale, brightnessRelativeScale, brightnessRelativeScale, 1.0f, 0, 0, 0, 0);
	}
	
	/**
	 * 設定顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix setContrastRelativeScale(float contrastRelativeScale){
		final float offset = 127.5f * (1.0f - contrastRelativeScale);
		return getColorMatrix(contrastRelativeScale, contrastRelativeScale, contrastRelativeScale, 1.0f, offset, offset, offset, 0);
	}
	
	/**
	 * 設定顏色飽和相對倍數
	 * saturationRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix setSaturationRelativeScale(float saturationRelativeScale){
		// R = 0.3086, G = 0.6094, B = 0.0820
		// R = 0.213, G = 0.715, B = 0.072
		// colorMatrix.setSaturation(saturationRelativeScale);
		final float diff = 1.0f - saturationRelativeScale;
		final float R = 0.213f * diff;
		final float G = 0.715f * diff;
		final float B = 0.072f * diff;
		ColorMatrix colorMatrix = new ColorMatrix();
		float[] color = new float[]{
				R + saturationRelativeScale, G, B, 0, 0
				, R, G + saturationRelativeScale, B, 0, 0
				, R, G, B + saturationRelativeScale, 0, 0
				, 0, 0, 0, 1, 0};
		colorMatrix.set(color);
		return colorMatrix;
	}
	
	/**
	 * 設定顏色色相（色調）旋轉相對偏移量
	 * hueDegreesRelativeRotateOffset = 0 or 360 為原色
	 */
	public static ColorMatrix setHueRelativeRotate(@FloatRange(from = 0.0, to = 360.0) float hueDegreesRelativeRotateOffset){
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setRotate(0, hueDegreesRelativeRotateOffset);
		colorMatrix.setRotate(1, hueDegreesRelativeRotateOffset);
		colorMatrix.setRotate(2, hueDegreesRelativeRotateOffset);
		return colorMatrix;
	}
	
	/**
	 * 設定顏色反相（負片、互補色）
	 */
	public static ColorMatrix setInverting(){
		return getColorMatrix(-1.0f, -1.0f, -1.0f, 1.0f, 255, 255, 255, 0);
	}
	
	/**
	 * 設定畫筆顏色亮度相對偏移量
	 */
	public static void setPaintBrightnessRelativeOffset(Paint paint, float brightnessRelativeOffset){
		paint.setColorFilter(new ColorMatrixColorFilter(setBrightnessRelativeOffset(brightnessRelativeOffset)));
	}
	
	/**
	 * 設定畫筆顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static void setPaintBrightnessRelativeScale(Paint paint, float brightnessRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(setBrightnessRelativeScale(brightnessRelativeScale)));
	}
	
	/**
	 * 設定畫筆顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static void setPaintContrastRelativeScale(Paint paint, float contrastRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(setContrastRelativeScale(contrastRelativeScale)));
	}
	
	/**
	 * 設定畫筆顏色飽和相對倍數
	 * saturationRelativeScale = 1.0 為原色
	 */
	public static void setPaintSaturationRelativeScale(Paint paint, float saturationRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(setSaturationRelativeScale(saturationRelativeScale)));
	}
	
	/**
	 * 設定畫筆顏色色相（色調）旋轉相對偏移量
	 * hueDegreesRelativeRotateOffset = 0 or 360 為原色
	 */
	public static void setPaintHueRelativeRotate(Paint paint, float hueDegreesRelativeRotateOffset){
		paint.setColorFilter(new ColorMatrixColorFilter(setHueRelativeRotate(hueDegreesRelativeRotateOffset)));
	}
	
	/**
	 * 設定畫筆顏色反相（負片、互補色）
	 */
	public static void setPaintInverting(Paint paint){
		paint.setColorFilter(new ColorMatrixColorFilter(setInverting()));
	}
	
	/**
	 * 設定圖片顏色亮度相對偏移量
	 */
	public static Bitmap drawBitmapBrightnessRelativeOffset(Bitmap bitmap, float brightnessRelativeOffset){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setBrightnessRelativeOffset(brightnessRelativeOffset)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static Bitmap drawBitmapBrightnessRelativeScale(Bitmap bitmap, float brightnessRelativeScale){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setBrightnessRelativeScale(brightnessRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static Bitmap drawBitmapContrastRelativeScale(Bitmap bitmap, float contrastRelativeScale){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setContrastRelativeScale(contrastRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色飽和相對倍數<br>
	 * saturationRelativeScale = 1.0 為原色
	 * saturationRelativeScale =0 為灰階效果
	 */
	public static Bitmap drawBitmapSaturationRelativeScale(Bitmap bitmap, float saturationRelativeScale){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setSaturationRelativeScale(saturationRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色色相（色調）旋轉相對偏移量
	 * hueDegreesRelativeRotateOffset = 0 or 360 為原色
	 */
	public static Bitmap drawBitmapHueRelativeRotate(Bitmap bitmap, float hueDegreesRelativeRotateOffset){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setHueRelativeRotate(hueDegreesRelativeRotateOffset)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色反相（負片、互補色）
	 */
	public static Bitmap drawBitmapInverting(Bitmap bitmap){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(setInverting()));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片浮雕效果
	 */
	public static Bitmap drawBitmapEmboss(Bitmap bitmap){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		
		// 設定浮雕效果
		EmbossMaskFilter emboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
		paint.setMaskFilter(emboss);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片模糊效果
	 */
	public static Bitmap drawBitmapBlur(Bitmap bitmap, float radius, BlurMaskFilter.Blur style){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		
		// 設定模糊效果
		BlurMaskFilter blur = new BlurMaskFilter(radius, style);
		paint.setMaskFilter(blur);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	public static Drawable bitmapToDrawable(Resources res, Bitmap bitmap){
		return new BitmapDrawable(res, bitmap);
	}

	public static Bitmap drawableToBitmap(Drawable drawable, boolean isOptimumConfig){
		if(drawable instanceof BitmapDrawable){
			return ((BitmapDrawable)drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap.Config config;
		PixelFormat pixelFormat = new PixelFormat();
		if(drawable.getOpacity() > -1){
			PixelFormat.getPixelFormatInfo(drawable.getOpacity(), pixelFormat);
			if(isOptimumConfig && !PixelFormat.formatHasAlpha(drawable.getOpacity())){
				config = Bitmap.Config.RGB_565;
			}else if(isOptimumConfig && pixelFormat.bytesPerPixel == 1 && pixelFormat.bitsPerPixel == 8){
				config = Bitmap.Config.ALPHA_8;
			}else{
				config = Bitmap.Config.ARGB_8888;
			}
		}else if(drawable.getOpacity() == -1){
			config = Bitmap.Config.RGB_565;
		}else{
			config = Bitmap.Config.ARGB_8888;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static Drawable getStateListDrawable(int[][] ints, Drawable[] drawable){
		StateListDrawable stateListDrawable = new StateListDrawable();
		for(int i=0; i<ints.length; i++){
			stateListDrawable.addState(ints[i], drawable[i]);
		}
		return stateListDrawable;
	}

	public static Drawable getStateListDrawable(Context context, int[][] ints, int[] resourceId){
		StateListDrawable stateListDrawable = new StateListDrawable();
		for(int i=0; i<ints.length; i++){
			stateListDrawable.addState(ints[i], ContextCompat.getDrawable(context, resourceId[i]));
		}
		return stateListDrawable;
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, int left, int top, int right, int bottom, Bitmap.Config config, float roundRadius){
		Bitmap bitmapOutput = Bitmap.createBitmap(right - left, bottom - top, config == null ? bitmap.getConfig() : config);
		Canvas canvas = getCanvas(bitmapOutput);
		Paint paint = getPaint(1);

		RectF rectF = new RectF(0, 0, bitmapOutput.getWidth(), bitmapOutput.getHeight());
		canvas.drawRoundRect(rectF, roundRadius, roundRadius, paint);

		Rect rect = new Rect(left, top, right, bottom);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rectF, paint);
		return bitmapOutput;
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, int width, int height, Bitmap.Config config, float roundRadius){
		return drawBitmapRoundRect(bitmap, 0, 0, width, height, config, roundRadius);
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, int width, int height, float roundRadius){
		return drawBitmapRoundRect(bitmap, 0, 0, width, height, bitmap.getConfig(), roundRadius);
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, float roundRadius){
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), roundRadius);
	}

	public static Bitmap drawBitmapRoundRect(Context context, int resourceId, Bitmap.Config config, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context, resourceId);
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), config, roundRadius);
	}

	public static Bitmap drawBitmapRoundRect(Context context, int resourceId, float roundRadius){
		return drawBitmapRoundRect(context, resourceId, null, roundRadius);
	}

	public static Drawable drawDrawableRoundRect(Resources res, Drawable drawable, int width, int height, float roundRadius){
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(res, drawBitmapRoundRect(bitmap, 0, 0, width, height, bitmap.getConfig(), roundRadius));
	}

	public static Drawable drawDrawableRoundRect(Resources res, Drawable drawable, float roundRadius){
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(res, drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), roundRadius));
	}

	public static Bitmap drawResourceBitmapRoundRect(Resources res, int resourceId, int inSampleSize, float roundRadius){
		Bitmap bitmap = getRawBitmap(res, resourceId, inSampleSize);
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), roundRadius);
	}

	public static Bitmap drawResourceBitmapRoundRect(Resources res, int resourceId, float specifiedSize, float roundRadius){
		Bitmap bitmap = getRawBitmap(res, resourceId, specifiedSize);
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), roundRadius);
	}

	public static Drawable drawResourceDrawableRoundRect(Context context, int resourceId, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context, resourceId);
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(context.getResources(), drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), roundRadius));
	}
}