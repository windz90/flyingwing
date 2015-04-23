/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.1.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.andy.library.module;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * 排序規則<br>
 * Comparator是在集合外定義Comparator接口實現的排序<br>
 * Comparable是在集合內部實作Comparable接口實現的排序<br><br>
 */
public class C_comparator implements Comparator<Map<String, String>>{
	
	public static final int STYLE_STRING = 1;
	public static final int STYLE_INT = 2;
	public static final int STYLE_LONG = 3;
	public static final int STYLE_FLOAT = 4;
	public static final int STYLE_DOUBLE = 5;
	public static final int STYLE_INT_REG_EXP = 6;
	public static final int STYLE_INT_REG_EXP_BEGIN = 7;
	public static final int STYLE_INT_REG_EXP_CONTAINS = 8;
	public static final int STYLE_FLOAT_REG_EXP = 9;
	public static final int STYLE_FLOAT_REG_EXP_BEGIN = 10;
	public static final int STYLE_FLOAT_REG_EXP_CONTAINS = 11;
	public static final int ORDER_BY_ASC = 1;
	public static final int ORDER_BY_DESC = 2;
	
	private Pattern pattern;
	private Matcher matcher;
	private String compareKey;
	private int style, orderBy, compareResult;
	private boolean isRunDefaultSort;
	private RuleBasedCollator collator;
	
	public C_comparator(String compareKey, int style, int orderBy, Locale locale, boolean isRunDefaultSort){
		this.compareKey = compareKey;
		this.style = style;
		this.orderBy = orderBy;
		collator = (RuleBasedCollator)Collator.getInstance(locale);
		this.isRunDefaultSort = isRunDefaultSort;
	}
	
	public C_comparator(String compareStr, int style, int orderBy){
		this(compareStr, style, orderBy, Locale.getDefault(), false);
	}
	
