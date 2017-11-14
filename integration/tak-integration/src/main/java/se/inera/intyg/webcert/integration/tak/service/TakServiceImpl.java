/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.tak.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumer;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@EnableScheduling
public class TakServiceImpl implements TakService {
    private static final Logger LOG = LoggerFactory.getLogger(TakServiceImpl.class);

    private static final String CERT_STATUS_FOR_CARE_V1_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:1";
    private static final String CERT_STATUS_FOR_CARE_V3_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";
    private static final String RECEIVE_MEDICAL_CERT_QUESTION_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1";
    private static final String RECEIVE_MEDICAL_CERT_ANSWER_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1";
    private static final String SEND_MESSAGE_TO_CARE_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCare:2";

    private static final String ERROR_STRING = "Den angivna enheten går ej att adressera för ärendekommunikation."
            + " (Tjänsten %s är inte registrerad för enhet %s i tjänsteadresseringskatalogen.";

    private String ntjpId;
    private String certificateStatusUpdateForCareV1Id;
    private String certificateStatusUpdateForCareV3Id;
    private String receiveMedicalCertificateQuestionId;
    private String receiveMedicalCertificateAnswerId;
    private String sendMessageToCareId;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Autowired
    private TakConsumer consumer;

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Value("${tak.timeout}")
    private int timeout;

    public void init() {
        update();
    }

