// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class osInfo extends Table {
  public static osInfo getRootAsosInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new osInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public osInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// system uptime 
  public long upTime() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// total memory
  public long totalMemory() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// free memory
  public long freeMemory() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// shared memory
  public long sharedMemory() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// buffered memory
  public long bufferedMemory() { int o = __offset(12); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// cached memory
  public long cachedMemory() { int o = __offset(14); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// total swap size
  public long totalSwap() { int o = __offset(16); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// free swap size
  public long freeSwap() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static int createosInfo(FlatBufferBuilder builder,
      long upTime,
      long totalMemory,
      long freeMemory,
      long sharedMemory,
      long bufferedMemory,
      long cachedMemory,
      long totalSwap,
      long freeSwap) {
    builder.startObject(8);
    osInfo.addFreeSwap(builder, freeSwap);
    osInfo.addTotalSwap(builder, totalSwap);
    osInfo.addCachedMemory(builder, cachedMemory);
    osInfo.addBufferedMemory(builder, bufferedMemory);
    osInfo.addSharedMemory(builder, sharedMemory);
    osInfo.addFreeMemory(builder, freeMemory);
    osInfo.addTotalMemory(builder, totalMemory);
    osInfo.addUpTime(builder, upTime);
    return osInfo.endosInfo(builder);
  }

  public static void startosInfo(FlatBufferBuilder builder) { builder.startObject(8); }
  public static void addUpTime(FlatBufferBuilder builder, long upTime) { builder.addLong(0, upTime, 0); }
  public static void addTotalMemory(FlatBufferBuilder builder, long totalMemory) { builder.addLong(1, totalMemory, 0); }
  public static void addFreeMemory(FlatBufferBuilder builder, long freeMemory) { builder.addLong(2, freeMemory, 0); }
  public static void addSharedMemory(FlatBufferBuilder builder, long sharedMemory) { builder.addLong(3, sharedMemory, 0); }
  public static void addBufferedMemory(FlatBufferBuilder builder, long bufferedMemory) { builder.addLong(4, bufferedMemory, 0); }
  public static void addCachedMemory(FlatBufferBuilder builder, long cachedMemory) { builder.addLong(5, cachedMemory, 0); }
  public static void addTotalSwap(FlatBufferBuilder builder, long totalSwap) { builder.addLong(6, totalSwap, 0); }
  public static void addFreeSwap(FlatBufferBuilder builder, long freeSwap) { builder.addLong(7, freeSwap, 0); }
  public static int endosInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishosInfoBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

