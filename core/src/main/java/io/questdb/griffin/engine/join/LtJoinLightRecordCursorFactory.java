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

package io.questdb.griffin.engine.join;

import io.questdb.cairo.AbstractRecordCursorFactory;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.ColumnTypes;
import io.questdb.cairo.RecordSink;
import io.questdb.cairo.map.Map;
import io.questdb.cairo.map.MapFactory;
import io.questdb.cairo.map.MapKey;
import io.questdb.cairo.map.MapValue;
import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.cairo.sql.RecordMetadata;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.Misc;
import io.questdb.std.Numbers;
import io.questdb.std.Transient;

public class LtJoinLightRecordCursorFactory extends AbstractRecordCursorFactory {
    private final RecordCursorFactory masterFactory;
    private final RecordCursorFactory slaveFactory;
    private final RecordSink masterKeySink;
    private final RecordSink slaveKeySink;
    private final LtJoinLightRecordCursor cursor;

    public LtJoinLightRecordCursorFactory(
            CairoConfiguration configuration,
            RecordMetadata metadata,
            RecordCursorFactory masterFactory,
            RecordCursorFactory slaveFactory,
            @Transient ColumnTypes joinColumnTypes,
            @Transient ColumnTypes valueTypes, // this expected to be just LONG, we store chain references in map
            RecordSink masterKeySink,
            RecordSink slaveKeySink,
            int columnSplit) {
        super(metadata);
        this.masterFactory = masterFactory;
        this.slaveFactory = slaveFactory;
        this.masterKeySink = masterKeySink;
        this.slaveKeySink = slaveKeySink;

        Map joinKeyMap = MapFactory.createMap(configuration, joinColumnTypes, valueTypes);
        this.cursor = new LtJoinLightRecordCursor(
                columnSplit,
                joinKeyMap,
                NullRecordFactory.getInstance(slaveFactory.getMetadata()),
                masterFactory.getMetadata().getTimestampIndex(),
                slaveFactory.getMetadata().getTimestampIndex()
        );
    }

    @Override
    protected void _close() {
        ((JoinRecordMetadata) getMetadata()).close();
        masterFactory.close();
        slaveFactory.close();
        cursor.close();
    }

    @Override
    public RecordCursor getCursor(SqlExecutionContext executionContext) throws SqlException {
        RecordCursor masterCursor = masterFactory.getCursor(executionContext);
        RecordCursor slaveCursor = null;
        try {
            slaveCursor = slaveFactory.getCursor(executionContext);
            cursor.of(masterCursor, slaveCursor);
            return cursor;
        } catch (Throwable e) {
            Misc.free(slaveCursor);
            Misc.free(masterCursor);
            throw e;
        }
    }

    @Override
    public boolean recordCursorSupportsRandomAccess() {
        return false;
    }

    @Override
    public boolean hasDescendingOrder() {
        return masterFactory.hasDescendingOrder();
    }

    private class LtJoinLightRecordCursor extends AbstractJoinCursor {
        private final OuterJoinRecord record;
        private final Map joinKeyMap;
        private final int masterTimestampIndex;
        private final int slaveTimestampIndex;
        private Record slaveRecord;
        private Record masterRecord;
        private long slaveTimestamp = Long.MIN_VALUE;
        private long lastSlaveRowID = Long.MIN_VALUE;
        private boolean isOpen;

        public LtJoinLightRecordCursor(
                int columnSplit,
                Map joinKeyMap,
                Record nullRecord,
                int masterTimestampIndex,
                int slaveTimestampIndex
        ) {
            super(columnSplit);
            this.record = new OuterJoinRecord(columnSplit, nullRecord);
            this.joinKeyMap = joinKeyMap;
            this.masterTimestampIndex = masterTimestampIndex;
            this.slaveTimestampIndex = slaveTimestampIndex;
            this.isOpen = true;
        }

        @Override
        public Record getRecord() {
            return record;
        }

        @Override
        public long size() {
            return masterCursor.size();
        }

        @Override
        public boolean hasNext() {

            if (masterCursor.hasNext()) {
                final long masterTimestamp = masterRecord.getTimestamp(masterTimestampIndex);
                MapKey key;
                MapValue value;
                long slaveTimestamp = this.slaveTimestamp;
                if (slaveTimestamp < masterTimestamp) {

                    if (lastSlaveRowID != Numbers.LONG_NaN) {
                        slaveCursor.recordAt(slaveRecord, lastSlaveRowID);
                        key = joinKeyMap.withKey();
                        key.put(slaveRecord, slaveKeySink);
                        value = key.createValue();
                        value.putLong(0, lastSlaveRowID);
                    }

                    final Record rec = slaveCursor.getRecord();
                    while (slaveCursor.hasNext()) {
                        slaveTimestamp = rec.getTimestamp(slaveTimestampIndex);
                        if (slaveTimestamp < masterTimestamp) {
                            key = joinKeyMap.withKey();
                            key.put(rec, slaveKeySink);
                            value = key.createValue();
                            value.putLong(0, rec.getRowId());
                        } else {
                            break;
                        }
                    }

                    // now we have dangling slave record, which we need to hold on to
                    this.slaveTimestamp = slaveTimestamp;
                    this.lastSlaveRowID = rec.getRowId();
                }
                key = joinKeyMap.withKey();
                key.put(masterRecord, masterKeySink);
                value = key.findValue();
                if (value != null) {
                    slaveCursor.recordAt(slaveRecord, value.getLong(0));
                    record.hasSlave(true);
                } else {
                    record.hasSlave(false);
                }

                return true;
            }
            return false;
        }

        @Override
        public void toTop() {
            joinKeyMap.clear();
            slaveTimestamp = Long.MIN_VALUE;
            lastSlaveRowID = Long.MIN_VALUE;
            masterCursor.toTop();
            slaveCursor.toTop();
        }

        void of(RecordCursor masterCursor, RecordCursor slaveCursor) {
            if (!this.isOpen) {
                this.isOpen = true;
                this.joinKeyMap.reallocate();
            }
            this.slaveTimestamp = Long.MIN_VALUE;
            this.lastSlaveRowID = Long.MIN_VALUE;
            this.masterCursor = masterCursor;
            this.slaveCursor = slaveCursor;
            this.masterRecord = masterCursor.getRecord();
            this.slaveRecord = slaveCursor.getRecordB();
            this.record.of(masterRecord, slaveRecord);
        }

        @Override
        public void close() {
            if (isOpen) {
                isOpen = false;
                joinKeyMap.close();
                super.close();
            }
        }
    }
}
