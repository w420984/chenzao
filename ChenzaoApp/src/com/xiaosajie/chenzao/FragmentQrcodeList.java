package com.xiaosajie.chenzao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.chenzao.exception.ChenzaoParseException;

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.models.MyDate;
import com.chenzao.models.QrcodeFile;
import com.chenzao.models.QrcodeFileList;
import com.chenzao.net.NetEngine;
import com.chenzao.pullrefresh.PullToRefreshBase;
import com.chenzao.pullrefresh.PullToRefreshListView;
import com.chenzao.pullrefresh.PullToRefreshBase.OnRefreshListener;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.chenzao.view.QrcodeFileItemView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentQrcodeList extends Fragment {
	
	class QrcodeListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mList != null && !mList.isEmpty()){
				return mList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			QrcodeFileItemView view = null;
			if (convertView == null){
				view = new QrcodeFileItemView(mActivity, 0, mList.get(position), mBmpCache);
			}else{
				view = (QrcodeFileItemView)convertView;
				view.update(mList.get(position));
			}
			return view;
		}
		
	}
	
	private ViewGroup mDateGroup;
	private TextView mDateMonth;
	private ImageView mBtnArrayLeft;
	private ImageView mBtnArrayRight;
	
	private Activity mActivity;
	private ListView mListView;
	private List<QrcodeFile> mList;
	private BaseAdapter mAdapter;
	private LoadDataTask mTask;
	private Map<String, Bitmap> mBmpCache = new HashMap<String, Bitmap>();
	
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
	private PullToRefreshListView mPullRefreshListView;
	private final int MODE_REFRESH = 1;
	private final int MODE_MORE = 2;
	private int mRefreshMode;
