/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;

@Service
public class IntygInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygInfoService.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private CertificateRelationService relationService;

    @Value("${job.utkastlock.locked.after.day}")
    private int lockedAfterDay;

    @Transactional(readOnly = true)
    public Optional<WcIntygInfo> getIntygInfo(String intygId) {
        Utkast utkast = utkastRepository.findOne(intygId);

        WcIntygInfo response = new WcIntygInfo();

        boolean foundHandelser = addHandelse(intygId, response);

        // Not found but maybe handelser
        if (utkast == null) {
            if (foundHandelser) {
                response.setIntygId(intygId);
            }

            return foundHandelser ? Optional.of(response) : Optional.empty();
        }

        response.setIntygId(utkast.getIntygsId());
        response.setIntygType(utkast.getIntygsTyp());
        response.setIntygVersion(utkast.getIntygTypeVersion());

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

        if (UtkastStatus.SIGNED.equals(utkast.getStatus())) {
            addArendeInformation(response);
        }

        addEvents(utkast, response);
        addRelations(intygId, response);

        return Optional.of(response);
    }

    private boolean addHandelse(String intygsId, WcIntygInfo response) {
        List<Handelse> handelses = handelseRepository.findByIntygsId(intygsId);

        handelses.forEach(handelse -> {
            switch (handelse.getCode()) {

                case SKAPAT:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS101));
                    break;
                case ANDRAT:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS102));
                    break;
                case RADERA:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS103));
                    break;
                case KFSIGN:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS105));
                    break;
                case SIGNAT:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS106));
                    break;
                case SKICKA:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS107));
                    break;
                case MAKULE:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS108));
                    break;
                case NYFRFM:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS109));
                    break;
                case NYFRFV:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS110));
                    break;
                case NYSVFM:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS111));
                    break;
                case HANFRFM:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS112));
                    break;
                case HANFRFV:
                    response.getEvents().add(new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS113));
                    break;
            }
        });

        return !handelses.isEmpty();
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
                default:
            }

            if (type != null && relation.getStatus().equals(UtkastStatus.SIGNED)) {
                IntygInfoEvent relationEvent = new IntygInfoEvent(Source.WEBCERT, relation.getSkapad(), type);
                relationEvent.addData("intygsId", relation.getIntygsId());

                Utkast relatedIntyg = utkastRepository.findOne(relation.getIntygsId());
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
        IntygInfoEvent createdBy = new IntygInfoEvent(Source.WEBCERT, utkast.getSkapad(), IntygInfoEventType.IS001);
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

            IntygInfoEvent relation = new IntygInfoEvent(Source.WEBCERT, utkast.getSkapad(), type);
            relation.addData("intygsId", utkast.getRelationIntygsId());
            events.add(relation);
        }

        // Locked
        if (UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
            IntygInfoEvent locked = new IntygInfoEvent(Source.WEBCERT);
            locked.setDate(utkast.getSkapad().plusDays(lockedAfterDay));
            locked.setType(IntygInfoEventType.IS003);
            events.add(locked);
        }

        // Signed
        if (UtkastStatus.SIGNED.equals(utkast.getStatus())) {
            IntygInfoEvent signed = new IntygInfoEvent(Source.WEBCERT, utkast.getSignatur().getSigneringsDatum(), IntygInfoEventType.IS004);
            signed.addData("hsaId", utkast.getSignatur().getSigneradAv());
            events.add(signed);
        } else {
            // Last saved
            IntygInfoEvent changed = new IntygInfoEvent(Source.WEBCERT, utkast.getSenastSparadDatum(), IntygInfoEventType.IS010);
            changed.addData("hsaId", utkast.getSenastSparadAv().getHsaId());
            changed.addData("name", utkast.getSenastSparadAv().getNamn());
            events.add(changed);
        }

        // Klart för signering
        if (utkast.getKlartForSigneringDatum() != null) {
            events.add(new IntygInfoEvent(Source.WEBCERT, utkast.getKlartForSigneringDatum(), IntygInfoEventType.IS018));
        }

        // Makulerad
        if (utkast.getAterkalladDatum() != null) {
            events.add(new IntygInfoEvent(Source.WEBCERT, utkast.getAterkalladDatum(), IntygInfoEventType.IS009));
        }

        // Skickat
        if (utkast.getSkickadTillMottagareDatum() != null) {
            IntygInfoEvent sent = new IntygInfoEvent(Source.WEBCERT, utkast.getSkickadTillMottagareDatum(), IntygInfoEventType.IS006);
            sent.addData("intygsmottagare", utkast.getSkickadTillMottagare());
            events.add(sent);
        }
    }

    private void addArendeInformation(WcIntygInfo response) {
        List<Arende> arenden = arendeService.getArendenInternal(response.getIntygId());

        // Kompletteringar
        List<Arende> kompletteringarQuestions = arenden.stream()
            .filter(a -> ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.FRAGA))
            .collect(Collectors.toList());
        long answered = kompletteringarQuestions.stream().filter(a -> Status.CLOSED.equals(a.getStatus())).count();

        response.setKomletteingar(kompletteringarQuestions.size());
        response.setKomletteingarAnswered((int) answered);

        Set<Status> answeredOrClosed = Stream.of(Status.ANSWERED, Status.CLOSED).collect(Collectors.toSet());
        Set<ArendeAmne> adminQuestionTypes = Stream.of(ArendeAmne.AVSTMN, ArendeAmne.KONTKT, ArendeAmne.OVRIGT).collect(Collectors.toSet());

        List<Arende> adminQuestions = arenden.stream()
            .filter(a -> adminQuestionTypes.contains(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.FRAGA))
            .collect(Collectors.toList());

        // Admin frågor skickade
        List<Arende> adminQuestionsSent = adminQuestions.stream()
            .filter(a -> FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsSentAnswered = adminQuestionsSent.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdminQuestionsSent(adminQuestionsSent.size());
        response.setAdminQuestionsSentAnswered((int) adminQuestionsSentAnswered);

        // Admin frågor mottagna
        List<Arende> adminQuestionsRecieved = adminQuestions.stream()
            .filter(a -> !FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsRecievedAnswered = adminQuestionsRecieved.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdminQuestionsReceived(adminQuestionsRecieved.size());
        response.setAdminQuestionsReceivedAnswered((int) adminQuestionsRecievedAnswered);

        // Kompletterings begäran
        kompletteringarQuestions.forEach(arende -> {
            IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS011);
            event.addData("intygsmottagare", arende.getSkickatAv());
            response.getEvents().add(event);
        });
        // kompletterings begäran svar
        arenden.stream()
            .filter(a -> ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .forEach(arende -> {
                IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS015);
                response.getEvents().add(event);
            });

        // Mottagna från intygsmottagare
        adminQuestionsRecieved.forEach(arende -> {
            IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS012);
            event.addData("intygsmottagare", arende.getSkickatAv());
            response.getEvents().add(event);

            // Hanterad
            if (Status.CLOSED.equals(arende.getStatus())) {
                IntygInfoEvent event2 = new IntygInfoEvent(Source.WEBCERT, arende.getSenasteHandelse(), IntygInfoEventType.IS017);
                event.addData("intygsmottagare", arende.getSkickatAv());
                response.getEvents().add(event2);
            }
        });
        // Besvarade av vården
        arenden.stream()
            .filter(a -> adminQuestionTypes.contains(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .filter(a -> FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .forEach(arende -> {
                IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS023);
                response.getEvents().add(event);
            });

        // Skickade från vården
        adminQuestionsSent.forEach(arende -> {
            IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS013);
            response.getEvents().add(event);

            // Hanterad
            if (Status.CLOSED.equals(arende.getStatus())) {
                IntygInfoEvent event2 = new IntygInfoEvent(Source.WEBCERT, arende.getSenasteHandelse(), IntygInfoEventType.IS025);
                response.getEvents().add(event2);
            }
        });
        // Besvarade av mottagare
        arenden.stream()
            .filter(a -> adminQuestionTypes.contains(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.SVAR))
            .filter(a -> !FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .forEach(arende -> {
                IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, arende.getSkickatTidpunkt(), IntygInfoEventType.IS024);
                event.addData("intygsmottagare", arende.getSkickatAv());
                response.getEvents().add(event);
            });
    }
}
