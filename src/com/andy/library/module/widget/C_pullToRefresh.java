package com.andy.library.module.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/** 
 * Copyright 2014 Andy Lin. All rights reserved.
 * @version 1.0.2
 * @author Andy Lin
 * @since JDK 1.5 and Android 4.0
 */
@SuppressLint("NewApi")
public class C_pullToRefresh {
	
	public static int NONE = -1;
	
	public static int PULL_USE_LISTVIEW = 0;
	public static int PULL_USE_SCROLLVIEW = 1;
	
	public static int PROGRESS_USE_LISTVIEW_HEADERVIEW = 0;
	public static int PROGRESS_USE_SCROLLVIEW = 1;
	public static int PROGRESS_USE_VIEWGROUP = 2;
	public static int PROGRESS_USE_ACTIONBAR = 3;
	
	private Context mContext;
	private DisplayMetrics mDisplayMetrics;
	private ActionBar mActionBar;
	private View mActionBarRefreshInfoView;
	private ListView mListView;
	private ScrollView mScrollView;
	private ViewGroup mViewGroup;
	private LinearLayout mProgressLayout;
	private View mProgressView;
	private ProgressBar mProgressBar;
	private OnRefreshListener mOnRefreshListener;
	private OnTouchListener mOnTouchListener;
	private GestureDetector mGestureDetector;
	private OnScrollListener mOnScrollListener;
	private boolean mIsFirstVisibleItemToTop;
	private int mProgressWidth;
	private int mPullFlag = NONE, mProgressFlag = NONE;
	private String mPullText, mRefreshText;
	
	public static abstract class OnRefreshListener{
		
		C_pullToRefresh pullToRefresh;
		
		public OnRefreshListener(C_pullToRefresh pullToRefresh){
			this.pullToRefresh = pullToRefresh;
		}
		
		/**
		 * 下拉更新已觸發，可以進行更新事件<br>
		 * 更新事件完畢後需調用C_pullToRefresh.done()或OnRefreshListener.done()通知更新已結束
		 */
		public abstract boolean onRefresh();
		
		/**
		 * 更新完畢，停止下拉更新各標示<br>
		 * OnRefreshListener建構子需傳入C_pullToRefresh實例
		 */
		public void done(){
			if(pullToRefresh != null){
				pullToRefresh.done();
			}
		}
	}
	
	public C_pullToRefresh(Context context, int progressViewBackgroundColor, int progressBarDrawableColor, AttributeSet progressBarAttrs
			, int progressBarStyleResourceId){
		if(context == null){
			throw new IllegalArgumentException("activity cannot be null");
		}
		
		mContext = context;
		
		mProgressView = new View(mContext);
		mProgressView.setBackgroundColor(progressViewBackgroundColor);
		
		mProgressBar = new ProgressBar(mContext, progressBarAttrs, progressBarStyleResourceId);
		// android.R.drawable.progress_horizontal
//		mProgressBar.setProgressDrawable(activity.getResources().getDrawable(android.R.drawable.progress_horizontal));
		// android.R.drawable.progress_indeterminate_horizontal
//		mProgressBar.setIndeterminateDrawable(activity.getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));
		if(progressBarDrawableColor != -1){
			mProgressBar.getProgressDrawable().setColorFilter(progressBarDrawableColor, PorterDuff.Mode.SRC_IN);
			mProgressBar.getIndeterminateDrawable().setColorFilter(progressBarDrawableColor, PorterDuff.Mode.SRC_IN);
		}
		
		mDisplayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
	}
	
	public C_pullToRefresh(Context context, int progressViewBackgroundColor, int progressBarDrawableColor){
		this(context, progressViewBackgroundColor, progressBarDrawableColor, null, android.R.attr.progressBarStyleHorizontal);
	}
	
	public C_pullToRefresh(Context context){
		this(context, android.R.color.holo_blue_light, -1, null, android.R.attr.progressBarStyleHorizontal);
	}
	
