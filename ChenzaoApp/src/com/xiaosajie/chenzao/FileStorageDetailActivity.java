package com.xiaosajie.chenzao;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.models.DailyUpdate;
import com.chenzao.models.MyDate;
import com.chenzao.models.QrcodeFile;
import com.chenzao.utils.BitmapHelper;
import com.chenzao.utils.Constants;
import com.chenzao.utils.FileUtils;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FileStorageDetailActivity extends BaseActivity {
	private TextView mDateDay;
	private TextView mDateWeek;
	private TextView mDateMonth;

	private ViewGroup mPicGroup;
	private ImageView mPic1;
	private ImageView mPic2;
	private ImageView mPic3;

	private ViewGroup mSoundGroup;
	private ImageView mPlaySound;
	private TextView mFileName;
	private MediaPlayer mPlayer;

	private ViewGroup mContentGroup;
	private TextView mContent;

	private ViewGroup mOfficialTaskGroup;
	private TextView mCustomize;
	private TextView mDayIndex;
	private ImageView mDayPic;
	
	private int PLAY_PREP = 1;
	private int PLAYING = 2;
	private int mPlayState;
	public static String EXTRA_FILE_ITEM = "EXTRA_FILE_ITEM";
	public static int MAX_PIC_WIDTH = 0;
	
	private int REQUEST_MODIFY = 1;
	
	private int mShowType;
	private MyDate mDate;
	private List<String> mPics;
	private String mContentString;
	private String mSoundPath;
	private int updateCount=0;
	private String mCustomizeString;
	private DailyUpdate mDailyUpdate;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			if (mDailyUpdate != null){
				Intent intent = new Intent(this, UpdateScheduleActivity.class);
				intent.putExtra(UpdateScheduleActivity.EXTRA_DAILYUPDATE, mDailyUpdate);
				intent.putExtra(Constants.TASK_SCHEDULER_TYPE, mShowType);
				intent.putExtra(UpdateScheduleActivity.EXTRA_MODE, UpdateScheduleActivity.MODE_MODIFY);
				startActivityForResult(intent, REQUEST_MODIFY);
			}
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.file_storage_detail);
		//setTitleBar(getString(R.string.back), getString(R.string.lable_message), null);

		mDateDay = (TextView)findViewById(R.id.date_day);
		mDateWeek = (TextView)findViewById(R.id.date_week);
		mDateMonth = (TextView)findViewById(R.id.date_month);
		
		mPicGroup = (ViewGroup)findViewById(R.id.pic_group);
		mPic1 = (ImageView)findViewById(R.id.add_pic1);
		mPic2 = (ImageView)findViewById(R.id.add_pic2);
		mPic3 = (ImageView)findViewById(R.id.add_pic3);
		mPic1.setOnClickListener(this);
		mPic2.setOnClickListener(this);
		mPic3.setOnClickListener(this);
		
		mSoundGroup = (ViewGroup)findViewById(R.id.sound_group);
		mPlaySound = (ImageView)findViewById(R.id.voice_icon);
		mFileName = (TextView)findViewById(R.id.file_name);
		
		mContentGroup = (ViewGroup)findViewById(R.id.content_group);
		mContent = (TextView)findViewById(R.id.content);
		
		mOfficialTaskGroup = (ViewGroup)findViewById(R.id.official_task_group);
		mCustomize = (TextView)findViewById(R.id.customize);
		mDayIndex = (TextView)findViewById(R.id.dayIndex);
		mDayPic = (ImageView)findViewById(R.id.dayPic);
		mDayPic.setOnClickListener(this);
		
		mPlaySound.setOnClickListener(this);
		
		initData();
		MAX_PIC_WIDTH = getResources().getDimensionPixelSize(R.dimen.new_storge_pic_width);
		updateView();
	}

	private void initData(){
		Intent i = getIntent();
		Object object = i.getSerializableExtra(EXTRA_FILE_ITEM);
		mShowType = i.getExtras().getInt(Constants.TASK_SCHEDULER_TYPE);
		if (object == null){
			finish();
		}
		if (object instanceof QrcodeFile){
			setTitleBar(getString(R.string.back), getString(R.string.lable_message), null);
			QrcodeFile file = (QrcodeFile) object;
			mDate = file.getDate();
			mPics = file.getPics();
			mContentString = file.getContent();
			mSoundPath = file.getSound();
		}else if (object instanceof DailyUpdate){
			setTitleBar(getString(R.string.back), getString(R.string.my_dailyupdates), 
					getString(R.string.change));
			DailyUpdate update = (DailyUpdate) object;
			mDailyUpdate = update;
			mDate = update.getDate();
			mPics = update.getPics();
			mContentString = update.getContent();
			mSoundPath = "";
			updateCount = update.getUpdateIndex();
			mCustomizeString = update.getCustomize();
		}
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
	
	public void updateView(){
		if (mDate != null){
			String month = mDate.year+getString(R.string.year)+
					mDate.month+getString(R.string.month);
			mDateMonth.setText(month);
			mDateDay.setText(mDate.day);
			mDateWeek.setText(mDate.week);			
		}
		
		if (mPics == null || mPics.isEmpty()){
			mPicGroup.setVisibility(View.GONE);
		}else{
			mPicGroup.setVisibility(View.VISIBLE);
			if (mPics.size()>=3){
				if (TextUtils.isEmpty(mPics.get(2)) || !FileUtils.isFileExist(mPics.get(2))){
					mPic3.setVisibility(View.GONE);
				}else{
					mPic3.setVisibility(View.VISIBLE);
					setImageView(mPics.get(2), mPic3);
				}
			}
			if (mPics.size()>=2){
				if (TextUtils.isEmpty(mPics.get(1)) || !FileUtils.isFileExist(mPics.get(1))){
					mPic2.setVisibility(View.GONE);
				}else{
					mPic2.setVisibility(View.VISIBLE);
					setImageView(mPics.get(1), mPic2);
				}
			}
			if (mPics.size()>=1){
				if (TextUtils.isEmpty(mPics.get(0)) || !FileUtils.isFileExist(mPics.get(0))){
					mPic1.setVisibility(View.GONE);
				}else{
					mPic1.setVisibility(View.VISIBLE);
					setImageView(mPics.get(0), mPic1);
				}
			}
			if (mPic1.getVisibility() == View.GONE
					&& mPic2.getVisibility() == View.GONE
					&& mPic3.getVisibility() == View.GONE){
				mPicGroup.setVisibility(View.GONE);
			}
		}
		
		if (TextUtils.isEmpty(mSoundPath) || !FileUtils.isFileExist(mSoundPath)){
			mSoundGroup.setVisibility(View.GONE);
		}else{
			mSoundGroup.setVisibility(View.VISIBLE);
			String name = Utils.getFileNameFromPath(mSoundPath);
			if (!TextUtils.isEmpty(name)){
				mFileName.setText(name);
			}
		}
		
		if (TextUtils.isEmpty(mContentString)){
			mContentGroup.setVisibility(View.GONE);
		}else{
			mContentGroup.setVisibility(View.VISIBLE);
			mContent.setText(mContentString);
		}
		
		if (mShowType == Constants.TASK_SCHEDULER_OFFICIAL){
			mOfficialTaskGroup.setVisibility(View.VISIBLE);
			String index;
			int dayindex = Utils.getDayPicIndex(updateCount);
			index = String.format(getResources().getString(R.string.day_index), dayindex);
			mDayIndex.setText(index);
			mDayPic.setImageResource(Utils.dayPics[dayindex-1]);
			String food = "";
			if (!TextUtils.isEmpty(mCustomizeString)){
				try {
					JSONObject obj = new JSONObject(mCustomizeString);
					food = obj.optString(Constants.CUSTOMIZE_FOOD);
				} catch (JSONException e) {
					food = "";
				}
			}
			if (TextUtils.isEmpty(food)){
				mCustomize.setVisibility(View.GONE);
			}else{
				mCustomize.setVisibility(View.VISIBLE);
				mCustomize.setText(getString(R.string.customize_food)+food);
			}

		}else{
			mOfficialTaskGroup.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		stopPlay();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPageEnd("NewFileStoreActivity"); 
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onPageStart("NewFileStoreActivity");
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == mPlaySound){
			if (mPlayState == PLAYING){
				stopPlay();
			}else{
				playAudio();
			}
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
		}else if (v == mDayPic){
			Intent i = new Intent(this, ImageViewerActivity.class);
			i.putExtra(ImageViewerActivity.EXTRA_PICINDEX, Utils.getDayPicIndex(updateCount));
			startActivity(i);
		}
		super.onClick(v);
	}

	private void playAudio(){
		try {
			if (mPlayer == null){
				mPlayer = new MediaPlayer();
			}
			mPlayer.reset();
			mPlayer.setDataSource(mSoundPath);
			mPlayer.prepare();
			mPlayer.start();
			mPlayState = PLAYING;
		} catch (Exception e) {
			mPlayState = PLAY_PREP;
			e.printStackTrace();
		}
	}
	
	private void stopPlay(){
		try {
			if (mPlayer != null){
				if (mPlayer.isPlaying()){
					mPlayer.stop();
					mPlayer.reset();
				}
				mPlayState = PLAY_PREP;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
		if (requestCode == REQUEST_MODIFY && data != null){
			DailyUpdate update = (DailyUpdate) data.getSerializableExtra(UpdateScheduleActivity.EXTRA_DAILYUPDATE);
			mDailyUpdate = update;
			mDate = update.getDate();
			mPics = update.getPics();
			mContentString = update.getContent();
			mSoundPath = "";
			updateCount = update.getUpdateIndex();
			mCustomizeString = update.getCustomize();
			
			updateView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
