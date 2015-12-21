/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.intyg;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.cxf.helpers.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.intyg.webcert.web.service.intyg.config.IntygServiceConfigurationManagerImpl;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.signatur.SignaturServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;

public abstract class AbstractIntygServiceTest extends AuthoritiesConfigurationTestSetup {

    protected static final String CONFIG_AS_JSON = "{config-as-json}";

    protected static final String INTYG_ID = "intyg-1";

    protected static final String INTYG_TYP_FK = "fk7263";

    @Mock
    protected RegisterCertificateResponderInterface intygSender;

    @Mock
    protected SendCertificateToRecipientResponderInterface sendService;

    @Mock
    protected RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    protected IntygModuleFacade moduleFacade;

    @Mock
    protected UtkastRepository intygRepository;

    @Mock
    protected WebCertUserService webCertUserService;

    @Mock
    protected LogService logService;

    @Mock
    protected NotificationService notificationService;

    @Mock
    protected MonitoringLogService monitoringService;

    @Mock
    protected CertificateSenderService certificateSenderService;

    // Here we test the real converter
    @Spy
    protected IntygServiceConverterImpl serviceConverter = new IntygServiceConverterImpl();

    // Here we use the real config manager
    @Spy
    protected IntygServiceConfigurationManager configurationManager = new IntygServiceConfigurationManagerImpl(new CustomObjectMapper());

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    ModuleApi moduleApi;

    @InjectMocks
    protected SignaturServiceImpl intygSignatureService = new SignaturServiceImpl();

    @InjectMocks
    protected IntygServiceImpl intygService = new IntygServiceImpl();

    protected String json;
    protected Utlatande utlatande;
    protected CertificateResponse certificateResponse;

    @Before
    public void setupMocks() throws Exception {
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = new CustomObjectMapper().readValue(json, Utlatande.class);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(certificateResponse);

        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        serviceConverter.setModuleRegistry(moduleRegistry);
    }

    private CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "FK", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        return metaData;
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(false);
    }

}
