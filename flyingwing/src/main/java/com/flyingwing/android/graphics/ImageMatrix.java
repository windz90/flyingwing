/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.4.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ImageMatrix {
	
	public interface ClickAction{
		void distClick(int position);
	}
	
	public interface TouchAction{
		void onTouch(View view, MotionEvent event);
		void onMove(float moveX, float moveY);
		void onZoom(MotionEvent event, float zoomScale);
	}
	
	public static class moveZoom{
		
		private static boolean isFollowJump;
		private static float defaultScale;
		private static float zoomScale;
		private static float[] touchSpacing = new float[2];
		private static float[][] rangeXY;
		private static Matrix matrix;
		private static ClickAction clickAction;
		private static TouchAction touchAction;
		
		public static void attach(final ImageView imageView, float defaultWidth){
			// 設定圖片適合寬高
			matrix = new Matrix();
			// 計算最佳比例
			defaultScale = Math.abs(defaultWidth / imageView.getDrawable().getIntrinsicWidth());
			matrix.postScale(defaultScale, defaultScale);
			imageView.setImageMatrix(matrix);
			zoomScale = defaultScale;
			
			OnGestureListener gestureListener = new OnGestureListener() {
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					if(imageView.isClickable()){
						click(e, clickAction);
					}
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					move(imageView, -distanceX, -distanceY);
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					return false;
				}
				
				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}
			};
			final GestureDetector gestureDetector = new GestureDetector(imageView.getContext(), gestureListener);
			
			OnTouchListener onTouchListener = new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int eventAction = event.getAction() & MotionEvent.ACTION_MASK;
					if(touchAction != null){
						touchAction.onTouch(v, event);
					}
					// 雙座標讀取定位不確定時僅更新前移動座標而不進行移動
					if(eventAction == MotionEvent.ACTION_POINTER_DOWN || eventAction == MotionEvent.ACTION_POINTER_UP){
						isFollowJump = true;
					}
					if(event.getPointerCount() == 1){
						if(eventAction == MotionEvent.ACTION_DOWN){
							v.setClickable(true);
						}
						gestureDetector.onTouchEvent(event);
					}else if(event.getPointerCount() == 2){
						v.setClickable(false);
						if(eventAction == MotionEvent.ACTION_POINTER_DOWN){
							// 計算兩點距離
							touchSpacing[0] = spacing(event);
						}
						if(eventAction == MotionEvent.ACTION_MOVE){
							zoom(imageView, event);
						}
					}
					return true;
				}
			};
			imageView.setOnTouchListener(onTouchListener);
		}
		
		public static void detach(ImageView imageView){
			imageView.setOnTouchListener(null);
		}
		
		public static void setImageLocation(float[][] rangeXY, ClickAction click){
			moveZoom.rangeXY = rangeXY;
			moveZoom.clickAction = click;
		}
		
		public static void setTouchAction(TouchAction touchAction){
			moveZoom.touchAction = touchAction;
		}
		
		public static void setZoomScale(float zoomScale){
			moveZoom.zoomScale = zoomScale;
		}
		
		public static float getZoomScale(){
			return zoomScale;
		}
		
		public static float getDefaultScale(){
			return defaultScale;
		}
		
		public static float[] getMatrixValues(){
			float[] matrixValues = new float[9];
			matrix.getValues(matrixValues);
			return matrixValues;
		}
		
		private static void click(MotionEvent event, ClickAction action){
			// 取得目前矩陣參數
			float[] matrixValues = getMatrixValues();
			if(rangeXY != null && rangeXY.length > 0){
				float rangeLeftX, rangeRightX, rangeTopY, rangeBottomY;
				for(int i=0; i<rangeXY.length; i++){
					rangeLeftX = rangeXY[i][0] * zoomScale + matrixValues[Matrix.MTRANS_X];
					rangeRightX = rangeXY[i][2] * zoomScale + matrixValues[Matrix.MTRANS_X];
					rangeTopY = rangeXY[i][1] * zoomScale + matrixValues[Matrix.MTRANS_Y];
					rangeBottomY = rangeXY[i][3] * zoomScale + matrixValues[Matrix.MTRANS_Y];
					if(event.getX() >= rangeLeftX && event.getY() >= rangeTopY && 
							event.getX() <= rangeRightX && event.getY() <= rangeBottomY){
						action.distClick(i);
						break;
					}
				}
			}
		}
		
		private static void move(ImageView imageView, float moveX, float moveY){
			if(!isFollowJump){
				// 繪製圖形平移
				matrix.postTranslate(moveX, moveY);
				imageView.setImageMatrix(matrix);
				if(touchAction != null){
					touchAction.onMove(moveX, moveY);
				}
			}else{
				isFollowJump = false;
			}
		}
		
		private static void zoom(ImageView imageView, MotionEvent event){
			// 計算移動後兩點距離
			touchSpacing[1] = spacing(event);
			// 計算當次縮放比例
			float zoomScaleNow = touchSpacing[1] / touchSpacing[0];
			// 比例不同才進行縮放處理
			if(zoomScaleNow != 1){
				// 計算累加縮放比例
				zoomScale = zoomScale * zoomScaleNow;
				// 限定縮放極限
				if((zoomScale >= defaultScale && zoomScale <= 2.0f) || 
						(zoomScale <= defaultScale && zoomScale >= 1.0f && defaultScale >= 2.0f)){
					// 計算縮放中心點
					PointF zoomMidPoint = new PointF();
					midPoint(zoomMidPoint, event);
					// 繪製圖形縮放
					matrix.postScale(zoomScaleNow, zoomScaleNow, zoomMidPoint.x, zoomMidPoint.y);
					imageView.setImageMatrix(matrix);
					if(touchAction != null){
						touchAction.onZoom(event, zoomScale);
					}
				}else{
					// 若當次未進行縮放則不計算累加縮放比例
					zoomScale = zoomScale / zoomScaleNow;
				}
			}
			// 以當次兩點間距做為下次計算比例的基準
			touchSpacing[0] = touchSpacing[1];
		}
	}
	
	public static class animation {
		
		private static boolean enabled = true;
		
		public static boolean isEnabled(){
			return enabled;
		}
		
		public static void setEnabled(boolean enabled){
			animation.enabled = enabled;
		}
		
		public static void rotate3D(Context context, final View view, final Bitmap bitmap, final float eachAngle, final int sumCount, int eachTime, final boolean xAxis, final boolean yAxis, final boolean zAxis, final Thread nextActionThread){
			
			final Resources res = context.getResources();
			final Timer timer = new Timer();
			final Handler handler = new Handler(new Handler.Callback() {

				float angle;

				@Override
				public boolean handleMessage(Message msg) {
					angle = angle + ((Bundle)msg.obj).getFloat("eachAngle");
					if(angle >= 360.0f || angle <= -360.0f){
						angle = 0f;
					}
					Matrix matrix = new Matrix();
					Camera camera = new Camera();
					camera.save();
					if(xAxis){
						camera.rotateX(angle);
					}
					if(yAxis){
						camera.rotateY(angle);
					}
					if(zAxis){
						camera.rotateZ(angle);
					}
					camera.getMatrix(matrix);
					camera.restore();

					matrix.preTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
					matrix.postTranslate(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
					Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
					matrix.reset();

					if(view instanceof ImageView){
						((ImageView)view).setImageBitmap(tmpBitmap);
					}else{
						BitmapDrawable bd = new BitmapDrawable(res, tmpBitmap);
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
							view.setBackground(bd);
						}else{
							//noinspection deprecation
							view.setBackgroundDrawable(bd);
						}
					}
					return false;
				}
			});
			
			TimerTask task = new TimerTask() {
				
				int exitOccasion;
				int count;
				
				@Override
				public void run() {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					
					if(count < (int)(sumCount / 10.0f * 3) || count > (int)(sumCount / 10.0f * 6)){
						bundle.putFloat("eachAngle", eachAngle);
						msg.obj = bundle;
						handler.sendMessage(msg);
					}else{
						bundle.putFloat("eachAngle", eachAngle);
						msg.obj = bundle;
						handler.sendMessage(msg);
					}
					
					count++;
					if(count == sumCount || sumCount == 0){
						count = 0;
						exitOccasion++;
					}
					if(exitOccasion == 1){
						timer.cancel();
						if(nextActionThread != null){
							nextActionThread.start();
						}
					}
				}
			};
			if(enabled && eachAngle != 0 && (xAxis || yAxis || zAxis)){
				timer.schedule(task, 0, eachTime);
			}else if(nextActionThread != null){
				nextActionThread.start();
			}
		}
	}
	
	public static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public static float rotate(MotionEvent event){
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		double radians = Math.atan2(y, x);
		return (float)Math.toDegrees(radians);
	}
	
	public static void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}