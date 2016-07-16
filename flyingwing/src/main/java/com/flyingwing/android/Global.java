package com.flyingwing.android;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.flyingwing.android.view.Main;

@SuppressWarnings("unused")
public class Global extends MultiDexApplication {

	public static final String SP_NAME = "FlyingWing";

	@SuppressLint("PrivateResource")
	public static Toolbar getToolbar(Context context, Drawable backgroundDrawable){
		int itemWi = ViewGroup.LayoutParams.MATCH_PARENT;
		int itemHe = context.getResources().getDimensionPixelSize(R.dimen.toolbarHeight);
		ViewGroup.MarginLayoutParams viewGroupMarginLayPar = new ViewGroup.MarginLayoutParams(itemWi, itemHe);
		Toolbar toolbar = new Toolbar(context);
		toolbar.setId(R.id.toolbar);
		if(backgroundDrawable != null){
			//noinspection deprecation
			toolbar.setBackgroundDrawable(backgroundDrawable);
		}
		toolbar.setLayoutParams(viewGroupMarginLayPar);
		toolbar.setContentInsetsRelative(0, 0);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
//		toolbar.setTitleTextColor(Color.BLACK);
		return toolbar;
	}

	public static void sendNotification(Context context, String ticker, String contentTitle, String contentText, int color, Long when
			, boolean isAutoCancel, boolean isQuiet, String tag, int id){
		Intent intentTo = new Intent(context, Main.class);
		intentTo.setAction(Intent.ACTION_MAIN);
		intentTo.addCategory(Intent.CATEGORY_LAUNCHER);
		intentTo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentTo.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intentTo, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(context);
		notificationCompatBuilder.setTicker(ticker);
		notificationCompatBuilder.setContentTitle(contentTitle);
		notificationCompatBuilder.setContentText(contentText);
		notificationCompatBuilder.setColor(color);
		notificationCompatBuilder.setSmallIcon(R.mipmap.ic_launcher);
		if(when != null){
			notificationCompatBuilder.setWhen(when);
		}
		notificationCompatBuilder.setAutoCancel(isAutoCancel);
		if(isQuiet){
			notificationCompatBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
		}else{
			notificationCompatBuilder.setDefaults(Notification.DEFAULT_ALL);
		}
		notificationCompatBuilder.setContentIntent(pendingIntent);

		Notification notification = notificationCompatBuilder.build();

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
		notificationManager.notify(tag, id, notification);
	}
}