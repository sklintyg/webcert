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
package se.inera.intyg.webcert.web.converter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class IntygConverterUtilTest {

    @Test
    void testBuildSendTypeFromUtlatande() throws Exception {

        final var utlatande = createUtlatandeFromJson();

        final var res = IntygConverterUtil.buildSendTypeFromUtlatande(utlatande);

        assertNotNull(res);

        assertNotNull(res.getAvsantTidpunkt());

        assertTrue(res.getVardReferensId().contains("SEND-123-"));

        assertEquals("123", res.getLakarutlatande().getLakarutlatandeId());
        assertNull(res.getLakarutlatande().getPatient().getFullstandigtNamn());
        assertEquals("19121212-1212", res.getLakarutlatande().getPatient().getPersonId().getExtension());
        assertNotNull(res.getLakarutlatande().getSigneringsTidpunkt());
        assertNull(res.getAdressVard().getHosPersonal().getForskrivarkod());
        assertEquals("En Läkare", res.getAdressVard().getHosPersonal().getFullstandigtNamn());
        assertEquals("Personal HSA-ID", res.getAdressVard().getHosPersonal().getPersonalId().getExtension());
        assertEquals("Kir mott", res.getAdressVard().getHosPersonal().getEnhet().getEnhetsnamn());
        assertEquals("VardenhetY", res.getAdressVard().getHosPersonal().getEnhet().getEnhetsId().getExtension());
        assertEquals("123456789011", res.getAdressVard().getHosPersonal().getEnhet().getArbetsplatskod().getExtension());
        assertEquals("Landstinget Norrland", res.getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivarnamn());
        assertEquals("VardgivarId", res.getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getExtension());

    }

    @Test
    void testConcatPatientName() {
        final var fName = "Adam Bertil Cesar";
        final var mName = "Davidsson";
        final var lName = "Eriksson";

        final var name = IntygConverterUtil.concatPatientName(fName, mName, lName);

        assertEquals("Adam Bertil Cesar Davidsson Eriksson", name);
    }

    @Test
    void testConcatPatientNameWithoutMiddlename() {
        final var fName = "Adam Bertil";
        final var lName = "Eriksson";

        final var name = IntygConverterUtil.concatPatientName(fName, null, lName);

        assertEquals("Adam Bertil Eriksson", name);
    }

    @Test
    void testConcatPatientNameWithBlankMiddlename() {
        final var fName = "Adam Bertil";
        final var lName = "Eriksson";

        final var name = IntygConverterUtil.concatPatientName(fName, " ", lName);

        assertEquals("Adam Bertil Eriksson", name);
    }

    @Test
    void testBuildVardRefId() {

        final var ts = LocalDateTime.parse("2014-01-01T12:34:56.123");

        final var res = IntygConverterUtil.buildVardReferensId("ABC123", ts);

        assertNotNull(res);
        assertEquals("SEND-ABC123-20140101T123456.123", res);
    }

    @Test
    void testBuildHosPersonalFromWebCertUser() {
        final var forskrivarkod = "forskrivarkod";
        final var hsaId = "hsaid";
        final var namn = "namn";
        final var arbetsplatskod = "arbetsplatskod";
        final var epost = "epost";
        final var enhetsId = "enhetsId";
        final var enhetsnamn = "enhetsnamn";
        final var postadress = "postadress";
        final var postnummer = "postnummer";
        final var postort = "postort";
        final var telefonnummer = "telefonnummer";
        final var vardgivarId = "vardgivarId";
        final var vardgivarnamn = "vardgivarnamn";
        Vardenhet valdVardenhet = new Vardenhet();
        valdVardenhet.setArbetsplatskod(arbetsplatskod);
        valdVardenhet.setEpost(epost);
        valdVardenhet.setId(enhetsId);
        valdVardenhet.setNamn(enhetsnamn);
        valdVardenhet.setPostadress(postadress);
        valdVardenhet.setPostnummer(postnummer);
        valdVardenhet.setPostort(postort);
        valdVardenhet.setTelefonnummer(telefonnummer);
        Vardgivare valdVardgivare = new Vardgivare();
        valdVardgivare.setId(vardgivarId);
        valdVardgivare.setNamn(vardgivarnamn);
        WebCertUser user = new WebCertUser();
        user.setForskrivarkod(forskrivarkod);
        user.setHsaId(hsaId);
        user.setNamn(namn);
        user.setValdVardenhet(valdVardenhet);
        user.setValdVardgivare(valdVardgivare);
        HoSPersonal result = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, null);

        assertEquals(forskrivarkod, result.getForskrivarKod());
        assertEquals(hsaId, result.getPersonId());
        assertEquals(namn, result.getFullstandigtNamn());
        assertEquals(arbetsplatskod, result.getVardenhet().getArbetsplatsKod());
        assertEquals(epost, result.getVardenhet().getEpost());
        assertEquals(enhetsId, result.getVardenhet().getEnhetsid());
        assertEquals(enhetsnamn, result.getVardenhet().getEnhetsnamn());
        assertEquals(postadress, result.getVardenhet().getPostadress());
        assertEquals(postnummer, result.getVardenhet().getPostnummer());
        assertEquals(postort, result.getVardenhet().getPostort());
        assertEquals(telefonnummer, result.getVardenhet().getTelefonnummer());
        assertEquals(vardgivarId, result.getVardenhet().getVardgivare().getVardgivarid());
        assertEquals(vardgivarnamn, result.getVardenhet().getVardgivare().getVardgivarnamn());
    }

    @Test
    void testBuildHosPersonalFromWebCertUserWithVardenhet() {
        final var forskrivarkod = "forskrivarkod";
        final var hsaId = "hsaid";
        final var namn = "namn";
        final var arbetsplatskod = "arbetsplatskod";
        final var epost = "epost";
        final var enhetsId = "enhetsId";
        final var enhetsnamn = "enhetsnamn";
        final var postadress = "postadress";
        final var postnummer = "postnummer";
        final var postort = "postort";
        final var telefonnummer = "telefonnummer";
        final var vardgivarId = "vardgivarId";
        final var vardgivarnamn = "vardgivarnamn";
        se.inera.intyg.common.support.model.common.internal.Vardgivare vardgivare = new se.inera.intyg.common.support.model.common.internal.Vardgivare();
        vardgivare.setVardgivarid(vardgivarId);
        vardgivare.setVardgivarnamn(vardgivarnamn);
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = new se.inera.intyg.common.support.model.common.internal.Vardenhet();
        vardenhet.setArbetsplatsKod(arbetsplatskod);
        vardenhet.setEpost(epost);
        vardenhet.setEnhetsid(enhetsId);
        vardenhet.setEnhetsnamn(enhetsnamn);
        vardenhet.setPostadress(postadress);
        vardenhet.setPostnummer(postnummer);
        vardenhet.setPostort(postort);
        vardenhet.setTelefonnummer(telefonnummer);
        vardenhet.setVardgivare(vardgivare);
        WebCertUser user = new WebCertUser();
        user.setForskrivarkod(forskrivarkod);
        user.setHsaId(hsaId);
        user.setNamn(namn);

        HoSPersonal result = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhet);

        assertEquals(forskrivarkod, result.getForskrivarKod());
        assertEquals(hsaId, result.getPersonId());
        assertEquals(namn, result.getFullstandigtNamn());
        assertEquals(arbetsplatskod, result.getVardenhet().getArbetsplatsKod());
        assertEquals(epost, result.getVardenhet().getEpost());
        assertEquals(enhetsId, result.getVardenhet().getEnhetsid());
        assertEquals(enhetsnamn, result.getVardenhet().getEnhetsnamn());
        assertEquals(postadress, result.getVardenhet().getPostadress());
        assertEquals(postnummer, result.getVardenhet().getPostnummer());
        assertEquals(postort, result.getVardenhet().getPostort());
        assertEquals(telefonnummer, result.getVardenhet().getTelefonnummer());
        assertEquals(vardgivarId, result.getVardenhet().getVardgivare().getVardgivarid());
        assertEquals(vardgivarnamn, result.getVardenhet().getVardgivare().getVardgivarnamn());
    }

    @Test
    void testBuildHosPersonalFromWebCertUserWithSpecialiseringarAndBefattningar() {
        final var hsaId = "hsaid";
        final var namn = "namn";
        final var befattning1 = "befattning1";
        final var befattning2 = "befattning2";
        final var specialisering1 = "specialisering1";
        final var specialisering2 = "specialisering2";
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = new se.inera.intyg.common.support.model.common.internal.Vardenhet();
        final var user = new WebCertUser();
        user.setHsaId(hsaId);
        user.setNamn(namn);
        user.setBefattningar(Arrays.asList(befattning1, befattning2));
        user.setSpecialiseringar(Arrays.asList(specialisering1, specialisering2));

        final var result = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhet);

        assertEquals(hsaId, result.getPersonId());
        assertEquals(namn, result.getFullstandigtNamn());
        assertEquals(2, result.getBefattningar().size());
        assertEquals(befattning1, result.getBefattningar().get(0));
        assertEquals(befattning2, result.getBefattningar().get(1));
        assertEquals(2, result.getSpecialiteter().size());
        assertEquals(specialisering1, result.getSpecialiteter().get(0));
        assertEquals(specialisering2, result.getSpecialiteter().get(1));
    }

    private Fk7263Utlatande createUtlatandeFromJson() throws Exception {
        return new CustomObjectMapper().readValue(new ClassPathResource("IntygServiceTest/utlatande.json").getFile(),
            Fk7263Utlatande.class);
    }

}
