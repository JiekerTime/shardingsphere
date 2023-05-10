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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dal;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlushStatementHandlerTest {
    
    @Test
    void assertGetSimpleTableSegmentForMySQL() {
        MySQLFlushStatement mySQLFlushStatement = new MySQLFlushStatement();
        assertTrue(FlushStatementHandler.getSimpleTableSegment(mySQLFlushStatement).isEmpty());
        mySQLFlushStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 2, new IdentifierValue("foo_table"))));
        assertThat(FlushStatementHandler.getSimpleTableSegment(mySQLFlushStatement), is(mySQLFlushStatement.getTables()));
    }
    
    @Test
    void assertGetSimpleTableSegmentForOtherDatabases() {
        assertTrue(FlushStatementHandler.getSimpleTableSegment(null).isEmpty());
    }
}