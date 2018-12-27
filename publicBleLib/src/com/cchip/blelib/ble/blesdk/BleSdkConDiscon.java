package com.cchip.blelib.ble.blesdk;

import java.util.Iterator;
import java.util.List;












import com.cchip.blelib.ble.blesdk.callback.ConnectStateCallback;
import com.cchip.blelib.ble.blesdk.callback.ReceiveDataCallback;
import com.cchip.blelib.ble.blesdk.callback.WriteDataCallback;
import com.cchip.blelib.ble.blesdk.util.ConnectInfo;
import com.cchip.blelib.ble.blesdk.util.TimeOut;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

public class BleSdkConDiscon {
   
	private static final String TAG = "BleSdkConDiscon";
	
	BluetoothAdapter mBluetoothAdapter;
	BleSdk mBleSdk;
	List<ConnectInfo> mConnectInfoList;
	//notify data prase
	ReceiveDataCallback mReciveDataCallback;
	BleSdkDataTransition mBleSdkDataTransition;
	
	WriteDataCallback mWriteDataCallback;
	
	public BleSdkConDiscon(BleSdk bleSdk){
		mBleSdk = bleSdk;
	}
	
	protected void setBleSdkDataTransition(BleSdkDataTransition bleSdkDataTransition){
		mBleSdkDataTransition = bleSdkDataTransition;
	}
	
	protected void init(BluetoothAdapter bluetoothAdapter, List< ConnectInfo > connectInfoList){
		mBluetoothAdapter = bluetoothAdapter;
		this.mConnectInfoList = connectInfoList;
	}
	
	protected void setWriteDataCallback(WriteDataCallback writeDataCallback){
		mWriteDataCallback = writeDataCallback;
	}
	
	protected void setReceiveDataCallback(ReceiveDataCallback receiveDataCallback){
		mReciveDataCallback = receiveDataCallback;
	}
	
	public int  getConnectState(String macAddr){
		ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
		if(connectInfo == null){
			return BleSdk.CONNECT_IDLE;
		}
		return connectInfo.getState();
	}
	
	public int connect(String macAddr, ConnectStateCallback connectStateCallback){
		if(connectStateCallback == null){
			Log.e(TAG, macAddr+":  connectStateCallback null");
			return BleSdk.FAIL_PARAMETER;
		}
		
		if(!BluetoothAdapter.checkBluetoothAddress(macAddr)){
			Log.e(TAG, macAddr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
		

		
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
			if(mBluetoothAdapter == null){
				Log.e(TAG, "mBluetoothAdapter == null");
			}
			Log.e(TAG, macAddr+":  FAIL_ADAPTER");
			return BleSdk.FAIL_ADAPTER;
		}
		
		int state = 0;
		ConnectInfo connectInfo = null;
		synchronized (this) {
		
			if(alreadyConnect(macAddr)){
				Log.e(TAG, macAddr+":  ALREDY_CONNECT");
				return BleSdk.ALREDY_CONNECT;
			}
			
			if(mConnectInfoList.size() >= BleSdk.MAX_CONNECT_SIZE){
				Log.e(TAG, "list is full");
				return BleSdk.MAX_CONNECT;
			}

			
			final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddr);	
	//		ConnectInfo connectInfo = createConnectInfo(macAddr, connectStateCallback,null);
			if(bluetoothDevice == null){
				Log.e(TAG, "bluetoothDevice is null");
				return BleSdk.FAIL;
			}
			
			BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(mBleSdk.mBleService, false, mGattCallback);
			
			if(bluetoothGatt == null){
				Log.e(TAG, "bluetoothGatt is null");
				return BleSdk.FAIL;
			}
			connectInfo = createConnectInfo(macAddr, connectStateCallback,bluetoothGatt);
	//		connectInfo.setmBluetoothGatt(bluetoothGatt);
	
			
			state = BleSdk.addConnecInfo(mConnectInfoList, connectInfo);
		}
		Log.i(TAG, "connecting");
		if(state == BleSdk.ADD_LIST_SUCCESS){
			connectInfo.getmConnectStateCallback().onConnectStateCallback(macAddr, BleSdk.CONNECTING);
		    return BleSdk.SUCCESS;
		}else if(state == BleSdk.ADD_LIST_FAIL_EXIST_CONNECTINOF){
			Log.e(TAG, macAddr+":  ALREDY_CONNECT  ADD_LIST_FAIL_EXIST_CONNECTINOF");
			return BleSdk.ALREDY_CONNECT;
		}else{
			Log.e(TAG, "MAX_CONNECT");
			return BleSdk.MAX_CONNECT;
		}
	}
	
	
	public void  disconnectAll(){
		if(mConnectInfoList == null){
			Log.i(TAG, "disconnectAll mConnectInfoList == null");
			return;
		}
		synchronized (BleSdk.mConnectInfoList) {
			
			Iterator<ConnectInfo> iterator = mConnectInfoList.iterator();
			while (iterator.hasNext()) {
				ConnectInfo connectInfo = (ConnectInfo) iterator.next();
				disconnect(connectInfo.getMacAddr());
			}
		}
	}
	
