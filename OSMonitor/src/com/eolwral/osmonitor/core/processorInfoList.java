// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class processorInfoList extends Table {
  public static processorInfoList getRootAsprocessorInfoList(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new processorInfoList()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public processorInfoList __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public processorInfo list(int j) { return list(new processorInfo(), j); }
  public processorInfo list(processorInfo obj, int j) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int listLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer listAsByteBuffer() { return __vector_as_bytebuffer(4, 4); }

  public static int createprocessorInfoList(FlatBufferBuilder builder,
      int list) {
    builder.startObject(1);
    processorInfoList.addList(builder, list);
    return processorInfoList.endprocessorInfoList(builder);
  }

  public static void startprocessorInfoList(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addList(FlatBufferBuilder builder, int listOffset) { builder.addOffset(0, listOffset, 0); }
  public static int createListVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startListVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endprocessorInfoList(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishprocessorInfoListBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

