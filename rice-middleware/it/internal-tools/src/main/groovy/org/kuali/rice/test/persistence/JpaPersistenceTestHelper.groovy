/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.test.persistence

import org.eclipse.persistence.jpa.jpql.parser.DateTime
import org.junit.Assert
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader
import org.kuali.rice.krad.data.DataObjectService
import org.kuali.rice.krad.data.KradDataServiceLocator
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource
import java.sql.Timestamp

/**
 * Helps with BO persistence tests
 */
class JpaPersistenceTestHelper {
    def DataObjectService boService;
    def DataSource datasource;

    JpaPersistenceTestHelper(String dsName) {
        boService = KradDataServiceLocator.getDataObjectService()
        datasource = (DataSource) GlobalResourceLoader.getService(dsName)
        if (!datasource) {
            throw new RuntimeException("DataSource bean not found: " + dsName)
        }
    }

    def bool(value, column) {
        [ (column): value ? "Y" : "N" ]
    }
    def default_field(bo) {
        bool(bo.dflt, 'DFLT_IND')
    }
    def active_field(bo) {
        bool(bo.active, 'ACTV_IND')
    }
    def edit_field(bo) {
        bool(bo.edit, 'EDIT_FLAG')
    }

    def basic_fields(bo) {
        [ OBJ_ID: bo.objectId,
          VER_NBR: bo.versionNumber ]
    }

    def standard_fields(bo) {
        active_field(bo) + basic_fields(bo)
    }

    def genDbTimestamp() {
        // this should not be rocket science but we have to deal
        // but it appears mysql (driver?) is truncating time component of datetimes
        // so we can only portably test timestamps without times...
        new Timestamp(new Date().time)
    }

    def toDbTimestamp(DateTime datetime) {
        return toDbTimestamp(datetime.millis)
    }

    def toDbTimestamp(long millis) {
        def timestamp = new java.sql.Timestamp(millis)
        timestamp.nanos = 0
        timestamp
    }

    def assertRow(Map fields, table, pk="id", ignore=["LAST_UPDT_DT","OBJ_ID","VER_NBR"]) {
        def pk_val = fields[pk]
        if (!pk_val) {
            throw new RuntimeException("No primary key value found for field: " + pk)
        }
        def sql = "select * from " + table + " where " + pk + " = '${pk_val}' "
        println "Running Query: $sql"
        Map row = new JdbcTemplate(datasource).queryForMap( sql )
        row.keySet().removeAll(ignore)
        fields.keySet().removeAll(ignore)
        /*for (Map.Entry e: fields.entrySet()) {
            println(e.getKey().getClass());
            println(e.getValue().getClass());
            println(e.getKey());
            println(e.getValue());
        }
        for (Map.Entry e: row.entrySet()) {
            println(e.getKey().getClass());
            println(e.getValue().getClass());
            println(e.getKey());
            println(e.getValue());
        }*/

        Assert.assertTrue("Error Saved data does not match: \n" + new TreeMap( row ) + "\n != \n" + new TreeMap( fields ), new TreeMap( fields ) == new TreeMap( row ));
    }
}
