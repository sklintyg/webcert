/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import javax.annotation.PostConstruct;
import javax.xml.crypto.KeySelector;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Company;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml2.metadata.EmailAddress;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.OrganizationURL;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml2.metadata.impl.CompanyBuilder;
import org.opensaml.saml2.metadata.impl.ContactPersonBuilder;
import org.opensaml.saml2.metadata.impl.EmailAddressBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.UsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.inera.intyg.webcert.web.web.controller.api.SignatureApiController;

/**
 * This service initializes metadata needed in the DSS (Digital Signing Service) integration
 * It depends on OpenSaml and that its bootstrapping is done prior to the instantiation of this service,
 * which in Webcert is done in the SAML configuration.
 */
@Service
public class DssMetadataService {

    private static final Logger LOG = LoggerFactory.getLogger(DssMetadataService.class);
    private static final String LANG = "sv";

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

    private ParserPool parserPool;
    private AbstractReloadingMetadataProvider dssServiceMetadataProvider;
    private EntityDescriptor clientEntityDescriptor;
    private ExtendedMetadata clientExtendedMetadata;
    private JKSKeyManager clientKeyManager;
    private KeyStore dssKeyStore;
    private KeySelector dssKeySelector;

    @Autowired
    public DssMetadataService(ParserPool parserPool) {
        this.parserPool = parserPool;
    }

    @PostConstruct
    void initialize() {
        initDssMetadata();
        initClientMetadata();

    }


    /**
     * Extracts the URL to use in SignRequest form. This URL should be used in
     * the POST action of the form.
     *
     * The source of this URL could either come from property or metadata
     *
     * @return URL for posting SignRequst form
     */
    public String getDssActionUrl() {

        if (StringUtils.hasText(actionUrlProperty)) {
            LOG.debug("Using property for actionURL");
            return actionUrlProperty;
        } else {
            LOG.debug("Using AssertionConsumerService from metadata for actionURL");
            SPSSODescriptor dssSpSsoDescriptor = getDssSpSsoDescriptor();
            AssertionConsumerService assertionConsumerService = dssSpSsoDescriptor.getDefaultAssertionConsumerService();
            return assertionConsumerService.getLocation();
        }
    }

    /**
     * Creates metadata for this dss client from the configured values.
     *
     * @return Client metadata as a String
     */
    public String getClientMetadataAsString() {
        try {
            return SAMLUtil.getMetadataAsString(null, clientKeyManager, clientEntityDescriptor, clientExtendedMetadata);
        } catch (MarshallingException exception) {
            LOG.error("Unable to get DSS Client metadata");
            throw new RuntimeException(exception);
        }
    }

    /**
     * Get a KeyStore created from the x509Certificates from the
     * DSS metadata.
     *
     * @return A KeyStore with valid certificates for the DSS
     */
    public KeyStore getDssKeyStore() {
        return dssKeyStore;
    }

    /**
     * Get the KeySelector that maps the DSS KeyStore.
     *
     * @return A KeySelector with valid certificates for the DSS
     */
    public KeySelector getDssKeySelector() {
        return dssKeySelector;
    }

    protected void initDssMetadata() {
        try {

            dssServiceMetadataProvider = new SpringResourceBackedMetadataProvider(new Timer(true), dssServiceMetadataResource);
            dssServiceMetadataProvider.setParserPool(parserPool);
            dssServiceMetadataProvider.setRequireValidMetadata(true);
            dssServiceMetadataProvider.initialize();

            initDssKeyStore(getDssSpSsoDescriptor());

        } catch (MetadataProviderException exception) {
            LOG.error("Unable to load DSS metadata from resource: " + dssServiceMetadataResource.toString());
            throw new RuntimeException(exception);
        }
    }

    protected void initClientMetadata() {
        Map<String, String> map = new HashMap<>();
        map.put(keystoreAlias, keystorePassword);
        this.clientKeyManager = new JKSKeyManager(keystoreFile, keystorePassword, map, keystoreAlias);

        clientExtendedMetadata = new ExtendedMetadata();
        clientExtendedMetadata.setSigningKey(keystoreAlias);
        clientExtendedMetadata.setSignMetadata(true);
        clientExtendedMetadata.setKeyInfoGeneratorName(""); // This will use the default one
        clientExtendedMetadata.setLocal(true);

        MetadataGenerator mdg = new MetadataGenerator();
        mdg.setEntityId(
            dssClientEntityHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH + SignatureApiController.SIGN_SERVICE_METADATA_PATH);
        mdg.setEntityBaseURL(dssClientResponseHostUrl);
        mdg.setRequestSigned(false);
        mdg.setWantAssertionSigned(false);
        mdg.setBindingsHoKSSO(null);
        mdg.setBindingsSLO(null);
        mdg.setBindingsSSO(null); // Will add our own AssertionConsumingService later
        mdg.setKeyManager(clientKeyManager);
        mdg.setNameID(Collections.emptyList());
        mdg.setExtendedMetadata(clientExtendedMetadata);

        clientEntityDescriptor = mdg.generateMetadata();

        SPSSODescriptor spssoDescriptor = clientEntityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        spssoDescriptor.getAssertionConsumerServices()
            .add(createAssertionConsumerService(
                dssClientResponseHostUrl + SignatureApiController.SIGNATUR_API_CONTEXT_PATH
                    + SignatureApiController.SIGN_SERVICE_RESPONSE_PATH));

        clientEntityDescriptor.getContactPersons()
            .add(createContactPerson(ContactPersonTypeEnumeration.SUPPORT));
        clientEntityDescriptor.getContactPersons()
            .add(createContactPerson(ContactPersonTypeEnumeration.TECHNICAL));

        clientEntityDescriptor.setOrganization(createOrganization());

    }