	public int disconnect(String macAddr){
		if(!BluetoothAdapter.checkBluetoothAddress(macAddr)){
			Log.e(TAG, macAddr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
		

		
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
			Log.e(TAG, macAddr+":  FAIL_ADAPTER");
			return BleSdk.FAIL_ADAPTER;
		}
		
		if(alreadyDisConnect(macAddr)){
			Log.e(TAG, macAddr+":  ALREDY_DISCONNECT");
			return BleSdk.ALREADY_DISCONNECT;
		}
		
		synchronized (this) {
			
			
			ConnectInfo connectInfo  = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
			if(connectInfo == null){
				Log.e(TAG, macAddr+":  not in the list");
				return BleSdk.FAIL;
			}
			
			BluetoothGatt bluetoothGatt = connectInfo.getmBluetoothGatt();
			
			if(bluetoothGatt== null){
				Log.e(TAG, macAddr+":  ALREDY_DISCONNECT not in list");
				return BleSdk.ALREADY_DISCONNECT;
			}
			
	
			connectInfo.setState(BleSdk.DISCONNECTING);
			connectInfo.getmConnectStateCallback().onConnectStateCallback(macAddr, BleSdk.DISCONNECTING);
	//		connectInfo.getConnectTimeOut().stopTimeout();
			TimeOut disconncectTimeOut = new TimeOut(mConnectInfoList,
					                             TimeOut.TPYE_DISCONNECT, macAddr);
			connectInfo.setDisconnectTimeOut(disconncectTimeOut);
			disconncectTimeOut.startTimeout();
			bluetoothGatt.disconnect();
			Log.i(TAG, "disconnecting");
		
		}
		return BleSdk.SUCCESS;
	}
	
	private ConnectInfo createConnectInfo(String macAddr, ConnectStateCallback connectStateCallback,
			                                BluetoothGatt bluetoothGatt){
		ConnectInfo connectInfo = new ConnectInfo();
		connectInfo.setMacAddr(macAddr);
		connectInfo.setState(BleSdk.CONNECTING);
		connectInfo.setmBluetoothGatt(bluetoothGatt);
		connectInfo.setmConnectStateCallback(connectStateCallback);
		TimeOut connectTimeOut = new TimeOut(mConnectInfoList,
				                              TimeOut.TPYE_CONNECT, macAddr);
		connectInfo.setConnectTimeOut(connectTimeOut);
		connectTimeOut.startTimeout();
		return connectInfo;
	}
	
	//�豸�Ƿ���������
	private boolean alreadyConnect(String macAddr){
		ConnectInfo connectInfo;
		if(mConnectInfoList == null){
			Log.e(TAG, "mConnectInfoList == null");
			return false;
		}
		for(int i=0;i<mConnectInfoList.size();i++){
			connectInfo = mConnectInfoList.get(i);
			if(connectInfo.getMacAddr().equals(macAddr)){
				return true;
			}
		}		
		return false;
	}
	
	//�豸�Ƿ��ѶϿ�
		private boolean alreadyDisConnect(String macAddr){
			ConnectInfo connectInfo;
			for(int i=0;i<mConnectInfoList.size();i++){
				connectInfo = mConnectInfoList.get(i);
				if(connectInfo.getMacAddr().equals(macAddr)){
					int state = connectInfo.getState();
					if(state==BleSdk.DISCONNECTING 
							|| state==BleSdk.DISCONNECTED
							|| state==BleSdk.DISCONNECT_TIMEOUT){
							return true;
					}
					break;
				}
			}		
			return false;
		}
	
	
	BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, int status,
				int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(gatt, status, newState);
			Log.i(TAG+" onConnectionStateChange", gatt.getDevice().getAddress()+":newState = "+newState+"   status="+status);
			
			ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, 
					                          gatt.getDevice().getAddress());
			if(newState == BluetoothProfile.STATE_CONNECTED){
				
//				if(status != BluetoothGatt.GATT_SUCCESS){
//					Log.e(TAG, "status != GATT_SUCCESS ,close ble");
//					connectInfo.getmConnectStateCallback()
//		             .onConnectStateCallback(gatt.getDevice().getAddress(), 
//		            		                 BleSdk.CONNECT_ERROR_NEEDTO_CLOSE_BLE);
//					return;
//				}
				
				if(connectInfo != null ){
					if(connectInfo.getConnectTimeOut() != null){
						connectInfo.getConnectTimeOut().stopTimeout();
						connectInfo.setConnectTimeOut(null);
					}
					connectInfo.setState(BleSdk.CONNECTED);
					connectInfo.getmConnectStateCallback()
					             .onConnectStateCallback(gatt.getDevice().getAddress(), 
					            		                 BleSdk.CONNECTED);
					Log.i(TAG,"discovery services");
					gatt.discoverServices();
					connectInfo.setState(BleSdk.DISCOVERY_SERVICE_ING);
					connectInfo.getmConnectStateCallback()
		             .onConnectStateCallback(gatt.getDevice().getAddress(), 
		            		                 BleSdk.DISCOVERY_SERVICE_ING);
					TimeOut discoveryTimeOut = new TimeOut(mConnectInfoList,
                            TimeOut.TPYE_SERVICE_DISCOVERY, connectInfo.getMacAddr());
					connectInfo.setDiscoveryServiceTimeOut(discoveryTimeOut);
					discoveryTimeOut.startTimeout();
				}else{
//				Thread thread = new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							
//							gatt.disconnect();
//							gatt.close();
//							Log.i(TAG,"connect gatt close not in list");
//						}
//					});
//					thread.start();
					gatt.disconnect();
					gatt.close();
					Log.i(TAG,"connect gatt close not in list");
				}
			}else if(newState == BluetoothProfile.STATE_DISCONNECTED){
				if(connectInfo != null){
					mBleSdk.mBleSdkDataTransition.clearSend(connectInfo.getMacAddr());
					TimeOut conncetTimeout = connectInfo.getConnectTimeOut();
					if(conncetTimeout!= null){
						conncetTimeout.stopTimeout();
						conncetTimeout = null;
						connectInfo.setConnectTimeOut(null);
					}
					
					TimeOut disconncetTimeout = connectInfo.getDisconnectTimeOut();
					if(disconncetTimeout!= null){
						disconncetTimeout.stopTimeout();
						conncetTimeout = null;
						connectInfo.setDisconnectTimeOut(null);
					}
					connectInfo.setState(BleSdk.DISCONNECTED);
//					BleSdk.removeConnectInfo(mConnectInfoList, gatt.getDevice().getAddress());
//					gatt.close();
					connectInfo.getmConnectStateCallback()
		             .onConnectStateCallback(gatt.getDevice().getAddress(), 
		            		                 BleSdk.DISCONNECTED);
				}else{
//					Thread thread = new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							
//							gatt.close();
//							Log.i(TAG,"disconnect gatt close not in list");
//						}
//					});
//					thread.start();
					gatt.close();
					Log.i(TAG,"disconnect gatt close not in list");
				}
			}else{
				Log.e(TAG,"unknow state");
			}
			
			
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);
			Log.i(TAG+" onServicesDiscovered",gatt.getDevice().getAddress()+" status="+status);
			ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, 
                    gatt.getDevice().getAddress());
			if(status == BluetoothGatt.GATT_SUCCESS){
				if(connectInfo != null){
					if(connectInfo.getDiscoveryServiceTimeOut()!= null){
						connectInfo.getDiscoveryServiceTimeOut().stopTimeout();
						connectInfo.setDisconnectTimeOut(null);
					}
					
					connectInfo.setState(BleSdk.DISCOVERY_SERVICE_OK);
					connectInfo.getmConnectStateCallback()
		             .onConnectStateCallback(gatt.getDevice().getAddress(), 
		            		                 BleSdk.DISCOVERY_SERVICE_OK);
				}
			}else{
				if(connectInfo != null){
					if(connectInfo.getDiscoveryServiceTimeOut()!= null){
						connectInfo.getDiscoveryServiceTimeOut().stopTimeout();
						connectInfo.setDisconnectTimeOut(null);
					}
					connectInfo.setState(BleSdk.DISCOVERY_SERVICE_FAIL);
					connectInfo.getmConnectStateCallback()
		             .onConnectStateCallback(gatt.getDevice().getAddress(), 
		            		                 BleSdk.DISCOVERY_SERVICE_FAIL);
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
			if(status == BluetoothGatt.GATT_SUCCESS){
				Log.i(TAG, " onCharacteristicRead:"+byteArrayToString(characteristic.getValue()));
				if(mReciveDataCallback == null){
					Log.e(TAG,"mReciveDataCallback is null");
					return;
				}
				mReciveDataCallback.onReceiveData(gatt.getDevice().getAddress(), 
						characteristic.getValue());
			}
		}

		@SuppressLint("NewApi")
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
			Log.i(TAG, " onCharacteristicWrite: "+byteArrayToString(characteristic.getValue()));
			
			if(mWriteDataCallback != null){
				mWriteDataCallback.onDataWrite(gatt.getDevice().getAddress(), characteristic.getValue());
			}
			
			if(!BleSdk.RELIABLE_WRITE){
				return;
			}
			
			if(mBleSdkDataTransition == null){
				return;
			}
			
			if(status == BluetoothGatt.GATT_SUCCESS){
				
				if(!mBleSdkDataTransition.isWriteDataSame(characteristic.getValue())){
					Log.i(TAG, " onCharacteristicWrite: data no same");
					mBleSdkDataTransition.interruptThread(gatt.getDevice().getAddress(), false);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		                gatt.abortReliableWrite();
	                } else {
	                    gatt.abortReliableWrite(gatt.getDevice());
	                }
//					gatt.abortReliableWrite();
				}else{
					Log.i(TAG, " onCharacteristicWrite: executeReliableWrite");
					gatt.executeReliableWrite();
				}
			}else{
				Log.i(TAG, " onCharacteristicWrite: not GATT_SUCCESS");
				mBleSdkDataTransition.interruptThread(gatt.getDevice().getAddress(), false);
			}
			
