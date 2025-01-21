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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@Service("sendCertificateFromWebcert")
public class SendCertificateFacadeServiceImpl implements SendCertificateFacadeService {

    private final IntygService intygService;
    private final CertificateReceiverService certificateReceiverService;

    @Autowired
    public SendCertificateFacadeServiceImpl(IntygService intygService, CertificateReceiverService certificateReceiverService) {
        this.intygService = intygService;
        this.certificateReceiverService = certificateReceiverService;
    }

    @Override
    public String sendCertificate(String certificateId) {
        final var intygTypeInfo = intygService.getIntygTypeInfo(certificateId);
        final var receivers = getMainReceivers(intygTypeInfo.getIntygType());

        return receivers.stream()
            .map(r -> intygService.sendIntyg(certificateId, intygTypeInfo.getIntygType(), r.getId(), false))
            .reduce((r1, r2) -> r1 != IntygServiceResult.OK ? r1 : r2)
            .orElse(IntygServiceResult.FAILED)
            .toString();
    }

    private List<IntygReceiver> getMainReceivers(String type) {
        return certificateReceiverService.listPossibleReceivers(type)
            .stream()
            .filter(IntygReceiver::isLocked)
            .collect(Collectors.toList());
    }
}
