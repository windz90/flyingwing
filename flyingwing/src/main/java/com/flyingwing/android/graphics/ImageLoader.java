/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 4.0.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach", "Convert2Diamond"})
public class ImageLoader {

	public static final int FLAG_FAIL = -1;
	public static final int FLAG_REMOTE = 1;
	public static final int FLAG_LOCAL = 2;
	public static final int FLAG_CACHE = 3;

	public interface OnLoadImageListener {
		byte[] onHaveToRead(int flag, String strUrl);
		void onHaveToWrite(String strUrl, Bitmap bitmap);
		Bitmap onGenerateImage(InputStream inputStream, int inSampleSize);
		void onObtainImage(int flag, String strUrl, Bitmap bitmap);
	}

	private static final class StaticNestedClass {
		private static final ImageLoader INSTANCE = new ImageLoader();
	}

	public static ImageLoader getInstance(){
		return StaticNestedClass.INSTANCE;
	}

	private final String IMAGE_SCALE_PREFIX = "_";

	private OnLoadImageListener mOnLoadImageListener;
	private ThreadPoolExecutor mThreadPoolExecutor;
	private Map<String, SoftReference<Bitmap>> mImageBufferMap;
	private Map<String, String> mImageScaleMap;
	private int mBufferSize = 1024 * 16;
	private boolean mIsPrintException;

	protected ImageLoader(){
		mThreadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		mImageBufferMap = new HashMap<String, SoftReference<Bitmap>>();
		mImageScaleMap = new HashMap<String, String>();
	}

	public void setOnLoadImageListener(OnLoadImageListener onLoadImageListener) {
		mOnLoadImageListener = onLoadImageListener;
	}

	public OnLoadImageListener getOnLoadImageListener() {
		return mOnLoadImageListener;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		mThreadPoolExecutor = threadPoolExecutor;
	}

	public ThreadPoolExecutor getThreadPoolExecutor() {
		return mThreadPoolExecutor;
	}

	public void setBufferSize(int bufferSize){
		mBufferSize = bufferSize;
	}

	public int getBufferSize(){
		return mBufferSize;
	}

	public void setPrintException(boolean isPrintException){
		mIsPrintException = isPrintException;
	}

	public boolean isPrintException(){
		return mIsPrintException;
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public byte[] openConnectionReadInputStream(Context context, String strUrl) throws IOException {
		if(!isAvailableByInternet(context.getApplicationContext())){
			return null;
		}
		strUrl = strUrl.replace(" ", "%20");
		strUrl = strUrl.replace("\\(", "%28");
		strUrl = strUrl.replace("\\)", "%29");
		URL url = new URL(strUrl);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		InputStream inputStreamError = httpURLConnection.getErrorStream();
		if(inputStreamError != null){
			if(mIsPrintException){
				byte[] bytes = inputStreamToByteArray(inputStreamError, mBufferSize);
				if(bytes != null){
					System.out.println(new String(bytes, Charset.forName("UTF-8")));
				}
			}
			inputStreamError.close();
			return null;
		}
		if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
			return null;
		}
		try {
			return inputStreamToByteArray(httpURLConnection.getInputStream(), mBufferSize);
		} finally {
			httpURLConnection.disconnect();
		}
	}

