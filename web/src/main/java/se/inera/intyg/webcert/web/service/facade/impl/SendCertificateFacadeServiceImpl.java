/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@Service
public class SendCertificateFacadeServiceImpl implements SendCertificateFacadeService {

    private final IntygService intygService;
    private final UtkastService utkastService;
    private final CertificateReceiverService certificateReceiverService;


    @Autowired
    public SendCertificateFacadeServiceImpl(IntygService intygService, UtkastService utkastService,
        CertificateReceiverService certificateReceiverService) {
        this.intygService = intygService;
        this.utkastService = utkastService;
        this.certificateReceiverService = certificateReceiverService;
    }

    @Override
    public String sendCertificate(String certificateId) {
        final var certificate = utkastService.getDraft(certificateId, false);
        final var receivers = getMainReceivers(certificate.getIntygsTyp());

        List<IntygServiceResult> results = new ArrayList<>();
        receivers.forEach((r) -> {
            results.add(intygService.sendIntyg(certificateId, certificate.getIntygsTyp(), r.getId(), false));
        });
        return results.stream().allMatch(r -> r == IntygServiceResult.OK) ? IntygServiceResult.OK.toString()
            : IntygServiceResult.FAILED.toString();
    }

    private List<IntygReceiver> getMainReceivers(String type) {
        return certificateReceiverService.listPossibleReceivers(type)
            .stream()
            .filter(IntygReceiver::isLocked)
            .collect(Collectors.toList());
    }

}
