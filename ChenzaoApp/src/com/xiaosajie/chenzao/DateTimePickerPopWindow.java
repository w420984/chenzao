package com.xiaosajie.chenzao;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.chenzao.utils.Utils;
import com.chenzao.wheelwidget.NumericWheelAdapter;
import com.chenzao.wheelwidget.OnWheelChangedListener;
import com.chenzao.wheelwidget.WheelView;

public class DateTimePickerPopWindow extends PopupWindow{

	private Activity mContext;
	private View mMenuView;
	private ViewFlipper viewfipper;
	private String startTime;
	private DateNumericAdapter monthAdapter, dayAdapter, yearAdapter;
	private WheelView year, month, day;
	private int mCurYear, mCurMonth, mCurDay;
	private String[] dateType;
	private Handler mHandler;
	private int mMessageId;
	private final int MIN_YEAR = 2014;
	

	public DateTimePickerPopWindow(Activity context, String date) {
		super(context);
		mContext = context;
		if(!TextUtils.isEmpty(date)){
			this.startTime = date;
		}else{
			this.startTime = "20140101";
		}
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.datetimewheel, null);
		viewfipper = new ViewFlipper(context);
		viewfipper.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		year = (WheelView) mMenuView.findViewById(R.id.year);
		month = (WheelView) mMenuView.findViewById(R.id.month);
		day = (WheelView) mMenuView.findViewById(R.id.day);
		
		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				updateDays(year, month, day);
				if(mHandler != null){
					Message msg = new Message();
					msg.what = mMessageId;
					String time = "" + (MIN_YEAR + year.getCurrentItem()) + Utils.format(month.getCurrentItem() + 1) + Utils.format(day.getCurrentItem() + 1);
					msg.obj = time;
					mHandler.sendMessage(msg);
				}
			}
		};
		
		if (!TextUtils.isEmpty(startTime)){
			mCurYear = Integer.parseInt(startTime.substring(0, 4));
			mCurMonth = Integer.parseInt(startTime.substring(4, 6)) - 1;
			mCurDay = Integer.parseInt(startTime.substring(6, 8)) - 1;
		}
		dateType = mContext.getResources().getStringArray(R.array.date); 
		monthAdapter = new DateNumericAdapter(context, 1, 12, 0);
		monthAdapter.setTextType(dateType[1]);
		month.setViewAdapter(monthAdapter);
		month.setCurrentItem(mCurMonth);
		month.addChangingListener(listener);
		// year

		yearAdapter = new DateNumericAdapter(context, MIN_YEAR , MIN_YEAR + 100,
				1);
		yearAdapter.setTextType(dateType[0]);
		year.setViewAdapter(yearAdapter);
		year.setCurrentItem(mCurYear);
		year.addChangingListener(listener);
		// day

		updateDays(year, month, day);
		day.setCurrentItem(mCurDay);
		updateDays(year, month, day);

		day.addChangingListener(listener);

		viewfipper.addView(mMenuView);
		viewfipper.setFlipInterval(6000000);
		this.setContentView(viewfipper);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		this.setBackgroundDrawable(dw);
		this.update();

	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		viewfipper.startFlipping();
	}


	private void updateDays(WheelView year, WheelView month, WheelView day) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR,
				calendar.get(Calendar.YEAR) + year.getCurrentItem());
		calendar.set(Calendar.MONTH, month.getCurrentItem());

		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		dayAdapter = new DateNumericAdapter(mContext, 1, maxDays,
				calendar.get(Calendar.DAY_OF_MONTH) - 1);
		dayAdapter.setTextType(dateType[2]);
		day.setViewAdapter(dayAdapter);
		int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
		day.setCurrentItem(curDay - 1, true);
	}

	/**
	 * Adapter for numeric wheels. Highlights the current value.
	 */
	private class DateNumericAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateNumericAdapter(Context context, int minValue, int maxValue,
				int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;
			setTextSize(24);
		}

		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			view.setTypeface(Typeface.SANS_SERIF);
		}

		public CharSequence getItemText(int index) {
			currentItem = index;
			return super.getItemText(index);
		}

	}

	public void setHandler(Handler handler, int msgid){
		mHandler = handler;
		mMessageId = msgid;
	}

}
