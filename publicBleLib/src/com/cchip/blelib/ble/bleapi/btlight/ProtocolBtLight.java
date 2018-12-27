/**   
 * @Title: Protocol.java 
 * @Package com.cchip.blesdk.ble.bleapi 
 * @Description: TODO
 * @author wch   
 * @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷10:21:02 
 * @version V1.0   
 */
package com.cchip.blelib.ble.bleapi.btlight;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
















import com.cchip.blelib.ble.bleapi.btlight.TimeUtil.DayOfWeek;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * @ClassName: Protocol
 * @Description: TODO
 * @author wch
 * @date 2015锟斤�?12锟斤�?4锟斤�? 锟斤拷锟斤拷10:21:02
 * 
 */
public class ProtocolBtLight {

	private static final String TAG = "Protocol";
	public static final UUID LIGHT_SERVICE = UUID
			.fromString("0000ffb0-0000-1000-8000-00805f9b34fb");
	public static final UUID COLOR_CHARATERISTIC_WRITE = UUID
			.fromString("0000ffb2-0000-1000-8000-00805f9b34fb");
	public static final UUID TIMESYNC_CHARATERISTIC_WRITE = UUID
			.fromString("0000ffb3-0000-1000-8000-00805f9b34fb");
	public static final UUID ALARM_CHARATERISTIC_WRITE_READ = UUID
			.fromString("0000ffb4-0000-1000-8000-00805f9b34fb");
	
	public static final byte HEAD_ALARM_SET = (byte) 0x01;
	public static final byte HEAD_ALARM_READ = (byte) 0x02;
	public static final byte HEAD_ALARM_CANCEL = (byte) 0x03;
	public static final byte HEAD_UNKNOW = (byte) 0x00;
	
	public static final byte ALARM_ON = (byte) 0x01;
	public static final byte ALARM_OFF = (byte) 0x00;
		
	public static final int CMD_SEND_SUCCESS = 0;
	public static final int CMD_SEND_FAIL = 1;
	public static final int CMD_SEND_PAREMETER_ERROR = 2;

	BleApiBtLight mBleApi;	
	
	private boolean readColorFlag = false;
	private boolean readAlarmFlag = false;
	
	private class DelaySendCmdAfterConnected extends TimerTask{

		String addr;
		
		public DelaySendCmdAfterConnected(String macAddr) {
			// TODO Auto-generated constructor stub
			this.addr = macAddr;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub

			setDefaultCurrentTime(addr);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			setDefaultColor(addr);
		}
	}

	public ProtocolBtLight(BleApiBtLight bleApi) {
		mBleApi = bleApi;	
	}
	
	void sendCmdAfterConnected(String addr){
//		new Timer().schedule(new DelaySendCmdAfterConnected(addr), 500);
	}
	
	void sendBroadcast(Intent intent) {
		mBleApi.sendBroadcast(intent);
	}

	private int setDefaultCurrentTime(String addr) {
		return setCurrentTime(addr,Calendar.getInstance(),true,true);		
	} 
	
	/**
	 * 
	* @Title: setCurrentTime 
	* @Description: TODO
	* @param @param addr      device mac address
	* @param @param time      the time set to sync
	* @param @param disconAutoLedoff      ble disconnect led auto off ? yes:ture; no: false;
	* @param @param conAutoLedon		  ble connect led auto on? yes:true; no:false;
	* @param @return 
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	* @throws
	 */

