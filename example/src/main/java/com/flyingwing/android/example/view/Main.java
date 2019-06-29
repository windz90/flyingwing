package com.flyingwing.android.example.view;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.flyingwing.android.example.R;
import com.flyingwing.android.util.Utils;

public class Main extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			try {
				getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
				getWindow().setExitTransition(new Slide(GravityCompat.START));
				getWindow().setReenterTransition(new Slide(GravityCompat.START));
				getWindow().setEnterTransition(new Explode());
				getWindow().setReturnTransition(new Explode());

				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
			} catch (Exception e) {
				if(getClass().getName().equals(First.class.getName())){
					Toast.makeText(this, "No support material design some setting, " + e.toString(), Toast.LENGTH_LONG).show();
				}
			}
//		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.layout_relative);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_example, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Utils.callNavigateUpTo(this);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}