package com.flyingwing.android.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.flyingwing.android.R;

public class Main extends CustomActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.layout_relative);
	}
}