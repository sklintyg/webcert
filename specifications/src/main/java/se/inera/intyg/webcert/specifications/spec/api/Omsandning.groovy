package se.inera.intyg.webcert.specifications.spec.api

import java.sql.Timestamp

import org.joda.time.LocalDateTime

import se.inera.intyg.common.specifications.spec.util.sql.SQLFixture

class Omsandning extends SQLFixture {
    
    private def statement = "insert into OMSANDNING (OMSANDNING_ID, INTYG_ID, NASTA_FORSOK, ANTAL_FORSOK, GALLRINGSDATUM, OPERATION, KONFIGURATION) values (:id, :intygsId, :nastaForsok, 0, :gallringsDatum, :operation, :config);"
    private LocalDateTime nastaForsok, gallringsDatum
    
    String id, intygsId, tid, gallring, operation
    
    void execute() {
       nastaForsok = tid ? LocalDateTime.parse(tid) : LocalDateTime.now()
       gallringsDatum = gallring ? LocalDateTime.parse(gallring) : LocalDateTime.now().plusDays(1)
       operation = operation ?: "STORE_INTYG"
       def config = operation == "STORE_INTYG" ? "{}" : '{"recipient":"FK"}'
       super.execute(statement, [id: id, intygsId: intygsId, nastaForsok: new Timestamp(nastaForsok.toDate().getTime()), gallringsDatum: new Timestamp(gallringsDatum.toDate().getTime()), operation: operation, config: config])
    }
}
