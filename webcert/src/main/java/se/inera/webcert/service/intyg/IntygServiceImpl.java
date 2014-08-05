package se.inera.webcert.service.intyg;

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
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    private String sendCertLogicalAddress;

    @Autowired
    private GetCertificateForCareResponderInterface getCertificateService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private RegisterCertificateResponderInterface intygSender;

    @Autowired
    private OmsandningRepository omsandningRepository;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private LogService logService;

    @Autowired
    private SendMedicalCertificateResponderInterface sendService;

    @Autowired
    private IntygModuleFacade modelFacade;

    @Autowired
    private IntygServiceConverter serviceConverter;

    @Override
    public IntygContentHolder fetchIntygData(String intygId) {
        try {
            IntygContentHolder intygAsExternal = fetchExternalIntygData(intygId);

            IntygMetadata metaData = intygAsExternal.getMetaData();

            String internalIntygJsonModel = modelFacade.convertFromExternalToInternal(metaData.getType(), intygAsExternal.getContents());

            return new IntygContentHolder(internalIntygJsonModel, metaData);

        } catch (IntygModuleFacadeException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public IntygContentHolder fetchExternalIntygData(String intygId) {
        try {

            GetCertificateForCareResponseType intygResponse = fetchIntygFromIntygstjanst(intygId);

            verifyEnhetsAuth(intygResponse.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension());

            String patientId = intygResponse.getCertificate().getPatient().getPersonId().getExtension();
            IntygMetadata metaData = serviceConverter.convertToIntygMetadata(patientId, intygResponse.getMeta());

            ExternalModelResponse intygAsExternal = modelFacade.convertFromTransportToExternal(metaData.getType(), intygResponse.getCertificate());

            LogRequest logRequest = createLogRequest(intygAsExternal.getExternalModel());
            logService.logReadOfIntyg(logRequest);

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

    public IntygPdf fetchIntygAsPdf(String intygId) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygId);

            IntygContentHolder intygAsExternal = fetchExternalIntygData(intygId);

            String intygType = intygAsExternal.getMetaData().getType();

            IntygPdf intygPdf = modelFacade.convertFromExternalToPdfDocument(intygType, intygAsExternal.getContents());

            LogRequest logRequest = createLogRequest(intygAsExternal.getExternalModel());
            logService.logPrintOfIntyg(logRequest);

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

    private LogRequest createLogRequest(Utlatande cert) {

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(cert.getId().getExtension());

        Patient patient = cert.getPatient();

        logRequest.setPatientId(patient.getId().getExtension());

        logRequest.setPatientName(patient.getFullstandigtNamn());

        Vardenhet skapadAvVardenhet = cert.getSkapadAv().getVardenhet();

        logRequest.setIntygCareUnitId(skapadAvVardenhet.getId().getExtension());
        logRequest.setIntygCareUnitName(skapadAvVardenhet.getNamn());

        Vardgivare skapadAvVardgivare = skapadAvVardenhet.getVardgivare();

        logRequest.setIntygCareGiverId(skapadAvVardgivare.getId().getExtension());
        logRequest.setIntygCareGiverName(skapadAvVardgivare.getNamn());

        return logRequest;
    }

    protected void verifyEnhetsAuth(String enhetsId) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }

    }

    @Override
    public boolean storeIntyg(Intyg intyg) {
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

        return omsandningRepository.save(omsandning);
    }

    public boolean storeIntyg(Omsandning omsandning) {
        return storeIntyg(intygRepository.findOne(omsandning.getIntygId()), omsandning);
    }

    public boolean storeIntyg(Intyg intyg, Omsandning omsandning) {
        try {

            if (!registerIntyg(intyg)) {
                scheduleResend(omsandning);
                return false;
            }

            omsandningRepository.delete(omsandning);
            return true;

        } catch (IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to register intyg " + intyg.getIntygsId(), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private boolean registerIntyg(Intyg intyg) throws IntygModuleFacadeException {

        LOG.info("Attempting to register intyg {}", intyg.getIntygsId());
        UtlatandeType utlatandeType = modelFacade.convertFromInternalToTransport(intyg.getIntygsTyp(), intyg.getModel());

        utlatandeType.setSigneringsdatum(intyg.getSenastSparadDatum());
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
    }

    public boolean sendIntyg(Omsandning omsandning) {
        return sendIntyg(intygRepository.findOne(omsandning.getIntygId()), omsandning);
    }

    public boolean sendIntyg(String intygId, String recipient) {
        return sendIntyg(intygRepository.findOne(intygId), recipient);
    }
    
    public boolean sendIntyg(Intyg intyg, String recipient) {
        Omsandning omsandning = createOmsandning(OmsandningOperation.SEND_INTYG, intyg.getIntygsId(), recipient);
        // Redan schedulerat för att skickas, men vi gör ett försök redan nu.
        return sendIntyg(intyg, omsandning);
    }

    public boolean sendIntyg(Intyg intyg, Omsandning omsandning) {
        try {
            if (!performSendIntyg(intyg, omsandning.getConfiguration())) {
                scheduleResend(omsandning);
                return false;
            }

            omsandningRepository.delete(omsandning);
            return true;

        } catch (IntygModuleFacadeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intyg.getIntygsId(), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private boolean performSendIntyg(Intyg intyg, String target) throws IntygModuleFacadeException {

        LOG.info("Attempting to send intyg {} of type {} to recipient {}", new Object[] { intyg.getIntygsId(),
                intyg.getIntygsTyp(), target });

        Utlatande utlatande = modelFacade.convertFromInternalToExternal(intyg.getIntygsTyp(), intyg.getModel());

        SendType sendType = serviceConverter.buildSendTypeFromUtlatande(utlatande);

        SendMedicalCertificateRequestType request = new SendMedicalCertificateRequestType();
        request.setSend(sendType);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(target);

        SendMedicalCertificateResponseType response;

        try {
            response = sendService.sendMedicalCertificate(uri, request);
        } catch (WebServiceException wse) {
            LOG.error("A WebServiceException occured when trying to send intyg " + intyg.getIntygsId(), wse);
            return false;
        }

        ResultOfCall resultOfCall = response.getResult();

        switch (resultOfCall.getResultCode()) {
        case OK:
            LOG.debug("Successfully sent intyg {} of type {} to recipient {}", new Object[] { intyg.getIntygsId(), intyg.getIntygsTyp(), target });
            return true;
        case INFO:
            LOG.warn("Call to send intyg {} returned an info message: {}", intyg.getIntygsId(), resultOfCall.getInfoText());
            return true;
        case ERROR:
            LOG.error("Call to send intyg {} casused an error: {}, ErrorId: {}", new Object[] { intyg.getIntygsId(), resultOfCall.getErrorText(),
                    resultOfCall.getErrorId() });
            return false;
        default:
            return false;
        }
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }
}
