package com.chenzao.zxing;


import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.xiaosajie.chenzao.FileStorageDetailActivity;
import com.xiaosajie.chenzao.NewFileStoreActivity;
import com.xiaosajie.chenzao.NewTaskSchedulerActivity;
import com.xiaosajie.chenzao.R;
import com.xiaosajie.chenzao.TaskSchedulerDetailActivity;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.QrcodeFile;
import com.chenzao.net.NetEngine;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.xiaosajie.chenzao.BaseActivity;


public class CaptureActivity extends BaseActivity implements Callback
{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private String mqrCode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setView(R.layout.zxing_main);
		setTitleBar(getString(R.string.back), "二维码扫描", "");

		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			initCamera(surfaceHolder);
		}
		else
		{
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
		{
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			CameraManager.get().openDriver(surfaceHolder);
		}
		catch (IOException ioe)
		{
			Utils.showToast(this, R.string.camera_error, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		catch (RuntimeException e)
		{
			Utils.showToast(this, R.string.camera_error, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		if (handler == null)
		{
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();

	}
public int value(char c){
	String s = "" + c;
	if(c >= '0' && c <= '9'){
		return Integer.parseInt(s);
	}else if(c >= 'a' && c <= 'f'){
		return (c - 'a' + 10);
	}else if(c >= 'A' && c <= 'F'){
		return (c - 'A' + 10);
	}
	return 0;
}
	public void handleDecode(final Result obj, Bitmap barcode)
	{
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String qrCode = obj.getText();
		mqrCode = qrCode;
		Log.d("wwt", "qrCode:"+qrCode);
		//24位长度二维码进行校验，判断是否为趁早网独有二维码
		if(qrCode.length() == 24){
			//二维码中的校验位
			int cCode0 = value(qrCode.charAt(2));
			int cCode1 = value(qrCode.charAt(11));
			int cCode2 = value(qrCode.charAt(23));
			
			//根据二维码信息计算校验位
			int type = value(qrCode.charAt(14));
			int c0 = value(qrCode.charAt(0)) ^ value(qrCode.charAt(1));
			int c1 = value(qrCode.charAt(3)) ^ value(qrCode.charAt(4))
			 		^ value(qrCode.charAt(5)) ^ value(qrCode.charAt(6))
			 		 ^ value(qrCode.charAt(7)) ^ value(qrCode.charAt(8))
			 		 ^ value(qrCode.charAt(9)) ^ value(qrCode.charAt(10));
			int c2 = value(qrCode.charAt(12)) ^ value(qrCode.charAt(13))
			 		 ^ value(qrCode.charAt(15)) ^ value(qrCode.charAt(16))
			 		 ^ value(qrCode.charAt(17)) ^ value(qrCode.charAt(18))
			 		 ^ value(qrCode.charAt(19)) ^ value(qrCode.charAt(20))
			 		 ^ value(qrCode.charAt(21)) ^ value(qrCode.charAt(22)) ^ type;
			if(c0 == cCode0 && c1 == cCode1 && c2 == cCode2){
				new VerifyQrcode().execute();
				return;
//				//符合此条件，说明是趁早网专用二维码
//				if(type == 0){
//					//任务计划类型
//				}else if(type == 1){
//					//文件存储类型
//					Intent intent  = new Intent(this, NewFileStoreActivity.class);
//					intent.putExtra(NewFileStoreActivity.EXTRA_QRCODE, mqrCode);
//					startActivity(intent);
//				}
//				Toast.makeText(CaptureActivity.this, "趁早网格式二维码", Toast.LENGTH_LONG).show();
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						try{
//							String result = NetEngine.getInstance(getApplicationContext()).verifyQrcode(Utils.getMyUid(getApplicationContext()), mqrCode);
//							try {
//								JSONObject obj = new JSONObject(result);
//							} catch (JSONException e) {
//								e.printStackTrace();
//							}
//						}catch(ChenzaoIOException e){
//							e.printStackTrace();
//						}
//					}
//				}).start();
//				
//				finish();
//
//				return;
			}
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		if (barcode == null)
		{
			dialog.setIcon(null);
		}
		else
		{

			Drawable drawable = new BitmapDrawable(barcode);
			dialog.setIcon(drawable);
		}
		dialog.setTitle("扫描成功");
		dialog.setMessage(obj.getText());
		dialog.setNegativeButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(obj.getText());
				intent.setData(content_url);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finish();
			}
		});
		dialog.setPositiveButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}
		});
		dialog.create().show();
	}

	private void initBeepSound()
	{
		if (playBeep && mediaPlayer == null)
		{
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.kakalib_scan);
			try
			{
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			}
			catch (IOException e)
			{
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate()
	{
		if (playBeep && mediaPlayer != null)
		{
			mediaPlayer.start();
		}
		if (vibrate)
		{
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener()
	{
		public void onCompletion(MediaPlayer mediaPlayer)
		{
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	protected void handleTitleBarEvent(int eventId) {
		// TODO Auto-generated method stub
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}		
	}

	class VerifyQrcode extends AsyncTask<Void, Void, String>{
		private Throwable mThr;
		@Override
		protected String doInBackground(Void... params) {
			String result = null;
			try {
				result = NetEngine.getInstance(getApplicationContext()).verifyQrcode(Utils.getMyUid(getApplicationContext()), mqrCode);
			} catch (ChenzaoIOException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (ChenzaoParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (ChenzaoApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mThr = e;
				return null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
				if (mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{					
				}
				finish();
				return;
			}
			try {
				JSONObject object = new JSONObject(result);
				int state = Integer.valueOf(object.optString("state"));
				switch (state) {
				//未使用存储项
				case 0:
					Intent intent  = new Intent(CaptureActivity.this, NewFileStoreActivity.class);
					intent.putExtra(NewFileStoreActivity.EXTRA_QRCODE, mqrCode);
					startActivity(intent);
					break;
				//已使用存储项
				case 1:
					JSONObject fileObject = object.optJSONObject("record");
					if (fileObject != null){
						QrcodeFile file = new QrcodeFile(CaptureActivity.this, fileObject);
						Intent i  = new Intent(CaptureActivity.this, FileStorageDetailActivity.class);
						i.putExtra(FileStorageDetailActivity.EXTRA_FILE_ITEM, (Serializable)file);
						i.putExtra(Constants.TASK_SCHEDULER_TYPE, 0);
						startActivity(i);						
					}
					break;
				//未使用计划任务
				case 2:
					Intent newintent  = new Intent(CaptureActivity.this, NewTaskSchedulerActivity.class);
					newintent.putExtra(NewTaskSchedulerActivity.EXTRA_QRCODE, mqrCode);
					startActivity(newintent);
					break;
				//已使用计划任务
				case 3:
					Intent newtaskintent  = new Intent(CaptureActivity.this, TaskSchedulerDetailActivity.class);
					startActivity(newtaskintent);
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
			finish();
		}
		
	}
}