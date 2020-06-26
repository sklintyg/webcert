package se.inera.intyg.webcert.web.service.underskrift.dss;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.dss.xsd.dsscore.InputDocuments;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignRequest;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignResponse;
import se.inera.intyg.webcert.dss.xsd.dssext.CertRequestPropertiesType;
import se.inera.intyg.webcert.dss.xsd.dssext.MappedAttributeType;
import se.inera.intyg.webcert.dss.xsd.dssext.SignMessageType;
import se.inera.intyg.webcert.dss.xsd.dssext.SignRequestExtensionType;
import se.inera.intyg.webcert.dss.xsd.dssext.SignResponseExtensionType;
import se.inera.intyg.webcert.dss.xsd.dssext.SignTasksType;
import se.inera.intyg.webcert.dss.xsd.samlassertion.v2.AttributeStatementType;
import se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ConditionsType;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.SignatureApiController;

@Service
public class DssSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignatureService.class);
    public static final String RESULTMAJOR_SUCCESS = "urn:oasis:names:tc:dss:1.0:resultmajor:Success"; //TODO
    public static final String REQUESTED_SIGN_ALGORITHM = XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256;

    private final DssMetadataService dssMetadataService;
    private final WebCertUserService userService;
    private final UtkastRepository utkastRepository;
    private final DssSignMessageService dssSignMessageService;
    private final UnderskriftService underskriftService;
    private final RedisTicketTracker redisTicketTracker;
    private final MonitoringLogService monitoringLogService;
    private final IntygModuleRegistry moduleRegistry;

    private final se.inera.intyg.webcert.dss.xsd.dsscore.ObjectFactory objectFactoryDssCore;
    private final se.inera.intyg.webcert.dss.xsd.dssext.ObjectFactory objectFactoryCsig;
    private final se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ObjectFactory objectFactorySaml;

    @Value("${dss.client.metadata.host.url}")
    private String dssClientHostUrl;

    @Value("${dss.service.clientid}")
    private String customerId;

    @Value("${dss.service.applicationid}")
    private String applicationId;

    @Value("${dss.service.idpurl}")
    private String idpUrl;

    @Value("${dss.service.serviceurl}")
    private String serviceUrl;

    @Value("${dss.service.signmessage}")
    private String signMessage;

    @Value("${dss.client.ie.unit.whitelist:}")
    private String dssUnitWhitelistForIeProperty;
    private List<String> dssUnitWhitelistForIe = new ArrayList<>();

    @Autowired
    public DssSignatureService(DssMetadataService dssMetadataService, DssSignMessageService dssSignMessageService,
        WebCertUserService userService, UtkastRepository utkastRepository, UnderskriftService underskriftService,
        RedisTicketTracker redisTicketTracker, MonitoringLogService monitoringLogService, IntygModuleRegistry moduleRegistry) {
        objectFactoryDssCore = new se.inera.intyg.webcert.dss.xsd.dsscore.ObjectFactory();
        objectFactoryCsig = new se.inera.intyg.webcert.dss.xsd.dssext.ObjectFactory();
        objectFactorySaml = new se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ObjectFactory();
        this.dssMetadataService = dssMetadataService;
        this.userService = userService;
        this.utkastRepository = utkastRepository;
        this.dssSignMessageService = dssSignMessageService;
        this.underskriftService = underskriftService;
        this.redisTicketTracker = redisTicketTracker;
        this.monitoringLogService = monitoringLogService;
        this.moduleRegistry = moduleRegistry;
    }

    @PostConstruct
    private void init() {
        if (StringUtils.hasText(dssUnitWhitelistForIeProperty)) {
            for (String s : dssUnitWhitelistForIeProperty.split(",")) {
                dssUnitWhitelistForIe.add(s.trim());
            }
        }
    }

    /**
     * Checks whether the current Care unit HSA-id is in the whitelist
     * for IE. If HSA id, or a wildcard version of it is in the list
     * then this method will return true.
     *
     * @param currentCareUnitHsaId The users currently selected care unit
     * @return true id care unit is in whitelist.
     */
    public boolean isUnitInIeWhitelist(String currentCareUnitHsaId) {
        if (StringUtils.isEmpty(currentCareUnitHsaId)) {
            return false;
        }

        boolean inWhitelist = false;
        for (String hsaIdInWhitelist : dssUnitWhitelistForIe) {
            if (hsaIdInWhitelist.endsWith("*")) {
                var wildcardRemovedSubstring = hsaIdInWhitelist.substring(0, hsaIdInWhitelist.lastIndexOf("*"));
                inWhitelist = currentCareUnitHsaId.toUpperCase().startsWith(wildcardRemovedSubstring.toUpperCase());
            } else {
                inWhitelist = currentCareUnitHsaId.equalsIgnoreCase(hsaIdInWhitelist);
            }
            if (inWhitelist) {
                break;
            }

        }
        return inWhitelist;
    }

    public DssSignRequestDTO createSignatureRequestDTO(SignaturBiljett sb) {
        var dateTimeNow = DateTime.now();

        var dssSignRequestDTO = new DssSignRequestDTO();
        String transactionID = sb.getTicketId();
        dssSignRequestDTO.setTransactionId(transactionID);
        dssSignRequestDTO.setActionUrl(dssMetadataService.getDssActionUrl());

        String signRequest = dssSignMessageService.signSignRequest(createSignRequest(dateTimeNow, sb, transactionID));
        String base64EncodedSignRequest = Base64.getEncoder().encodeToString(signRequest.getBytes(Charset.forName("UTF-8")));
        dssSignRequestDTO.setSignRequest(base64EncodedSignRequest);

        return dssSignRequestDTO;
    }

    private SignRequest createSignRequest(DateTime dateTimeNow, SignaturBiljett sb, String transactionID) {
        var signRequest = objectFactoryDssCore.createSignRequest();
        signRequest.setRequestID(transactionID);
        signRequest.setProfile("http://id.elegnamnden.se/csig/1.1/dss-ext/profile");
        signRequest.setInputDocuments(createInputDocuments(sb));

        var optionalInputs = objectFactoryDssCore.createAnyType();
        optionalInputs.getAny().add(createSignRequestExtension(sb.getIntygsId(), dateTimeNow));

        signRequest.setOptionalInputs(optionalInputs);

        return signRequest;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public String createTransactionID() {
        var timestamp = Long.toHexString(DateTime.now().getMillis());
        var uuid = generateUUID();
        return String.format("%s-%s-%s-%s", formatIdField(customerId), formatIdField(applicationId), timestamp, uuid);
    }

    private String formatIdField(String id) {
        var idStr = id.replaceAll("[^a-zA-Z\\d:]", "");
        return idStr.substring(0, Math.min(idStr.length(), 11)).toLowerCase();
    }

    private InputDocuments createInputDocuments(SignaturBiljett sb) {
        var signTaskDataType = objectFactoryCsig.createSignTaskDataType();
        signTaskDataType.setSigType("XML");
        signTaskDataType.setSignTaskId(generateUUID());
        signTaskDataType.setToBeSignedBytes(sb.getHash().getBytes());

        var tasks = objectFactoryCsig.createSignTasksType();
        tasks.getSignTaskData().add(signTaskDataType);

        var signTasks = objectFactoryCsig.createSignTasks(tasks);

        var other = objectFactoryDssCore.createAnyType();
        other.getAny().add(signTasks);

        var inputDocuments = new InputDocuments();
        inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(other);
        return inputDocuments;
    }

    private JAXBElement<SignRequestExtensionType> createSignRequestExtension(String intygsId, DateTime dateTimeNow) {
        var signRequestExtensionType = objectFactoryCsig.createSignRequestExtensionType();

        signRequestExtensionType.setRequestTime(convertToXmlGregorianTime(dateTimeNow));

        signRequestExtensionType.setConditions(createConditions(dateTimeNow));

        signRequestExtensionType.setSigner(createSigner());

        var identityProvider = objectFactorySaml.createNameIDType();
        identityProvider.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        identityProvider.setValue(idpUrl);
        signRequestExtensionType.setIdentityProvider(identityProvider);

        var signRequester = objectFactorySaml.createNameIDType();
        signRequester.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        signRequester.setValue(
            dssClientHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_METADATA_PATH);
        signRequestExtensionType.setSignRequester(signRequester);

        var signService = objectFactorySaml.createNameIDType();
        signService.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        signService.setValue(serviceUrl);
        signRequestExtensionType.setSignService(signService);

        signRequestExtensionType.setRequestedSignatureAlgorithm(REQUESTED_SIGN_ALGORITHM);

        signRequestExtensionType.setCertRequestProperties(createCertRequestProperties());

        signRequestExtensionType.setSignMessage(createSignMessage(intygsId));

        return objectFactoryCsig.createSignRequestExtension(signRequestExtensionType);
    }

    private SignMessageType createSignMessage(String intygsId) {
        var utkast = utkastRepository.findById(intygsId).orElse(null);
        String intygsTyp = "";
        Personnummer patientPersonnummer = null;

        if (utkast != null) {
            patientPersonnummer = utkast.getPatientPersonnummer();
            try {
                intygsTyp = moduleRegistry.getIntygModule(utkast.getIntygsTyp()).getLabel();
            } catch (ModuleNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        SignMessageType signMessage = objectFactoryCsig.createSignMessageType();
        signMessage.setMimeType(MimeTypeUtils.TEXT_HTML_VALUE);
        signMessage.setMustShow(false);
        String message = this.signMessage.replace("{intygsTyp}", intygsTyp)
            .replace("{patientPnr}", patientPersonnummer != null ? patientPersonnummer.getPersonnummerWithDash() : "")
            .replace("{intygsId}", intygsId);
        signMessage.setMessage(message.getBytes(StandardCharsets.UTF_8));

        return signMessage;
    }

    private CertRequestPropertiesType createCertRequestProperties() {

        var certRequestPropertiesType = objectFactoryCsig.createCertRequestPropertiesType();
        certRequestPropertiesType.setCertType("PKC");
        certRequestPropertiesType.setAuthnContextClassRef("http://id.sambi.se/loa/loa3");

        var requestedAttributesType = objectFactoryCsig.createRequestedAttributesType();

        MappedAttributeType attributeGivenName = getMappedAttributeType("2.5.4.42", "givenName", true, "urn:oid:2.5.4.42");
        requestedAttributesType.getRequestedCertAttribute().add(attributeGivenName);

        MappedAttributeType attributeSn = getMappedAttributeType("2.5.4.4", "sn", true, "urn:oid:2.5.4.4");
        requestedAttributesType.getRequestedCertAttribute().add(attributeSn);

        MappedAttributeType attributeSerialNumber = getMappedAttributeType("2.5.4.5", "serialNumber", false, "urn:oid:1.2.752.29.4.13");
        requestedAttributesType.getRequestedCertAttribute().add(attributeSerialNumber);

        MappedAttributeType attributeCommonName = getMappedAttributeType("2.5.4.3", "commonName", false,
            "urn:oid:2.16.840.1.113730.3.1.241");
        requestedAttributesType.getRequestedCertAttribute().add(attributeCommonName);

        MappedAttributeType attributeDisplayName = getMappedAttributeType("2.16.840.1.113730.3.1.241", "displayName", false,
            "urn:oid:2.16.840.1.113730.3.1.241");
        requestedAttributesType.getRequestedCertAttribute().add(attributeDisplayName);

        certRequestPropertiesType.setRequestedCertAttributes(requestedAttributesType);
        return certRequestPropertiesType;
    }

    private MappedAttributeType getMappedAttributeType(String ref, String name, boolean req, String attribute) {
        var mappedAttributeType = objectFactoryCsig.createMappedAttributeType();
        mappedAttributeType.setCertAttributeRef(ref);
        mappedAttributeType.setFriendlyName(name);
        mappedAttributeType.setRequired(req);
        var preferredSAMLAttributeNameType = objectFactoryCsig.createPreferredSAMLAttributeNameType();
        preferredSAMLAttributeNameType.setValue(attribute);
        mappedAttributeType.getSamlAttributeName().add(preferredSAMLAttributeNameType);
        return mappedAttributeType;
    }

    private AttributeStatementType createSigner() {
        var user = userService.getUser();

        var attributeType = objectFactorySaml.createAttributeType();
        attributeType.setName("urn:oid:1.2.752.29.4.13");
        attributeType.getAttributeValue().add(user.getHsaId());

        var attributeStatementType = objectFactorySaml.createAttributeStatementType();
        attributeStatementType.getAttributeOrEncryptedAttribute().add(attributeType);
        return attributeStatementType;
    }

    private ConditionsType createConditions(DateTime dateTimeNow) {
        var conditionsType = objectFactorySaml.createConditionsType();

        var beforeTime = dateTimeNow.minusMinutes(2);
        conditionsType.setNotBefore(convertToXmlGregorianTime(beforeTime));

        var afterTime = dateTimeNow.plusMinutes(5);
        conditionsType.setNotOnOrAfter(convertToXmlGregorianTime(afterTime));

        var audienceRestrictionType = objectFactorySaml.createAudienceRestrictionType();
        audienceRestrictionType.getAudience()
            .add(dssClientHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_RESPONSE_PATH);

        conditionsType.getConditionOrAudienceRestrictionOrOneTimeUse().add(audienceRestrictionType);
        return conditionsType;
    }

    private XMLGregorianCalendar convertToXmlGregorianTime(DateTime beforeCalendar) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(beforeCalendar.toGregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            LOG.error("Error converting date for SignRequest!", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
        return xmlGregorianCalendar;
    }

    @SuppressWarnings("unchecked")
    public SignaturBiljett receiveSignResponse(String relayState, String eIdSignResponse) {
        SignResponse signResponse;
        try {
            signResponse = unMarshallSignResponse(eIdSignResponse);

            var result = signResponse.getResult();

            var resultMajor = result.getResultMajor();
            if (RESULTMAJOR_SUCCESS.equals(resultMajor)) {

                var signTasks = (JAXBElement<SignTasksType>) signResponse.getSignatureObject().getOther().getAny().get(0);
                var signTaskDataType = signTasks.getValue().getSignTaskData().get(0);
                byte[] signature = signTaskDataType.getBase64Signature()
                    .getValue();

                var signResponseExtension = (JAXBElement<SignResponseExtensionType>) signResponse.getOptionalOutputs().getAny().get(0);
                var certificates = signResponseExtension.getValue().getSignatureCertificateChain().getX509Certificate();
                var certificate = findFirstCertificateInChain(certificates);

                return underskriftService.netidSignature(relayState, signature, certificate);
            } else {
                var resultMinor = result.getResultMinor();
                var resultMessage = result.getResultMessage() == null ? null : result.getResultMessage().getValue();

                SignaturBiljett signaturBiljett = redisTicketTracker.updateStatus(relayState, SignaturStatus.ERROR);
                monitoringLogService
                    .logSignServiceErrorReceived(relayState, signaturBiljett.getIntygsId(), resultMajor, resultMinor, resultMessage);

                return signaturBiljett;
            }
        } catch (JAXBException e) {
            SignaturBiljett signaturBiljett = redisTicketTracker.updateStatus(relayState, SignaturStatus.ERROR);
            monitoringLogService
                .logSignResponseInvalid(relayState, signaturBiljett.getIntygsId(), "Could not unmarshal sign response: " + e.getMessage());
            return signaturBiljett;
        } catch (Exception e) {
            SignaturBiljett signaturBiljett = redisTicketTracker.updateStatus(relayState, SignaturStatus.ERROR);
            monitoringLogService
                .logSignResponseInvalid(relayState, signaturBiljett.getIntygsId(),
                    "Internal problem: " + e.getMessage());
            return signaturBiljett;
        }
    }

    private String findFirstCertificateInChain(List<byte[]> certificateByteArrayList) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            List<Certificate> certificateList = new ArrayList<>();
            for (var certBytes : certificateByteArrayList) {
                ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
                certificateList.add(factory.generateCertificate(bais));
            }

            CertPath cp = factory.generateCertPath(certificateList);
            return Base64.getEncoder().encodeToString(cp.getCertificates().get(0).getEncoded());
        } catch (CertificateException e) {
            e.printStackTrace(); //TODO
        }
        return "";
    }

    private SignResponse unMarshallSignResponse(String eIdSignResponse) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SignResponse.class, SignResponseExtensionType.class, SignTasksType.class);
        return (SignResponse) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(eIdSignResponse.getBytes()));
    }

    public String findReturnErrorUrl(String intygsId, String ticketId) {
        var utkastOptional = utkastRepository.findById(intygsId);

        if (utkastOptional.isPresent()) {
            var utkast = utkastOptional.get();
            var intygsTyp = utkast.getIntygsTyp();
            var intygTypeVersion = utkast.getIntygTypeVersion();

            //#/lisjp/1.1/edit/86beec75-b790-42cd-9fb9-3c9585e1bbed/
            return String.format("%s/#/%s/%s/edit/%s/?error&ticket=%s", dssClientHostUrl, intygsTyp, intygTypeVersion, intygsId, ticketId);
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Can't find certificate to return to!");
        }
    }

    public String findReturnUrl(String intygsId) {
        var utkastOptional = utkastRepository.findById(intygsId);

        if (utkastOptional.isPresent()) {
            var utkast = utkastOptional.get();
            var intygsTyp = utkast.getIntygsTyp();
            var intygTypeVersion = utkast.getIntygTypeVersion();

            //#/intyg/lisjp/1.1/4dfd56f4-7321-4dda-a35a-8df6fa942a8a/?signed
            return String.format("%s/#/intyg/%s/%s/%s/?signed", dssClientHostUrl, intygsTyp, intygTypeVersion, intygsId);
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Can't find certificate to return to!");
        }
    }

    public SignaturBiljett updateSignatureTicketWithError(String relayState) {
        return redisTicketTracker.updateStatus(relayState, SignaturStatus.ERROR);
    }
}
