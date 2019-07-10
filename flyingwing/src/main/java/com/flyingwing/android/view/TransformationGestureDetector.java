/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.0.2
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TransformationGestureDetector {

	public interface OnTransformationGestureListener {

		void onClick(MotionEvent motionEvent, float xScreenCoordinate, float yScreenCoordinate);

		void onTap(MotionEvent motionEvent, float xScreenCoordinate, float yScreenCoordinate);

		void onTranslate(MotionEvent motionEvent, float xDifference, float yDifference, boolean isFling);

		void onRotate(MotionEvent motionEvent, float angleDifference);

		void onScale(MotionEvent motionEvent, float scaleFactor);
	}

	public static class SimpleOnTransformationGestureListener implements OnTransformationGestureListener {

		@Override
		public void onClick(MotionEvent motionEvent, float xScreenCoordinate, float yScreenCoordinate) {}

		public void onTap(MotionEvent motionEvent, float xScreenCoordinate, float yScreenCoordinate){}

		public void onTranslate(MotionEvent motionEvent, float xDifference, float yDifference, boolean isFling){}

		public void onRotate(MotionEvent motionEvent, float angleDifference){}

		public void onScale(MotionEvent motionEvent, float scaleFactor){}
	}

	private OnTransformationGestureListener mOnTransformationGestureListener;
	private VelocityTracker mVelocityTracker;
	private final float[][] mPointsExisted = new float[2][2];
	private final float[] mPointFirstDown = new float[2];
	private final int[] mLocationOnScreen = new int[2];
	private float mScale;

	private int mDurationTap, mGapDistanceTap, mMinimumFlingVelocity, mMaximumFlingVelocity;

	public TransformationGestureDetector(Context context, OnTransformationGestureListener onTransformationGestureListener) {
		mOnTransformationGestureListener = onTransformationGestureListener;
		setTapDuration(ViewConfiguration.getTapTimeout() * 2);
		if(context == null){
			setTapGap(ViewConfiguration.getTouchSlop() * 2);
			mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
			mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
		}else{
			ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
			setTapGap(viewConfiguration.getScaledTouchSlop() * 2);
			mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
			mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
		}
	}

	public TransformationGestureDetector(Context context, int durationTap, int gapTap, OnTransformationGestureListener onTransformationGestureListener) {
		this(context, onTransformationGestureListener);
		setTapDuration(durationTap);
		setTapGap(gapTap);

	}

	public void setTapDuration(int durationTap){
		mDurationTap = durationTap;
		if(mDurationTap < 200){
			mDurationTap = 200;
		}
	}

	public int getTapDuration() {
		return mDurationTap;
	}

	public void setTapGap(int gapTap){
		mGapDistanceTap = gapTap * gapTap;
	}

	public void setTapDistance(int gapDistanceTap){
		mGapDistanceTap = gapDistanceTap;
	}

	public int getTapDistance() {
		return mGapDistanceTap;
	}

	public boolean onTouchEvent(View view, MotionEvent motionEvent) {
		int action = motionEvent.getAction();
		view.getLocationOnScreen(mLocationOnScreen);
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker .addMovement(motionEvent);
		boolean handled = false;

		if(action == MotionEvent.ACTION_DOWN){
			mPointFirstDown[0] = mPointsExisted[0][0] = motionEvent.getX(0) + mLocationOnScreen[0];
			mPointFirstDown[1] = mPointsExisted[0][1] = motionEvent.getY(0) + mLocationOnScreen[1];
			handled = true;
		}else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP && motionEvent.getPointerCount() == 2){
			int actionIndex = motionEvent.getActionIndex();
			int targetIndex = actionIndex == 0 ? actionIndex + 1 : 0;
			mPointsExisted[0][0] = motionEvent.getX(targetIndex) + mLocationOnScreen[0];
			mPointsExisted[0][1] = motionEvent.getY(targetIndex) + mLocationOnScreen[1];
		}else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && motionEvent.getPointerCount() == 2){
			mPointsExisted[0][0] = motionEvent.getX(0) + mLocationOnScreen[0];
			mPointsExisted[0][1] = motionEvent.getY(0) + mLocationOnScreen[1];
			mPointsExisted[1][0] = motionEvent.getX(1) + mLocationOnScreen[0];
			mPointsExisted[1][1] = motionEvent.getY(1) + mLocationOnScreen[1];
		}else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP && motionEvent.getPointerCount() == 3){
			int actionIndex = motionEvent.getActionIndex();
			int targetIndex = actionIndex == 0 ? actionIndex + 1 : 0;
			mPointsExisted[0][0] = motionEvent.getX(targetIndex) + mLocationOnScreen[0];
			mPointsExisted[0][1] = motionEvent.getY(targetIndex) + mLocationOnScreen[1];
			targetIndex = actionIndex == 2 ? actionIndex - 1 : 2;
			mPointsExisted[1][0] = motionEvent.getX(targetIndex) + mLocationOnScreen[0];
			mPointsExisted[1][1] = motionEvent.getY(targetIndex) + mLocationOnScreen[1];
		}else if(action == MotionEvent.ACTION_MOVE){
			float x1 = motionEvent.getX(0) + mLocationOnScreen[0];
			float y1 = motionEvent.getY(0) + mLocationOnScreen[1];
			float dx = x1 - mPointsExisted[0][0];
			float dy = y1 - mPointsExisted[0][1];
			mOnTransformationGestureListener.onTranslate(motionEvent, dx, dy, false);
			if(motionEvent.getPointerCount() > 1){
				float x2 = motionEvent.getX(1) + mLocationOnScreen[0];
				float y2 = motionEvent.getY(1) + mLocationOnScreen[1];
				float ax = mPointsExisted[1][0] - mPointsExisted[0][0];
				float ay = mPointsExisted[1][1] - mPointsExisted[0][1];
				float bx = x2 - x1;
				float by = y2 - y1;
				float dot = ax * bx + ay * by;
				float cross = ax * by - bx * ay;
				float distance1 = (float) Math.sqrt(ax * ax + ay * ay);
				float distance2 = (float) Math.sqrt(bx * bx + by * by);
				if(cross != 0){
					float angle = (cross > 0 ? 1 : -1) * (float) (Math.acos(dot / (distance1 * distance2)) / Math.PI * 180);
					// Avoid data gap caused by missing motion events
					if(angle < 15 && angle > -15 && angle != 0){
						mOnTransformationGestureListener.onRotate(motionEvent, angle);
					}
				}
				float scale = distance2 / distance1;
				// Avoid data gap caused by missing motion events
				if(scale < 1.2f && scale > -0.8f && scale != 0){
					mOnTransformationGestureListener.onScale(motionEvent, scale);
				}
				mPointsExisted[1][0] = x2;
				mPointsExisted[1][1] = y2;
			}
			mPointsExisted[0][0] = x1;
			mPointsExisted[0][1] = y1;
		}else if(action == MotionEvent.ACTION_UP){
			float x1 = motionEvent.getX(0) + mLocationOnScreen[0];
			float y1 = motionEvent.getY(0) + mLocationOnScreen[1];
			mOnTransformationGestureListener.onClick(motionEvent, x1, y1);
			if(motionEvent.getEventTime() - motionEvent.getDownTime() <= mDurationTap){
				float dx = x1 - mPointFirstDown[0];
				float dy = y1 - mPointFirstDown[1];
				if(dx * dx + dy * dy <= mGapDistanceTap){
					mOnTransformationGestureListener.onTap(motionEvent, x1, y1);
				}
			}else{
				mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
				float velocityX = mVelocityTracker.getXVelocity(motionEvent.getPointerId(0));
				float velocityY = mVelocityTracker.getYVelocity(motionEvent.getPointerId(0));
				mVelocityTracker.recycle();
				mVelocityTracker = null;
				if(Math.abs(velocityX) > mMinimumFlingVelocity || Math.abs(velocityY) > mMinimumFlingVelocity){
					mOnTransformationGestureListener.onTranslate(motionEvent, velocityX, velocityY, true);
				}
			}
		}else if(action == MotionEvent.ACTION_CANCEL){
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		return handled;
	}
}