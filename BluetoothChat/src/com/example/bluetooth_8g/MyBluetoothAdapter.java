package com.example.bluetooth_8g;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bluetooth.R;

public class MyBluetoothAdapter extends BaseAdapter {
	private ArrayList<BluetoothDevice> deviceList;
	private Context context;

	public MyBluetoothAdapter(Context context,
			ArrayList<BluetoothDevice> deviceList) {
		this.context = context;
		this.deviceList = deviceList;
	}

	@Override
	public int getCount() {
		return deviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = View.inflate(context, R.layout.device_item, null);
		TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		TextView tv_state = (TextView) view.findViewById(R.id.tv_state);

		tv_address.setText(deviceList.get(position).getAddress());
		tv_name.setText(deviceList.get(position).getName());
		int bondState = deviceList.get(position).getBondState();
		switch (bondState) {
		case BluetoothDevice.BOND_NONE:
			tv_state.setText("未配对");
			tv_state.setTextColor(Color.BLACK);
			break;
		case BluetoothDevice.BOND_BONDING:
			tv_state.setText("正在配对");
			tv_state.setTextColor(Color.RED);
			break;
		case BluetoothDevice.BOND_BONDED:
			tv_state.setText("已配对");
			tv_state.setTextColor(Color.BLUE);
			break;
		default:
			break;
		}

		return view;
	}

}
