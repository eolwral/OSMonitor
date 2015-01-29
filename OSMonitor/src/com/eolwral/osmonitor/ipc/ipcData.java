// automatically generated, do not modify

package com.eolwral.osmonitor.ipc;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class ipcData extends Table {
  public static ipcData getRootAsipcData(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new ipcData()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public ipcData __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public byte category() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public byte payload(int j) { int o = __offset(6); return o != 0 ? bb.get(__vector(o) + j * 1) : 0; }
  public int payloadLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer payloadAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }

  public static int createipcData(FlatBufferBuilder builder,
      byte category,
      int payload) {
    builder.startObject(2);
    ipcData.addPayload(builder, payload);
    ipcData.addCategory(builder, category);
    return ipcData.endipcData(builder);
  }

  public static void startipcData(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addCategory(FlatBufferBuilder builder, byte category) { builder.addByte(0, category, 0); }
  public static void addPayload(FlatBufferBuilder builder, int payloadOffset) { builder.addOffset(1, payloadOffset, 0); }
  public static int createPayloadVector(FlatBufferBuilder builder, byte[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addByte(data[i]); return builder.endVector(); }
  public static void startPayloadVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static int endipcData(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

