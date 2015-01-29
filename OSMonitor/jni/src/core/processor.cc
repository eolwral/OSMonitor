/**
 * @file processor.cc
 * @brief Processor Class file
 */

#include "processor.h"
#include <android/log.h>


namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  processor::~processor()
  {
    // clean up
    if (this->_curFlatBuffer != NULL)
      delete this->_curFlatBuffer;

    // clean up
    if (this->_preFlatBuffer != NULL)
      delete this->_preFlatBuffer;
  }

  processor::processor()
  {
    this->_curFlatBuffer = NULL;
    this->_preFlatBuffer = NULL;

    this->_maximumCPUs = getProcessorNumber();
    int DetectedCPUs = sysconf(_SC_NPROCESSORS_CONF);
    if (this->_maximumCPUs < DetectedCPUs)
      this->_maximumCPUs = DetectedCPUs;
  }

  /*
   * Issue 26490: cpufeatures library (NDK) might report wrong number of CPUs/cores
   * https://code.google.com/p/android/issues/detail?id=26490
   */
  int processor::getProcessorNumber()
  {
    int res, i = -1, j = -1;

    /* open file */
    FILE* file = fopen(PROCESSOR_PRESENT, "r");
    if (file == 0)
      return (-1); /* failure */

    /* read and interpret line */
    res = fscanf(file, "%d-%d", &i, &j);

    /* close file */
    fclose(file);

    /* interpret result */
    if (res == 1 && i == 0) /* single-core? */
      return (1);
    if (res == 2 && i == 0 && j < INT_MAX) /* 2+ cores */
      return (j+1);

    return (-1); /* failure */
  }

  void processor::resetAllPermissions()
  {
    // check root permission
    if(getuid() != 0)
      return;

    for (int curNumber = 0; curNumber < this->_maximumCPUs; curNumber++)
    {
      // max cur frequency
      this->resetPermission(curNumber, PROCESSOR_FREQ_MAX, S_IRUSR | S_IRGRP | S_IROTH);

      // min cur frequency
      this->resetPermission(curNumber, PROCESSOR_FREQ_MIN, S_IRUSR | S_IRGRP | S_IROTH);

      // scaling cur frequency
      this->resetPermission(curNumber, PROCESSOR_SCALING_CUR, S_IRUSR | S_IRGRP | S_IROTH);

      // scaling max frequency
      this->resetPermission(curNumber, PROCESSOR_SCALING_MAX, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

      // scaling min frequency
      this->resetPermission(curNumber, PROCESSOR_SCALING_MIN, S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH);

      // scaling governor
      this->resetPermission(curNumber, PROCESSOR_SCALING_GOR, S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH);

      // status
      this->resetPermission(curNumber, PROCESSOR_STATUS, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

      // available frequency
      this->resetPermission(curNumber, PROCESSOR_AVAILABLE_FREQ, S_IRUSR | S_IRGRP | S_IROTH);

      // available governors
      this->resetPermission(curNumber, PROCESSOR_AVAILABLE_GOR, S_IRUSR | S_IRGRP | S_IROTH);
    }
  }

  void processor::resetPermission(int number, const char* pattern, unsigned short mode)
  {
    int statFile = 0;
    struct stat fileStat;
    char buffer[BufferSize];

    sprintf(buffer, pattern, number);

    statFile = open(buffer, O_RDONLY);
    if (statFile < 0)
      return;

    if(fstat(statFile, &fileStat) > 0)
    {
      if (fileStat.st_mode != mode)
      {
        if(fchmod(statFile, mode) != 0)
          __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Unable to reset file permission");
      }
    }

    close(statFile);
  }

  void processor::gatherProcessor()
  {

    char extractString[BufferSize];

    unsigned int minFrequency = 0;
    unsigned int maxFrequency = 0;
    unsigned int curScaling = 0;
    unsigned int maxScaling = 0;
    unsigned int minScaling = 0;
    unsigned int threshold = 0;
    Offset<String> governors = 0;
    Offset<String> availableFrequency = 0;
    Offset<String> availableGovernors = 0;
    bool offLine = true;

    for (int curNumber = 0; curNumber < this->_maximumCPUs; curNumber++)
    {
      const processorInfo *prevProcessor = this->getPrevProcessor(curNumber);

      // get processor maximum frequency
      maxFrequency = this->getProcessorValue(curNumber, PROCESSOR_FREQ_MAX);
      if (maxFrequency != 0)
        threshold++;
      else if (prevProcessor != NULL)
        maxFrequency = prevProcessor->maxFrequency();

      // get processor minimum frequency
      minFrequency = this->getProcessorValue(curNumber, PROCESSOR_FREQ_MIN);
      if (minFrequency != 0)
        threshold++;
      else if (prevProcessor != NULL)
        minFrequency = prevProcessor->minFrequency();

      // get scaling cur frequency
      curScaling = this->getProcessorValue(curNumber, PROCESSOR_SCALING_CUR);
      if (curScaling != 0)
        threshold++;
      else if (prevProcessor != NULL)
        curScaling = prevProcessor->currentScaling();

      // get scaling max frequency
      maxScaling = this->getProcessorValue(curNumber, PROCESSOR_SCALING_MAX);
      if (maxScaling != 0)
        threshold++;
      else if (prevProcessor != NULL)
        maxScaling = prevProcessor->maxScaling();

      // get scaling min frequency
      minScaling = this->getProcessorValue(curNumber, PROCESSOR_SCALING_MIN);
      if (minScaling != 0)
        threshold++;
      else if (prevProcessor != NULL)
        minScaling = prevProcessor->minScaling();

      // get scaling governor
      if (this->getProcessorString(curNumber, PROCESSOR_SCALING_GOR, extractString, BufferSize))
        threshold++;
      else if (prevProcessor != NULL)
        strncpy(extractString, prevProcessor->governors()->c_str(), BufferSize-1);
      else
        strncpy(extractString, "", BufferSize);
      governors = this->_curFlatBuffer->CreateString(extractString);

      // available frequency
      if (this->getProcessorString(curNumber, PROCESSOR_AVAILABLE_FREQ, extractString, BufferSize))
        threshold++;
      else if (prevProcessor != NULL)
        strncpy(extractString, prevProcessor->availableFrequency()->c_str(), BufferSize-1);
      else
        strncpy(extractString, "", BufferSize);
      availableFrequency = this->_curFlatBuffer->CreateString(extractString);

      // available governors
      if (this->getProcessorString(curNumber, PROCESSOR_AVAILABLE_GOR, extractString, BufferSize))
        threshold++;
      else if (prevProcessor != NULL)
        strncpy(extractString, prevProcessor->availableGovernors()->c_str(), BufferSize-1);
      else
        strncpy(extractString, "", BufferSize);
      availableGovernors = this->_curFlatBuffer->CreateString(extractString);

      // status
      if(this->getProcessorValue(curNumber, PROCESSOR_STATUS) != 0)
        offLine = false;
      else
        offLine = true;

      // some devices don't have a status file, but CPU is online.
      // if we got enough data, it should be online.
      // if (threshold >= 6) offLine = false;

      processorInfoBuilder curProcessor(*this->_curFlatBuffer);
      curProcessor.add_number(curNumber);
      curProcessor.add_maxFrequency(maxFrequency);
      curProcessor.add_minFrequency(minFrequency);
      curProcessor.add_currentScaling(curScaling);
      curProcessor.add_maxScaling(maxScaling);
      curProcessor.add_minScaling(minScaling);
      curProcessor.add_governors(governors);
      curProcessor.add_availableFrequency(availableFrequency);
      curProcessor.add_availableGovernors(availableGovernors);
      curProcessor.add_offLine(offLine);
      this->_list.push_back(curProcessor.Finish());
    }
  }

  bool processor::getProcessorString(int number, const char* fileName, char* extractString, int extractLen)
  {
    char buffer[BufferSize];

    memset(extractString, 0, extractLen);
    sprintf(buffer, fileName, number);
    FILE *processorFile = fopen(buffer, "r");
    if (processorFile)
    {
      fgets(extractString, extractLen, processorFile);
      fclose(processorFile);
      return true;
    }
    return false;
  }

  int processor::getProcessorValue(int number, const char* fileName)
  {
    char buffer[BufferSize];
    unsigned int extractValue = 0;

    memset(buffer, 0, BufferSize);
    sprintf(buffer, fileName, number);
    FILE *processorFile = fopen(buffer, "r");
    if (processorFile)
    {
      if (fscanf(processorFile, "%d", &extractValue) != 1)
        extractValue = 0;
      fclose(processorFile);
    }
    else
      extractValue = -1;

    return extractValue;
  }

  const processorInfo* processor::getPrevProcessor(int number)
  {
    if (this->_preFlatBuffer == NULL)
      return NULL;

    const processorInfoList *prevProcessorInfoList = GetprocessorInfoList(this->_preFlatBuffer->GetBufferPointer());
    for (int iterCPU = 0; iterCPU < prevProcessorInfoList->list()->Length(); iterCPU++)
    {
      const processorInfo *prevProcessorInfo = prevProcessorInfoList->list()->Get(iterCPU);
      if (prevProcessorInfo->number() == number) {
        return prevProcessorInfo;
      }
    }
    return NULL;
  }

  void processor::prepareBuffer()
  {
    if (this->_preFlatBuffer != NULL)
      delete this->_preFlatBuffer;

    this->_preFlatBuffer = this->_curFlatBuffer;
    this->_curFlatBuffer = new FlatBufferBuilder();
    this->_list.clear();
  }

  void processor::finishBuffer()
  {
    auto mloc = CreateprocessorInfoList(*this->_curFlatBuffer, this->_curFlatBuffer->CreateVector(this->_list));
    FinishprocessorInfoListBuffer(*this->_curFlatBuffer, mloc);
  }

  void processor::refresh()
  {
    // clean up
    this->prepareBuffer();

    // check file permission
    this->resetAllPermissions();

    // gather processors
    this->gatherProcessor();

    // create a cpuInfoList
    this->finishBuffer();

    return;
  }

  const uint8_t* processor::getData()
  {
    return this->_curFlatBuffer->GetBufferPointer();
  }

  const uoffset_t processor::getSize()
  {
    return this->_curFlatBuffer->GetSize();
  }

}
}
}
}

