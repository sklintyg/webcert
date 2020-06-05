package se.inera.intyg.webcert.web.service.underskrift.dss;

import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import oasis.names.tc.dss._1_0.core.schema.InputDocuments;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.ConditionsType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.xml.transform.StringResult;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.elegnamnden.id.csig._1_1.dss_ext.ns.CertRequestPropertiesType;
import se.elegnamnden.id.csig._1_1.dss_ext.ns.MappedAttributeType;
import se.elegnamnden.id.csig._1_1.dss_ext.ns.SignMessageType;
import se.elegnamnden.id.csig._1_1.dss_ext.ns.SignRequestExtensionType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.SignatureApiController;

@Service
public class DssSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignatureService.class);

    private final DssMetadataService dssMetadataService;
    private final WebCertUserService userService;
    private final UtkastRepository utkastRepository;

    private final oasis.names.tc.dss._1_0.core.schema.ObjectFactory objectFactoryDssCore;
    private final se.elegnamnden.id.csig._1_1.dss_ext.ns.ObjectFactory objectFactoryCsig;
    private final oasis.names.tc.saml._2_0.assertion.ObjectFactory objectFactorySaml;

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
    public DssSignatureService(DssMetadataService dssMetadataService, WebCertUserService userService, UtkastRepository utkastRepository) {
        objectFactoryDssCore = new oasis.names.tc.dss._1_0.core.schema.ObjectFactory();
        objectFactoryCsig = new se.elegnamnden.id.csig._1_1.dss_ext.ns.ObjectFactory();
        objectFactorySaml = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();
        this.dssMetadataService = dssMetadataService;
        this.userService = userService;
        this.utkastRepository = utkastRepository;
    }

    public DssSignRequestDTO createSignatureRequestDTO(SignaturBiljett sb) {
        var dateTimeNow = DateTime.now();

        var dssSignRequestDTO = new DssSignRequestDTO();
        dssSignRequestDTO.setTransactionId(createTransactionID(dateTimeNow));
        dssSignRequestDTO.setActionUrl(dssMetadataService.getDssActionUrl());

        var signRequest = objectFactoryDssCore.createSignRequest();
        signRequest.setRequestID(generateUUID());
        signRequest.setProfile("http://id.elegnamnden.se/csig/1.1/dss-ext/profile");
        signRequest.setInputDocuments(createInputDocuments(sb));

        var optionalInputs = objectFactoryDssCore.createAnyType();
        optionalInputs.getAny().add(createSignRequestExtension(sb.getIntygsId(), dateTimeNow));
        optionalInputs.getAny().add(createSignature());

        signRequest.setOptionalInputs(optionalInputs);

        Jaxb2Marshaller marshaller = getJaxb2Marshaller();

        var stringResult = new StringResult();
        marshaller.marshal(signRequest, stringResult);

        dssSignRequestDTO.setSignRequest(stringResult.toString());
        return dssSignRequestDTO;
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

    private JAXBElement<SignatureType> createSignature() {
        org.w3._2000._09.xmldsig_.ObjectFactory of = new org.w3._2000._09.xmldsig_.ObjectFactory();
        return of.createSignature(of.createSignatureType()); //TODO Signature
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

    private Jaxb2Marshaller getJaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        String[] packages = {"oasis.names.tc", "org.w3._2000._09.xmldsig_", "org.w3._2001._04.xmlenc_", "se.elegnamnden.id.csig"};
        marshaller.setPackagesToScan(packages);

        marshaller.setMarshallerProperties(new HashMap<String, Object>() {
            {
                put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
                put(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
            }
        });
        return marshaller;
    }
}
