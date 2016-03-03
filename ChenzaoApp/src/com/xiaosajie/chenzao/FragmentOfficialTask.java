package com.xiaosajie.chenzao;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.MyDate;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.chenzao.view.Task100DayIntroView;
import com.chenzao.view.TaskShowView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.xiaosajie.chenzao.FragmentDefaultMain.TaskSchedulerReceiver;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentOfficialTask extends Fragment {
    private class SchedulerPagerAdpter extends PagerAdapter {
        private SparseArray<SoftReference<TaskShowView>> mSubViewCache = new SparseArray<SoftReference<TaskShowView>>();

        @Override
        public int getCount() {
            if( mSchedulerList == null || mSchedulerList.isEmpty()) {
            	return 1;
            }

            //没有任务时只显示新增任务页面
            return mSchedulerList.size();
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
        	View v = null;

            if( v == null ) {
            	if ( mSchedulerList == null || mSchedulerList.isEmpty()
            			|| mSchedulerList.get(position).getUpdateCount() == 100
            			|| mSchedulerList.get(position).getProgress() >= 100){
            		v = new Task100DayIntroView(mActivity);
            	}else{
            		v = new TaskShowView( mActivity );
            	}

                ViewGroup.LayoutParams params = v.getLayoutParams();
                if( params == null ) {
                    params = new ViewPager.LayoutParams();
                }
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            if (v instanceof TaskShowView){
            	mMainLayout.setBackgroundResource(R.drawable.official_bg_1);
        		TextView history = (TextView)v.findViewById(R.id.tv_history);
        		history.setOnClickListener(new View.OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
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
                
	            v.setTag( mSchedulerList.get(position).getID() );
				//float progress = mSchedulerList.get(position).getUpdateCount()%100;
				float progress = mSchedulerList.get(position).getProgress();
	            ((TaskShowView) v).setProgress(progress);
	            ((TaskShowView) v).setDays(Utils.getLastTime(mSchedulerList.get(position)));
	            ((TaskShowView) v).setTaskScheduler(mSchedulerList.get(position));

	            ((TextView)v.findViewById(R.id.tv_task_title)).setText(mSchedulerList.get(position).getTitle());
            	
            	dayGroup.setVisibility(View.VISIBLE);
            	int updateCount = mSchedulerList.get(position).getUpdateCount();
            	int index = 100-updateCount;
            	if (index <= 0){
            		index = 1;
            	}
            	((TextView)v.findViewById(R.id.date_num)).setText(index +"");
            	dayGroup.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mActivity, ImageViewerActivity.class);
						int dayIndex = 100-mSchedulerList.get(position).getUpdateCount();
						if (dayIndex <= 0){
							dayIndex = 1;
		            	}
						intent.putExtra(ImageViewerActivity.EXTRA_PICINDEX, dayIndex);
						mActivity.startActivity(intent);
					}
				});
                
            }else if (v instanceof Task100DayIntroView){
            	mMainLayout.setBackgroundResource(R.drawable.official_1_bg);
            	Button startBtn = (Button) v.findViewById(R.id.btn_start);
            	ViewGroup taskInfo = (ViewGroup) v.findViewById(R.id.rl_task_info);
            	TextView history = (TextView) v.findViewById(R.id.tv_history);
            	TextView restart = (TextView) v.findViewById(R.id.tv_restart);
            	
            	if (mSchedulerList == null || mSchedulerList.isEmpty()){
            		startBtn.setVisibility(View.VISIBLE);
            		taskInfo.setVisibility(View.GONE);
            		startBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							addNewTask();
						}
					});
            	}else{
            		startBtn.setVisibility(View.GONE);
            		taskInfo.setVisibility(View.VISIBLE);
            		history.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mCurrentSchedulerIndex >= mSchedulerList.size()){
	    						return;
	    					}
	    					Intent i = new Intent(mActivity, DailyUpdateListActivity.class);
	    					i.putExtra(Constants.TASK_SCHEDULER_ID, mSchedulerList.get(mCurrentSchedulerIndex).getID());
	    					i.putExtra(Constants.TASK_SCHEDULER_TYPE, Constants.TASK_SCHEDULER_OFFICIAL);
	    					getActivity().startActivity(i);
						}
					});
            		restart.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							addNewTask();
						}
					});
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
	
    class LoadTaskSchedulerListTask extends AsyncTask<Void, Void, List<TaskScheduler>>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected List<TaskScheduler> doInBackground(Void... arg0) {
			String result = "";
			try {
				result = NetEngine.getInstance(mActivity).searchTaskScheduler(Utils.getMyUid(mActivity), 
												0, 0, "desc", "", Constants.TASK_SCHEDULER_OFFICIAL);
			} catch (ChenzaoIOException e) {
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoParseException e) {
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoApiException e) {
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

			mCurrentSchedulerIndex = 0;
			mSchedulerAdapter.mSubViewCache.clear();
			mSchedulerViewPager.removeAllViews();
			mSchedulerViewPager.setAdapter(mSchedulerAdapter);
			
			//写入数据库
			ChenzaoDBAdapter db = new ChenzaoDBAdapter(mActivity.getApplicationContext());
			db.open();
			for(int i = 0; i < mSchedulerList.size(); i++){
				db.insert(mSchedulerList.get(i));
			}
			db.close();
		}
		
	}
	
    class NewTaskSchedulerTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;
		private TaskScheduler mTaskScheduler;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";
			mTaskScheduler = new TaskScheduler();
			mTaskScheduler.setTitle(mActivity.getString(R.string.task_100day_title));
			mTaskScheduler.setNote(mActivity.getString(R.string.task_100day_note));
			mTaskScheduler.setOnoff1(0);
			mTaskScheduler.setOnoff2(0);
			long start = System.currentTimeMillis();
			MyDate date = new MyDate(mActivity, start+"");
			mTaskScheduler.setStartTime(date.year+
					Utils.format(Integer.valueOf(date.month))+
					Utils.format(Integer.valueOf(date.day)));
			start += 100*24*60*60*1000L;
			date = new MyDate(mActivity, start+"");
			mTaskScheduler.setEndTime(date.year+
					Utils.format(Integer.valueOf(date.month))+
					Utils.format(Integer.valueOf(date.day)));
			try {
				result = NetEngine.getInstance(mActivity).
							addTaskScheduler(Utils.getMyUid(mActivity),
									mTaskScheduler.getTitle(), 
									mTaskScheduler.getNote(), 
									mTaskScheduler.getStartTime(), 
									mTaskScheduler.getEndTime(),
									mTaskScheduler.getRemindTime1(), 
									mTaskScheduler.getRepeatType1(), 
									mTaskScheduler.getOnoff1(),
									mTaskScheduler.getRemindTime2(), mTaskScheduler.getRepeatType2(), mTaskScheduler.getOnoff2(),
									mTaskScheduler.getConcentTime(), mTaskScheduler.getCompleteWords(), mTaskScheduler.getBGImage(), 
									Constants.TASK_SCHEDULER_OFFICIAL);
			} catch (ChenzaoIOException e) {
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoParseException e) {
				e.printStackTrace();
				mThr = e;
			} catch (ChenzaoApiException e) {
				e.printStackTrace();
				mThr = e;
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mActivity);
				}else{
					Utils.showToast(mActivity, R.string.ChenzaoParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			
			JSONObject object = null;
			try {
				object = new JSONObject(result);
				mTaskScheduler.setID(object.getString("scheduleid"));
				mTaskScheduler.setCreateTime(object.getString("createdate"));
				mTaskScheduler.setType(Constants.TASK_SCHEDULER_OFFICIAL);
			} catch (JSONException e) {
				e.printStackTrace();
				Utils.showToast(mActivity, R.string.ChenzaoParseException, Toast.LENGTH_SHORT);
				return;
			}
			
			ChenzaoDBAdapter mDatabase = new ChenzaoDBAdapter(mActivity);
			mDatabase.open();
			mDatabase.insert(mTaskScheduler);
			mDatabase.close();
			
			mCurrentSchedulerIndex = 0;
			mSchedulerList.clear();
			mSchedulerAdapter.mSubViewCache.clear();
			mSchedulerViewPager.removeAllViews();
			loadTaskScheduler(Constants.TASK_SCHEDULER_OFFICIAL);
			mSchedulerViewPager.setAdapter(mSchedulerAdapter);
			
			Utils.cancelAlarmAlert(mActivity, mTaskScheduler);
			Utils.setAlarmAlert(mActivity, mTaskScheduler);
						
//			if(!mSharetoWeixin){
//				finish();
//				return;
//			}
//			mSharetoWeixin = false;
//			
//			// 添加微信平台
//			UMWXHandler wxHandler = new UMWXHandler(getApplicationContext(),Utils.getUmengAppkey(getApplicationContext()));
//			wxHandler.addToSocialSDK();
////			// 支持微信朋友圈
//			UMWXHandler wxCircleHandler = new UMWXHandler(getApplicationContext(),"wxf044f8dd4ea8e538");
//			wxCircleHandler.setToCircle(true);
//			wxCircleHandler.addToSocialSDK();
//			com.umeng.socialize.controller.UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
//			//mController.setShareMedia(weixinContent);
//			// 设置分享内容
//			mController.setShareContent("[趁早Android客户端] 新计划任务--" + mTaskScheduler.getTitle());
//
//			mController.postShare(getApplicationContext(),SHARE_MEDIA.WEIXIN_CIRCLE, 
//			        new SnsPostListener() {
//			                @Override
//			                public void onStart() {
//			                    Toast.makeText(getApplicationContext(), "开始分享.", Toast.LENGTH_SHORT).show();
//			                }
//
//							@Override
//							public void onComplete(SHARE_MEDIA arg0, int eCode,
//									SocializeEntity entity) {
//								if (eCode == 200) {
//			                         Toast.makeText(getApplicationContext(), "分享成功.", Toast.LENGTH_SHORT).show();
//			                     } else {
//			                          String eMsg = "";
//			                          if (eCode == -101){
//			                              eMsg = "没有授权";
//			                          }
//			                          Toast.makeText(getApplicationContext(), "分享失败[" + eCode + "] " + 
//			                                             eMsg,Toast.LENGTH_SHORT).show();
//			                     }
//			         			finish();
//							}
//			});
		}
		
	}
    
	private Activity mActivity;
	private int                mCurrentSchedulerIndex           = 0;
	private ViewPager          mSchedulerViewPager;
	private SchedulerPagerAdpter  mSchedulerAdapter;
	private List<TaskScheduler> mSchedulerList;
	private ViewGroup mMainLayout;
	private CustomToast mProgressDialog;
	private LoadTaskSchedulerListTask mLoadTaskSchedulerListTask;
	private NewTaskSchedulerTask mNewTaskSchedulerTask;
	private TaskSchedulerReceiver mUpdateTaskSchedulerReceiver;

	private final int[] mBG = {R.drawable.task_background_1,
			R.drawable.task_background_2,
			R.drawable.task_background_3,
			R.drawable.task_background_4,
			R.drawable.task_background_5,
			R.drawable.task_background_6,
			R.drawable.task_background_7};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = getActivity();
		
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(Constants.UPDATE_TASK_SCHEDULER_SUCCESS);
		intentfilter.addAction(Constants.DELETE_TASK_SCHEDULER_SUCCESS);
		mUpdateTaskSchedulerReceiver = new TaskSchedulerReceiver();
		mActivity.registerReceiver(mUpdateTaskSchedulerReceiver, intentfilter);

		View view = inflater.inflate(R.layout.official_task_main, container, false);
		initView(view);	
		setTitleBar();
		return view;
	}

	private void initView(View view){
		mMainLayout = (ViewGroup)view.findViewById(R.id.rlMain);
		if(!loadTaskScheduler(Constants.TASK_SCHEDULER_OFFICIAL)){
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
        mSchedulerViewPager.setCurrentItem( mCurrentSchedulerIndex );
        mSchedulerViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				//mMainLayout.setBackgroundResource(R.drawable.official_1_bg);
				mCurrentSchedulerIndex = position;
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
    }
	
	public void setTitleBar(){
		((MainActivity)mActivity).setTitleBar(R.drawable.showleft_selector, getString(R.string.official_task), 0);
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

	@Override
	public void onDestroy() {
		if(mUpdateTaskSchedulerReceiver != null){
			mActivity.unregisterReceiver(mUpdateTaskSchedulerReceiver);
		}
		dismissProgress();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("FragmentOfficialTask"); 
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("FragmentOfficialTask");
		super.onResume();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK && 
			requestCode == Constants.TASK_SCHEDULER_DAILY_UPDATE){
			//java.lang.NullPointerException,FragmentOfficialTask.java:584[s]
			if( mSchedulerList == null || mSchedulerList.isEmpty()) {
            	return;
            }
			//java.lang.NullPointerException,FragmentOfficialTask.java:584[s]
			mSchedulerList.set(mCurrentSchedulerIndex, 
					Utils.getTaskFromDbByID(mActivity, mSchedulerList.get(mCurrentSchedulerIndex).getID()));
			float progress = mSchedulerList.get(mCurrentSchedulerIndex).getProgress();
			int updateCount = mSchedulerList.get(mCurrentSchedulerIndex).getUpdateCount();
        	int index = 100-updateCount;
        	if (index <= 0){
        		index = 1;
        	}
        	TaskShowView view = (TaskShowView) mSchedulerViewPager.getChildAt(mCurrentSchedulerIndex);
        	((TextView)view.findViewById(R.id.date_num)).setText(index +"");
        	view.setProgress(progress);
        	view.setTaskScheduler(mSchedulerList.get(mCurrentSchedulerIndex));
		}
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
	
	boolean loadTaskScheduler(int type){
		ChenzaoDBAdapter db = new ChenzaoDBAdapter(mActivity.getApplicationContext());
		db.open();
		mSchedulerList = db.getTaskSchedulerListByType(type);
		db.close();
		if(mSchedulerList.size() == 0){
			return false;
		}
		if(mSchedulerAdapter != null){
			mSchedulerAdapter.notifyDataSetChanged();
		}
		return true;
	}
	
	private void addNewTask(){
		mNewTaskSchedulerTask = new NewTaskSchedulerTask();
		try{
			mNewTaskSchedulerTask.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
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
						intent.getExtras().getInt(Constants.TASK_SCHEDULER_TYPE) != Constants.TASK_SCHEDULER_OFFICIAL){
					return;
				}
				mCurrentSchedulerIndex = 0;
				mSchedulerList.clear();
				mSchedulerAdapter.mSubViewCache.clear();
				mSchedulerViewPager.removeAllViews();
				loadTaskScheduler(Constants.TASK_SCHEDULER_OFFICIAL);
				mSchedulerViewPager.setAdapter(mSchedulerAdapter);
			}
				
		}
		
	}
}
