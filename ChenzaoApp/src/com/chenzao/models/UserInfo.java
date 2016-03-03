package com.chenzao.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.utils.Utils;

import android.content.Context;

public class UserInfo {
	private String mUuid;
	private String mNickName;
	private int mAge;
	private int mGender;
	private String mConstellation;
	private String mExpactWords;
	
	public UserInfo(Context context){
		mUuid = Utils.getMyUid(context);
		mNickName = "";
		mAge = 0;
		mConstellation = "";
		mExpactWords = "";
		mGender = 0;
		
	}
	
	public UserInfo(String uuid, String nick, int gender, int age, String constellation, String expacting){
		mUuid = uuid;
		mNickName = nick;
		mAge = age;
		mConstellation = constellation;
		mExpactWords = expacting;
		mGender = gender;
	}
	
	public UserInfo(JSONObject object) throws JSONException{
		mUuid = object.getString("userId");
		mNickName = object.getString("nickName");
		mExpactWords = object.getString("dream");
		
		String string = object.getString("age");
		if(string != null && !"".equals(string)){
			mAge = Integer.parseInt(string);
		}else{
			mAge = -1;
		}
		
		string = object.getString("sex");
		if(string != null && !"".equals(string)){
			mGender = Integer.parseInt(string);
		}else{
			mGender = 0;
		}
		
		mConstellation = object.getString("astro");
	}

	public String getUuid(){
		return mUuid;
	}

	public String getNickName(){
		return mNickName;
	}

	public int getAge(){
		return mAge;
	}

	public int getGender(){
		return mGender;
	}

	public String getExpactWords(){
		return mExpactWords;
	}

	public String getConstellation(){
		return mConstellation;
	}
	
	public void setUuid(String uuid){
		mUuid = uuid;
	}

	public void setNickName(String nick){
		mNickName = nick;
	}

	public void setAge(int age){
		mAge = age;
	}

	public void setGender(int age){
		mGender = age;
	}

	public void setExpactWords(String expact){
		mExpactWords = expact;
	}

	public void setConstellation(String constellation){
		mConstellation = constellation;
	}
}
