package com.eolwral.osmonitor.ui;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ConnectionInfo.connectionInfo;
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
import com.eolwral.osmonitor.util.WhoisUtil;
import com.eolwral.osmonitor.util.WhoisUtilDataSet;

public class ConnectionFragment extends SherlockListFragment 
                                implements ipcClientListener {

	// ipc client
	private IpcService ipcService = IpcService.getInstance();;
	private boolean ipcStop = false;

	// data
	private ArrayList<connectionInfo> data = new ArrayList<connectionInfo>();
	private Settings settings = null;

	private ProcessUtil infoHelper = null;

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, String> map = new HashMap<Integer, String>();

	// stop or start
	private boolean stopUpdate = false;
	private MenuItem stopButton = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		infoHelper = ProcessUtil.getInstance(getSherlockActivity().getApplicationContext(), true);
		setListAdapter(new ConnectionListAdapter(getSherlockActivity().getApplicationContext()));
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_connection_fragment, container, false);
 
		// enable fragment option menu 
		setHasOptionsMenu(true);
 
		return v;
	}
	
	@Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.ui_connection_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
		
		MenuItem helpMenu = menu.findItem(R.id.ui_menu_help);
		helpMenu.setOnMenuItemClickListener( new HelpMenuClickListener());
		
		MenuItem settingMenu = menu.findItem(R.id.ui_menu_setting);
		settingMenu.setOnMenuItemClickListener( new SettingMenuClickListener());
		
		MenuItem exitMenu = menu.findItem(R.id.ui_menu_exit);
		exitMenu.setOnMenuItemClickListener(new ExitMenuClickListener());

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
			settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
 
	@SuppressLint("NewApi")
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		// bypass 0.0.0.0
		String QueryIP = data.get(position).getRemoteIP().replace("::ffff:", "");
		if (QueryIP.equals("0.0.0.0"))
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			new QueryWhois(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, QueryIP);
		else
			new QueryWhois(getActivity()).execute(QueryIP);
	};

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

		ipcService.removeRequest(this);
		ipcStop = !isVisibleToUser;

		if(isVisibleToUser == true) {
			settings = new Settings(getActivity());
			ipcAction newCommand[] = { ipcAction.CONNECTION, ipcAction.PROCESS };
			ipcService.addRequest(newCommand, 0, this);
		}
		
	}	
	
	@Override
	public void onRecvData(ipcMessage result) {
		
		// check 
		if(ipcStop == true)
			return;
		
		if (stopUpdate == true || result == null) {
			ipcAction newCommand[] = { ipcAction.CONNECTION, ipcAction.PROCESS };
			ipcService.addRequest(newCommand, settings.getInterval(), this);
			return;
		}
		
		// clean up
		while (!data.isEmpty())
			data.remove(0);
		data.clear();

		map.clear();

		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);

				// prepare mapping table
				if(rawData.getAction() == ipcAction.PROCESS)
				{
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						processInfo psInfo = processInfo.parseFrom(rawData.getPayload(count));
						if (!infoHelper.checkPackageInformation(psInfo.getName())) {
							infoHelper.doCacheInfo(psInfo.getUid(), psInfo.getOwner(), psInfo.getName());
						}
						map.put(psInfo.getUid(), infoHelper.getPackageName(psInfo.getName()));
					}
				}
				
				if(rawData.getAction() != ipcAction.CONNECTION)
					continue;

				// process processInfo
				for (int count = 0; count < rawData.getPayloadCount(); count++) {
					connectionInfo cnInfo = connectionInfo.parseFrom(rawData.getPayload(count));
					data.add(cnInfo);
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		((ConnectionListAdapter) getListAdapter()).refresh();

		// send command again
		ipcAction newCommand[] = { ipcAction.CONNECTION, ipcAction.PROCESS };
		ipcService.addRequest(newCommand, settings.getInterval(), this);
	}
	
	/**
	 * implement viewholder class for connection list
	 */
	private class ViewHolder {
		// main information
		TextView type;
		TextView src;
		TextView dst;
		TextView owner;
		TextView status;
	}	
	
	private class ConnectionListAdapter extends BaseAdapter {

		private LayoutInflater itemInflater = null;
		private ViewHolder holder = null;

		public ConnectionListAdapter(Context mContext) {
			itemInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			connectionInfo item = data.get(position);

			// prepare view
			if (convertView == null) {

				sv = (View) itemInflater.inflate(R.layout.ui_connection_item, parent, false);

				holder = new ViewHolder();
				holder.type = ((TextView) sv.findViewById(R.id.id_connection_type));
				holder.src = ((TextView) sv.findViewById(R.id.id_connection_src));
				holder.dst = ((TextView) sv.findViewById(R.id.id_connection_dst));
				holder.owner = ((TextView) sv.findViewById(R.id.id_connection_owner));
				holder.status = ((TextView) sv.findViewById(R.id.id_connection_status));

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

			// prepare main information
			switch (item.getType().getNumber()) {
			case connectionInfo.connectionType.TCPv4_VALUE:
				holder.type.setText("TCP4");
				break;
			case connectionInfo.connectionType.TCPv6_VALUE:
				holder.type.setText("TCP6");
				break;
			case connectionInfo.connectionType.UDPv4_VALUE:
				holder.type.setText("UDP4");
				break;
			case connectionInfo.connectionType.UDPv6_VALUE:
				holder.type.setText("UDP6");
				break;
			case connectionInfo.connectionType.RAWv4_VALUE:
				holder.type.setText("RAW4");
				break;
			case connectionInfo.connectionType.RAWv6_VALUE:
				holder.type.setText("RAW6");
				break;
			}
			
			holder.src.setText(convertFormat(item.getLocalIP(), item.getLocalPort()));
			holder.dst.setText(convertFormat(item.getRemoteIP(), item.getRemotePort()));
			
			switch(item.getStatus().getNumber())
			{
			case connectionInfo.connectionStatus.CLOSE_VALUE:
				holder.status.setText("CLOSE");
				break;
			case connectionInfo.connectionStatus.CLOSE_WAIT_VALUE:
				holder.status.setText("CLOSE_WAIT");
				break;
			case connectionInfo.connectionStatus.CLOSING_VALUE:
				holder.status.setText("CLOSING");
				break;
			case connectionInfo.connectionStatus.ESTABLISHED_VALUE:
				holder.status.setText("ESTABLISHED");
				break;
			case connectionInfo.connectionStatus.FIN_WAIT1_VALUE:
				holder.status.setText("FIN_WAIT1");
				break;
			case connectionInfo.connectionStatus.FIN_WAIT2_VALUE:
				holder.status.setText("FIN_WAIT2");
				break;
			case connectionInfo.connectionStatus.LAST_ACK_VALUE:
				holder.status.setText("LAST_ACK");
				break;
			case connectionInfo.connectionStatus.LISTEN_VALUE:
				holder.status.setText("LISTEN");
				break;
			case connectionInfo.connectionStatus.SYN_RECV_VALUE:
				holder.status.setText("SYN_RECV");
				break;
			case connectionInfo.connectionStatus.SYN_SENT_VALUE:
				holder.status.setText("SYN_SENT");
				break;
			case connectionInfo.connectionStatus.TIME_WAIT_VALUE:
				holder.status.setText("TIME_WAIT");
				break;
			case connectionInfo.connectionStatus.UNKNOWN_VALUE:
				holder.status.setText("UNKNOWN");
				break;
			}
			
			if(item.getUid() == 0)
				holder.owner.setText("System");
			else if(map.containsKey(item.getUid()))
				holder.owner.setText(map.get(item.getUid()));
			else
				holder.owner.setText(item.getUid()+"(UID)");
			
			return sv;
		}
		
		private String convertFormat(String ip, int port) {
			
			// replace IPv6 to IPv4
			ip = ip.replace("::ffff:", "");

			if(port == 0)
				return ip+":*";
			return ip+":"+port;
			
		}
		
		public void refresh() {
			this.notifyDataSetChanged();
		}
		
	}
	
    class CacheQuery 
    {
    	public String Msg;
    	public float Longtiude;
        public float Latitude; 
    }
    
    private final HashMap<String, CacheQuery> CacheWhois = new HashMap<String, CacheQuery>();
	class QueryWhois extends AsyncTask<String, Integer, CacheQuery>
	{
		private Context mContext = null;
		private ProgressDialog ProcDialog = null;
		private boolean forceStop = false;

		
		public QueryWhois(Context mContext) {
		     this.mContext = mContext;
		}

		protected void onPreExecute() {
	    	super.onPreExecute();

	    	// show progress dialog
			ProcDialog = ProgressDialog.show(mContext, "", 
					  getSherlockActivity().getResources().getText(R.string.ui_text_refresh), true);
			ProcDialog.setOnCancelListener( new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					forceStop = true;
				}
			});
			ProcDialog.setCancelable(true);
		}
		
	    protected void onPostExecute(CacheQuery result) {
	    	ProcDialog.dismiss();
	    	
	    	// stopped by user
	    	if(forceStop == true)
	    		return;
	    	
	    	// Replace One Fragment with Another
	    	// http://developer.android.com/training/basics/fragments/fragment-ui.html#Replace
	    	
	    	// pass information
	    	ConnectionMapFragment newMap = new ConnectionMapFragment();
	    	Bundle args = new Bundle();
	    	args.putFloat(ConnectionMapFragment.LONGTIUDE, result.Longtiude);
	    	args.putFloat(ConnectionMapFragment.LATITUDE, result.Latitude);
	    	args.putString(ConnectionMapFragment.MESSAGE, result.Msg);
	    	newMap.setArguments(args);
	    	
	    	// replace current fragment
	    	final FragmentManager fragmanger = getSherlockActivity().getSupportFragmentManager();
	    	final FragmentTransaction transaction = fragmanger.beginTransaction();
	    	transaction.replace(R.id.ui_connection_layout, newMap, "WHOIS");
	    	transaction.addToBackStack(null);
	    	transaction.commit();
	    	
	    }
		
		@Override
		protected CacheQuery doInBackground(String... params) {
			
			String QueryIP = params[0];
			if(CacheWhois.get(QueryIP) != null)
				return CacheWhois.get(QueryIP);

			StringBuilder whoisInfo = new StringBuilder();
	        CacheQuery WhoisQuery = new CacheQuery();
	        
	        // nslookup 
	        String HostName = QueryIP;
			try {
				HostName = InetAddress.getByName(QueryIP).getHostName();
			} catch (UnknownHostException e) { }
	        
			// detect if it is belong to our API's IP
	        if(HostName.contains("utrace.de"))
	        {
	        	WhoisQuery.Msg = "<b>WHOIS API</b><br/>"+
	                             "http://en.utrace.de/api.php";
	        	WhoisQuery.Latitude = (float) 51.165691;
	        	WhoisQuery.Longtiude = (float) 10.451526;
	        	return WhoisQuery;
	        }
	        
	        // execute whois query
			try {
	            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	            WhoisUtil SAXHandler = new WhoisUtil();

	            URL url = new URL("http://xml.utrace.de/?query="+QueryIP);
	            
	            InputStream urlData = url.openStream();
	            xmlReader.setContentHandler(SAXHandler);
	            xmlReader.parse(new InputSource(urlData));
	            urlData.close();

	            WhoisUtilDataSet parsedDataSet = SAXHandler.getParsedData();

	            whoisInfo.append(parsedDataSet.toString());

	            String WhoisMsg = whoisInfo.toString();

	            WhoisMsg = "<b>DNS:</b> "+HostName+"<br/>" + WhoisMsg;

				WhoisQuery.Msg = WhoisMsg;
				WhoisQuery.Longtiude = parsedDataSet.getMapLongtiude();
				WhoisQuery.Latitude = parsedDataSet.getMapnLatitude();
		        CacheWhois.put(QueryIP, WhoisQuery);
	        } 
			catch (Exception e) 
	        {
				WhoisQuery.Msg = "Query failed!";
	        }  

			return WhoisQuery;
		}
    }
	
    @SuppressLint("SetJavaScriptEnabled")
	void ShowHelp()
    {
    	CommonUtil.showHelp(getActivity(), "file:///android_asset/help/help-connection.html");
    }
}
