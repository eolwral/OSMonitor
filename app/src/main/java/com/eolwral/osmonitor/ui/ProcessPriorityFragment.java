package com.eolwral.osmonitor.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.ipcCategory;

public class ProcessPriorityFragment extends DialogFragment {

  // ipc client
  private static IpcService ipcService = IpcService.getInstance();

  // set pid
  public final static String TARGETPID = "TargetPID";
  public final static String TARGETNAME = "TargetName";
  public final static String DEFAULTPRIORITY = "DefaultPriority";
  private int targetPID = 0;
  private String targetName = "";
  private int defaultPriority = 0;
  private int targetPriority = 0;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // get pid
    targetPID = getArguments().getInt(TARGETPID);
    targetName = getArguments().getString(TARGETNAME);
    defaultPriority = getArguments().getInt(DEFAULTPRIORITY);
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.ui_process_priority, container, false);

    Button setButton = (Button) v.findViewById(R.id.id_process_priority_btn);
    setButton.setOnClickListener(new SetPriorityListener());

    Spinner prioritySpinner = (Spinner) v.findViewById(R.id.id_process_priority);
    prioritySpinner.setOnItemSelectedListener(new SelectPriorityListener());

    for (int index = 0; index < prioritySpinner.getCount(); index++) {
      if (Integer.parseInt(prioritySpinner.getItemAtPosition(index).toString()) == defaultPriority) {
        prioritySpinner.setSelection(index);
        break;
      }
    }

    getDialog().setTitle(targetName);

    return v;
  }

  private class SelectPriorityListener implements OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
        long id) {
      targetPriority = Integer.parseInt(parent.getItemAtPosition(position)
          .toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
  }

  private class SetPriorityListener implements OnClickListener {

    @Override
    public void onClick(View v) {
      ipcService.sendCommand(ipcCategory.SETPRIORITY, targetPID, targetPriority);
      ProcessPriorityFragment.this.dismiss();
    }

  }

}