    private SPSSODescriptor getDssSpSsoDescriptor() {
        try {
            return dssServiceMetadataProvider
                .getEntityDescriptor(dssServiceMetadataEntityId)
                .getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        } catch (Exception exception) {
            LOG.error("Unable to get DSS SpSsoDescriptor with entityId: " + dssServiceMetadataEntityId);
            throw new RuntimeException(exception);
        }
    }


    private AssertionConsumerService createAssertionConsumerService(String location) {
        AssertionConsumerService assertionConsumerService = new AssertionConsumerServiceBuilder().buildObject();
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        assertionConsumerService.setLocation(location);
        assertionConsumerService.setIsDefault(true);
        assertionConsumerService.setIndex(0);

        return assertionConsumerService;
    }

    private Organization createOrganization() {
        OrganizationURL xmlOrganizationURL = new OrganizationURLBuilder().buildObject();
        xmlOrganizationURL.setURL(new LocalizedString(organizationUrl, LANG));
        Organization organization = new OrganizationBuilder().buildObject();
        organization.getURLs().add(xmlOrganizationURL);
        OrganizationDisplayName xmlOrganizationDisplayName = new OrganizationDisplayNameBuilder().buildObject();
        xmlOrganizationDisplayName.setName(new LocalizedString(organizationDisplayName, LANG));
        organization.getDisplayNames().add(xmlOrganizationDisplayName);
        OrganizationName xmlOrganizationName = new OrganizationNameBuilder().buildObject();
        xmlOrganizationName.setName(new LocalizedString(organizationName, LANG));
        organization.getOrganizationNames().add(xmlOrganizationName);
        return organization;
    }

    private ContactPerson createContactPerson(ContactPersonTypeEnumeration type) {
        EmailAddress emailAddress = new EmailAddressBuilder().buildObject();
        emailAddress.setAddress(organizationEmail);
        Company company = new CompanyBuilder().buildObject();
        company.setName(organizationName);
        ContactPerson contactPerson = new ContactPersonBuilder().buildObject();
        contactPerson.setType(type);
        contactPerson.setCompany(company);
        return contactPerson;
    }

    private void initDssKeyStore(SPSSODescriptor spSSODescriptor) {

        int aliasNumber = 0;

        try {
            dssKeyStore = KeyStore.getInstance("JKS");
            char[] pwdArray = keystorePassword.toCharArray();
            dssKeyStore.load(null, pwdArray);

            if (spSSODescriptor != null) {
                var keyDescriptors = spSSODescriptor.getKeyDescriptors();
                if (keyDescriptors != null) {
                    for (var keyDescriptor : keyDescriptors) {
                        if (UsageType.SIGNING.equals(keyDescriptor.getUse()) || UsageType.UNSPECIFIED.equals(keyDescriptor.getUse())) {
                            var keyInfo = keyDescriptor.getKeyInfo();
                            if (keyInfo != null) {
                                var x509Datas = keyInfo.getX509Datas();
                                if (x509Datas != null) {
                                    for (var x509Data : x509Datas) {
                                        var x509Certificates = x509Data.getX509Certificates();
                                        if (x509Certificates != null) {
                                            for (var x509Certificate : x509Certificates) {
                                                var base64Certificate = SecurityHelper.buildJavaX509Cert(x509Certificate.getValue());
                                                dssKeyStore.setCertificateEntry("dss" + aliasNumber, base64Certificate);
                                                aliasNumber++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (dssKeyStore.size() == 0) {
                LOG.error("No valid certificates found in dss metadata: " + dssServiceMetadataResource.toString());
                throw new RuntimeException();
            }

            dssKeySelector = new KeyStoreKeySelector(dssKeyStore);

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exception) {
            LOG.error("Unable to load DSS kyeStore from metadata resource: " + dssServiceMetadataResource.toString());
            throw new RuntimeException(exception);
        }
    }

}
