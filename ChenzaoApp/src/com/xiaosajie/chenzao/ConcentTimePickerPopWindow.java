package com.xiaosajie.chenzao;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.chenzao.wheelwidget.NumericWheelAdapter;
import com.chenzao.wheelwidget.OnWheelChangedListener;
import com.chenzao.wheelwidget.WheelView;

public class ConcentTimePickerPopWindow extends PopupWindow{

	private Activity mContext;
	private View mMenuView;
	private ViewFlipper viewfipper;
	private DateNumericAdapter hourAdapter;
	private WheelView hour;
	private String[] timeType;
	private Handler mHandler;
	private int mMessageId;
	private final int MIN_HOUR = 1;
	private final int MAX_HOUR = 3;
	private final float HOUR_STEP = 0.5f;
	

	public ConcentTimePickerPopWindow(Activity context) {
		super(context);
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.concenttimewheel, null);
		viewfipper = new ViewFlipper(context);
		viewfipper.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		hour = (WheelView) mMenuView.findViewById(R.id.hour);
		
		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if(mHandler != null){
					Message msg = new Message();
					msg.what = mMessageId;
					msg.arg1 = hour.getCurrentItem();
					mHandler.sendMessage(msg);
				}
			}
		};

		timeType = mContext.getResources().getStringArray(R.array.concenttime); 

		hourAdapter = new DateNumericAdapter(context, MIN_HOUR * 2 , MAX_HOUR * 2, 1, 1);
		hourAdapter.setTextType(timeType[0]);
		hour.setViewAdapter(hourAdapter);
		hour.setCurrentItem(18);
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
//			return super.getItemText(index);
			float i = ((float)MAX_HOUR - (float)MIN_HOUR)/HOUR_STEP + 1;
			int total = (int)i;
			if (index >= 0 && index < total) {
				float value = MIN_HOUR + index * HOUR_STEP;
				if((value * 10)%2 > 0){
					return Float.toString(value);
				}else{
					return Integer.toString((int)value);
				}
			}
			return null;
		}

	}

	public void setHandler(Handler handler, int msgid){
		mHandler = handler;
		mMessageId = msgid;
	}

}
