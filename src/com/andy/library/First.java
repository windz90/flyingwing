package com.andy.library;

import com.andy.library.R;
import com.andy.library.module.C_display;
import com.andy.library.module.C_networkAccess;
import com.andy.library.module.Utils;
import com.andy.library.module.C_display.EventCallBack;
import com.andy.library.module.widget.C_progressDialog;
import com.andy.library.view.Main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class First extends Activity {
	
	private boolean isLoaded;
	private boolean isGetVisibleHe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.first);
        
		C_display.setVisibleHeightWaitOnDraw(this, new EventCallBack() {
			
			@Override
			public void completed(int visibleHe) {
				Utils.putSharedPreferences(First.this, Main.SP_NAME, Utils.SP_KEY_STATUSBAR_HEIGHT, visibleHe, null);
				
				isGetVisibleHe = true;
				if(isLoaded && isGetVisibleHe){
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
        
        if(C_networkAccess.isConnect(this)){
        	C_progressDialog.getInstance(this);
        	C_progressDialog.getInstanceDialog().setOnCancelListener(new OnCancelListener() {
				
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
		C_progressDialog.dismissInstance();
		super.onDestroy();
	}
    
	private void goHome(){
		C_progressDialog.dismissInstance();
        Intent intent = new Intent(this, Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
	}
}