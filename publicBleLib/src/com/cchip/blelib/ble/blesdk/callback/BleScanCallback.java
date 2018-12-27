package com.cchip.blelib.ble.blesdk.callback;

import android.bluetooth.BluetoothDevice;

public interface BleScanCallback {

	void onScanCallback(BluetoothDevice device, int rssi, byte[] scanRecord);
}
