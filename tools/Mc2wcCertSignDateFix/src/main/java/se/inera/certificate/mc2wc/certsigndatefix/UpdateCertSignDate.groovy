package se.inera.certificate.mc2wc.certsigndatefix

import groovy.sql.Sql

class UpdateCertSignDate {

    static void main(String[] args) {
        println "Startar uppdatering av signeringsdatum"

        long start = System.currentTimeMillis()

        def props = new Properties()
        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        
        def config = new ConfigSlurper().parse(props)
        
        Sql webcertSQL = Sql.newInstance(config.dataSource.wc.url, config.dataSource.wc.username, config.dataSource.wc.password, config.dataSource.wc.driver)
        
        Sql intygstjanstSQL = Sql.newInstance(config.dataSource.intyg.url, config.dataSource.intyg.username, config.dataSource.intyg.password, config.dataSource.intyg.driver)
        
        int count = 0
                
        webcertSQL.eachRow("SELECT DISTINCT INTYGS_ID FROM FRAGASVAR WHERE SIGNERINGS_DATUM IS NULL") { row ->
            def intygsId = row.INTYGS_ID
       
            def intyg = intygstjanstSQL.firstRow( "SELECT SIGNED_DATE FROM CERTIFICATE WHERE ID = :id" , [id : intygsId])
       
            webcertSQL.executeUpdate("UPDATE FRAGASVAR SET SIGNERINGS_DATUM = :signDate WHERE INTYGS_ID = :intygsId", [signDate : intyg.SIGNED_DATE, intygsId : intygsId])
            
            if (++count % 100 == 0) {
                println "+ ${count} fragasvar uppdaterade"
            }
        }      
        
        webcertSQL.close()
        intygstjanstSQL.close();
        
        long end = System.currentTimeMillis()
        
        println "Klart! ${count} fragasvar uppdaterade med signeringsdatum på ${(int)((end-start) / 1000)} sekunder"
    }
}