//	private boolean mHasMore = true;
	
	private int mCurListIndex = 0;	//当前显示的月份
	private List<QrcodeFileList> mMonthList; //将整个列表按月份分成小列表

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.qrcode_list, container, false);
		mActivity = getActivity();
		setTitleBar();
		initView(view);
		return view;
	}

	public void setTitleBar(){
		((MainActivity)mActivity).setTitleBar(R.drawable.showleft_selector, getString(R.string.qrcode_list), R.drawable.header_btn_search);
	}
	
	private void initView(View view){
		mDateGroup = (ViewGroup)view.findViewById(R.id.dateGroup);
		mDateGroup.setVisibility(View.GONE);
		mDateMonth = (TextView)view.findViewById(R.id.date);
		mBtnArrayLeft = (ImageView)view.findViewById(R.id.date_arrow_l);
		mBtnArrayRight = (ImageView)view.findViewById(R.id.date_arrow_r);
		mBtnArrayLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPreMonth();
			}
		});
		mBtnArrayRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showNextMonth();
			}
		});
		
		
		mPullRefreshListView = (PullToRefreshListView)view.findViewById(R.id.pullRefreshListView);
		mPullRefreshListView.setPullLoadEnabled(false);
		mPullRefreshListView.setScrollLoadEnabled(true);
		mListView = mPullRefreshListView.getRefreshableView();
		mAdapter = new QrcodeListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setDivider(null);		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				if (mList != null && !mList.isEmpty() && position < mList.size()){
					Intent i = new Intent(mActivity, FileStorageDetailActivity.class);
					i.putExtra(FileStorageDetailActivity.EXTRA_FILE_ITEM, mList.get(position));
					i.putExtra(Constants.TASK_SCHEDULER_TYPE, 0);
					startActivity(i);
				}
			}
		});
		
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				mRefreshMode = MODE_REFRESH;
				loadData();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (showNextMonth()){
					new sLeepTask().execute();
				}else{
					mRefreshMode = MODE_MORE;
					loadData();
				}
			}
		});
	}
		
	class sLeepTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPullRefreshListView.onPullUpRefreshComplete();
			super.onPostExecute(result);
		}
		
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		MobclickAgent.onPageEnd("FragmentQrcodeList"); 
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("FragmentQrcodeList");
		if (mList == null || mList.isEmpty()){
			setLastUpdateTime();
			mPullRefreshListView.doPullRefreshing(true, 500);
		}
	}

	@Override
	public void onDestroy() {
		Utils.recycleBitmapCache(mBmpCache);
		super.onDestroy();
	}

	private void loadData(){
		mTask = new LoadDataTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void processResult(List<QrcodeFile> list){
		if (mMonthList == null){
			mMonthList = new ArrayList<QrcodeFileList>();
		}
		if (mRefreshMode == MODE_REFRESH){
			mMonthList.clear();
			mCurListIndex = 0;
		}
		
		//按月份分的列表的最后一项
		List<QrcodeFile> lastList = null;
		//整个列表中的最后一条记录
		QrcodeFile file;
				
		for (int i=0; i<list.size(); i++){
			if (!mMonthList.isEmpty()){
				lastList = mMonthList.get(mMonthList.size()-1).mFileList;
			}
			if (lastList == null || lastList.isEmpty()){
				file = null;
			}else{
				file = lastList.get(lastList.size()-1);
			}
			if (file == null || isNewMonth(file, list.get(i))){
				//如果本次加载更多的第一条就是新的月份,就自动显示下一个月份
				if (i == 0 && mRefreshMode == MODE_MORE){
					mCurListIndex++;
				}
				List<QrcodeFile> temp = new ArrayList<QrcodeFile>();
				temp.add(list.get(i));
				MyDate date = list.get(i).getDate();
				String month = date.year+getString(R.string.year)+
							date.month+getString(R.string.month);
				QrcodeFileList fileList = new QrcodeFileList(month, temp);
				mMonthList.add(fileList);
			}else{
				lastList.add(list.get(i));
			}
		}
		update();
	}
	
	private void update(){
		mList = mMonthList.get(mCurListIndex).mFileList;
		mAdapter.notifyDataSetChanged();
		setDateText();
	}
	
	private void showPreMonth(){
		if (mCurListIndex == 0){
			return;
		}
		mCurListIndex--;
		update();
	}
	
	private boolean showNextMonth(){
		if (mMonthList!=null && mMonthList.size()>1 && mCurListIndex<mMonthList.size()-1){
			mCurListIndex++;
			update();
			return true;
		}
		return false;
	}
	
	private void setDateText(){
		mDateGroup.setVisibility(View.VISIBLE);
		String month = mList.get(0).getDate().year+getString(R.string.year)+
				mList.get(0).getDate().month+getString(R.string.month);
		mDateMonth.setText(month);
	}
	
	private boolean isNewMonth(QrcodeFile file, QrcodeFile newFile){
		MyDate date = file.getDate();
		MyDate newDate = newFile.getDate();
		if (date.month != newDate.month && date.year != newDate.year){
			return true;
		}
		return false;
	}

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullRefreshListView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        
        return mDateFormat.format(new Date(time));
    }
	
	class LoadDataTask extends AsyncTask<Void, Void, List<QrcodeFile>>{
		private Throwable mThr;

		@Override
		protected List<QrcodeFile> doInBackground(Void... params) {
			String result = "";
			try {
				String offset = "0";
				int size=0;
				if (mRefreshMode == MODE_MORE){
					for (int i=0; mMonthList!=null && i<mMonthList.size(); i++){
						size += mMonthList.get(i).mFileList.size();
					}
					offset = size+"";
				}
				result = NetEngine.getInstance(mActivity).queryFileRecordHistory(Utils.getMyUid(mActivity), offset);
			} catch (ChenzaoIOException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (ChenzaoParseException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (ChenzaoApiException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			}
			try {
				JSONObject object = null;
				object = new JSONObject(result);
				JSONArray array = object.getJSONArray("records");
				if (array == null){
					return null;
				}
				List<QrcodeFile> list = new ArrayList<QrcodeFile>();
				for(int i=0; i<array.length(); i++){
					QrcodeFile item = new QrcodeFile(mActivity, (JSONObject)array.get(i));
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
		}

		@Override
		protected void onPostExecute(List<QrcodeFile> result) {
			mPullRefreshListView.onPullDownRefreshComplete();
			mPullRefreshListView.onPullUpRefreshComplete();
			setLastUpdateTime();
			if (result == null){
				if (mThr != null){
					Utils.handleErrorEvent(mThr, mActivity);
				}else{
					Utils.showToast(mActivity, R.string.no_data, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (result.isEmpty()){
				if (mRefreshMode == MODE_MORE){
					//mPullRefreshListView.setHasMoreData(false);
					Utils.showToast(mActivity, R.string.no_more_data, Toast.LENGTH_SHORT);
				}
				return;
			}
		
			processResult(result);
		}
	
	}
	
}
