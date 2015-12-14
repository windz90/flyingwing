package com.flyingwing.base.module.notification.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CustomBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences("gjun", Context.MODE_PRIVATE);
		if(sp.getBoolean("gjunNotify", false)){
			if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
				intent = new Intent(context, MsgService.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(intent);
			}
		}
	}
}