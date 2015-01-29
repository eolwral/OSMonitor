// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class networkInfo extends Table {
  public static networkInfo getRootAsnetworkInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new networkInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public networkInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// interface name 
  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  /// MAC address 
  public String mac() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer macAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  /// IPv4 address 
  public String ipv4Addr() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer ipv4AddrAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  /// IPv4 netmask 
  public String netMaskv4() { int o = __offset(10); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer netMaskv4AsByteBuffer() { return __vector_as_bytebuffer(10, 1); }
  /// IPv6 address 
  public String ipv6Addr() { int o = __offset(12); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer ipv6AddrAsByteBuffer() { return __vector_as_bytebuffer(12, 1); }
  /// IPv6 netmask 
  public int netMaskv6() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// status flag 
  public int flags() { int o = __offset(16); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// received bytes 
  public long recvBytes() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// received packages 
  public long recvPackages() { int o = __offset(20); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// error bytes when receiving 
  public long recvErrorBytes() { int o = __offset(22); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// drop bytes when receiving  
  public long recvDropBytes() { int o = __offset(24); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// FIFO bytes when receiving 
  public long recvFIFOBytes() { int o = __offset(26); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// received frames 
  public long recvFrames() { int o = __offset(28); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// received compressed bytes 
  public long recvCompressedBytes() { int o = __offset(30); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// received multi-cast bytes 
  public long recvMultiCastBytes() { int o = __offset(32); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// transmitted bytes 
  public long transBytes() { int o = __offset(34); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// transmitted packages 
  public long transPackages() { int o = __offset(36); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// error bytes when transmitting 
  public long transErrorBytes() { int o = __offset(38); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// dropped bytes when transmitting 
  public long transDropBytes() { int o = __offset(40); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// FIFO bytes when transmitting 
  public long transFIFOBytes() { int o = __offset(42); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// transmitted compressed bytes 
  public long transCompressedBytes() { int o = __offset(44); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// collision times 
  public int collisionTimes() { int o = __offset(46); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// carrier error times 
  public int carrierErros() { int o = __offset(48); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// transmitted usage 
  public long transUsage() { int o = __offset(50); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  /// received usage 
  public long recvUsage() { int o = __offset(52); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static int createnetworkInfo(FlatBufferBuilder builder,
      int name,
      int mac,
      int ipv4Addr,
      int netMaskv4,
      int ipv6Addr,
      int netMaskv6,
      int flags,
      long recvBytes,
      long recvPackages,
      long recvErrorBytes,
      long recvDropBytes,
      long recvFIFOBytes,
      long recvFrames,
      long recvCompressedBytes,
      long recvMultiCastBytes,
      long transBytes,
      long transPackages,
      long transErrorBytes,
      long transDropBytes,
      long transFIFOBytes,
      long transCompressedBytes,
      int collisionTimes,
      int carrierErros,
      long transUsage,
      long recvUsage) {
    builder.startObject(25);
    networkInfo.addRecvUsage(builder, recvUsage);
    networkInfo.addTransUsage(builder, transUsage);
    networkInfo.addTransCompressedBytes(builder, transCompressedBytes);
    networkInfo.addTransFIFOBytes(builder, transFIFOBytes);
    networkInfo.addTransDropBytes(builder, transDropBytes);
    networkInfo.addTransErrorBytes(builder, transErrorBytes);
    networkInfo.addTransPackages(builder, transPackages);
    networkInfo.addTransBytes(builder, transBytes);
    networkInfo.addRecvMultiCastBytes(builder, recvMultiCastBytes);
    networkInfo.addRecvCompressedBytes(builder, recvCompressedBytes);
    networkInfo.addRecvFrames(builder, recvFrames);
    networkInfo.addRecvFIFOBytes(builder, recvFIFOBytes);
    networkInfo.addRecvDropBytes(builder, recvDropBytes);
    networkInfo.addRecvErrorBytes(builder, recvErrorBytes);
    networkInfo.addRecvPackages(builder, recvPackages);
    networkInfo.addRecvBytes(builder, recvBytes);
    networkInfo.addCarrierErros(builder, carrierErros);
    networkInfo.addCollisionTimes(builder, collisionTimes);
    networkInfo.addFlags(builder, flags);
    networkInfo.addNetMaskv6(builder, netMaskv6);
    networkInfo.addIpv6Addr(builder, ipv6Addr);
    networkInfo.addNetMaskv4(builder, netMaskv4);
    networkInfo.addIpv4Addr(builder, ipv4Addr);
    networkInfo.addMac(builder, mac);
    networkInfo.addName(builder, name);
    return networkInfo.endnetworkInfo(builder);
  }

  public static void startnetworkInfo(FlatBufferBuilder builder) { builder.startObject(25); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addMac(FlatBufferBuilder builder, int macOffset) { builder.addOffset(1, macOffset, 0); }
  public static void addIpv4Addr(FlatBufferBuilder builder, int ipv4AddrOffset) { builder.addOffset(2, ipv4AddrOffset, 0); }
  public static void addNetMaskv4(FlatBufferBuilder builder, int netMaskv4Offset) { builder.addOffset(3, netMaskv4Offset, 0); }
  public static void addIpv6Addr(FlatBufferBuilder builder, int ipv6AddrOffset) { builder.addOffset(4, ipv6AddrOffset, 0); }
  public static void addNetMaskv6(FlatBufferBuilder builder, int netMaskv6) { builder.addInt(5, netMaskv6, 0); }
  public static void addFlags(FlatBufferBuilder builder, int flags) { builder.addInt(6, flags, 0); }
  public static void addRecvBytes(FlatBufferBuilder builder, long recvBytes) { builder.addLong(7, recvBytes, 0); }
  public static void addRecvPackages(FlatBufferBuilder builder, long recvPackages) { builder.addLong(8, recvPackages, 0); }
  public static void addRecvErrorBytes(FlatBufferBuilder builder, long recvErrorBytes) { builder.addLong(9, recvErrorBytes, 0); }
  public static void addRecvDropBytes(FlatBufferBuilder builder, long recvDropBytes) { builder.addLong(10, recvDropBytes, 0); }
  public static void addRecvFIFOBytes(FlatBufferBuilder builder, long recvFIFOBytes) { builder.addLong(11, recvFIFOBytes, 0); }
  public static void addRecvFrames(FlatBufferBuilder builder, long recvFrames) { builder.addLong(12, recvFrames, 0); }
  public static void addRecvCompressedBytes(FlatBufferBuilder builder, long recvCompressedBytes) { builder.addLong(13, recvCompressedBytes, 0); }
  public static void addRecvMultiCastBytes(FlatBufferBuilder builder, long recvMultiCastBytes) { builder.addLong(14, recvMultiCastBytes, 0); }
  public static void addTransBytes(FlatBufferBuilder builder, long transBytes) { builder.addLong(15, transBytes, 0); }
  public static void addTransPackages(FlatBufferBuilder builder, long transPackages) { builder.addLong(16, transPackages, 0); }
  public static void addTransErrorBytes(FlatBufferBuilder builder, long transErrorBytes) { builder.addLong(17, transErrorBytes, 0); }
  public static void addTransDropBytes(FlatBufferBuilder builder, long transDropBytes) { builder.addLong(18, transDropBytes, 0); }
  public static void addTransFIFOBytes(FlatBufferBuilder builder, long transFIFOBytes) { builder.addLong(19, transFIFOBytes, 0); }
  public static void addTransCompressedBytes(FlatBufferBuilder builder, long transCompressedBytes) { builder.addLong(20, transCompressedBytes, 0); }
  public static void addCollisionTimes(FlatBufferBuilder builder, int collisionTimes) { builder.addInt(21, collisionTimes, 0); }
  public static void addCarrierErros(FlatBufferBuilder builder, int carrierErros) { builder.addInt(22, carrierErros, 0); }
  public static void addTransUsage(FlatBufferBuilder builder, long transUsage) { builder.addLong(23, transUsage, 0); }
  public static void addRecvUsage(FlatBufferBuilder builder, long recvUsage) { builder.addLong(24, recvUsage, 0); }
  public static int endnetworkInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

