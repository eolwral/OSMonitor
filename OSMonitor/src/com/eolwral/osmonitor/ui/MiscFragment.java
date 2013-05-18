package com.eolwral.osmonitor.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.NetworkInfo.networkInfo;
import com.eolwral.osmonitor.core.OsInfo.osInfo;
import com.eolwral.osmonitor.core.ProcessorInfo.processorInfo;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.preference.Preference;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.Settings;
import com.google.protobuf.InvalidProtocolBufferException;

public class MiscFragment extends SherlockFragment 
                                implements ipcClientListener {

	// ipc client
	private IpcService ipcService =  IpcService.getInstance();;
	private boolean ipcStop = false;

	// data
	private osInfo osdata = null;
	private ArrayList<processorInfo> coredata = new ArrayList<processorInfo>();
	private ArrayList<networkInfo> nwdata = new ArrayList<networkInfo>();
	private Settings settings = null;
	
	// list
	private MiscListAdapter misckAdapter = null;
	private String [] miscItems = null;
	
	// expand 
	private HashMap<String, Boolean> networkExpanded = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> coreExpanded = new HashMap<String, Boolean>();
	
	// stop or start
	private boolean stopUpdate = false;
	private MenuItem stopButton = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_misc_fragment, container, false);

		miscItems = getResources().getStringArray(R.array.ui_misc_item);

		ExpandableListView miscList = (ExpandableListView) v.findViewById(android.R.id.list);
		misckAdapter = new MiscListAdapter(getSherlockActivity().getApplicationContext());
		miscList.setGroupIndicator(null);
		miscList.setAdapter(misckAdapter);
		
		int count = misckAdapter.getGroupCount();
		for (int position = 1; position <= count; position++)
			miscList.expandGroup(position - 1);
		
		// enable fragment option menu 
		setHasOptionsMenu(true);
				
		return v;
	}
	
	@Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.ui_misc_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem helpMenu = menu.findItem(R.id.ui_menu_help);
		helpMenu.setOnMenuItemClickListener( new HelpMenuClickListener());
		
		MenuItem settingMenu = menu.findItem(R.id.ui_menu_setting);
		settingMenu.setOnMenuItemClickListener( new SettingMenuClickListener());
		
		// refresh button
		stopButton = (MenuItem) menu.findItem(R.id.ui_menu_stop);

		if(stopUpdate) 
			stopButton.setIcon(R.drawable.ic_action_start);
		else
			stopButton.setIcon(R.drawable.ic_action_stop);

		stopButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				stopUpdate = !stopUpdate;
				
				if(stopUpdate) 
					stopButton.setIcon(R.drawable.ic_action_start);
				else
					stopButton.setIcon(R.drawable.ic_action_stop);
				return false;
			}
		});		
		return; 
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
	
	// TODO: use view holder to reduce consuming resource 
	private class MiscListAdapter extends BaseExpandableListAdapter {

		private LayoutInflater itemInflater = null;

		public MiscListAdapter(Context mContext) {
			itemInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			
			View sv = null;
			
			switch(groupPosition){
			case 0:
				sv = prepareSystemView(childPosition, convertView, parent);
				break;
			case 1:
				sv = prepareCPUView(childPosition, convertView, parent);
				break;
			case 2:
				sv = prepareMemoryView(childPosition, convertView, parent);
				break;
			case 3:
				sv = prepareSharedView(childPosition, convertView, parent);
				break;
			case 4:
				sv = prepareNetworkView(childPosition, convertView, parent);
				break;
			}
			return sv;
		}
		
		public View prepareNetworkView(int position, View convertView, ViewGroup parent) {
			
			View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_network, parent, false);
			
			if(nwdata.size() < position)
				return sv;
			
			// get data
			networkInfo item = nwdata.get(position);
			
			// prepare main information
			((TextView) sv.findViewById(R.id.id_network_interface)).setText(item.getName());
			if(item.hasIpv4Addr())
				((TextView) sv.findViewById(R.id.id_network_ip4)).setText(item.getIpv4Addr()+"/"+item.getNetMaskv4());
			else
				((TextView) sv.findViewById(R.id.id_network_ip4)).setText("");
			
			if(item.hasIpv6Addr())
				((TextView) sv.findViewById(R.id.id_network_ip6)).setText(item.getIpv6Addr()+"/"+item.getNetMaskv6());
			else
				((TextView) sv.findViewById(R.id.id_network_ip6)).setText("");
			
			// prepare main information
			((TextView) sv.findViewById(R.id.id_network_mac)).setText(item.getMac());
			((TextView) sv.findViewById(R.id.id_network_rx)).setText(String.format("%,d", item.getRecvBytes())+" ("+
					               CommonUtil.convertLong(item.getRecvBytes())+")");
			((TextView) sv.findViewById(R.id.id_network_tx)).setText(String.format("%,d", item.getTransBytes())+" ("+
					               CommonUtil.convertLong(item.getTransBytes())+")");
			
			StringBuilder status = new StringBuilder();

			final Resources resMgr = getSherlockActivity().getResources();
			
			// IFF_UP = 0x1,         /* Interface is up.  */
			if((item.getFlags() & 0x1) != 0)
				status.append(resMgr.getString(R.string.ui_network_status_up));
			else
				status.append(resMgr.getString(R.string.ui_network_status_down));
			
			// IFF_BROADCAST = 0x2,  /* Broadcast address valid.  */
			if((item.getFlags() & 0x2) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_broadcast));
			
			// IFF_DEBUG = 0x4,              /* Turn on debugging.  */
			// IFF_LOOPBACK = 0x8,           /* Is a loopback net.  */
			if((item.getFlags() & 0x8) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_loopback));
			
			// IFF_POINTOPOINT = 0x10,       /* Interface is point-to-point link.  */
			if((item.getFlags() & 0x10) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_p2p));

			
			// IFF_NOTRAILERS = 0x20,        /* Avoid use of trailers.  */
			// IFF_RUNNING = 0x40,           /* Resources allocated.  */
			if((item.getFlags() & 0x40) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_running));
			
			// IFF_NOARP = 0x80,             /* No address resolution protocol.  */
			// IFF_PROMISC = 0x100,          /* Receive all packets.  */
			if((item.getFlags() & 0x100) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_promisc));

			// IFF_ALLMULTI = 0x200,         /* Receive all multicast packets.  */
			// IFF_MASTER = 0x400,           /* Master of a load balancer.  */
			// IFF_SLAVE = 0x800,            /* Slave of a load balancer.  */
			
			// IFF_MULTICAST = 0x1000,       /* Supports multicast.  */
			if((item.getFlags() & 0x1000) != 0)
				status.append(" "+resMgr.getString(R.string.ui_network_status_multicast));

			// IFF_PORTSEL = 0x2000,         /* Can set media type.  */
			// IFF_AUTOMEDIA = 0x4000,       /* Auto media select active.  */
			// IFF_DYNAMIC = 0x8000          /* Dialup device with changing addresses.  */

			((TextView) sv.findViewById(R.id.id_network_status)).setText(status.toString());
			
			// prepare click
			sv.setTag(item.getName());
			sv.setClickable(true);
			sv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Boolean flag = networkExpanded.get(v.getTag());
					if(flag == null) {
						networkExpanded.put((String) v.getTag(), true);
						flag = true;
					}
					else {
						networkExpanded.put((String) v.getTag(), !flag);
						flag = !flag;
					}
					
					if (flag)
						v.findViewById(R.id.id_misc_item_network_detail).setVisibility(View.VISIBLE);
					else
						v.findViewById(R.id.id_misc_item_network_detail).setVisibility(View.GONE);
				}
   			});

			// set visibility
			Boolean flag = networkExpanded.get(item.getName());
			if(flag == null) 
				flag = false;
			if(flag)
				sv.findViewById(R.id.id_misc_item_network_detail).setVisibility(View.VISIBLE);
			else
				sv.findViewById(R.id.id_misc_item_network_detail).setVisibility(View.GONE);

			return sv;			
		}
		
		public View prepareCPUView(int position, View convertView, ViewGroup parent) {

			// prepare view
			View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_cpu, parent, false);

			if(coredata.size() < position)
				return sv;
			
			// get data
			processorInfo item = coredata.get(position);

			((TextView) sv.findViewById(R.id.id_processor_freq_min)).setText(""+item.getMinFrequency());
			((TextView) sv.findViewById(R.id.id_processor_freq_max)).setText(""+item.getMaxFrequency());
			((TextView) sv.findViewById(R.id.id_processor_cur_max)).setText(""+item.getMinScaling());
			((TextView) sv.findViewById(R.id.id_processor_cur_min)).setText(""+item.getMaxScaling());
			((TextView) sv.findViewById(R.id.id_processor_cur)).setText(""+item.getCurrentScaling());
			((TextView) sv.findViewById(R.id.id_processor_gov)).setText(item.getGrovernors());
			((TextView) sv.findViewById(R.id.id_processor_core)).setText(""+item.getNumber());
			 
			if(item.getOffLine() == true)
				((TextView) sv.findViewById(R.id.id_processor_status)).setText(
					getSherlockActivity().getResources().getText(R.string.ui_processor_status_offline));
			else
				((TextView) sv.findViewById(R.id.id_processor_status)).setText(
					getSherlockActivity().getResources().getText(R.string.ui_processor_status_online));
  
			// prepare click
			sv.setTag(""+item.getNumber());
			sv.setClickable(true);
			sv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Boolean flag = coreExpanded.get(v.getTag());
					if(flag == null) {
						coreExpanded.put((String) v.getTag(), true);
						flag = true;
					}
					else {
						coreExpanded.put((String) v.getTag(), !flag);
						flag = !flag;
					}
					
					if (flag)
						v.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(View.VISIBLE);
					else
						v.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(View.GONE);
				}
   			});

			// set visibility
			Boolean flag = coreExpanded.get(""+item.getNumber());
			if(flag == null) 
				flag = false;
			if(flag)
				sv.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(View.VISIBLE);
			else
				sv.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(View.GONE);

			return sv;			
		}

		private View prepareSharedView(int position, View convertView, ViewGroup parent) {
			View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_swap, parent, false);
			
			if(osdata != null) {
				
				((TextView) sv.findViewById(R.id.id_swap_total)).setText(
						String.format("%,d", osdata.getTotalSwap())+" ("+
                        CommonUtil.convertLong(osdata.getTotalSwap())+")");
				((TextView) sv.findViewById(R.id.id_swap_free)).setText(
						String.format("%,d", osdata.getFreeSwap())+" ("+
                        CommonUtil.convertLong(osdata.getFreeSwap())+")");			
				}
		
			return sv;
		}

		private View prepareMemoryView(int position, View convertView, ViewGroup parent) {
			View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_memory, parent, false);
			
			if(osdata != null) {

				((TextView) sv.findViewById(R.id.id_memory_total)).setText(
						 String.format("%,d", osdata.getTotalMemory())+" ("+
                         CommonUtil.convertLong(osdata.getTotalMemory())+")");
				
				((TextView) sv.findViewById(R.id.id_memory_free)).setText(
						 String.format("%,d", osdata.getFreeMemory())+" ("+
                         CommonUtil.convertLong(osdata.getFreeMemory())+")");
				
				((TextView) sv.findViewById(R.id.id_memory_shared)).setText(
						 String.format("%,d", osdata.getSharedMemory())+" ("+
                         CommonUtil.convertLong(osdata.getSharedMemory())+")");
				
				((TextView) sv.findViewById(R.id.id_memory_buffered)).setText(
						String.format("%,d", osdata.getBufferedMemory())+" ("+
                        CommonUtil.convertLong(osdata.getBufferedMemory())+")");			
			}
		
			return sv;
		}

		private View prepareSystemView(int position, View convertView, ViewGroup parent) {
			View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_system, parent, false);

			final Calendar calendar = Calendar.getInstance();
			final DateFormat convertTool = DateFormat.getDateTimeInstance();
			if(osdata != null){
				calendar.setTimeInMillis(osdata.getUptime()*1000);
				((TextView) sv.findViewById(R.id.id_system_update)).setText(
						convertTool.format(calendar.getTime()));
			}
			
			return sv;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int count = 0;
			switch(groupPosition){
			case 0:
				count = 1;
				break;
			case 1:
				count = coredata.size();
				break;
			case 2:
				count = 1;
				break;
			case 3:
				count = 1;
				break;
			default:
				count = nwdata.size();
				break;
			}
			return count;
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getGroupCount() {
			return miscItems.length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
			
			View sv = null;

			// prepare view
			if (convertView == null) 
				sv = (View) itemInflater.inflate(R.layout.ui_misc_item, parent, false); 
			else 
				sv = (View) convertView;

			// set title
			((TextView) sv.findViewById(R.id.id_misc_title)).setText(miscItems[groupPosition]);
			
			return sv;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    ipcService.removeRequest(this);
		ipcStop = !isVisibleToUser;

		if(isVisibleToUser == true) {
			settings = new Settings(getActivity());
			ipcAction newCommand[] = { ipcAction.OS, ipcAction.PROCESSOR, ipcAction.NETWORK };
			ipcService.addRequest(newCommand, 0, this);
		}
		
	}	

	@Override
	public void onRecvData(ipcMessage result) {
		
		// check 
		if(ipcStop == true)
			return;
		
		if (stopUpdate == true || result == null) {
			ipcAction newCommand[] = { ipcAction.OS , ipcAction.PROCESSOR, ipcAction.NETWORK };
			ipcService.addRequest(newCommand, settings.getInterval(), this);
			return;
		}
		
		coredata.clear();
		nwdata.clear();
		
		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);

				if(rawData.getAction() == ipcAction.OS)
					osdata = osInfo.parseFrom(rawData.getPayload(0));
							
				if(rawData.getAction() == ipcAction.PROCESSOR) {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						processorInfo prInfo = processorInfo.parseFrom(rawData.getPayload(count));
						coredata.add(prInfo);
					}
				}
				
				if(rawData.getAction() == ipcAction.NETWORK) {
					// process processInfo
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						networkInfo nwInfo = networkInfo.parseFrom(rawData.getPayload(count));
						
						if(nwInfo.getIpv4Addr().length() > 0 ||
						   nwInfo.getIpv6Addr().length() > 0 ||
						   nwInfo.getMac().length() > 0)
							nwdata.add(nwInfo);
					}					
				}
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// refresh
		misckAdapter.notifyDataSetChanged();

		// send command again
		ipcAction newCommand[] = { ipcAction.OS , ipcAction.PROCESSOR, ipcAction.NETWORK };
		ipcService.addRequest(newCommand, settings.getInterval(), this);
	}

    @SuppressLint("SetJavaScriptEnabled")
	void ShowHelp()
    {
    	CommonUtil.showHelp(getActivity(), "file:///android_asset/help/help-misc.html");
    }
}
