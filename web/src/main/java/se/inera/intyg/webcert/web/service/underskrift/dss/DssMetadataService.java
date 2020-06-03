/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.io.File;
import java.util.List;
import javax.annotation.PostConstruct;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.signature.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This service initializes metadata needed in the DSS (Digital Signing Service) integration
 * It depends on OpenSaml and that its bootstrapping is done prior to the instantiation of this service,
 * which in Webcert is done in the SAML configuration.
 */
@Service
public class DssMetadataService {

    private static final Logger LOG = LoggerFactory.getLogger(DssMetadataService.class);


    private String dssServiceMetadataPath;
    private String dssServiceMetadataEntityId;
    private ParserPool parserPool;
    private FilesystemMetadataProvider dssServiceMetadata;

    @Autowired
    public DssMetadataService(ParserPool parserPool) {
        this.parserPool = parserPool;
    }

    @Autowired
    public void setDssServiceMetadataPath(@Value("${dss.service.metadata.path}") String dssServiceMetadataPath) {
        this.dssServiceMetadataPath = dssServiceMetadataPath;
    }

    @Autowired
    public void setDssServiceMetadataEntityId(@Value("${dss.service.metadata.entityid}") String dssServiceMetadataEntityId) {
        this.dssServiceMetadataEntityId = dssServiceMetadataEntityId;
    }


    @PostConstruct
    void initialize() {
        try {
            this.dssServiceMetadata = initDssMetadata(this.dssServiceMetadataPath);
        } catch (MetadataProviderException exception) {
            LOG.error("Unable to load DSS metadata with path: " + this.dssServiceMetadataPath);
            throw new RuntimeException(exception);
        }

    }

    private FilesystemMetadataProvider initDssMetadata(String dssServiceMetadataPath)
        throws MetadataProviderException {
        File dssMetadataFile = new File(dssServiceMetadataPath);
        FilesystemMetadataProvider metadataProvider = new FilesystemMetadataProvider(dssMetadataFile);
        metadataProvider.setParserPool(this.parserPool);

        metadataProvider.initialize();

        return metadataProvider;
    }

    private SPSSODescriptor getDssSpSsoDescriptor() {
        EntityDescriptor entityDescriptor = null;
        try {
            entityDescriptor = this.dssServiceMetadata.getEntityDescriptor(this.dssServiceMetadataEntityId);
        } catch (MetadataProviderException e) {
            // TODO
            e.printStackTrace();
        }

        return entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
    }

    // Get POST address for the form (actionURL)
    public String getDssActionUrl() {

        SPSSODescriptor dssSpSsoDescriptor = getDssSpSsoDescriptor();

        AssertionConsumerService assertionConsumerService = dssSpSsoDescriptor.getDefaultAssertionConsumerService();

        // TODO Check binding. Should be HTTP-Post

        return assertionConsumerService.getLocation();
    }

    // Get DSS certificate to use in validation of SigningResponse
    public List<X509Certificate> getDssCertificate() {
        SPSSODescriptor dssSpSsoDescriptor = getDssSpSsoDescriptor();

        // TODO
        return dssSpSsoDescriptor.getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates();
    }

    // Get Client certificate to add as keyInfo in SignRequest

    // Get client credentials used then signing SignRequest
}
