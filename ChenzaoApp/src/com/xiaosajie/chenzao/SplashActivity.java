package com.xiaosajie.chenzao;

import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.R;
import com.chenzao.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //友盟数据统计
        MobclickAgent.openActivityDurationTrack(false);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				goNextScreen();
			}
		}, 3000);
    }

    private void goNextScreen(){
    	if (needLogin()){
        	startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        	//startActivity(new Intent(getApplicationContext(), ChangePassword.class));
    	}else{
    		MainActivity.mMode = MainActivity.MODE_OFFICIAL_TASK;
        	startActivity(new Intent(getApplicationContext(), MainActivity.class));    		
    	}
    	finish();
    	
    }
        
    private boolean needLogin(){
    	//return true;
    	return TextUtils.isEmpty(Utils.getMyUid(this));
    }
    
}
