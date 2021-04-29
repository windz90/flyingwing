/*
 * Copyright (C) 2016 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;

import androidx.annotation.ColorInt;

@SuppressWarnings({"unused"})
public class TextViewAttribute {

	private int mGravity = Gravity.TOP | Gravity.START;
	private ColorStateList mColorStateList;
	private float mTextSize;
	private Typeface mTypeface;
	private boolean mSingleLine;
	private TextUtils.TruncateAt mTruncateAt;
	private int mMaxLines;
	private int mLines;

	public TextViewAttribute(){}

	public TextViewAttribute(int gravity, ColorStateList colorStateList, int textSize, Typeface typeface, int typefaceStyle){
		mGravity = gravity;
		mColorStateList = colorStateList;
		mTextSize = textSize;
		setTypeface(typeface, typefaceStyle);
	}

	public TextViewAttribute(int gravity, ColorStateList colorStateList, int textSize, Typeface typeface){
		mGravity = gravity;
		mColorStateList = colorStateList;
		mTextSize = textSize;
		mTypeface = typeface;
	}

	public TextViewAttribute(int gravity, @ColorInt int color, int textSize, Typeface typeface, int typefaceStyle){
		mGravity = gravity;
		mColorStateList = ColorStateList.valueOf(color);
		mTextSize = textSize;
		setTypeface(typeface, typefaceStyle);
	}

	public TextViewAttribute(int gravity, @ColorInt int color, int textSize, Typeface typeface){
		mGravity = gravity;
		mColorStateList = ColorStateList.valueOf(color);
		mTextSize = textSize;
		mTypeface = typeface;
	}

	public void setGravity(int gravity){
		mGravity = gravity;
	}

	public int getGravity(){
		return mGravity;
	}

	public void setTextColor(@ColorInt int color){
		mColorStateList = ColorStateList.valueOf(color);
	}

	public void setTextColor(ColorStateList colorStateList){
		mColorStateList = colorStateList;
	}

	public ColorStateList getTextColor(){
		return mColorStateList;
	}

	public void setTextSize(float textSize){
		mTextSize = textSize;
	}

	public float getTextSize(){
		return mTextSize;
	}

	public void setTypeface(Typeface typeface){
		mTypeface = typeface;
	}

	public void setTypeface(Typeface typeface, int typefaceStyle){
		if (typefaceStyle > 0) {
			if (typeface == null) {
				typeface = Typeface.defaultFromStyle(typefaceStyle);
			} else {
				typeface = Typeface.create(typeface, typefaceStyle);
			}
		}
		mTypeface = typeface;
	}

	public Typeface getTypeface(){
		return mTypeface;
	}

	public void setSingleLine(boolean singleLine){
		mSingleLine = singleLine;
	}

	public boolean isSingleLine(){
		return mSingleLine;
	}

	public void setEllipsize(TextUtils.TruncateAt truncateAt){
		mTruncateAt = truncateAt;
	}

	public TextUtils.TruncateAt getEllipsize(){
		return mTruncateAt;
	}

	public void setMaxLines(int maxLines){
		mMaxLines = maxLines;
	}

	public int getMaxLines(){
		return mMaxLines;
	}

	public void setLines(int lines){
		mLines = lines;
	}

	public int getLines(){
		return mLines;
	}
}