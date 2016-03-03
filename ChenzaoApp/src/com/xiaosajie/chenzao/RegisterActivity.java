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
import com.xiaosajie.chenzao.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity {
	private EditText etUserName;
	private EditText etPassWord;
	private EditText etRePassWord;
	private EditText etEmail;
	private Button btnSumit;
	private String mUserName;
	private String mPassWord;
	private String mRePassWord;
	private String mEmail;
	private RegisterTask mTask;
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
		setView(R.layout.regist);
		setTitleBar(getString(R.string.back), getString(R.string.regist), null);
		
		etUserName = (EditText) findViewById(R.id.etUsername);
		etPassWord = (EditText) findViewById(R.id.etPwd);
		etRePassWord = (EditText) findViewById(R.id.etRePwd);
		etEmail = (EditText) findViewById(R.id.etEmail);
		btnSumit = (Button) findViewById(R.id.bnSubmit);
		
		btnSumit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mUserName = etUserName.getText().toString();
				mPassWord = etPassWord.getText().toString();
				mRePassWord = etRePassWord.getText().toString();
				mEmail = etEmail.getText().toString();
				
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
		MobclickAgent.onPageEnd("RegisterActivity"); 
		MobclickAgent.onPause(this);


	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("RegisterActivity");
		MobclickAgent.onResume(this);
	}
	
	private boolean checkInput(){
		if (TextUtils.isEmpty(mUserName) ||
				TextUtils.isEmpty(mPassWord) || 
				TextUtils.isEmpty(mRePassWord) ||
				TextUtils.isEmpty(mEmail)){
			Utils.showToast(RegisterActivity.this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return false;
		}
		if (!mPassWord.equals(mRePassWord)){
			Utils.showToast(RegisterActivity.this, R.string.repassword_error, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	private void doSumit(){
		if (!checkInput()){
			return;
		}
		
		mTask = new RegisterTask();
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
	
	class RegisterTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result =  NetEngine.getInstance(RegisterActivity.this).
						register(mUserName, mPassWord, mRePassWord, mEmail);
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
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(RegisterActivity.this, R.string.register_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject object = null;
			String uid = null;
			try {
				object = new JSONObject(result);
				uid = object.optString("uuid");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
			if (!TextUtils.isEmpty(uid)){
				Intent intent = new Intent();
				intent.putExtra(Constants.MYUID, uid);
				setResult(RESULT_OK, intent);
				finish();
			}else{
				Utils.showToast(RegisterActivity.this, R.string.register_failed, Toast.LENGTH_SHORT);
			}
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}
		
	}
}
