/**
 * @file core.cc
 * @brief Core library for OSMonitor
 * @date 2012/09/16
 * @version 0.1
 * @author eolwral@gmail.com
 *
 *  Main core program
 */


// Linux
#include <string.h>
#include <unistd.h>

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <sys/resource.h>

// STL
#include <vector>

// Android
#include <android/log.h>
#define APPNAME "OSMCore"

// OSMCore library
#include <os.h>
#include <cpu.h>
#include <processor.h>
#include <process.h>
#include <connection.h>
#include <network.h>
#include <dmesg.h>
#include <logcat.h>

// OSMIPC library
#include <ipcserver.h>

#define BufferSize 256

// using name space
using namespace com::eolwral::osmonitor;

// global variables
static ipc::ipcserver server;
static ipc::ipcMessage command;

// system object
static std::vector<core::base *> adapter;

// for cache mechanize
struct cachedData {
  int id;
  int time;
  ipc::ipcData data;
};

static std::vector<cachedData *> storage;

// buffer
bool endLoop = false;
static char* buffer = NULL;
static int bufferSize = 0;

bool prepareIPC()
{
  // initialize
  bool result = false;

#ifdef ANDROD_L_BINARY
  result = server.init(PORTNUMBER);
#else
  result = server.init(SOCKETNAME);
#endif
  if (!result)
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"can't initialize socket!\n");
    return (false);
  }

  // prepare socket
  if(!server.bind())
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"can't bind socket!\n");
    return (false);
  }

  return (true);
}

bool prepareBuffer(int requireSize)
{
  // check size
  if(requireSize > bufferSize)
  {
    // reallocate buffer
    if(buffer != NULL)
      delete buffer;
    buffer = new char[requireSize];

    // check buffer
    if(buffer == NULL)
      bufferSize = 0;
    else
      bufferSize = requireSize;
  }

  // valid buffer
  if(bufferSize == 0)
    return (false);

  // clean up
  memset(buffer, 0, bufferSize);
  return (true);
}

bool checkToken()
{
  // check token
   if(server.isVerified())
     return (true);

   server.checkToken();
   return (false);
}

bool receiveCMD()
{
  if(!prepareBuffer(1024))
    return (false);

  // clean up
  command.Clear();

  int recvSize = 0;
  if(!server.receieve(buffer, bufferSize, recvSize))
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"can't receive data!\n");
    return (false);
  }

  if(!command.ParseFromArray(buffer, recvSize))
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"can't parse data!\n");
    return (false);
  }

  return (true);
}

bool sendData(ipc::ipcMessage& result)
{
  if(result.has_type() == false) {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "action type is empty!\n");
    return (false);
  }

  if(!prepareBuffer(result.ByteSize()))
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "can't prepare send buffer!\n");
    return (false);
  }

  if(!result.SerializeToArray(buffer, result.GetCachedSize()))
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"can't serialize!\n");
    return (false);
  }

  if(!server.send(buffer, result.GetCachedSize()))
  {
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "can't send data!\n");
    return (false);
  }
  return (true);
}


bool fillPayload(const std::vector<google::protobuf::Message *>& objList,
                 ipc::ipcData& result)
{
  // put data into payload
  for (int ptr = 0; ptr < objList.size(); ptr++)
    result.add_payload(objList[ptr]->SerializeAsString());

  return (true);
}

void initAdapter()
{
  // initial the array
  for (int i = 0; i < ipc::ipcAction_MAX+1; i++)
  {
    adapter.push_back(NULL);
    storage.push_back(NULL);
  }
}

