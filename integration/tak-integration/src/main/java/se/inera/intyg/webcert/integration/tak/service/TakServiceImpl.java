/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumer;
import se.inera.intyg.webcert.integration.tak.consumer.TakServiceException;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion.VERSION_3;

@Service
@EnableScheduling
public class TakServiceImpl implements TakService {
    private static final Logger LOG = LoggerFactory.getLogger(TakServiceImpl.class);

    private static final String CERT_STATUS_FOR_CARE_V3_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";
    private static final String RECEIVE_MEDICAL_CERT_QUESTION_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1";
    private static final String RECEIVE_MEDICAL_CERT_ANSWER_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1";
    private static final String SEND_MESSAGE_TO_CARE_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2";

    private static final String ERROR_STRING_BASE = "Tjänsten %s är inte registrerad för enhet %s i tjänsteadresseringskatalogen.";

    private static final String ERROR_STRING_ARENDEHANTERING = "Den angivna enheten går ej att adressera för ärendekommunikation. "
            + ERROR_STRING_BASE;

    private String ntjpId;
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

    @Scheduled(cron = "${tak.update.cron}")
    public void initUpdate() {
        try {
            update();
        } catch (TakServiceException e) {
            LOG.error("Update failed, TAK-service  returned null values", e);
        }
    }

    public void update() throws TakServiceException {
        ntjpId = consumer.getConnectionPointId();
        certificateStatusUpdateForCareV3Id = consumer.getServiceContractId(CERT_STATUS_FOR_CARE_V3_NS);
        receiveMedicalCertificateQuestionId = consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_QUESTION_NS);
        receiveMedicalCertificateAnswerId = consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_ANSWER_NS);
        sendMessageToCareId = consumer.getServiceContractId(SEND_MESSAGE_TO_CARE_NS);

        LOG.info("Updated IDs via TAK-rest-api. Ntjp-id: {}, statusUpdateForCareV3: {} "
                        + "receiveQuestion: {}, receiveAnswer: {}, sendMsgToCare: {}",
                ntjpId, certificateStatusUpdateForCareV3Id, receiveMedicalCertificateQuestionId,
                receiveMedicalCertificateAnswerId, sendMessageToCareId);

    }

    @Override
    public TakResult verifyTakningForCareUnit(String hsaId, String intygsTyp, SchemaVersion schemaVersion, IntygUser user) {
        LOG.info("Checking TAK-status for {}", intygsTyp);
        List<String> errors = new ArrayList<>();
        boolean ret;
        try {
            ret = CompletableFuture.supplyAsync(() -> {
                /*
                 * We do not know when NTJP updates their ids of schemas, consumers and producers.
                 * Hence we need to make sure we have the latest configuration before rejecting the request.
                 */
                boolean res = isTakConfiguredCorrectly(intygsTyp, user, errors, hsaId, schemaVersion);
                if (!res) {
                    update();
                    errors.clear();
                    res = isTakConfiguredCorrectly(intygsTyp, user, errors, hsaId, schemaVersion);
                }
                return res;
            }).get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If the time taken for entirety of this check exceeds the timeout, log a warning and allow creation.
            LOG.warn("The overall timeout for this call was reached, draft creation allowed anyway.");
            ret = true;
        } catch (TakServiceException e) {
            LOG.error("Contacting TAK was unsuccessful", e);
            ret = true;
        } catch (ResourceAccessException e) {
            // This handles timeouts from the actual REST-calls in TakConsumer, log and allow creation.
            LOG.warn("Connection to TAK-api timed out, draft creation allowed anyway.");
            ret = true;
        } catch (Exception e) {
            LOG.warn("Unknown exception occured when communicating with TAK. Draft creation allowed anyway.", e);
            ret = true;
        }
        return new TakResult(ret, errors);
    }

    private boolean isTakConfiguredCorrectly(String intygsTyp, IntygUser user, List<String> errors, String hsaId, SchemaVersion version) {
        List<String> hsaIds = new ArrayList<>();
        hsaIds.add(hsaId);

        if (authoritiesValidator.given(user).features(AuthoritiesConstants.FEATURE_TAK_KONTROLL_TRADKLATTRING).isVerified()) {
            // Vardenhet (the unit over mottagning - if existing)
            String careUnitId;
            try {
                careUnitId = hsaOrganizationsService.getParentUnit(hsaId);
            } catch (HsaServiceCallException e) {
                LOG.warn("Could not reach HSA to get HealthCareUnitId client will need to try again.");
                return false;
            }
            if (!Objects.equals(hsaId, careUnitId)) {
                hsaIds.add(careUnitId);
                LOG.debug("Adding parentUnit {} to list of hsaIds for unit {}.", careUnitId, hsaId);
            }

            // Caregiver
            String careGiverId = hsaOrganizationsService.getVardgivareOfVardenhet(hsaId);
            if (careGiverId != null) {
                hsaIds.add(careGiverId);
                LOG.debug("Adding careGiverId {} to list of hsaIds for unit {}.", careGiverId, hsaId);
            }

        }
        return checkConfiguration(intygsTyp, user, errors, hsaIds, version);
    }

    private boolean checkConfiguration(String intygsTyp, IntygUser user, List<String> errors, List<String> hsaIds, SchemaVersion version) {
        LOG.debug("Checking configuration for {}", intygsTyp);
        if (hsaIds.stream().noneMatch(hsaId -> isValid(consumer.doLookup(ntjpId, hsaId, resolveContract(version))))) {
            errors.add(String.format(ERROR_STRING_BASE, CERT_STATUS_FOR_CARE_V3_NS, hsaIds.get(0)));
            return false;
        }

        // Utilize authoritiesValidator to check arendehantering
        if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).isVerified()) {
            if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygsTyp)) {
                // yes? -> fk7263
                if (hsaIds.stream().noneMatch(hsaId -> isValid(consumer.doLookup(ntjpId, hsaId, receiveMedicalCertificateAnswerId)))) {
                    errors.add(String.format(ERROR_STRING_ARENDEHANTERING, RECEIVE_MEDICAL_CERT_ANSWER_NS, hsaIds.get(0)));
                    return false;
                }
                if (hsaIds.stream().noneMatch(hsaId -> isValid(consumer.doLookup(ntjpId, hsaId, receiveMedicalCertificateQuestionId)))) {
                    errors.add(String.format(ERROR_STRING_ARENDEHANTERING, RECEIVE_MEDICAL_CERT_QUESTION_NS, hsaIds.get(0)));
                    return false;
                }
            } else {
                // yes? -> other
                if (hsaIds.stream().noneMatch(hsaId -> isValid(consumer.doLookup(ntjpId, hsaId, sendMessageToCareId)))) {
                    errors.add(String.format(ERROR_STRING_ARENDEHANTERING, SEND_MESSAGE_TO_CARE_NS, hsaIds.get(0)));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(TakLogicalAddress[] takLogicalAddresses) {
        return takLogicalAddresses != null && takLogicalAddresses.length > 0;
    }

    private String resolveContract(SchemaVersion version) {
        if (version.equals(VERSION_3)) {
            return certificateStatusUpdateForCareV3Id;
        }
        return "NO_SCHEMA_VERSION_AVAILABLE";
    }
}
