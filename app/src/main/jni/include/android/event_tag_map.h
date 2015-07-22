/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _LIBS_CUTILS_EVENTTAGMAP_H
#define _LIBS_CUTILS_EVENTTAGMAP_H

#ifdef __cplusplus
extern "C" {
#endif

#define EVENT_TAG_MAP_FILE  "/system/etc/event-log-tags"

struct EventTagMap;
typedef struct EventTagMap EventTagMap;

/*
 * Open the specified file as an event log tag map.
 *
 * Returns NULL on failure.
 */
EventTagMap* android_openEventTagMap(const char* fileName);

/*
 * Close the map.
 */
void android_closeEventTagMap(EventTagMap* map);

/*
 * Look up a tag by index.  Returns the tag string, or NULL if not found.
 */
const char* android_lookupEventTag(const EventTagMap* map, int tag);

typedef unsigned char           uint8_t;
typedef unsigned short          uint16_t;
typedef unsigned int            uint32_t;
typedef unsigned long long      uint64_t;

/*
 * Extract a 4-byte value from a byte stream.
 */
static inline uint32_t get4LE(const uint8_t* src)
{
    return (src[0] | (src[1] << 8) | (src[2] << 16) | (src[3] << 24));
}

/*
 * Extract an 8-byte value from a byte stream.
 */

static inline uint64_t get8LE(const uint8_t* src)
{
    uint32_t low, high;

    low = src[0] | (src[1] << 8) | (src[2] << 16) | (src[3] << 24);
    high = src[4] | (src[5] << 8) | (src[6] << 16) | (src[7] << 24);
    return (((long long) high << 32) | (long long) low);
}

/*
 * Event log entry types.  These must match up with the declarations in
 * java/android/android/util/EventLog.java.
 */

typedef enum
{
  EVENT_TYPE_INT = 0,
  EVENT_TYPE_LONG = 1,
  EVENT_TYPE_STRING = 2,
  EVENT_TYPE_LIST = 3,
}
AndroidEventLogType;

#ifdef __cplusplus
}
#endif

#endif /*_LIBS_CUTILS_EVENTTAGMAP_H*/
