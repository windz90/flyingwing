package com.flyingwing.android.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.flyingwing.android.R;

public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.layout_relative);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_flyingwing, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				callNavigateUpTo();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callNavigateUpTo(){
		String className = NavUtils.getParentActivityName(this);
		if(getIntent().getAction() != null){
			className = getIntent().getAction();
		}
		if(className != null){
			Intent intent = new Intent();
			intent.setClassName(this, className);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			NavUtils.navigateUpTo(this, intent);
		}
	}
}