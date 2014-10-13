package se.inera.certificate.mc2wc.certsigndatefix

import groovy.sql.Sql
import au.com.bytecode.opencsv.CSVReader
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.LocalDateTime

class UpdateCertSignDate {

    static void main(String[] args) {
        println "Startar uppdatering av signeringsdatum"

        long start = System.currentTimeMillis()

        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }

        def config = new ConfigSlurper().parse(props)

        def medcertDataMap = [:]

        List<String[]> medcertData = new CSVReader(
                new InputStreamReader(this.getClass().getResourceAsStream("/kronoberg.txt"))).readAll()

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")

        medcertData.each { row ->
            LocalDateTime certSignDate = formatter.parseLocalDateTime(row[1])
            medcertDataMap.put(row[0], certSignDate)
        }

        println "Laddat ${medcertDataMap.size()} signeringsdatum från Medcert"

        Sql webcertSQL = Sql.newInstance(config.dataSource.wc.url, config.dataSource.wc.username, config.dataSource.wc.password, config.dataSource.wc.driver)

        Sql intygstjanstSQL = Sql.newInstance(config.dataSource.intyg.url, config.dataSource.intyg.username, config.dataSource.intyg.password, config.dataSource.intyg.driver)

        int updatedCount = 0
        int fetchedFromIT = 0
        int fetchedFromFile = 0

        webcertSQL.eachRow("SELECT DISTINCT INTYGS_ID FROM FRAGASVAR WHERE SIGNERINGS_DATUM IS NULL") { row ->
            def intygsId = row.INTYGS_ID
            java.sql.Timestamp sqlSignDate

            def intyg = intygstjanstSQL.firstRow("SELECT SIGNED_DATE FROM CERTIFICATE WHERE ID = :id" , [id : intygsId])

            if (intyg) {
                sqlSignDate = intyg.SIGNED_DATE
                fetchedFromIT++
            } else {
                LocalDateTime medcertSignDate = medcertDataMap.get(intygsId)

                if (medcertSignDate) {
                    sqlSignDate = new java.sql.Timestamp(medcertSignDate.toDate().getTime())
                    fetchedFromFile++
                }
            }

            if (sqlSignDate) {
                int updates = webcertSQL.executeUpdate("UPDATE FRAGASVAR SET SIGNERINGS_DATUM = :signDate WHERE INTYGS_ID = :intygsId",
                        [signDate : sqlSignDate, intygsId : intygsId])
                updatedCount = updatedCount + updates
            } else {
                println "+ Fel! Hittade inget signeringsdatum för intyg ${intygsId}"
            }

            if (updatedCount % 100 == 0) {
                println "+ ${updatedCount} rader i fragasvar uppdaterade"
            }
        }

        webcertSQL.close()
        intygstjanstSQL.close();

        long end = System.currentTimeMillis()

        println "Klart! ${updatedCount} rader i fragasvar uppdaterade med signeringsdatum på ${(int)((end-start) / 1000)} sekunder"
        println "${fetchedFromIT} hämtade från Intyg, ${fetchedFromFile} hämtade från textfil"
    }
}
