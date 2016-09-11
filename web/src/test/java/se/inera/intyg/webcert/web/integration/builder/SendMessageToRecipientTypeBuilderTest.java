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

package se.inera.intyg.webcert.web.integration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToRecipientTypeBuilderTest {

    private static final ArendeAmne AMNE = ArendeAmne.AVSTMN;
    private static final String MEDDELANDE = "meddelande";
    private static final String MEDDELANDE_ID = "meddelandeId";
    private static final String PAMINNELSE_MEDDELANDE_ID = "paminnelseMeddelandeId";
    private static final String PATIENT_PERSON_ID = "patientPersonId";
    private static final String REFERENS_ID = "referensId";
    private static final String RUBRIK = "rubrik";
    private static final LocalDate SISTA_DATUM_FOR_SVAR = LocalDate.now().plusDays(10);
    private static final LocalDateTime SKICKAT_TIDPUNKT = LocalDateTime.now();
    private static final String SVAR_PA_ID = "svarPaId";
    private static final String SVAR_PA_REFERENS = "svarPaReferens";
    private static final String ENHET_ID = "enhetId";
    private static final String INTYG_ID = "intygId";
    private static final String PERSONAL_HSA_ID = "personalHsaId";
    private static final String USER_NAMN = "user namn";
    private static final String USER_FORSKRIVARKOD = "förskrivarkod";
    private static final String ARBETSPLATSKOD = "arbetsplatskod";
    private static final String VARDENHET_EPOST = "test@epost.com";
    private static final String VARDENHET_ENHET_ID = "vardenhet enhetsId";
    private static final String VARDENHET_NAMN = "vardenhet namn";
    private static final String VARDENHET_POSTADRESS = "vardenhet postadress";
    private static final String VARDENHET_POSTNUMMER = "vardenhet postnummer";
    private static final String VARDENHET_POSTORT = "vardenhet postort";
    private static final String VARDENHET_TELEFONNUMMER = "vardenhet telefonnummer";
    private static final String VARDGIVARE_ID = "vardgivare id";
    private static final String VARDGIVARE_NAMN = "vardgivare namn";

    @Test
    public void buildTest() {
        final Arende arende = buildArende();
        final WebCertUser skickatAv = buildWebCertUser();
        final String logiskAdressMottagare = "logical address";
        SendMessageToRecipientType result = SendMessageToRecipientTypeBuilder.build(arende, skickatAv, logiskAdressMottagare);

        assertNotNull(result);
        assertEquals(logiskAdressMottagare, result.getLogiskAdressMottagare());
        assertEquals(AMNE.name(), result.getAmne().getCode());
        assertEquals(AMNE.getDescription(), result.getAmne().getDisplayName());
        assertNotNull(result.getAmne().getCodeSystem());
        assertEquals(MEDDELANDE, result.getMeddelande());
        assertEquals(MEDDELANDE_ID, result.getMeddelandeId());
        assertEquals(PAMINNELSE_MEDDELANDE_ID, result.getPaminnelseMeddelandeId());
        assertEquals(PATIENT_PERSON_ID, result.getPatientPersonId().getExtension());
        assertNotNull(result.getPatientPersonId().getRoot());
        assertEquals(REFERENS_ID, result.getReferensId());
        assertEquals(RUBRIK, result.getRubrik());
        assertEquals(SISTA_DATUM_FOR_SVAR, result.getSistaDatumForSvar());
        assertEquals(SKICKAT_TIDPUNKT, result.getSkickatTidpunkt());
        assertEquals(SVAR_PA_ID, result.getSvarPa().getMeddelandeId());
        assertEquals(SVAR_PA_REFERENS, result.getSvarPa().getReferensId().get(0));
        assertEquals(ENHET_ID, result.getIntygsId().getRoot());
        assertEquals(INTYG_ID, result.getIntygsId().getExtension());
        assertEquals(PERSONAL_HSA_ID, result.getSkickatAv().getPersonalId().getExtension());
        assertNotNull(result.getSkickatAv().getPersonalId().getRoot());
        assertEquals(USER_NAMN, result.getSkickatAv().getFullstandigtNamn());
        assertEquals(USER_FORSKRIVARKOD, result.getSkickatAv().getForskrivarkod());
        assertEquals(ARBETSPLATSKOD, result.getSkickatAv().getEnhet().getArbetsplatskod().getExtension());
        assertNotNull(result.getSkickatAv().getEnhet().getArbetsplatskod().getRoot());
        assertEquals(VARDENHET_EPOST, result.getSkickatAv().getEnhet().getEpost());
        assertEquals(VARDENHET_ENHET_ID, result.getSkickatAv().getEnhet().getEnhetsId().getExtension());
        assertNotNull(result.getSkickatAv().getEnhet().getEnhetsId().getRoot());
        assertEquals(VARDENHET_NAMN, result.getSkickatAv().getEnhet().getEnhetsnamn());
        assertEquals(VARDENHET_POSTADRESS, result.getSkickatAv().getEnhet().getPostadress());
        assertEquals(VARDENHET_POSTNUMMER, result.getSkickatAv().getEnhet().getPostnummer());
        assertEquals(VARDENHET_POSTORT, result.getSkickatAv().getEnhet().getPostort());
        assertEquals(VARDENHET_TELEFONNUMMER, result.getSkickatAv().getEnhet().getTelefonnummer());
        assertEquals(VARDGIVARE_ID, result.getSkickatAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        assertNotNull(result.getSkickatAv().getEnhet().getVardgivare().getVardgivareId().getRoot());
        assertEquals(VARDGIVARE_NAMN, result.getSkickatAv().getEnhet().getVardgivare().getVardgivarnamn());
    }

    @Test
    public void buildQuestionTest() {
        final Arende arende = buildArende();
        arende.setSvarPaId(null); // question
        final WebCertUser skickatAv = buildWebCertUser();
        final String logiskAdressMottagare = "logical address";
        SendMessageToRecipientType result = SendMessageToRecipientTypeBuilder.build(arende, skickatAv, logiskAdressMottagare);

        assertNotNull(result);
        assertEquals(AMNE.name(), result.getAmne().getCode());
        assertNull(result.getSvarPa());
    }

    private Arende buildArende() {
        Arende arende = new Arende();
        arende.setAmne(AMNE);
        arende.setEnhetId(ENHET_ID);
        arende.setIntygsId(INTYG_ID);
        arende.setMeddelande(MEDDELANDE);
        arende.setMeddelandeId(MEDDELANDE_ID);
        arende.setPaminnelseMeddelandeId(PAMINNELSE_MEDDELANDE_ID);
        arende.setPatientPersonId(PATIENT_PERSON_ID);
        arende.setReferensId(REFERENS_ID);
        arende.setRubrik(RUBRIK);
        arende.setSistaDatumForSvar(SISTA_DATUM_FOR_SVAR);
        arende.setSkickatTidpunkt(SKICKAT_TIDPUNKT);
        arende.setSvarPaId(SVAR_PA_ID);
        arende.setSvarPaReferens(SVAR_PA_REFERENS);
        return arende;
    }

    private WebCertUser buildWebCertUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId(PERSONAL_HSA_ID);
        user.setNamn(USER_NAMN);
        user.setForskrivarkod(USER_FORSKRIVARKOD);
        Vardenhet valdVardenhet = new Vardenhet();
        valdVardenhet.setArbetsplatskod(ARBETSPLATSKOD);
        valdVardenhet.setEpost(VARDENHET_EPOST);
        valdVardenhet.setId(VARDENHET_ENHET_ID);
        valdVardenhet.setNamn(VARDENHET_NAMN);
        valdVardenhet.setPostadress(VARDENHET_POSTADRESS);
        valdVardenhet.setPostnummer(VARDENHET_POSTNUMMER);
        valdVardenhet.setPostort(VARDENHET_POSTORT);
        valdVardenhet.setTelefonnummer(VARDENHET_TELEFONNUMMER);
        Vardgivare valdVardgivare = new Vardgivare();
        valdVardgivare.setId(VARDGIVARE_ID);
        valdVardgivare.setNamn(VARDGIVARE_NAMN);
        user.setValdVardenhet(valdVardenhet);
        user.setValdVardgivare(valdVardgivare);
        return user;
    }
}
