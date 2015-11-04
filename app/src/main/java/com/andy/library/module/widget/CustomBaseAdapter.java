/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 3.0.4
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module.widget;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andy.library.R;
import com.andy.library.module.ImageProcessor;
import com.andy.library.module.ImageProcessor.DownLoadComplete;

public class CustomBaseAdapter extends BaseAdapter{
	
	public static final int STYLE_SIMPLE_LIST = 1;
	public static final int STYLE_ITEM_LIST = 2;
	public static final int STYLE_CALENDAR_FOR_DAY = 11;
	
	private DisplayMetrics dm;
	private Context context;
	private C_viewArray viewArray;
	private String[] dataArray;
	private String[][] data2DArray;
	private String[][][] data3DArray;
	private Map<String, String> dataMap;
	private List<Map<String, String>> dataList;
	private int[] intArray;
	private Bitmap[] bitmapArray;
	private AdapterView<?> adapterView;
	private Handler handler;
	private boolean[] checkArray;
	private int itemWidth, itemHeight, style, status;
	private LayoutInflater itemInflater;
	private Resources res;
	
	public CustomBaseAdapter(Context context, int style){
		this.context = context;
		this.style = style;
		itemInflater = LayoutInflater.from(context);
		
		res = context.getResources();
		dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
	}
	
	public CustomBaseAdapter(Context context, String[] dataArray, int style){
		this(context, style);
		setDataArray(dataArray);
	}
	
	public CustomBaseAdapter(Context context, String[][] data2DArray, int style){
		this(context, style);
		setData2DArray(data2DArray);
	}
	
	public CustomBaseAdapter(Context context, String[][][] data3DArray, int style){
		this(context, style);
		setData3DArray(data3DArray);
	}
	
	public CustomBaseAdapter(Context context, Map<String, String> map, int style){
		this(context, style);
	}
	
	public CustomBaseAdapter(Context context, List<Map<String, String>> dataList, int style){
		this(context, style);
		setDataList(dataList);
	}
	
	public CustomBaseAdapter(Context context, int[] intArray, int style){
		this(context, style);
		setIntArray(intArray);
	}
	
	public CustomBaseAdapter(Context context, Bitmap[] bitmapArray, int style){
		this(context, style);
		setBitmapArray(bitmapArray);
	}
	
	public void setDataArray(String[] dataArray){
		this.dataArray = dataArray;
	}
	
	public void setData2DArray(String[][] data2DArray){
		this.data2DArray = data2DArray;
	}
	
	public void setData3DArray(String[][][] data3DArray){
		this.data3DArray = data3DArray;
	}
	
	public void setDataMap(Map<String, String> dataMap){
		this.dataMap = dataMap;
	}
	
	public void setDataList(List<Map<String, String>> dataList){
		this.dataList = dataList;
	}
	
	public void setIntArray(int[] intArray){
		this.intArray = intArray;
	}
	
	public void setBitmapArray(Bitmap[] bitmapArray){
		int count = 0;
		for(int i=0; i<bitmapArray.length; i++){
			if(bitmapArray[i] != null){
				count++;
			}
		}
		this.bitmapArray = new Bitmap[count];
		count = 0;
		for(int i=0; i<bitmapArray.length; i++){
			if(bitmapArray[i] != null){
				this.bitmapArray[count] = bitmapArray[i];
				count++;
			}
		}
	}
	
	public String[] getDataArray(){
		return dataArray;
	}
	
	public String[][] getData2DArray(){
		return data2DArray;
	}
	
	public String[][][] getData3DArray(){
		return data3DArray;
	}
	
	public Map<String, String> getDataMap(){
		return dataMap;
	}
	
	public List<Map<String, String>> getDataList(){
		return dataList;
	}
	
	public int[] getIntArray(){
		return intArray;
	}
	
	public Bitmap[] getBitmapArray(){
		return bitmapArray;
	}
	
	public void setAdapterView(AdapterView<?> adapterView){
		this.adapterView = adapterView;
	}
	
