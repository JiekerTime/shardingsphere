/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.metadata.data;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.shardingsphere.ShardingSphereTableStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectTableStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingTableStatisticsCollectorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private DialectTableStatisticsCollector statisticsCollector;
    
    @BeforeEach
    void setUp() {
        statisticsCollector = TypedSPILoader.getService(ShardingSphereTableStatisticsCollector.class, "shardingsphere.sharding_table_statistics");
    }
    
    @Test
    void assertCollectWithoutShardingRule() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        Collection<Map<String, Object>> actualRows = statisticsCollector.collect("foo_db", "shardingsphere", "sharding_table_statistics", metaData);
        assertTrue(actualRows.isEmpty());
    }
    
    @Test
    void assertCollectWithShardingRule() throws SQLException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingTables()).thenReturn(Collections.singletonMap("foo_tbl", new ShardingTable(Arrays.asList("ds_0", "ds_1"), "foo_tbl")));
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mock(StorageUnit.class, RETURNS_DEEP_STUBS));
        storageUnits.put("ds_1", mock(StorageUnit.class, RETURNS_DEEP_STUBS));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), storageUnits), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        Collection<Map<String, Object>> actualRows = statisticsCollector.collect("foo_db", "shardingsphere", "sharding_table_statistics", metaData);
        assertFalse(actualRows.isEmpty());
        Collection<Map<String, Object>> expectedRows = new LinkedList<>();
        expectedRows.add(createRowColumnValues(1L, "foo_db", "foo_tbl", "ds_0", "foo_tbl", new BigDecimal("0"), new BigDecimal("0")));
        expectedRows.add(createRowColumnValues(2L, "foo_db", "foo_tbl", "ds_1", "foo_tbl", new BigDecimal("0"), new BigDecimal("0")));
        assertRowsValue(expectedRows, actualRows);
    }
    
    private Map<String, Object> createRowColumnValues(final long id, final String logicDatabaseName, final String logicTableName, final String actualDatabaseName,
                                                      final String actualTableName, final BigDecimal rowCount, final BigDecimal size) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("logic_database_name", logicDatabaseName);
        result.put("logic_table_name", logicTableName);
        result.put("actual_database_name", actualDatabaseName);
        result.put("actual_table_name", actualTableName);
        result.put("row_count", rowCount);
        result.put("size", size);
        return result;
    }
    
    private void assertRowsValue(final Collection<Map<String, Object>> expectedRows, final Collection<Map<String, Object>> actualRows) {
        assertThat(actualRows.size(), is(expectedRows.size()));
        Iterator<Map<String, Object>> actualRowsIterator = actualRows.iterator();
        for (Map<String, Object> each : expectedRows) {
            Map<String, Object> actualRow = actualRowsIterator.next();
            for (Entry<String, Object> entry : each.entrySet()) {
                assertTrue(actualRow.containsKey(entry.getKey()));
                assertThat(actualRow.get(entry.getKey()), is(entry.getValue()));
            }
        }
    }
}
