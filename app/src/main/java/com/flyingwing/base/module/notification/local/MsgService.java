package com.flyingwing.base.module.notification.local;

import java.util.Calendar;

import com.flyingwing.base.R;
import com.flyingwing.base.R.drawable;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

public class MsgService extends Service {
	
	static String gjunMsg = "NewMsg";
	
	private AlarmManager am;
	private PendingIntent penIntent;
	
	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("onCreateService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences sp = getSharedPreferences("gjun", Context.MODE_PRIVATE);
		if(sp.getBoolean("gjunNotify", false)){
			Bundle bundle = intent.getExtras();
			if(bundle == null){
				System.out.println("onStartCommandService");
				flags = START_NOT_STICKY;
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.DATE, 1);
				cal.set(Calendar.HOUR_OF_DAY, 12);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				
		        intent = new Intent(this, MsgService.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        bundle = new Bundle();
		        bundle.putBoolean("timeWake", true);
		        intent.putExtras(bundle);
				penIntent = PendingIntent.getService(this, 0, intent, 0);
				
				AlarmManager am = (AlarmManager)this.getSystemService(ALARM_SERVICE);
				am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24 * 60 * 60 * 1000, penIntent);
			}else if(bundle.getBoolean("timeWake") == true){
				// 方法一，Service與Activity互相溝通，由MsgService呼叫MsgManager註冊廣播接收，MsgManager註冊後告知MsgService發送廣播，MsgManager收到廣播訊息後發送Notify
//				if(bundle.getString("managerStatus") == null){
//					intent = new Intent(this, MsgManager.class);
//			        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			        startActivity(intent);
//			        System.out.println("waitManaging");
//				}else if(bundle.getString("managerStatus").equals("registerReceiver")){
//					bundle = new Bundle();
//					bundle.putInt("icon", R.drawable.icon_i);
//					bundle.putString("iconTxt", "notify");
//					bundle.putString("title", "title");
//					bundle.putString("msg", "test");
//					intent = new Intent(gjunMsg);
//					intent.putExtras(bundle);
//					sendBroadcast(intent);
//					System.out.println("sendMsg");
//				}
				
				// 方法二，Service單獨運作，直接將訊息發送Notify
				bundle = new Bundle();
				bundle.putInt("icon", R.drawable.overlay_mark_self);
				bundle.putString("iconTxt", "notify");
				bundle.putString("title", "title");
				bundle.putString("msg", "test");
				System.out.println("sendMsg");
				M_notification(bundle);
			}
		}else{
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		am.cancel(penIntent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private void M_notification(Bundle bundle){
		try{
			NotificationManager notifiManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Notification notifi = new Notification();
			notifi.icon = bundle.getInt("icon");
			notifi.tickerText = bundle.getString("iconTxt");
			notifi.defaults = Notification.DEFAULT_ALL;
			
	        Intent intent = new Intent(MsgService.this, MsgPrompt.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.putExtras(bundle);
			penIntent = PendingIntent.getActivity(MsgService.this, 0, intent, 0);
			
			notifi.setLatestEventInfo(MsgService.this, bundle.getString("title"), bundle.getString("msg"), penIntent);
			notifiManager.notify(0, notifi);
			System.out.println("notify end");
		}catch (Exception e) {
			System.out.println("notify fail " + e);
		}
	}
}