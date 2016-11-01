package com.flyingwing.android;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.flyingwing.android.view.Main;

@SuppressWarnings("unused")
public class Global extends MultiDexApplication {

	public static final String SP_NAME = "FlyingWing";

	@SuppressLint("PrivateResource")
	public static Toolbar getToolbar(Context context, Drawable backgroundDrawable, int containsStatusHeight){
		int itemWi = ViewGroup.LayoutParams.MATCH_PARENT;
		int itemHe = context.getResources().getDimensionPixelSize(R.dimen.toolbarHeight);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && containsStatusHeight > 0){
			itemHe += containsStatusHeight;
		}
		ViewGroup.MarginLayoutParams viewGroupMarginLayoutParams = new ViewGroup.MarginLayoutParams(itemWi, itemHe);
		Toolbar toolbar = new Toolbar(context);
		toolbar.setId(R.id.toolbar);
		if(backgroundDrawable != null){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				toolbar.setBackground(backgroundDrawable);
			}else{
				//noinspection deprecation
				toolbar.setBackgroundDrawable(backgroundDrawable);
			}
		}
		toolbar.setLayoutParams(viewGroupMarginLayoutParams);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && containsStatusHeight > 0){
			toolbar.setPadding(0, containsStatusHeight, 0, 0);
		}
		toolbar.setContentInsetsRelative(0, 0);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
//		toolbar.setTitleTextColor(Color.BLACK);
		return toolbar;
	}
}