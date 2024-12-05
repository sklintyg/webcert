/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.underskrift.dss;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import se.inera.intyg.webcert.dss.xsd.dssext.RequestedAttributesType;
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
    public static final String RESULTMAJOR_SUCCESS = "urn:oasis:names:tc:dss:1.0:resultmajor:Success";
    public static final String REQUESTED_SIGN_ALGORITHM = XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256;
    private static final String ACTIVATE_SUPPORT_FOR_SEVERAL_LOA_AND_AUTH_PROFILE = "1.4";
    private static final String AUTHN_PROFILE = "digg_ap_hsaid_01";
    private static final String NAMEID_FORMAT_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    private static final String REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME = "givenName";
    private static final String REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME_REF = "2.5.4.42";
    private static final String REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME_SAML_NAME = "http://sambi.se/attributes/1/givenName";

    private static final String REQUESTED_CERT_ATTRIBUTE_SURNAME = "sn";
    private static final String REQUESTED_CERT_ATTRIBUTE_SURNAME_REF = "2.5.4.4";
    private static final String REQUESTED_CERT_ATTRIBUTE_SURNAME_SAML_NAME = "http://sambi.se/attributes/1/surname";

    private static final String REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER = "serialNumber";
    private static final String REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER_REF = "2.5.4.5";
    private static final String REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER_SAML_NAME = "http://sambi.se/attributes/1/employeeHsaId";

    private static final String REQUESTED_CERT_ATTRIBUTE_COMMON_NAME = "commonName";
    private static final String REQUESTED_CERT_ATTRIBUTE_COMMON_NAME_REF = "2.5.4.3";
    private static final String REQUESTED_CERT_ATTRIBUTE_COMMON_NAME_SAML_NAME = "urn:name";

    private final DssMetadataService dssMetadataService;
    private final WebCertUserService userService;
    private final UtkastRepository utkastRepository;
    private final DssSignMessageService dssSignMessageService;
    private final DssSignMessageIdpProvider dssSignMessageIdpProvider;
    private final UnderskriftService underskriftService;
    private final RedisTicketTracker redisTicketTracker;
    private final MonitoringLogService monitoringLogService;
    private final IntygModuleRegistry moduleRegistry;

    private final se.inera.intyg.webcert.dss.xsd.dsscore.ObjectFactory objectFactoryDssCore;
    private final se.inera.intyg.webcert.dss.xsd.dssext.ObjectFactory objectFactoryCsig;
    private final se.inera.intyg.webcert.dss.xsd.samlassertion.v2.ObjectFactory objectFactorySaml;

    @Value("${dss.client.metadata.host.url}")
    private String dssClientEntityHostUrl;

    @Value("${dss.client.response.host.url}")
    private String dssClientResponseHostUrl;

    @Value("${dss.service.clientid}")
    private String customerId;

    @Value("${dss.service.applicationid}")
    private String applicationId;

    @Value("${dss.service.serviceurl}")
    private String serviceUrl;

    @Value("${dss.service.signmessage}")
    private String signMessage;

    @Value("#{${dss.client.approved.loa:}}")
    private List<String> approvedLoaList;

    @Value("${dss.client.ie.unit.whitelist:}")
    private String dssUnitWhitelistForIeProperty;
    private final List<String> dssUnitWhitelistForIe = new ArrayList<>();

    @Value("${dss.service.validity.request.time.in.minutes}")
    private int signRequestValidityInMinutes;

    @Autowired
    public DssSignatureService(DssMetadataService dssMetadataService, DssSignMessageService dssSignMessageService,
        WebCertUserService userService, UtkastRepository utkastRepository, DssSignMessageIdpProvider dssSignMessageIdpProvider,
        @Qualifier("signAggregator") UnderskriftService underskriftService,
        RedisTicketTracker redisTicketTracker, MonitoringLogService monitoringLogService, IntygModuleRegistry moduleRegistry) {
        this.dssSignMessageIdpProvider = dssSignMessageIdpProvider;
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

    public boolean shouldUseSigningService(String currentCareUnitHsaId) {
        if (currentCareUnitHsaId == null || currentCareUnitHsaId.isBlank()) {
            return true;
        }

        return dssUnitWhitelistForIe.stream()
            .filter(value -> !value.isBlank())
            .noneMatch(value -> {
                    if (value.equals("*")) {
                        return true;
                    }
                    if (value.endsWith("*")) {
                        final var wildcardRemovedSubstring = value.substring(0, value.lastIndexOf("*"));
                        return currentCareUnitHsaId.toUpperCase().startsWith(wildcardRemovedSubstring.toUpperCase());
                    } else {
                        return currentCareUnitHsaId.equalsIgnoreCase(value);
                    }
                }
            );
    }

    public DssSignRequestDTO createSignatureRequestDTO(SignaturBiljett sb) {
        var dateTimeNow = DateTime.now();

        var dssSignRequestDTO = new DssSignRequestDTO();
        String transactionID = sb.getTicketId();
        dssSignRequestDTO.setTransactionId(transactionID);
        dssSignRequestDTO.setActionUrl(dssMetadataService.getDssActionUrl());

        String signRequest = dssSignMessageService.signSignRequest(createSignRequest(dateTimeNow, sb, transactionID));
        String base64EncodedSignRequest = Base64.getEncoder().encodeToString(signRequest.getBytes(StandardCharsets.UTF_8));
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
        return idStr.substring(0, Math.min(idStr.length(), 12)).toLowerCase();
    }

    private InputDocuments createInputDocuments(SignaturBiljett sb) {
        var signTaskDataType = objectFactoryCsig.createSignTaskDataType();
        signTaskDataType.setSigType("XML");
        signTaskDataType.setSignTaskId(generateUUID());
        signTaskDataType.setToBeSignedBytes(sb.getHash().getBytes(StandardCharsets.UTF_8));

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

        signRequestExtensionType.setAuthnProfile(AUTHN_PROFILE);

        final var idpUrl = dssSignMessageIdpProvider.get(
            userService.getUser().getIdentityProviderForSign()
        );

        var identityProvider = objectFactorySaml.createNameIDType();
        identityProvider.setFormat(NAMEID_FORMAT_ENTITY);
        identityProvider.setValue(idpUrl);
        signRequestExtensionType.setIdentityProvider(identityProvider);

        var signRequester = objectFactorySaml.createNameIDType();
        signRequester.setFormat(NAMEID_FORMAT_ENTITY);
        signRequester.setValue(
            dssClientEntityHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_METADATA_PATH);
        signRequestExtensionType.setSignRequester(signRequester);

        var signService = objectFactorySaml.createNameIDType();
        signService.setFormat(NAMEID_FORMAT_ENTITY);
        signService.setValue(serviceUrl);
        signRequestExtensionType.setSignService(signService);

        signRequestExtensionType.setRequestedSignatureAlgorithm(REQUESTED_SIGN_ALGORITHM);

        signRequestExtensionType.setCertRequestProperties(createCertRequestProperties());

        signRequestExtensionType.setSignMessage(createSignMessage(intygsId, idpUrl));

        signRequestExtensionType.setVersion(ACTIVATE_SUPPORT_FOR_SEVERAL_LOA_AND_AUTH_PROFILE);

        return objectFactoryCsig.createSignRequestExtension(signRequestExtensionType);
    }

    private SignMessageType createSignMessage(String intygsId, String idpUrl) {
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
        signMessage.setMustShow(true);
        signMessage.setDisplayEntity(idpUrl);
        String message = this.signMessage.replace("{intygsTyp}", intygsTyp)
            .replace("{patientPnr}", patientPersonnummer != null ? patientPersonnummer.getPersonnummerWithDash() : "")
            .replace("{intygsId}", intygsId);
        signMessage.setMessage(message.getBytes(StandardCharsets.UTF_8));

        return signMessage;
    }

    private CertRequestPropertiesType createCertRequestProperties() {

        var certRequestPropertiesType = objectFactoryCsig.createCertRequestPropertiesType();
        certRequestPropertiesType.setCertType("PKC");
        var loaList = certRequestPropertiesType.getAuthnContextClassRef();
        loaList.addAll(approvedLoaList);

        var requestedAttributesType = objectFactoryCsig.createRequestedAttributesType();

        addMappedAttribute(requestedAttributesType, REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME_REF, REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME,
            true, REQUESTED_CERT_ATTRIBUTE_GIVEN_NAME_SAML_NAME);
        addMappedAttribute(requestedAttributesType, REQUESTED_CERT_ATTRIBUTE_SURNAME_REF, REQUESTED_CERT_ATTRIBUTE_SURNAME,
            true, REQUESTED_CERT_ATTRIBUTE_SURNAME_SAML_NAME);
        addMappedAttribute(requestedAttributesType, REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER_REF, REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER,
            true, REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER_SAML_NAME);
        addMappedAttribute(requestedAttributesType, REQUESTED_CERT_ATTRIBUTE_COMMON_NAME_REF, REQUESTED_CERT_ATTRIBUTE_COMMON_NAME,
            true, REQUESTED_CERT_ATTRIBUTE_COMMON_NAME_SAML_NAME);

        certRequestPropertiesType.setRequestedCertAttributes(requestedAttributesType);
        return certRequestPropertiesType;
    }

    private void addMappedAttribute(RequestedAttributesType requestedAttributesType, String ref, String name, boolean req,
        String attribute) {
        requestedAttributesType.getRequestedCertAttribute().add(createMappedAttributeType(ref, name, req, attribute));
    }

    private MappedAttributeType createMappedAttributeType(String ref, String name, boolean req, String attribute) {
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
        attributeType.setName(REQUESTED_CERT_ATTRIBUTE_SERIAL_NUMBER_SAML_NAME);
        attributeType.getAttributeValue().add(user.getHsaId());

        var attributeStatementType = objectFactorySaml.createAttributeStatementType();
        attributeStatementType.getAttributeOrEncryptedAttribute().add(attributeType);
        return attributeStatementType;
    }

    private ConditionsType createConditions(DateTime dateTimeNow) {
        var conditionsType = objectFactorySaml.createConditionsType();

        var beforeTime = dateTimeNow.minusMinutes(2);
        conditionsType.setNotBefore(convertToXmlGregorianTime(beforeTime));

        var afterTime = dateTimeNow.plusMinutes(signRequestValidityInMinutes);
        conditionsType.setNotOnOrAfter(convertToXmlGregorianTime(afterTime));

        var audienceRestrictionType = objectFactorySaml.createAudienceRestrictionType();
        audienceRestrictionType.getAudience()
            .add(dssClientResponseHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH
                + SignatureApiController.SIGN_SERVICE_RESPONSE_PATH);

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
            LOG.warn("Unexpected error occurred when handling sign response with transaction id '%s'".formatted(relayState), e);
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
        return (SignResponse) context.createUnmarshaller()
            .unmarshal(new ByteArrayInputStream(eIdSignResponse.getBytes(StandardCharsets.UTF_8)));
    }

    public String findReturnErrorUrl(String intygsId, String ticketId) {
        var utkastOptional = utkastRepository.findById(intygsId);

        if (utkastOptional.isPresent()) {
            var utkast = utkastOptional.get();
            var intygsTyp = utkast.getIntygsTyp();
            var intygTypeVersion = utkast.getIntygTypeVersion();

            //#/lisjp/1.1/edit/86beec75-b790-42cd-9fb9-3c9585e1bbed/
            return String
                .format("%s/#/%s/%s/edit/%s/?error&ticket=%s", dssClientResponseHostUrl, intygsTyp, intygTypeVersion, intygsId, ticketId);
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
            return String.format("%s/#/intyg/%s/%s/%s/?signed", dssClientResponseHostUrl, intygsTyp, intygTypeVersion, intygsId);
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Can't find certificate to return to!");
        }
    }

    public SignaturBiljett updateSignatureTicketWithError(String relayState) {
        return redisTicketTracker.updateStatus(relayState, SignaturStatus.ERROR);
    }
}