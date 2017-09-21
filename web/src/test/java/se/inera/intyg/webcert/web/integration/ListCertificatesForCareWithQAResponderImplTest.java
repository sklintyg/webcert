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
package se.inera.intyg.webcert.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCareWithQAResponderImplTest {

    @Mock
    private IntygService intygService;

    @InjectMocks
    private ListCertificatesForCareWithQAResponderImpl responder;

    @Test
    public void testListCertificatesForCareWithQA() {
        final Personnummer personnummer = new Personnummer("191212121212");
        final String enhet = "enhetHsaId";
        final LocalDate deadline = LocalDate.of(2017, 1, 1);
        final String reference = "ref";
        Handelse handelse = new Handelse();
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setTimestamp(LocalDateTime.now());
        handelse.setAmne(ArendeAmne.AVSTMN);
        handelse.setSistaDatumForSvar(deadline);

        when(intygService.listCertificatesForCareWithQA(any(IntygWithNotificationsRequest.class))).thenReturn(Arrays.asList(
                new IntygWithNotificationsResponse(null, Arrays.asList(handelse), new ArendeCount(1, 1, 1, 1),
                        new ArendeCount(2, 2, 2, 2), reference)));

        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getPersonnummer());
        request.setPersonId(personId);
        HsaId hsaId = new HsaId();
        hsaId.setExtension(enhet);
        request.getEnhetsId().add(hsaId);

        ListCertificatesForCareWithQAResponseType response = responder.listCertificatesForCareWithQA("logicalAdress", request);

        assertNotNull(response);
        assertNotNull(response.getList());
        assertNotNull(response.getList().getItem());
        assertEquals(1, response.getList().getItem().size());
        assertEquals(1, response.getList().getItem().get(0).getHandelser().getHandelse().size());

        assertEquals(reference, response.getList().getItem().get(0).getRef());
        assertEquals(deadline, response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getSistaDatumForSvar());
        assertEquals(HandelsekodEnum.SKAPAT.name(),
                response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getHandelsekod().getCode());
        assertEquals(ArendeAmne.AVSTMN.name(), response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getAmne().getCode());
    }

    @Test
    public void missingBothEnhetAndVardgivareShouldNotThrow() {
        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension("191212121212");
        request.setPersonId(personId);

        responder.listCertificatesForCareWithQA("logicalAdress", request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bothEnhetAndVardgivareExistingShouldThrow() {
        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension("191212121212");
        request.setPersonId(personId);
        HsaId hsaId = new HsaId();
        hsaId.setExtension("enhetId");
        request.getEnhetsId().add(hsaId);
        HsaId vardgivarId = new HsaId();
        hsaId.setExtension("vardgivarId");
        request.setVardgivarId(vardgivarId);

        responder.listCertificatesForCareWithQA("logicalAdress", request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPersonnummerShouldThrow() {
        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        HsaId hsaId = new HsaId();
        hsaId.setExtension("enhetId");
        request.getEnhetsId().add(hsaId);
        HsaId vardgivarId = new HsaId();
        hsaId.setExtension("vardgivarId");
        request.setVardgivarId(vardgivarId);

        responder.listCertificatesForCareWithQA("logicalAdress", request);
    }
}
