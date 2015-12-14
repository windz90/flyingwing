package com.flyingwing.base;

import com.flyingwing.base.R;
import com.flyingwing.base.module.DisplayTool;
import com.flyingwing.base.module.NetworkAccess;
import com.flyingwing.base.module.Utils;
import com.flyingwing.base.module.DisplayTool.EventCallBack;
import com.flyingwing.base.module.widget.CustomProgressDialog;
import com.flyingwing.base.view.Main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class First extends Activity {
	
    public static final String SP_NAME = "test";
	
	private boolean isLoaded;
	private boolean isGetVisibleHe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.first);
        
		DisplayTool.setVisibleHeightWaitOnDraw(this, new EventCallBack() {

			@Override
			public void completed(int visibleHe) {
				Utils.putSharedPreferences(First.this, SP_NAME, Utils.SP_KEY_STATUSBAR_HEIGHT, visibleHe, null);

				isGetVisibleHe = true;
				if (isLoaded && isGetVisibleHe) {
					goHome();
				}
			}
		});
        
        Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
	        	isLoaded = true;
	        	if(isLoaded && isGetVisibleHe){
	        		goHome();
	        	}
			}
		});
        
        if(NetworkAccess.isConnect(this)){
        	CustomProgressDialog.getInstance(this);
        	CustomProgressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					isLoaded = true;
					if(isLoaded && isGetVisibleHe){
						goHome();
					}
				}
			});
        	
        	thread.start();
        }else{
			isLoaded = true;
			if(isLoaded && isGetVisibleHe){
				goHome();
			}
        }
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