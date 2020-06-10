package se.inera.intyg.webcert.web.service.underskrift.dss;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
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
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.SignatureApiController;

@Service
public class DssSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignatureService.class);

    private final DssMetadataService dssMetadataService;
    private final WebCertUserService userService;
    private final UtkastRepository utkastRepository;
    private final DssSignMessageService dssSignMessageService;

    private final UnderskriftService underskriftService;

    private final se.inera.intyg.webcert.dss.xsd.dsscore.ObjectFactory objectFactoryDssCore;
    private final se.inera.intyg.webcert.dss.xsd.dssext.ObjectFactory objectFactoryCsig;
    private final se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ObjectFactory objectFactorySaml;

    @Value("${webcert.host.url}")
    private String webcertHostUrl;

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


    @Autowired
    public DssSignatureService(DssMetadataService dssMetadataService, DssSignMessageService dssSignMessageService,
        WebCertUserService userService, UtkastRepository utkastRepository, UnderskriftService underskriftService) {
        objectFactoryDssCore = new se.inera.intyg.webcert.dss.xsd.dsscore.ObjectFactory();
        objectFactoryCsig = new se.inera.intyg.webcert.dss.xsd.dssext.ObjectFactory();
        objectFactorySaml = new se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ObjectFactory();
        this.dssMetadataService = dssMetadataService;
        this.userService = userService;
        this.utkastRepository = utkastRepository;
        this.dssSignMessageService = dssSignMessageService;
        this.underskriftService = underskriftService;
    }

    public DssSignRequestDTO createSignatureRequestDTO(SignaturBiljett sb) {
        var dateTimeNow = DateTime.now();

        var dssSignRequestDTO = new DssSignRequestDTO();
        dssSignRequestDTO.setTransactionId(createTransactionID(dateTimeNow));
        dssSignRequestDTO.setActionUrl(dssMetadataService.getDssActionUrl());

        dssSignRequestDTO.setSignRequest(dssSignMessageService.signSignRequest(createSignRequest(dateTimeNow, sb)));

        return dssSignRequestDTO;
    }

    private SignRequest createSignRequest(DateTime dateTimeNow, SignaturBiljett sb) {
        var signRequest = objectFactoryDssCore.createSignRequest();
        signRequest.setRequestID(sb.getTicketId());
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

    private String createTransactionID(DateTime dateTimeNow) {
        var timestamp = Long.toHexString(dateTimeNow.getMillis());
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
            webcertHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_METADATA_PATH);
        signRequestExtensionType.setSignRequester(signRequester);

        var signService = objectFactorySaml.createNameIDType();
        signService.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        signService.setValue(serviceUrl);
        signRequestExtensionType.setSignService(signService);

        signRequestExtensionType.setRequestedSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");

        signRequestExtensionType.setCertRequestProperties(createCertRequestProperties());

        signRequestExtensionType.setSignMessage(createSignMessage(intygsId));

        return objectFactoryCsig.createSignRequestExtension(signRequestExtensionType);
    }

    private SignMessageType createSignMessage(String intygsId) {
        var utkast = utkastRepository.findById(intygsId).orElse(null);
        String intygsTyp = "";
        Personnummer patientPersonnummer = null;

        if (utkast != null) {
            intygsTyp = utkast.getIntygsTyp();
            patientPersonnummer = utkast.getPatientPersonnummer();
        }

        SignMessageType signMessage = objectFactoryCsig.createSignMessageType();
        signMessage.setMimeType("text");
        signMessage.setMustShow(true);
        String message = this.signMessage.replace("{intygsTyp}", intygsTyp)
            .replace("{patientPnr}", patientPersonnummer != null ? patientPersonnummer.getPersonnummerWithDash() : "")
            .replace("{intygsId}", intygsId);
        signMessage.setMessage(Base64.getEncoder().encode(message.getBytes()));

        return signMessage;
    }

    private CertRequestPropertiesType createCertRequestProperties() {

        var certRequestPropertiesType = objectFactoryCsig.createCertRequestPropertiesType();
        certRequestPropertiesType.setCertType("PKC");
        certRequestPropertiesType.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI");

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
            .add(webcertHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_RESPONSE_PATH);

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
        }
        return xmlGregorianCalendar;
    }

    public SignaturBiljett receiveSignResponse(String eIdSignResponse) {
        try {
            var signResponse = unMarshallSignResponse(eIdSignResponse);

            var requestId = signResponse.getRequestID();
            var result = signResponse.getResult();

            //TODO validate result
//            result.getResultMajor();

            var signatureObject = signResponse.getSignatureObject();
            var signResponseExtension = (JAXBElement<SignResponseExtensionType>) signResponse.getOptionalOutputs().getAny().get(0);

            var signTasks = (JAXBElement<SignTasksType>) signatureObject.getOther().getAny().get(0);
            byte[] signatur = signTasks.getValue().getSignTaskData().get(0).getBase64Signature()
                .getValue(); // SignatureObject Base64Signature
            String certifikat = Arrays.toString(signResponseExtension.getValue().getSignatureCertificateChain().getX509Certificate()
                .get(0)); // SignResponseExtension SignatureCertificateChain X509Certificate

            var sb = underskriftService.netidSignature(requestId, signatur, certifikat);

            return sb;
        } catch (JAXBException e) {
            e.printStackTrace(); //TODO
        }
        return null;
    }

    private SignResponse unMarshallSignResponse(String eIdSignResponse) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SignResponse.class, SignResponseExtensionType.class, SignTasksType.class);
        return (SignResponse) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(eIdSignResponse.getBytes()));
    }
}
