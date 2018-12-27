package com.cchip.blelib.ble.bleapi;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import com.cchip.blelib.ble.blesdk.BleSdk;
import com.cchip.blelib.ble.blesdk.callback.ConnectStateCallback;

public class AutoReConnect implements ConnectStateCallback {

	private BlePublicApi mBleApi;
	private Set<String> autoConnectSet = new HashSet<String>();

	private static final int RECONNECTCOUNTSMAX = 5;
	private int reConnectCounts = 0;

	public AutoReConnect(BlePublicApi blePublicApi) {
		// TODO Auto-generated constructor stub
		mBleApi = blePublicApi;
	}

	@Override
	public void onConnectStateCallback(final String addr, int state) {
		// TODO Auto-generated method stub
		switch (state) {
		case BleSdk.CONNECTING:

			break;
		case BleSdk.CONNECTED:
			reConnectCounts = 0;
			break;

		case BleSdk.CONNECT_TIMEOUT:
			reConnectCounts++;
			if (reConnectCounts == RECONNECTCOUNTSMAX) {
				Intent intent = new Intent();
				intent.setAction(Constant.ACTION_BLERECONNECT_MAX);
				mBleApi.sendBroadcast(intent);
				reConnectCounts = 0;
			}
			if (needAutoConnect(addr)) {
				mBleApi.connect(addr);
			}
			break;

		case BleSdk.DISCONNECTING:

			break;
		case BleSdk.DISCONNECTED:
			reConnectCounts = 0;
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (needAutoConnect(addr)) {
						mBleApi.connect(addr);
					}
				}
			}, 500);
			break;

		case BleSdk.DISCONNECT_TIMEOUT:
			if (needAutoConnect(addr)) {
				mBleApi.connect(addr);
			}
			break;

		case BleSdk.DISCOVERY_SERVICE_ING:

			break;

		case BleSdk.DISCOVERY_SERVICE_OK:

			break;

		case BleSdk.DISCOVERY_SERVICE_FAIL:

			break;

		case BleSdk.CONNECT_ERROR_NEEDTO_CLOSE_BLE:

			break;
		case BleSdk.CONNECT_SCAN_NOT_FOUND:
			if (needAutoConnect(addr)) {
				mBleApi.connect(addr);
			}
			break;
		default:
			break;
		}
	}

	boolean needAutoConnect(String addr) {

		return autoConnectSet.contains(addr);
	}

	void autoConnect() {
		if (autoConnectSet.size() > 0) {
			for (String macaddr : autoConnectSet) {
				mBleApi.connect(macaddr);
			}
		}
	}

	void addAutoConnectSet(String addr){
		autoConnectSet.add(addr);
	}
	
	void removeAutoConnectSet(String addr){
		autoConnectSet.remove(addr);
	}
	
	void clearAutoConnectSet(){
		autoConnectSet.clear();
	}
}
