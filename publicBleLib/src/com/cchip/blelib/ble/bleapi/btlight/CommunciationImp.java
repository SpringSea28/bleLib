package com.cchip.blelib.ble.bleapi.btlight;

import java.util.HashMap;
import java.util.Map;

import com.cchip.blelib.ble.bleapi.Communciation;
import com.cchip.blelib.ble.bleapi.Constant;
import com.cchip.blelib.ble.blesdk.BleSdk;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public class CommunciationImp implements Communciation<CommunicationChannelBean>{
	private static final String TAG = "Communciation";
	
	BleApiBtLight mBleApiBtLight;
	
	private Map<String,CommunicationChannelBean> communicationChannelMap 
		= new HashMap<String, CommunicationChannelBean>();
	
	public CommunciationImp(BleApiBtLight bleApiBtLight) {
		// TODO Auto-generated constructor stub
		mBleApiBtLight = bleApiBtLight;
	}
	
	private synchronized void addCommunicationChannel(String addr,CommunicationChannelBean channel){
    	
    	if(addr == null || channel == null){
    		Log.e(TAG, "addCommunicationChannel addr == null || channel == null");
    		return;
    	}
    	
    	communicationChannelMap.put(addr, channel);
    }
    
	private synchronized void removeCommunicationChannel(String addr){
    	if(addr == null ){
    		Log.e(TAG, "removeCommunicationChannel addr == null");
    	}
    	
    	communicationChannelMap.remove(addr);
    }
    
    public synchronized CommunicationChannelBean getCommunicationChannel(String addr){
    	if(addr == null ){
    		Log.e(TAG, "removeCommunicationChannel addr == null");
    	}
    	
    	return communicationChannelMap.get(addr);
    }

    private synchronized void removeAllCommunicationChannel(){
    	communicationChannelMap.clear();
    }
    
    public void commucateInitAall(){
    	removeAllCommunicationChannel();
    }
    
    public void commucateInit(String addr){
    	removeCommunicationChannel(addr);
    }
    
    public boolean getCommunication(String addr){
    	BluetoothGattCharacteristic colorWriteCharacteristic = mBleApiBtLight.getWriteCharateristic(addr,
				ProtocolBtLight.LIGHT_SERVICE,ProtocolBtLight.COLOR_CHARATERISTIC_WRITE);
		
		BluetoothGattCharacteristic timeSyncColorWriteCharacteristic = mBleApiBtLight.getWriteCharateristic(addr,
				ProtocolBtLight.LIGHT_SERVICE,ProtocolBtLight.TIMESYNC_CHARATERISTIC_WRITE);
		
		BluetoothGattCharacteristic alarmWriteCharacteristic = mBleApiBtLight.getWriteCharateristic(addr,
				ProtocolBtLight.LIGHT_SERVICE,ProtocolBtLight.ALARM_CHARATERISTIC_WRITE_READ);
    
		if(colorWriteCharacteristic == null 
				|| timeSyncColorWriteCharacteristic == null
				|| alarmWriteCharacteristic == null){
			return false;
		}else{
			CommunicationChannelBean channel = 
					new CommunicationChannelBean(colorWriteCharacteristic, 
							timeSyncColorWriteCharacteristic, alarmWriteCharacteristic);
			addCommunicationChannel(addr, channel);
			return true;
		}
    } 
    
    public boolean isCommunicte(String macAddr){
    	if(mBleApiBtLight.getBleAdapterState() == Constant.BLUETHOOTH_STATE_ON){
    		int state = mBleApiBtLight.getConnectState(macAddr);
//    		Log.i(TAG, "getConnectState state ="+state);
    		if(state == BleSdk.DISCOVERY_SERVICE_OK){  			
    			CommunicationChannelBean channel = getCommunicationChannel(macAddr);
    			if(channel != null){
    				if(channel.lightColorWriteCharacteristic !=null 
    						&& channel.timeSyncWriteCharacteristic !=null
    						&& channel.alarmWriteReadCharacteristic !=null){
        				return true;
        			}
    			}
//    			Log.i(TAG, "mNotificationSuccess ="+mNotificationSuccess);
//    			Log.i(TAG, "mWriteCharacteristic ="+mWriteCharacteristic);
    		}
    	}
//    	Log.i(TAG, "getBleAdapterState() ="+getBleAdapterState());
    	return false;
    }
}
