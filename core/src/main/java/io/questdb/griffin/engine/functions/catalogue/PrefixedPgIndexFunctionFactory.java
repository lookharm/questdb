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

package io.questdb.griffin.engine.functions.catalogue;

import io.questdb.cairo.ColumnType;
import io.questdb.cairo.GenericRecordMetadata;
import io.questdb.cairo.TableColumnMetadata;
import io.questdb.cairo.sql.RecordMetadata;

public class PrefixedPgIndexFunctionFactory extends AbstractEmptyCatalogueFunctionFactory {
    private final static RecordMetadata METADATA;

    public PrefixedPgIndexFunctionFactory() {
        super("pg_catalog.pg_index()", METADATA);
    }

    @Override
    public boolean isRuntimeConstant() {
        return true;
    }

    static {
        final GenericRecordMetadata metadata = new GenericRecordMetadata();
        metadata.add(new TableColumnMetadata("indkey", 1, ColumnType.INT));
        metadata.add(new TableColumnMetadata("indrelid", 2, ColumnType.INT));
        metadata.add(new TableColumnMetadata("indexrelid", 3, ColumnType.INT));
        metadata.add(new TableColumnMetadata("indisprimary", 4, ColumnType.BOOLEAN));
        METADATA = metadata;
    }
}
