package com.chenzao.models;

import java.util.ArrayList;
import java.util.List;

public class DailyUpdateList {
	public String mMonth;
	public List<DailyUpdate> mFileList;
	
	public DailyUpdateList(String month, List<DailyUpdate> list){
		mMonth = month;
		mFileList = list;
		if (month == null){
			mMonth = "";
		}
		if (list == null){
			mFileList = new ArrayList<DailyUpdate>();
		}
	}
}
