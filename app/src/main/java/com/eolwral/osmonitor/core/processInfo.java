// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import com.google.flatbuffers.*;

public class processInfo extends Table {
  public static processInfo getRootAsprocessInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new processInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public processInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// process name 
  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  /// process owner 
  public String owner() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer ownerAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  /// process status 
  public byte status() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) : 0; }
  /// process uid 
  public int uid() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// process pid 
  public int pid() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// process pid for parent 
  public int ppid() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// resident set size 
  public long rss() { int o = __offset(16); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// virtual size 
  public long vsz() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// CPU usage 
  public float cpuUsage() { int o = __offset(20); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  /// thread count for this process 
  public int threadCount() { int o = __offset(22); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// priority from -20 to 20 
  public int priorityLevel() { int o = __offset(24); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// used user time (user mode) 
  public long usedUserTime() { int o = __offset(26); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// used system time (kernel mode) 
  public long usedSystemTime() { int o = __offset(28); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// start time 
  public long startTime() { int o = __offset(30); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// CPU time 
  public long cpuTime() { int o = __offset(32); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static int createprocessInfo(FlatBufferBuilder builder,
      int name,
      int owner,
      byte status,
      int uid,
      int pid,
      int ppid,
      long rss,
      long vsz,
      float cpuUsage,
      int threadCount,
      int priorityLevel,
      long usedUserTime,
      long usedSystemTime,
      long startTime,
      long cpuTime) {
    builder.startObject(15);
    processInfo.addCpuTime(builder, cpuTime);
    processInfo.addStartTime(builder, startTime);
    processInfo.addUsedSystemTime(builder, usedSystemTime);
    processInfo.addUsedUserTime(builder, usedUserTime);
    processInfo.addVsz(builder, vsz);
    processInfo.addRss(builder, rss);
    processInfo.addPriorityLevel(builder, priorityLevel);
    processInfo.addThreadCount(builder, threadCount);
    processInfo.addCpuUsage(builder, cpuUsage);
    processInfo.addPpid(builder, ppid);
    processInfo.addPid(builder, pid);
    processInfo.addUid(builder, uid);
    processInfo.addOwner(builder, owner);
    processInfo.addName(builder, name);
    processInfo.addStatus(builder, status);
    return processInfo.endprocessInfo(builder);
  }

  public static void startprocessInfo(FlatBufferBuilder builder) { builder.startObject(15); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addOwner(FlatBufferBuilder builder, int ownerOffset) { builder.addOffset(1, ownerOffset, 0); }
  public static void addStatus(FlatBufferBuilder builder, byte status) { builder.addByte(2, status, 0); }
  public static void addUid(FlatBufferBuilder builder, int uid) { builder.addInt(3, uid, 0); }
  public static void addPid(FlatBufferBuilder builder, int pid) { builder.addInt(4, pid, 0); }
  public static void addPpid(FlatBufferBuilder builder, int ppid) { builder.addInt(5, ppid, 0); }
  public static void addRss(FlatBufferBuilder builder, long rss) { builder.addLong(6, rss, 0); }
  public static void addVsz(FlatBufferBuilder builder, long vsz) { builder.addLong(7, vsz, 0); }
  public static void addCpuUsage(FlatBufferBuilder builder, float cpuUsage) { builder.addFloat(8, cpuUsage, 0); }
  public static void addThreadCount(FlatBufferBuilder builder, int threadCount) { builder.addInt(9, threadCount, 0); }
  public static void addPriorityLevel(FlatBufferBuilder builder, int priorityLevel) { builder.addInt(10, priorityLevel, 0); }
  public static void addUsedUserTime(FlatBufferBuilder builder, long usedUserTime) { builder.addLong(11, usedUserTime, 0); }
  public static void addUsedSystemTime(FlatBufferBuilder builder, long usedSystemTime) { builder.addLong(12, usedSystemTime, 0); }
  public static void addStartTime(FlatBufferBuilder builder, long startTime) { builder.addLong(13, startTime, 0); }
  public static void addCpuTime(FlatBufferBuilder builder, long cpuTime) { builder.addLong(14, cpuTime, 0); }
  public static int endprocessInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

