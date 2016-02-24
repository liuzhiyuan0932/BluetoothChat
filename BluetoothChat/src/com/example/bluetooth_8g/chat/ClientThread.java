package com.example.bluetooth_8g.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ClientThread extends Thread {
	private BluetoothDevice bluetoothDevice;
	private Handler handler;
	private static final String TAG = "Chat";
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	// 通过一个规定好的串号，生成一个UUID号
	private static final UUID MY_UUID = UUID.fromString(SPP_UUID);
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	/**
	 * 设定一个标记值，来标记客户端连接是否成功
	 */
	public static boolean flag = false;

	/**
	 * 在创建这个客户端的线程的时候，需要将要连接的bluetoothDevice传递过来
	 * 
	 * @param bluetoothDevice
	 * @param handler
	 */
	public ClientThread(BluetoothDevice bluetoothDevice, Handler handler) {
		this.handler = handler;
		this.bluetoothDevice = bluetoothDevice;
	}

	@Override
	public void run() {
		super.run();
		try {
			// 获取客户端的socket
			BluetoothSocket socket = bluetoothDevice
					.createRfcommSocketToServiceRecord(MY_UUID);
			// 连接服务器------要么成功，要么异常
			socket.connect();
			// 如果没有抛出异常，代表连接成功，进行一个标志位
			flag = true;
			// 将字节流转换成字符流
			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				Log.i(TAG, "收到服务端的消息" + line);
				// 收到消息，发送给主线程，显示界面上 tv_content
				handler.obtainMessage(MyChatActivity.ACCEPT_MESSAGE, line)
						.sendToTarget();
			}
		} catch (IOException e) {
			e.printStackTrace();
			// 如果抛出异常，进行一下标志位的置位
			flag = false;
			Log.i(TAG, "客户端连接失败");
		}
	}

	/**
	 * 客户端线程发送消息给服务端
	 * 
	 * @param msg
	 */
	public void sendMessage(String msg) {
		if (bufferedWriter != null) {
			try {
				bufferedWriter.write(msg + "\n");
				Log.i(TAG, "客户端发送消息:" + msg);
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "客户端未连接");
			handler.obtainMessage(MyChatActivity.FAILED, "客户端未连接")
					.sendToTarget();
		}
	}
}
