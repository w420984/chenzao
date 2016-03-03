package com.chenzao.alarm;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.models.TaskScheduler;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.xiaosajie.chenzao.AlarmAlert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class TaskSchedulerRemindReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("sereinli", "TaskSchedulerRemindReceiver, onReceive");
		if(intent == null){
			return;
		}
	
		String action = intent.getAction();
		Log.v("sereinli", "TaskSchedulerRemindReceiver, onReceive:"+action);
		if(Constants.TASK_SCHEDULER_REMIND_ACTION_1.equals(action)
				|| Constants.TASK_SCHEDULER_REMIND_ACTION_2.equals(action)){
			Bundle bundle = intent.getExtras();
			String id = bundle.getString("id");
			Log.v("sereinli","id:"+id);
			ChenzaoDBAdapter db = new ChenzaoDBAdapter(context);
			db.open();
			Cursor cursor = db.query(id);
			if(cursor == null){
				db.close();
				return;
			}
			TaskScheduler task = new TaskScheduler(ChenzaoDBAdapter.cursor2Taskscheduler(cursor));
			db.close();
			
			String time = task.getStartTime();
			Log.v("sereinli", "TaskSchedulerRemindReceiver, start:"+time);
			int year = 0, month = 0, day = 0, hour = 0, min = 0;
			if (time != null) {
				year = Integer.parseInt(time.substring(0, 4));
				month = Integer.parseInt(time.substring(4, 6));
				day = Integer.parseInt(time.substring(6, 8));
			}
			Calendar target_start = Calendar.getInstance();
			target_start.set(Calendar.YEAR, year);
			target_start.set(Calendar.MONTH, month - 1);
			target_start.set(Calendar.DAY_OF_MONTH, day);
			target_start.set(Calendar.HOUR_OF_DAY, 0);
			target_start.set(Calendar.MINUTE, 0);
			target_start.set(Calendar.SECOND, 0);
			target_start.set(Calendar.MILLISECOND, 0);
			
			time = task.getEndTime();
			Log.v("sereinli", "TaskSchedulerRemindReceiver, end:"+time);
			if (time != null) {
				year = Integer.parseInt(time.substring(0, 4));
				month = Integer.parseInt(time.substring(4, 6));
				day = Integer.parseInt(time.substring(6, 8));
			}
			Calendar target_end = Calendar.getInstance();
			target_end.set(Calendar.YEAR, year);
			target_end.set(Calendar.MONTH, month - 1);
			target_end.set(Calendar.DAY_OF_MONTH, day);
			target_end.set(Calendar.HOUR_OF_DAY, 0);
			target_end.set(Calendar.MINUTE, 0);
			target_end.set(Calendar.SECOND, 0);
			target_end.set(Calendar.MILLISECOND, 0);
			
			Log.v("sereinli", "setAlarm, timeinmillis:"+System.currentTimeMillis()+","+target_start.getTimeInMillis() +","+target_end.getTimeInMillis());
			if(!(System.currentTimeMillis() >= target_start.getTimeInMillis() && System.currentTimeMillis() <= target_end.getTimeInMillis())){
				return;
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			int week = calendar.get(Calendar.DAY_OF_WEEK);
			Log.v("sereinli", "TaskSchedulerRemindReceiver, week:"+week);
			boolean canRemind = false;
			//根据当前repeat type设置，检查是否需要提醒
			if(Constants.TASK_SCHEDULER_REMIND_ACTION_1.equals(action)){
				Log.v("sereinli", "TaskSchedulerRemindReceiver, repeat1:"+task.getRepeatType1());
				if(task.getRepeatType1() == 0){
					//每天
					canRemind = true;
				}else if(task.getRepeatType1() == 1){
					//工作日（周一～周五）
					if(week > 1 && week < 7){
						canRemind = true;
					}
				}else if(task.getRepeatType1() == 2){
					//周末
					if(week == 1 || week == 7){
						canRemind = true;
					}
				}
			}else{
				Log.v("sereinli", "TaskSchedulerRemindReceiver, repeat2:"+task.getRepeatType2());
				if(task.getRepeatType2() == 0){
					//每天
					canRemind = true;
				}else if(task.getRepeatType2() == 1){
					//工作日（周一～周五）
					if(week > 1 && week < 7){
						canRemind = true;
					}
				}else if(task.getRepeatType2() == 2){
					//周末
					if(week == 1 || week == 7){
						canRemind = true;
					}
				}
			}

			Log.v("sereinli", "TaskSchedulerRemindReceiver, canRemind:"+canRemind);
			if(!canRemind){
				return;
			}
			
			//show remind dialog here
			Intent newintent = new Intent(context, AlarmAlert.class);
			newintent.setAction(action);
			newintent.putExtra(Constants.TASK_SCHEDULER_ID, (Serializable)task);
			newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newintent);
		}else if("android.intent.action.BOOT_COMPLETED".equals(action)){
			ChenzaoDBAdapter db = new ChenzaoDBAdapter(context);
			db.open();
			List<TaskScheduler> lst = db.getTaskSchedulerList();
			if(lst == null){
				db.close();	
				return;
			}
			
			for(TaskScheduler obj : lst){
				Utils.cancelAlarmAlert(context, obj);
				Utils.setAlarmAlert(context, obj);
			}
			
			db.close();	
		}
	}
}
