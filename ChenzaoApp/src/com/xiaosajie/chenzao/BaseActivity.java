package com.xiaosajie.chenzao;


import com.chenzao.utils.Utils;
import com.chenzao.view.BaseLayout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public abstract class BaseActivity extends Activity implements OnClickListener{
	public final static int RIGHT_BUTTON = 0;
	public final static int LEFT_BUTTON = 1;
	protected BaseLayout ly;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	protected abstract void handleTitleBarEvent(int eventId);

	@Override
	public void onClick(View v) {
		if (v == this.ly.leftButton) {
			handleTitleBarEvent(LEFT_BUTTON);
		}
		else if (v == this.ly.rightButton) {
			handleTitleBarEvent(RIGHT_BUTTON);
		}
	}

	protected void setTitleBar(String left, String middle,
			String right) {
		if (ly != null) {
			if (TextUtils.isEmpty(left) &&
					TextUtils.isEmpty(middle) &&
					TextUtils.isEmpty(right)){
				ly.titlebar.setVisibility(View.GONE);
			}else{				
				ly.setButtonTypeAndInfo(left, middle, right);
			}
		}
	}
	
	protected void setView(int resId) {
		ly = new BaseLayout(this, resId);
		setContentView(ly);
		ly.leftButton.setOnClickListener(this);
		ly.rightButton.setOnClickListener(this);
	}
	

	
}
