package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CircleView extends RelativeLayout {
	private TextView mPercent;
	private TextView mSchedule;
	private ImageView mCircle;
	private ViewGroup mBubble;
	public boolean mIsSelect = false;
	private CircleClickListener mListener;

	public CircleView(Context context) {
		super(context);
		init();
	}
	
	public CircleView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	private void init(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.circle_view, this);
    	
    	mBubble = (ViewGroup)findViewById(R.id.rlPercent);
    	mPercent = (TextView)findViewById(R.id.percent);
    	mSchedule = (TextView)findViewById(R.id.text);
    	mCircle = (ImageView)findViewById(R.id.circle);
    	setPercent(mIsSelect);
    	mCircle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsSelect = true;
				setPercent(mIsSelect);
				if (mListener != null){
					mListener.onCircleClick(CircleView.this);
				}
			}
		});
	}
	
	public void setSchedule(String str){
		mSchedule.setText(str);
	}
	
	public String getSchedule(){
		return mSchedule.getText().toString();
	}
	
	public void setPercent(boolean isSelect){
		String str = mSchedule.getText() + "%";
		mPercent.setText(str);
    	if (isSelect){
    		mBubble.setVisibility(View.VISIBLE);
    		mCircle.setImageResource(R.drawable.update_circle_sel);
    	}else{
    		mBubble.setVisibility(View.INVISIBLE);
    		mCircle.setImageResource(R.drawable.update_circle);
    	}
	}
	
	public void setCircleListener(CircleClickListener listener){
		mListener = listener;
	}
	
	public interface CircleClickListener{
		public void onCircleClick(View view);
	}
}
