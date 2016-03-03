package com.xiaosajie.chenzao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.chenzao.utils.Constants;
import com.chenzao.view.LoadingBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.RelativeLayout;

public class WebBrowser extends Activity
{
	private LoadingBar mLoadingBar;
	private String mUrl;
	private RelativeLayout mWebViewContainer;
	private RelativeLayout mLoadingBarView;
	private WebView mWevView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_browser);
    
    initUrlInfoFromIntent();
    initView();
    initWebView();

    mWevView.loadUrl(mUrl);
  }
  
  private void initView(){
	  mLoadingBar = (LoadingBar)findViewById(R.id.loading_bar);
	  mWebViewContainer = (RelativeLayout)findViewById(R.id.webview_container);
	  mLoadingBarView = (RelativeLayout)findViewById(R.id.rl_loading_bar);
  }
  
  private void initWebView(){
	  if(mWevView != null){
		  mWebViewContainer.removeView(mWevView);
		  mWevView.destroy();
		  mWevView = null;
	  }
	  
	  mWevView = new WebView(this);
	  mWebViewContainer.addView(mWevView);
	  WebChromeClient chromeclient = new WebChromeClient(){

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// TODO Auto-generated method stub
			mLoadingBar.drawProgress(newProgress);
		}
		  
	  };
	  mWevView.setWebChromeClient(chromeclient);
	  WebViewClient client = new WebViewClient(){

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			mLoadingBarView.setVisibility(View.INVISIBLE);
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			mLoadingBarView.setVisibility(View.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			return super.shouldOverrideUrlLoading(view, url);
		}
		  
	  };
	  mWevView.setWebViewClient(client);
	  
	mWevView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	mWevView.requestFocusFromTouch();
	mWevView.getSettings().setJavaScriptEnabled(true);
	invokeVoidMethod(mWevView.getSettings(), "setPluginsEnabled", true);
	mWevView.getSettings().setSupportZoom(true);
	mWevView.getSettings().setBuiltInZoomControls(true);
	mWevView.getSettings().setAllowFileAccess(true);
	mWevView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
	mWevView.getSettings().setUseWideViewPort(true);
	invokeVoidMethod(mWevView.getSettings(), "setLoadWithOverviewMode", true);
	invokeVoidMethod(mWevView.getSettings(), "setDisplayZoomControls", false);
	  }
  
  @Override
public boolean onKeyUp(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
	if (mWevView.canGoBack()) {
		mWevView.goBack();
    } else {
		finish();
	}
	return true;
}

private void initUrlInfoFromIntent(){
	  Intent intent = getIntent();
	  if(intent == null){
		  return;
	  }
	  intent.getStringExtra(Constants.WEB_BROWSER_URL);
	  String url = intent.getStringExtra(Constants.WEB_BROWSER_URL);
	  if(!TextUtils.isEmpty(url)){
		  mUrl = url;
	  }
  }

private void invokeVoidMethod(Object owner, String methodName, boolean property) {
	try {
		Method method = owner.getClass().getMethod(methodName, boolean.class);
		method.invoke(owner, property);
	} catch (SecurityException e) {
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		e.printStackTrace();
	}  
}

}

