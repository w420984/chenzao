package com.xiaosajie.chenzao;


import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.R;
import com.chenzao.db.ChenzaoDBUserInfoAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.UserInfo;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

	public static int REGISTER = 1;
	private AutoCompleteTextView etUsrname;
	private EditText etPassword;
	private Button bnLogin;
	private TextView tvRegist;
	private TextView tvForgetPsw;
	private String mUserName;
	private String mPassWord;
	private CustomToast mProgressDialog;
	private LoginTask mLoginTask;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			Utils.exitApp(this);
			break;
		}


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.login);
		setTitleBar(null, null, null);
		etPassword = (EditText) findViewById(R.id.etLoginPsw);
		etUsrname = (AutoCompleteTextView) findViewById(R.id.etLoginUsername);
		tvForgetPsw = (TextView) findViewById(R.id.tvForgetpsw);
		tvForgetPsw.setOnClickListener(this);
		bnLogin = (Button) findViewById(R.id.btnLogin);
		tvRegist = (TextView) findViewById(R.id.tvRegist);
//		
		bnLogin.setOnClickListener(this);
		tvRegist.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v == tvForgetPsw){
        	startActivity(new Intent(LoginActivity.this, ForgetPassword.class));
			return;
		}else if (v == bnLogin){
			mUserName = etUsrname.getText().toString();
			mPassWord = etPassword.getText().toString();			
			doLogin();
		}else if (v == tvRegist){
			Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivityForResult(intent, REGISTER);
		}
	}

	@Override
	protected void onDestroy() {
		if (mLoginTask != null && mLoginTask.getStatus() == AsyncTask.Status.RUNNING){
			mLoginTask.cancel(true);
			mLoginTask = null;
		}
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("LoginActivity");
		MobclickAgent.onResume(this);		
	}

	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
		MobclickAgent.onPageEnd("LoginActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == REGISTER){
				if (data != null){
					String uid = data.getStringExtra(Constants.MYUID);
					fetchUserInfo();
					Utils.setMyUid(this, uid);
					startActivity(new Intent(this, MainActivity.class));
					finish();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void doLogin(){
		if (!checkInput()){
			return ;
		}
		mLoginTask = new LoginTask();
		try {
			mLoginTask.execute();
		} catch (RejectedExecutionException e) {
		}
		showProgress();
	}
	
	private boolean checkInput(){
		if (TextUtils.isEmpty(mUserName) || TextUtils.isEmpty(mPassWord)){
			Utils.showToast(this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
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
	
	class LoginTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(LoginActivity.this).login(mUserName, mPassWord);
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
					Utils.showToast(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT);
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
				
				fetchUserInfo();
				Utils.setMyUid(LoginActivity.this, uid);
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();		
			}else{
				Utils.showToast(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT);
			}
		}

	}
	
	private void fetchUserInfo(){
		//get user info
		ChenzaoDBUserInfoAdapter userDB = new ChenzaoDBUserInfoAdapter(LoginActivity.this);
		userDB.open();
		List<UserInfo> userList = userDB.getUserList();
		int connectNet = 0;
		if(userList.size() == 0){
			connectNet = 1;
		}else{
			UserInfo c = userDB.getUserInfoByUuid(Utils.getMyUid(LoginActivity.this));
			if(c == null){
				connectNet = 1;
			}
		}
		userDB.close();
		if(connectNet == 1){
			FetchUserInfoTask task = new FetchUserInfoTask();
			try {
				task.execute();
			} catch (RejectedExecutionException e) {
			}
		}
	}
	
	class FetchUserInfoTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(getApplicationContext()).getUserInfo(Utils.getMyUid(getApplicationContext()));
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
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
//					Utils.handleErrorEvent(mThr, getActivity().getApplicationContext());
				}else{
//					Utils.showToast(getActivity().getApplicationContext(), R.string.get_userinfo_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject object = null;
			try {
				object = new JSONObject(result);

				String juser = object.optString("user");

				if(juser != null && !juser.equals("")){
					UserInfo user_info = new UserInfo(new JSONObject(juser));
					if(user_info != null){
						ChenzaoDBUserInfoAdapter userDB = new ChenzaoDBUserInfoAdapter(getApplicationContext());
						userDB.open();
						userDB.insert(user_info);
						userDB.close();
						
						Intent successIntent = new Intent(Constants.UPDATE_USER_INFO_DISPLAY);
						sendBroadcast(successIntent);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();

				return;
			}
			
		}
		
	}

}
