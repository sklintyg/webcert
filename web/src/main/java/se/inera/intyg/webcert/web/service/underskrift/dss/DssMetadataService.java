/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport.getMarshallerFactory;
import static org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport.getParserPool;
import static se.inera.intyg.webcert.web.web.controller.api.SignatureApiController.SIGNATUR_API_CONTEXT_PATH;
import static se.inera.intyg.webcert.web.web.controller.api.SignatureApiController.SIGN_SERVICE_METADATA_PATH;
import static se.inera.intyg.webcert.web.web.controller.api.SignatureApiController.SIGN_SERVICE_RESPONSE_PATH;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.Timer;
import javax.xml.crypto.KeySelector;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml.saml2.metadata.impl.CompanyBuilder;
import org.opensaml.saml.saml2.metadata.impl.ContactPersonBuilder;
import org.opensaml.saml.saml2.metadata.impl.EmailAddressBuilder;
import org.opensaml.saml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.KeyDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml.saml2.metadata.impl.SPSSODescriptorBuilder;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.impl.KeyInfoBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.X509CertificateBuilder;
import org.opensaml.xmlsec.signature.impl.X509DataBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * This service initializes metadata needed in the DSS (Digital Signing Service) integration
 * It depends on OpenSaml and that its bootstrapping is done prior to the instantiation of this service,
 * which in Webcert is done in the SAML configuration.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DssMetadataService {

    @Value("${dss.client.metadata.host.url}")
    private String dssClientEntityHostUrl;
    @Value("${dss.client.response.host.url}")
    private String dssClientResponseHostUrl;
    @Value("${dss.service.metadata.resource}")
    private Resource dssServiceMetadataResource;
    @Value("${dss.service.metadata.entityid}")
    private String dssServiceMetadataEntityId;
    @Value("${dss.client.keystore.alias}")
    private String keystoreAlias;
    @Value("${dss.service.action.url}")
    private String actionUrlProperty;
    @Value("${dss.client.keystore.password}")
    private String keystorePassword;
    @Value("${dss.client.keystore.file}")
    private Resource keystoreFile;
    @Value("${dss.client.metadata.org.name}")
    private String organizationName;
    @Value("${dss.client.metadata.org.displayname}")
    private String organizationDisplayName;
    @Value("${dss.client.metadata.org.url}")
    private String organizationUrl;
    @Value("${dss.client.metadata.org.email}")
    private String organizationEmail;

    @Getter
    private KeyStore dssKeyStore;
    @Getter
    private KeySelector dssKeySelector;

    private AbstractReloadingMetadataResolver dssServiceMetadataProvider;
    private EntityDescriptor clientEntityDescriptor;

    private static final String LANG = "sv";

    @PostConstruct
    void initialize() {
        initDssMetadata();
        initClientMetadata();
    }

    public String getClientMetadataAsString() {
        try {
            final var metadata = new StreamResult(new StringWriter());
            final var marshaller = Objects.requireNonNull(getMarshallerFactory().getMarshaller(clientEntityDescriptor));
            final var metadataXml = marshaller.marshall(clientEntityDescriptor);
            Signer.signObject(Objects.requireNonNull(clientEntityDescriptor.getSignature()));
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(metadataXml), metadata);
            return metadata.getWriter().toString();

        } catch (Exception e) {
            log.error("Unable to get DSS Client metadata");
            throw new IllegalStateException("Failure parsing DSS Client metadata.", e);
        }
    }

    protected void initDssMetadata() {
        try {
            dssServiceMetadataProvider = new SpringResourceBackedMetadataProvider(new Timer(true), dssServiceMetadataResource);
            dssServiceMetadataProvider.setParserPool(Objects.requireNonNull(getParserPool()));
            dssServiceMetadataProvider.setRequireValidMetadata(true);
            dssServiceMetadataProvider.setId("dssMetadata");
            dssServiceMetadataProvider.initialize();

            initDssKeyStore(getDssSpSsoDescriptor());

        } catch (ComponentInitializationException e) {
            log.error("Unable to load DSS metadata from resource: {}", dssServiceMetadataResource);
            throw new IllegalStateException(e);
        }
    }

    protected void initClientMetadata() {
        try {
            final var keyStoreSiths = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStoreSiths.load(keystoreFile.getInputStream(), keystorePassword.toCharArray());

            final var privateKey = (PrivateKey) keyStoreSiths.getKey(keystoreAlias, keystorePassword.toCharArray());
            final var certificate = (X509Certificate) keyStoreSiths.getCertificate(keystoreAlias);

            final var assertionCOnsumerService = dssClientResponseHostUrl + SIGNATUR_API_CONTEXT_PATH + SIGN_SERVICE_RESPONSE_PATH;
            final var spssoDescriptor = new SPSSODescriptorBuilder().buildObject();
            spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
            spssoDescriptor.setAuthnRequestsSigned(false);
            spssoDescriptor.setWantAssertionsSigned(false);
            spssoDescriptor.getAssertionConsumerServices().add(createAssertionConsumerService(assertionCOnsumerService));
            spssoDescriptor.getKeyDescriptors().add(getKeyDescriptor(UsageType.SIGNING, certificate));
            spssoDescriptor.getKeyDescriptors().add(getKeyDescriptor(UsageType.ENCRYPTION, certificate));

            final var credential = new BasicX509Credential(certificate);
            credential.setPrivateKey(privateKey);

            final var signature = new SignatureBuilder().buildObject();
            signature.setSigningCredential(credential);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setKeyInfo(getKeyInfo(certificate));

            final var entityId = dssClientEntityHostUrl + SIGNATUR_API_CONTEXT_PATH + SIGN_SERVICE_METADATA_PATH;
            clientEntityDescriptor = new EntityDescriptorBuilder().buildObject();
            clientEntityDescriptor.setEntityID(entityId);
            clientEntityDescriptor.setID(entityId.replaceAll("[:/]", "_"));
            clientEntityDescriptor.setOrganization(createOrganization());
            clientEntityDescriptor.getContactPersons().add(createContactPerson(ContactPersonTypeEnumeration.SUPPORT));
            clientEntityDescriptor.getContactPersons().add(createContactPerson(ContactPersonTypeEnumeration.TECHNICAL));
            clientEntityDescriptor.getRoleDescriptors().add(spssoDescriptor);
            clientEntityDescriptor.setSignature(signature);

        } catch (Exception e) {
            throw new IllegalStateException("Failure initializing DSS Client metadata.", e);
        }
    }

    private KeyDescriptor getKeyDescriptor(UsageType usageType, X509Certificate certificate) throws CertificateEncodingException {
        final var keyDescriptor = new KeyDescriptorBuilder().buildObject();
        keyDescriptor.setUse(usageType);
        keyDescriptor.setKeyInfo(getKeyInfo(certificate));
        return keyDescriptor;
    }

    private KeyInfo getKeyInfo(X509Certificate certificate) throws CertificateEncodingException {
        final var keyInfo = new KeyInfoBuilder().buildObject();
        final var x509Data = new X509DataBuilder().buildObject();
        final var x509Certificate = new X509CertificateBuilder().buildObject();
        keyInfo.getX509Datas().add(x509Data);
        x509Data.getX509Certificates().add(x509Certificate);
        x509Certificate.setValue(Base64.getEncoder().encodeToString(certificate.getEncoded()));
        return keyInfo;
    }

    private SPSSODescriptor getDssSpSsoDescriptor() {
        try {
            final var entityIdCriteriaSet = new CriteriaSet();
            entityIdCriteriaSet.add(new EntityIdCriterion(dssServiceMetadataEntityId));
            final var entityDescriptor = dssServiceMetadataProvider.resolveSingle(entityIdCriteriaSet);
            return Objects.requireNonNull(entityDescriptor).getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        } catch (Exception e) {
            log.error("Unable to get DSS SpSsoDescriptor with entityId: {}", dssServiceMetadataEntityId);
            throw new IllegalStateException(e);
        }
    }

    private AssertionConsumerService createAssertionConsumerService(String location) {
        final var assertionConsumerService = new AssertionConsumerServiceBuilder().buildObject();
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        assertionConsumerService.setLocation(location);
        assertionConsumerService.setIsDefault(true);
        assertionConsumerService.setIndex(0);

        return assertionConsumerService;
    }

    private Organization createOrganization() {
        final var xmlOrganizationURL = new OrganizationURLBuilder().buildObject();
        xmlOrganizationURL.setXMLLang(LANG);
        xmlOrganizationURL.setURI(organizationUrl);

        final var organization = new OrganizationBuilder().buildObject();
        organization.getURLs().add(xmlOrganizationURL);

        final var xmlOrganizationDisplayName = new OrganizationDisplayNameBuilder().buildObject();
        xmlOrganizationDisplayName.setXMLLang(LANG);
        xmlOrganizationDisplayName.setValue(organizationDisplayName);
        organization.getDisplayNames().add(xmlOrganizationDisplayName);

        final var xmlOrganizationName = new OrganizationNameBuilder().buildObject();
        xmlOrganizationName.setXMLLang(LANG);
        xmlOrganizationName.setValue(organizationName);
        organization.getOrganizationNames().add(xmlOrganizationName);
        return organization;
    }

    private ContactPerson createContactPerson(ContactPersonTypeEnumeration type) {
        final var emailAddress = new EmailAddressBuilder().buildObject();
        emailAddress.setURI(organizationEmail);

        final var company = new CompanyBuilder().buildObject();
        company.setValue(organizationName);

        final var contactPerson = new ContactPersonBuilder().buildObject();
        contactPerson.setType(type);
        contactPerson.setCompany(company);
        contactPerson.getEmailAddresses().add(emailAddress);
        return contactPerson;
    }

    /**
     * Extracts the URL to use in SignRequest form. This URL should be used in
     * the POST action of the form.
     * <p>
     * The source of this URL could either come from property or metadata
     *
     * @return URL for posting SignRequst form
     */
    public String getDssActionUrl() {

        if (StringUtils.hasText(actionUrlProperty)) {
            log.debug("Using property for actionURL");
            return actionUrlProperty;
        } else {
            log.debug("Using AssertionConsumerService from metadata for actionURL");
            SPSSODescriptor dssSpSsoDescriptor = getDssSpSsoDescriptor();
            AssertionConsumerService assertionConsumerService = dssSpSsoDescriptor.getDefaultAssertionConsumerService();
            return assertionConsumerService.getLocation();
        }
    }

    private void initDssKeyStore(SPSSODescriptor spSSODescriptor) {

        int aliasNumber = 0;

        try {
            dssKeyStore = KeyStore.getInstance("JKS");
            char[] pwdArray = keystorePassword.toCharArray();
            dssKeyStore.load(null, pwdArray);

            final var keyDescriptors = Objects.requireNonNull(spSSODescriptor).getKeyDescriptors();
            for (final var keyDescriptor : Objects.requireNonNull(keyDescriptors)) {
                if (!UsageType.SIGNING.equals(keyDescriptor.getUse()) && !UsageType.UNSPECIFIED.equals(keyDescriptor.getUse())) {
                    continue;
                }
                final var keyInfo = keyDescriptor.getKeyInfo();
                final var x509Datas = Objects.requireNonNull(keyInfo).getX509Datas();
                for (final var x509Data : x509Datas) {
                    final var x509Certificates = Objects.requireNonNull(x509Data).getX509Certificates();
                    for (final var x509Certificate : x509Certificates) {
                        final var cf = CertificateFactory.getInstance("X.509");
                        final var decodedX509Cert = Base64.getMimeDecoder().decode(Objects.requireNonNull(x509Certificate).getValue());
                        final var inputStream = new ByteArrayInputStream(decodedX509Cert);
                        final var base64Certificate = (X509Certificate) cf.generateCertificate(inputStream);
                        dssKeyStore.setCertificateEntry("dss" + aliasNumber, base64Certificate);
                        aliasNumber++;
                    }
                }
            }

            if (dssKeyStore.size() == 0) {
                log.error("No valid certificates found in dss metadata: {}", dssServiceMetadataResource);
                throw new IllegalStateException("Failure setting upp DSS Key store.");
            }

            dssKeySelector = new KeyStoreKeySelector(dssKeyStore);

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | NullPointerException e) {
            log.error("Unable to load DSS kyeStore from metadata resource: {}", dssServiceMetadataResource);
            throw new IllegalStateException("Failure setting up DSSKey store", e);
        }
    }

}
