package com.eolwral.osmonitor.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ui.HelpWindows;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class CommonUtil {
  
  /**
  * predefine location for osmcore
  */
  private final static String binaryName = "osmcore";
  
  /**
   * bring up help activity
   * @param context  
   * @param url 
   */
  @SuppressLint("SetJavaScriptEnabled")
  public static void showHelp(Context context, String url)
  {
	  Intent intent = new Intent(context, HelpWindows.class);
	  intent.putExtra("URL", url);
	  context.startActivity(intent);
  }
  
  /**
   * kill a process 
   * @param pid
   * @param context
   */
  public static void killProcess(int pid, Context context) {
	  final Settings settings = new Settings(context);
	  if (!settings.isRoot()) 
		  android.os.Process.killProcess(pid);
	  else
		  IpcService.getInstance().killProcess(pid);
  }

  /**
   * copy a binary from asset directory to working directory.
   * @param assetPath
   * @param localPath
   * @param context
   * @return true == copied, false == text busy
   */
  private static boolean copyFile(String assetPath, String localPath, Context context) {
    try {
    	
      // detect architecture
      if (isARM()) 
        assetPath += "_arm";
      else 
    	assetPath += "_x86";  
    	  

	  InputStream binary = context.getAssets().open(assetPath);
      FileOutputStream execute = new FileOutputStream(localPath);
      
      int read = 0;
	  byte[] buffer = new byte[4096];
		      
      while ((read = binary.read(buffer)) > 0) 
	    execute.write(buffer, 0, read);
		      
	  execute.close();
	  binary.close();
	  
	  execute = null;
	  binary = null;
		      
	} catch (IOException e) {
	  return false;
	}
    return true;
  }

  /**
   * is ARM base ?
   * @return true == yes , false == no
   */
  public static boolean isARM() {
	return (android.os.Build.CPU_ABI.equalsIgnoreCase("armeabi")) ||
    	  (android.os.Build.CPU_ABI2.equalsIgnoreCase("armeabi"));
  }
  
  /**
   * check file status
   * @param file path
   * @return true == exist, false == not exist
   */
  @SuppressWarnings("unused")
  private static boolean fileExist(String localPath) {
	  File targetFile = new File(localPath);
	  return targetFile.exists();
  }
  
  /**
   * execute osmcore as a binary execute
   * @param context
   * @throws InterruptedException 
   */
  public static boolean execCore(Context context) {
	String binary = context.getFilesDir().getAbsolutePath()+"/"+binaryName;

	// copy file 
	if(!copyFile("osmcore", binary, context))
		return false; 
	
	// lock file
	File file = new File(binary+".lock");
	FileChannel channel = null;
	FileLock lock = null;
	try {
		channel = new RandomAccessFile(file, "rw").getChannel();
		lock = channel.tryLock();
	} catch (Exception e) {
		return false;
	}
	
	// execute osmcore
	try {
		final Settings settings = new Settings(context);
		Process process = null;
		if (!settings.isRoot()) 
			process = Runtime.getRuntime().exec("sh");
		else
			process = Runtime.getRuntime().exec("su");
		
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		os.writeBytes("chmod 777 " + binary + "\n");
		os.writeBytes(binary + " " + settings.getToken().toString()+ " &\n");
		os.writeBytes("exit \n");
		process.waitFor(); 
	} catch (Exception e) {
		return false;
	}
	
    // release the lock
    try {
		lock.release();
	    channel.close();
	} catch (Exception e) {}
       
	return true;
  }
  
  /**
   * detect that Device is rooted or not
   * @return true == rooted, false == non-rooted
   */
  public static boolean preCheckRoot() {
	boolean flag = false; 
	
	try {
		Process process = Runtime.getRuntime().exec("su");
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		os.writeBytes("exit \n");
		process.waitFor();
		
		if(process.exitValue() == 0)
			flag = true;
		
	} catch (Exception e) { }
	
	return flag;
  }
  
  
  /**
   * convert data as string 
   * @param data
   * @return a string with correct format
   */
  public static String convertLong(long data) {
	if (data > (1024*1024))
      return (data/(1024*1024))+"M";
	else if (data > 1024)
      return (data/1024)+"K";		
    return ""+data;
  }
  
  /**
   * convert data as float value
   * @param data
   * @return a string of float value
   */
  @SuppressLint("DefaultLocale")
  public static String convertFloat(float data) {
	return String.format("%.1f", data); 	  
  }

  
  /**
   * detect background service is running or not
   * @param context
   * @return running == ture, none == false
   */
  public static boolean isServiceRunning(Context context){
	final ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	final List<RunningServiceInfo> services = actMgr.getRunningServices(Integer.MAX_VALUE);
	  
    for (RunningServiceInfo serviceInfo : services) {
      if (serviceInfo.service.getClassName().equals(OSMonitorService.class.getName()))
        return true;
    }
    return false;
  }
}
