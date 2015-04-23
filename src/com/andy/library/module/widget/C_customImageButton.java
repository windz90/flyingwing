/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class C_customImageButton extends ImageButton{
	
	private String text = "";
	private TextPaint txtPaint;
	
	public C_customImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		txtPaint = new TextPaint();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 文字水平置中
		txtPaint.setTextAlign(TextPaint.Align.CENTER);
		// 取得字體高度屬性
//		FontMetrics fontMetrics = txtPaint.getFontMetrics();
		// 設定Paint抗鋸齒
		txtPaint.setAntiAlias(true);
		// 設定Canvas抗鋸齒、抖動平滑
		PaintFlagsDrawFilter paintF = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
		canvas.setDrawFilter(paintF);
		
		// 繪製所在位置
		canvas.drawText(text, this.getWidth() / 2, this.getHeight() * 0.85f, txtPaint);
//		canvas.drawBitmap(iconB, point.x - iconB.getWidth() / 2, point.y - iconB.getHeight(), null);
	}
	
	public void setTextARGB(int Transparent, int Red, int Green, int Blue){
		txtPaint.setARGB(Transparent, Red, Green, Blue);
	}
	
	public void setTextColor(int color){
		txtPaint.setColor(color);
	}
	
	public void setTextSize(int size){
		txtPaint.setTextSize(size);
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public void setTextFakeBoldText(boolean isBold){
		// 粗體
		txtPaint.setFakeBoldText(isBold);
	}
}