/*
 * Copyright (C) 2012 Andy Lin. All rights reserved.
 * @version 1.1.2
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public class EncryptUtils {
	
	public static final String TYPE_MD2 = "MD2";
	public static final String TYPE_MD5 = "MD5";
	public static final String TYPE_SHA_1 = "SHA-1";
	public static final String TYPE_SHA_224 = "SHA-224";
	public static final String TYPE_SHA_256 = "SHA-256";
	public static final String TYPE_SHA_384 = "SHA-384";
	public static final String TYPE_SHA_512 = "SHA-512";
	public static final String TYPE_BASE64 = "BASE64";
	
	public static byte[] getEncrypted(String type, byte[] value){
		if(isNull(type, value)){
			return null;
		}
		if(type.equals(TYPE_BASE64)){
			return getBase64Encode(value);
		}else{
			return getMessageDigestEncode(type, value);
		}
	}
	
	public static byte[] getEncrypted(String type, String value){
		if(isNull(type, value)){
			return null;
		}
		return getEncrypted(type, value.getBytes());
	}
	
	public static String getEncryptedToString(String type, byte[] value){
		if(isNull(type, value)){
			return null;
		}
		if(type.equals(TYPE_BASE64)){
			return getBase64EncodeToString(value);
		}else{
			return encodeToString(getEncrypted(type, value));
		}
	}
	
	public static String getEncryptedToString(String type, String value){
		if(isNull(type, value)){
			return null;
		}
		return getEncryptedToString(type, value.getBytes());
	}
	
	public static byte[] getDecrypted(String type, byte[] value){
		if(isNull(type, value)){
			return null;
		}
		if(type.equals(TYPE_BASE64)){
			return getBase64Decode(value);
		}else{
			return null;
		}
	}
	
	public static byte[] getDecrypted(String type, String value){
		if(isNull(type, value)){
			return null;
		}
		return getDecrypted(type, value.getBytes());
	}
	
	public static String getDecryptedToString(String type, byte[] value){
		if(isNull(type, value)){
			return null;
		}
		if(type.equals(TYPE_BASE64)){
			return getBase64DecodeToString(value);
		}else{
			return null;
		}
	}
	
	public static String getDecryptedToString(String type, String value){
		if(isNull(type, value)){
			return null;
		}
		return getDecryptedToString(type, value.getBytes());
	}
	
	private static byte[] getMessageDigestEncode(String type, byte[] value){
		try {
			// MD2、MD5、SHA-1、SHA-256、SHA-384、SHA-512
			MessageDigest messageDigest = MessageDigest.getInstance(type);
			messageDigest.update(value);
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException | NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String encodeToString(byte[] encode){
		StringBuilder sb = new StringBuilder();// StringBuilder速度較快但不支援多執行緒同步
		for(int i=0; i<encode.length; i++){
			int num = 0xff & encode[i];
			String hex = Integer.toHexString(num);
			if(num > 0x0f){
				sb.append(hex);
			}else{
				sb.append("0").append(hex);
			}
		}
		return sb.toString();
	}
	
	private static byte[] getBase64Encode(byte[] value){
		return Base64.encode(value, Base64.DEFAULT);
	}
	
	private static String getBase64EncodeToString(byte[] value){
		return Base64.encodeToString(value, Base64.DEFAULT);
	}
	
	private static byte[] getBase64Decode(byte[] value){
		return Base64.decode(value, Base64.DEFAULT);
	}
	
	private static String getBase64DecodeToString(byte[] value){
		return new String(Base64.decode(value, Base64.DEFAULT));
	}
	
	private static boolean isNull(Object...content){
		if(content == null){
			return true;
		}
		for(Object item : content){
			if(item == null){
				return true;
			}
		}
		return false;
	}
}