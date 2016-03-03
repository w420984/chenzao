package com.xiaosajie.chenzao;

import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.db.ChenzaoDBUserInfoAdapter;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.FlagToString;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentSettings extends Fragment{
	private Activity mActivity;
	private ListView mListView;
	private ListMenuAdapter mListMenuAdapter;
	private Button mLogoutBtn;
	private final int[] mMenuItemString = {R.string.personal_setting, R.string.changePsw, R.string.about_us, R.string.feed_back};
	private final int[] mMenuItemIcon = {R.drawable.icon_user, R.drawable.icon_pw, R.drawable.settings_aboutus_icon, R.drawable.settings_aboutus_icon};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.settings, container, false);
		mActivity = getActivity();
		setTitleBar();
		initView(view);
		return view;
	}

	public void setTitleBar(){
		((MainActivity)mActivity).setTitleBar(R.drawable.showleft_selector, getString(R.string.setting), 0);
	}
	
	private void initView(View view){
		mListView = (ListView)view.findViewById(R.id.setting_list);
		mListMenuAdapter = new ListMenuAdapter(mActivity);
		mListView.setAdapter(mListMenuAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {
				if(position == 0){
					//personal info
					Intent intent = new Intent(mActivity, EditUserInfoActivity.class);
					startActivity(intent);
				}else if(position == 1){
					//change psw
					Intent intent_psw = new Intent(mActivity, ChangePassword.class);
					startActivity(intent_psw);
				}else if(position == 2){
					//about us
					Intent intent_about = new Intent(mActivity, AboutActivity.class);
					startActivity(intent_about);
				}else if(position == 3){
					FeedbackAgent agent = new FeedbackAgent(mActivity);
				    agent.startFeedbackActivity();
				}
				
			}
		});
		
		mLogoutBtn = (Button)view.findViewById(R.id.btn_logout);
		mLogoutBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Utils.setMyUid(getActivity(), null);
				ChenzaoDBAdapter db = new ChenzaoDBAdapter(mActivity);
				db.open();
				db.cleanTable();
				db.close();
				
				ChenzaoDBUserInfoAdapter user_db = new ChenzaoDBUserInfoAdapter(mActivity);
				user_db.open();
				user_db.cleanTable();
				user_db.close();
				
				MainActivity.mMode = MainActivity.MODE_OFFICIAL_TASK;
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				getActivity().finish();
			}
		});
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("FragmentSettings"); 

	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("FragmentSettings");
	}
	
	class ListMenuAdapter extends BaseAdapter {  
		private Context mContext;
		
		ListMenuAdapter(Context context){
			mContext = context;
		}
	 
	    public int getCount() {  
	        return mMenuItemString.length;  
	    }  
	 
	    @Override  
	    public boolean areAllItemsEnabled() {  
	        return false;  
	    }  
	 
	    public Object getItem(int position) {  
	        return position;  
	    }  
	 
	    public long getItemId(int position) {  
	        return position;  
	    }  
	 
	    public View getView(int position, View convertView, ViewGroup parent) {  
	        ImageView iamge = null;  
	        TextView text = null;  
	        if (convertView == null) {  
		        convertView = LayoutInflater.from(mContext).inflate(R.layout.settings_menu_item, null);  
	        }   
	        iamge = (ImageView) convertView.findViewById(R.id.image);  
	        text =(TextView) convertView.findViewById(R.id.menu);  
	        text.setText(mMenuItemString[position]);  
	        iamge.setImageResource(mMenuItemIcon[position]);  
	        return convertView;  
	    }
	}
}