	public int setCurrentTime(String addr, Calendar time,
			boolean disconAutoLedoff,boolean conAutoLedon) {
		 TimeUtil dateTime = new TimeUtil(time);
		 
		byte disconAutoLedoffByte = disconAutoLedoff?(byte)1:0;
		byte conAutoLedonByte = conAutoLedon?(byte)1:0;
		
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {(byte) (dateTime.getYear()>>>8),(byte) dateTime.getYear(),
				(byte) dateTime.getMonth(), (byte) dateTime.getDay(),
				(byte) dateTime.getHour(), (byte) dateTime.getMinute(),
				(byte) dateTime.getSecond(),(byte)dateTime.getDayofWeek(),
				disconAutoLedoffByte,conAutoLedonByte});
		BluetoothGattCharacteristic timeSyncCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			timeSyncCharacteristic = channel.getTimeSyncWriteCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,timeSyncCharacteristic, cmd);
		if (result)
			return CMD_SEND_SUCCESS;
		else
			return CMD_SEND_FAIL;
	}

	private int setDefaultColor(String addr) {
		return setColor(addr,(byte)0,(byte)0,(byte)0,(byte)255);
	}

	
	/**
	 * 
	* @Title: setColor 
	* @Description: TODO
	* @param @param addr     device mac address
	* @param @param r        red
	* @param @param g        green
	* @param @param b        blue
	* @param @param w        alpha
	* @param @return 
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	* @throws
	 */
	public int setColor(String addr,byte r,byte g,byte b,byte w) {
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {b,g,r,w  });
		
		BluetoothGattCharacteristic colorCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			colorCharacteristic = channel.getLightColorWriteCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,colorCharacteristic, cmd);
		if (result)
			return CMD_SEND_SUCCESS;
		else
			return CMD_SEND_FAIL;
	}

	
	/**
	 * 
	* @Title: readColor 
	* @Description: TODO    read the color
	* @param @param addr           device mac address
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	* @throws
	 */
	public int readColor(String addr){
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getLightColorWriteCharacteristic();
		}
		readColorFlag = true;
		boolean  result = mBleApi.readData(addr,alarmCharacteristic);
		if(result){		
			return CMD_SEND_SUCCESS;
		}
		return CMD_SEND_FAIL;
	}

	/**
	 * 
	* @Title: setAlarmOn 
	* @Description: TODO
	* @param @param addr           device mac address
	* @param @param number         alarm index 0-3
	* @param @param hour           
	* @param @param minute   
	* @param @param second
	* @param @param dayofweek      weekday
	* @param @return 
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	*                 CMD_SEND_PAREMETER_ERROR       parameter error: index out?
	* @throws
	 */
	public int setAlarmOn(String addr,byte number,byte hour,byte minute,byte second,DayOfWeek[] daysofweek,
			byte r,byte g,byte b,byte w) {
		if(number>3 || number <0 ){
			return CMD_SEND_PAREMETER_ERROR;
		}
		byte dayOfWeek = 0;
		if(daysofweek != null){		
			for(int i=0;i<daysofweek.length;i++){
				byte tempDayOfWeek = (byte) (0x01 <<daysofweek[i].getValue());
				dayOfWeek |= tempDayOfWeek;
			}
		}
		
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {HEAD_ALARM_SET,number,hour,minute,second,dayOfWeek,ALARM_ON,b,g,r,w });
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getAlarmWriteReadCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,alarmCharacteristic, cmd);
		if (result)
			return CMD_SEND_SUCCESS;
		else
			return CMD_SEND_FAIL;
	}

	/**
	 * 
	* @Title: setAlarmOff 
	* @Description: TODO
	* @param @param addr           device mac address
	* @param @param number         alarm index 0-3
	* @param @param hour           
	* @param @param minute   
	* @param @param second
	* @param @param dayofweek      weekday
	* @param @return 
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	*                 CMD_SEND_PAREMETER_ERROR       parameter error: index out?
	* @throws
	 */
	public int setAlarmOff(String addr,byte number,byte hour,byte minute,byte second,DayOfWeek[] daysofweek,
			byte r,byte g,byte b,byte w) {
		if(number>3 || number <0 ){
			return CMD_SEND_PAREMETER_ERROR;
		}
		
		byte dayOfWeek = 0;
		if(daysofweek != null){		
			for(int i=0;i<daysofweek.length;i++){
				byte tempDayOfWeek = (byte) (0x01 <<daysofweek[i].getValue());
				dayOfWeek |= tempDayOfWeek;
			}
		}
		
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {HEAD_ALARM_SET,number,hour,minute,second,dayOfWeek,ALARM_OFF,b,g,r,w });
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getAlarmWriteReadCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,alarmCharacteristic, cmd);
		if (result)
			return CMD_SEND_SUCCESS;
		else
			return CMD_SEND_FAIL;
	}
	
	
	/**
	 * 
	* @Title: readAlarm 
	* @Description: TODO    read the alarm, the alarm data will get by broadcast
	* @param @param addr           device mac address
	* @param @param number         alarm index 0-3
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	*                 CMD_SEND_PAREMETER_ERROR       parameter error: index out?
	* @throws
	 */
	public int readAlarm(final String addr,final byte number){
		if(number>3 || number <0 ){
			return CMD_SEND_PAREMETER_ERROR;
		}
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {HEAD_ALARM_READ,number,0x00, 0,0,0,0,0,0,0,0});
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getAlarmWriteReadCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,alarmCharacteristic, cmd);
		
		if(result){
			return CMD_SEND_SUCCESS;
		}
		
		return CMD_SEND_FAIL;
		
		
	}
	
	public int readAlarmTest(String addr,byte number){
		if(number>3 || number <0 ){
			return CMD_SEND_PAREMETER_ERROR;
		}
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {HEAD_ALARM_READ,number,0x00, 0,0,0,0,0,0,0,0});
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getAlarmWriteReadCharacteristic();
		}
