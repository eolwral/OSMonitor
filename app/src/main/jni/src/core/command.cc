/**
 * @file command.cpp
 * @brief Command Class file
 */

#include <processor.h>
#include <command.h>
#include <android/log.h>


namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  command::command(ipc::ipcCategory category, const commandInfo* info)
  {
    this->category = category;
    this->info = (commandInfo *) info;
  }

  void command::setPriority()
  {

    if (info->arguments() == 0 || info->arguments()->size() < 2)
      return;

    int pid = atoi((*info->arguments()->Get(0)).c_str());
    int priority = atoi((*info->arguments()->Get(1)).c_str());

    setpriority(PRIO_PROCESS, pid, priority);
    return;
  }

  void command::killProcess()
  {
    if (info->arguments() == 0 || info->arguments()->Length() < 1)
      return;

    int pid = atoi((*info->arguments()->Get(0)).c_str());
    kill(pid, SIGKILL);
    return;
  }

  void command::setCPUStatus()
  {
    if (info->arguments() == 0 || info->arguments()->Length() < 2)
      return;

    int cpu = atoi((*info->arguments()->Get(0)).c_str());
    short status = atoi((*info->arguments()->Get(1)).c_str());

    mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH;

    char buffer[BufferSize];
    sprintf(buffer, PROCESSOR_STATUS, cpu);
    if (chmod(buffer, mode) == 0)
    {
      FILE *processorFile = fopen(buffer, "w");
      if (processorFile)
      {
        fprintf(processorFile, "%d", status);
        fclose(processorFile);
      }
    }
    return;
  }

  void command::setCPUMaxFrequency()
  {
    if (info->arguments() == 0 || info->arguments()->Length() < 2)
      return;

    int cpu = atoi((*info->arguments()->Get(0)).c_str());
    const char* freq = (*info->arguments()->Get(1)).c_str();

    mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

    char buffer[BufferSize];
    sprintf(buffer, PROCESSOR_SCALING_MAX, cpu);
    if (chmod(buffer, mode) == 0)
    {
      FILE *processorFile = fopen(buffer, "w");
      if (processorFile)
      {
        fprintf(processorFile, "%s", freq);
        fclose(processorFile);
      }
    }
  }

  void command::setCPUMinFrequency()
  {
    if (info->arguments() == 0 || info->arguments()->Length() < 2)
      return;

    int cpu = atoi((*info->arguments()->Get(0)).c_str());
    const char* freq = (*info->arguments()->Get(1)).c_str();

    mode_t mode =S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

    char buffer[BufferSize];
    sprintf(buffer, PROCESSOR_SCALING_MIN, cpu);
    if ( chmod(buffer, mode) == 0)
    {
      FILE *processorFile = fopen(buffer, "w");
      if (processorFile)
      {
        fprintf(processorFile, "%s", freq);
        fclose(processorFile);
      }
    }
    return;
  }

  void command::setCPUGovernor()
  {
    if (info->arguments() == 0 || info->arguments()->Length() < 2)
      return;

    int cpu = atoi((*info->arguments()->Get(0)).c_str());
    const char* gov = (*info->arguments()->Get(1)).c_str();

    mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

    char buffer[BufferSize];
    sprintf(buffer, PROCESSOR_SCALING_GOR, cpu);
    if ( chmod(buffer, mode) == 0 )
    {
      FILE *processorFile = fopen(buffer, "w");
      if (processorFile)
      {
        fprintf(processorFile, "%s", gov);
        fclose(processorFile);
      }
    }
    return;
  }

  void command::execute()
  {
    if (info == NULL) return;

    switch (category)
    {
      case ipc::ipcCategory_SETPRIORITY:
        this->setPriority();
        break;
      case ipc::ipcCategory_KILLPROCESS:
        this->killProcess();
        break;
      case ipc::ipcCategory_SETCPUSTATUS:
        this->setCPUStatus();
        break;
      case ipc::ipcCategory_SETCPUMAXFREQ:
        this->setCPUMaxFrequency();
        break;
      case ipc::ipcCategory_SETCPUMINFREQ:
        this->setCPUMinFrequency();
        break;
      case ipc::ipcCategory_SETCPUGORV:
        this->setCPUGovernor();
        break;
    }
    return;
  }
}
}
}
}