	public void setBufferBitmap(Bitmap bitmap, String name, int inSampleSize){
		if(name == null || name.trim().length() == 0){
			return;
		}
		try {
			name = new String(MessageDigest.getInstance("MD5").digest(name.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			if(mIsPrintException){e.printStackTrace();}
		}
		mImageBufferMap.put(name + IMAGE_SCALE_PREFIX + inSampleSize, new SoftReference<Bitmap>(bitmap));
		String scale = mImageScaleMap.get(name);
		if(TextUtils.isEmpty(scale)){
			mImageScaleMap.put(name, String.valueOf(inSampleSize));
		}else if(!isMatchBufferSample(name, inSampleSize)){
			mImageScaleMap.put(name, scale + "," + inSampleSize);
		}
	}

	public Bitmap getBufferBitmap(String name, int inSampleSize){
		if(name == null || name.trim().length() == 0){
			return null;
		}
		try {
			name = new String(MessageDigest.getInstance("MD5").digest(name.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			if(mIsPrintException){e.printStackTrace();}
		}
		if(inSampleSize != -1 && mImageBufferMap.containsKey(name + IMAGE_SCALE_PREFIX + inSampleSize)){
			// 取得Map暫存於記憶體中的圖片
			SoftReference<Bitmap> softReference = mImageBufferMap.get(name + IMAGE_SCALE_PREFIX + inSampleSize);
			if(softReference != null){
				return softReference.get();
			}
		}
		return null;
	}

	public void deleteBufferBitmap(String name){
		if(name == null || name.trim().length() == 0){
			return;
		}
		try {
			name = new String(MessageDigest.getInstance("MD5").digest(name.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			if(mIsPrintException){e.printStackTrace();}
		}
		String scale = mImageScaleMap.get(name);
		mImageScaleMap.remove(name);
		if(scale == null || scale.trim().length() == 0){
			return;
		}
		String[] scaleArray = scale.split(",");
		for(int i=0; i<scaleArray.length; i++){
			mImageBufferMap.remove(name + IMAGE_SCALE_PREFIX + scaleArray[i]);
		}
	}

	public boolean isMatchBufferSample(String name, int inSampleSize){
		if(name == null || name.trim().length() == 0){
			return false;
		}
		String scale = mImageScaleMap.get(name);
		if(scale == null || scale.trim().length() == 0){
			return false;
		}
		String[] scaleArray = scale.split(",");
		for(int i=0; i<scaleArray.length; i++){
			// 尋找符合的採樣值
			if(scaleArray[i].equals(String.valueOf(inSampleSize))){
				return true;
			}
		}
		return false;
	}

	public int getBufferSample(String name, float targetSize){
		if(name == null || name.trim().length() == 0){
			return -1;
		}
		try {
			name = new String(MessageDigest.getInstance("MD5").digest(name.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			if(mIsPrintException){e.printStackTrace();}
		}
		String scale = mImageScaleMap.get(name);
		if(scale == null || scale.trim().length() == 0){
			return -1;
		}
		String[] scaleArray = scale.split(",");
		int width, height;
		for(int i=0; i<scaleArray.length; i++){
			int bufferSample = Integer.parseInt(scaleArray[i]);
			SoftReference<Bitmap> softReference = mImageBufferMap.get(name + IMAGE_SCALE_PREFIX + bufferSample);
			if(softReference == null){
				continue;
			}
			Bitmap bitmap = softReference.get();
			if(bitmap == null){
				continue;
			}

			width = bitmap.getWidth() * bufferSample;
			height = bitmap.getHeight() * bufferSample;
			return calculateImageTargetSizeMinimumScale(new float[]{width, height}, targetSize);
		}
		return -1;
	}

	public Bitmap getImageAsync(final String strUrl, final float targetSize, final ThreadPoolExecutor threadPoolExecutor, OnLoadImageListener onLoadImageListenerCase){
		final OnLoadImageListener onLoadImageListener = onLoadImageListenerCase == null ? mOnLoadImageListener : onLoadImageListenerCase;
		if(strUrl == null || strUrl.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onObtainImage(FLAG_FAIL, strUrl, null);
			}
			return null;
		}

		final Handler handlerConnectionRead = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(@NonNull Message msg) {
				if(msg.what == 0){
					onLoadImageListener.onObtainImage(FLAG_FAIL, strUrl, null);
				}else if(msg.what == 1){
					onLoadImageListener.onObtainImage(FLAG_REMOTE, strUrl, (Bitmap) msg.obj);
				}
				return false;
			}
		});
		final Runnable runnableConnectionRead = new Runnable() {

			@Override
			public void run() {
				try {
					byte[] bytes = onLoadImageListener.onHaveToRead(FLAG_REMOTE, strUrl);
					if(bytes == null){
						handlerConnectionRead.sendEmptyMessage(0);
						return;
					}
					InputStream inputStream = new ByteArrayInputStream(bytes);
					int scale = calculateImageTargetSizeMinimumScale(inputStream, targetSize);
					try {
						inputStream.reset();
					} catch (IOException e1) {
						try {
							inputStream.close();
						} catch (Exception ignored) {}
						inputStream = new ByteArrayInputStream(bytes);
						if(mIsPrintException){e1.printStackTrace();}
					}
					Bitmap bitmap = onLoadImageListener.onGenerateImage(inputStream, scale);
					if(bitmap == null){
						handlerConnectionRead.sendEmptyMessage(0);
						return;
					}

					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, strUrl, scale);

					onLoadImageListener.onHaveToWrite(strUrl, bitmap);

					Message msg = Message.obtain();
					msg.what = 1;
					msg.obj = bitmap;
					handlerConnectionRead.sendMessage(msg);
				} catch (Exception e) {
					if(mIsPrintException){e.printStackTrace();}
				}
			}
		};

		Handler handlerNeedConnection = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(@NonNull Message msg) {
				if(threadPoolExecutor == null){
					mThreadPoolExecutor.submit(runnableConnectionRead);
				}else{
					threadPoolExecutor.submit(runnableConnectionRead);
				}
				return false;
			}
		});
		return getImageAsyncLocalOnly(strUrl, targetSize, handlerNeedConnection, onLoadImageListenerCase);
	}

	public Bitmap getImageAsync(String strUrl, float targetSize, OnLoadImageListener onLoadImageListenerCase){
		return getImageAsync(strUrl, targetSize, null, onLoadImageListenerCase);
	}

	public Bitmap getImageAsync(final String strUrl, final float targetSize, final ThreadPoolExecutor threadPoolExecutor){
		return getImageAsync(strUrl, targetSize, threadPoolExecutor, null);
	}

	public Bitmap getImageAsync(String strUrl, float targetSize){
		return getImageAsync(strUrl, targetSize, null, null);
	}

	public Bitmap getImageAsyncLocalOnly(final String imageName, final float targetSize, final Handler handlerNotFound, OnLoadImageListener onLoadImageListenerCase){
		final OnLoadImageListener onLoadImageListener = onLoadImageListenerCase == null ? mOnLoadImageListener : onLoadImageListenerCase;
		if(imageName == null || imageName.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onObtainImage(FLAG_FAIL, imageName, null);
			}
			return null;
		}

		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
		Bitmap bitmap = getBufferBitmap(imageName, getBufferSample(imageName, targetSize));
		if(bitmap != null){
			if(onLoadImageListener != null){
				onLoadImageListener.onObtainImage(FLAG_CACHE, imageName, bitmap);
			}
			return bitmap;
		}
		if(onLoadImageListener == null){
			return null;
		}

		final Handler handlerStorageRead = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(@NonNull Message msg) {
				onLoadImageListener.onObtainImage(FLAG_LOCAL, imageName, (Bitmap) msg.obj);
				return false;
			}
		});
		Thread threadStorageRead = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] bytes = onLoadImageListener.onHaveToRead(FLAG_LOCAL, imageName);
					if(bytes == null){
						handlerNotFound.sendEmptyMessage(0);
						return;
					}
					InputStream inputStream = new ByteArrayInputStream(bytes);
					int scale = calculateImageTargetSizeMinimumScale(inputStream, targetSize);
					try {
						inputStream.reset();
					} catch (IOException e1) {
						try {
							inputStream.close();
						} catch (Exception ignored) {}
						inputStream = new ByteArrayInputStream(bytes);
						if(mIsPrintException){e1.printStackTrace();}
					}
					Bitmap bitmap = onLoadImageListener.onGenerateImage(inputStream, scale);
					if(bitmap == null){
						handlerNotFound.sendEmptyMessage(0);
						return;
					}

					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, imageName, scale);

					Message msg = Message.obtain();
					msg.obj = bitmap;
					handlerStorageRead.sendMessage(msg);
				} catch (Exception e) {
					if(mIsPrintException){e.printStackTrace();}
				}

			}
		});
		threadStorageRead.start();
		return null;
	}

	public Bitmap getImageAsyncLocalOnly(final String strUrl, final float targetSize, final Handler handlerNotFound){
		return getImageAsyncLocalOnly(strUrl, targetSize, handlerNotFound, null);
	}

	public Bitmap getImageAsyncRemoteOnly(final String strUrl, final float targetSize, ThreadPoolExecutor threadPoolExecutor, OnLoadImageListener onLoadImageListenerCase){
		final OnLoadImageListener onLoadImageListener = onLoadImageListenerCase == null ? mOnLoadImageListener : onLoadImageListenerCase;
		if(strUrl == null || strUrl.trim().length() == 0){
			if(onLoadImageListener != null){
				onLoadImageListener.onObtainImage(FLAG_FAIL, strUrl, null);
			}
			return null;
		}

		// 若軟引用內的圖片尚未被GC回收，則直接回傳圖片
		Bitmap bitmap = getBufferBitmap(strUrl, getBufferSample(strUrl, targetSize));
		if(bitmap != null){
			if(onLoadImageListener != null){
				onLoadImageListener.onObtainImage(FLAG_CACHE, strUrl, bitmap);
			}
			return bitmap;
		}
		if(onLoadImageListener == null){
			return null;
		}

		final Handler handlerConnectionRead = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(@NonNull Message msg) {
				if(msg.what == 0){
					onLoadImageListener.onObtainImage(FLAG_FAIL, strUrl, null);
				}else if(msg.what == 1){
					onLoadImageListener.onObtainImage(FLAG_REMOTE, strUrl, (Bitmap) msg.obj);
				}
				return false;
			}
		});
		Runnable runnableConnectionRead = new Runnable() {

			@Override
			public void run() {
				try {
					byte[] bytes = onLoadImageListener.onHaveToRead(FLAG_REMOTE, strUrl);
					if(bytes == null){
						handlerConnectionRead.sendEmptyMessage(0);
						return;
					}
					InputStream inputStream = new ByteArrayInputStream(bytes);
					int scale = calculateImageTargetSizeMinimumScale(inputStream, targetSize);
					try {
						inputStream.reset();
					} catch (IOException e1) {
						try {
							inputStream.close();
						} catch (Exception ignored) {}
						inputStream = new ByteArrayInputStream(bytes);
						if(mIsPrintException){e1.printStackTrace();}
					}
					Bitmap bitmap = onLoadImageListener.onGenerateImage(inputStream, scale);
					if(bitmap == null){
						handlerConnectionRead.sendEmptyMessage(0);
						return;
					}

					// 使用Map暫存已下載的圖片，並透過軟引用保存圖片，GC在系統發生OutOfMemory之前會回收軟引用來釋放記憶體
					setBufferBitmap(bitmap, strUrl, scale);

					Message msg = Message.obtain();
					msg.what = 1;
					msg.obj = bitmap;
					handlerConnectionRead.sendMessage(msg);
				} catch (Exception e) {
					if(mIsPrintException){e.printStackTrace();}
				}

			}
		};

		if(threadPoolExecutor == null){
			mThreadPoolExecutor.submit(runnableConnectionRead);
		}else{
			threadPoolExecutor.submit(runnableConnectionRead);
		}
		return null;
	}

	public Bitmap getImageAsyncRemoteOnly(String strUrl, float targetSize, OnLoadImageListener onLoadImageListenerCase){
		return getImageAsyncRemoteOnly(strUrl, targetSize, null, onLoadImageListenerCase);
	}

	public Bitmap getImageAsyncRemoteOnly(final String strUrl, final float targetSize, final ThreadPoolExecutor threadPoolExecutor){
		return getImageAsyncRemoteOnly(strUrl, targetSize, threadPoolExecutor, null);
	}

	public Bitmap getImageAsyncRemoteOnly(String strUrl, float targetSize){
		return getImageAsyncRemoteOnly(strUrl, targetSize, null, null);
	}

	@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isAvailableByInternet(@NonNull Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){
			return false;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			Network network = connectivityManager.getActiveNetwork();
			if(network == null){
				return false;
			}
			NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
			return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
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

	public static float[] readImageSize(InputStream inputStream){
		// 圖片設定
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 是否讓BitmapFactory.Options只讀取圖片寬高資料而不實際將圖片載入Bitmap
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(inputStream, null, options);

		return new float[]{options.outWidth, options.outHeight};
	}

	public static int calculateImageTargetSizeMinimumScale(float[] imageSize, float targetSize){
		if(targetSize == 0){
			return 1;
		}
		float differenceWidth = Math.abs(imageSize[0] - targetSize);
		float differenceHeight = Math.abs(imageSize[1] - targetSize);
		float scaleMinimum;
		if(differenceWidth < differenceHeight){
			scaleMinimum = imageSize[0] / targetSize;
		}else{
			scaleMinimum = imageSize[1] / targetSize;
		}
		if(scaleMinimum < 1){
			scaleMinimum = 1;
		}
		return Math.round(scaleMinimum + 0.001f);
	}

	public static int calculateImageTargetSizeMinimumScale(InputStream inputStream, float targetSize){
		return calculateImageTargetSizeMinimumScale(readImageSize(inputStream), targetSize);
	}
}