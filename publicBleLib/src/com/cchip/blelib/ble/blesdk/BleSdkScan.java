package com.cchip.blelib.ble.blesdk;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;





import com.cchip.blelib.ble.blesdk.callback.BleScanCallback;

import android.R.anim;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

public class BleSdkScan {

	private static final String TAG = "BleSdkScan";
	
	
	BluetoothAdapter mBluetoothAdapter;
	int mScanState = BleSdk.SCAN_IDLE;
	BleScanCallback mScanCallback;
	BleSdk mBleSdk;
	
	
	BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			if(mScanCallback == null){
				Log.e(TAG, "mScanCallback is null");
				return;
			}
//			Log.e(TAG, "name ="+device.getName());
//			if(scanRecord!= null){
//				Log.e(TAG,"mac = "+ device.getAddress());
//				Log.e(TAG, " onLeScanbyte:"+byteArrayToString(scanRecord));
//				Log.e(TAG, " onLeScan:"+parseFromBytes(scanRecord));
//			}
			mScanCallback.onScanCallback(device, rssi, scanRecord);
		}
	};
	
	
	public BleSdkScan(BleSdk bleSdk){
		mBleSdk = bleSdk;
	}
	
	
	public void  init(BluetoothAdapter bluetoothAdapter){
		mBluetoothAdapter = bluetoothAdapter;
		mScanState = BleSdk.SCAN_IDLE;
	}
	
	public int getScanState(){
		return mScanState;
	}
	
	public void setScanState(int state){
		 mScanState = state;
		 
	}
	
	protected void setScanCallback(BleScanCallback scanCallback){
		mScanCallback = scanCallback;
	}
	

//	public int startScan(){
//		if(mScanState == BleSdk.SCANNING){
//			Log.e(TAG, "ALREADY_SCAN_START");
//			return BleSdk.ALREADY_SCAN_START;
//		}
//		
//		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() ){
//			Log.e(TAG, "FAIL_ADAPTER");
//			return BleSdk.FAIL_ADAPTER;
//		}
//		
//		if(!mBluetoothAdapter.startLeScan(mLeScanCallback)){
//			Log.e(TAG, "FAIL");
//			return BleSdk.FAIL;
//		}
//		mScanState = BleSdk.SCANNING;
//		return BleSdk.SUCCESS;
//	}
	
	//wch 2017.3.17    add synchronized  to avoid scancallback  not return data
	public synchronized int startScan(UUID[] serviceUUID){

		
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() ){
			Log.e(TAG, "FAIL_ADAPTER");
			mScanState = BleSdk.SCAN_IDLE;
			return BleSdk.FAIL_ADAPTER;
		}
		
		if(mScanState == BleSdk.SCANNING){
			Log.e(TAG, "ALREADY_SCAN_START");
			return BleSdk.ALREADY_SCAN_START;
		}
		
		if(!mBluetoothAdapter.startLeScan(serviceUUID,mLeScanCallback)){
			Log.e(TAG, "FAIL");
			stopScan();
			return BleSdk.FAIL;
		}
		mScanState = BleSdk.SCANNING;
		return BleSdk.SUCCESS;
	}
	
	
	public int stopScan(){

		
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() ){
			Log.e(TAG, "FAIL_ADAPTER");
			if(mBluetoothAdapter == null){
				Log.e(TAG, "mBluetoothAdapter == null");
			}
			return BleSdk.FAIL_ADAPTER;
		}
		
//		if(mScanState == BleSdk.SCAN_IDLE){
//			Log.e(TAG, "ALREADY_SCAN_STOP");
//			return BleSdk.ALREADY_SCAN_STOP;
//		}
		
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		mScanState = BleSdk.SCAN_IDLE;
		return BleSdk.SUCCESS;
	}
	
	
	private String byteArrayToString(byte[] bytes){
		String b = "";
		for (int i = 0; i < bytes.length; i++){
		       b += Integer.toHexString(bytes[i] & 0xff) + "  ";
		}
	    return b;
	}
	
	private String byteArrayToCharString(byte[] bytes){
		String b = "";
		for (int i = 0; i < bytes.length; i++){
		       b += (char)bytes[i]  + "  ";
		}
	    return b;
	}
	
	public String parseFromBytes(byte[] scanRecord) {

        String result = "";
        if (scanRecord == null) {
            return "";
        }

        int currentPos = 0;

        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = scanRecord[currentPos++] & 0xFF;
//                if(fieldType == (0xfe)){
//                	if(scanRecord[currentPos] == 0x53 && scanRecord[currentPos+1] == 0x4e){
//                		for(int i =0;i<dataLength;i++){
//                			result += Integer.toHexString(scanRecord[currentPos+i] & 0xff);
//                		}
//                		Log.e(TAG, "sn:"+result);
//                		break;
//                	}
//                } 
                
              if(fieldType == (0xff)){
            	int tempCurrentPos = currentPos;
            	
            	while(tempCurrentPos+3 < dataLength){
            		if(scanRecord[tempCurrentPos] == 0x0F
            				&& scanRecord[tempCurrentPos+1] == (byte)0xfe
            				&& scanRecord[tempCurrentPos+2] == 0x53
            				&& scanRecord[tempCurrentPos+3] == 0x4e){
            			for(int i =0;i<scanRecord[tempCurrentPos]-1;i++){
                			result += Integer.toHexString(scanRecord[tempCurrentPos+2+i] & 0xff);
                		}
                		Log.e(TAG, "sn:"+result);
                		break;
            		}
            		tempCurrentPos++;
            	}
            	
//            	if(scanRecord[currentPos] == 0x53 && scanRecord[currentPos+1] == 0x4e){
//            		for(int i =0;i<dataLength;i++){
//            			result += Integer.toHexString(scanRecord[currentPos+i] & 0xff);
//            		}
//            		Log.e(TAG, "sn:"+result);
//            		break;
//            	}
              } 
                	
                currentPos += dataLength;
            }
            return result;

        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return "";
        }
    }
}
