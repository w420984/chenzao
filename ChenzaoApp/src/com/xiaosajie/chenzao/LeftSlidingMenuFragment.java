package com.xiaosajie.chenzao;

import java.util.List;

import com.chenzao.db.ChenzaoDBUserInfoAdapter;
import com.chenzao.models.UserInfo;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.chenzao.view.MenuItemView;
import com.chenzao.view.RoundedImageView;
import com.chenzao.zxing.CaptureActivity;
import com.xiaosajie.chenzao.FragmentDefaultMain.TaskSchedulerReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
/**
 * 主要控制左边按钮点击事件
 * @author Administrator
 *
 */
public class LeftSlidingMenuFragment extends Fragment implements OnClickListener{
	private View mInfoLayout;
	private MenuItemView mQrcodeLayout;
	private MenuItemView mOwnTaskLayout;
	private MenuItemView mOfficialTaskLayout;
	private MenuItemView mFileLayout;
	private MenuItemView mWebLayout;
	private MenuItemView mStoreLayout;
	private MenuItemView mSettingLayout;
	private RoundedImageView roundedImageView;
	private MainActivity mActivity;
	private TextView mNickName;
	private TextView mSignature;
	private UpdateUserInfoDisplayReceiver mUpdateUserInfoDisplayReceiver;
	
