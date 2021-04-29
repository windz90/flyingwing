/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 4.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.flyingwing.android.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach", "Convert2Diamond"})
public class UIUtils {

	public static final int SIZE_TEXT_S = 10;
	public static final int SIZE_BULLET_S = 11;
	public static final int SIZE_TITLE_S = 12;
	// Above is not recommended
	public static final int SIZE_TAB_S = 13;
	public static final int SIZE_SUBJECT_S = 14;
	public static final int SIZE_TEXT = 15;
	public static final int SIZE_BULLET = 16;
	public static final int SIZE_TITLE = 17;
	public static final int SIZE_TAB = 18;
	public static final int SIZE_SUBJECT = 19;
	public static final int SIZE_TEXT_L = 20;
	public static final int SIZE_BULLET_L = 21;
	public static final int SIZE_TITLE_L = 22;
	public static final int SIZE_TAB_L = 23;
	public static final int SIZE_SUBJECT_L = 24;
	public static final int SIZE_TEXT_XL = 25;
	public static final int SIZE_BULLET_XL = 26;
	public static final int SIZE_TITLE_XL = 27;
	public static final int SIZE_TAB_XL = 28;
	public static final int SIZE_SUBJECT_XL = 29;

	public static void setTextSizeFix(Context context, TextView textView, int unit, float textSize){
		DisplayMetrics displayMetricsFromWindowManager = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		if(windowManager == null){
			textView.setTextSize(unit, textSize);
			return;
		}
		windowManager.getDefaultDisplay().getMetrics(displayMetricsFromWindowManager);
		DisplayMetrics displayMetricsFromResources = context.getResources().getDisplayMetrics();
		if(displayMetricsFromWindowManager.scaledDensity == displayMetricsFromResources.scaledDensity){
			textView.setTextSize(unit, textSize);
			return;
		}
		// Reflection反射調用private方法
		try {
			@SuppressLint("DiscouragedPrivateApi")
			Method method = textView.getClass().getDeclaredMethod("setRawTextSize", float.class, boolean.class);
			method.setAccessible(true);
			method.invoke(textView, TypedValue.applyDimension(unit, textSize, displayMetricsFromWindowManager), true);
			method.setAccessible(false);
		} catch (Exception e) {
			float sizeRaw = TypedValue.applyDimension(unit, textSize, displayMetricsFromWindowManager);
			if(sizeRaw == textView.getTextSize()){
				return;
			}
			textView.getPaint().setTextSize(sizeRaw);
			// Adjust layout
			textView.setEllipsize(textView.getEllipsize());
			e.printStackTrace();
		}
	}

	public static void setTextSizeFix(Context context, TextView textView, float textSize){
		setTextSizeFix(context, textView, TypedValue.COMPLEX_UNIT_SP, textSize);
	}

	public static float getTextSize(float textSize, boolean isBigScreen, float offsetSize){
		if(isBigScreen){
			textSize = textSize + offsetSize;
		}
		return textSize;
	}

	public static float getTextSize(float textSize, boolean isBigScreen){
		return getTextSize(textSize, isBigScreen, 3);
	}

	/**
	 * @param textView {@link TextView#getLayoutParams()}.{@link ViewGroup.LayoutParams#width} or {@link View#getWidth()} must have actual width value.
	 */
	public static float adjustSingleLineTextSizeFitWidth(Context context, TextView textView, String text){
		int width = 0;
		if(textView.getLayoutParams().width > 0){
			width = textView.getLayoutParams().width;
		}else if(textView.getWidth() > 0){
			width = textView.getWidth();
		}
		if(width == 0){
			return 0;
		}

		Paint paint = textView.getPaint();
		int length = text.length();
		int widthDiff = (int) (width - paint.measureText(text, 0, length));
		if(widthDiff == 0){
			return 0;
		}
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		paint.setTextSize(paint.getTextSize() + (int) (widthDiff / displayMetrics.scaledDensity));
		widthDiff = (int) (width - paint.measureText(text, 0, length));
		paint.setTextSize(paint.getTextSize() + (int) (widthDiff / displayMetrics.scaledDensity));

		while (paint.measureText(text, 0, length) < width) {
			paint.setTextSize(paint.getTextSize() + 1);
		}
		while (paint.measureText(text, 0, length) > width) {
			paint.setTextSize(paint.getTextSize() - 1);
		}
		// Adjust layout
		textView.setEllipsize(textView.getEllipsize());
		return paint.getTextSize();
	}

