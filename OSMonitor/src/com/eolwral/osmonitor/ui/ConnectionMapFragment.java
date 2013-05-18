package com.eolwral.osmonitor.ui;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.actionbarsherlock.app.SherlockFragment;
import com.eolwral.osmonitor.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
 
public class ConnectionMapFragment extends SherlockFragment
{
    public static String LONGTIUDE = "Longtiude";
    public static String LATITUDE = "Latitude";
    public static String MESSAGE = "Message";
    
    private WebView mapView = null;
 
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.ui_connection_map, container, false);
 
    	String url = "file:///android_asset/osm.html";
    	String lat = Float.toString(getArguments().getFloat(LATITUDE));
    	String lon = Float.toString(getArguments().getFloat(LONGTIUDE));
    	String msg = getArguments().getString(MESSAGE);
    	
    	try {
			url = url + "?lat=" + lat + "&lon=" + lon + "&msg=" + URLEncoder.encode(msg, "utf-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {}
        
        mapView =  new WebView(getSherlockActivity().getApplicationContext());
        WebSettings settings = mapView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        mapView.setInitialScale(100);
        mapView.loadUrl(url);

        FrameLayout mapLayout =  (FrameLayout) view.findViewById(R.id.id_map_view);
        mapLayout.removeAllViews();
        mapLayout.addView(mapView);
       
        return view;
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	
    	mapView.removeAllViews();
    	mapView.destroy();
    	
	    System.gc();
    }
    
 
}