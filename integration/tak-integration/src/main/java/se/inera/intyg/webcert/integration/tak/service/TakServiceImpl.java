package se.inera.intyg.webcert.integration.tak.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumer;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class TakServiceImpl implements TakService {
    private static final Logger LOG = LoggerFactory.getLogger(TakServiceImpl.class);

    private final String CERT_STATUS_FOR_CARE_V1_NS = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:1";
    private final String CERT_STATUS_FOR_CARE_V3_NS = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";
    private final String RECEIVE_MEDICAL_CERT_QUESTION_NS = "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1";
    private final String RECEIVE_MEDICAL_CERT_ANSWER_NS = "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1";
    private final String SEND_MESSAGE_TO_CARE_NS = "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCare:2";

    private final String ERROR_STRING = "Den angivna enheten går ej att adressera för ärendekommunikation." +
            " (Tjänsten %s är inte registrerad för enhet %s i tjänsteadresseringskatalogen.";

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

        LOG.info("Updated IDs via TAK-rest-api. Ntjp-id: {}, statusUpdateForCareV1: {}, statusUpdateForCareV3: {} " +
                        "receiveQuestion: {}, receiveAnswer: {}, sendMsgToCare: {}",
                ntjpId, certificateStatusUpdateForCareV1Id, certificateStatusUpdateForCareV3Id,
                receiveMedicalCertificateQuestionId, receiveMedicalCertificateAnswerId, sendMessageToCareId);
    }

    @Override
    public TakResult verifyTakningForCareUnit(String careUnitId, String intygsTyp, String schemaVersion, IntygUser user) {
        String certStatusUpdateId = schemaVersion.equals("V1") ? certificateStatusUpdateForCareV1Id :
                certificateStatusUpdateForCareV3Id;
        String certStatusUpdateNs = schemaVersion.equals("V1") ? CERT_STATUS_FOR_CARE_V1_NS : CERT_STATUS_FOR_CARE_V3_NS;

        boolean isTakad;

        List<String> errors = new ArrayList<>();

        String actualHsaId;

        try {
            InternalResult initialResult = isTakForCertificateStatusUpdateForCare(careUnitId, certStatusUpdateId, certStatusUpdateNs);
            isTakad = initialResult.getResult();
            actualHsaId = initialResult.getHsaId();

            if (initialResult.getError() != null) {
                errors.add(initialResult.getError());
            }

            // Check user and intygstyp for arendekommunikation
            if (authoritiesValidator.given(user, intygsTyp).features(WebcertFeature.HANTERA_FRAGOR).isVerified()) {
                if (intygsTyp.equalsIgnoreCase(Fk7263EntryPoint.MODULE_ID)) {
                    // yes? -> fk7263
                    if (consumer.doLookup(ntjpId, actualHsaId, receiveMedicalCertificateAnswerId).length < 1) {
                        isTakad = false;
                        errors.add(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_ANSWER_NS, actualHsaId));
                    }
                    if (consumer.doLookup(ntjpId, actualHsaId, receiveMedicalCertificateQuestionId).length < 1) {
                        isTakad = false;
                        errors.add(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_QUESTION_NS, actualHsaId));
                    }
                } else {
                    // yes? -> other
                    if (consumer.doLookup(ntjpId, actualHsaId, sendMessageToCareId).length < 1) {
                        isTakad = false;
                        errors.add(String.format(ERROR_STRING, SEND_MESSAGE_TO_CARE_NS, actualHsaId));
                    }
                }
            }
        } catch (ResourceAccessException e) {
            // If the Api is not responding or request timed out, log av warning and allow creation anyway.
            LOG.warn("Connection to TAK-api timed out, draft creation allowed anyway.");
            isTakad = true;
        } catch (HsaServiceCallException he) {
            LOG.error("Internal application error while looking up careUnit in TakService");
            isTakad = false;
        }
        return new TakResult(isTakad, errors);
    }

    private InternalResult isTakForCertificateStatusUpdateForCare(String careUnitId, String certStatusUpdateId,
                                                                  String certStatusUpdateNs) throws HsaServiceCallException {
        TakLogicalAddress[] response = lookupCareUnitAndParent(careUnitId, certStatusUpdateId);
        boolean retried = false;
        while(response.length < 1 && !retried) {
            update();
            lookupCareUnitAndParent(careUnitId, certStatusUpdateId);
            retried = true;
        }
        if (response.length < 1) {
            LOG.error("CareUnit not addressable in TAK-register for the given service.");
            return new InternalResult(careUnitId, false,
                    String.format(ERROR_STRING, certStatusUpdateNs, careUnitId));
        } else {
            LOG.info("TAK - id: {}, description: {}, logicalAddress: {}", response[0].getId(),
                    response[0].getDescription(), response[0].getLogicalAddress());
            return new InternalResult(careUnitId, true);
        }
    }

    private TakLogicalAddress[] lookupCareUnitAndParent(String careUnitId, String contract) throws HsaServiceCallException {
        TakLogicalAddress[] response = consumer.doLookup(ntjpId, careUnitId, contract);
        if (response.length > 0) {
            return response;
        } else {
            LOG.info("Got nothing while checking TAK-status, trying with parent unit");
            Vardenhet parent = hsaOrganizationsService.getParentUnit(careUnitId);
            return consumer.doLookup(ntjpId, parent.getVardgivareHsaId(), contract);
        }
    }

    private class InternalResult {
        private final String hsaId;
        private final boolean result;
        private final String error;

        InternalResult(String hsaId, boolean result) {
            this(hsaId, result, null);
        }

        InternalResult(String hsaId, boolean result, String error) {
            this.hsaId = hsaId;
            this.result = result;
            this.error = error;
        }

        String getHsaId() {
            return this.hsaId;
        }
        boolean getResult() {
            return this.result;
        }
        String getError() {
            return this.error;
        }
    }
}
