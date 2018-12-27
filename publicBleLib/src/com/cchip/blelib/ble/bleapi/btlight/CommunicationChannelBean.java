package com.cchip.blelib.ble.bleapi.btlight;

import android.bluetooth.BluetoothGattCharacteristic;

public class CommunicationChannelBean {

//	boolean mNotificationSuccess;
	BluetoothGattCharacteristic lightColorWriteCharacteristic;
	BluetoothGattCharacteristic timeSyncWriteCharacteristic;
	BluetoothGattCharacteristic alarmWriteReadCharacteristic;
	
	public CommunicationChannelBean(BluetoothGattCharacteristic lightColorWriteCharacteristic,
								BluetoothGattCharacteristic timeSyncWriteCharacteristic,
								BluetoothGattCharacteristic alarmWriteReadCharacteristic) {
		// TODO Auto-generated constructor stub
		this.lightColorWriteCharacteristic = lightColorWriteCharacteristic;
		this.timeSyncWriteCharacteristic = timeSyncWriteCharacteristic;
		this.alarmWriteReadCharacteristic = alarmWriteReadCharacteristic;
	}

	public BluetoothGattCharacteristic getLightColorWriteCharacteristic() {
		return lightColorWriteCharacteristic;
	}

	public void setLightColorWriteCharacteristic(
			BluetoothGattCharacteristic lightColorWriteCharacteristic) {
		this.lightColorWriteCharacteristic = lightColorWriteCharacteristic;
	}

	public BluetoothGattCharacteristic getTimeSyncWriteCharacteristic() {
		return timeSyncWriteCharacteristic;
	}

	public void setTimeSyncWriteCharacteristic(
			BluetoothGattCharacteristic timeSyncWriteCharacteristic) {
		this.timeSyncWriteCharacteristic = timeSyncWriteCharacteristic;
	}

	public BluetoothGattCharacteristic getAlarmWriteReadCharacteristic() {
		return alarmWriteReadCharacteristic;
	}

	public void setAlarmWriteReadCharacteristic(
			BluetoothGattCharacteristic alarmWriteReadCharacteristic) {
		this.alarmWriteReadCharacteristic = alarmWriteReadCharacteristic;
	}
	
	
}
