// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class processInfoList extends Table {
  public static processInfoList getRootAsprocessInfoList(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new processInfoList()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public processInfoList __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public processInfo list(int j) { return list(new processInfo(), j); }
  public processInfo list(processInfo obj, int j) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int listLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer listAsByteBuffer() { return __vector_as_bytebuffer(4, 4); }

  public static int createprocessInfoList(FlatBufferBuilder builder,
      int list) {
    builder.startObject(1);
    processInfoList.addList(builder, list);
    return processInfoList.endprocessInfoList(builder);
  }

  public static void startprocessInfoList(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addList(FlatBufferBuilder builder, int listOffset) { builder.addOffset(0, listOffset, 0); }
  public static int createListVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startListVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endprocessInfoList(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishprocessInfoListBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

