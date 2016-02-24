package com.example.bluetooth_8g.chat;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.R;

public class MyChatActivity extends Activity {

	private EditText et_content;
	private LinearLayout ll_content;
	private String address;
	public static final int ACCEPT_MESSAGE = 1;
	public static final int FAILED = 2;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == ACCEPT_MESSAGE) {
				// չʾ������
				String message = (String) msg.obj;
				if (!TextUtils.isEmpty(message)) {
					addSheContent(message);
				}
			} else if (msg.what == FAILED) {
				String message = (String) msg.obj;
				Toast.makeText(MyChatActivity.this, message, 0).show();
			}
		};
	};
	private BluetoothAdapter defaultAdapter;
	private ClientThread clientThread;
	private ServerThread serverThread;
	private BluetoothDevice remoteDevice;
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		TextView tv_address = (TextView) findViewById(R.id.tv_address);
		et_content = (EditText) findViewById(R.id.et_content);
		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		// ��ȡҪ��������mac��ַ
		address = getIntent().getStringExtra("address");
		tv_address.setText(address);
		// ��ȡdefaultAdapter
		defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		// ���������
		serverThread = new ServerThread(handler, defaultAdapter);
		// ����������̣߳��ȴ����տͻ���
		serverThread.start();
		// ��ȡҪ���ӵ������豸
		remoteDevice = defaultAdapter.getRemoteDevice(address);
		// һ��ʼ�ͽ������ӷ��������������ʧ�ܣ������ֶ�ȥ��
		connect();

	}

	public void send(View v) {
		String msg = et_content.getText().toString().trim();
		if (ClientThread.flag) {
			// ��Ϊ�ͻ��˴���
			clientThread.sendMessage(msg);
		} else {
			serverThread.sendMessage(msg);
		}
		addMeContent(msg);
	}

	/**
	 * �����ť���Բ��ֶ����ӣ�����������ʧ�ܵ�ʱ�򣬽����ֶ����ӣ���������
	 * 
	 * @param v
	 */
	public void connect(View v) {
		connect();
	}

	/**
	 * ����
	 */
	private void connect() {
		// �����ͻ����߳�
		clientThread = new ClientThread(remoteDevice, handler);
		clientThread.start();
	}

	// �ṩһ������
	public void addMeContent(String content) {
		View view = View.inflate(MyChatActivity.this,
				R.layout.list_say_me_item, null);
		TextView tv_me_time = (TextView) view.findViewById(R.id.tv_me_time);

		TextView tv_me_content = (TextView) view
				.findViewById(R.id.tv_me_content);
		tv_me_time.setText(getTime());
		tv_me_content.setText(content);
		scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		// ���������Ĳ��֣���ӵ�
		ll_content.addView(view);

	}

	// �ṩһ������,��ӱ���˵�Ļ�
	public void addSheContent(String content) {
		View view = View.inflate(MyChatActivity.this,
				R.layout.list_say_she_item, null);
		TextView tv_she_time = (TextView) view.findViewById(R.id.tv_she_time);

		TextView tv_she_content = (TextView) view
				.findViewById(R.id.tv_she_content);
		tv_she_time.setText(getTime());
		tv_she_content.setText(content);
		scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		// ���������Ĳ��֣���ӵ�
		ll_content.addView(view);

	}

	/**
	 * ��ȡ��ʽ��ʱ��
	 * 
	 * @return
	 */
	public String getTime() {
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String format = dateFormat.format(currentTimeMillis);
		return format;
	}
}
