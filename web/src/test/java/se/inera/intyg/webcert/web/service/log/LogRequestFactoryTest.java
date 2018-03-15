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
package se.inera.intyg.webcert.web.service.log;

import org.junit.Test;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static final Personnummer patientPersonnummer = Personnummer.createValidatedPersonnummer(patientId).get();

    @Test
    public void testCreateLogRequestFromUtkast() {

        Utkast utkast = buildUtkast(intygsId, "ts-bas", patientPersonnummer, patientFornamn, patientMellannamn, patientEfternamn, enhetsid, enhetsnamn, vardgivarid, vardgivarnamn);

        LogRequest res = LogRequestFactory.createLogRequestFromUtkast(utkast);

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

        LogRequest res = LogRequestFactory.createLogRequestFromUtkast(utkast, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }

    @Test
    public void testCreateLogRequestFromUtlatande() {
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

        LogRequest res = LogRequestFactory.createLogRequestFromUtlatande(utlatande);

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

        LogRequest res = LogRequestFactory.createLogRequestFromUtlatande(utlatande, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }

    @Test
    public void testPatientNameRemovedForFkIntyg() {

        Utkast utkast = buildUtkast(intygsId, "luse", patientPersonnummer, patientFornamn, patientMellannamn, patientEfternamn, enhetsid, enhetsnamn, vardgivarid, vardgivarnamn);

        LogRequest res = LogRequestFactory.createLogRequestFromUtkast(utkast);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals(patientPersonnummer, res.getPatientId());
        assertEquals("", res.getPatientName());

        assertNull(res.getAdditionalInfo());
    }

    private Utkast buildUtkast(String intygsId, String intygsTyp, Personnummer patientPersonnummer, String patientFornamn, String patientMellannamn, String patientEfternamn, String enhetsid, String enhetsnamn, String vardgivarid, String vardgivarnamn) {
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
