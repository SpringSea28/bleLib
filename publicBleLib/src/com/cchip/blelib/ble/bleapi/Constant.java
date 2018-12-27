package com.cchip.blelib.ble.bleapi;

public class Constant {
	public static final int CONNET_IDLE = 0;
	public static final int CONNETING = 1;
	public static final int CONNETED = 2;
	public static final int DISCONNETING = 3;
	public static final int DISCONNETED = 4;
	
	public static final int BLUETHOOTH_STATE_ON = 0;
	public static final int BLUETHOOTH_STATE_OFF = 1;
	
	public static final String ACTION_BLERECONNECT_MAX = "ACTION_BLERECONNECT_MAX";
	public static final String ACTION_BLUETHOOTH_STATE_CHANGE = "ACTION_BLUETHOOTH_STATE_CHANGE";
	public static final String ACTION_DEVICE_CONNECT_STATUS = "ACTION_DEVICE_CONNECT_STATUS";
	
	public static final String EXTRA_BLUETHOOTH_STATE_CHANGE = "EXTRA_BLUETHOOTH_STATE_CHANGE";
	public static final String EXTRA_DEVICE_CONNECT_STATUS = "EXTRA_DEVICE_CONNECT_STATUS";
	public static final String EXTRA_DEVICE_CONNECT_STATUS_ADDRESS = "EXTRA_DEVICE_CONNECT_STATUS_ADDRESS";
}
