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
package se.inera.intyg.webcert.web.service.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactoryImpl;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@RunWith(MockitoJUnitRunner.class
)
public class LogRequestFactoryTest {

    private static final String intygsId = "intygsId";
    private static final String patientId = "20121212-1212";
    private static final String patientFornamn = "fornamn";
    private static final String patientMellannamn = "mellannamn";
    private static final String patientEfternamn = "efternamn";
    private static final String enhetsid = "enhetsid";
    private static final String enhetsnamn = "enhetsnamn";
    private static final String vardgivarid = "vardgivarid";
    private static final String vardgivarnamn = "vardgivarnamn";

    private static final Personnummer patientPersonnummer = Personnummer.createPersonnummer(patientId).get();

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private LogRequestFactoryImpl testee = new LogRequestFactoryImpl();

    @Before
    public void init() throws ModuleNotFoundException {
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn("FKASSA");
        when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        when(patientDetailsResolver.isTestIndicator(any())).thenReturn(false);
    }

    @Test
    public void testCreateLogRequestFromUtkast() {
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn("TS");
        Utkast utkast = buildUtkast(intygsId, "ts-bas", patientPersonnummer, patientFornamn, patientMellannamn, patientEfternamn, enhetsid,
            enhetsnamn, vardgivarid, vardgivarnamn);

        LogRequest res = testee.createLogRequestFromUtkast(utkast);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals(patientPersonnummer, res.getPatientId());
        assertEquals(patientFornamn + " " + patientMellannamn + " " + patientEfternamn, res.getPatientName());
        assertEquals(enhetsid, res.getIntygCareUnitId());
        assertEquals(enhetsnamn, res.getIntygCareUnitName());
        assertEquals(vardgivarid, res.getIntygCareGiverId());
        assertEquals(vardgivarnamn, res.getIntygCareGiverName());
        assertNull(res.getAdditionalInfo());
    }


