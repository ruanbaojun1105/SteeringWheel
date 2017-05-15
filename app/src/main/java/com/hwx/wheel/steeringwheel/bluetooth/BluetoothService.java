/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwx.wheel.steeringwheel.bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.hwx.wheel.steeringwheel.Application;
import com.hwx.wheel.steeringwheel.BuildConfig;
import com.hwx.wheel.steeringwheel.Constants;
import com.hwx.wheel.steeringwheel.LogUtils;
import com.hwx.wheel.steeringwheel.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothService {
	private static final String TAG = "BluetoothChatService";
	private static final boolean D = true;
	private static final String NAME = "BluetoothChat";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");//???????
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote device

	private Object lock = new Object();

	private static BluetoothService instance = null;
	public static BluetoothService getInstance() {
		if (instance==null){
			synchronized (BluetoothService.class) {
				if (instance==null){
					instance=new BluetoothService();
				}
			}
			}
		return instance;
	}
	public interface DialogItemListener{
		void todosomething(BluetoothDevice item);
	}
	public static Dialog showBluetoothListDialog(final Activity activity, ItemClickAdapter adapter, DialogItemListener onclickInterFace){
		if (activity==null)
			return null;
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.bluttooth_dialog_list);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.transparent)));
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		Display d = activity.getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
		WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); // 获取对话框当前的参数值
		p.width =(int)( d.getWidth()*0.7);
		p.height =(int)( d.getHeight()*0.7);
		dialog.getWindow().setAttributes(p);

		RecyclerView mRecyclerView= (RecyclerView) dialog.findViewById(R.id.recycleview_glass);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setHasFixedSize(true);
		//StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
		LinearLayoutManager mLayoutManager=new LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false);
		mRecyclerView.setLayoutManager(mLayoutManager);

		adapter.openLoadAnimation();
		SpacesItemDecoration decoration = new SpacesItemDecoration(10);
		mRecyclerView.addItemDecoration(decoration);
		mRecyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener((adapter1, view, position) -> {
			BluetoothDevice item= (BluetoothDevice) adapter1.getItem(position);
            if (onclickInterFace!=null){
                if (activity!=null)
                    activity.runOnUiThread(() -> onclickInterFace.todosomething(item));
            }
            dialog.dismiss();
        });
		dialog.show();
		return dialog;
	}

	public BluetoothService() {
		mState = STATE_NONE;
	}

	private synchronized void setState(int state) {
		if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		Bundle bundle = new Bundle();
		bundle.putInt("state", state);
		Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_STATE,bundle);
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "start");

		// Cancel any thread currently running a connection
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mAcceptThread = new AcceptThread(device);
		mAcceptThread.start();
		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		// Cancel any thread currently running a connection
		start(device);
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,BluetoothDevice device) {
		if (D)Log.d(TAG, "connected");
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		Bundle bundle = new Bundle();
		bundle.putString("name", device.getName());
		bundle.putString("address",device.getAddress());
		Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_NAME,bundle);
		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_NONE);
	}
	public boolean write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		try {
			if (r!=null) {
				r.write(out);
				LogUtils.e("蓝牙命令发送成功 ");
			}else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e("蓝牙命令发送失败 ");
			return  false;
		}
		return true;
	}
	public static final int safeCode = 0x10;//校验码
	final static byte[] endCode = new byte[]{0x0c, 0x0d};//结束符
	static final int starCode = 0x3a;//头
	static final int addrCode = 0x01;//地址
	public void sendData(byte[] data) {
		write(data);
	}
	/**
	 *发送数据
	 */
	public void sendData(/*SerialPort mSerialPort , OutputStream mOutputStream, */byte function, String data, boolean isAutoSafeCode) {
		// TODO Auto-generated method stub
		sendData(function,data.getBytes(),isAutoSafeCode);
	}
	/**
	 *发送数据
	 */
	public void sendData(byte function,byte[] content,boolean isAutoSafeCode) {
		isAutoSafeCode=true;//所有都自动算出来
		byte[] head=new byte[]{starCode,addrCode,function,(byte) (content.length/256),(byte) (content.length%256)};
		byte safe=safeCode;
		if (isAutoSafeCode)
			safe=checkSafeCod(content);
		byte[] end=new byte[]{safe,endCode[0],endCode[1]};
		List<byte[]> list=new ArrayList<byte[]>();
		list.add(head);
		list.add(content);
		list.add(end);

		byte[] a=sysCopy(list);
		write(a);
		Log.e("dd:","number code ："+function+" 's data send is "+mConnectedThread==null?"error!":"ok!");
	}
	public static byte[] reData(byte function,byte[] content,boolean isAutoSafeCode) {
		isAutoSafeCode=true;//所有都自动算出来
		byte[] head=new byte[]{starCode,addrCode,function,(byte) (content.length/256),(byte) (content.length%256)};
		byte safe=safeCode;
		if (isAutoSafeCode)
			safe=checkSafeCod(content);
		byte[] end=new byte[]{safe,endCode[0],endCode[1]};
		List<byte[]> list=new ArrayList<byte[]>();
		list.add(head);
		list.add(content);
		list.add(end);

		byte[] a=sysCopy(list);
		return a;
	}
	public static byte checkSafeCod(byte[] data){
		byte safeCode=0;
		for(byte at:data){
			safeCode^=at;
		}
		return safeCode;
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		setState(STATE_LISTEN);
		Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL);
	}
	/**
	 * 创建新的线程用于和对端的蓝牙设备建立连接。
	 */
	private class AcceptThread extends Thread {
		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public AcceptThread(BluetoothDevice device) {
			BluetoothSocket socket = null;
			mmDevice = device;
			try {
				socket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {

			}
			mmSocket = socket;
		}
		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				if (null != mmSocket) {
					mmSocket.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
		public void run() {

			try {
				mmSocket.connect();
			} catch (IOException e) {
				try {
					mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket",
							new Class[]{int.class}).invoke(mmDevice, Integer.valueOf(1));
				} catch (IllegalAccessException e1) {
                    connectionFailed();
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
                    connectionFailed();
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
                    connectionFailed();
					e1.printStackTrace();
				}
				try {
					mmSocket.connect();
					if (BuildConfig.DEBUG) Log.d(TAG, "connected");
				} catch (IOException e1) {
					Log.e(TAG, "connect to bluetooth device failed");
					try {
						mmSocket.close();
                        connectionFailed();
					} catch (IOException e2) {
					}
					return;
				}
			}
			connected(mmSocket, mmSocket.getRemoteDevice());
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		int fact_size=0;
		byte[] data_buffer=new byte[0];

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[64];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				synchronized (lock) {
					try {
						Log.e("bt", "read:" + StringHexUtils.hexStr2Str(StringHexUtils.Bytes2HexString(buffer)));
						// Read from the InputStream
						bytes = mmInStream.read(buffer);
						fact_size+=bytes;
						if (bytes==0&&fact_size>0){//clear
							initNumber();
						}
						try {
							if (bytes>0) {
								byte[] b= Arrays.copyOfRange(buffer, 0, bytes);
								data_buffer= byteMerger(data_buffer,b);
								//write(b);
								if (checkDataHead(data_buffer)){
									initNumber();
								}else if (getNumberData(fact_size,data_buffer)){//clear
									initNumber();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						Log.e(TAG, "disconnected", e);
						connectionLost();
						break;
					}
				}
			}
		}
		void initNumber(){
			data_buffer=new byte[0];
			fact_size=0;
			System.gc();
		}
		/**
		 * 十六进制接收解析
		 * @param size
		 * @param buffer
		 * 3a 01 05 00 03 01 02 03 10 0c 0d
		 */

		private boolean getNumberData(int size,byte[] buffer) {
			byte numberNo = 0;//功能编码
			int count_data = 0;//数据长度
			if (size > 8 ) {//一段数据至少9位
				if (addrCode != buffer[1])
					return true;//抛弃这段
				count_data = (int) buffer[3] * 256 + buffer[4];
				int a = 5 + count_data;
				if (a+2>=size)
					return false;
				numberNo = buffer[2];

				LogUtils.e("resolve :"+numberNo);
				if (buffer[a + 1] == endCode[0] && buffer[a + 2] == endCode[1]) {
					byte[] content= Arrays.copyOfRange(buffer, 5, a);
					byte sa=checkSafeCod(content);
					if (numberNo==0x05){
						sa=0x01;
					}
					sendData((byte) 0xff,new byte[]{0x00,0x00},true);//收到消息返回一条

					Bundle bundle1=new Bundle();
					bundle1.putByteArray("data",content);
					bundle1.putByte("numberNo",numberNo);
					bundle1.putByte("safeCode",buffer[a]);
					Application.sendLocalBroadCast(Constants.SERIAL_PORT_COMMAND,bundle1);
                /*if (sa==buffer[a]) {//测试暂时不开验证
                    onDataReceived(content, numberNo, buffer[a]);
                    return true;
                }*/
				}
				return true;
			}
			return  false;
		}
		boolean checkDataHead(byte[] buffer){
			if (buffer[0] == starCode)
				return false;
        /*else {
            //此处可以继续判断，但一般不会有这样情况
            for (int i = 0; i <buffer.length ; i++) {
                if (buffer[i] == starCode){
                    data_buffer=Arrays.copyOfRange(buffer, i, buffer.length);
                    return false;
                }
            }
        }*/
			return true;
		}

		/**
		 * Write to the connected OutStream.
		 *
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				if (mmOutStream!=null) {
					mmOutStream.write(buffer);
					Log.i("bt", new String(buffer));
				}
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}
		public void sendData(byte[] data) {
			write(data);
		}
		/**
		 *发送数据
		 */
		public void sendData(/*SerialPort mSerialPort , OutputStream mOutputStream, */byte function, String data, boolean isAutoSafeCode) {
			// TODO Auto-generated method stub
			sendData(function,data.getBytes(),isAutoSafeCode);
		}
		/**
		 *发送数据
		 */
		public void sendData(byte function,byte[] content,boolean isAutoSafeCode) {
			isAutoSafeCode=true;//所有都自动算出来
			byte[] head=new byte[]{starCode,addrCode,function,(byte) (content.length/256),(byte) (content.length%256)};
			byte safe=safeCode;
			if (isAutoSafeCode)
				safe=checkSafeCod(content);
			byte[] end=new byte[]{safe,endCode[0],endCode[1]};
			List<byte[]> list=new ArrayList<byte[]>();
			list.add(head);
			list.add(content);
			list.add(end);

			byte[] a=sysCopy(list);
			write(a);
			Log.e("dd:","number code ："+function+" 's data send is "+mmOutStream==null?"error!":"ok!");
		}
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
	//java 合并两个byte数组
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
		byte[] byte_3 = new byte[byte_1.length+byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}
	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray:srcArrays) {
			len+= srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray:srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}
}
