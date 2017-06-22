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
package se.inera.intyg.webcert.web.converter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import org.junit.Test;

import iso.v21090.dt.v1.II;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;

public class ConvertToFKTypesTest {

    @Test
    public void testToIIRootNull() {
        II res = ConvertToFKTypes.toII(null, "ext");

        assertNull(res);
    }

    @Test
    public void testToIIExtNull() {
        II res = ConvertToFKTypes.toII("root", null);

        assertNull(res);
    }

    @Test
    public void testToII() {
        final String root = "root";
        final String ext = "ext";
        II res = ConvertToFKTypes.toII(root, ext);

        assertNotNull(res);
        assertEquals(root, res.getRoot());
        assertEquals(ext, res.getExtension());
    }

    @Test
    public void testToAmneTyp() {
        assertEquals(Amnetyp.ARBETSTIDSFORLAGGNING, ConvertToFKTypes.toAmneTyp(Amne.ARBETSTIDSFORLAGGNING));
        assertEquals(Amnetyp.AVSTAMNINGSMOTE, ConvertToFKTypes.toAmneTyp(Amne.AVSTAMNINGSMOTE));
        assertEquals(Amnetyp.KOMPLETTERING_AV_LAKARINTYG, ConvertToFKTypes.toAmneTyp(Amne.KOMPLETTERING_AV_LAKARINTYG));
        assertEquals(Amnetyp.KONTAKT, ConvertToFKTypes.toAmneTyp(Amne.KONTAKT));
        assertEquals(Amnetyp.MAKULERING_AV_LAKARINTYG, ConvertToFKTypes.toAmneTyp(Amne.MAKULERING_AV_LAKARINTYG));
        assertEquals(Amnetyp.OVRIGT, ConvertToFKTypes.toAmneTyp(Amne.OVRIGT));
        assertEquals(Amnetyp.PAMINNELSE, ConvertToFKTypes.toAmneTyp(Amne.PAMINNELSE));
    }

    @Test
    public void testToInnehallType() {
        final String text = "text";
        LocalDateTime signeringsdatum = LocalDateTime.now();
        InnehallType res = ConvertToFKTypes.toInnehallType(text, signeringsdatum);

        assertNotNull(res);
        assertEquals(text, res.getMeddelandeText());
        assertEquals(signeringsdatum, res.getSigneringsTidpunkt());
    }

    @Test
    public void testToInnehallTypeSigneringsdatumNull() {
        final String text = "text";
        InnehallType res = ConvertToFKTypes.toInnehallType(text, null);

        assertNotNull(res);
        assertEquals(text, res.getMeddelandeText());
        assertNull(res.getSigneringsTidpunkt());
    }

    @Test
    public void testToLakarUtlatande() {
        final String intygsId = "intygsId";
        final String patientNamn = "fullständigt namn";
        final String patientId = "patientId";
        final LocalDateTime signeringsdatum = LocalDateTime.now();
        IntygsReferens ir = new IntygsReferens();
        ir.setIntygsId(intygsId);
        ir.setPatientNamn(patientNamn);
        ir.setPatientId(new Personnummer(patientId));
        ir.setSigneringsDatum(signeringsdatum);
        LakarutlatandeEnkelType res = ConvertToFKTypes.toLakarUtlatande(ir);

        assertNotNull(res);
        assertEquals(intygsId, res.getLakarutlatandeId());
        assertNull(res.getPatient().getFullstandigtNamn());
        assertEquals("1.2.752.129.2.1.3.1", res.getPatient().getPersonId().getRoot());
        assertEquals(patientId, res.getPatient().getPersonId().getExtension());
        assertEquals(signeringsdatum, res.getSigneringsTidpunkt());
    }

    @Test
    public void testToLakarUtlatandeSamordningsnummer() {
        final String intygsId = "intygsId";
        final String patientNamn = "fullständigt namn";
        final String patientId = "999999-9999";
        final LocalDateTime signeringsdatum = LocalDateTime.now();
        IntygsReferens ir = new IntygsReferens();
        ir.setIntygsId(intygsId);
        ir.setPatientNamn(patientNamn);
        ir.setPatientId(new Personnummer(patientId));
        ir.setSigneringsDatum(signeringsdatum);
        LakarutlatandeEnkelType res = ConvertToFKTypes.toLakarUtlatande(ir);

        assertNotNull(res);
        assertEquals(intygsId, res.getLakarutlatandeId());
        assertNull(res.getPatient().getFullstandigtNamn());
        assertEquals("1.2.752.129.2.1.3.3", res.getPatient().getPersonId().getRoot());
        assertEquals(patientId, res.getPatient().getPersonId().getExtension());
        assertEquals(signeringsdatum, res.getSigneringsTidpunkt());
    }

    @Test
    public void testToLakarUtlatandeIntygsReferensNull() {
        LakarutlatandeEnkelType res = ConvertToFKTypes.toLakarUtlatande(null);

        assertNull(res);
    }

