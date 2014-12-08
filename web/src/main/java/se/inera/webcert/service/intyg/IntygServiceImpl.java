package se.inera.webcert.service.intyg;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.SendIntygConfiguration;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.intyg.dto.IntygRecipient;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService, IntygOmsandningService {

    public enum Event {
        REGISTER, SEND, REVOKE;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private GetCertificateForCareResponderInterface getCertificateService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private RegisterCertificateResponderInterface intygSender;

    @Autowired
    private GetRecipientsForCertificateResponderInterface getRecipientsForCertificateService;

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private SendMedicalCertificateResponderInterface sendService;

    @Autowired
    private OmsandningRepository omsandningRepository;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygModuleFacade modelFacade;

    @Autowired
    private IntygServiceConverter serviceConverter;

    @Autowired
    private IntygServiceConfigurationManager configurationManager;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;


    @Override
    public IntygContentHolder fetchIntygData(String intygId) {
        try {
            IntygContentHolder intygAsExternal = getIntygAsExternalModel(intygId);

            IntygMetadata metaData = intygAsExternal.getMetaData();

            String internalIntygJsonModel = modelFacade.convertFromExternalToInternal(metaData.getType(), intygAsExternal.getContents());

            return new IntygContentHolder(internalIntygJsonModel, metaData);

        } catch (IntygModuleFacadeException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public IntygContentHolder fetchExternalIntygData(String intygId) {

        IntygContentHolder intygAsExternal = getIntygAsExternalModel(intygId);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromExternalModel(intygAsExternal.getExternalModel());
        logService.logReadOfIntyg(logRequest);

        return intygAsExternal;
    }

    private IntygContentHolder getIntygAsExternalModel(String intygId) {
        try {

            GetCertificateForCareResponseType intygResponse = fetchIntygFromIntygstjanst(intygId);

            verifyEnhetsAuth(intygResponse.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension(), true);

            String patientId = intygResponse.getCertificate().getPatient().getPersonId().getExtension();
            IntygMetadata metaData = serviceConverter.convertToIntygMetadata(patientId, intygResponse.getMeta());

            ExternalModelResponse intygAsExternal = modelFacade.convertFromTransportToExternal(metaData.getType(), intygResponse.getCertificate());

            return new IntygContentHolder(intygAsExternal.getExternalModelJson(),
                    intygAsExternal.getExternalModel(), metaData);

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }

    }

    @Override
    public List<IntygItem> listIntyg(List<String> enhetId, String personnummer) {
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setNationalIdentityNumber(personnummer);
        request.getCareUnit().addAll(enhetId);

        ListCertificatesForCareResponseType response = listCertificateService.listCertificatesForCare(logicalAddress,
                request);

        switch (response.getResult().getResultCode()) {
        case OK:
            return serviceConverter.convertToListOfIntygItem(response.getMeta());
        default:
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                    "listCertificatesForCare WS call: ERROR :" + response.getResult().getResultText());
        }
    }

    public List<IntygRecipient> fetchListOfRecipientsForIntyg(String intygType) {

        intygType = intygType.toLowerCase();

        LOG.debug("Fetching recipients for intyg type '{}'", intygType);

        List<IntygRecipient> recipientsList = new ArrayList<IntygRecipient>();

        GetRecipientsForCertificateType request = new GetRecipientsForCertificateType();
        request.setCertificateType(intygType);

        GetRecipientsForCertificateResponseType response = getRecipientsForCertificateService.getRecipientsForCertificate(logicalAddress, request);

        ResultType resultType = response.getResult();

        if (resultType.getResultCode() != ResultCodeType.OK) {
            LOG.error("Retrieving list of recipients for type '{}' failed with error id; {}, msg; {}", new Object[] {
                    intygType, resultType.getErrorId(), resultType.getResultText() });
            return recipientsList;
        }

        for (RecipientType recipientType : response.getRecipient()) {
            recipientsList.add(new IntygRecipient(recipientType.getId(), recipientType.getName()));
        }

        return recipientsList;
    }

    public IntygPdf fetchIntygAsPdf(String intygId) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygId);

            IntygContentHolder intygAsExternal = getIntygAsExternalModel(intygId);

            String intygType = intygAsExternal.getMetaData().getType();

            IntygPdf intygPdf = modelFacade.convertFromExternalToPdfDocument(intygType, intygAsExternal.getContents());

            LogRequest logRequest = LogRequestFactory.createLogRequestFromExternalModel(intygAsExternal.getExternalModel());
            logService.logPrintOfIntygAsPDF(logRequest);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private GetCertificateForCareResponseType fetchIntygFromIntygstjanst(String intygsId) {
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(intygsId);

        GetCertificateForCareResponseType response = getCertificateService.getCertificateForCare(logicalAddress,
                request);

        switch (response.getResult().getResultCode()) {
        case INFO:
        case OK:
            return response;
        case ERROR:
            switch (response.getResult().getErrorId()) {
            case REVOKED:
                return response;
            case VALIDATION_ERROR:
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                        "getCertificateForCare WS call:  VALIDATION_ERROR :" + response.getResult().getResultText());
            default:
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                        "getCertificateForCare WS call: ERROR :" + response.getResult().getResultText());
            }
        default:
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                    "getCertificateForCare WS call: ERROR :" + response.getResult().getResultText());
        }
    }

    private void checkIfCertificateIsRevoked(GetCertificateForCareResponseType response) {
        if (ResultCodeType.ERROR.equals(response.getResult().getResultCode())) {
            if (ErrorIdType.REVOKED.equals(response.getResult().getErrorId())) {
                LOG.info("Certificate is revoked");
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is revoked");
            }
        }
    }

    protected void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            LOG.info("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }
    }

    @Override
    public IntygServiceResult storeIntyg(Intyg intyg) {
        Omsandning omsandning = createOmsandning(OmsandningOperation.STORE_INTYG, intyg.getIntygsId(), null);
        // Redan schedulerat för att skickas, men vi gör ett försök redan nu.
        return storeIntyg(intyg, omsandning);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Omsandning createOmsandning(OmsandningOperation operation, String intygId, String configuration) {
        Omsandning omsandning = new Omsandning(operation, intygId);
        omsandning.setAntalForsok(0);
        omsandning.setGallringsdatum(new LocalDateTime().plusHours(24 * 7));
        omsandning.setNastaForsok(new LocalDateTime().plusHours(1));

        if (configuration != null) {
            omsandning.setConfiguration(configuration);
        }

        LOG.debug("Creating Omsandning with operation {} for intyg {}", operation, intygId);

        return omsandningRepository.save(omsandning);
    }

    public IntygServiceResult storeIntyg(Omsandning omsandning) {
        return storeIntyg(intygRepository.findOne(omsandning.getIntygId()), omsandning);
    }

    public IntygServiceResult storeIntyg(Intyg intyg, Omsandning omsandning) {
        try {

            if (!registerIntyg(intyg)) {
                scheduleResend(omsandning);
                return IntygServiceResult.RESCHEDULED;
            }

            omsandningRepository.delete(omsandning);
            return IntygServiceResult.OK;

        } catch (IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to register intyg " + intyg.getIntygsId(), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private boolean registerIntyg(Intyg intyg) throws IntygModuleFacadeException {

        LOG.info("Attempting to register intyg {}", intyg.getIntygsId());
        UtlatandeType utlatandeType = modelFacade.convertFromInternalToTransport(intyg.getIntygsTyp(), intyg.getModel());

        utlatandeType.setSigneringsdatum(utlatandeType.getSigneringsdatum());
        utlatandeType.setSkickatdatum(new LocalDateTime());

        RegisterCertificateType registerCertRequest = new RegisterCertificateType();
        registerCertRequest.setUtlatande(utlatandeType);

        ResultType result;

        try {
            RegisterCertificateResponseType registerCertificateResponseType = intygSender.registerCertificate(logicalAddress,
                    registerCertRequest);
            result = registerCertificateResponseType.getResult();
        } catch (WebServiceException wse) {
            LOG.error("A WebServiceException occured when trying to register intyg " + intyg.getIntygsId(), wse);
            return false;
        }

        switch (result.getResultCode()) {
        case OK:
            LOG.debug("Successfully resgistered intyg {}", intyg.getIntygsId());
            return true;
        case ERROR:
            LOG.error("Call to register intyg {} returned an ERROR; {}, error id; {}", new Object[] { intyg.getIntygsId(), result.getResultText(),
                    result.getErrorId() });
            return false;
        case INFO:
            LOG.warn("Call to register intyg {} returned an INFO; {}", intyg.getIntygsId(), result.getResultText());
            return true;
        default:
            return false;
        }
    }

    private void scheduleResend(Omsandning omsandning) {
        omsandning.setNastaForsok(new LocalDateTime().plusHours(1));
        omsandning.setAntalForsok(omsandning.getAntalForsok() + 1);
        omsandningRepository.save(omsandning);
        LOG.info("Rescheduled {}", omsandning.toString());
    }

    public IntygServiceResult sendIntyg(Omsandning omsandning) {
        SendIntygConfiguration sendConfig = configurationManager.unmarshallConfig(omsandning.getConfiguration(), SendIntygConfiguration.class);

        GetCertificateForCareResponseType intygResponse = fetchIntygFromIntygstjanst(omsandning.getIntygId());
        UtlatandeType utlatandeType = intygResponse.getCertificate();

        return sendIntyg(omsandning, sendConfig, utlatandeType);
    }

    public IntygServiceResult sendIntyg(String intygsId, String recipient, boolean hasPatientConsent) {

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, hasPatientConsent);
        String sendConfigAsJson = configurationManager.marshallConfig(sendConfig);

        Omsandning omsandning = createOmsandning(OmsandningOperation.SEND_INTYG, intygsId, sendConfigAsJson);

        GetCertificateForCareResponseType intygResponse = fetchIntygFromIntygstjanst(intygsId);
        UtlatandeType utlatandeType = intygResponse.getCertificate();

        verifyEnhetsAuth(utlatandeType.getSkapadAv().getEnhet().getEnhetsId().getExtension(), false);

        return sendIntyg(omsandning, sendConfig, utlatandeType);
    }

    public IntygServiceResult sendIntyg(Omsandning omsandning, SendIntygConfiguration sendConfig, UtlatandeType utlatandeType) {

        String intygsId = omsandning.getIntygId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = utlatandeType.getTypAvUtlatande().getCode();

        try {
            LOG.info("Sending intyg {} of type {} to recipient {}", new Object[] { intygsId, intygsTyp, recipient });

            ExternalModelResponse intygAsExternal = modelFacade.convertFromTransportToExternal(intygsTyp, utlatandeType);
            Utlatande utlatande = intygAsExternal.getExternalModel();

            if (!performSendIntyg(utlatande, recipient)) {
                LOG.info("Sending intyg {} to recipient {} failed, rescheduling send...", intygsId, recipient);
                scheduleResend(omsandning);
                return IntygServiceResult.RESCHEDULED;
            }

            omsandningRepository.delete(omsandning);

            // Send PDL log event
            LogRequest logRequest = LogRequestFactory.createLogRequestFromExternalModel(utlatande);
            logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
            logService.logSendIntygToRecipient(logRequest);

            // Notify stakeholders when a certificate is sent
            notify(intygsId, Event.SEND);

            return IntygServiceResult.OK;

        } catch (WebServiceException wse) {
            LOG.error("An WebServiceException occured when trying to fetch and send intyg: " + intygsId, wse);
            scheduleResend(omsandning);
            return IntygServiceResult.RESCHEDULED;
        } catch (WebCertServiceException wcse) {
            // removing omsandning since exception is thrown when fetching
            LOG.error("WebCertServiceException occured when trying to revoke intyg: " + intygsId, wcse);
            omsandningRepository.delete(omsandning);
            throw wcse;
        } catch (IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intygsId, e);
            omsandningRepository.delete(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private boolean performSendIntyg(Utlatande utlatande, String recipient) {

        String intygsId = serviceConverter.extractUtlatandeId(utlatande);

        String intygsTyp = utlatande.getTyp().getCode();

        LOG.debug("Attempting to send intyg {} of type {} to recipient {}", new Object[] { intygsId,
                intygsTyp, recipient });

        SendType sendType = serviceConverter.buildSendTypeFromUtlatande(utlatande);

        SendMedicalCertificateRequestType request = new SendMedicalCertificateRequestType();
        request.setSend(sendType);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(recipient);

        SendMedicalCertificateResponseType response;

        try {
            response = sendService.sendMedicalCertificate(uri, request);
        } catch (WebServiceException wse) {
            LOG.error("A WebServiceException occured when trying to send intyg " + intygsId, wse);
            return false;
        }

        ResultOfCall resultOfCall = response.getResult();

        switch (resultOfCall.getResultCode()) {
        case OK:
            LOG.debug("Successfully sent intyg {} of type {} to recipient {}", new Object[] { intygsId, intygsTyp, recipient });
            return true;
        case INFO:
            LOG.warn("Call to send intyg {} returned an info message: {}", intygsId, resultOfCall.getInfoText());
            return true;
        case ERROR:
            LOG.error("Call to send intyg {} caused an error: {}, ErrorId: {}", new Object[] { intygsId, resultOfCall.getErrorText(),
                    resultOfCall.getErrorId() });
            return false;
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.webcert.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    public IntygServiceResult revokeIntyg(String intygsId, String revokeMessage) {
        try {

            LOG.info("Attempting to revoke intyg {}", intygsId);

            GetCertificateForCareResponseType intygResponse = fetchIntygFromIntygstjanst(intygsId);

            checkIfCertificateIsRevoked(intygResponse);

            verifyEnhetsAuth(intygResponse.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension(), false);

            UtlatandeType utlatandeType = intygResponse.getCertificate();
            String intygsTyp = utlatandeType.getTypAvUtlatande().getCode();

            ExternalModelResponse intygAsExternal = modelFacade.convertFromTransportToExternal(intygsTyp, utlatandeType);

            RevokeType revokeType = serviceConverter.buildRevokeTypeFromUtlatande(intygAsExternal.getExternalModel(), revokeMessage);

            RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
            request.setRevoke(revokeType);

            AttributedURIType uri = new AttributedURIType();
            uri.setValue(logicalAddress);

            // Revoke certificate
            RevokeMedicalCertificateResponseType response = revokeService.revokeMedicalCertificate(uri, request);

            // Notify stakeholders when a certificate is revoked
            notify(intygsId, Event.REVOKE);

            // Setup return statement
            ResultOfCall resultOfCall = response.getResult();

            switch (resultOfCall.getResultCode()) {
            case OK:
                LOG.info("Successfully revoked intyg {}", intygsId);
                return IntygServiceResult.OK;
            case INFO:
                LOG.warn("Call to revoke intyg {} returned an info message: {}", intygsId, resultOfCall.getInfoText());
                return IntygServiceResult.OK;
            case ERROR:
                LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}", new Object[] { intygsId, resultOfCall.getErrorText(),
                        resultOfCall.getErrorId() });
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, resultOfCall.getErrorText());
            default:
                return IntygServiceResult.FAILED;
            }

        } catch (IntygModuleFacadeException imfe) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, imfe);
        }
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    private void notify(String intygId, Event event) {
        Intyg intyg = lookupIntyg(intygId);
        notify(intyg, event);
    }

    private void notify(Intyg intyg, Event event) {
        NotificationRequestType notificationRequestType = null;

        switch (event) {
        case REVOKE:
            notificationRequestType = NotificationMessageFactory.createNotificationFromRevokedCertificate(intyg);
            break;
        case SEND:
            notificationRequestType = NotificationMessageFactory.createNotificationFromSentCertificate(intyg);
        }

        notificationService.notify(notificationRequestType);
    }

    private Intyg lookupIntyg(String intygId) {
        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Intyg could not be found");
        }

        return intyg;
    }

}
