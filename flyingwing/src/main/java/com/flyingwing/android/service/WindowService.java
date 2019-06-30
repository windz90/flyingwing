/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.flyingwing.android.R;

@SuppressWarnings("unused")
public class WindowService extends Service {

	public static final String SERVICE_RUNNING = "serviceRunning";
	public static final String SERVICE_FOREGROUND_TOGGLE_TRIGGER = "serviceForegroundToggleTrigger";
	
	private WindowManager mWindowManager;
	private RelativeLayout mRelLayMask;
	private ImageView mImageView, mImageViewAnchor;
	private Notification mNotification;
	private boolean mIsRunning, mIsShow;

	public interface LayoutStatusListener {
		void onShow();
		void onLayoutChanged();
		void onDismiss();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		buildUi();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.hasExtra(SERVICE_RUNNING)){
			mIsRunning = intent.getBooleanExtra(SERVICE_RUNNING, false);
		}
		if(!mIsRunning){
			stopSelf(startId);
			return Service.START_NOT_STICKY;
		}
		if(intent.getBooleanExtra(SERVICE_FOREGROUND_TOGGLE_TRIGGER, false)){
			mIsShow = !mIsShow;
			if(mIsShow){
				if(mNotification == null){
					mNotification = buildForegroundNotification();
				}
				startForeground(1, mNotification);
				mImageView.setVisibility(View.VISIBLE);
			}else{
				mImageView.setVisibility(View.GONE);
				stopForeground(true);
			}
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWindowManager.removeView(mRelLayMask);
		mWindowManager.removeView(mImageView);
		stopForeground(true);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected boolean isRunning(){
		return mIsRunning;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	private void buildUi(){
		final Resources res = getResources();

		final DisplayMetrics dm = new DisplayMetrics();
		mWindowManager = (WindowManager)(getSystemService(Context.WINDOW_SERVICE));
		mWindowManager.getDefaultDisplay().getMetrics(dm);

		int itemWi, itemHe;
		WindowManager.LayoutParams windowLayPar;
		RelativeLayout.LayoutParams relLayPar;

		itemWi = res.getDimensionPixelSize(R.dimen.toolbarHeight);
		itemHe = res.getDimensionPixelSize(R.dimen.toolbarHeight);
		windowLayPar = new WindowManager.LayoutParams();
		windowLayPar.width = itemWi;
		windowLayPar.height = itemHe;
		windowLayPar.gravity = Gravity.START | Gravity.TOP;
		windowLayPar.x = dm.widthPixels - windowLayPar.width;
		windowLayPar.y = windowLayPar.height;
		windowLayPar.type = WindowManager.LayoutParams.TYPE_PHONE;
		windowLayPar.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		windowLayPar.format = PixelFormat.RGBA_8888;
		mImageView = new ImageView(this);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageView.setVisibility(View.GONE);
		// First add control view
		mWindowManager.addView(mImageView, windowLayPar);

		itemWi = WindowManager.LayoutParams.MATCH_PARENT;
		itemHe = WindowManager.LayoutParams.MATCH_PARENT;
		windowLayPar = new WindowManager.LayoutParams();
		windowLayPar.width = itemWi;
		windowLayPar.height = itemHe;
		windowLayPar.gravity = Gravity.START | Gravity.TOP;
		windowLayPar.x = 0;
		windowLayPar.y = 0;
		windowLayPar.type = WindowManager.LayoutParams.TYPE_PHONE;
		windowLayPar.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		windowLayPar.format = PixelFormat.RGBA_8888;
		mRelLayMask = new RelativeLayout(this);
		mRelLayMask.setVisibility(View.INVISIBLE);
		// Second add mask layout
		mWindowManager.addView(mRelLayMask, windowLayPar);

		itemWi = mImageView.getLayoutParams().width;
		itemHe = mImageView.getLayoutParams().height;
		relLayPar = new RelativeLayout.LayoutParams(itemWi, itemHe);
		relLayPar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mImageViewAnchor = new ImageView(this);
		mImageViewAnchor.setLayoutParams(relLayPar);
		mImageViewAnchor.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageViewAnchor.setVisibility(View.INVISIBLE);

		mRelLayMask.addView(mImageViewAnchor);

		mImageView.setOnTouchListener(new View.OnTouchListener() {

			private final int MAX_CLICK_DURATION = 300;
			private final int MAX_CLICK_DISTANCE = 5;

			private WindowManager.LayoutParams windowLayPar;
			private long startTouchTime;
			private float downX, downY, x, y;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					startTouchTime = System.currentTimeMillis();
					downX = x = event.getRawX();
					downY = y = event.getRawY();
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					windowLayPar = (WindowManager.LayoutParams)mImageView.getLayoutParams();
					windowLayPar.x += (int) (event.getRawX() - x);
					windowLayPar.x = windowLayPar.x < 0 ? 0 : windowLayPar.x;
					windowLayPar.x = windowLayPar.x > dm.widthPixels ? dm.widthPixels : windowLayPar.x;
					windowLayPar.y += (int) (event.getRawY() - y);
					windowLayPar.y = windowLayPar.y < 0 ? 0 : windowLayPar.y;
					windowLayPar.y = windowLayPar.y > mRelLayMask.getHeight() ? mRelLayMask.getHeight() : windowLayPar.y;
					x = event.getRawX();
					x = x < 0 ? 0 : x;
					x = x > dm.widthPixels ? dm.widthPixels : x;
					y = event.getRawY();
					y = y < 0 ? 0 : y;
					y = y > mRelLayMask.getHeight() ? mRelLayMask.getHeight() : y;
					mWindowManager.updateViewLayout(mImageView, windowLayPar);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (System.currentTimeMillis() - startTouchTime < MAX_CLICK_DURATION &&
							Math.abs(x - downX) < MAX_CLICK_DISTANCE && Math.abs(y - downY) < MAX_CLICK_DISTANCE) {
						v.performClick();
					}
				}
				return false;
			}
		});
	}
	