//		boolean result = mBleApi.writeData(addr,alarmCharacteristic, cmd);
		boolean result = true;
		if (result){
			//may be need to delay some ms
			readAlarmFlag = true;
			result = mBleApi.readData(addr,alarmCharacteristic);
			if(result){		
				return CMD_SEND_SUCCESS;
			}
		}
		return CMD_SEND_FAIL;
	}
	
	/**
	 * 
	* @Title: cancelAlarm 
	* @Description: TODO
	* @param @param addr           device mac address
	* @param @param number         alarm index 0-3
	* @return int     CMD_SEND_SUCCESS    cmd send success;
	*                 CMD_SEND_FAIL       cmd send fail;
	*                 CMD_SEND_PAREMETER_ERROR       parameter error: index out?
	* @throws
	 */
	public int cancelAlarm(String addr,byte number){
		if(number>3 || number <0 ){
			return CMD_SEND_PAREMETER_ERROR;
		}
		ArrayList<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add(new byte[] {HEAD_ALARM_CANCEL,number,(byte)0xFF, 
				(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
				(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF});
		
		BluetoothGattCharacteristic alarmCharacteristic = null;
		CommunicationChannelBean channel = mBleApi.mCommunciation.getCommunicationChannel(addr);
		if(channel != null){
			alarmCharacteristic = channel.getAlarmWriteReadCharacteristic();
		}
		boolean result = mBleApi.writeData(addr,alarmCharacteristic, cmd);
		if (result){	
			return CMD_SEND_SUCCESS;
		}
		return CMD_SEND_FAIL;
	}

	public void prasedata(String macAddr, byte[] data) {
		Log.i(TAG, "prasedata:"+byteArrayToString(data));
			
		if (data[0] == HEAD_ALARM_READ) {
			if(readAlarmFlag){
				praseAlarmData(macAddr, data);
				readAlarmFlag = false;
			}
			
			if(readColorFlag){
				praseColorData(macAddr, data);
				readColorFlag = false;
			}
		}else{
			praseColorData(macAddr,data);
		}
	}
	
	public void reliableWriteDataCallback(String macAddr, byte[] data,int result) {
		Log.i(TAG, "reliableWriteDataCallback ="+result);
		Log.i(TAG, "reliableWriteDataCallback:"+data[0]);
	}
	
	public void writeDataCallback(String macAddr, byte[] data) {
		Log.i(TAG, "writeDataCallback: "+byteArrayToString(data));
		if(data[0]==2){
				//may be need to delay some ms
				readAlarmFlag = true;
				int result = readAlarmTest(macAddr,data[1]);
				Log.i(TAG, "read result:"+result);
//				new Timer().schedule(new TimerTask() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						int result = readAlarmTest(addr,number);
//						Log.e(TAG, "read result:"+result);
//					}
//				}, 300);
		}
	}
	
	private void praseColorData(String macAddr, byte[] data){
		
		Intent intent = new Intent();
		intent.setAction(ConstantLight.ACTION_COLOR);
		byte[] rgbw = new byte[4];
		rgbw[0] = data[0];
		rgbw[1] = data[1];
		rgbw[2] = data[2];
		rgbw[3] = data[3];		
		
		intent.putExtra(ConstantLight.EXTRAC_COLOR, rgbw);

		sendBroadcast(intent);
	}

	private void praseAlarmData(String macAddr, byte[] data){
		
		Intent intent = new Intent();
		intent.setAction(ConstantLight.ACTION_ALARM);
		
			byte number = data[1];
			
			byte hour = data[2];
			byte minute = data[3];
			byte second = data[4];
			
			byte dayofweekByte = data[5];
			
			byte onoff = data[6];
			
			byte b = data[7];
			byte g = data[8];
			byte r = data[9];
			byte w = data[10];
			
			AlarmBean alarmBean = new AlarmBean(number, hour, minute, second, getDayOfWeek(dayofweekByte), onoff,r,g,b,w);	
			intent.putExtra(ConstantLight.EXTRAC_ALARM, alarmBean);

		sendBroadcast(intent);
	}

	private DayOfWeek[] getDayOfWeek(byte data){
		
		DayOfWeek[] result = new DayOfWeek[7];
		List<DayOfWeek> daysOfWeek = new ArrayList<DayOfWeek>();
		if((data & 0x01) != 0){
			daysOfWeek.add(DayOfWeek.SUNDAY);
		}
		
		if((data & 0x02) != 0){
			daysOfWeek.add(DayOfWeek.MONDAY);
		}
		
		if((data & 0x04) != 0){
			daysOfWeek.add(DayOfWeek.TUESDAY);
		}
		
		if((data & 0x08) != 0){
			daysOfWeek.add(DayOfWeek.WEDNESDAY);
		}
		
		if((data & 0x10) != 0){
			daysOfWeek.add(DayOfWeek.THURSDAY);
		}
		
		if((data & 0x20) != 0){
			daysOfWeek.add(DayOfWeek.FRIDAY);
		}
		
		if((data & 0x40) != 0){
			daysOfWeek.add(DayOfWeek.SATURDAY);
		}
		return daysOfWeek.toArray(result);
	}
	
	private String byteArrayToString(byte[] bytes){
		String b = "";
		for (int i = 0; i < bytes.length; i++){
		       b += Integer.toHexString(bytes[i] & 0xff) + "  ";
		}
	    return b;
	}

}
