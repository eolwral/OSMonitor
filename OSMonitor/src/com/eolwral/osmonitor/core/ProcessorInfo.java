// automatically generated, do not modify

package com.eolwral.osmonitor.core;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class processorInfo extends Table {
  public static processorInfo getRootAsprocessorInfo(ByteBuffer _bb) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (new processorInfo()).__init(_bb.getInt(_bb.position()) + _bb.position(), _bb); }
  public processorInfo __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /// Maximum Frequency 
  public int maxFrequency() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Minimum Frequency 
  public int minFrequency() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Maximum Scaling Frequency 
  public int maxScaling() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Minimum Scaling Frequency 
  public int minScaling() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Current Scaling Frequency 
  public int currentScaling() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Current Governors 
  public String governors() { int o = __offset(14); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer governorsAsByteBuffer() { return __vector_as_bytebuffer(14, 1); }
  /// Number 
  public int number() { int o = __offset(16); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /// Off-line 
  public byte offLine() { int o = __offset(18); return o != 0 ? bb.get(o + bb_pos) : 0; }
  /// Available Grovernors 
  public String availableGovernors() { int o = __offset(20); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer availableGovernorsAsByteBuffer() { return __vector_as_bytebuffer(20, 1); }
  /// Available Frequency 
  public String availableFrequency() { int o = __offset(22); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer availableFrequencyAsByteBuffer() { return __vector_as_bytebuffer(22, 1); }

  public static int createprocessorInfo(FlatBufferBuilder builder,
      int maxFrequency,
      int minFrequency,
      int maxScaling,
      int minScaling,
      int currentScaling,
      int governors,
      int number,
      byte offLine,
      int availableGovernors,
      int availableFrequency) {
    builder.startObject(10);
    processorInfo.addAvailableFrequency(builder, availableFrequency);
    processorInfo.addAvailableGovernors(builder, availableGovernors);
    processorInfo.addNumber(builder, number);
    processorInfo.addGovernors(builder, governors);
    processorInfo.addCurrentScaling(builder, currentScaling);
    processorInfo.addMinScaling(builder, minScaling);
    processorInfo.addMaxScaling(builder, maxScaling);
    processorInfo.addMinFrequency(builder, minFrequency);
    processorInfo.addMaxFrequency(builder, maxFrequency);
    processorInfo.addOffLine(builder, offLine);
    return processorInfo.endprocessorInfo(builder);
  }

  public static void startprocessorInfo(FlatBufferBuilder builder) { builder.startObject(10); }
  public static void addMaxFrequency(FlatBufferBuilder builder, int maxFrequency) { builder.addInt(0, maxFrequency, 0); }
  public static void addMinFrequency(FlatBufferBuilder builder, int minFrequency) { builder.addInt(1, minFrequency, 0); }
  public static void addMaxScaling(FlatBufferBuilder builder, int maxScaling) { builder.addInt(2, maxScaling, 0); }
  public static void addMinScaling(FlatBufferBuilder builder, int minScaling) { builder.addInt(3, minScaling, 0); }
  public static void addCurrentScaling(FlatBufferBuilder builder, int currentScaling) { builder.addInt(4, currentScaling, 0); }
  public static void addGovernors(FlatBufferBuilder builder, int governorsOffset) { builder.addOffset(5, governorsOffset, 0); }
  public static void addNumber(FlatBufferBuilder builder, int number) { builder.addInt(6, number, 0); }
  public static void addOffLine(FlatBufferBuilder builder, byte offLine) { builder.addByte(7, offLine, 0); }
  public static void addAvailableGovernors(FlatBufferBuilder builder, int availableGovernorsOffset) { builder.addOffset(8, availableGovernorsOffset, 0); }
  public static void addAvailableFrequency(FlatBufferBuilder builder, int availableFrequencyOffset) { builder.addOffset(9, availableFrequencyOffset, 0); }
  public static int endprocessorInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

