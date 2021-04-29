package com.flyingwing.android.example.view;

import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.flyingwing.android.example.R;

public class Base extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			try {
				window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
				window.setExitTransition(new Slide(Gravity.START));
				window.setReenterTransition(new Slide(Gravity.START));
				window.setEnterTransition(new Explode());
				window.setReturnTransition(new Explode());

				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
				window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
			} catch (Exception e) {
				if(getClass().getName().equals(First.class.getName())){
					Toast.makeText(this, "Some UI settings are not supported.", Toast.LENGTH_LONG).show();
				}
			}
//		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}
}