	public void setProgressBar(ProgressBar progressBar){
		mProgressBar = progressBar;
	}
	
	public ProgressBar getProgressBar(){
		return mProgressBar;
	}
	
	private void setPullView(ListView listView, ScrollView scrollView){
		mListView = listView;
		mScrollView = scrollView;
	}
	
	public void setPullView(ListView listView){
		setPullView(listView, null);
		mPullFlag = PULL_USE_LISTVIEW;
	}
	
//	public void setPullView(ScrollView scrollView){
//		setPullView(null, scrollView);
//		mPullFlag = PULL_USE_SCROLLVIEW;
//	}
	
	public void setProgressFlag(int progressFlag){
		mProgressFlag = progressFlag;
	}
	
	public void setProgressUsePullListView(int progressWidth){
		mProgressWidth = progressWidth;
		mProgressFlag = PROGRESS_USE_LISTVIEW_HEADERVIEW;
	}
	
	public void setProgressUsePullListView(){
		if(mListView != null && mListView.getLayoutParams() != null){
			setProgressUsePullListView(mListView.getLayoutParams().width);
		}
	}
	
//	public void setProgressUsePullScrollView(int progressWidth){
//		mProgressWidth = progressWidth;
//		mProgressFlag = PROGRESS_USE_SCROLLVIEW;
//	}
	
//	public void setProgressUsePullScrollView(){
//		if(mScrollView != null && mScrollView.getLayoutParams() != null){
//			setProgressUsePullScrollView(mScrollView.getLayoutParams().width);
//		}
//	}
	
	public void setProgressUsePullView(){
		if(mListView != null && mScrollView == null){
			setProgressUsePullListView();
//		}else if(mListView == null && mScrollView != null){
//			setProgressUsePullScrollView();
		}
	}
	
	public void setProgressUseLayout(ViewGroup viewGroup, int progressWidth){
		mViewGroup = viewGroup;
		mProgressWidth = progressWidth;
		mProgressFlag = PROGRESS_USE_VIEWGROUP;
	}
	
	public void setProgressUseLayout(ViewGroup viewGroup){
		if(viewGroup != null && viewGroup.getLayoutParams() != null){
			setProgressUseLayout(viewGroup, viewGroup.getLayoutParams().width);
		}
	}
	
	public void setProgressUseActionBar(ActionBar actionBar, int progressWidth){
		mActionBar = actionBar;
		mProgressWidth = progressWidth;
		mProgressFlag = PROGRESS_USE_ACTIONBAR;
	}
	
	public void setProgressUseActionBar(ActionBar actionBar){
		setProgressUseActionBar(actionBar, mDisplayMetrics.widthPixels);
	}
	
	public void setProgressWidth(int progressWidth){
		mProgressWidth = progressWidth;
	}
	
	public void setRefreshInfoViewToActionBar(ActionBar actionBar, View actionBarRefreshInfoView){
		mActionBar = actionBar;
		mActionBarRefreshInfoView = actionBarRefreshInfoView;
	}
	
	public void setRefreshInfoViewToActionBar(View actionBarRefreshInfoView){
		setRefreshInfoViewToActionBar(mActionBar, actionBarRefreshInfoView);
	}
	
	public void setRefreshText(String pullText, String refreshText){
		mPullText = pullText;
		mRefreshText = refreshText;
	}
	
//	public void setRefreshBitmap(Bitmap pullBitmap, Bitmap refreshBitmap){
//		mPullBitmap = pullBitmap;
//		mRefreshBitmap = refreshBitmap;
//	}
	
	public void setRefreshInfoViewToActionBar(ActionBar actionBar, View actionBarRefreshInfoView, String pullText, String refreshText){
		setRefreshInfoViewToActionBar(actionBar, actionBarRefreshInfoView);
		setRefreshText(pullText, refreshText);
	}
	
