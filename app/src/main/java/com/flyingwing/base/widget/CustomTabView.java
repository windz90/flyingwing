/*
 * Copyright (C) 2014 Andy Lin. All rights reserved.
 * @version 1.0.5
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.flyingwing.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "Convert2Diamond"})
public class CustomTabView extends LinearLayout {
	
	private RelativeLayout mRelLay;
	private Button mButton;
	private RadioGroup mRadioGroup;
	private CustomPagerAdapter mCustomPagerAdapter;
	private ViewPager mViewPager;
	private List<View[]> mList;
	private OnTouchListener mOnTabBarTouchListener;
	private OnTabChangeListener mOnTabChangeListener;
	private OnVisibilityChangeListener mOnVisibilityChangeListener;
	private OnDismissListener mOnDismissListener;
	private Drawable mLeftDrawable, mMiddleDrawable, mRightDrawable;
	private int mItemWi, mItemHe, mMinHeight, mMaxHeight;
	private boolean mIsDynamicControl;
	private Resources mRes;
	private LayoutParams mLinLayPar;
	private RelativeLayout.LayoutParams mRelLayPar;
	private DisplayMetrics mDisplayMetrics;
	
	public interface DynamicResizeControl{
		void layoutChange(int diffWidth, int diffHeight);
		void layoutChanged(int viewWidth, int viewHeight);
	}
	
	public interface OnTabChangeListener{
		void onTabSelected(int position);
		void onTabScrolled(int position, float positionOffset, int positionOffsetPixels);
		void onTabScrollStateChanged(int state);
	}
	
	public interface OnVisibilityChangeListener{
		void onVisibilityChange(int visibility);
	}
	
	public interface OnDismissListener{
		void onDismiss(CustomTabView tabView, boolean isClickDismiss);
	}
	
	public CustomTabView(Context context){
		super(context);
		mRes = context.getResources();
		
		mDisplayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
		
		LinearLayout relLayHorizScrollLinLay;
		
		mList = new ArrayList<View[]>();
		
		this.setOrientation(LinearLayout.VERTICAL);
		this.setBackgroundResource(android.R.color.white);
		this.setGravity(Gravity.CENTER_HORIZONTAL);
		
		mItemWi = LayoutParams.MATCH_PARENT;
		mItemHe = LayoutParams.WRAP_CONTENT;
		mLinLayPar = new LayoutParams(mItemWi, mItemHe);
		mRelLay = new RelativeLayout(context);
		mRelLay.setLayoutParams(mLinLayPar);
		mRelLay.setGravity(Gravity.CENTER_VERTICAL);
		this.addView(mRelLay);
		
		mItemWi = LayoutParams.MATCH_PARENT;
		mItemHe = LayoutParams.WRAP_CONTENT;
		mRelLayPar = new RelativeLayout.LayoutParams(mItemWi, mItemHe);
		mRelLayPar.addRule(RelativeLayout.LEFT_OF, R.id.dismissButton);
		HorizontalScrollView relLayHorizScrollView = new HorizontalScrollView(context){

			@Override
			public boolean onTouchEvent(@NonNull MotionEvent ev) {
				super.onTouchEvent(ev);
				return false;
			}
		};
		relLayHorizScrollView.setLayoutParams(mRelLayPar);
		relLayHorizScrollView.setFillViewport(true);
		relLayHorizScrollView.setHorizontalScrollBarEnabled(false);
		mRelLay.addView(relLayHorizScrollView);
		
		mItemWi = LayoutParams.MATCH_PARENT;
		mItemHe = LayoutParams.MATCH_PARENT;
		relLayHorizScrollLinLay = new LinearLayout(context);
		relLayHorizScrollLinLay.setOrientation(LinearLayout.VERTICAL);
		relLayHorizScrollLinLay.setLayoutParams(new FrameLayout.LayoutParams(mItemWi, mItemHe));
		relLayHorizScrollLinLay.setGravity(Gravity.CENTER_HORIZONTAL);
		relLayHorizScrollView.addView(relLayHorizScrollLinLay);
		
		mRadioGroup = new RadioGroup(context);
		mRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
		mRadioGroup.setGravity(Gravity.CENTER);
		relLayHorizScrollLinLay.addView(mRadioGroup);
		
		mCustomPagerAdapter = new CustomPagerAdapter();
		
		mViewPager = new ViewPager(context);
		mViewPager.setAdapter(mCustomPagerAdapter);
		this.addView(mViewPager);
		
		mRelLay.setOnTouchListener(new OnTouchListener() {
			
			DynamicResizeControl dynamicResizeControl;
			float preRawY;
			int resizeHeight, diffHeight, offsetHeight;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!(mIsDynamicControl && getCurrentContentView() instanceof DynamicResizeControl)) {
					return mOnTabBarTouchListener == null || mOnTabBarTouchListener.onTouch(v, event);
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					preRawY = event.getRawY();
					if (mOnTabBarTouchListener != null) {
						mOnTabBarTouchListener.onTouch(v, event);
					}
					return true;
				}

				dynamicResizeControl = (DynamicResizeControl) getCurrentContentView();
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					diffHeight = (int) (preRawY - event.getRawY());
					resizeHeight = getLayoutParams().height + diffHeight;
					if (resizeHeight > mMaxHeight && mMaxHeight > 0) {
						offsetHeight = mMaxHeight - resizeHeight;
					} else if (resizeHeight < mMinHeight) {
						offsetHeight = mMinHeight - resizeHeight;
					} else {
						offsetHeight = 0;
					}
					getLayoutParams().height = resizeHeight + offsetHeight;
					setLayoutParams(getLayoutParams());
					preRawY = event.getRawY();

					dynamicResizeControl.layoutChange(getLayoutParams().width, diffHeight + offsetHeight);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dynamicResizeControl.layoutChanged(getLayoutParams().width, getLayoutParams().height - getTabBarLayoutParams().height);
					v.performClick();
				}

				return mOnTabBarTouchListener != null && mOnTabBarTouchListener.onTouch(v, event);
			}
		});
		
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mViewPager.setCurrentItem(mCustomPagerAdapter.getItemPosition(group.findViewById(checkedId)));
			}
		});
		
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				setCurrentItem(arg0);
				if(mOnTabChangeListener != null){
					mOnTabChangeListener.onTabSelected(arg0);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if(mOnTabChangeListener != null){
					mOnTabChangeListener.onTabScrolled(arg0, arg1, arg2);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				if(mOnTabChangeListener != null){
					mOnTabChangeListener.onTabScrollStateChanged(arg0);
				}
			}
		});
	}
	
	@Override
	public void setVisibility(int visibility) {
		boolean isChange = getVisibility() != visibility;
		super.setVisibility(visibility);
		if(isChange && mOnVisibilityChangeListener != null){
			mOnVisibilityChangeListener.onVisibilityChange(visibility);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setTabBarBackground(Drawable drawable){
		mRelLay.setBackgroundDrawable(drawable);
	}
	
	public void setTabBarLayoutParams(LayoutParams linLayPar){
		mRelLay.setLayoutParams(linLayPar);
	}
	
	public ViewGroup.LayoutParams getTabBarLayoutParams(){
		return mRelLay.getLayoutParams();
	}
	
	@SuppressWarnings("deprecation")
	public void setHeadGroupBackground(Drawable drawable){
		mRadioGroup.setBackgroundDrawable(drawable);
	}
	
	public void setHeadGroupLayoutParams(LayoutParams linLayPar){
		mRadioGroup.setLayoutParams(linLayPar);
	}
	
	public ViewGroup.LayoutParams getHeadGroupLayoutParams(){
		return mRadioGroup.getLayoutParams();
	}
	
	public void setHeadButtonBackground(Drawable leftDrawable, Drawable rightDrawable, Drawable middleDrawable){
		mLeftDrawable = leftDrawable;
		mRightDrawable = rightDrawable;
		mMiddleDrawable = middleDrawable;
	}
	
	@SuppressWarnings("deprecation")
	public void setHeadButtonBackground(int leftId, int rightId, int middleId){
		mLeftDrawable = ContextCompat.getDrawable(getContext(), leftId);
		mRightDrawable = ContextCompat.getDrawable(getContext(), rightId);
		mMiddleDrawable = ContextCompat.getDrawable(getContext(), middleId);
	}
	
	@SuppressWarnings("deprecation")
	public void setContentGroupBackground(Drawable drawable){
		mViewPager.setBackgroundDrawable(drawable);
	}
	
	public void setContentGroupLayoutParams(LayoutParams linLayPar){
		mViewPager.setLayoutParams(linLayPar);
	}
	
	public ViewGroup.LayoutParams getContentGroupLayoutParams(){
		return mViewPager.getLayoutParams();
	}
	
	public void setTabBarDynamicResizeLimit(int minHeight, int maxHeight){
		mMinHeight = minHeight;
		mMaxHeight = maxHeight;
	}
	
	public void setTabBarDynamicResizeControl(boolean isDynamicControl){
		mIsDynamicControl = isDynamicControl;
	}
	
	public void setTabBarDynamicResizeControl(int minHeight, int maxHeight, boolean isDynamicControl){
		setTabBarDynamicResizeLimit(minHeight, maxHeight);
		setTabBarDynamicResizeControl(isDynamicControl);
	}
	
	public boolean isTabBarDynamicResizeControl(){
		return mIsDynamicControl;
	}
	
	@SuppressWarnings("deprecation")
	public void addTab(CompoundButton headButton, View contentView, int location){
		headButton.setId(R.id.dismissButton + 1 + mList.size());
		
		mList.add(location, new View[]{headButton, contentView});
		mCustomPagerAdapter.notifyDataSetChanged();
		
		int size = mList.size();
		if(size > 1){
			View view;
			for(int i=0; i<size; i++){
				view = mList.get(i)[0];
				if(i == 0){
					view.setBackgroundDrawable(mLeftDrawable);
				}else if(i == size-1){
					view.setBackgroundDrawable(mRightDrawable);
				}else{
					view.setBackgroundDrawable(mMiddleDrawable);
				}
			}
		}else if(size == 1){
			mList.get(0)[0].setBackgroundDrawable(mMiddleDrawable);
		}
		mRadioGroup.addView(headButton, location);
		
		if(mList.size() == 1){
			setCurrentItem(0);
		}
	}
	
	public void addTab(CompoundButton headButton, View contentView){
		addTab(headButton, contentView, mList.size());
	}
	
	public void addTab(CompoundButton[] headButtonArray, View[] contentViewArray){
		for(int i=0; i<headButtonArray.length; i++){
			addTab(headButtonArray[i], contentViewArray[i], mList.size());
		}
	}
	
	public void addTab(String title, View contentView, int location){
		addTab(buildHeadButton(title), contentView, location);
	}
	
	public void addTab(String title, View contentView){
		addTab(buildHeadButton(title), contentView, mList.size());
	}
	
	public void addTab(String[] titleArray, View[] viewArray){
		for(int i=0; i<titleArray.length; i++){
			addTab(buildHeadButton(titleArray[i]), viewArray[i], mList.size());
		}
	}
	
	/**
	 * @param objectArray [[String, View], n]
	 */
	public void addTab(Object[]...objectArray){
		for(Object[] object : objectArray){
			if(object[0] instanceof CompoundButton && object[1] instanceof View){
				addTab((CompoundButton)object[0], (View)object[1], mList.size());
			}else if(object[0] instanceof String && object[1] instanceof View){
				addTab(buildHeadButton((String)object[0]), (View)object[1], mList.size());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void removeTab(int location){
		if(getCurrentItem() == location){
			setCurrentItem(location--);
		}
		
		mRadioGroup.removeViewAt(location);
		mRadioGroup.clearCheck();
		
		View[] viewArray = mList.remove(location);
		viewArray[0] = null;
		viewArray[1] = null;
		mCustomPagerAdapter.notifyDataSetChanged();
		
		View view;
		int size = mList.size();
		for(int i=0; i<size; i++){
			view = mList.get(i)[0];
			if(i == 0){
				view.setBackgroundDrawable(mLeftDrawable);
			}else if(i == size-1){
				view.setBackgroundDrawable(mRightDrawable);
			}else{
				view.setBackgroundDrawable(mMiddleDrawable);
			}
		}
	}
	
	public void removeAllTab(){
		View[] viewArray;
		mViewPager.setCurrentItem(0);
		while(!mList.isEmpty()){
			mRadioGroup.removeViewAt(mRadioGroup.getChildCount()-1);
			viewArray = mList.remove(mList.size()-1);
			viewArray[0] = null;
			if(viewArray[1] instanceof ViewGroup){
				((ViewGroup)viewArray[1]).removeAllViews();
			}
			viewArray[1] = null;
		}
		mRadioGroup.clearCheck();
		mCustomPagerAdapter.notifyDataSetChanged();
	}
	
	public Button buildDismissButton(int width, int height, String text){
		mRelLayPar = new RelativeLayout.LayoutParams(width, height);
		mRelLayPar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		Button button = new Button(getContext());
		button.setLayoutParams(mRelLayPar);
		button.setPadding(0, 0, 0, 0);
		button.setGravity(Gravity.CENTER);
		button.setEllipsize(TruncateAt.END);
		button.setSingleLine(true);
		
		button.setText(text);
		return button;
	}
	
	public Button buildDismissButton(int width, int height, int backgroundId, String text){
		return buildDismissButton(width, height, text);
	}
	
	public Button buildDismissButton(String text){
		mItemWi = LayoutParams.WRAP_CONTENT;
		mItemHe = LayoutParams.WRAP_CONTENT;
		return buildDismissButton(mItemWi, mItemHe, text);
	}
	
	public void setDismissButton(Button button, final boolean isDismissSelf){
		mButton = button;
		if(mButton != null){
			mButton.setId(R.id.dismissButton);
			mRelLay.addView(mButton);
			mRelLayPar = (RelativeLayout.LayoutParams)mRelLay.getChildAt(0).getLayoutParams();
			mRelLayPar.leftMargin = mButton.getLayoutParams().width;
			mButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismiss(true, isDismissSelf);
				}
			});
		}
	}
	
	public void setDismissButton(String text, boolean isDismissSelf){
		setDismissButton(buildDismissButton(text), isDismissSelf);
	}
	
	public Button getDismissButton(){
		return mButton;
	}
	
	public RadioButton buildHeadButton(int width, int height, String title){
		mLinLayPar = new LayoutParams(width, height);
		RadioButton radioButton = new RadioButton(getContext());
		radioButton.setButtonDrawable(android.R.color.transparent);
		radioButton.setLayoutParams(mLinLayPar);
		radioButton.setPadding(0, 0, 0, 0);
		radioButton.setGravity(Gravity.CENTER);
		radioButton.setEllipsize(TruncateAt.END);
		radioButton.setMaxLines(2);
		
		radioButton.setText(title);
		return radioButton;
	}
	
	public RadioButton buildHeadButton(String title){
		mItemWi = LayoutParams.WRAP_CONTENT;
		mItemHe = LayoutParams.WRAP_CONTENT;
		return buildHeadButton(mItemWi, mItemHe, title);
	}
	
	public View getHeadButton(int location){
		return mList.get(location)[0];
	}
	
	public View getCurrentHeadButton(){
		int location = getCurrentItem();
		if(location >= 0 && location < mList.size()){
			return mList.get(location)[0];
		}
		return null;
	}
	
	public void bindContentView(View contentView, int location){
		mList.get(location)[1] = contentView;
		mCustomPagerAdapter.notifyDataSetChanged();
	}
	
	public View getContentView(int location){
		return mList.get(location)[1];
	}
	
	public View getCurrentContentView(){
		int location = getCurrentItem();
		if(location >= 0 && location < mList.size()){
			return mList.get(location)[1];
		}
		return null;
	}
	
	public void removeContentView(int location){
		mList.get(location)[1] = null;
		mCustomPagerAdapter.notifyDataSetChanged();
	}
	
	public void removeAllContentView(){
		int size = mList.size();
		for(int i=0; i<size; i++){
			mList.get(i)[1] = null;
		}
		mCustomPagerAdapter.notifyDataSetChanged();
	}
	
	public int getTabCount(){
		return mCustomPagerAdapter.getCount();
	}
	
	public void setCurrentItem(int item){
		if(item >= 0 && item < mList.size() && mRadioGroup.getCheckedRadioButtonId() != mList.get(item)[0].getId()){
			mRadioGroup.check(mList.get(item)[0].getId());
			mViewPager.setCurrentItem(item);
		}
	}
	
	public int getCurrentItem(){
		return mViewPager.getCurrentItem();
	}
	
	/**
	 * 
	 * @param isClickDismiss must implements OnDismissListener
	 * @param isDismissSelf<br>
	 * true : Dismiss all contain self<br>
	 * false : Only dismiss inside tab, self hidden
	 */
	public void dismiss(boolean isClickDismiss, boolean isDismissSelf){
		this.setVisibility(View.GONE);
		if(mOnDismissListener != null){
			mOnDismissListener.onDismiss(this, isClickDismiss);
		}
		removeAllTab();
		
		if(isDismissSelf){
			mViewPager.clearOnPageChangeListeners();
			mViewPager = null;
			mCustomPagerAdapter = null;
			mButton = null;
			mRadioGroup = null;
			mRelLay = null;
			mLinLayPar = null;
			mRelLayPar = null;
			
			mList = null;
			mOnTabBarTouchListener = null;
			mOnTabChangeListener = null;
			mOnVisibilityChangeListener = null;
			mOnDismissListener = null;
			mLeftDrawable = null; mMiddleDrawable = null; mRightDrawable = null;
			mRes = null;
			mDisplayMetrics = null;
			this.removeAllViews();
		}
	}
	
	public void requestDisallowInterceptContentGroupTouchEvent(boolean disallowIntercept){
		mViewPager.requestDisallowInterceptTouchEvent(disallowIntercept);
	}
	
	public void setOnTabBarTouchListener(OnTouchListener onTouchListener){
		mOnTabBarTouchListener = onTouchListener;
	}
	
	public void setOnHeadGroupTouchListener(OnTouchListener onTouchListener){
		mRadioGroup.setOnTouchListener(onTouchListener);
	}
	
	public void setOnContentGroupTouchListener(OnTouchListener onTouchListener){
		mViewPager.setOnTouchListener(onTouchListener);
	}
	
	public void setOnTabChangeListener(OnTabChangeListener onTabChangeListener){
		mOnTabChangeListener = onTabChangeListener;
	}
	
	public OnTabChangeListener getOnTabChangeListener(){
		return mOnTabChangeListener;
	}
	
	public void setOnVisibilityChangeListener(OnVisibilityChangeListener onVisibilityChangeListener){
		mOnVisibilityChangeListener = onVisibilityChangeListener;
	}
	
	public OnVisibilityChangeListener getOnVisibilityChangeListener(){
		return mOnVisibilityChangeListener;
	}
	
	public void setOnDismissListener(OnDismissListener onDismissListener){
		mOnDismissListener = onDismissListener;
	}
	
	public OnDismissListener getOnDismissListener(){
		return mOnDismissListener;
	}
	
	public class CustomPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public int getItemPosition(Object object) {
			int size = mList.size();
			for(int i=0; i<size; i++){
				if(mList.get(i)[0] == object){
					return i;
				}
			}
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mList.get(position)[1];
			if(view != null){
				container.addView(view);
			}
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if(position >= 0 && position < mList.size()){
				container.removeView(mList.get(position)[1]);
			}
		}
	}
}