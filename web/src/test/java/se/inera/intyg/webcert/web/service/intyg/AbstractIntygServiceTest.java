/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

public abstract class AbstractIntygServiceTest extends AuthoritiesConfigurationTestSetup {

    protected static final String CONFIG_AS_JSON = "{config-as-json}";

    protected static final String INTYG_ID = "intyg-1";

    protected static final String INTYG_TYP_FK = "fk7263";

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
    protected ArendeService arendeService;

    @Mock
    protected UtkastIntygDecorator utkastIntygDecorator;

    @Mock
    protected MonitoringLogService monitoringService;

    @Mock
    protected CertificateSenderService certificateSenderService;

    @Spy
    protected ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    protected IntygServiceImpl intygService = new IntygServiceImpl();

    protected String json;
    protected Fk7263Utlatande utlatande;
    protected CertificateResponse certificateResponse;

    @Before
    public void setupMocks() throws Exception {
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(certificateResponse);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString())).thenReturn(utlatande);
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
