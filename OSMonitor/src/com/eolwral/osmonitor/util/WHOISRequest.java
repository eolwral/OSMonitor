package com.eolwral.osmonitor.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public  class  WHOISRequest extends JsonObjectRequest {

	public WHOISRequest( int method, String url, JSONObject jsonRequest,
															Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
		this.setShouldCache(false);
	}

	/**
	 *  I don't like to be monitored, replace my user-agent as  most common one
	 *  http://techblog.willshouse.com/2012/01/03/most-common-user-agents/
	 */
	@Override
    public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String>  headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");  
		return headers;  
	}
	
}