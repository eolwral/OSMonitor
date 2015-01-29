// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class cpuInfo extends Table {
  public static cpuInfo getRootAscpuInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new cpuInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public cpuInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// normal processes executing in user mode 
  public long userTime() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// niced processes executing in user mode 
  public long niceTime() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// processes executing in kernel mode 
  public long systemTime() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// twiddling thumbs 
  public long idleTime() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// waiting for I/O to complete 
  public long ioWaitTime() { int o = __offset(12); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// servicing interrupts 
  public long irqTime() { int o = __offset(14); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// servicing softirqs 
  public long softIrqTime() { int o = __offset(16); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// CPU utilization 
  public float cpuUtilization() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  /// Io utilization
  public float ioUtilization() { int o = __offset(20); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  /// CPU Time 
  public long cpuTime() { int o = __offset(22); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// CPU Number 
  public int cpuNumber() { int o = __offset(24); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// CPU Offline 
  public byte offLine() { int o = __offset(26); return o != 0 ? bb.get(o + bb_pos) : 0; }

  public static int createcpuInfo(FlatBufferBuilder builder,
      long userTime,
      long niceTime,
      long systemTime,
      long idleTime,
      long ioWaitTime,
      long irqTime,
      long softIrqTime,
      float cpuUtilization,
      float ioUtilization,
      long cpuTime,
      int cpuNumber,
      byte offLine) {
    builder.startObject(12);
    cpuInfo.addCpuTime(builder, cpuTime);
    cpuInfo.addSoftIrqTime(builder, softIrqTime);
    cpuInfo.addIrqTime(builder, irqTime);
    cpuInfo.addIoWaitTime(builder, ioWaitTime);
    cpuInfo.addIdleTime(builder, idleTime);
    cpuInfo.addSystemTime(builder, systemTime);
    cpuInfo.addNiceTime(builder, niceTime);
    cpuInfo.addUserTime(builder, userTime);
    cpuInfo.addCpuNumber(builder, cpuNumber);
    cpuInfo.addIoUtilization(builder, ioUtilization);
    cpuInfo.addCpuUtilization(builder, cpuUtilization);
    cpuInfo.addOffLine(builder, offLine);
    return cpuInfo.endcpuInfo(builder);
  }

  public static void startcpuInfo(FlatBufferBuilder builder) { builder.startObject(12); }
  public static void addUserTime(FlatBufferBuilder builder, long userTime) { builder.addLong(0, userTime, 0); }
  public static void addNiceTime(FlatBufferBuilder builder, long niceTime) { builder.addLong(1, niceTime, 0); }
  public static void addSystemTime(FlatBufferBuilder builder, long systemTime) { builder.addLong(2, systemTime, 0); }
  public static void addIdleTime(FlatBufferBuilder builder, long idleTime) { builder.addLong(3, idleTime, 0); }
  public static void addIoWaitTime(FlatBufferBuilder builder, long ioWaitTime) { builder.addLong(4, ioWaitTime, 0); }
  public static void addIrqTime(FlatBufferBuilder builder, long irqTime) { builder.addLong(5, irqTime, 0); }
  public static void addSoftIrqTime(FlatBufferBuilder builder, long softIrqTime) { builder.addLong(6, softIrqTime, 0); }
  public static void addCpuUtilization(FlatBufferBuilder builder, float cpuUtilization) { builder.addFloat(7, cpuUtilization, 0); }
  public static void addIoUtilization(FlatBufferBuilder builder, float ioUtilization) { builder.addFloat(8, ioUtilization, 0); }
  public static void addCpuTime(FlatBufferBuilder builder, long cpuTime) { builder.addLong(9, cpuTime, 0); }
  public static void addCpuNumber(FlatBufferBuilder builder, int cpuNumber) { builder.addInt(10, cpuNumber, 0); }
  public static void addOffLine(FlatBufferBuilder builder, byte offLine) { builder.addByte(11, offLine, 0); }
  public static int endcpuInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

