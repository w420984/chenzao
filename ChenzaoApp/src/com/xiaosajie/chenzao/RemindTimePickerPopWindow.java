package com.xiaosajie.chenzao;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.chenzao.wheelwidget.NumericWheelAdapter;
import com.chenzao.wheelwidget.OnWheelChangedListener;
import com.chenzao.wheelwidget.WheelView;

public class RemindTimePickerPopWindow extends PopupWindow{

	private Activity mContext;
	private View mMenuView;
	private ViewFlipper viewfipper;
	private DateNumericAdapter minuteAdapter, hourAdapter;
	private WheelView hour, minute;
	private String[] timeType;
	private Handler mHandler;
	private int mMessageId;
	private final int MIN_HOUR = 0;
	private final int MAX_HOUR = 23;
	private final int HOUR_STEP = 1;
	private final int MINUTE_STEP = 15;
	

	public RemindTimePickerPopWindow(Activity context, String time) {
		super(context);
		mContext = context;
		int curHourIndex = 0;
		int curMinIndex = 0;
		
		if(!TextUtils.isEmpty(time)){
			curHourIndex = Integer.parseInt(time.substring(0, 2)) - MIN_HOUR;
			curMinIndex = Integer.parseInt(time.substring(2, 4)) / MINUTE_STEP;
		}

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.remindtimewheel, null);
		viewfipper = new ViewFlipper(context);
		viewfipper.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		hour = (WheelView) mMenuView.findViewById(R.id.hour);
		minute = (WheelView) mMenuView.findViewById(R.id.minute);
		
		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if(mHandler != null){
					Message msg = new Message();
					msg.what = mMessageId;
					Time time = new Time();
					time.hour = MIN_HOUR + hour.getCurrentItem() * HOUR_STEP;
					time.minute = minute.getCurrentItem() * MINUTE_STEP;
					msg.obj = time;
					mHandler.sendMessage(msg);
				}
			}
		};

		timeType = mContext.getResources().getStringArray(R.array.time); 
		minuteAdapter = new DateNumericAdapter(context, 0, 59, 0, MINUTE_STEP);
		minuteAdapter.setTextType(timeType[1]);
		minute.setViewAdapter(minuteAdapter);
		minute.setCurrentItem(curMinIndex);
		minute.addChangingListener(listener);
		// year

		hourAdapter = new DateNumericAdapter(context, MIN_HOUR , MAX_HOUR,
				1, HOUR_STEP);
		hourAdapter.setTextType(timeType[0]);
		hour.setViewAdapter(hourAdapter);
		hour.setCurrentItem(curHourIndex);
		hour.addChangingListener(listener);

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
				int current, int step) {
//			super(context, minValue, maxValue);
			super(context, minValue, maxValue, null, step);
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
