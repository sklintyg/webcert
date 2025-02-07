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
package se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.csintegration.patient.GetCertificatesWithQAFromCertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCareWithQAResponderImplTest {

    private static final String CERTIFICATE_ID_FROM_CS = "certificateIdFromCS";
    private static final String CERTIFICATE_ID_FROM_WC = "certificateIdFromWC";
    private static final String REFERENCE = "reference";
    @Mock
    private NotificationService notificationService;
    @Mock
    private GetCertificatesWithQAFromCertificateService getCertificatesWithQAFromCertificateService;
    @Mock
    private IntygService intygService;

    @InjectMocks
    private ListCertificatesForCareWithQAResponderImpl responder;

    @Test
    public void testListCertificatesForCareWithQA() {
        final var personnummer = Personnummer.createPersonnummer("191212121212").get();
        final var enhet = "enhetHsaId";
        final var deadline = LocalDate.of(2017, 1, 1);
        Handelse handelse = new Handelse();
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setTimestamp(LocalDateTime.now());
        handelse.setAmne(ArendeAmne.AVSTMN);
        handelse.setSistaDatumForSvar(deadline);
        handelse.setIntygsId(CERTIFICATE_ID_FROM_WC);
        final var notifications = List.of(handelse);

        doReturn(notifications).when(notificationService).findNotifications(any(IntygWithNotificationsRequest.class));
        doReturn(
            List.of(
                new IntygWithNotificationsResponse(null, List.of(handelse), new ArendeCount(1, 1, 1, 1),
                    new ArendeCount(2, 2, 2, 2), REFERENCE)))
            .when(intygService).listCertificatesForCareWithQA(eq(notifications));

        final var request = getListCertificatesForCareWithQATypeRequest(personnummer, enhet);

        final var response = responder.listCertificatesForCareWithQA("logicalAdress", request);

        assertNotNull(response);
        assertNotNull(response.getList());
        assertNotNull(response.getList().getItem());
        assertEquals(1, response.getList().getItem().size());
        assertEquals(1, response.getList().getItem().get(0).getHandelser().getHandelse().size());

        assertEquals(REFERENCE, response.getList().getItem().get(0).getRef());
        assertEquals(deadline, response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getSistaDatumForSvar());
        assertEquals(HandelsekodEnum.SKAPAT.name(),
            response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getHandelsekod().getCode());
        assertEquals(ArendeAmne.AVSTMN.name(), response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getAmne().getCode());
    }

    @Test
    public void testListCertificatesForCareWithQAFromCertificateService() {
        final var expectedListItem = new ListItem();
        final var personnummer = Personnummer.createPersonnummer("191212121212").get();
        final var enhet = "enhetHsaId";
        final var deadline = LocalDate.of(2017, 1, 1);
        Handelse handelse = new Handelse();
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setTimestamp(LocalDateTime.now());
        handelse.setAmne(ArendeAmne.AVSTMN);
        handelse.setSistaDatumForSvar(deadline);
        handelse.setIntygsId(CERTIFICATE_ID_FROM_WC);
        final var notifications = List.of(handelse);

        final var intyg = new Intyg();
        final var intygsId = new IntygId();
        intygsId.setExtension(CERTIFICATE_ID_FROM_CS);
        intyg.setIntygsId(intygsId);

        expectedListItem.setIntyg(intyg);

        doReturn(notifications).when(notificationService).findNotifications(any(IntygWithNotificationsRequest.class));
        doReturn(List.of(expectedListItem)).when(getCertificatesWithQAFromCertificateService).get(notifications);

        final var request = getListCertificatesForCareWithQATypeRequest(personnummer, enhet);

        final var response = responder.listCertificatesForCareWithQA("logicalAdress", request);

        assertEquals(expectedListItem, response.getList().getItem().get(0));
    }

    @Test
    public void testListCertificatesForCareWithQAWithMergedResult() {
        final var expectedListItem = new ListItem();
        final var personnummer = Personnummer.createPersonnummer("191212121212").get();
        final var enhet = "enhetHsaId";
        final var deadline = LocalDate.of(2017, 1, 1);
        Handelse handelse = new Handelse();
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setTimestamp(LocalDateTime.now());
        handelse.setAmne(ArendeAmne.AVSTMN);
        handelse.setSistaDatumForSvar(deadline);
        handelse.setIntygsId(CERTIFICATE_ID_FROM_WC);
        final var notifications = List.of(handelse);

        final var intyg = new Intyg();
        final var intygsId = new IntygId();
        intygsId.setExtension(CERTIFICATE_ID_FROM_CS);
        intyg.setIntygsId(intygsId);

        expectedListItem.setIntyg(intyg);

        doReturn(notifications).when(notificationService).findNotifications(any(IntygWithNotificationsRequest.class));
        doReturn(List.of(expectedListItem)).when(getCertificatesWithQAFromCertificateService).get(notifications);
        doReturn(
            List.of(
                new IntygWithNotificationsResponse(null, List.of(handelse), new ArendeCount(1, 1, 1, 1),
                    new ArendeCount(2, 2, 2, 2), REFERENCE)))
            .when(intygService).listCertificatesForCareWithQA(eq(notifications));

        final var request = getListCertificatesForCareWithQATypeRequest(personnummer, enhet);

        final var response = responder.listCertificatesForCareWithQA("logicalAdress", request);

        assertNotNull(response);
        assertNotNull(response.getList());
        assertNotNull(response.getList().getItem());
        assertEquals(2, response.getList().getItem().size());
        assertEquals(1, response.getList().getItem().get(0).getHandelser().getHandelse().size());
        assertEquals(REFERENCE, response.getList().getItem().get(0).getRef());
        assertEquals(deadline, response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getSistaDatumForSvar());
        assertEquals(HandelsekodEnum.SKAPAT.name(),
            response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getHandelsekod().getCode());
        assertEquals(ArendeAmne.AVSTMN.name(), response.getList().getItem().get(0).getHandelser().getHandelse().get(0).getAmne().getCode());
    }

    private static ListCertificatesForCareWithQAType getListCertificatesForCareWithQATypeRequest(Personnummer personnummer, String enhet) {
        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getPersonnummer());
        request.setPersonId(personId);
        HsaId hsaId = new HsaId();
        hsaId.setExtension(enhet);
        request.getEnhetsId().add(hsaId);
        return request;
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingBothEnhetAndVardgivareShouldThrow() {
        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension("191212121212");
        request.setPersonId(personId);

        responder.listCertificatesForCareWithQA("logicalAdress", request);
    }

    @Test
    public void bothEnhetAndVardgivareExistingShouldNotThrow() {
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
