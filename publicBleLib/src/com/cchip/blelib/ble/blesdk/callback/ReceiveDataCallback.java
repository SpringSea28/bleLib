/**   
* @Title: ReceiveDataCallback.java 
* @Package com.cchip.blesdk.ble.blesdk.callback 
* @Description: TODO
* @author wch   
* @date 2015��12��3�� ����10:39:47 
* @version V1.0   
*/
package com.cchip.blelib.ble.blesdk.callback;

/** 
 * @ClassName: ReceiveDataCallback 
 * @Description: TODO
 * @author wch
 * @date 2015��12��3�� ����10:39:47 
 *  
 */
public interface ReceiveDataCallback {

	void onReceiveData(String macAddr,byte[] data);
}
