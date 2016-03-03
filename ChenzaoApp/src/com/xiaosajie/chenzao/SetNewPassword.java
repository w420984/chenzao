package com.xiaosajie.chenzao;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetNewPassword extends BaseActivity {
	private EditText etCaptcha;
	private EditText etNewPassWord;
	private EditText etRePassWord;
	private Button mSubmit;
	private String mUserName;
	private String mCaptcha;
	private String mNewPassWord;
	private String mRePassWord;
	private ResetPassWordTask mTask;
	private CustomToast mProgressDialog;
	
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
		super.onCreate(savedInstanceState);
		setView(R.layout.set_new_password);
		setTitleBar(getString(R.string.back), getString(R.string.setNewPsw), null);
		
		mUserName = getIntent().getExtras().getString(ForgetPassword.USERNAME);
		etCaptcha = (EditText)findViewById(R.id.etCaptcha);
		etNewPassWord = (EditText)findViewById(R.id.etNewPd);
		etRePassWord = (EditText)findViewById(R.id.etRePwd);
		mSubmit = (Button)findViewById(R.id.bnSubmit);
		mSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCaptcha = etCaptcha.getText().toString();
				mNewPassWord = etNewPassWord.getText().toString();
				mRePassWord = etRePassWord.getText().toString();
				doSumit();
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
		MobclickAgent.onPageEnd("SetNewPasswordActiviy"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("SetNewPasswordActiviy");
		MobclickAgent.onResume(this);
}

	private boolean checkInput(){
		if (TextUtils.isEmpty(mCaptcha) ||
				TextUtils.isEmpty(mNewPassWord) || 
				TextUtils.isEmpty(mRePassWord)){
			Utils.showToast(SetNewPassword.this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return false;
		}
		if (!mNewPassWord.equals(mRePassWord)){
			Utils.showToast(SetNewPassword.this, R.string.repassword_error, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	private void doSumit(){
		if (!checkInput()){
			return;
		}
		
		mTask = new ResetPassWordTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
		}
		showProgress();
	}

	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
	
	class ResetPassWordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(SetNewPassword.this).
						resetPassWord(mUserName, mCaptcha, mNewPassWord, mRePassWord);
			} catch (ChenzaoIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(SetNewPassword.this, R.string.reset_password_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject object = null;
			String suc = null;
			try {
				object = new JSONObject(result);
				suc = object.optString("result");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
        	if ("success".equals(suc)){
				Utils.showToast(SetNewPassword.this, R.string.reset_password_success, Toast.LENGTH_SHORT);
				finish();
        	}else{
				Utils.showToast(SetNewPassword.this, R.string.reset_password_failed, Toast.LENGTH_SHORT);
        	}
		}
		
	}
}
