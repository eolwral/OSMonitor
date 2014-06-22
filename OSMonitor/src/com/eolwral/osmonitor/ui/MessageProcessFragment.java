package com.eolwral.osmonitor.ui;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.ProcessUtil;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.widget.TextView;

public class MessageProcessFragment extends Dialog {
	
	private processInfo item = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_process_item_detail);
		
		preapreProcessDetail();
	}

	public MessageProcessFragment(Context context) {
		super(context);
	}

	public void setProcessData(processInfo item) {
		this.item = item;
	}
	
	private void preapreProcessDetail() {
		
		ProcessUtil infoHelper = ProcessUtil.getInstance(getContext(), true);
		
		TextView detailName = ((TextView) findViewById(R.id.id_process_detail_name));
		TextView detailStatus = ((TextView) findViewById(R.id.id_process_detail_status));
		TextView detailStime = ((TextView) findViewById(R.id.id_process_detail_stime));
		TextView detailUtime = ((TextView) findViewById(R.id.id_process_detail_utime));
		TextView detailCPUtime = ((TextView) findViewById(R.id.id_process_detail_cputime));
		TextView detailMemory = ((TextView) findViewById(R.id.id_process_detail_memory));
		TextView detailPPID = ((TextView) findViewById(R.id.id_process_detail_ppid));
		TextView detailUser = ((TextView) findViewById(R.id.id_process_detail_user));
		TextView detailStarttime = ((TextView) findViewById(R.id.id_process_detail_starttime));
		TextView detailThread = ((TextView) findViewById(R.id.id_process_detail_thread));
		TextView detailNice = ((TextView) findViewById(R.id.id_process_detail_nice));
		
		detailName.setText(item.getName());
		detailStime.setText(String.format("%,d", item.getUsedSystemTime()));
		detailUtime.setText(String.format("%,d", item.getUsedUserTime()));
		
		detailCPUtime.setText(String.format("%02d:%02d", item.getCpuTime()/60, item.getCpuTime() % 60));
		
		detailThread.setText(String.format("%d", item.getThreadCount()));
		detailNice.setText(String.format("%d", item.getPriorityLevel()));
		
		// get memory information
		MemoryInfo memInfo = infoHelper.getMemoryInfo(item.getPid());
		String memoryData = CommonUtil.convertToSize((item.getRss()*1024), true)+" /  "+
				                                     CommonUtil.convertToSize(memInfo.getTotalPss()*1024, true)+" / " +
				                                     CommonUtil.convertToSize(memInfo.getTotalPrivateDirty()*1024, true) ;

		detailMemory.setText(memoryData); 
		
		detailPPID.setText(""+item.getPpid());
		
		// convert time format
		final Calendar calendar = Calendar.getInstance();
		final DateFormat convertTool = DateFormat.getDateTimeInstance(DateFormat.LONG,
				                                   DateFormat.MEDIUM, Locale.getDefault());
		calendar.setTimeInMillis(item.getStartTime()*1000);
		detailStarttime.setText(convertTool.format(calendar.getTime()));
		
		detailUser.setText(item.getOwner());
		
		// convert status
		switch(item.getStatus().getNumber())
		{
		case processInfo.processStatus.Unknown_VALUE:
			detailStatus.setText(R.string.ui_process_status_unknown);
			break;
		case processInfo.processStatus.Running_VALUE:
			detailStatus.setText(R.string.ui_process_status_running);
			break;
		case processInfo.processStatus.Sleep_VALUE:
			detailStatus.setText(R.string.ui_process_status_sleep);
			break;
		case processInfo.processStatus.Stopped_VALUE:
			detailStatus.setText(R.string.ui_process_status_stop);
			break;
		case processInfo.processStatus.Page_VALUE:
		case processInfo.processStatus.Disk_VALUE:
			detailStatus.setText(R.string.ui_process_status_waitio);
			break;
		case processInfo.processStatus.Zombie_VALUE:
			detailStatus.setText(R.string.ui_process_status_zombie);
			break;
		}
	}
}
