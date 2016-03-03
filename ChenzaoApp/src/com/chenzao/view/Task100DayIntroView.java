package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class Task100DayIntroView extends RelativeLayout {

	public Task100DayIntroView(Context context) {
		super(context);
		init();
	}

	public Task100DayIntroView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	private void init(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.task_100day_intro, this);
	}
}
