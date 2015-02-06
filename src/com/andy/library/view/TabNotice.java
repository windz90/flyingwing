package com.andy.library.view;

import com.andy.library.R;
import com.andy.library.module.C_imageProcessor;

import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

public class TabNotice extends TabActivity {
	
	private Display display;
	private DisplayMetrics dm;
	private Resources res;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.tabhost);
		
		res = getResources();
		
		// 準備空間測量工具
		display = this.getWindowManager().getDefaultDisplay();
		dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		// 版面空間配置
		int itemWi, itemHe;
		RelativeLayout.LayoutParams relLayPar;
		
		final TabHost tabHost = this.getTabHost();
		final TabWidget tabWidget = tabHost.getTabWidget();
		TabHost.TabSpec tabSpec;
		Intent intent = null;
		
		itemWi = display.getWidth();
		itemHe = (int)(61.5f * 1.0f * dm.density);
		relLayPar = new RelativeLayout.LayoutParams(itemWi, itemHe);
		tabWidget.setLayoutParams(relLayPar);
		
		tabWidget.setPadding(0, 0, 0, 3);
		tabWidget.setBackgroundResource(R.drawable.back00);
		
//		intent = new Intent(this, TabNoticeBulletin.class);
//		intent.putExtra("selectedTab", 0);
		tabSpec = tabHost.newTabSpec("bulletin");
		tabSpec.setIndicator(buildTabView(0));
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);
		
//		intent = new Intent(this, TabNoticeNotify.class);
//		intent.putExtra("selectedTab", 1);
		tabSpec = tabHost.newTabSpec("classNotify");
		tabSpec.setIndicator(buildTabView(1));
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);
		
		tabHost.setCurrentTab(0);
		selectedTabAction(tabHost, 0, true);
		selectedTabAction(tabHost, 1, false);
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals("bulletin")){
					selectedTabAction(tabHost, 0, true);
					selectedTabAction(tabHost, 1, false);
				}else if(tabId.equals("classNotify")){
					selectedTabAction(tabHost, 0, false);
					selectedTabAction(tabHost, 1, true);
				}
			}
		});
	}
	
	private void selectedTabAction(TabHost tabHost, int tabNum, boolean isSelect){
		LinearLayout tabhostLinLay = (LinearLayout)tabHost.getTabWidget().getChildAt(tabNum).findViewById(R.id.tabhostLinLay);
		ImageView tabhostImg = (ImageView)tabHost.getTabWidget().getChildAt(tabNum).findViewById(R.id.tabhostImg);
		tabhostLinLay.setBackgroundResource(R.color.BlackAlphaCC);
		
		switch (tabNum) {
		case 0:
			tabhostLinLay.setGravity(Gravity.RIGHT);
			if(isSelect){
				tabhostImg.setImageBitmap(C_imageProcessor.getRawBitmap(res, R.drawable.bulletin_tab1down, 1));
			}else{
				tabhostImg.setImageBitmap(C_imageProcessor.getRawBitmap(res, R.drawable.bulletin_tab1, 1));
			}
			break;
		case 1:
			tabhostLinLay.setGravity(Gravity.LEFT);
			if(isSelect){
				tabhostImg.setImageBitmap(C_imageProcessor.getRawBitmap(res, R.drawable.bulletin_tab2down, 1));
			}else{
				tabhostImg.setImageBitmap(C_imageProcessor.getRawBitmap(res, R.drawable.bulletin_tab2, 1));
			}
			break;
		}
	}
	
	private View buildTabView(int arg){
		LayoutInflater inflater = LayoutInflater.from(this);
		View itemView = inflater.inflate(R.layout.tabhost_view, null);
		
		LinearLayout tabhostLinLay = (LinearLayout)itemView.findViewById(R.id.tabhostLinLay);
		ImageView tabhostImg = (ImageView)itemView.findViewById(R.id.tabhostImg);
		
		// 版面空間配置
		int itemWi, itemHe;
		LinearLayout.LayoutParams linLayPar;
		
		itemWi = display.getWidth() / 2;
		itemHe = (int)(61.5f * 1.0f * dm.density);
		linLayPar = new LayoutParams(itemWi, itemHe);
		tabhostLinLay.setLayoutParams(linLayPar);
		
		itemWi = display.getWidth() / 3;
		linLayPar = new LayoutParams(itemWi, itemHe);
		tabhostImg.setLayoutParams(linLayPar);
		return tabhostLinLay;
	}
}