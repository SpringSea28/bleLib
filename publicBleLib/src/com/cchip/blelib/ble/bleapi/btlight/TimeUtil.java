/**   
* @Title: TimeUtil.java 
* @Package com.cchip.blesdk.ble.blesdk.util 
* @Description: TODO
* @author wch   
* @date 2015��12��4�� ����5:52:39 
* @version V1.0   
*/
package com.cchip.blelib.ble.bleapi.btlight;

import java.util.Calendar;

import android.util.Log;

/** 
 * @ClassName: TimeUtil 
 * @Description: TODO
 * @author wch
 * @date 2015��12��4�� ����5:52:39 
 *  
 */
public class TimeUtil {
	
	private static final String TAG = "TimeUtil";
	
	
	public static final int SUNDAY = 0;
	public static final int  MONDAY = 1;
	public static final int  TUESDAY = 2;
	public static final int  WEDNESDAY = 3;
	public static final int  THURSDAY = 4;
	public static final int  FRIDAY = 5;
	public static final int  SATURDAY = 6;
	
	
     private int  year ;
     private int  month;
     private int  day;
     public int hour;
     private int minute;
     private int second;
     
     
     private int dayofWeek;
     
     public TimeUtil(Calendar calendar){
    	 try {
    		 this.year  = calendar.get(Calendar.YEAR) ;
             this.month = calendar.get(Calendar.MONTH)+1;
             this.day = calendar.get(Calendar.DAY_OF_MONTH);
             this.hour = calendar.get(Calendar.HOUR_OF_DAY);
             this.minute = calendar.get(Calendar.MINUTE);
             this.second = calendar.get(Calendar.SECOND);
             this.dayofWeek = calendar.get(Calendar.DAY_OF_WEEK);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG,"calendar error");
		}
    	
     }
     
	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(int day) {
		this.day = day;
	}

	/**
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * @param hour the hour to set
	 */
	public void setHour(int hour) {
		this.hour = hour;
	}

	/**
	 * @return the minute
	 */
	public int getMinute() {
		return minute;
	}

	/**
	 * @param minute the minute to set
	 */
	public void setMinute(int minute) {
		this.minute = minute;
	}

	/**
	 * @return the second
	 */
	public int getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(int second) {
		this.second = second;
	}


	public int getDayofWeek() {
		int day  = 0;
		switch (dayofWeek) {
		case Calendar.SUNDAY:
//			day = SUNDAY;
			day = DayOfWeek.SUNDAY.getValue();
			break;
		case Calendar.MONDAY:
//			day = MONDAY;
			day = DayOfWeek.MONDAY.getValue();
			break;
		case Calendar.TUESDAY:
//			day = TUESDAY;
			day = DayOfWeek.TUESDAY.getValue();
			break;
		case Calendar.WEDNESDAY:
//			day = WEDNESDAY;
			day = DayOfWeek.WEDNESDAY.getValue();
			break;
		case Calendar.THURSDAY:
//			day = THURSDAY;
			day = DayOfWeek.THURSDAY.getValue();
			break;
		case Calendar.FRIDAY:
//			day = FRIDAY;
			day = DayOfWeek.FRIDAY.getValue();
			break;
		case Calendar.SATURDAY:
//			day = SATURDAY;
			day = DayOfWeek.SATURDAY.getValue();
			break;
		default:
			break;
		}
		return day;
	}

	public void setDayofWeek(int dayofWeek) {
		this.dayofWeek = dayofWeek;
	}
	
	public enum DayOfWeek {
		SUNDAY(0),MONDAY(1),TUESDAY(2),WEDNESDAY(3),THURSDAY(4),FRIDAY(5),SATURDAY(6);
		
		private int value;
		private DayOfWeek(int value) {
			// TODO Auto-generated constructor stub
			this.value = value;
		}
		
		public int getValue(){
			return this.value;
		}
		
		public static DayOfWeek valueof(int value){
			switch (value) {
			case 0:
				return SUNDAY;
			case 1:
				return MONDAY;
			case 2:
				return TUESDAY;
			case 3:
				return WEDNESDAY;
			case 4:
				return THURSDAY;
			case 5:
				return FRIDAY;
			case 6:
				return SATURDAY;
			default:
				break;
			}
			return null;
		}
	}
}
