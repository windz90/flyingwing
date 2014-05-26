package com.andy.library.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Copyright 2012 Andy Lin. All rights reserved.
 * @version 3.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */
public class C_formatData {
	
	public static final String JSON_ARRAY_MAPKEY = "mapKey_array";
	
	private static final String JSON_OBJECT_START = "{";
	private static final String JSON_OBJECT_END = "}";
	private static final String JSON_ARRAY_START = "[";
	private static final String JSON_ARRAY_END = "]";
	
//	private static int style;
	private static String delimiter, delimiterKV, delimiterEnd, delimiterRow, delimiterRowStart, delimiterRowEnd;
	
	public static List<Map<String, String>> getList(Object objectData, int style, boolean isAddNullString){
//		C_formatData.style = style;
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		Map<String, String> hashMap;
		if(objectData == null || objectData.equals("") && isAddNullString){
			System.out.println("objectData is null");
			hashMap = new HashMap<String, String>();
			hashMap.put("Results0x0", "objectData is null");
			dataList.add(hashMap);
			return dataList;
		}
		if(objectData.toString().contains("Connection Fail")){
			hashMap = new HashMap<String, String>();
			hashMap.put("Results0x0", objectData.toString());
			dataList.add(hashMap);
			return dataList;
		}
		try{
			addData(objectData, style, dataList, isAddNullString);
		}catch (Exception e) {
			System.out.println("ReadyData Exception " + e);
			System.out.println("Results0x0 " + objectData.toString());
			dataList = new ArrayList<Map<String, String>>();
			hashMap = new HashMap<String, String>();
			hashMap.put("Results0x0", "ReadyData Fail Data Not Match");
			dataList.add(hashMap);
		}
		return dataList;
	}
	
	public static List<Map<String, String>> getList(Object objectData, int style){
		return getList(objectData, style, false);
	}
	
	private static void addData(Object objectData, int style, List<Map<String, String>> dataList, boolean isAddNullString){
		switch (style) {
		case C_downLoadEvent.STYLE_STORE_BY_NEIGHBOR:
			deployJsonAction(dataList, objectData.toString(), style, isAddNullString);
			break;
		default:
			setDelimiter();
			String[] objectTemp = objectData.toString().replace("\r\n", "").replace("\n", "").split(delimiterRow);
			Map<String, String> hashMap;
			for(int i=0; i<objectTemp.length; i++){
				hashMap = new HashMap<String, String>();
				hashMap = deployAction(hashMap, objectTemp, i);
				if(isAddNullString && hashMap.size() == 0){
					System.out.println("Results0x0 NoAvailableData " + objectData.toString());
					hashMap.put("Results0x0", objectData.toString());
				}
				dataList.add(hashMap);
			}
			break;
		}
	}
	
	private static void deployJsonAction(List<Map<String, String>> dataList, String data, int style, boolean isAddNullString){
		switch (style) {
		case C_downLoadEvent.STYLE_STORE_BY_NEIGHBOR:
			break;
		}
		if(isAddNullString && dataList.size() == 0){
			System.out.println("Results0x0 NoAvailableData " + data);
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("Results0x0", data);
			dataList.add(hashMap);
		}
	}
	
	public static Map<String, String> deployJson(String data){
		Map<String, String> hashMap = new HashMap<String, String>();
		if(isJSONArray(data)){
			try{
				JSONArray jsonArray = new JSONArray(data);
				List<String> list = getJSONArrayToList(jsonArray);
				for(int i=0; i<list.size(); i++){
					hashMap.put(JSON_ARRAY_MAPKEY + i, list.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(isJSONObject(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				hashMap = getJSONObjectToMap(jsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return hashMap;
	}
	
	public static boolean isJSONArray(String data){
		try {
			Object object = new JSONTokener(data).nextValue();
			if(object instanceof JSONArray){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isJSONObject(String data){
		try {
			Object object = new JSONTokener(data).nextValue();
			if(object instanceof JSONObject){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isCustomArray(String data){
		data = data.replace("\r\n", "").replace("\n", "").trim();
		if(data.indexOf(JSON_ARRAY_START) == 0 && 
				data.lastIndexOf(JSON_ARRAY_END) == data.length() - 1){
			return true;
		}
		return false;
	}
	
	public static boolean isCustomObject(String data){
		data = data.replace("\r\n", "").replace("\n", "").trim();
		if(data.indexOf(JSON_OBJECT_START) == 0 && 
				data.lastIndexOf(JSON_OBJECT_END) == data.length() - 1){
			return true;
		}
		return false;
	}
	
	public static List<String> getJSONArrayToList(JSONArray jsonArray){
		List<String> list = new ArrayList<String>();
		for(int i=0; i<jsonArray.length(); i++){
			list.add(jsonArray.optString(i));
		}
		return list;
	}
	
	public static String[][] getJSONObjectToArray(JSONObject jsonObject){
		String key;
		JSONArray jsonArrayKey = jsonObject.names();
		String[][] element = new String[jsonArrayKey.length()][2];
		for(int i=0; i<jsonArrayKey.length(); i++){
			key = jsonArrayKey.optString(i);
			element[i][0] = key;
			element[i][1] = jsonObject.optString(key);
		}
		return element;
	}
	
	public static Map<String, String> getJSONObjectToMap(JSONObject jsonObject){
		String key;
		JSONArray jsonArrayKey = jsonObject.names();
		Map<String, String> map = new HashMap<String, String>();
		for(int i=0; i<jsonArrayKey.length(); i++){
			key = jsonArrayKey.optString(i);
			map.put(key, jsonObject.optString(key));
		}
		return map;
	}
	
	private static Map<String, String> deployAction(Map<String, String> hashMap, String[] objectTemp, int position){
		String key, value;
		String[] itemArray = objectTemp[position].split(delimiterEnd);
		for(int i=0; i<itemArray.length; i++){
			if(itemArray.length == 1){
				itemArray[i] = itemArray[i].substring(itemArray[i].lastIndexOf(delimiterRowStart)
						, itemArray[i].indexOf(delimiterRowEnd));
			}else if(i == 0 && position == 0){
				itemArray[i] = itemArray[i].substring(itemArray[i].lastIndexOf(delimiterRowStart));
			}else if(i == itemArray.length - 1 && position == objectTemp.length - 1 || 
					itemArray[i].contains(delimiterRowEnd)){
				itemArray[i] = delimiter + itemArray[i].substring(0, itemArray[i].indexOf(delimiterRowEnd));
			}else{
				itemArray[i] = delimiter + itemArray[i];
			}
			key = itemArray[i].substring(
					itemArray[i].lastIndexOf(delimiter, itemArray[i].indexOf(delimiterKV)-1) + 1
					, itemArray[i].indexOf(delimiterKV));
			value = itemArray[i].substring(
					itemArray[i].indexOf(delimiterKV, itemArray[i].indexOf(delimiter + key + delimiter)) + 2).trim();
			if(value != null && value.length() > 0){
				String s = value.charAt(0) + "" + value.charAt(value.length()-1);
				if(s.equals(delimiter + delimiter)){
					value = value.substring(1, value.length()-1);
				}
			}
			hashMap.put(key, value);
		}
		return hashMap;
	}
	
	private static void setDelimiter(){
		delimiter = "\"";
		delimiterKV = "\":";
		delimiterEnd = ",\"";
		delimiterRow = "\\},\\{\"";
		delimiterRowStart = "{";
		delimiterRowEnd = "}";
	}
}