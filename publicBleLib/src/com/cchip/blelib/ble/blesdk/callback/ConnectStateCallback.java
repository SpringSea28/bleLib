package com.cchip.blelib.ble.blesdk.callback;

/** 
* @ClassName: ConnectStateCallback 
* @Description: TODO
* @author wch
* @date 2015��12��3�� ����10:12:11 
*  
*/
public interface ConnectStateCallback {
	
	/**
	 * 
	* @Title: onConnectStateCallback 
	* @Description: TODO
	* @param @param addr  macAddress
	* @param @param state   
	* @return void   
	* @throws
	 */
	void  onConnectStateCallback(String addr, int state);
}
