package com.cchip.blelib.ble.blesdk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;





import com.cchip.blelib.ble.blesdk.callback.ReliableWriteDataCallback;
import com.cchip.blelib.ble.blesdk.util.CmdBean;
import com.cchip.blelib.ble.blesdk.util.ConnectInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;


public class BleSdkDataTransition {
	private static final String TAG = "BleSdkDataTransition";
	private static final int cmdSendIntervalMS_Reliablewrite = 10000;  //10s
	private int cmdSendIntervalMS_notReliablewrite = 200;  //200ms
	
	BluetoothAdapter mBluetoothAdapter;
	BleSdk mBleSdk;
	List< ConnectInfo >  mConnectInfoList;
	ReliableWriteDataCallback mReliableWriteDataCallback;
	
	BlockingDeque<CmdBean> cmdQueue = new LinkedBlockingDeque<CmdBean>();
	
//	ArrayList<SendThread> threadSend = new ArrayList<SendThread>();
//	long threadNumber = 0;
	
	Thread sendThread;
	BluetoothGatt mCurBluetoothGatt;
	BluetoothGattCharacteristic mCurBluetoothGattCharacteristic;
	boolean isSuccess = false;
	boolean isInterrupted = false;
	
	public BleSdkDataTransition(BleSdk bleSdk){
		mBleSdk = bleSdk;
		startCmdThread();
	}
	
	protected  void init(BluetoothAdapter bluetoothAdapter, 
							List< ConnectInfo > connectInfoList){
		mBluetoothAdapter = bluetoothAdapter;
		mConnectInfoList = connectInfoList; 
//		threadNumber = 0;
	}
	
	public void setReliableWriteDataCallback(ReliableWriteDataCallback reliableWriteDataCallback){
		mReliableWriteDataCallback = reliableWriteDataCallback;
	}
	
	public void setCmdSendIntervalMS(int intervalMs){
		cmdSendIntervalMS_notReliablewrite = intervalMs;
	}
	
//	protected  void clearSend(){
//		for(int i = 0;i<threadSend.size();i++){
//			threadSend.get(i).bluetoothGatt = null;
//			threadSend.get(i).writeCharacteristic = null;
//		}
//		threadSend.clear();
//		threadNumber = 0;
//	}
//	
//	protected  void clearSend(String macAddr){
//		for(int i = 0;i<threadSend.size();i++){
//			if(macAddr.equals(threadSend.get(i).macAddr)){
//				threadSend.get(i).bluetoothGatt = null;
//				threadSend.get(i).writeCharacteristic = null;	
//			}
//		}
//	}
	
	protected  synchronized void clearSend(){
		cmdQueue.clear();
	}
	
	protected synchronized void clearSend(String macAddr){
		for(CmdBean cmdBean : cmdQueue){
			if(cmdBean.getMacAddr().equals(macAddr)){
				cmdQueue.remove(cmdBean);
			}
		}
	}
	
	public boolean readData(String macAddr, BluetoothGattCharacteristic readCharacteristic){
		ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
		if(connectInfo == null){
			Log.e(TAG,"connectInfoList not cotain "+macAddr);
			return false;
		}
		
		if(connectInfo.getState()!= BleSdk.DISCOVERY_SERVICE_OK){
			Log.e(TAG,"connectInfoList connect state is not DISCOVERY_SERVICE_OK"+macAddr);
			return false;
		}
		
		return connectInfo.getmBluetoothGatt().readCharacteristic(readCharacteristic);	
	}
	
