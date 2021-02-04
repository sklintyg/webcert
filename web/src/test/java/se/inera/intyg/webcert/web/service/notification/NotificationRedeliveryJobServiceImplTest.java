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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.common.db.v1.model.internal.DbUtlatandeV1;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.lisjp.v1.rest.LisjpModuleApiV1;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategyStandard;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.UnderskriftType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryJobServiceImplTest {

    private static final String INTYG_TYP_FK = "fk7263";
    private static final String INTYG_ID = "1234";
    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";
    private static final String LOGISK_ADDR = "SE12345678-1234";
    private static final String ENHET_ID = "enhetId";
    private static final String ENHET_NAMN = "enhetName";
    private static final String SIGNED_BY_HSA_ID = "signedByHsaId";
    private static final String ARENDE_ID = "arendeId";
    private static final String VARDGIVAR_ID = "vardgivarId";
    private static final String SKAPAD_AV_HSA_ID = "skapadAvHsaID";
    private static final String SKAPAD_AV_FULL_NAME = "Firstname Lastname";
    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("19121212-1212").orElse(null);
    private static final Long FRAGASVAR_ID = 1L;
    private static final String USER_REFERENCE = "some-ref";

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    private UtkastService draftService;

    @Mock
    private IntygService certificateService;

    @Mock
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    @Mock
    FeaturesHelper featuresHelper;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    LisjpModuleApiV1 lisjpModuleApiV1;

    @Spy
    ObjectMapper objectMapper;


    @InjectMocks
    private NotificationRedeliveryJobServiceImpl notificationRedeliveryJobService;

    private static final String PERSNR = "191212121212";




    @Before
    public void setup() {
        //List<NotificationRedelivery> redeliveryList = new ArrayList<>();
        //redeliveryList.add(createNotificationRedelivery(createEvent(NotificationDeliveryStatusEnum.FAILURE), "CORRELATION_ID",
        //    LocalDateTime.now(), 1, null));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitiateRedeliveryOfDraft() throws IOException {
        Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);
        NotificationRedelivery redelivery = createNotificationRedelivery(event, "CORRELATION_ID", LocalDateTime.now(),
            1, new NotificationRedeliveryStrategyStandard());
        NotificationRedeliveryMessage redeliveryMessage = createNotificationRedeliveryMessageForDraft("lisjp");
        redelivery.setMessage(objectMapper.writeValueAsBytes(redeliveryMessage));

        when(notificationRedeliveryService.getNotificationsForRedelivery()).thenReturn(Collections.singletonList(redelivery));
        when(notificationRedeliveryService.getEventById(any(Long.class))).thenReturn(event);
        when(notificationRedeliveryService.isRedundantRedelivery(any(Handelse.class))).thenReturn(false);

        notificationRedeliveryJobService.resendNotifications();

        verifyNoInteractions(draftService);
        verifyNoInteractions(certificateService);
        verify(jmsTemplate).convertAndSend(any(byte[].class), any(MessagePostProcessor.class));
    }

    @Test
    public void testInitiateRedeliveryOfCertificateFoundInWebcert() throws IOException, ModuleException, ModuleNotFoundException {
        final String Lisjp = "lisjp";
        Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);
        NotificationRedelivery redelivery = createNotificationRedelivery(event, "CORRELATION_ID", LocalDateTime.now(),
            1, new NotificationRedeliveryStrategyStandard());
        NotificationRedeliveryMessage redeliveryMessage = createNotificationRedeliveryMessageForCertificate(Lisjp);
        redelivery.setMessage(objectMapper.writeValueAsBytes(redeliveryMessage));

        when(notificationRedeliveryService.getNotificationsForRedelivery()).thenReturn(Collections.singletonList(redelivery));
        when(notificationRedeliveryService.getEventById(any(Long.class))).thenReturn(event);
        when(notificationRedeliveryService.isRedundantRedelivery(any(Handelse.class))).thenReturn(false);

        when(draftService.getDraft(any(String.class), any(String.class), eq(false))).thenReturn(createUtkast());

        when(moduleRegistry.getModuleIdFromExternalId(any(String.class))).thenReturn(Lisjp);
        when(moduleRegistry.getModuleApi(eq(Lisjp), any())).thenReturn(lisjpModuleApiV1);

        when(lisjpModuleApiV1.getUtlatandeFromJson(any(String.class))).thenReturn(createUtlatande());
        when(lisjpModuleApiV1.getIntygFromUtlatande(any(Utlatande.class))).thenReturn(createIntyg(Lisjp));
        when(featuresHelper.isFeatureActive(any())).thenReturn(true);


        notificationRedeliveryJobService.resendNotifications();

        verify(draftService).getDraft(any(String.class), any(String.class), eq(false));
        verifyNoInteractions(certificateService);
        verify(jmsTemplate).convertAndSend(any(byte[].class), any(MessagePostProcessor.class));
    }

    @Test
    public void testInitiateRedeliveryOfCertificateFoundInIntygstjanst() throws Exception {
        Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);
        NotificationRedelivery redelivery = createNotificationRedelivery(event, "CORRELATION_ID", LocalDateTime.now(),
            1, new NotificationRedeliveryStrategyStandard());
        NotificationRedeliveryMessage redeliveryMessage = createNotificationRedeliveryMessageForCertificate("lisjp");
        redelivery.setMessage(objectMapper.writeValueAsBytes(redeliveryMessage));

        when(notificationRedeliveryService.getNotificationsForRedelivery()).thenReturn(Collections.singletonList(redelivery));
        when(notificationRedeliveryService.getEventById(any(Long.class))).thenReturn(event);
        when(notificationRedeliveryService.isRedundantRedelivery(any(Handelse.class))).thenReturn(false);
        when(draftService.getDraft(any(String.class), any(String.class), eq(false))).thenThrow(WebCertServiceException.class);

        when(certificateService.fetchIntygDataForInternalUse(any(String.class), eq(true))).thenReturn(createIntygContentHolder());

        when(moduleRegistry.getModuleIdFromExternalId(any(String.class))).thenReturn("lisjp");
        when(moduleRegistry.getModuleApi(eq("lisjp"), any())).thenReturn(lisjpModuleApiV1);
        when(lisjpModuleApiV1.getUtlatandeFromJson(any(String.class))).thenReturn(createUtlatande());
        when(lisjpModuleApiV1.getIntygFromUtlatande(any(Utlatande.class))).thenReturn(createIntyg("lisjp"));
        when(featuresHelper.isFeatureActive(any())).thenReturn(true);

        notificationRedeliveryJobService.resendNotifications();

        verify(draftService).getDraft(any(String.class), any(String.class), eq(false));
        verify(certificateService).fetchIntygDataForInternalUse(any(String.class), eq(true));
        verify(jmsTemplate).convertAndSend(any(byte[].class), any(MessagePostProcessor.class));
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

    private NotificationRedeliveryMessage createNotificationRedeliveryMessageForDraft(String draftType) {
        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage();
        redeliveryMessage.setReference("REFERENCE");
        redeliveryMessage.setSent(new CertificateMessages());
        redeliveryMessage.setReceived(new CertificateMessages());
        redeliveryMessage.set(createIntyg(draftType));
        return redeliveryMessage;
    }

    private NotificationRedeliveryMessage createNotificationRedeliveryMessageForCertificate(String certificateType) {
        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage();
        redeliveryMessage.setReference("REFERENCE");
        redeliveryMessage.setSent(new CertificateMessages());
        redeliveryMessage.setReceived(new CertificateMessages());
        Intyg certificate = createIntyg(certificateType);
        certificate.setUnderskrift(new UnderskriftType());
        certificate.getUnderskrift().setSignature(new SignatureType());
        redeliveryMessage.set(certificate);
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
        //intyg.getSvar().add(InternalConverterUtil.aSvar("")
        //    .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
        //    .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, Year.of(1999)))
        //    .build());
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

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setEnhetsId(ENHET_ID);
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel(INTYG_JSON);
        utkast.setPatientPersonnummer(PATIENT_ID);
        utkast.setSkapadAv(new VardpersonReferens(SKAPAD_AV_HSA_ID, SKAPAD_AV_FULL_NAME));
        return utkast;
    }

    private HoSPersonal createHosPersonal() {
        HoSPersonal hosPerson = new HoSPersonal();
        hosPerson.setPersonId("hsaId1");
        hosPerson.setFullstandigtNamn("Doktor A");
        hosPerson.setVardenhet(createVardenhet());
        return hosPerson;
    }

    private Vardenhet createVardenhet() {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid("hsaId");
        vardenhet.setEnhetsnamn("ve1");
        vardenhet.setVardgivare(new se.inera.intyg.common.support.model.common.internal.Vardgivare());
        vardenhet.getVardgivare().setVardgivarid("vg1");
        vardenhet.getVardgivare().setVardgivarnamn("vg1");
        return vardenhet;
    }

    private Utlatande createUtlatande() {
        GrundData gd = new GrundData();
        gd.setPatient(new se.inera.intyg.common.support.model.common.internal.Patient());
        gd.getPatient().setPersonId(Personnummer.createPersonnummer("191212121212").get());
        HoSPersonal skapadAv = createHosPersonal();
        gd.setSkapadAv(skapadAv);
        return DbUtlatandeV1.builder().setId("intygId").setGrundData(gd).setTextVersion("").build();
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));
        Fk7263Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
            "IntygDraftServiceImplTest/utlatande.json").getFile(), Fk7263Utlatande.class);
        return IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(utlatande)
            .setStatuses(status)
            .setRevoked(false)
            .setRelations(new Relations())
            // .setReplacedByRelation(null)
            // .setComplementedByRelation(null)
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();
    }

}
