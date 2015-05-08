package se.inera.webcert.service.intyg;

import java.io.IOException;
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

import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.model.common.internal.Vardenhet;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.OmsandningOperation;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepository;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.fragasvar.dto.FrageStallare;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.SendIntygConfiguration;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

import com.fasterxml.jackson.databind.ObjectMapper;


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
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

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
    private MonitoringLogService monitoringService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private ObjectMapper objectMapper;
    
    /* --------------------- Public scope --------------------- */

    @Override
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp) {
        IntygContentHolder intygsData = getIntygData(intygsId, intygsTyp);
        verifyEnhetsAuth(intygsData.getUtlatande(), true);
        
        // Log read to PDL
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intygsData.getUtlatande());
        logService.logReadIntyg(logRequest);
        
        // Log read to monitoring log
        monitoringService.logIntygRead(intygsId, intygsTyp);
        
        return intygsData;
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

    @Override
    public IntygPdf fetchIntygAsPdf(String intygsId, String intygsTyp) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygsId);

            IntygContentHolder intyg = getIntygData(intygsId, intygsTyp);
            verifyEnhetsAuth(intyg.getUtlatande(), true);

            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygsTyp, intyg.getContents(), intyg.getStatuses());
            
            // Log print as PDF to PDL log
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
            logService.logPrintIntygAsPDF(logRequest);
            
            // Log print as PDF to monitoring log
            monitoringService.logIntygPrintPdf(intygsId, intygsTyp);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    @Override
    public IntygServiceResult storeIntyg(Utkast utkast) {
        Omsandning omsandning = createOmsandning(OmsandningOperation.STORE_INTYG, utkast.getIntygsId(), utkast.getIntygsTyp(), null);
        
        // Audit log
        monitoringService.logIntygRegistered(utkast.getIntygsId(), utkast.getIntygsTyp());

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
        Utkast utkast = utkastRepository.findOne(omsandning.getIntygId());
        if (utkast == null) {
            LOG.warn("Could not store intyg in Intygstjansten, no draft found for intyg id '{}'", omsandning.getIntygId());
            return IntygServiceResult.FAILED;
        }
        return storeIntyg(utkast, omsandning);
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
        Utlatande intyg = getUtlatandeForIntyg(omsandning.getIntygId(), omsandning.getIntygTyp());
        return sendIntyg(omsandning, sendConfig, intyg);
    }

    @Override
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient, boolean hasPatientConsent) {

        Utlatande intyg = getUtlatandeForIntyg(intygsId, typ);
        verifyEnhetsAuth(intyg, true);

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, hasPatientConsent, webCertUserService.getWebCertUser());
        String sendConfigAsJson = configurationManager.marshallConfig(sendConfig);

        monitoringService.logIntygSent(intygsId, recipient);

        // send PDL log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        Omsandning omsandning = createOmsandning(OmsandningOperation.SEND_INTYG, intygsId, typ, sendConfigAsJson);

        return sendIntyg(omsandning, sendConfig, intyg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    @Override
    public IntygServiceResult revokeIntyg(String intygsId, String intygsTyp, String revokeMessage) {
        LOG.debug("Attempting to revoke intyg {}", intygsId);
        IntygContentHolder intyg = getIntygData(intygsId, intygsTyp);
        verifyEnhetsAuth(intyg.getUtlatande(), true);

        if (intyg.isRevoked()) {
            LOG.debug("Certificate with id '{}' is already revoked", intygsId);
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
            String hsaId = webCertUserService.getWebCertUser().getHsaId();
            monitoringService.logIntygRevoked(intygsId, hsaId);
            return whenSuccessfulRevoke(intyg.getUtlatande());
        case INFO:
            LOG.warn("Call to revoke intyg {} returned an info message: {}", intygsId, resultOfCall.getInfoText());
            return whenSuccessfulRevoke(intyg.getUtlatande());
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

    protected IntygServiceResult sendIntyg(Omsandning omsandning, SendIntygConfiguration sendConfig, Utlatande intyg) {

        String intygsId = omsandning.getIntygId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = omsandning.getIntygTyp();

        try {
            LOG.debug("Sending intyg {} of type {} to recipient {}", new Object[] { intygsId, intygsTyp, recipient });

            SendCertificateToRecipientType request = new SendCertificateToRecipientType();
            request.setUtlatandeId(intygsId);
            request.setPersonId(intyg.getGrundData().getPatient().getPersonId());
            request.setMottagareId(recipient);

            SendCertificateToRecipientResponseType response = sendService.sendCertificateToRecipient(logicalAddress, request);

            // check whether call was successful or not
            if (ResultCodeType.ERROR.equals(response.getResult().getResultCode())) {
                LOG.error("Error occured when trying to send intyg '{}'; {}", intygsId, response.getResult().getResultText());
                scheduleResend(omsandning);
                return IntygServiceResult.RESCHEDULED;
            } else {
                if (ResultCodeType.INFO.equals(response.getResult().getResultCode())) {
                    LOG.warn("Warning occured when trying to send intyg '{}'; {}", intygsId, response.getResult().getResultText());
                }
                // Notify stakeholders when a certificate is sent
                notificationService.sendNotificationForIntygSent(intygsId);
                LOG.debug("Notification sent: certificate with id '{}' has been sent to '{}'", intygsId, recipient);

                omsandningRepository.delete(omsandning);

                return IntygServiceResult.OK;
            }

        } catch (WebServiceException wse) {
            LOG.error("An WebServiceException occured when trying to send intyg: " + intygsId, wse);
            scheduleResend(omsandning);
            return IntygServiceResult.RESCHEDULED;
        } catch (RuntimeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intygsId, e);
            scheduleResend(omsandning);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    protected void verifyEnhetsAuth(Utlatande utlatande, boolean isReadOnlyOperation) {
        Vardenhet vardenhet = utlatande.getGrundData().getSkapadAv().getVardenhet();
        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(), isReadOnlyOperation)) {
            LOG.debug("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + vardenhet.getEnhetsid());
        }
    }

    /* --------------------- Private scope --------------------- */

    private IntygContentHolder getIntygData(String intygId, String typ) {
        try {
            CertificateResponse certificate = modelFacade.getCertificate(intygId, typ);
            List<Status> status = certificate.getMetaData().getStatus();
            String internalIntygJsonModel = certificate.getInternalModel();
            return new IntygContentHolder(internalIntygJsonModel, certificate.getUtlatande(), status, certificate.isRevoked());
        } catch (IntygModuleFacadeException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private Utlatande getUtlatandeForIntyg(String intygId, String typ) {
        Utkast utkast = utkastRepository.findOne(intygId);
        if (utkast != null) {
            try {
                return objectMapper.readValue(utkast.getModel(), Utlatande.class);
            } catch (IOException e) {
                LOG.error("Module problems occured when trying to unmarshall utlandande.", e);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
            }
        } else {
            IntygContentHolder intyg = getIntygData(intygId, typ);
            return intyg.getUtlatande();
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
        LOG.debug("Rescheduled {}", omsandning.toString());
    }

    /**
     * Send a notification message to stakeholders informing that
     * a question related to a revoked certificate has been closed.
     * 
     * @param intyg
     * @return
     */
    private IntygServiceResult whenSuccessfulRevoke(Utlatande intyg) {
        String intygsId = intyg.getId();
        // First: send a notification informing stakeholders that this certificate has been revoked
        notificationService.sendNotificationForIntygRevoked(intygsId);
        LOG.debug("Notification sent: certificate with id '{}' was revoked", intygsId);

        // Second: send a notification informing stakeholders that all questions related to the revoked
        // certificate has been closed.
        FragaSvar[] closedFragaSvarArr = fragaSvarService.closeAllNonClosedQuestions(intygsId);

        for (FragaSvar closedFragaSvar : closedFragaSvarArr) {
            String frageStallare = closedFragaSvar.getFrageStallare();
            if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
                notificationService.sendNotificationForQuestionHandled(closedFragaSvar);
            } else if (FrageStallare.WEBCERT.equals(frageStallare)) {
                notificationService.sendNotificationForAnswerHandled(closedFragaSvar);
            }

            LOG.debug("Notification sent: question with id '{}' (related with certificate with id '{}') was closed",
                    closedFragaSvar.getInternReferens(),
                    intygsId);
        }

        // Third: create a log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logService.logRevokeIntyg(logRequest);

        // Return OK
        return IntygServiceResult.OK;
    }

}
