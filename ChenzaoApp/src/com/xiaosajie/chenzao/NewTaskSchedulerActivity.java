package com.xiaosajie.chenzao;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.R;
import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.xiaosajie.chenzao.DateTimePickerPopWindow;
import com.xiaosajie.chenzao.wxapi.WXUtils;

public class NewTaskSchedulerActivity extends BaseActivity{
	public static final int SHOW_START_TIME_DATE_PICKER = 100;
	public static final int SHOW_END_TIME_DATE_PICKER = 101;
	public static final int SHOW_REMIND_1_TIME_PICKER = 102;
	public static final int SHOW_REMIND_2_TIME_PICKER = 103;
	public static final int SHOW_CONCENT_TIME_PICKER = 104;
	public static final int SHOW_REPEAT_1_TYPE_PICKER = 105;
	public static final int SHOW_REPEAT_2_TYPE_PICKER = 106;
	public static String EXTRA_QRCODE = "EXTRA_QRCODE";
	private int mStartYear;
	private int mStartMonth;
	private int mStartDay;
	private int mEndYear;
	private int mEndMonth;
	private int mEndDay;
	private EditText mEditTitle;
	private EditText mEditNote;
	private TextView mStartTime;
	private TextView mEndTime;
	private TextView mRemindTime1;
	private TextView mRemindRepeat1;
	private TextView mRemindTime2;
	private ImageView mRemindOnOff1;
	private TextView mRemindRepeat2;
	private ImageView mRemindOnOff2;
	private TextView mConcentTime;
	private EditText mWordsWhenComplete;
	private Button mSaveAndShare;
	private Button mSaveNoShare;
	private String mQrcode;
	private TaskScheduler mTaskScheduler;
	private NewTaskSchedulerTask mTask;
	private CustomToast mProgressDialog;
	private ChenzaoDBAdapter mDatabase;
	
	private TextView mNoteCharsCounter;
	private TextView mCompleteWordsCharsCounter;
	private boolean mSharetoWeixin = false;
	
//	private NewTaskSchedulerReceiver mNewTaskSchedulerReceiver;
	
	private static final int MAX_TITLE_LEN = 10;
	private static final int MAX_MULTIPLE_EDITTEXT_LEN = 512;
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
					timeString = "" + mStartYear + getString(R.string.year) + Utils.format(mStartMonth) + getString(R.string.month) + Utils.format(mStartDay) + getString(R.string.day);
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
					timeString = "" + mEndYear + getString(R.string.year) + Utils.format(mEndMonth) + getString(R.string.month) + Utils.format(mEndDay) + getString(R.string.day);
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
				case SHOW_CONCENT_TIME_PICKER:
				{
					double cencentTime []= {1, 1.5, 2, 2.5, 3};
					mConcentTime.setText(cencentTime[msg.arg1]+getString(R.string.hour_full_name));
					mTaskScheduler.setConcentTime(msg.arg1);
					
//					float f = 1 + msg.arg1 * (float)0.5; 
//					mTaskScheduler.setConcentTime((int)msg.arg1);
//					if((f * 10)%2 > 0){
//						mConcentTime.setText(Float.toString(f) + getString(R.string.hour_full_name));
//					}else{
//						mConcentTime.setText(Integer.toString((int)f) + getString(R.string.hour_full_name));
//					}
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

	@Override
	protected void handleTitleBarEvent(int eventId) {
		// TODO Auto-generated method stub
		switch (eventId) {
		case RIGHT_BUTTON:
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setView(R.layout.newtaskscheduler);
		setTitleBar(getString(R.string.back), getString(R.string.new_task), null);
		
		mTaskScheduler = new TaskScheduler();
				
		//编辑任务标题
		mEditTitle = (EditText)findViewById(R.id.tvTitleContent);
		mEditTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mEditNote = (EditText)findViewById(R.id.tvTaskNote);
		mEditNote.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				mNoteCharsCounter.setText("("+Utils.calculateLength(s.toString())+"/"+MAX_MULTIPLE_EDITTEXT_LEN+")");
			}
		});
		mNoteCharsCounter = (TextView)findViewById(R.id.tv_note_counter);
		
		mStartTime = (TextView)findViewById(R.id.tv_start_time);
		mStartTime.setOnClickListener(this);
		
		mEndTime = (TextView)findViewById(R.id.tv_end_time);
		mEndTime.setOnClickListener(this);

		mRemindTime1 = (TextView)findViewById(R.id.et_time_1);
		mRemindTime1.setOnClickListener(this);
		
		mRemindRepeat1 = (TextView)findViewById(R.id.et_repeat_1);
		mRemindRepeat1.setOnClickListener(this);
		String [] repeatType = getApplicationContext().getResources().getStringArray(R.array.repeat); 
		mRemindRepeat1.setText(repeatType[0]);
		
