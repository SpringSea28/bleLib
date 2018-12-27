/**   
* @Title: BleApi.java 
* @Package com.cchip.blesdk.ble.bleapi 
* @Description: TODO
* @author wch   
* @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷9:48:17 
* @version V1.0   
*/
package com.cchip.blelib.ble.bleapi;

/**
 * 
 * 1.0.2 修改扫描时间可改，修改查询蓝牙状态时的空指针异常
 * 
 * 1.0.3 添加直连接口
 * 
 * 1.0.4 修改连接时打印的连接信息字符串由string.xml --> 代码中 ，修复引用jar包导致的崩溃
 * 
 * 1.0.5 修改connect权限由public  -->default
 *       修改 方法名setStartTimeBeforConnect --> setScanTimeBeforConnect
 *       添加设置命令发送间隔接口 setCmdSendIntervalMs
 * 1.0.6 修改蓝牙关闭后，执行停止扫描 --> 不执行停止蓝牙扫描
 *       扫描方法改为同步方法
 * **/




import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.cchip.blelib.R;
import com.cchip.blelib.ble.blesdk.BleSdk;
import com.cchip.blelib.ble.blesdk.callback.BleScanCallback;
import com.cchip.blelib.ble.blesdk.callback.BluethoothAdapterStateChangCallback;
import com.cchip.blelib.ble.blesdk.callback.ConnectStateCallback;
import com.cchip.blelib.ble.blesdk.callback.ReceiveDataCallback;
import com.cchip.blelib.ble.blesdk.callback.ReliableWriteDataCallback;
import com.cchip.blelib.ble.blesdk.callback.WriteDataCallback;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/** 
 * @ClassName: BleApi 
 * @Description: TODO
 * @author wch
 * @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷9:48:17 
 *  
 */
public abstract  class BlePublicApi extends Service{

	private static final String TAG = "BlePublicApi";
	private static final int MSG_WHAT_SCAN_DEVICE_FOUND = 0;
	private static final String MSG_BUNDLE_KEY_SCAN_DEVICE_ADDRESS = "MSG_BUNDLE_KEY_SCAN_DEVICE_ADDRESS";
	
	public static final String BLEPUBLICAPI_VERSION = "V1.0.6";
	
	BleSdk mBleSdk;
	BleScanCallback mBleScanCallbackToUI;
	ConnectStateCallback mConnectCallbackToUI;
	
	AutoReConnect mAutoReConnect;
	private boolean restartBle = false;
	private boolean needAutoReConnect = true;
	private boolean needConnectStatusBroadcast = true;
	private boolean needClearReConnectSetAfterBleOff = true;
	
	private boolean isUiScanning = false;
	private HashMap<String, Timer> scanTimerMap = new HashMap<String, Timer>(); 
	
	private int scanTimeBeforeConnect = 6000;
	
	Handler mHandler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_SCAN_DEVICE_FOUND:
				String addr = msg.getData().getString(MSG_BUNDLE_KEY_SCAN_DEVICE_ADDRESS);
				Timer timer = scanTimerMap.get(addr);
//				Log.e(TAG, "addr:"+addr);
				if(timer != null){
					synchronized (scanTimerMap) {
						timer.cancel();
						scanTimerMap.remove(addr);
					}
						
					Log.e(TAG, "mHandler remove:"+addr);
					if(scanTimerMap.isEmpty()){
						stopScanBeforeConnect();
					}
					
					reallyConnect(addr);
				}
				
				break;

