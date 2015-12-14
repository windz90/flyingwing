package com.flyingwing.base.module.notification.local;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class MsgManager extends Activity {
	
	private String managerStatus = "init";
	private C_serviceReceiver broadcastR;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		managerStatus = "onCreate";
		System.out.println("onCreateManager");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		try{
			IntentFilter intentF = new IntentFilter(MsgService.gjunMsg);
			broadcastR = new C_serviceReceiver();
			registerReceiver(broadcastR, intentF);
			managerStatus = "registerReceiver";
			System.out.println("registerReceiver");
		}catch (Exception e) {
			managerStatus = "register fail";
			System.out.println("register fail " + e);
		}
		Intent intentService = new Intent(this, MsgService.class);
		intentService.putExtra("timeWake", true);
		intentService.putExtra("managerStatus", managerStatus);
		startService(intentService);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(managerStatus.equals("register fail")){
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		managerStatus = "onPause";
		try{
			unregisterReceiver(broadcastR);
			managerStatus = "unregisterReceiver";
			System.out.println("unregisterReceiver");
		}catch (Exception e) {
			System.out.println(e);
			managerStatus = "unregister fail";
		}
	}
	
	public class C_serviceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			managerStatus = "onReceive";
			System.out.println("onReceiveManager");
			Bundle bundle = intent.getExtras();
			M_notification(bundle);
			finish();
		}
		
		private void M_notification(Bundle bundle){
			try{
				NotificationManager notifiManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				Notification notifi = new Notification();
				notifi.icon = bundle.getInt("icon");
				notifi.tickerText = bundle.getString("iconTxt");
				notifi.defaults = Notification.DEFAULT_ALL;
				
		        Intent intent = new Intent(MsgManager.this, MsgPrompt.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        intent.putExtras(bundle);
				PendingIntent penIntent = PendingIntent.getActivity(MsgManager.this, 0, intent, 0);
				
				notifi.setLatestEventInfo(MsgManager.this, bundle.getString("title"), bundle.getString("msg"), penIntent);
				notifiManager.notify(0, notifi);
				managerStatus = "notify end";
				System.out.println("notify end");
			}catch (Exception e) {
				managerStatus = "notify fail";
				System.out.println("notify fail " + e);
			}
		}
	}
}