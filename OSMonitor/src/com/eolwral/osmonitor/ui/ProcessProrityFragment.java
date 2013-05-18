package com.eolwral.osmonitor.ui;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.ipc.IpcService;

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

public class ProcessProrityFragment extends DialogFragment {
	
	// ipc client
	private static IpcService ipcService = IpcService.getInstance();

	// set pid
	public final static String TARGETPID = "TargetPID";
	public final static String TARGETNAME = "TargetName";
	public final static String DEFAULTPRORITY = "DefaultPrority";
	private int targetPID = 0;
	private String targetName = "";
	private int defaultPrority = 0;
	private int targetPrority = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);   

		// get pid
		targetPID = getArguments().getInt(TARGETPID);
		targetName = getArguments().getString(TARGETNAME);
		defaultPrority = getArguments().getInt(DEFAULTPRORITY);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_process_prority, container, false);
		
		Button setButton = (Button) v.findViewById(R.id.id_process_prority_btn);
		setButton.setOnClickListener(new SetProrityListener());
		
		Spinner proritySpinner = (Spinner) v.findViewById(R.id.id_process_prority);
		proritySpinner.setOnItemSelectedListener(new SelectProrityListener());
		
		for (int index = 0; index < proritySpinner.getCount(); index++) {
			if(Integer.parseInt(proritySpinner.getItemAtPosition(index).toString()) == defaultPrority) {
				proritySpinner.setSelection(index);
				break;
			}
		}
		
		getDialog().setTitle(targetName);

		return v;  
	}
	
	private class SelectProrityListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			targetPrority = Integer.parseInt(parent.getItemAtPosition(position).toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	
	private class SetProrityListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			ipcService.setPrority(targetPID, targetPrority);
			ProcessProrityFragment.this.dismiss();
		}
			
	}

}
