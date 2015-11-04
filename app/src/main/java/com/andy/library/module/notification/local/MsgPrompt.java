package com.andy.library.module.notification.local;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class MsgPrompt extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		System.out.println("openMsg");
		Bundle bundle = getIntent().getExtras();
		AlertDialog.Builder msgAlertB = new AlertDialog.Builder(this);
		msgAlertB.setTitle(bundle.getString("iconTxt"));
		msgAlertB.setMessage(bundle.getString("title") + "\n" + bundle.getString("msg"));
		
		DialogInterface.OnClickListener dialogClick = new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
		
		msgAlertB.setNegativeButton("確定", dialogClick);
		msgAlertB.show();
	}
}