	public void setRefreshInfoViewToActionBar(View actionBarRefreshInfoView, String pullText, String refreshText){
		setRefreshInfoViewToActionBar(mActionBar, actionBarRefreshInfoView);
		setRefreshText(pullText, refreshText);
	}
	
//	public void setRefreshInfoViewToActionBar(ActionBar actionBar, View actionBarRefreshInfoView, Bitmap pullBitmap, Bitmap refreshBitmap){
//		setRefreshInfoViewToActionBar(actionBar, actionBarRefreshInfoView);
//		setRefreshBitmap(pullBitmap, refreshBitmap);
//	}
//	
//	public void setRefreshInfoViewToActionBar(View actionBarRefreshInfoView, Bitmap pullBitmap, Bitmap refreshBitmap){
//		setRefreshInfoViewToActionBar(mActionBar, actionBarRefreshInfoView);
//		setRefreshBitmap(pullBitmap, refreshBitmap);
//	}
	
	public void setOnRefreshListener(OnRefreshListener onRefreshListener){
		mOnRefreshListener = onRefreshListener;
	}
	
	public void setListViewOnTouchListener(OnTouchListener onTouchListener){
		mOnTouchListener = onTouchListener;
	}
	
	public void setListViewOnScrollListener(OnScrollListener onScrollListener){
		mOnScrollListener = onScrollListener;
	}
	
	public int getActionBarHeight(int defValue){
		TypedValue typedValue = new TypedValue();
		if(mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
			return TypedValue.complexToDimensionPixelSize(typedValue.data, mDisplayMetrics);
		}
		return defValue;
	}
	
	public void attach(int progressWidth, final int progressViewHeight, final int progressBarHeight){
		if(mProgressFlag == NONE){
			setProgressUsePullView();
		}
		mProgressWidth = progressWidth;
		
		LinearLayout.LayoutParams linLayPar;
		
		mProgressLayout = new LinearLayout(mContext);
		mProgressLayout.setOrientation(LinearLayout.VERTICAL);
		mProgressLayout.setGravity(Gravity.CENTER);
		
		linLayPar = new LayoutParams(0, progressViewHeight);
		mProgressView.setLayoutParams(linLayPar);
		mProgressView.setVisibility(View.GONE);
		mProgressLayout.addView(mProgressView);
		
		linLayPar = new LayoutParams(mProgressWidth, progressBarHeight);
		mProgressBar.setLayoutParams(linLayPar);
		mProgressBar.setPadding(0, 0, 0, 0);
		mProgressBar.setIndeterminate(true);
		mProgressBar.setVisibility(View.GONE);
		mProgressLayout.addView(mProgressBar);
		
		final int actionBarHe = getActionBarHeight(0);
		LinearLayout customLayout = null;
		if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
			customLayout = new LinearLayout(mContext);
			customLayout.setOrientation(LinearLayout.VERTICAL);
			customLayout.setBackgroundResource(android.R.color.transparent);
			customLayout.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
			
			if(mActionBarRefreshInfoView != null){
				int height = actionBarHe - (mProgressFlag == PROGRESS_USE_ACTIONBAR ? progressViewHeight : 0);
				linLayPar = new LayoutParams(LayoutParams.MATCH_PARENT, height);
				mActionBarRefreshInfoView.setLayoutParams(linLayPar);
				customLayout.addView(mActionBarRefreshInfoView);
			}
			
			mActionBar.setCustomView(customLayout);
			mActionBar.setDisplayShowCustomEnabled(false);
		}
		
		if(mProgressFlag == PROGRESS_USE_LISTVIEW_HEADERVIEW && mListView != null){
			ListAdapter listAdapter = mListView.getAdapter();
			if(listAdapter instanceof HeaderViewListAdapter){
				listAdapter = ((HeaderViewListAdapter)listAdapter).getWrappedAdapter();
			}
			// 提升對Android 4.2以下的相容性，校正設定順序，先設定header再設定adapter，避免舊版拋出IllegalStateException
			mListView.setAdapter(null);
			mListView.addHeaderView(mProgressLayout, null, false);
			mListView.setAdapter(listAdapter);
		}else if(mProgressFlag == PROGRESS_USE_SCROLLVIEW && mScrollView != null){
		}else if(mProgressFlag == PROGRESS_USE_VIEWGROUP && mViewGroup != null){
			mViewGroup.addView(mProgressLayout, 0);
		}else if(mProgressFlag == PROGRESS_USE_ACTIONBAR && customLayout != null){
			customLayout.addView(mProgressLayout);
		}
		
