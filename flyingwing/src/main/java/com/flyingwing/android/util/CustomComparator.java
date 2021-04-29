/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 2.1.3
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.util;

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
@SuppressWarnings({"unused"})
public class CustomComparator implements Comparator<Map<String, String>>{
	
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
	private int style, orderBy;
	private boolean isRunDefaultSort;
	private RuleBasedCollator collator;
	
	public CustomComparator(String compareKey, int style, int orderBy, Locale locale, boolean isRunDefaultSort){
		this.compareKey = compareKey;
		this.style = style;
		this.orderBy = orderBy;
		collator = (RuleBasedCollator)Collator.getInstance(locale);
		this.isRunDefaultSort = isRunDefaultSort;
	}
	
	public CustomComparator(String compareStr, int style, int orderBy){
		this(compareStr, style, orderBy, Locale.getDefault(), false);
	}
	
	@Override
	public int compare(Map<String, String> lhs, Map<String, String> rhs) {
		String lValue = lhs.get(compareKey), rValue = rhs.get(compareKey);
		int compareResult;
		switch (style) {
		case STYLE_INT:
			compareResult = compareByInt(lValue, rValue, orderBy);
			break;
		case STYLE_LONG:
			compareResult = compareByLong(lValue, rValue, orderBy);
			break;
		case STYLE_FLOAT:
			compareResult = compareByFloat(lValue, rValue, orderBy);
			break;
		case STYLE_DOUBLE:
			compareResult = compareByDouble(lValue, rValue, orderBy);
			break;
		case STYLE_INT_REG_EXP:
			pattern = Pattern.compile("^\\d+$");
			compareResult = compareByIntRegExp(lValue, rValue, orderBy);
			break;
		case STYLE_INT_REG_EXP_BEGIN:
			pattern = Pattern.compile("^\\d+");
			compareResult = compareByIntRegExp(lValue, rValue, orderBy);
			break;
		case STYLE_INT_REG_EXP_CONTAINS:
			pattern = Pattern.compile("\\d+");
			compareResult = compareByIntRegExp(lValue, rValue, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP:
			pattern = Pattern.compile("^\\d+(\\.\\d+)?$");
			compareResult = compareByFloatRegExp(lValue, rValue, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP_BEGIN:
			pattern = Pattern.compile("^\\d+(\\.\\d+)?");
			compareResult = compareByFloatRegExp(lValue, rValue, orderBy);
			break;
		case STYLE_FLOAT_REG_EXP_CONTAINS:
			pattern = Pattern.compile("\\d+(\\.\\d+)?");
			compareResult = compareByFloatRegExp(lValue, rValue, orderBy);
			break;
		default:
			compareResult = compareByString(lValue, rValue, orderBy);
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

	private int compareByInt(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByIntAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByIntDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByLong(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByLongAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByLongDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByFloat(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByFloatAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByFloatDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByDouble(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByDoubleAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByDoubleDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByIntRegExp(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByIntRegExpAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByIntRegExpDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByFloatRegExp(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByFloatRegExpAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByFloatRegExpDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByString(String lValue, String rValue, int orderBy){
		if(orderBy == ORDER_BY_ASC){
			return compareByStringAsc(lValue, rValue);
		}else if(orderBy == ORDER_BY_DESC){
			return compareByStringDesc(lValue, rValue);
		}
		return 0;
	}

	private int compareByIntAsc(String lValue, String rValue){
		int lhsInt, rhsInt;
		try {
			lhsInt = Integer.parseInt(lValue.trim());
		} catch (Exception e) {
			lhsInt = 0;
		}
		try {
			rhsInt = Integer.parseInt(rValue.trim());
		} catch (Exception e) {
			rhsInt = 0;
		}
		if(lhsInt < rhsInt){
			return -1;
		}else if(lhsInt > rhsInt){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByIntDesc(String lValue, String rValue){
		int lhsInt, rhsInt;
		try {
			lhsInt = Integer.parseInt(lValue.trim());
		} catch (Exception e) {
			lhsInt = 0;
		}
		try {
			rhsInt = Integer.parseInt(rValue.trim());
		} catch (Exception e) {
			rhsInt = 0;
		}
		if(lhsInt > rhsInt){
			return -1;
		}else if(lhsInt < rhsInt){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByLongAsc(String lValue, String rValue){
		long lhsLong, rhsLong;
		try {
			lhsLong = Long.parseLong(lValue.trim());
		} catch (Exception e) {
			lhsLong = 0;
		}
		try {
			rhsLong = Long.parseLong(rValue.trim());
		} catch (Exception e) {
			rhsLong = 0;
		}
		if(lhsLong < rhsLong){
			return -1;
		}else if(lhsLong > rhsLong){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByLongDesc(String lValue, String rValue){
		long lhsLong, rhsLong;
		try {
			lhsLong = Long.parseLong(lValue.trim());
		} catch (Exception e) {
			lhsLong = 0;
		}
		try {
			rhsLong = Long.parseLong(rValue.trim());
		} catch (Exception e) {
			rhsLong = 0;
		}
		if(lhsLong > rhsLong){
			return -1;
		}else if(lhsLong < rhsLong){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByFloatAsc(String lValue, String rValue){
		float lhsFloat, rhsFloat;
		try {
			lhsFloat = Float.parseFloat(lValue.trim());
		} catch (Exception e) {
			lhsFloat = 0;
		}
		try {
			rhsFloat = Float.parseFloat(rValue.trim());
		} catch (Exception e) {
			rhsFloat = 0;
		}
		if(lhsFloat < rhsFloat){
			return -1;
		}else if(lhsFloat > rhsFloat){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByFloatDesc(String lValue, String rValue){
		float lhsFloat, rhsFloat;
		try {
			lhsFloat = Float.parseFloat(lValue.trim());
		} catch (Exception e) {
			lhsFloat = 0;
		}
		try {
			rhsFloat = Float.parseFloat(rValue.trim());
		} catch (Exception e) {
			rhsFloat = 0;
		}
		if(lhsFloat > rhsFloat){
			return -1;
		}else if(lhsFloat < rhsFloat){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByDoubleAsc(String lValue, String rValue){
		double lhsDouble, rhsDouble;
		try {
			lhsDouble = Double.parseDouble(lValue.trim());
		} catch (Exception e) {
			lhsDouble = 0;
		}
		try {
			rhsDouble = Double.parseDouble(rValue.trim());
		} catch (Exception e) {
			rhsDouble = 0;
		}
		if(lhsDouble < rhsDouble){
			return -1;
		}else if(lhsDouble > rhsDouble){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByDoubleDesc(String lValue, String rValue){
		double lhsDouble, rhsDouble;
		try {
			lhsDouble = Double.parseDouble(lValue.trim());
		} catch (Exception e) {
			lhsDouble = 0;
		}
		try {
			rhsDouble = Double.parseDouble(rValue.trim());
		} catch (Exception e) {
			rhsDouble = 0;
		}
		if(lhsDouble > rhsDouble){
			return -1;
		}else if(lhsDouble < rhsDouble){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByIntRegExpAsc(String lValue, String rValue){
		int lhsInt = 0, rhsInt = 0;
		matcher = pattern.matcher(lValue.trim());
		if(matcher.find()){
			lhsInt = Integer.parseInt(matcher.group());
		}
		matcher = pattern.matcher(rValue.trim());
		if(matcher.find()){
			rhsInt = Integer.parseInt(matcher.group());
		}
		if(lhsInt < rhsInt){
			return -1;
		}else if(lhsInt > rhsInt){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByIntRegExpDesc(String lValue, String rValue){
		int lhsInt = 0, rhsInt = 0;
		matcher = pattern.matcher(lValue.trim());
		if(matcher.find()){
			lhsInt = Integer.parseInt(matcher.group());
		}
		matcher = pattern.matcher(rValue.trim());
		if(matcher.find()){
			rhsInt = Integer.parseInt(matcher.group());
		}
		if(lhsInt > rhsInt){
			return -1;
		}else if(lhsInt < rhsInt){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByFloatRegExpAsc(String lValue, String rValue){
		float lhsFloat = 0, rhsFloat = 0;
		matcher = pattern.matcher(lValue.trim());
		if(matcher.find()){
			lhsFloat = Float.parseFloat(matcher.group());
		}
		matcher = pattern.matcher(rValue.trim());
		if(matcher.find()){
			rhsFloat = Float.parseFloat(matcher.group());
		}
		if(lhsFloat < rhsFloat){
			return -1;
		}else if(lhsFloat > rhsFloat){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByFloatRegExpDesc(String lValue, String rValue){
		float lhsFloat = 0, rhsFloat = 0;
		matcher = pattern.matcher(lValue.trim());
		if(matcher.find()){
			lhsFloat = Float.parseFloat(matcher.group());
		}
		matcher = pattern.matcher(rValue.trim());
		if(matcher.find()){
			rhsFloat = Float.parseFloat(matcher.group());
		}
		if(lhsFloat > rhsFloat){
			return -1;
		}else if(lhsFloat < rhsFloat){
			return 1;
		}
		return defaultSort(lValue.trim(), rValue.trim());
	}

	private int compareByStringAsc(String lValue, String rValue){
		return collator.compare(lValue.trim(), rValue.trim());
	}

	private int compareByStringDesc(String lValue, String rValue){
		return collator.compare(rValue.trim(), lValue.trim());
	}
}