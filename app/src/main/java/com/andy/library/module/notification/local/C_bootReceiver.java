package com.andy.library.module.notification.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class C_bootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences("gjun", Context.MODE_PRIVATE);
		if(sp.getBoolean("gjunNotify", false)){
			if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
				intent = new Intent(context, C_msgService.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(intent);
			}
		}
	}
}