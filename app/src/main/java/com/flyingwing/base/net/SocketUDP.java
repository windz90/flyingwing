/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.1
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unused")
public class SocketUDP {

	public static final int STATE_RECEIVER_SUCCESS = 100;
	public static final int STATE_RECEIVER_FAIL = 101;
	public static final int STATE_SEND_SUCCESS = 200;
	public static final int STATE_SEND_FAIL = 201;

	private DatagramSocket mDatagramSocket;
	private DatagramPacket mDatagramPacketReceive;
	private DatagramPacket mDatagramPacketSend;
	private ReadWriteLock mReadWriteLock;
	private boolean mIsStopKeepReceiver;

	/**
	 * syncReceive() can call stopKeepReceiver() or close() stop receiver or close socket.<br/>
	 * bundle include:<br/>
	 * Serializable : InetAddress<br/>
	 * Serializable : InetSocketAddress<br/>
	 * int : port<br/>
	 * byte[] : data
	 */
	public static abstract class SocketCallback {

		private Handler handler;

		protected void setCallbackForMainThread(int what, Bundle bundle){
			if(handler == null){
				handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
					@Override
					public boolean handleMessage(Message msg) {
						receiveForMainThread(msg);
						return false;
					}
				});
			}
			Message message = handler.obtainMessage(what);
			message.setData(bundle);
			handler.sendMessage(message);
		}

		public void syncReceive(Bundle bundle){}
		public abstract void receiveForMainThread(Message message);
	}

	public SocketUDP(boolean isReuseAddress, @IntRange(from = 0, to = 65535) int portLocal){
		try {
			mDatagramSocket = new DatagramSocket(null);
			mDatagramSocket.setReuseAddress(isReuseAddress);
			mDatagramSocket.bind(new InetSocketAddress(portLocal));
			mReadWriteLock = new ReentrantReadWriteLock();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public SocketUDP(@IntRange(from = 0, to = 65535) int portLocal){
		try {
			mDatagramSocket = new DatagramSocket(portLocal);
			mReadWriteLock = new ReentrantReadWriteLock();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void setBroadcast(boolean broadcast){
		try {
			mDatagramSocket.setBroadcast(broadcast);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public boolean isBroadcast(){
		try {
			return mDatagramSocket.getBroadcast();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setReuseAddress(boolean isReuse){
		try {
			mDatagramSocket.setReuseAddress(isReuse);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public boolean isReuseAddress(){
		try {
			return mDatagramSocket.getReuseAddress();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setReceiveBufferSize(int size){
		try {
			mDatagramSocket.setReceiveBufferSize(size);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void setSendBufferSize(int size){
		try {
			mDatagramSocket.setSendBufferSize(size);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void setSoTimeout(int timeout){
		try {
			mDatagramSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void connect(@NonNull InetAddress inetAddress, @IntRange(from=0, to=65535) int portRemote){
		try {
			mDatagramSocket.connect(new InetSocketAddress(inetAddress, portRemote));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void disconnect(){
		mDatagramSocket.disconnect();
	}

	public boolean isConnected(){
		return mDatagramSocket.isConnected();
	}
	
	private void receiverPacketImpl(@NonNull Bundle bundle, @NonNull SocketCallback socketCallback, @NonNull String charsetName){
		try {
			mDatagramSocket.receive(mDatagramPacketReceive);

			byte[] bytes = mDatagramPacketReceive.getData();
			String data = new String(bytes == null ? new byte[0] : bytes, mDatagramPacketReceive.getOffset(), mDatagramPacketReceive.getLength(), charsetName);

			bundle.remove("InetAddress");
			bundle.remove("InetSocketAddress");
			bundle.remove("port");
			bundle.remove("data");

			bundle.putSerializable("InetAddress", mDatagramPacketReceive.getAddress());
			bundle.putSerializable("InetSocketAddress", mDatagramPacketReceive.getSocketAddress());
			bundle.putInt("port", mDatagramPacketReceive.getPort());
			bundle.putString("data", data);

			socketCallback.syncReceive(bundle);
			socketCallback.setCallbackForMainThread(STATE_RECEIVER_SUCCESS, bundle);
		} catch (Exception e) {
			e.printStackTrace();
			socketCallback.setCallbackForMainThread(STATE_RECEIVER_FAIL, null);
		}
	}

	/**
	 * @param packetDataLength receiver packet max length.
	 */
	public void receiverPacket(int packetDataLength, @NonNull final SocketCallback socketCallback, @NonNull final String charsetName){
		// receiver one packet, DatagramPacket must re-new, avoid receiving old packet.
		mDatagramPacketReceive = new DatagramPacket(new byte[packetDataLength], packetDataLength);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bundle bundle;
				if (!isClosed()) {
					bundle = new Bundle();
					receiverPacketImpl(bundle, socketCallback, charsetName);
				}
			}
		}).start();
	}

	/**
	 * @param packetDataLength receiver packet max length.
	 */
	public void receiverPacket(int packetDataLength, @NonNull final SocketCallback socketCallback) {
		receiverPacket(packetDataLength, socketCallback, "ISO-8859-1");
	}

	/**
	 * @param packetDataLength receiver packet max length.
	 */
	public void receiverPacketKeep(int packetDataLength, @NonNull final SocketCallback socketCallback, @NonNull final String charsetName){
		mDatagramPacketReceive = new DatagramPacket(new byte[packetDataLength], packetDataLength);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bundle bundle;
				while (!mIsStopKeepReceiver && !isClosed()) {
					bundle = new Bundle();
					receiverPacketImpl(bundle, socketCallback, charsetName);
				}
			}
		}).start();
	}

	/**
	 * @param packetDataLength receiver packet max length.
	 */
	public void receiverPacketKeep(int packetDataLength, @NonNull final SocketCallback socketCallback) {
		receiverPacketKeep(packetDataLength, socketCallback, "ISO-8859-1");
	}

	public void stopKeepReceiver(boolean isStopReceiver){
		mIsStopKeepReceiver = isStopReceiver;
	}

	public boolean isStopKeepReceiver(){
		return mIsStopKeepReceiver;
	}

	public void sendPacket(String data, InetAddress inetAddress, @IntRange(from=0, to=65535) int portRemote, final int sendCount, final SocketCallback socketCallback){
		byte[] bytes = data.getBytes();
		mDatagramPacketSend = new DatagramPacket(bytes, bytes.length, inetAddress, portRemote);
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean isSendSuccess = false;
				for(int i=0; !isClosed() && i<sendCount; i++){
					try {
						mDatagramSocket.send(mDatagramPacketSend);
						isSendSuccess = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(socketCallback != null){
					socketCallback.setCallbackForMainThread(isSendSuccess ? STATE_SEND_SUCCESS : STATE_SEND_FAIL, null);
				}
			}
		}).start();
	}

	public boolean isClosed(){
		mReadWriteLock.readLock().lock();
		boolean isClose = mDatagramSocket == null || mDatagramSocket.isClosed();
		mReadWriteLock.readLock().unlock();
		return isClose;
	}

	public void close(){
		mReadWriteLock.writeLock().lock();
		try {
			if(mDatagramSocket != null){
				if(!mDatagramSocket.isClosed()){
					mDatagramSocket.close();
				}
				mDatagramSocket = null;
			}
			if(mDatagramPacketReceive != null){
				mDatagramPacketReceive = null;
			}
			if(mDatagramPacketSend != null){
				mDatagramPacketSend = null;
			}
		} finally {
			mReadWriteLock.writeLock().unlock();
		}
	}
}