	public static float getTextWidths(Paint textViewPaint, String text){
		/*
		 * 1.
		 * width = TextView.getPaint().measureText(text);
		 *
		 * 2.
		 * width = Layout.getDesiredWidth(text, textPaint);
		 *
		 * 3.
		 * width = new StaticLayout(text, textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true).getLineWidth(0);
		 *
		 * 4.minimal bounds
		 * Rect rect = new Rect();
		 * paint.getTextBounds(text, 0, text.length(), rect);
		 * width = rect.width();
		 */

		float width = 0;
		if(TextUtils.isEmpty(text)){
			return 0;
		}

		int length = text.length();
		float[] widths = new float[length];
		textViewPaint.getTextWidths(text, widths);
		for(int i=0; i<length; i++){
			width = width + (float)Math.ceil(widths[i]);
		}
		return width;
	}

	public static float getTextWidths(TextView textView, String text){
		return getTextWidths(textView.getPaint(), text);
	}

	/**
	 * Inaccurate, not recommended
	 */
	public static float getTextWidths(Context context, float textSize, String text){
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Paint paint = new Paint();
		paint.setTextSize(textSize * displayMetrics.scaledDensity);
		return getTextWidths(paint, text);
	}

	public static float getTextBaselineY(Paint paint, int canvasHeight){
		Paint.FontMetrics fontMetrics = paint.getFontMetrics();
		return (canvasHeight + 0f) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
	}

	public static float getTextStaticLayoutVerticalCenterOffsetY(Paint paint){
		Paint.FontMetrics fontMetrics = paint.getFontMetrics();
		return (fontMetrics.descent - fontMetrics.ascent) / 2 + fontMetrics.descent;
	}

