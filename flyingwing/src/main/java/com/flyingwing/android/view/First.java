package com.flyingwing.android.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.multidex.BuildConfig;
import android.view.Window;

import com.flyingwing.android.Global;
import com.flyingwing.android.R;
import com.flyingwing.android.graphics.ImageProcessor;
import com.flyingwing.android.net.NetworkAccess;
import com.flyingwing.android.util.DisplayUtils;
import com.flyingwing.android.util.DisplayUtils.MeasureCallback;
import com.flyingwing.android.util.Utils;
import com.flyingwing.android.widget.CustomProgressDialog;

public class First extends Activity {

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

		NetworkAccess.setPrintConnectionUrl(BuildConfig.DEBUG);
		NetworkAccess.setPrintConnectException(BuildConfig.DEBUG);
		ImageProcessor.setPrintLoadStreamException(false);

		DisplayUtils.measureUsableHeightWaitOnDraw(this, new MeasureCallback() {

			@Override
			public void completed(int statusBarHe, int usableHe) {
				Utils.writeSharedPreferencesCommitAsync(First.this, Global.SP_NAME, Utils.SP_KEY_STATUS_BAR_HEIGHT, statusBarHe, null);
				next();
			}
		});

		CustomProgressDialog.getInstance(this);
	}

	@Override
	protected void onDestroy() {
		CustomProgressDialog.dismissInstance();
		super.onDestroy();
	}

	private void next(){
		CustomProgressDialog.dismissInstance();
		Intent intent = new Intent(this, Main.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		finish();
	}
}