			default:
				break;
			}
		};
	};
	
	BleScanCallback mBleScanCallbackToBleSdk = new BleScanCallback() {
		
		@Override
		public void onScanCallback(BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			// TODO Auto-generated method stub
			
			
			if(mBleScanCallbackToUI == null){
//				Log.i(TAG, "mBleScanCallbackToUI is null");
			}else{
				mBleScanCallbackToUI.onScanCallback(device, rssi, scanRecord);
			}
			if(needAutoReConnect){
				if(mAutoReConnect.needAutoConnect(device.getAddress())){
					Bundle bundle = new Bundle();
					bundle.putString(MSG_BUNDLE_KEY_SCAN_DEVICE_ADDRESS, device.getAddress());
					Message msg = new Message();
					msg.what = MSG_WHAT_SCAN_DEVICE_FOUND;
					msg.setData(bundle);
					mHandler.sendMessage(msg);
				}
			}
		}
	};
	

	final ConnectStateCallback mConnectStateCallbackToSdk = new ConnectStateCallback() {
		
		@Override
		public void onConnectStateCallback(final String addr, int state) {
			// TODO Auto-generated method stub
			Log.i(TAG, addr + "  state="+getStateString(state));

			switch (state) {
			case BleSdk.CONNECTING:
				
				break;
			case BleSdk.CONNECTED:
				
				break;
				
			case BleSdk.CONNECT_TIMEOUT:
				commucateInit(addr);
				handleClose(addr);
//				disconnectCauseByConnectTimeout(addr);
				break;
				
			case BleSdk.DISCONNECTING:
				
				break;
			case BleSdk.DISCONNECTED:
				commucateInit(addr);
				handleClose(addr);
				break;
				
			case BleSdk.DISCONNECT_TIMEOUT:
				commucateInit(addr);
				handleClose(addr);
				break;
				
			case BleSdk.DISCOVERY_SERVICE_ING:
				
				break;
				
			case BleSdk.DISCOVERY_SERVICE_OK:
				if(getCommunication(addr)){
					sendCmdAfterConnected(addr);
					state = BleSdk.COMMUNICATE_SUCCESS;
				}else{
					state = BleSdk.COMMUNICATE_FAIL;
					commucateInit(addr);
					mBleSdk.mBleSdkConDiscon.disconnect(addr);
				}
				break;
				
			case BleSdk.DISCOVERY_SERVICE_FAIL:
				Log.e(TAG,"service found error");
				state = BleSdk.COMMUNICATE_FAIL;
				commucateInit(addr);
				mBleSdk.mBleSdkConDiscon.disconnect(addr);
				break;
								
			case BleSdk.CONNECT_ERROR_NEEDTO_CLOSE_BLE:
				restartBle = true;
				mBleSdk.closeBle();
				break;				

			default:
				break;
			}
			
			sendStateToUi(addr, state);
			onAutoConnectStateCallback(addr, state);		
			sendConnectStateBroadcast(addr,state);
		}
	};
	
	BluethoothAdapterStateChangCallback mBAStateChangeCb = new BluethoothAdapterStateChangCallback(){
		public void onBluethoothAdapterState(int state) {
			Log.e(TAG, "BluetoothAdapter state is "+state);
			if(state != BluetoothAdapter.STATE_ON){
				commucateInitAall();
			}
			
			if(state == BluetoothAdapter.STATE_ON){
				Intent intent = new Intent();
				intent.setAction(Constant.ACTION_BLUETHOOTH_STATE_CHANGE);
				intent.putExtra(Constant.EXTRA_BLUETHOOTH_STATE_CHANGE, Constant.BLUETHOOTH_STATE_ON);
				sendBroadcast(intent);

				autoReConnect();
			}else if(state == BluetoothAdapter.STATE_OFF){
				Intent intent = new Intent();
				intent.setAction(Constant.ACTION_BLUETHOOTH_STATE_CHANGE);
				intent.putExtra(Constant.EXTRA_BLUETHOOTH_STATE_CHANGE, Constant.BLUETHOOTH_STATE_OFF);
				sendBroadcast(intent);
				clearAllStopScanTimer();
				if(needClearReConnectSetAfterBleOff){
					mAutoReConnect.clearAutoConnectSet();
				}
				if(restartBle){
					restartBle = false;
					mBleSdk.openBle();
				}
			}
		};
	};
	
	ReceiveDataCallback mReceiveDataCallback = new ReceiveDataCallback() {
		
		@Override
		public void onReceiveData(String macAddr,byte[] data) {
			// TODO Auto-generated method stub
			prasedata(macAddr, data);
		}
	};
	
	ReliableWriteDataCallback mReliableWriteDataCallback = new ReliableWriteDataCallback() {
		
		@Override
		public void onWriteDateResult(String macAddr, byte[] data, int result) {
			// TODO Auto-generated method stub
			reliableWriteDataCallback(macAddr,data,result);
		}
	};
	
	WriteDataCallback mWriteDataCallback = new WriteDataCallback() {
		
		@Override
		public void onDataWrite(String macAddr, byte[] data) {
			// TODO Auto-generated method stub
			writeDataCallback(macAddr, data);
		}
	};
	
	
	private void handleClose(String addr){
//		Message msg = new Message();
//		msg.what = 0;
//		Bundle bundle = new Bundle();
//		bundle.putString("ADDRESS", addr);
//		msg.setData(bundle);
//		handler.sendMessage(msg);
		mBleSdk.mBleSdkConDiscon.closeGatt(addr);
	}
