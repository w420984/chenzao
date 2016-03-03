package com.chenzao.view;

import com.xiaosajie.chenzao.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseLayout extends RelativeLayout {
    // Title type
    public final static int TYPE_BUTTON_GROUP = 0;
    public final static int TYPE_NORMAL = 1;

	public TextView leftButton;
    public TextView rightButton;
    public TextView tvInfo;

    public RelativeLayout progressButton;
    public View titlebar;

    public void setButtonTypeAndInfo(String left, String middle, String right) {
        // mode = type;
        Resources r = this.getResources();
        if (TextUtils.isEmpty(left)) {
            leftButton.setVisibility(View.GONE);
        } else {
            if (left.equalsIgnoreCase(r.getString(R.string.back))) {
                leftButton.setBackgroundResource(R.drawable.title_back);
            } else {
                leftButton.setBackgroundResource(R.drawable.title_button);
                leftButton.setText(left);
            }
        }
        if (!(TextUtils.isEmpty(middle))) {
            this.setTitle(middle);
        }
        if (TextUtils.isEmpty(right)) {
            rightButton.setVisibility(View.GONE);
        } else {
            if (right.equalsIgnoreCase(r.getString(R.string.save))) {
            	rightButton.setBackgroundResource(R.drawable.header_icon_save);
            } else {
				rightButton.setBackgroundResource(R.drawable.common_tab_bg);
				rightButton.setText(right);
            }
        }
    }

    public void setTitle(String title) {
        tvInfo.setText(title, TextView.BufferType.NORMAL);
    }

    public BaseLayout(Context context, int resId) {
        super(context);

        LayoutInflater i = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        titlebar = i.inflate(R.layout.titlebar, null);
        LayoutParams titlelp = null;

        /**
         * hdpi title bar 高度为 65px
         * mdpi title bar 高度为 45px
         * ldpi title bar 高度为 35px
         */
        int titleHeight = getResources().getDimensionPixelSize(R.dimen.baselayout_title_height);
        titlelp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, titleHeight);
        titlelp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        // titlebar.setLayoutParams(titlelp);
        this.addView(titlebar, titlelp);

        View contentView = i.inflate(resId, null);
        LayoutParams contentlp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        contentlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
        this.addView(contentView, contentlp);
        leftButton = (TextView) findViewById(R.id.titleBack);
        rightButton = (TextView) findViewById(R.id.titleSave);
        tvInfo = (TextView) findViewById(R.id.titleText);
        progressButton = (RelativeLayout) findViewById(R.id.rlProgressBar);
        tvInfo.setBackgroundDrawable(null);
    }

}
