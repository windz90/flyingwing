package com.andy.library.module.widget;

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

import com.enlightouch.homework.module.Utils;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/** 
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.2
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
		public void onItemClick(C_customRecyclerAdapter adapter, RecyclerView.ViewHolder viewHolder, int position);
	}
	
	public static interface OnItemLongClickListener {
		public void onItemLongClick(C_customRecyclerAdapter adapter, RecyclerView.ViewHolder viewHolder, int position);
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

	public void addItemInArray(String[] stringArray, int index){
		if(mDataArray != null){
			String[][] dataArray = new String[mDataArray.length + 1][2];
			if(mDataArray.length - 1 == index){
				System.arraycopy(mDataArray, 0, dataArray, 0, mDataArray.length);
				mDataArray = dataArray;
				mDataArray[mDataArray.length - 1] = stringArray;
			}else{
				for(int i=0; i<mDataArray.length; i++){
					if(i < index){
						dataArray[i] = mDataArray[i];
					}else if(i >= index + 1){
						dataArray[i] = mDataArray[i - 1];
					}else{
						dataArray[i] = stringArray;
					}
				}
			}
			notifyItemInserted(mDataArray.length - 1);
		}
	}

	public void addRangeItemInArray(String[][] stringArray, int positionStart){
		if(mDataArray != null){
			String[][] dataArray = new String[mDataArray.length + stringArray.length][2];
			if(mDataArray.length - 1 == positionStart){
				System.arraycopy(mDataArray, 0, dataArray, 0, mDataArray.length);
				mDataArray = dataArray;
				for(int i=0; i<stringArray.length; i++){
					mDataArray[positionStart + i] = stringArray[i];
				}
			}else{
				for(int i=0; i<mDataArray.length; i++){
					if(i < positionStart){
						dataArray[i] = mDataArray[i];
					}else if(i >= positionStart + stringArray.length){
						dataArray[i] = mDataArray[i - stringArray.length];
					}else{
						dataArray[i] = stringArray[i - positionStart];
					}
				}
			}
			notifyItemRangeInserted(positionStart, stringArray.length);
		}
	}

	public void removeItemFromArray(int index){
		if(mDataArray != null && mDataArray.length > index){
			String[][] dataArray = new String[mDataArray.length - 1][2];
			for(int i=0; i<mDataArray.length; i++){
				if(i < index){
					dataArray[i] = mDataArray[i];
				}else if(i > index){
					dataArray[i - 1] = mDataArray[i];
				}
			}
			mDataArray = dataArray;
			notifyItemRemoved(index);
		}
	}

	public void removeRangeItemFromArray(int positionStart, int itemCount){
		if(mDataArray != null && mDataArray.length > positionStart + itemCount){
			String[][] dataArray = new String[mDataArray.length - itemCount][2];
			for(int i=0; i<mDataArray.length; i++){
				if(i < positionStart){
					dataArray[i] = mDataArray[i];
				}else if(i >= positionStart + itemCount){
					dataArray[i - itemCount] = mDataArray[i];
				}
			}
			mDataArray = dataArray;
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void setDataList(List<Map<String, String>> list){
		mDataList = list;
	}
	
	public List<Map<String, String>> getDataList(){
		return mDataList;
	}

	public void addItemInList(Map<String, String> map, int index){
		if(mDataList != null){
			mDataList.add(index, map);
			notifyItemInserted(index);
		}
	}

	public void addRangeItemInList(List<Map<String, String>> list, int positionStart){
		if(mDataList != null) {
			int size = list.size();
			for(int i=0; i<size; i++){
				mDataList.add(positionStart + i, list.get(i));
			}
			notifyItemRangeInserted(positionStart, size);
		}
	}

	public void removeItemFromList(int index){
		if(mDataList != null && mDataList.size() > index){
			mDataList.remove(index);
			notifyItemRemoved(index);
		}
	}

	public void removeRangeItemFromList(int positionStart, int itemCount){
		if(mDataList != null && mDataList.size() > positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mDataList.remove(positionStart + itemCount);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void setJSONArray(JSONArray jsonArray){
		mJsonArray = jsonArray;
	}
	
	public JSONArray getJSONArray(){
		return mJsonArray;
	}
	
	public void addItemInJSONArray(Object object){
		if(mJsonArray != null){
			mJsonArray.put(object);
			notifyItemInserted(mJsonArray.length() - 1);
		}
	}

	public void addRangeItemInJSONArray(JSONArray jsonArray, int positionStart){
		if(mJsonArray != null){
			int length = jsonArray.length();
			for(int i=0; i<length; i++){
				try {
					mJsonArray.put(positionStart + i, jsonArray.opt(i));
				} catch (Exception e) {}
			}
			notifyItemRangeInserted(positionStart, length);
		}
	}

	/**
	 * API < Build.VERSION_CODES.KITKAT Use Reflection
	 * @param index
	 */
	public void removeItemFromJSONArray(int index){
		if(mJsonArray != null && mJsonArray.length() > index){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				mJsonArray.remove(index);
			}else{
				List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
				if(list != null){
					list.remove(index);
				}
			}
			notifyItemRemoved(index);
		}
	}

	/**
	 * API < Build.VERSION_CODES.KITKAT Use Reflection
	 * @param positionStart
	 * @param itemCount
	 */
	public void removeRangeItemFromJSONArray(int positionStart, int itemCount){
		if(mJsonArray != null && mJsonArray.length() > positionStart + itemCount){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				for(int i=0; i<itemCount; i++){
					mJsonArray.remove(positionStart + itemCount);
				}
			}else{
				List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
				if(list != null){
					for(int i=0; i<itemCount; i++){
						list.remove(positionStart + itemCount);
					}
				}
			}
			notifyItemRangeRemoved(positionStart, itemCount);
		}
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
							mOnItemClickListener.onItemClick(C_customRecyclerAdapter.this, ViewHolder.this, getAdapterPosition());
							timeClick = System.currentTimeMillis();
						}
					}
				});
			}
			if(mOnItemLongClickListener != null){
				itemView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						mOnItemLongClickListener.onItemLongClick(C_customRecyclerAdapter.this, ViewHolder.this, getAdapterPosition());
						return false;
					}
				});
			}
		}
	}
}