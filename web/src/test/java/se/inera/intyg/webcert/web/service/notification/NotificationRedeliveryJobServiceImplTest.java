/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.notification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationRedeliveryServiceImpl;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateTypeFormatEnum;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.UnderskriftType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

public class NotificationRedeliveryJobServiceImplTest {

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @InjectMocks
    private NotificationRedeliveryJobServiceImpl notificationRedeliveryJobService;

    private static final String PERSNR = "191212121212";

    @Before
    public void setup() {
        List<NotificationRedelivery> redeliveryList = new ArrayList<>();
        redeliveryList.add(createNotificationRedelivery(createEvent(NotificationDeliveryStatusEnum.FAILURE), "CORRELATION_ID",
            LocalDateTime.now(), 1, null));
    }

    @Test
    public void testInitiateRedeliveryOfNonFailedEvent() {

    }



    private Handelse createEvent(NotificationDeliveryStatusEnum notificationDeliveryStatusEnum) {
        final Handelse event = new Handelse();
        event.setDeliveryStatus(notificationDeliveryStatusEnum);
        event.setCertificateIssuer("CERTIFICATE_ISSUER");
        event.setCertificateType("CERTIFICATE_TYPE");
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setEnhetsId("UNIT_ID");
        event.setHanteratAv("HANDLE_BY");
        event.setId(1000L);
        event.setIntygsId("CERTIFICATE_ID");
        event.setPersonnummer("191212121212");
        event.setTimestamp(LocalDateTime.now());
        event.setVardgivarId("CAREPROVIDER_ID");
        return event;
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, String correlationId,
        LocalDateTime redeliveryTime, int attemptedDeliveries, NotificationRedeliveryStrategy notificationRedeliveryStrategy) {
        final NotificationRedelivery notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setEventId(event.getId());
        notificationRedelivery.setCorrelationId(correlationId);
        notificationRedelivery.setAttemptedDeliveries(attemptedDeliveries);
        if (notificationRedeliveryStrategy != null) {
            notificationRedelivery.setRedeliveryStrategy(notificationRedeliveryStrategy.getName());
        }
        notificationRedelivery.setRedeliveryTime(redeliveryTime);
        return notificationRedelivery;
    }

    private NotificationRedeliveryMessage createNotificationRedeliveryMessageForDraft() {
        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage();
        redeliveryMessage.setReference("REFERENCE");
        redeliveryMessage.setSent(new CertificateMessages());
        redeliveryMessage.setReceived(new CertificateMessages());
        redeliveryMessage.set(createIntyg("lisjp"));
        return redeliveryMessage;
    }

    private NotificationRedeliveryMessage createNotificationRedliveryMessageForCertificate() {
        NotificationRedeliveryMessage redeliveryMessage = createNotificationRedeliveryMessageForDraft();
        redeliveryMessage.getCert().setUnderskrift(new UnderskriftType());
        return redeliveryMessage;
    }

    private Intyg createIntyg(String intygsTyp) {
        return createIntyg(intygsTyp, "1.0", "INTYG_ID");
    }

    private static Intyg createIntyg(String intygsTyp, String intygTypeVersion, String intygsId) {
        Intyg intyg = new Intyg();
        IntygId intygId = new IntygId();
        intygId.setExtension(intygsId);
        intyg.setIntygsId(intygId);

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        intyg.setTyp(typAvIntyg);
        intyg.setVersion(intygTypeVersion);

        intyg.setPatient(buildPatient());

        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setVardgivare(new Vardgivare());
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);
        // DatePeriodType and PartialDateType must be allowed
        intyg.getSvar().add(InternalConverterUtil.aSvar("")
            .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
            .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, Year.of(1999)))
            .build());
        return intyg;
    }

    public static Person buildPerson(boolean sekretessmarkering) {
        return new Person(Personnummer.createPersonnummer(PERSNR).get(),
            sekretessmarkering, false, "Tolvan", "Mellis", "Tolvansson", "Tolvgatan 12", "12121", "Tolvhult");
    }

    public static Patient buildPatient() {
        PersonId personId = new PersonId();
        personId.setExtension(PERSNR);

        Patient patient = new Patient();
        patient.setPersonId(personId);
        patient.setFornamn("");
        patient.setMellannamn("");
        patient.setEfternamn("");
        patient.setPostadress("");
        patient.setPostnummer("");
        patient.setPostort("");

        return patient;
    }

}
