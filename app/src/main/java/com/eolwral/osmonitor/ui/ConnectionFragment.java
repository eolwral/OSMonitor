package com.eolwral.osmonitor.ui;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.connectionInfo;
import com.eolwral.osmonitor.core.connectionInfoList;
import com.eolwral.osmonitor.core.processInfo;
import com.eolwral.osmonitor.core.processInfoList;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.preference.Preference;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.HttpUtil;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

public class ConnectionFragment extends ListFragment implements
    ipcClientListener {

  // ipc client
  private IpcService ipcService = IpcService.getInstance();
  private boolean ipcStop = false;

  // data
  private ArrayList<connectionInfo> data = new ArrayList<connectionInfo>();
  private Settings settings = null;

  private ProcessUtil infoHelper = null;

  @SuppressLint("UseSparseArrays")
  private SimpleArrayMap<Integer, String> map = new SimpleArrayMap<Integer, String>();

  // tablet
  private boolean tabletLayout = false;
  private Fragment previousMap = null;
  private ProgressDialog procDialog = null;

  // stop or start
  private boolean stopUpdate = false;
  private MenuItem stopButton = null;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    settings = Settings.getInstance(getActivity().getApplicationContext());
    infoHelper = ProcessUtil.getInstance(getActivity().getApplicationContext(),
        true);
    setListAdapter(new ConnectionListAdapter(getActivity()
        .getApplicationContext()));
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View v = inflater
        .inflate(R.layout.ui_connection_fragment, container, false);

    // detect layout
    if (v.findViewById(R.id.ui_connection_map) != null)
      tabletLayout = true;
    else
      tabletLayout = false;

    // enable fragment option menu
    setHasOptionsMenu(true);

    return v;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.ui_connection_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);

    // refresh button
    stopButton = (MenuItem) menu.findItem(R.id.ui_menu_stop);

    if (stopUpdate)
      stopButton.setIcon(R.drawable.ic_action_start);
    else
      stopButton.setIcon(R.drawable.ic_action_stop);

    return;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.ui_menu_setting:
      onSettingClick();
      break;
    case R.id.ui_menu_stop:
      onStopClick(item);
      break;
    case R.id.ui_menu_exit:
      onExitClick();
      break;
    case R.id.ui_connection_export:
      onExportClick();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onExportClick() {

    final Resources exportRes = getActivity().getResources();
    final Calendar calendar = Calendar.getInstance();
    final SimpleDateFormat formatter = new SimpleDateFormat(
        "yyyy-MM-dd-hh.mm.ss", Locale.getDefault());

    Builder exportDialog = new AlertDialog.Builder(getActivity());
    View exportView = LayoutInflater.from(getActivity()).inflate(
        R.layout.ui_message_export, null);
    TextView exportFile = (TextView) exportView
        .findViewById(R.id.id_export_filename);
    exportFile.setText("Connection-" + formatter.format(calendar.getTime()));
    exportDialog.setView(exportView);

    exportDialog.setTitle(exportRes.getText(R.string.ui_menu_logexport));
    exportDialog.setNegativeButton(exportRes.getText(R.string.ui_text_cancel),
        null);

    exportDialog.setPositiveButton(exportRes.getText(R.string.ui_text_okay),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            String FileName = ((EditText) ((AlertDialog) dialog)
                .findViewById(R.id.id_export_filename)).getText().toString();
            exportLog(FileName);
          }
        });

    exportDialog.create().show();
    return;
  }

  private void exportLog(String fileName) {

    if (fileName.trim().equals(""))
      return;

    if (!fileName.contains(".csv"))
      fileName += ".csv";

    try {

      File logFile = new File(Environment.getExternalStorageDirectory()
          .getPath() + "/" + fileName);

      if (logFile.exists()) {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.ui_menu_logexport)
            .setMessage(R.string.ui_text_fileexist)
            .setPositiveButton(R.string.ui_text_okay,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
                }).create().show();
        return;
      }

      logFile.createNewFile();

      FileWriter logWriter = new FileWriter(logFile);

      for (int index = 0; index < data.size(); index++) {

        StringBuilder logLine = new StringBuilder();

        // item
        connectionInfo item = data.get(index);

        // prepare main information
        logLine.append(UserInterfaceUtil.getConnectionType(item.type()));
        logLine.append(",");

        logLine.append(UserInterfaceUtil.convertToIPv4(item.localIP(), item.localPort()));
        logLine.append(",");

        logLine.append(UserInterfaceUtil.convertToIPv4(item.remoteIP(), item.remotePort()));
        logLine.append(",");

        logLine.append(UserInterfaceUtil.getConnectionStatus(item.status()));
        logLine.append(",");

        if (item.uid() == 0)
          logLine.append("System");
        else if (map.containsKey(item.uid()))
          logLine.append(infoHelper.getPackageName(map.get(item.uid())));
        else
          logLine.append(item.uid() + "(UID)");
        logLine.append("\n");

        logWriter.write(logLine.toString());
      }

      logWriter.close();
    } catch (Exception e) {
      new AlertDialog.Builder(getActivity())
          .setTitle(R.string.ui_menu_logexport)
          .setMessage(e.getMessage())
          .setPositiveButton(R.string.ui_text_okay,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
              }).create().show();

      return;
    }

    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.ui_menu_logexport)
        .setMessage(R.string.ui_text_exportdone)
        .setPositiveButton(R.string.ui_text_okay,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
              }
            }).create().show();

  }

  private void onStopClick(MenuItem stopButton) {
    stopUpdate = !stopUpdate;

    if (stopUpdate)
      stopButton.setIcon(R.drawable.ic_action_start);
    else
      stopButton.setIcon(R.drawable.ic_action_stop);
    return;
  }

  private void onExitClick() {
    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
        new Intent("Exit"));
    return;
  }

  private void onSettingClick() {
    Intent settings = new Intent(getActivity(), Preference.class);
    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(settings);
    return;
  }

  @SuppressLint("NewApi")
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {

    // detect local IP address via InetAddress 
    try {
      InetAddress ia = InetAddress.getByName(data.get(position).remoteIP());
      if (ia.isAnyLocalAddress() || ia.isSiteLocalAddress() || ia.isLoopbackAddress() )
        return;
    } catch (Exception e) { }

    // execute WHOIS procedure 
    String QueryIP = data.get(position).remoteIP().replace("::ffff:", "");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      new PrepareQuery().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, QueryIP);
    else
      new PrepareQuery().execute(QueryIP);
  };

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);

    ipcService.removeRequest(this);
    ipcStop = !isVisibleToUser;

    if (isVisibleToUser == true) {
      byte newCommand[] = { ipcCategory.CONNECTION, ipcCategory.PROCESS };
      ipcService.addRequest(newCommand, 0, this);
    }

  }

  @Override
  public void onRecvData(byte [] result) {

    // check
    if (ipcStop == true)
      return;

    if (stopUpdate == true || result == null) {
      byte newCommand[] = { ipcCategory.CONNECTION, ipcCategory.PROCESS };
      ipcService.addRequest(newCommand, settings.getInterval(), this);
      return;
    }

    // clean up
    while (!data.isEmpty())
      data.remove(0);
    data.clear();

    map.clear();

    // convert data
    ipcMessage resultMessage = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));
    try {
      for (int index = 0; index < resultMessage.dataLength(); index++) {

        ipcData rawData = resultMessage.data(index);

        // prepare mapping table
        if (rawData.category() == ipcCategory.PROCESS)
          extractProfessInfo(rawData);
        else if (rawData.category() == ipcCategory.CONNECTION)
          extractConnectionInfo(rawData);

      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ((ConnectionListAdapter) getListAdapter()).refresh();

    // send command again
    byte newCommand[] = { ipcCategory.CONNECTION, ipcCategory.PROCESS };
    ipcService.addRequest(newCommand, settings.getInterval(), this);
  }

  private void extractConnectionInfo(ipcData rawData)
  {
    // process connectionInfo
    connectionInfoList list = connectionInfoList.getRootAsconnectionInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < list.listLength(); count++) {
      connectionInfo cnInfo = list.list(count);
      data.add(cnInfo);
    }
  }

  private void extractProfessInfo(ipcData rawData)
  {
    processInfoList list = processInfoList.getRootAsprocessInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < list.listLength(); count++) {
      processInfo psInfo = list.list(count);
      if (!infoHelper.checkPackageInformation(psInfo.name())) {
        infoHelper.doCacheInfo(psInfo.uid(), psInfo.owner(), psInfo.name());
      }
      map.put(psInfo.uid(), psInfo.name());
    }
  }

  /**
   * implement viewholder class for connection list
   */
  private class ViewHolder {
    // main information
    ImageView icon;
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
      itemInflater = (LayoutInflater) mContext
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        sv = (View) itemInflater.inflate(R.layout.ui_connection_item, parent,
            false);

        holder = new ViewHolder();
        holder.icon = ((ImageView) sv.findViewById(R.id.id_connection_icon));
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

      // draw icon when screen is not small
      if (holder.icon != null) {
        if (item.uid() == 0)
          holder.icon.setImageDrawable(infoHelper.getDefaultIcon());
        else
          holder.icon.setImageDrawable(infoHelper.getPackageIcon(map.get(item.uid())));
      }

      // prepare main information
      holder.type.setText(UserInterfaceUtil.getConnectionType(item.type()));
      holder.src.setText(UserInterfaceUtil.convertToIPv4(item.localIP(), item.localPort()));
      holder.dst.setText(UserInterfaceUtil.convertToIPv4(item.remoteIP(), item.remotePort()));
      holder.status.setText(UserInterfaceUtil.getConnectionStatus(item.status()));

      if (item.uid() == 0)
        holder.owner.setText("System");
      else if (map.containsKey(item.uid()))
        holder.owner.setText(infoHelper.getPackageName(map.get(item.uid())));
      else
        holder.owner.setText(item.uid() + "(UID)");

      return sv;
    }

    public void refresh() {
      this.notifyDataSetChanged();
    }

  }

  private void showLoading() {
    getActivity().runOnUiThread(new Runnable() {
      public void run() {
        // show progress dialog
        procDialog = ProgressDialog.show(getActivity(), "", getActivity()
            .getResources().getText(R.string.ui_text_refresh), true);

        procDialog.setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            HttpUtil.getInstance(getActivity().getApplicationContext())
                .cancelRequest();
          }
        });
        procDialog.setCancelable(true);
      }
    });
  }

  private void closeLoading() {

    // because activity may be destroyed by system, we need check it before
    // using.
    // Fix: java.lang.NullPointerException
    if (getActivity() == null)
      return;

    getActivity().runOnUiThread(new Runnable() {
      public void run() {
        if (procDialog != null)
          procDialog.dismiss();
        procDialog = null;
      }
    });
  }

  private void showMap(CacheQuery result) {
    // Replace One Fragment with Another
    // http://developer.android.com/training/basics/fragments/fragment-ui.html#Replace

    // pass information
    ConnectionStaticMapFragment newMap = new ConnectionStaticMapFragment();
    Bundle args = new Bundle();
    args.putFloat(ConnectionStaticMapFragment.LONGTIUDE, result.Longtiude);
    args.putFloat(ConnectionStaticMapFragment.LATITUDE, result.Latitude);
    args.putString(ConnectionStaticMapFragment.MESSAGE, result.Msg);
    newMap.setArguments(args);

    // if Activity isn't attached, just skip
    if (getActivity() == null)
      return;

    final FragmentManager fm = getActivity().getSupportFragmentManager();
    final FragmentTransaction transaction = fm.beginTransaction();

    if (tabletLayout) {
      // push current fragment
      transaction.replace(R.id.ui_connection_map, newMap, "WHOIS");
      transaction.commitAllowingStateLoss();
      previousMap = newMap;
    } else {
      // replace current fragment
      transaction.replace(R.id.ui_connection_layout, newMap, "WHOIS");
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
    }
  }

  private void cleanUp() {
    final FragmentTransaction transaction = getActivity()
        .getSupportFragmentManager().beginTransaction();
    if (previousMap != null) {
      transaction.remove(previousMap).commit();
      previousMap = null;
    }
  }

  class CacheQuery {
    public String Msg;
    public float Longtiude;
    public float Latitude;
  }

  private final SimpleArrayMap<String, CacheQuery> CacheWhois = new SimpleArrayMap<String, CacheQuery>();

  class PrepareQuery extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
      String QueryIP = params[0];
      if (QueryIP != null)
        new QueryWhois(QueryIP);
      return null;
    }

  }

  class QueryWhois {

    public QueryWhois(String QueryIP) {

      // if data cached, showMap directly
      if (CacheWhois.get(QueryIP) != null) {
        showMap(CacheWhois.get(QueryIP));
        return;
      }

      // clean up
      cleanUp();

      // show prepare dialog
      showLoading();

      String URL = "https://osmonitor.mobi/WhoisAPI?IP=" + QueryIP;

      JsonObjectRequest queryRequest = new JsonObjectRequest(
          Request.Method.GET, URL, null, new Response(QueryIP),
          new ResponseError());

      HttpUtil.getInstance(getActivity().getApplicationContext()).addRequest(
          queryRequest);
    }

    private String getHostName(String QueryIP) {
      // nslookup
      String HostName = QueryIP;
      try {
        HostName = InetAddress.getByName(QueryIP).getHostName();
      } catch (UnknownHostException e) {
      }

      return HostName;
    }

    private class Response implements Listener<JSONObject> {

      private String QueryIP;
      private String HostName;

      public Response(String QueryIP) {
        this.QueryIP = QueryIP;
        this.HostName = getHostName(QueryIP);
      }

      @Override
      public void onResponse(JSONObject response) {
        CacheQuery WhoisQuery = new CacheQuery();
        StringBuilder whoisInfo = new StringBuilder();

        whoisInfo.append("<b>DNS:</b> " + HostName + "<br/>");
        whoisInfo.append("<b>IP:</b> " + QueryIP + "<br/>");

        try {
          whoisInfo.append("<b>Country:</b> " + response.getString("country") + "<br/>");
          whoisInfo.append("<b>Region:</b> " + response.getString("regionName") + "<br/>");
          whoisInfo.append("<b>City:</b> " + response.getString("city") + "<br/>");
          whoisInfo.append("<b>ISP:</b> " + response.getString("isp") + "<br/>");
          whoisInfo.append("<b>Org:</b> " + response.getString("org") + "<br/>");
          whoisInfo.append("<b>Latitude:</b> " + response.getString("lat") + "<br/>");
          whoisInfo.append("<b>Longitude:</b> " + response.getString("lon"));

          WhoisQuery.Longtiude = (float) response.getDouble("lon");
          WhoisQuery.Latitude = (float) response.getDouble("lat");

        } catch (JSONException e) {
        }

        WhoisQuery.Msg = whoisInfo.toString();

        CacheWhois.put(QueryIP, WhoisQuery);

        closeLoading();

        showMap(WhoisQuery);
      }
    }

    private class ResponseError implements ErrorListener {
      @Override
      public void onErrorResponse(VolleyError error) {
        closeLoading();
      }
    }
  }
}
