package com.xiaosajie.chenzao;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chenzao.utils.Utils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.umeng.update.UpdateStatus;

public class MainActivity extends SlidingFragmentActivity implements
		OnClickListener {
	
	public static final int MODE_MAIN = 0;
	public static final int MODE_OFFICIAL_TASK = 1;
	public static final int MODE_PERSONAL_TASK = 2;
	public static final int MODE_STORAGELIST = 3;
	public static final int MODE_WEB = 4;
	public static final int MODE_STORE = 5;
	public static final int MODE_SETTING = 6;
	public static final int MODE_INFO = 7;
	
	public Fragment mQrcodeList;
	public Fragment mDefaultMain;
	public Fragment mOfficialTask;
	public Fragment mSettings;
	
	protected SlidingMenu leftRightSlidingMenu;
	private ImageButton ivTitleBtnLeft;
	private ImageButton ivTitleBtnRight;
	private TextView titleText;
	public Object mContent;
	public static int mMode = MODE_OFFICIAL_TASK;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mMode == MODE_PERSONAL_TASK){
			mDefaultMain.onActivityResult(requestCode, resultCode, data);
		}else if (mMode == MODE_OFFICIAL_TASK){
			mOfficialTask.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initLeftRightSlidingMenu();
		setContentView(R.layout.main);
		initView();
		UMUpdateCheck();
	}

	protected void setTitleBar(int left, String middle, int right){
		if (left != 0){
			ivTitleBtnLeft.setBackgroundResource(left);
			ivTitleBtnLeft.setVisibility(View.VISIBLE);
		}else{
			ivTitleBtnLeft.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(middle)){
			titleText.setText(middle);
			titleText.setVisibility(View.VISIBLE);
		}else{
			titleText.setVisibility(View.GONE);
		}
		if (right != 0){
			ivTitleBtnRight.setBackgroundResource(right);
			ivTitleBtnRight.setVisibility(View.VISIBLE);
		}else{
			ivTitleBtnRight.setVisibility(View.GONE);
		}
	}

	private void initView() {
		mDefaultMain = new FragmentDefaultMain();
		mQrcodeList = new FragmentQrcodeList();
		mOfficialTask = new FragmentOfficialTask();
		mSettings = new FragmentSettings();
		
		ivTitleBtnLeft = (ImageButton)this.findViewById(R.id.ivTitleBtnLeft);
		ivTitleBtnLeft.setOnClickListener(this);
		ivTitleBtnRight = (ImageButton)this.findViewById(R.id.ivTitleBtnRight);
		ivTitleBtnRight.setOnClickListener(this);
		titleText = (TextView)this.findViewById(R.id.ivTitleName);
	}

	private void initLeftRightSlidingMenu() {
		if (mMode == MODE_PERSONAL_TASK){
			mContent = new FragmentDefaultMain();
		}else if (mMode == MODE_OFFICIAL_TASK){
			mContent = new FragmentOfficialTask();
		}
//				getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, (Fragment)mContent).commit();
		setBehindContentView(R.layout.main_left_layout);
		FragmentTransaction leftFragementTransaction = getSupportFragmentManager().beginTransaction();
		Fragment leftFrag = new LeftSlidingMenuFragment();
		leftFragementTransaction.replace(R.id.main_left_fragment, leftFrag);
		leftFragementTransaction.commit();
		// customize the SlidingMenu
		leftRightSlidingMenu = getSlidingMenu();
		leftRightSlidingMenu.setMode(SlidingMenu.LEFT);// 设置是左滑还是右滑，还是左右都可以滑，我这里只做了左滑
		leftRightSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);// 设置菜单宽度
		leftRightSlidingMenu.setFadeDegree(0.35f);// 设置淡入淡出的比例
		leftRightSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);//设置手势模式
		leftRightSlidingMenu.setShadowDrawable(R.drawable.shadow);// 设置左菜单阴影图片
		leftRightSlidingMenu.setFadeEnabled(true);// 设置滑动时菜单的是否淡入淡出
		leftRightSlidingMenu.setBehindScrollScale(0.333f);// 设置滑动时拖拽效果