    @Test
    public void testToVardAdresseringsType() {
        final String enhetsId = "enhetsId";
        final String enhetsnamn = "enhetsnamn";
        final String arbetsplatskod = "arbetsplatskod";
        final String vardgivarid = "vardgivarid";
        final String vardgivarnamn = "vardgivarnamn";
        final String fullstandigtNamn = "fullstandigt namn";
        final String personalId = "personalId";
        final String forskrivarkod = "forskrivarkod";
        final String epost = "epost";
        final String postadress = "postadress";
        final String postnummer = "postnummer";
        final String postort = "postort";
        Vardperson vp = new Vardperson();
        vp.setArbetsplatsKod(arbetsplatskod);
        vp.setEnhetsId(enhetsId);
        vp.setEnhetsnamn(enhetsnamn);
        vp.setForskrivarKod(forskrivarkod);
        vp.setHsaId(personalId);
        vp.setNamn(fullstandigtNamn);
        vp.setVardgivarId(vardgivarid);
        vp.setVardgivarnamn(vardgivarnamn);
        vp.setEpost(epost);
        vp.setPostadress(postadress);
        vp.setPostnummer(postnummer);
        vp.setPostort(postort);
        VardAdresseringsType res = ConvertToFKTypes.toVardAdresseringsType(vp);

        assertNotNull(res);
        assertEquals("1.2.752.129.2.1.4.1", res.getHosPersonal().getEnhet().getEnhetsId().getRoot());
        assertEquals(enhetsId, res.getHosPersonal().getEnhet().getEnhetsId().getExtension());
        assertEquals(enhetsnamn, res.getHosPersonal().getEnhet().getEnhetsnamn());
        assertEquals("1.2.752.29.4.71", res.getHosPersonal().getEnhet().getArbetsplatskod().getRoot());
        assertEquals(arbetsplatskod, res.getHosPersonal().getEnhet().getArbetsplatskod().getExtension());
        assertEquals("1.2.752.129.2.1.4.1", res.getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getRoot());
        assertEquals(vardgivarid, res.getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getExtension());
        assertEquals(vardgivarnamn, res.getHosPersonal().getEnhet().getVardgivare().getVardgivarnamn());
        assertEquals(fullstandigtNamn, res.getHosPersonal().getFullstandigtNamn());
        assertEquals("1.2.752.129.2.1.4.1", res.getHosPersonal().getPersonalId().getRoot());
        assertEquals(personalId, res.getHosPersonal().getPersonalId().getExtension());
        assertEquals(forskrivarkod, res.getHosPersonal().getForskrivarkod());
        assertEquals(epost, res.getHosPersonal().getEnhet().getEpost());
        assertEquals(postadress, res.getHosPersonal().getEnhet().getPostadress());
        assertEquals(postnummer, res.getHosPersonal().getEnhet().getPostnummer());
        assertEquals(postort, res.getHosPersonal().getEnhet().getPostort());
    }

    @Test
    public void testToVardAdresseringsTypeNoArbetsplatskod() {
        final String enhetsId = "enhetsId";
        final String enhetsnamn = "enhetsnamn";
        final String vardgivarid = "vardgivarid";
        final String vardgivarnamn = "vardgivarnamn";
        final String fullstandigtNamn = "fullstandigt namn";
        final String personalId = "personalId";
        Vardperson vp = new Vardperson();
        vp.setEnhetsId(enhetsId);
        vp.setEnhetsnamn(enhetsnamn);
        vp.setHsaId(personalId);
        vp.setNamn(fullstandigtNamn);
        vp.setVardgivarId(vardgivarid);
        vp.setVardgivarnamn(vardgivarnamn);
        VardAdresseringsType res = ConvertToFKTypes.toVardAdresseringsType(vp);

        assertNotNull(res);
        assertNull(res.getHosPersonal().getEnhet().getArbetsplatskod());
    }

    @Test
    public void testToVardAdresseringsTypeNoVardgivarid() {
        final String enhetsId = "enhetsId";
        final String enhetsnamn = "enhetsnamn";
        final String vardgivarnamn = "vardgivarnamn";
        final String fullstandigtNamn = "fullstandigt namn";
        final String personalId = "personalId";
        Vardperson vp = new Vardperson();
        vp.setEnhetsId(enhetsId);
        vp.setEnhetsnamn(enhetsnamn);
        vp.setHsaId(personalId);
        vp.setNamn(fullstandigtNamn);
        vp.setVardgivarnamn(vardgivarnamn);
        VardAdresseringsType res = ConvertToFKTypes.toVardAdresseringsType(vp);

        assertNotNull(res);
        assertNull(res.getHosPersonal().getEnhet().getVardgivare().getVardgivareId());
    }

    @Test
    public void testToVardAdresseringsTypeVardpersonNull() {
        VardAdresseringsType res = ConvertToFKTypes.toVardAdresseringsType(null);

        assertNull(res);
    }

    @Test
    public void testToEnhetTypeVardpersonNull() {
        EnhetType res = ConvertToFKTypes.toEnhetType(null);

        assertNull(res);
    }

}
