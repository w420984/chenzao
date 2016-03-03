package com.chenzao.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.xiaosajie.chenzao.R;

import android.content.Context;


public class MyDate implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7376291190165246069L;
	public String year;
	public String month;
	public String day;
	public String week;
	
	public MyDate(Context context, String timeStamp){
		Calendar c = new GregorianCalendar();
		Date d = new Date(Long.valueOf(timeStamp));
		c.setTime(d);
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		year = String.valueOf(c.get(Calendar.YEAR));
		month = String.valueOf(c.get(Calendar.MONTH)+1);
		day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		switch (c.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			week = context.getResources().getString(R.string.sunday);
			break;
		case Calendar.MONDAY:
			week = context.getResources().getString(R.string.monday);
			break;
		case Calendar.TUESDAY:
			week = context.getResources().getString(R.string.tuesday);
			break;
		case Calendar.WEDNESDAY:
			week = context.getResources().getString(R.string.wednesday);
			break;
		case Calendar.THURSDAY:
			week = context.getResources().getString(R.string.thursday);
			break;
		case Calendar.FRIDAY:
			week = context.getResources().getString(R.string.friday);
			break;
		case Calendar.SATURDAY:
			week = context.getResources().getString(R.string.saturday);
			break;

		default:
			break;
		}
	}
	
	public String getYear(){
		return year;
	}
	
	public String getMonth(){
		return month;
	}
	
	public String getDay(){
		return day;
	}
	
	public String getWeek(){
		return week;
	}
}
