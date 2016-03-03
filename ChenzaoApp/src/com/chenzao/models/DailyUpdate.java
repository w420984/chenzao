package com.chenzao.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.chenzao.utils.Constants;

import android.content.Context;
import android.text.TextUtils;

public class DailyUpdate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6285935614841843903L;

	private String mId;
	private String mScheduleId;
	private String mContent;
	private String mDailyrate;
	private List<String> mPics;
	private MyDate mDate;
	private String mWechat;
	private int mUpdateIndex; //第多少次更新
	private String mCustomize;
	
	public DailyUpdate(){
		mId = "";
		mScheduleId = "";
		mContent = "";
		mDailyrate = "";
		mDate = null;
		mWechat = "";
		mPics = new ArrayList<String>();
		mUpdateIndex = 0;
		mCustomize = "";
	}
	
	public DailyUpdate(Context context, JSONObject object){
		if (object == null){
			mId = "";
			mScheduleId = "";
			mContent = "";
			mDailyrate = "";
			mDate = null;
			mWechat = "";
			mPics = new ArrayList<String>();
			mUpdateIndex = 0;
			mCustomize = "";
			return;
		}
		
		mId = object.optString("id");
		mScheduleId = object.optString("scheduleId");
		mContent = object.optString("content");
		mDate = new MyDate(context, object.optString("updatedate"));
		mWechat = object.optString("wechat");
		mDailyrate = object.optString("dailyrate");
		mCustomize = object.optString("customize");
		String updateindex = object.optString("index");
		if (TextUtils.isEmpty(updateindex)){
			updateindex = "0";
		}
		mUpdateIndex = Integer.valueOf(updateindex);
		mPics = new ArrayList<String>();
		for (int i=0; i<Constants.MAX_UPLOAD_PIC_NUM; i++){
			String picPath = object.optString("image"+(i+1));
			if (!TextUtils.isEmpty(picPath) && !"null".equals(picPath)){
				mPics.add(picPath);
			}
		}
	}
	
	public void setId(String id){
		mId = id;
	}
	
	public void setScheduleId(String scheduleId){
		mScheduleId = scheduleId;
	}
	
	public void setContent(String content){
		mContent = content;
	}
	
	public void setDailyrate(String dailyrate){
		mDailyrate = dailyrate;
	}
	
	public void setWechat(String wechat){
		mWechat = wechat;
	}
	
	public void setDate(MyDate date){
		mDate = date;
	}
	
	public void setPics(List<String> pics){
		mPics = pics;
	}
	
	public void setUpdateIndex(int index){
		mUpdateIndex = index;
	}
	
	public void setCustomize(String customize){
		mCustomize = customize;
	}
	
	public String getCustomize(){
		return mCustomize;
	}
	
	public int getUpdateIndex(){
		return mUpdateIndex;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getScheduleId(){
		return mScheduleId;
	}
	
	public String getContent(){
		return mContent;
	}
	
	public String getDailyrate(){
		return mDailyrate;
	}
	
	public String getWechat(){
		return mWechat;
	}
	
	public MyDate getDate(){
		return mDate;
	}
	
	public List<String> getPics(){
		return mPics;
	}
}
