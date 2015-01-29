// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class logcatInfo extends Table {
  public static logcatInfo getRootAslogcatInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new logcatInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public logcatInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// priority
  public byte priority() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  /// seconds since Epoch 
  public long seconds() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// nanoseconds 
  public long nanoSeconds() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// generating process's pid 
  public int pid() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// generating process's tid 
  public int tid() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Tag 
  public String tag() { int o = __offset(14); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer tagAsByteBuffer() { return __vector_as_bytebuffer(14, 1); }
  /// message
  public String message() { int o = __offset(16); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer messageAsByteBuffer() { return __vector_as_bytebuffer(16, 1); }

  public static int createlogcatInfo(FlatBufferBuilder builder,
      byte priority,
      long seconds,
      long nanoSeconds,
      int pid,
      int tid,
      int tag,
      int message) {
    builder.startObject(7);
    logcatInfo.addNanoSeconds(builder, nanoSeconds);
    logcatInfo.addSeconds(builder, seconds);
    logcatInfo.addMessage(builder, message);
    logcatInfo.addTag(builder, tag);
    logcatInfo.addTid(builder, tid);
    logcatInfo.addPid(builder, pid);
    logcatInfo.addPriority(builder, priority);
    return logcatInfo.endlogcatInfo(builder);
  }

  public static void startlogcatInfo(FlatBufferBuilder builder) { builder.startObject(7); }
  public static void addPriority(FlatBufferBuilder builder, byte priority) { builder.addByte(0, priority, 0); }
  public static void addSeconds(FlatBufferBuilder builder, long seconds) { builder.addLong(1, seconds, 0); }
  public static void addNanoSeconds(FlatBufferBuilder builder, long nanoSeconds) { builder.addLong(2, nanoSeconds, 0); }
  public static void addPid(FlatBufferBuilder builder, int pid) { builder.addInt(3, pid, 0); }
  public static void addTid(FlatBufferBuilder builder, int tid) { builder.addInt(4, tid, 0); }
  public static void addTag(FlatBufferBuilder builder, int tagOffset) { builder.addOffset(5, tagOffset, 0); }
  public static void addMessage(FlatBufferBuilder builder, int messageOffset) { builder.addOffset(6, messageOffset, 0); }
  public static int endlogcatInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

