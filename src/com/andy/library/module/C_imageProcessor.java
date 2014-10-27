package com.andy.library.module;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
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
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.4.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_imageProcessor {
	
	private static final ImageSetting IMAGESETTING = new ImageSetting();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(IMAGESETTING.threadPoolSum);
	private static final Map<String, SoftReference<Bitmap>> BUFFER_MAP = new HashMap<String, SoftReference<Bitmap>>();
	private static final Map<String, String> SAMPLE_MAP = new HashMap<String, String>();
	private static final String SAMPLE_WORD = "_SampleSize";
	
	public interface DownLoadComplete{
		public void loadedImage(String streamURL, Bitmap bitmap);
		public void loadFail(String streamURL);
	}
	
	public static class ImageSetting{
		private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
		private boolean inNativeAlloc = true;
		private boolean isPrintLoadStreamException = true;
		private int inputBufferSize = 1024;
		private int threadPoolSum = 3;
	}
	
	public static void setBitmapConfig(Bitmap.Config bitmapConfig){
		C_imageProcessor.IMAGESETTING.bitmapConfig = bitmapConfig;
	}
	
	public static Bitmap.Config getBitmapConfig(){
		return IMAGESETTING.bitmapConfig;
	}
	
	public static void setInNativeAlloc(boolean inNativeAlloc){
		C_imageProcessor.IMAGESETTING.inNativeAlloc = inNativeAlloc;
	}
	
	public static boolean getInNativeAlloc(){
		return IMAGESETTING.inNativeAlloc;
	}
	
	public static void setPrintLoadStreamException(boolean isPrintLoadStreamException){
		C_imageProcessor.IMAGESETTING.isPrintLoadStreamException = isPrintLoadStreamException;
	}
	
	public static boolean getIsPrintLoadStreamException(){
		return IMAGESETTING.isPrintLoadStreamException;
	}
	
	public static void setInputBufferSize(int inputBufferSize){
		C_imageProcessor.IMAGESETTING.inputBufferSize = inputBufferSize;
	}
	
	public static int getInputBufferSize(){
		return IMAGESETTING.inputBufferSize;
	}
	
	public static void setThreadPoolSum(int threadPoolSum){
		C_imageProcessor.IMAGESETTING.threadPoolSum = threadPoolSum;
	}
	
	public static int getThreadPoolSum(){
		return IMAGESETTING.threadPoolSum;
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, int inSampleSize){
		// 讀取APK中/res/raw/底下被記錄在R.java的資源
		// 此目錄僅支援讀取1MB以下的檔案
		Bitmap bitmap = getBufferBitmap("Raw" + File.separator + resource, SAMPLE_WORD, inSampleSize);
		if(bitmap != null){
			return bitmap;
		}
		try {
			InputStream is = res.openRawResource(resource);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			setBufferBitmap(bitmap, "Raw" + File.separator + resource, SAMPLE_WORD, inSampleSize);
		} catch (IOException e) {
			System.out.println(e);
		}
		return bitmap;
	}
	
	public static Bitmap getRawBitmap(Resources res, int resource, float specifiedSize){
		InputStream is = res.openRawResource(resource);
		int scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
		return getRawBitmap(res, resource, scale);
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, int inSampleSize){
		// 讀取APK中/assets/底下的檔案
		// 此目錄僅支援讀取1MB以下的檔案
		Bitmap bitmap = getBufferBitmap("Assets" + File.separator + imageName, SAMPLE_WORD, inSampleSize);
		if(bitmap != null){
			return bitmap;
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
			setBufferBitmap(bitmap, "Assets" + File.separator + imageName, SAMPLE_WORD, inSampleSize);
			return bitmap;
		} catch (IOException e) {
			System.out.println(e);
		}
		return null;
	}
	
	public static Bitmap readAssetsBitmap(Context context, String imageName, float specifiedSize){
		AssetManager assetManager = context.getAssets();
		int scale = 1;
		try {
			InputStream is = assetManager.open(imageName);
			try {
				scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		return readAssetsBitmap(context, imageName, scale);
	}
	
	public static void writeInsidePrivateImage(Context context, Bitmap bitmap, int quality, String imageName){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		try {
			FileOutputStream fileOutStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
			bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
			fileOutStream.flush();
			fileOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, int inSampleSize){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		Bitmap bitmap = getBufferBitmap(imageName, SAMPLE_WORD, inSampleSize);
		if(bitmap != null){
			return bitmap;
		}
		try {
			InputStream is = context.openFileInput(imageName);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			setBufferBitmap(bitmap, imageName, SAMPLE_WORD, inSampleSize);
			return bitmap;
		} catch (FileNotFoundException e) {
//			System.out.println(e);
		} catch (IOException e) {
//			System.out.println(e);
		}
		return null;
	}
	
	public static Bitmap readInsidePrivateImage(Context context, String imageName, float specifiedSize){
		// 使用context.open處理具私有權限保護的/data/data/packageName/檔案
		imageName = imageName.replace(File.separator, "_");
		try {
			InputStream is = context.openFileInput(imageName);
			int scale;
			try {
				scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
			Bitmap bitmap = getBufferBitmap(imageName, SAMPLE_WORD, scale);
			if(bitmap != null){
				return bitmap;
			}
			
			is = context.openFileInput(imageName);
			try {
				bitmap = getAgileBitmap(is, scale);
			} finally {
				is.close();
				is = null;
			}
			setBufferBitmap(bitmap, imageName, SAMPLE_WORD, scale);
			return bitmap;
		} catch (FileNotFoundException e) {
//			System.out.println(e);
		} catch (IOException e) {
//			System.out.println(e);
		}
		return null;
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
			file.mkdirs();
		}
		file = new File(insidePath + path + imageName);
		try {
			FileOutputStream fileOutStream = new FileOutputStream(file, false);
			bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
			fileOutStream.flush();
			fileOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, int inSampleSize){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		Bitmap bitmap = getBufferBitmap(insidePath + path + imageName, SAMPLE_WORD, inSampleSize);
		if(bitmap != null){
			return bitmap;
		}
		File file = new File(insidePath + path + imageName);
		try {
			InputStream is = new FileInputStream(file);
			try {
				bitmap = getAgileBitmap(is, inSampleSize);
			} finally {
				is.close();
				is = null;
			}
			setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, inSampleSize);
			return bitmap;
		} catch (FileNotFoundException e) {
//			System.out.println(e);
		} catch (IOException e) {
//			System.out.println(e);
		}
		return null;
	}
	
	public static Bitmap readInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName, float specifiedSize){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		File file = new File(insidePath + path + imageName);
		try {
			InputStream is = new FileInputStream(file);
			int scale;
			try {
				scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
			} finally {
				is.close();
				is = null;
			}
			Bitmap bitmap = getBufferBitmap(insidePath + path + imageName, SAMPLE_WORD, scale);
			if(bitmap != null){
				return bitmap;
			}
			
			is = new FileInputStream(file);
			try {
				bitmap = getAgileBitmap(is, scale);
			} finally {
				is.close();
				is = null;
			}
			setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, scale);
			return bitmap;
		} catch (FileNotFoundException e) {
//			System.out.println(e);
		} catch (IOException e) {
//			System.out.println(e);
		}
		return null;
	}
	
	public static boolean deleteInsideImageFile(Context context, int mode, String rootFolderName, String path, String imageName){
		// 取得app路徑/data/data/packageName
		String insidePath = context.getDir(rootFolderName, mode).getPath() + File.separator;
		deleteBufferBitmap(insidePath + path + imageName, SAMPLE_WORD);
		File file = new File(insidePath + path + imageName);
		return file.delete();
	}
	
	public static void writeSDCardImageFile(Bitmap bitmap, int quality, String directory, String imageName){
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
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
				file.mkdirs();
			}
			file = new File(sdCardPath + directory + imageName);
			try {
				FileOutputStream fileOutStream = new FileOutputStream(file, false);
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutStream);
				fileOutStream.flush();
				fileOutStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, int inSampleSize){
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			Bitmap bitmap = getBufferBitmap(sdCardPath + directory + imageName, SAMPLE_WORD, inSampleSize);
			if(bitmap != null){
				return bitmap;
			}
			file = new File(sdCardPath + directory + imageName);
			try {
				InputStream is = new FileInputStream(file);
				try {
					bitmap = getAgileBitmap(is, inSampleSize);
				} finally {
					is.close();
					is = null;
				}
				setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, inSampleSize);
				return bitmap;
			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		return null;
	}
	
	public static Bitmap readSDCardImageFile(String directory, String imageName, float specifiedSize){
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		// 確認sdCard是否掛載
		if(sdCardExist){
			// 取得sdCard路徑/mnt/sdcard
			File file = Environment.getExternalStorageDirectory();
			String sdCardPath = file.toString() + File.separator;
			if(directory.indexOf(sdCardPath) == 0){
				sdCardPath = "";
			}
			file = new File(sdCardPath + directory + imageName);
			try {
				InputStream is = new FileInputStream(file);
				int scale;
				try {
					scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
				} finally {
					is.close();
					is = null;
				}
				Bitmap bitmap = getBufferBitmap(sdCardPath + directory + imageName, SAMPLE_WORD, scale);
				if(bitmap != null){
					return bitmap;
				}
				
				is = new FileInputStream(file);
				try {
					bitmap = getAgileBitmap(is, scale);
				} finally {
					is.close();
					is = null;
				}
				setBufferBitmap(bitmap, file.toString(), SAMPLE_WORD, scale);
				return bitmap;
			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		return null;
	}
	
	public static boolean deleteSDCardImageFile(Context context, String directory, String imageName){
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
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
	
	public static byte[] loadStream(String streamURL) throws IOException{
		InputStream is = null;
		streamURL = streamURL.replace(" ", "%20");
		streamURL = streamURL.replace("\\(", "%28");
		streamURL = streamURL.replace("\\)", "%29");
		URL url = new URL(streamURL);
		is = new BufferedInputStream(url.openStream(), IMAGESETTING.inputBufferSize);
		return inputStreamToByteArray(is);
	}
	
	public static boolean isConnect(Context context) {
		boolean isConnect = false;
		ConnectivityManager connectManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectManager.getActiveNetworkInfo() != null){
			isConnect = connectManager.getActiveNetworkInfo().isAvailable();
		}
//		NetworkInfo[] networkInfo = ContyManager.getAllNetworkInfo();
//		if(networkInfo != null){
//			for(int i=0; i<networkInfo.length; i++){
//				if(networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
//					return true;
//				}
//			}
//		}
		return isConnect;
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
		return inputStreamToByteArray(is, 1024 * 8);
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
			return getImagespecifiedSizeNarrowScale(new float[]{width, height}, specifiedSize);
		}
		return -1;
	}
	
	public static Bitmap getImageAsyncLoad(Context context, final String streamURL, final float specifiedSize, final DownLoadComplete complete){
		if(streamURL == null || streamURL.trim().length() == 0){
			return null;
		}
		
		Bitmap bitmap = getBufferBitmap(streamURL, SAMPLE_WORD, getBufferSample(streamURL, SAMPLE_WORD, specifiedSize));
		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
        if(bitmap != null){
            return bitmap;
        }
		
		final Handler handler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(msg.what == 0 && complete != null){
					complete.loadedImage(streamURL, (Bitmap)msg.obj);
				}else if(msg.what == 1 && complete != null){
					complete.loadFail(streamURL);
				}
				return false;
			}
		});
		Runnable runnableLoad = new Runnable() {
			
			@Override
			public void run() {
				try {
					byte[] byteArray = loadStream(streamURL);
					InputStream is = new ByteArrayInputStream(byteArray);
					int scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
					try {
						is.reset();
					} catch (IOException e1) {
						try {
							is.close();
							is = null;
						} catch (IOException e2) {}
						is = new ByteArrayInputStream(byteArray);
						System.out.println(e1);
					}
					Bitmap bitmap;
					try {
						bitmap = getAgileBitmap(is, scale);
					} finally {
						is.close();
						is = null;
					}
					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, streamURL, SAMPLE_WORD, scale);
					Message msg = new Message();
					msg.obj = bitmap;
					handler.sendMessage(msg);
				} catch (IOException e) {
					handler.sendEmptyMessage(1);
					if(IMAGESETTING.isPrintLoadStreamException){System.out.println(e);}
				}
			}
		};
		
		if(isConnect(context)){
			EXECUTOR_SERVICE.submit(runnableLoad);
		}
		return null;
	}
	
	public static Bitmap getImageAsyncLoadWriteLocal(final Context context, final String streamURL, final float specifiedSize, final int quality
			, final String imageName, final DownLoadComplete complete){
		if(streamURL == null || streamURL.trim().length() == 0){
			return null;
		}
		
		final String imageNameCopy = imageName.replace(File.separator, "_");
		final Handler handlerLoad = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(msg.what == 0 && complete != null){
					complete.loadedImage(streamURL, (Bitmap)msg.obj);
				}else if(msg.what == 1 && complete != null){
					complete.loadFail(streamURL);
				}
				return false;
			}
		});
		final Runnable runnableLoad = new Runnable() {
			
			@Override
			public void run() {
				try {
					byte[] byteArray = loadStream(streamURL);
					InputStream is = new ByteArrayInputStream(byteArray);
					int scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
					try {
						is.reset();
					} catch (IOException e1) {
						try {
							is.close();
							is = null;
						} catch (Exception e2) {}
						is = new ByteArrayInputStream(byteArray);
						System.out.println(e1);
					}
					Bitmap bitmap;
					try {
						bitmap = getAgileBitmap(is, scale);
					} finally {
						is.close();
						is = null;
					}
					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, imageNameCopy, SAMPLE_WORD, scale);
					// 將下載的圖片儲存於本地端
					writeInsidePrivateImage(context, bitmap, quality, imageNameCopy);
					Message msg = new Message();
					msg.obj = bitmap;
					handlerLoad.sendMessage(msg);
				} catch (IOException e) {
					handlerLoad.sendEmptyMessage(1);
					if(IMAGESETTING.isPrintLoadStreamException){System.out.println(e);}
				}
			}
		};
		
		Handler handlerNone = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(isConnect(context)){
					EXECUTOR_SERVICE.submit(runnableLoad);
				}
				return false;
			}
		});
		return getImageAsyncLocal(context, streamURL, specifiedSize, imageName, complete, handlerNone);
	}
	
	public static Bitmap getImageAsyncLocal(final Context context, final String streamURL, final float specifiedSize, final String imageName
			, final DownLoadComplete complete, final Handler handlerNone){
		if(streamURL == null || streamURL.trim().length() == 0){
			return null;
		}
		
		final String imageNameCopy = imageName.replace(File.separator, "_");
		Bitmap bitmap = getBufferBitmap(imageNameCopy, SAMPLE_WORD, getBufferSample(imageNameCopy, SAMPLE_WORD, specifiedSize));
		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
		if(bitmap != null){
			return bitmap;
		}
		
		final Handler handlerRead = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if(complete != null){
					complete.loadedImage(streamURL, (Bitmap)msg.obj);
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
						scale = getImagespecifiedSizeNarrowScale(is, specifiedSize);
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
					setBufferBitmap(bitmap, imageNameCopy, SAMPLE_WORD, scale);
				} catch (FileNotFoundException e) {
//					System.out.println(e);
				} catch (IOException e) {
//					System.out.println(e);
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
	
	@SuppressLint("NewApi")
	public static Bitmap getAgileBitmap(InputStream is, int inSampleSize){
		// 圖片設定
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 設定是否讓BitmapFactory.Options只讀取圖片寬高資料而不實際將圖片載入Bitmap
		options.inJustDecodeBounds = false;
		// 設定匯入後圖片的寬高縮小比例，預設1為原始寬高
		options.inSampleSize = inSampleSize;
		// 設定圖片ARGB屬性佔用記憶體空間，預設Bitmap.Config.ARGB_8888為各佔8Bit
		options.inPreferredConfig = IMAGESETTING.bitmapConfig;
		// 設定是否系統記憶體不足時先行回收部分的記憶體，但回收動作仍會佔用JVM的記憶體
		options.inPurgeable = true;
		options.inInputShareable = true;
		// SDK 11 開始可設定是否讓圖片內容允許變動
		if(Build.VERSION.SDK_INT >= 11){
			options.inMutable = true;
		}
		try {
			// 直接把不使用的記憶體歸給JVM，回收動作不佔用JVM的記憶體
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(options, IMAGESETTING.inNativeAlloc);
		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
		} catch (SecurityException e) {
//			e.printStackTrace();
		} catch (IllegalAccessException e) {
//			e.printStackTrace();
		} catch (NoSuchFieldException e) {
//			e.printStackTrace();
		}
		Bitmap bitmap = null;
		try {
			try {
				// BitmapFactory.decodeStream調用JNI>>nativeDecodeAsset()匯入圖片，避開JAVA層createBitmap的記憶體佔用
				bitmap = BitmapFactory.decodeStream(is, null, options);
			} finally {
				is.close();
				is = null;
			}
		} catch (IOException e) {
//			e.printStackTrace();
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
	
	public static int getImagespecifiedSizeNarrowScale(float[] imageSize, float specifiedSize){
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
	
	public static int getImagespecifiedSizeNarrowScale(InputStream is, float specifiedSize){
		return getImagespecifiedSizeNarrowScale(getImageSize(is), specifiedSize);
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
			fileChannel.close();
			randomAccessFile.close();
			tempFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			bitmap = null;
			tempFile = null;
			e.printStackTrace();
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
		// 設定抗鋸齒、抖動平滑
		PaintFlagsDrawFilter paintF = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		canvas.setDrawFilter(paintF);
		return canvas;
	}
	
	public static Paint getPaint(float strokeWidth){
		// 設定抗鋸齒、抖動平滑
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		paint.setStrokeWidth(strokeWidth);
		// 設定Paint抗鋸齒
//		paint.setAntiAlias(true);
//		paint.setFilterBitmap(true);
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
		clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		return clearPaint;
	}
	
	public static void setPaintColorFilter(Paint paint, float baseRed, float baseGreen, float baseBlue, float baseAlpha
			, float offsetRed, float offsetGreen, float offsetBlue, float offsetAlpha){
		ColorMatrix colorMatrix = new ColorMatrix();
		// 設定顏色矩陣R, G, B, A, offset
		float[] color = new float[]{
				baseRed, 0, 0, 0, offsetRed// sumRed
				, 0, baseGreen, 0, 0, offsetGreen// sumGreen
				, 0, 0, baseBlue, 0, offsetBlue// sumBlue
				, 0, 0, 0, baseAlpha, offsetAlpha};// sumAlpha
		// 設定筆色效果
		colorMatrix.set(color);
		paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}
	
	public static void setPaintColorFilter(Paint paint, float offsetRed, float offsetGreen, float offsetBlue, float offsetAlpha){
		setPaintColorFilter(paint, 1.0f, 1.0f, 1.0f, 1.0f, offsetRed, offsetGreen, offsetBlue, offsetAlpha);
	}
	
	public static void setPaintColorFilter(Paint paint, ColorMatrix colorMatrix){
		paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}
	
	/**
	 * 設定畫筆顏色亮度
	 * @param paint
	 * @param brightnessValue
	 */
	public static void setPaintBrightness(Paint paint, float brightnessValue){
		setPaintColorFilter(paint, 1.0f, 1.0f, 1.0f, 1.0f, brightnessValue, brightnessValue, brightnessValue, 0);
	}
	
	/**
	 * 設定畫筆顏色對比
	 * @param paint
	 * @param contrastValue
	 */
	public static void setPaintContrast(Paint paint, float contrastValue){
		final float offset = 127.5f * (1.0f - contrastValue);
		setPaintColorFilter(paint, contrastValue, contrastValue, contrastValue, 1.0f, offset, offset, offset, 0);
	}
	
	/**
	 * 設定畫筆顏色飽和
	 * @param paint
	 * @param saturationValue
	 */
	public static void setPaintSaturation(Paint paint, float saturationValue){
		// R = 0.3086, G = 0.6094, B = 0.0820
		// R = 0.213, G = 0.715, B = 0.072
		// colorMatrix.setSaturation(saturationValue);
		final float diff = 1 - saturationValue;
		final float R = 0.213f * diff;
		final float G = 0.715f * diff;
		final float B = 0.072f * diff;
		ColorMatrix colorMatrix = new ColorMatrix();
		float[] color = new float[]{
				R + saturationValue, G, B, 0, 0
				, R, G + saturationValue, B, 0, 0
				, R, G, B + saturationValue, 0, 0
				, 0, 0, 0, 1, 0};
		colorMatrix.set(color);
		paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}
	
	/**
	 * 設定畫筆顏色色相（色調）旋轉
	 * @param paint
	 * @param hueDegreesOffsetValue
	 */
	public static void setPaintHueRotate(Paint paint, float hueDegreesOffsetValue){
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setRotate(0, hueDegreesOffsetValue);
		colorMatrix.setRotate(1, hueDegreesOffsetValue);
		colorMatrix.setRotate(2, hueDegreesOffsetValue);
		paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}
	
	/**
	 * 設定畫筆顏色反相（負片、互補色）
	 * @param paint
	 * @param offsetRed
	 * @param offsetGreen
	 * @param offsetBlue
	 */
	public static void setPaintInverting(Paint paint){
		setPaintColorFilter(paint, -1.0f, -1.0f, -1.0f, 1.0f, 255, 255, 255, 0);
	}
	
	/**
	 * 設定畫筆顏色高低互補色
	 * @param paint
	 * @param offsetRed
	 * @param offsetGreen
	 * @param offsetBlue
	 */
	public static void setPaintComplementaryHighLowColor(Paint paint, float offsetRed, float offsetGreen, float offsetBlue
			, float offsetAlpha){
		float[] colors = new float[]{offsetRed, offsetGreen, offsetBlue};
		Arrays.sort(colors);
		float sumValue = colors[0] + colors[2];
		setPaintColorFilter(paint, 1.0f, 1.0f, 1.0f, 1.0f, sumValue - offsetRed, sumValue - offsetGreen, sumValue - offsetBlue
				, offsetAlpha);
	}
	
	/**
	 * 設定圖片顏色亮度
	 * @param bitmap
	 * @param brightnessValue
	 */
	public static Bitmap drawBitmapBrightness(Bitmap bitmap, float brightnessValue){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		setPaintBrightness(paint, brightnessValue);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色對比
	 * @param bitmap
	 * @param contrastValue
	 */
	public static Bitmap drawBitmapContrast(Bitmap bitmap, float contrastValue){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		setPaintContrast(paint, contrastValue);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色飽和<br>
	 * saturationValue值等於0時為灰階效果
	 * @param bitmap
	 * @param saturationValue
	 * @return
	 */
	public static Bitmap drawBitmapSaturation(Bitmap bitmap, float saturationValue){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		setPaintSaturation(paint, saturationValue);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色色相（色調）旋轉
	 * @param bitmap
	 * @param hueDegreesOffsetValue
	 */
	public static Bitmap drawBitmapHueRotate(Bitmap bitmap, float hueDegreesOffsetValue){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		setPaintHueRotate(paint, hueDegreesOffsetValue);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片顏色反相（負片、互補色）
	 * @param bitmap
	 * @return
	 */
	public static Bitmap drawBitmapInverting(Bitmap bitmap){
		Canvas canvas = getCanvas(bitmap);
		Paint paint = getPaint(1);
		setPaintInverting(paint);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 設定圖片浮雕效果
	 * @param bitmap
	 * @return
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
	 * @param bitmap
	 * @param radius
	 * @param style
	 * @return
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
}