	public static void extendViewOverlayStatusBarInLollipopOrLater(Context context, View view, Drawable backgroundDrawable, int containsStatusHeight){
		int itemWi = ViewGroup.LayoutParams.MATCH_PARENT;
		int itemHe = context.getResources().getDimensionPixelSize(R.dimen.toolbarHeight);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && containsStatusHeight > 0){
			itemHe += containsStatusHeight;
		}
		ViewGroup.MarginLayoutParams viewGroupMarginLayoutParams = new ViewGroup.MarginLayoutParams(itemWi, itemHe);
		if(backgroundDrawable != null){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				view.setBackground(backgroundDrawable);
			}else{
				view.setBackgroundDrawable(backgroundDrawable);
			}
		}
		view.setLayoutParams(viewGroupMarginLayoutParams);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && containsStatusHeight > 0){
			view.setPadding(0, containsStatusHeight, 0, 0);
		}
	}

	@SuppressLint("PrivateResource")
	public static Toolbar getToolbarExtendOverlayStatusBarInLollipopOrLater(Context context, Drawable backgroundDrawable, int containsStatusHeight){
		Toolbar toolbar = new Toolbar(context);
		toolbar.setId(R.id.toolbar);
		extendViewOverlayStatusBarInLollipopOrLater(context, toolbar, backgroundDrawable, containsStatusHeight);
		toolbar.setContentInsetsRelative(0, 0);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
		return toolbar;
	}

	public static Toolbar getToolbarExtendOverlayStatusBarInLollipopOrLater(Context context, Drawable backgroundDrawable){
		return getToolbarExtendOverlayStatusBarInLollipopOrLater(context, backgroundDrawable, 0);
	}

	public static void getViewGroupAllView(View view, List<View> list){
		list.add(view);
		if(view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			View viewChild;
			for(int i=0; i<viewGroup.getChildCount(); i++){
				viewChild = viewGroup.getChildAt(i);
				list.add(viewChild);
				if(viewChild instanceof ViewGroup){
					getViewGroupAllView(viewChild, list);
				}
			}
		}
	}

	public static List<View> getViewGroupAllView(View view){
		List<View> list = new ArrayList<View>();
		getViewGroupAllView(view, list);
		return list;
	}

	public static List<View> findViewByClass(View view, Class<?> targetClass, boolean isPrintClassName){
		List<View> list = new ArrayList<View>();
		List<View> listMatch = new ArrayList<View>();
		getViewGroupAllView(view, list);
		int size = list.size();
		for(int i=0; i<size; i++){
			if(isPrintClassName){
				System.out.println(i + " " + list.get(i).getClass().getName());
			}
			if(targetClass.isInstance(list.get(i))){
				listMatch.add(list.get(i));
			}
		}
		list.clear();
		return listMatch;
	}

	public static List<View> findViewByClass(View view, Class<?> targetClass){
		return findViewByClass(view, targetClass, false);
	}

	public static void clearViewGroupInsideDrawable(ViewGroup viewGroup, boolean isIndicatesGC){
		List<View> list = new ArrayList<View>();
		getViewGroupAllView(viewGroup, list);
		View view;
		int size = list.size();
		for(int i=size - 1; i>=0; i--){
			view = list.get(i);
			if(view != null){
				clearViewInsideDrawable(view, true, true, false);
				view.clearFocus();
			}
		}
		list.clear();
		if(isIndicatesGC){
			System.gc();
		}
	}

	public static void clearActivityInsideDrawable(Activity activity, boolean isIndicatesGC){
		clearViewGroupInsideDrawable((ViewGroup)activity.getWindow().getDecorView(), isIndicatesGC);
	}

	public static void clearViewInsideDrawable(View view, boolean foreground, boolean background, boolean isIndicatesGC){
		/*
		Known Direct Subclasses
		AnimatedVectorDrawable, BitmapDrawable, ClipDrawable, ColorDrawable, DrawableContainer, GradientDrawable, InsetDrawable, LayerDrawable
		, NinePatchDrawable, PictureDrawable, RotateDrawable, RoundedBitmapDrawable, ScaleDrawable, ShapeDrawable, VectorDrawable
		Known Indirect Subclasses
		AnimatedStateListDrawable, AnimationDrawable, LevelListDrawable, PaintDrawable, RippleDrawable, StateListDrawable, TransitionDrawable
		*/
		if(foreground){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				clearDrawable(view.getForeground());
			}else if(view instanceof FrameLayout){
				clearDrawable(((FrameLayout) view).getForeground());
			}
			if(view instanceof ImageView){
				clearDrawable(((ImageView)view).getDrawable());
				((ImageView)view).setImageDrawable(null);
			}else if(view instanceof TextView){
				Drawable[] drawables = ((TextView)view).getCompoundDrawables();
				((TextView)view).setCompoundDrawables(null, null, null, null);
				for(int i=0; i<drawables.length; i++){
					clearDrawable(drawables[i]);
				}
			}
		}

		if(background){
			clearDrawable(view.getBackground());
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				view.setBackground(null);
			}else{
				view.setBackgroundDrawable(null);
			}
		}

		if(isIndicatesGC){
			System.gc();
		}
	}

	public static void clearDrawable(Drawable drawable){
		if(drawable == null){
			return;
		}
		drawable.setCallback(null);
		if(drawable instanceof BitmapDrawable){
			BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
			recycleBitmap(bitmapDrawable.getBitmap(), false);
		}
	}

	/**
	 * @param isIndicatesGC Indicates to the VM that it would be a good time to run the garbage collector. Note that this is a hint only. There is no guarantee that the garbage collector will actually be run.
	 */
	public static void recycleBitmap(Bitmap bitmap, boolean isIndicatesGC){
		if(bitmap == null){
			return;
		}
		bitmap.recycle();
		if(isIndicatesGC){
			System.gc();
		}
	}

	public static void setToast(Context context, CharSequence text, int duration){
		Toast toast = Toast.makeText(context.getApplicationContext(), text, duration);
		toast.show();
	}

	public static void setToastShort(Context context, CharSequence text){
		setToast(context, text, Toast.LENGTH_SHORT);
	}

	public static void setToastLong(Context context, CharSequence text){
		setToast(context, text, Toast.LENGTH_LONG);
	}
}