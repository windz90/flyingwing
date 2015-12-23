package com.flyingwing.base;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Window;

import com.flyingwing.BuildConfig;
import com.flyingwing.R;
import com.flyingwing.base.net.NetworkUtils;
import com.flyingwing.base.util.DisplayUtils;
import com.flyingwing.base.util.DisplayUtils.EventCallback;
import com.flyingwing.base.util.Utils;
import com.flyingwing.base.view.Main;
import com.flyingwing.base.widget.CustomProgressDialog;

public class First extends Activity {

	public static final String SP_NAME = "test";

	private boolean isLoaded;
	private boolean isGetVisibleHe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.permitDiskReads()
					.penaltyLog()
					.build());

			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build());
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.layout_relative);

		DisplayUtils.setVisibleHeightWaitOnDraw(this, new EventCallback() {

			@Override
			public void completed(int visibleHe) {
				Utils.putSharedPreferences(First.this, SP_NAME, Utils.SP_KEY_STATUS_BAR_HEIGHT
						, DisplayUtils.getDisplayMetricsFromWindowManager(First.this).heightPixels - visibleHe, null);

				isGetVisibleHe = true;
				if (isLoaded) {
					goHome();
				}
			}
		});

		if(!NetworkUtils.isAvailable(this)){
			isLoaded = true;
			if(isGetVisibleHe){
				goHome();
			}
			return;
		}

		CustomProgressDialog.getInstance(this);
		CustomProgressDialog.getInstanceDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				isLoaded = true;
				if (isGetVisibleHe) {
					goHome();
				}
			}
		});

		new Thread(new Runnable() {

			@Override
			public void run() {
				isLoaded = true;
				if(isGetVisibleHe){
					goHome();
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		CustomProgressDialog.dismissInstance();
		super.onDestroy();
	}

	private void goHome(){
		CustomProgressDialog.dismissInstance();
		Intent intent = new Intent(this, Main.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		finish();
	}
}