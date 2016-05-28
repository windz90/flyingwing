/*
 * Copyright 2016 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.widget;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.View;

import java.util.List;

// rewrite from FloatingActionButton.Behavior
public class DefaultBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

	private float mViewTranslationY;

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		if (dependency instanceof Snackbar.SnackbarLayout) {
			updateViewTranslationForSnackbar(parent, child);
		}
		return false;
	}

	private void updateViewTranslationForSnackbar(CoordinatorLayout coordinatorLayout, View view) {
		if (view.getVisibility() != View.VISIBLE) {
			return;
		}
		final float targetTransY = getViewTranslationYForSnackbar(coordinatorLayout, view);
		if (mViewTranslationY == targetTransY) {
			return;
		}

		ViewCompat.setTranslationY(view, targetTransY);
		mViewTranslationY = targetTransY;
	}

	private float getViewTranslationYForSnackbar(CoordinatorLayout coordinatorLayout, View view) {
		float minOffset = 0;
		final List<View> dependencies = coordinatorLayout.getDependencies(view);
		for (int i = 0, z = dependencies.size(); i < z; i++) {
			final View childView = dependencies.get(i);
			if (childView instanceof Snackbar.SnackbarLayout && coordinatorLayout.doViewsOverlap(view, childView)) {
				minOffset = Math.min(minOffset, ViewCompat.getTranslationY(childView) - childView.getHeight());
			}
		}

		return minOffset;
	}
}