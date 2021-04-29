/*
 * Copyright 2016 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget.helper;

import android.view.View;

/**
 * Gallery characteristic :<br>
 * 1. Item clicked scroll to centered. (Use {@link GalleryHelper#getItemMoveToCenterPositionOffset(int, int, int)})<br>
 * 2. Boundary item can scroll to centered. (Add empty item to first and last position)<br>
 * 3. Scrolled boundary visible item offset align. (Use {@link GalleryHelper#getHorizontalScrolledMostCompleteVisibleItemPosition})<br>
 * 4. Can scroll multiple items and support fling velocity. (List characteristic)<br>
 * 5. Can visible multiple items. (List characteristic)
 * PS : Gallery total width must be divisible, avoid firstVisibleItemPosition or lastVisibleItemPosition calculated incorrectly.
 */
@SuppressWarnings({"unused"})
public class GalleryHelper {

	public static int getItemMoveToCenterPositionOffset(int position, int firstVisibleItemPosition, int lastVisibleItemPosition){
		float positionMedian = (firstVisibleItemPosition + lastVisibleItemPosition) * 0.5f;
		int offset;
		if(position <= positionMedian){
			offset = position - (int)Math.ceil(positionMedian);
		}else{
			offset = position - (int)Math.floor(positionMedian);
		}
		return offset;
	}

	public static int getHorizontalScrolledMostCompleteVisibleItemPosition(int firstVisibleItemLeftBoundary, int firstVisibleItemPosition, int parentLeftVisibleBoundary
			, int lastVisibleItemRightBoundary, int lastVisibleItemPosition, int parentRightVisibleBoundary){
		return Math.abs(parentLeftVisibleBoundary - firstVisibleItemLeftBoundary) < Math.abs(parentRightVisibleBoundary - lastVisibleItemRightBoundary) ? 
				firstVisibleItemPosition : lastVisibleItemPosition;
	}

	public static int getHorizontalScrolledMostCompleteVisibleItemPosition(View firstVisibleItemView, int firstVisibleItemPosition, int parentLeftVisibleBoundary
			, View lastVisibleItemView, int lastVisibleItemPosition, int parentRightVisibleBoundary){
		int left = firstVisibleItemView.getLeft();
		int right = lastVisibleItemView.getRight();
		return Math.abs(parentLeftVisibleBoundary - left) < Math.abs(parentRightVisibleBoundary - right) ? firstVisibleItemPosition : lastVisibleItemPosition;
	}

	public static int getVerticalScrolledMostCompleteVisibleItemPosition(int firstVisibleItemTopBoundary, int firstVisibleItemPosition, int parentTopVisibleBoundary
			, int lastVisibleItemBottomBoundary, int lastVisibleItemPosition, int parentBottomVisibleBoundary){
		return Math.abs(parentTopVisibleBoundary - firstVisibleItemTopBoundary) < Math.abs(parentBottomVisibleBoundary - lastVisibleItemBottomBoundary) ?
				firstVisibleItemPosition : lastVisibleItemPosition;
	}

	public static int getVerticalScrolledMostCompleteVisibleItemPosition(View firstVisibleItemView, int firstVisibleItemPosition, int parentTopVisibleBoundary
			, View lastVisibleItemView, int lastVisibleItemPosition, int parentBottomVisibleBoundary){
		int top = firstVisibleItemView.getTop();
		int bottom = lastVisibleItemView.getBottom();
		return Math.abs(parentTopVisibleBoundary - top) < Math.abs(parentBottomVisibleBoundary - bottom) ? firstVisibleItemPosition : lastVisibleItemPosition;
	}
}