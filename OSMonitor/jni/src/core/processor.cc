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

  void processor::gatherProcessor()
  {
    char buffer[BufferSize];
    for (int curNumber = 0; curNumber < MaximumCPUs; curNumber++)
    {
      processorInfo* curProcessor = new processorInfo();
      unsigned int extractValue = 0;

      curProcessor->set_number(curNumber);

      curProcessor->set_offline(false);

      // get processor maximum frequency
      sprintf(buffer, PROCESSOR_FREQ_MAX, curNumber);
      FILE *processorFile = fopen(buffer, "r");
      if (!processorFile)
      {
        curProcessor->set_offline(true);
        curProcessor->set_maxfrequency(0);
        curProcessor->set_minfrequency(0);
        curProcessor->set_currentscaling(0);
        curProcessor->set_maxscaling(0);
        curProcessor->set_minscaling(0);
        curProcessor->set_grovernors("Unknown");
        this->_curProcessorList.push_back(curProcessor);
        continue;
      }
      fscanf(processorFile, "%d", &extractValue);
      curProcessor->set_maxfrequency(extractValue);
      fclose(processorFile);

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

