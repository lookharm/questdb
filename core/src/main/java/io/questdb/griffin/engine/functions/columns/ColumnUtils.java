/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2022 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.columns;

import io.questdb.std.Unsafe;

public class ColumnUtils {
    static final int STATIC_COLUMN_COUNT = 32;

    public static void symbolColumnUpdateKeys(long columnMemory, long columnMemorySize, long remapTableMemory, long remapMemorySize) {
        final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();
        for (int offset = 0; offset < columnMemorySize; offset += Integer.BYTES) {
            final int oldKey = unsafe.getInt(columnMemory + offset);
            final long remapOffset = (long) oldKey * Integer.BYTES;
            if (remapOffset >= 0 && remapOffset < remapMemorySize) {
                final int newKey = unsafe.getInt(remapTableMemory + remapOffset);
                unsafe.putInt(columnMemory + offset, newKey);
            }
        }
    }
}
