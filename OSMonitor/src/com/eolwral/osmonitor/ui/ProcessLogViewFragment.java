package com.eolwral.osmonitor.ui;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.LogcatInfo.logcatInfo;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.settings.Settings;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProcessLogViewFragment extends DialogFragment 
                                     implements ipcClientListener {
	
	// ipc client
	private static IpcService ipcService = IpcService.getInstance();

	// set pid
	public final static String TARGETPID = "TargetPID";
	public final static String TARGETNAME = "TargetName";
	private int targetPID = 0;
	private String targetName = "";
	
	// data  
	private ArrayList<logcatInfo> viewLogcatData = new ArrayList<logcatInfo>();
	private ipcAction logType = ipcAction.LOGCAT_MAIN; 

	private MessageListAdapter messageList = null;
	private Settings settings = null;
	
	private TextView messageCount = null;
	  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);   

		// settings
		settings = Settings.getInstance(getActivity().getApplicationContext());
		
		// set list
		messageList = new MessageListAdapter(getActivity().getApplicationContext());
		
		// get pid
		targetPID = getArguments().getInt(TARGETPID);
		targetName = getArguments().getString(TARGETNAME);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_message_fragment, container, false);
		
		// set count
		messageCount = ((TextView) v.findViewById(R.id.id_message_count));
		
		// set list
		ListView list = (ListView) v.findViewById(android.R.id.list);
		list.setAdapter(messageList);
		
		// set title
		getDialog().setTitle(targetName);

		Button exportButton = (Button) v.findViewById(R.id.id_message_exportbtn);
		exportButton.setVisibility(View.VISIBLE);
		exportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Resources exportRes = getActivity().getResources();
				final Calendar calendar = Calendar.getInstance();
				final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss", Locale.getDefault());
				
				Builder exportDialog = new AlertDialog.Builder(getActivity());
				View exportView = LayoutInflater.from(getActivity()).inflate(R.layout.ui_message_export, null);
				TextView exportFile = (TextView) exportView.findViewById(R.id.id_export_filename);
				exportFile.setText("Log-"+formatter.format(calendar.getTime()));
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
			}
		});
		
		
		return v;  
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

	    		int LogCount = viewLogcatData.size();

	    		FileWriter logWriter = new FileWriter(logFile);

	    		final Calendar calendar = Calendar.getInstance();

	    		for(int index = 0; index < LogCount ; index++)
	    		{
	    			StringBuilder logLine = new StringBuilder();

	    			calendar.setTimeInMillis(viewLogcatData.get(index).getSeconds()*1000);

	    			logLine.append(DateFormat.format("yyyy-MM-dd hh:mm:ss", calendar.getTime()) + ",");

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
	
	@Override
	public void onStart() {
	    super.onStart();
		
	    ipcService.removeRequest(this);
		ipcAction newCommand[] = { logType };
		ipcService.addRequest(newCommand, 0, this);
	}
	 
	@Override 
	public void onStop() {
		super.onStop();
		    
		ipcService.removeRequest(this);
	}
	
	@Override
	public void onRecvData(ipcMessage result) {
		
		if(result == null) {
			ipcAction newCommand[] = new ipcAction[1];
			newCommand [0] =  logType ; 
			ipcService.addRequest(newCommand, settings.getInterval(), this);
			return;
		}
		
		// clean up
		viewLogcatData.clear();

		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);

				for (int count = 0; count < rawData.getPayloadCount(); count++) {
					logcatInfo lgInfo = logcatInfo.parseFrom(rawData.getPayload(count));
					
					// filter 
					if(lgInfo.getPid() != targetPID)
						continue;
					
					viewLogcatData.add(lgInfo);
				}
	
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} 
		
		// refresh
		messageList.refresh();

		// send command again
		ipcAction newCommand[] = new ipcAction[1];
		newCommand [0] =  logType ; 
		ipcService.addRequest(newCommand, settings.getInterval(), this);
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
		  
		public MessageListAdapter(Context mContext) {
			itemInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return viewLogcatData.size();
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
			logcatInfo item = viewLogcatData.get(position);
				
			final Calendar calendar = Calendar.getInstance();
			final java.text.DateFormat convertTool = java.text.DateFormat.getDateTimeInstance();
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
			messageCount.setText(String.format("%,d", viewLogcatData.size()));
			notifyDataSetChanged();
		}
	}
}
