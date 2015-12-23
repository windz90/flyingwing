/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.0
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.base.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.IOException;
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
	private boolean mIsStopReceiver;
	
	public static abstract class SocketCallback {

		private Handler handler;

		protected void setAsyncCallback(int what, Bundle bundle){
			if(handler == null){
				handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
					@Override
					public boolean handleMessage(Message msg) {
						asyncCallback(msg);
						return false;
					}
				});
			}
			Message message = handler.obtainMessage(what);
			message.setData(bundle);
			handler.sendMessage(message);
		}

		public abstract void syncEvent(Bundle bundle);
		public abstract void asyncCallback(Message message);
	}

	public SocketUDP(int portLocal){
		try {
			mDatagramSocket = new DatagramSocket(portLocal);
			mDatagramSocket.setBroadcast(true);
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

	public void setReuseAddress(boolean reuse){
		try {
			mDatagramSocket.setReuseAddress(reuse);
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

	public void connect(@NonNull InetAddress inetAddress, int portRemote){
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

	/**
	 * @param packetDataLength receiver packet max length.
	 * @param socketCallback sync event can call stopReceiver() or close() stop receiver or close socket, bundle include:<br/>
	 * Serializable : InetAddress,
	 * Serializable : InetSocketAddress,
	 * int : port,
	 * byte[] : data
	 */
	public void receiverPacket(int packetDataLength, @NonNull final SocketCallback socketCallback){
		mDatagramPacketReceive = new DatagramPacket(new byte[packetDataLength], packetDataLength);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bundle bundle;
				while (!mIsStopReceiver && !isClosed()) {
					try {
						mDatagramSocket.receive(mDatagramPacketReceive);

						bundle = new Bundle();
						bundle.putSerializable("InetAddress", mDatagramPacketReceive.getAddress());
						bundle.putSerializable("InetSocketAddress", mDatagramPacketReceive.getSocketAddress());
						bundle.putInt("port", mDatagramPacketReceive.getPort());
						bundle.putByteArray("data", mDatagramPacketReceive.getData());

						socketCallback.syncEvent(bundle);
						socketCallback.setAsyncCallback(STATE_RECEIVER_SUCCESS, bundle);
					} catch (IOException e) {
						e.printStackTrace();
						socketCallback.setAsyncCallback(STATE_RECEIVER_FAIL, null);
					}
				}
			}
		}).start();
	}
	
	public void stopReceiver(){
		mIsStopReceiver = true;
	}

	public void sendPacket(String data, InetAddress inetAddress, int portRemote, final int sendCount, final SocketCallback socketCallback){
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
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(socketCallback != null){
					socketCallback.setAsyncCallback(isSendSuccess ? STATE_SEND_SUCCESS : STATE_SEND_FAIL, null);
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