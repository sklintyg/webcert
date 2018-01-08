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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificationTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        final String intygsId = "intygsid";

        final LocalDateTime handelsetid = LocalDateTime.now().minusDays(1);
        final HandelsekodEnum handelsetyp = HandelsekodEnum.ANDRAT;

        final int skickadeFragorTotalt = 8;
        final int skickadeFragorHanterade = 7;
        final int skickadeFragorBesvarade = 6;
        final int skickadeFragorEjBesvarade = 5;
        final int mottagnaFragorTotalt = 4;
        final int mottagnaFragorHanterade = 3;
        final int mottagnaFragorBesvarade = 2;
        final int mottagnaFragorEjBesvarade = 1;

        final Intyg intyg = buildIntyg();

        ArendeCount skickadeFragor = new ArendeCount(skickadeFragorTotalt, skickadeFragorEjBesvarade, skickadeFragorBesvarade,
                skickadeFragorHanterade);
        ArendeCount mottagnaFragor = new ArendeCount(mottagnaFragorTotalt, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade,
                mottagnaFragorHanterade);

        NotificationMessage msg = new NotificationMessage(intygsId, "luse", handelsetid, handelsetyp, "address", "", null, skickadeFragor,
                mottagnaFragor, SchemaVersion.VERSION_3, "ref");

        CertificateStatusUpdateForCareType res = NotificationTypeConverter.convert(msg, intyg);

        assertEquals(intyg, res.getIntyg());
        assertEquals(HandelsekodEnum.ANDRAT.value(), res.getHandelse().getHandelsekod().getCode());
        assertEquals(HandelsekodEnum.ANDRAT.description(), res.getHandelse().getHandelsekod().getDisplayName());
        assertEquals(handelsetid, res.getHandelse().getTidpunkt());
        assertNotNull(res.getHandelse().getHandelsekod().getCodeSystem());
        // handelsekod -> codeSystemName is not valid in schema but incorrectly generated in java class
        // therefore we should not populate this field
        assertNull(res.getHandelse().getHandelsekod().getCodeSystemName());

        assertSkickadeFrågor(skickadeFragorTotalt, skickadeFragorHanterade, skickadeFragorBesvarade, skickadeFragorEjBesvarade, res);
        assertMottagnaFragor(mottagnaFragorTotalt, mottagnaFragorHanterade, mottagnaFragorBesvarade, mottagnaFragorEjBesvarade, res);

        // Make sure we have a valid Intyg according to service contract
        assertEquals(NotificationTypeConverter.TEMPORARY_ARBETSPLATSKOD,
                res.getIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
        assertNull(res.getIntyg().getSkapadAv().getEnhet().getEpost());
    }

    @Test
    public void testConvertWhenHandelsekodIsNYFRFM() throws Exception {
        final String intygsId = "intygsid";

        final LocalDateTime handelsetid = LocalDateTime.now().minusDays(1);
        final LocalDate sistaSvarsDatum = LocalDate.now().plusWeeks(3);

        final HandelsekodEnum handelsetyp = HandelsekodEnum.NYFRFM;
        final Amneskod amneskod = AmneskodCreator.create("KOMPLT", "Komplettering");

        final int skickadeFragorTotalt = 8;
        final int skickadeFragorHanterade = 7;
        final int skickadeFragorBesvarade = 6;
        final int skickadeFragorEjBesvarade = 5;
        final int mottagnaFragorTotalt = 4;
        final int mottagnaFragorHanterade = 3;
        final int mottagnaFragorBesvarade = 2;
        final int mottagnaFragorEjBesvarade = 1;

        final Intyg intyg = buildIntyg();

        ArendeCount skickadeFragor = new ArendeCount(skickadeFragorTotalt, skickadeFragorEjBesvarade, skickadeFragorBesvarade,
                skickadeFragorHanterade);
        ArendeCount mottagnaFragor = new ArendeCount(mottagnaFragorTotalt, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade,
                mottagnaFragorHanterade);

        NotificationMessage msg = new NotificationMessage(intygsId, "luse", handelsetid, handelsetyp, "address", "", null,
                skickadeFragor, mottagnaFragor, SchemaVersion.VERSION_3, "ref", amneskod, sistaSvarsDatum);

        CertificateStatusUpdateForCareType res = NotificationTypeConverter.convert(msg, intyg);

        assertEquals(intyg, res.getIntyg());
        assertEquals(HandelsekodEnum.NYFRFM.value(), res.getHandelse().getHandelsekod().getCode());
        assertEquals(HandelsekodEnum.NYFRFM.description(), res.getHandelse().getHandelsekod().getDisplayName());
        assertEquals(handelsetid, res.getHandelse().getTidpunkt());

        assertEquals(sistaSvarsDatum, res.getHandelse().getSistaDatumForSvar());

        assertEquals(amneskod.getCode(), res.getHandelse().getAmne().getCode());
        assertEquals(amneskod.getCodeSystem(), res.getHandelse().getAmne().getCodeSystem());
        assertEquals(amneskod.getDisplayName(), res.getHandelse().getAmne().getDisplayName());

        assertSkickadeFrågor(skickadeFragorTotalt, skickadeFragorHanterade, skickadeFragorBesvarade, skickadeFragorEjBesvarade, res);
        assertMottagnaFragor(mottagnaFragorTotalt, mottagnaFragorHanterade, mottagnaFragorBesvarade, mottagnaFragorEjBesvarade, res);

        // Make sure we have a valid Intyg according to service contract
        assertEquals(NotificationTypeConverter.TEMPORARY_ARBETSPLATSKOD,
                res.getIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
        assertNull(res.getIntyg().getSkapadAv().getEnhet().getEpost());
    }

    @Test
    public void testNotUpdatingExistingValues() {
        final String intygsId = "intygsid";
        final String arbetsplatskod = "ARBETSPLATSKOD";
        final String epost = "EPOST";

        final LocalDateTime handelsetid = LocalDateTime.now().minusDays(1);
        final HandelsekodEnum handelsetyp = HandelsekodEnum.ANDRAT;

        Intyg intyg = buildIntyg();

        Enhet enhet = intyg.getSkapadAv().getEnhet();
        enhet.getArbetsplatskod().setExtension(arbetsplatskod);
        enhet.setEpost(epost);

        NotificationMessage msg = new NotificationMessage(intygsId, "luse", handelsetid, handelsetyp, "address", "", null,
                new ArendeCount(4, 3, 2, 1),
                new ArendeCount(4, 3, 2, 1),
                SchemaVersion.VERSION_3, "ref");

        CertificateStatusUpdateForCareType res = NotificationTypeConverter.convert(msg, intyg);

        assertEquals(arbetsplatskod, res.getIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
        assertEquals(epost, res.getIntyg().getSkapadAv().getEnhet().getEpost());
    }

    private void assertMottagnaFragor(int mottagnaFragorTotalt, int mottagnaFragorHanterade, int mottagnaFragorBesvarade, int mottagnaFragorEjBesvarade, CertificateStatusUpdateForCareType res) {
        assertEquals(mottagnaFragorTotalt, res.getMottagnaFragor().getTotalt());
        assertEquals(mottagnaFragorEjBesvarade, res.getMottagnaFragor().getEjBesvarade());
        assertEquals(mottagnaFragorBesvarade, res.getMottagnaFragor().getBesvarade());
        assertEquals(mottagnaFragorHanterade, res.getMottagnaFragor().getHanterade());
    }

    private void assertSkickadeFrågor(int skickadeFragorTotalt, int skickadeFragorHanterade, int skickadeFragorBesvarade, int skickadeFragorEjBesvarade, CertificateStatusUpdateForCareType res) {
        assertEquals(skickadeFragorTotalt, res.getSkickadeFragor().getTotalt());
        assertEquals(skickadeFragorEjBesvarade, res.getSkickadeFragor().getEjBesvarade());
        assertEquals(skickadeFragorBesvarade, res.getSkickadeFragor().getBesvarade());
        assertEquals(skickadeFragorHanterade, res.getSkickadeFragor().getHanterade());
    }

    private Intyg buildIntyg() {
        Enhet enhet = new Enhet();
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        enhet.setVardgivare(new Vardgivare());
        enhet.setEpost(""); // Not accepted value

        HosPersonal skapadAv = new HosPersonal();
        skapadAv.setEnhet(enhet);

        Intyg intyg = new Intyg();
        intyg.setSkapadAv(skapadAv);

        return intyg;
    }
}