//				leftRightSlidingMenu.setSecondaryMenu(R.layout.main_right_layout);
//				FragmentTransaction rightFragementTransaction = getSupportFragmentManager().beginTransaction();
//				Fragment rightFrag = new RightSlidingMenuFragment();
//				leftFragementTransaction.replace(R.id.main_right_fragment, rightFrag);
//				rightFragementTransaction.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivTitleBtnLeft:
			leftRightSlidingMenu.showMenu();
			break;
		case R.id.ivTitleBtnRight:
			if (mContent instanceof FragmentQrcodeList){
				startActivity(new Intent(this, SearchResultActivity.class));
			}else if (mContent instanceof FragmentDefaultMain){
				startActivity(new Intent(this, NewTaskSchedulerActivity.class));
			}
			break;
		default:
			break;
		}

	}
 

	/**
	 *    左侧菜单点击切换首页的内容
	 */

	public void switchContent(Fragment from, Fragment to) {
		//Utils.logd("mContent="+mContent);
		//Utils.logd("mContent="+to);
		if (mContent != to) {
            mContent = to;
            FragmentTransaction transaction = getSupportFragmentManager()
    				.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
				//Utils.logd("!to.isAdded()");
                transaction.hide(from).add(R.id.content_frame, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
				//Utils.logd("to.isAdded()");
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
            if (mContent instanceof FragmentDefaultMain && ((FragmentDefaultMain) mContent).getActivity() != null){
            	((FragmentDefaultMain) mContent).setTitleBar();
            }else if (mContent instanceof FragmentQrcodeList && ((FragmentQrcodeList) mContent).getActivity() != null){
            	((FragmentQrcodeList) mContent).setTitleBar();
            }else if (mContent instanceof FragmentOfficialTask && ((FragmentOfficialTask) mContent).getActivity() != null){
            	((FragmentOfficialTask) mContent).setTitleBar();
            }else if(mContent instanceof FragmentSettings && ((FragmentSettings) mContent).getActivity() != null){
            	((FragmentSettings) mContent).setTitleBar();
            }
        }else{
        	if (mContent instanceof FragmentDefaultMain && ((FragmentDefaultMain) mContent).getActivity() != null){
            	((FragmentDefaultMain) mContent).setTitleBar();
            }else if (mContent instanceof FragmentOfficialTask && ((FragmentOfficialTask) mContent).getActivity() != null){
            	((FragmentOfficialTask) mContent).setTitleBar();
            }
        }
//				mContent = fragment;
//				getSupportFragmentManager()
//				.beginTransaction()
//				.replace(R.id.content_frame, fragment)
//				.commit();
		getSlidingMenu().showContent();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Fragment fragment;
		switch (mMode){
		case MODE_MAIN:
		case MODE_PERSONAL_TASK:
			fragment = mDefaultMain;
			break;
		case MODE_OFFICIAL_TASK:
			fragment = mOfficialTask;
			break;
		case MODE_STORAGELIST:
			fragment = mQrcodeList;
			break;
		case MODE_SETTING:
			fragment = mSettings;
			break;
		default:
			fragment = mDefaultMain;
		}
		switchContent((Fragment) mContent, fragment);
		//java.lang.NullPointerException,MainActivity.onResume(MainActivity.java:231) [s]
		try{
			super.onResume();
		}catch(Exception e){
			e.printStackTrace();
		}
		//java.lang.NullPointerException,MainActivity.onResume(MainActivity.java:231) [e]
	}

    private void UMUpdateCheck(){
    	//友盟版本更新
    	MobclickAgent.updateOnlineConfig(getApplicationContext());
    	UpdateConfig.setDebug(true);	//调试信息开关
    	UmengUpdateAgent.setUpdateOnlyWifi(false);	//设置是否仅wifi模式才更新
    	UmengUpdateAgent.setUpdateCheckConfig(true);		//集成监测开关
    	UmengUpdateAgent.update(getApplicationContext());
    	
    	String updateConfig = MobclickAgent.getConfigParams(getApplicationContext(), "upgrade_mode");
    	Utils.logd("updateConfig="+updateConfig);
    	if (TextUtils.isEmpty(updateConfig)){
    		return;
    	}
    	try {
			JSONObject obj = new JSONObject(updateConfig);
			String online_versionCode = obj.getString("online_versionCode");
			JSONArray array = obj.getJSONArray("config");
			List<String> codeList = null;
			List<String> modeList = null;
			if (array != null && array.length() > 0){
				for (int i=0; i<array.length(); i++){
					codeList = new ArrayList<String>();
					modeList = new ArrayList<String>();
					
					codeList.add(array.getJSONObject(i).getString("versionCode"));
					modeList.add(array.getJSONObject(i).getString("mode"));
				}
			}
			if (codeList == null || codeList.isEmpty()
					|| modeList == null || modeList.isEmpty()
					|| TextUtils.isEmpty(online_versionCode)){
				return;
			}
			
			if (codeList.contains(online_versionCode)){
				String modeString = modeList.get(codeList.indexOf(online_versionCode));
				Utils.logd("modeString="+modeString);
				if ("F".equals(modeString)){
					UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

					    @Override
					    public void onClick(int status) {
					        switch (status) {
					        case UpdateStatus.Update:
					            break;
					        default:
					        	if (MainActivity.this.isFinishing()){
					        		Utils.showToast(getApplicationContext(), R.string.update_notice, Toast.LENGTH_SHORT);
									Utils.exitApp(MainActivity.this);
									return;
					        	}
					        	AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
					        	builder.setMessage(getString(R.string.update_notice));
					        	builder.setCancelable(false);		
					        	builder.setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Utils.exitApp(MainActivity.this);
									}
								});
					        	builder.show();
					        }
					    }
					});
				}else {
					
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
}