//	private final IBinder binder = new LocalBinder();	
//	public class LocalBinder extends Binder {
//		public BlePublicApi getService() {
//			return BlePublicApi.this;
//		}
//	} 
//	
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		Log.e(TAG, "onbind");
//		return binder;
//	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e(TAG, "oncreat");
		mAutoReConnect = new AutoReConnect(this);
		mBleSdk = new BleSdk(this);
		mBleSdk.registerBluetoothAdapterBroastReciver();
		init();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onUnbind");
		return super.onUnbind(intent);
	}
	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onRebind");
		super.onRebind(intent);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		clearAllStopScanTimer();
		mAutoReConnect.clearAutoConnectSet();
		mBleSdk.mBleSdkConDiscon.disconnectAll();
		mBleSdk.unRegisterBluetoothAdapterBroastReciver();
		mBleSdk = null;
	}
	
	//return false need restart bluethooth
    public boolean init(){
    	boolean result = mBleSdk.init();
//    	if(result){
    		mBleSdk.setCallback(mBleScanCallbackToBleSdk, mReceiveDataCallback,
    				mBAStateChangeCb,mReliableWriteDataCallback,mWriteDataCallback);
//    	}
    		
    	return result;
    }

    /**
     * 
    * @Title: openBle 
    * @Description: TODO
    * @param @return 
    * @return boolean
    * @throws
     */
    public boolean openBle(){
    	return mBleSdk.openBle();
    }
    
    /**
     * 
    * @Title: closeBle 
    * @Description: TODO
    * @param @return 
    * @return boolean
    * @throws
     */
    public boolean closeBle(){
    	return mBleSdk.closeBle();
    }
    
    
    /**
     * 
    * @Title: startScan 
    * @Description: TODO
    * @param @param bleScanCallback
    * @param @return   
    * @return int    BleSdk.SUCCESS 
    *                BleSdk.FAIL    
    *                BleSdk.ALREADY_SCAN_START 
    *                BleSdk.FAIL_ADAPTER 
    * @throws
     */
    public int startScan(BleScanCallback bleScanCallback){
    	mBleScanCallbackToUI = bleScanCallback;
//    	return mBleSdk.mBleSdkScan.startScan(new UUID[]{Protocol.PEDOMETER_SERVICE});
    	isUiScanning = true;
    	int state = mBleSdk.mBleSdkScan.startScan(null);
    	return state;
    }
    
    /**
     * 
    * @Title: startScanFilterByService 
    * @Description: TODO
    * @param @param bleScanCallback
    * @param @param services
    * @return int    BleSdk.SUCCESS 
    *                BleSdk.FAIL    
    *                BleSdk.ALREADY_SCAN_START 
    *                BleSdk.FAIL_ADAPTER 
    * @throws
     */
    protected int startScanFilterByService(BleScanCallback bleScanCallback,UUID[] services){
    	mBleScanCallbackToUI = bleScanCallback;
    	return mBleSdk.mBleSdkScan.startScan(services);
    }
    
    
    /**
     * 
    * @Title: stopScan 
    * @Description: TODO
    * @param @param bleScanCallback
    * @param @return   
    * @return int    BleSdk.SUCCESS 
    *                BleSdk.ALREADY_SCAN_STOP 
    *                BleSdk.FAIL_ADAPTER  
    * @throws
     */
    public int stopScan(BleScanCallback bleScanCallback){
    	
    	isUiScanning = false;
    	
    	int state = mBleSdk.mBleSdkScan.stopScan();
    	if(state == BleSdk.SUCCESS){
    		mBleScanCallbackToUI = null;
    	}
    	return state;
    }
    

    
    /**
     * 
    * @Title: disconnect 
    * @Description: TODO
    * @param @param addr
    * @param @return   
    * @return int   BleSdk.SUCCESS 
    *               BleSdk.FAIL
	*               BleSdk.FAIL_PARAMETER 
	*               BleSdk.ALREDY_DISCONNECT 
	*               BleSdk.FAIL_ADAPTER 
    * @throws
     */
    public int disconnect(String addr){
    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.e(TAG, addr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
    	
    	removeAutoConnectSet(addr);
    	return mBleSdk.mBleSdkConDiscon.disconnect(addr);
    }
    
    private int disconnectCauseByConnectTimeout(String addr){
    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.e(TAG, addr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
    	return mBleSdk.mBleSdkConDiscon.disconnect(addr);
    }
    
    
	/**
	 * 
	* @Title: autoConnect 
	* @Description: TODO
	* @param @param addr
	* @param @param connectStateCallback
	* @param @return   
	* @return int   BleSdk.SUCCESS 
	*               BleSdk.FAIL_PARAMETER 
	*               BleSdk.ALREDY_CONNECT 
	*               BleSdk.FAIL_ADAPTER 
	*               BleSdk.MAX_CONNECT 
	* @throws
	 */
    public int autoConnect(String addr, ConnectStateCallback connectStateCallback){
    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.e(TAG, addr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
    		
    	addAutoConnectSet(addr);
    	mConnectCallbackToUI = connectStateCallback;
    	return connect(addr);
    }
    
    public int autoConnectNotSetCallback(String addr){
    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.e(TAG, addr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
    		
    	addAutoConnectSet(addr);
    	return connect(addr);
    }
    
    public int autoConnectDirectly(String addr, ConnectStateCallback connectStateCallback){
    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.e(TAG, addr+":  MacAddress error");
			return BleSdk.FAIL_PARAMETER;
		}
    		
    	addAutoConnectSet(addr);
    	mConnectCallbackToUI = connectStateCallback;
    	return reallyConnect(addr);
    }
    
    
     int connect(String addr){
    	
    	int state = getConnectState(addr);
    	if(state != BleSdk.CONNECT_IDLE){
    		Log.e(TAG, "already in connectlist");
    		return BleSdk.SUCCESS;
    	}
    	
    	if(needAutoReConnect){
    		startScanBeforeConnect(addr);
    	}else{
    		reallyConnect(addr);
    	}
//    	Log.i(TAG, "start connecting");
//    	int state = mBleSdk.mBleSdkConDiscon.connect(addr, mConnectStateCallbackToSdk);
//    	Log.i(TAG, "connecting result ="+state);
//    	return state;
    	return BleSdk.SUCCESS;
    }
    
    
    private int reallyConnect(String addr){
    	Log.i(TAG, "start connecting");
    	int state = mBleSdk.mBleSdkConDiscon.connect(addr, mConnectStateCallbackToSdk);
    	Log.i(TAG, "connecting result ="+state);
    	return state;
    }
    
    
    public BluetoothGattCharacteristic getWriteCharateristic(String macAddr,
    		           UUID service,UUID characteristic){
    	return mBleSdk.mBleSdkServiceCommunicate
    			.getChrateristic(macAddr, service, characteristic);
    }
    
    public boolean setCharateristicNotification(String macAddr,
    		                             UUID service,UUID notifyCharacteristic){
    	return mBleSdk.mBleSdkServiceCommunicate.
    			setNotificationCharateristic(macAddr, service, notifyCharacteristic);
    }

    public int getBleAdapterState(){
//    	Log.e(TAG, "mBleSdk.getBluethoothState()"+mBleSdk.getBluethoothState());
    	if(mBleSdk.getBluethoothState() == BluetoothAdapter.STATE_ON){
    		return Constant.BLUETHOOTH_STATE_ON;
    	}else{
    		return Constant.BLUETHOOTH_STATE_OFF;
    	}
    }
    
    /**
     * 
    * @Title: getConnectState 
    * @Description: TODO
    * @param @param addr
    * @param @return 
    * @return     BleSdk.CONNECT_IDLE ;
                  BleSdk.CONNECTING ;
				  BleSdk.CONNECTED ;
				  BleSdk.CONNECT_TIMEOUT;
				  BleSdk.DISCONNECTING;
				  BleSdk.DISCONNECTED ;
				  BleSdk.DISCONNECT_TIMEOUT;
				  BleSdk.DISCOVERY_SERVICE_ING ;
				  BleSdk.DISCOVERY_SERVICE_OK ;
				  BleSdk.DISCOVERY_SERVICE_FAIL;
    * @throws
     */
    public int getConnectState(String addr){
    	int state = mBleSdk.mBleSdkConDiscon.getConnectState(addr);
    	return state;	
    }
    
    /**
     * 
    * @Title: getDeviceConnectState 
    * @Description: TODO
    * @param @param addr
    * @param @return 
    * @return      Constant.CONNET_IDLE
    *               Constant.CONNETING
    *                Constant.CONNETED
    *                 Constant.DISCONNETING
    *                  Constant.DISCONNETED
    *              
    * @throws
     */
    public int getDeviceConnectState(String addr){
    	int state = mBleSdk.mBleSdkConDiscon.getConnectState(addr);
    	return transformConnectStateToUiQuery(state);	
    }
    
    public boolean isInAutoReconnectSet(String addr){
    	
    	return mAutoReConnect.needAutoConnect(addr);	
    }
    
    private int transformConnectStateToUiQuery(int state){
    	
    	if(state == BleSdk.CONNECTING 
    			|| state == BleSdk.CONNECTED
    			|| state == BleSdk.DISCOVERY_SERVICE_ING){
    		return Constant.CONNETING;
    	}else if(state == BleSdk.DISCOVERY_SERVICE_OK){
    		return Constant.CONNETED;
    	}else if(state == BleSdk.DISCOVERY_SERVICE_FAIL 
//    			|| state == BleSdk.CONNECT_TIMEOUT
    			|| state == BleSdk.DISCONNECTING){
    		return Constant.DISCONNETING;
    	}else if(state == BleSdk.DISCONNECTED 
    			|| state == BleSdk.CONNECT_TIMEOUT
    			|| state == BleSdk.DISCONNECT_TIMEOUT){
    		return Constant.DISCONNETED;
    	}else if(state == BleSdk.CONNECT_IDLE){
    		return Constant.CONNET_IDLE;
    	}else{
    		return Constant.DISCONNETED;
    	}
    	
    }
    

    private int transformConnectStateToUiReport(int state){
    	
    	if(state == BleSdk.CONNECTING 
    			|| state == BleSdk.CONNECTED
    			|| state == BleSdk.DISCOVERY_SERVICE_ING){
    		return Constant.CONNETING;
    	}else if(state == BleSdk.DISCOVERY_SERVICE_FAIL 
//    			|| state == BleSdk.CONNECT_TIMEOUT
    			|| state == BleSdk.DISCONNECTING){
    		return Constant.DISCONNETING;
    	}else if(state == BleSdk.DISCONNECTED 
    			|| state == BleSdk.CONNECT_TIMEOUT
    			|| state == BleSdk.DISCONNECT_TIMEOUT){
    		return Constant.DISCONNETED;
    	}else if(state == BleSdk.COMMUNICATE_SUCCESS){
    		return Constant.CONNETED;
    	}else if(state == BleSdk.COMMUNICATE_FAIL){
    		return Constant.DISCONNETING;
    	}else{
    		return Constant.DISCONNETED;
    	}
    	
    }
 
    
    /**
     * 
    * @Title: writeData 
    * @Description: TODO
    * @param @param addr
    * @param @param mWriteCharacteristic
    * @param @param data
    * @param @return 
    * @return boolean
    * @throws
     */
   public boolean writeData(String addr,BluetoothGattCharacteristic mWriteCharacteristic, ArrayList<byte[]>data){

    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
    		Log.i(TAG, "macaddress false");
    		return false;
    	}
    	if(!isCommunicte(addr)){
    		Log.i(TAG, "isCommunicte false");
    		return false;
    	}
    	return mBleSdk.mBleSdkDataTransition.writeData(addr, mWriteCharacteristic, data);
    }
    
   /**
    * 
   * @Title: readData 
   * @Description: TODO
   * @param @param addr
   * @param @param mWriteCharacteristic
   * @param @return 
   * @return boolean
   * @throws
    */
    public boolean readData(String addr,BluetoothGattCharacteristic mWriteCharacteristic){

    	if(!BluetoothAdapter.checkBluetoothAddress(addr)){
    		Log.i(TAG, "macaddress false");
    		return false;
    	}
    	if(!isCommunicte(addr)){
    		Log.i(TAG, "isCommunicte false");
    		return false;
    	}
    	return mBleSdk.mBleSdkDataTransition.readData(addr, mWriteCharacteristic);
    }
   
    
    private void sendStateToUi(String addr,int state){
		if(mConnectCallbackToUI == null){
			Log.e(TAG, "mConnectCallbackToUI is null");
			return;
		}
		
		mConnectCallbackToUI.onConnectStateCallback(addr, transformConnectStateToUiReport(state));
    }
    
    public void setConnectCallbackToUI(ConnectStateCallback callback){
    	mConnectCallbackToUI = callback;
    }     
    
	public void setNeedAutoReConnect(boolean needAutoReConnect) {
		this.needAutoReConnect = needAutoReConnect;
	}
	
	public void setNeedConnectStatusBroadcast(boolean needConnectStatusBroadcast) {
		this.needConnectStatusBroadcast = needConnectStatusBroadcast;
	}
	
	public void setNeedClearReConnectSetAfterBleOff(boolean needClearReConnectSetAfterBleOff) {
		this.needClearReConnectSetAfterBleOff = needClearReConnectSetAfterBleOff;
	}
	
	public void setScanTimeBeforConnect(int  time) {
		this.scanTimeBeforeConnect = time;
	}
	
	public void setCmdSendIntervalMs(int intervalMs) {
		Log.e(TAG, "cmd send intervalMs:"+intervalMs);
		mBleSdk.mBleSdkDataTransition.setCmdSendIntervalMS(intervalMs);
	}

	private void autoReConnect() {
		// TODO Auto-generated method stub
		if(needAutoReConnect){
			mAutoReConnect.autoConnect();
		}
	}

	private void removeAutoConnectSet(String addr) {
		// TODO Auto-generated method stub
		if(needAutoReConnect){
			mAutoReConnect.removeAutoConnectSet(addr);
		}
	}

	private void addAutoConnectSet(String addr) {
		// TODO Auto-generated method stub
		if(needAutoReConnect){
			mAutoReConnect.addAutoConnectSet(addr);
		}
	}

	private void onAutoConnectStateCallback(String addr, int state) {
		// TODO Auto-generated method stub
		if(needAutoReConnect){
			mAutoReConnect.onConnectStateCallback(addr, state);
		}
	}  
    
	private void sendConnectStateBroadcast(String addr, int state) {
		// TODO Auto-generated method stub
		if(needConnectStatusBroadcast){
			int tempstate = transformConnectStateToUiReport(state);
			if(tempstate == Constant.CONNETED || tempstate == Constant.DISCONNETED){
				Log.i(TAG, "sendConnectStateBroadcast:"+tempstate);
				Intent intent = new Intent();
				intent.setAction(Constant.ACTION_DEVICE_CONNECT_STATUS);
				intent.putExtra(Constant.EXTRA_DEVICE_CONNECT_STATUS_ADDRESS, addr);
				intent.putExtra(Constant.EXTRA_DEVICE_CONNECT_STATUS, tempstate);
				sendBroadcast(intent);
			}
		}
	}  
    
	
	private String getStateString(int state){
		
		String stateString = "";
		switch (state) {
		case BleSdk.CONNECT_IDLE:
			stateString = "connect_idle";
//			stateString = getString(R.string.connect_idle);
			break;
		case BleSdk.CONNECTING:
			stateString = "connecting";
//			stateString = getString(R.string.connecting);
			break;
		case BleSdk.CONNECTED:
			stateString = "connected";
//			stateString = getString(R.string.connected);
			break;
		case BleSdk.CONNECT_TIMEOUT:
			stateString = "connect_timeout";
//			stateString = getString(R.string.connect_timeout);
			break;
		case BleSdk.DISCONNECTING:
			stateString = "disconnecting";
//			stateString = getString(R.string.disconnecting);
			break;
		case BleSdk.DISCONNECTED:
			stateString = "disconnected";
//			stateString = getString(R.string.disconnected);
			break;
		case BleSdk.DISCONNECT_TIMEOUT:
			stateString = "disconnect_timeout";
//			stateString = getString(R.string.disconnect_timeout);
			break;
		case BleSdk.DISCOVERY_SERVICE_ING:
			stateString = "discoverying";
//			stateString = getString(R.string.discoverying);
			break;
		case BleSdk.DISCOVERY_SERVICE_OK:
			stateString = "discoverysuccess";
//			stateString = getString(R.string.discoverysuccess);
			break;
		case BleSdk.DISCOVERY_SERVICE_FAIL:
			stateString = "discoveryfail";
//			stateString = getString(R.string.discoveryfail);
			break;

		default:
			break;
		}
		
		return stateString;
	}
	
	
	private void startScanBeforeConnect(final String addr){
		if(mBleSdk.mBleSdkScan.getScanState() == BleSdk.SCAN_IDLE){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub					
					mBleSdk.mBleSdkScan.startScan(null);
				}
			}).start();
		}
		synchronized (scanTimerMap) {
			
			if(!scanTimerMap.containsKey(addr)){
				Timer stopScanTimeoutTimer = new Timer();
				stopScanTimeoutTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						scanTimerMap.remove(addr);
						Log.e(TAG, "stopScanTimeoutTimer remove:"+addr);
						if(scanTimerMap.isEmpty()){
							stopScanBeforeConnect();
						}
						mAutoReConnect.onConnectStateCallback(addr, BleSdk.CONNECT_SCAN_NOT_FOUND);
					}
				}, scanTimeBeforeConnect);
				Log.e(TAG, "scanTimerMap put:"+addr);
				scanTimerMap.put(addr, stopScanTimeoutTimer);
			}
		}
	}
	
	private void stopScanBeforeConnect(){
		if(!isUiScanning){
			mBleSdk.mBleSdkScan.stopScan();
		}		
	}
	
	private void clearAllStopScanTimer(){
		Log.i(TAG, "clearAllStopScanTimer");
		synchronized (scanTimerMap) {
			Iterator<Entry<String, Timer>> iterator = scanTimerMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<java.lang.String, java.util.Timer> entry = (Map.Entry<java.lang.String, java.util.Timer>) iterator
						.next();
				entry.getValue().cancel();
				iterator.remove();
			}
		}
	}
	
    public abstract void commucateInit(String addr);
    public abstract void commucateInitAall();   
    public abstract boolean getCommunication(String addr);
    public abstract boolean isCommunicte(String addr);
    
    public abstract void sendCmdAfterConnected(String addr);     
    public abstract void prasedata(String macAddr,byte[] data);
    
    public abstract void reliableWriteDataCallback(String macAddr,byte[] data,int result);
    
    public abstract void writeDataCallback(String macAddr,byte[] data);
    
//    public abstract void autoReConnect();   
//    public abstract void removeAutoConnectSet(String addr); 
//    public abstract void addAutoConnectSet(String addr);
//    public abstract void onAutoConnectStateCallback(String addr, int state);
    
}
