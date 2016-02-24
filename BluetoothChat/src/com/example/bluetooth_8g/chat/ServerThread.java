package com.example.bluetooth_8g.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ServerThread extends Thread {
	private static final String TAG = "Chat";
	private static final String NAME = "Chat";
	String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	// 通过一个规定好的串号，生成一个UUID号
	UUID MY_UUID = UUID.fromString(SPP_UUID);
	/**
	 * 服务端的socket
	 */
	private BluetoothServerSocket serverSocket;

	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private Handler handler;

	/**
	 * 服务端线程
	 * 
	 * @param handler
	 *            handler对象，用于发送消息给主线程
	 * @param bluetoothAdapter
	 */
	public ServerThread(Handler handler, BluetoothAdapter bluetoothAdapter) {
		try {
			this.handler = handler;
			// 通过BluetoothAdapter创建一个服务端，NAME是自己定义 UUID需要根据一个串号去生成
			serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
					NAME, MY_UUID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();
		while (true) {
			try {
				// 阻塞
				BluetoothSocket socket = serverSocket.accept();
				// 如果接受到这样一个客户端，就获取输入输出流
				bufferedReader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					Log.i(TAG, "收到客户端的消息" + line);
					// 收到消息，发送给主线程，显示界面上 tv_content
					handler.obtainMessage(MyChatActivity.ACCEPT_MESSAGE, line)
							.sendToTarget();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 服务端发送消息给客户端
	 * 
	 * @param msg
	 */
	public void sendMessage(String msg) {
		if (bufferedWriter != null) {
			try {
				//发送消息，并进行刷新
				bufferedWriter.write(msg + "\n");
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "服务端没有获取到任何的客户端");
			handler.obtainMessage(MyChatActivity.FAILED, "服务端没有获取到任何的客户端")
					.sendToTarget();
		}
	}
}
