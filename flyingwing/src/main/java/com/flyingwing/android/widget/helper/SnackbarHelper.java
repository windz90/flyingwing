/*
 * Copyright 2017 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.flyingwing.android.R;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SnackbarHelper {

	public static abstract class PendingMakeWindowSnackbarCallback {
		public void onMakeWindowSnackbar(Snackbar snackbar, Snackbar.Callback callbackExit, WindowManager.LayoutParams windowManagerLayoutParams){}
		public void onMakeWindowSnackbar(Snackbar snackbar, WindowManager.LayoutParams windowManagerLayoutParams){}
		public void onMakeWindowSnackbar(Snackbar snackbar){}
	}

	@IntDef({Snackbar.LENGTH_INDEFINITE, Snackbar.LENGTH_SHORT, Snackbar.LENGTH_LONG})
	public @interface Duration {}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void pendingMakeWindowSnackbar(final Context context, final int gravity, @NonNull final CharSequence text, @Duration final int duration
			, final PendingMakeWindowSnackbarCallback pendingMakeWindowSnackbarCallback){
		final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		WindowManager.LayoutParams windowManagerLayoutParams = new WindowManager.LayoutParams();
		windowManagerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		windowManagerLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		windowManagerLayoutParams.gravity = gravity;
		windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		windowManagerLayoutParams.format = PixelFormat.TRANSPARENT;
		View view = new View(context){
			@Override
			protected void onAttachedToWindow() {
				super.onAttachedToWindow();
				tokenViewOnAttachedToWindow(this);
			}

			private void tokenViewOnAttachedToWindow(final View viewToken){
				final WindowManager.LayoutParams windowManagerLayoutParams = new WindowManager.LayoutParams();
				windowManagerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
				windowManagerLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				windowManagerLayoutParams.gravity = gravity;
				windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
				windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
				windowManagerLayoutParams.format = PixelFormat.TRANSPARENT;
				windowManagerLayoutParams.token = getWindowToken();
				CoordinatorLayout coordinatorLayout = new CoordinatorLayout(new ContextThemeWrapper(context.getApplicationContext(), R.style.Theme_AppCompat)){

					Snackbar snackbar;

					@Override
					public boolean dispatchKeyEvent(KeyEvent event) {
						if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
							snackbar.dismiss();
						}
						return super.dispatchKeyEvent(event);
					}

					@Override
					public void onAttachedToWindow() {
						super.onAttachedToWindow();
						snackbarContainerOnAttachedToWindow(this);
					}

					private void snackbarContainerOnAttachedToWindow(final ViewGroup viewGroupSnackbarContainer){
						snackbar = Snackbar.make(this, text, duration);
						Snackbar.Callback callbackExit = new Snackbar.Callback() {
							@Override
							public void onDismissed(Snackbar transientBottomBar, int event) {
								super.onDismissed(transientBottomBar, event);
								if(viewToken.getParent() != null && viewGroupSnackbarContainer.getParent() != null){
									windowManager.removeView(viewGroupSnackbarContainer);
									windowManager.removeView(viewToken);
								}
							}
						};
						pendingMakeWindowSnackbarCallback.onMakeWindowSnackbar(snackbar, callbackExit, windowManagerLayoutParams);
						pendingMakeWindowSnackbarCallback.onMakeWindowSnackbar(snackbar, windowManagerLayoutParams);
						pendingMakeWindowSnackbarCallback.onMakeWindowSnackbar(snackbar);
					}
				};
				windowManager.addView(coordinatorLayout, windowManagerLayoutParams);
			}
		};
		windowManager.addView(view, windowManagerLayoutParams);
		view.setVisibility(View.GONE);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void pendingMakeWindowSnackbar(Context context, @NonNull CharSequence text, @Duration int duration
			, PendingMakeWindowSnackbarCallback pendingMakeWindowSnackbarCallback){
		pendingMakeWindowSnackbar(context, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, text, duration, pendingMakeWindowSnackbarCallback);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void pendingMakeWindowSnackbar(Context context, int gravity, @StringRes int resId, @Duration int duration
			, PendingMakeWindowSnackbarCallback pendingMakeWindowSnackbarCallback){
		pendingMakeWindowSnackbar(context, gravity, context.getString(resId), duration, pendingMakeWindowSnackbarCallback);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void pendingMakeWindowSnackbar(Context context, @StringRes int resId, @Duration int duration
			, PendingMakeWindowSnackbarCallback pendingMakeWindowSnackbarCallback){
		pendingMakeWindowSnackbar(context, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, context.getString(resId), duration, pendingMakeWindowSnackbarCallback);
	}
}