package com.eolwral.osmonitor.ipc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData.Builder;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.Settings;
import com.google.protobuf.ByteString;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;

/**
 * implement communicate mechanize between process with Unix socket
 */
public class IpcService {
	 
	/** 
	 * Singleton instance
	 */
	private static IpcService instance = null;
	private Context ipcContext = null;

	/**
	 * predefine socket name 
	 */
	private final static String socketName = "osmipc";

	/**
	 * predefine buffer size
	 */
	private final static int bufferSize = 131072; /* 128K */

	/**
	 * Unix socket
	 */
	private LocalSocket clientSocket = null;

	/**
	 * Unix socket address
	 */
	private LocalSocketAddress clientAddress = null;

	/**
	 * comparator for using time stamp
	 */
	private class QueuedComparator implements Comparator<QueuedTask> {
		@Override
		public int compare(QueuedTask lhs, QueuedTask rhs) {
			if(lhs.timestamp > rhs.timestamp)
				return 1;
			else if (lhs.timestamp < rhs.timestamp)
				return -1;
			return 0;
		}
		
	}
	
	/**
	 * a queue for IPC command
	 */
	private static PriorityQueue<QueuedTask> cmdQueue = null;
	
	/**
	 * exclusive lock for cmdQueue 
	 */
	private final Semaphore cmdQueueLock = new Semaphore(1, true);

	/**
	 * a class for single task
	 */
	private class QueuedTask {
		public ipcAction[] action = null;
		public long timestamp = 0;
		public ipcClientListener listener = null;
		public ipcMessage result = null;

		/**
		 * constructor
		 */
		QueuedTask(ipcAction[] action, long timestamp, ipcClientListener listener) {
			this.action = action;
			this.timestamp = timestamp;
			this.listener = listener;
		}

		/**
		 * replace equals that will only check listener
		 */
		@Override 
		public boolean equals (Object object) {
			if(object instanceof QueuedTask)
			{
				QueuedTask compareObj = (QueuedTask)object; 
				if(compareObj.listener == this.listener)
					return true;
			}
			return false; 
		}

	}

	/**
	 * callback interface for ipcClient
	 */
	public interface ipcClientListener {
		public void onRecvData(ipcMessage result);
	}

	/**
     * background worker for IpcService 
     */
	private static ipcTask worker = null;


	/**
	 * initialize IpcService
	 * @param context
	 */
	public static void Initialize(Context context) {
		if (instance == null)
		{
			instance = new IpcService(socketName);
			instance.ipcContext = context;
		}
	}
	
	/**
	 * get a instance for IpcService [avoid duplicated connections] 
	 * @return IpcService
	 */
	public static IpcService getInstance() {
	    return instance;
	}

	/**
	 * internal use only for creating object
	 * 
	 * @param serverName
	 */
	private IpcService(String serverName) {
		clientAddress = new LocalSocketAddress(serverName, LocalSocketAddress.Namespace.ABSTRACT);
		
		// prepare a priority queue
		QueuedComparator cmdComparator = new QueuedComparator();
		cmdQueue = new PriorityQueue<QueuedTask>(1, cmdComparator);
	}

	/**
	 * destructor for ipcClient
	 */
	protected void finalize() {
		try {
			clientSocket.shutdownInput();
			clientSocket.shutdownOutput();
			clientSocket.close();
			clientSocket = null;
			clientAddress = null;
		} catch (IOException e) { }
	}

