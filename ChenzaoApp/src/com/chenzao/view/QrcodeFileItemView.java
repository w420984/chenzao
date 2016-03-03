package com.chenzao.view;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.chenzao.models.DailyUpdate;
import com.chenzao.models.MyDate;
import com.chenzao.models.QrcodeFile;
import com.chenzao.utils.BitmapHelper;
import com.chenzao.utils.Constants;
import com.chenzao.utils.FileUtils;
import com.chenzao.utils.Utils;
import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QrcodeFileItemView extends RelativeLayout {
	private TextView mDateDay;
	private TextView mDateWeek;
	private ImageView mPic1;
	private ImageView mPic2;
	private ImageView mPic3;
	private TextView mContent;
	private ImageView mVoice;
	private ViewGroup mPicGroup;
	private TextView mDayIndex;
	private ImageView mDayPic;
	private ViewGroup mDayGroup;
	private ViewGroup mContentGroup;
	private TextView mCustomize;
	private Context mContext;
	private int mShowType = 0;  //0表示存储项，1表示私人任务，2表示官方任务
	
	private Map<String, Bitmap> mBmpCache;
	public static int MAX_PIC_WIDTH = 0;

	public QrcodeFileItemView(Context context){
		super(context);
		mContext = context;
		init();
	}
	
	public QrcodeFileItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public QrcodeFileItemView(Context context, int showType, Object item, Map<String, Bitmap> map){
		super(context);
		mBmpCache = map;
		mShowType = showType;
		mContext = context;
		init();
		update(item);
	}
	
	private void init(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.qrcode_file_item, this);
		
		mDateDay = (TextView)findViewById(R.id.date_day);
		mDateWeek = (TextView)findViewById(R.id.date_week);
		mPic1 = (ImageView)findViewById(R.id.pic1);
		mPic2 = (ImageView)findViewById(R.id.pic2);
		mPic3 = (ImageView)findViewById(R.id.pic3);
		mPicGroup = (ViewGroup)findViewById(R.id.pics);
		mContent = (TextView)findViewById(R.id.content);
		mVoice = (ImageView)findViewById(R.id.recordIcon);
		mDayIndex = (TextView)findViewById(R.id.dayIndex);
		mDayPic = (ImageView)findViewById(R.id.dayPic);
		mDayGroup = (ViewGroup)findViewById(R.id.official_task_group);
		mContentGroup = (ViewGroup)findViewById(R.id.contentGroup);
		mCustomize = (TextView)findViewById(R.id.customize);
		MAX_PIC_WIDTH = getResources().getDimensionPixelSize(R.dimen.qrcode_file_item_pic_width);
	}
	
	private void setImageView(String path, ImageView view){
		if (view == null || TextUtils.isEmpty(path)){
			return;
		}
		
		Bitmap bitmap = Utils.getBitmapCache(path, mBmpCache);
		if (bitmap != null && !bitmap.isRecycled()){
			view.setImageBitmap(bitmap);
		}else{
			bitmap = BitmapHelper.getBitmapFromFile(path, MAX_PIC_WIDTH, 
					MAX_PIC_WIDTH, true, true);
			if (bitmap != null && !bitmap.isRecycled()){
				view.setImageBitmap(bitmap);
				Utils.saveBitmapCache(path, bitmap, mBmpCache);
			}else{
				view.setVisibility(View.GONE);
			}
		}
	}
	
	public void update(Object item){
		MyDate date = null;
		List<String> pics = null;
		String content = "";
		String sound = "";
		int dayIndex = 0;
		String customize = "";
		
		if (item instanceof QrcodeFile){
			QrcodeFile file = (QrcodeFile) item;
			date = file.getDate();
			pics = file.getPics();
			content = file.getContent();
			sound = file.getSound();
		}else if (item instanceof DailyUpdate){
			DailyUpdate update = (DailyUpdate) item;
			date = update.getDate();
			pics = update.getPics();
			content = update.getContent();
			sound = "";
			dayIndex = update.getUpdateIndex();
			customize = update.getCustomize();
		}
		
		mDateDay.setText(date.day);
		mDateWeek.setText(date.week);
		if (pics.isEmpty()){
			mPicGroup.setVisibility(View.GONE);
		}else{
			mPicGroup.setVisibility(View.VISIBLE);
			if (pics.size() >= 3){
				if (TextUtils.isEmpty(pics.get(2)) || !FileUtils.isFileExist(pics.get(2))){
					mPic3.setVisibility(View.GONE);
				}else{
					mPic3.setVisibility(View.VISIBLE);
					setImageView(pics.get(2), mPic3);
				}
			}
			if (pics.size() >= 2){
				if (TextUtils.isEmpty(pics.get(1)) || !FileUtils.isFileExist(pics.get(1))){
					mPic2.setVisibility(View.GONE);
				}else{
					mPic2.setVisibility(View.VISIBLE);
					setImageView(pics.get(1), mPic2);
				}
			}
			if (pics.size() >= 1){
				if (TextUtils.isEmpty(pics.get(0)) || !FileUtils.isFileExist(pics.get(0))){
					mPic1.setVisibility(View.GONE);
				}else{
					mPic1.setVisibility(View.VISIBLE);
					setImageView(pics.get(0), mPic1);
				}
			}
			
			if (mPic1.getVisibility() == View.GONE
					&& mPic2.getVisibility() == View.GONE
					&& mPic3.getVisibility() == View.GONE){
				mPicGroup.setVisibility(View.GONE);
			}
		}
		
		if (TextUtils.isEmpty(content)){
			mContent.setVisibility(View.GONE);
		}else{
			mContentGroup.setVisibility(View.VISIBLE);
			mContent.setVisibility(View.VISIBLE);
			mContent.setText(content);
		}
		
		if (TextUtils.isEmpty(sound) || !FileUtils.isFileExist(sound)){
			mVoice.setVisibility(View.GONE);
		}else{
			mContentGroup.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.VISIBLE);
		}
		
		if (mVoice.getVisibility() == View.GONE 
				&& mContent.getVisibility() == View.GONE){
			findViewById(R.id.contentGroup).setVisibility(View.GONE);
		}
		
		if (mShowType == Constants.TASK_SCHEDULER_OFFICIAL){
			mDayGroup.setVisibility(View.VISIBLE);
			String index;
			dayIndex = Utils.getDayPicIndex(dayIndex);
			index = String.format(getResources().getString(R.string.day_index), dayIndex);
			mDayIndex.setText(index);
			mDayPic.setImageResource(Utils.dayPics[dayIndex-1]);
			String food = "";
			if (!TextUtils.isEmpty(customize)){
				try {
					JSONObject obj = new JSONObject(customize);
					food = obj.optString(Constants.CUSTOMIZE_FOOD);
				} catch (JSONException e) {
					food = "";
				}
			}
			if (TextUtils.isEmpty(food)){
				mCustomize.setVisibility(View.GONE);
			}else{
				mCustomize.setVisibility(View.VISIBLE);
				mCustomize.setText(mContext.getString(R.string.customize_food)+food);
			}
			//mCustomize.setText("负能量：面包，蛋糕");
		}else {
			mDayGroup.setVisibility(View.GONE);
		}
	}
}