    @Scheduled(cron = "${tak.update.cron}")
    public void update() {
        ntjpId = consumer.getConnectionPointId();
        certificateStatusUpdateForCareV1Id = consumer.getServiceContractId(CERT_STATUS_FOR_CARE_V1_NS);
        certificateStatusUpdateForCareV3Id = consumer.getServiceContractId(CERT_STATUS_FOR_CARE_V3_NS);
        receiveMedicalCertificateQuestionId = consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_QUESTION_NS);
        receiveMedicalCertificateAnswerId = consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_ANSWER_NS);
        sendMessageToCareId = consumer.getServiceContractId(SEND_MESSAGE_TO_CARE_NS);

        LOG.info("Updated IDs via TAK-rest-api. Ntjp-id: {}, statusUpdateForCareV1: {}, statusUpdateForCareV3: {} "
                        + "receiveQuestion: {}, receiveAnswer: {}, sendMsgToCare: {}",
                ntjpId, certificateStatusUpdateForCareV1Id, certificateStatusUpdateForCareV3Id,
                receiveMedicalCertificateQuestionId, receiveMedicalCertificateAnswerId, sendMessageToCareId);
    }

    @Override
    public TakResult verifyTakningForCareUnit(String careUnitId, String intygsTyp, String schemaVersion,
                                              IntygUser user) {

        String certStatusUpdateId = SchemaVersion.VERSION_1.getVersion().equalsIgnoreCase(schemaVersion) ? certificateStatusUpdateForCareV1Id
                : certificateStatusUpdateForCareV3Id;
        String certStatusUpdateNs = SchemaVersion.VERSION_3.getVersion().equalsIgnoreCase(schemaVersion) ? CERT_STATUS_FOR_CARE_V1_NS
                : CERT_STATUS_FOR_CARE_V3_NS;

        List<String> errors = new ArrayList<>();
        boolean ret;

        // Snugly wrapped to implement a configurable timeout for the entire TAK-process
        try {
            ret = CompletableFuture.supplyAsync(() -> {
                try {
                    InternalResult initialResult = isTakForCertificateStatusUpdateForCare(careUnitId,
                            certStatusUpdateId, certStatusUpdateNs);
                    final boolean isTakad = initialResult.getResult().length > 0;
                    final String actualHsaId = initialResult.getHsaId();

                    if (!isTakad && initialResult.getError() != null) {
                        errors.add(initialResult.getError());
                    }
                    // Check user and intygstyp for arendekommunikation
                    return (isTakad && isTakForArendekommunikation(intygsTyp, user, errors, actualHsaId));

                } catch (ResourceAccessException  e) {
                    // This handles timeouts from the actual REST-calls in TakConsumer, log and allow creation.
                    LOG.warn("Connection to TAK-api timed out, draft creation allowed anyway.");
                    return true;
                } catch (HsaServiceCallException he) {
                    LOG.error("Internal application error while looking up careUnit in TakService: {}",
                            he.getMessage());
                    return false;
                }
            }).get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If the time taken for entirety of this check exceeds the timeout, log a warning and allow creation.
            LOG.warn("The overall timeout for this call was reached, draft creation allowed anyway.");
            ret = true;
        } catch (InterruptedException | ExecutionException ee) {
            LOG.error("Internal application error in TakService: {}", ee.getMessage());
            ret = false;
        }
        return new TakResult(ret, errors);
    }

    private boolean isTakForArendekommunikation(String intygsTyp, IntygUser user, List<String> errors,
                                                String actualHsaId) {
        // Utilize authoritiesValidator to check arendehantering
        if (authoritiesValidator.given(user, intygsTyp).features(WebcertFeature.HANTERA_FRAGOR).isVerified()) {
            if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygsTyp)) {
                // yes? -> fk7263
                if (consumer.doLookup(ntjpId, actualHsaId, receiveMedicalCertificateAnswerId).length < 1) {
                    errors.add(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_ANSWER_NS, actualHsaId));
                    return false;
                }
                if (consumer.doLookup(ntjpId, actualHsaId, receiveMedicalCertificateQuestionId).length < 1) {
                    errors.add(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_QUESTION_NS, actualHsaId));
                    return false;
                }
            } else {
                // yes? -> other
                if (consumer.doLookup(ntjpId, actualHsaId, sendMessageToCareId).length < 1) {
                    errors.add(String.format(ERROR_STRING, SEND_MESSAGE_TO_CARE_NS, actualHsaId));
                    return false;
                }
            }
        }
        return true;
    }

    private InternalResult isTakForCertificateStatusUpdateForCare(String careUnitId, String certStatusUpdateId,
            String certStatusUpdateNs) throws HsaServiceCallException {
        InternalResult response = lookupCareUnitAndParent(careUnitId, certStatusUpdateId);
        boolean retried = false;

        while (response.getResult().length < 1  && !retried) {
            update();
            lookupCareUnitAndParent(careUnitId, certStatusUpdateId);
            retried = true;
        }

        if (response.getResult().length < 1) {
            LOG.error("CareUnit not addressable in TAK-register for the given service.");
            response.setError(String.format(ERROR_STRING, certStatusUpdateNs, careUnitId));
            return response;
        } else {
            LOG.info("TAK - id: {}, description: {}, logicalAddress: {}", response.getResult()[0].getId(),
                    response.getResult()[0].getDescription(), response.getResult()[0].getLogicalAddress());
            return response;
        }
    }

    private InternalResult lookupCareUnitAndParent(String careUnitId, String contract) throws HsaServiceCallException {
        TakLogicalAddress[] response = consumer.doLookup(ntjpId, careUnitId, contract);
        if (response.length > 0) {
            return new InternalResult(careUnitId, response);
        } else {
            String parentId = hsaOrganizationsService.getParentUnit(careUnitId);
            LOG.info("Got nothing while checking TAK-status, trying with parent unit {}", parentId);
            return new InternalResult(parentId, consumer.doLookup(ntjpId, parentId, contract));
        }
    }

    private static class InternalResult {
        private final String hsaId;
        private final TakLogicalAddress[] result;
        private String error;

        InternalResult(String hsaId, TakLogicalAddress[] result) {
            this(hsaId, result, null);
        }

        InternalResult(String hsaId, TakLogicalAddress[] result, String error) {
            this.hsaId = hsaId;
            this.result = result;
            this.error = error;
        }

        String getHsaId() {
            return this.hsaId;
        }
        TakLogicalAddress[] getResult() {
            return this.result;
        }
        String getError() {
            return this.error;
        }

        void setError(String error) {
            this.error = error;
        }
    }
}
