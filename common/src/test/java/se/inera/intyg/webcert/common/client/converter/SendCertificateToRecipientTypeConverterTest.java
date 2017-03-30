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
package se.inera.intyg.webcert.common.client.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;

public class SendCertificateToRecipientTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        final String intygsId = "intygsid";
        final String enhetsId = "enhetsid";
        final String enhetsnamn = "enhetsnamn";
        final String patientPersonId = "pid";
        final String skapadAvFullstandigtNamn = "fullständigt namn";
        final String skapadAvPersonId = "skapad av pid";
        final String arbetsplatsKod = "arbetsplatsKod";
        final String postadress = "postadress";
        final String postNummer = "postNummer";
        final String postOrt = "postOrt";
        final String epost = "epost";
        final String telefonNummer = "telefonNummer";
        final String vardgivarid = "vardgivarid";
        final String vardgivarNamn = "vardgivarNamn";
        final String forskrivarKod = "forskrivarKod";
        final String recipient = "TS";

        HoSPersonal skickatAv = buildHosPersonal(enhetsId, enhetsnamn, skapadAvFullstandigtNamn, skapadAvPersonId, arbetsplatsKod,
                postadress,
                postNummer, postOrt, epost, telefonNummer, vardgivarid, vardgivarNamn, forskrivarKod);

        SendCertificateToRecipientType result = SendCertificateToRecipientTypeConverter.convert(intygsId, patientPersonId, skickatAv,
                recipient);

        assertNotNull(result.getIntygsId().getRoot());
        assertEquals(intygsId, result.getIntygsId().getExtension());
        assertNotNull(result.getPatientPersonId().getRoot());
        assertEquals(patientPersonId, result.getPatientPersonId().getExtension());
        assertEquals(skapadAvFullstandigtNamn, result.getSkickatAv().getHosPersonal().getFullstandigtNamn());
        assertNotNull(result.getSkickatAv().getHosPersonal().getPersonalId().getRoot());
        assertEquals(skapadAvPersonId, result.getSkickatAv().getHosPersonal().getPersonalId().getExtension());
        assertNotNull(result.getSkickatAv().getHosPersonal().getEnhet().getEnhetsId().getRoot());
        assertEquals(enhetsId, result.getSkickatAv().getHosPersonal().getEnhet().getEnhetsId().getExtension());
        assertNotNull(result.getSkickatAv().getHosPersonal().getEnhet().getEnhetsId().getExtension());
        assertEquals(enhetsnamn, result.getSkickatAv().getHosPersonal().getEnhet().getEnhetsnamn());
        assertNotNull(result.getSkickatAv().getHosPersonal().getEnhet().getArbetsplatskod().getRoot());
        assertEquals(arbetsplatsKod, result.getSkickatAv().getHosPersonal().getEnhet().getArbetsplatskod().getExtension());
        assertEquals(postadress, result.getSkickatAv().getHosPersonal().getEnhet().getPostadress());
        assertEquals(postNummer, result.getSkickatAv().getHosPersonal().getEnhet().getPostnummer());
        assertEquals(postOrt, result.getSkickatAv().getHosPersonal().getEnhet().getPostort());
        assertEquals(epost, result.getSkickatAv().getHosPersonal().getEnhet().getEpost());
        assertEquals(telefonNummer, result.getSkickatAv().getHosPersonal().getEnhet().getTelefonnummer());
        assertNotNull(result.getSkickatAv().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getRoot());
        assertEquals(vardgivarid, result.getSkickatAv().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getExtension());
        assertEquals(vardgivarNamn, result.getSkickatAv().getHosPersonal().getEnhet().getVardgivare().getVardgivarnamn());
        assertEquals(forskrivarKod, result.getSkickatAv().getHosPersonal().getForskrivarkod());
        assertNotNull(result.getSkickatTidpunkt());
        assertNotNull(result.getMottagare().getCodeSystem());
        assertEquals("TRANSP", result.getMottagare().getCode());
    }

    private HoSPersonal buildHosPersonal(String enhetsId, String enhetsnamn, String skapadAvFullstandigtNamn, String skapadAvPersonId,
            String arbetsplatsKod, String postadress, String postNummer, String postOrt, String epost, String telefonNummer,
            String vardgivarid,
            String vardgivarNamn, String forskrivarKod) {
        HoSPersonal hosPersonal = new HoSPersonal();
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(enhetsId);
        vardenhet.setEnhetsnamn(enhetsnamn);
        vardenhet.setArbetsplatsKod(arbetsplatsKod);
        vardenhet.setPostadress(postadress);
        vardenhet.setPostnummer(postNummer);
        vardenhet.setPostort(postOrt);
        vardenhet.setEpost(epost);
        vardenhet.setTelefonnummer(telefonNummer);
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(vardgivarid);
        vardgivare.setVardgivarnamn(vardgivarNamn);
        vardenhet.setVardgivare(vardgivare);
        hosPersonal.setVardenhet(vardenhet);
        hosPersonal.setFullstandigtNamn(skapadAvFullstandigtNamn);
        hosPersonal.setPersonId(skapadAvPersonId);
        hosPersonal.setForskrivarKod(forskrivarKod);
        return hosPersonal;
    }
}
