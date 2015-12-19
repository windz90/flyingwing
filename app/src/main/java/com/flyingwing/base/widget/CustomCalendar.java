/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.widget;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import com.flyingwing.R;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class CustomCalendar {
	
	public static int calendarBackgroundResource = 0xFFBBBBBB;
	public static int toDay = 0xff6dcff6;
	public static int markDay = 0xfff5989d;
	public static int markToday = 0xffa3d49c;
	public static int touchDown = 0xff9c009c;
	public static int touchUp = 0xFFFFFFFF;
	public static int drawable_click = R.drawable.calendar_selected;
	public static int xml_textSelector = R.color.selector_textcolor_item;
	
	private LinearLayout calendarCalLinLayCal;
	private TextView calendarCalHeadCenter;
	private TextView[][] dateTxt;
	private ListView listView;
	private SimpleDateFormat sdf, sdfyMd;
	private Calendar calOriginal;
	private Date dateOriginal, dateVaria;
	private List<Map<String, String>> dataListContainsIndex;
	private List<Map<String, String>> everyDayList;
	private CustomBaseAdapter cadp;
	private Activity activity;
	private int size;
	private Resources res;
	
	public CustomCalendar(Activity activity, TextView calendarCalHeadCenter, LinearLayout calendarCalLinLayCal){
		this.calendarCalLinLayCal = calendarCalLinLayCal;
		this.calendarCalHeadCenter = calendarCalHeadCenter;
		this.activity = activity;
		res = activity.getResources();
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		size = (width < height)? width / 7 : height / 7;
	}
	
	public void setCalendar(Calendar cal){
		sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
		sdfyMd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		// 設定原始日期
		calOriginal = Calendar.getInstance();
		dateOriginal = calOriginal.getTime();
		
		// 取得變動日期
		dateVaria = cal.getTime();
		
		TextPaint txtPaint = calendarCalHeadCenter.getPaint();
		txtPaint.setFakeBoldText(true);
		calendarCalHeadCenter.setText(sdf.format(dateVaria));
		
		// 取得年度
		int thisVariaYear = cal.get(Calendar.YEAR);
		// 取得月份
		int thisVariaMonth = cal.get(Calendar.MONTH);
		// 取得今天日期
		int variaToday = cal.get(Calendar.DAY_OF_MONTH);
		
		// 設定每週第一天為星期日
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		// 取得當月最後一天日期
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		// 取當月最後一天的星期歸屬
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		int lastDayWeek = cal.get(Calendar.DAY_OF_WEEK);
		// 月曆指針移至當月最後一週的週末日期，到下個月
		cal.add(Calendar.DATE, 7 - lastDayWeek);
		// 最後一週的最後一天
		int lastWeekLastDay = cal.get(Calendar.DATE);
		
		// 返回當天
		cal.set(thisVariaYear, thisVariaMonth, variaToday);
		
		// 取當月第一天的星期歸屬
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int firstDayWeek = cal.get(Calendar.DAY_OF_WEEK);
		// 月曆指針移至當月第一週的週一日期，到上個月
		cal.add(Calendar.DATE, -(firstDayWeek - 1));
		// 第一週的第一天
		int firstWeekFirstDay = cal.get(Calendar.DATE);
		
		// 取得隔月週在內的總日數
		int sumDay = firstDayWeek - 1 + lastDay + 7 - lastDayWeek;
		// 取得隔月週在內的總週數
		int sumWeek;
		if(sumDay % 7 > 0){
			sumWeek = sumDay / 7 + 1;
		}else{
			sumWeek = sumDay / 7;
		}
//		System.out.println("firstWeekFirstDay:" + firstWeekFirstDay + " lastWeekLastDay:" + lastWeekLastDay + " sumWeek:" + sumWeek + " sumDay:" + sumDay);
		
		// 建構月曆
		calendarCalLinLayCal.setBackgroundColor(calendarBackgroundResource);
		calendarCalLinLayCal.removeAllViews();
        LinearLayout[] calendarCalLinLayDate = new LinearLayout[6];
        dateTxt = new TextView[sumWeek][7];
        for(int i=0; i<dateTxt.length; i++){
        	calendarCalLinLayDate[i] = new LinearLayout(activity);
        	calendarCalLinLayDate[i].setOrientation(LinearLayout.HORIZONTAL);
        	calendarCalLinLayDate[i].setGravity(Gravity.CENTER);
        	for(int j=0; j<dateTxt[i].length; j++){
        		dateTxt[i][j] = new TextView(activity);
        		LinearLayout.LayoutParams linLayPar = new LayoutParams(size-2, size-2);
        		linLayPar.setMargins(1, 1, 1, 1);
        		dateTxt[i][j].setLayoutParams(linLayPar);
        		dateTxt[i][j].setGravity(Gravity.CENTER);
        		
        		// 自先前月曆指針停留的第一週週一開始推加天數
        		dateVaria = cal.getTime();
				int viewDate = cal.get(Calendar.DATE);
				cal.add(Calendar.DATE, 1);
				
				dateTxt[i][j].setBackgroundColor(touchUp);
				try {
					XmlResourceParser xrp = res.getXml(xml_textSelector);
					ColorStateList colorList = ColorStateList.createFromXml(res, xrp);
					dateTxt[i][j].setTextColor(colorList);
				} catch (XmlPullParserException e) {
					dateTxt[i][j].setTextColor(res.getColor(R.color.Black));
					e.printStackTrace();
				} catch (IOException e) {
					dateTxt[i][j].setTextColor(res.getColor(R.color.Black));
					e.printStackTrace();
				}
				
				// 改變非當月的日期顏色
				if(i == 0 && firstWeekFirstDay > 1 && viewDate >= firstWeekFirstDay){
					dateTxt[i][j].setTextColor(res.getColor(R.color.WhiteGray));
				}else if(i == sumWeek-1 && lastWeekLastDay < 7 && viewDate <= lastWeekLastDay){
					dateTxt[i][j].setTextColor(res.getColor(R.color.WhiteGray));
				}
				
				dateTxt[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        		dateTxt[i][j].setText("" + viewDate);
        		dateTxt[i][j].setId(Integer.parseInt(sdfyMd.format(dateVaria)));
            	txtPaint = dateTxt[i][j].getPaint();
            	txtPaint.setFakeBoldText(true);
        		
        		// 校對標記
				dateTxt[i][j] = (TextView)setMarkDay(dateTxt[i][j]);
				dateTxt[i][j] = (TextView)setToday(dateTxt[i][j]);
				
        		dateTxt[i][j].setOnTouchListener(touchListener);
        		dateTxt[i][j].setOnClickListener(clickListener);
        		calendarCalLinLayDate[i].addView(dateTxt[i][j]);
        	}
        	calendarCalLinLayCal.addView(calendarCalLinLayDate[i]);
        }
        cal.set(thisVariaYear, thisVariaMonth, variaToday);
        dateVaria = cal.getTime();
	}
	
	public void putDataListContainsIndex(List<Map<String, String>> dataListContainsIndex){
		this.dataListContainsIndex = dataListContainsIndex;
	}
	
	public List<Map<String, String>> getEveryDaylist(){
		return everyDayList;
	}
	
	public void addListView(ListView listView){
		if(sdf.format(dateVaria).equals(sdf.format(dateOriginal))){
			everyDayList = readyDayData(sdfyMd.format(dateOriginal));
		}else{
			everyDayList = new ArrayList<Map<String,String>>();
		}
		cadp = new CustomBaseAdapter(activity, everyDayList, CustomBaseAdapter.STYLE_CALENDAR_FOR_DAY);
		listView.setAdapter(cadp);
		this.listView = listView;
	}
	
	public View setToday(View dateView){
		if(sdfyMd.format(dateOriginal).equals("" + dateView.getId())){
			if(dateView.getTag() != null && dateView.getTag().equals(2)){
				dateView.setBackgroundColor(markToday);
				dateView.setTag(3);
			}else{
				dateView.setBackgroundColor(toDay);
				dateView.setTag(1);
			}
		}
		return dateView;
	}
	
	public View setMarkDay(View dateView){
		// 自List取得日期索引並以目前日期檢查索引是否有記錄
		String yMdIndex = dataListContainsIndex.get(dataListContainsIndex.size()-1).get("" + dateView.getId());
		if(yMdIndex != null){
			dateView.setBackgroundColor(markDay);
			dateView.setTag(2);
		}
		return dateView;
	}
	
	public void checkToday(View dateView){
		if(dateView.getTag() != null){
			int viewTag = (Integer)dateView.getTag();
			if(viewTag == 1){
				dateView.setBackgroundColor(toDay);
			}else if(viewTag == 3){
				dateView.setBackgroundColor(markToday);
			}
		}
	}
	
	public void checkMarkDay(View dateView){
		if(dateView.getTag() != null){
			int viewTag = (Integer)dateView.getTag();
			if(viewTag == 2){
				dateView.setBackgroundColor(markDay);
			}
		}
	}
	
	public List<Map<String,String>> readyDayData(String dateViewId){
		List<Map<String,String>> everyDayList = new ArrayList<Map<String,String>>();
		if(dataListContainsIndex.size() > 1){
			// 自List取得日期索引並轉存索引對應的當日資料
			String yMdIndex = dataListContainsIndex.get(dataListContainsIndex.size()-1).get(dateViewId);
			if(yMdIndex != null){
				// 當日資料可能對應多個索引
				String[] yMdIndexArray = yMdIndex.split(",");
				for(int i=0; i<yMdIndexArray.length; i++){
					everyDayList.add(dataListContainsIndex.get(Integer.parseInt(yMdIndexArray[i])));
				}
			}
		}
		return everyDayList;
	}
	
	OnTouchListener touchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int eventAction = event.getAction();
			switch (eventAction) {
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundColor(touchDown);
				break;
			case MotionEvent.ACTION_UP:
				if(v.isSelected()){
					v.setBackgroundResource(drawable_click);
					checkToday(v);
				}else{
					v.setBackgroundColor(touchUp);
					checkMarkDay(v);
					checkToday(v);
				}
				break;
			}
			return false;
		}
	};
	
	OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
	        for(int i=0; i<dateTxt.length; i++){
	        	for(int j=0; j<dateTxt[i].length; j++){
	        		dateTxt[i][j].setSelected(false);
	        		dateTxt[i][j].setBackgroundColor(touchUp);
	        		checkMarkDay(dateTxt[i][j]);
	        		checkToday(dateTxt[i][j]);
	        	}
	        }
	        v.setSelected(true);
			v.setBackgroundResource(drawable_click);
			everyDayList = readyDayData("" + v.getId());
			cadp.setDataList(everyDayList);
			listView.setAdapter(cadp);
			checkToday(v);
		}
	};
}