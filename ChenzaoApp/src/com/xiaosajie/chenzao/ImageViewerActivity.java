package com.xiaosajie.chenzao;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.chenzao.utils.BitmapUtils;
import com.chenzao.utils.FileUtils;
import com.chenzao.utils.Constants;
import com.chenzao.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageViewerActivity extends Activity implements OnClickListener{
	public static String EXTRA_PICINDEX = "picindex";
	private WebView mGifView;
	private ImageView mImageView;
	private Bitmap mBitmap;
	private String mTempPath;
	private boolean isShowTitleBar = false;
	private ViewGroup mTitleBar;
	private ImageView mBtnBack;
	private int mPicIndex=100;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.imageviewer);

        Intent intent = getIntent();
        if (intent != null){
        	mPicIndex = intent.getExtras().getInt(EXTRA_PICINDEX);
        }
		String index = String.format(getResources().getString(R.string.day_index), mPicIndex);
		((TextView)findViewById(R.id.titleText)).setText(index);
		
        mTitleBar = (ViewGroup)findViewById(R.id.titleBar);
        mBtnBack = (ImageView)findViewById(R.id.titleBack);
        mGifView = (WebView)findViewById(R.id.gif_pic);
        mImageView = (ImageView)findViewById(R.id.normal_pic);
        mImageView.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mGifView.setOnTouchListener( new View.OnTouchListener() {
            float mStartX = 0;
            float mStartY = 0;

            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = event.getX();
                        mStartY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果move的距离很小,则认为是click事件,隐藏或者显示标题
                        if (Math.abs( mStartX - event.getX() ) < 5f
                                && Math.abs( mStartY - event.getY() ) < 5f) {
                			isShowTitleBar = !isShowTitleBar;
                			showTitleBar(isShowTitleBar);
                        }
                        mStartX = 0;
                        mStartY = 0;
                        break;
                    default:
                        break;
                }
                return false;
            }
        } );
        
        
        showPic();
	}

	private void showPic(){
		switch (mPicIndex){
		case 1:
		case 6:
		case 23:
		case 36:
		case 45:
		case 58:
		case 67:
		case 76:
		case 86:
		case 94:
		case 98:
			showGif();
			break;
		default:
			showNormalPic();
		}
	}
	
	private void showGif(){
		mGifView.setVisibility( View.GONE );
		mGifView.setBackgroundColor( 0x00000000 );
		mGifView.getSettings().setLayoutAlgorithm( LayoutAlgorithm.SINGLE_COLUMN );
		mGifView.setScrollBarStyle( WebView.SCROLLBARS_OUTSIDE_OVERLAY );

		mTempPath = Constants.CAMERA_IMAGE_BUCKET_NAME + "temp.gif";
		int index = mPicIndex > 0 ? mPicIndex-1 : 0;
		savePic2File(Utils.dayPics[index], mTempPath);
		String str = "file://" + mTempPath;
        try {
			mGifView.loadUrl(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        mGifView.setVisibility( View.VISIBLE );
	}
	
	private void savePic2File(int id, String path){
		if (TextUtils.isEmpty(path)){
			return ;
		}
		
		try {
			FileUtils.makesureParentExist(path);
			
			InputStream is = getResources().openRawResource(id); 
			
			FileOutputStream fos = new FileOutputStream(path); 
 
			byte[] buffer = new byte[8192]; 
			System.out.println("3"); 
			int count = 0; 
			 
			// 开始复制Logo图片文件 
			while((count=is.read(buffer)) > 0) 
			{ 
			    fos.write(buffer, 0, count); 
			} 
			fos.close(); 
			is.close();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private void showNormalPic(){
		int scaleWidth;
		int scaleHeight;
		Display display=getWindowManager().getDefaultDisplay();
		int index = mPicIndex > 0 ? mPicIndex-1 : 0;
		Bitmap bp = BitmapFactory.decodeResource(getResources(), Utils.dayPics[index]);
		int width = bp.getWidth();
		int height = bp.getHeight();
		int w = display.getWidth();
		int h = display.getHeight();
		scaleWidth = w;
		scaleHeight = (w/width)*height;
		
		try {
			mBitmap = BitmapUtils.createScaledBitmap(bp, scaleWidth, scaleHeight, Bitmap.Config.ARGB_4444);
	        if (mBitmap != null && !mBitmap.isRecycled()){
				bp.recycle();
				bp = mBitmap;
	        }
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}finally{
			mImageView.setImageBitmap(bp);
	        mImageView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onPause() {
		MobclickAgent.onPageEnd("ImageViewerActivity"); 
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onPageStart("ImageViewerActivity");
		MobclickAgent.onResume(this);		
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.isRecycled();
		}
		if (mGifView != null){
			((RelativeLayout)mGifView.getParent()).removeView(mGifView);
			mGifView.clearCache( true );
			mGifView.removeAllViews();
			mGifView.destroy();
			mGifView = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mImageView){
			isShowTitleBar = !isShowTitleBar;
			showTitleBar(isShowTitleBar);
		}else if (v == mBtnBack){
			finish();
		}
	}

	private void showTitleBar(boolean show){
		if (isShowTitleBar){
			mTitleBar.setVisibility(View.VISIBLE);
		}else{
			mTitleBar.setVisibility(View.GONE);
		}
	}
}
