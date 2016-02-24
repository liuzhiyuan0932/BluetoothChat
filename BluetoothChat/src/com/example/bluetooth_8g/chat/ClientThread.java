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
	// ͨ��һ���涨�õĴ��ţ�����һ��UUID��
	private static final UUID MY_UUID = UUID.fromString(SPP_UUID);
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	/**
	 * �趨һ�����ֵ������ǿͻ��������Ƿ�ɹ�
	 */
	public static boolean flag = false;

	/**
	 * �ڴ�������ͻ��˵��̵߳�ʱ����Ҫ��Ҫ���ӵ�bluetoothDevice���ݹ���
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
			// ��ȡ�ͻ��˵�socket
			BluetoothSocket socket = bluetoothDevice
					.createRfcommSocketToServiceRecord(MY_UUID);
			// ���ӷ�����------Ҫô�ɹ���Ҫô�쳣
			socket.connect();
			// ���û���׳��쳣���������ӳɹ�������һ����־λ
			flag = true;
			// ���ֽ���ת�����ַ���
			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				Log.i(TAG, "�յ�����˵���Ϣ" + line);
				// �յ���Ϣ�����͸����̣߳���ʾ������ tv_content
				handler.obtainMessage(MyChatActivity.ACCEPT_MESSAGE, line)
						.sendToTarget();
			}
		} catch (IOException e) {
			e.printStackTrace();
			// ����׳��쳣������һ�±�־λ����λ
			flag = false;
			Log.i(TAG, "�ͻ�������ʧ��");
		}
	}

	/**
	 * �ͻ����̷߳�����Ϣ�������
	 * 
	 * @param msg
	 */
	public void sendMessage(String msg) {
		if (bufferedWriter != null) {
			try {
				bufferedWriter.write(msg + "\n");
				Log.i(TAG, "�ͻ��˷�����Ϣ:" + msg);
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "�ͻ���δ����");
			handler.obtainMessage(MyChatActivity.FAILED, "�ͻ���δ����")
					.sendToTarget();
		}
	}
}
