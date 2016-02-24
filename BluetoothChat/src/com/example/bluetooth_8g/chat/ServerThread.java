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
	// ͨ��һ���涨�õĴ��ţ�����һ��UUID��
	UUID MY_UUID = UUID.fromString(SPP_UUID);
	/**
	 * ����˵�socket
	 */
	private BluetoothServerSocket serverSocket;

	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private Handler handler;

	/**
	 * ������߳�
	 * 
	 * @param handler
	 *            handler�������ڷ�����Ϣ�����߳�
	 * @param bluetoothAdapter
	 */
	public ServerThread(Handler handler, BluetoothAdapter bluetoothAdapter) {
		try {
			this.handler = handler;
			// ͨ��BluetoothAdapter����һ������ˣ�NAME���Լ����� UUID��Ҫ����һ������ȥ����
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
				// ����
				BluetoothSocket socket = serverSocket.accept();
				// ������ܵ�����һ���ͻ��ˣ��ͻ�ȡ���������
				bufferedReader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					Log.i(TAG, "�յ��ͻ��˵���Ϣ" + line);
					// �յ���Ϣ�����͸����̣߳���ʾ������ tv_content
					handler.obtainMessage(MyChatActivity.ACCEPT_MESSAGE, line)
							.sendToTarget();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����˷�����Ϣ���ͻ���
	 * 
	 * @param msg
	 */
	public void sendMessage(String msg) {
		if (bufferedWriter != null) {
			try {
				//������Ϣ��������ˢ��
				bufferedWriter.write(msg + "\n");
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "�����û�л�ȡ���κεĿͻ���");
			handler.obtainMessage(MyChatActivity.FAILED, "�����û�л�ȡ���κεĿͻ���")
					.sendToTarget();
		}
	}
}
