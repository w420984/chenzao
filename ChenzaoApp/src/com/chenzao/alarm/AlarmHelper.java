package com.chenzao.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHelper {
	private Context mContext;
	private AlarmManager mAlarmManager;
	private static AlarmHelper mAlarmHelper;
	
	public AlarmHelper(Context context){
		mContext = context;
		mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
	}
	
	public static AlarmHelper getNetInstance( Context context ) {
        if (mAlarmHelper == null) {
        	mAlarmHelper = new AlarmHelper(context);
        }
        return mAlarmHelper;
    }

	public void setAlarm(int id, Intent intent, long timeinmillis){
		PendingIntent pendingintent = PendingIntent.getBroadcast(mContext, id, intent, 0);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeinmillis, AlarmManager.INTERVAL_DAY, pendingintent);
//		mAlarmManager.set(AlarmManager.RTC_WAKEUP, timeinmillis, pendingintent);
	}
	
	public void cancelAlarm(int id, Intent intent){
		PendingIntent pendingintent = PendingIntent.getBroadcast(mContext, id, intent, 0);
		mAlarmManager.cancel(pendingintent);
	}
}
