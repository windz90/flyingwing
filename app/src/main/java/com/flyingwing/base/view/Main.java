package com.flyingwing.base.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.flyingwing.R;

public class Main extends CustomActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.layout_relative);
	}
}