/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.tools.anonymisering

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.common.tools.anonymisering.AnonymiseraDatum;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraHsaId;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraJson;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraPersonId;
import se.inera.intyg.common.tools.anonymisering.AnonymizeString;

class AnonymiseraWebCertDatabas {

    static void main(String[] args) {
        println "Starting anonymization"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraDatum anonymiseraDatum = new AnonymiseraDatum()
        AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId, anonymiseraDatum)
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
        def certificates = bootstrapSql.rows("select INTYGS_ID from INTYG")
        bootstrapSql.close()
        println "${certificates.size()} certificates found to anonymize"
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificates.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.INTYGS_ID
                Sql sql = new Sql(dataSource)
                try {
                    sql.withTransaction {
                        // Anonymisera alla befintliga frågor och svar
                        def certificate = sql.firstRow( '''select PATIENT_PERSONNUMMER, PATIENT_FORNAMN, PATIENT_MELLANNAMN, PATIENT_EFTERNAMN, MODEL,
                                                            SKAPAD_AV_HSAID, SKAPAD_AV_NAMN, SENAST_SPARAD_AV_HSAID, SENAST_SPARAD_AV_NAMN
                                                     from INTYG where INTYGS_ID = :id''' , [id : id])
                        String personNr = anonymiseraPersonId.anonymisera(certificate.PATIENT_PERSONNUMMER)
                        String patientFornamn = certificate.PATIENT_FORNAMN ? AnonymizeString.anonymize(certificate.PATIENT_FORNAMN) : null
                        String patientMellannamn = certificate.PATIENT_MELLANNAMN ? AnonymizeString.anonymize(certificate.PATIENT_MELLANNAMN) : null
                        String patientEfternamn = certificate.PATIENT_EFTERNAMN ? AnonymizeString.anonymize(certificate.PATIENT_EFTERNAMN) : null
                        String model = anonymiseraJson.anonymiseraIntygsJson(new String(certificate.MODEL, 'UTF-8'), personNr)
                        String skapadAvHsaId = anonymiseraHsaId.anonymisera(certificate.SKAPAD_AV_HSAID)
                        String skapadAvNamn = AnonymizeString.anonymize(certificate.SKAPAD_AV_NAMN)
                        String sparadAvHsaId = anonymiseraHsaId.anonymisera(certificate.SENAST_SPARAD_AV_HSAID)
                        String sparadAvNamn = AnonymizeString.anonymize(certificate.SENAST_SPARAD_AV_NAMN)
                        sql.executeUpdate('''update INTYG set PATIENT_PERSONNUMMER = :personNr,
                                                                  PATIENT_FORNAMN = :patientFornamn,      
                                                                  PATIENT_MELLANNAMN = :patientMellannamn,
                                                                  PATIENT_EFTERNAMN = :patientEfternamn,
                                                                  MODEL = :model,
                                                                  SKAPAD_AV_HSAID = :skapadAvHsaId,
                                                                  SKAPAD_AV_NAMN = :skapadAvNamn,
                                                                  SENAST_SPARAD_AV_HSAID = :sparadAvHsaId,
                                                                  SENAST_SPARAD_AV_NAMN = :sparadAvNamn
                                              where INTYGS_ID = :id''',
                                              [personNr: personNr, patientFornamn: patientFornamn,
                                               patientMellannamn: patientMellannamn, patientEfternamn: patientEfternamn,
                                               model: model.getBytes('UTF-8'), skapadAvHsaId: skapadAvHsaId, skapadAvNamn: skapadAvNamn,
                                               sparadAvHsaId: sparadAvHsaId, sparadAvNamn: sparadAvNamn, id: id])
                    }
                    int current = count.addAndGet(1)
                    if (current % 100 == 0) {
                        println "${current} certificates anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
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
        println "${count} certificates anonymized with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        
        start = System.currentTimeMillis()
        bootstrapSql = new Sql(dataSource)
        def questionAnswers = bootstrapSql.rows("select internReferens from FRAGASVAR")
        bootstrapSql.close()
        println "${questionAnswers.size()} question/answers found to anonymize"
        count.set(0)
        errorCount.set(0)
        GParsPool.withPool(numberOfThreads) {
            output = questionAnswers.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.internReferens
                Sql sql = new Sql(dataSource)
                try {
                    sql.withTransaction {
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
                                               vardAktorNamn: vardAktorNamn, id: id])
                        def kompletteringar = sql.rows( 'select TEXT from KOMPLETTERING where FRAGASVAR_ID = :id' , [id : id])
                        if (kompletteringar) {
                            kompletteringar.each {
                                String originalText = it.TEXT
                                if (originalText) {
                                    String text = AnonymizeString.anonymize(originalText)
                                    sql.executeUpdate('update KOMPLETTERING set TEXT = :text where FRAGASVAR_ID = :id and TEXT = :originalText',
                                                      [text: text, id : id, originalText: originalText])
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
        end = System.currentTimeMillis()
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
                    // Anonymisera alla migrerade intyg
                    def migreratIntyg = sql.firstRow( '''select PATIENT_NAMN, PATIENT_SSN, INTYGS_DATA
                                                 from MIGRERADE_INTYG_FRAN_MEDCERT where INTYG_ID = :id''' , [id : id])
                    String patientNamn = AnonymizeString.anonymize(migreratIntyg.PATIENT_NAMN)
                    String personNr = anonymiseraPersonId.anonymisera(migreratIntyg.PATIENT_SSN)
                    // Utkommenterat, eftersom json-formatet för MedCert skiljer sig åt
//                    String jsonDoc = new String(intyg.INTYGS_DATA, 'UTF-8')
//                    String anonymiseradJson = anonymiseraJson.anonymiseraIntygsJson(jsonDoc, personNr)
                    // Byt ut intygsdata mot en fix sträng istället
                    String anonymiseradJson = "XXXXXXXXXX"
                    sql.executeUpdate('''update MIGRERADE_INTYG_FRAN_MEDCERT set PATIENT_NAMN = :patientNamn, PATIENT_SSN = :personNr,
                                                                                 INTYGS_DATA = :document
                                          where INTYG_ID = :id''',
                                          [patientNamn: patientNamn, personNr: personNr,
                                           document: anonymiseradJson.getBytes('UTF-8'),
                                           id: id])
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
    


