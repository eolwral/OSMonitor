package com.eolwral.osmonitor;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ui.ConnectionFragment;
import com.eolwral.osmonitor.ui.MessageFragment;
import com.eolwral.osmonitor.ui.MiscFragment;
import com.eolwral.osmonitor.ui.ProcessFragment;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.settings.Settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;


public class OSMonitor extends ActionBarActivity  implements
    ActionBar.TabListener, ViewPager.OnPageChangeListener {
 
  private ViewPager mViewPager = null;
  
  @Override 
  public void onStop() {
    super.onStop();  
    
    if(mViewPager == null)
      return;
    
    ((OSMonitorPagerAdapter)mViewPager.getAdapter()).
       getItem(mViewPager.getCurrentItem()).setUserVisibleHint(false);
    
    // end self 
    if(isFinishing())
    {
      IpcService.getInstance().disconnect();
      android.os.Process.killProcess(android.os.Process.myPid());
    }
  }
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    // create view
    super.onCreate(savedInstanceState);
    
    IpcService.Initialize(this);
    
    // load layout
    setContentView(R.layout.ui_main);
 
    // prepare pager
    mViewPager = (ViewPager) findViewById(R.id.mainpager);
    mViewPager.setAdapter(new OSMonitorPagerAdapter(getSupportFragmentManager()));
    mViewPager.setOnPageChangeListener(this);

    // keep all fragments 
    mViewPager.setOffscreenPageLimit(5);
    
    // prepare action bar
    final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    actionBar.addTab(actionBar.newTab().setText(R.string.ui_process_tab)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.ui_connection_tab)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.ui_misc_tab)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.ui_debug_tab)
        .setTabListener(this));

    mViewPager.setCurrentItem(0);
    
    // start background service
    final Settings setting = Settings.getInstance(this);
    if(( setting.isEnableCPUMeter() || setting.isAddShortCut()) && !CommonUtil.isServiceRunning(this))
      startService(new Intent(this, OSMonitorService.class));
    
    // prepare exit 
    LocalBroadcastManager.getInstance(this).registerReceiver(ExitReceiver, new IntentFilter("Exit"));
  }
  
  private BroadcastReceiver ExitReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        IpcService.getInstance().forceExit();
      context.stopService(new Intent(context, OSMonitorService.class));
      finish();
    }
  };
  
  @Override
  protected void onDestroy() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(ExitReceiver);
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
      //No call for super(). Bug on API Level > 11.
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    /* prepare option on action bar */
    super.onCreateOptionsMenu(menu);
    return true;
  }
  
  @Override
  public void onRestart() {
    super.onRestart();
    
    if(mViewPager == null)
      return;
    
    Fragment mFragment = ((OSMonitorPagerAdapter)mViewPager.getAdapter()).
                      getItem(mViewPager.getCurrentItem());
    
    if(mFragment != null) {
      // catch exception when back from screen off
      try {
        mFragment.setUserVisibleHint(true);
      } catch (Exception e) {};
    }
  }
  
  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    mViewPager.setCurrentItem(tab.getPosition());
     if (mViewPager.getCurrentItem() != tab.getPosition())
       mViewPager.setCurrentItem(tab.getPosition());

     // force display menu when selected
    ((OSMonitorPagerAdapter)mViewPager.getAdapter()).getItem(mViewPager.getCurrentItem()).setMenuVisibility(true);    
  }

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    // force to hidden when unselected
    ((OSMonitorPagerAdapter)mViewPager.getAdapter()).getItem(tab.getPosition()).setMenuVisibility(false);   
  }

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {
  }

  @Override
  public void onPageScrollStateChanged(int arg0) {
  }

  @Override
  public void onPageScrolled(int arg0, float arg1, int arg2) {
  }

  @Override
  public void onPageSelected(int arg0) {
    getSupportActionBar().setSelectedNavigationItem(arg0);
  }     
  
  /* Pager Adapter for OSMonitor */
  public class OSMonitorPagerAdapter extends FragmentPagerAdapter {

    @SuppressLint("UseSparseArrays")
    HashMap<Integer, Fragment> mFragment = new HashMap<Integer, Fragment>(); 
        
    public OSMonitorPagerAdapter(FragmentManager fm) {
      super(fm);

    }

    @Override
    public Fragment getItem(int position) {
      
      if(mFragment.containsKey(position))
        return mFragment.get(position);
      
      switch (position) {
      /* Process */ 
      case 0:
        mFragment.put(0, new ProcessFragment());
        break;

      /* Connection */
      case 1:
        mFragment.put(1, new ConnectionFragment());
        break;

      /* Misc */ 
      case 2:
        mFragment.put(2, new MiscFragment());
        break;

      /* Message */
      case 3:
        mFragment.put(3, new MessageFragment());
        break;
        
      /* Monitor */
      case 4:
        // under construction
        break;
      }
      return mFragment.get(position);
    } 

    @Override
    public int getCount() {
      return 4;
    }
    
  }


}


