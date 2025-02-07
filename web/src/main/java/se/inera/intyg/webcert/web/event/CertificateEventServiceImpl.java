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
package se.inera.intyg.webcert.web.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;


@Service
@Transactional
public class CertificateEventServiceImpl implements CertificateEventService {

    private static final String UNKNOWN_USER = "unknown";
    private static final String WEBCERT_USER = "Webcert";

    @Autowired
    CertificateEventRepository certificateEventRepository;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    IntygService intygService;

    @Autowired
    ArendeService arendeService;


    @Override
    public void createCertificateEvent(String certificateId, String user, EventCode eventCode) {
        createCertificateEvent(certificateId, user, eventCode, eventCode.getDescription());
    }

    @Override
    public void createCertificateEvent(String certificateId, String user, EventCode eventCode, String message) {
        if (message == null || message.isEmpty()) {
            save(certificateId, user, eventCode, LocalDateTime.now(), eventCode.getDescription());
        } else {
            save(certificateId, user, eventCode, LocalDateTime.now(), message);
        }
    }

    @Override
    public void createCertificateEventFromCopyUtkast(Utkast certificate, String user, EventCode eventCode, String originalCertificateId) {
        String message = getMessageForCertificateEvent(eventCode, originalCertificateId);
        save(certificate.getIntygsId(), user, eventCode, LocalDateTime.now(), message);
    }


