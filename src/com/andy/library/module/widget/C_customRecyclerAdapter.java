/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module.widget;

import java.util.Collections;
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

	public void addRangeItemInArray(String[][] stringArray, int positionStart){
		if(mDataArray != null){
			String[][] dataArrayNew = new String[mDataArray.length + stringArray.length][2];
			if(mDataArray.length - stringArray.length == positionStart){
				System.arraycopy(mDataArray, 0, dataArrayNew, 0, mDataArray.length);
				for(int i=0; i<stringArray.length; i++){
					dataArrayNew[positionStart + i] = stringArray[i];
				}
			}else{
				for(int i=0; i<mDataArray.length; i++){
					if(i < positionStart){
						dataArrayNew[i] = mDataArray[i];
					}else if(i >= positionStart + stringArray.length){
						dataArrayNew[i] = mDataArray[i - stringArray.length];
					}else{
						dataArrayNew[i] = stringArray[i - positionStart];
					}
				}
			}
			mDataArray = dataArrayNew;
			notifyItemRangeInserted(positionStart, stringArray.length);
		}
	}

	public void addItemInArray(String[] stringItem, int index){
		addRangeItemInArray(new String[][]{stringItem}, index);
	}
	
	public void updateRangeItemFormArray(String[][] stringArray, int positionStart){
		if(mDataArray != null && mDataArray.length >= positionStart + stringArray.length){
			System.arraycopy(stringArray, 0, mDataArray, positionStart, stringArray.length);
			notifyItemRangeChanged(positionStart, stringArray.length);
		}
	}

	public void updateItemFormArray(String[] stringItem, int index){
		updateRangeItemFormArray(new String[][]{stringItem}, index);
	}

	public void removeRangeItemFromArray(int positionStart, int itemCount){
		if(mDataArray != null && mDataArray.length >= positionStart + itemCount){
			String[][] dataArrayNew = new String[mDataArray.length - itemCount][2];
			for(int i=0; i<mDataArray.length; i++){
				if(i < positionStart){
					dataArrayNew[i] = mDataArray[i];
				}else if(i >= positionStart + itemCount){
					dataArrayNew[i - itemCount] = mDataArray[i];
				}
			}
			mDataArray = dataArrayNew;
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeItemFromArray(int index){
		removeRangeItemFromArray(index, 1);
	}

	public void setDataList(List<Map<String, String>> list){
		mDataList = list;
	}
	
	public List<Map<String, String>> getDataList(){
		return mDataList;
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

	public void addItemInList(Map<String, String> map, int index){
		addRangeItemInList(Collections.singletonList(map), index);
	}

	public void updateRangeItemFormList(List<Map<String, String>> list, int positionStart){
		int size = list.size();
		if(mDataList != null && mDataList.size() >= positionStart + size){
			for (int i = 0; i < size; i++) {
				mDataList.set(positionStart + i, list.get(i));
			}
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void updateItemFormList(Map<String, String> map, int index){
		updateRangeItemFormList(Collections.singletonList(map), index);
	}

	public void removeRangeItemFromList(int positionStart, int itemCount){
		if(mDataList != null && mDataList.size() >= positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mDataList.remove(positionStart + i);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeItemFromList(int index){
		removeRangeItemFromList(index, 1);
	}

	public void setJSONArray(JSONArray jsonArray){
		mJsonArray = jsonArray;
	}
	
	public JSONArray getJSONArray(){
		return mJsonArray;
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

	public void addItemInJSONArray(Object object, int index){
		addRangeItemInJSONArray(new JSONArray(Collections.singletonList(object)), index);
	}

	public void updateRangeItemFormJSONArray(JSONArray jsonArray, int positionStart){
		if(mJsonArray != null && mJsonArray.length() >= positionStart + jsonArray.length()){
			List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
			List<Object> listUpdate = Utils.reflectionJSONArrayToList(jsonArray);
			if(list != null && listUpdate != null){
				int size = listUpdate.size();
				for(int i=0; i<size; i++){
					list.set(positionStart + i, listUpdate.get(i));
				}
				notifyItemRangeChanged(positionStart, size);
			}
		}
	}

	public void updateItemFormJSONArray(Object object, int index){
		updateRangeItemFormJSONArray(new JSONArray(Collections.singletonList(object)), index);
	}

	public void removeRangeItemFromJSONArray(int positionStart, int itemCount){
		if(mJsonArray != null && mJsonArray.length() >= positionStart + itemCount){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				for(int i=0; i<itemCount; i++){
					mJsonArray.remove(positionStart + itemCount);
				}
				notifyItemRangeRemoved(positionStart, itemCount);
				return;
			}
			List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
			if(list != null){
				for(int i=0; i<itemCount; i++){
					list.remove(positionStart + i);
				}
				notifyItemRangeRemoved(positionStart, itemCount);
			}
		}
	}

	public void removeItemFromJSONArray(int index){
		removeRangeItemFromJSONArray(index, 1);
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