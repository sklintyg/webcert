/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.specifications.spec.util.sql

import groovy.sql.Sql

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.common.specifications.spec.util.FitnesseHelper

class SQLFixture {

    private BasicDataSource dataSource
    
    SQLFixture() {
        def props = new Properties()
        FitnesseHelper.getFile("dataSource.properties").withInputStream { stream ->
        props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        dataSource = new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                        username: config.dataSource.username, password: config.dataSource.password,
                        initialSize: 1, maxTotal: 1)
    }
    
    protected def execute(String statement, def args) {
        def result
        def sql
        try {
            sql = new Sql(dataSource)
            result = sql.execute(statement, args)
        } finally { sql?.close() }
        result
    }

    protected def firstRow(String statement, def args) {
        def result
        def sql
        try {
            sql = new Sql(dataSource)
            result = sql.firstRow(statement, args)
        } finally { sql?.close() }
        result
    }
}
