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
		// 获取蓝牙适配对象
		defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		// 如果蓝牙状态不可用
		// 打开蓝牙设备
		defaultAdapter.enable();
		// 注册广播
		registBluetoothReceiver();
		// 设置条目点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				try {
					// 获取点击的条目
					BluetoothDevice bluetoothDevice = deviceList.get(position);
					// 未配对，就去配对
					if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
						String address = bluetoothDevice.getAddress();
						// 获取远程设备
						BluetoothDevice remoteDevice = defaultAdapter
								.getRemoteDevice(address);
						// 配对操作
						// 先获取字节码文件对象
						Class<BluetoothDevice> clz = BluetoothDevice.class;
						// 获取方法，这个方法是隐藏的，调不到，随时用反射
						Method method = clz.getMethod("createBond", null);
						// 执行配对该方法
						method.invoke(remoteDevice, null);
						// 如果当前条目的状态，是已经配好对的，就开始聊天
					} else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
						// 跳转界面，开始聊天
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
	 * 注册蓝牙广播
	 */
	private void registBluetoothReceiver() {
		// 定义一个广播接收器
		receiver = new MyBluetoothReceiver();
		// 创建一个意图过滤器
		IntentFilter filter = new IntentFilter();
		// 注册一个搜素到蓝牙的一个意图action
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		// 添加一个action 监听配对状态改变的一个事件
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(receiver, filter);

	}

	public void search(View v) {
		// 搜寻蓝牙设备 已配对和没有配对的设备
		searchBondedDevices();
		// 搜索未配对设备
		searchUnBondedDevices();
		// 展示设备
		setAdapter();
	}

	/**
	 * 搜索未配对设备
	 */
	private void searchUnBondedDevices() {
		new Thread() {
			public void run() {
				// 如果当前正在搜素，先停止，开始本次搜索
				if (defaultAdapter.isDiscovering()) {
					defaultAdapter.cancelDiscovery();
				}
				// 开始搜索，就可以搜索到未配对的设备
				defaultAdapter.startDiscovery();
			};
		}.start();
	}

	/**
	 * 设置数据适配器
	 */
	private void setAdapter() {
		// 如果适配器为空，创建，设置适配器
		if (myBluetoothAdapter == null) {
			myBluetoothAdapter = new MyBluetoothAdapter(this, deviceList);
			listView.setAdapter(myBluetoothAdapter);
		} else {
			// 刷新适配器
			myBluetoothAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * 搜寻已经配对的设备
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
			// 反注册广播
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	// 接收所注册过的action的消息
	class MyBluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取事件类型
			String action = intent.getAction();
			// 获取蓝牙设备
			BluetoothDevice bluetoothDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// 添加到一开始定义的集合中
				if (!deviceList.contains(bluetoothDevice)) {
					deviceList.add(bluetoothDevice);
				}
				// 刷新数据适配器
				setAdapter();
			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				int bondState = bluetoothDevice.getBondState();
				switch (bondState) {
				case BluetoothDevice.BOND_NONE:
					Toast.makeText(MainActivity.this, "配对失败", 0).show();
					break;
				case BluetoothDevice.BOND_BONDING:
					Toast.makeText(MainActivity.this, "正在配对", 0).show();
					break;
				case BluetoothDevice.BOND_BONDED:
					Toast.makeText(MainActivity.this, "配对成功", 0).show();
					// 在集合中移除
					deviceList.remove(bluetoothDevice);
					// 将这个条目重新添加到集合中
					deviceList.add(0, bluetoothDevice);
					// 设置数据适配器
					setAdapter();
					break;
				default:
					break;
				}
			}
		}
	}
}
