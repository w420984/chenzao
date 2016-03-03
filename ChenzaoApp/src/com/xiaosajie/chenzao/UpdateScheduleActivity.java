package com.xiaosajie.chenzao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.DailyUpdate;
import com.chenzao.models.MyDate;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.BitmapHelper;
import com.chenzao.utils.Constants;
import com.chenzao.utils.FileUtils;
import com.chenzao.utils.Utils;
import com.chenzao.view.CircleView;
import com.chenzao.view.CircleView.CircleClickListener;
import com.umeng.analytics.MobclickAgent;
import com.xiaosajie.chenzao.wxapi.WXUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateScheduleActivity extends BaseActivity implements CircleClickListener, OnLongClickListener{
	public static String EXTRA_DAILYUPDATE = "extra_dailyupdate";
	public static String EXTRA_MODE = "extra_mode";
	public static final int MODE_NEW = 0;
	public static final int MODE_MODIFY = 1;
	
	private CircleView mCircle1;
	private CircleView mCircle2;
	private CircleView mCircle3;
	private CircleView mCircle4;
	private CircleView mCircle5;
	private CircleView mCircle6;
	private CircleView mCircle7;

	private TextView mDateDay;
	private TextView mDateWeek;

	private ImageView mInsertPics;
	private ImageView mPic1;
	private ImageView mPic2;
	private ImageView mPic3;
	
	private Button mSubmit;
	private Button mSubmitNoShare;
	private EditText mEditText;
	private TextView mInputNumText;
	private ViewGroup mFoodGroup;
	private EditText mFoodText;
	
	public static final int REQUEST_ALBUM = 1;
	public static final int REQUEST_CAMERA = 2;
	public static int MAX_PIC_WIDTH = 0;

	private List<String> mPics;
	private Uri mCameraPicUri;
	private View mDeleteView;
	private int mRate = 100;
	private String mTaskSchedulerID;
	private CustomToast mProgressDialog;
	private UpdateDailyTask mUpdateDailyTask;
	private boolean mSharetoWeixin = false;
	private int mTaskType;
	private DailyUpdate mDailyUpdate;
	private int mMode = MODE_NEW;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
//			mSharetoWeixin = false;
//			mUpdateDailyTask = new UpdateDailyTask();
//			try{
//				mUpdateDailyTask.execute();
//			}catch(RejectedExecutionException e){
//				e.printStackTrace();
//			}
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.update_schedule); 
		setTitleBar(getString(R.string.back), getString(R.string.update_schedule), "");
		MAX_PIC_WIDTH = getResources().getDimensionPixelSize(R.dimen.new_storge_pic_width);
		
		Intent intent = getIntent();
		mTaskSchedulerID = null;
		if(intent != null){
			mDailyUpdate = (DailyUpdate) intent.getSerializableExtra(EXTRA_DAILYUPDATE);
			mTaskType = intent.getExtras().getInt(Constants.TASK_SCHEDULER_TYPE);
			mMode = intent.getExtras().getInt(EXTRA_MODE, MODE_NEW);
			if (mDailyUpdate == null){
				mTaskSchedulerID = intent.getExtras().getString(Constants.TASK_SCHEDULER_ID);
			}else{
				mTaskSchedulerID = mDailyUpdate.getScheduleId();
			}
		}

		initCircleView();
		mDateDay = (TextView)findViewById(R.id.date_day);
		mDateWeek = (TextView)findViewById(R.id.date_week);
		setDate();
		
		mInsertPics = (ImageView)findViewById(R.id.add_pic_btn);
		mPic1 = (ImageView)findViewById(R.id.add_pic1);
		mPic2 = (ImageView)findViewById(R.id.add_pic2);
		mPic3 = (ImageView)findViewById(R.id.add_pic3);
		mPic1.setOnClickListener(this);
		mPic2.setOnClickListener(this);
		mPic3.setOnClickListener(this);
		mPic1.setOnLongClickListener(this);
		mPic2.setOnLongClickListener(this);
		mPic3.setOnLongClickListener(this);
		if (mDailyUpdate != null){
			mPics = mDailyUpdate.getPics();
		}

		mFoodGroup = (ViewGroup)findViewById(R.id.food_group);
		mFoodText = (EditText)findViewById(R.id.tv_food);
		if (mTaskType == Constants.TASK_SCHEDULER_OFFICIAL){
			mFoodGroup.setVisibility(View.VISIBLE);
			if (mDailyUpdate != null){
				String food = "";
				String customize = mDailyUpdate.getCustomize();
				if (!TextUtils.isEmpty(customize)){
					try {
						JSONObject obj = new JSONObject(customize);
						food = obj.optString(Constants.CUSTOMIZE_FOOD);
					} catch (JSONException e) {
						food = "";
					}
				}
				mFoodText.setText(food);
			}
		}else{
			mFoodGroup.setVisibility(View.GONE);
		}
		
		mInputNumText = (TextView)findViewById(R.id.input_num);
		mEditText = (EditText)findViewById(R.id.content);
		if (mDailyUpdate != null){
			mEditText.setText(mDailyUpdate.getContent());
		}
		mSubmit = (Button)findViewById(R.id.submit);
		setInputNumText();
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				setInputNumText();
			}
		});

		mSubmit.setOnClickListener(this);		
		mSubmitNoShare = (Button)findViewById(R.id.submit_no_share);
		mSubmitNoShare.setOnClickListener(this);
		mInsertPics.setOnClickListener(this);
	}
	
	private void setInputNumText(){
		int restNum;
		if (TextUtils.isEmpty(mEditText.getText())){
			restNum = Constants.MAX_INPUT_TEXT_NUM;
		}else{
			restNum = Constants.MAX_INPUT_TEXT_NUM - (int)Utils.calculateLength(mEditText.getText().toString());
		}
		if (restNum > 0){
			mInputNumText.setTextColor(getResources().getColor(R.color.color_gray));
		}else{
			mInputNumText.setTextColor(getResources().getColor(R.color.color_red));
		}
		mInputNumText.setText("(" + restNum + ")");
	}

	private void setDate(){
		long millis = System.currentTimeMillis();
		MyDate myDate = new MyDate(this, millis+"");
		if (mDailyUpdate != null){
			myDate = mDailyUpdate.getDate();
		}
		mDateDay.setText(myDate.day);
		mDateWeek.setText(myDate.week);
	}
 
	private void initCircleView(){
		mCircle1 = (CircleView)findViewById(R.id.circle1);
		mCircle2 = (CircleView)findViewById(R.id.circle2);
		mCircle3 = (CircleView)findViewById(R.id.circle3);
		mCircle4 = (CircleView)findViewById(R.id.circle4);
		mCircle5 = (CircleView)findViewById(R.id.circle5);
		mCircle6 = (CircleView)findViewById(R.id.circle6);
		mCircle7 = (CircleView)findViewById(R.id.circle7);
		
		String dailyRate = "100";
		if (mDailyUpdate != null){
			dailyRate = mDailyUpdate.getDailyrate();
		}
		
		mCircle1.setSchedule("0");
		mCircle1.setPercent("0".equals(dailyRate));
		mCircle2.setSchedule("25");
		mCircle2.setPercent("25".equals(dailyRate));
		mCircle3.setSchedule("50");
		mCircle3.setPercent("50".equals(dailyRate));
		mCircle4.setSchedule("75");
		mCircle4.setPercent("75".equals(dailyRate));
		mCircle5.setSchedule("100");
		mCircle5.setPercent("100".equals(dailyRate));
		mCircle6.setSchedule("125");
		mCircle6.setPercent("125".equals(dailyRate));
		mCircle7.setSchedule("150");
		mCircle7.setPercent("150".equals(dailyRate));
		mCircle1.setCircleListener(this);
		mCircle2.setCircleListener(this);
		mCircle3.setCircleListener(this);
		mCircle4.setCircleListener(this);
		mCircle5.setCircleListener(this);
		mCircle6.setCircleListener(this);
		mCircle7.setCircleListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mUpdateDailyTask != null && mUpdateDailyTask.getStatus() == AsyncTask.Status.RUNNING){
			 mUpdateDailyTask.cancel(true);
			 mUpdateDailyTask = null;
		}

		dismissProgress();
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPageEnd("UpdateScheduleActivity"); 
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		setPicGroup();
		MobclickAgent.onPageStart("UpdateScheduleActivity");
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == mInsertPics){
			showPop();
		}else if (v == mPic1){
			Intent i = new Intent(this, PicFilterActivity.class);
			i.putStringArrayListExtra(PicFilterActivity.TAG_PIC_ATTACHMENTS, 
					(ArrayList<String>) mPics);
			i.putExtra(PicFilterActivity.TAG_CURRENT_PIC_INDEX, 0);
			startActivity(i);
		}else if (v == mPic2){
			Intent i = new Intent(this, PicFilterActivity.class);
			i.putStringArrayListExtra(PicFilterActivity.TAG_PIC_ATTACHMENTS, 
					(ArrayList<String>) mPics);
			i.putExtra(PicFilterActivity.TAG_CURRENT_PIC_INDEX, 1);
			startActivity(i);
		}else if (v == mPic3){
			Intent i = new Intent(this, PicFilterActivity.class);
			i.putStringArrayListExtra(PicFilterActivity.TAG_PIC_ATTACHMENTS, 
					(ArrayList<String>) mPics);
			i.putExtra(PicFilterActivity.TAG_CURRENT_PIC_INDEX, 2);
			startActivity(i);
		}else if(v == mSubmit){
			if(mTaskSchedulerID == null){
				//任务id不能为空
				return;
			}
			mSharetoWeixin = true;
			mUpdateDailyTask = new UpdateDailyTask();
			try{
				mUpdateDailyTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}else if(v == mSubmitNoShare){
			if(mTaskSchedulerID == null){
				//任务id不能为空
				return;
			}
			mSharetoWeixin = false;
			mUpdateDailyTask = new UpdateDailyTask();
			try{
				mUpdateDailyTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		super.onClick(v);
	}

	private void showPop(){
		Button camera;
		Button album;
		Button cancel;
		
		final Dialog dialog = new Dialog(this, R.style.NoTitleDialog);
		LayoutInflater li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View v=li.inflate(R.layout.add_pic_pop_layout, null);
        camera = (Button)v.findViewById(R.id.camera);
        album = (Button)v.findViewById(R.id.album);
        cancel = (Button)v.findViewById(R.id.cancel);
        dialog.setContentView(v);
		Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.4); 
        p.width = (int) d.getWidth(); 
        dialogWindow.setAttributes(p);
 
        camera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (FileUtils.hasSDCardMounted()){
		            String picUri = Constants.CAMERA_IMAGE_BUCKET_NAME + System.currentTimeMillis() + ".jpg";
		            mCameraPicUri = Uri.fromFile( new File( picUri ) );
		            FileUtils.makesureParentExist( picUri );
		            
		            try{
			            Intent i = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			            i.putExtra( MediaStore.EXTRA_OUTPUT, mCameraPicUri );
			            startActivityForResult( i, REQUEST_CAMERA );
		            }catch(ActivityNotFoundException e){
						Utils.showToast(UpdateScheduleActivity.this, R.string.camera_not_allowed, Toast.LENGTH_SHORT);
						e.printStackTrace();
		            }

				}else{
					Utils.showToast(UpdateScheduleActivity.this, R.string.pls_insert_sdcard, Toast.LENGTH_SHORT);
				}
				dialog.dismiss();
			}
		});
        album.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UpdateScheduleActivity.this, PhotoAlbumActivity.class);
				int num=3;
				if (mPics != null){
					num = 3-mPics.size();
				}
				intent.putExtra(PhotoAlbumActivity.PARAM_DATA_SELECT_NUMBER, num);
				startActivityForResult(intent, REQUEST_ALBUM);
				dialog.dismiss();
			}
		});
        cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
        
        dialog.show();
	}

	@Override
	public void onCircleClick(View v) {
		mCircle1.mIsSelect = false;
		mCircle2.mIsSelect = false;
		mCircle3.mIsSelect = false;
		mCircle4.mIsSelect = false;
		mCircle5.mIsSelect = false;
		mCircle6.mIsSelect = false;
		mCircle7.mIsSelect = false;
		
		((CircleView)v).mIsSelect = true;
		
		mCircle1.setPercent(mCircle1.mIsSelect);
		mCircle2.setPercent(mCircle2.mIsSelect);
		mCircle3.setPercent(mCircle3.mIsSelect);
		mCircle4.setPercent(mCircle4.mIsSelect);
		mCircle5.setPercent(mCircle5.mIsSelect);
		mCircle6.setPercent(mCircle6.mIsSelect);
		mCircle7.setPercent(mCircle7.mIsSelect);
		
		mRate = Integer.parseInt(((CircleView)v).getSchedule());
	}
	
	private void showDeleteDialog(){
		new AlertDialog.Builder(this)
				.setMessage(R.string.delete_confirm)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mDeleteView == mPic1){
							FileUtils.deleteDependon(mPics.get(0));
							mPics.remove(0);
						}else if (mDeleteView == mPic2){
							if (mPics.size()>1){
								FileUtils.deleteDependon(mPics.get(1));
								mPics.remove(1);
							}else{
								FileUtils.deleteDependon(mPics.get(0));
								mPics.remove(0);
							}
						}else if (mDeleteView == mPic3){
							if (mPics.size()>2){
								FileUtils.deleteDependon(mPics.get(2));
								mPics.remove(2);
							}else if (mPics.size()>1){
								FileUtils.deleteDependon(mPics.get(1));
								mPics.remove(1);
							}else{
								FileUtils.deleteDependon(mPics.get(0));
								mPics.remove(0);
							}
						}
						setPicGroup();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	@Override
	public boolean onLongClick(View v) {
		boolean isShowDelete = false;
		if (v == mPic1){
			isShowDelete = true;
		}else if (v == mPic2){
			isShowDelete = true;
		}else if (v == mPic3){
			isShowDelete = true;
		}
		if (isShowDelete){
			mDeleteView = v;
			showDeleteDialog();
		}
		return false;
	}

	private void setImageView(String path, ImageView view){
		if (view == null || TextUtils.isEmpty(path)){
			return;
		}
		
		Bitmap bitmap = BitmapHelper.getBitmapFromFile(path, MAX_PIC_WIDTH, 
						MAX_PIC_WIDTH, true, true);
		if (bitmap != null && !bitmap.isRecycled()){
			view.setImageBitmap(bitmap);
		}else{
			view.setVisibility(View.GONE);
		}
	}

	private void setPicGroup(){
		if (mPics == null || mPics.isEmpty()){
			mInsertPics.setVisibility(View.VISIBLE);
			mPic1.setVisibility(View.GONE);
			mPic2.setVisibility(View.GONE);
			mPic3.setVisibility(View.GONE);
		}else if (mPics.size() == 3){
			mPic1.setVisibility(View.VISIBLE);
			mPic2.setVisibility(View.VISIBLE);
			mPic3.setVisibility(View.VISIBLE);
			mInsertPics.setVisibility(View.GONE);
			setImageView(mPics.get(0), mPic1);
			setImageView(mPics.get(1), mPic2);
			setImageView(mPics.get(2), mPic3);
		}else if (mPics.size() == 2){
			mPic1.setVisibility(View.VISIBLE);
			mPic2.setVisibility(View.VISIBLE);
			mPic3.setVisibility(View.GONE);
			mInsertPics.setVisibility(View.VISIBLE);
			setImageView(mPics.get(0), mPic1);
			setImageView(mPics.get(1), mPic2);
		}else{
			mPic1.setVisibility(View.VISIBLE);
			mPic2.setVisibility(View.GONE);
			mPic3.setVisibility(View.GONE);
			mInsertPics.setVisibility(View.VISIBLE);
			setImageView(mPics.get(0), mPic1);
		}
		if (mPic1.getVisibility() == View.VISIBLE){
			mSubmit.setBackgroundResource(R.drawable.button_selector);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mPics == null){
			mPics = new ArrayList<String>();
		}
		switch (requestCode) {
		case REQUEST_ALBUM:
			if (resultCode == RESULT_OK && data != null){
//				if (mPics != null){
//					mPics.addAll((ArrayList<String>)(data.getSerializableExtra(PhotoAlbumActivity.RETURN_DATA)));
//				}
				copyPic2ChenzaoPath((ArrayList<String>)(data.getSerializableExtra(PhotoAlbumActivity.RETURN_DATA)));
			}			
			break;
		case REQUEST_CAMERA:
            String orgPicPath = Utils.getAbsolutePath( this, mCameraPicUri );
			if (resultCode == Activity.RESULT_CANCELED){
                FileUtils.deleteDependon( orgPicPath );
				return;
			}
			if (FileUtils.doesExisted(orgPicPath)){
				mPics.add(orgPicPath);
			}else if( data != null &&  data.getData() != null) {
				mCameraPicUri = data.getData();
				orgPicPath = Utils.getAbsolutePath( this, mCameraPicUri );
				mPics.add(orgPicPath);
            }
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private void copyPic2ChenzaoPath(List<String> list){
		if (list == null || list.isEmpty()){
			return;
		}
		for (int i=0; i<list.size(); i++){
			if (list.get(i).startsWith(Constants.CAMERA_IMAGE_BUCKET_NAME)){
				mPics.add(list.get(i));
				continue;
			}
			String path = Constants.CAMERA_IMAGE_BUCKET_NAME + Utils.getMyUid(this)
					 + "_" + System.currentTimeMillis() + ".jpg";
			try {
				FileUtils.copy(list.get(i), path);
				mPics.add(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, UpdateScheduleActivity.this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
	
	class UpdateDailyTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";
			try {
				String customize = "";
				if (mTaskType == Constants.TASK_SCHEDULER_OFFICIAL){
					JSONObject obj= new JSONObject();
					obj.put(Constants.CUSTOMIZE_FOOD, mFoodText.getText().toString());
					customize = obj.toString();
				}
				if (mMode == MODE_NEW){
					result = NetEngine.getInstance(UpdateScheduleActivity.this).updateDailyTaskScheduler(Utils.getMyUid(getApplicationContext()),
							mTaskSchedulerID, mRate, mEditText.getText().toString(), mPics, 0, customize);
				}else{
					result = NetEngine.getInstance(UpdateScheduleActivity.this).modifyDailyUpdate(Utils.getMyUid(getApplicationContext()),
							mTaskSchedulerID, mDailyUpdate.getId(), mRate, mEditText.getText().toString(), mPics, 0, customize);
				}
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
			} catch (JSONException e) {
				mThr = e;
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, UpdateScheduleActivity.this);
				}else{
					Utils.showToast(UpdateScheduleActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			
			if (mMode == MODE_NEW){
				ChenzaoDBAdapter db = new ChenzaoDBAdapter(getApplicationContext());
				db.open();
				TaskScheduler task = db.getTaskSchedulerById(mTaskSchedulerID);
				if(task != null){
					JSONObject object = null;
					try {
						object = new JSONObject(result);
						task.setProgress(Float.parseFloat(object.getString("progress")));
						task.setUpdateTime(Long.valueOf(object.getString("updateDate")));
						task.setUpdateCount(task.getUpdateCount()+1);
						db.update(task);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
				db.close();
	
				Toast.makeText(getApplicationContext(), "上传当日任务进展成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.putExtra("progress", task.getProgress());
				intent.putExtra("updateTime", task.getUpdateTime());
				intent.putExtra("updateCount", task.getUpdateCount());
				setResult(Activity.RESULT_OK, intent);
				
				if(!mSharetoWeixin){
					finish();
					return;
				}
				mSharetoWeixin = false;
				String rate = String.valueOf(task.getProgress());
				if(rate.contains(".")){
					int index = rate.indexOf('.');
					rate = rate.substring(0, index + 2);
					if(rate.equals("0.0")){
						rate = "0";
					}
				}
				
				String content = String.format(getString(R.string.share_WX_update), 
									task.getTitle(),
									Utils.getLastTime(task),
									mEditText.getEditableText().toString(),
									rate + "%");
				WXUtils.shareToWX(UpdateScheduleActivity.this, 
									getString(R.string.update_schedule), 
									content, 
									null);
			}else{
				if (!TextUtils.isEmpty(mEditText.getText().toString())){
					mDailyUpdate.setContent(mEditText.getText().toString());
				}
				if (!TextUtils.isEmpty(mFoodText.getText().toString())){
					JSONObject obj= new JSONObject();
					try {
						obj.put(Constants.CUSTOMIZE_FOOD, mFoodText.getText().toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String customize = obj.toString();
					if (!TextUtils.isEmpty(customize)){
						mDailyUpdate.setCustomize(customize);
					}
				}
				if (mPics != null && !mPics.isEmpty()){
					mDailyUpdate.setPics(mPics);
				}
				Intent intent = new Intent();
				intent.putExtra(EXTRA_DAILYUPDATE, mDailyUpdate);
				setResult(Activity.RESULT_OK, intent);

				Intent successIntent = new Intent(Constants.MODIFY_DAILYUPDATE_SUCCESS);
				sendBroadcast(successIntent);
				
				Toast.makeText(getApplicationContext(), "上传当日任务进展成功", Toast.LENGTH_SHORT).show();

				if(!mSharetoWeixin){
					finish();
					return;
				}
				mSharetoWeixin = false;
				ChenzaoDBAdapter db = new ChenzaoDBAdapter(getApplicationContext());
				db.open();
				TaskScheduler task = db.getTaskSchedulerById(mTaskSchedulerID);
				if(task != null){
					JSONObject object = null;
					try {
						object = new JSONObject(result);
						task.setProgress(Float.parseFloat(object.getString("progress")));
						db.update(task);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
				db.close();
				String rate = String.valueOf(task.getProgress());
				if(rate.contains(".")){
					int index = rate.indexOf('.');
					rate = rate.substring(0, index + 2);
					if(rate.equals("0.0")){
						rate = "0";
					}
				}
				
				String content = String.format(getString(R.string.share_WX_update), 
									task.getTitle(),
									Utils.getLastTime(task),
									mEditText.getText().toString(),
									rate + "%");
				WXUtils.shareToWX(UpdateScheduleActivity.this, 
									getString(R.string.update_schedule), 
									content, 
									null);
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgress();
		}
		
	}

}
