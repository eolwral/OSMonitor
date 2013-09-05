package com.eolwral.osmonitor.preference;

import com.eolwral.osmonitor.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

/*
 * Copyright (C) 2011-2012 George Yunaev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
public class ColorPickerPreference extends DialogPreference implements DialogInterface.OnClickListener
{
    // Keeps the colors in the nice integer format
    private int[]   m_colors;
    private Resources m_resmgr;
 
    // Font adaptor responsible for redrawing the item TextView with the appropriate background color
    public class ColorAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return m_colors.length;
        }
 
        @Override
        public Object getItem(int position)
        {
            return m_colors[ position ];
        }
 
        @Override
        public long getItemId(int position)
        {
            // We use the position as ID
            return position;
        }
 
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            View view = convertView;
 
            // This function may be called in two cases: a new view needs to be created, 
            // or an existing view needs to be reused
            if ( view == null )
            {
                // Since we're using the system list for the layout, use the system inflater
                final LayoutInflater inflater = (LayoutInflater) 
                                  getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
 
                // And inflate the view android.R.layout.select_dialog_singlechoice
                // Why? See com.android.internal.app.AlertController method createListView()
                view = inflater.inflate( android.R.layout.select_dialog_singlechoice, parent, false);
            }
 
            if ( view != null )
            {
                // Find the text view from our interface
                CheckedTextView tv = (CheckedTextView) view.findViewById( android.R.id.text1 );
                
                // Color the text view background using our color
                tv.setBackgroundColor( m_colors[position] );
            }
 
            return view;
        }
    }
 
    public ColorPickerPreference( Context context, AttributeSet attrs )
    {
        super(context, attrs);

        m_colors = new int[] {Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY,
        											   Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
        											   Color.YELLOW, Color.CYAN, Color.MAGENTA};
        
        m_resmgr = context.getResources();
    }
 
    @Override
    protected void onPrepareDialogBuilder( Builder builder )
    {
        super.onPrepareDialogBuilder(builder);
 
        int checked_item = 0;
        int selectedValue = getSharedPreferences().getInt( getKey(), 0 );
 
        // Find out the checked item index
        for ( int idx = 0; idx < m_colors.length; idx++ )
        {
            if ( m_colors[idx] == selectedValue )
            {
                checked_item = idx;
                break;
            }
        }
 
        // Create out adapter
        // If you're building for API 11 and up, you can pass builder.getContext 
       // instead of current context
        ColorAdapter adapter = new ColorAdapter();
        builder.setSingleChoiceItems( adapter, checked_item, this );
 
        // The typical interaction for list-based dialogs is to have 
        // click-on-an-item dismiss the dialog
        builder.setPositiveButton(m_resmgr.getText(R.string.ui_text_default), new OnClickListener() {
        	
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Editor editor = getSharedPreferences().edit();
	            editor.putInt( getKey(), -1 );
	            editor.commit();
	 
	            dialog.dismiss();
			}
        });
    }
 
    public void onClick(DialogInterface dialog, int which)
    {
        if ( which >=0 && which < m_colors.length )
        {
            Editor editor = getSharedPreferences().edit();
            editor.putInt( getKey(), m_colors[ which ] );
            editor.commit();
 
            dialog.dismiss();
        }
    }
}