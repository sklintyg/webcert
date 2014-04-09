package se.inera.webcert.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateStatusType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.IntygMetadata;
import se.inera.webcert.service.dto.IntygStatus;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService {

    @Value("${intygstjanst.logicaladdress}")
    String logicalAddress;

    private static Marshaller marshaller;
    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    static {
        try {
            JAXBContext context = JAXBContext.newInstance(UtlatandeType.class);
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            LOG.error("Failed to initialize marshaller for GetCertificate interaction", e);
            Throwables.propagate(e);
        }
    }

    @Autowired
    private GetCertificateForCareResponderInterface getCertificateService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private LogService logService;

    @Override
    public IntygContentHolder fetchIntygData(String intygId) {

        IntygContentHolder intygAsExternal = fetchExternalIntygData(intygId);

        IntygMetadata metaData = intygAsExternal.getMetaData();

        try {

            ModuleApi moduleApi = moduleRegistry.getModuleApi(metaData.getType());

            ExternalModelHolder extHolder = new ExternalModelHolder(intygAsExternal.getContents());
            InternalModelResponse internalModelReponse = moduleApi.convertExternalToInternal(extHolder);

            return new IntygContentHolder(internalModelReponse.getInternalModel(), metaData);

        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public IntygContentHolder fetchExternalIntygData(String intygId) {
        try {

            GetCertificateForCareResponseType intyg = fetchIntygFromIntygstjanst(intygId);

            verifyEnhetsAuth(intyg.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension());

            String patientId = intyg.getCertificate().getPatient().getPersonId().getExtension();
            IntygMetadata metaData = convertToCertificateContentMeta(patientId, intyg.getMeta());

            String intygType = metaData.getType();

            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

            String xml = marshal(intyg.getCertificate());
            ExternalModelResponse unmarshallResponse = moduleApi.unmarshall(new TransportModelHolder(xml));

            LogRequest logRequest = createLogRequest(unmarshallResponse.getExternalModel());
            logService.logReadOfIntyg(logRequest);

            return new IntygContentHolder(unmarshallResponse.getExternalModelJson(),
                    unmarshallResponse.getExternalModel(), metaData);

        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private IntygMetadata convertToCertificateContentMeta(String patientId, CertificateMetaType source) {

        IntygMetadata metaData = new IntygMetadata();
        metaData.setPatientId(patientId);
        metaData.setId(source.getCertificateId());
        metaData.setType(source.getCertificateType());
        metaData.setFromDate(source.getValidFrom());
        metaData.setTomDate(source.getValidTo());

        metaData.setStatuses(convertStatus(source.getStatus()));

        return metaData;
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
            return convert(response.getMeta());
        default:
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                    "listCertificatesForCare WS call: ERROR :" + response.getResult().getResultText());
        }
    }

    public PdfResponse fetchIntygAsPdf(String intygId) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygId);

            IntygContentHolder intygAsExternal = fetchExternalIntygData(intygId);

            String intygType = intygAsExternal.getMetaData().getType();

            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

            PdfResponse pdfResponse = moduleApi.pdf(new ExternalModelHolder(intygAsExternal.getContents()));

            LogRequest logRequest = createLogRequest(intygAsExternal.getExternalModel());
            logService.logPrintOfIntyg(logRequest);

            return pdfResponse;

        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private List<IntygItem> convert(List<CertificateMetaType> source) {
        List<IntygItem> intygItems = new ArrayList<>();
        for (CertificateMetaType certificateMetaType : source) {
            intygItems.add(convert(certificateMetaType));
        }
        return intygItems;
    }

    private IntygItem convert(CertificateMetaType source) {

        IntygItem item = new IntygItem();
        item.setId(source.getCertificateId());
        item.setType(source.getCertificateType());
        item.setFromDate(source.getValidFrom());
        item.setTomDate(source.getValidTo());
        item.setStatuses(convertStatus(source.getStatus()));
        item.setSignedBy(source.getIssuerName());
        item.setSignedDate(source.getSignDate());

        return item;
    }

    private List<IntygStatus> convertStatus(List<CertificateStatusType> source) {
        List<IntygStatus> status = new ArrayList<>();
        for (CertificateStatusType certificateStatusType : source) {
            status.add(convert(certificateStatusType));
        }
        return status;
    }

    private IntygStatus convert(CertificateStatusType source) {
        return new IntygStatus(source.getType().value(), source.getTarget(), source.getTimestamp());
    }

    private String marshal(UtlatandeType utlatandeTyp) {
        StringWriter writer = new StringWriter();
        try {
            JAXBElement<UtlatandeType> jaxbElement = new ObjectFactory().createUtlatande(utlatandeTyp);
            marshaller.marshal(jaxbElement, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Failed to marshall intyg coming from intygstjanst");
        }
    }

    private GetCertificateForCareResponseType fetchIntygFromIntygstjanst(String intygsId) {
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(intygsId);

        GetCertificateForCareResponseType response = getCertificateService.getCertificateForCare(logicalAddress,
                request);

        switch (response.getResult().getResultCode()) {
        case OK:
        case REVOKED:
            return response;
        case VALIDATION_ERROR:
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "getCertificateForCare WS call:  VALIDATION_ERROR :" + response.getResult().getResultText());
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

        String patientFornamn = StringUtils.join(patient.getFornamn(), " ");

        logRequest.setPatientName(patientFornamn, patient.getEfternamn());

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

}
