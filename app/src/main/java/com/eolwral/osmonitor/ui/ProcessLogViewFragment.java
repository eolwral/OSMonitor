package com.eolwral.osmonitor.ui;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
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
import com.eolwral.osmonitor.core.logPriority;
import com.eolwral.osmonitor.core.logcatInfo;
import com.eolwral.osmonitor.core.logcatInfoList;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

public class ProcessLogViewFragment extends DialogFragment implements
    ipcClientListener {

  // ipc client
  private static IpcService ipcService = IpcService.getInstance();

  // set pid
  public final static String TARGETPID = "TargetPID";
  public final static String TARGETNAME = "TargetName";
  private int targetPID = 0;
  private String targetName = "";

  // data
  private ArrayList<logcatInfo> viewLogcatData = new ArrayList<logcatInfo>();
  private byte logType = ipcCategory.LOGCAT_MAIN_R;

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
        final SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd-hh.mm.ss", Locale.getDefault());

        Builder exportDialog = new AlertDialog.Builder(getActivity());
        View exportView = LayoutInflater.from(getActivity()).inflate(
            R.layout.ui_message_export, null);
        TextView exportFile = (TextView) exportView
            .findViewById(R.id.id_export_filename);
        exportFile.setText("Log-" + formatter.format(calendar.getTime()));
        exportDialog.setView(exportView);

        exportDialog.setTitle(exportRes.getText(R.string.ui_menu_logexport));
        exportDialog.setNegativeButton(
            exportRes.getText(R.string.ui_text_cancel), null);

        exportDialog.setPositiveButton(
            exportRes.getText(R.string.ui_text_okay),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                String FileName = ((EditText) ((AlertDialog) dialog)
                    .findViewById(R.id.id_export_filename)).getText()
                    .toString();
                exportLog(FileName);
              }
            });

        exportDialog.create().show();
      }
    });

    return v;
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

      int LogCount = viewLogcatData.size();

      FileWriter logWriter = new FileWriter(logFile);

      final Calendar calendar = Calendar.getInstance();

      for (int index = 0; index < LogCount; index++) {
        StringBuilder logLine = new StringBuilder();

        calendar.setTimeInMillis(viewLogcatData.get(index).seconds() * 1000);

        logLine.append(DateFormat.format("yyyy-MM-dd hh:mm:ss",
            calendar.getTime())
            + ",");

        switch (viewLogcatData.get(index).priority()) {
        case logPriority.SILENT:
          logLine.append("SILENT,");
          break;
        case logPriority.UNKNOWN:
          logLine.append("UNKNOWN,");
          break;
        case logPriority.DEFAULT:
          logLine.append("DEFAULT,");
          break;
        case logPriority.VERBOSE:
          logLine.append("VERBOSE,");
          break;
        case logPriority.WARN:
          logLine.append("WARNING,");
          break;
        case logPriority.INFO:
          logLine.append("INFORMATION,");
          break;
        case logPriority.FATAL:
          logLine.append("FATAL,");
          break;
        case logPriority.ERROR:
          logLine.append("ERROR,");
          break;
        case logPriority.DEBUG:
          logLine.append("DEBUG,");
          break;
        }
        logLine.append(viewLogcatData.get(index).tag() + ",");
        logLine.append(viewLogcatData.get(index).message() + "\n");

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

    return;
  }

  @Override
  public void onStart() {
    super.onStart();

    ipcService.removeRequest(this);
    byte newCommand[] = { logType };
    ipcService.addRequest(newCommand, 0, this);
  }

  @Override
  public void onStop() {
    super.onStop();

    ipcService.removeRequest(this);
  }

  @Override
  public void onRecvData(byte [] result) {

    if (result == null) {
      byte newCommand[] = new byte[1];
      newCommand[0] = logType;
      ipcService.addRequest(newCommand, settings.getInterval(), this);
      return;
    }

    // clean up
    viewLogcatData.clear();

    // convert data
    ipcMessage ipcMessageResult = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));
    for (int index = 0; index < ipcMessageResult.dataLength(); index++) {

      try {
        ipcData rawData = ipcMessageResult.data(index);

        logcatInfoList list = logcatInfoList.getRootAslogcatInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
        for (int count = 0; count < list.listLength(); count++) {
          logcatInfo lgInfo = list.list(count);

          // filter
          if (lgInfo.pid() != this.targetPID)
            continue;

          viewLogcatData.add(lgInfo);
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // refresh
    messageList.refresh();

    // send command again
    byte newCommand[] = new byte[1];
    newCommand[0] = logType;
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
      itemInflater = (LayoutInflater) mContext
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        sv = (View) itemInflater.inflate(R.layout.ui_message_item, parent,
            false);

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
      final java.text.DateFormat convertTool = java.text.DateFormat
          .getDateTimeInstance();
      calendar.setTimeInMillis(item.seconds() * 1000);
      holder.time.setText(convertTool.format(calendar.getTime()));

      holder.tag.setText(item.tag());

      holder.msg.setText(item.message().toString());

      holder.level.setTextColor(Color.BLACK);

      holder.level.setBackgroundColor(UserInterfaceUtil.getLogcatColor(item.priority()));
      holder.level.setText(UserInterfaceUtil.getLogcatTag(item.priority()));

      // avoid to trigger errors when refreshing
      sv.setOnTouchListener(new OnTouchListener() {

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
