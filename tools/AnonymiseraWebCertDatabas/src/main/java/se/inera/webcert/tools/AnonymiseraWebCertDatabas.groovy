package se.inera.webcert.tools

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.certificate.tools.anonymisering.AnonymiseraDatum;
import se.inera.certificate.tools.anonymisering.AnonymiseraHsaId;
import se.inera.certificate.tools.anonymisering.AnonymiseraJson;
import se.inera.certificate.tools.anonymisering.AnonymiseraPersonId;
import se.inera.certificate.tools.anonymisering.AnonymiseraXml;
import se.inera.certificate.tools.anonymisering.AnonymizeString;

class AnonymiseraWebCertDatabas {

    static void main(String[] args) {
        println "Starting anonymization"
        
        int numberOfThreads = args.length > 0 ? args[0] : 4
        long start = System.currentTimeMillis()
        AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraDatum anonymiseraDatum = new AnonymiseraDatum()
        AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId, anonymiseraDatum)
        AnonymiseraXml anonymiseraXml = new AnonymiseraXml(anonymiseraPersonId, anonymiseraHsaId, anonymiseraDatum)
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)
        def questionAnswers = bootstrapSql.rows("select internReferens from FRAGASVAR")
        bootstrapSql.close()
        println "${questionAnswers.size()} question/answers found to anonymize"
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = questionAnswers.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.internReferens
                Sql sql = new Sql(dataSource)
                try {
                    // Anonymisera alla befintliga frågor och svar
                    def fragasvar = sql.firstRow( '''select PATIENT_NAMN, PATIENT_ID_EXTENSION, FRAGE_STALLARE, FRAGE_TEXT,
                                                        SVARS_TEXT, FORSKRIVAR_KOD, HSAID, NAMN, VARD_AKTOR_HSAID, VARD_AKTOR_NAMN
                                                 from FRAGASVAR where internReferens = :id''' , [id : id])
                    String patientNamn = AnonymizeString.anonymize(fragasvar.PATIENT_NAMN)
                    String personNr = anonymiseraPersonId.anonymisera(fragasvar.PATIENT_ID_EXTENSION)
                    String frageStallare = fragasvar.FRAGE_STALLARE ? AnonymizeString.anonymize(fragasvar.FRAGE_STALLARE) : null
                    String frageText = fragasvar.FRAGE_TEXT ? AnonymizeString.anonymize(fragasvar.FRAGE_TEXT) : null
                    String svarsText = fragasvar.SVARS_TEXT ? AnonymizeString.anonymize(fragasvar.SVARS_TEXT) : null
                    String forskrivarKod = fragasvar.FORSKRIVAR_KOD ? AnonymizeString.anonymize(fragasvar.FORSKRIVAR_KOD) : null
                    String hsaId = fragasvar.HSAID ? anonymiseraHsaId.anonymisera(fragasvar.HSAID) : null
                    String namn = fragasvar.NAMN ? AnonymizeString.anonymize(fragasvar.NAMN) : null
                    String vardAktorHsaId = fragasvar.VARD_AKTOR_HSAID ? anonymiseraHsaId.anonymisera(fragasvar.VARD_AKTOR_HSAID) : null
                    String vardAktorNamn = fragasvar.VARD_AKTOR_NAMN ? AnonymizeString.anonymize(fragasvar.VARD_AKTOR_NAMN) : null
                    sql.executeUpdate('''update FRAGASVAR set PATIENT_NAMN = :patientNamn, PATIENT_ID_EXTENSION = :personNr, 
                                                              FRAGE_STALLARE = :frageStallare,
                                                              FRAGE_TEXT = :frageText, SVARS_TEXT = :svarsText, FORSKRIVAR_KOD = :forskrivarKod,
                                                              HSAID = :hsaId, NAMN = :namn, VARD_AKTOR_HSAID = :vardAktorHsaId,
                                                              VARD_AKTOR_NAMN = :vardAktorNamn
                                          where internReferens = :id''',
                                          [patientNamn: patientNamn, personNr: personNr,
                                           frageStallare: frageStallare, frageText: frageText, svarsText: svarsText,
                                           forskrivarKod: forskrivarKod, hsaId: hsaId, namn: namn, vardAktorHsaId: vardAktorHsaId,
                                           vardAktorNamn: vardAktorNamn])
                    def kompletteringar = sql.rows( 'select TEXT from KOMPLETTERING where FRAGASVAR_ID = :id' , [id : id])
                    if (kompletteringar) {
                        kompletteringar.each {
                            String orginalText = it.TEXT
                            if (orginalText) {
                                String text = AnonymizeString.anonymize(orginalText)
                                sql.executeUpdate('update KOMPLETTERING set TEXT = :text where FRAGASVAR_ID = :id and TEXT = :originalText',
                                                  [text: text, id : id, orginalText: orginalText])
                            }
                        }
                    }
                    def externaKontakter = sql.rows( 'select KONTAKT from EXTERNA_KONTAKTER where FRAGASVAR_ID = :id' , [id : id])
                    if (externaKontakter) {
                        externaKontakter.each {
                            String originalKontakt = AnonymizeString.anonymize(it.KONTAKT)
                            if (originalKontakt) {
                                String kontakt = AnonymizeString.anonymize(originalKontakt)
                                sql.executeUpdate('update EXTERNA_KONTAKTER set KONTAKT = :kontakt where FRAGASVAR_ID = :id and KONTAKT = :originalKontakt',
                                                  [kontakt: kontakt, id : id, originalKontakt: originalKontakt])
                            }
                        }
                    }
                    int current = count.addAndGet(1)
                    if (current % 1000 == 0) {
                        println "${current} question/answers anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
                    }
                } catch (Throwable t) {
                    t.printStackTrace()
                    result << "Anonymizing ${id} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }
                result.toString()
            }
        }
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "${count} question/answers anonymized with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        
        start = System.currentTimeMillis()
        bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows("select INTYG_ID from MIGRERADE_INTYG_FRAN_MEDCERT")
        bootstrapSql.close()
        println "${certificateIds.size()} imported certificates found to anonymize"
        count.set(0)
        errorCount.set(0)
        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.INTYG_ID
                Sql sql = new Sql(dataSource)
                try {
                    // Anonymisera alla befintliga frågor och svar
                    def fragasvar = sql.firstRow( '''select PATIENT_NAMN, PATIENT_SSN, INTYGS_DATA
                                                 from MIGRERADE_INTYG_FRAN_MEDCERT where INTYG_ID = :id''' , [id : id])
                    String patientNamn = AnonymizeString.anonymize(fragasvar.PATIENT_NAMN)
                    String personNr = anonymiseraPersonId.anonymisera(fragasvar.PATIENT_SSN)
                    // Utkommenterat, eftersom json-formatet för MedCert skiljer sig åt
//                    String jsonDoc = new String(intyg.INTYGS_DATA, 'UTF-8')
//                    String anonymiseradJson = anonymiseraJson.anonymiseraIntygsJson(jsonDoc, personNr)
                    // Byt ut intygsdata mot en fix sträng istället
                    String anonymiseradJson = "XXXXXXXXXX"
                    sql.executeUpdate('''update MIGRERADE_INTYG_FRAN_MEDCERT set PATIENT_NAMN = :patientNamn, PATIENT_SSN = :personNr,
                                                                                 INTYGS_DATA = :document
                                          where INTYG_ID = :id''',
                                          [patientNamn: patientNamn, personNr: personNr,
                                           document: anonymiseradJson.getBytes('UTF-8')])
                    int current = count.addAndGet(1)
                    if (current % 1000 == 0) {
                        println "${current} imported certificates anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
                    }
                } catch (Throwable t) {
                    t.printStackTrace()
                    result << "Anonymizing ${id} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }
                result.toString()
            }
        }
        end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "${count} imported certificates anonymized with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"

    }
}
    


