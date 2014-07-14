LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libosmcore
LOCAL_MODULE_TAGS := optional

LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_CPP_EXTENSION := .cc .cpp

LOCAL_PBC_FILES := \
                  src/google/protobuf/stubs/strutil.cc                 \
                  src/google/protobuf/stubs/substitute.cc              \
                  src/google/protobuf/stubs/stringprintf.cc	               \
                  src/google/protobuf/stubs/atomicops_internals_x86_gcc.cc       \
                  src/google/protobuf/stubs/structurally_valid.cc      \
                  src/google/protobuf/descriptor.cc                    \
                  src/google/protobuf/descriptor.pb.cc                 \
                  src/google/protobuf/descriptor_database.cc           \
                  src/google/protobuf/dynamic_message.cc               \
                  src/google/protobuf/extension_set_heavy.cc           \
                  src/google/protobuf/generated_message_reflection.cc  \
                  src/google/protobuf/message.cc                       \
                  src/google/protobuf/reflection_ops.cc                \
                  src/google/protobuf/service.cc                       \
                  src/google/protobuf/text_format.cc                   \
                  src/google/protobuf/unknown_field_set.cc             \
                  src/google/protobuf/wire_format.cc                   \
                  src/google/protobuf/io/gzip_stream.cc                \
                  src/google/protobuf/io/printer.cc                    \
                  src/google/protobuf/io/tokenizer.cc                  \
                  src/google/protobuf/io/zero_copy_stream_impl.cc      \
                  src/google/protobuf/compiler/importer.cc             \
                  src/google/protobuf/compiler/parser.cc               \
                  src/google/protobuf/stubs/common.cc                  \
                  src/google/protobuf/stubs/once.cc                    \
                  src/google/protobuf/extension_set.cc                 \
                  src/google/protobuf/generated_message_util.cc        \
                  src/google/protobuf/message_lite.cc                  \
                  src/google/protobuf/repeated_field.cc                \
                  src/google/protobuf/wire_format_lite.cc              \
                  src/google/protobuf/io/coded_stream.cc               \
                  src/google/protobuf/io/zero_copy_stream.cc           \
                  src/google/protobuf/io/zero_copy_stream_impl_lite.cc                  

LOCAL_SRC_FILES := \
                   core.cc \
                   src/core/base.cc \
                   src/core/os.cc \
                   src/core/osInfo.pb.cc \
                   src/core/cpu.cc \
                   src/core/cpuInfo.pb.cc \
                   src/core/connection.cc \
                   src/core/connectionInfo.pb.cc \
                   src/core/network.cc \
                   src/core/networkInfo.pb.cc \
                   src/core/process.cc \
                   src/core/processInfo.pb.cc \
                   src/core/processor.cc \
                   src/core/processorInfo.pb.cc \
                   src/core/dmesg.cc \
                   src/core/dmesgInfo.pb.cc \
                   src/core/logcat.cc \
                   src/core/logcatInfo.pb.cc \
                   src/ipc/ipcserver.cc \
                   src/ipc/ipcMessage.pb.cc \
                   src/android/event_tag_map.c \
                   $(LOCAL_PBC_FILES) 

LOCAL_C_INCLUDES := \
                   $(LOCAL_PATH)/src \
                   $(LOCAL_PATH)/include \
                   $(LOCAL_PATH)/include/core \
                   $(LOCAL_PATH)/include/ipc \
                   $(JNI_H_INCLUDE)                   
                   
LOCAL_LDLIBS := -lz -llog

# stlport conflicts with the host stl library
ifneq ($(TARGET_SIMULATOR),true)
LOCAL_C_INCLUDES += external/stlport/stlport
LOCAL_SHARED_LIBRARIES += libstlport
endif

LOCAL_CFLAGS := -DGOOGLE_PROTOBUF_NO_RTTI -D_GLIBCXX_PERMIT_BACKWARD_HASH 

# compile executeable binary for test 
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)

LOCAL_MODULE := libosmcore_l
LOCAL_MODULE_TAGS := optional

LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_CPP_EXTENSION := .cc .cpp

LOCAL_PBC_FILES := \
                  src/google/protobuf/stubs/strutil.cc                 \
                  src/google/protobuf/stubs/substitute.cc              \
                  src/google/protobuf/stubs/stringprintf.cc	               \
                  src/google/protobuf/stubs/atomicops_internals_x86_gcc.cc       \
                  src/google/protobuf/stubs/structurally_valid.cc      \
                  src/google/protobuf/descriptor.cc                    \
                  src/google/protobuf/descriptor.pb.cc                 \
                  src/google/protobuf/descriptor_database.cc           \
                  src/google/protobuf/dynamic_message.cc               \
                  src/google/protobuf/extension_set_heavy.cc           \
                  src/google/protobuf/generated_message_reflection.cc  \
                  src/google/protobuf/message.cc                       \
                  src/google/protobuf/reflection_ops.cc                \
                  src/google/protobuf/service.cc                       \
                  src/google/protobuf/text_format.cc                   \
                  src/google/protobuf/unknown_field_set.cc             \
                  src/google/protobuf/wire_format.cc                   \
                  src/google/protobuf/io/gzip_stream.cc                \
                  src/google/protobuf/io/printer.cc                    \
                  src/google/protobuf/io/tokenizer.cc                  \
                  src/google/protobuf/io/zero_copy_stream_impl.cc      \
                  src/google/protobuf/compiler/importer.cc             \
                  src/google/protobuf/compiler/parser.cc               \
                  src/google/protobuf/stubs/common.cc                  \
                  src/google/protobuf/stubs/once.cc                    \
                  src/google/protobuf/extension_set.cc                 \
                  src/google/protobuf/generated_message_util.cc        \
                  src/google/protobuf/message_lite.cc                  \
                  src/google/protobuf/repeated_field.cc                \
                  src/google/protobuf/wire_format_lite.cc              \
                  src/google/protobuf/io/coded_stream.cc               \
                  src/google/protobuf/io/zero_copy_stream.cc           \
                  src/google/protobuf/io/zero_copy_stream_impl_lite.cc                  

LOCAL_SRC_FILES := \
                   core.cc \
                   src/core/base.cc \
                   src/core/os.cc \
                   src/core/osInfo.pb.cc \
                   src/core/cpu.cc \
                   src/core/cpuInfo.pb.cc \
                   src/core/connection.cc \
                   src/core/connectionInfo.pb.cc \
                   src/core/network.cc \
                   src/core/networkInfo.pb.cc \
                   src/core/process.cc \
                   src/core/processInfo.pb.cc \
                   src/core/processor.cc \
                   src/core/processorInfo.pb.cc \
                   src/core/dmesg.cc \
                   src/core/dmesgInfo.pb.cc \
                   src/core/logcat.cc \
                   src/core/logcatInfo.pb.cc \
                   src/ipc/ipcserver.cc \
                   src/ipc/ipcMessage.pb.cc \
                   src/android/event_tag_map.c \
                   $(LOCAL_PBC_FILES) 

LOCAL_C_INCLUDES := \
                   $(LOCAL_PATH)/src \
                   $(LOCAL_PATH)/include \
                   $(LOCAL_PATH)/include/core \
                   $(LOCAL_PATH)/include/ipc \
                   $(JNI_H_INCLUDE)                   
                   
LOCAL_LDLIBS := -lz -llog

# stlport conflicts with the host stl library
ifneq ($(TARGET_SIMULATOR),true)
LOCAL_C_INCLUDES += external/stlport/stlport
LOCAL_SHARED_LIBRARIES += libstlport
endif

LOCAL_CFLAGS := -DGOOGLE_PROTOBUF_NO_RTTI -D_GLIBCXX_PERMIT_BACKWARD_HASH 

# compatiable with L
LOCAL_LDFLAGS += -fPIC -pie

# compile executeable binary for test 
include $(BUILD_EXECUTABLE)
