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

package se.inera.intyg.webcert.specifications.spec.api

import static groovyx.net.http.ContentType.JSON

import org.joda.time.LocalDateTime

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper
import se.inera.intyg.webcert.logmessages.Enhet
import se.inera.intyg.webcert.logmessages.IntygCreateMessage
import se.inera.intyg.webcert.logmessages.IntygDeleteMessage
import se.inera.intyg.webcert.logmessages.IntygPrintMessage
import se.inera.intyg.webcert.logmessages.IntygReadMessage
import se.inera.intyg.webcert.logmessages.IntygRevokeMessage
import se.inera.intyg.webcert.logmessages.IntygSendMessage
import se.inera.intyg.webcert.logmessages.IntygSignMessage
import se.inera.intyg.webcert.logmessages.IntygUpdateMessage
import se.inera.intyg.webcert.logmessages.Patient
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class ProduceraLoggMeddelande extends RestClientFixture {

    def restClient = createRestClient(logSenderBaseUrl)

    def CustomObjectMapper objectMapper = new CustomObjectMapper()
    
    def aktivitet
    def vardperson
    def personnummer
    def intygId = "intyg-123"
    def enhet = "enEnhet"
    def vardgivare = "enVårdgivare"
    def namn = "namn"
    def systemId = "systemId"
    def systemNamn = "systemNamn"

    def execute() {
        def m
        switch(aktivitet) {
        case "Läsa":
            m = new IntygReadMessage(intygId)
            break
        case "Skapa":
            m = new IntygCreateMessage(intygId)
            break
        case "Spara":
            m = new IntygUpdateMessage(intygId)
            break
        case "Signera":
            m = new IntygSignMessage(intygId)
            break
        case "Radera":
            m = new IntygDeleteMessage(intygId)
            break
        case "Utskrift":
            m = new IntygPrintMessage(intygId, "Intyg")
            break
        case "Återkalla":
            m = new IntygRevokeMessage(intygId)
            break
        case "SkickaTillMottagare":
            m = new IntygSendMessage(intygId, "Mottagare")
            break
        }
        m.systemId = systemId
        m.systemName = systemNamn
        m.timestamp = new LocalDateTime()
        m.userId = vardperson
        m.userCareUnit = new Enhet(enhet, enhet, vardgivare, vardgivare)
        m.resourceOwner = new Enhet(enhet, enhet, vardgivare, vardgivare)
        if (personnummer) {
            m.patient = new Patient(personnummer, namn)
        }
        println objectMapper.writeValueAsString(m)
        restClient.post(
            path: "logMessages/",
            body: objectMapper.writeValueAsString(m),
            requestContentType: JSON
        )
    }

}
