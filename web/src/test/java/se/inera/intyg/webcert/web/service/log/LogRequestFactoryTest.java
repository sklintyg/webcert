/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;

public class LogRequestFactoryTest {

    @Test
    public void testCreateLogRequestFromUtkast() {
        final String intygsId = "intygsId";
        final Personnummer patientPersonnummer = new Personnummer("personId");
        final String patientFornamn = "fornamn";
        final String patientMellannamn = "mellannamn";
        final String patientEfternamn = "efternamn";
        final String enhetsid = "enhetsid";
        final String enhetsnamn = "enhetsnamn";
        final String vardgivarid = "vardgivarid";
        final String vardgivarnamn = "vardgivarnamn";
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygsId);
        utkast.setPatientPersonnummer(patientPersonnummer);
        utkast.setPatientFornamn(patientFornamn);
        utkast.setPatientMellannamn(patientMellannamn);
        utkast.setPatientEfternamn(patientEfternamn);
        utkast.setEnhetsId(enhetsid);
        utkast.setEnhetsNamn(enhetsnamn);
        utkast.setVardgivarId(vardgivarid);
        utkast.setVardgivarNamn(vardgivarnamn);

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

        LogRequest res = LogRequestFactory.createLogRequestFromUtkast(utkast, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }

    @Test
    public void testCreateLogRequestFromUtlatande() {
        final String intygsId = "intygsId";
        final Personnummer patientPersonnummer = new Personnummer("personId");
        final String patientNamn = "fornamn mellannamn efternamn";
        final String enhetsid = "enhetsid";
        final String enhetsnamn = "enhetsnamn";
        final String vardgivarid = "vardgivarid";
        final String vardgivarnamn = "vardgivarnamn";
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
        when(utlatande.getGrundData()).thenReturn(grundData);

        LogRequest res = LogRequestFactory.createLogRequestFromUtlatande(utlatande, true);

        assertNotNull(res);
        assertEquals(intygsId, res.getIntygId());
        assertEquals("Läsning i enlighet med sammanhållen journalföring", res.getAdditionalInfo());
    }
}
