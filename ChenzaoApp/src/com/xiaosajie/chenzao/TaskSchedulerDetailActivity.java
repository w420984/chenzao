package com.xiaosajie.chenzao;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskSchedulerDetailActivity extends BaseActivity{
	public static final int SHOW_START_TIME_DATE_PICKER = 1000;
	public static final int SHOW_END_TIME_DATE_PICKER = 1001;
	public static final int SHOW_REMIND_1_TIME_PICKER = 1002;
	public static final int SHOW_REMIND_2_TIME_PICKER = 1003;
	public static final int SHOW_CONCENT_TIME_PICKER = 1004;
	public static final int SHOW_REPEAT_1_TYPE_PICKER = 1005;
	public static final int SHOW_REPEAT_2_TYPE_PICKER = 1006;
	private int mStartYear;
	private int mStartMonth;
	private int mStartDay;
	private int mEndYear;
	private int mEndMonth;
	private int mEndDay;
	private ViewGroup mTimeGroup;
	private TextView mStartTime;
	private TextView mEndTime;
	private TextView mRemindTime1;
	private TextView mRemindRepeat1;
	private TextView mRemindTime2;
	private ImageView mRemindOnOff1;
	private TextView mRemindRepeat2;
	private ImageView mRemindOnOff2;
	private TextView mProgress;
	private ImageView mProgressMinus;
	private ImageView mProgressPlus;
	private Button mShowHistory;
	private Button mDeleteButton;
	private TaskScheduler mTaskScheduler;
	private UpdateTaskSchedulerTask mTask;
	private CustomToast mProgressDialog;
	private ChenzaoDBAdapter mDatabase;
	private float mOldProgress;
	private DeleteTaskSchedule mDeleteScheduleTask;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			if(mOldProgress != mTaskScheduler.getProgress()){
				if(Utils.getLastTime(mTaskScheduler).equals("0")){
					Toast.makeText(TaskSchedulerDetailActivity.this, R.string.detail_cannot_change_progress, Toast.LENGTH_SHORT).show();
					return;
				}
			}
			try{
				mTask = new UpdateTaskSchedulerTask();
				mTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}

			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("TaskSchedulerDetailActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("TaskSchedulerDetailActivity");
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		if(mDeleteScheduleTask != null && mDeleteScheduleTask.getStatus() == AsyncTask.Status.RUNNING){
			mDeleteScheduleTask.cancel(true);
			mDeleteScheduleTask = null;
		}

		dismissProgress();		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setView(R.layout.show_taskscheduler);
		setTitleBar(getString(R.string.back), getString(R.string.detail_title_bar_string), getString(R.string.save));
		
		mOldProgress = 0;
		
		Intent intent = getIntent();
		if(intent != null){
//			mTaskScheduler = (TaskScheduler)intent.getExtras().getSerializable(Constants.TASK_SCHEDULER_ID);
			mTaskScheduler = (TaskScheduler)intent.getSerializableExtra(Constants.TASK_SCHEDULER_ID);
			mOldProgress = mTaskScheduler.getProgress();
		}
		TextView title = (TextView)findViewById(R.id.tv_title);
		title.setText(mTaskScheduler.getTitle());

		TextView intro = (TextView)findViewById(R.id.tv_intro);
		intro.setText(mTaskScheduler.getNote());
		
		mStartTime = (TextView)findViewById(R.id.tv_start_time);
		mStartTime.setText(mTaskScheduler.getStartTime());
		//mStartTime.setOnClickListener(this);
		mEndTime = (TextView)findViewById(R.id.tv_end_time);
		mEndTime.setText(mTaskScheduler.getEndTime());
		//mEndTime.setOnClickListener(this);
		mTimeGroup = (ViewGroup)findViewById(R.id.rl_time);
		if (mTaskScheduler.getType() == Constants.TASK_SCHEDULER_OFFICIAL){
			mTimeGroup.setVisibility(View.GONE);
		}else{
			mTimeGroup.setVisibility(View.VISIBLE);
		}
		
		TextView concenttime = (TextView)findViewById(R.id.tv_concent_time);

//		float f = 1 + mTaskScheduler.getConcentTime() * (float)0.5; 
//		if((f * 10)%2 > 0){
//			concenttime.setText(Float.toString(f) + getString(R.string.hour_full_name));
//		}else{
//			concenttime.setText(Integer.toString((int)f) + getString(R.string.hour_full_name));
//		}
		double cencentTime []= {1, 1.5, 2, 2.5, 3};
		concenttime.setText(cencentTime[mTaskScheduler.getConcentTime()]+getString(R.string.hour_full_name));
		
		mRemindOnOff1 = (ImageView)findViewById(R.id.iv_icon_alarm_status_1);
		mRemindOnOff1.setOnClickListener(this);
		if(mTaskScheduler.getOnoff1() == 0){
			mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_off);
		}else{
			mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_on);
		}
		mRemindOnOff2 = (ImageView)findViewById(R.id.iv_icon_alarm_status_2);
		mRemindOnOff2.setOnClickListener(this);
		if(mTaskScheduler.getOnoff2() == 0){
			mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_off);
		}else{
			mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_on);
		}
		
		int hour = 0;
		int min = 0;
		
		mRemindTime1 = (TextView)findViewById(R.id.et_time_1);
		mRemindTime1.setOnClickListener(this);		
		if (!TextUtils.isEmpty(mTaskScheduler.getRemindTime1())){
			hour = Integer.parseInt(mTaskScheduler.getRemindTime1().substring(0, 2));
			min = Integer.parseInt(mTaskScheduler.getRemindTime1().substring(2, 4));
			mRemindTime1.setText("" + hour + getString(R.string.hour) + min + getString(R.string.minute));
		}
		
		mRemindTime2 = (TextView)findViewById(R.id.et_time_2);
		mRemindTime2.setOnClickListener(this);
		if (!TextUtils.isEmpty(mTaskScheduler.getRemindTime2())){
			hour = Integer.parseInt(mTaskScheduler.getRemindTime2().substring(0, 2));
			min = Integer.parseInt(mTaskScheduler.getRemindTime2().substring(2, 4));
			mRemindTime2.setText("" + hour + getString(R.string.hour) + min + getString(R.string.minute));
		}
		
		String[] repeatType = getApplicationContext().getResources().getStringArray(R.array.repeat);;
		mRemindRepeat1 = (TextView)findViewById(R.id.et_repeat_1);
		mRemindRepeat1.setOnClickListener(this);
		mRemindRepeat1.setText(repeatType[mTaskScheduler.getRepeatType1()]);
		mRemindRepeat2 = (TextView)findViewById(R.id.et_repeat_2);
		mRemindRepeat2.setOnClickListener(this);
		mRemindRepeat2.setText(repeatType[mTaskScheduler.getRepeatType2()]);
		
		String rate = String.valueOf(mTaskScheduler.getProgress());
		if(rate.contains(".")){
			int index = rate.indexOf('.');
			rate = rate.substring(0, index + 2);
			if(rate.equals("0.0")){
				rate = "0";
			}
		}
		mProgress = (TextView)findViewById(R.id.tv_complete_progress);
		mProgress.setText(rate + "%");
		
		mProgressMinus = (ImageView)findViewById(R.id.iv_progress_minus);
		mProgressMinus.setOnClickListener(this);
		
		mProgressPlus = (ImageView)findViewById(R.id.iv_progress_plus);
		mProgressPlus.setOnClickListener(this);
		
		mShowHistory = (Button)findViewById(R.id.btn_show_history);
		mShowHistory.setOnClickListener(this);
		
		mDeleteButton = (Button)findViewById(R.id.btn_delete);
		mDeleteButton.setOnClickListener(this);
