package com.andy.library.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andy.library.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 2.3.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_mapOverlay extends Overlay{
	
	private Activity activity;
	private MapView mapView;
	private MapView.LayoutParams mapLayPar;
	private View subView;
	private List<Overlay> listSubOverlays;
	private GeoPoint geoPointSelf;
	private List<Map<String, String>> mapDataList;
	private String[] addressInfo;
	private MyLocationOverlay myLocationOverlay;
	private C_itemizedOverlay[] itemizedOverlay;
	private Projection pj;
	private Point point;
	private Handler overlayOnTouchHandler, overlayOnTapHandler;
	private Resources res;
//	private final static int CWJ_HEAP_SIZE = 6 * 1024 * 1024;
//	private final static float TARGET_HEAP_UTILIZATION = 0.75f;
	
	public C_mapOverlay(Activity activity, MapView mapView, View subView, GeoPoint geoPointSelf, List<Map<String, String>> mapDataList){
		// 設定最小Heap記憶體，2.3以後已被移除
//		VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
		// 設定Heap記憶體處理效率，調整GC工作，2.3以後已被移除
//		VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
		this.activity = activity;
		this.mapView = mapView;
		this.subView = subView;
		this.geoPointSelf = geoPointSelf;
		this.mapDataList = mapDataList;
		// 準備子視景樣本
		mapView.removeView(subView);
		mapLayPar = (MapView.LayoutParams)mapView.getTag();
//		if(mapLayPar == null){
//			MapView.LayoutParams mapLayPar = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, 
//					MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER);
//			subView.setLayoutParams(mapLayPar);
//		}
		if(subView != null){
			mapView.addView(subView);
			subView.setVisibility(View.GONE);
		}
		res = this.activity.getResources();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// 設定投影及經緯度與畫布位置轉換
		pj = mapView.getProjection();
		point = new Point();
		if(geoPointSelf != null){
			try {
				pj.toPixels(geoPointSelf, point);
			} catch (Exception e) {
				System.out.println("Projection Exception " + e.toString());
			}
		}
		
		// 設定畫筆
//		TextPaint txtPaint = new TextPaint();
//		txtPaint.setARGB(255, 255, 0, 0);
//		txtPaint.setTextSize(36);
		// 粗體
//		txtPaint.setFakeBoldText(true);
		// 文字水平置中
//		txtPaint.setTextAlign(TextPaint.Align.CENTER);
		// 取得字體高度屬性
//		FontMetrics fontMetrics = txtPaint.getFontMetrics();
		// 設定Paint抗鋸齒
//		txtPaint.setAntiAlias(true);
		// 設定Canvas抗鋸齒、抖動平滑
//		PaintFlagsDrawFilter paintF = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
//		canvas.setDrawFilter(paintF);
		
		// 繪製所在位置
//		canvas.drawText(provider, point.x - (txtPaint.getTextSize() * provider.length() / 4), point.y - (iconB.getHeight() + txtPaint.getTextSize() / 4), txtPaint);
//		canvas.drawBitmap(iconB, point.x - iconB.getWidth() / 2, point.y - iconB.getHeight(), null);
		
		// 準備圖層
		listSubOverlays = mapView.getOverlays();
		listSubOverlays.clear();
		if(myLocationOverlay != null){
			listSubOverlays.add(myLocationOverlay);
		}
		
		itemizedOverlay = new C_itemizedOverlay[3];
		// 所在位置加入圖層及點選控制
		Drawable drawableSelf = res.getDrawable(R.drawable.overlay_mark_self);
		itemizedOverlay[0] = new C_itemizedOverlay(drawableSelf);
		
		if(geoPointSelf != null){
			try {
				itemizedOverlay[0].addToList(new OverlayItem(geoPointSelf, "您的位置", "self"), true);
				// 讓自己所在點顯示子視景及資料
				itemizedOverlay[0].setSubView(0);
			} catch (Exception e) {
				System.out.println("geoPointSelf Exception " + e.toString());
			}
		}
		
		// 其它位置加入圖層及點選控制
		Drawable drawable1 = res.getDrawable(R.drawable.overlay_mark01);
		itemizedOverlay[1] = new C_itemizedOverlay(drawable1);
		Drawable drawable2 = res.getDrawable(R.drawable.overlay_mark02);
		itemizedOverlay[2] = new C_itemizedOverlay(drawable2);
		
		if(mapDataList != null && mapDataList.size() > 0){
			for(int i=0; i<mapDataList.size(); i++){
				String latitStr = mapDataList.get(i).get("Latitude");
				String longitStr = mapDataList.get(i).get("Longitude");
				if(latitStr != null && latitStr.trim().length() > 0 && longitStr != null && longitStr.trim().length() > 0){
					String itemName = mapDataList.get(i).get("deptname").trim();
					float latit = Float.parseFloat(latitStr);
					float longit = Float.parseFloat(longitStr);
					GeoPoint geoPointOther = new GeoPoint((int)(latit * 1E6), (int)(longit * 1E6));
					
					if(mapDataList.get(i).get("deptc").equals("1")){
						itemizedOverlay[1].addToList(new OverlayItem(geoPointOther, itemName, "" + i), false);
					}else if(mapDataList.get(i).get("deptc").equals("2")){
						itemizedOverlay[2].addToList(new OverlayItem(geoPointOther, itemName, "" + i), false);
					}
				}
			}
		}
		for(int i=0; i<itemizedOverlay.length; i++){
			listSubOverlays.add(itemizedOverlay[i]);
		}
		subView.setVisibility(View.GONE);
	}
	
	public void setGeoPointSelf(GeoPoint geoPointSelf){
		this.geoPointSelf = geoPointSelf;
	}
	
	public GeoPoint getGeoPointSelf(){
		return geoPointSelf;
	}
	
	public void setDataList(List<Map<String, String>> mapDataList){
		this.mapDataList = mapDataList;
	}
	
	public List<Map<String, String>> getDataList(){
		return mapDataList;
	}
	
	public void setAddressInfo(String...addressInfo){
		this.addressInfo = addressInfo;
	}
	
	public void setMyLocationOverlay(MyLocationOverlay myLocationOverlay){
		this.myLocationOverlay = myLocationOverlay;
	}
	
	public List<Overlay> getListOverlay(){
		return listSubOverlays;
	}
	
	public void setOnTouchHandler(Handler overlayOnTouchHandler){
		this.overlayOnTouchHandler = overlayOnTouchHandler;
	}
	
	public Handler getOnTouchHandler(){
		return overlayOnTouchHandler;
	}
	
	public void setOnTapHandler(Handler overlayOnTapHandler){
		this.overlayOnTapHandler = overlayOnTapHandler;
	}
	
	public Handler getOnTapHandler(){
		return overlayOnTapHandler;
	}
	
	public class C_itemizedOverlay extends ItemizedOverlay<OverlayItem>{
		
		private Drawable marker;
		private List<OverlayItem> listOverlayItem;
		
		public C_itemizedOverlay(Drawable defaultMarker) {
			super(defaultMarker);
			marker = defaultMarker;
			listOverlayItem = new ArrayList<OverlayItem>();
			if(subView != null){
				subView.getBackground().setAlpha(255);
			}
		}
		
		@Override
		public void draw(Canvas arg0, MapView arg1, boolean arg2) {
			// boolean為true時進行第一次繪圖，繪製圖形陰影，boolean為false時進行第二次繪圖，繪製本體圖形
			if(!arg2){
				super.draw(arg0, arg1, arg2);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//			if(event.getAction() == MotionEvent.ACTION_DOWN){
//				msg.obj = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY());
//			}
			if(event.getAction() == MotionEvent.ACTION_UP){
				long milliSecond = event.getEventTime() - event.getDownTime();
				if(subView != null && milliSecond <= 250){
					subView.setVisibility(View.GONE);
				}else if(overlayOnTouchHandler != null){
					Message msg = new Message();
					msg.obj = mapView.getMapCenter();
					overlayOnTouchHandler.sendMessage(msg);
				}
			}
			return super.onTouchEvent(event, mapView);
		}

		@Override
		protected boolean onTap(int arg0) {
			setSubView(arg0);
			return super.onTap(arg0);
		}

		@Override
		protected OverlayItem createItem(int i) {
			return listOverlayItem.get(i);
		}

		@Override
		public int size() {
			return listOverlayItem.size();
		}
		
		public void addToList(OverlayItem overlayItem, boolean self){
			overlayItem.setMarker(marker);
			if(self){
				marker.setBounds(-(marker.getIntrinsicWidth() / 2), -marker.getIntrinsicHeight(), marker.getIntrinsicWidth() / 2, 0);
			}else{
				marker.setBounds((int)(-marker.getIntrinsicWidth() * 0.25f), -marker.getIntrinsicHeight(), (int)(marker.getIntrinsicWidth() - marker.getIntrinsicWidth() * 0.25f), 0);
			}
			listOverlayItem.add(overlayItem);
			populate();
		}
		
		public void setSubView(int arg0){
			if(subView != null){
				subView.setVisibility(View.GONE);
				// 判斷目前要顯示的資料位於哪個圖層
//				if(arg0 >= itemizedOverlay[0].size()){
//					arg0 = arg0 - itemizedOverlay[0].size();
//					for(int i=1; i<itemizedOverlay.length; i++){
//						if(arg0 >= itemizedOverlay[i].size()){
//							arg0 = arg0 - itemizedOverlay[i].size();
//						}else{
//							break;
//						}
//					}
//				}
				if(listOverlayItem.get(arg0) != null){
					// 設定overlayItem出現的位置
					pj.toPixels(listOverlayItem.get(arg0).getPoint(), point);
					mapLayPar.point = listOverlayItem.get(arg0).getPoint();
					mapView.updateViewLayout(subView, mapLayPar);
					subView.setVisibility(View.VISIBLE);
					
					// 設定overlayItem顯示的資料
					LinearLayout overlayRelLayLinLay = (LinearLayout)subView.findViewById(R.id.overlayRelLayLinLay);
					ImageView overlayItemImgLeft = (ImageView)subView.findViewById(R.id.overlayImgLeft);
					ImageView overlayItemImgRight = (ImageView)subView.findViewById(R.id.overlayImgRight);
					TextView overlayItemTxtId = (TextView)subView.findViewById(R.id.overlayId);
					TextView overlayItemTxtTitle = (TextView)subView.findViewById(R.id.overlayTitle);
					TextView overlayItemTxtText = (TextView)subView.findViewById(R.id.overlayText);
					
					subView.setContentDescription(listOverlayItem.get(arg0).getSnippet());
					if(listOverlayItem.get(arg0).getSnippet().equals("self")){
						overlayItemTxtTitle.setText(listOverlayItem.get(arg0).getTitle());
						overlayItemTxtText.setText(addressInfo[1]);
						overlayRelLayLinLay.setPadding(10, 0, 10, 0);
						overlayItemTxtTitle.setGravity(Gravity.CENTER);
						overlayItemImgLeft.setVisibility(View.GONE);
						overlayItemImgRight.setVisibility(View.GONE);
						subView.setClickable(false);
					}else if(mapDataList != null && mapDataList.size() > 0){
						int listIndex = Integer.parseInt(listOverlayItem.get(arg0).getSnippet());
						overlayItemTxtId.setText(mapDataList.get(listIndex).get("_id"));
						overlayItemTxtTitle.setText(listOverlayItem.get(arg0).getTitle());
						overlayItemTxtText.setText(mapDataList.get(listIndex).get("deptaddr"));
						
						Bitmap bitmap;
						if(mapDataList.get(listIndex).get("deptc").equals("1")){
							bitmap = C_imageProcessor.getRawBitmap(res, R.drawable.gjunlogo, 1);
							overlayItemImgLeft.setImageBitmap(bitmap);
						}else if(mapDataList.get(listIndex).get("deptc").equals("2")){
							bitmap = C_imageProcessor.getRawBitmap(res, R.drawable.gjunlogo2, 1);
							overlayItemImgLeft.setImageBitmap(bitmap);
						}
						bitmap = C_imageProcessor.getRawBitmap(res, R.drawable.arrowicon, 1);
						overlayItemImgRight.setImageBitmap(bitmap);
						
						overlayRelLayLinLay.setPadding(0, 0, 0, 0);
						overlayItemTxtTitle.setGravity(Gravity.LEFT);
						overlayItemImgLeft.setVisibility(View.VISIBLE);
						overlayItemImgRight.setVisibility(View.VISIBLE);
						subView.setClickable(true);
						
						if(overlayOnTapHandler != null){
							Message msg = new Message();
							msg.obj = listOverlayItem.get(arg0).getPoint();
							overlayOnTapHandler.sendMessage(msg);
						}
					}
				}
			}
		}
	}
}