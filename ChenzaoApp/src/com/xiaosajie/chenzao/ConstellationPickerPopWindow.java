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

public class ConstellationPickerPopWindow extends PopupWindow{

	private Activity mContext;
	private View mMenuView;
	private ViewFlipper viewfipper;
	private DateNumericAdapter constellationAdapter;
	private WheelView mConstellation;
	private String[] constellationName;
	private Handler mHandler;
	private int mMessageId;
	private final int MIN_CONSTELLATION = 0;
	private final int MAX_CONSTELLATION = 11;
	private final int CONSTELLATION_STEP = 1;
	

	public ConstellationPickerPopWindow(Activity context, int constellation) {
		super(context);
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.concenttimewheel, null);
		viewfipper = new ViewFlipper(context);
		viewfipper.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		mConstellation = (WheelView) mMenuView.findViewById(R.id.hour);
		
		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if(mHandler != null){
					Message msg = new Message();
					msg.what = mMessageId;
					msg.obj = constellationName[mConstellation.getCurrentItem() * CONSTELLATION_STEP + MIN_CONSTELLATION];
					mHandler.sendMessage(msg);
				}
			}
		};

		constellationName = mContext.getResources().getStringArray(R.array.constellation); 
		constellationAdapter = new DateNumericAdapter(context, MIN_CONSTELLATION , MAX_CONSTELLATION, constellation, CONSTELLATION_STEP);
		mConstellation.setViewAdapter(constellationAdapter);
		mConstellation.addChangingListener(listener);
		mConstellation.setCurrentItem(constellation);

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
			return constellationName[index];
		}

	}

	public void setHandler(Handler handler, int msgid){
		mHandler = handler;
		mMessageId = msgid;
	}

}
