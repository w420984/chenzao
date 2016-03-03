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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePassword extends BaseActivity {
	private EditText etOldPassWord;
	private EditText etNewPassWord;
	private EditText etRePassWord;
	private Button mSubmit;
	private String mOldPassWord;
	private String mNewPassWord;
	private String mRePassWord;
	private ChangePassWordTask mTask;
	private CustomToast mProgressDialog;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("ChangePasswordActivity"); 
		MobclickAgent.onResume(this);
	}

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
		setView(R.layout.change_password);
		super.onCreate(savedInstanceState);
		setTitleBar(getString(R.string.back), getString(R.string.changePsw), null);
		
		etOldPassWord = (EditText)findViewById(R.id.etOldPd);
		etNewPassWord = (EditText)findViewById(R.id.etNewPd);
		etRePassWord = (EditText)findViewById(R.id.etRePwd);
		mSubmit = (Button)findViewById(R.id.bnSubmit);
		mSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOldPassWord = etOldPassWord.getText().toString();
				mNewPassWord = etNewPassWord.getText().toString();
				mRePassWord = etRePassWord.getText().toString();
				doSumit();
			}
		});
	}

	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
		MobclickAgent.onPageEnd("ChangePasswordActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}

	private boolean checkInput(){
		if (TextUtils.isEmpty(mOldPassWord) ||
				TextUtils.isEmpty(mNewPassWord) || 
				TextUtils.isEmpty(mRePassWord)){
			Utils.showToast(ChangePassword.this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return false;
		}
		if (!mNewPassWord.equals(mRePassWord)){
			Utils.showToast(ChangePassword.this, R.string.repassword_error, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	private void doSumit(){
		if (!checkInput()){
			return;
		}
		
		mTask = new ChangePassWordTask();
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
	
	class ChangePassWordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ChangePassword.this).
						changePassWord(Utils.getMyUid(ChangePassword.this), mOldPassWord, mNewPassWord, mRePassWord);
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
					Utils.showToast(ChangePassword.this, R.string.change_password_failed, Toast.LENGTH_SHORT);
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
				Utils.showToast(ChangePassword.this, R.string.change_password_success, Toast.LENGTH_SHORT);
				startActivity(new Intent(ChangePassword.this, LoginActivity.class));
				finish();
        	}else{
				Utils.showToast(ChangePassword.this, R.string.change_password_failed, Toast.LENGTH_SHORT);
        	}
		}
		
	}	
}
