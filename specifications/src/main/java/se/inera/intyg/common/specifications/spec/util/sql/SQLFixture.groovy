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
