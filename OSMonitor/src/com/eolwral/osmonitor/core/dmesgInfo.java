// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class dmesgInfo extends Table {
  public static dmesgInfo getRootAsdmesgInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new dmesgInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public dmesgInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// message level 
  public byte level() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 6; }
  /// passed seconds since boot 
  public long seconds() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// message 
  public String message() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer messageAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }

  public static int createdmesgInfo(FlatBufferBuilder builder,
      byte level,
      long seconds,
      int message) {
    builder.startObject(3);
    dmesgInfo.addSeconds(builder, seconds);
    dmesgInfo.addMessage(builder, message);
    dmesgInfo.addLevel(builder, level);
    return dmesgInfo.enddmesgInfo(builder);
  }

  public static void startdmesgInfo(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addLevel(FlatBufferBuilder builder, byte level) { builder.addByte(0, level, 6); }
  public static void addSeconds(FlatBufferBuilder builder, long seconds) { builder.addLong(1, seconds, 0); }
  public static void addMessage(FlatBufferBuilder builder, int messageOffset) { builder.addOffset(2, messageOffset, 0); }
  public static int enddmesgInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