    @Test
    public void testCreateLogRequestFromUtkastCoherentJournaling() {
        final String intygsId = "intygsId";
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp("ts-bas");
        utkast.setPatientPersonnummer(patientPersonnummer);

        LogRequest res = testee.createLogRequestFromUtkast(utkast, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }

    @Test
    public void testCreateLogRequestFromUtlatande() {
        when(moduleEntryPoint.getDefaultRecipient()).thenReturn("TS");
        final String patientNamn = Arrays.asList(patientFornamn, patientMellannamn, patientEfternamn)
            .stream()
            .collect(Collectors.joining(" "));

        Utlatande utlatande = mock(Utlatande.class);

        GrundData grundData = new GrundData();
        grundData.setPatient(new Patient());
        grundData.getPatient().setPersonId(patientPersonnummer);
        grundData.getPatient().setFullstandigtNamn(patientNamn);
        grundData.setSkapadAv(new HoSPersonal());
        grundData.getSkapadAv().setVardenhet(new Vardenhet());
        grundData.getSkapadAv().getVardenhet().setEnhetsid(enhetsid);
        grundData.getSkapadAv().getVardenhet().setEnhetsnamn(enhetsnamn);
        grundData.getSkapadAv().getVardenhet().setVardgivare(new Vardgivare());
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarid(vardgivarid);
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarnamn(vardgivarnamn);

        when(utlatande.getId()).thenReturn(intygsId);
        when(utlatande.getTyp()).thenReturn("ts-bas");
        when(utlatande.getGrundData()).thenReturn(grundData);

        LogRequest res = testee.createLogRequestFromUtlatande(utlatande);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals(patientPersonnummer, res.getPatientId());
        assertEquals(patientNamn, res.getPatientName());
        assertEquals(enhetsid, res.getIntygCareUnitId());
        assertEquals(enhetsnamn, res.getIntygCareUnitName());
        assertEquals(vardgivarid, res.getIntygCareGiverId());
        assertEquals(vardgivarnamn, res.getIntygCareGiverName());
        assertNull(res.getAdditionalInfo());
    }

    @Test
    public void testCreateLogRequestFromUtlatandeCoherentJournaling() {
        final String intygsId = "intygsId";
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grundData = new GrundData();
        grundData.setPatient(new Patient());
        grundData.setSkapadAv(new HoSPersonal());
        grundData.getSkapadAv().setVardenhet(new Vardenhet());
        grundData.getSkapadAv().getVardenhet().setVardgivare(new Vardgivare());

        when(utlatande.getId()).thenReturn(intygsId);
        when(utlatande.getTyp()).thenReturn("ts-bas");
        when(utlatande.getGrundData()).thenReturn(grundData);

        LogRequest res = testee.createLogRequestFromUtlatande(utlatande, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }

    @Test
    public void testPatientNameRemovedForFkIntyg() {

        Utkast utkast = buildUtkast(intygsId, "luse", patientPersonnummer, patientFornamn, patientMellannamn, patientEfternamn, enhetsid,
            enhetsnamn, vardgivarid, vardgivarnamn);

        LogRequest res = testee.createLogRequestFromUtkast(utkast);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals(patientPersonnummer, res.getPatientId());
        assertEquals("", res.getPatientName());

        assertNull(res.getAdditionalInfo());
    }

    @Test
    public void shallCreateLogRequestWithCareUnitFromUser() {
        final var user = buildUser(false);

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertEquals(user.getValdVardenhet().getId(), actualLogRequest.getIntygCareUnitId());
        assertEquals(user.getValdVardenhet().getNamn(), actualLogRequest.getIntygCareUnitName());

        assertEquals(user.getValdVardgivare().getId(), actualLogRequest.getIntygCareGiverId());
        assertEquals(user.getValdVardgivare().getNamn(), actualLogRequest.getIntygCareGiverName());
    }

    @Test
    public void shallCreateLogRequestWithCareProviderFromUser() {
        final var user = buildUser(false);

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertEquals(user.getValdVardgivare().getId(), actualLogRequest.getIntygCareGiverId());
        assertEquals(user.getValdVardgivare().getNamn(), actualLogRequest.getIntygCareGiverName());
    }

    @Test
    public void shallCreateLogRequestWithCoherentJournalingFromUser() {
        final var user = buildUser(true);

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertEquals("Läsning i enlighet med sammanhållen journalföring", actualLogRequest.getAdditionalInfo());
    }

    @Test
    public void shallCreateLogRequestWithTestIntygFlagIfPatientIsTestIndicated() {
        final var user = buildUser(false);

        doReturn(true).when(patientDetailsResolver).isTestIndicator(any(Personnummer.class));

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertTrue("Expected isTestIntyg to be true", actualLogRequest.isTestIntyg());
    }

    @Test
    public void shallCreateLogRequestWithoutTestIntygFlagIfPatientIsntTestIndicated() {
        final var user = buildUser(false);

        doReturn(false).when(patientDetailsResolver).isTestIndicator(any(Personnummer.class));

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertFalse("Expected isTestIntyg to be false", actualLogRequest.isTestIntyg());
    }

    @Test
    public void shallCreateLogRequestWithoutPatientName() {
        final var user = buildUser(false);

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertTrue("Expected patient name to be empty", actualLogRequest.getPatientName().isEmpty());
    }

    @Test
    public void shallCreateLogRequestWithPatientId() {
        final var user = buildUser(false);

        final var actualLogRequest = testee.createLogRequestFromUser(user, patientId);

        assertEquals(patientId, actualLogRequest.getPatientId().getOriginalPnr());
    }

    private WebCertUser buildUser(boolean coherentJournaling) {
        final var user = mock(WebCertUser.class);

        final var expectedCareUnit = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet();
        expectedCareUnit.setId("careUnitId");
        expectedCareUnit.setNamn("careUnitName");
        doReturn(expectedCareUnit).when(user).getValdVardenhet();

        final var expectedCareProvider = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare();
        expectedCareProvider.setId("careProviderId");
        expectedCareProvider.setNamn("careProviderName");
        doReturn(expectedCareProvider).when(user).getValdVardgivare();

        if (coherentJournaling) {
            final var parameters = IntegrationParameters
                .of(null, null, null, null, null, null, null, null, null, coherentJournaling, false, false, false);
            doReturn(parameters).when(user).getParameters();
        }
        return user;
    }


    private Utkast buildUtkast(String intygsId, String intygsTyp, Personnummer patientPersonnummer, String patientFornamn,
        String patientMellannamn, String patientEfternamn, String enhetsid, String enhetsnamn, String vardgivarid, String vardgivarnamn) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(intygsTyp);
        utkast.setPatientPersonnummer(patientPersonnummer);
        utkast.setPatientFornamn(patientFornamn);
        utkast.setPatientMellannamn(patientMellannamn);
        utkast.setPatientEfternamn(patientEfternamn);
        utkast.setEnhetsId(enhetsid);
        utkast.setEnhetsNamn(enhetsnamn);
        utkast.setVardgivarId(vardgivarid);
        utkast.setVardgivarNamn(vardgivarnamn);
        return utkast;
    }
}
