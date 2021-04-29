/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.graphics;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.ImageView;

@SuppressWarnings({"unused"})
public class MotionMatrixOperator {

	public static final int AREA_INDEX_INVALID = -1;

	private Matrix mMatrix, mMatrixByCamera;
	private Camera mCamera;
	private float[][] mAreasLTRB;
	private float[] mMatrixValues;

	public MotionMatrixOperator(){
		mMatrix = new Matrix();
	}

	public MotionMatrixOperator(float[][] areasLTRB){
		this();
		setArea(areasLTRB);
	}

	public Matrix getMatrix(){
		return mMatrix;
	}

	public void applyCanvas(Canvas canvas){
		canvas.setMatrix(mMatrix);
	}

	public void applyImageView(ImageView imageView){
		imageView.setImageMatrix(mMatrix);
	}

	public void applyImageViewAfterCorrection(ImageView imageView){
		if(imageView.getScaleType() != ImageView.ScaleType.MATRIX){
			imageView.setScaleType(ImageView.ScaleType.MATRIX);
		}
		imageView.setImageMatrix(mMatrix);
	}

	public void setArea(float[][] areasLTRB){
		mAreasLTRB = areasLTRB;
		if(mMatrixValues == null){
			mMatrixValues = new float[9];
		}
	}

	public float[][] getAreas(){
		return mAreasLTRB;
	}

	public int getTapAreaIndex(float x, float y){
		if(mAreasLTRB == null || mAreasLTRB.length == 0){
			return AREA_INDEX_INVALID;
		}
		mMatrix.getValues(mMatrixValues);
		for(int i = 0; i< mAreasLTRB.length; i++){
			if(x >= mAreasLTRB[i][0] * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MTRANS_X]
					&& y >= mAreasLTRB[i][1] * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MTRANS_Y]
					&& x <= mAreasLTRB[i][2] * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MTRANS_X]
					&& y <= mAreasLTRB[i][3] * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MTRANS_Y]){
				return i;
			}
		}
		return AREA_INDEX_INVALID;
	}

	public void translate(float xDifference, float yDifference){
		mMatrix.postTranslate(xDifference, yDifference);
	}

	public void rotate(float angleDifference, float xCenter, float yCenter){
		mMatrix.postRotate(angleDifference, xCenter, yCenter);
	}

	public void rotate(float angleDifference){
		mMatrix.postRotate(angleDifference);
	}

	public void rotateByAxis(float angleDifference, int axisIndex, float xCenter, float yCenter){
		if(mCamera == null){
			mMatrixByCamera = new Matrix();
			mCamera = new Camera();
		}
		mCamera.save();
		switch (axisIndex) {
			case 0:
				mCamera.rotateX(angleDifference);
				break;
			case 1:
				mCamera.rotateY(angleDifference);
				break;
			case 2:
				mCamera.rotateZ(angleDifference);
				break;
		}
		mCamera.getMatrix(mMatrixByCamera);
		mCamera.restore();
		mMatrixByCamera.preTranslate(-xCenter, -yCenter);
		mMatrixByCamera.postTranslate(xCenter, yCenter);
		mMatrix.postConcat(mMatrixByCamera);
	}

	public void rotateByAxis(float angleDifference, int axisIndex){
		rotateByAxis(angleDifference, axisIndex, 0, 0);
	}

	public void scale(float scaleFactor, float xCenter, float yCenter){
		mMatrix.postScale(scaleFactor, scaleFactor, xCenter, yCenter);
	}

	public void scale(float scaleFactor){
		mMatrix.postScale(scaleFactor, scaleFactor);
	}
}