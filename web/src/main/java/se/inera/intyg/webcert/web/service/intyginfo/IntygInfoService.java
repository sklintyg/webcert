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
package se.inera.intyg.webcert.web.service.intyginfo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;

@Service("getCertificateAdminInfoFromWC")
@RequiredArgsConstructor
public class IntygInfoService implements IntygInfoServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(IntygInfoService.class);

    private final UtkastRepository utkastRepository;
    private final ArendeService arendeService;
    private final FragaSvarRepository fragaSvarRepository;
    private final IntygModuleRegistry moduleRegistry;
    private final GetIntygInfoEventsService getIntygInfoEventsService;
    private final CertificateRelationService relationService;

    @Value("${job.utkastlock.locked.after.day}")
    private int lockedAfterDay;

    @Transactional(readOnly = true)
    public Optional<WcIntygInfo> getIntygInfo(String intygId) {
        Utkast utkast = utkastRepository.findById(intygId).orElse(null);

        WcIntygInfo response = new WcIntygInfo();

        final var events = getIntygInfoEventsService.get(intygId);
        response.getEvents().addAll(events);
        boolean foundArenden = addArendeInformation(intygId, response);

        // Utkast not found but may have arenden (created in other system) or handelser (deleted utkast)
        if (utkast == null) {
            if (!events.isEmpty() || foundArenden) {
                response.setIntygId(intygId);

                return Optional.of(response);
            }

            return Optional.empty();
        }

        response.setCreatedInWC(events.stream().anyMatch(
            event -> event.getType() == IntygInfoEventType.IS101
                || event.getType() == IntygInfoEventType.IS102
                || event.getType() == IntygInfoEventType.IS103
        ));
        response.setIntygId(utkast.getIntygsId());
        response.setIntygType(utkast.getIntygsTyp());
        response.setIntygVersion(utkast.getIntygTypeVersion());

        response.setTestCertificate(utkast.isTestIntyg());

        response.setDraftCreated(utkast.getSkapad());

        response.setSentToRecipient(utkast.getSkickadTillMottagareDatum());

        response.setCareUnitHsaId(utkast.getEnhetsId());
        response.setCareUnitName(utkast.getEnhetsNamn());

        response.setCareGiverHsaId(utkast.getVardgivarId());
        response.setCareGiverName(utkast.getVardgivarNamn());

        if (utkast.getSignatur() != null) {
            Signatur signatur = utkast.getSignatur();

            response.setSignedDate(signatur.getSigneringsDatum());
            response.setSignedByHsaId(signatur.getSigneradAv());

            try {
                ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
                Utlatande utlatande = moduleApi.getUtlatandeFromJson(utkast.getModel());

                response.setSignedByName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());

            } catch (ModuleNotFoundException | ModuleException | IOException e) {
                LOG.error("Module not found for intyg " + intygId, e);
            }
        }

        addEvents(utkast, response);
        addRelations(intygId, response);

        return Optional.of(response);
    }

    private void addRelations(String intygsId, WcIntygInfo response) {
        List<WebcertCertificateRelation> relations = relationService.findChildRelations(intygsId);

        relations.forEach(relation -> {
            IntygInfoEventType type = null;

            switch (relation.getRelationKod()) {
                case FRLANG:
                    type = IntygInfoEventType.IS007;
                    break;
                case ERSATT:
                    type = IntygInfoEventType.IS008;
                    break;
                case KOMPLT:
                    type = IntygInfoEventType.IS014;
                    break;
                case KOPIA:
                    type = IntygInfoEventType.IS026;
                    break;
            }

            if (type != null) {
                IntygInfoEvent relationEvent = createEvent(relation.getSkapad(), type, "intygsId", relation.getIntygsId());

                Utkast relatedIntyg = utkastRepository.findById(relation.getIntygsId()).orElse(null);
                if (relatedIntyg != null) {
                    relationEvent.addData("name", relatedIntyg.getSkapadAv().getNamn());
                    relationEvent.addData("hsaId", relatedIntyg.getSkapadAv().getHsaId());
                }

                response.getEvents().add(relationEvent);
            }
        });
    }

    private void addEvents(Utkast utkast, WcIntygInfo response) {
        List<IntygInfoEvent> events = response.getEvents();

        // Created by
        IntygInfoEvent createdBy = createEvent(utkast.getSkapad(), IntygInfoEventType.IS001);
        createdBy.addData("hsaId", utkast.getSkapadAv().getHsaId());
        createdBy.addData("name", utkast.getSkapadAv().getNamn());
        events.add(createdBy);

        // Created from
        if (utkast.getRelationKod() != null) {
            IntygInfoEventType type = null;

            switch (utkast.getRelationKod()) {
                case FRLANG:
                    type = IntygInfoEventType.IS019;
                    break;
                case ERSATT:
                    type = IntygInfoEventType.IS020;
                    break;
                case KOMPLT:
                    type = IntygInfoEventType.IS021;
                    break;
                case KOPIA:
                    type = IntygInfoEventType.IS022;
                    break;
            }

            events.add(createEvent(utkast.getSkapad(), type, "intygsId", utkast.getRelationIntygsId()));
        }

        // Locked
        if (UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
            IntygInfoEvent locked = new IntygInfoEvent(Source.WEBCERT);
            if (utkast.getSkapad() != null) {
                locked.setDate(utkast.getSkapad().plusDays(lockedAfterDay));
            }
            locked.setType(IntygInfoEventType.IS003);
            events.add(locked);
        }

        // Signed
        if (UtkastStatus.SIGNED.equals(utkast.getStatus())) {
            events.add(createEvent(
                utkast.getSignatur().getSigneringsDatum(),
                IntygInfoEventType.IS004,
                "hsaId", utkast.getSignatur().getSigneradAv()));
        } else {
            // Last saved
            IntygInfoEvent changed = createEvent(utkast.getSenastSparadDatum(), IntygInfoEventType.IS010);

            if (!Objects.isNull(utkast.getSenastSparadAv())) {
                changed.addData("hsaId", utkast.getSenastSparadAv().getHsaId());
                changed.addData("name", utkast.getSenastSparadAv().getNamn());
            }
            events.add(changed);
        }

        // Klart för signering
        if (utkast.getKlartForSigneringDatum() != null) {
            events.add(createEvent(utkast.getKlartForSigneringDatum(), IntygInfoEventType.IS018));
        }

        // Makulerad
        if (utkast.getAterkalladDatum() != null) {
            events.add(createEvent(utkast.getAterkalladDatum(), IntygInfoEventType.IS009));
        }

        // Skickat
        if (utkast.getSkickadTillMottagareDatum() != null) {
            events.add(createEvent(utkast.getSkickadTillMottagareDatum(), IntygInfoEventType.IS006,
                "intygsmottagare", utkast.getSkickadTillMottagare()));
        }
    }

    private boolean addArendeInformation(String intygId, WcIntygInfo response) {
        List<Arende> arenden = arendeService.getArendenInternal(intygId);
        List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygId);

        List<Arende> arendeList = fragaSvarList.stream().map(fragaSvar -> {
            Arende arende = new Arende();
            arende.setStatus(fragaSvar.getStatus());

            Optional<ArendeAmne> arendeAmne = ArendeAmne.fromAmne(fragaSvar.getAmne());

            if (arendeAmne.isPresent()) {
                arende.setAmne(arendeAmne.get());
            }

            arende.setSkickatAv(fragaSvar.getFrageStallare());
            arende.setSkickatTidpunkt(fragaSvar.getFrageSkickadDatum());

            if (fragaSvar.getInternReferens() != null) {
                arende.setIntygTyp(fragaSvar.getIntygsReferens().getIntygsTyp());
            }

            return arende;
        }).collect(Collectors.toList());

        arendeList.addAll(arenden);

        // Kompletteringar
        List<Arende> kompletteringarQuestions = arendeList.stream()
            .filter(a -> ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.FRAGA))
            .collect(Collectors.toList());
        long answered = kompletteringarQuestions.stream().filter(a -> Status.CLOSED.equals(a.getStatus())).count();

        response.setKompletteringar(kompletteringarQuestions.size());
        response.setKompletteringarAnswered((int) answered);

        Set<Status> answeredOrClosed = Stream.of(Status.ANSWERED, Status.CLOSED).collect(Collectors.toSet());
        Set<ArendeType> fragaOrPaminnelse = Stream.of(ArendeType.FRAGA, ArendeType.PAMINNELSE).collect(Collectors.toSet());

        List<Arende> adminQuestions = arendeList.stream()
            .filter(a -> !ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> fragaOrPaminnelse.contains(ArendeViewConverter.getArendeType(a)))
            .collect(Collectors.toList());

        // Admin frågor skickade
        List<Arende> adminQuestionsSent = adminQuestions.stream()
            .filter(a -> FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsSentAnswered = adminQuestionsSent.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdministrativaFragorSent(adminQuestionsSent.size());
        response.setAdministrativaFragorSentAnswered((int) adminQuestionsSentAnswered);

        // Admin frågor mottagna
        List<Arende> adminQuestionsRecieved = adminQuestions.stream()
            .filter(a -> !FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsRecievedAnswered = adminQuestionsRecieved.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdministrativaFragorReceived(adminQuestionsRecieved.size());
        response.setAdministrativaFragorReceivedAnswered((int) adminQuestionsRecievedAnswered);

        // Kompletterings begäran
        kompletteringarQuestions.forEach(arende -> {
            response.getEvents()
                .add(createEvent(arende.getSkickatTidpunkt(), IntygInfoEventType.IS011, "intygsmottagare", arende.getSkickatAv()));

            // Hanterad
            if (Status.CLOSED.equals(arende.getStatus())) {
                response.getEvents()
                    .add(createEvent(arende.getSenasteHandelse(), IntygInfoEventType.IS016, "intygsmottagare", arende.getSkickatAv()));
            }
        });
        // kompletterings begäran svar
        arendeList.stream()
            .filter(a -> ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .forEach(arende -> {
                response.getEvents().add(createEvent(arende.getSkickatTidpunkt(), IntygInfoEventType.IS015));
            });

        // Mottagna från intygsmottagare
        adminQuestionsRecieved.forEach(arende -> {
            response.getEvents()
                .add(createEvent(arende.getSkickatTidpunkt(), IntygInfoEventType.IS012, "intygsmottagare", arende.getSkickatAv()));

            // Hanterad
            if (Status.CLOSED.equals(arende.getStatus())) {
                response.getEvents()
                    .add(createEvent(arende.getSenasteHandelse(), IntygInfoEventType.IS017, "intygsmottagare", arende.getSkickatAv()));
            }
        });
        // Besvarade av vården
        arendeList.stream()
            .filter(a -> !ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .filter(a -> FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .forEach(arende -> {
                response.getEvents().add(createEvent(arende.getSkickatTidpunkt(), IntygInfoEventType.IS023));
            });

        // Skickade från vården
        adminQuestionsSent.forEach(arende -> {
            IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS013);

            try {
                event.addData("intygsmottagare", moduleRegistry.getModuleEntryPoint(arende.getIntygTyp()).getDefaultRecipient());
            } catch (ModuleNotFoundException e) {
                LOG.info("Couldn't find moduleEntryPoint", e);
            }

            response.getEvents().add(event);

            // Hanterad
            if (Status.CLOSED.equals(arende.getStatus())) {
                response.getEvents().add(createEvent(arende.getSenasteHandelse(), IntygInfoEventType.IS025));
            }
        });
        // Besvarade av mottagare
        arendeList.stream()
            .filter(a -> !ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .filter(a -> !FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .forEach(arende -> {
                response.getEvents()
                    .add(createEvent(arende.getSkickatTidpunkt(), IntygInfoEventType.IS024, "intygsmottagare", arende.getSkickatAv()));
            });

        return !arendeList.isEmpty();
    }

    private IntygInfoEvent createEvent(LocalDateTime date, IntygInfoEventType type) {
        return createEvent(date, type, null, null);
    }

    private IntygInfoEvent createEvent(LocalDateTime date, IntygInfoEventType type, String key1, String data1) {
        IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, date, type);

        if (!Objects.isNull(key1)) {
            event.addData(key1, data1);
        }

        return event;
    }
}
