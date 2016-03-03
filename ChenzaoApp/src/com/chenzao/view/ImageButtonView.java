package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageButtonView extends LinearLayout {
	private TextView mTextView;
	private ImageView mImageView;


    public ImageButtonView(Context context) {
		this(context, null);
	}

	public ImageButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		LayoutInflater.from(context).inflate(R.layout.image_text_button, this, true);
		mTextView = (TextView)findViewById(R.id.image_text_btn_text);
		mImageView = (ImageView)findViewById(R.id.image_text_btn_img);
	}

	public void setImageTextButtonImageResource(int resId){
		mImageView.setImageResource(resId);
	}
	
	public void setImageTextButtonImageResource(Drawable drawable){
		mImageView.setImageDrawable(drawable);
	}

	public void setImageTextButtonTextString(int resId){
		mTextView.setText(resId);
	}
}