bool prepareAdapter(ipc::ipcAction action)
{
  // check and clean up when processing _R
  if (adapter[action] != NULL)
  {
    switch (action)
    {
    case ipc::LOGCAT_MAIN_R:
      delete adapter[action];
      adapter[action] = NULL;
      break;

    default:
      return (true);
    }
  }

  // prepare
  switch(action)
  {
    case ipc::CPU:
      adapter[ipc::CPU] = (core::base*) new core::cpu();
      break;
    case ipc::PROCESSOR:
      adapter[ipc::PROCESSOR] = (core::base *) new core::processor();
      break;
    case ipc::OS:
      adapter[ipc::OS] =(core::base *) new core::os();
      break;
    case ipc::PROCESS:
      adapter[ipc::PROCESS] =(core::base *) new core::process();
      break;
    case ipc::CONNECTION:
      adapter[ipc::CONNECTION] =(core::base *) new core::connection();
      break;
    case ipc::NETWORK:
      adapter[ipc::NETWORK] =(core::base *) new core::network();
      break;

    case ipc::DMESG:
      adapter[ipc::DMESG] =(core::base *) new core::dmesg();
      break;

    case ipc::LOGCAT_RADIO:
      adapter[ipc::LOGCAT_RADIO] =
                 (core::base *) new core::logcat(core::RADIO);
      break;

    case ipc::LOGCAT_EVENT:
      adapter[ipc::LOGCAT_EVENT] =
                 (core::base *) new core::logcat(core::EVENTS);
      break;

    case ipc::LOGCAT_SYSTEM:
      adapter[ipc::LOGCAT_SYSTEM] =
                 (core::base *) new core::logcat(core::SYSTEM);
      break;

    case ipc::LOGCAT_MAIN:
      adapter[ipc::LOGCAT_MAIN] =
                 (core::base *) new core::logcat(core::MAIN);
      break;

    case ipc::LOGCAT_MAIN_R:
      adapter[ipc::LOGCAT_MAIN_R] =
                 (core::base *) new core::logcat(core::MAIN);
      break;

    default:
      return (false);
  }

  return (true);
}

void cleanUp()
{
  // close all clients
  server.clean();

  // remove and empty all data
  for (int index = 0; index < ipc::ipcAction_MAX + 1; index++)
  {
    if (adapter[index] != NULL)
      delete adapter[index];
    if (storage[index] != NULL)
      delete storage[index];
  }
  return;
}

bool processActionMsg()
{
  // current result
  ipc::ipcMessage result;
  bool flag = true;

  //prepare ipcMessage
  result.Clear();

  // result
  result.set_type(ipc::ipcMessage::RESULT);

  // process ACTION message
  for (int index = 0; index < command.data_size(); index++)
  {

    // get data
    ipc::ipcData data = command.data(index);

    // check cache status and use cached data
    if (storage[data.action()] != NULL)
    {
      if (storage[data.action()]->id != server.getClientId()
       && storage[data.action()]->time > (time(NULL) - 3))
      {
        ipc::ipcData* newData = result.add_data();
        newData->set_action(data.action());
        newData->CopyFrom(storage[data.action()]->data);
        continue;
      }
    }

    // prepare
    if (!prepareAdapter(data.action()))
    {
      flag = false;
      break;
    }

    // refresh
    (adapter[data.action()])->refresh();

    // get list
    const std::vector<google::protobuf::Message *>& objList =
                                 adapter[data.action()]->getData();

    // add a new data
    ipc::ipcData* newData = result.add_data();

    // fill data
    newData->set_action(data.action());
    fillPayload(objList, *newData);

    // set cached data as empty
    if (storage[data.action()] != NULL)
    {
      delete storage[data.action()];
      storage[data.action()] = NULL;
    }

    // save data into cache storage
    cachedData *newCache = new cachedData();
    newCache->id = server.getClientId();
    newCache->data.CopyFrom(*newData);
    newCache->time = time(NULL);
    storage[data.action()] = newCache;
    newCache = NULL;
  }

  // clear up
  command.Clear();

  // send data
  if (!sendData(result))
  {
    server.close();
    flag = false;
  }

  return (flag);
}

