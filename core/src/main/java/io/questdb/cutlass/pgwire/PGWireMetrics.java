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

package io.questdb.cutlass.pgwire;

import io.questdb.metrics.Gauge;
import io.questdb.metrics.MetricsRegistry;

public class PGWireMetrics {

    private final Gauge cachedSelectsGauge;
    private final Gauge cachedUpdatesGauge;

    public PGWireMetrics(MetricsRegistry metricsRegistry) {
        this.cachedSelectsGauge = metricsRegistry.newGauge("pg_wire_select_queries_cached");
        this.cachedUpdatesGauge = metricsRegistry.newGauge("pg_wire_update_queries_cached");
    }

    public Gauge cachedSelectsGauge() {
        return cachedSelectsGauge;
    }

    public Gauge cachedUpdatesGauge() {
        return cachedUpdatesGauge;
    }
}
