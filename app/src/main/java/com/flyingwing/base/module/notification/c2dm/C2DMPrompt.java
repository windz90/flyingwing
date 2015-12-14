package com.flyingwing.base.module.notification.c2dm;

import com.flyingwing.base.R;
import com.flyingwing.base.view.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class C2DMPrompt extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		System.out.println("openMsg");
		Bundle bundle = getIntent().getExtras();
		AlertDialog.Builder msgAlert = new AlertDialog.Builder(this);
		msgAlert.setTitle(bundle.getString("title"));
		msgAlert.setMessage(bundle.getString("msg"));
		
		DialogInterface.OnClickListener dialogClick = new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == AlertDialog.BUTTON_POSITIVE){
		            Intent intent = new Intent(C2DMPrompt.this, Main.class);
		            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		            startActivity(intent);
		            finish();
				}else{
					finish();
				}
			}
		};
		
		msgAlert.setPositiveButton(getResources().getString(R.string.open), dialogClick);
		msgAlert.setNegativeButton(getResources().getString(R.string.exit), dialogClick);
		msgAlert.show();
	}
}