/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.CertificateEventDTO.ExtendedEventMessage;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;

@Component
public class CertificateEventConverter {

    @Autowired
    private IntygService intygService;

    public CertificateEventDTO convertToCertificateEventDTO(CertificateEvent event) {
        CertificateEventDTO eventDto = new CertificateEventDTO(event);

        if (event.getEventCode() == EventCode.ERSATTER
            || event.getEventCode() == EventCode.FORLANGER
            || event.getEventCode() == EventCode.SKAPATFRAN
            || event.getEventCode() == EventCode.KOPIERATFRAN
            || event.getEventCode() == EventCode.KOMPLETTERAR) {
            Optional<ExtendedEventMessage> dataForExtendedMessage = getDataForExtendedMessage(event);
            dataForExtendedMessage.ifPresent(message -> eventDto.setExtendedMessage(message));
        }
        return eventDto;
    }

    private Optional<ExtendedEventMessage> getDataForExtendedMessage(CertificateEvent event) {
        IntygContentHolder currentCertificate = intygService.fetchIntygDataForInternalUse(event.getCertificateId(), true);
        if (currentCertificate != null && currentCertificate.getRelations() != null) {
            WebcertCertificateRelation parent = currentCertificate.getRelations().getParent();

            if (parent != null) {
                IntygTypeInfo parentIntygTypeInfo = intygService.getIntygTypeInfo(parent.getIntygsId());

                ExtendedEventMessage message = new ExtendedEventMessage();
                message.setOriginalCertificateId(parent.getIntygsId());
                message.setOriginalCertificateType(parentIntygTypeInfo.getIntygType());
                message.setOriginalCertificateTypeVersion(parentIntygTypeInfo.getIntygTypeVersion());
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }
}
