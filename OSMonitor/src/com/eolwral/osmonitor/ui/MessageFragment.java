package com.eolwral.osmonitor.ui;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
  
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Resources;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;


import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.DmesgInfo.dmesgInfo;
import com.eolwral.osmonitor.core.LogcatInfo.logcatInfo;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.Settings;

public class MessageFragment extends SherlockListFragment 
                                implements ipcClientListener {

	// ipc client
	private static IpcService ipc = IpcService.getInstance();
	private static boolean ipcStop = false;
	private ipcAction selectedType = ipcAction.LOGCAT_MAIN;
	
	// data  
	private ArrayList<logcatInfo> viewLogcatData = new ArrayList<logcatInfo>();
	private ArrayList<dmesgInfo> viewDmesgData = new ArrayList<dmesgInfo>();
	private ipcAction logType = ipcAction.LOGCAT_MAIN; 
	private Settings settings = null;
	
	// filter
	private ArrayList<logcatInfo> sourceLogcatData = new ArrayList<logcatInfo>();
	private boolean [] filterLogcatArray = new boolean[logcatInfo.logPriority.SILENT_VALUE+1];  

	private ArrayList<dmesgInfo> sourceDmesgData = new ArrayList<dmesgInfo>();
	private boolean [] filterDmesgArray = new boolean[dmesgInfo.dmesgLevel.DEBUG_VALUE+1];

	private MessageListAdapter messageList = null;
	private String filterString = "";
	
	// stop or start
	private boolean stopUpdate = false;
	private ImageButton stopButton = null;
	
	private TextView messageCount = null;
	  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);   
		
		// persist 
		setRetainInstance(true);
		
		// enable fragment option menu 
		setHasOptionsMenu(true);
		
		for(int index = 0; index < filterLogcatArray.length; index++)
			filterLogcatArray[index] = true;

		for(int index = 0; index < filterDmesgArray.length; index++)
			filterDmesgArray[index] = true;
		
		// set list
		messageList = new MessageListAdapter(getSherlockActivity().getApplicationContext());
		setListAdapter(messageList);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_message_fragment, container, false);
		
		messageCount = ((TextView) v.findViewById(R.id.id_message_count));
		  
		return v;  
	}
	
	@Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.ui_message_menu, menu);

		MenuItem helpMenu = menu.findItem(R.id.ui_menu_help);
		helpMenu.setOnMenuItemClickListener( new HelpMenuClickListener());

		// export menu
		MenuItem exportItem = menu.findItem(R.id.ui_message_export);
		exportItem.setOnMenuItemClickListener(new ExportMenuClickListener());

		MenuItem exitMenu = menu.findItem(R.id.ui_menu_exit);
		exitMenu.setOnMenuItemClickListener(new ExitMenuClickListener());

		// sort extend menu
		MenuItem expendMenu = menu.findItem(R.id.ui_message_sort);
		Spinner expendItem = (Spinner) expendMenu.getActionView();

		switch(selectedType) {
		case LOGCAT_MAIN:
			expendItem.setSelection(0);
			break;
		case LOGCAT_SYSTEM:
			expendItem.setSelection(1);
			break;
		case LOGCAT_EVENT:
			expendItem.setSelection(2);
			break;
		case DMESG:
			expendItem.setSelection(3);
			break;
		default:
			break;
		}
		
		// source menu
		expendItem.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch(arg2) {
				case 0:
					selectedType = ipcAction.LOGCAT_MAIN;
					break;
				case 1:
					selectedType = ipcAction.LOGCAT_SYSTEM;
					break;
				case 2:
					selectedType = ipcAction.LOGCAT_EVENT;
					break;
				case 3:
					selectedType = ipcAction.DMESG;
					break;
				}

				// force refresh
				forceRefresh();
				
				// restart if it has been stopped 
				if(!stopUpdate)
					stopUpdate = false;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});
		
		// sort extend menu
		MenuItem searchMenu = menu.findItem(R.id.ui_message_search);
		View searchItem = (View) searchMenu.getActionView();

		// instant search
		TextView searchView = (TextView) searchItem.findViewById(R.id.id_action_search_text);
		searchView.setText(filterString);
		searchView.addTextChangedListener(new TextWatcher() {
 
			@Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	messageList.getFilter().filter(cs);
            }
 
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
 
            @Override
            public void afterTextChanged(Editable arg0) { }
		});

		// refresh button
		stopButton = (ImageButton) searchItem.findViewById(R.id.id_action_stop);

		if(stopUpdate) 
			stopButton.setImageResource(R.drawable.ic_action_start);
		else
			stopButton.setImageResource(R.drawable.ic_action_stop);

		stopButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopUpdate = !stopUpdate;
				
				if(stopUpdate) 
					stopButton.setImageResource(R.drawable.ic_action_start);
				else
					stopButton.setImageResource(R.drawable.ic_action_stop);
			}
		});

		// filter spinner
		ImageButton filterButton = (ImageButton) searchItem.findViewById(R.id.id_action_filter);
		filterButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMultiChoiceItems();
			}
		});
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	private void forceRefresh() {
		if(logType != selectedType) {
			ipc.removeRequest(this);
			ipcAction newCommand[] = new ipcAction[1];
			newCommand [0] =  selectedType; 
			ipc.addRequest(newCommand, 0, this);
		}
	}
	
    private void showMultiChoiceItems() {
        Builder builder = new AlertDialog.Builder(getSherlockActivity());
        
        if(isLogcat(logType)) {
        	builder.setMultiChoiceItems(R.array.ui_message_logcat_level,
        			filterLogcatArray,
        			new OnMultiChoiceClickListener() {

        		@Override
        		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        			filterLogcatArray[which] = isChecked;
        		}
        	});
        }
        else {
        	builder.setMultiChoiceItems(R.array.ui_message_dmesg_level,
        			filterDmesgArray,
        			new OnMultiChoiceClickListener() {

        		@Override
        		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        			filterDmesgArray[which] = isChecked;
        		}
        	});
        	
        }
                        
        builder.setPositiveButton(R.string.ui_text_okay, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		messageList.getFilter().doFilter();
        	}
        });
        
        builder.show();
    }
	
    private void exportLog(String fileName)
    {
    	if(fileName.trim().equals(""))
    		return;
    	
    	if(!fileName.contains(".csv"))
    		fileName += ".csv";
    	
    	try {
        	File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
    		
        	if (logFile.exists())
        	{
        		new AlertDialog.Builder(getActivity())
   		   		    .setTitle(R.string.ui_menu_logexport)
    		   		.setMessage(R.string.ui_message_fileexist)
    		   		.setPositiveButton(R.string.ui_text_okay,
    		   				new DialogInterface.OnClickListener() {
    		   			public void onClick(DialogInterface dialog, int whichButton) { } })
    		   		.create()
    		   		.show();
        		return;
        	}

        	logFile.createNewFile();
        	
        	int LogCount = 0;
        	if(isLogcat(logType))
        		LogCount = viewLogcatData.size();
        	else
        		LogCount = viewDmesgData.size();
        	
        	FileWriter logWriter = new FileWriter(logFile);
        	
        	final Calendar calendar = Calendar.getInstance();
			final DateFormat convertTool = DateFormat.getDateTimeInstance();
        	
        	for(int index = 0; index < LogCount ; index++)
        	{
        		StringBuilder logLine = new StringBuilder();
        		
        		if(isLogcat(logType))
        		{
        			calendar.setTimeInMillis(viewLogcatData.get(index).getSeconds()*1000);
        			
        			logLine.append(convertTool.format(calendar.getTime()) + ",");
        			
    				switch(viewLogcatData.get(index).getPriority().getNumber())
    				{
    				case logcatInfo.logPriority.SILENT_VALUE:
    					logLine.append("SILENT,");
    					break;
    				case logcatInfo.logPriority.UNKNOWN_VALUE:
    					logLine.append("UNKNOWN,");
    					break;
    				case logcatInfo.logPriority.DEFAULT_VALUE:
    					logLine.append("DEFAULT,");
    					break;
    				case logcatInfo.logPriority.VERBOSE_VALUE:
    					logLine.append("VERBOSE,");
    					break;
    				case logcatInfo.logPriority.WARN_VALUE:
    					logLine.append("WARNING,");
    					break;
    				case logcatInfo.logPriority.INFO_VALUE:
    					logLine.append("INFORMATION,");
    					break;
    				case logcatInfo.logPriority.FATAL_VALUE:
    					logLine.append("FATAL,");
    					break;
    				case logcatInfo.logPriority.ERROR_VALUE:
    					logLine.append("ERROR,");
    					break;
    				case logcatInfo.logPriority.DEBUG_VALUE:
    					logLine.append("DEBUG,");
    					break;
    				}        	
        			logLine.append(viewLogcatData.get(index).getTag() + ",");
        			logLine.append(viewLogcatData.get(index).getMessage() + "\n");       					 
        			
        		}
        		else
        		{
    				if(viewDmesgData.get(index).getSeconds() != 0) 
    				{
    					calendar.setTimeInMillis(viewDmesgData.get(index).getSeconds()*1000);
    					logLine.append(convertTool.format(calendar.getTime()) + ",");
    				}
    				
    				switch(viewDmesgData.get(index).getLevel().getNumber())
    				{
    				case dmesgInfo.dmesgLevel.DEBUG_VALUE:
    					logLine.append("DEBUG,");
    					break;
    				case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
    					logLine.append("INFORMATION,");
    					break;
    				case dmesgInfo.dmesgLevel.NOTICE_VALUE:
    					logLine.append("NOTICE,");
    					break;
    				case dmesgInfo.dmesgLevel.WARNING_VALUE:
    					logLine.append("WARNING,");
    					break;
    				case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
    					logLine.append("EMERGENCY,");
    					break;
    				case dmesgInfo.dmesgLevel.ERROR_VALUE:
    					logLine.append("ERROR,");
    					break;
    				case dmesgInfo.dmesgLevel.ALERT_VALUE:
    					logLine.append("ALERT,");
    					break;
    				case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
    					logLine.append("CRITICAL,");
    					break;
    				}

    				logLine.append(viewDmesgData.get(index).getMessage().toString() + "\n");
        		}
        		logWriter.write(logLine.toString());
        	}

        	logWriter.close();

    	} catch (Exception e) {
	    	new AlertDialog.Builder(getActivity())
		  		.setTitle(R.string.ui_menu_logexport)
		  		.setMessage(e.getMessage())
		  		.setPositiveButton(R.string.ui_text_okay,
		  				new DialogInterface.OnClickListener() {
		  			public void onClick(DialogInterface dialog, int whichButton) { } })
		  		.create()
		  		.show();

	    	return;
	    }
	    	
    	new AlertDialog.Builder(getActivity())
	  		.setTitle(R.string.ui_menu_logexport)
	  		.setMessage(R.string.ui_message_exportdone)
	  		.setPositiveButton(R.string.ui_text_okay,
	  				new DialogInterface.OnClickListener() {
	  			public void onClick(DialogInterface dialog, int whichButton) { } })
	  		.create()
	  		.show();

	  	return;
	}
    
	private class ExitMenuClickListener implements OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			getActivity().stopService(new Intent(getActivity(), OSMonitorService.class));
			android.os.Process.killProcess(android.os.Process.myPid());
			return false;
		}
		
	}
	
    private class ExportMenuClickListener implements OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			final Resources exportRes = getActivity().getResources();
			final Calendar calendar = Calendar.getInstance();
			final DateFormat convertTool = DateFormat.getDateTimeInstance();
			
			Builder exportDialog = new AlertDialog.Builder(getActivity());
			View exportView = LayoutInflater.from(getActivity()).inflate(R.layout.ui_message_export, null);
			TextView exportFile = (TextView) exportView.findViewById(R.id.id_export_filename);
			exportFile.setText("Log-"+convertTool.format(calendar.getTime()));
			exportDialog.setView(exportView);
			
			exportDialog.setTitle(exportRes.getText(R.string.ui_menu_logexport));
			exportDialog.setNegativeButton(exportRes.getText(R.string.ui_text_cancel), null);
			
			exportDialog.setPositiveButton(exportRes.getText(R.string.ui_text_okay),
		    new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	String FileName = ((EditText)((AlertDialog)dialog).findViewById(R.id.id_export_filename)).getText().toString();
	            	exportLog(FileName);
	            }
	        });
			
			exportDialog.create().show();
			return false;
		}
	}
    
	private class HelpMenuClickListener implements OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			ShowHelp();
			return false;
		}
	}

	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

		ipc.removeRequest(this);
		ipcStop = !isVisibleToUser;

		if(isVisibleToUser == true) {
			settings = new Settings(getActivity());
			ipcAction newCommand[] = { logType };
			ipc.addRequest(newCommand, 0, this);
		}
		
	}
	
	@Override
	public void onRecvData(ipcMessage result) {
		
		// check 
		if(ipcStop == true)
		  return;
		
		// update
		if (stopUpdate == true || result == null) {
			ipcAction newCommand[] = new ipcAction[1];
			newCommand [0] =  selectedType; 
			ipc.addRequest(newCommand, settings.getInterval(), this);
			return;
		}
		
		// clean up
		sourceLogcatData.clear();
		sourceDmesgData.clear();

		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);

				if(isLogcat(rawData.getAction())) {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						logcatInfo lgInfo = logcatInfo.parseFrom(rawData.getPayload(count));
						sourceLogcatData.add(lgInfo);
					}
				}
				else {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						dmesgInfo dgInfo = dmesgInfo.parseFrom(rawData.getPayload(count));
						sourceDmesgData.add(dgInfo);
					}
				} 

				logType = rawData.getAction();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		} 

		// processing filter action
		messageList.getFilter().doFilter();

		// send command again
		ipcAction newCommand[] = new ipcAction[1];
		newCommand [0] =  selectedType ; 
		if(selectedType != logType) 
			ipc.addRequest(newCommand, 0, this);
		else
			ipc.addRequest(newCommand, settings.getInterval(), this);
	}
	
	private boolean isLogcat(ipcAction logType) {
		if (logType == ipcAction.LOGCAT_MAIN ||
			logType == ipcAction.LOGCAT_EVENT ||
			logType == ipcAction.LOGCAT_SYSTEM ||
			logType == ipcAction.LOGCAT_RADIO) 
			return true;
		return false;
	}
	
	/**
	 * implement viewholder class for connection list
	 */
	private class ViewHolder {
		// main information
		TextView time;
		TextView tag;
		TextView level;
		TextView msg;
	}	
	
	private class MessageListAdapter extends BaseAdapter {

		private LayoutInflater itemInflater = null;
		private ViewHolder holder = null;
		private MessageFilter filter = null;
		  
		public MessageListAdapter(Context mContext) {
			itemInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public MessageFilter getFilter() {
		   if (filter == null)
		    filter  = new MessageFilter();
		   return filter;
		}
		 
		@Override
		public int getCount() {
			if(isLogcat(logType))
				return viewLogcatData.size();
			return viewDmesgData.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		} 

		public View getView(int position, View convertView, ViewGroup parent) {

			View sv = null;

			// prepare view
			if (convertView == null) {

				sv = (View) itemInflater.inflate(R.layout.ui_message_item, parent, false);

				holder = new ViewHolder();
				holder.time = ((TextView) sv.findViewById(R.id.id_message_time));
				holder.level = ((TextView) sv.findViewById(R.id.id_message_level));
				holder.tag = ((TextView) sv.findViewById(R.id.id_message_tag));
				holder.msg = ((TextView) sv.findViewById(R.id.id_message_text));

				sv.setTag(holder);
			} else {
				sv = (View) convertView;
				holder = (ViewHolder) sv.getTag();
			}

			// draw current color for each item
			if (position % 2 == 0)
				sv.setBackgroundColor(getResources().getColor(R.color.dkgrey_osmonitor));
			else
				sv.setBackgroundColor(getResources().getColor(R.color.black_osmonitor));

			// get data 
			if(isLogcat(logType) && viewLogcatData.size() > position) {
				logcatInfo item = viewLogcatData.get(position);
				
				final Calendar calendar = Calendar.getInstance();
				final DateFormat convertTool = DateFormat.getDateTimeInstance();
				calendar.setTimeInMillis(item.getSeconds()*1000);
				holder.time.setText(convertTool.format(calendar.getTime()));
				
				holder.tag.setText(item.getTag());
				
				holder.msg.setText(item.getMessage().toString());
				
				holder.level.setTextColor(Color.BLACK);
 
				switch(item.getPriority().getNumber())
				{
				case logcatInfo.logPriority.SILENT_VALUE:
				case logcatInfo.logPriority.UNKNOWN_VALUE:
				case logcatInfo.logPriority.DEFAULT_VALUE:
					holder.level.setBackgroundColor(Color.LTGRAY);
					holder.level.setText("S");
					break;
					
				case logcatInfo.logPriority.VERBOSE_VALUE:
					holder.level.setBackgroundColor(Color.WHITE);
					holder.level.setText("V");
					break;
					
				case logcatInfo.logPriority.WARN_VALUE:
					holder.level.setBackgroundColor(Color.YELLOW);
					holder.level.setText("W");
					break;
					
				case logcatInfo.logPriority.INFO_VALUE:
					holder.level.setBackgroundColor(Color.GREEN);
					holder.level.setText("I");
					break;
					
				case logcatInfo.logPriority.FATAL_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("F");
					break;
					
				case logcatInfo.logPriority.ERROR_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("E");
					break;
					
				case logcatInfo.logPriority.DEBUG_VALUE:
					holder.level.setBackgroundColor(Color.BLUE);
					holder.level.setText("D");
					break;
				}
			}
			else if (viewDmesgData.size() > position)
			{
				dmesgInfo item = viewDmesgData.get(position);

				if(item.getSeconds() != 0) 
				{
					final Calendar calendar = Calendar.getInstance();
					final DateFormat convertTool = DateFormat.getDateTimeInstance();
					calendar.setTimeInMillis(item.getSeconds()*1000);
					holder.time.setText(convertTool.format(calendar.getTime()));
				}
				
				holder.tag.setText("");
				holder.msg.setText(item.getMessage().toString());

				holder.level.setTextColor(Color.BLACK);
				switch(item.getLevel().getNumber())
				{
				case dmesgInfo.dmesgLevel.DEBUG_VALUE:
					holder.level.setBackgroundColor(Color.BLUE);
					holder.level.setText("D");
					break;
				case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
					holder.level.setBackgroundColor(Color.GREEN);
					holder.level.setText("I");
					break;
				case dmesgInfo.dmesgLevel.NOTICE_VALUE:
					holder.level.setBackgroundColor(Color.MAGENTA);
					holder.level.setText("N");
					break;
				case dmesgInfo.dmesgLevel.WARNING_VALUE:
					holder.level.setBackgroundColor(Color.YELLOW);
					holder.level.setText("W");
					break;
				case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("E");
					break;
				case dmesgInfo.dmesgLevel.ERROR_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("E");
					break;
				case dmesgInfo.dmesgLevel.ALERT_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("A");
					break;
				case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
					holder.level.setBackgroundColor(Color.RED);
					holder.level.setText("C");
					break;
				}
			}
			
			// avoid to trigger errors when refreshing
			sv.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
				
			});
			
			return sv;
		}
		
		public void refresh() {
			
			if(isLogcat(logType)) 
				messageCount.setText(String.format("%,d", viewLogcatData.size()));
			else
				messageCount.setText(String.format("%,d", viewDmesgData.size()));
			
			notifyDataSetChanged();
		}
		
		private class MessageFilter extends Filter {
			
			public void doFilter() {
				filter(filterString);
			}
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				FilterResults result = new FilterResults();

				// non-filter

				if(constraint != null)
					filterString = constraint.toString();
				else 
					filterString = "";

				// filter
				if (isLogcat(logType)) {
					ArrayList<logcatInfo> filteredItems = new ArrayList<logcatInfo>();
					for (int index = 0; index < sourceLogcatData.size(); index++) {
						logcatInfo item = sourceLogcatData.get(index);
						
						if (filterLogcatArray[convertLogcatType(item.getPriority().getNumber())] == false )
							continue;
						
						if (filterString.length() != 0)
							if (!item.getMessage().toLowerCase().contains(filterString) &&
								!item.getTag().toLowerCase().contains(filterString)) 
							continue;

						filteredItems.add(item);
					}
					result.count = filteredItems.size();
					result.values = filteredItems;
				} else {
					ArrayList<dmesgInfo> filteredItems = new ArrayList<dmesgInfo>();
					for (int index = 0; index < sourceDmesgData.size(); index++) {
						dmesgInfo item = sourceDmesgData.get(index);
						
						if (filterDmesgArray[convertDmesgType(item.getLevel().getNumber())] == false)
							continue;

						if (filterString.length() != 0)
						  if (!item.getMessage().toLowerCase().contains(filterString))
							continue;
						
						filteredItems.add(item);
					}
					result.count = filteredItems.size();
					result.values = filteredItems;
				}

				return result;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				
				if(results.values == null)
				{
					viewDmesgData = sourceDmesgData;
					viewLogcatData = sourceLogcatData;
				}
				else {
					
					if (isLogcat(logType))
						viewLogcatData = (ArrayList<logcatInfo>) results.values;
					else
						viewDmesgData = (ArrayList<dmesgInfo>) results.values;
				}
				
				refresh();
			}

		}
	}
	
	
	private int convertLogcatType(int type) 
	{
		int result = 0;
		
		switch(type) 
		{
		case logcatInfo.logPriority.DEBUG_VALUE:
			result = 0 ;
			break;
		case logcatInfo.logPriority.VERBOSE_VALUE:
			result = 1 ;
			break;
		case logcatInfo.logPriority.INFO_VALUE:
			result = 2 ;
			break;
		case logcatInfo.logPriority.WARN_VALUE:
			result = 3 ;
			break;
		case logcatInfo.logPriority.ERROR_VALUE:
			result = 4 ;
			break;
		case logcatInfo.logPriority.FATAL_VALUE:
			result = 5 ;
			break;
		}
		return result;
	}
	
	private int convertDmesgType(int type) 
	{
		int result = 0;
		
		switch(type) 
		{
		case dmesgInfo.dmesgLevel.DEBUG_VALUE:
			result = 0 ;
			break;
		case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
			result = 1 ;
			break;
		case dmesgInfo.dmesgLevel.NOTICE_VALUE:
			result = 2 ;
			break;
		case dmesgInfo.dmesgLevel.WARNING_VALUE:
			result = 3 ;
			break;
		case dmesgInfo.dmesgLevel.ALERT_VALUE:
			result = 4 ;
			break;
		case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
			result = 5 ;
			break;
		case dmesgInfo.dmesgLevel.ERROR_VALUE:
			result = 6 ;
			break;
		case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
			result = 7 ;
			break;
		}
		return result;
	}
	
    @SuppressLint("SetJavaScriptEnabled")
	private void ShowHelp()
    {
    	CommonUtil.showHelp(getActivity(), "file:///android_asset/help/help-message.html");
    }

}
