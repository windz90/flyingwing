/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

@SuppressWarnings({"unused", "deprecation"})
public class MediaOperator {
	
	private Context mContext;
	private MediaPlayer mMediaPlayer;
	private MediaController mMediaController;

	public MediaOperator(Context context, View anchorView){
		initMediaPlayerAndMediaController(context);
		setAnchorView(anchorView);
		mContext = context;
	}

	public MediaOperator(Context context){
		initMediaPlayerAndMediaController(context);
		mContext = context;
	}

	public void initial(){
		initMediaPlayerAndMediaController(mContext);
	}

	public void setAnchorView(View anchorView){
		mMediaController.setAnchorView(anchorView);
	}

	public void prepare(File sourceFile){
		prepareMediaPlayerAndMediaController(sourceFile);
	}

	public void start(){
		if(mMediaPlayer != null && !mMediaPlayer.isPlaying()){
			mMediaPlayer.start();
		}
	}

	public void show(){
		if(mMediaController != null && !mMediaController.isShowing()){
			mMediaController.show();
		}
	}

	public void hide(){
		if(mMediaController != null && mMediaController.isShowing()){
			mMediaController.hide();
		}
	}

	public void pause(){
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.pause();
		}
	}

	public void stop(){
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
		}
	}

	public void release(){
		releaseMediaPlayerAndMediaController();
	}

	private void initMediaPlayerAndMediaController(Context context){
		if(mMediaPlayer == null){
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					mMediaController.show();
					// Reflection反射調用private屬性
					try {
						Field field = mMediaController.getClass().getDeclaredField("mPauseButton");
						field.setAccessible(true);
						((ImageButton)field.get(mMediaController)).performClick();
						field.setAccessible(false);
					} catch (Exception e) {
						mMediaPlayer.start();
						e.printStackTrace();
					}
				}
			});
			mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
				}
			});
			mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {

				@Override
				public boolean onInfo(MediaPlayer mp, int what, int extra) {
					return false;
				}
			});
		}
		if(mMediaController == null){
			mMediaController = new MediaController(context);
			mMediaController.setMediaPlayer(new MediaController.MediaPlayerControl() {

				@Override
				public void start() {
					if(mMediaPlayer != null){
						mMediaPlayer.start();
					}
				}

				@Override
				public void seekTo(int pos) {
					if(mMediaPlayer != null){
						mMediaPlayer.seekTo(pos);
					}
				}

				@Override
				public void pause() {
					if(mMediaPlayer != null){
						mMediaPlayer.pause();
					}
				}

				@Override
				public boolean isPlaying() {
					return mMediaPlayer != null && mMediaPlayer.isPlaying();
				}

				@Override
				public int getDuration() {
					if(mMediaPlayer != null){
						return mMediaPlayer.getDuration();
					}
					return 0;
				}

				@Override
				public int getCurrentPosition() {
					if(mMediaPlayer != null){
						return mMediaPlayer.getCurrentPosition();
					}
					return 0;
				}

				@Override
				public int getBufferPercentage() {
					return 0;
				}

				@Override
				public int getAudioSessionId() {
					if(mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
						return mMediaPlayer.getAudioSessionId();
					}
					return 0;
				}

				@Override
				public boolean canSeekForward() {
					return true;
				}

				@Override
				public boolean canSeekBackward() {
					return true;
				}

				@Override
				public boolean canPause() {
					return true;
				}
			});
		}
	}

	private void prepareMediaPlayerAndMediaController(final File sourceFile){
		if(sourceFile == null){
			return;
		}
		final Handler handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				mMediaController.show();
				return false;
			}
		});
		String savePath = mMediaController.getContentDescription() == null ? null : mMediaController.getContentDescription().toString();
		if(savePath != null && savePath.equals(sourceFile.getPath())){
			handler.sendEmptyMessage(0);
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					FileInputStream fileInputStream = new FileInputStream(sourceFile);
					mMediaPlayer.reset();
					mMediaPlayer.setDataSource(fileInputStream.getFD());
					mMediaPlayer.prepare();
					mMediaController.setContentDescription(sourceFile.getPath());
					fileInputStream.close();
					handler.sendEmptyMessage(0);
				} catch (Exception e) {
					try {
						mMediaPlayer.setDataSource("");
					} catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e1) {
						e1.printStackTrace();
					}
					mMediaPlayer.reset();
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void setMediaControllerCurrentTimeTextColor(ColorStateList colorStateList){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mCurrentTime");
			field.setAccessible(true);
			((TextView)field.get(mMediaController)).setTextColor(colorStateList);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerEndTimeTextColor(ColorStateList colorStateList){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mEndTime");
			field.setAccessible(true);
			((TextView)field.get(mMediaController)).setTextColor(colorStateList);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerPauseButtonBackgroundDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mPauseButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setBackgroundDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerPauseButtonImageDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mPauseButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setImageDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerForwardButtonBackgroundDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mFfwdButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setBackgroundDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerForwardButtonImageDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mFfwdButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setImageDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerRewindButtonBackgroundDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mRewButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setBackgroundDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerRewindButtonImageDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mRewButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setImageDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerNextButtonBackgroundDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mNextButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setBackgroundDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerNextButtonImageDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mNextButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setImageDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerPrevButtonBackgroundDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mPrevButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setBackgroundDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setMediaControllerPrevButtonImageDrawable(Drawable drawable){
		// Reflection反射調用private屬性
		try {
			Field field = mMediaController.getClass().getDeclaredField("mPrevButton");
			field.setAccessible(true);
			((ImageButton)field.get(mMediaController)).setImageDrawable(drawable);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void releaseMediaPlayerAndMediaController(){
		if(mMediaPlayer != null){
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if(mMediaController != null){
			mMediaController.hide();
			mMediaController = null;
		}
	}
}