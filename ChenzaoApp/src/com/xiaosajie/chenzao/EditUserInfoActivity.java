package com.xiaosajie.chenzao;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.db.ChenzaoDBUserInfoAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.UserInfo;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditUserInfoActivity extends BaseActivity{
	public static final int SHOW_AGE_PICKER = 1000;
	public static final int SHOW_CONSTELLATION_PICKER = 1001;
	private ImageView mImgFamale;
	private ImageView mImgMale;
	private TextView mTvAge;
	private TextView mTvConstellation;
	private EditText mEVExpact;
	private UserInfo mUserInfo;
	private CustomToast mProgressDialog;
	private UpdateUserInfoTask mLoginTask;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			mUserInfo.setExpactWords(mEVExpact.getText().toString());
			mLoginTask = new UpdateUserInfoTask();
			try {
				mLoginTask.execute();
			} catch (RejectedExecutionException e) {
			}
			showProgress();
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
		setView(R.layout.edit_user_info);
		setTitleBar(getString(R.string.back), getString(R.string.user_info), getString(R.string.save));
		
		ChenzaoDBUserInfoAdapter userDB = new ChenzaoDBUserInfoAdapter(getApplicationContext());
		userDB.open();
		List<UserInfo> userList = userDB.getUserList();
		if(userList.size() > 0){
			mUserInfo =  userDB.getUserInfoByUuid(Utils.getMyUid(EditUserInfoActivity.this));
		}		
		userDB.close();
		if (mUserInfo == null){
			mUserInfo = new UserInfo(this);
		}
		mImgFamale = (ImageView)findViewById(R.id.iv_gender_female);
		mImgMale = (ImageView)findViewById(R.id.iv_gender_male);
		mTvAge = (TextView)findViewById(R.id.tv_my_age);
		mTvConstellation = (TextView)findViewById(R.id.tv_my_constellation);
		mEVExpact = (EditText)findViewById(R.id.individuality_signature);
		
		if(mUserInfo.getGender() == 0){
			mImgFamale.setImageResource(R.drawable.settings_f_icon_sel);
			mImgMale.setImageResource(R.drawable.settings_m_icon);
		}else{
			mImgFamale.setImageResource(R.drawable.settings_f_icon);
			mImgMale.setImageResource(R.drawable.settings_m_icon_sel);
		}
		
		if(mUserInfo.getAge() == -1){
			mTvAge.setText(R.string.please_select);
		}else{
			mTvAge.setText(String.valueOf(mUserInfo.getAge()));
		}
		
		if(mUserInfo.getConstellation().equals("")){
			mTvConstellation.setText(R.string.please_select);
		}else{
			mTvConstellation.setText(mUserInfo.getConstellation());
		}
		
		if(!mUserInfo.getExpactWords().equals("")){
			mEVExpact.setText(mUserInfo.getExpactWords());
		}
	
		mImgFamale.setOnClickListener(this);
		mImgMale.setOnClickListener(this);
		mTvAge.setOnClickListener(this);
		mTvConstellation.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		
		if(v == mImgFamale || v == mImgMale){
			if(mUserInfo.getGender() == 0){
				mImgFamale.setImageResource(R.drawable.settings_f_icon);
				mImgMale.setImageResource(R.drawable.settings_m_icon_sel);
				mUserInfo.setGender(1);
			}else{
				mImgFamale.setImageResource(R.drawable.settings_f_icon_sel);
				mImgMale.setImageResource(R.drawable.settings_m_icon);
				mUserInfo.setGender(0);
			}			
		}else if(v == mTvAge){
			AgePickerPopWindow agepopwindow = new AgePickerPopWindow(EditUserInfoActivity.this, mUserInfo.getAge());
			agepopwindow.setHandler(mHandler, SHOW_AGE_PICKER);
			agepopwindow.showAtLocation(EditUserInfoActivity.this.findViewById(R.id.rl_edit_user_info),
					Gravity.BOTTOM, 0, 0);
		}else if(v == mTvConstellation){
			int cur_index = 0;
			if(!mUserInfo.getConstellation().equals("")){
				String[] options = getResources().getStringArray(R.array.constellation);
				for(int i = 0; i < options.length; i++){
					if(options[i].equals(mUserInfo.getConstellation())){
						cur_index = i;
						break;
					}
				}
			}
			ConstellationPickerPopWindow constellationpopwindow = new ConstellationPickerPopWindow(EditUserInfoActivity.this, cur_index);
			constellationpopwindow.setHandler(mHandler, SHOW_CONSTELLATION_PICKER);
			constellationpopwindow.showAtLocation(EditUserInfoActivity.this.findViewById(R.id.rl_edit_user_info),
					Gravity.BOTTOM, 0, 0);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mLoginTask != null && mLoginTask.getStatus() == AsyncTask.Status.RUNNING){
			mLoginTask.cancel(true);
			mLoginTask = null;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("EditUserInfoActivity"); 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("EditUserInfoActivity"); 
		MobclickAgent.onResume(this);
	}

	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {

			switch(msg.what)
			{
				case SHOW_AGE_PICKER:
				{
					mTvAge.setText(String.valueOf(msg.arg1));
					mUserInfo.setAge(msg.arg1);
					break;
				}
				case SHOW_CONSTELLATION_PICKER:
				{
					mTvConstellation.setText((String)msg.obj);
					mUserInfo.setConstellation((String)msg.obj);
					break;
				}
			}
		}
		
	};
	
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
	
	class UpdateUserInfoTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(EditUserInfoActivity.this).updateUserInfo(mUserInfo);
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
					Utils.showToast(EditUserInfoActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
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
						userDB.update(user_info);
						userDB.close();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Utils.showToast(EditUserInfoActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
				return;
			}
			Intent successIntent = new Intent(Constants.UPDATE_USER_INFO_DISPLAY);
			sendBroadcast(successIntent);
			
			finish();
		}

	}
	
}
