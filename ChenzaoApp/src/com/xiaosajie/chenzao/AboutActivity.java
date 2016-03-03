package com.xiaosajie.chenzao;

import com.umeng.analytics.MobclickAgent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends BaseActivity{
	TextView mVersion;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setView(R.layout.about);
		setTitleBar(getString(R.string.back), getString(R.string.about_us), "");
		mVersion = (TextView)findViewById(R.id.about_version);
		PackageInfo info;
		String nowVersion = "";
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            nowVersion = info.versionName;
        } catch (NameNotFoundException e) {
            
        }
        mVersion.setText(getString(R.string.version)+nowVersion);
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
		MobclickAgent.onPageEnd("AboutActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("AboutActivity"); 
		MobclickAgent.onResume(this);
	}

}
