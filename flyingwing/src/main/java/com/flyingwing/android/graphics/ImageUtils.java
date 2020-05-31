/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 4.0.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.graphics;

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
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ImageUtils {

	public static boolean writeBitmapEncode(Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality, OutputStream outputStream){
		boolean isSuccess = bitmap.compress(compressFormat, quality, outputStream);
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public static boolean writeBitmapEncode(Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality, File file){
		try {
			OutputStream outputStream = new FileOutputStream(file);
			return writeBitmapEncode(bitmap, compressFormat, quality, outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeBitmapEncodeToPNG(Bitmap bitmap, int quality, OutputStream outputStream){
		boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public static boolean writeBitmapEncodeToPNG(Bitmap bitmap, int quality, File file){
		try {
			OutputStream outputStream = new FileOutputStream(file);
			return writeBitmapEncodeToPNG(bitmap, quality, outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeBitmapEncodeToJPEG(Bitmap bitmap, int quality, OutputStream outputStream){
		boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public static boolean writeBitmapEncodeToJPEG(Bitmap bitmap, int quality, File file){
		try {
			OutputStream outputStream = new FileOutputStream(file);
			return writeBitmapEncodeToJPEG(bitmap, quality, outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * The file is located at APK [/res/raw/], file cannot exist in subdirectory.
	 * In the past, maximum file size of raw directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readRawBitmap(Resources resources, int resourceId, int inSampleSize, Bitmap.Config config){
		return readBitmapByNative(resources.openRawResource(resourceId), inSampleSize, config);
	}

	/**
	 * The file is located at APK [/res/raw/], file cannot exist in subdirectory.
	 * In the past, maximum file size of raw directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readRawBitmap(Resources resources, int resourceId, int inSampleSize){
		return readBitmapByNative(resources.openRawResource(resourceId), inSampleSize, Bitmap.Config.ARGB_8888);
	}

	/**
	 * The file is located at APK [/res/raw/], file cannot exist in subdirectory.
	 * In the past, maximum file size of raw directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readRawBitmap(Resources resources, int resourceId, float targetSize, Bitmap.Config config){
		int scale = 1;
		try {
			InputStream inputStream = resources.openRawResource(resourceId);
			scale = calculateImageTargetSizeMinimumScale(inputStream, targetSize);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readBitmapByNative(resources.openRawResource(resourceId), scale, config);
	}

	/**
	 * The file is located at APK [/res/raw/], file cannot exist in subdirectory.
	 * In the past, maximum file size of raw directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readRawBitmap(Resources resources, int resourceId, float targetSize){
		return readRawBitmap(resources, resourceId, targetSize, Bitmap.Config.ARGB_8888);
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readAssetsBitmap(Context context, String imageName, int inSampleSize, Bitmap.Config config){
		try {
			InputStream inputStream = context.getApplicationContext().getAssets().open(imageName);
			return readBitmapByNative(inputStream, inSampleSize, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readAssetsBitmap(Context context, String imageName, int inSampleSize){
		return readAssetsBitmap(context, imageName, inSampleSize, Bitmap.Config.ARGB_8888);
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readAssetsBitmap(Context context, String imageName, float targetSize, Bitmap.Config config){
		try {
			AssetManager assetManager = context.getApplicationContext().getAssets();
			InputStream inputStream = assetManager.open(imageName);
			int scale = calculateImageTargetSizeMinimumScale(inputStream, targetSize);
			// AssetInputStream support reset method
			try {
				inputStream.reset();
			} catch (IOException e1) {
				try {
					inputStream.close();
				} catch (Exception ignored) {}
				inputStream = assetManager.open(imageName);
				e1.printStackTrace();
			}
			return readBitmapByNative(inputStream, scale, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The file is located at APK [/assets/], file can exist in subdirectory.
	 * In the past, maximum file size of assets directory was limited to 1MB.
	 */
	public static @Nullable Bitmap readAssetsBitmap(Context context, String imageName, float targetSize){
		return readAssetsBitmap(context, imageName, targetSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readFileBitmap(Context context, File file, int inSampleSize, Bitmap.Config config){
		try {
			InputStream inputStream = new FileInputStream(file);
			return readBitmapByNative(inputStream, inSampleSize, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static @Nullable Bitmap readFileBitmap(Context context, File file, int inSampleSize){
		return readFileBitmap(context, file, inSampleSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readFileBitmap(Context context, File file, float targetSize, Bitmap.Config config){
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			FileChannel fileChannel = fileInputStream.getChannel();
			int scale = calculateImageTargetSizeMinimumScale(fileInputStream, targetSize);
			try {
				fileChannel.position(0);
			} catch (IOException e1) {
				try {
					fileInputStream.close();
				} catch (Exception ignored) {}
				fileInputStream = new FileInputStream(file);
				e1.printStackTrace();
			}
			return readBitmapByNative(fileInputStream, scale, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static @Nullable Bitmap readFileBitmap(Context context, File file, float targetSize){
		return readFileBitmap(context, file, targetSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readFileDescriptorBitmap(Context context, FileDescriptor fileDescriptor, int inSampleSize, Bitmap.Config config){
		InputStream inputStream = new FileInputStream(fileDescriptor);
		return readBitmapByNative(inputStream, inSampleSize, config);
	}

	public static @Nullable Bitmap readFileDescriptorBitmap(Context context, FileDescriptor fileDescriptor, int inSampleSize){
		return readFileDescriptorBitmap(context, fileDescriptor, inSampleSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readFileDescriptorBitmap(Context context, FileDescriptor fileDescriptor, float targetSize, Bitmap.Config config){
		try {
			FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
			FileChannel fileChannel = fileInputStream.getChannel();
			int scale = calculateImageTargetSizeMinimumScale(fileInputStream, targetSize);
			try {
				fileChannel.position(0);
			} catch (IOException e1) {
				try {
					fileInputStream.close();
				} catch (Exception ignored) {}
				fileInputStream = new FileInputStream(fileDescriptor);
				e1.printStackTrace();
			}
			return readBitmapByNative(fileInputStream, scale, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static @Nullable Bitmap readFileDescriptorBitmap(Context context, FileDescriptor fileDescriptor, float targetSize){
		return readFileDescriptorBitmap(context, fileDescriptor, targetSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readBitmapByNative(InputStream inputStream, int inSampleSize, Bitmap.Config config){
		// 圖片設定
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 設定是否讓BitmapFactory.Options只讀取圖片寬高資料而不實際將圖片載入Bitmap
		options.inJustDecodeBounds = false;
		// 設定匯入後圖片的寬高縮小比例，預設1為原始寬高
		options.inSampleSize = inSampleSize;
		// 設定圖片ARGB屬性佔用記憶體空間，預設Bitmap.Config.ARGB_8888為各佔8Bit
		if(config != null){
			options.inPreferredConfig = config;
		}
		// 設定是否讓圖片內容允許變動
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			options.inMutable = true;
		}
		// 直接把不使用的記憶體歸給JVM，回收動作不佔用JVM的記憶體
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			try {
				//noinspection JavaReflectionMemberAccess
				BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(options, true);
			} catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException ignored) {}
		}
		// 設定是否系統記憶體不足時先行回收部分的記憶體，但回收動作仍會佔用JVM的記憶體
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
			options.inPurgeable = true;
			options.inInputShareable = true;
		}
		Bitmap bitmap;
		try {
			// BitmapFactory.decodeStream調用JNI Function創建圖片，避開Java層createBitmap的記憶體佔用
			bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		} catch (OutOfMemoryError e) {
			bitmap = null;
			e.printStackTrace();
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (Exception ignored) {}
			}
		}
		return bitmap;
	}

	public static @Nullable Bitmap readBitmapByNative(InputStream inputStream, int inSampleSize){
		return readBitmapByNative(inputStream, inSampleSize, Bitmap.Config.ARGB_8888);
	}

	public static @Nullable Bitmap readBitmapByNative(InputStream inputStream, Bitmap.Config config){
		return readBitmapByNative(inputStream, 1, config);
	}

	public static @Nullable Bitmap readBitmapByNative(InputStream inputStream){
		return readBitmapByNative(inputStream, 1, Bitmap.Config.ARGB_8888);
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
				size = pixelByte * (long) bufferWidth * (long) bufferHeight;
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
					 * Bitmap.createBitmap(source, x, y, width, height, matrix, filter);
					 * Bitmap.createBitmap(source, x, y, width, height)
					 * Bitmap.createBitmap(src)
					 */
					bitmap = Bitmap.createBitmap(newWidth, newHeight, config);
					mappedByteBuffer.position(0);
					bitmap.copyPixelsFromBuffer(mappedByteBuffer);
				}
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
					System.out.println("Delete file failed, path:\n" + tempFile.getPath());
				}
			}
		}
		return bitmap;
	}

	public static Bitmap convertMappedBitmap(Bitmap bitmap, int newWidth, int newHeight, File tempFile){
		Bitmap.Config config = bitmap.getConfig();
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}

	public static Bitmap convertMappedBitmapUseInsidePrivate(Context context, Bitmap bitmap, int newWidth, int newHeight, Bitmap.Config config){
		File tempFile = new File(context.getApplicationContext().getFilesDir().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}

	public static Bitmap convertMappedBitmapUseInsidePrivate(Context context, Bitmap bitmap, int newWidth, int newHeight){
		Bitmap.Config config = bitmap.getConfig();
		File tempFile = new File(context.getApplicationContext().getFilesDir().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}

	public static Bitmap convertMappedBitmapUseExternalStorage(Bitmap bitmap, int newWidth, int newHeight, Bitmap.Config config){
		File tempFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}

	public static Bitmap convertMappedBitmapUseExternalStorage(Bitmap bitmap, int newWidth, int newHeight){
		Bitmap.Config config = bitmap.getConfig();
		File tempFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "temp.tmp");
		return convertMappedBitmap(bitmap, newWidth, newHeight, config, tempFile);
	}

	public static Canvas createCanvas(Bitmap bitmap){
		Canvas canvas = new Canvas(bitmap);
		// 設定抗鋸齒、濾波處理、抖動處理
		PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.setDrawFilter(paintFlagsDrawFilter);
		return canvas;
	}

	public static Paint createPaint(float strokeWidth){
		// 設定抗鋸齒、濾波處理、抖動處理
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		paint.setStrokeWidth(strokeWidth);
		// 設定Paint抗鋸齒
//		paint.setAntiAlias(true);
		// 設定Paint濾波處理
//		paint.setFilterBitmap(true);
		// 設定Paint抖動處理
//		paint.setDither(true);
		return paint;
	}

	public static Paint createPaint(float strokeWidth, int alpha, int red, int green, int blue){
		Paint paint = createPaint(strokeWidth);
		paint.setARGB(alpha, red, green, blue);
		return paint;
	}

	public static Paint createClearPaint(int strokeWidth){
		Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		clearPaint.setAntiAlias(true);
		clearPaint.setStrokeWidth(strokeWidth);
		clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		return clearPaint;
	}

	public static ColorMatrix createColorMatrix(float baseRed, float baseGreen, float baseBlue, float baseAlpha
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
		paint.setColorFilter(new ColorMatrixColorFilter(createColorMatrix(baseRed, baseGreen, baseBlue, baseAlpha, offsetRed, offsetGreen, offsetBlue, offsetAlpha)));
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
	public static ColorMatrix createBrightnessRelativeOffset(float brightnessRelativeOffset){
		return createColorMatrix(1.0f, 1.0f, 1.0f, 1.0f, brightnessRelativeOffset, brightnessRelativeOffset
				, brightnessRelativeOffset, 0);
	}

	/**
	 * 設定顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix createBrightnessRelativeScale(float brightnessRelativeScale){
		// colorMatrix.setScale(brightnessRelativeScale, brightnessRelativeScale, brightnessRelativeScale, 1.0f);
		return createColorMatrix(brightnessRelativeScale, brightnessRelativeScale, brightnessRelativeScale, 1.0f, 0, 0, 0
				, 0);
	}

	/**
	 * 設定顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix createContrastRelativeScale(float contrastRelativeScale){
		final float offset = 127.5f * (1.0f - contrastRelativeScale);
		return createColorMatrix(contrastRelativeScale, contrastRelativeScale, contrastRelativeScale, 1.0f, offset, offset, offset, 0);
	}

	/**
	 * 設定顏色飽和相對倍數
	 * saturationRelativeScale = 1.0 為原色
	 */
	public static ColorMatrix createSaturationRelativeScale(float saturationRelativeScale){
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
	public static ColorMatrix createHueRelativeRotate(@FloatRange(from = 0.0, to = 360.0) float hueDegreesRelativeRotateOffset){
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setRotate(0, hueDegreesRelativeRotateOffset);
		colorMatrix.setRotate(1, hueDegreesRelativeRotateOffset);
		colorMatrix.setRotate(2, hueDegreesRelativeRotateOffset);
		return colorMatrix;
	}

	/**
	 * 設定顏色反相（負片、互補色）
	 */
	public static ColorMatrix createInverting(){
		return createColorMatrix(-1.0f, -1.0f, -1.0f, 1.0f, 255, 255, 255, 0);
	}

	/**
	 * 設定畫筆顏色亮度相對偏移量
	 */
	public static void setPaintBrightnessRelativeOffset(Paint paint, float brightnessRelativeOffset){
		paint.setColorFilter(new ColorMatrixColorFilter(createBrightnessRelativeOffset(brightnessRelativeOffset)));
	}

	/**
	 * 設定畫筆顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static void setPaintBrightnessRelativeScale(Paint paint, float brightnessRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(createBrightnessRelativeScale(brightnessRelativeScale)));
	}

	/**
	 * 設定畫筆顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static void setPaintContrastRelativeScale(Paint paint, float contrastRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(createContrastRelativeScale(contrastRelativeScale)));
	}

	/**
	 * 設定畫筆顏色飽和相對倍數
	 * saturationRelativeScale = 1.0 為原色
	 */
	public static void setPaintSaturationRelativeScale(Paint paint, float saturationRelativeScale){
		paint.setColorFilter(new ColorMatrixColorFilter(createSaturationRelativeScale(saturationRelativeScale)));
	}

	/**
	 * 設定畫筆顏色色相（色調）旋轉相對偏移量
	 * hueDegreesRelativeRotateOffset = 0 or 360 為原色
	 */
	public static void setPaintHueRelativeRotate(Paint paint, float hueDegreesRelativeRotateOffset){
		paint.setColorFilter(new ColorMatrixColorFilter(createHueRelativeRotate(hueDegreesRelativeRotateOffset)));
	}

	/**
	 * 設定畫筆顏色反相（負片、互補色）
	 */
	public static void setPaintInverting(Paint paint){
		paint.setColorFilter(new ColorMatrixColorFilter(createInverting()));
	}

	/**
	 * 設定圖片顏色亮度相對偏移量
	 */
	public static Bitmap drawBitmapBrightnessRelativeOffset(Bitmap bitmap, float brightnessRelativeOffset){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createBrightnessRelativeOffset(brightnessRelativeOffset)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片顏色亮度相對倍數
	 * brightnessRelativeScale = 1.0 為原色
	 */
	public static Bitmap drawBitmapBrightnessRelativeScale(Bitmap bitmap, float brightnessRelativeScale){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createBrightnessRelativeScale(brightnessRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片顏色對比相對倍數
	 * contrastRelativeScale = 1.0 為原色
	 */
	public static Bitmap drawBitmapContrastRelativeScale(Bitmap bitmap, float contrastRelativeScale){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createContrastRelativeScale(contrastRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片顏色飽和相對倍數<br>
	 * saturationRelativeScale = 1.0 為原色
	 * saturationRelativeScale =0 為灰階效果
	 */
	public static Bitmap drawBitmapSaturationRelativeScale(Bitmap bitmap, float saturationRelativeScale){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createSaturationRelativeScale(saturationRelativeScale)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片顏色色相（色調）旋轉相對偏移量
	 * hueDegreesRelativeRotateOffset = 0 or 360 為原色
	 */
	public static Bitmap drawBitmapHueRelativeRotate(Bitmap bitmap, float hueDegreesRelativeRotateOffset){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createHueRelativeRotate(hueDegreesRelativeRotateOffset)));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片顏色反相（負片、互補色）
	 */
	public static Bitmap drawBitmapInverting(Bitmap bitmap){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);
		paint.setColorFilter(new ColorMatrixColorFilter(createInverting()));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片浮雕效果
	 */
	public static Bitmap drawBitmapEmboss(Bitmap bitmap){
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);

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
		Canvas canvas = createCanvas(bitmap);
		Paint paint = createPaint(1);

		// 設定模糊效果
		BlurMaskFilter blur = new BlurMaskFilter(radius, style);
		paint.setMaskFilter(blur);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bitmap;
	}

	/**
	 * 設定圖片高斯模糊效果
	 * @param radius from=0.0, to=25.0
	 */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Bitmap drawBitmapGaussianBlur(Context context, Bitmap bitmap, @FloatRange(from = 0.0, to = 25.0) float radius){
		RenderScript renderScript = RenderScript.create(context.getApplicationContext());
		Allocation allocationInput = Allocation.createFromBitmap(renderScript, bitmap);
		Allocation allocationOutput = Allocation.createTyped(renderScript, allocationInput.getType());

		ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
		// max 25.0f
		scriptIntrinsicBlur.setRadius(radius);
		scriptIntrinsicBlur.setInput(allocationInput);
		scriptIntrinsicBlur.forEach(allocationOutput);

		allocationOutput.copyTo(bitmap);

		scriptIntrinsicBlur.destroy();
		allocationOutput.destroy();
		allocationInput.destroy();
		renderScript.destroy();
		return bitmap;
	}

	public static byte[] bitmapToBytes(Bitmap bitmap){
		ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		bitmap.copyPixelsToBuffer(byteBuffer);
		return byteBuffer.array();
	}

	public static byte[] bitmapToBytes(Bitmap bitmap, File tempFile){
		if(tempFile == null){
			return bitmapToBytes(bitmap);
		}
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
			FileChannel fileChannel = randomAccessFile.getChannel();
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, bitmap.getRowBytes() * bitmap.getHeight());
			bitmap.copyPixelsToBuffer(mappedByteBuffer);
			bitmap.recycle();
			System.gc();
			return mappedByteBuffer.array();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap bytesToBitmap(byte[] bytes){
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	public static Bitmap bytesToBitmap(byte[] bytes, BitmapFactory.Options options){
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	}

	public static Drawable bitmapToDrawable(Resources resources, Bitmap bitmap){
		return new BitmapDrawable(resources, bitmap);
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
		}else if(drawable.getOpacity() == PixelFormat.OPAQUE){
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

	public static Drawable getStateListDrawable(int[][] ints, Drawable[] drawables){
		StateListDrawable stateListDrawable = new StateListDrawable();
		for(int i=0; i<ints.length; i++){
			stateListDrawable.addState(ints[i], drawables[i]);
		}
		return stateListDrawable;
	}

	public static Drawable getStateListDrawable(Context context, int[][] ints, int[] resourceIds){
		StateListDrawable stateListDrawable = new StateListDrawable();
		for(int i=0; i<ints.length; i++){
			stateListDrawable.addState(ints[i], ContextCompat.getDrawable(context.getApplicationContext(), resourceIds[i]));
		}
		return stateListDrawable;
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, int left, int top, int right, int bottom, Bitmap.Config config, float roundRadius){
		Bitmap bitmapOutput = Bitmap.createBitmap(right - left, bottom - top, config == null ? bitmap.getConfig() : config);
		Canvas canvas = createCanvas(bitmapOutput);
		Paint paint = createPaint(1);

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
		return drawBitmapRoundRect(bitmap, 0, 0, width, height, null, roundRadius);
	}

	public static Bitmap drawBitmapRoundRect(Bitmap bitmap, float roundRadius){
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, roundRadius);
	}

	public static Drawable drawDrawableRoundRect(Resources resources, Drawable drawable, int width, int height, Bitmap.Config config, float roundRadius){
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(resources, drawBitmapRoundRect(bitmap, 0, 0, width, height, config, roundRadius));
	}

	public static Drawable drawDrawableRoundRect(Resources resources, Drawable drawable, int width, int height, float roundRadius){
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(resources, drawBitmapRoundRect(bitmap, 0, 0, width, height, null, roundRadius));
	}

	public static Drawable drawDrawableRoundRect(Resources resources, Drawable drawable, float roundRadius){
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(resources, drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, roundRadius));
	}

	public static @Nullable Bitmap drawResourceBitmapRoundRect(Resources resources, int resourceId, int inSampleSize, Bitmap.Config config, float roundRadius){
		Bitmap bitmap = readBitmapByNative(resources.openRawResource(resourceId), inSampleSize, config);
		if(bitmap == null){
			return null;
		}
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), config, roundRadius);
	}

	public static @Nullable Bitmap drawResourceBitmapRoundRect(Resources resources, int resourceId, int inSampleSize, float roundRadius){
		Bitmap bitmap = readBitmapByNative(resources.openRawResource(resourceId), inSampleSize, Bitmap.Config.ARGB_8888);
		if(bitmap == null){
			return null;
		}
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, roundRadius);
	}

	public static @Nullable Bitmap drawResourceBitmapRoundRect(Resources resources, int resourceId, float targetSize, float roundRadius){
		int scale = calculateImageTargetSizeMinimumScale(resources.openRawResource(resourceId), targetSize);
		Bitmap bitmap = readBitmapByNative(resources.openRawResource(resourceId), scale, Bitmap.Config.ARGB_8888);
		if(bitmap == null){
			return null;
		}
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, roundRadius);
	}

	public static @Nullable Bitmap drawResourceDrawableToBitmapRoundRect(Context context, int resourceId, int width, int height, Bitmap.Config config, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(), resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return drawBitmapRoundRect(bitmap, 0, 0, width, height, config, roundRadius);
	}

	public static @Nullable Bitmap drawResourceDrawableToBitmapRoundRect(Context context, int resourceId, Bitmap.Config config, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(), resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), config, roundRadius);
	}

	public static @Nullable Bitmap drawResourceDrawableToBitmapRoundRect(Context context, int resourceId, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(), resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, roundRadius);
	}

	public static @Nullable Drawable drawResourceDrawableRoundRect(Context context, int resourceId, int width, int height, Bitmap.Config config, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(), resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(context.getApplicationContext().getResources(), drawBitmapRoundRect(bitmap, 0, 0, width, height, config, roundRadius));
	}

	public static @Nullable Drawable drawResourceDrawableRoundRect(Context context, int resourceId, Bitmap.Config config, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context, resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(context.getApplicationContext().getResources(), drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight()
				, config, roundRadius));
	}

	public static @Nullable Drawable drawResourceDrawableRoundRect(Context context, int resourceId, float roundRadius){
		Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(), resourceId);
		if(drawable == null){
			return null;
		}
		Bitmap bitmap = drawableToBitmap(drawable, true);
		return new BitmapDrawable(context.getApplicationContext().getResources(), drawBitmapRoundRect(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight()
				, null, roundRadius));
	}
}