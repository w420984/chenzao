package com.chenzao.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;

public class QrcodeFile  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1646360219968106913L;
	private String mId;
	private String mContent;
	private String mSound;
	private List<String> mPics;
	private MyDate mDate;
	
	public QrcodeFile(){
		mId = "";
		mContent = "";
		mSound = "";
		mPics = new ArrayList<String>();
		mDate = null;
	}
	
	public QrcodeFile(String id, String content, String sound, List<String> pics, MyDate date){
		mId = id;
		mContent = content;
		mSound = sound;
		mPics = pics;
		mDate = date;
	}
	
	public QrcodeFile(Context context, JSONObject object){
		if (object == null){
			mId = "";
			mContent = "";
			mSound = "";
			mPics = new ArrayList<String>();
			mDate = null;
			return;
		}
		mId = object.optString("id");
		mDate = new MyDate(context, object.optString("publishDate"));
		mPics = new ArrayList<String>();
		for (int i=0; i<Constants.MAX_UPLOAD_PIC_NUM; i++){
			String picPath = object.optString("image"+(i+1));
			if (!TextUtils.isEmpty(picPath) && !"null".equals(picPath)){
				mPics.add(picPath);
			}
		}
		mContent = object.optString("content");
		mSound = object.optString("sound");
	}
	
	public void setId(String id){
		mId = id;
	}
	
	public void setContent(String content){
		mContent = content;
	}
	
	public void setSound(String sound){
		mSound = sound;
	}
	
	public void setPics(List<String> pics){
		mPics = pics;
	}
	
	public void setDate(MyDate date){
		mDate = date;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getContent(){
		return mContent;
	}
	
	public String getSound(){
		return mSound;
	}
	
	public List<String> getPics(){
		return mPics;
	}
	
	public MyDate getDate(){
		return mDate;
	}
}