void processCommandMsg()
{
  // process ACTION message
  for (int index = 0; index < command.data_size(); index++)
  {
    // get data
    ipc::ipcData data = command.data(index);

    // set priority
    if(data.action() == ipc::SETPRIORITY)
    {
      int pid = atoi(data.payload(0).c_str());
      int priority = atoi(data.payload(1).c_str());
      setpriority(PRIO_PROCESS, pid, priority);
      continue;
    }

    // kill process
    if(data.action() == ipc::KILLPROCESS)
    {
      int pid = atoi(data.payload(0).c_str());
      kill(pid, SIGKILL);
      continue;
    }

    // set CPU status
    if(data.action() == ipc::SETCPUSTATUS)
    {
        int cpu = atoi(data.payload(0).c_str());
        short status = atoi(data.payload(1).c_str());
        mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH;

        char buffer[BufferSize];
        sprintf(buffer, PROCESSOR_STATUS, cpu);
        chmod(buffer, mode);
        FILE *processorFile = fopen(buffer, "w");
        if (processorFile)
        {
          fprintf(processorFile, "%d", status);
          fclose(processorFile);
        }
        continue;
    }

    // set CPU max frequency
    if(data.action() == ipc::SETCPUMAXFREQ)
    {
        int cpu = atoi(data.payload(0).c_str());
        const char* freq = data.payload(1).c_str();
        mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

        char buffer[BufferSize];
        sprintf(buffer, PROCESSOR_SCALING_MAX, cpu);
        chmod(buffer, mode);
        FILE *processorFile = fopen(buffer, "w");
        if (processorFile)
        {
          fprintf(processorFile, "%s", freq);
          fclose(processorFile);
        }
        continue;
    }

    // set CPU min frequency
    if(data.action() == ipc::SETCPUMINFREQ)
    {
        int cpu = atoi(data.payload(0).c_str());
        const char* freq = data.payload(1).c_str();
        mode_t mode =S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

        char buffer[BufferSize];
        sprintf(buffer, PROCESSOR_SCALING_MIN, cpu);
        chmod(buffer, mode);
        FILE *processorFile = fopen(buffer, "w");
        if (processorFile)
        {
          fprintf(processorFile, "%s", freq);
          fclose(processorFile);
        }
        continue;
    }

    // set CPU governor
    if(data.action() == ipc::SETCPUGORV)
    {
        int cpu = atoi(data.payload(0).c_str());
        const char* gov = data.payload(1).c_str();
        mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

        char buffer[BufferSize];
        sprintf(buffer, PROCESSOR_SCALING_GOR, cpu);
        chmod(buffer, mode);
        FILE *processorFile = fopen(buffer, "w");
        if (processorFile)
        {
          fprintf(processorFile, "%s", gov);
          fclose(processorFile);
        }
        continue;
    }



  }

  // clear up
  command.Clear();

  return;
}

bool processCommand()
{
  // check token
  if(checkToken() == false)
    return (true);

  // receive ipcMessage
  if( receiveCMD() == false)
    return (true);

  // process Message
  switch(command.type())
  {

  // process EXIT message
  case ipc::ipcMessage::EXIT:
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"force Exit\n");
    endLoop = true;
    return (false);

  // process ACTION message
  case ipc::ipcMessage::ACTION:
    return (processActionMsg());

  case ipc::ipcMessage::COMMAND:
    processCommandMsg();
    return (true);
  }

  return (false);
}

int main(int argc, char* argv[])
{
  if (argc == 1)
    return (1);

  // prepare IPC
  if(!prepareIPC())
    return (2);

  initAdapter();

  // extract and erase Token
  server.extractToken(argv[1]);

  // receive commands
  endLoop = false;
  while(!endLoop)
  {
    ipc::ipcserver::EVENT event = server.poll();
    switch(event)
    {
    // error
    case ipc::ipcserver::ERROR:
      endLoop = true;
      break;

    // wait clients
    case ipc::ipcserver::WAIT:
      // do nothing
      break;

    // accept connections
    case ipc::ipcserver::CONNECTION:
      server.accept();
      break;

    // process command
    case ipc::ipcserver::COMMAND:
      processCommand();
      break;
    }
  }

  // clean up
  cleanUp();

  return (0);
}


