/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.receiver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

@Service
public class CertificateReceiverServiceImpl implements CertificateReceiverService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateReceiverServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private ListPossibleReceiversResponderInterface listPossibleReceiversClient;

    @Autowired
    private RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient;

    @Override
    public List<IntygReceiver> listPossibleReceivers(String intygsTyp) {

        if (Strings.isNullOrEmpty(intygsTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "intygsTyp must be specified");
        }

        ListPossibleReceiversType request = new ListPossibleReceiversType();
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        request.setIntygTyp(typAvIntyg);
        ListPossibleReceiversResponseType response = listPossibleReceiversClient.listPossibleReceivers(logicalAddress, request);

        if (response == null || response.getReceiverList() == null || response.getReceiverList().size() == 0) {
            LOG.warn("Call to ListPossibleReceivers for intygstyp '{}' returned no possible receivers, check recipient "
                    + "configuration in intygstjänsten.");
            return new ArrayList<>();
        }

        return response.getReceiverList().stream().map(rcpt -> IntygReceiver.IntygReceiverBuilder.anIntygReceiver()
                .withId(rcpt.getReceiverId())
                .withName(rcpt.getReceiverName())
                .withReceiverType(rcpt.getReceiverType().name())
                .build())
                .collect(Collectors.toList());
    }

    @Override
    public void registerApprovedReceivers(String intygsId, List<String> receiverIds) {
        if (Strings.isNullOrEmpty(intygsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "intygsId must be specified");
        }

        RegisterApprovedReceiversType req = new RegisterApprovedReceiversType();
        IntygId intygId = new IntygId();
        intygId.setExtension(intygsId);
        req.setIntygId(intygId);
        req.getApprovedReceivers().addAll(receiverIds);

        registerApprovedReceiversClient.registerApprovedReceivers(logicalAddress, req);
    }
}
