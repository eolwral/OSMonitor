package com.eolwral.osmonitor.preference;

import java.util.ArrayList;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ProcessorInfo.processorInfo;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.Settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ProcessorPreference extends DialogPreference 
								 implements ipcClientListener {
	
	private ArrayList<processorInfo> coredata = new ArrayList<processorInfo>();
	private boolean [] coreEnable = null;
	
	private class processorConfig {
		public boolean enable = true;
		public long maxFreq = 0;
		public long minFreq = 0;
		public String gov = ""; 
	}
	
	private ArrayList<processorConfig> setdata = new ArrayList<processorConfig>(); 

	// ipc client
	private IpcService ipcService =  IpcService.getInstance();
	
	// working dialog
	private ListView cpuList = null;
	private LinearLayout loadingText = null;
	
	public ProcessorPreference(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    setDialogLayoutResource(R.layout.ui_misc_item_processor_fragment);
	}
	
	@Override
	protected void onBindDialogView(View view) {

		cpuList = (ListView) view.findViewById(android.R.id.list);
		loadingText = (LinearLayout) view.findViewById(R.id.id_processor_data_loading);
	    coredata.clear();
		
	    super.onBindDialogView(view);
	    
	    cpuList.setAdapter(new ProcessorListAdapter(getContext()));
		ipcAction newCommand[] = { ipcAction.PROCESSOR };
		ipcService.addRequest(newCommand, 0, this);

	    loadingText.setVisibility(View.VISIBLE);
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
		}
		
		if(forceOnline == false) {
			
			// prepare settings data
			for (int index = 0; index < coredata.size(); index++) {
				processorConfig newConfig = new processorConfig();
				newConfig.maxFreq = coredata.get(index).getMaxScaling();
				newConfig.minFreq = coredata.get(index).getMinScaling();
				newConfig.gov = coredata.get(index).getGrovernors();
				newConfig.enable = coreEnable[index];
				setdata.add(newConfig);
			}
			
			((ProcessorListAdapter) cpuList.getAdapter()).refresh();
		    loadingText.setVisibility(View.GONE);
		}
		else {
			ipcAction newCommand[] = { ipcAction.PROCESSOR };
			ipcService.addRequest(newCommand, 0, this);
		}
				
	}
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult){
			
			StringBuilder preferenceString = new StringBuilder(); 
			for(int index = 0; index < setdata.size(); index++) {
				
				// valid value
				if (setdata.get(index).maxFreq == 0 ||
					setdata.get(index).minFreq == 0 ||
					setdata.get(index).gov.equals("") )
					continue;
					
				String coreSetting  = index+ "," + 
			                          setdata.get(index).maxFreq + "," + 
			                          setdata.get(index).minFreq + "," +
			                          setdata.get(index).gov;
				if(setdata.get(index).enable == true)
					coreSetting += ",1";
				else
					coreSetting += ",0";
				
				if (preferenceString.length() > 0)
					preferenceString.append(";");
				preferenceString.append(coreSetting);
			}
			persistString(preferenceString.toString());
		}
		
		if(coreEnable != null) {
			for(int index = 0; index < coreEnable.length; index++) {
				if(coreEnable[index] == false)
					ipcService.setCPUStatus(index, 0);
			}
		}
		
		super.onDialogClosed(positiveResult);
	}

	
	public ProcessorPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private class ProcessorListAdapter extends BaseAdapter {
    	
    	private LayoutInflater mInflater = null;
        private Context mContext = null;
        
        public ProcessorListAdapter(Context context)
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

            	final TextView maxSeekBarValue = (TextView)sv.findViewById(R.id.id_processor_freq_max_title);
        		final Spinner maxSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_max_value);
        		
        		final TextView minSeekBarValue = (TextView)sv.findViewById(R.id.id_processor_freq_min_title);
        		final Spinner minSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_min_value);
        		
        		final Spinner govSeekBar = (Spinner)sv.findViewById(R.id.id_processor_detail_gov_value);

                enableBox.setChecked(coreEnable[position]);
                
            	String [] freqList = CommonUtil.eraseNonIntegarString(coredata.get(position).getAvaiableFrequeucy().split(" "));
        		ArrayAdapter<String> freqAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, freqList);
        		freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		maxSeekBar.setAdapter(freqAdapter);
        		minSeekBar.setAdapter(freqAdapter);
        		
            	String [] govList = CommonUtil.eraseEmptyString(coredata.get(position).getAvaiableGovernors().split(" "));
        		ArrayAdapter<String> govAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, govList);
        		govAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		govSeekBar.setAdapter(govAdapter);

        		((TextView)sv.findViewById(R.id.id_processor_title)).setText(
        				mContext.getResources().getString(R.string.ui_processor_core)+" "+coredata.get(position).getNumber());

        		for(int i = 0; i < govList.length;i++)
        			if(govList[i].equals(coredata.get(position).getGrovernors()))
        				govSeekBar.setSelection(i);

        		if (coredata.get(position).getMaxScaling() != -1)
        			maxSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_max_title)+" "+coredata.get(position).getMaxScaling());            
        		
        		for(int i = 0; i < freqList.length;i++)
        			if(coredata.get(position).getMaxScaling() == Integer.parseInt(freqList[i]))
        				maxSeekBar.setSelection(i);
        		
        		if (coredata.get(position).getMinScaling() != -1)
        			minSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_min_title)+" "+coredata.get(position).getMinScaling());
        		
        		for(int i = 0; i < freqList.length;i++)
        			if(coredata.get(position).getMinFrequency() == Integer.parseInt(freqList[i]))
        				minSeekBar.setSelection(i);
                
                final Settings setting = new Settings(mContext);
        		if(setting.isRoot())
        		{
        			maxSeekBar.setClickable(true);
        			minSeekBar.setClickable(true);
        			govSeekBar.setClickable(true);
        		}
        		else
        		{
        			maxSeekBar.setClickable(false);
        			minSeekBar.setClickable(false);
        			govSeekBar.setClickable(false);
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
						
						setdata.get(CPUNum).enable = isChecked;
							
					}
                });
                
                govSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        				int CPUNum = Integer.parseInt((String) ((View)parentView.getParent()).getTag());
        				String selected = parentView.getItemAtPosition(position).toString();
        				ipcService.setCPUGov(CPUNum, selected);

        				setdata.get(CPUNum).gov = parentView.getItemAtPosition(position).toString();
        			}
        			public void onNothingSelected(AdapterView<?> parentView) { }

				});

        		
                maxSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

        				if(maxSeekBar.getSelectedItemPosition() < minSeekBar.getSelectedItemPosition()) {
        					maxSeekBar.setSelection(minSeekBar.getSelectedItemPosition());
        					position = maxSeekBar.getSelectedItemPosition();
        				}

        				int CPUNum = Integer.parseInt((String) ((View)parentView.getParent()).getTag());
       				    String [] CPUFreqList = coredata.get(CPUNum).getAvaiableFrequeucy().trim().split(" ");
        				
        				maxSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_max_title)
        						   						+" "+CPUFreqList[maxSeekBar.getSelectedItemPosition()]);

        				ipcService.setCPUStatus(CPUNum, 1);
        				ipcService.setCPUMaxFreq(CPUNum, Long.parseLong(CPUFreqList[maxSeekBar.getSelectedItemPosition()]));

        				setdata.get(CPUNum).maxFreq = Long.parseLong(CPUFreqList[maxSeekBar.getSelectedItemPosition()]);
        			}
        			public void onNothingSelected(AdapterView<?> parentView) { }

				});

                minSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        				
     				    if(maxSeekBar.getSelectedItemPosition() < minSeekBar.getSelectedItemPosition()) {
        					minSeekBar.setSelection(maxSeekBar.getSelectedItemPosition());
        					position = minSeekBar.getSelectedItemPosition();
        				}

     				    int CPUNum = Integer.parseInt((String) ((View) minSeekBar.getParent()).getTag());
       				    String [] CPUFreqList = coredata.get(CPUNum).getAvaiableFrequeucy().trim().split(" ");
        				
        				minSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_min_title)
        						   						+" "+CPUFreqList[minSeekBar.getSelectedItemPosition()]);

        				ipcService.setCPUStatus(CPUNum, 1);
        				ipcService.setCPUMinFreq(CPUNum, Long.parseLong(CPUFreqList[minSeekBar.getSelectedItemPosition()]));

        				setdata.get(CPUNum).minFreq = Long.parseLong(CPUFreqList[minSeekBar.getSelectedItemPosition()]);
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