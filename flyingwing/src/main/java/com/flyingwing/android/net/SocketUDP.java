/*
 * Copyright 2015 Andy Lin. All rights reserved.
 * @version 1.0.7
 * @author Andy Lin
 * @since JDK 1.5 and Android 2.2
 */

package com.flyingwing.android.net;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SocketUDP {

	public static final int STATE_RECEIVE_SUCCESS = 100;
	public static final int STATE_RECEIVE_FAILED = 101;
	public static final int STATE_SEND_SUCCESS = 200;
	public static final int STATE_SEND_FAILED = 201;

	private DatagramSocket mDatagramSocket;
	private DatagramPacket mDatagramPacketReceive;
	private DatagramPacket mDatagramPacketSend;
	private InetAddress mInetAddressPacketRemote;
	private int mPortPacketRemote;
	private boolean mIsKeepReceive;

	/**
	 * if running {@link #receivePacketKeep(int, SocketCallback)}, {@link #receiveSync(int, Bundle)} can call {@link #keepReceive(boolean)} stop receive loop, or call {@link #close()} close socket.<br>
	 * bundle include:<br>
	 * Serializable : InetAddress<br>
	 * Serializable : InetSocketAddress<br>
	 * int : port<br>
	 * byte[] : data
	 */
	public static abstract class SocketCallback {

		private Handler handler;

		protected void callbackForMainThread(final int what, Bundle bundle){
			if(handler == null){
				handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
					@Override
					public boolean handleMessage(@NonNull Message msg) {
						if(what == STATE_RECEIVE_SUCCESS || what == STATE_RECEIVE_FAILED){
							receiveForMainThread(msg);
						}else{
							sendCallbackForMainThread(what);
						}
						return false;
					}
				});
			}
			Message message = handler.obtainMessage(what);
			message.setData(bundle);
			handler.sendMessage(message);
		}

		public void receiveSync(int flag, Bundle bundle){}
		public void receiveForMainThread(Message message){}
		public void sendCallbackSync(int flag){}
		public void sendCallbackForMainThread(int flag){}
	}

	public SocketUDP(boolean isReuseAddress, @IntRange(from = 0, to = 65535) int portLocal){
		try {
			mDatagramSocket = new DatagramSocket(null);
			mDatagramSocket.setReuseAddress(isReuseAddress);
			mDatagramSocket.bind(new InetSocketAddress(portLocal));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reuse random port
	 * @param portRangeMin random min value
	 * @param portRangeMax random max value
	 * @param isTryAsyncLoopBind is try async loop bind unused local port in the range(not reuseAddress)
	 */
	public SocketUDP(@IntRange(from = 0, to = 65535) final int portRangeMin, @IntRange(from = 0, to = 65535) final int portRangeMax, boolean isTryAsyncLoopBind
			, final int...excludePort){
		int randomPort = (int)(Math.random() * (portRangeMax - portRangeMin + 1)) + portRangeMin;
		for(int i=0; i<excludePort.length; i++){
			if(randomPort == excludePort[i]){
				randomPort = (int)(Math.random() * (portRangeMax - portRangeMin + 1)) + portRangeMin;
				i = -1;
			}
		}
		try {
			mDatagramSocket = new DatagramSocket(null);
			mDatagramSocket.setReuseAddress(true);
			mDatagramSocket.bind(new InetSocketAddress(randomPort));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(isTryAsyncLoopBind){
			new Thread(new Runnable() {
				@Override
				public void run() {
					bindLoop(portRangeMin, portRangeMax, excludePort);
				}
			}).start();
		}
	}

	/**
	 * Reuse random port
	 * @param portRangeMin random min value
	 * @param portRangeMax random max value
	 */
	public SocketUDP(@IntRange(from = 0, to = 65535) final int portRangeMin, @IntRange(from = 0, to = 65535) final int portRangeMax, int...excludePort){
		this(portRangeMin, portRangeMax, false, excludePort);
	}

	/**
	 * Reuse random port
	 */
	public SocketUDP(){
		this(1025, 65535, false);
	}

	public void setBroadcast(boolean broadcast){
		try {
			mDatagramSocket.setBroadcast(broadcast);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isBroadcast(){
		try {
			return mDatagramSocket.getBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setReuseAddress(boolean isReuse){
		try {
			mDatagramSocket.setReuseAddress(isReuse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isReuseAddress(){
		try {
			return mDatagramSocket.getReuseAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void bind(int portLocal) throws SocketException {
		mDatagramSocket.bind(new InetSocketAddress(portLocal));
	}

	/**
	 * Try loop bind unused local port in the range(not reuseAddress)
	 */
	public void bindLoop(@IntRange(from = 0, to = 65535) int portRangeMin, @IntRange(from = 0, to = 65535) int portRangeMax, int...excludePort){
		if(excludePort.length > 1){
			Arrays.sort(excludePort);
		}
		InetSocketAddress inetSocketAddress;
		for(int i=portRangeMin; i<=portRangeMax; i++){
			if(excludePort.length > 0){
				if(i == excludePort[0] || i == excludePort[excludePort.length - 1]){
					continue;
				}else if(i > excludePort[0] || i < excludePort[excludePort.length - 1]){
					boolean isMatch = false;
					int j = 0;
					while (j < excludePort.length) {
						if(i == excludePort[j]){
							isMatch = true;
							break;
						}
						j++;
					}
					if(isMatch){
						continue;
					}
				}
			}
			try {
				inetSocketAddress = new InetSocketAddress(i);
				mDatagramSocket.bind(inetSocketAddress);
				break;
			} catch (Exception ignored) {}
			if(i == portRangeMax){
				System.out.println(portRangeMin + "-" + portRangeMax + " local port bind fail");
			}
		}
	}

	public int getSocketLocalPort(){
		return mDatagramSocket == null ? -1 : mDatagramSocket.getLocalPort();
	}

	public int getSocketRemotePort(){
		return mDatagramSocket == null ? -1 : mDatagramSocket.getPort();
	}

	@Nullable
	public InetAddress getSocketLocalInetAddress(){
		return mDatagramSocket == null ? null : mDatagramSocket.getLocalAddress();
	}

	@Nullable
	public InetAddress getSocketRemoteInetAddress(){
		return mDatagramSocket == null ? null : mDatagramSocket.getInetAddress();
	}

	@Nullable
	public SocketAddress getSocketLocalSocketAddress(){
		return mDatagramSocket == null ? null : mDatagramSocket.getLocalSocketAddress();
	}

	@Nullable
	public SocketAddress getSocketRemoteSocketAddress(){
		return mDatagramSocket == null ? null : mDatagramSocket.getRemoteSocketAddress();
	}

	@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public ParcelFileDescriptor getDatagramSocketFileDescriptor(){
		return ParcelFileDescriptor.fromDatagramSocket(mDatagramSocket);
	}

	public void setReceiveBufferSize(int size){
		try {
			mDatagramSocket.setReceiveBufferSize(size);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSendBufferSize(int size){
		try {
			mDatagramSocket.setSendBufferSize(size);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSoTimeout(int timeout){
		try {
			mDatagramSocket.setSoTimeout(timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect(@NonNull InetAddress inetAddress, @IntRange(from = 0, to = 65535) int portRemote) {
		try {
			mDatagramSocket.connect(new InetSocketAddress(inetAddress, portRemote));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect(){
		try {
			mDatagramSocket.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected(){
		return mDatagramSocket != null && mDatagramSocket.isConnected();
	}

	public void setPacketRemoteIP(String ip) throws UnknownHostException {
		mInetAddressPacketRemote = InetAddress.getByName(ip);
	}

	public void setPacketRemoteIP(InetAddress inetAddress) {
		mInetAddressPacketRemote = inetAddress;
	}

	public InetAddress getPacketRemoteIP(){
		return mInetAddressPacketRemote;
	}

	public void setPacketRemotePort(@IntRange(from = 0, to = 65535) int portRemote){
		mPortPacketRemote = portRemote;
	}

	public int getPacketRemotePort(){
		return mPortPacketRemote;
	}

	private void receivePacketImpl(@NonNull Bundle bundle, @NonNull SocketCallback socketCallback){
		try {
			mDatagramSocket.receive(mDatagramPacketReceive);

			byte[] bytes = mDatagramPacketReceive.getData();
			byte[] bytesCopy = new byte[mDatagramPacketReceive.getLength()];
			System.arraycopy(bytes, mDatagramPacketReceive.getOffset(), bytesCopy, 0, bytesCopy.length);

			bundle.clear();
			bundle.putSerializable("InetAddress", mDatagramPacketReceive.getAddress());
			bundle.putSerializable("InetSocketAddress", mDatagramPacketReceive.getSocketAddress());
			bundle.putInt("port", mDatagramPacketReceive.getPort());
			bundle.putByteArray("data", bytesCopy);

			socketCallback.receiveSync(STATE_RECEIVE_SUCCESS, bundle);
			socketCallback.callbackForMainThread(STATE_RECEIVE_SUCCESS, bundle);
		} catch (Exception e) {
			bundle.clear();
			bundle.putSerializable("Exception", null);
			socketCallback.receiveSync(STATE_RECEIVE_FAILED, bundle);
			socketCallback.callbackForMainThread(STATE_RECEIVE_FAILED, bundle);
		}
	}

	/**
	 * @param packetDataLength receive packet max length.
	 */
	public void receivePacket(int packetDataLength, @NonNull final SocketCallback socketCallback){
		// receive one packet, DatagramPacket must re-new, avoid receiving old packet.
		mDatagramPacketReceive = new DatagramPacket(new byte[packetDataLength], packetDataLength);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bundle bundle;
				if (!isClosed()) {
					bundle = new Bundle();
					receivePacketImpl(bundle, socketCallback);
				}
			}
		}).start();
	}

	/**
	 * @param packetDataLength receive packet max length.
	 */
	public void receivePacketKeep(int packetDataLength, @NonNull final SocketCallback socketCallback){
		mIsKeepReceive = true;
		mDatagramPacketReceive = new DatagramPacket(new byte[packetDataLength], packetDataLength);
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramPacket datagramPacket = mDatagramPacketReceive;
				Bundle bundle;
				while (mIsKeepReceive && datagramPacket == mDatagramPacketReceive && !isClosed()) {
					bundle = new Bundle();
					receivePacketImpl(bundle, socketCallback);
				}
			}
		}).start();
	}

	public void keepReceive(boolean keepReceive){
		mIsKeepReceive = keepReceive;
	}

	public boolean isKeepReceive(){
		return mIsKeepReceive;
	}

	public void sendPacket(byte[] bytes, InetAddress inetAddress, @IntRange(from = 0, to = 65535) int portRemote, final int sendCount, final SocketCallback socketCallback){
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
					socketCallback.sendCallbackSync(isSendSuccess ? STATE_SEND_SUCCESS : STATE_SEND_FAILED);
					socketCallback.callbackForMainThread(isSendSuccess ? STATE_SEND_SUCCESS : STATE_SEND_FAILED, null);
				}
			}
		}).start();
	}

	public void sendPacket(String data, InetAddress inetAddress, @IntRange(from = 0, to = 65535) int portRemote, int sendCount, SocketCallback socketCallback){
		byte[] bytes = data.getBytes();
		sendPacket(bytes, inetAddress, portRemote, sendCount, socketCallback);
	}

	public void sendPacket(byte[] bytes, int sendCount, SocketCallback socketCallback){
		sendPacket(bytes, mInetAddressPacketRemote, mPortPacketRemote, sendCount, socketCallback);
	}

	public void sendPacket(String data, int sendCount, SocketCallback socketCallback){
		byte[] bytes = data.getBytes();
		sendPacket(bytes, mInetAddressPacketRemote, mPortPacketRemote, sendCount, socketCallback);
	}

	public synchronized boolean isClosed(){
		return mDatagramSocket == null || mDatagramSocket.isClosed();
	}

	public synchronized void close(){
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
	}
}