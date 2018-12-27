/**   
* @Title: BleApi.java 
* @Package com.cchip.blesdk.ble.bleapi 
* @Description: TODO
* @author wch   
* @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷9:48:17 
* @version V1.0   
*/
package com.cchip.blelib.ble.bleapi.btlight;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.cchip.blelib.ble.bleapi.BlePublicApi;
import com.cchip.blelib.ble.bleapi.Communciation;
import com.cchip.blelib.ble.blesdk.BleSdk;
import com.cchip.blelib.ble.blesdk.callback.BleScanCallback;
import com.cchip.blelib.ble.blesdk.callback.BluethoothAdapterStateChangCallback;
import com.cchip.blelib.ble.blesdk.callback.ConnectStateCallback;
import com.cchip.blelib.ble.blesdk.callback.ReceiveDataCallback;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/** 
 * @ClassName: BleApi 
 * @Description: TODO
 * @author wch
 * @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷9:48:17 
 *  
 */
public class BleApiBtLight extends BlePublicApi{

	private static final String TAG = "BleApiBtLight";
	
	public static final String BLEAPI_BTLIGHT_VERSION = "V1.0.0";
	
	private static final boolean NEED_AUTO_RECONNECT = true;
	private static final boolean NEED_CONNECT_STATUS_BROADCAST = true;
	
	public ProtocolBtLight mProtocol;
//	AutoReConnect mAutoReConnect;
	Communciation<CommunicationChannelBean> mCommunciation;
	
	
	private final IBinder binder = new LocalBinder();	
	public class LocalBinder extends Binder {
		public BleApiBtLight getService() {
			return BleApiBtLight.this;
		}
	} 
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onbind");
		return binder;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		super.setNeedAutoReConnect(NEED_AUTO_RECONNECT);
		super.setNeedConnectStatusBroadcast(NEED_CONNECT_STATUS_BROADCAST);
		Log.e(TAG, "oncreat");
//		mAutoReConnect = new AutoReConnect(this);
		mCommunciation = new CommunciationImp(this);
		mProtocol = new ProtocolBtLight(this);
	}

	
	@Override
	public void commucateInit(String addr) {
		// TODO Auto-generated method stub
		mCommunciation.commucateInit(addr);
	}

	@Override
	public void commucateInitAall() {
		// TODO Auto-generated method stub
		mCommunciation.commucateInitAall();
	}

	@Override
	public boolean getCommunication(String addr) {
		// TODO Auto-generated method stub
		return mCommunciation.getCommunication(addr);
	}

	@Override
	public boolean isCommunicte(String addr) {
		// TODO Auto-generated method stub
		return mCommunciation.isCommunicte(addr);
	}

	@Override
	public void sendCmdAfterConnected(String addr) {
		// TODO Auto-generated method stub
		mProtocol.sendCmdAfterConnected(addr);
	}

	@Override
	public void prasedata(String macAddr, byte[] data) {
		// TODO Auto-generated method stub
		mProtocol.prasedata(macAddr, data);
	}
	
	@Override
	public void reliableWriteDataCallback(String macAddr, byte[] data, int result) {
		// TODO Auto-generated method stub
		mProtocol.reliableWriteDataCallback(macAddr, data, result);
	}
	
	@Override
	public void writeDataCallback(String macAddr, byte[] data) {
		// TODO Auto-generated method stub
		mProtocol.writeDataCallback(macAddr, data);
	}
	
	public int startScanFilterByService(BleScanCallback bleScanCallback) {
		// TODO Auto-generated method stub
		return super.startScanFilterByService(bleScanCallback, new UUID[]{ProtocolBtLight.LIGHT_SERVICE});
	}

//	@Override
//	public void autoReConnect() {
//		// TODO Auto-generated method stub
//		mAutoReConnect.autoConnect();
//	}
//
//	@Override
//	public void removeAutoConnectSet(String addr) {
//		// TODO Auto-generated method stub
//		mAutoReConnect.removeAutoConnectSet(addr);
//	}
//
//	@Override
//	public void addAutoConnectSet(String addr) {
//		// TODO Auto-generated method stub
//		mAutoReConnect.addAutoConnectSet(addr);
//	}
//
//	@Override
//	public void onAutoConnectStateCallback(String addr, int state) {
//		// TODO Auto-generated method stub
//		mAutoReConnect.onConnectStateCallback(addr, state);
//	}      
	
}