//			if(status == BluetoothGatt.GATT_SUCCESS){
//				gatt.executeReliableWrite();
//			}else{
//				Log.i(TAG, " onCharacteristicWrite: not GATT_SUCCESS");
//			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			Log.i(TAG, " onCharacteristicChanged:"+byteArrayToString(characteristic.getValue()));
			if(mReciveDataCallback == null){
				Log.e(TAG,"mReciveDataCallback is null");
				return;
			}
			mReciveDataCallback.onReceiveData(gatt.getDevice().getAddress(), 
					characteristic.getValue());
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
			Log.e(TAG, "onDescriptorWrite:"+status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onReliableWriteCompleted(gatt, status);
			Log.i(TAG, " onReliableWriteCompleted: status="+status);
			
			if(!BleSdk.RELIABLE_WRITE){
				return;
			}
			if(mBleSdkDataTransition == null){
				return;
			}
			if(status == BluetoothGatt.GATT_SUCCESS){
				mBleSdkDataTransition.interruptThread(gatt.getDevice().getAddress(), true);
			}else{
				Log.i(TAG, " onReliableWriteCompleted: not GATT_SUCCESS");
				mBleSdkDataTransition.interruptThread(gatt.getDevice().getAddress(), false);
			}
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			super.onReadRemoteRssi(gatt, rssi, status);
		}

	};
	
	
	public void closeGatt(String macAddr){
		ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
		BluetoothGatt gatt = connectInfo.getmBluetoothGatt();
		BleSdk.removeConnectInfo(mConnectInfoList, gatt.getDevice().getAddress());
		gatt.close();
	}
	
	private String byteArrayToString(byte[] bytes){
		String b = "";
		for (int i = 0; i < bytes.length; i++){
		       b += Integer.toHexString(bytes[i] & 0xff) + "  ";
		}
	    return b;
	}

	SimulateData thread;
	public void simulatedatastart(String address){
		thread = new SimulateData(address);
		thread.start();
	}
	
	public void simulatedatastop(String address){
		thread.interrupt();
	}
	
	private class SimulateData extends Thread{
		
		String address;
		
		public SimulateData(String address) {
			// TODO Auto-generated constructor stub
			this.address = address;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				for(byte i=1;i<=7;i++){
					for(byte j=0;j<24;j++){
						byte[] data = new byte[]{0x55,i,j,0x00,(byte)0,(byte)(j),(byte)0,(byte)(j),
								0x55,i,j,(byte)0xFF,(byte)0,(byte)(j),(byte)0,(byte)(j)};
						mReciveDataCallback.onReceiveData(address, 
								data);
						sleep(100);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
	}
	
}