	public boolean writeData(String macAddr, BluetoothGattCharacteristic writeCharacteristic,ArrayList<byte[]> data){
		ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
		if(connectInfo == null){
			Log.e(TAG,"connectInfoList not cotain "+macAddr);
			return false;
		}
		
		if(connectInfo.getState()!= BleSdk.DISCOVERY_SERVICE_OK){
			Log.e(TAG,"connectInfoList connect state is not DISCOVERY_SERVICE_OK"+macAddr);
			return false;
		}
//		writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//		writeCharacteristic.setValue(data);
//
//		boolean state =connectInfo.getmBluetoothGatt().writeCharacteristic(writeCharacteristic);
//		if(state)
//			Log.i(TAG, "write byte:"+byteArrayToString(data));
//		return state;
//		SendThread temp = new SendThread(connectInfo.getmBluetoothGatt(), macAddr, writeCharacteristic, data,threadNumber++);
//		threadSend.add(temp);
//		Log.e(TAG, "add threadSend.size() = "+threadSend.size());
//		temp.startThread();
		
		CmdBean cmdBean = new CmdBean(macAddr, connectInfo.getmBluetoothGatt(), writeCharacteristic, data);
		boolean result = false;
		try {
			result = cmdQueue.add(cmdBean);	
//			Log.e(TAG, "add cmdbean ="+cmdBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
	
	
	private String byteArrayToString(byte[] bytes){
		String b = "";
		for (int i = 0; i < bytes.length; i++){
		       b += Integer.toHexString(bytes[i] & 0xff) + "  ";
		}
	    return b;
	}
	
	class SendThread extends Thread{
		long id ;
		BluetoothGatt bluetoothGatt;
		String macAddr;
		BluetoothGattCharacteristic writeCharacteristic;
		ArrayList<byte[]> data;
//		byte[] data;
		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			for(int i = 0;i<data.size();i++){
				byte[] tempdata = data.get(i);
				if(writeCharacteristic !=null && bluetoothGatt !=null){
					writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
					writeCharacteristic.setValue(tempdata);
		
					boolean state =bluetoothGatt.writeCharacteristic(writeCharacteristic);
					
//					if(state)
//						Log.i(TAG, "write byte:"+byteArrayToString(tempdata));
//					else
//						Log.e(TAG, "write byte error");
//					try {
//						sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
//			threadSend.remove(this);
//			Log.e(TAG, "remove threadSend.size() = "+threadSend.size());
		}

		public SendThread(BluetoothGatt bluetoothGatt,
		String macAddr,
		BluetoothGattCharacteristic writeCharacteristic,
		ArrayList<byte[]> data,long id) {
			// TODO Auto-generated constructor stub
			this.bluetoothGatt = bluetoothGatt;
			this.macAddr = macAddr;
			this.writeCharacteristic = writeCharacteristic;
			this.data = data;
			this.id = id;
		}
		
        public void startThread(){
        	this.start();
        }
        
        @Override
        public boolean equals(Object o) {
        	// TODO Auto-generated method stub
        	if(o == null)
    			return false;
    		if(o instanceof SendThread){
    			if(this.id == ((SendThread)o).id);
    				return true;
    		}
    		return false;
        }
		
	}
	
	private void startCmdThread(){
		sendThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					try {
						CmdBean cmdBean = cmdQueue.take();
						ArrayList<byte[]> data = cmdBean.getData();
						BluetoothGatt bluetoothGatt = cmdBean.getBluetoothGatt();
						BluetoothGattCharacteristic writeCharacteristic = cmdBean.getWriteCharacteristic();
						
						for(int i = 0;i<data.size();i++){
							byte[] tempdata = data.get(i);
							if(writeCharacteristic !=null && bluetoothGatt !=null){
								
								initBeforeOneSend(bluetoothGatt,writeCharacteristic);
								
								writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
					
//								bluetoothGatt.beginReliableWrite();
								writeCharacteristic.setValue(tempdata);
								boolean state =bluetoothGatt.writeCharacteristic(writeCharacteristic);
								
								if(state)
									Log.i(TAG, "write byte:"+byteArrayToString(tempdata));
								else
									Log.e(TAG, "write byte error");
								if(BleSdk.RELIABLE_WRITE){
									if(!isInterrupted){
										Thread.sleep(cmdSendIntervalMS_Reliablewrite);
										writeTimeout(mCurBluetoothGatt.getDevice().getAddress());
										initafterOneSend();
									}else{
										writeResult();
										initafterOneSend();
									}
								}
							}
						}
						if(!BleSdk.RELIABLE_WRITE){
							Thread.sleep(cmdSendIntervalMS_notReliablewrite);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if(BleSdk.RELIABLE_WRITE){
							writeResult();
							initafterOneSend();
						}
					}
				}
			}
		});
		sendThread.start();
	}
	
	public boolean isWriteDataSame(byte[] data){
		byte[] older = mCurBluetoothGattCharacteristic.getValue();
		if(older == null || data == null){
			return false;
		}
		if(older.length != data.length){
			return false;
		}
		
		for(int i=0;i<data.length;i++){
			if(data[i] != older[i]){
				return false;
			}
		}
		
		return true;
	}
	
	public void interruptThread(String addr,boolean success){
		if(mCurBluetoothGatt == null){
			return ;
		}
		if(addr.equals(mCurBluetoothGatt.getDevice().getAddress())){
			isSuccess = success;
			sendThread.interrupt();
			isInterrupted = true;
		}
	}
	
	private void initBeforeOneSend(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){
		mCurBluetoothGatt = gatt;
		mCurBluetoothGattCharacteristic = characteristic;
		isSuccess = false;
		isInterrupted = false;
	}
	
	private void initafterOneSend(){
		mCurBluetoothGatt = null;
		mCurBluetoothGattCharacteristic = null;
		isSuccess = false;
		isInterrupted = false;
	}
	
	
	private void writeResult(){
		if(isSuccess){
			writeSuccess(mCurBluetoothGatt.getDevice().getAddress());
		}else{
			writeFial(mCurBluetoothGatt.getDevice().getAddress());
		}
	}
	
	private void writeSuccess(String addr){
		Log.i(TAG, "writeSuccess");
		if(mReliableWriteDataCallback != null){
			mReliableWriteDataCallback.onWriteDateResult(addr,
					mCurBluetoothGattCharacteristic.getValue(), BleSdk.RELIABLE_WRITE_SUCCESS);
		}
	}
	
	private void writeFial(String addr){
		Log.i(TAG, "writeFial");
		if(mReliableWriteDataCallback != null){
			mReliableWriteDataCallback.onWriteDateResult(addr,
					mCurBluetoothGattCharacteristic.getValue(), BleSdk.RELIABLE_WRITE_FAIL);
		}
	}
	
	private void writeTimeout(String addr){
		Log.i(TAG, "writeTimeout");
		if(mReliableWriteDataCallback != null){
			mReliableWriteDataCallback.onWriteDateResult(addr,
					mCurBluetoothGattCharacteristic.getValue(), BleSdk.RELIABLE_WRITE_TIMEOUT);
		}
	}
}
