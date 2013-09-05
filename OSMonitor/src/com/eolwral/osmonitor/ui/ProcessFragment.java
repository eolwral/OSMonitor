package com.eolwral.osmonitor.ui;

import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.OsInfo.osInfo;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.preference.Preference;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.Settings;

public class ProcessFragment extends SherlockListFragment 
                             implements	ipcClientListener {
	
	// ipc client  
	private IpcService ipcService = IpcService.getInstance();
	private boolean ipcStop = false;  
 
	// screen 
	private TextView processCount = null;
	private TextView cpuUsage = null;
	private TextView memoryTotal = null;
	private TextView memoryFree = null;

	// data
	private ArrayList<processInfo> data = new ArrayList<processInfo>();
	private osInfo info = null;
	private Settings settings = null;

	// status
	private static int itemColor[] = null;
	private static int oddItem = 0;
	private static int evenItem = 1;
	private static int selectedItem = 2;
	
	private final HashMap<String, Boolean> expandStatus = new HashMap<String, Boolean>();
	private final HashMap<String, Integer> selectedStatus = new HashMap<String, Integer>();

	// preference
	private enum SortType {
		SortbyUsage,
		SortbyMemory,
		SortbyPid,
		SortbyName,
		SortbyCPUTime
	} 
	private SortType sortSetting = SortType.SortbyUsage;

	// view mode
	private enum ModeType {
		View,
		Tools
	}
	private ModeType modeSetting = ModeType.View;
	
	// stop or start
	private boolean stopUpdate = false;
	private ImageButton stopButton = null;
	
	private PopupWindow sortMenu = null;
	private boolean openSortMenu = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// prepare draw color
		itemColor = new int[3];
		itemColor[oddItem] = getResources().getColor(R.color.dkgrey_osmonitor);
		itemColor[evenItem] = getResources().getColor(R.color.black_osmonitor);
		itemColor[selectedItem] = getResources().getColor(R.color.selected_osmonitor);

		setListAdapter(new ProcessListAdapter(getSherlockActivity().getApplicationContext()));
	
	}

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_process_fragment, container, false);
		
		// enable fragment option menu 
		setHasOptionsMenu(true);
		
		// get UI item
		processCount = ((TextView) v.findViewById(R.id.id_process_count));
		cpuUsage = ((TextView) v.findViewById(R.id.id_process_cpuusage));
		memoryTotal = ((TextView) v.findViewById(R.id.id_process_memorytotal));
		memoryFree = ((TextView) v.findViewById(R.id.id_process_memoryfree));
		
		// detect last sort mode
		Settings settings = new Settings(getSherlockActivity());
		if(settings.getSortType().equals("Usage")) {
			sortSetting = SortType.SortbyUsage;
		} else if(settings.getSortType().equals("Pid")) {
			sortSetting = SortType.SortbyPid;
		} else if(settings.getSortType().equals("Memory")) {
			sortSetting = SortType.SortbyMemory;
		} else if(settings.getSortType().equals("Name")) {
			sortSetting = SortType.SortbyName;
		} else if(settings.getSortType().equals("CPUTime")) {
			sortSetting = SortType.SortbyCPUTime;
		}
		return v;
	}
	

	@Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.ui_process_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem helpMenu = menu.findItem(R.id.ui_menu_help);
		helpMenu.setOnMenuItemClickListener( new HelpMenuClickListener());

		MenuItem settingMenu = menu.findItem(R.id.ui_menu_setting);
		settingMenu.setOnMenuItemClickListener( new SettingMenuClickListener());
		
		MenuItem toolsMenu = menu.findItem(R.id.ui_menu_tools);
		toolsMenu.setOnActionExpandListener(new ToolActionExpandListener());
		
		MenuItem exitMenu = menu.findItem(R.id.ui_menu_exit);
		exitMenu.setOnMenuItemClickListener(new ExitMenuClickListener());

		ImageButton sortButton = (ImageButton) toolsMenu.getActionView().findViewById(R.id.id_action_sort);
		sortButton.setOnClickListener( new SortMenuClickListener());

		ImageButton killButton = (ImageButton) toolsMenu.getActionView().findViewById(R.id.id_action_kill);
		killButton.setOnClickListener( new KillButtonClickListener());

		// refresh button
		stopButton = (ImageButton) toolsMenu.getActionView().findViewById(R.id.id_action_stop);

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

	private class SettingMenuClickListener implements OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			Intent settings = new Intent(getActivity(), Preference.class);
	        startActivity(settings);
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
	
	private class ToolActionExpandListener implements OnActionExpandListener {

		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			modeSetting = ModeType.Tools;
			return true;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			modeSetting = ModeType.View;
			selectedStatus.clear();
			((ProcessListAdapter) getListAdapter()).refresh();
			return true;
		}

	}
	
	private class KillButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			// selected items is empty 
			if(selectedStatus.size() == 0)
				return;

			// show message
			String rawFormat = getResources().getString(R.string.ui_text_kill);  
			String strFormat = String.format(rawFormat, selectedStatus.size());  
			Toast.makeText(getSherlockActivity().getApplicationContext(),
							strFormat, Toast.LENGTH_SHORT).show();
			
			// kill all selected items
			for(Entry<String, Integer> item : selectedStatus.entrySet())
				killProcess(item.getValue(), item.getKey());
			
			// clean up selected items
			selectedStatus.clear();
		}
	}
	
	private class SortMenuClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {

			openSortMenu = !openSortMenu;
			if (!openSortMenu) {
				sortMenu.dismiss();
				return;
			}

			if (null == sortMenu) {
				View layout = LayoutInflater.from(getSherlockActivity())
						.inflate(R.layout.ui_process_menu_sort, null);
				sortMenu = new PopupWindow(layout);
				sortMenu.setBackgroundDrawable(new BitmapDrawable());
				sortMenu.setFocusable(true);
				sortMenu.setWidth(LayoutParams.WRAP_CONTENT);
				sortMenu.setHeight(LayoutParams.WRAP_CONTENT);
				sortMenu.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						openSortMenu = false;
					}
				});
			}

			RadioGroup sortGroup = (RadioGroup) sortMenu.getContentView()
					.findViewById(R.id.id_process_sort_group);
			switch (sortSetting) {
			case SortbyUsage:
				sortGroup.check(R.id.id_process_sort_usage);
				break;
			case SortbyMemory:
				sortGroup.check(R.id.id_process_sort_memory);
				break;
			case SortbyPid:
				sortGroup.check(R.id.id_process_sort_pid);
				break;
			case SortbyName:
				sortGroup.check(R.id.id_process_sort_name);
				break;
			case SortbyCPUTime:
				sortGroup.check(R.id.id_process_sort_cputime);
				break;
			}
			
			sortGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					switch (checkedId) {
					case R.id.id_process_sort_usage:
						sortSetting = SortType.SortbyUsage;
						settings.setSortType("Usage");
						break;
					case R.id.id_process_sort_memory:
						sortSetting = SortType.SortbyMemory;
						settings.setSortType("Memory");
						break;
					case R.id.id_process_sort_pid:
						sortSetting = SortType.SortbyPid;
						settings.setSortType("Pid");
						break;
					case R.id.id_process_sort_name:
						sortSetting = SortType.SortbyName;
						settings.setSortType("Name");
						break;
					case R.id.id_process_sort_cputime:
						sortSetting = SortType.SortbyCPUTime;
						settings.setSortType("CPUTime");
						break;
					}

					// force update after setting has been changed
					if(stopUpdate == true)
						stopButton.performClick();
					
					sortMenu.dismiss();
				}

			});

			sortMenu.showAsDropDown(v);
		
		}
	}
	 
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);
	    
	    ipcService.removeRequest(this);
		ipcStop = !isVisibleToUser; 
 
		if(isVisibleToUser == true) { 
			settings = new Settings(getActivity());
			ipcAction newCommand[] = { ipcAction.OS, ipcAction.PROCESS };
			ipcService.addRequest(newCommand, 0, this);
		}
		
	}
	
	private void killProcess(int pid, String process) {
		CommonUtil.killProcess(pid, getActivity());
		((ActivityManager) getSherlockActivity().
			getSystemService(Context.ACTIVITY_SERVICE)).restartPackage(process);
	}

	
	@Override
	public void onRecvData(ipcMessage result) {

		// check 
		if(ipcStop == true)
		  return;

		// stop update
		if (stopUpdate == true || result == null) {
			ipcAction newCommand[] = { ipcAction.OS, ipcAction.PROCESS };
			ipcService.addRequest(newCommand, settings.getInterval(), this);
			return;
		}
		
		// clean up
		while (!data.isEmpty())
			data.remove(0);
		data.clear();

		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);

				// process osInfo
				if (rawData.getAction() == ipcAction.OS) {
					info = osInfo.parseFrom(rawData.getPayload(0));
					continue;
				}

				// skip others
				if (rawData.getAction() != ipcAction.PROCESS)
					continue;

				// summary all system processes
				processInfo.Builder syspsInfo = processInfo.newBuilder();
				
				// fixed value
				syspsInfo.setPid(0);
				syspsInfo.setUid(0);
				syspsInfo.setPpid(0);
				syspsInfo.setName("System");
				syspsInfo.setOwner("root");
				syspsInfo.setPriorityLevel(0);
				syspsInfo.setStatus(processInfo.processStatus.Running);

				// summary value
				syspsInfo.setCpuUsage(0);
				syspsInfo.setRss(0);
				syspsInfo.setVsz(0);
				syspsInfo.setStartTime(0);
				syspsInfo.setThreadCount(0);
				syspsInfo.setUsedSystemTime(0);
				syspsInfo.setUsedUserTime(0);
				syspsInfo.setCpuTime(0);

				// process processInfo
				for (int count = 0; count < rawData.getPayloadCount(); count++) {
					processInfo psInfo = processInfo.parseFrom(rawData.getPayload(count));
                    
					boolean doMerge = false;
					
					if( psInfo.getUid() == 0 ||
						psInfo.getName().contains("/system/") ||
						psInfo.getName().contains("/sbin/") )
						doMerge = true;

					if(psInfo.getName().toLowerCase(Locale.getDefault()).contains("osmcore"))
						doMerge = false;
					
					if(settings.isUseExpertMode())
						doMerge = false;

					// Don't merge data
					if(doMerge == false)
					{
						data.add(psInfo);
						continue;
					}
					
					// Merge process information into a process
					syspsInfo.setCpuUsage(syspsInfo.getCpuUsage()+psInfo.getCpuUsage());
					syspsInfo.setRss(syspsInfo.getRss()+psInfo.getRss());
					syspsInfo.setVsz(syspsInfo.getVsz()+psInfo.getVsz());
					syspsInfo.setThreadCount(syspsInfo.getThreadCount()+psInfo.getThreadCount());
					syspsInfo.setUsedSystemTime(syspsInfo.getUsedSystemTime()+psInfo.getUsedSystemTime());
					syspsInfo.setUsedUserTime(syspsInfo.getUsedUserTime()+psInfo.getUsedUserTime());
					syspsInfo.setCpuTime(syspsInfo.getCpuTime()+psInfo.getCpuTime());
						
					if(syspsInfo.getStartTime() < psInfo.getStartTime() ||
					   syspsInfo.getStartTime() == 0)
					syspsInfo.setStartTime(psInfo.getStartTime());
					
				}
				
				if(!settings.isUseExpertMode())
					data.add(syspsInfo.build());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// calculate CPU Usage
		float totalCPUUsage = 0;
		for (int index = 0; index < data.size(); index++)
			totalCPUUsage += data.get(index).getCpuUsage();

		// sort data
		switch(sortSetting)
		{
		case SortbyUsage:
			Collections.sort(data, new SortbyUsage());
			break;
		case SortbyMemory:
			Collections.sort(data, new SortbyMemory());
			break;
		case SortbyPid:
			Collections.sort(data, new SortbyPid());
			break;
		case SortbyName:
			Collections.sort(data, new SortbyName());
			break;
		case SortbyCPUTime:
			Collections.sort(data, new SortbyCPUTime());
			break;
		}

		processCount.setText(""+data.size());
		cpuUsage.setText(CommonUtil.convertToUsage(totalCPUUsage) + "%");
		memoryTotal.setText(CommonUtil.convertToSize(info.getTotalMemory(), true));
		memoryFree.setText(CommonUtil.convertToSize(info.getFreeMemory()+
				                                  info.getBufferedMemory()+
				                                  info.getCachedMemory(), true));

		getSherlockActivity().runOnUiThread( new Runnable() {
			public void run() { 
				((ProcessListAdapter) getListAdapter()).refresh();
			}
		});

		// send command again
		ipcAction newCommand[] = { ipcAction.OS, ipcAction.PROCESS };
		ipcService.addRequest(newCommand, settings.getInterval(), this);
	}
	
	/**
	 * Comparator class for sort by usage 
	 */
	private class SortbyUsage implements Comparator<processInfo> {

		@Override
		public int compare(processInfo lhs, processInfo rhs) {
			if (lhs.getCpuUsage() > rhs.getCpuUsage())
				return -1;
			else if (lhs.getCpuUsage() < rhs.getCpuUsage())
				return 1;
			return 0;
		}		
	}
	
	/**
	 * Comparator class for sort by memory  
	 */
	private class SortbyMemory implements Comparator<processInfo> {

		@Override
		public int compare(processInfo lhs, processInfo rhs) {
			if (lhs.getRss() > rhs.getRss())
				return -1;
			else if (lhs.getRss() < rhs.getRss())
				return 1;
			return 0;
		}		
	}
	
	/**
	 * Comparator class for sort by Pid 
	 */
	private class SortbyPid implements Comparator<processInfo> {

		@Override
		public int compare(processInfo lhs, processInfo rhs) {
			if (lhs.getPid() > rhs.getPid())
				return -1;
			else if (lhs.getPid() < rhs.getPid())
				return 1;
			return 0;
		}		
	}

	/**
	 * Comparator class for sort by Name 
	 */
	private class SortbyName implements Comparator<processInfo> {

		@Override
		public int compare(processInfo lhs, processInfo rhs) {
			Collator collator = Collator.getInstance();
			if (collator.compare(lhs.getName(), rhs.getName()) == -1)
				return -1;
			else if (collator.compare(lhs.getName(), rhs.getName()) == 1)
				return 1;
			return 0;
		}		
	}
	
	/**
	 * Comparator class for sort by CPU time 
	 */
	private class SortbyCPUTime implements Comparator<processInfo> {

		@Override
		public int compare(processInfo lhs, processInfo rhs) {
			if (lhs.getCpuTime() > rhs.getCpuTime())
				return -1;
			else if (lhs.getCpuTime() < rhs.getCpuTime())
				return 1;
			return 0;
		}		
	}

	/**
	 * implement viewholder class for process list
	 */
	private class ViewHolder {
		
		// main information
		TextView pid;
		ImageView icon;
		TextView name;
		TextView cpuUsage;
		
		// color
		int bkcolor;
		
		// detail information
		RelativeLayout detail;
		
		TextView detailName;
		TextView detailStatus;
		TextView detailStime;
		TextView detailUtime;
		TextView detailCPUtime;
		TextView detailMemory;
		TextView detailPPID;
		TextView detailUser;
		TextView detailStarttime;
		TextView detailThread;
		TextView detailNice;
	}
	
	private static void setItemStatus(View v, boolean status) {

		// change expand status
		if (status) {

			ViewHolder holder = (ViewHolder) v.getTag();
			if (holder.detail == null) {
				
				// loading view
				ViewStub stub = (ViewStub) v.findViewById(R.id.id_process_detail_viewstub);
				stub.inflate();
				
				// prepare detail information
				holder.detail = (RelativeLayout) v.findViewById(R.id.id_process_detail_stub);
				holder.detailName = ((TextView) v.findViewById(R.id.id_process_detail_name));
				holder.detailStatus = ((TextView) v.findViewById(R.id.id_process_detail_status));
				holder.detailStime = ((TextView) v.findViewById(R.id.id_process_detail_stime));
				holder.detailUtime = ((TextView) v.findViewById(R.id.id_process_detail_utime));
				holder.detailCPUtime = ((TextView) v.findViewById(R.id.id_process_detail_cputime));
				holder.detailMemory = ((TextView) v.findViewById(R.id.id_process_detail_memory));
				holder.detailPPID = ((TextView) v.findViewById(R.id.id_process_detail_ppid));
				holder.detailUser = ((TextView) v.findViewById(R.id.id_process_detail_user));
				holder.detailStarttime = ((TextView) v.findViewById(R.id.id_process_detail_starttime));
				holder.detailThread = ((TextView) v.findViewById(R.id.id_process_detail_thread));
				holder.detailNice = ((TextView) v.findViewById(R.id.id_process_detail_nice));
				
			} else 
				holder.detail.setVisibility(View.VISIBLE);

		} else {
			ViewHolder holder = (ViewHolder) v.getTag();
			if (holder.detail != null) 
				holder.detail.setVisibility(View.GONE);
		}
	}
	
	private static void setSelectStatus(View v, int position, boolean status) {

		if (status) 
			v.setBackgroundColor(itemColor[selectedItem]);
		else if (position % 2 == 0)
			v.setBackgroundColor(itemColor[oddItem]); 
		else
			v.setBackgroundColor(itemColor[evenItem]);

	}

	private class ProcessListAdapter extends BaseAdapter {

		private LayoutInflater itemInflater = null;
		private ProcessUtil infoHelper = null;
		private ViewHolder holder = null;

		public ProcessListAdapter(Context mContext) {
			itemInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			infoHelper = ProcessUtil.getInstance(mContext, true);
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View sv = null;

			// get data
			processInfo item = data.get(position);

			// check cached status
			if (!infoHelper.checkPackageInformation(item.getName())) {
				if(item.getName().toLowerCase(Locale.getDefault()).contains("osmcore")) 
					infoHelper.doCacheInfo(android.os.Process.myUid(), item.getOwner(), item.getName());
				else
					infoHelper.doCacheInfo(item.getUid(), item.getOwner(), item.getName());
			}

			// prepare view
			if (convertView == null) {

				sv = (View) itemInflater.inflate(R.layout.ui_process_item, parent, false);

				holder = new ViewHolder();
				holder.pid = ((TextView) sv.findViewById(R.id.id_process_pid));
				holder.name = ((TextView) sv.findViewById(R.id.id_process_name));
				holder.cpuUsage = ((TextView) sv.findViewById(R.id.id_process_value));
				holder.icon = ((ImageView) sv.findViewById(R.id.id_process_icon));

				sv.setTag(holder);
			} else {
				sv = (View) convertView;
				holder = (ViewHolder) sv.getTag();
			}

			sv.setOnClickListener(new ProcessClickListener(position));
			sv.setOnLongClickListener(new ProcessLongClickListener(position));
			
			// check expand status
			if (expandStatus.containsKey(data.get(position).getName()) == true)
				setItemStatus(sv, true);
			else 
                setItemStatus(sv, false);
			
			// draw current color for each item
			if (selectedStatus.containsKey(data.get(position).getName()) == true) 
				holder.bkcolor = itemColor[selectedItem];
			else if (position % 2 == 0)
				holder.bkcolor = itemColor[oddItem]; 
			else
				holder.bkcolor = itemColor[evenItem];
			
			sv.setBackgroundColor(holder.bkcolor);

			// offer better indicator for interactive
			sv.setOnTouchListener(new OnTouchListener () {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					
					// skip when on tools mode
					//if(modeSetting == ModeType.Tools)
					//	return false;
					
					switch(event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
					    v.setBackgroundColor(getResources().getColor(R.color.selected_osmonitor));
					    break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					    ViewHolder holder = (ViewHolder) v.getTag();
					    v.setBackgroundColor(holder.bkcolor);
					    break;
					}
					return false;
				}
				
			});

			// prepare main information
			holder.pid.setText(String.format("%5d", item.getPid()));
			holder.name.setText(infoHelper.getPackageName(item.getName()));
			holder.icon.setImageDrawable(infoHelper.getPackageIcon(item.getName()));
			
			if(sortSetting == SortType.SortbyMemory)
				holder.cpuUsage.setText(CommonUtil.convertToSize((item.getRss()*1024), true));
			else if(sortSetting == SortType.SortbyCPUTime)
				holder.cpuUsage.setText(String.format("%02d:%02d", item.getCpuTime()/60, item.getCpuTime() % 60));
			else 
				holder.cpuUsage.setText(CommonUtil.convertToUsage(item.getCpuUsage()));
			
			// prepare detail information
			if (holder.detail != null)
			{
				holder.detailName.setText(item.getName());
				holder.detailStime.setText(String.format("%,d", item.getUsedSystemTime()));
				holder.detailUtime.setText(String.format("%,d", item.getUsedUserTime()));
				
				holder.detailCPUtime.setText(String.format("%02d:%02d", item.getCpuTime()/60, item.getCpuTime() % 60));
				
				holder.detailThread.setText(String.format("%d", item.getThreadCount()));
				holder.detailNice.setText(String.format("%d", item.getPriorityLevel()));
				
				// get memory information
				MemoryInfo memInfo = infoHelper.getMemoryInfo(item.getPid());
				String memoryData = CommonUtil.convertToSize((item.getRss()*1024), true)+" ("+
						            CommonUtil.convertToSize(memInfo.getTotalPss()*1024, true)+")";

				holder.detailMemory.setText(memoryData); 
				
				holder.detailPPID.setText(""+item.getPpid());
				
				// convert time format
				final Calendar calendar = Calendar.getInstance();
				final DateFormat convertTool = DateFormat.getDateTimeInstance(DateFormat.LONG,
						                                   DateFormat.LONG, Locale.getDefault());
				calendar.setTimeInMillis(item.getStartTime()*1000);
				holder.detailStarttime.setText(convertTool.format(calendar.getTime()));
				
				holder.detailUser.setText(item.getOwner());
				
				// convert status
				switch(item.getStatus().getNumber())
				{
				case processInfo.processStatus.Unknown_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_unknown);
					break;
				case processInfo.processStatus.Running_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_running);
					break;
				case processInfo.processStatus.Sleep_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_sleep);
					break;
				case processInfo.processStatus.Stopped_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_stop);
					break;
				case processInfo.processStatus.Page_VALUE:
				case processInfo.processStatus.Disk_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_waitio);
					break;
				case processInfo.processStatus.Zombie_VALUE:
					holder.detailStatus.setText(R.string.ui_process_status_zombie);
					break;
				}
				
			}

			return sv;
		}

		public void refresh() {
			this.notifyDataSetChanged();
		}
		
		private class ProcessLongClickListener implements OnLongClickListener {
			private int position;

			public ProcessLongClickListener(int position) {
				this.position = position;
			}

			@Override
			public boolean onLongClick(View v) {
				
				//if(modeSetting == ModeType.Tools)
				//	  return false;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    builder.setTitle(infoHelper.getPackageName(data.get(position).getName()))
			           .setItems(R.array.ui_process_menu_item, new ProcessItemMenu(position));
			    builder.create().show();
			    return false;
			}
			
		}
		
		private class ProcessItemMenu implements DialogInterface.OnClickListener {
			private String process;
			private int pid;
			private int prority;
			
            public ProcessItemMenu(int position) {
            	this.process = data.get(position).getName();
            	this.pid = data.get(position).getPid();
            	this.prority = data.get(position).getPriorityLevel();
			}

			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					killProcess(pid, process);
					break;
				case 1:
		   	    	switchToProcess();
		   	        break;
				case 2:
					watchLog();
					break;
				case 3:
					setPrority();
					break;
		   	        					
				}
            }
			
			private void setPrority() {
		    	// pass information
				ProcessProrityFragment newPrority = new ProcessProrityFragment();
		    	Bundle args = new Bundle();
		    	args.putInt(ProcessProrityFragment.TARGETPID, pid);
		    	args.putString(ProcessProrityFragment.TARGETNAME, infoHelper.getPackageName(process));
		    	args.putInt(ProcessProrityFragment.DEFAULTPRORITY, prority);
		    	newPrority.setArguments(args);
		    	
		    	// replace current fragment
		    	final FragmentManager fragmanger = getSherlockActivity().getSupportFragmentManager();
		    	newPrority.show(fragmanger, "prority");
			}
			
			private void watchLog() {
		    	// pass information
				ProcessLogViewFragment newLog = new ProcessLogViewFragment();
		    	Bundle args = new Bundle();
		    	args.putInt(ProcessLogViewFragment.TARGETPID, pid);
		    	args.putString(ProcessLogViewFragment.TARGETNAME, infoHelper.getPackageName(process));
		    	newLog.setArguments(args);
		    	
		    	// replace current fragment
		    	final FragmentManager fragmanger = getSherlockActivity().getSupportFragmentManager();
		    	newLog.show(fragmanger, "logview");
			}

			private void switchToProcess() {
				PackageManager QueryPackage = getSherlockActivity().getPackageManager();
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> appList = QueryPackage.queryIntentActivities(mainIntent, 0);
				String className = null;
				for(int index = 0; index < appList.size(); index++)
					if(appList.get(index).activityInfo.applicationInfo.packageName.equals(process))
						className = appList.get(index).activityInfo.name;
				
				if(className != null) {
				    Intent switchIntent = new Intent();
				    switchIntent.setAction(Intent.ACTION_MAIN);
				    switchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				    switchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				    		   			  Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
				    		   			  Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				    switchIntent.setComponent(new ComponentName(process, className));
				    startActivity(switchIntent);
				    getSherlockActivity().finish();
				}
			}
		}
		
		private class ProcessClickListener implements OnClickListener {
			private int position;

			public ProcessClickListener(int position) {
				this.position = position;
			}

			public void onClick(View v) {

				switch (modeSetting)
				{
				case View:
					this.ToogleExpand(v);
					break;
				case Tools:
					this.ToogleSelected(v);
					break;
				}
			}
			
			private void ToogleSelected(View v)	{
				// change expand status
				if (selectedStatus.containsKey(data.get(position).getName()) == false) {
					selectedStatus.put(data.get(position).getName(), data.get(position).getPid());
					setSelectStatus(v, position, true);
				} else {
					selectedStatus.remove(data.get(position).getName());
					setSelectStatus(v, position, false);
				}				
			} 
			
			private void ToogleExpand(View v)	{
				// change expand status
				if (expandStatus.containsKey(data.get(position).getName()) == false) {
					expandStatus.put(data.get(position).getName(),	Boolean.TRUE);
					setItemStatus(v, true);
				} else {
					expandStatus.remove(data.get(position).getName());
					setItemStatus(v, false);
				}				
			}

		}
	}
	
    @SuppressLint("SetJavaScriptEnabled")
	void ShowHelp()
    {
    	CommonUtil.showHelp(getActivity(), "file:///android_asset/help/help-process.html");
    }
}
