package com.chenzao.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.chenzao.utils.Constants;

public class TaskScheduler implements Serializable{
	 private static final long serialVersionUID = 1002373799843455456L;
	private String mTSId;
	private String mTSTitle;
	private String mTSNote;
	private String mTSStartTime;
	private String mTSEndTime;
	private String mTSRemindTime1;
	private int mTSRepeatType1;
	private int mTSRemindOnoff1;
	private String mTSRemindTime2;
	private int mTSRepeatType2;
	private int mTSRemindOnoff2;
	private int mTSConcentTime;
	private String mTSCompleteWords;
	private float mTSProgress;
	private String mTSCreateTime;
	private long mUpdateTime;
	private int mType;		//任务类型，1表示私人任务，2表示官方任务
	private int mUpdateCount;
	private String mBGImage;

	public TaskScheduler(String id, String title, String note, String starttime, String endtime,
							String remindtime1, int repeat1, int onoff1,
							String remindtime2, int repeat2, int onoff2,
							int concenttime, String completewords, int progress, 
							String createtime, long updateTime, int type, int updateCount, String bgimage){
		this.mTSId = id;
		this.mTSTitle = title;
		this.mTSNote = note;
		this.mTSStartTime =starttime;
		this.mTSEndTime = endtime;
		this.mTSRemindTime1 = remindtime1;
		this.mTSRepeatType1 = repeat1;
		this.mTSRemindOnoff1 = onoff1;
		this.mTSRemindTime2 = remindtime2;
		this.mTSRepeatType2 = repeat2;
		this.mTSRemindOnoff2 = onoff2;
		this.mTSConcentTime = concenttime;
		this.mTSCompleteWords = completewords;
		this.mTSProgress = progress;
		this.mTSCreateTime = createtime;
		this.mUpdateTime = updateTime;
		this.mType = type;
		this.mUpdateCount = updateCount;
		this.mBGImage = bgimage;
	}
	
	public TaskScheduler(TaskScheduler data){
		this.mTSId = data.mTSId;System.currentTimeMillis();
		this.mTSTitle = data.mTSTitle;
		this.mTSNote = data.mTSNote;
		this.mTSStartTime = data.mTSStartTime;
		this.mTSEndTime = data.mTSEndTime;
		this.mTSRemindTime1 = data.mTSRemindTime1;
		this.mTSRepeatType1 = data.mTSRepeatType1;
		this.mTSRemindOnoff1 = data.mTSRemindOnoff1;
		this.mTSRemindTime2 = data.mTSRemindTime2;
		this.mTSRepeatType2 = data.mTSRepeatType2;
		this.mTSRemindOnoff2 = data.mTSRemindOnoff2;
		this.mTSConcentTime = data.mTSConcentTime;
		this.mTSCompleteWords = data.mTSCompleteWords;
		this.mTSProgress = data.mTSProgress;
		this.mTSCreateTime = data.mTSCreateTime;
		this.mUpdateTime = data.mUpdateTime;
		this.mType = data.mType;
		this.mUpdateCount = data.mUpdateCount;
		this.mBGImage = data.mBGImage;
	}
	
	public TaskScheduler(){
		this.mTSId = "";
		this.mTSTitle = "";
		this.mTSNote = "";
		this.mTSStartTime = "";
		this.mTSEndTime = "";
		this.mTSRemindTime1 = "";
		this.mTSRepeatType1 = 0;
		this.mTSRemindOnoff1 = 1;
		this.mTSRemindTime2 = "";
		this.mTSRepeatType2 = 0;
		this.mTSRemindOnoff2 = 1;
		this.mTSConcentTime = 0;
		this.mTSCompleteWords = "";
		this.mTSProgress = 0;
		this.mTSCreateTime = "";
		this.mUpdateTime = (long) 0;
		this.mType = Constants.TASK_SCHEDULER_CUSTOMRISE;
		this.mUpdateCount = 0;
		int random = (int)(Math.random() * 7 + 1);
		this.mBGImage = "a00" + random + ".jpg";
	}
	
