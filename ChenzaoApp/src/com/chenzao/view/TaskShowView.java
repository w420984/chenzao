package com.chenzao.view;

import java.io.Serializable;

import com.chenzao.models.TaskScheduler;
import com.chenzao.utils.Constants;
import com.xiaosajie.chenzao.NewTaskSchedulerActivity;
import com.xiaosajie.chenzao.R;
import com.xiaosajie.chenzao.TaskSchedulerDetailActivity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TaskShowView extends RelativeLayout implements View.OnClickListener{
	private CircleProgressBar mCircleProgressBar;
	private TaskScheduler mTaskScheduler;
	private TextView mDays;
	private TextView mPercent;
	private TextView mDaysPoxfix;
	private TextView mPercentPoxfix;
	private Context mContext;
	private ImageView mAddTaskScheduler;

	public TaskShowView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public TaskShowView(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		init();
	}
	
	private void init(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.task_show_view, this);
    	
    	mCircleProgressBar = (CircleProgressBar)findViewById(R.id.circleProgressBar);
    	mPercent = (TextView)findViewById(R.id.percent);
    	mPercent.setOnClickListener(this);
    	mDays = (TextView)findViewById(R.id.days);
    	mDays.setOnClickListener(this);
    	mPercentPoxfix = (TextView)findViewById(R.id.percent_poxfix);
    	mPercentPoxfix.setOnClickListener(this);
    	mDaysPoxfix = (TextView)findViewById(R.id.day_poxfix);
    	mDaysPoxfix.setOnClickListener(this);
    	mAddTaskScheduler = (ImageView)findViewById(R.id.iv_add_taskscheduler);
    	mAddTaskScheduler.setOnClickListener(this);
    	
    	mTaskScheduler = null;
	}
	
	public void setProgress(float progress){
		String rate = String.valueOf(progress);
		if(rate.contains(".")){
			int index = rate.indexOf('.');
			rate = rate.substring(0, index + 2);
			if(rate.equals("0.0")){
				rate = "0";
			}
		}
		float r = Float.parseFloat(rate);
		mCircleProgressBar.setProgress(r);
		mPercent.setText(rate);
	}
	
	public void setDays(String days){
		mDays.setText(days);
	}
	
	public void setTaskScheduler(TaskScheduler data){
		mTaskScheduler = data;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.percent || v.getId() == R.id.days
				|| v.getId() == R.id.percent_poxfix || v.getId() == R.id.day_poxfix
				|| v.getId() == R.id.circleProgressBar){
			Intent intent = new Intent(mContext, TaskSchedulerDetailActivity.class);
			if(mTaskScheduler != null){
				intent.putExtra(Constants.TASK_SCHEDULER_ID, (Serializable)mTaskScheduler);
			}
			mContext.startActivity(intent);
		}else if(v.getId() == R.id.iv_add_taskscheduler){
			Intent intent = new Intent(mContext, NewTaskSchedulerActivity.class);
			mContext.startActivity(intent);
		}
	}
}