     @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    }
     
     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
			mActivity = (MainActivity) getActivity();
    	 View view = inflater.inflate(R.layout.main_left_fragment, container,false);
    	  mInfoLayout = view.findViewById(R.id.item_info);
    	  mInfoLayout.setOnClickListener(this);

    	  mQrcodeLayout = (MenuItemView) view.findViewById(R.id.item_qrcode);
    	  mQrcodeLayout.setItemImage(R.drawable.icon_scan_selector);
    	  mQrcodeLayout.setItemTitle(R.string.scanning);
    	  mQrcodeLayout.setOnClickListener(this);
    	 
    	  mOwnTaskLayout = (MenuItemView) view.findViewById(R.id.item_own_task);
    	  mOwnTaskLayout.setItemImage(R.drawable.icon_owntask_selector);
    	  mOwnTaskLayout.setItemTitle(R.string.personal_task);
    	  mOwnTaskLayout.setOnClickListener(this);
    	 
    	  mOfficialTaskLayout = (MenuItemView) view.findViewById(R.id.item_official_task);
    	  mOfficialTaskLayout.setItemImage(R.drawable.icon_officialtask_selector);
    	  mOfficialTaskLayout.setItemTitle(R.string.official_task);
    	  mOfficialTaskLayout.setNotice("1");
    	  mOfficialTaskLayout.setOnClickListener(this);
    	 
    	  mFileLayout = (MenuItemView) view.findViewById(R.id.item_file);
    	  mFileLayout.setItemImage(R.drawable.icon_file_selector);
    	  mFileLayout.setItemTitle(R.string.qrcode_list);
    	  mFileLayout.setOnClickListener(this);
    	 
    	  mWebLayout = (MenuItemView) view.findViewById(R.id.item_chenzao_web);
    	  mWebLayout.setItemImage(R.drawable.icon_website_selector);
    	  mWebLayout.setItemTitle(R.string.chenzao_web);
    	  mWebLayout.setOnClickListener(this);
    	  
    	  mStoreLayout = (MenuItemView) view.findViewById(R.id.item_chenzao_store);
    	  mStoreLayout.setItemImage(R.drawable.icon_store_selector);
    	  mStoreLayout.setItemTitle(R.string.chenzao_store);
    	  mStoreLayout.setOnClickListener(this);
    	  
    	  mSettingLayout = (MenuItemView) view.findViewById(R.id.item_setting);
    	  mSettingLayout.setItemImage(R.drawable.icon_setting_selector);
    	  mSettingLayout.setItemTitle(R.string.setting);
    	  mSettingLayout.setOnClickListener(this);
    	  
    	  roundedImageView = (RoundedImageView)view.findViewById(R.id.headImageView);
    	  roundedImageView.setOnClickListener(this);
    	  
    	  mNickName = (TextView)view.findViewById(R.id.nickNameTextView);
    	  mSignature = (TextView)view.findViewById(R.id.sign);

    	  ChenzaoDBUserInfoAdapter userDB = new ChenzaoDBUserInfoAdapter(getActivity());
    	  userDB.open();
    	  List<UserInfo> userList = userDB.getUserList();
    	  if(userList.size() > 0){
        	  UserInfo user =  userDB.getUserInfoByUuid(Utils.getMyUid(getActivity()));

        	  if(user != null && !user.getNickName().equals("")){
        		  mNickName.setText(user.getNickName());
        	  }else{
        		  mNickName.setText(R.string.default_nick_name);
        	  }
//        	  if(user != null && !user.getExpactWords().equals("")){
//        		  mSignature.setText(user.getExpactWords());
//        	  }else{
//        		  mSignature.setText(R.string.alarm_alert_dialog_content_todo);
//        	  }
    	  }
    	  userDB.close();
    	  
  		IntentFilter intentfilter = new IntentFilter();
  		intentfilter.addAction(Constants.UPDATE_USER_INFO_DISPLAY);
  		mUpdateUserInfoDisplayReceiver = new UpdateUserInfoDisplayReceiver();
  		mActivity.registerReceiver(mUpdateUserInfoDisplayReceiver, intentfilter);

    	  System.out.println();
    	return view;
    }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mUpdateUserInfoDisplayReceiver != null){
			mActivity.unregisterReceiver(mUpdateUserInfoDisplayReceiver);
		}
	}

	@Override
	public void onClick(View v) {
		Fragment newContent = null;
		switch (v.getId()) {
		case R.id.item_info:
			MainActivity.mMode = MainActivity.MODE_INFO;
			newContent = mActivity.mDefaultMain;
			mInfoLayout.setSelected(true);
			mQrcodeLayout.setSelected(false);
			mOwnTaskLayout.setSelected(false);
			mOfficialTaskLayout.setSelected(false);
			mFileLayout.setSelected(false);
			mWebLayout.setSelected(false);
			mStoreLayout.setSelected(false);
			mSettingLayout.setSelected(false);
			break;
		case R.id.item_qrcode:
			Intent scanIntent  = new Intent(getActivity(), CaptureActivity.class);
			startActivity(scanIntent);
//			mInfoLayout.setSelected(false);
//			mQrcodeLayout.setSelected(true);
//			mOwnTaskLayout.setSelected(false);
//			mOfficialTaskLayout.setSelected(false);
//			mFileLayout.setSelected(false);
//			mWebLayout.setSelected(false);
//			mStoreLayout.setSelected(false);
//			mSettingLayout.setSelected(false);
			break;
		case R.id.item_own_task:
			MainActivity.mMode = MainActivity.MODE_PERSONAL_TASK;
			newContent = mActivity.mDefaultMain;
			mInfoLayout.setSelected(false);
			mQrcodeLayout.setSelected(false);
			mOwnTaskLayout.setSelected(true);
			mOfficialTaskLayout.setSelected(false);
			mFileLayout.setSelected(false);
			mWebLayout.setSelected(false);
			mStoreLayout.setSelected(false);
			mSettingLayout.setSelected(false);
		    break;
		case R.id.item_official_task:
			MainActivity.mMode = MainActivity.MODE_OFFICIAL_TASK;
			newContent = mActivity.mOfficialTask;
			mInfoLayout.setSelected(false);
			mQrcodeLayout.setSelected(false);
			mOwnTaskLayout.setSelected(false);
			mOfficialTaskLayout.setSelected(true);
			mFileLayout.setSelected(false);
			mWebLayout.setSelected(false);
			mStoreLayout.setSelected(false);
			mSettingLayout.setSelected(false);
		    break;
		case R.id.item_file:
			MainActivity.mMode = MainActivity.MODE_STORAGELIST;
			newContent = mActivity.mQrcodeList;
			mInfoLayout.setSelected(false);
			mQrcodeLayout.setSelected(false);
			mOwnTaskLayout.setSelected(false);
			mOfficialTaskLayout.setSelected(false);
			mFileLayout.setSelected(true);
			mWebLayout.setSelected(false);
			mStoreLayout.setSelected(false);
			mSettingLayout.setSelected(false);
		    break;
		case R.id.item_chenzao_web:
			Intent webIntent  = new Intent(getActivity(), WebBrowser.class);
			webIntent.putExtra(Constants.WEB_BROWSER_URL, Constants.WEB_BROWSER_CHENZAO_URL);
			startActivity(webIntent);
//			MainActivity.mMode = MainActivity.MODE_WEB;
//			mInfoLayout.setSelected(false);
//			mQrcodeLayout.setSelected(false);
//			mOwnTaskLayout.setSelected(false);
//			mOfficialTaskLayout.setSelected(false);
//			mFileLayout.setSelected(false);
//			mWebLayout.setSelected(true);
//			mStoreLayout.setSelected(false);
//			mSettingLayout.setSelected(false);
			break;
		case R.id.item_chenzao_store:
			Intent storeIntent  = new Intent(getActivity(), WebBrowser.class);
			storeIntent.putExtra(Constants.WEB_BROWSER_URL, Constants.WEB_BROWSER_CHENZAO_STORE_URL);
			startActivity(storeIntent);
//			MainActivity.mMode = MainActivity.MODE_STORE;
//			mInfoLayout.setSelected(false);
//			mQrcodeLayout.setSelected(false);
//			mOwnTaskLayout.setSelected(false);
//			mOfficialTaskLayout.setSelected(false);
//			mFileLayout.setSelected(false);
//			mWebLayout.setSelected(false);
//			mStoreLayout.setSelected(true);
//			mSettingLayout.setSelected(false);
			break;
		case R.id.item_setting:
			MainActivity.mMode = MainActivity.MODE_SETTING;
			newContent = mActivity.mSettings;
			mInfoLayout.setSelected(false);
			mQrcodeLayout.setSelected(false);
			mOwnTaskLayout.setSelected(false);
			mOfficialTaskLayout.setSelected(false);
			mFileLayout.setSelected(false);
			mWebLayout.setSelected(false);
			mStoreLayout.setSelected(false);
			mSettingLayout.setSelected(true);
			break;
		default:
			break;
		}
		
		if (newContent != null)
			switchFragment(newContent);
		
	}
	
	
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
			mActivity.switchContent((Fragment) mActivity.mContent, fragment);
		
	}
	

	public class UpdateUserInfoDisplayReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if(intent == null){
				return;
			}
			
			String action = intent.getAction();
			if(Constants.UPDATE_USER_INFO_DISPLAY.equals(action)){
				ChenzaoDBUserInfoAdapter userDB = new ChenzaoDBUserInfoAdapter(getActivity());
				userDB.open();
				List<UserInfo> userList = userDB.getUserList();
				if(userList.size() > 0){
					UserInfo user =  userDB.getUserInfoByUuid(Utils.getMyUid(getActivity()));
					if(!user.getNickName().equals("")){
						mNickName.setText(user.getNickName());
					}else{
						mNickName.setText(R.string.default_nick_name);
					}
//					if(!user.getExpactWords().equals("")){
//						mSignature.setText(user.getExpactWords());
//					}else{
//						mSignature.setText(R.string.alarm_alert_dialog_content_todo);
//					}
				}
				userDB.close();
			}	
		}
		
	}
}