//		if (mTaskScheduler.getType() == Constants.TASK_SCHEDULER_OFFICIAL){
//			mDeleteButton.setVisibility(View.GONE);
//		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tv_start_time:
		{
			DateTimePickerPopWindow datetimepopwindow = new DateTimePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getStartTime());
			datetimepopwindow.setHandler(mHandler, SHOW_START_TIME_DATE_PICKER);
			datetimepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.tv_end_time:
		{
			DateTimePickerPopWindow datetimepopwindow = new DateTimePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getEndTime());
			datetimepopwindow.setHandler(mHandler, SHOW_END_TIME_DATE_PICKER);
			datetimepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_time_1:
		{
			RemindTimePickerPopWindow remindtimepopwindow = new RemindTimePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getRemindTime1());
			remindtimepopwindow.setHandler(mHandler, SHOW_REMIND_1_TIME_PICKER);
			remindtimepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_time_2:
		{
			RemindTimePickerPopWindow remindtimepopwindow = new RemindTimePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getRemindTime2());
			remindtimepopwindow.setHandler(mHandler, SHOW_REMIND_2_TIME_PICKER);
			remindtimepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_concent_time:
		{
			ConcentTimePickerPopWindow concenttimepopwindow = new ConcentTimePickerPopWindow(TaskSchedulerDetailActivity.this);
			concenttimepopwindow.setHandler(mHandler, SHOW_CONCENT_TIME_PICKER);
			concenttimepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_repeat_1:
		{
			RepeatTypePickerPopWindow repeattypepopwindow = new RepeatTypePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getRepeatType1());
			repeattypepopwindow.setHandler(mHandler, SHOW_REPEAT_1_TYPE_PICKER);
			repeattypepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_repeat_2:
		{
			RepeatTypePickerPopWindow repeattypepopwindow = new RepeatTypePickerPopWindow(TaskSchedulerDetailActivity.this, mTaskScheduler.getRepeatType2());
			repeattypepopwindow.setHandler(mHandler, SHOW_REPEAT_2_TYPE_PICKER);
			repeattypepopwindow.showAtLocation(TaskSchedulerDetailActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.iv_icon_alarm_status_1:
		{
			int remind1onoff = mTaskScheduler.getOnoff1();
			if(remind1onoff == 1){
				mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_off);
				mRemindTime1.setClickable(false);
				mRemindRepeat1.setClickable(false);
				mTaskScheduler.setOnoff1(0);
			}else{
				mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_on);
				mRemindTime1.setClickable(true);
				mRemindRepeat1.setClickable(true);
				mTaskScheduler.setOnoff1(1);
			}
			break;
		}
		case R.id.iv_icon_alarm_status_2:
		{
			int remind2onoff = mTaskScheduler.getOnoff2();
			if(remind2onoff == 1){
				mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_off);
				mRemindTime2.setClickable(false);
				mRemindRepeat2.setClickable(false);
				mTaskScheduler.setOnoff2(0);
			}else{
				mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_on);
				mRemindTime2.setClickable(true);
				mRemindRepeat2.setClickable(true);
				mTaskScheduler.setOnoff2(1);
			}
			break;
		}
		case R.id.btn_show_history:
		{
			Intent i = new Intent(this, DailyUpdateListActivity.class);
			i.putExtra(Constants.TASK_SCHEDULER_ID, mTaskScheduler.getID());
			i.putExtra(Constants.TASK_SCHEDULER_TYPE, mTaskScheduler.getType());
			startActivity(i);
			break;
		}
		case R.id.iv_progress_minus:
		{
			float progress = mTaskScheduler.getProgress();
			float mod = progress % 1;
			int target = 0;
			if(mod > 0.00 && mod < 1.0){
				target = (int)(progress - mod);
			}else{
				target = (int)progress;
				target -= 1;
			}
			if(target < 0){
				target = 0;
			}

			mTaskScheduler.setProgress(target);
			mProgress.setText(Integer.toString(target) + "%");
			break;
		}
		case R.id.iv_progress_plus:
		{
			float progress = mTaskScheduler.getProgress();
			float mod = progress % 1;
			int target = 0;
			if(mod > 0.00 && mod < 1.0){
				target = (int)(progress - mod) + 1;
			}else{
				target = (int)progress;
				target += 1;
			}
			
			if(target > 100){
				target = 100;
			}
			mTaskScheduler.setProgress(target);

			mProgress.setText(Integer.toString(target) + "%");
			break;
		}
		case R.id.btn_delete:
		{
			showDialog();
			break;
		}
		default:
			break;
		}
		super.onClick(v);
	}
	
	private void showDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true)
		.setMessage(R.string.delete_warning)
		.setPositiveButton(R.string.btn_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteTask();
			}
		}).setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
		

	}
	
	private void deleteTask(){
		try{
			mDeleteScheduleTask = new DeleteTaskSchedule();
			mDeleteScheduleTask.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}	
	}
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			String timeString;
			String[] repeatType;
			int index;
			switch(msg.what)
			{
				case SHOW_START_TIME_DATE_PICKER:
				{
					String startTime = (String)msg.obj;
					if (!TextUtils.isEmpty(startTime)){
						mStartYear = Integer.parseInt(startTime.substring(0, 4));
						mStartMonth = Integer.parseInt(startTime.substring(4, 6));
						mStartDay = Integer.parseInt(startTime.substring(6, 8));
						mTaskScheduler.setStartTime("" + mStartYear + Utils.format(mStartMonth) + Utils.format(mStartDay));
					}
					timeString = "" + mStartYear + getString(R.string.year) + mStartMonth + getString(R.string.month) + mStartDay + getString(R.string.day);
					mStartTime.setText(timeString);
					break;
				}
				case SHOW_END_TIME_DATE_PICKER:
				{
					String endtime = (String)msg.obj;
					if (!TextUtils.isEmpty(endtime)){
						mEndYear = Integer.parseInt(endtime.substring(0, 4));
						mEndMonth = Integer.parseInt(endtime.substring(4, 6));
						mEndDay = Integer.parseInt(endtime.substring(6, 8));
						mTaskScheduler.setEndTime("" + mEndYear + Utils.format(mEndMonth) + Utils.format(mEndDay));
					}
					timeString = "" + mEndYear + getString(R.string.year) + mEndMonth + getString(R.string.month) + mEndDay + getString(R.string.day);
					mEndTime.setText(timeString);
					break;
				}
				case SHOW_REMIND_1_TIME_PICKER:
				{
					Time time =(Time)msg.obj;
					mTaskScheduler.setRemindTime1("" + Utils.format(time.hour) + Utils.format(time.minute));
					timeString = "" + time.hour + getString(R.string.hour) + time.minute + getString(R.string.minute);
					mRemindTime1.setText(timeString);
					break;
				}
				case SHOW_REMIND_2_TIME_PICKER:
				{
					Time time =(Time)msg.obj;
					mTaskScheduler.setRemindTime2("" + Utils.format(time.hour) + Utils.format(time.minute));
					timeString = "" + time.hour + getString(R.string.hour) + time.minute + getString(R.string.minute);
					mRemindTime2.setText(timeString);
					break;
				}
				case SHOW_REPEAT_1_TYPE_PICKER:
				{
					index = msg.arg1;
					mTaskScheduler.setRepeatType1(index);
					repeatType = getApplicationContext().getResources().getStringArray(R.array.repeat); 
					mRemindRepeat1.setText(repeatType[index]);
					break;
				}
				case SHOW_REPEAT_2_TYPE_PICKER:
				{
					index = msg.arg1;
					mTaskScheduler.setRepeatType2(index);
					repeatType = getApplicationContext().getResources().getStringArray(R.array.repeat); 
					mRemindRepeat2.setText(repeatType[index]);
					break;
				}
			}
		}
		
	};

	class UpdateTaskSchedulerTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			String id = mTaskScheduler.getID();
			
			String result = "";
			try {
				result = NetEngine.getInstance(getApplicationContext()).editTaskScheduler(Utils.getMyUid(getApplicationContext()), id,
											mTaskScheduler.getRemindTime1(), mTaskScheduler.getRepeatType1(), mTaskScheduler.getOnoff1(),
											mTaskScheduler.getRemindTime2(), mTaskScheduler.getRepeatType2(), mTaskScheduler.getOnoff2(),
											mTaskScheduler.getProgress());

			} catch (ChenzaoIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(TaskSchedulerDetailActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			
			mDatabase = new ChenzaoDBAdapter(TaskSchedulerDetailActivity.this);
			mDatabase.open();
			mDatabase.update(mTaskScheduler);
			mDatabase.close();
			
			Utils.cancelAlarmAlert(getApplicationContext(), mTaskScheduler);
			Utils.setAlarmAlert(getApplicationContext(), mTaskScheduler);
			
			//通知更新列表数据
			Intent successIntent = new Intent(Constants.UPDATE_TASK_SCHEDULER_SUCCESS);
			successIntent.putExtra(Constants.TASK_SCHEDULER_TYPE, mTaskScheduler.getType());
			sendBroadcast(successIntent);
			
			finish();
		}
		
	}
	
	private class DeleteTaskSchedule extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			
			try {
				result = NetEngine.getInstance(TaskSchedulerDetailActivity.this).
							deleteTaskScheduler(Utils.getMyUid(TaskSchedulerDetailActivity.this), 
											mTaskScheduler.getID());
			} catch (ChenzaoIOException e) {
				mThr = e;
				e.printStackTrace();
			} catch (ChenzaoParseException e) {
				mThr = e;
				e.printStackTrace();
			} catch (ChenzaoApiException e) {
				mThr = e;
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(TaskSchedulerDetailActivity.this, R.string.delete_task_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			
			JSONObject object = null;
			String suc = null;
			try {
				object = new JSONObject(result);
				suc = object.optString("result");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
        	if ("success".equals(suc)){
				Utils.showToast(TaskSchedulerDetailActivity.this, R.string.delete_task_success, Toast.LENGTH_SHORT);
				Intent successIntent = new Intent(Constants.DELETE_TASK_SCHEDULER_SUCCESS);
				sendBroadcast(successIntent);
				mDatabase = new ChenzaoDBAdapter(TaskSchedulerDetailActivity.this);
				mDatabase.open();
				mDatabase.delete(mTaskScheduler.getID());
				mDatabase.close();
				finish();
        	}else{
				Utils.showToast(TaskSchedulerDetailActivity.this, R.string.delete_task_failed, Toast.LENGTH_SHORT);
        	}
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
	
}
