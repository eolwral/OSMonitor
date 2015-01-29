// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class cpuInfoList extends Table {
  public static cpuInfoList getRootAscpuInfoList(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new cpuInfoList()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public cpuInfoList __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public cpuInfo list(int j) { return list(new cpuInfo(), j); }
  public cpuInfo list(cpuInfo obj, int j) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int listLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer listAsByteBuffer() { return __vector_as_bytebuffer(4, 4); }

  public static int createcpuInfoList(FlatBufferBuilder builder,
      int list) {
    builder.startObject(1);
    cpuInfoList.addList(builder, list);
    return cpuInfoList.endcpuInfoList(builder);
  }

  public static void startcpuInfoList(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addList(FlatBufferBuilder builder, int listOffset) { builder.addOffset(0, listOffset, 0); }
  public static int createListVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startListVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endcpuInfoList(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishcpuInfoListBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

