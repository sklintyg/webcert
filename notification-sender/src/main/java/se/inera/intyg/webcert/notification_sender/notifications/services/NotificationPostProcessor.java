/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

public class NotificationPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationPostProcessor.class);

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STATUS_UPDATE_FOR_CARE_RESULT = "statusUpdateForCareResult";

    public void process(Exchange exchange) {

        String statusUpdateMessageJson = exchange.getMessage().getBody(String.class);
        CertificateStatusUpdateForCareType statusUpdateMessage = statusUpdateMessageFromJson(statusUpdateMessageJson);
        String statusUpdateResult = exchange.getMessage().getHeader(STATUS_UPDATE_FOR_CARE_RESULT, String.class);

        Handelse eventForPersist = createEventForPersist(statusUpdateMessage, statusUpdateResult);

    }

    private CertificateStatusUpdateForCareType statusUpdateMessageFromJson(String message) {
        try {
            return objectMapper.readValue(message, CertificateStatusUpdateForCareType.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Handelse createEventForPersist(CertificateStatusUpdateForCareType statusUpdateMessage, String statusUpdateResult) {
        //Amneskod topicCode = statusUpdateMessage.getHandelse().getAmne();
        Amneskod topicCode = statusUpdateMessage.getHandelse().getAmne();
        HsaId hanteratAv = statusUpdateMessage.getHanteratAv();
        Handelse event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdateMessage.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        event.setIntygsId(statusUpdateMessage.getIntyg().getIntygsId().getExtension());
        event.setPersonnummer(statusUpdateMessage.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdateMessage.getHandelse().getTidpunkt());
        event.setVardgivarId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        event.setAmne(topicCode != null ? ArendeAmne.valueOf(topicCode.getCode()) : null);
        event.setSistaDatumForSvar(statusUpdateMessage.getHandelse().getSistaDatumForSvar());
        event.setHanteratAv(hanteratAv != null ? hanteratAv.getExtension() : null);
        //event.setSendResult(statusUpdateResult);

        return event;
    }
}