	protected void setFloatingViewIcon(Drawable drawable){
		mImageView.setImageDrawable(drawable);
	}

	protected void setFloatingViewOnClickListener(View.OnClickListener onClickListener){
		mImageView.setOnClickListener(onClickListener);
	}

	protected boolean isShow(){
		return mIsShow;
	}
	
	protected Notification buildForegroundNotification(NotificationCompat.Builder notificationCompatBuilder){
		notificationCompatBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
		Notification notification = notificationCompatBuilder.build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
		return notification;
	}

	protected Notification buildForegroundNotification(){
		return buildForegroundNotification(new NotificationCompat.Builder(this, "Top float view channel"));
	}

	protected void setNotification(Notification notification){
		this.mNotification = notification;
	}

	protected Notification getNotification(){
		return mNotification;
	}

	protected PopupWindow buildPopupWindow(final View contentView, final LayoutStatusListener layoutStatusListener){
		if(mImageView.getLayoutParams() instanceof WindowManager.LayoutParams){
			WindowManager.LayoutParams windowLayPar = (WindowManager.LayoutParams)mImageView.getLayoutParams();
			mImageView.setTag(windowLayPar.y);
			windowLayPar.y = 0;
			mWindowManager.updateViewLayout(mImageView, windowLayPar);
		}

		// 偵測軟鍵盤升降
		final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

			private Rect rectPre;

			@Override
			public void onGlobalLayout() {
				Rect rect = new Rect();
				contentView.getWindowVisibleDisplayFrame(rect);
				if(rectPre == null){
					rectPre = rect;
					return;
				}
				if(!rectPre.equals(rect)){
					// PopupWindow Height為自訂的實際數值時，軟鍵盤升降不會自動調整視窗，需自行調整視窗高度
					int heightRecFixPre = rectPre.height() - mImageViewAnchor.getHeight();
					int heightRecFix = rect.height() - mImageViewAnchor.getHeight();
					int heightChange = contentView.getHeight() - (heightRecFixPre - heightRecFix);
					// 檢查軟鍵盤升降後是否未進行自動調整
					if(heightChange == heightRecFixPre || heightChange == heightRecFix){
						contentView.getLayoutParams().height = heightChange;
						contentView.setLayoutParams(contentView.getLayoutParams());
					}

					layoutStatusListener.onLayoutChanged();
				}
				rectPre.set(rect);
			}
		};
		contentView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		mRelLayMask.setVisibility(View.VISIBLE);

		int itemWi = WindowManager.LayoutParams.MATCH_PARENT;
		int itemHe = WindowManager.LayoutParams.MATCH_PARENT;
		final PopupWindow popupWindow = new PopupWindow(mImageViewAnchor.getContext());
		popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(contentView.getContext(), android.R.color.transparent));
		popupWindow.setWidth(itemWi);
		popupWindow.setHeight(itemHe);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setContentView(contentView);

		final BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				popupWindow.dismiss();
			}
		};

		// 監聽KEYCODE_HOME、KEYCODE_APP_SWITCH按鍵觸發廣播Intent.ACTION_CLOSE_SYSTEM_DIALOGS
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				layoutStatusListener.onDismiss();
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
					contentView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
				}else{
					contentView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
				}
				mRelLayMask.setVisibility(View.INVISIBLE);

				unregisterReceiver(receiver);

				if (mImageView.getLayoutParams() instanceof WindowManager.LayoutParams && mImageView.getTag() instanceof Integer) {
					WindowManager.LayoutParams windowLayPar = (WindowManager.LayoutParams) mImageView.getLayoutParams();
					windowLayPar.y = (int) mImageView.getTag();
					mWindowManager.updateViewLayout(mImageView, windowLayPar);
				}
			}
		});

		layoutStatusListener.onShow();
		popupWindow.showAsDropDown(mImageViewAnchor);
		return popupWindow;
	}
}