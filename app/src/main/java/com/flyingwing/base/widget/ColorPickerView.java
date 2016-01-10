/*
 * Copyright 2016 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

@SuppressWarnings("unused")
public class ColorPickerView extends View {

	public static final int DISPLAY_FLAG_HUE_SINGLE = 1;
	public static final int DISPLAY_FLAG_SATURATION_SINGLE = 2;
	public static final int DISPLAY_FLAG_VALUE_SINGLE = 3;
	public static final int DISPLAY_FLAG_HUE_MIX_SATURATION = 4;

	public static final int HORIZONTAL_LEFT_TO_RIGHT = 0;
	public static final int HORIZONTAL_RIGHT_TO_LEFT = 1;
	public static final int VERTICAL_TOP_TO_BOTTOM = 2;
	public static final int VERTICAL_BOTTOM_TO_TOP = 3;
	public static final int RING = 4;

	private final int[] COLOR_AXIS = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};

	private Paint mPaint;
	private int mDisplayFlag;
	private int mOrientation;
	private float mNowX, mNowY;
	private RectF mRectF;
	private OnHSVChangeListener mOnHSVChangeListener;
	private Bitmap mMarkBitmap;
	private float[] mHSV = new float[3];

	public interface OnHSVChangeListener {
		void onHueChange(float hueRing, int hueRGB);
		void onSaturationChange(float saturation);
		void onBrightnessChange(float brightness);
	}

	public ColorPickerView(Context context, int displayFlag, int orientation) {
		super(context);
		mDisplayFlag = displayFlag;
		mOrientation = orientation;
		if(mOrientation == RING && mRectF == null){
			mRectF = new RectF();
		}
		mPaint = new Paint(new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
		mPaint.setStrokeWidth(1);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// buildHueMixSaturationLayerForLinear() use ComposeShader must setting this option
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				switch (mDisplayFlag) {
					case DISPLAY_FLAG_HUE_SINGLE:
						if (mOrientation == HORIZONTAL_LEFT_TO_RIGHT) {
							buildHueLayerForHorizontalLeftToRight();
						} else if (mOrientation == HORIZONTAL_RIGHT_TO_LEFT) {
							buildHueLayerForHorizontalRightToLeft();
						} else if (mOrientation == VERTICAL_TOP_TO_BOTTOM) {
							buildHueLayerForVerticalTopToBottom();
						} else if (mOrientation == VERTICAL_BOTTOM_TO_TOP) {
							buildHueLayerForVerticalBottomToTop();
						} else if (mOrientation == RING) {
							buildHueLayerForSweep(getWidth() / 2, getHeight() / 2);
						}
						break;
					case DISPLAY_FLAG_SATURATION_SINGLE:
						if (mOrientation == HORIZONTAL_LEFT_TO_RIGHT) {
							buildSaturationLayerForHorizontalLeftToRight();
						} else if (mOrientation == HORIZONTAL_RIGHT_TO_LEFT) {
							buildSaturationLayerForHorizontalRightToLeft();
						} else if (mOrientation == VERTICAL_TOP_TO_BOTTOM) {
							buildSaturationLayerForVerticalTopToBottom();
						} else if (mOrientation == VERTICAL_BOTTOM_TO_TOP) {
							buildSaturationLayerForVerticalBottomToTop();
						} else if (mOrientation == RING) {
							buildSaturationLayerForSweep(getWidth() / 2, getHeight() / 2);
						}
						break;
					case DISPLAY_FLAG_VALUE_SINGLE:
						if (mOrientation == HORIZONTAL_LEFT_TO_RIGHT) {
							buildValueLayerForHorizontalLeftToRight();
						} else if (mOrientation == HORIZONTAL_RIGHT_TO_LEFT) {
							buildValueLayerForHorizontalRightToLeft();
						} else if (mOrientation == VERTICAL_TOP_TO_BOTTOM) {
							buildValueLayerForVerticalTopToBottom();
						} else if (mOrientation == VERTICAL_BOTTOM_TO_TOP) {
							buildValueLayerForVerticalBottomToTop();
						} else if (mOrientation == RING) {
							buildValueLayerForSweep(getWidth() / 2, getHeight() / 2);
						}
						break;
					case DISPLAY_FLAG_HUE_MIX_SATURATION:
					default:
						buildHueMixSaturationLayerForLinear();
						break;
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					//noinspection deprecation
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

	public ColorPickerView(Context context, int displayFlag) {
		this(context, displayFlag, 0);
	}

	public ColorPickerView(Context context) {
		this(context, 0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mOrientation == RING){
			mRectF.left = 0;
			mRectF.top = 0;
			mRectF.right = canvas.getWidth();
			mRectF.bottom = canvas.getHeight();
			canvas.drawArc(mRectF, 0, 360, false, mPaint);
		}else{
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaint);
		}
		if(mMarkBitmap != null){
			canvas.drawBitmap(mMarkBitmap, mNowX, mNowY, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if(mNowX != event.getX() || mNowY != event.getY()){
					mNowX = event.getX();
					mNowY = event.getY();
					invalidate();
				}
				setTouchValue(event);
				break;
		}
		super.onTouchEvent(event);
		return true;
	}

	public void setDisplayFlag(int displayFlag){
		mDisplayFlag = displayFlag;
	}

	public void setOrientation(int orientation){
		mOrientation = orientation;
		if(mOrientation == RING && mRectF == null){
			mRectF = new RectF();
		}
	}

	private void changeHueRingResetShader(){
		if(mDisplayFlag == DISPLAY_FLAG_SATURATION_SINGLE){
			if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
				buildSaturationLayerForHorizontalLeftToRight();
			}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
				buildSaturationLayerForHorizontalRightToLeft();
			}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
				buildSaturationLayerForVerticalTopToBottom();
			}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
				buildSaturationLayerForVerticalBottomToTop();
			}else if(mOrientation == RING){
				buildSaturationLayerForSweep(getWidth() / 2, getHeight() / 2);
			}
			invalidate();
		}else if(mDisplayFlag == DISPLAY_FLAG_VALUE_SINGLE){
			if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
				buildValueLayerForHorizontalLeftToRight();
			}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
				buildValueLayerForHorizontalRightToLeft();
			}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
				buildValueLayerForVerticalTopToBottom();
			}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
				buildValueLayerForVerticalBottomToTop();
			}else if(mOrientation == RING){
				buildValueLayerForSweep(getWidth() / 2, getHeight() / 2);
			}
			invalidate();
		}
	}

	public void setHSV(@NonNull @Size(3) float[] hsv){
		mHSV = hsv;
	}

	public void changeHSV(@NonNull @Size(3) float[] hsv){
		mHSV = hsv;
		changeHueRingResetShader();
	}

	public float[] getHSV(){
		return mHSV;
	}

	public void setHSVFromRGBColor(int rgbColor){
		Color.colorToHSV(rgbColor, mHSV);
	}

	public void changeHSVFromRGBColor(int rgbColor){
		Color.colorToHSV(rgbColor, mHSV);
		changeHueRingResetShader();
	}

	public void setHueRing(@FloatRange(from=0.0, to=360.0) float hueRing){
		mHSV[0] = hueRing;
	}

	public void changeHueRing(@FloatRange(from=0.0, to=360.0) float hueRing){
		mHSV[0] = hueRing;
		changeHueRingResetShader();
	}

	public void setHueRingFromRGBColor(int rgbColor){
		mHSV[0] = hueRGBToHueRing(rgbColor);
	}

	public void changeHueRingFromRGBColor(int rgbColor){
		mHSV[0] = hueRGBToHueRing(rgbColor);
		changeHueRingResetShader();
	}

	public float getHueRing(){
		return mHSV[0];
	}

	public int getHueRGB(){
		return Color.HSVToColor(new float[]{mHSV[0], 1f, 1f});
	}

	public void setSaturation(@FloatRange(from=0.0, to=1.0) float saturation){
		mHSV[1] = saturation;
	}

	public void changeSaturation(@FloatRange(from=0.0, to=1.0) float saturation){
		mHSV[1] = saturation;
		if(mDisplayFlag == DISPLAY_FLAG_VALUE_SINGLE){
			if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
				buildValueLayerForHorizontalLeftToRight();
			}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
				buildValueLayerForHorizontalRightToLeft();
			}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
				buildValueLayerForVerticalTopToBottom();
			}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
				buildValueLayerForVerticalBottomToTop();
			} else if (mOrientation == RING){
				buildValueLayerForSweep(getWidth() / 2, getHeight() / 2);
			}
			invalidate();
		}
	}

	public float getSaturation(){
		return mHSV[1];
	}

	public int getSaturationToInt(){
		return (int)Math.ceil(mHSV[1] * 255);
	}

	public void setBrightness(@FloatRange(from=0.0, to=1.0) float brightness){
		mHSV[2] = brightness;
	}

	public float getBrightness(){
		return mHSV[2];
	}

	public int getBrightnessToInt(){
		return (int)Math.ceil(mHSV[2] * 255);
	}

	private void buildHueLayerForLinear(float x0, float y0, float x1, float y1){
		Shader shader = new LinearGradient(x0, y0, x1, y1, COLOR_AXIS, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	private void buildHueLayerForHorizontalLeftToRight(){
		buildHueLayerForLinear(0, 0, getWidth(), 0);
	}

	private void buildHueLayerForHorizontalRightToLeft(){
		buildHueLayerForLinear(getWidth(), 0, 0, 0);
	}

	private void buildHueLayerForVerticalTopToBottom(){
		buildHueLayerForLinear(0, 0, 0, getHeight());
	}

	private void buildHueLayerForVerticalBottomToTop(){
		buildHueLayerForLinear(0, getHeight(), 0, 0);
	}

	private void buildHueLayerForSweep(float x, float y){
		Shader shader = new SweepGradient(x, y, COLOR_AXIS, null);
		mPaint.setShader(shader);
	}

	private void buildSaturationLayerForLinear(float x0, float y0, float x1, float y1){
		int[] saturationAxis = new int[]{0xFF000000, hueRingToHueRGB(mHSV[0])};
		Shader shader = new LinearGradient(x0, y0, x1, y1, saturationAxis, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	private void buildSaturationLayerForHorizontalLeftToRight(){
		buildSaturationLayerForLinear(0, 0, getWidth(), 0);
	}

	private void buildSaturationLayerForHorizontalRightToLeft(){
		buildSaturationLayerForLinear(getWidth(), 0, 0, 0);
	}

	private void buildSaturationLayerForVerticalTopToBottom(){
		buildSaturationLayerForLinear(0, 0, 0, getHeight());
	}

	private void buildSaturationLayerForVerticalBottomToTop() {
		buildSaturationLayerForLinear(0, getHeight(), 0, 0);
	}

	private void buildSaturationLayerForSweep(float x, float y){
		int[] saturationAxis = new int[]{0xFF000000, hueRingToHueRGB(mHSV[0])};
		Shader shader = new SweepGradient(x, y, saturationAxis, null);
		mPaint.setShader(shader);
	}

	private void buildValueLayerForLinear(float x0, float y0, float x1, float y1){
		int[] valueAxis = new int[]{0xFF000000, Color.HSVToColor(new float[]{mHSV[0], mHSV[1], 1f})};
		Shader shader = new LinearGradient(x0, y0, x1, y1, valueAxis, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	private void buildValueLayerForHorizontalLeftToRight(){
		buildValueLayerForLinear(0, 0, getWidth(), 0);
	}

	private void buildValueLayerForHorizontalRightToLeft(){
		buildValueLayerForLinear(getWidth(), 0, 0, 0);
	}

	private void buildValueLayerForVerticalTopToBottom(){
		buildValueLayerForLinear(0, 0, 0, getHeight());
	}

	private void buildValueLayerForVerticalBottomToTop() {
		buildValueLayerForLinear(0, getHeight(), 0, 0);
	}

	private void buildValueLayerForSweep(float x, float y){
		int[] valueAxis = new int[]{0xFF000000, Color.HSVToColor(new float[]{mHSV[0], mHSV[1], 1f})};
		Shader shader = new SweepGradient(x, y, valueAxis, null);
		mPaint.setShader(shader);
	}

	private void buildHueMixSaturationLayerForLinear(){
		int[] saturationCompositeHueAxis = new int[]{0xFF000000, 0xFFFFFFFF};
		Shader shader = new ComposeShader(new LinearGradient(0, 0, getWidth(), 0, COLOR_AXIS, null, Shader.TileMode.CLAMP)
				, new LinearGradient(0, getHeight(), 0, 0, saturationCompositeHueAxis, null, Shader.TileMode.CLAMP), PorterDuff.Mode.MULTIPLY);
		mPaint.setShader(shader);
	}

	public void setOnHSVChangeListener(OnHSVChangeListener onOnHSVChangeListener){
		mOnHSVChangeListener = onOnHSVChangeListener;
	}
	
	public void setMarkBitmap(Bitmap bitmap){
		mMarkBitmap = bitmap;
	}

	private void setTouchValue(MotionEvent event){
		switch (mDisplayFlag) {
			case DISPLAY_FLAG_HUE_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[0] = event.getX() / getWidth() * 360f;
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[0] = (1 - event.getX() / getWidth()) * 360f;
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[0] = event.getY() / getHeight() * 360f;
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[0] = (1 - event.getY() / getHeight()) * 360f;
				}else if(mOrientation == RING){
					mHSV[0] = angle(getWidth() / 2, getHeight() / 2, event.getX(), event.getY());
				}
				mOnHSVChangeListener.onHueChange(mHSV[0], getHueRGB());
				break;
			case DISPLAY_FLAG_SATURATION_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[1] = event.getX() / getWidth();
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[1] = 1 - event.getX() / getWidth();
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[1] = event.getY() / getHeight();
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[1] = 1 - event.getY() / getHeight();
				}else if(mOrientation == RING){
					mHSV[1] = angle(getWidth() / 2, getHeight() / 2, event.getX(), event.getY()) / 360f;
				}
				mOnHSVChangeListener.onSaturationChange(mHSV[1]);
				break;
			case DISPLAY_FLAG_VALUE_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[2] = event.getX() / getWidth();
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[2] = 1 - event.getX() / getWidth();
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[2] = event.getY() / getHeight();
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[2] = 1 - event.getY() / getHeight();
				}else if(mOrientation == RING){
					mHSV[2] = angle(getWidth() / 2, getHeight() / 2, event.getX(), event.getY()) / 360f;
				}
				mOnHSVChangeListener.onBrightnessChange(mHSV[2]);
				break;
			case DISPLAY_FLAG_HUE_MIX_SATURATION:
			default:
				mHSV[0] = event.getX() / getWidth() * 360f;
				mHSV[1] = event.getY() / getHeight();
				mOnHSVChangeListener.onHueChange(mHSV[0], getHueRGB());
				mOnHSVChangeListener.onSaturationChange(mHSV[1]);
				break;
		}
	}

	public static int hueRingToHueRGB(float hueRing){
		float[] hsv = new float[]{hueRing, 1f, 1f};
		return Color.HSVToColor(hsv);
	}

	public static float hueRGBToHueRing(int hueRGB){
		float[] hsv = new float[3];
		Color.colorToHSV(hueRGB, hsv);
		return hsv[0];
	}

	public static int saturationFloatToInt(float saturationFloat){
		return (int)Math.ceil(saturationFloat * 255);
	}

	public static float saturationIntToFloat(int saturationInt){
		return Math.round(saturationInt / 255f * 100) / 100f;
	}

	public static int brightnessFloatToInt(float brightnessFloat){
		return (int)Math.ceil(brightnessFloat * 255);
	}

	public static float brightnessIntToFloat(int brightnessInt){
		return Math.round(brightnessInt / 255f * 100) / 100f;
	}

	public static float angle(float x0, float y0, float x1, float y1) {
		float x = Math.abs(x0 - x1);
		float y = Math.abs(y0 - y1);
		float z = (float)Math.sqrt(x * x + y * y);
		return (float)(Math.asin(y / z) / Math.PI * 180);
	}
}