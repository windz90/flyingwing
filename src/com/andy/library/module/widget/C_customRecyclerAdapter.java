package com.andy.library.module.widget;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.andy.library.module.Utils;

/** 
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public abstract class C_customRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	protected OnItemClickListener mOnItemClickListener;
	protected OnItemLongClickListener mOnItemLongClickListener;
	protected String[][] mDataArray;
	protected List<Map<String, String>> mDataList;
	protected JSONArray mJsonArray;
	protected int mSelectorBgRes;
	protected int mItemWidth, mItemHeight;
	
	private long timeClick;
	
	public static interface OnItemClickListener {
		public void onItemClick(C_customRecyclerAdapter adp, View v, int position);
	}
	
	public static interface OnItemLongClickListener {
		public void onItemLongClick(C_customRecyclerAdapter adp, View v, int position);
	}
	
	public static abstract class OnItemClickSimpleListener implements RecyclerView.OnItemTouchListener {
		
		GestureDetector mGestureDetector;
		
		public abstract void onItemClick(RecyclerView recyclerView, View childView, int position);
		
		public OnItemClickSimpleListener(Context context){
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}
			});
		}
		
		@Override
		public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
			if(mGestureDetector != null){
				View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
				if(childView != null && mGestureDetector.onTouchEvent(e)){
					onItemClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
					return true;
				}
			}
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView recyclerView, MotionEvent Event) {}
	}
	
	public static abstract class OnItemClickMultipleListener implements RecyclerView.OnItemTouchListener {
		
		GestureDetector mGestureDetector;
		
		public abstract void onItemClick(RecyclerView recyclerView, View childView, int position);
		public abstract void onItemLongClick(RecyclerView recyclerView, View childView, int position);
		
		public OnItemClickMultipleListener(Context context, final RecyclerView recyclerView){
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
					if(childView != null){
						onItemLongClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
					}
				}
			});
		}
		
		@Override
		public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
			if(mGestureDetector != null){
				View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
				if(childView != null && mGestureDetector.onTouchEvent(e)){
					onItemClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
					return true;
				}
			}
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView recyclerView, MotionEvent Event) {}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public C_customRecyclerAdapter(Context context){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mSelectorBgRes = Utils.getAttributeResorce(context, android.R.attr.selectableItemBackground, android.R.color.white);
		}else{
			mSelectorBgRes = android.R.color.white;
		}
	}
	
	public void setDataArray(String[][] array){
		mDataArray = array;
	}
	
	public String[][] getDataArray(){
		return mDataArray;
	}
	
	public void setDataList(List<Map<String, String>> list){
		mDataList = list;
	}
	
	public List<Map<String, String>> getDataList(){
		return mDataList;
	}
	
	public void setJSONArray(JSONArray jsonArray){
		mJsonArray = jsonArray;
	}
	
	public JSONArray getJSONArray(){
		return mJsonArray;
	}
	
	public void setItemWidth(int itemWidth){
		mItemWidth = itemWidth;
	}
	
	public int getItemWidth(){
		return mItemWidth;
	}
	
	public void setItemHeight(int itemHeight){
		mItemHeight = itemHeight;
	}
	
	public int getItemHeight(){
		return mItemHeight;
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		mOnItemClickListener = onItemClickListener;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
		mOnItemLongClickListener = onItemLongClickListener;
	}
	
	@Override
	public abstract int getItemCount();
	
	@Override
	public abstract ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);
	
	@Override
	public abstract void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position);
	
	public class ViewHolder extends RecyclerView.ViewHolder {
		
		public ViewHolder(View itemView) {
			super(itemView);
			if(itemView == null){
				return;
			}
			if(mOnItemClickListener != null){
				itemView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// 提升對Android 4.0的相容性，避免RecyclerView.ViewHolder的itemView.OnClick重複執行
						if(System.currentTimeMillis() - timeClick > 300L){
							mOnItemClickListener.onItemClick(C_customRecyclerAdapter.this, v, getAdapterPosition());
							timeClick = System.currentTimeMillis();
						}
					}
				});
			}
			if(mOnItemLongClickListener != null){
				itemView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						mOnItemLongClickListener.onItemLongClick(C_customRecyclerAdapter.this, v, getAdapterPosition());
						return false;
					}
				});
			}
		}
	}
}