	/**
	 * connection to osmcore
	 * @return true == success, false == fail
	 */
	private boolean connect() {
	
		try {
			final Settings settings = new Settings(ipcContext);

			clientSocket = new LocalSocket();
			clientSocket.connect(clientAddress);
			clientSocket.setSendBufferSize(bufferSize);
			clientSocket.setReceiveBufferSize(bufferSize);
			
			// Notice: the value is milliseconds
			clientSocket.setSoTimeout(settings.getInterval()*1000);
			
			// send token
			OutputStream outData = clientSocket.getOutputStream();
			byte [] outToken = settings.getToken().getBytes();
			outData.write(outToken);
			 
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * check daemon status
	 * @return true == alive, false == dead
	 */
	private boolean checkStatus()
	{
		if(clientSocket == null)
			return false;
		
		return clientSocket.isConnected();
	}
	
	/**
	 * restart the daemon
	 * @param result
	 */
	private boolean restartDaemon()	{
		return CommonUtil.execCore(ipcContext);
	}
	
	/**
	 * send a force exit command
	 */
	public void forceExit() {
		if(!checkStatus())
			return;

		ipcMessage.Builder exitCommand = ipcMessage.newBuilder();
		exitCommand.setType(ipcMessage.ipcType.EXIT);
		
		// send
		try {
			OutputStream outData = clientSocket.getOutputStream();
			exitCommand.build().writeTo(outData);			
		} catch (IOException e) {}
		
		return;
	}
	
	
	/**
	 * adjust the priority of process
	 * @param pid 
	 * @param priority
	 */
	public void setPrority(int pid, int priority) {
		if(!checkStatus())
			return;

		ipcMessage.Builder setCommand = ipcMessage.newBuilder();
		setCommand.setType(ipcMessage.ipcType.COMMAND);
		
		Builder data = setCommand.addDataBuilder();
		
		data.setAction(ipcAction.SETPRIORITY);

		String pidData = ""+pid;
		data.addPayload(ByteString.copyFrom(pidData.getBytes()));

		String prorityData = ""+priority;
		data.addPayload(ByteString.copyFrom(prorityData.getBytes()));
		
		// send
		try {
			OutputStream outData = clientSocket.getOutputStream();
			setCommand.build().writeTo(outData);			
		} catch (IOException e) {}
		
		return;
	}
	
	/**
	 * kill processes
	 * @param pid
	 */
	public void killProcess(int pid) {
		if(!checkStatus())
			return;

		ipcMessage.Builder setCommand = ipcMessage.newBuilder();
		setCommand.setType(ipcMessage.ipcType.COMMAND);

		Builder data = setCommand.addDataBuilder();
		data.setAction(ipcAction.KILLPROCESS);
		String pidData = ""+pid;
		data.addPayload(ByteString.copyFrom(pidData.getBytes()));

		// send
		try {
			OutputStream outData = clientSocket.getOutputStream();
			setCommand.build().writeTo(outData);			
		} catch (IOException e) {}
		
		return;
	}

	/**
	 * add a request to queue  
	 * @param action request actions which is a array
	 * @param sec how long before execute the request
	 * @param obj callback when request done
	 * @return true == success, false == fail
	 */
	public boolean addRequest(ipcAction[] action, int sec, ipcClientListener obj) {
 
		// check worker 
		if (worker == null) {
			worker = new ipcTask();
			worker.execute();
		}

		// add a task into queue
		sec = (1000 * sec);
		long timestamp = System.currentTimeMillis()+ sec;
		try {
			cmdQueueLock.acquire();
			
			QueuedTask newTask = new QueuedTask(action, timestamp, obj);
			if(!cmdQueue.contains(newTask))
				cmdQueue.add(newTask);
			
			cmdQueueLock.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * search specific listener and remove its requests from queue 
	 * @param listenr 
	 */
	public void removeRequest(ipcClientListener obj) {
		try {
			QueuedTask checkObj = new QueuedTask(null, 0, obj);
			cmdQueueLock.acquire();
            cmdQueue.remove(checkObj);
			cmdQueueLock.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 

	/**
	 * remove all requests from queue
	 */
	public void removeAllRequest() {
		try {
			cmdQueueLock.acquire();
            cmdQueue.clear();
			cmdQueueLock.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * disconnect with daemon
	 */
	public void disconnect() {
		if(clientSocket == null)
			return;
		
		try {
			clientSocket.shutdownOutput();
			clientSocket.shutdownInput();
			clientSocket.close();
			clientSocket = null;
		} catch (IOException e) {}
		return;
	}

	/**
	 * wait for specific seconds 
	 * @param seconds
	 */
	private void waitTime(int sec) {
		try {
			// sleep
			Thread.sleep(1000*sec);
		} catch (InterruptedException e) { }
	}
	
	/* AsyncTask */
	private class ipcTask extends AsyncTask<Void, QueuedTask, Void> {

		/**
		 * internal receive buffer
		 */
		private byte[] buffer = new byte[bufferSize];
		private int curBufferSize = bufferSize;

		private boolean prepareIpc() {
			// check connection's status
			if (!checkStatus())
			{ 
				restartDaemon();
				if(!connect())
					return false;
			}
			return true;
		}
		
		/**
		 * background worker for IPC communication
		 */
		@Override
		protected Void doInBackground(Void... params) {
			QueuedTask job = null;
			
			while (true) { 
				
				// check connection's status
				if (!prepareIpc())
				{ 
					waitTime(1);
					continue;
				}
				
				// search jobs
				try {
					cmdQueueLock.acquire();
					job = cmdQueue.peek();
					cmdQueueLock.release();
				} catch (InterruptedException e) {}				
				
				// if no jobs, just wait
				if(job == null) {
					waitTime(1);
					continue;
				}
				
				// if job isn't ready to go , just wait
				if( job.timestamp > System.currentTimeMillis()) 
				{
					waitTime(1);
				    continue;
				}
				
				// process job
				try {
					cmdQueueLock.acquire();
					job = cmdQueue.poll();
					cmdQueueLock.release();
				} catch (InterruptedException e) { }
				
				
				job.result = sendMessage(job);
				
				// if result is empty, just disconnect 
				if(job.result == null) 
					disconnect(); 
			
				publishProgress(job);
				
				job = null;
			}
		}

		/**
		 * send result to the requester
		 * @param job the job has been finished
		 */
		protected void onProgressUpdate(QueuedTask... job) {

			if (job.length == 0)
				return;
			
			QueuedTask procJob = job[job.length - 1];
			
			procJob.listener.onRecvData(procJob.result);
			procJob.listener = null;
			procJob.action = null;
			procJob.result = null;
		}
		
		/**
		 * send request to osmcore and get data when data is ready
		 * @param job the new job
		 * @return result the new data
		 */
		private ipcMessage sendMessage(QueuedTask job) {
			ipcMessage result = null;
			OutputStream outData = null;
			InputStream inData = null; 

			try {
				// prepare ipcMessage
				ipcMessage.Builder ipcmsg = ipcMessage.newBuilder();
				ipcmsg.setType(ipcMessage.ipcType.ACTION);
				for (int index = 0; index < job.action.length; index++) {
					ipcData.Builder data = ipcData.newBuilder();
					data.setAction(job.action[index]);
					ipcmsg.addData(data);
				} 

				// send message and wait result
			
				// send
				outData = clientSocket.getOutputStream();
				ipcmsg.build().writeTo(outData);
 
				// receive (blocking mode)
				inData = clientSocket.getInputStream();

				int totalSize = 0;
    
				// read data size
				if( inData.read(buffer, 0, 4) == 0)
					throw new IOException("Unable to get transfer size"); 
				 
				// convert byte to int 
				totalSize = (int) buffer[0] & 0xFF; 
				totalSize |= (int) (buffer[1] & 0xFF ) << 8;
				totalSize |= (int) (buffer[2] & 0xFF ) << 16; 
				totalSize |= (int) (buffer[3] & 0xFF ) << 24;
				
				// check limit (1M)
				if(totalSize > 1048576)
					throw new Exception("Excced memory limit");
				
				 
				// prepare enough buffer size
				if (curBufferSize < totalSize) {
					buffer = new byte[totalSize];
					curBufferSize = totalSize; 
				} 
				
				// receive data  
				int transferSize = 0; 
				while(transferSize != totalSize) 
					transferSize += inData.read(buffer, transferSize, totalSize-transferSize);
    
				// convert to ipcMessage
				ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer, 0, totalSize+0);
				result = ipcMessage.parseFrom(byteStream);
				byteStream.close();
				byteStream = null; 

			} catch (Exception e) { 
				result = null;
			}
			 
			return result;
		}
	}

}
