package com.cchip.blelib.ble.bleapi.btlight;

import java.io.Serializable;
import java.util.Arrays;

import com.cchip.blelib.ble.bleapi.btlight.TimeUtil.DayOfWeek;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmBean implements Serializable{

	private byte number;
	
	private byte hour;
	private byte minute;
	private byte second;
	
	private DayOfWeek[] dayofweek;
	
	private byte onoff;
	
	private byte r;
	private byte g;
	private byte b;
	private byte w;
	
	
	public AlarmBean(byte number,byte hour,byte minute,
			byte second,DayOfWeek[] dayofweek,byte onoff,
			byte r,byte g,byte b,byte w) {
		// TODO Auto-generated constructor stub
		this.number = number;
		
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		
		this.dayofweek = dayofweek;
		
		this.onoff = onoff;
		this.r = r;
		this.g = g;
		this.b = b;
		this.w = w;
	}


	public byte getHour() {
		return hour;
	}


	public void setHour(byte hour) {
		this.hour = hour;
	}


	public byte getMinute() {
		return minute;
	}


	public void setMinute(byte minute) {
		this.minute = minute;
	}


	public byte getSecond() {
		return second;
	}


	public void setSecond(byte second) {
		this.second = second;
	}


	public DayOfWeek[] getDayofweek() {
		return dayofweek;
	}


	public void setDayofweek(DayOfWeek[] dayofweek) {
		this.dayofweek = dayofweek;
	}


	public byte getOnoff() {
		return onoff;
	}


	public void setOnoff(byte onoff) {
		this.onoff = onoff;
	}


	public byte getNumber() {
		return number;
	}


	public void setNumber(byte number) {
		this.number = number;
	}
	
	
	
	public byte getR() {
		return r;
	}


	public void setR(byte r) {
		this.r = r;
	}


	public byte getG() {
		return g;
	}


	public void setG(byte g) {
		this.g = g;
	}


	public byte getB() {
		return b;
	}


	public void setB(byte b) {
		this.b = b;
	}


	public byte getW() {
		return w;
	}


	public void setW(byte w) {
		this.w = w;
	}


	public AlarmBean(Parcel source) {
		// TODO Auto-generated constructor stub
		this.number = source.readByte();
		
		this.hour = source.readByte();
		this.minute = source.readByte();
		this.second = source.readByte();
//		int dayofweekInt = source.readInt();
		
		this.dayofweek = (DayOfWeek[]) source.readArray(DayOfWeek.class.getClassLoader());
		
		this.onoff = source.readByte();
		
		this.r = source.readByte();
		this.g = source.readByte();
		this.b = source.readByte();
		this.w = source.readByte();
	}


	@Override
	public String toString() {
		return "AlarmBean [number=" + number + ", hour=" + hour + ", minute="
				+ minute + ", second=" + second + ", dayofweek="
				+ Arrays.toString(dayofweek) + ", onoff=" + onoff + ", r=" + r
				+ ", g=" + g + ", b=" + b + ", w=" + w + "]";
	}
	
	
	
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		// TODO Auto-generated method stub
//		dest.writeByte(number);
//		dest.writeByte(hour);
//		dest.writeByte(minute);
//		dest.writeByte(second);
//		dest.writeByte(number);
//		
//		dest.writeArray(dayofweek);
//		dest.writeByte(onoff);
//		
//		dest.writeByte(r);
//		dest.writeByte(g);
//		dest.writeByte(b);
//		dest.writeByte(w);
//	}
//	
//	public static final Parcelable.Creator<AlarmBean> CREATOR = new Creator<AlarmBean>() {
//		
//		@Override
//		public AlarmBean[] newArray(int size) {
//			// TODO Auto-generated method stub
//			return new AlarmBean[size];
//		}
//		
//		@Override
//		public AlarmBean createFromParcel(Parcel source) {
//			// TODO Auto-generated method stub
//			return new AlarmBean(source);
//		}
//	};
//
//
//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
	
}