	public AdapterView<?> getAdapterView(){
		return adapterView;
	}
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public Handler getHandler(){
		return handler;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setCheckedArray(boolean[] checkArray){
		this.checkArray = checkArray;
	}
	
	public boolean[] getCheckedArray(){
		return checkArray;
	}
	
	public void setItemWidth(int itemWidth){
		this.itemWidth = itemWidth;
	}
	
	public int getItemWidth(){
		return itemWidth;
	}
	
	public void setItemHeight(int itemHeight){
		this.itemHeight = itemHeight;
	}
	
	public int getItemHeight(){
		return itemHeight;
	}
	
	public void setStyle(int style){
		this.style = style;
	}
	
	public int getStyle(){
		return style;
	}
	
	@Override
	public int getCount() {
		if(dataArray != null){
			return dataArray.length;
		}else if(data2DArray != null){
			return data2DArray.length;
		}else if(data3DArray != null){
			return data3DArray.length;
		}else if(dataMap != null){
			return dataMap.size();
		}else if(dataList != null){
			return dataList.size();
		}else if(intArray != null){
			return intArray.length;
		}else if(bitmapArray != null){
			return bitmapArray.length;
		}else{
//			System.out.println("customAdapter Count Never Setting");
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		if(dataArray != null){
			return dataArray[position];
		}else if(data2DArray != null){
			return data2DArray[position];
		}else if(data3DArray != null){
			return data3DArray[position];
		}else if(dataMap != null){
			return dataMap.get(position);
		}else if(dataList != null){
			return dataList.get(position);
		}else if(intArray != null){
			return intArray[position];
		}else if(bitmapArray != null){
			return bitmapArray[position];
		}else{
			return null;
		}
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (style) {
		case STYLE_SIMPLE_LIST:
			convertView = setViewSimpleList(convertView, position);
			break;
		case STYLE_ITEM_LIST:
			convertView = setViewItemList(convertView, position);
			break;
		case STYLE_CALENDAR_FOR_DAY:
			convertView = setViewCalendarForDay(convertView, position);
			break;
		}
		
		if(style < 10){
			Bitmap bitmap;
			if(position % 2 == 1){
				bitmap = ImageProcessor.getRawBitmap(res, R.drawable.list_back02, 1);
			}else{
				bitmap = ImageProcessor.getRawBitmap(res, R.drawable.list_back01, 1);
			}
			BitmapDrawable bd = new BitmapDrawable(res, bitmap);
			Drawable drawable = bd;
//			viewArray.getView()[0].setBackgroundDrawable(drawable);
			viewArray.getView()[0].setBackground(drawable);
			bitmap = null;
			bd = null;
			drawable = null;
		}
		return convertView;
	}
	
	public View setViewSimpleList(View itemView, final int position){
		if(itemView == null){
			itemView = itemInflater.inflate(R.layout.custom_adapter_simple_list, null);
			
			LinearLayout cadpSimpleListLinLay = (LinearLayout)itemView.findViewById(R.id.cadpSimpleListLinLay);
			LinearLayout cadpSimpleListLinLayHoriz = (LinearLayout)itemView.findViewById(R.id.cadpSimpleListLinLayHoriz);
			LinearLayout cadpSimpleListLinLayHorizLeft = (LinearLayout)itemView.findViewById(R.id.cadpSimpleListLinLayHorizLeft);
			TextView cadpSimpleListId = (TextView)itemView.findViewById(R.id.cadpSimpleListId);
			TextView cadpSimpleListArea = (TextView)itemView.findViewById(R.id.cadpSimpleListArea);
			TextView cadpSimpleListTitle = (TextView)itemView.findViewById(R.id.cadpSimpleListTitle);
			TextView cadpSimpleListTxt = (TextView)itemView.findViewById(R.id.cadpSimpleListTxt);
			ImageView cadpSimpleListImgRight = (ImageView)itemView.findViewById(R.id.cadpSimpleListImgRight);
			
			int itemWi, itemHe;
			LinearLayout.LayoutParams linLayPar;
			
			itemWi = dm.widthPixels;
			itemHe = (int)(61.5f * 1.0f * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpSimpleListLinLay.setLayoutParams(linLayPar);
			cadpSimpleListLinLay.setPadding(0, 0, 0, 0);
			
			cadpSimpleListLinLayHoriz.setLayoutParams(linLayPar);
			
			itemWi = (int)(15 * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpSimpleListImgRight.setLayoutParams(linLayPar);
			
			itemWi = (int)(dm.widthPixels * 0.95f - itemWi);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpSimpleListLinLayHorizLeft.setLayoutParams(linLayPar);
			
			linLayPar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(itemHe * 0.5f));
			cadpSimpleListTitle.setLayoutParams(linLayPar);
			linLayPar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(itemHe * 0.5f));
			cadpSimpleListTxt.setLayoutParams(linLayPar);
			
			cadpSimpleListTitle.setEllipsize(TruncateAt.END);
			cadpSimpleListTitle.setMaxLines(2);
			cadpSimpleListTxt.setEllipsize(TruncateAt.END);
			cadpSimpleListTxt.setMaxLines(2);
			
			TextPaint txtPaint = cadpSimpleListTitle.getPaint();
			txtPaint.setFakeBoldText(true);
			
//			cadpSimpleListImgRight.setImageBitmap(ImageProcessor.getRawBitmap(res, R.drawable.arrow, 1));
			
			viewArray = new C_viewArray(cadpSimpleListLinLay, cadpSimpleListLinLayHoriz, cadpSimpleListLinLayHorizLeft
					, cadpSimpleListId, cadpSimpleListArea, cadpSimpleListTitle
					, cadpSimpleListTxt, cadpSimpleListImgRight);
			itemView.setTag(viewArray);
		}else{
			viewArray = (C_viewArray)itemView.getTag();
		}
		try{
			((TextView)viewArray.getView()[3]).setText(dataList.get(position).get("_id"));
			((TextView)viewArray.getView()[5]).setText(dataList.get(position).get("title"));
			((TextView)viewArray.getView()[6]).setText(dataList.get(position).get("summary"));
		}catch (Exception e) {
			System.out.println("setViewSimpleList Exception " + e);
		}
		return itemView;
	}
	
	public View setViewItemList(View itemView, final int position){
		if(itemView == null){
			itemView = itemInflater.inflate(R.layout.custom_adapter_item_list, null);
			
			LinearLayout cadpItemListLinLay = (LinearLayout)itemView.findViewById(R.id.cadpItemListLinLay);
			LinearLayout cadpItemListLinLayHoriz = (LinearLayout)itemView.findViewById(R.id.cadpItemListLinLayHoriz);
			LinearLayout cadpItemListLinLayHorizCenter = (LinearLayout)itemView.findViewById(R.id.cadpItemListLinLayHorizCenter);
			RelativeLayout cadpItemListRelLayHorizLeft = (RelativeLayout)itemView.findViewById(R.id.cadpItemListRelLayHorizLeft);
			ImageView cadpItemListImgLeft = (ImageView)itemView.findViewById(R.id.cadpItemListImgLeft);
			ImageView cadpItemListImgLeftMask = (ImageView)itemView.findViewById(R.id.cadpItemListImgLeftMask);
			ImageView cadpItemListImgRight = (ImageView)itemView.findViewById(R.id.cadpItemListImgRight);
			TextView cadpItemListId = (TextView)itemView.findViewById(R.id.cadpItemListId);
			TextView cadpItemListArea = (TextView)itemView.findViewById(R.id.cadpItemListArea);
			TextView cadpItemListTitle = (TextView)itemView.findViewById(R.id.cadpItemListTitle);
			TextView cadpItemListTxt = (TextView)itemView.findViewById(R.id.cadpItemListTxt);
			TextView cadpItemListTxt2 = (TextView)itemView.findViewById(R.id.cadpItemListTxt2);
			TextView cadpItemListTxtRight = (TextView)itemView.findViewById(R.id.cadpItemListTxtRight);
			
			int itemWi, itemHe, margin;
			LinearLayout.LayoutParams linLayPar;
			RelativeLayout.LayoutParams relLayPar;
			
			itemWi = dm.widthPixels;
			itemHe = (int)(61.5f * 1.5f * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpItemListLinLay.setLayoutParams(linLayPar);
			cadpItemListLinLay.setPadding(0, 0, 0, 0);
			
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpItemListLinLayHoriz.setLayoutParams(linLayPar);
			
			margin = (int)(5 * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemHe, itemHe);
			linLayPar.rightMargin = margin;
			cadpItemListRelLayHorizLeft.setLayoutParams(linLayPar);
			
			relLayPar = new RelativeLayout.LayoutParams(itemHe, itemHe);
			cadpItemListImgLeft.setLayoutParams(relLayPar);
			
			itemWi = (int)(15 * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpItemListImgRight.setLayoutParams(linLayPar);
			
			itemWi = (int)(dm.widthPixels * 0.95f - (itemHe + margin) - itemWi);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpItemListLinLayHorizCenter.setLayoutParams(linLayPar);
			
			linLayPar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(itemHe * 0.4f));
			cadpItemListTitle.setLayoutParams(linLayPar);
			linLayPar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(itemHe * 0.3f));
			cadpItemListTxt.setLayoutParams(linLayPar);
			linLayPar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(itemHe * 0.3f));
			cadpItemListTxt2.setLayoutParams(linLayPar);
			
			cadpItemListTxt2.setEllipsize(TruncateAt.END);
			cadpItemListTxt2.setMaxLines(2);
			
			TextPaint txtPaint = cadpItemListTitle.getPaint();
			txtPaint.setFakeBoldText(true);
			
//			cadpItemListImgRight.setImageBitmap(ImageProcessor.getRawBitmap(res, R.drawable.arrow, 1));
			
			viewArray = new C_viewArray(cadpItemListLinLay, cadpItemListLinLayHoriz, cadpItemListRelLayHorizLeft
					, cadpItemListLinLayHorizCenter, cadpItemListImgLeft, cadpItemListImgLeftMask
					, cadpItemListId, cadpItemListArea, cadpItemListTitle
					, cadpItemListTxt, cadpItemListTxt2, cadpItemListTxtRight
					, cadpItemListImgRight);
			itemView.setTag(viewArray);
		}else{
			viewArray = (C_viewArray)itemView.getTag();
		}
		try{
			((TextView)viewArray.getView()[6]).setText(dataList.get(position).get("_id"));
			((TextView)viewArray.getView()[8]).setText(dataList.get(position).get("title"));
			((TextView)viewArray.getView()[9]).setText(dataList.get(position).get("url"));
			((TextView)viewArray.getView()[10]).setText(dataList.get(position).get("summary"));
			
			String itemURL = "";
			if(dataList != null && dataList.size() > 0){
				itemURL = dataList.get(position).get("pic");
			}
			if(itemURL == null){
				itemURL = "";
			}
			int itemHe = (int)(61.5f * 1.5f * dm.density);
			Bitmap bitmap = getImage(viewArray.getView()[4], itemURL, itemHe
					, "ItemList/" + itemURL.replace("http://", ""));
			if(bitmap != null){
				((ImageView)viewArray.getView()[4]).setImageBitmap(bitmap);
			}
		}catch (Exception e) {
			System.out.println("setViewItemList Exception " + e);
		}
		return itemView;
	}
	
	public View setViewCalendarForDay(View itemView, int position){
		if(itemView == null){
			itemView = itemInflater.inflate(R.layout.custom_adapter_calendar_day, null);
			
			LinearLayout cadpCalendarDayLinLay = (LinearLayout)itemView.findViewById(R.id.cadpCalendarDayLinLay);
			LinearLayout cadpCalendarDayLinLay2 = (LinearLayout)itemView.findViewById(R.id.cadpCalendarDayLinLay2);
			LinearLayout cadpCalendarDayLinLay3 = (LinearLayout)itemView.findViewById(R.id.cadpCalendarDayLinLay3);
			LinearLayout cadpCalendarDayTxtLinLay = (LinearLayout)itemView.findViewById(R.id.cadpCalendarDayTxtLinLay);
			TextView cadpCalendarDayId = (TextView)itemView.findViewById(R.id.cadpCalendarDayId);
			TextView cadpCalendarDayTitle = (TextView)itemView.findViewById(R.id.cadpCalendarDayTitle);
			TextView cadpCalendarDayTxt = (TextView)itemView.findViewById(R.id.cadpCalendarDayTxt);
			TextView cadpCalendarDayTxt2 = (TextView)itemView.findViewById(R.id.cadpCalendarDayTxt2);
			
			int itemWi, itemHe;
			LinearLayout.LayoutParams linLayPar;
			
			itemWi = dm.widthPixels;
			itemHe = (int)(61.5f * 1.0f * dm.density);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpCalendarDayLinLay.setLayoutParams(linLayPar);
			cadpCalendarDayLinLay.setPadding(1, 0, 1, 1);
			
			itemWi = (int)(itemWi * 0.95f);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpCalendarDayLinLay3.setLayoutParams(linLayPar);
			
			cadpCalendarDayTitle.setWidth((int)(itemWi * 0.21f));
			cadpCalendarDayTitle.setHeight((int)(itemHe * 0.95f));
			cadpCalendarDayTxt2.setWidth((int)(itemWi * 0.15f));
			cadpCalendarDayTxt2.setHeight((int)(itemHe * 0.95f));
			
			itemWi = (int)(itemWi * 0.64f);
			itemHe = (int)(itemHe * 0.95f);
			linLayPar = new LinearLayout.LayoutParams(itemWi, itemHe);
			cadpCalendarDayTxtLinLay.setLayoutParams(linLayPar);
			
			TextPaint txtPaint = cadpCalendarDayTitle.getPaint();
			txtPaint.setFakeBoldText(true);
			txtPaint = cadpCalendarDayTxt.getPaint();
			txtPaint.setFakeBoldText(true);
			txtPaint = cadpCalendarDayTxt2.getPaint();
			txtPaint.setFakeBoldText(true);
			
			viewArray = new C_viewArray(cadpCalendarDayLinLay, cadpCalendarDayLinLay2, cadpCalendarDayLinLay3
					, cadpCalendarDayId, cadpCalendarDayTitle, cadpCalendarDayTxtLinLay
					, cadpCalendarDayTxt ,cadpCalendarDayTxt2);
			itemView.setTag(viewArray);
		}else{
			viewArray = (C_viewArray)itemView.getTag();
		}
		try{
			((TextView)viewArray.getView()[3]).setText(dataList.get(position).get("classdate"));
			((TextView)viewArray.getView()[4]).setText(dataList.get(position).get("DEPT_SNAME"));
			((TextView)viewArray.getView()[6]).setText(dataList.get(position).get("coursename"));
			((TextView)viewArray.getView()[7]).setText(dataList.get(position).get("stime") + "\n" + dataList.get(position).get("etime"));
		}catch (Exception e) {
			System.out.println("setViewCalendarForDay Exception " + e);
		}
		return itemView;
	}
	
	public Bitmap getImage(View view, String itemURL, float limitSize, String imageName){
		Bitmap bitmap = null;
		if(TextUtils.isEmpty(imageName)){
			return checkImage(bitmap, imageName);
		}
		view.setTag(itemURL);
		bitmap = ImageProcessor.getImageAsync(context, itemURL, limitSize, 100, imageName, new DownLoadComplete() {

			@Override
			public void cacheImage(String streamURL, Bitmap bitmap) {
				remoteLoadedImage(streamURL, bitmap);
			}

			@Override
			public void localLoadedImage(String streamURL, Bitmap bitmap) {
				remoteLoadedImage(streamURL, bitmap);
			}

			@Override
			public void remoteLoadedImage(String streamURL, Bitmap bitmap) {
				ImageView imageViewByTag = (ImageView) adapterView.findViewWithTag(streamURL);
				if (imageViewByTag != null) {
					if (bitmap != null) {
						imageViewByTag.setImageBitmap(bitmap);
					}
				}
			}

			@Override
			public void loadFail(String streamURL) {
			}
		});
		return checkImage(bitmap, imageName);
	}
	
	public Bitmap checkImage(Bitmap bitmap, String imageName){
		if(bitmap == null){
			bitmap = ImageProcessor.getRawBitmap(res, R.drawable.tensile_bg, 1);
		}
		return bitmap;
	}
	
	public class C_viewArray{
		
		private View[] baseView;
		
		public C_viewArray(View...allView){
			baseView = allView;
		}
		
		public View[] getView(){
			return baseView;
		}
		
		public void setView(View[] outsideView){
			baseView = outsideView;
		}
	}
}