		mRemindOnOff1 = (ImageView)findViewById(R.id.iv_icon_alarm_status_1);
		mRemindOnOff1.setOnClickListener(this);

		mRemindTime2 = (TextView)findViewById(R.id.et_time_2);
		mRemindTime2.setOnClickListener(this);

		mRemindRepeat2 = (TextView)findViewById(R.id.et_repeat_2);
		mRemindRepeat2.setOnClickListener(this);
		mRemindRepeat2.setText(repeatType[0]);

		mRemindOnOff2 = (ImageView)findViewById(R.id.iv_icon_alarm_status_2);
		mRemindOnOff2.setOnClickListener(this);
		
		mConcentTime = (TextView)findViewById(R.id.et_concent_time);
		mConcentTime.setOnClickListener(this);
		
		mWordsWhenComplete = (EditText)findViewById(R.id.tvCompleteContent);
		mWordsWhenComplete.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				mCompleteWordsCharsCounter.setText("("+Utils.calculateLength(s.toString())+"/"+MAX_MULTIPLE_EDITTEXT_LEN+")");
			}
		});
		mCompleteWordsCharsCounter = (TextView)findViewById(R.id.tv_complete_words_counter);
		
		mSaveAndShare =(Button)findViewById(R.id.btn_save_share);
		mSaveAndShare.setOnClickListener(this);
		mSaveNoShare = (Button)findViewById(R.id.submit_no_share);
		mSaveNoShare.setOnClickListener(this);
		
		int remind1onoff = mTaskScheduler.getOnoff1();
		if(remind1onoff == 1){
			mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_on);
			mRemindTime1.setClickable(true);
			mRemindRepeat1.setClickable(true);
		}else{
			mRemindOnOff1.setImageResource(R.drawable.newtask_alarm_off);
			mRemindTime1.setClickable(false);
			mRemindRepeat1.setClickable(false);
		}
		
		int remind2onoff = mTaskScheduler.getOnoff2();
		if(remind2onoff == 1){
			mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_on);
			mRemindTime2.setClickable(true);
			mRemindRepeat2.setClickable(true);
		}else{
			mRemindOnOff2.setImageResource(R.drawable.newtask_alarm_off);
			mRemindTime2.setClickable(false);
			mRemindRepeat2.setClickable(false);
		}
		
//		Calendar calendar = Calendar.getInstance();
//		mStartYear = calendar.get(Calendar.YEAR);
//		mStartMonth = calendar.get(Calendar.MONTH);
//		mStartDay = calendar.get(Calendar.DAY_OF_MONTH);
//		String time = "" + mStartYear + getString(R.string.year) + mStartMonth + getString(R.string.month) + mStartDay + getString(R.string.day);
//		mStartTime.setText(time);
//
//		calendar.add(Calendar.DAY_OF_MONTH, 30);
//		mEndYear = calendar.get(Calendar.YEAR);
//		mEndMonth = calendar.get(Calendar.MONTH);
//		mEndDay = calendar.get(Calendar.DAY_OF_MONTH);
//		time = "" + mEndYear + getString(R.string.year) + mEndMonth + getString(R.string.month) + mEndDay + getString(R.string.day);
//		mEndTime.setText(time);
		
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null){
			mQrcode = intent.getExtras().getString(EXTRA_QRCODE);
			mTaskScheduler.setID(mQrcode);
		}
		
//		IntentFilter intentfilter = new IntentFilter();
//		intentfilter.addAction(Constants.DATE_TIME_PICKER_START_TIME);
//		intentfilter.addAction(Constants.DATE_TIME_PICKER_END_TIME);
//		mNewTaskSchedulerReceiver = new NewTaskSchedulerReceiver();
//		registerReceiver(mNewTaskSchedulerReceiver, intentfilter);
}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		dismissProgress();
		super.onDestroy();
