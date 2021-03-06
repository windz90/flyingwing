package com.flyingwing.android.example.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Window;

import androidx.multidex.BuildConfig;

import com.flyingwing.android.R;
import com.flyingwing.android.example.Global;
import com.flyingwing.android.net.NetworkAccess;
import com.flyingwing.android.util.IOUtils;
import com.flyingwing.android.util.Utils;
import com.flyingwing.android.view.DisplayUtils;
import com.flyingwing.android.view.DisplayUtils.MeasureCallback;
import com.flyingwing.android.widget.CustomProgressDialog;

public class First extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		NetworkAccess.setPrintConnectRequest(false);
		NetworkAccess.setPrintConnectResponse(false);

		DisplayUtils.measureUsableHeightWaitOnDraw(this, new MeasureCallback() {

			@Override
			public void completed(int statusBarHe, int usableHe) {
				IOUtils.writeSharedPreferencesCommitAsync(First.this, Global.SP_NAME, DisplayUtils.SP_KEY_STATUS_BAR_HEIGHT, statusBarHe, null);
				CustomProgressDialog.dismissInstance();
				startActivity(Utils.getIntentFromHistoryStackToFront(First.this, Main.class));
				finish();
			}
		});

		CustomProgressDialog.getInstance(this);
	}

	@Override
	protected void onDestroy() {
		CustomProgressDialog.dismissInstance();
		super.onDestroy();
	}
}