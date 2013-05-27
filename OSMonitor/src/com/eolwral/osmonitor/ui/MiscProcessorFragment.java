package com.eolwral.osmonitor.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ProcessorInfo.processorInfo;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.util.Settings;

public class MiscProcessorFragment extends SherlockListFragment 
                                   implements ipcClientListener {

	private ArrayList<processorInfo> coredata = new ArrayList<processorInfo>();
	private boolean [] coreEnable = null;
	

	// ipc client
	private IpcService ipcService =  IpcService.getInstance();
	
	// working dialog
	private ProgressDialog ipcProcess = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new CPUListAdapter(getSherlockActivity().getApplicationContext()));
		
	}
	
	public void onStop() {
		super.onStop();
		for(int index = 0; index < coreEnable.length; index++) {
			if(coreEnable[index] == false)
				ipcService.setCPUStatus(index, 0);
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.ui_misc_item_processor_fragment, container, false);
		
		// enable fragment option menu 
		setHasOptionsMenu(false);

		ipcProcess = ProgressDialog.show(getSherlockActivity(), getResources().getString(R.string.ui_processor_enable_title),
									getResources().getString(R.string.ui_processor_enable_msg), true, true);
		
		ipcAction newCommand[] = { ipcAction.PROCESSOR };
		ipcService.addRequest(newCommand, 0, this);

		return v;
	}
	
	@Override
	public void onRecvData(ipcMessage result) {

		if(result == null) {
			ipcAction newCommand[] = { ipcAction.PROCESSOR };
			ipcService.addRequest(newCommand, 0, this);
		}
		
		coredata.clear();
		
		// convert data
		// TODO: reuse old objects
		for (int index = 0; index < result.getDataCount(); index++) {

			try {
				ipcData rawData = result.getData(index);
							
				if(rawData.getAction() == ipcAction.PROCESSOR) {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						processorInfo prInfo = processorInfo.parseFrom(rawData.getPayload(count));
						coredata.add(prInfo);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (coreEnable == null) {
			coreEnable = new boolean[coredata.size()];
			for(int index = 0; index < coredata.size(); index++)
				coreEnable[index] = !coredata.get(index).getOffLine();
		}

		boolean forceOnline = false;
		for (int index = 0; index < coredata.size(); index++) {
			if(coredata.get(index).getOffLine() == true) {
				ipcService.setCPUStatus(index, 0);
				ipcService.setCPUStatus(index, 1);
				forceOnline = true;
			}
			
			if(coredata.get(index).getAvaiableFrequeucy().equals("") ||
			   coredata.get(index).getAvaiableGovernors().equals("") ) {
			   
				if(forceOnline == false)
					coredata.remove(index);
			 }
		}
		
		if(forceOnline == false) {
			((CPUListAdapter) getListAdapter()).refresh();
			ipcProcess.dismiss(); 
		}
		else {
			ipcAction newCommand[] = { ipcAction.PROCESSOR };
			ipcService.addRequest(newCommand, 0, this);
		}
				
	}
	
	private class CPUListAdapter extends BaseAdapter {
    	
    	private LayoutInflater mInflater = null;
        private Context mContext = null;
        
        public CPUListAdapter(Context context)
        {
            mContext = context;
        	mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  

        }

        public int getCount() {
            return coredata.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	View sv = null;

            if (convertView == null) {
            	sv = (View) mInflater.inflate(R.layout.ui_misc_item_processor_detail, parent, false);
            	
            	final CheckBox enableBox = (CheckBox) sv.findViewById(R.id.id_processor_enable);
        		final Spinner MaxSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_max_value);
        		final Spinner MinSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_min_value);
        		final Spinner GovSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_gov_value);
        		
                final TextView MaxSeekBarValue = (TextView)sv.findViewById(R.id.id_processor_freq_max_title);
                final TextView MinSeekBarValue = (TextView)sv.findViewById(R.id.id_processor_freq_min_title);

                enableBox.setChecked(coreEnable[position]);
                
            	String [] CPUFreqList = coredata.get(position).getAvaiableFrequeucy().trim().split(" ");
        		ArrayAdapter<String> FreqAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, CPUFreqList);
        		FreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		MaxSeekBar.setAdapter(FreqAdapter);
        		MinSeekBar.setAdapter(FreqAdapter);
        		
            	String [] CPUGovList = coredata.get(position).getAvaiableGovernors().trim().split(" ");
        		ArrayAdapter<String> GovAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, CPUGovList);
        		GovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		GovSeekBar.setAdapter(GovAdapter);

        		((TextView)sv.findViewById(R.id.id_processor_title)).setText(
        				mContext.getResources().getString(R.string.ui_processor_core)+" "+coredata.get(position).getNumber());

        		for(int i = 0; i < CPUGovList.length;i++)
        			if(CPUGovList[i].equals(coredata.get(position).getGrovernors()))
        				GovSeekBar.setSelection(i);
        		
        		MaxSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_max_title)+" "+coredata.get(position).getMaxScaling());            
        		for(int i = 0; i < CPUFreqList.length;i++)
        			if(coredata.get(position).getMaxScaling() == Integer.parseInt(CPUFreqList[i]))
        				MaxSeekBar.setSelection(i);
        		
        		MinSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_min_title)+" "+coredata.get(position).getMinScaling());            
        		for(int i = 0; i < CPUFreqList.length;i++)
        			if(coredata.get(position).getMinFrequency() == Integer.parseInt(CPUFreqList[i]))
        				MinSeekBar.setSelection(i);
                
                final Settings setting = new Settings(mContext);
        		if(setting.isRoot())
        		{
        			((Spinner)sv.findViewById(R.id.id_processor_detail_max_value)).setClickable(true);
        			((Spinner)sv.findViewById(R.id.id_processor_detail_min_value)).setClickable(true);
        			((Spinner)sv.findViewById(R.id.id_processor_detail_gov_value)).setClickable(true);
        		}
        		else
        		{
        			((Spinner)sv.findViewById(R.id.id_processor_detail_max_value)).setClickable(false);
        			((Spinner)sv.findViewById(R.id.id_processor_detail_min_value)).setClickable(false);
        			((Spinner)sv.findViewById(R.id.id_processor_detail_gov_value)).setClickable(false);
        		}
     
                if(position % 2 == 1)
    	     		sv.setBackgroundColor(0x80444444);
    	     	else
    	     		sv.setBackgroundColor(0x80000000);          

        		enableBox.setTag(""+coredata.get(position).getNumber());
                enableBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						int CPUNum = Integer.parseInt((String)buttonView.getTag());
						
						// prevent to disable CPU0
						if (CPUNum == 0 && isChecked == false) {
							buttonView.setChecked(true);
							return;
						}
							
						// change CPU status
						if(isChecked)
							ipcService.setCPUStatus(CPUNum, 1);
						else
							ipcService.setCPUStatus(CPUNum, 0);
						
						coreEnable[CPUNum] = isChecked;
					}
                });
                
                GovSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        				int CPUNum = Integer.parseInt((String) ((View)parentView.getParent()).getTag());
        				String selected = parentView.getItemAtPosition(position).toString();
        				ipcService.setCPUGov(CPUNum, selected);
        			}
        			public void onNothingSelected(AdapterView<?> parentView) { }

				});
        		
                MaxSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

        				if(MaxSeekBar.getSelectedItemPosition() < MinSeekBar.getSelectedItemPosition()) {
        					MaxSeekBar.setSelection(MinSeekBar.getSelectedItemPosition());
        					position = MaxSeekBar.getSelectedItemPosition();
        				}

        				int CPUNum = Integer.parseInt((String) ((View)parentView.getParent()).getTag());
       				    String [] CPUFreqList = coredata.get(CPUNum).getAvaiableFrequeucy().trim().split(" ");
        				
        				MaxSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_max_title)
        						   						+" "+CPUFreqList[MaxSeekBar.getSelectedItemPosition()]);

        				ipcService.setCPUStatus(CPUNum, 1);
        				ipcService.setCPUMaxFreq(CPUNum, Long.parseLong(CPUFreqList[MaxSeekBar.getSelectedItemPosition()]));
        			}
        			public void onNothingSelected(AdapterView<?> parentView) { }

				});

                MinSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        				
     				    if(MaxSeekBar.getSelectedItemPosition() < MinSeekBar.getSelectedItemPosition()) {
        					MinSeekBar.setSelection(MaxSeekBar.getSelectedItemPosition());
        					position = MinSeekBar.getSelectedItemPosition();
        				}

     				    int CPUNum = Integer.parseInt((String) ((View) MinSeekBar.getParent()).getTag());
       				    String [] CPUFreqList = coredata.get(CPUNum).getAvaiableFrequeucy().trim().split(" ");
        				
        				MinSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_min_title)
        						   						+" "+CPUFreqList[MinSeekBar.getSelectedItemPosition()]);

        				ipcService.setCPUStatus(CPUNum, 1);
        				ipcService.setCPUMinFreq(CPUNum, Long.parseLong(CPUFreqList[MinSeekBar.getSelectedItemPosition()]));
        			}
        			public void onNothingSelected(AdapterView<?> parentView) { }

				});

        		sv.setTag(""+coredata.get(position).getNumber());
            } else {
            	sv = convertView;
            }
            return sv;
        }
        
        public void refresh() {
        	notifyDataSetChanged();
        }

    }


}
