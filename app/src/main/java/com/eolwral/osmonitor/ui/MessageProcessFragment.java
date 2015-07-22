package com.eolwral.osmonitor.ui;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.processInfo;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

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

    detailName.setText(item.name());
    detailStime.setText(String.format("%,d", item.usedSystemTime()));
    detailUtime.setText(String.format("%,d", item.usedUserTime()));

    detailCPUtime.setText(String.format("%02d:%02d", item.cpuTime() / 60,
        item.cpuTime() % 60));

    detailThread.setText(String.format("%d", item.threadCount()));
    detailNice.setText(String.format("%d", item.priorityLevel()));

    // get memory information
    MemoryInfo memInfo = infoHelper.getMemoryInfo(item.pid());
    String memoryData = UserInterfaceUtil.convertToSize((item.rss() * 1024), true)
        + " /  " + UserInterfaceUtil.convertToSize(memInfo.getTotalPss() * 1024, true)
        + " / " + UserInterfaceUtil.convertToSize(memInfo.getTotalPrivateDirty() * 1024, true);

    detailMemory.setText(memoryData);

    detailPPID.setText("" + item.ppid());

    // convert time format
    final Calendar calendar = Calendar.getInstance();
    final DateFormat convertTool = DateFormat.getDateTimeInstance(
        DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
    calendar.setTimeInMillis(item.startTime() * 1000);
    detailStarttime.setText(convertTool.format(calendar.getTime()));

    detailUser.setText(item.owner());

    // convert status
    detailStatus.setText(UserInterfaceUtil.getSatusString(item.status()));
  }
}
