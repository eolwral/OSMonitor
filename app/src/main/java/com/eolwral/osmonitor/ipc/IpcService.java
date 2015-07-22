package com.eolwral.osmonitor.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.os.AsyncTask;

import com.eolwral.osmonitor.util.CoreUtil;
import com.eolwral.osmonitor.core.commandInfo;
import com.eolwral.osmonitor.settings.Settings;
import com.google.flatbuffers.FlatBufferBuilder;

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
   * Connection Object
   */
  private IpcConnection client = null;
  
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
    public byte[] action = null;
    public long timestamp = 0;
    public ipcClientListener listener = null;
    public byte [] result = null;

    /**
     * constructor
     */
    QueuedTask(byte[] action, long timestamp, ipcClientListener listener) {
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
    public void onRecvData(byte [] result);
  }

  /**
     * background worker for IpcService 
     */
  private static ipcTask worker = null;

  /**
   * initialize IpcService
   * @param[in] context
   */
  public static void Initialize(Context context) {
    if (instance == null)
    {
      instance = new IpcService();
      instance.ipcContext = context;
      instance.createConnection();
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
   * @param[in] context Context
   */
  private IpcService() {    
    // prepare a priority queue
    QueuedComparator cmdComparator = new QueuedComparator();
    cmdQueue = new PriorityQueue<QueuedTask>(1, cmdComparator);
  }
  
  /**
   * create a connection 
   */
  public void createConnection() {
    //clean up connection
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) { }
    }
    
    // create ipcConnection
    client = new IpcConnection(CoreUtil.getSocketName(instance.ipcContext));
  }
   
  /**
   * destructor for ipcClient
   */
  protected void finalize() {
    try {
      if (client != null)
        client.close();
    } catch (IOException e) { }
  }

  /**
   * connection to osmcore
   * @return true == success, false == fail
   */
  private boolean connect() {
  
    try {
      Settings settings = Settings.getInstance(ipcContext); 

      client.connect(settings.getInterval()*1000);

      // send token
      OutputStream outData = client.getOutputStream();
      if (outData == null)
        throw new IOException();

      byte [] outToken = settings.getToken().getBytes();
      outData.write(outToken);

    } catch (IOException e) {
      return false;
    } catch (Exception e) {
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
    if (client == null)
      return false;
    return client.isConnected();
  }
  
  /**
   * restart the daemon
   * @return true == success, false == fail
   */
  private boolean restartDaemon() {
    return CoreUtil.execCore(ipcContext);
  }

  /**
   * force connect to osmcore
   * @return true == connected, false == not connected
   */
  public boolean forceConnect() {
    if(restartDaemon()) {
      waitTime(1);
      return connect();
    }
    return false;
  }

  /**
   * send a force exit command
   */
  public void forceExit() {
    if(!checkStatus()) 
      return;

    FlatBufferBuilder flatbuffer = new FlatBufferBuilder(1);

    // prepare an empty ipcData
    int [] ipcDataArray = new int[1];
    byte [] ipcDataPayLoad = new byte[1];
    int emptyPayLoad = ipcData.createPayloadVector(flatbuffer, ipcDataPayLoad);
    ipcDataArray[0] = ipcData.createipcData(flatbuffer, ipcCategory.NONEXIST, emptyPayLoad);
    int ipcDataList = ipcMessage.createDataVector(flatbuffer, ipcDataArray);

    // prepare an ipcMessage
    ipcMessage.startipcMessage(flatbuffer);
    ipcMessage.addType(flatbuffer, ipcType.EXIT);
    ipcMessage.addData(flatbuffer, ipcDataList);
    int message = ipcMessage.endipcMessage(flatbuffer);
    ipcMessage.finishipcMessageBuffer(flatbuffer, message);

    // send
    try {
      OutputStream outputStream = client.getOutputStream();
      outputStream.write(flatbuffer.sizedByteArray());
    } catch (IOException e) {}

    return;
  }

  /**
   * send command to osmcore
   * @param[in] category command type
   * @param[in] arguments arguments for command
   */
  public void sendCommand(byte category, Object... arguments) {
    FlatBufferBuilder cmdFlatBuffer = new FlatBufferBuilder(1);
    
    // prepare arguments of commandInfo
    int commandArgsIndex = 0;
    int [] commandArgs = new int[arguments.length];
    for (Object argument: arguments) {
      if (String.class.isInstance(argument)) 
        commandArgs[commandArgsIndex] = cmdFlatBuffer.createString((String) argument);
      else
        commandArgs[commandArgsIndex] = cmdFlatBuffer.createString(argument.toString());
      commandArgsIndex++;
    }

    // prepare commandInfo
    int commandArgsList = commandInfo.createArgumentsVector(cmdFlatBuffer, commandArgs);
    int commandInfoObject = commandInfo.createcommandInfo(cmdFlatBuffer, commandArgsList);
    commandInfo.finishcommandInfoBuffer(cmdFlatBuffer, commandInfoObject);

    // prepare the payload of ipcMessage
    FlatBufferBuilder flatbuffer = new FlatBufferBuilder(1);
    int commandPayLoad = ipcData.createPayloadVector(flatbuffer, cmdFlatBuffer.sizedByteArray());

    // prepare an ipcMessage
    int [] cmdDataArray = new int[1];
    cmdDataArray[0] = ipcData.createipcData(flatbuffer, category, commandPayLoad);
    int cmdDataList = ipcMessage.createDataVector(flatbuffer, cmdDataArray);
    int cmdMessage = ipcMessage.createipcMessage(flatbuffer, ipcType.COMMAND, cmdDataList);
    ipcMessage.finishipcMessageBuffer(flatbuffer, cmdMessage);

    // send
    try {
      OutputStream outputStream = client.getOutputStream();
      outputStream.write(flatbuffer.sizedByteArray());
    } catch (IOException e) {}

  }


  /**
   * add a request to queue  
   * @param[in] action request actions which is a array
   * @param[in] sec how long before execute the request
   * @param[in] obj callback when request done
   * @return true == success, false == fail
   */
  public boolean addRequest(byte[] action, int sec, ipcClientListener obj) {
 
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
   * @param[in] listener 
   */
  public void removeRequest(ipcClientListener obj) {
    try {
      QueuedTask checkObj = new QueuedTask(null, 0, obj);
      cmdQueueLock.acquire();
      cmdQueue.remove(checkObj);
      cmdQueueLock.release();
    } catch (InterruptedException e) { }
  } 

  /**
   * remove all requests from queue
   */
  public void removeAllRequest() {
    try {
      cmdQueueLock.acquire();
      cmdQueue.clear();
      cmdQueueLock.release();
    } catch (InterruptedException e) { }
  }
  
  /**
   * disconnect with daemon
   */
  public void disconnect() {
    if(client == null) {
      return;
    }

    removeAllRequest();

    try {
      client.close();
    } catch (Exception e) {}
    return;
  }

  /**
   * wait for specific seconds 
   * @param[in] seconds
   */
  private void waitTime(int sec) {
    try {
      // sleep
      Thread.sleep(1000*sec);
    } catch (InterruptedException e) { }
  }
  
  /* AsyncTask */
  private class ipcTask extends AsyncTask<Void, QueuedTask, Void> {

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
        
        // if no jobs, just wait
        if(job == null) {
          waitTime(1);
          continue;
        }
        
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
     * @param[in] job the job has been finished
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
     * @param[in] job the new job
     * @return result the new data
     */
    private byte [] sendMessage(QueuedTask job) {

      byte [] result = null;

      try {
        // prepare ipcMessage
        FlatBufferBuilder flatbuffer = new FlatBufferBuilder(1);

        // prepare a vector for ipcData
        int [] ipcDataArray = new int[job.action.length];
        for (int index = 0; index < job.action.length; index++) {
          // prepare empty data payload
          byte [] ipcDataPayLoad = new byte[1];
          int emptyPayLoad = ipcData.createPayloadVector(flatbuffer, ipcDataPayLoad);
          ipcDataArray[index] = ipcData.createipcData(flatbuffer, job.action[index], emptyPayLoad);
        }

        int ipcDataList = ipcMessage.createDataVector(flatbuffer, ipcDataArray);

        // prepare ipcMessage
        ipcMessage.startipcMessage(flatbuffer);
        ipcMessage.addType(flatbuffer, ipcType.ACTION);
        ipcMessage.addData(flatbuffer, ipcDataList);
        int message = ipcMessage.endipcMessage(flatbuffer);
        ipcMessage.finishipcMessageBuffer(flatbuffer, message);

        // send message and wait result
        OutputStream outputStream = client.getOutputStream();
        InputStream inputStream = client.getInputStream();

        // send
        outputStream.write(flatbuffer.sizedByteArray());

        // receive (blocking mode) & read data size
        int totalSize = 0;
        byte[] sizeBuffer = new byte[4];
        if( inputStream.read(sizeBuffer, 0, 4) == 0)
          throw new IOException("Unable to get transfer size"); 

        // convert byte to int 
        totalSize = (int) sizeBuffer[0] & 0xFF; 
        totalSize |= (int) (sizeBuffer[1] & 0xFF ) << 8;
        totalSize |= (int) (sizeBuffer[2] & 0xFF ) << 16; 
        totalSize |= (int) (sizeBuffer[3] & 0xFF ) << 24;

        // check limit (10M)
        if(totalSize > IpcConnection.recvBufferSize)
          throw new Exception("Excced memory limit");

        if (totalSize == 0)
          throw new Exception("No Data");

        // prepare enough buffer size
        result = new byte[totalSize];

        // receive data  
        int transferSize = 0; 
        while(transferSize != totalSize)  
          transferSize += inputStream.read(result, transferSize, totalSize-transferSize);

      } catch (Exception e) {
        result = null;
      }

      return result;
    }
  }

}
