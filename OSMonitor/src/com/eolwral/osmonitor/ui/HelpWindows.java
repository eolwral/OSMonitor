package com.eolwral.osmonitor.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.eolwral.osmonitor.R;


public class HelpWindows extends Activity  {

	private WebView helpView = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		// create view 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_help_windows);
        
        String url = getIntent().getStringExtra("URL");
        
        helpView =  new WebView(getApplicationContext());
        WebSettings settings = helpView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        helpView.setInitialScale(100);
        helpView.loadUrl(url);
        
        LinearLayout helpLayout = (LinearLayout) findViewById(R.id.id_help_view);
        helpLayout.addView(helpView);
	}

	@Override
	public void onPause() {
		super.onPause();
		finish();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    helpView.removeAllViews();
	    helpView.destroy();
	    
	    System.gc();
	}
	
}
