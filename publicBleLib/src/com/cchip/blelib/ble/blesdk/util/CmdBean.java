package com.cchip.blelib.ble.blesdk.util;

import java.util.ArrayList;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * @ClassName: CmdBean
 * @Description: Command send to pedometer 
 * @author wch
 * @date 2016骞�4鏈�14鏃� 涓嬪崍1:47:55
 * 
 */
public class CmdBean {

	BluetoothGatt bluetoothGatt;

	String macAddr;

	BluetoothGattCharacteristic writeCharacteristic;

	ArrayList<byte[]> data;

	/**
	 * 
	* <p>Title: </p> 
	* <p>Description: constructor</p> 
	* @param macAddr                    device mac
	* @param bluetoothGatt              gatt to write
	* @param writeCharacteristic        characteristic to write
	* @param data                       cmd
	 */
	
	public CmdBean(String macAddr,BluetoothGatt bluetoothGatt,
			BluetoothGattCharacteristic writeCharacteristic,
			ArrayList<byte[]> data) {
		// TODO Auto-generated constructor stub
		this.macAddr = macAddr;
		this.bluetoothGatt = bluetoothGatt;
		this.writeCharacteristic = writeCharacteristic;
		this.data = data;
	}	
	
	
	public BluetoothGatt getBluetoothGatt() {
		return bluetoothGatt;
	}

	public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
		this.bluetoothGatt = bluetoothGatt;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}

	public BluetoothGattCharacteristic getWriteCharacteristic() {
		return writeCharacteristic;
	}

	public void setWriteCharacteristic(
			BluetoothGattCharacteristic writeCharacteristic) {
		this.writeCharacteristic = writeCharacteristic;
	}

	public ArrayList<byte[]> getData() {
		return data;
	}

	public void setData(ArrayList<byte[]> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "macAddr:" + macAddr + "\r\n" + "data:" + dataToString(data)
				+ "\r\n" + "writeCharacteristic:" + writeCharacteristic + "\r\n"
				+ "bluetoothGatt:" + bluetoothGatt + "\r\n";
	}

	private String dataToString(ArrayList<byte[]> data) {
		if (data == null || data.size() == 0) {
			return "";
		}

		String str = "";
		for (int i = 0; i < data.size(); i++) {
			str += byteArrayToString(data.get(i)) + "\r\n";
		}

		return str;
	}

	private String byteArrayToString(byte[] bytes) {
		String b = "";
		for (int i = 0; i < bytes.length; i++) {
			b += Integer.toHexString(bytes[i] & 0xff) + "  ";
		}
		return b;
	}
}