	public TaskScheduler(Context context, JSONObject object) throws JSONException{
		if (object == null){
			this.mTSId = "";
			this.mTSTitle = "";
			this.mTSNote = "";
			this.mTSStartTime = "";
			this.mTSEndTime = "";
			this.mTSRemindTime1 = "";
			this.mTSRepeatType1 = 0;
			this.mTSRemindOnoff1 = 0;
			this.mTSRemindTime2 = "";
			this.mTSRepeatType2 = 0;
			this.mTSRemindOnoff2 = 0;
			this.mTSConcentTime = 0;
			this.mTSCompleteWords = "";
			this.mTSProgress = 0;
			this.mTSCreateTime = "";
			this.mUpdateTime = (long) 0;
			this.mType = Constants.TASK_SCHEDULER_CUSTOMRISE;
			this.mUpdateCount = 0;
			int random = (int)(Math.random() * 7 + 1);
			this.mBGImage = "a00" + random + ".jpg";
			return;
		}
		this.mTSId = object.getString("id");
		this.mTSTitle = object.getString("title");
		this.mTSNote = object.getString("note");
		this.mTSStartTime = object.getString("startdate");
		this.mTSEndTime = object.getString("enddate");
		this.mTSRemindTime1 = object.getString("alarm1time");
		this.mBGImage = object.getString("bgImg");
		if(mBGImage.equals("")){
			this.mBGImage = "a00" + ((int)(Math.random() * 7 + 1)) + ".jpg";
		}
		
		String string = object.getString("alarm1repeat");
		if(string != null && !"".equals(string)){
			this.mTSRepeatType1 = Integer.parseInt(string);
		}else{
			this.mTSRepeatType1 = 0;
		}
		
		string = object.getString("alarm1on");
		if(string != null && !"".equals(string)){
			this.mTSRemindOnoff1 = Integer.parseInt(string);
		}else{
			this.mTSRemindOnoff1 = 0;
		}
		this.mTSRemindTime2 = object.getString("alarm2time");

		string = object.getString("alarm2repeat");
		if(string != null && !"".equals(string)){
			this.mTSRepeatType2 = Integer.parseInt(string);
		}else{
			this.mTSRepeatType2 = 0;
		}
		
		string = object.getString("alarm2on");
		if(string != null && !"".equals(string)){
			this.mTSRemindOnoff2 = Integer.parseInt(string);
		}else{
			this.mTSRemindOnoff2 = 0;
		}
		
		string = object.getString("focustime");
		if(string != null && !string.equals("")){
			this.mTSConcentTime = Integer.parseInt(string);
		}else{
			this.mTSConcentTime = 0;
		}

		string = object.getString("type");
		if(string != null && !string.equals("")){
			this.mType = Integer.parseInt(string);
		}else{
			this.mType = Constants.TASK_SCHEDULER_CUSTOMRISE;
		}

		string = object.getString("updateDate");
		if (TextUtils.isEmpty(string)){
			this.mUpdateTime = 0;
		}else{
			this.mUpdateTime = Long.valueOf(string);
		}

		string = object.getString("updatecount");
		if (TextUtils.isEmpty(string)){
			this.mUpdateCount = 0;
		}else{
			this.mUpdateCount = Integer.valueOf(string);
		}
		
		this.mTSCompleteWords = object.getString("finishnote");
		
		this.mTSProgress = Float.parseFloat(TextUtils.isEmpty(object.getString("progress")) ? "0" : object.getString("progress"));
		if (this.mTSProgress > 100){
			this.mTSProgress = 100;
		}
		
		this.mTSCreateTime = object.getString("createDate");
		this.mUpdateTime = (long) 0;
	}
	
	public String getID(){
		return mTSId;
	}

	public String getTitle(){
		return mTSTitle;
	}

	public String getNote(){
		return mTSNote;
	}

	public String getStartTime(){
		return mTSStartTime;
	}

	public String getEndTime(){
		return mTSEndTime;
	}

	public String getRemindTime1(){
		return mTSRemindTime1;
	}

	public int getRepeatType1(){
		return mTSRepeatType1;
	}

	public int getOnoff1(){
		return mTSRemindOnoff1;
	}

	public String getRemindTime2(){
		return mTSRemindTime2;
	}

	public int getRepeatType2(){
		return mTSRepeatType2;
	}

	public int getOnoff2(){
		return mTSRemindOnoff2;
	}

	public int getConcentTime(){
		return mTSConcentTime;
	}

	public String getCompleteWords(){
		return mTSCompleteWords;
	}
	
	public float getProgress(){
		return mTSProgress;
	}
	
	public String getCreateTime(){
		return mTSCreateTime;
	}
	
	public long getUpdateTime(){
		return mUpdateTime;
	}
	
	public int getType(){
		return mType;
	}
	
	public int getUpdateCount(){
		return mUpdateCount;
	}
	
	public String getBGImage(){
		return mBGImage;
	}
	
	public void setUpdateCount(int updateCount){
		mUpdateCount = updateCount;
	}
	
	public void setType(int type){
		mType = type;
	}
	
	public void setUpdateTime(long updateTime){
		mUpdateTime = updateTime;
	}
	
	public void setID(String id){
		mTSId = id;
	}

	public void setTitle(String title){
		mTSTitle = title;
	}

	public void setNote(String note){
		mTSNote = note;
	}

	public void setStartTime(String time){
		mTSStartTime = time;
	}

	public void setEndTime(String time){
		mTSEndTime = time;
	}

	public void setRemindTime1(String time){
		mTSRemindTime1 = time;
	}

	public void setRepeatType1(int repeat){
		mTSRepeatType1 = repeat;
	}

	public void setOnoff1(int onoff){
		mTSRemindOnoff1 = onoff;
	}

	public void setRemindTime2(String time){
		mTSRemindTime2 = time;
	}

	public void setRepeatType2(int repeat){
		mTSRepeatType2 = repeat;
	}

	public void setOnoff2(int onoff){
		mTSRemindOnoff2 = onoff;
	}

	public void setConcentTime(int time){
		mTSConcentTime = time;
	}

	public void setCompleteWords(String words){
		mTSCompleteWords = words;
	}

	public void setProgress(float progress){
		mTSProgress = progress;
	}
	
	public void setCreateTime(String createtime){
		mTSCreateTime = createtime;
	}
	
	public void setBGImage(String imagepath){
		mBGImage = imagepath;
	}
}

