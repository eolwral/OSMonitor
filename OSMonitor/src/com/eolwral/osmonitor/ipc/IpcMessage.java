// automatically generated, do not modify

package com.eolwral.osmonitor.ipc;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class ipcMessage extends Table {
  public static ipcMessage getRootAsipcMessage(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new ipcMessage()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public ipcMessage __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public byte type() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public ipcData data(int j) { return data(new ipcData(), j); }
  public ipcData data(ipcData obj, int j) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int dataLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer dataAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }

  public static int createipcMessage(FlatBufferBuilder builder,
      byte type,
      int data) {
    builder.startObject(2);
    ipcMessage.addData(builder, data);
    ipcMessage.addType(builder, type);
    return ipcMessage.endipcMessage(builder);
  }

  public static void startipcMessage(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(0, type, 0); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(1, dataOffset, 0); }
  public static int createDataVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startDataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endipcMessage(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishipcMessageBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

