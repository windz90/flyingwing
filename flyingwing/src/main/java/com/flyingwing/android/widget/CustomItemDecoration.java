/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CustomItemDecoration extends RecyclerView.ItemDecoration {

	private Drawable mDrawable;
	private int mStroke;
	private Rect mRectPadding;
	private boolean mIsPreDraw;

	public CustomItemDecoration(Drawable drawable, int stroke, Rect rectPadding, boolean isPreDraw){
		mDrawable = drawable;
		mStroke = stroke;
		mRectPadding = rectPadding;
		mIsPreDraw = isPreDraw;
	}

	public CustomItemDecoration(Drawable drawable, int stroke, boolean isPreDraw){
		this(drawable, stroke, null, isPreDraw);
	}

	public CustomItemDecoration(Drawable drawable, int stroke){
		this(drawable, stroke, null, false);
	}

	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		super.getItemOffsets(outRect, view, parent, state);
		if(mRectPadding == null || mDrawable == null){
			return;
		}
		RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
		if(layoutManager instanceof LinearLayoutManager){
			outRect.left += mRectPadding.left;
			outRect.right -= mRectPadding.right;
			outRect.top += mRectPadding.top;
			outRect.bottom -= mRectPadding.bottom;
		}
	}

	@Override
	public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		super.onDraw(c, parent, state);
		if(mIsPreDraw){
			draw(c, parent, state);
		}
	}

	@Override
	public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		super.onDrawOver(c, parent, state);
		if(!mIsPreDraw){
			draw(c, parent, state);
		}
	}

	private void draw(Canvas c, RecyclerView parent, RecyclerView.State state){
		if(mDrawable == null){
			return;
		}
		RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
		if(!(layoutManager instanceof LinearLayoutManager)){
			return;
		}
		RecyclerView.LayoutParams recyclerLayPar;
		int left, right, top, bottom;
		int count = parent.getChildCount();
		for(int i=0; i<count; i++){
			View viewChild = parent.getChildAt(i);
			recyclerLayPar = (RecyclerView.LayoutParams)viewChild.getLayoutParams();
			if(((LinearLayoutManager)layoutManager).getOrientation() == LinearLayoutManager.VERTICAL){
				left = viewChild.getLeft();
				right = viewChild.getRight();
				top = viewChild.getBottom() + recyclerLayPar.bottomMargin;
				bottom = top + mStroke;
				mDrawable.setBounds(left, top, right, bottom);
				mDrawable.draw(c);
			}else if(((LinearLayoutManager)layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL){
				left = viewChild.getRight() + recyclerLayPar.rightMargin;
				right = left + mStroke;
				top = viewChild.getTop();
				bottom = viewChild.getBottom();
				mDrawable.setBounds(left, top, right, bottom);
				mDrawable.draw(c);
			}
		}
	}
}