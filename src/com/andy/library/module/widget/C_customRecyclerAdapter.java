/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.4
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
	protected String[][] m2DArray;
	protected List<Object> mList;
	protected List<Map<String, Object>> mComplexList;
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
	
	public void set2DArray(String[][] array){
		m2DArray = array;
	}
	
	public String[][] get2DArray(){
		return m2DArray;
	}

	public void addRangeItemIn2DArray(String[][] stringArray, int positionStart){
		if(m2DArray != null){
			String[][] dataArrayNew = new String[m2DArray.length + stringArray.length][2];
			if(m2DArray.length - stringArray.length == positionStart){
				System.arraycopy(m2DArray, 0, dataArrayNew, 0, m2DArray.length);
				for(int i=0; i<stringArray.length; i++){
					dataArrayNew[positionStart + i] = stringArray[i];
				}
			}else{
				for(int i=0; i< m2DArray.length; i++){
					if(i < positionStart){
						dataArrayNew[i] = m2DArray[i];
					}else if(i >= positionStart + stringArray.length){
						dataArrayNew[i] = m2DArray[i - stringArray.length];
					}else{
						dataArrayNew[i] = stringArray[i - positionStart];
					}
				}
			}
			m2DArray = dataArrayNew;
			notifyItemRangeInserted(positionStart, stringArray.length);
		}
	}

	public void addItemIn2DArray(String[] stringItem, int index){
		addRangeItemIn2DArray(new String[][]{stringItem}, index);
	}
	
	public void updateRangeItemForm2DArray(String[][] stringArray, int positionStart){
		if(m2DArray != null && m2DArray.length >= positionStart + stringArray.length){
			System.arraycopy(stringArray, 0, m2DArray, positionStart, stringArray.length);
			notifyItemRangeChanged(positionStart, stringArray.length);
		}
	}

	public void updateItemForm2DArray(String[] stringItem, int index){
		updateRangeItemForm2DArray(new String[][]{stringItem}, index);
	}

	public void removeRangeItemFrom2DArray(int positionStart, int itemCount){
		if(m2DArray != null && m2DArray.length >= positionStart + itemCount){
			String[][] dataArrayNew = new String[m2DArray.length - itemCount][2];
			for(int i=0; i< m2DArray.length; i++){
				if(i < positionStart){
					dataArrayNew[i] = m2DArray[i];
				}else if(i >= positionStart + itemCount){
					dataArrayNew[i - itemCount] = m2DArray[i];
				}
			}
			m2DArray = dataArrayNew;
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeItemFrom2DArray(int index){
		removeRangeItemFrom2DArray(index, 1);
	}

	public void setComplexList(List<Map<String, Object>> list){
		mComplexList = list;
	}
	
	public List<Map<String, Object>> getComplexList(){
		return mComplexList;
	}

	public void addRangeItemInComplexList(List<Map<String, Object>> list, int positionStart){
		if(mComplexList != null) {
			int size = list.size();
			for(int i=0; i<size; i++){
				mComplexList.add(positionStart + i, list.get(i));
			}
			notifyItemRangeInserted(positionStart, size);
		}
	}

	public void addItemInComplexList(Map<String, Object> map, int index){
		addRangeItemInComplexList(Collections.singletonList(map), index);
	}

	public void updateRangeItemFormComplexList(List<Map<String, Object>> list, int positionStart){
		int size = list.size();
		if(mComplexList != null && mComplexList.size() >= positionStart + size){
			for (int i = 0; i < size; i++) {
				mComplexList.set(positionStart + i, list.get(i));
			}
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void updateItemFormComplexList(Map<String, Object> map, int index){
		updateRangeItemFormComplexList(Collections.singletonList(map), index);
	}

	public void removeRangeItemFromComplexList(int positionStart, int itemCount){
		if(mComplexList != null && mComplexList.size() >= positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mComplexList.remove(positionStart + i);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeItemFromComplexList(int index){
		removeRangeItemFromComplexList(index, 1);
	}

	public void setList(List<Object> list){
		mList = list;
	}

	public List<Object> getList(){
		return mList;
	}

	public void addRangeItemInList(List<Object> list, int positionStart){
		if(mList != null) {
			int size = list.size();
			for(int i=0; i<size; i++){
				mList.add(positionStart + i, list.get(i));
			}
			notifyItemRangeInserted(positionStart, size);
		}
	}

	public void addItemInList(Object object, int index){
		addRangeItemInList(Collections.singletonList(object), index);
	}

	public void updateRangeItemFormList(List<Object> list, int positionStart){
		int size = list.size();
		if(mList != null && mList.size() >= positionStart + size){
			for (int i = 0; i < size; i++) {
				mList.set(positionStart + i, list.get(i));
			}
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void updateItemFormComplexList(Object object, int index){
		updateRangeItemFormList(Collections.singletonList(object), index);
	}

	public void removeRangeItemFromList(int positionStart, int itemCount){
		if(mList != null && mList.size() >= positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mList.remove(positionStart + i);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeItemFromList(int index){
		removeRangeItemFromComplexList(index, 1);
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