package com.flyingwing.base.module.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataBaseDAO {
	
	public static final String[] SQLITE_ARRAY = new String[]{"test.sqlite"};
	public static final int DATABASE_TEST = 0;
	
	public static final String TABLE_TEST = "test";
	
	private static String dataBaseName = SQLITE_ARRAY[DATABASE_TEST];
	private static String tableName;
	
	public interface QueryComplete{
		public void dataQuery(String[] getField, String sql, Cursor cursor);
	}
	
	public static void setDataBaseName(String dataBaseName){
		DataBaseDAO.dataBaseName = dataBaseName;
	}
	
	public static void setTableName(String tableName){
		DataBaseDAO.tableName = tableName;
	}
	
	public static String getDataBaseName(){
		return dataBaseName;
	}
	
	public static String getTableName(){
		return tableName;
	}
	
	public static List<Map<String, String>> findBySQL(Context context, String sql) {
		DataBaseHelper helper = new DataBaseHelper(context, dataBaseName);
		SQLiteDatabase dataBase = null;
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		try {
			helper.createDataBase(context, dataBaseName);
			dataBase = helper.getReadableDatabase();
			if(sql != null && sql.trim().length() > 0){
				Cursor cursor = dataBase.rawQuery(sql, null);
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<cursor.getColumnCount(); i++){
						hashMap.put(cursor.getColumnName(i), cursor.getString(i));
					}
					dataList.add(hashMap);
				}
			}
		} catch (Exception e) {
			Log.i("findBySQL", e.toString());
		} finally {
			try {
				if (dataBase.isOpen())
					dataBase.close();
			} catch (Exception e) {
				Log.i("Exception", e.toString());
			}
		}
		return dataList;
	}
	
	public static void findByKey(Context context, String[]getField, String queryField, String appendSql, QueryComplete complete) {
		DataBaseHelper helper = new DataBaseHelper(context, dataBaseName);
		SQLiteDatabase dataBase = null;
		
		try {
			helper.createDataBase(context, dataBaseName);
			dataBase = helper.getReadableDatabase();
			String sql = "select " + queryField + " from " + tableName;
			if((appendSql != null && appendSql.trim().length() > 0)){
				sql = sql + " " + appendSql;
			}
			Cursor cursor = dataBase.rawQuery(sql, null);
			complete.dataQuery(getField, sql, cursor);
		} catch (Exception e) {
			Log.i("findByKey", e.toString());
		} finally {
			try {
				if (dataBase.isOpen())
					dataBase.close();
			} catch (Exception e) {
				Log.i("Exception", e.toString());
			}
		}
	}
	
	public static List<Map<String, String>> findByKey(Context context, String[]getField, String queryField, String appendSql) {
		DataBaseHelper helper = new DataBaseHelper(context, dataBaseName);
		SQLiteDatabase dataBase = null;
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		try {
			helper.createDataBase(context, dataBaseName);
			dataBase = helper.getReadableDatabase();
			String sql = "select " + queryField + " from " + tableName;
			if((appendSql != null && appendSql.trim().length() > 0)){
				sql = sql + " " + appendSql;
			}
			Cursor cursor = dataBase.rawQuery(sql, null);
			
			if(getField == null || getField[0].trim().equals("*")){
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<cursor.getColumnCount(); i++){
						hashMap.put(cursor.getColumnName(i), cursor.getString(i));
					}
					dataList.add(hashMap);
				}
			}else{
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<getField.length; i++){
						hashMap.put(getField[i].trim(), cursor.getString(cursor.getColumnIndex(getField[i].trim())));
					}
					dataList.add(hashMap);
				}
			}
		} catch (Exception e) {
			Log.i("findByKey", e.toString());
		} finally {
			try {
				if (dataBase.isOpen())
					dataBase.close();
			} catch (Exception e) {
				Log.i("Exception", e.toString());
			}
		}
		return dataList;
	}
	
	public static List<Map<String, String>> findByKeyWhere(Context context, String[]getField, String queryField, String key, String value, String appendSql) {
		DataBaseHelper helper = new DataBaseHelper(context, dataBaseName);
		SQLiteDatabase dataBase = null;
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		try {
			helper.createDataBase(context, dataBaseName);
			dataBase = helper.getReadableDatabase();
			String sql = "select " + queryField + " from " + tableName;
			if((key != null && key.trim().length() > 0) && (value != null && value.trim().length() > 0)){
				sql = sql + " where " + key  + " = '" + value + "'";
			}
			if((appendSql != null && appendSql.trim().length() > 0)){
				sql = sql + " " + appendSql;
			}
			Cursor cursor = dataBase.rawQuery(sql, null);
			
			if(getField == null || getField[0].trim().equals("*")){
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<cursor.getColumnCount(); i++){
						hashMap.put(cursor.getColumnName(i), cursor.getString(i));
					}
					dataList.add(hashMap);
				}
			}else{
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<getField.length; i++){
						hashMap.put(getField[i].trim(), cursor.getString(cursor.getColumnIndex(getField[i].trim())));
					}
					dataList.add(hashMap);
				}
			}
		} catch (Exception e) {
			Log.i("findByKeyWhere", e.toString());
		} finally {
			try {
				if (dataBase.isOpen())
					dataBase.close();
			} catch (Exception e) {
				Log.i("Exception", e.toString());
			}
		}
		return dataList;
	}
	
	public static List<Map<String, String>> findByKeyWhereLike(Context context, String[]getField, String queryField, String key, String value, String appendSql) {
		DataBaseHelper helper = new DataBaseHelper(context, dataBaseName);
		SQLiteDatabase dataBase = null;
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		try {
			helper.createDataBase(context, dataBaseName);
			dataBase = helper.getReadableDatabase();
			String sql = "select " + queryField + " from " + tableName;
			if((key != null && key.trim().length() > 0) && (value != null && value.trim().length() > 0)){
				sql = sql + " where " + key  + " like '%" + value + "%'";
			}
			if((appendSql != null && appendSql.trim().length() > 0)){
				sql = sql + " " + appendSql;
			}
			Cursor cursor = dataBase.rawQuery(sql, null);
			
			if(getField == null || getField[0].trim().equals("*")){
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<cursor.getColumnCount(); i++){
						hashMap.put(cursor.getColumnName(i), cursor.getString(i));
					}
					dataList.add(hashMap);
				}
			}else{
				while (cursor.moveToNext()) {
					Map<String, String> hashMap = new HashMap<String, String>();
					for(int i=0; i<getField.length; i++){
						hashMap.put(getField[i].trim(), cursor.getString(cursor.getColumnIndex(getField[i].trim())));
					}
					dataList.add(hashMap);
				}
			}
		} catch (Exception e) {
			Log.i("findByKeyWhereLike", e.toString());
		} finally {
			try {
				if (dataBase.isOpen())
					dataBase.close();
			} catch (Exception e) {
				Log.i("Exception", e.toString());
			}
		}
		return dataList;
	}
}