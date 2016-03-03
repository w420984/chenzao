package com.xiaosajie.chenzao;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgetPassword extends BaseActivity {
	private Button mSubmit;
	private EditText etUserName;
	private EditText etEmail;
	private String mUserName;
	private String mEmail;
	private CustomToast mProgressDialog;
	private ForgetPassWordTask mTask;
	public static String USERNAME = "USERNAME";

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
		MobclickAgent.onPageEnd("ForgetPasswordActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.forget_password);
		setTitleBar(getString(R.string.back), getString(R.string.setNewPsw), null);

		etUserName = (EditText)findViewById(R.id.etUserName);
		etEmail = (EditText)findViewById(R.id.etEmail);
		mSubmit = (Button)findViewById(R.id.bnSubmit);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == mSubmit){
			mUserName = etUserName.getText().toString();
			mEmail = etEmail.getText().toString();
			doSubmit();
			return;
		}
		super.onClick(v);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("ForgetPasswordActivity");
		MobclickAgent.onResume(this);
	}

	private boolean checkInput(){
		if (TextUtils.isEmpty(mUserName) ||
				TextUtils.isEmpty(mEmail)){
			Utils.showToast(ForgetPassword.this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	private void doSubmit(){
		if (!checkInput()){
			return;
		}
		mTask = new ForgetPassWordTask();
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
	
	class ForgetPassWordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ForgetPassword.this).forgetPassword(mUserName, mEmail);
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
					Utils.showToast(ForgetPassword.this, R.string.sent_captcha_failed, Toast.LENGTH_SHORT);
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
			if (!TextUtils.isEmpty(suc)){
	        	startActivity(new Intent(ForgetPassword.this, SetNewPassword.class).
	        			putExtra(USERNAME, mUserName));
				finish();
			}else{
				Utils.showToast(ForgetPassword.this, R.string.sent_captcha_failed, Toast.LENGTH_SHORT);
			}
		}
		
	}
}
