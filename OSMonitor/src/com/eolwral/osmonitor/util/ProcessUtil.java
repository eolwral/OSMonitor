package com.eolwral.osmonitor.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import com.eolwral.osmonitor.R;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * help to gather the information of process and cache data 
 * @author eolwral
 */
public class ProcessUtil extends Thread {

	private static ProcessUtil singletone = null;
	private PackageManager packageMgr = null;
	private Resources resourceMgr = null;
	private ActivityManager activityMgr = null;
	private Drawable commonIcon = null;
	private boolean useDetail = true;
	private int iconSize = 60; 

	private class CacheItem {
		public String name;
		public Drawable icon;
	}

	// cache
	private final HashMap<String, CacheItem> cacheStorage = new HashMap<String, CacheItem>();

	// work queue for background thread
	private final Semaphore queryQueueLock = new Semaphore(1, true);
	private final LinkedList<QueueJob> queryQueue = new LinkedList<QueueJob>();

	public static ProcessUtil getInstance(Context context, boolean detail) {

		if (singletone == null) {
			singletone = new ProcessUtil();

			singletone.packageMgr = context.getPackageManager();
			singletone.resourceMgr = context.getResources();
			singletone.activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager wmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wmanager.getDefaultDisplay().getMetrics(metrics);

			switch(metrics.densityDpi){
			
			case DisplayMetrics.DENSITY_LOW:
				singletone.iconSize = 12;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				singletone.iconSize = 48;
				break;
			case DisplayMetrics.DENSITY_HIGH:
				singletone.iconSize = 60;
				break;
			case DisplayMetrics.DENSITY_XHIGH:
				singletone.iconSize = 100;
				break;
			}

			singletone.useDetail = detail;
			if (singletone.useDetail == true)
				singletone.commonIcon = resizeImage(singletone.resourceMgr.getDrawable(R.drawable.ic_process_system), singletone.iconSize);
			
			singletone.start();
		}

		return singletone;
	}

	public void doCacheInfo(int uid, String owner, String process) {
		// add a job into work queue
		try {
			queryQueueLock.acquire();
		} catch (InterruptedException e) {
			return;
		}

		queryQueue.add(new QueueJob(uid, owner, process));
		queryQueueLock.release();

		// create a skeleton object
		CacheItem skeletonItem = new CacheItem();
		skeletonItem.name = process;
		skeletonItem.icon = commonIcon;
		cacheStorage.put(process, skeletonItem);
  
		return;   
	}

	/**
	 * a class for queued job 
	 */
	private class QueueJob {
		public int uid; 
		public String owner;
		public String process;

		public QueueJob(int uid, String owner, String process) {
			this.uid = uid;
			this.owner = owner;
			this.process = process;
		}
	}

	@Override
	public void run() {
		while (true) {
			if (getCacheInfo())
			continue; 
			try {
				sleep(500);
			} catch (InterruptedException e) { }
		}
	}

	/**
	 * get information for process
	 * @return true == success, false == failed 
	 */
	public boolean getCacheInfo() {

		if (queryQueue.isEmpty())
			return false;

		try {
			queryQueueLock.acquire();
		} catch (InterruptedException e) {
			return false;
		}

		QueueJob processJob = queryQueue.remove();
		queryQueueLock.release();

		PackageInfo curPackageInfo = null;
		String curPackageName = null;
		if (processJob.process.contains(":"))
			curPackageName = processJob.process.substring(0,
					processJob.process.indexOf(":"));
		else 
			curPackageName = processJob.process;
		

		// for system user
		if (processJob.owner.contains("system")
				&& processJob.process.contains("system")
				&& !processJob.process.contains(".") 
				&& !processJob.process.contains("osmcore"))
			curPackageName = "android";
		try {
			curPackageInfo = packageMgr.getPackageInfo(curPackageName, 0);
		} catch (NameNotFoundException e) {	}

		if (curPackageInfo == null && processJob.uid > 0) {
			String[] subPackageName = packageMgr
					.getPackagesForUid(processJob.uid);

			if (subPackageName != null) {
				for (int PackagePtr = 0; PackagePtr < subPackageName.length; PackagePtr++) {
					if (subPackageName[PackagePtr] == null)
						continue;
					try {
						curPackageInfo = packageMgr.getPackageInfo(subPackageName[PackagePtr], 0);
						PackagePtr = subPackageName.length;
					} catch (NameNotFoundException e) { }
				}
			}
		}

		CacheItem processItem = new CacheItem();

		if (curPackageInfo != null) {
			processItem.name = curPackageInfo.applicationInfo.loadLabel(packageMgr).toString();
			if(useDetail == true)
				processItem.icon = resizeImage(curPackageInfo.applicationInfo.loadIcon(packageMgr), iconSize);
		} else {
			processItem.name = curPackageName;
			processItem.icon = commonIcon;
		}

		cacheStorage.put(processJob.process, processItem);

		return true;
	}

	/**
	 * resize icon to the specific size
	 * @param icon input image
	 * @param size icon size
	 * @return resized image 
	 */
	private static Drawable resizeImage(Drawable Icon, int iconSize) {
		Bitmap BitmapOrg = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
		Canvas BitmapCanvas = new Canvas(BitmapOrg);
		Icon.setBounds(0, 0, iconSize, iconSize);
		Icon.draw(BitmapCanvas);
		
		BitmapCanvas = null;
		return new BitmapDrawable(BitmapOrg);
	}

	/**
	 * get the name of process 
	 * @param process the name of process
	 * @return process name
	 */
	public String getPackageName(String process) {
		CacheItem Process = cacheStorage.get(process);
		if (Process != null)
			return Process.name;
		return null;
	}

	/**
	 * get the icon of process
	 * @param process the name of process
	 * @return icon 
	 */
	public Drawable getPackageIcon(String process) {
		CacheItem Process = cacheStorage.get(process);
		if (Process != null)
			return cacheStorage.get(process).icon;
		return null;
	}

	/**
	 * check process has been processed
	 * @param process the name of process
	 * @return true == cached, false == none
	 */
	public boolean checkPackageInformation(String process) {
		CacheItem Process = cacheStorage.get(process);
		if (Process == null)
			return false;
		return true;
	}
	
	/**
	 * get private memory
	 * @param pid the process id of process
	 * @return private memory value
	 */
	public MemoryInfo getMemoryInfo(int pid) {
		int processPID[] = new int[1];
		processPID[0] = pid;
		MemoryInfo[] memInfo = activityMgr.getProcessMemoryInfo(processPID);
		return memInfo[0];
	}

}
