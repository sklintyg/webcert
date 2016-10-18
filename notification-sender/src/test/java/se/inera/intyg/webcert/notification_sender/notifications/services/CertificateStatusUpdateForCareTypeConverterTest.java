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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import org.junit.Test;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

public class CertificateStatusUpdateForCareTypeConverterTest {

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
        ArendeCount skickadeFragor = new ArendeCount(skickadeFragorTotalt, skickadeFragorEjBesvarade, skickadeFragorBesvarade, skickadeFragorHanterade);
        ArendeCount mottagnaFragor = new ArendeCount(mottagnaFragorTotalt, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade, mottagnaFragorHanterade);

        NotificationMessage msg = new NotificationMessage(intygsId, "luse", handelsetid, handelsetyp, "address", "", null, skickadeFragor, mottagnaFragor,
                SchemaVersion.VERSION_2, "ref");
        CertificateStatusUpdateForCareType res = CertificateStatusUpdateForCareTypeConverter.convert(msg, intyg);

        assertEquals(intyg, res.getIntyg());
        assertEquals(HandelsekodEnum.ANDRAT.value(), res.getHandelse().getHandelsekod().getCode());
        assertEquals(HandelsekodEnum.ANDRAT.description(), res.getHandelse().getHandelsekod().getDisplayName());
        assertEquals(handelsetid, res.getHandelse().getTidpunkt());
        assertNotNull(res.getHandelse().getHandelsekod().getCodeSystem());
        // handelsekod -> codeSystemName is not valid in schema but incorrectly generated in java class
        // therefore we should not populate this field
        assertNull(res.getHandelse().getHandelsekod().getCodeSystemName());
        assertEquals(skickadeFragorTotalt, res.getSkickadeFragor().getTotalt());
        assertEquals(skickadeFragorEjBesvarade, res.getSkickadeFragor().getEjBesvarade());
        assertEquals(skickadeFragorBesvarade, res.getSkickadeFragor().getBesvarade());
        assertEquals(skickadeFragorHanterade, res.getSkickadeFragor().getHanterade());
        assertEquals(mottagnaFragorTotalt, res.getMottagnaFragor().getTotalt());
        assertEquals(mottagnaFragorEjBesvarade, res.getMottagnaFragor().getEjBesvarade());
        assertEquals(mottagnaFragorBesvarade, res.getMottagnaFragor().getBesvarade());
        assertEquals(mottagnaFragorHanterade, res.getMottagnaFragor().getHanterade());

        // Make sure we have a valid Intyg according to service contract
        assertEquals(CertificateStatusUpdateForCareTypeConverter.TEMPORARY_ARBETSPLATSKOD,
                res.getIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
        assertNull(res.getIntyg().getSkapadAv().getEnhet().getEpost());
    }

    @Test
    public void testNotUpdatingExistingValues() {
        final String intygsId = "intygsid";
        final LocalDateTime handelsetid = LocalDateTime.now().minusDays(1);
        final HandelsekodEnum handelsetyp = HandelsekodEnum.ANDRAT;

        Intyg intyg = buildIntyg();
        final String arbetsplatskod = "ARBETSPLATSKOD";
        final String epost = "EPOST";

        Enhet enhet = intyg.getSkapadAv().getEnhet();
        enhet.getArbetsplatskod().setExtension(arbetsplatskod);
        enhet.setEpost(epost);

        NotificationMessage msg = new NotificationMessage(intygsId, "luse", handelsetid, handelsetyp, "address", "", null, new ArendeCount(4, 3, 2, 1),
                new ArendeCount(4, 3, 2, 1),
                SchemaVersion.VERSION_2, "ref");
        CertificateStatusUpdateForCareType res = CertificateStatusUpdateForCareTypeConverter.convert(msg, intyg);

        assertEquals(arbetsplatskod, res.getIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
        assertEquals(epost, res.getIntyg().getSkapadAv().getEnhet().getEpost());

    }

    private Intyg buildIntyg() {
        Intyg intyg = new Intyg();
        HosPersonal skapadAv = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        Vardgivare vardgivare = new Vardgivare();
        enhet.setVardgivare(vardgivare);
        enhet.setEpost(""); // Not accepted value
        skapadAv.setEnhet(enhet);
        intyg.setSkapadAv(skapadAv);
        return intyg;
    }
}