    @Override
    public List<CertificateEvent> getCertificateEvents(String certificateId) {
        final var events = certificateEventRepository.findByCertificateId(certificateId);

        if (events.isEmpty()) {
            return addEventsForCertificate(certificateId);
        }

        final var intygContentHolder = intygService.fetchIntygDataForInternalUse(certificateId, true);
        if (intygContentHolder == null) {
            return events;
        }

        final var latestTimestamp = events.stream()
            .map(CertificateEvent::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        final var eventsFromArende = createEventsFromArende(certificateId, latestTimestamp);
        events.addAll(eventsFromArende);

        if (shouldCreateSentEvent(events, intygContentHolder)) {
            events.add(createSentEvent(certificateId, sentStatus(intygContentHolder)));
        }

        return events;
    }

    private Status sentStatus(IntygContentHolder intygContentHolder) {
        return intygContentHolder.getStatuses().stream()
            .filter(status -> status.getType() == CertificateState.SENT)
            .findFirst().orElseThrow();
    }

    private boolean shouldCreateSentEvent(List<CertificateEvent> events, IntygContentHolder intygContentHolder) {
        return missingSentEvent(events) && containsSentStatus(intygContentHolder);
    }

    private boolean missingSentEvent(List<CertificateEvent> events) {
        return events.stream().noneMatch(certificateEvent -> certificateEvent.getEventCode() == EventCode.SKICKAT);
    }

    private boolean containsSentStatus(IntygContentHolder intygContentHolder) {
        return intygContentHolder.getStatuses() != null
            && intygContentHolder.getStatuses().stream()
            .anyMatch(status -> status.getType() == CertificateState.SENT);
    }

    private List<CertificateEvent> addEventsForCertificate(String certificateId) {
        List<CertificateEvent> events = new ArrayList<>();
        Utkast certificate = utkastRepository.findById(certificateId).orElse(null);

        if (certificate != null) {
            events = createEventsFromUtkast(certificate);
        }

        if (events.isEmpty()) {
            IntygContentHolder intygContentHolder = intygService.fetchIntygDataForInternalUse(certificateId, true);
            events = createEventsFromIntygContentHolder(certificateId, intygContentHolder);
        }
        return events;
    }

    private List<CertificateEvent> createEventsFromUtkast(Utkast certificate) {
        List<CertificateEvent> events = new ArrayList<>();

        createEventFromCreation(certificate).ifPresent(events::add);
        events.addAll(createEventsFromState(certificate));
        createEventFromSent(certificate).ifPresent(events::add);
        events.addAll(createEventsFromArende(certificate.getIntygsId()));

        return events;
    }

    private List<CertificateEvent> createEventsFromIntygContentHolder(String certificateId, IntygContentHolder certificate) {
        List<CertificateEvent> events = new ArrayList<>();

        createEventFromCreation(certificateId, certificate).ifPresent(events::add);
        createEventFromSigned(certificateId, certificate.getUtlatande()).ifPresent(events::add);
        events.addAll(createEventsFromStatuses(certificateId, certificate));
        events.addAll(createEventsFromArende(certificateId));

        return events;
    }

    private Optional<CertificateEvent> createEventFromCreation(Utkast certificate) {
        if (certificate.getSkapad() != null) {
            String createdBy = certificate.getSkapadAv() != null ? certificate.getSkapadAv().getHsaId() : UNKNOWN_USER;

            if (certificate.getRelationKod() != null && certificate.getRelationIntygsId() != null) {
                EventCode code = getEventCode(certificate.getRelationKod());
                String relationAsMessage = getMessageForCertificateEvent(code, certificate.getRelationIntygsId());
                CertificateEvent savedEvent = save(certificate.getIntygsId(), createdBy, code, certificate.getSkapad(), relationAsMessage);
                return Optional.of(savedEvent);
            } else {
                CertificateEvent savedEvent = save(certificate.getIntygsId(), createdBy, EventCode.SKAPAT, certificate.getSkapad());
                return Optional.of(savedEvent);
            }
        }
        return Optional.empty();
    }

    private Optional<CertificateEvent> createEventFromCreation(String certificateId, IntygContentHolder certificate) {
        if (certificate.getCreated() != null) {
            GrundData grundData = certificate.getUtlatande().getGrundData();

            if (certificate.getRelations() != null && certificate.getRelations().getParent() != null) {
                WebcertCertificateRelation parent = certificate.getRelations().getParent();
                EventCode code = getEventCode(parent.getRelationKod());
                String relation = getMessageForCertificateEvent(code, parent.getIntygsId());
                CertificateEvent savedEvent = save(certificateId, grundData.getSkapadAv().getPersonId(), code, certificate.getCreated(),
                    relation);
                return Optional.of(savedEvent);
            } else {
                String skapadAv = grundData.getSkapadAv() != null ? grundData.getSkapadAv().getPersonId() : UNKNOWN_USER;
                EventCode code = EventCode.SKAPAT;
                CertificateEvent savedEvent = save(certificateId, skapadAv, code, certificate.getCreated(), code.getDescription());
                return Optional.of(savedEvent);
            }
        }
        return Optional.empty();
    }

    private List<CertificateEvent> createEventsFromState(Utkast certificate) {
        List<CertificateEvent> events = new ArrayList<>();
        String certificateId = certificate.getIntygsId();

        if (certificate.getStatus() == UtkastStatus.DRAFT_COMPLETE) {
            CertificateEvent savedEvent = save(certificateId, WEBCERT_USER, EventCode.KFSIGN, certificate.getKlartForSigneringDatum());
            events.add(savedEvent);
        } else if (certificate.getStatus() == UtkastStatus.SIGNED) {
            Signatur signature = certificate.getSignatur();
            CertificateEvent savedEvent = save(certificateId, signature.getSigneradAv(), EventCode.SIGNAT, signature.getSigneringsDatum(),
                "Certificate type: " + certificate.getIntygsTyp());
            events.add(savedEvent);
            if (certificate.getAterkalladDatum() != null) {
                savedEvent = save(certificateId, WEBCERT_USER, EventCode.MAKULERAT, certificate.getAterkalladDatum());
                events.add(savedEvent);
            }
        }
        return events;
    }

    private Optional<CertificateEvent> createEventFromSigned(String certificateId, Utlatande utlatande) {
        GrundData grundData = utlatande.getGrundData();

        if (grundData.getSigneringsdatum() != null) {
            String signedBy = grundData.getSkapadAv().getPersonId();
            CertificateEvent savedEvent = save(certificateId, signedBy, EventCode.SIGNAT, grundData.getSigneringsdatum(),
                utlatande.getTyp());
            return Optional.of(savedEvent);
        }
        return Optional.empty();
    }


    private Optional<CertificateEvent> createEventFromSent(Utkast certificate) {
        if (certificate.getSkickadTillMottagareDatum() != null) {
            CertificateEvent savedEvent = save(certificate.getIntygsId(), WEBCERT_USER, EventCode.SKICKAT,
                certificate.getSkickadTillMottagareDatum(),
                "Recipient: " + certificate.getSkickadTillMottagare());
            return Optional.of(savedEvent);
        }
        return Optional.empty();
    }

    private List<CertificateEvent> createEventsFromStatuses(String certificateId, IntygContentHolder certificate) {
        List<CertificateEvent> events = new ArrayList<>();

        if (!CollectionUtils.isEmpty(certificate.getStatuses())) {
            for (Status status : certificate.getStatuses()) {
                if (status.getType().name().equals("SENT") && status.getTimestamp() != null) {
                    CertificateEvent savedEvent = createSentEvent(certificateId, status);
                    events.add(savedEvent);
                }
            }
        }
        return events;
    }

    private CertificateEvent createSentEvent(String certificateId, Status status) {
        String message = "Recipient: " + status.getTarget();
        return save(certificateId, UNKNOWN_USER, EventCode.SKICKAT, status.getTimestamp(), message);
    }

    private List<CertificateEvent> createEventsFromArende(String certificateId) {
        return createEventsFromArende(certificateId, null);
    }

    private List<CertificateEvent> createEventsFromArende(String certificateId, LocalDateTime afterTimestamp) {
        List<CertificateEvent> events = new ArrayList<>();
        List<Arende> messages;

        if (afterTimestamp != null) {
            final var afterTimestampWithExtraSecond = addASecondToDealWithTimingIssue(afterTimestamp);
            messages = arendeService.getArendenInternal(certificateId).stream()
                .filter(m -> m.getTimestamp().isAfter(afterTimestampWithExtraSecond))
                .collect(Collectors.toList());
        } else {
            messages = arendeService.getArendenInternal(certificateId);
        }

        if (!messages.isEmpty()) {

            for (Arende message : messages) {
                String sentBy = message.getSkickatAv() != null ? message.getSkickatAv() : UNKNOWN_USER;
                if (message.getSvarPaId() != null || message.getSvarPaReferens() != null) {
                    events.add(save(certificateId, sentBy, EventCode.NYSVFM, message.getTimestamp(),
                        getTitleForMessage(message, EventCode.NYSVFM)));
                } else {
                    if (message.getPaminnelseMeddelandeId() != null) {
                        events.add(save(certificateId, sentBy, EventCode.PAMINNELSE, message.getTimestamp()));
                    } else if (message.getAmne() == ArendeAmne.KOMPLT) {
                        events.add(save(certificateId, sentBy, EventCode.KOMPLBEGARAN, message.getTimestamp()));
                    } else {
                        events.add(save(certificateId, sentBy, EventCode.NYFRFM, message.getTimestamp(),
                            getTitleForMessage(message, EventCode.NYFRFM)));
                    }
                }
            }
        }

        return events;
    }

    /**
     * The afterTimestamp is defined based on the latest CertificateEvent created. When compared to the timestamp on the Arende, it can
     * falsely consider it as 'before' even if it logically happened at the same time. This is due to a timing issue and because the
     * precision of the two LocalDateTime differs. The Arende timestamp doesn't include milliseconds but the event does.
     */
    private LocalDateTime addASecondToDealWithTimingIssue(LocalDateTime afterTimestamp) {
        return afterTimestamp.plusSeconds(1L);
    }

    private String getTitleForMessage(Arende message, EventCode code) {
        return message.getAmne() != null ? message.getAmne().getDescription() : code.getDescription();
    }


    private CertificateEvent save(String certificateId, String user, EventCode eventCode, LocalDateTime eventTimestamp) {
        return save(certificateId, user, eventCode, eventTimestamp, eventCode.getDescription());
    }

    private CertificateEvent save(String certificateId, String user, EventCode eventCode, LocalDateTime eventTimestamp, String message) {
        CertificateEvent event = new CertificateEvent();
        event.setCertificateId(certificateId);
        event.setUser(user);
        event.setEventCode(eventCode);
        event.setTimestamp(eventTimestamp);
        event.setMessage(message);

        return certificateEventRepository.save(event);
    }

    private EventCode getEventCode(RelationKod relationKod) {
        switch (relationKod) {
            case ERSATT:
                return EventCode.ERSATTER;
            case KOMPLT:
                return EventCode.KOMPLETTERAR;
            case FRLANG:
                return EventCode.FORLANGER;
            case KOPIA:
                return EventCode.KOPIERATFRAN;
        }
        return null;
    }

    private String getMessageForCertificateEvent(EventCode event, String originalId) {
        switch (event) {
            case ERSATTER:
                return "Replaces " + originalId;
            case KOMPLETTERAR:
                return "Complements " + originalId;
            case FORLANGER:
                return "Renews " + originalId;
            case SKAPATFRAN:
                return "Created from " + originalId;
            case KOPIERATFRAN:
                return "Copied from " + originalId;
            default:
        }
        return null;
    }

}
