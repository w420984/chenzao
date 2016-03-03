package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuItemView extends RelativeLayout {
	public ImageView mItemImage;
	public TextView mItemTitle;
	public TextView mItemNotice;
	
	public MenuItemView(Context context) {
		super(context);
		init();
	}

	public MenuItemView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public MenuItemView(Context context, int image, int title){
		super(context);
		init();
		mItemImage.setImageResource(image);
		mItemTitle.setText(title);
	}
	
	private void init(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.menu_item_layout, this);
    	mItemImage = (ImageView)findViewById(R.id.item_image);
    	mItemTitle = (TextView)findViewById(R.id.item_title);
    	mItemNotice = (TextView)findViewById(R.id.item_notice);
		
	}
	
	public void setItemImage(int image){
		mItemImage.setImageResource(image);		
	}
	
	public void setItemTitle(int title){
		mItemTitle.setText(title);
	}
	
	public void setNotice(String str){
		mItemNotice.setText(str);
		mItemNotice.setVisibility(View.VISIBLE);
	}
}
