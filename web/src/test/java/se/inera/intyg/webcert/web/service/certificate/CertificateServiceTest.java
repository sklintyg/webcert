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
package se.inera.intyg.webcert.web.service.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CertificateServiceTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private LogService logService;
    @Mock
    private ITIntegrationService itIntegrationService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @InjectMocks
    CertificateServiceImpl certificateService;

    private final String civicRegistrationNumber = "191212121212";
    private final String civicRegistrationNumberWithDash = "19121212-1212";
    private final String CERTIFICATE_TYPE = "lisjp";

    @Test
    public void errorGettingCertificatesFromIT() {
        Mockito.when(itIntegrationService.getCertificatesForDoctor(null, null)).thenReturn(null);
        var certificateListResponse = certificateService.listCertificatesForDoctor(null);
        verify(logService, times(0)).logListIntyg(any(), any());
        assertTrue(certificateListResponse.isErrorFromIT());
    }

    @Test
    public void shouldReturnListOfEmptyCertificatesIfErrorGettingCertificatesFromIT() {
        Mockito.when(itIntegrationService.getCertificatesForDoctor(null, null)).thenReturn(null);
        var certificateListResponse = certificateService.listCertificatesForDoctor(null);
        verify(logService, times(0)).logListIntyg(any(), any());
        assertTrue(certificateListResponse.getCertificates().isEmpty());
    }

    @Test
    public void decoratePatientWithAllFlags() {
        testDecorateWithPatientFlags(true, false);
    }

    @Test
    public void decoratePatientWithNoFlags() {
        testDecorateWithPatientFlags(false, false);
    }

    @Test
    public void decoratePatientWithOnlyUndefinedSekretessStatus() {
        testDecorateWithPatientFlags(false, true);
    }

    @Test
    public void decoratePatientWithUndefinedSekretessStatus() {
        testDecorateWithPatientFlags(true, true);
    }

    private void testDecorateWithPatientFlags(boolean hasFlags, boolean testUndefined) {
        WebCertUser user = buildUser();
        var response = getCertificateListResponse();
        var parameters = new QueryIntygParameter();
        SekretessStatus sekretessStatus;
        Set<String> certificateTypes = new HashSet<>();
        certificateTypes.add(CERTIFICATE_TYPE);

        if (testUndefined) {
            sekretessStatus = SekretessStatus.UNDEFINED;
        } else if (hasFlags) {
            sekretessStatus = SekretessStatus.TRUE;
        } else {
            sekretessStatus = SekretessStatus.FALSE;
        }

        Mockito.when(webCertUserService.getUser()).thenReturn(user);
        Mockito.when(authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG))
            .thenReturn(certificateTypes);
        Mockito.when(patientDetailsResolver.getSekretessStatus(getCivicRegistrationNumber(civicRegistrationNumber))).
            thenReturn(sekretessStatus);
        Mockito.when(patientDetailsResolver.isAvliden(getCivicRegistrationNumber(civicRegistrationNumber))).thenReturn(hasFlags);
        Mockito.when(patientDetailsResolver.isTestIndicator(getCivicRegistrationNumber(civicRegistrationNumber))).thenReturn(hasFlags);
        Mockito.when(itIntegrationService.getCertificatesForDoctor(any(), any())).
            thenReturn(response);

        response = certificateService.listCertificatesForDoctor(parameters);
        List<CertificateListEntry> certificates = response.getCertificates();

        assertFalse(response.isErrorFromIT());
        assertTrue(certificates.size() > 0);
        assertEquals(testUndefined || hasFlags, certificates.get(0).isProtectedIdentity());
        assertEquals(hasFlags, certificates.get(0).isDeceased());
        assertEquals(hasFlags, certificates.get(0).isTestIndicator());
        verify(logService, times(1)).logListIntyg(user, civicRegistrationNumberWithDash);
    }

    private WebCertUser buildUser() {
        WebCertUser user = new WebCertUser();
        user.setOrigin(UserOriginType.NORMAL.name());
        user.setAuthorities(new HashMap<>());
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
            createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        return user;
    }

    private Privilege createPrivilege(String privilegeName) {
        Privilege privilege = new Privilege();
        privilege.setName(privilegeName);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(UserOriginType.NORMAL.name());
        requestOrigin.setIntygstyper(Collections.singletonList(CERTIFICATE_TYPE));
        privilege.setRequestOrigins(Collections.singletonList(requestOrigin));
        privilege.setIntygstyper(Collections.singletonList(CERTIFICATE_TYPE));
        return privilege;
    }

    private Personnummer getCivicRegistrationNumber(String civicRegistrationNumber) {
        Optional<Personnummer> optionalCRN = Personnummer.createPersonnummer(civicRegistrationNumber);
        return optionalCRN.get();
    }

    private CertificateListResponse getCertificateListResponse() {
        CertificateListEntry certificate = new CertificateListEntry();
        certificate.setCertificateType(CERTIFICATE_TYPE);
        certificate.setCivicRegistrationNumber(civicRegistrationNumber);
        List<CertificateListEntry> certificates = new ArrayList<>();
        certificates.add(certificate);
        CertificateListResponse response = new CertificateListResponse();
        response.setCertificates(certificates);
        return response;
    }

}

