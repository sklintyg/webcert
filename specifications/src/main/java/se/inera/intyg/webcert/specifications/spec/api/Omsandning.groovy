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
