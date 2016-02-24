package com.example.bluetooth_8g;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetooth.R;
import com.example.bluetooth_8g.chat.MyChatActivity;

public class MainActivity extends Activity {

	private ListView listView;
	private BluetoothAdapter defaultAdapter;
	private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	private MyBluetoothAdapter myBluetoothAdapter;
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listView);
		// ��ȡ�����������
		defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		// �������״̬������
		// �������豸
		defaultAdapter.enable();
		// ע��㲥
		registBluetoothReceiver();
		// ������Ŀ����¼�
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				try {
					// ��ȡ�������Ŀ
					BluetoothDevice bluetoothDevice = deviceList.get(position);
					// δ��ԣ���ȥ���
					if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
						String address = bluetoothDevice.getAddress();
						// ��ȡԶ���豸
						BluetoothDevice remoteDevice = defaultAdapter
								.getRemoteDevice(address);
						// ��Բ���
						// �Ȼ�ȡ�ֽ����ļ�����
						Class<BluetoothDevice> clz = BluetoothDevice.class;
						// ��ȡ������������������صģ�����������ʱ�÷���
						Method method = clz.getMethod("createBond", null);
						// ִ����Ը÷���
						method.invoke(remoteDevice, null);
						// �����ǰ��Ŀ��״̬�����Ѿ���öԵģ��Ϳ�ʼ����
					} else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
						// ��ת���棬��ʼ����
						Intent intent = new Intent(MainActivity.this,
								MyChatActivity.class);
						intent.putExtra("address", bluetoothDevice.getAddress());
						startActivity(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * ע�������㲥
	 */
	private void registBluetoothReceiver() {
		// ����һ���㲥������
		receiver = new MyBluetoothReceiver();
		// ����һ����ͼ������
		IntentFilter filter = new IntentFilter();
		// ע��һ�����ص�������һ����ͼaction
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		// ���һ��action �������״̬�ı��һ���¼�
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(receiver, filter);

	}

	public void search(View v) {
		// ��Ѱ�����豸 ����Ժ�û����Ե��豸
		searchBondedDevices();
		// ����δ����豸
		searchUnBondedDevices();
		// չʾ�豸
		setAdapter();
	}

	/**
	 * ����δ����豸
	 */
	private void searchUnBondedDevices() {
		new Thread() {
			public void run() {
				// �����ǰ�������أ���ֹͣ����ʼ��������
				if (defaultAdapter.isDiscovering()) {
					defaultAdapter.cancelDiscovery();
				}
				// ��ʼ�������Ϳ���������δ��Ե��豸
				defaultAdapter.startDiscovery();
			};
		}.start();
	}

	/**
	 * ��������������
	 */
	private void setAdapter() {
		// ���������Ϊ�գ�����������������
		if (myBluetoothAdapter == null) {
			myBluetoothAdapter = new MyBluetoothAdapter(this, deviceList);
			listView.setAdapter(myBluetoothAdapter);
		} else {
			// ˢ��������
			myBluetoothAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * ��Ѱ�Ѿ���Ե��豸
	 */
	private void searchBondedDevices() {
		Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
		for (BluetoothDevice bluetoothDevice : bondedDevices) {
			if (!deviceList.contains(bluetoothDevice))
				deviceList.add(bluetoothDevice);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			// ��ע��㲥
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	// ������ע�����action����Ϣ
	class MyBluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// ��ȡ�¼�����
			String action = intent.getAction();
			// ��ȡ�����豸
			BluetoothDevice bluetoothDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// ��ӵ�һ��ʼ����ļ�����
				if (!deviceList.contains(bluetoothDevice)) {
					deviceList.add(bluetoothDevice);
				}
				// ˢ������������
				setAdapter();
			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				int bondState = bluetoothDevice.getBondState();
				switch (bondState) {
				case BluetoothDevice.BOND_NONE:
					Toast.makeText(MainActivity.this, "���ʧ��", 0).show();
					break;
				case BluetoothDevice.BOND_BONDING:
					Toast.makeText(MainActivity.this, "�������", 0).show();
					break;
				case BluetoothDevice.BOND_BONDED:
					Toast.makeText(MainActivity.this, "��Գɹ�", 0).show();
					// �ڼ������Ƴ�
					deviceList.remove(bluetoothDevice);
					// �������Ŀ������ӵ�������
					deviceList.add(0, bluetoothDevice);
					// ��������������
					setAdapter();
					break;
				default:
					break;
				}
			}
		}
	}
}
