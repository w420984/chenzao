package com.xiaosajie.chenzao;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.R;
import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.MyDate;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.chenzao.view.TaskShowView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentDefaultMain extends Fragment{
	private Activity mActivity;
	private int                mCurrentSchedulerIndex           = 0;
	private ViewPager          mSchedulerViewPager;
	private SchedulerPagerAdpter  mSchedulerAdapter;
	private List<TaskScheduler> mSchedulerList;
	private TaskSchedulerReceiver mUpdateTaskSchedulerReceiver;
	private CustomToast mProgressDialog;
	private LoadTaskSchedulerListTask mLoadTaskSchedulerListTask;
//	private RelativeLayout mRelativeLayoutBG;
	private final int[] mBG = {R.drawable.task_background_1,
								R.drawable.task_background_2,
								R.drawable.task_background_3,
								R.drawable.task_background_4,
								R.drawable.task_background_5,
								R.drawable.task_background_6,
								R.drawable.task_background_7};
	
	class LoadTaskSchedulerListTask extends AsyncTask<Void, Void, List<TaskScheduler>>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected List<TaskScheduler> doInBackground(Void... arg0) {
			String result = "";
			try {
				result = NetEngine.getInstance(mActivity).searchTaskScheduler(Utils.getMyUid(mActivity), 0, 0, "desc", "", Constants.TASK_SCHEDULER_CUSTOMRISE);
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

			try {
				JSONObject object = null;
				object = new JSONObject(result);
				JSONArray array = object.getJSONArray("schedules");

				List<TaskScheduler> list = new ArrayList<TaskScheduler>();
				for(int i=0; i<array.length(); i++){
					TaskScheduler item = new TaskScheduler(mActivity, (JSONObject)array.get(i));
					list.add(item);
				}
				return list;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(List<TaskScheduler> result) {
			dismissProgress();
			
			if (result == null){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mActivity);
				}else{
					Utils.showToast(mActivity, R.string.upload_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			if(mSchedulerList != null){
				mSchedulerList.clear();
			}
			mSchedulerList = result;
			if(mSchedulerAdapter != null){
				mSchedulerAdapter.notifyDataSetChanged();
			}

			Intent successIntent = new Intent(Constants.GET_TASK_SCHEDULER_LISTS_SUCCESS);
			mActivity.sendBroadcast(successIntent);
			
			//写入数据库
			ChenzaoDBAdapter db = new ChenzaoDBAdapter(mActivity.getApplicationContext());
			db.open();
			for(int i = 0; i < mSchedulerList.size(); i++){
				db.insert(mSchedulerList.get(i));
				Utils.cancelAlarmAlert(mActivity, mSchedulerList.get(i));
				Utils.setAlarmAlert(mActivity, mSchedulerList.get(i));
			}
			db.close();
		}
		
	}
	
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("FragmentDefaultMain"); 

	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("FragmentDefaultMain");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = getActivity();

		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(Constants.ADD_TASK_SCHEDULER_SUCCESS);
		intentfilter.addAction(Constants.GET_TASK_SCHEDULER_LISTS_SUCCESS);
		intentfilter.addAction(Constants.UPDATE_TASK_SCHEDULER_SUCCESS);
		intentfilter.addAction(Constants.DELETE_TASK_SCHEDULER_SUCCESS);
		mUpdateTaskSchedulerReceiver = new TaskSchedulerReceiver();
		mActivity.registerReceiver(mUpdateTaskSchedulerReceiver, intentfilter);

		View view = inflater.inflate(R.layout.activity_main, container, false);
		initView(view);	
		setTitleBar();
		return view;	
    }

	public void setTitleBar(){
		((MainActivity)mActivity).setTitleBar(R.drawable.showleft_selector, getString(R.string.personal_task), 0);
	}
	
	private void initView(View view){
//		mRelativeLayoutBG = (RelativeLayout)view.findViewById(R.id.rl_main);
		
		if(!loadTaskScheduler(Constants.TASK_SCHEDULER_CUSTOMRISE)){
			mLoadTaskSchedulerListTask = new LoadTaskSchedulerListTask();
			try{
				mLoadTaskSchedulerListTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		
		mSchedulerViewPager = (ViewPager)view.findViewById(R.id.schedulerPager);
        mSchedulerAdapter = new SchedulerPagerAdpter();
        mSchedulerViewPager.setAdapter( mSchedulerAdapter );
        mSchedulerViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				mCurrentSchedulerIndex = position;
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
        
        mSchedulerViewPager.setCurrentItem( mCurrentSchedulerIndex );
	}

	boolean loadTaskScheduler(int type){
		ChenzaoDBAdapter db = new ChenzaoDBAdapter(mActivity.getApplicationContext());
		db.open();
		mSchedulerList = db.getTaskSchedulerListByType(type);
		db.close();
		if(mSchedulerList.size() == 0){
			return false;
		}
		//由于新增任务绑定背景功能，为了防止新版本覆盖安装旧版本时，
		//有数据的情况下不会重新联网，在此处判断数据里面有没有背景图，如果没有，
		//说明是旧数据，重新联网获取新数据
		if(TextUtils.isEmpty(mSchedulerList.get(0).getBGImage())){
			return false;
		}
		if(mSchedulerAdapter != null){
			mSchedulerAdapter.notifyDataSetChanged();
		}
		return true;
	}

    private class SchedulerPagerAdpter extends PagerAdapter {
        private SparseArray<SoftReference<TaskShowView>> mSubViewCache = new SparseArray<SoftReference<TaskShowView>>();

        @Override
        public int getCount() {
            if( mSchedulerList != null) {
                return mSchedulerList.size() + 1;
            }

            //没有任务时只显示新增任务页面
            return 1;
        }

        @Override
        public boolean isViewFromObject( View view, Object obj ) {
            return view == obj;
        }

        @Override
        public void destroyItem( ViewGroup container, int position, Object object ) {
            container.removeView( (View)object );
        }

        @Override
        public Object instantiateItem( ViewGroup container, final int position ) {
        	TaskShowView v = null;

            if( mSubViewCache.get( position ) != null ) {
                v = mSubViewCache.get( position ).get();
            }

            if( v == null ) {
                v = new TaskShowView( mActivity );

                ViewGroup.LayoutParams params = v.getLayoutParams();
                if( params == null ) {
                    params = new ViewPager.LayoutParams();
                }
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mSubViewCache.put( position, new SoftReference<TaskShowView>( v ) );
            }

    		TextView history = (TextView)v.findViewById(R.id.tv_history);
    		history.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mCurrentSchedulerIndex >= mSchedulerList.size()){
						return;
					}
					Intent i = new Intent(getActivity(), DailyUpdateListActivity.class);
					i.putExtra(Constants.TASK_SCHEDULER_ID, mSchedulerList.get(mCurrentSchedulerIndex).getID());
					i.putExtra(Constants.TASK_SCHEDULER_TYPE, mSchedulerList.get(mCurrentSchedulerIndex).getType());
					getActivity().startActivity(i);
				}
			});
    		TextView dailyupdate = (TextView)v.findViewById(R.id.tv_daily_update);
    		dailyupdate.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mCurrentSchedulerIndex >= mSchedulerList.size()){
						return;
					}
					
					if (checkUpdateTime(mSchedulerList.get(mCurrentSchedulerIndex))){
						Utils.showToast(mActivity, R.string.has_updated, Toast.LENGTH_SHORT);
						return;
					}
					
					Intent intent = new Intent(getActivity(), UpdateScheduleActivity.class);
					intent.putExtra(Constants.TASK_SCHEDULER_ID, mSchedulerList.get(mCurrentSchedulerIndex).getID());
					intent.putExtra(Constants.TASK_SCHEDULER_TYPE, mSchedulerList.get(mCurrentSchedulerIndex).getType());
					getActivity().startActivityForResult(intent, Constants.TASK_SCHEDULER_DAILY_UPDATE);
				}
			});
    		
            ViewGroup dayGroup = (ViewGroup)v.findViewById(R.id.dayGroup);
            if(mSchedulerList.size() == position){
            	v.setTag("new");
            	v.findViewById(R.id.rl_circle_info).setVisibility(View.INVISIBLE);
            	v.findViewById(R.id.rl_add_new).setVisibility(View.VISIBLE);
            	v.findViewById(R.id.rl_group).setVisibility(View.INVISIBLE);
            	dayGroup.setVisibility(View.INVISIBLE);
            	v.findViewById(R.id.task_show_view_main).setBackgroundResource(mBG[position % 7]);
            }else{
	            v.setTag( mSchedulerList.get(position).getID() );
	            float progress = mSchedulerList.get(position).getProgress();
	            v.setProgress(progress);
	            v.setDays(Utils.getLastTime(mSchedulerList.get(position)));
	            v.setTaskScheduler(mSchedulerList.get(position));

	            ((TextView)v.findViewById(R.id.tv_task_title)).setText(mSchedulerList.get(position).getTitle());
            	
	            dayGroup.setVisibility(View.INVISIBLE);
	            
	            String imagepath = mSchedulerList.get(position).getBGImage();
	            if(imagepath != null && !imagepath.equals("")){
	            	int index = 0;
					try{
						index = Integer.parseInt(imagepath.substring(3, 4)) - 1;
						if(index >= 0 && index < mBG.length){
							v.findViewById(R.id.task_show_view_main).setBackgroundResource(mBG[index]);
						}else{
							v.findViewById(R.id.task_show_view_main).setBackgroundResource(mBG[position % 7]);
						}
					}catch(NumberFormatException e){
						v.findViewById(R.id.task_show_view_main).setBackgroundResource(mBG[position % 7]);
					}
	            }
            }
    		((TextView)v.findViewById(R.id.tv_index_show)).setText((position + 1) + "/" + mSchedulerAdapter.getCount());

    		if( v.getParent() == null ) {
                container.addView( v );
            }
            else {
                container.requestLayout();
                container.invalidate();
            }

    		return v;
        }
        
        public int getItemPosition( Object object ) {
            final View cachedView = currentSchedulerView();
            final View v = (View) object;
            if (cachedView != null 
                    && cachedView.getTag() != null
                    && cachedView.getTag().equals( v.getTag() )) {
                return POSITION_UNCHANGED;
            }
            else {
                return POSITION_NONE;
            }
        }

        private TaskShowView currentSchedulerView() {
            if (mSchedulerAdapter != null
                    && mSchedulerAdapter.mSubViewCache.get( mSchedulerViewPager.getCurrentItem() ) != null) {
                return mSchedulerAdapter.mSubViewCache.get( mSchedulerViewPager.getCurrentItem() ).get();
            }
            return null;
        }
    }

	public class TaskSchedulerReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if(intent == null){
				return;
			}
			
			String action = intent.getAction();
			if(Constants.ADD_TASK_SCHEDULER_SUCCESS.equals(action) 
					|| Constants.UPDATE_TASK_SCHEDULER_SUCCESS.equals(action)
					|| Constants.DELETE_TASK_SCHEDULER_SUCCESS.equals(action)){
				if (Constants.UPDATE_TASK_SCHEDULER_SUCCESS.equals(action) &&
						intent.getExtras().getInt(Constants.TASK_SCHEDULER_TYPE) != Constants.TASK_SCHEDULER_CUSTOMRISE){
					return;
				}
				mCurrentSchedulerIndex = 0;
				mSchedulerList.clear();
				mSchedulerAdapter.mSubViewCache.clear();
				mSchedulerViewPager.removeAllViews();
				loadTaskScheduler(Constants.TASK_SCHEDULER_CUSTOMRISE);
				mSchedulerViewPager.setAdapter(mSchedulerAdapter);
			}else if(Constants.GET_TASK_SCHEDULER_LISTS_SUCCESS.equals(action)){
				mCurrentSchedulerIndex = 0;
				mSchedulerAdapter.mSubViewCache.clear();
				mSchedulerViewPager.removeAllViews();
				mSchedulerViewPager.setAdapter(mSchedulerAdapter);
			}
				
		}
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mLoadTaskSchedulerListTask != null && mLoadTaskSchedulerListTask.getStatus() == AsyncTask.Status.RUNNING){
			 mLoadTaskSchedulerListTask.cancel(true);
			 mLoadTaskSchedulerListTask = null;
		}
		if(mUpdateTaskSchedulerReceiver != null){
			mActivity.unregisterReceiver(mUpdateTaskSchedulerReceiver);
		}
		dismissProgress();
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, mActivity);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK &&
				requestCode == Constants.TASK_SCHEDULER_DAILY_UPDATE){
			mSchedulerList.set(mCurrentSchedulerIndex, 
					Utils.getTaskFromDbByID(mActivity, mSchedulerList.get(mCurrentSchedulerIndex).getID()));
			mSchedulerAdapter.currentSchedulerView().setProgress(mSchedulerList.get(mCurrentSchedulerIndex).getProgress());
			mSchedulerAdapter.currentSchedulerView().setTaskScheduler(mSchedulerList.get(mCurrentSchedulerIndex));
		}
	}
	
	//判断任务今天是否已经更新过进度了
	private boolean checkUpdateTime(TaskScheduler task){
		if (task == null){
			return false;
		}
		if (task.getUpdateTime() == 0){
			return false;
		}
		
		MyDate curTime = new MyDate(mActivity, String.valueOf(System.currentTimeMillis()));
		MyDate lastUpdateTime = new MyDate(mActivity, String.valueOf(task.getUpdateTime()));
		if (curTime.year.equals(lastUpdateTime.year)
				&& curTime.month.equals(lastUpdateTime.month)
				&& curTime.day.equals(lastUpdateTime.day)){
			return true;
		}
		return false;
	}
	
}
