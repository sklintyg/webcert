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

import se.inera.certificate.clinicalprocess.healthcond.certificate.getmedicalcertificateforcare.v1.GetMedicalCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.util.ModelConverter;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.OmsandningOperation;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepository;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.SendIntygConfiguration;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.intyg.dto.IntygRecipient;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService, IntygOmsandningService {

    public enum Event {
        REVOKE, SEND;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private GetMedicalCertificateForCareResponderInterface getCertificateService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private GetRecipientsForCertificateResponderInterface getRecipientsForCertificateService;

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private SendMedicalCertificateResponderInterface sendService;

    @Autowired
    private OmsandningRepository omsandningRepository;

    @Autowired
    private UtkastRepository utkastRepository;

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

    @Autowired
    private FragaSvarService fragaSvarService;

    /* --------------------- Public scope --------------------- */

    @Override
    public IntygContentHolder fetchIntygData(String intygId, String typ) {
        IntygContentHolder intygData = getIntygData(intygId, typ);
        verifyEnhetsAuth(intygData.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid(), true);
        return intygData;
    }

    @Override
    public List<IntygItem> listIntyg(List<String> enhetId, String personnummer) {
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId(personnummer);
        request.getEnhet().addAll(enhetId);

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

    public List<IntygRecipient> fetchListOfRecipientsForIntyg(String intygTyp) {

        intygTyp = intygTyp.toLowerCase();

        LOG.debug("Fetching recipients for certificate of type '{}'", intygTyp);

        List<IntygRecipient> recipientsList = new ArrayList<IntygRecipient>();

        GetRecipientsForCertificateType request = new GetRecipientsForCertificateType();
        request.setCertificateType(intygTyp);

        GetRecipientsForCertificateResponseType response = getRecipientsForCertificateService.getRecipientsForCertificate(logicalAddress, request);

        ResultType resultType = response.getResult();

        if (resultType.getResultCode() != ResultCodeType.OK) {
            LOG.error("Retrieving list of recipients for certificate type '{}' failed with error id; {}, msg; {}",
                    new Object[] { intygTyp, resultType.getErrorId(), resultType.getResultText() });
            return recipientsList;
        }

        for (RecipientType recipientType : response.getRecipient()) {
            recipientsList.add(new IntygRecipient(recipientType.getId(), recipientType.getName(), intygTyp));
        }

        return recipientsList;
    }

    @Override
    public IntygPdf fetchIntygAsPdf(String intygId, String intygTyp) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygId);

            IntygContentHolder intyg = fetchIntygData(intygId, intygTyp);
            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygTyp, intyg.getContents(),
                    serviceConverter.convertListOfIntygStatusToListOfStatus(intyg.getStatuses()));

            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
            logService.logPrintOfIntygAsPDF(logRequest);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    @Override
    public IntygServiceResult storeIntyg(Utkast utkast) {
        Omsandning omsandning = createOmsandning(OmsandningOperation.STORE_INTYG, utkast.getIntygsId(), utkast.getIntygsTyp(), null);
        // Redan schedulerat för att skickas, men vi gör ett försök redan nu.
        return storeIntyg(utkast, omsandning);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Omsandning createOmsandning(OmsandningOperation operation, String intygId, String typ, String configuration) {
        Omsandning omsandning = new Omsandning(operation, intygId, typ);
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
        return storeIntyg(utkastRepository.findOne(omsandning.getIntygId()), omsandning);
    }

    public IntygServiceResult storeIntyg(Utkast utkast, Omsandning omsandning) {
        try {
            registerIntyg(utkast);
            omsandningRepository.delete(omsandning);
            return IntygServiceResult.OK;
        } catch (ExternalServiceCallException | WebServiceException esce) {
            LOG.error("An WebServiceException occured when trying to fetch and send intyg: " + utkast.getIntygsId(), esce);
            scheduleResend(omsandning);
            return IntygServiceResult.RESCHEDULED;
        } catch (ModuleException | IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to send intyg " + utkast.getIntygsId(), e);
            omsandningRepository.delete(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    @Override
    public IntygServiceResult sendIntyg(Omsandning omsandning) {
        SendIntygConfiguration sendConfig = configurationManager.unmarshallConfig(omsandning.getConfiguration(), SendIntygConfiguration.class);
        IntygContentHolder intyg = getIntygData(omsandning.getIntygId(), omsandning.getIntygTyp());
        return sendIntyg(omsandning, sendConfig, intyg);
    }

    @Override
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient, boolean hasPatientConsent) {

        IntygContentHolder intyg = fetchIntygData(intygsId, typ);

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, hasPatientConsent);
        String sendConfigAsJson = configurationManager.marshallConfig(sendConfig);

        Omsandning omsandning = createOmsandning(OmsandningOperation.SEND_INTYG, intygsId, typ, sendConfigAsJson);

        return sendIntyg(omsandning, sendConfig, intyg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    @Override
    public IntygServiceResult revokeIntyg(String intygsId, String typ, String revokeMessage) {
        LOG.info("Attempting to revoke intyg {}", intygsId);

        IntygContentHolder intyg = fetchIntygData(intygsId, typ);

        if (intyg.isRevoked()) {
            LOG.info("Certificate with id '{}' is already revoked", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is already revoked");
        }

        RevokeType revokeType = serviceConverter.buildRevokeTypeFromUtlatande(intyg.getUtlatande(), revokeMessage);

        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(revokeType);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(logicalAddress);

        // Revoke the certificate
        RevokeMedicalCertificateResponseType response = revokeService.revokeMedicalCertificate(uri, request);

        // Take care of the response
        ResultOfCall resultOfCall = response.getResult();

        switch (resultOfCall.getResultCode()) {
        case OK:
            LOG.info("Successfully revoked intyg {}", intygsId);
            return whenSuccessfulRevoke(intygsId);
        case INFO:
            LOG.warn("Call to revoke intyg {} returned an info message: {}", intygsId, resultOfCall.getInfoText());
            return whenSuccessfulRevoke(intygsId);
        case ERROR:
            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}",
                    new Object[] { intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId() });
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, resultOfCall.getErrorText());
        default:
            return IntygServiceResult.FAILED;
        }
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    /* --------------------- Protected scope --------------------- */

    protected IntygServiceResult sendIntyg(Omsandning omsandning, SendIntygConfiguration sendConfig, IntygContentHolder intyg) {

        String intygsId = omsandning.getIntygId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = omsandning.getIntygTyp();

        try {
            LOG.info("Sending intyg {} of type {} to recipient {}", new Object[] { intygsId, intygsTyp, recipient });

            AttributedURIType address = new AttributedURIType();
            address.setValue(logicalAddress);

            SendType send = new SendType();
            send.setAdressVard(ModelConverter.toVardAdresseringsType(intyg.getUtlatande().getGrundData()));
            send.setLakarutlatande(ModelConverter.toLakarutlatandeEnkelType(intyg.getUtlatande()));
            send.setAvsantTidpunkt(LocalDateTime.now());
            send.setVardReferensId(intyg.getUtlatande().getId());

            SendMedicalCertificateRequestType parameters = new SendMedicalCertificateRequestType();
            parameters.setSend(send);

            SendMedicalCertificateResponseType response = sendService.sendMedicalCertificate(address, parameters);

            // check whether call was successful or not
            if (response.getResult().getResultCode() == ResultCodeEnum.ERROR) {
                String message = response.getResult().getErrorId() + " : " + response.getResult().getErrorText();
                LOG.error("Module problems occured when trying to send intyg " + intygsId + " : " + message);
                scheduleResend(omsandning);
                return IntygServiceResult.RESCHEDULED;
            } else {
                if (response.getResult().getResultCode() == ResultCodeEnum.INFO) {
                    String message = response.getResult().getInfoText();
                    LOG.warn("Warning occured when trying to send intyg " + intygsId + " : " + message);
                }
                omsandningRepository.delete(omsandning);

                // send PDL log event
                LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
                logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
                logService.logSendIntygToRecipient(logRequest);

                // Notify stakeholders when a certificate is sent
                sendNotification(intygsId, Event.SEND);

                return IntygServiceResult.OK;
            }

        } catch (RuntimeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intygsId, e);
            omsandningRepository.delete(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    protected void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            LOG.info("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }
    }

    /* --------------------- Private scope --------------------- */

    private IntygContentHolder getIntygData(String intygId, String typ) {
        try {
            CertificateResponse certificate = modelFacade.getCertificate(intygId, typ);
            List<IntygStatus> status = serviceConverter.convertListOfStatusToListOfIntygStatus(certificate.getMetaData().getStatus());
            String internalIntygJsonModel = certificate.getInternalModel();
            return new IntygContentHolder(internalIntygJsonModel, certificate.getUtlatande(), status, certificate.isRevoked());
        } catch (IntygModuleFacadeException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private void registerIntyg(Utkast utkast) throws IntygModuleFacadeException, ModuleException {
        LOG.debug("Attempting to register signed utkast {}", utkast.getIntygsId());
        modelFacade.registerCertificate(utkast.getIntygsTyp(), utkast.getModel());
        LOG.debug("Successfully registered signed utkast {}", utkast.getIntygsId());
    }

    private void scheduleResend(Omsandning omsandning) {
        omsandning.setNastaForsok(new LocalDateTime().plusHours(1));
        omsandning.setAntalForsok(omsandning.getAntalForsok() + 1);
        omsandningRepository.save(omsandning);
        LOG.info("Rescheduled {}", omsandning.toString());
    }

    /**
     * @see se.inera.webcert.service.intyg.IntygServiceImpl#sendNotification(se.inera.webcert.persistence.utkast.model.Utkast,
     *      se.inera.webcert.service.intyg.IntygServiceImpl.Event)
     */
    private void sendNotification(String intygId, Event event) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast != null) {
            sendNotification(utkast, event);
        } else {
            LOG.debug("Utkast '{}' was not found - no notification sent.", intygId);
        }
    }

    /**
     * Send a notification message to stakeholders informing that
     * an event of some type for this certificate has occurred.
     *
     * @param utkast
     *            the certificate that has been revoked
     * @param event
     *            the event for this notification
     */
    private void sendNotification(Utkast utkast, Event event) {

        switch (event) {
        case REVOKE:
            sendRevokedNotification(utkast);
            break;
        case SEND:
            notificationService.sendNotificationForIntygSent(utkast);
            LOG.debug("Notification sent: certificate with id '{}' has been sent to FK", utkast.getIntygsId());
        }

    }

    /**
     * Send a notification message to stakeholders informing that
     * a question related to a revoked certificate has been closed.
     *
     * @param utkast
     *            the certificate that has been revoked
     */
    private void sendRevokedNotification(Utkast utkast) {
        // First: send a notification informing stakeholders that this certificate has been revoked
        notificationService.sendNotificationForIntygRevoked(utkast);
        LOG.debug("Notification sent: certificate with id '{}' was revoked", utkast.getIntygsId());

        // Second: send a notification informing stakeholders that all questions related to the revoked
        // certificate has been closed.
        FragaSvar[] array = fragaSvarService.closeAllNonClosedQuestions(utkast.getIntygsId());
        for (int i = 0; i < array.length; i++) {
            // TODO: Add support for array of utkast ids in NotificatioService
            
            //notificationRequestType = NotificationMessageFactory.createNotificationFromClosedQuestionFromFK(array[i]);
            //notificationService.notify(notificationRequestType);
            LOG.debug("Notification sent: question with id '{}' (related with certificate with id '{}') was closed", array[i].getInternReferens(),
                    utkast.getIntygsId());
        }
    }

    private IntygServiceResult whenSuccessfulRevoke(String intygsId) {
        // Notify stakeholders when a certificate is revoked
        sendNotification(intygsId, Event.REVOKE);
        // Return OK
        return IntygServiceResult.OK;
    }

}
