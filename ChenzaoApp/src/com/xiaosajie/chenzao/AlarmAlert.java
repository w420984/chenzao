package com.xiaosajie.chenzao;

import java.io.Serializable;

import com.chenzao.models.TaskScheduler;
import com.chenzao.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class AlarmAlert extends Activity implements OnClickListener{
	private TaskScheduler mTaskScheduler;
	private TextView mTitle;
	private TextView mMsgContent;
	private SoundPool mSoundPool;
	private int mStreamID;
	private int mSoundID;
	private Vibrator mVibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alertdialog);
		// Make us non-modal, so that others can receive touch events. 
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		// ...but notify us that it happened.
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		
		Intent intent = getIntent();
		String action = "";
		if(intent != null){
			mTaskScheduler = (TaskScheduler)intent.getSerializableExtra(Constants.TASK_SCHEDULER_ID);
			action = intent.getAction();
		}
		
		mTitle = (TextView)findViewById(R.id.tvDialogTitle);
		mMsgContent = (TextView)findViewById(R.id.tvDialogMsg);
		if(action != null && action.equals(Constants.TASK_SCHEDULER_REMIND_ACTION_1)){
			mTitle.setText(R.string.alarm_alert_dialog_title_todo);
//			mMsgContent.setText(R.string.alarm_alert_dialog_content_todo);
			mMsgContent.setText(mTaskScheduler.getTitle());
		}else if(action != null && action.equals(Constants.TASK_SCHEDULER_REMIND_ACTION_2)){
			mTitle.setText(R.string.alarm_alert_dialog_title_update);
//			mMsgContent.setText(R.string.alarm_alert_dialog_content_update);
			mMsgContent.setText(mTaskScheduler.getTitle());
		}
		
		Button negativeBtn = (Button)findViewById(R.id.btn_negativebutton);
		negativeBtn.setOnClickListener(this);
		Button positiveBtn = (Button)findViewById(R.id.btn_positivebutton);
		positiveBtn.setOnClickListener(this);

		mSoundID = initSoundPool();
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				playSound();
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (MotionEvent.ACTION_OUTSIDE == event.getAction())
		{
			//区域外不响应
			//finish();
			return true;
		} 
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		stopPlay();
		if(v.getId() == R.id.btn_negativebutton){
			finish();
		}else{
			if(mTaskScheduler != null){
				if (mTaskScheduler.getType() == Constants.TASK_SCHEDULER_CUSTOMRISE){
					Intent intent = new Intent(AlarmAlert.this, TaskSchedulerDetailActivity.class);
					intent.putExtra(Constants.TASK_SCHEDULER_ID, (Serializable)mTaskScheduler);
					startActivity(intent);
				}else if (mTaskScheduler.getType() == Constants.TASK_SCHEDULER_OFFICIAL){
					Intent intent = new Intent(AlarmAlert.this, ImageViewerActivity.class);
					int dayIndex = 100-mTaskScheduler.getUpdateCount();
					if (dayIndex <= 0){
						dayIndex = 1;
	            	}
					intent.putExtra(ImageViewerActivity.EXTRA_PICINDEX, dayIndex);
					startActivity(intent);
				}
			}
			finish();
		}
		
	}
	
	private int initSoundPool(){
		mSoundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 0);
		int loadId = mSoundPool.load(AlarmAlert.this, com.xiaosajie.chenzao.R.raw.notificationsound, 1);
		return loadId;
	}
	
	private void playSound(){
		AudioManager mAudioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
		if(curVolume == 0){
			return;
		}
		
		mVibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		playVibrator();
		mStreamID = mSoundPool.play(mSoundID, curVolume, curVolume, 0, 0, 1);
	}
	
	private void stopPlay(){
		mSoundPool.stop(mStreamID);
		stopVibrator();
	}
	
	private void playVibrator(){
//		mVibrator.vibrate(new long[] {100, 100}, 0);
		if(mVibrator != null){
			//mVibrator.vibrate(55000);
			mVibrator.vibrate(new long[] {20, 300, 20, 300, 20, 300}, -1);
		}
	}
	
	private void stopVibrator(){
		if (mVibrator != null){
			mVibrator.cancel();
		}
	}
}
