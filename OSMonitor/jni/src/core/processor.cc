/**
 * @file processor.cc
 * @brief Processor Class file
 */

#include "processor.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  processor::~processor()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curProcessorList);

    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_prevProcessorList);
  }

  processor::processor()
  {
    MaximumCPUs = sysconf(_SC_NPROCESSORS_CONF);
  }

  void processor::resetPermission()
  {
    // check root permission
    if(getuid() != 0)
      return;

    // 644
    mode_t mode = 0;

    char buffer[BufferSize];
    for (int curNumber = 0; curNumber < MaximumCPUs; curNumber++)
    {
      // max cur frequency
      mode = S_IRUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_FREQ_MAX, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // min cur frequency
      mode = S_IRUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_FREQ_MIN, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // scaling cur frequency
      mode = S_IRUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_SCALING_CUR, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // scaling max frequency
      mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_SCALING_MAX, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // scaling min frequency
      mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_SCALING_MIN, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // scaling governor
      mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_SCALING_GOR, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // status
      mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_STATUS, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // available frequency
      mode = S_IRUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_AVAILABLE_FREQ, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);

      // available governors
      mode = S_IRUSR | S_IRGRP | S_IROTH;
      sprintf(buffer, PROCESSOR_AVAILABLE_GOR, curNumber);
      if(mode != getPermission(buffer))
        chmod(buffer, mode);
    }
  }

  mode_t processor::getPermission(const char* fileName)
  {
    struct stat fileStat;
    if(stat(fileName, &fileStat) < 0)
      return (0);
    return (fileStat.st_mode);
  }

  void processor::gatherProcessor()
  {

    char buffer[BufferSize];
    for (int curNumber = 0; curNumber < MaximumCPUs; curNumber++)
    {
      processorInfo* curProcessor = new processorInfo();
      unsigned int extractValue = 0;

      curProcessor->set_number(curNumber);
      curProcessor->set_maxfrequency(-1);
      curProcessor->set_minfrequency(-1);
      curProcessor->set_currentscaling(-1);
      curProcessor->set_maxscaling(-1);
      curProcessor->set_minscaling(-1);
      curProcessor->set_grovernors("Unknown");
      curProcessor->set_offline(false);
      curProcessor->set_avaiablefrequeucy("");
      curProcessor->set_avaiablegovernors("");

      // get processor maximum frequency
      sprintf(buffer, PROCESSOR_FREQ_MAX, curNumber);
      FILE *processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        fscanf(processorFile, "%d", &extractValue);
        curProcessor->set_maxfrequency(extractValue);
        fclose(processorFile);
      }

      // get processor minimum frequency
      sprintf(buffer, PROCESSOR_FREQ_MIN, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        fscanf(processorFile, "%d", &extractValue);
        curProcessor->set_minfrequency(extractValue);
        fclose(processorFile);
      }

      // get scaling cur frequency
      sprintf(buffer, PROCESSOR_SCALING_CUR, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        fscanf(processorFile, "%d", &extractValue);
        curProcessor->set_currentscaling(extractValue);
        fclose(processorFile);
      }

      // get scaling max frequency
      sprintf(buffer, PROCESSOR_SCALING_MAX, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        fscanf(processorFile, "%d", &extractValue);
        curProcessor->set_maxscaling(extractValue);
        fclose(processorFile);
      }

      // get scaling min frequency
      sprintf(buffer, PROCESSOR_SCALING_MIN, curNumber);

      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        fscanf(processorFile, "%d", &extractValue);
        curProcessor->set_minscaling(extractValue);
        fclose(processorFile);
      }

      // get scaling governor
      sprintf(buffer, PROCESSOR_SCALING_GOR, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        char curScaling[BufferSize];
        memset(curScaling, 0, BufferSize);
        fscanf(processorFile, "%64s", curScaling);
        fclose(processorFile);

        curProcessor->set_grovernors(curScaling);
      }

      // status
      sprintf(buffer, PROCESSOR_STATUS, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
          fscanf(processorFile, "%d", &extractValue);
          if(extractValue == 1)
            curProcessor->set_offline(false);
          else
            curProcessor->set_offline(true);
          fclose(processorFile);
      }

      // available frequency
      sprintf(buffer, PROCESSOR_AVAILABLE_FREQ, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        char availableFreq[BufferSize];
        memset(availableFreq, 0, BufferSize);
        fgets(availableFreq, BufferSize, processorFile);
        fclose(processorFile);

        curProcessor->set_avaiablefrequeucy(availableFreq);
      }

      // available grovenors
      sprintf(buffer, PROCESSOR_AVAILABLE_GOR, curNumber);
      processorFile = fopen(buffer, "r");
      if (processorFile)
      {
        char availableGor[BufferSize];
        memset(availableGor, 0, BufferSize);
        fgets(availableGor, BufferSize, processorFile);
        fclose(processorFile);

        curProcessor->set_avaiablegovernors(availableGor);
      }

      this->_curProcessorList.push_back(curProcessor);
    }
  }

  void processor::processOfflineProcessor()
  {
    std::vector<processorInfo*>::iterator iterPrevProcessorInfo = this->_prevProcessorList.begin();
    while (iterPrevProcessorInfo != this->_prevProcessorList.end())
    {
      // lookup CPUs
      bool findProcessor = false;
      std::vector<processorInfo*>::iterator iterCurProcessorInfo = this->_curProcessorList.begin();
      while (iterCurProcessorInfo != this->_curProcessorList.end())
      {
        if ((*iterPrevProcessorInfo)->number() == (*iterCurProcessorInfo)->number())
        {
          findProcessor = true;
          break;
        }
        iterCurProcessorInfo++;
      }

      // if Processor is off-line, just copy old data
      if (findProcessor == true &&  (*iterCurProcessorInfo)->offline() == true)
      {
        (*iterCurProcessorInfo)->set_maxfrequency((*iterPrevProcessorInfo)->maxfrequency());
        (*iterCurProcessorInfo)->set_minfrequency((*iterPrevProcessorInfo)->minfrequency());
        (*iterCurProcessorInfo)->set_currentscaling((*iterPrevProcessorInfo)->currentscaling());
        (*iterCurProcessorInfo)->set_maxscaling((*iterPrevProcessorInfo)->maxscaling());
        (*iterCurProcessorInfo)->set_minscaling((*iterPrevProcessorInfo)->minscaling());
        (*iterCurProcessorInfo)->set_grovernors((*iterPrevProcessorInfo)->grovernors());
      }

      iterPrevProcessorInfo++;
    }
  }

  void processor::refresh()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_prevProcessorList);

    // move
    this->moveDataSet((std::vector<google::protobuf::Message*>&) this->_curProcessorList,
                      (std::vector<google::protobuf::Message*>&) this->_prevProcessorList);

    // check file permission
    this->resetPermission();

    // gather processors
    this->gatherProcessor();

    // process off-line processors
    this->processOfflineProcessor();

    return;
  }

  const std::vector<google::protobuf::Message*>& processor::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curProcessorList);
  }

}
}
}
}

