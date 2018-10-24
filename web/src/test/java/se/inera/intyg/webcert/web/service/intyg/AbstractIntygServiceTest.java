/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.decorator.IntygRelationHelper;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

public abstract class AbstractIntygServiceTest extends AuthoritiesConfigurationTestSetup {

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
    protected CertificateRelationService certificateRelationService;

    @Mock
    protected CertificateSenderService certificateSenderService;

    @Mock
    protected IntygRelationHelper intygRelationHelper;

    @Mock
    protected PatientDetailsResolver patientDetailsResolver;

    @Mock
    protected ModuleApi moduleApi;

    @Mock
    protected IntygModuleRegistry moduleRegistry;

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
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(certificateResponse);
        when(certificateRelationService.getNewestRelationOfType(anyString(), any(RelationKod.class), any(List.class))).thenReturn(Optional.empty());
        when(intygRelationHelper.getRelationsForIntyg(anyString())).thenReturn(new Relations());

        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(buildPatient(false, false));
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(new Fk7263Utlatande());
    }

    protected CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "FKASSA", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        metaData.setSignDate(LocalDateTime.now());
        return metaData;
    }

    protected Patient buildPatient(boolean sekretessMarkering, boolean avliden) {
        Patient patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer("19121212-1212").get());
        patient.setFornamn("fornamn");
        patient.setMellannamn("mellannamn");
        patient.setEfternamn("efternamn");
        patient.setSekretessmarkering(sekretessMarkering);
        patient.setAvliden(avliden);

        return patient;

    }
}
