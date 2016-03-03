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

public class RepeatTypePickerPopWindow extends PopupWindow{

	private Activity mContext;
	private View mMenuView;
	private ViewFlipper viewfipper;
	private DateNumericAdapter repeatAdapter;
	private WheelView repeat;
	private String[] repeatType;
	private Handler mHandler;
	private int mMessageId;
	private final int MIN_COUNT = 1;
	private final int MAX_COUNT = 3;
	private final int COUNT_STEP = 1;
	

	public RepeatTypePickerPopWindow(Activity context, int index) {
		super(context);
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.concenttimewheel, null);
		viewfipper = new ViewFlipper(context);
		viewfipper.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		repeat = (WheelView) mMenuView.findViewById(R.id.hour);

		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if(mHandler != null){
					Message msg = new Message();
					msg.what = mMessageId;
					msg.arg1 = repeat.getCurrentItem();
					mHandler.sendMessage(msg);
				}
			}
		};

		repeatType = mContext.getResources().getStringArray(R.array.repeat); 

		repeatAdapter = new DateNumericAdapter(context, MIN_COUNT , MAX_COUNT, 1, COUNT_STEP);
		repeat.setViewAdapter(repeatAdapter);
		repeat.setCurrentItem(index);
		repeat.addChangingListener(listener);

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
			int total = (MAX_COUNT - MIN_COUNT) / COUNT_STEP + 1;
			if (index >= 0 && index < total) {
//				int value = MIN_COUNT + index * COUNT_STEP;
				return repeatType[index];
			}
			return null;
		}

	}

	public void setHandler(Handler handler, int msgid){
		mHandler = handler;
		mMessageId = msgid;
	}

}
