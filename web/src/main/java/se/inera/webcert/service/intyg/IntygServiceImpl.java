package se.inera.webcert.service.intyg;

import java.util.ArrayList;
import java.util.List;

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
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
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
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.intyg.dto.IntygRecipient;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.intyg.dto.IntygStatus;
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
    private GetMedicalCertificateForCareResponderInterface getCertificateService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private RegisterCertificateResponderInterface intygSender;

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
    public IntygContentHolder fetchIntygData(String intygId, String typ) {
        try {
            CertificateResponse certificate = modelFacade.getCertificate(intygId, typ);
            verifyEnhetsAuth(certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid(), true);
            List<IntygStatus> status = serviceConverter.convertListOfStatusToListOfIntygStatus(certificate.getMetaData().getStatus());
            String internalIntygJsonModel = certificate.getInternalModel();

            return new IntygContentHolder(internalIntygJsonModel, certificate.getUtlatande(), status, certificate.isRevoked());

        } catch (IntygModuleFacadeException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
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

    @Override
    public IntygPdf fetchIntygAsPdf(String intygTyp, String intygId) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygId);

            IntygContentHolder intyg = fetchIntygData(intygTyp, intygId);
            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygTyp, intyg.getContents());

            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
            logService.logPrintOfIntygAsPDF(logRequest);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    protected void verifyEnhetsAuth(String enhetsId, boolean readOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, readOnlyOperation)) {
            LOG.info("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }

    }

    @Override
    public IntygServiceResult storeIntyg(Intyg intyg) {
        Omsandning omsandning = createOmsandning(OmsandningOperation.STORE_INTYG, intyg.getIntygsId(), intyg.getIntygsTyp(), null);
        // Redan schedulerat för att skickas, men vi gör ett försök redan nu.
        return storeIntyg(intyg, omsandning);
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
        return storeIntyg(intygRepository.findOne(omsandning.getIntygId()), omsandning);
    }

    public IntygServiceResult storeIntyg(Intyg intyg, Omsandning omsandning) {
        try {
            registerIntyg(intyg);
            omsandningRepository.delete(omsandning);
            return IntygServiceResult.OK;
        } catch (ExternalServiceCallException esce) {
            LOG.error("An WebServiceException occured when trying to fetch and send intyg: " + intyg.getIntygsId(), esce);
            scheduleResend(omsandning);
            return IntygServiceResult.RESCHEDULED;
        } catch (ModuleException | IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intyg.getIntygsId(), e);
            omsandningRepository.delete(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private void registerIntyg(Intyg intyg) throws IntygModuleFacadeException, ModuleException {
        LOG.debug("Attempting to register intyg {}", intyg.getIntygsId());
        modelFacade.registerCertificate(intyg.getIntygsTyp(), intyg.getModel());
        LOG.debug("Successfully registered intyg {}", intyg.getIntygsId());
    }

    private void scheduleResend(Omsandning omsandning) {
        omsandning.setNastaForsok(new LocalDateTime().plusHours(1));
        omsandning.setAntalForsok(omsandning.getAntalForsok() + 1);
        omsandningRepository.save(omsandning);
        LOG.info("Rescheduled {}", omsandning.toString());
    }

    @Override
    public IntygServiceResult sendIntyg(Omsandning omsandning) {
            SendIntygConfiguration sendConfig = configurationManager.unmarshallConfig(omsandning.getConfiguration(), SendIntygConfiguration.class);
            IntygContentHolder intyg = fetchIntygData(omsandning.getIntygTyp(), omsandning.getIntygId());
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

    public IntygServiceResult sendIntyg(Omsandning omsandning, SendIntygConfiguration sendConfig, IntygContentHolder intyg) {

        String intygsId = omsandning.getIntygId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = omsandning.getIntygTyp();
        
        try {
            LOG.info("Sending intyg {} of type {} to recipient {}", new Object[] { intygsId, intygsTyp, recipient });

            modelFacade.sendCertificate(intygsTyp, intyg.getContents(), recipient);

            omsandningRepository.delete(omsandning);

            // send PDL log event
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
            logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
            logService.logSendIntygToRecipient(logRequest);

            // Notify stakeholders when a certificate is sent
            notify(intygsId, Event.SEND);

            return IntygServiceResult.OK;

        } catch (ExternalServiceCallException esce) {
            LOG.error("An WebServiceException occured when trying to fetch and send intyg: " + intygsId, esce);
            scheduleResend(omsandning);
            return IntygServiceResult.RESCHEDULED;
        } catch (ModuleException | IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intygsId, e);
            omsandningRepository.delete(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    public IntygServiceResult revokeIntyg(String intygsId, String typ, String revokeMessage) {
        LOG.info("Attempting to revoke intyg {}", intygsId);

        IntygContentHolder intyg = fetchIntygData(intygsId, typ);

        if (intyg.isRevoked()) {
            LOG.info("Certificate is revoked");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is revoked");
        }

        RevokeType revokeType = serviceConverter.buildRevokeTypeFromUtlatande(intyg.getUtlatande(), revokeMessage);

        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(revokeType);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(logicalAddress);
        // Notify stakeholders when a certificate is revoked
        notify(intygsId, Event.REVOKE);

        RevokeMedicalCertificateResponseType response = revokeService.revokeMedicalCertificate(uri, request);

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
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    private void notify(String intygId, Event event) {
        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg != null) {
            notify(intyg, event);
        } else {
            LOG.debug("Intyg '{}' was not found", intygId);
        }
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

}