	@Override
	public int compare(Map<String, String> lhs, Map<String, String> rhs) {
		switch (style) {
		case STYLE_INT:
			compareResult = compareByInt(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_LONG:
			compareResult = compareByLong(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_FLOAT:
			compareResult = compareByFloat(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_DOUBLE:
			compareResult = compareByDouble(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_INT_REG_EXP:
			pattern = Pattern.compile("^\\d+$");
			compareResult = compareByIntRegExp(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_INT_REG_EXP_BEGIN:
			pattern = Pattern.compile("^\\d+");
			compareResult = compareByIntRegExp(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_INT_REG_EXP_CONTAINS:
			pattern = Pattern.compile("\\d+");
			compareResult = compareByIntRegExp(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP:
			pattern = Pattern.compile("^\\d+(\\.\\d+)?$");
			compareResult = compareByFloatRegExp(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP_BEGIN:
			pattern = Pattern.compile("^\\d+(\\.\\d+)?");
			compareResult = compareByFloatRegExp(lhs, rhs, compareKey, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP_CONTAINS:
			pattern = Pattern.compile("\\d+(\\.\\d+)?");
			compareResult = compareByFloatRegExp(lhs, rhs, compareKey, orderBy);
			break;
		default:
			compareResult = compareByString(lhs, rhs, compareKey, orderBy);
			break;
		}
		return compareResult;
	}
	
	private int defaultSort(String source, String target){
		if(isRunDefaultSort){
			return collator.compare(source, target);
		}
		return 0;
	}
	
	private int compareByInt(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByIntAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByIntDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByLong(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByLongAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByLongDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByFloat(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByFloatAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByFloatDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByDouble(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByDoubleAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByDoubleDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByIntRegExp(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByIntRegExpAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByIntRegExpDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByFloatRegExp(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByFloatRegExpAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByFloatRegExpDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByString(Map<String, String> lhs, Map<String, String> rhs, String compareKey, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByStringAsc(lhs, rhs, compareKey);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByStringDesc(lhs, rhs, compareKey);
		}
		return 0;
	}
	
	private int compareByIntAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		int lhsInt, rhsInt;
		try {
			lhsInt = Integer.parseInt(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsInt = 0;
		}
		try {
			rhsInt = Integer.parseInt(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsInt = 0;
		}
		if(lhsInt < rhsInt){
			return -1;
		}else if(lhsInt > rhsInt){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByIntDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		int lhsInt, rhsInt;
		try {
			lhsInt = Integer.parseInt(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsInt = 0;
		}
		try {
			rhsInt = Integer.parseInt(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsInt = 0;
		}
		if(lhsInt > rhsInt){
			return -1;
		}else if(lhsInt < rhsInt){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByLongAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		long lhsLong, rhsLong;
		try {
			lhsLong = Long.parseLong(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsLong = 0;
		}
		try {
			rhsLong = Long.parseLong(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsLong = 0;
		}
		if(lhsLong < rhsLong){
			return -1;
		}else if(lhsLong > rhsLong){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByLongDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		long lhsLong, rhsLong;
		try {
			lhsLong = Long.parseLong(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsLong = 0;
		}
		try {
			rhsLong = Long.parseLong(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsLong = 0;
		}
		if(lhsLong > rhsLong){
			return -1;
		}else if(lhsLong < rhsLong){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByFloatAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		float lhsFloat, rhsFloat;
		try {
			lhsFloat = Float.parseFloat(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsFloat = 0;
		}
		try {
			rhsFloat = Float.parseFloat(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsFloat = 0;
		}
		if(lhsFloat < rhsFloat){
			return -1;
		}else if(lhsFloat > rhsFloat){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByFloatDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		float lhsFloat, rhsFloat;
		try {
			lhsFloat = Float.parseFloat(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsFloat = 0;
		}
		try {
			rhsFloat = Float.parseFloat(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsFloat = 0;
		}
		if(lhsFloat > rhsFloat){
			return -1;
		}else if(lhsFloat < rhsFloat){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByDoubleAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		double lhsDouble, rhsDouble;
		try {
			lhsDouble = Double.parseDouble(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsDouble = 0;
		}
		try {
			rhsDouble = Double.parseDouble(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsDouble = 0;
		}
		if(lhsDouble < rhsDouble){
			return -1;
		}else if(lhsDouble > rhsDouble){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByDoubleDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		double lhsDouble, rhsDouble;
		try {
			lhsDouble = Double.parseDouble(lhs.get(compareKey).trim());
		} catch (Exception e) {
			lhsDouble = 0;
		}
		try {
			rhsDouble = Double.parseDouble(rhs.get(compareKey).trim());
		} catch (Exception e) {
			rhsDouble = 0;
		}
		if(lhsDouble > rhsDouble){
			return -1;
		}else if(lhsDouble < rhsDouble){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByIntRegExpAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		int lhsInt = 0, rhsInt = 0;
		matcher = pattern.matcher(lhs.get(compareKey).trim());
		if(matcher.find()){
			lhsInt = Integer.parseInt(matcher.group());
		}
		matcher = pattern.matcher(rhs.get(compareKey).trim());
		if(matcher.find()){
			rhsInt = Integer.parseInt(matcher.group());
		}
		if(lhsInt < rhsInt){
			return -1;
		}else if(lhsInt > rhsInt){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByIntRegExpDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		int lhsInt = 0, rhsInt = 0;
		matcher = pattern.matcher(lhs.get(compareKey).trim());
		if(matcher.find()){
			lhsInt = Integer.parseInt(matcher.group());
		}
		matcher = pattern.matcher(rhs.get(compareKey).trim());
		if(matcher.find()){
			rhsInt = Integer.parseInt(matcher.group());
		}
		if(lhsInt > rhsInt){
			return -1;
		}else if(lhsInt < rhsInt){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByFloatRegExpAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		float lhsFloat = 0, rhsFloat = 0;
		matcher = pattern.matcher(lhs.get(compareKey).trim());
		if(matcher.find()){
			lhsFloat = Float.parseFloat(matcher.group());
		}
		matcher = pattern.matcher(rhs.get(compareKey).trim());
		if(matcher.find()){
			rhsFloat = Float.parseFloat(matcher.group());
		}
		if(lhsFloat < rhsFloat){
			return -1;
		}else if(lhsFloat > rhsFloat){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByFloatRegExpDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		float lhsFloat = 0, rhsFloat = 0;
		matcher = pattern.matcher(lhs.get(compareKey).trim());
		if(matcher.find()){
			lhsFloat = Float.parseFloat(matcher.group());
		}
		matcher = pattern.matcher(rhs.get(compareKey).trim());
		if(matcher.find()){
			rhsFloat = Float.parseFloat(matcher.group());
		}
		if(lhsFloat > rhsFloat){
			return -1;
		}else if(lhsFloat < rhsFloat){
			return 1;
		}
		return defaultSort(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByStringAsc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		return collator.compare(lhs.get(compareKey).trim(), rhs.get(compareKey).trim());
	}
	
	private int compareByStringDesc(Map<String, String> lhs, Map<String, String> rhs, String compareKey){
		return collator.compare(rhs.get(compareKey).trim(), lhs.get(compareKey).trim());
	}
}