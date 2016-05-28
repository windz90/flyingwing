/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.9
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.flyingwing.base.util.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "Convert2Diamond"})
public abstract class CustomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	protected OnItemClickListener mOnItemClickListener;
	protected OnItemLongClickListener mOnItemLongClickListener;
	protected Handler mHandler;
	protected String[][] m2DArray;
	protected List<? super Object> mList;
	protected List<Map<String, ? super Object>> mComplexList;
	protected JSONArray mJsonArray;
	protected int mSelectorBgRes;
	protected int mItemWidth, mItemHeight;
	
	private long timeClick;

	public interface OnItemClickListener {
		void onItemClick(CustomRecyclerAdapter adapter, RecyclerView.ViewHolder viewHolder, String clickDescription, int position);
	}

	public interface OnItemLongClickListener {
		void onItemLongClick(CustomRecyclerAdapter adapter, RecyclerView.ViewHolder viewHolder, String clickDescription, int position);
	}
	
	public static abstract class OnItemTouchListener implements RecyclerView.OnItemTouchListener {
		
		GestureDetector mGestureDetector;
		
		public abstract void onItemClick(RecyclerView recyclerView, View itemView, String clickDescription, int position);
		public void onItemLongClick(RecyclerView recyclerView, View itemView, String clickDescription, int position){}
		
		public OnItemTouchListener(Context context, final RecyclerView recyclerView){
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
					if(childView != null){
						onItemLongClick(recyclerView, childView, "itemView", recyclerView.getChildAdapterPosition(childView));
					}
				}
			});
		}
		
		@Override
		public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
			if(mGestureDetector != null){
				View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
				if(childView != null && mGestureDetector.onTouchEvent(e)){
					onItemClick(recyclerView, childView, "itemView", recyclerView.getChildAdapterPosition(childView));
					return true;
				}
			}
			return false;
		}

		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CustomRecyclerAdapter(Context context){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mSelectorBgRes = Utils.getAttributeResource(context, android.R.attr.selectableItemBackground, android.R.color.white);
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
		if(stringArray == null || stringArray.length == 0){
			return;
		}
		if((m2DArray == null ? 0 : m2DArray.length) < positionStart){
			return;
		}
		if(m2DArray == null){
			m2DArray = new String[0][0];
		}
		String[][] dataArrayNew = new String[m2DArray.length + stringArray.length][];
		if(m2DArray.length == positionStart){
			if(m2DArray.length > 0){
				System.arraycopy(m2DArray, 0, dataArrayNew, 0, m2DArray.length);
			}
			System.arraycopy(stringArray, 0, dataArrayNew, positionStart, stringArray.length);
		}else{
			for(int i=0; i<dataArrayNew.length; i++){
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
		notifyItemRangeChanged(positionStart, stringArray.length);
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
		if(m2DArray != null && m2DArray.length > index){
			m2DArray[index] = stringItem;
			notifyItemRangeChanged(index, 1);
		}
	}

	public void removeRangeItemFrom2DArray(int positionStart, int itemCount){
		if(m2DArray != null && m2DArray.length >= positionStart + itemCount){
			String[][] dataArrayNew = new String[m2DArray.length - itemCount][];
			for(int i=0; i< m2DArray.length; i++){
				if(i < positionStart){
					dataArrayNew[i] = m2DArray[i];
				}else if(i >= positionStart + itemCount){
					dataArrayNew[i - itemCount] = m2DArray[i];
				}
			}
			m2DArray = dataArrayNew;
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	public void removeItemFrom2DArray(int index){
		removeRangeItemFrom2DArray(index, 1);
	}

	public void setComplexList(List<Map<String, ? super Object>> list){
		mComplexList = list;
	}
	
	public List<Map<String, ? super Object>> getComplexList(){
		return mComplexList;
	}

	public void addRangeItemInComplexList(List<Map<String, ? super Object>> list, int positionStart){
		if(mComplexList == null){
			if(positionStart != 0){
				return;
			}
			mComplexList = new ArrayList<Map<String, ? super Object>>();
		}
		if(mComplexList.size() >= positionStart){
			int size = list.size();
			for(int i=0; i<size; i++){
				mComplexList.add(positionStart + i, list.get(i));
			}
			notifyItemRangeInserted(positionStart, size);
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void addItemInComplexList(Map<String, ? super Object> map, int index){
		if(mComplexList == null){
			if(index != 0){
				return;
			}
			mComplexList = new ArrayList<Map<String, ? super Object>>();
		}
		if(mComplexList.size() >= index){
			mComplexList.add(index, map);
			notifyItemRangeInserted(index, 1);
			notifyItemRangeChanged(index, 1);
		}
	}

	public void updateRangeItemFormComplexList(List<Map<String, ? super Object>> list, int positionStart){
		int size = list.size();
		if(mComplexList != null && mComplexList.size() >= positionStart + size){
			for (int i = 0; i < size; i++) {
				mComplexList.set(positionStart + i, list.get(i));
			}
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void updateItemFormComplexList(Map<String, ? super Object> map, int index){
		if(mComplexList != null && mComplexList.size() > index){
			mComplexList.set(index, map);
			notifyItemRangeChanged(index, 1);
		}
	}

	public void removeRangeItemFromComplexList(int positionStart, int itemCount){
		if(mComplexList != null && mComplexList.size() >= positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mComplexList.remove(positionStart + i);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	public void removeItemFromComplexList(int index){
		removeRangeItemFromComplexList(index, 1);
	}

	public void setList(List<? super Object> list){
		mList = list;
	}

	public List<? super Object> getList(){
		return mList;
	}

	public void addRangeItemInList(List<?> list, int positionStart){
		if(mList == null){
			if(positionStart != 0){
				return;
			}
			mList = new ArrayList<Object>();
		}
		if(mList.size() >= positionStart){
			int size = list.size();
			for(int i=0; i<size; i++){
				mList.add(positionStart + i, list.get(i));
			}
			notifyItemRangeInserted(positionStart, size);
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void addItemInList(Object object, int index){
		if(mList == null){
			if(index != 0){
				return;
			}
			mList = new ArrayList<Object>();
		}
		if(mList.size() >= index){
			mList.add(index, object);
			notifyItemRangeInserted(index, 1);
			notifyItemRangeChanged(index, 1);
		}
	}

	public void updateRangeItemFormList(List<?> list, int positionStart){
		int size = list.size();
		if(mList != null && mList.size() >= positionStart + size){
			for (int i = 0; i < size; i++) {
				mList.set(positionStart + i, list.get(i));
			}
			notifyItemRangeChanged(positionStart, size);
		}
	}

	public void updateItemFormComplexList(Object object, int index){
		if(mList != null && mList.size() > index){
			mList.set(index, object);
			notifyItemRangeChanged(index, 1);
		}
	}

	public void removeRangeItemFromList(int positionStart, int itemCount){
		if(mList != null && mList.size() >= positionStart + itemCount){
			for(int i=0; i<itemCount; i++){
				mList.remove(positionStart + i);
			}
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
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
		if(mJsonArray == null){
			if(positionStart != 0){
				return;
			}
			mJsonArray = new JSONArray();
		}
		if(mJsonArray.length() >= positionStart){
			int length = jsonArray.length();
			for(int i=0; i<length; i++){
				try {
					mJsonArray.put(positionStart + i, jsonArray.opt(i));
				} catch (Exception ignored) {}
			}
			notifyItemRangeInserted(positionStart, length);
			notifyItemRangeChanged(positionStart, length);
		}
	}

	public void addItemInJSONArray(Object object, int index){
		if(mJsonArray == null){
			if(index != 0){
				return;
			}
			mJsonArray = new JSONArray();
		}
		if(mJsonArray.length() >= index){
			try {
				mJsonArray.put(index, object);
			} catch (Exception ignored) {}
			notifyItemRangeInserted(index, 1);
			notifyItemRangeChanged(index, 1);
		}
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
		if(mJsonArray != null && mJsonArray.length() > index){
			List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
			if(list != null){
				list.set(index, object);
				notifyItemRangeChanged(index, 1);
			}
		}
	}

	public void removeRangeItemFromJSONArray(int positionStart, int itemCount){
		if(mJsonArray != null && mJsonArray.length() >= positionStart + itemCount){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				for(int i=0; i<itemCount; i++){
					mJsonArray.remove(positionStart + i);
				}
				notifyItemRangeRemoved(positionStart, itemCount);
				notifyItemRangeChanged(positionStart, itemCount);
				return;
			}
			List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
			if(list != null){
				for(int i=0; i<itemCount; i++){
					list.remove(positionStart + i);
				}
				notifyItemRangeRemoved(positionStart, itemCount);
				notifyItemRangeChanged(positionStart, itemCount);
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

	public void setHandler(Handler handler){
		this.mHandler = handler;
	}

	public Handler getHandler(){
		return mHandler;
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
			if(mOnItemClickListener != null){
				itemView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// 提升對Android 4.0的相容性，避免RecyclerView.ViewHolder的itemView.OnClick重複執行
						if(System.currentTimeMillis() - timeClick > 300L){
							mOnItemClickListener.onItemClick(CustomRecyclerAdapter.this, ViewHolder.this, "itemView", getAdapterPosition());
							timeClick = System.currentTimeMillis();
						}
					}
				});
			}
			if(mOnItemLongClickListener != null){
				itemView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						mOnItemLongClickListener.onItemLongClick(CustomRecyclerAdapter.this, ViewHolder.this, "itemView", getAdapterPosition());
						return false;
					}
				});
			}
		}
	}
}