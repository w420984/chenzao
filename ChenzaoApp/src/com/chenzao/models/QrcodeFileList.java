package com.chenzao.models;

import java.util.ArrayList;
import java.util.List;

public class QrcodeFileList {
	public String mMonth;
	public List<QrcodeFile> mFileList;
	
	public QrcodeFileList(String month, List<QrcodeFile> list){
		mMonth = month;
		mFileList = list;
		if (month == null){
			mMonth = "";
		}
		if (list == null){
			mFileList = new ArrayList<QrcodeFile>();
		}
	}
}
