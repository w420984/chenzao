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

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.QrcodeFile;
import com.chenzao.net.NetEngine;
import com.chenzao.pullrefresh.PullToRefreshBase;
import com.chenzao.pullrefresh.PullToRefreshListView;
import com.chenzao.pullrefresh.PullToRefreshBase.OnRefreshListener;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.chenzao.view.QrcodeFileItemView;
import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.FragmentQrcodeList.LoadDataTask;
import com.xiaosajie.chenzao.FragmentQrcodeList.QrcodeListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchResultActivity extends BaseActivity {
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
				view = new QrcodeFileItemView(SearchResultActivity.this, 0, mList.get(position), mBmpCache);
			}else{
				view = (QrcodeFileItemView)convertView;
				view.update(mList.get(position));
			}
			return view;
		}
		
	}
	
	private ImageView mBtnBack;
	private ImageView mBtnSearch;
	private EditText mKeyWord;
	private TextView mNodata;
	private ListView mListView;

	private BaseAdapter mAdapter;
	private LoadDataTask mTask;	
	private List<QrcodeFile> mList;
	private Map<String, Bitmap> mBmpCache = new HashMap<String, Bitmap>();
	
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
	private PullToRefreshListView mPullRefreshListView;
	private final int MODE_REFRESH = 1;
	private final int MODE_MORE = 2;
	private int mRefreshMode;

	@Override
	protected void handleTitleBarEvent(int eventId) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.search_result_layout);
		setTitleBar(null, null, null);
		
		mBtnBack = (ImageView)findViewById(R.id.btn_back);
		mBtnSearch = (ImageView)findViewById(R.id.btn_search);
		mKeyWord = (EditText)findViewById(R.id.input_keyword);
		mNodata = (TextView)findViewById(R.id.noData);
		mBtnBack.setOnClickListener(this);
		mBtnSearch.setOnClickListener(this);
		
		mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.listView);
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
					Intent i = new Intent(SearchResultActivity.this, FileStorageDetailActivity.class);
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
				mRefreshMode = MODE_MORE;
				loadData();
			}
		});

	}
	
	private void loadData(){
		mTask = new LoadDataTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
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

	private void doSearch(){
		if (TextUtils.isEmpty(mKeyWord.getText().toString())){
			return;
		}
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mKeyWord.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        setLastUpdateTime();
        mPullRefreshListView.doPullRefreshing(true, 500);
	}
	
	@Override
	protected void onDestroy() {
		Utils.recycleBitmapCache(mBmpCache);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPageEnd("SetNewPasswordActiviy"); 
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onPageStart("SetNewPasswordActiviy");
		MobclickAgent.onResume(this);
		if (mList == null || mList.isEmpty()){
			mNodata.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}else{
			mNodata.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnBack){
			finish();
		}else if (v == mBtnSearch){
			doSearch();
		}
		super.onClick(v);
	}

	class LoadDataTask extends AsyncTask<Void, Void, List<QrcodeFile>>{
		private Throwable mThr;

		@Override
		protected List<QrcodeFile> doInBackground(Void... params) {
			String result;
			try {
				String offset = "0";
				if (mRefreshMode == MODE_MORE){
					offset = mList.size()+"";
				}
				result = NetEngine.getInstance(SearchResultActivity.this).
						searchFileRecord(Utils.getMyUid(SearchResultActivity.this), 
						mKeyWord.getText().toString(), offset);
			} catch (ChenzaoIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (ChenzaoApiException e){
				mThr = e;
				return null;
			} catch (ChenzaoParseException e) {
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
					QrcodeFile item = new QrcodeFile(SearchResultActivity.this, (JSONObject)array.get(i));
					list.add(item);
				}
				return list;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
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
					Utils.handleErrorEvent(mThr, SearchResultActivity.this);
				}else{
					Utils.showToast(SearchResultActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (mRefreshMode == MODE_REFRESH){
				if (!result.isEmpty()){
					mListView.setVisibility(View.VISIBLE);
					mNodata.setVisibility(View.GONE);
					mList = result;
					mAdapter.notifyDataSetChanged();
				}else {
					Utils.showToast(SearchResultActivity.this, R.string.no_search_data, Toast.LENGTH_SHORT);
				}
			}else{
				if (result.isEmpty()){
					mPullRefreshListView.setHasMoreData(false);
					Utils.showToast(SearchResultActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mList.addAll(result);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	
	}
	
}
