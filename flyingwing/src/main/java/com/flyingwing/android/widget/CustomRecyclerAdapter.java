/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.16
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.widget;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flyingwing.android.util.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "Convert2Diamond", "ManualMinMaxCalculation"})
public abstract class CustomRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	protected OnItemClickListener mOnItemClickListener;
	protected OnItemLongClickListener mOnItemLongClickListener;
	protected Handler mHandler;
	protected String[][] m2DArray;
	protected List<T> mList;
	protected List<Map<String, T>> mComplexList;
	protected JSONArray mJsonArray;
	protected int mSelectorBgRes;
	protected int mItemWidth, mItemHeight;

	private long timeClick;

	public interface OnItemClickListener {
		void onItemClick(CustomRecyclerAdapter<?> adapter, RecyclerView.ViewHolder viewHolder, String clickDescription, int position);
	}

	public interface OnItemLongClickListener {
		void onItemLongClick(CustomRecyclerAdapter<?> adapter, RecyclerView.ViewHolder viewHolder, String clickDescription, int position);
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
		public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
			if(mGestureDetector != null){
				View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
				if(childView != null && mGestureDetector.onTouchEvent(motionEvent)){
					onItemClick(recyclerView, childView, "itemView", recyclerView.getChildAdapterPosition(childView));
					return true;
				}
			}
			return false;
		}

		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
	}

	public CustomRecyclerAdapter(Context context){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mSelectorBgRes = Utils.getAttributeResource(context, android.R.attr.selectableItemBackground, android.R.color.white);
		}else{
			mSelectorBgRes = android.R.color.white;
		}
	}

	public void set2DArray(String[][] arrays){
		m2DArray = arrays;
	}

	public String[][] get2DArray(){
		return m2DArray;
	}

	public void addRangeItemIn2DArray(String[][] arrays, int positionStart){
		if(arrays == null || arrays.length == 0){
			return;
		}
		if(m2DArray == null){
			m2DArray = new String[0][0];
		}
		int count = (m2DArray.length > positionStart ? m2DArray.length : positionStart) + arrays.length;
		String[][] arraysNew = new String[count][];
		if(m2DArray.length == positionStart){
			if(m2DArray.length > 0){
				System.arraycopy(m2DArray, 0, arraysNew, 0, m2DArray.length);
			}
			System.arraycopy(arrays, 0, arraysNew, positionStart, arrays.length);
		}else{
			for(int i=0; i<arraysNew.length; i++){
				if(i < positionStart){
					if(i < m2DArray.length){
						arraysNew[i] = m2DArray[i];
					}
				}else if(i >= positionStart + arrays.length){
					arraysNew[i] = m2DArray[i - arrays.length];
				}else{
					arraysNew[i] = arrays[i - positionStart];
				}
			}
		}
		m2DArray = arraysNew;
		notifyItemRangeInserted(positionStart, arrays.length);
		notifyItemRangeChanged(positionStart, arrays.length);
	}

	public void addItemIn2DArray(String[] arrayItem, int index){
		addRangeItemIn2DArray(new String[][]{arrayItem}, index);
	}

	public void updateRangeItemForm2DArray(String[][] arrays, int positionStart){
		if(arrays == null || arrays.length == 0){
			return;
		}
		if(m2DArray == null){
			m2DArray = new String[0][0];
		}
		if(m2DArray.length >= positionStart + arrays.length){
			System.arraycopy(arrays, 0, m2DArray, positionStart, arrays.length);
		}else{
			String[][] arraysNew = new String[positionStart + arrays.length][];
			for(int i=0; i<arraysNew.length; i++){
				if(i < positionStart){
					if(i < m2DArray.length){
						arraysNew[i] = m2DArray[i];
					}
				}else{
					arraysNew[i] = arrays[i - positionStart];
				}
			}
			m2DArray = arraysNew;
		}
		notifyItemRangeChanged(positionStart, arrays.length);
	}

	public void updateItemForm2DArray(String[] arrayItem, int index){
		if(m2DArray == null){
			m2DArray = new String[0][0];
		}
		if(m2DArray.length > index){
			m2DArray[index] = arrayItem;
		}else{
			String[][] arraysNew = new String[index + 1][];
			for(int i=0; i<arraysNew.length; i++){
				if(i < index){
					if(i < m2DArray.length){
						arraysNew[i] = m2DArray[i];
					}
				}else{
					arraysNew[i] = arrayItem;
				}
			}
			m2DArray = arraysNew;
		}
		notifyItemRangeChanged(index, 1);
	}

	public void removeRangeItemFrom2DArray(int positionStart, int itemCount){
		if(m2DArray != null && m2DArray.length >= positionStart + itemCount){
			String[][] arraysNew = new String[m2DArray.length - itemCount][];
			for(int i=0; i< m2DArray.length; i++){
				if(i < positionStart){
					arraysNew[i] = m2DArray[i];
				}else if(i >= positionStart + itemCount){
					arraysNew[i - itemCount] = m2DArray[i];
				}
			}
			m2DArray = arraysNew;
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	public void removeItemFrom2DArray(int index){
		removeRangeItemFrom2DArray(index, 1);
	}

	public void setList(List<T> list){
		mList = list;
	}

	public List<T> getList(){
		return mList;
	}

	public void addRangeItemInList(List<T> list, int positionStart){
		if(list == null || list.size() == 0){
			return;
		}
		if(mList == null){
			mList = new ArrayList<T>();
		}
		if(positionStart > mList.size()){
			for(int i=mList.size(); i<positionStart; i++){
				mList.add(null);
			}
		}
		int size = list.size();
		for(int i=0; i<size; i++){
			mList.add(positionStart + i, list.get(i));
		}
		notifyItemRangeInserted(positionStart, size);
		notifyItemRangeChanged(positionStart, size);
	}

	public void addItemInList(T t, int index){
		if(mList == null){
			mList = new ArrayList<T>();
		}
		if(index > mList.size()){
			for(int i=mList.size(); i<index; i++){
				mList.add(null);
			}
		}
		mList.add(index, t);
		notifyItemRangeInserted(index, 1);
		notifyItemRangeChanged(index, 1);
	}

	public void updateRangeItemFormList(List<T> list, int positionStart){
		if(list == null || list.size() == 0){
			return;
		}
		if(mList == null){
			mList = new ArrayList<T>();
		}
		if(positionStart > mList.size()){
			for(int i=mList.size(); i<positionStart; i++){
				mList.add(null);
			}
		}
		int size = list.size();
		for(int i=0; i<size; i++){
			if(mList.size() > positionStart + i){
				mList.set(positionStart + i, list.get(i));
			}else{
				mList.add(list.get(i));
			}
		}
		notifyItemRangeChanged(positionStart, size);
	}

	public void updateItemFormComplexList(T t, int index){
		if(mList == null){
			mList = new ArrayList<T>();
		}
		if(index > mList.size()){
			for(int i=mList.size(); i<index; i++){
				mList.add(null);
			}
		}
		if(mList.size() > index){
			mList.set(index, t);
		}else{
			mList.add(t);
		}
		notifyItemRangeChanged(index, 1);
	}

	public void removeRangeItemFromList(int positionStart, int itemCount){
		if(mList != null && mList.size() >= positionStart + itemCount){
			int size = itemCount;
			for(int i=0; i<size; i++){
				mList.remove(positionStart + i);
				i--;
				size--;
			}
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	public void removeItemFromList(int index){
		removeRangeItemFromList(index, 1);
	}


	public void setComplexList(List<Map<String, T>> list){
		mComplexList = list;
	}

	public List<Map<String, T>> getComplexList(){
		return mComplexList;
	}

	public void addRangeItemInComplexList(List<Map<String, T>> list, int positionStart){
		if(list == null || list.size() == 0){
			return;
		}
		if(mComplexList == null){
			mComplexList = new ArrayList<Map<String, T>>(list.size());
		}
		if(positionStart > mComplexList.size()){
			for(int i=mComplexList.size(); i<positionStart; i++){
				mComplexList.add(null);
			}
		}
		int size = list.size();
		for(int i=0; i<size; i++){
			mComplexList.add(positionStart + i, list.get(i));
		}
		notifyItemRangeInserted(positionStart, size);
		notifyItemRangeChanged(positionStart, size);
	}

	public void addItemInComplexList(Map<String, T> map, int index){
		if(mComplexList == null){
			mComplexList = new ArrayList<Map<String, T>>();
		}
		if(index > mComplexList.size()){
			for(int i=mComplexList.size(); i<index; i++){
				mComplexList.add(null);
			}
		}
		mComplexList.add(index, map);
		notifyItemRangeInserted(index, 1);
		notifyItemRangeChanged(index, 1);
	}

	public void updateRangeItemFormComplexList(List<Map<String, T>> list, int positionStart){
		if(list == null || list.size() == 0){
			return;
		}
		if(mComplexList == null){
			mComplexList = new ArrayList<Map<String, T>>(list.size());
		}
		if(positionStart > mComplexList.size()){
			for(int i=mComplexList.size(); i<positionStart; i++){
				mComplexList.add(null);
			}
		}
		int size = list.size();
		for(int i=0; i<size; i++){
			if(mComplexList.size() > positionStart + i){
				mComplexList.set(positionStart + i, list.get(i));
			}else{
				mComplexList.add(list.get(i));
			}
		}
		notifyItemRangeChanged(positionStart, size);
	}

	public void updateItemFormComplexList(Map<String, T> map, int index){
		if(mComplexList == null){
			mComplexList = new ArrayList<Map<String, T>>();
		}
		if(index > mComplexList.size()){
			for(int i=mComplexList.size(); i<index; i++){
				mComplexList.add(null);
			}
		}
		if(mComplexList.size() > index){
			mComplexList.set(index, map);
		}else{
			mComplexList.add(map);
		}
		notifyItemRangeChanged(index, 1);
	}

	public void removeRangeItemFromComplexList(int positionStart, int itemCount){
		if(mComplexList != null && mComplexList.size() >= positionStart + itemCount){
			int size = itemCount;
			for(int i=0; i<size; i++){
				mComplexList.remove(positionStart + i);
				i--;
				size--;
			}
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	public void removeItemFromComplexList(int index){
		removeRangeItemFromComplexList(index, 1);
	}

	public void setJSONArray(JSONArray jsonArray){
		mJsonArray = jsonArray;
	}

	public JSONArray getJSONArray(){
		return mJsonArray;
	}

	public void addRangeItemInJSONArray(JSONArray jsonArray, int positionStart){
		if(jsonArray == null || jsonArray.length() == 0){
			return;
		}
		if(mJsonArray == null){
			mJsonArray = new JSONArray();
		}
		if(positionStart > mJsonArray.length()){
			for(int i=mJsonArray.length(); i<positionStart; i++){
				mJsonArray.put(null);
			}
		}
		List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
		int length = jsonArray.length();
		for(int i=0; i<length; i++){
			if(mJsonArray.length() > positionStart + i){
				if(list != null){
					list.add(positionStart + i, jsonArray.opt(i));
				}
			}else{
				mJsonArray.put(jsonArray.opt(i));
			}
		}
		notifyItemRangeInserted(positionStart, length);
		notifyItemRangeChanged(positionStart, length);
	}

	public void addItemInJSONArray(Object object, int index){
		if(mJsonArray == null){
			mJsonArray = new JSONArray();
		}
		if(index > mJsonArray.length()){
			for(int i=mJsonArray.length(); i<index; i++){
				mJsonArray.put(null);
			}
		}
		List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
		if(mJsonArray.length() > index){
			if(list != null){
				list.add(index, object);
			}
		}else{
			mJsonArray.put(object);
		}
		notifyItemRangeInserted(index, 1);
		notifyItemRangeChanged(index, 1);
	}

	public void updateRangeItemFormJSONArray(JSONArray jsonArray, int positionStart){
		if(jsonArray == null || jsonArray.length() == 0){
			return;
		}
		if(mJsonArray == null){
			mJsonArray = new JSONArray();
		}
		if(positionStart > mJsonArray.length()){
			for(int i=mJsonArray.length(); i<positionStart; i++){
				mJsonArray.put(null);
			}
		}
		int length = jsonArray.length();
		for(int i=0; i<length; i++){
			if(mJsonArray.length() > positionStart + i){
				try {
					mJsonArray.put(positionStart + i, jsonArray.opt(i));
				} catch (Exception ignored) {}
			}else{
				mJsonArray.put(jsonArray.opt(i));
			}
		}
		notifyItemRangeChanged(positionStart, length);
	}

	public void updateItemFormJSONArray(Object object, int index){
		if(mJsonArray == null){
			mJsonArray = new JSONArray();
		}
		if(index > mJsonArray.length()){
			for(int i=mJsonArray.length(); i<index; i++){
				mJsonArray.put(null);
			}
		}
		if(mJsonArray.length() > index){
			try {
				mJsonArray.put(index, object);
			} catch (Exception ignored) {}
		}else{
			mJsonArray.put(object);
		}
		notifyItemRangeChanged(index, 1);
	}

	public void removeRangeItemFromJSONArray(int positionStart, int itemCount){
		if(mJsonArray != null && mJsonArray.length() >= positionStart + itemCount){
			int size = itemCount;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				for(int i=0; i<size; i++){
					mJsonArray.remove(positionStart + i);
					i--;
					size--;
				}
				notifyItemRangeRemoved(positionStart, itemCount);
				notifyItemRangeChanged(positionStart, itemCount);
				return;
			}
			List<Object> list = Utils.reflectionJSONArrayToList(mJsonArray);
			if(list != null){
				for(int i=0; i<size; i++){
					list.remove(positionStart + i);
					i--;
					size--;
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

	@NonNull
	@Override
	public abstract ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

	@Override
	public abstract void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position);

	public class ViewHolder extends RecyclerView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
			if(mOnItemClickListener != null){
				itemView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 提升對Android 4.0的相容性，避免RecyclerView.ViewHolder的itemView.OnClick重複執行
						if(System.currentTimeMillis() - timeClick > 300L){
							mOnItemClickListener.onItemClick(CustomRecyclerAdapter.this, ViewHolder.this, "itemView", getBindingAdapterPosition());
							timeClick = System.currentTimeMillis();
						}
					}
				});
			}
			if(mOnItemLongClickListener != null){
				itemView.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						mOnItemLongClickListener.onItemLongClick(CustomRecyclerAdapter.this, ViewHolder.this, "itemView", getBindingAdapterPosition());
						return false;
					}
				});
			}
		}
	}
}