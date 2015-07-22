// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class connectionInfo extends Table {
  public static connectionInfo getRootAsconnectionInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new connectionInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public connectionInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public byte type() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public byte status() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public String localIP() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer localIPAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public int localPort() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public String remoteIP() { int o = __offset(12); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer remoteIPAsByteBuffer() { return __vector_as_bytebuffer(12, 1); }
  public int remotePort() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int uid() { int o = __offset(16); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createconnectionInfo(FlatBufferBuilder builder,
      byte type,
      byte status,
      int localIP,
      int localPort,
      int remoteIP,
      int remotePort,
      int uid) {
    builder.startObject(7);
    connectionInfo.addUid(builder, uid);
    connectionInfo.addRemotePort(builder, remotePort);
    connectionInfo.addRemoteIP(builder, remoteIP);
    connectionInfo.addLocalPort(builder, localPort);
    connectionInfo.addLocalIP(builder, localIP);
    connectionInfo.addStatus(builder, status);
    connectionInfo.addType(builder, type);
    return connectionInfo.endconnectionInfo(builder);
  }

  public static void startconnectionInfo(FlatBufferBuilder builder) { builder.startObject(7); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(0, type, 0); }
  public static void addStatus(FlatBufferBuilder builder, byte status) { builder.addByte(1, status, 0); }
  public static void addLocalIP(FlatBufferBuilder builder, int localIPOffset) { builder.addOffset(2, localIPOffset, 0); }
  public static void addLocalPort(FlatBufferBuilder builder, int localPort) { builder.addInt(3, localPort, 0); }
  public static void addRemoteIP(FlatBufferBuilder builder, int remoteIPOffset) { builder.addOffset(4, remoteIPOffset, 0); }
  public static void addRemotePort(FlatBufferBuilder builder, int remotePort) { builder.addInt(5, remotePort, 0); }
  public static void addUid(FlatBufferBuilder builder, int uid) { builder.addInt(6, uid, 0); }
  public static int endconnectionInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

