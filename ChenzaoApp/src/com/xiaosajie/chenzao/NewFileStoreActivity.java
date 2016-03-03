package com.xiaosajie.chenzao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.MyDate;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.BitmapHelper;
import com.chenzao.utils.Constants;
import com.chenzao.utils.FileUtils;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class NewFileStoreActivity extends BaseActivity implements OnClickListener, OnLongClickListener{
	private TextView mDateDay;
	private TextView mDateWeek;
	private TextView mDateMonth;
	
	private ImageView mInsertPics;
	private ImageView mPic1;
	private ImageView mPic2;
	private ImageView mPic3;
	
	private ImageView mStartRecord;
	private ViewGroup mRecordGroup;
	private ViewGroup mFileGroup;
	private ImageView mDeleteBtn;
	private TextView mRecordContent;
	private ImageView mPlaySound;
	private TextView mFileName;
	
	private Button mSubmit;
	private EditText mEditText;
	private TextView mInputNumText;

	public static final int REQUEST_ALBUM = 1;
	public static final int REQUEST_CAMERA = 2;
	public static String EXTRA_QRCODE = "EXTRA_QRCODE";
	private int PLAY_PREP = 1;
	private int PLAYING = 2;
	
	private List<String> mPics;
	private String mQrcode;
	private String mSoundFilePath;
	private AddRecordTask mTask;
	private CustomToast mProgressDialog;
	private Uri mCameraPicUri;
	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private String mInputFileName;
	private int mPlayState;
	public static int MAX_PIC_WIDTH = 0;
	private View mDeleteView;

	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.new_file_store_activity);
		setTitleBar(getString(R.string.back), getString(R.string.storge_message), null);	
		
		mDateDay = (TextView)findViewById(R.id.date_day);
		mDateWeek = (TextView)findViewById(R.id.date_week);
		mDateMonth = (TextView)findViewById(R.id.date_month);
		
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
		
		mStartRecord = (ImageView)findViewById(R.id.record_btn);
		mRecordContent = (TextView)findViewById(R.id.record_content);
		mRecordGroup = (ViewGroup)findViewById(R.id.record_btn_group);
		mFileGroup = (ViewGroup)findViewById(R.id.record_file_group);
		mDeleteBtn = (ImageView)findViewById(R.id.delete_icon);
		mPlaySound = (ImageView)findViewById(R.id.voice_icon);
		mFileName = (TextView)findViewById(R.id.file_name);
		
		mInputNumText = (TextView)findViewById(R.id.input_num);
		mEditText = (EditText)findViewById(R.id.content);
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
		mInsertPics.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
		mPlaySound.setOnClickListener(this);

		mStartRecord.setTouchDelegate(new TouchDelegate(new Rect(), mStartRecord){
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				switch (event.getAction()) {
		        case MotionEvent.ACTION_DOWN:
		        	startAudioRecord();
		        	mStartRecord.setPressed(true);
		            break;
		        case MotionEvent.ACTION_UP:
		        case MotionEvent.ACTION_CANCEL:
		        	mStartRecord.setPressed(false);
		        	stopAudioRecord();
		        	break;
		        case MotionEvent.ACTION_MOVE:
		            break;
				}
				return true;
			}
		});

		MAX_PIC_WIDTH = getResources().getDimensionPixelSize(R.dimen.new_storge_pic_width);
		setDate();
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null){
			mQrcode = intent.getExtras().getString(EXTRA_QRCODE);
		}
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
	
	private String generateRecordPath(){
		StringBuilder sBuilder = new StringBuilder();
		String skipMiderScan = new StringBuilder().append(Utils.getSDPath())
				.append(Constants.ORI_AUDIO_DIR_SUFFIX).append(".nomedia").toString();
		File file = new File(skipMiderScan);
		FileUtils.makesureFileExist(file);
		
		sBuilder.append(Utils.getSDPath())
				.append(Constants.ORI_AUDIO_DIR_SUFFIX)
				.append(System.currentTimeMillis())
				.append(".amr");
		return sBuilder.toString();
	}
	
	private void startAudioRecord(){
		if (!FileUtils.hasSDCardMounted()){
			Utils.showToast(this, R.string.pls_insert_sdcard, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.haveFreeSpace()){
			Utils.showToast(this, R.string.have_no_enough_external_space, Toast.LENGTH_SHORT);
			return;
		}
    	mRecordContent.setText(R.string.stop_record);
        try {
        	if (TextUtils.isEmpty(mSoundFilePath)){
        		mSoundFilePath = generateRecordPath();
        	}
	    	mRecorder = new MediaRecorder();
	    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
	        mRecorder.setOutputFile(mSoundFilePath);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	        //mRecorder.setMaxDuration(10*60*1000) //最大录音时间
	        mRecorder.prepare();
	        mRecorder.start();
        } catch (Exception e) {
            Utils.showToast(this, R.string.voice_record_err, Toast.LENGTH_SHORT);
            mRecordContent.setText(R.string.start_record);
        }
	}
	
	private void stopAudioRecord(){
		if (mRecorder != null){
			try {
				mRecorder.stop();
		        mRecorder.release();
		        mRecorder = null;
			} catch (Exception e) {
				Utils.showToast(this, R.string.start_record, Toast.LENGTH_SHORT);
			}
            mRecordContent.setText(R.string.start_record);
            showSaveDialog();
		}
	}
	
	private void showSaveDialog(){
		final EditText etFileName;
		Button ok;
		Button cancel;
		
		final Dialog dialog = new Dialog(this, R.style.NoTitleDialog);
		LayoutInflater li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View v=li.inflate(R.layout.save_record_layout, null);
        etFileName = (EditText)v.findViewById(R.id.file_name);
        ok = (Button)v.findViewById(R.id.btn_ok);
        cancel = (Button)v.findViewById(R.id.btn_cancel);
        ok.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mInputFileName = etFileName.getText().toString();
				if (TextUtils.isEmpty(mInputFileName)){
					return;
				}
				String destFilePath = Utils.getSDPath()+Constants.ORI_AUDIO_DIR_SUFFIX
							+mInputFileName+".amr";
				File src = new File(mSoundFilePath);
				File dest = new File(destFilePath);
				try {
					FileUtils.renameTo(src, dest);
					mSoundFilePath = destFilePath;
					setSoundGroup();
					dialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
					
				
			}
		});
        cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.deleteDependon(mSoundFilePath);
				mSoundFilePath = null;
				setSoundGroup();
				dialog.dismiss();
			}
		});
		
        dialog.setContentView(v);
		Window dialogWindow = dialog.getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.35); 
        p.width = (int) (d.getWidth() * 0.8); 
        dialogWindow.setAttributes(p);

        dialog.setCancelable(false);                
        dialog.show();
		
	}
	
	private void setDate(){
		String month;
		long millis = System.currentTimeMillis();
		MyDate myDate = new MyDate(this, millis+"");
		month = myDate.year+getString(R.string.year)+myDate.month+getString(R.string.month);
		mDateMonth.setText(month);
		mDateDay.setText(myDate.day);
		mDateWeek.setText(myDate.week);
	}
	
	private void setSoundGroup(){
		//if (TextUtils.isEmpty(mSoundFilePath)){
		if (TextUtils.isEmpty(mSoundFilePath) || !FileUtils.isFileExist(mSoundFilePath)){
			mRecordGroup.setVisibility(View.VISIBLE);
			mFileGroup.setVisibility(View.GONE);
			mRecordContent.setText(R.string.start_record);
		}else{
			mRecordGroup.setVisibility(View.GONE);
			mFileGroup.setVisibility(View.VISIBLE);
			mFileName.setText(mInputFileName);
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
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("NewFileStoreActivity"); 
		MobclickAgent.onPause(this);
		dismissProgress();
		stopPlay();
	}

	@Override
	protected void onResume() {
		setPicGroup();
		setSoundGroup();
		super.onResume();
		MobclickAgent.onPageStart("NewFileStoreActivity");
		MobclickAgent.onResume(this);
	}
	
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
						Utils.showToast(NewFileStoreActivity.this, R.string.camera_not_allowed, Toast.LENGTH_SHORT);
						e.printStackTrace();
		            }

				}else{
					Utils.showToast(NewFileStoreActivity.this, R.string.pls_insert_sdcard, Toast.LENGTH_SHORT);
				}
				dialog.dismiss();
			}
		});
        album.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewFileStoreActivity.this, PhotoAlbumActivity.class);
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
	
	private void showSubmitDialog(){
		new AlertDialog.Builder(this)
		.setMessage(R.string.submit_confirm)
		.setCancelable(true)
		.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doSubmit();
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}
	
	@Override
	public void onClick(View v) {
		if (v == mInsertPics){
			showPop();
		}else if (v == mSubmit){
			if (TextUtils.isEmpty(mEditText.getText().toString())
					&&mPic1.getVisibility() == View.GONE 
					&&mFileGroup.getVisibility() == View.GONE){
				Utils.showToast(this, getString(R.string.no_change_submit_notice), Toast.LENGTH_SHORT);
				return;
			}
			showSubmitDialog();
		}else if (v == mDeleteBtn){
			//删掉录音文件
			FileUtils.deleteDependon(mSoundFilePath);
			mSoundFilePath = null;
			setSoundGroup();
		}else if (v == mPlaySound){
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
		}
		super.onClick(v);
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
	
	private void playAudio(){
		try {
			if (mPlayer == null){
				mPlayer = new MediaPlayer();
			}
			mPlayer.reset();
			mPlayer.setDataSource(mSoundFilePath);
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
	
	private void doSubmit(){
		mTask = new AddRecordTask();
		try {
			mTask.execute();
			showProgress();
		} catch (RejectedExecutionException e) {
		}
		
	}
	
	class AddRecordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result="";
			try {
				result = NetEngine.getInstance(NewFileStoreActivity.this).
						addNewFileRecord(Utils.getMyUid(NewFileStoreActivity.this), mQrcode, 
								mEditText.getText().toString(), mSoundFilePath, mPics);
			} catch (ChenzaoIOException e) {
				mThr = e;
				e.printStackTrace();
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
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(NewFileStoreActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject object = null;
			String suc = null;
			try {
				object = new JSONObject(result);
				suc = object.optString("recordid");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
        	if (!TextUtils.isEmpty(suc)){
				Utils.showToast(NewFileStoreActivity.this, R.string.upload_success, Toast.LENGTH_SHORT);
				MainActivity.mMode = MainActivity.MODE_STORAGELIST;
				finish();
        	}else{
				Utils.showToast(NewFileStoreActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT);
        	}
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}
		
	}
}
