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
package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.event.CertificateEventService;

@Component
@RequiredArgsConstructor
public class UpdateIntygstjanstTestabilityUtil {

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;
    private final SendCertificateServiceClient sendCertificateServiceClient;
    private final CertificateEventService certificateEventService;
    private final ObjectMapper objectMapper;
    private final IntygModuleRegistry moduleRegistry;

    public void update(Utkast utkast, HoSPersonal hosPersonal, String personId) {
        try {
            if (utkast.getSignatur() != null) {
                final var moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
                moduleApi.registerCertificate(utkast.getModel(), logicalAddress);

                certificateEventService.createCertificateEvent(utkast.getIntygsId(), personId, EventCode.SIGNAT,
                    String.format("Certificate type: %s", utkast.getIntygsTyp()));

                if (utkast.getSkickadTillMottagareDatum() != null) {
                    sendCertificateServiceClient.sendCertificate(
                        utkast.getIntygsId(),
                        utkast.getPatientPersonnummer().getPersonnummer(),
                        objectMapper.writeValueAsString(hosPersonal),
                        utkast.getSkickadTillMottagare(),
                        logicalAddress
                    );
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
