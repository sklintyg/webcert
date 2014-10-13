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
        
        List<String[]> medcertData
        new File(config.file.medcert).withInputStream { stream ->
            medcertData = new CSVReader(
                    new InputStreamReader(stream)).readAll()
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
        
        def medcertDataMap = [:]
        medcertData.each { row ->
            LocalDateTime certSignDate = formatter.parseLocalDateTime(row[1])
            medcertDataMap.put(row[0], certSignDate)
        }

        println "Laddat ${medcertDataMap.size()} signeringsdatum från fil ${config.file.medcert}"

        Sql webcertSQL = Sql.newInstance(config.dataSource.wc.url, config.dataSource.wc.username, config.dataSource.wc.password, config.dataSource.wc.driver)

        int updatedCount = 0
        
        webcertSQL.eachRow("SELECT DISTINCT INTYGS_ID FROM FRAGASVAR WHERE SIGNERINGS_DATUM IS NULL") { row ->
            def intygsId = row.INTYGS_ID

            LocalDateTime medcertSignDate = medcertDataMap.get(intygsId)

            if (medcertSignDate) {
                java.sql.Timestamp sqlSignDate = new java.sql.Timestamp(medcertSignDate.toDate().getTime())
                int updates = webcertSQL.executeUpdate("UPDATE FRAGASVAR SET SIGNERINGS_DATUM = :signDate, INTYGS_TYP = 'fk7263' WHERE INTYGS_ID = :intygsId",
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

        long end = System.currentTimeMillis()

        println "Klart! ${updatedCount} rader i fragasvar uppdaterade med signeringsdatum på ${(int)((end-start) / 1000)} sekunder"
    }
}
