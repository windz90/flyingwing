/*
 * Copyright 2016 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
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

	public static final int HORIZONTAL_LEFT_TO_RIGHT = 1;
	public static final int HORIZONTAL_RIGHT_TO_LEFT = 2;
	public static final int VERTICAL_TOP_TO_BOTTOM = 4;
	public static final int VERTICAL_BOTTOM_TO_TOP = 8;
	public static final int RING = 16;

	private final int[] HUE_AXIS = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
	private final int[] WHITE_BLACK_AXIS = new int[]{0xFFFFFFFF, 0xFF000000};

	private Paint mPaint;
	private int mDisplayFlag;
	private int mOrientation;
	private float[] mHSV = new float[3];
	private float mTouchX, mTouchY;
	private RectF mRectF;
	private OnHSVChangeListener mOnHSVChangeListener;
	private Drawable mMarkDrawable;

	public interface OnHSVChangeListener {
		void onHueChange(float hueRing, int hueRGB, boolean isChanged);
		void onSaturationChange(float saturation, boolean isChanged);
		void onBrightnessChange(float brightness, boolean isChanged);
	}

	public ColorPickerView(Context context, int displayFlag, int orientation) {
		super(context);
		mDisplayFlag = displayFlag;
		mOrientation = orientation;
		if(mOrientation == RING && mRectF == null){
			mRectF = new RectF();
		}
		mPaint = new Paint(new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// buildHueMixSaturationLayerForLinear() use ComposeShader must setting this option
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				buildHSVLayer();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					//noinspection deprecation
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

	public ColorPickerView(Context context) {
		this(context, DISPLAY_FLAG_HUE_MIX_SATURATION, HORIZONTAL_LEFT_TO_RIGHT | VERTICAL_BOTTOM_TO_TOP);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mOrientation == RING){
			int radius = (int)mPaint.getStrokeWidth() >> 1;
			mRectF.left = radius;
			mRectF.top = radius;
			mRectF.right = getWidth() - radius;
			mRectF.bottom = getHeight() - radius;
			mPaint.setStyle(Paint.Style.STROKE);
			canvas.drawArc(mRectF, 0, 360, false, mPaint);
		}else{
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
		}
		if(mMarkDrawable != null){
			int radiusWi = mMarkDrawable.getIntrinsicWidth() >> 1;
			int radiusHe = mMarkDrawable.getIntrinsicHeight() >> 1;
			mMarkDrawable.setBounds((int) mTouchX - radiusWi, (int) mTouchY - radiusHe, (int) mTouchX + radiusWi, (int) mTouchY + radiusHe);
			mMarkDrawable.draw(canvas);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				if(mTouchX != event.getX() || mTouchY != event.getY()){
					mTouchX = event.getX();
					mTouchY = event.getY();
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

	public int getDisplayFlag(){
		return mDisplayFlag;
	}

	public void setOrientation(int orientation){
		mOrientation = orientation;
		if(mOrientation == RING && mRectF == null){
			mRectF = new RectF();
		}
	}

	public int getOrientation(){
		return mOrientation;
	}

	protected void changeHueRingResetShader(){
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
				buildSaturationLayerForSweep(getWidth() >> 1, getHeight() >> 1);
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
				buildValueLayerForSweep(getWidth() >> 1, getHeight() >> 1);
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
				buildValueLayerForSweep(getWidth() >> 1, getHeight() >> 1);
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

	public void setPaint(@NonNull Paint paint){
		mPaint = paint;
	}

	public Paint getPaint(){
		return mPaint;
	}

	public float getTouchX(){
		return mTouchX;
	}

	public float getTouchY(){
		return mTouchY;
	}

	protected void buildHueLayerForLinear(float x0, float y0, float x1, float y1){
		Shader shader = new LinearGradient(x0, y0, x1, y1, HUE_AXIS, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	public int[] getHueAxis(){
		return HUE_AXIS;
	}

	private void buildHueLayerForHorizontalLeftToRight(){
		buildHueLayerForLinear(0, 0, getWidth(), 0);
		mTouchX = mHSV[0] / 360f * getWidth();
	}

	private void buildHueLayerForHorizontalRightToLeft(){
		buildHueLayerForLinear(getWidth(), 0, 0, 0);
		mTouchX = (1f - mHSV[0] / 360f) * getWidth();
	}

	private void buildHueLayerForVerticalTopToBottom(){
		buildHueLayerForLinear(0, 0, 0, getHeight());
		mTouchY = mHSV[0] / 360f * getHeight();
	}

	private void buildHueLayerForVerticalBottomToTop(){
		buildHueLayerForLinear(0, getHeight(), 0, 0);
		mTouchY = (1f - mHSV[0] / 360f) * getHeight();
	}

	protected void buildHueLayerForSweep(float x, float y){
		Shader shader = new SweepGradient(x, y, HUE_AXIS, null);
		mPaint.setShader(shader);
	}

	protected void buildSaturationLayerForLinear(float x0, float y0, float x1, float y1){
		int[] saturationAxis = new int[]{0xFF000000, hueRingToHueRGB(mHSV[0])};
		Shader shader = new LinearGradient(x0, y0, x1, y1, saturationAxis, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	private void buildSaturationLayerForHorizontalLeftToRight(){
		buildSaturationLayerForLinear(0, 0, getWidth(), 0);
		mTouchX = mHSV[1] * getWidth();
	}

	private void buildSaturationLayerForHorizontalRightToLeft(){
		buildSaturationLayerForLinear(getWidth(), 0, 0, 0);
		mTouchX = (1f - mHSV[1]) * getWidth();
	}

	private void buildSaturationLayerForVerticalTopToBottom(){
		buildSaturationLayerForLinear(0, 0, 0, getHeight());
		mTouchY = mHSV[1] * getHeight();
	}

	private void buildSaturationLayerForVerticalBottomToTop(){
		buildSaturationLayerForLinear(0, getHeight(), 0, 0);
		mTouchY = (1f - mHSV[1]) * getHeight();
	}

	protected void buildSaturationLayerForSweep(float x, float y){
		int[] saturationAxis = new int[]{0xFF000000, hueRingToHueRGB(mHSV[0])};
		Shader shader = new SweepGradient(x, y, saturationAxis, null);
		mPaint.setShader(shader);
	}

	protected void buildValueLayerForLinear(float x0, float y0, float x1, float y1){
		int[] valueAxis = new int[]{0xFF000000, Color.HSVToColor(new float[]{mHSV[0], mHSV[1], 1f})};
		Shader shader = new LinearGradient(x0, y0, x1, y1, valueAxis, null, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	private void buildValueLayerForHorizontalLeftToRight(){
		buildValueLayerForLinear(0, 0, getWidth(), 0);
		mTouchX = mHSV[2] * getWidth();
	}

	private void buildValueLayerForHorizontalRightToLeft(){
		buildValueLayerForLinear(getWidth(), 0, 0, 0);
		mTouchX = (1f - mHSV[2]) * getWidth();
	}

	private void buildValueLayerForVerticalTopToBottom(){
		buildValueLayerForLinear(0, 0, 0, getHeight());
		mTouchY = mHSV[2] * getHeight();
	}

	private void buildValueLayerForVerticalBottomToTop(){
		buildValueLayerForLinear(0, getHeight(), 0, 0);
		mTouchY = (1f - mHSV[2]) * getHeight();
	}

	protected void buildValueLayerForSweep(float x, float y){
		int[] valueAxis = new int[]{0xFF000000, Color.HSVToColor(new float[]{mHSV[0], mHSV[1], 1f})};
		Shader shader = new SweepGradient(x, y, valueAxis, null);
		mPaint.setShader(shader);
	}

	protected void buildHueMixSaturationLayerForLinear(float hueX0, float hueY0, float hueX1, float hueY1, float satX0, float satY0, float satX1, float satY1){
		Shader shader = new ComposeShader(new LinearGradient(hueX0, hueY0, hueX1, hueY1, HUE_AXIS, null, Shader.TileMode.CLAMP)
				, new LinearGradient(satX0, satY0, satX1, satY1, WHITE_BLACK_AXIS, null, Shader.TileMode.CLAMP), PorterDuff.Mode.SCREEN);
		mPaint.setShader(shader);
	}

	private void buildHueMixSaturationLayerForLeftToRightTopToBottom(){
		buildHueMixSaturationLayerForLinear(0, 0, getWidth(), 0, 0, 0, 0, getHeight());
		mTouchX = mHSV[0] / 360f * getWidth();
		mTouchY = mHSV[1] * getHeight();
	}

	private void buildHueMixSaturationLayerForLeftToRightBottomToTop(){
		buildHueMixSaturationLayerForLinear(0, 0, getWidth(), 0, 0, getHeight(), 0, 0);
		mTouchX = mHSV[0] / 360f * getWidth();
		mTouchY = (1f - mHSV[1]) * getHeight();
	}

	private void buildHueMixSaturationLayerForRightToLeftTopToBottom(){
		buildHueMixSaturationLayerForLinear(getWidth(), 0, 0, 0, 0, 0, 0, getHeight());
		mTouchX = (1f - mHSV[0] / 360f) * getWidth();
		mTouchY = mHSV[1] * getHeight();
	}

	private void buildHueMixSaturationLayerForRightToLeftBottomToTop(){
		buildHueMixSaturationLayerForLinear(getWidth(), 0, 0, 0, 0, getHeight(), 0, 0);
		mTouchX = (1f - mHSV[0] / 360f) * getWidth();
		mTouchY = (1f - mHSV[1]) * getHeight();
	}

	public void setSaturationBlackWhiteAxisForMix(int black, int white){
		WHITE_BLACK_AXIS[0] = black;
		WHITE_BLACK_AXIS[1] = white;
	}

	public int[] getSaturationBlackWhiteAxisForMix(){
		return WHITE_BLACK_AXIS;
	}

	protected void buildHSVLayer(){
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
					buildHueLayerForSweep(getWidth() >> 1, getHeight() >> 1);
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
					buildSaturationLayerForSweep(getWidth() >> 1, getHeight() >> 1);
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
					buildValueLayerForSweep(getWidth() >> 1, getHeight() >> 1);
				}
				break;
			case DISPLAY_FLAG_HUE_MIX_SATURATION:
			default:
				if (mOrientation == (HORIZONTAL_LEFT_TO_RIGHT | VERTICAL_TOP_TO_BOTTOM)) {
					buildHueMixSaturationLayerForLeftToRightTopToBottom();
				} else if (mOrientation == (HORIZONTAL_LEFT_TO_RIGHT | VERTICAL_BOTTOM_TO_TOP)) {
					buildHueMixSaturationLayerForLeftToRightBottomToTop();
				} else if (mOrientation == (HORIZONTAL_RIGHT_TO_LEFT | VERTICAL_TOP_TO_BOTTOM)) {
					buildHueMixSaturationLayerForRightToLeftTopToBottom();
				} else if (mOrientation == (HORIZONTAL_RIGHT_TO_LEFT | VERTICAL_BOTTOM_TO_TOP)) {
					buildHueMixSaturationLayerForRightToLeftBottomToTop();
				}
				break;
		}
	}

	public void setOnHSVChangeListener(OnHSVChangeListener onOnHSVChangeListener){
		mOnHSVChangeListener = onOnHSVChangeListener;
	}

	public OnHSVChangeListener getOnHSVChangeListener(){
		return mOnHSVChangeListener;
	}

	public void setMarkDrawable(Drawable drawable){
		mMarkDrawable = drawable;
	}

	public Drawable getMarkDrawable(){
		return mMarkDrawable;
	}

	protected void setTouchValue(MotionEvent event){
		switch (mDisplayFlag) {
			case DISPLAY_FLAG_HUE_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[0] = event.getX() / getWidth() * 360f;
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[0] = (1f - event.getX() / getWidth()) * 360f;
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[0] = event.getY() / getHeight() * 360f;
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[0] = (1f - event.getY() / getHeight()) * 360f;
				}else if(mOrientation == RING){
					mHSV[0] = getRoundAngle(getWidth() >> 1, getHeight() >> 1, event.getX(), event.getY(), 0);
				}
				mOnHSVChangeListener.onHueChange(mHSV[0], getHueRGB(), event.getAction() == MotionEvent.ACTION_UP);
				break;
			case DISPLAY_FLAG_SATURATION_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[1] = event.getX() / getWidth();
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[1] = 1f - event.getX() / getWidth();
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[1] = event.getY() / getHeight();
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[1] = 1f - event.getY() / getHeight();
				}else if(mOrientation == RING){
					mHSV[1] = getRoundAngle(getWidth() >> 1, getHeight() >> 1, event.getX(), event.getY(), 0) / 360f;
				}
				mOnHSVChangeListener.onSaturationChange(mHSV[1], event.getAction() == MotionEvent.ACTION_UP);
				break;
			case DISPLAY_FLAG_VALUE_SINGLE:
				if(mOrientation == HORIZONTAL_LEFT_TO_RIGHT){
					mHSV[2] = event.getX() / getWidth();
				}else if(mOrientation == HORIZONTAL_RIGHT_TO_LEFT){
					mHSV[2] = 1f - event.getX() / getWidth();
				}else if(mOrientation == VERTICAL_TOP_TO_BOTTOM){
					mHSV[2] = event.getY() / getHeight();
				}else if(mOrientation == VERTICAL_BOTTOM_TO_TOP){
					mHSV[2] = 1f - event.getY() / getHeight();
				}else if(mOrientation == RING){
					mHSV[2] = getRoundAngle(getWidth() >> 1, getHeight() >> 1, event.getX(), event.getY(), 0) / 360f;
				}
				mOnHSVChangeListener.onBrightnessChange(mHSV[2], event.getAction() == MotionEvent.ACTION_UP);
				break;
			case DISPLAY_FLAG_HUE_MIX_SATURATION:
			default:
				if (mOrientation == (HORIZONTAL_LEFT_TO_RIGHT | VERTICAL_TOP_TO_BOTTOM)) {
					mHSV[0] = event.getX() / getWidth() * 360f;
					mHSV[1] = event.getY() / getHeight();
				} else if (mOrientation == (HORIZONTAL_LEFT_TO_RIGHT | VERTICAL_BOTTOM_TO_TOP)) {
					mHSV[0] = event.getX() / getWidth() * 360f;
					mHSV[1] = 1f - event.getY() / getHeight();
				} else if (mOrientation == (HORIZONTAL_RIGHT_TO_LEFT | VERTICAL_TOP_TO_BOTTOM)) {
					mHSV[0] = (1f - event.getX() / getWidth()) * 360f;
					mHSV[1] = event.getY() / getHeight();
				} else if (mOrientation == (HORIZONTAL_RIGHT_TO_LEFT | VERTICAL_BOTTOM_TO_TOP)) {
					mHSV[0] = (1f - event.getX() / getWidth()) * 360f;
					mHSV[1] = 1f - event.getY() / getHeight();
				}
				mOnHSVChangeListener.onHueChange(mHSV[0], getHueRGB(), event.getAction() == MotionEvent.ACTION_UP);
				mOnHSVChangeListener.onSaturationChange(mHSV[1], event.getAction() == MotionEvent.ACTION_UP);
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
}