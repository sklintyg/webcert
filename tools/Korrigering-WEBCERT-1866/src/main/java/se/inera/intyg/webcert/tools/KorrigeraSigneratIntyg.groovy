package se.inera.intyg.webcert.tools

import groovy.sql.Sql

import java.util.concurrent.atomic.AtomicInteger

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

import org.apache.commons.dbcp2.BasicDataSource

/**
 * WEBCERT-1866:
 * Korrigering av signerade intyg, där utkastet skrivits över med gammal version.
 */
class KorrigeraSigneratIntyg {

    static void main(String[] args) {

        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        
        BasicDataSource intygstjanstDataSource =
            new BasicDataSource(driverClassName: config.intygstjanst.dataSource.driver, url: config.intygstjanst.dataSource.url,
                                username: config.intygstjanst.dataSource.username, password: config.intygstjanst.dataSource.password,
                                initialSize: 1, maxTotal: 1)
        def intygstjanstSql = new Sql(intygstjanstDataSource)
        BasicDataSource webcertDataSource =
            new BasicDataSource(driverClassName: config.webcert.dataSource.driver, url: config.webcert.dataSource.url,
                                username: config.webcert.dataSource.username, password: config.webcert.dataSource.password,
                                initialSize: 1, maxTotal: 1)
        def webcertSql = new Sql(webcertDataSource)
        
        args.each {intygsId ->
            try {
                def row = intygstjanstSql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :intygsId' , [intygsId : intygsId])
                webcertSql.execute('update INTYG set MODEL = :model where INTYGS_ID = :intygsId', [model: row.DOCUMENT, intygsId : intygsId])
                println "Intyg '${intygsId}' korrigerat"
            } catch (Exception e) {
                println "Korrigering av intyg '${intygsId}' misslyckades:"
                println "${e.message}"
            }
        }
        intygstjanstSql.close()
        webcertSql.close()
        
    }
    
}
