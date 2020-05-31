/*
 * Copyright 2016 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

// rewrite from FloatingActionButton.Behavior
@SuppressWarnings("unused")
public class DefaultBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

	private float mViewTranslationY;

	@Override
	public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
		if (dependency instanceof Snackbar.SnackbarLayout) {
			updateViewTranslationForSnackbar(parent, child);
		}
		return false;
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	private void updateViewTranslationForSnackbar(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V view) {
		if (view.getVisibility() != View.VISIBLE) {
			return;
		}
		final float targetTransY = getViewTranslationYForSnackbar(coordinatorLayout, view);
		if (mViewTranslationY == targetTransY) {
			return;
		}

		view.setTranslationY(targetTransY);
		mViewTranslationY = targetTransY;
	}

	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	private float getViewTranslationYForSnackbar(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V view) {
		float minOffset = 0;
		final List<View> dependencies = coordinatorLayout.getDependencies(view);
		for (int i = 0, z = dependencies.size(); i < z; i++) {
			final View childView = dependencies.get(i);
			if (childView instanceof Snackbar.SnackbarLayout && coordinatorLayout.doViewsOverlap(view, childView)) {
				minOffset = Math.min(minOffset, childView.getTranslationY() - childView.getHeight());
			}
		}

		return minOffset;
	}
}