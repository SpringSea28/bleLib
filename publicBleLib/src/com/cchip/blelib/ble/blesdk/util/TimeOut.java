package com.cchip.blelib.ble.blesdk.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



import com.cchip.blelib.ble.blesdk.BleSdk;

import android.util.Log;


public class TimeOut extends Timer{

	private static final String TAG = "TimeOut";
	public static int TPYE_CONNECT = 0;
	public static int TPYE_DISCONNECT = 1;
	public static int TPYE_SERVICE_DISCOVERY = 2;
	public static int TIMEOUT_MS = 10000; //time out millisecond
	
	int type;
	String macAddr;
	List< ConnectInfo > mConnectInfoList;
	
	TimerTask mTimeOutTask = new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(TAG, "timer out run type:"+type);
			if(type!=TPYE_CONNECT && type!=TPYE_DISCONNECT && type!=TPYE_SERVICE_DISCOVERY){
				Log.e(TAG, "type error ="+type);
				return;
			}
			
			ConnectInfo connectInfo = BleSdk.getConnectInfo(mConnectInfoList, macAddr);
			if(connectInfo == null){
				Log.e(TAG, "connectInfo == null");
				return;
			}
			
			if(type == TPYE_CONNECT){
				if(connectInfo.getState() == BleSdk.CONNECTING){

					connectInfo.setState(BleSdk.CONNECT_TIMEOUT);

//					connectInfo.getmBluetoothGatt().close();
//
//					BleSdk.removeConnectInfo(mConnectInfoList, connectInfo.getMacAddr());
					connectInfo.getmConnectStateCallback()
					.onConnectStateCallback(connectInfo.getMacAddr(), 
							BleSdk.CONNECT_TIMEOUT);
				}else{
					Log.e(TAG, "connect timeout but state is "+connectInfo.getState());
				}
			}
			else if(type == TPYE_DISCONNECT){
				if(connectInfo.getState() == BleSdk.DISCONNECTING){
					connectInfo.setState(BleSdk.DISCONNECT_TIMEOUT);
//					connectInfo.getmBluetoothGatt().close();
//					BleSdk.removeConnectInfo(mConnectInfoList, connectInfo.getMacAddr());
					connectInfo.getmConnectStateCallback()
					         .onConnectStateCallback(connectInfo.getMacAddr(), 
					        		           BleSdk.DISCONNECT_TIMEOUT);
				}else{
					Log.e(TAG, "disconnect timeout but state is "+connectInfo.getState());
				}
			}else if(type == TPYE_SERVICE_DISCOVERY){
				if(connectInfo.getState() == BleSdk.DISCOVERY_SERVICE_ING){
					connectInfo.setState(BleSdk.DISCOVERY_SERVICE_FAIL);
					connectInfo.getmConnectStateCallback()
			         .onConnectStateCallback(connectInfo.getMacAddr(), 
			        		           BleSdk.DISCOVERY_SERVICE_FAIL);
				}else{
					Log.e(TAG, "discovery service timeout but state is "+connectInfo.getState());
				}
			}
		}
	};
	
	public TimeOut(List< ConnectInfo > connectInfoList,int type,String macAddr){
		this.mConnectInfoList = connectInfoList;
		this.type = type;
		this.macAddr = macAddr;
	}
	
	public void startTimeout(){
		Log.i(TAG, "type ="+type+" timer creat");
		this.schedule(mTimeOutTask, TIMEOUT_MS);
	}
	
	public void stopTimeout(){
		Log.i(TAG, "type ="+type+" timer cancel");
		this.cancel();
	}
	
}
