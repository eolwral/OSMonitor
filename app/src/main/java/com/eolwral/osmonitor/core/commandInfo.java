// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class commandInfo extends Table {
  public static commandInfo getRootAscommandInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new commandInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public commandInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public String arguments(int j) { int o = __offset(4); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int argumentsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer argumentsAsByteBuffer() { return __vector_as_bytebuffer(4, 4); }

  public static int createcommandInfo(FlatBufferBuilder builder,
      int arguments) {
    builder.startObject(1);
    commandInfo.addArguments(builder, arguments);
    return commandInfo.endcommandInfo(builder);
  }

  public static void startcommandInfo(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addArguments(FlatBufferBuilder builder, int argumentsOffset) { builder.addOffset(0, argumentsOffset, 0); }
  public static int createArgumentsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startArgumentsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endcommandInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishcommandInfoBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

