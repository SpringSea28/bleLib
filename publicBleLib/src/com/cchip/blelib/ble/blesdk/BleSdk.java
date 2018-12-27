package com.cchip.blelib.ble.blesdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;





import com.cchip.blelib.ble.blesdk.callback.BleScanCallback;
import com.cchip.blelib.ble.blesdk.callback.BluethoothAdapterStateChangCallback;
import com.cchip.blelib.ble.blesdk.callback.ReceiveDataCallback;
import com.cchip.blelib.ble.blesdk.callback.ReliableWriteDataCallback;
import com.cchip.blelib.ble.blesdk.callback.WriteDataCallback;
import com.cchip.blelib.ble.blesdk.util.ConnectInfo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BleSdk {
	
    private static final String TAG = "BleSdk";  
    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int FAIL_ADAPTER = 2;
    public static final int FAIL_PARAMETER  = 3; // PARAMETER error
    public static final int ALREADY_SCAN_START = 4;
    public static final int ALREADY_SCAN_STOP = 5;
    public static final int ALREDY_CONNECT =6;
    public static final int ALREADY_DISCONNECT = 7;
    public static final int MAX_CONNECT = 8;
	
	//scan state
    public static final int SCAN_IDLE = 0;
    public static final int SCANNING = 1;
	
	//connect state
    public static final int CONNECT_IDLE = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int CONNECT_TIMEOUT = 3;
    public static final int DISCONNECTING = 4;
    public static final int DISCONNECTED = 5;
    public static final int DISCONNECT_TIMEOUT = 6;
    public static final int DISCOVERY_SERVICE_ING = 7;
    public static final int DISCOVERY_SERVICE_OK = 8;
    public static final int DISCOVERY_SERVICE_FAIL = 9;
    public static final int COMMUNICATE_SUCCESS = 10;
    public static final int COMMUNICATE_FAIL = 11;
    public static final int CONNECT_ERROR_NEEDTO_CLOSE_BLE = 12;
    public static final int CONNECT_SCAN_NOT_FOUND = 13;
    public static final int OTHER = 14;
	
    public static final int MAX_CONNECT_SIZE = 8;
    
    public static final int ADD_LIST_SUCCESS = 0;
    public static final int ADD_LIST_FAIL_MAX_SIZE = 1;
    public static final int ADD_LIST_FAIL_EXIST_CONNECTINOF = 2;
    
    public static final boolean RELIABLE_WRITE = false;
    public static final int RELIABLE_WRITE_SUCCESS = 0;
    public static final int RELIABLE_WRITE_FAIL    = 1;
    public static final int RELIABLE_WRITE_TIMEOUT = 2;
	
	Service mBleService;
	BluetoothAdapter mBluetoothAdapter;
	public BleSdkScan mBleSdkScan;
	public BleSdkConDiscon  mBleSdkConDiscon;
	public BleSdkServiceCommunicate mBleSdkServiceCommunicate;
	public BleSdkDataTransition  mBleSdkDataTransition;
	static List< ConnectInfo > mConnectInfoList = new ArrayList<ConnectInfo>();
	
	//bluethooth state
	private int  mBluethoothState = BluetoothAdapter.STATE_OFF;
	
	BluethoothAdapterStateChangCallback mBAStateChangCb;
	
	
	/** 
	* <p>Title: </p> 
	* <p>Description: </p>  
	*/
	public BleSdk(Service service) {
		super();
		// TODO Auto-generated constructor stub
		mBleService = service;
		mBleSdkScan = new BleSdkScan(this);
		mBleSdkConDiscon = new BleSdkConDiscon(this);
		mBleSdkServiceCommunicate = new BleSdkServiceCommunicate(this);
		mBleSdkDataTransition = new BleSdkDataTransition(this);
	}
	
	public boolean init(){
		BluetoothManager mBluetoothManager;
		mBluetoothManager = (BluetoothManager) mBleService.getSystemService(Context.BLUETOOTH_SERVICE);
		if (mBluetoothManager == null) {
			Log.e(TAG, "Unable to initialize BluetoothManager.");
			return false;
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			Log.e(TAG, "BluetoothAdapter is not enable");
			return false;
		}
		removeAllConnectInfo(mConnectInfoList);
		mBleSdkScan.init(mBluetoothAdapter);
		mBleSdkConDiscon.init(mBluetoothAdapter, mConnectInfoList);
		mBleSdkServiceCommunicate.init(mBluetoothAdapter, mConnectInfoList);
		mBleSdkDataTransition.init(mBluetoothAdapter, mConnectInfoList);
		mBleSdkConDiscon.setBleSdkDataTransition(mBleSdkDataTransition);
		return true;
	}
	
	public void setCallback(BleScanCallback bleScanCallback,
			                  ReceiveDataCallback receiveDataCallback,
			                  BluethoothAdapterStateChangCallback bAStateChangCb,
			                  ReliableWriteDataCallback reliableWriteDataCallback,
			                  WriteDataCallback writeDataCallback){
		mBleSdkScan.setScanCallback(bleScanCallback);
		mBleSdkConDiscon.setReceiveDataCallback(receiveDataCallback);
		mBAStateChangCb = bAStateChangCb;
		mBleSdkDataTransition.setReliableWriteDataCallback(reliableWriteDataCallback);
		mBleSdkConDiscon.setWriteDataCallback(writeDataCallback);
	}
	
	
	public static  int addConnecInfo(List< ConnectInfo > connectInfoList,
			                                   ConnectInfo connectInfo){
		synchronized(mConnectInfoList){
			if(connectInfoList.size()>=MAX_CONNECT_SIZE)
			{
				Log.e(TAG, "connectInfoList is full");
				return ADD_LIST_FAIL_MAX_SIZE;
			}
			for(int i=0;i<connectInfoList.size();i++){
				ConnectInfo tempConnectInfo = connectInfoList.get(i);
				if(tempConnectInfo.getMacAddr().equals(connectInfo.getMacAddr())){
					Log.e(TAG, "connectInfoList already contain this connectInfo");
					return ADD_LIST_FAIL_EXIST_CONNECTINOF;
				}
			}
			connectInfoList.add(connectInfo);

			Log.i(TAG, "connectInfoList.add:"+connectInfo.getMacAddr());
//			for(int i=0;i<connectInfoList.size();i++){
//				Log.e(TAG, ""+connectInfoList.get(i).toString());
//			}
			return ADD_LIST_SUCCESS;
		}
		
	}
	
	public static  ConnectInfo  getConnectInfo(List< ConnectInfo > connectInfoList,
			                              String macAddr){
		if(connectInfoList == null){
			return null;
		}
		synchronized(mConnectInfoList){
			for(int i=0;i<connectInfoList.size();i++){
				ConnectInfo tempConnectInfo = connectInfoList.get(i);
				if(tempConnectInfo.getMacAddr().equals(macAddr)){
					return tempConnectInfo;
				}
			}
			return null;
		}
		
	}
	
	public static  void  removeConnectInfo(List< ConnectInfo > connectInfoList,
			                                 String macAddr){	
		
		synchronized (mConnectInfoList) {
			Iterator<ConnectInfo> iterator = connectInfoList.iterator();
			while (iterator.hasNext()) {
				ConnectInfo connectInfo = (ConnectInfo) iterator.next();
				if(connectInfo.getMacAddr().equals(macAddr)){
					iterator.remove();
				}
			}
			Log.i(TAG, "connectInfoList.remove:"+macAddr);
//			for(int i=0;i<connectInfoList.size();i++){
//				Log.e(TAG, ""+connectInfoList.get(i).toString());
//			}
		}
		
	}
	
	
	public static  void removeAllConnectInfo(List< ConnectInfo > connectInfoList){
//		for(int i=0;i<connectInfoList.size();i++){
//			connectInfoList.get(i).setmBluetoothGatt(null);
////			tempConnectInfo.getmBluetoothGatt().disconnect();
//		}
		synchronized (mConnectInfoList) {
			
			connectInfoList.clear();
			Log.i(TAG, "connectInfoList.clear:");
		}
	}
	
	
	public int getBluethoothState(){
		if(mBluetoothAdapter != null)
			return mBluetoothAdapter.getState();
		else
			return BluetoothAdapter.STATE_OFF;
	}
	
	private class  BleAdapterBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				mBluethoothState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
//				intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
				if(mBluethoothState == BluetoothAdapter.STATE_ON){
					Log.i(TAG, "STATE_ON init()="+init());
//					if(mBluetoothAdapter!= null){
//						mBleSdkScan.stopScan();
//					}
				}else if(mBluethoothState == BluetoothAdapter.STATE_OFF){
					removeAllConnectInfo(mConnectInfoList);
					mBleSdkDataTransition.clearSend();
					mBleSdkScan.setScanState(BleSdk.SCAN_IDLE);
					//wch 2017.3.17     avoid stopscan cause scanresult callback slowly
//					if(mBluetoothAdapter!= null){
//						mBleSdkScan.stopScan();
//					}
				}
				mBAStateChangCb.onBluethoothAdapterState(mBluethoothState);
			}
		}
		
	}
	BleAdapterBroadcastReceiver bleAdapterBroadcastReceiver = new BleAdapterBroadcastReceiver();
	public void  registerBluetoothAdapterBroastReciver(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mBleService.registerReceiver(bleAdapterBroadcastReceiver, intentFilter);
	}
	
	public void unRegisterBluetoothAdapterBroastReciver(){
		mBleService.unregisterReceiver(bleAdapterBroadcastReceiver);
	}
	
	public boolean  openBle(){
		if(mBluetoothAdapter != null){
			Log.e(TAG, "mBluetoothAdapter.enable()");
			return mBluetoothAdapter.enable();
		}
		
		return false;
	}
	
	public boolean  closeBle(){
		
		if(mBluetoothAdapter != null){
			Log.e(TAG, "mBluetoothAdapter.disable()");
			return mBluetoothAdapter.disable();
		}
		
		return false;
	}
	
}