		OnTouchListener onTouchListener = new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				boolean isConsumed = false;
				if(event.getAction() != MotionEvent.ACTION_UP || mProgressView.getLayoutParams().width < 1){
					if(mOnTouchListener != null){
						isConsumed = mOnTouchListener.onTouch(v, event);
					}
					return isConsumed;
				}
				
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					int diffWidth = mProgressWidth / 10;
					
					Handler handler = new Handler(new Callback() {
						
						@Override
						public boolean handleMessage(Message msg) {
							if(mProgressView == null){
								return false;
							}
							mProgressView.setLayoutParams(new LayoutParams(msg.what, progressViewHeight));
							if(msg.what > 0){
								return false;
							}
							
							mProgressView.setVisibility(View.GONE);
							if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
								mActionBar.setDisplayShowCustomEnabled(false);
							}
							timer.cancel();
							cancel();
							return false;
						}
					});
					
					@Override
					public void run() {
						if(mProgressView == null){
							timer.cancel();
							cancel();
							return;
						}
						int progress = mProgressView.getLayoutParams().width - diffWidth;
						progress = progress < 0 ? 0 : progress;
						handler.sendEmptyMessage(progress);
					}
				}, 0, 50);
				
				if(mOnTouchListener != null){
					isConsumed = mOnTouchListener.onTouch(v, event);
				}
				v.performClick();
				return isConsumed;
			}
		};
		
		final OnScrollListener onScrollListener = new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(view.getChildCount() > 0){
					mIsFirstVisibleItemToTop = view.getFirstVisiblePosition() == 0 && view.getChildAt(0).getTop() == 0;
				}else{
					mIsFirstVisibleItemToTop = view.getFirstVisiblePosition() == 0;
				}
				
				if(mOnScrollListener != null){
					mOnScrollListener.onScrollStateChanged(view, scrollState);
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(mOnScrollListener != null){
					mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}
			}
		};
		
		mGestureDetector = new GestureDetector(mContext, new OnGestureListener() {
			
			boolean isRefreshing;
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				if(e1 == null || e2 == null){
					return false;
				}
				if(!mIsFirstVisibleItemToTop || mProgressBar.getVisibility() == View.VISIBLE || isRefreshing){
					return false;
				}
				if(e2.getY() <= e1.getY()){
					if(mProgressView.getVisibility() == View.VISIBLE){
						mProgressView.setVisibility(View.GONE);
						if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
							mActionBar.setDisplayShowCustomEnabled(false);
						}
					}
					return false;
				}
				
				int progress = (int)(e2.getY() - e1.getY());
				if(progress >= mProgressWidth / 3){
					progress = progress + (progress - mProgressWidth / 3) * 2;
				}
				if(progress < mProgressWidth){
					if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
						if(mActionBarRefreshInfoView != null){
							int height = actionBarHe - (mProgressFlag == PROGRESS_USE_ACTIONBAR ? progressViewHeight : 0);
							mActionBarRefreshInfoView.getLayoutParams().height = height;
						}
						mActionBar.setDisplayShowCustomEnabled(true);
					}
					if(mProgressView.getVisibility() == View.GONE){
						mProgressView.setVisibility(View.VISIBLE);
					}
					mProgressView.setLayoutParams(new LayoutParams(progress, progressViewHeight));
				}else if(progress >= mProgressWidth){
					mProgressView.setLayoutParams(new LayoutParams(0, progressViewHeight));
					mProgressView.setVisibility(View.GONE);
					
					if(mActionBar != null && mActionBarRefreshInfoView != null){
						int height = actionBarHe - (mProgressFlag == PROGRESS_USE_ACTIONBAR ? progressBarHeight : 0);
						mActionBarRefreshInfoView.getLayoutParams().height = height;
						if(mActionBarRefreshInfoView instanceof TextView){
							((TextView)mActionBarRefreshInfoView).setText(mRefreshText);
						}
					}
					
					mProgressBar.setVisibility(View.VISIBLE);
					isRefreshing = true;
					mProgressBar.post(new Runnable() {
						public void run() {
							if(mOnRefreshListener != null){
								mOnRefreshListener.onRefresh();
							}
						}
					});
				}
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				// 刷新結束且再次發生手勢觸碰才能執行下拉動作
				if(mProgressBar.getVisibility() == View.GONE){
					isRefreshing = false;
					onScrollListener.onScrollStateChanged(mListView, OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
					if(mIsFirstVisibleItemToTop && mActionBarRefreshInfoView instanceof TextView){
						((TextView)mActionBarRefreshInfoView).setText(mPullText);
					}
				}
				return false;
			}
		});
		
		if(mPullFlag == PULL_USE_LISTVIEW){
			mListView.setOnTouchListener(onTouchListener);
			mListView.setOnScrollListener(onScrollListener);
		}else if(mPullFlag == PULL_USE_SCROLLVIEW){}
	}
	
	public void attach(int progressWidth){
		int itemHe = (int)(5 * mDisplayMetrics.density);
		attach(progressWidth, itemHe, itemHe * 2);
	}
	
	public void attach(){
		int itemHe = (int)(5 * mDisplayMetrics.density);
		attach(mProgressWidth, itemHe, itemHe * 2);
	}
	
	/**
	 * 更新完畢，停止下拉更新各標示
	 */
	public void done(){
		if(mProgressBar != null){
			mProgressBar.post(new Runnable() {
				
				@Override
				public void run() {
					if(mProgressBar != null){
						mProgressBar.setVisibility(View.GONE);
					}
					if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
						mActionBar.setDisplayShowCustomEnabled(false);
					}
				}
			});
		}
	}
	
	public void detach(){
		if(mListView != null){
			mListView.removeHeaderView(mProgressLayout);
			mListView.setOnTouchListener(mOnTouchListener);
			mListView.setOnScrollListener(mOnScrollListener);
			mListView = null;
		}
		if(mScrollView != null){
			mScrollView.removeView(mProgressLayout);
			mScrollView.setOnTouchListener(mOnTouchListener);
			mScrollView = null;
		}
		if(mViewGroup != null){
			if(mViewGroup.getChildAt(0) == mProgressLayout){
				mViewGroup.removeViewAt(0);
			}else{
				mViewGroup.removeView(mProgressLayout);
			}
			mViewGroup = null;
		}
		if(mActionBar != null && (mProgressFlag == PROGRESS_USE_ACTIONBAR || mActionBarRefreshInfoView != null)){
			if(mActionBarRefreshInfoView != null){
				mActionBarRefreshInfoView = null;
			}
			mActionBar.setCustomView(null);
			mActionBar.setDisplayShowCustomEnabled(false);
			mActionBar = null;
		}
		if(mProgressView != null){
			mProgressView = null;
		}
		if(mProgressBar != null){
			mProgressBar = null;
		}
		if(mProgressLayout != null){
			mProgressLayout = null;
		}
		if(mGestureDetector != null){
			mGestureDetector = null;
		}
		if(mOnRefreshListener != null){
			mOnRefreshListener.pullToRefresh = null;
			mOnRefreshListener = null;
		}
		if(mOnTouchListener != null){
			mOnTouchListener = null;
		}
		if(mOnScrollListener != null){
			mOnScrollListener = null;
		}
		mPullText = null;
		mRefreshText = null;
	}
	
	public void cancel(){
		detach();
		if(mContext != null){
			mContext = null;
		}
		if(mDisplayMetrics != null){
			mDisplayMetrics = null;
		}
	}
}