//		if(mNewTaskSchedulerReceiver != null){
//			unregisterReceiver(mNewTaskSchedulerReceiver);
//		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		dismissProgress();
		MobclickAgent.onPageEnd("NewTaskScheduleActivity"); 
		MobclickAgent.onPause(this);


	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("NewTaskScheduleActivity");
		MobclickAgent.onResume(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		int id = v.getId();
		if (id == R.id.tv_start_time || id == R.id.tv_end_time
				|| id == R.id.et_time_1 || id == R.id.et_time_2
				|| id == R.id.et_repeat_1 || id == R.id.et_repeat_2
				|| id == R.id.et_concent_time){
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
			imm.hideSoftInputFromWindow(mEditTitle.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(mEditNote.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(mWordsWhenComplete.getWindowToken(), 0);
		}
		
		switch(id){
		case R.id.tv_start_time:
		{
			DateTimePickerPopWindow datetimepopwindow = new DateTimePickerPopWindow(NewTaskSchedulerActivity.this, null);
			datetimepopwindow.setHandler(mHandler, SHOW_START_TIME_DATE_PICKER);
			datetimepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.tv_end_time:
		{
			DateTimePickerPopWindow datetimepopwindow = new DateTimePickerPopWindow(NewTaskSchedulerActivity.this, null);
			datetimepopwindow.setHandler(mHandler, SHOW_END_TIME_DATE_PICKER);
			datetimepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_time_1:
		{
			RemindTimePickerPopWindow remindtimepopwindow = new RemindTimePickerPopWindow(NewTaskSchedulerActivity.this, "1800");
			remindtimepopwindow.setHandler(mHandler, SHOW_REMIND_1_TIME_PICKER);
			remindtimepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_time_2:
		{
			RemindTimePickerPopWindow remindtimepopwindow = new RemindTimePickerPopWindow(NewTaskSchedulerActivity.this, "2000");
			remindtimepopwindow.setHandler(mHandler, SHOW_REMIND_2_TIME_PICKER);
			remindtimepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_concent_time:
		{
			ConcentTimePickerPopWindow concenttimepopwindow = new ConcentTimePickerPopWindow(NewTaskSchedulerActivity.this);
			concenttimepopwindow.setHandler(mHandler, SHOW_CONCENT_TIME_PICKER);
			concenttimepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_repeat_1:
		{
			RepeatTypePickerPopWindow repeattypepopwindow = new RepeatTypePickerPopWindow(NewTaskSchedulerActivity.this, 0);
			repeattypepopwindow.setHandler(mHandler, SHOW_REPEAT_1_TYPE_PICKER);
			repeattypepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
					Gravity.BOTTOM, 0, 0);
			break;
		}
		case R.id.et_repeat_2:
		{
			RepeatTypePickerPopWindow repeattypepopwindow = new RepeatTypePickerPopWindow(NewTaskSchedulerActivity.this, 0);
			repeattypepopwindow.setHandler(mHandler, SHOW_REPEAT_2_TYPE_PICKER);
			repeattypepopwindow.showAtLocation(NewTaskSchedulerActivity.this.findViewById(R.id.sl_root),
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
		case R.id.btn_save_share:
		{
			mSharetoWeixin = true;
			addNewTaskScheduler();
			break;
		}
		case R.id.submit_no_share:
		{
			mSharetoWeixin = false;
			addNewTaskScheduler();
			break;
		}
		default:
			break;
		}
	}

	public static int getContentLength(String text) {
		/*
		 * 其实这里有一个很大的缺陷， 我们将所有非 ASCII 码中的字符全部按照 1 的中国汉字大小计算了
		 */
		int totle = 0;
		boolean isEmptyText = true;
		char ch = 0;
		for (int i = 0, len = text.length(); i < len; i++) {
			ch = (char) text.codePointAt(i);
			if (isEmptyText && ch != ' ' && ch != '\n') { // 文本含有不是空格和回车的字符
				isEmptyText = false;
			}
			totle += ch > 255 ? 2 : 1;
		}
		if (isEmptyText) { // 文本只含有空格和回车
			return -1;
		}
		return (int) Math.ceil(totle / 2.);
	}

//	public class NewTaskSchedulerReceiver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			if(intent == null){
//				return;
//			}
//			String action = intent.getAction();
//			if(action.equals(Constants.DATE_TIME_PICKER_START_TIME)){
//				mStartYear = Integer.parseInt(intent.getExtras().getString(Constants.KEY_YEAR));
//				mStartMonth = Integer.parseInt(intent.getExtras().getString(Constants.KEY_MONTH));
//				mStartDay = Integer.parseInt(intent.getExtras().getString(Constants.KEY_DAY));
//				String time = "" + mStartYear + getString(R.string.year) + mStartMonth + getString(R.string.month) + mStartDay + getString(R.string.day);
//				mStartTime.setText(time);
//			}else if(action.equals(Constants.DATE_TIME_PICKER_END_TIME)){
//				mEndYear = Integer.parseInt(intent.getExtras().getString(Constants.KEY_YEAR));
//				mEndMonth = Integer.parseInt(intent.getExtras().getString(Constants.KEY_MONTH));
//				mEndDay = Integer.parseInt(intent.getExtras().getString(Constants.KEY_DAY));
//				String time = "" + mEndYear + getString(R.string.year) + mEndMonth + getString(R.string.month) + mEndDay + getString(R.string.day);
//				mEndTime.setText(time);
//			}
//		}
//		
//	}
	
	class NewTaskSchedulerTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";
			try {
				result = NetEngine.getInstance(getApplicationContext()).addTaskScheduler(Utils.getMyUid(getApplicationContext()),
												mTaskScheduler.getTitle(), mTaskScheduler.getNote(), mTaskScheduler.getStartTime(), mTaskScheduler.getEndTime(),
												mTaskScheduler.getRemindTime1(), mTaskScheduler.getRepeatType1(), mTaskScheduler.getOnoff1(),
												mTaskScheduler.getRemindTime2(), mTaskScheduler.getRepeatType2(), mTaskScheduler.getOnoff2(),
												mTaskScheduler.getConcentTime(), mTaskScheduler.getCompleteWords(), mTaskScheduler.getBGImage(),
												Constants.TASK_SCHEDULER_CUSTOMRISE);
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
					Utils.showToast(NewTaskSchedulerActivity.this, R.string.ChenzaoParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			
			JSONObject object = null;
			try {
				object = new JSONObject(result);
				mTaskScheduler.setID(object.getString("scheduleid"));
				mTaskScheduler.setCreateTime(object.getString("createdate"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Utils.showToast(NewTaskSchedulerActivity.this, R.string.ChenzaoParseException, Toast.LENGTH_SHORT);
				return;
			}
			
			Intent successIntent = new Intent(Constants.ADD_TASK_SCHEDULER_SUCCESS);
			sendBroadcast(successIntent);
			
			mTaskScheduler.setProgress(0);
			mTaskScheduler.setTitle(mEditTitle.getText().toString());
			mTaskScheduler.setNote(mEditNote.getText().toString());
			mTaskScheduler.setCompleteWords(mWordsWhenComplete.getText().toString());
			
			mDatabase = new ChenzaoDBAdapter(NewTaskSchedulerActivity.this);
			mDatabase.open();
			mDatabase.insert(mTaskScheduler);
			mDatabase.close();
			
			Utils.cancelAlarmAlert(getApplicationContext(), mTaskScheduler);
			Utils.setAlarmAlert(getApplicationContext(), mTaskScheduler);
						
			if(!mSharetoWeixin){
				finish();
				return;
			}
			mSharetoWeixin = false;
			
			String content = String.format(getString(R.string.share_WX_new_task), 
								mTaskScheduler.getTitle(),
								mTaskScheduler.getNote());
			WXUtils.shareToWX(NewTaskSchedulerActivity.this, 
					getString(R.string.new_task), 
					content, 
					null);
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
	
	private void addNewTaskScheduler(){
		mTaskScheduler.setTitle(mEditTitle.getText().toString());
		if(Utils.calculateLength(mEditTitle.getText().toString()) > MAX_TITLE_LEN){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_task_title_too_long, Toast.LENGTH_SHORT).show();
			return;
		}
		if(Utils.calculateLength(mEditNote.getText().toString()) > MAX_MULTIPLE_EDITTEXT_LEN){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_task_note_too_long, Toast.LENGTH_SHORT).show();
			return;
		}
		mTaskScheduler.setNote(mEditNote.getText().toString());
		if(Utils.calculateLength(mWordsWhenComplete.getText().toString()) > MAX_MULTIPLE_EDITTEXT_LEN){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_task_complete_words_too_long, Toast.LENGTH_SHORT).show();
			return;
		}
		mTaskScheduler.setCompleteWords(mWordsWhenComplete.getText().toString());
		String title = mTaskScheduler.getTitle();
		if(title == null || title.equals("")){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_no_title, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(mTaskScheduler.getStartTime() == null || mTaskScheduler.getStartTime().equals("")){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_no_start_time, Toast.LENGTH_SHORT).show();
			return;
		}
		if(mTaskScheduler.getEndTime() == null || mTaskScheduler.getEndTime().equals("")){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_no_end_time, Toast.LENGTH_SHORT).show();
			return;
		}
		long time1 = Long.parseLong(mTaskScheduler.getStartTime());
		long time2 = Long.parseLong(mTaskScheduler.getEndTime());
		if(time1 >= time2){
			Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_invalid_date, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(mTaskScheduler.getOnoff1() == 1){
			if(mTaskScheduler.getRemindTime1() == null || mTaskScheduler.getRemindTime1().equals("")){
				Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_no_remind_time1, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if(mTaskScheduler.getOnoff2() == 1){
			if(mTaskScheduler.getRemindTime2() == null || mTaskScheduler.getRemindTime2().equals("")){
				Toast.makeText(NewTaskSchedulerActivity.this, R.string.detail_no_remind_time2, Toast.LENGTH_SHORT).show();
				return;
			}
		}

		try{
			mTask = new NewTaskSchedulerTask();
			mTask.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
	}
}
