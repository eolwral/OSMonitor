package com.eolwral.osmonitor.ui;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.processorInfo;
import com.eolwral.osmonitor.core.processorInfoList;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.util.UserInterfaceUtil;
import com.eolwral.osmonitor.settings.Settings;

public class MiscProcessorFragment extends ListFragment implements
    ipcClientListener {

  private ArrayList<processorInfo> coredata = new ArrayList<processorInfo>();
  private ArrayList<processorConfig> setdata = new ArrayList<processorConfig>();

  // settings
  private Settings settings = null;

  // ipc client
  private IpcService ipcService = IpcService.getInstance();

  // working dialog
  private ProgressDialog ipcProcess = null;


  private class processorConfig {
    public boolean enable = false;
    public long maxFreq = 0;
    public long minFreq = 0;
    public String gov = "";
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // settings
    settings = Settings.getInstance(getActivity().getApplicationContext());

    setListAdapter(new ProcessorListAdapter(getActivity()));

  }

  public void onStart() {
    super.onStart();

    byte newCommand[] = { ipcCategory.PROCESSOR };
    ipcService.addRequest(newCommand, 0, this);
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.ui_misc_item_processor_fragment, container, false);

    // enable fragment option menu
    setHasOptionsMenu(false);

    ipcProcess = ProgressDialog.show(getActivity(),
        getResources().getString(R.string.ui_processor_enable_title),
        getResources().getString(R.string.ui_processor_enable_msg), true, true);

    return v;
  }

  @Override
  public void onRecvData(byte [] result) {

    if (result == null) {
      byte newCommand[] = { ipcCategory.PROCESSOR };
      ipcService.addRequest(newCommand, 0, this);
      return;
    }

    // cleanup
    coredata.clear();
    setdata.clear();

    // convert data
    ipcMessage resultMessage = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));
    for (int index = 0; index < resultMessage.dataLength(); index++) {

      try {
        ipcData rawData = resultMessage.data(index);

        if (rawData.category() == ipcCategory.PROCESSOR)
          extractProcessorInfo(rawData);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (coredata.size() <= 0) {
      byte newCommand[] = { ipcCategory.PROCESSOR };
      ipcService.addRequest(newCommand, 0, this);
      return;
    }


    ((ProcessorListAdapter) getListAdapter()).refresh();
    ipcProcess.dismiss();

    for (int index = 0; index < coredata.size(); index++) {

      processorConfig newConfig = new processorConfig();

      newConfig.enable = (coredata.get(index).offLine() != 0x1);

      if (newConfig.enable) {
        newConfig.maxFreq = coredata.get(index).maxFrequency();
        newConfig.minFreq = coredata.get(index).minFrequency();
        newConfig.gov = coredata.get(index).governors();
      }
      else {
        // assign cpu0's value as default value
        newConfig.maxFreq = coredata.get(0).maxFrequency();
        newConfig.minFreq = coredata.get(0).minFrequency();
        newConfig.gov = "";
      }

      setdata.add(newConfig);
    }
  }

  private void extractProcessorInfo(ipcData rawData)
  {
    processorInfoList list = processorInfoList.getRootAsprocessorInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < list.listLength(); count++) {
      processorInfo prInfo = list.list(count);
      coredata.add(prInfo);
    }
  }

  private class ProcessorListAdapter extends BaseAdapter {

    private Context mContext = null;

    public ProcessorListAdapter(Context context) {
      mContext = context;
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
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        sv = (View) mInflater.inflate(R.layout.ui_misc_item_processor_detail,
            parent, false);
      } else 
        sv = convertView;

      final CheckBox enableBox = (CheckBox) sv
          .findViewById(R.id.id_processor_enable);
      enableBox.setOnCheckedChangeListener(null);

      final Spinner govSeekBar = (Spinner) sv
          .findViewById(R.id.id_processor_detail_gov_value);
      govSeekBar.setOnItemSelectedListener(null);

      final Spinner maxSeekBar = (Spinner) sv
          .findViewById(R.id.id_processor_detail_max_value);
      final TextView maxSeekBarValue = (TextView) sv
          .findViewById(R.id.id_processor_freq_max_title);
      maxSeekBar.setOnItemSelectedListener(null);

      final Spinner minSeekBar = (Spinner) sv
          .findViewById(R.id.id_processor_detail_min_value);
      final TextView minSeekBarValue = (TextView) sv
          .findViewById(R.id.id_processor_freq_min_title);
      minSeekBar.setOnItemSelectedListener(null);

      enableBox.setChecked(setdata.get(position).enable);

      String[] freqList = new String[0];
      if (coredata.get(position).availableFrequency() != null) {
        freqList = UserInterfaceUtil.eraseNonIntegarString(coredata
                            .get(position).availableFrequency().split(" "));
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<String>(mContext,
                             android.R.layout.simple_spinner_item, freqList);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxSeekBar.setAdapter(freqAdapter);
        minSeekBar.setAdapter(freqAdapter);
      }

      String[] govList = new String[0];
      if (coredata.get(position).availableGovernors() != null) {
        govList = UserInterfaceUtil.eraseEmptyString(coredata.get(position)
                                   .availableGovernors().split(" "));
        ArrayAdapter<String> govAdapter = new ArrayAdapter<String>(mContext,
                                   android.R.layout.simple_spinner_item, govList);
        govAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        govSeekBar.setAdapter(govAdapter);
      }

      ((TextView) sv.findViewById(R.id.id_processor_title)).setText(mContext
          .getResources().getString(R.string.ui_processor_core)
          + " "
          + coredata.get(position).number());

      for (int i = 0; i < govList.length; i++) {
        if (govList[i].trim().equals(setdata.get(position).gov.trim())) {
          govSeekBar.setSelection(i);
          setdata.get(position).gov = govList[i];
        }
      }

      if (coredata.get(position).maxScaling() != -1)
        maxSeekBarValue.setText(mContext.getResources().getString(
            R.string.ui_processor_freq_max_title)
            + " " + coredata.get(position).maxScaling());

      for (int i = 0; i < freqList.length; i++) {
        if (setdata.get(position).maxFreq == Integer.parseInt(freqList[i])) {
          maxSeekBar.setSelection(i);
          setdata.get(position).maxFreq = Integer.parseInt(freqList[i]);
        }
      }

      if (coredata.get(position).minScaling() != -1)
        minSeekBarValue.setText(mContext.getResources().getString(
            R.string.ui_processor_freq_min_title)
            + " " + coredata.get(position).minScaling());

      // set minimum value 
      minSeekBar.setSelection(0);
      if (freqList.length > 0)
        setdata.get(0).minFreq = Integer.parseInt(freqList[0]);

      for (int i = 0; i < freqList.length; i++) {
        if (setdata.get(position).minFreq == Integer.parseInt(freqList[i])) {
          minSeekBar.setSelection(i);
          setdata.get(position).minFreq = Integer.parseInt(freqList[i]);
        }
      }

      if (settings.isRoot()) {
        maxSeekBar.setClickable(true);
        minSeekBar.setClickable(true);
        govSeekBar.setClickable(true);
      } else {
        maxSeekBar.setClickable(false);
        minSeekBar.setClickable(false);
        govSeekBar.setClickable(false);
      }

      if (position % 2 == 1)
        sv.setBackgroundColor(0x80444444);
      else
        sv.setBackgroundColor(0x80000000);

      enableBox.setTag("" + coredata.get(position).number());
      enableBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
          int CPUNum = Integer.parseInt((String) buttonView.getTag());

          // prevent to disable CPU0
          if (CPUNum == 0 && isChecked == false) {
            buttonView.setChecked(true);
            return;
          }

          // change CPU status
          if (isChecked)
            ipcService.sendCommand(ipcCategory.SETCPUSTATUS, CPUNum, 1);
          else
            ipcService.sendCommand(ipcCategory.SETCPUSTATUS, CPUNum, 0);

          setdata.get(CPUNum).enable = isChecked;
        }
      });

      govSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parentView,
            View selectedItemView, int position, long id) {
          int CPUNum = Integer.parseInt((String) ((View) parentView.getParent()).getTag());
          String selected = parentView.getItemAtPosition(position).toString();
          setdata.get(CPUNum).gov = selected;
          ipcService.sendCommand(ipcCategory.SETCPUGORV, CPUNum, selected);
        }

        public void onNothingSelected(AdapterView<?> parentView) {
        }

      });

      maxSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parentView,
            View selectedItemView, int position, long id) {

          if (maxSeekBar.getSelectedItemPosition() < minSeekBar.getSelectedItemPosition()) {
            maxSeekBar.setSelection(minSeekBar.getSelectedItemPosition());
            position = maxSeekBar.getSelectedItemPosition();
          }

          int CPUNum = Integer.parseInt((String) ((View) parentView.getParent()).getTag());
          String[] freqList = UserInterfaceUtil.eraseNonIntegarString(coredata
              .get(CPUNum).availableFrequency().split(" "));

          maxSeekBarValue.setText(mContext.getResources().getString(R.string.ui_processor_freq_max_title)
              + " " + freqList[maxSeekBar.getSelectedItemPosition()]);

          ipcService.sendCommand(ipcCategory.SETCPUSTATUS, CPUNum, 1);
          ipcService.sendCommand(ipcCategory.SETCPUMAXFREQ, CPUNum,
              Long.parseLong(freqList[maxSeekBar.getSelectedItemPosition()]));
          setdata.get(CPUNum).maxFreq = Long.parseLong(freqList[maxSeekBar.getSelectedItemPosition()]);
        }

        public void onNothingSelected(AdapterView<?> parentView) {
        }

      });

      minSeekBar.setOnItemSelectedListener(new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parentView,
            View selectedItemView, int position, long id) {

          if (maxSeekBar.getSelectedItemPosition() < minSeekBar.getSelectedItemPosition()) {
            minSeekBar.setSelection(maxSeekBar.getSelectedItemPosition());
            position = minSeekBar.getSelectedItemPosition();
          }

          int CPUNum = Integer.parseInt((String) ((View) minSeekBar.getParent())
              .getTag());
          String[] freqList = UserInterfaceUtil.eraseNonIntegarString(coredata
              .get(CPUNum).availableFrequency().split(" "));

          minSeekBarValue.setText(mContext.getResources().getString(
              R.string.ui_processor_freq_min_title)
              + " " + freqList[minSeekBar.getSelectedItemPosition()]);

          ipcService.sendCommand(ipcCategory.SETCPUSTATUS, CPUNum, 1);
          ipcService.sendCommand(ipcCategory.SETCPUMINFREQ, CPUNum,
              Long.parseLong(freqList[minSeekBar.getSelectedItemPosition()]));
          setdata.get(CPUNum).minFreq = Long.parseLong(freqList[minSeekBar.getSelectedItemPosition()]);
        }

        public void onNothingSelected(AdapterView<?> parentView) {
        }

      });

      sv.setTag("" + coredata.get(position).number());

      return sv;
    }

    public void refresh() {
      notifyDataSetChanged();
    }

  }

}
