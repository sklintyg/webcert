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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.ws.WebServiceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
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
    private ListApprovedReceiversResponderInterface listApprovedReceiversClient;

    @Autowired
    private CertificateSenderService certificateSenderService;

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Registration of approved receivers goes through our messaging solution.
     *
     * @param intygsId
     * @param receiverIds
     */
    @Override
    public void registerApprovedReceivers(String intygsId, List<String> receiverIds) {
        if (Strings.isNullOrEmpty(intygsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "intygsId must be specified");
        }

        try {
            certificateSenderService.sendRegisterApprovedReceivers(intygsId, objectMapper.writeValueAsString(receiverIds));
        } catch (JsonProcessingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not convert list of approved receivers to JSON array.");
        }
    }

    @Override
    public List<IntygReceiver> listPossibleReceiversWithApprovedInfo(String intygsTyp, String intygsId) {
        if (Strings.isNullOrEmpty(intygsTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "intygsTyp must be specified");
        }
        if (Strings.isNullOrEmpty(intygsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "intygsId must be specified");
        }

        try {
            List<IntygReceiver> intygReceivers = listPossibleReceivers(intygsTyp);

            ListApprovedReceiversType req = new ListApprovedReceiversType();
            IntygId intygId = new IntygId();
            intygId.setExtension(intygsId);
            req.setIntygsId(intygId);


            ListApprovedReceiversResponseType resp = listApprovedReceiversClient.listApprovedReceivers(logicalAddress, req);
            List<String> approvedReceiverIds = resp.getReceiverList().stream().map(receiver -> receiver.getReceiverId())
                    .collect(Collectors.toList());

            for (IntygReceiver ir : intygReceivers) {
                ir.setApprovalStatus(
                        approvedReceiverIds.contains(ir.getId()) ? IntygReceiver.ApprovalStatus.YES : IntygReceiver.ApprovalStatus.NO);
                ir.setLocked(CertificateReceiverTypeType.HUVUDMOTTAGARE.name().equalsIgnoreCase(ir.getReceiverType()));
            }
            return intygReceivers;
        } catch (WebServiceException wse) {
            LOG.warn("Caught WebServiceException fetching approved or possible receivers, only returning Huvudmottagare.");
            try {
                ModuleEntryPoint moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(intygsTyp);
                String recipientId = moduleEntryPoint.getDefaultRecipient();
                IntygReceiver intygReceiver = IntygReceiver.IntygReceiverBuilder.anIntygReceiver()
                        .withId(recipientId)
                        .withReceiverType(CertificateReceiverTypeType.HUVUDMOTTAGARE.name())
                        .withApprovalStatus(IntygReceiver.ApprovalStatus.YES)
                        .withLocked(true)
                        .withName(recipientId)
                        .build();
                return Arrays.asList(intygReceiver);
            } catch (Exception e) {
                LOG.error("Unable to resolve default recipient using ModuleEntryPoint for '{}', throwing exception.", intygsTyp);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e);
            }
        }
    }

    private List<IntygReceiver> listPossibleReceivers(String intygsTyp) {

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
                .withApprovalStatus(rcpt.getReceiverType() == CertificateReceiverTypeType.HUVUDMOTTAGARE ? IntygReceiver.ApprovalStatus.YES
                        : IntygReceiver.ApprovalStatus.UNDEFINED)
                .withLocked(rcpt.getReceiverType() == CertificateReceiverTypeType.HUVUDMOTTAGARE)
                .build())
                .collect(Collectors.toList());
    }
}
