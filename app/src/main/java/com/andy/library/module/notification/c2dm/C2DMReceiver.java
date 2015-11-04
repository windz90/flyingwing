package com.andy.library.module.notification.c2dm;

import java.util.Timer;
import java.util.TimerTask;

import com.andy.library.R;
import com.andy.library.module.NetworkAccess;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings.Secure;

public class C2DMReceiver extends BroadcastReceiver{
	
    private static SharedPreferences sp;
    private static SharedPreferences.Editor spEdit;
    private static String senderID;
    
    private Timer timer = new Timer();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")){
			receiveRegistration(context, intent);
		}else if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")){
			receiveNotification(context, intent);
		}
	}
	
	private void receiveRegistration(final Context context, Intent intent){
		String registrationId = intent.getStringExtra("registration_id");
		String errorId = intent.getStringExtra("error");
		String unregister = intent.getStringExtra("unregistered");
		timer.cancel();
		if(registrationId != null){
			saveRegistrationID(context, registrationId);
			sendRegistrationID(context, registrationId);
		}else if(errorId != null){
			if("SERVICE_NOT_AVAILABLE".equals(errorId)){
				final Handler handler = new Handler(new Callback() {

					@Override
					public boolean handleMessage(Message msg) {
						c2dmRegister(context, senderID);
						return false;
					}
				});
				TimerTask task = new TimerTask() {
					
					@Override
					public void run() {
						handler.sendEmptyMessage(0);
					}
				};
				timer = new Timer();
				timer.schedule(task, 0, 60000);
			}
			System.out.println("C2DM Register Fail " + errorId);
		}else if(unregister != null){
			removeRegistrationID(context);
		}
	}
	
	@TargetApi(16)
	private void receiveNotification(Context context, Intent intent){
		Bundle bundle = intent.getExtras();
		bundle.putInt("icon", R.drawable.ic_launcher);
		try{
			NotificationManager notifiManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
			
	        intent = new Intent(context, C2DMPrompt.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.putExtras(bundle);
	        PendingIntent penIntent = PendingIntent.getActivity(context, 0, intent, 0);
			
			Notification.Builder notifiBuilder = new Notification.Builder(context);
			notifiBuilder.setSmallIcon(bundle.getInt("icon"));
			notifiBuilder.setTicker(bundle.getString("title"));
			notifiBuilder.setDefaults(Notification.DEFAULT_ALL);
			notifiBuilder.setContentIntent(penIntent);
			Notification notifi = notifiBuilder.build();
			
//			Notification notifi = new Notification();
//			notifi.icon = bundle.getInt("icon");
//			notifi.tickerText = bundle.getString("title");
//			notifi.defaults = Notification.DEFAULT_ALL;
//			notifi.setLatestEventInfo(context, bundle.getString("title"), bundle.getString("msg"), penIntent);
			
			notifiManager.notify(0, notifi);
			System.out.println("notify end");
		}catch (Exception e) {
			System.out.println("notify fail " + e);
		}
	}
	
	public static void c2dmRegister(Context context, String senderID){
		C2DMReceiver.senderID = senderID;
		Intent intentService = new Intent("com.google.android.c2dm.intent.REGISTER");
		intentService.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		intentService.putExtra("sender", senderID);
		context.startService(intentService);
	}
	
	public static void c2dmUnregister(Context context){
		Intent intentService = new Intent("com.google.android.c2dm.intent.UNREGISTER");
		intentService.putExtra("app", PendingIntent.getBroadcast(context, 0, intentService, 0));
		context.startService(intentService);
	}
	
	public static void sendRegistrationID(final Context context, final String registrationId){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				setSharedPreferences(context);
				String id = "";
				if(sp.getString("loginStatus", "notLogin").equals("loginOk")){
					id = sp.getString("account", "");
				}
				String androidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
				System.out.println("androidID:" + androidID);
				String ver = "";
				try {
					ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
				String urlPost = "http://gmhqappb.pcschool.com.tw/GetGjunInfo.ashx";
				String[][] postData = new String[][]{{"id", id}
				, {"email", androidID}
				, {"PushToken", registrationId}, {"platform", "1"}, {"ver", ver}};
				NetworkAccess.connectUseHttpClient(context, urlPost, postData);
			}
		});
		if(NetworkAccess.isConnect(context)){
			thread.start();
		}else{
			System.out.println("c2dmID Send Fail No Network Connection");
		}
	}
	
	public static void saveRegistrationID(Context context, String registrationId){
		System.out.println("C2DM ID:" + registrationId);
		handleExistRegistrationID(context, registrationId, 1);
	}
	
	public static void removeRegistrationID(Context context){
		handleExistRegistrationID(context, null, 2);
	}
	
	private static void handleExistRegistrationID(Context context, String registrationId, int flag){
		setSharedPreferences(context);
		if(flag == 1){
			spEdit.putString("registrationId", registrationId);
			spEdit.commit();
		}else if(flag == 2){
			spEdit.remove("registrationId");
			spEdit.commit();
		}
	}
	
	private static void setSharedPreferences(Context context){
		if(sp == null){
			sp = context.getSharedPreferences("gjun", Context.MODE_PRIVATE);
		}
		if(spEdit == null){
			spEdit = sp.edit();
		}
	}
}