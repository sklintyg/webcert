/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.common.support.common.enumerations.EventKod;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;
import se.inera.intyg.webcert.persistence.event.repository.UtkastEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;


@Service
@Transactional
public class UtkastEventServiceImpl implements UtkastEventService {

    private static final String OKAND_ANVANDARE = "okänd";
    private static final String WEBCERT_ANVANDARE = "Webcert";

    @Autowired
    UtkastEventRepository utkastEventRepository;

    @Autowired
    UtkastService utkastService;

    @Autowired
    IntygService intygService;

    @Autowired
    ArendeService arendeService;


    @Override
    public void createUtkastEvent(String intygsId, String user, EventKod eventKod) {
        createUtkastEvent(intygsId, user, eventKod, eventKod.getKlartext());
    }

    @Override
    public void createUtkastEvent(String intygsId, String user, EventKod eventKod, String meddelande) {
        if (meddelande.isEmpty()) {
            save(intygsId, user, eventKod, LocalDateTime.now(), eventKod.getKlartext());
        } else {
            save(intygsId, user, eventKod, LocalDateTime.now(), meddelande);
        }
    }

    @Override
    public void createUtkastEventFromCopyUtkast(Utkast utkast, String user, EventKod eventKod, String originalIntygsId) {
        String meddelande = getMeddelandeForUtkastEvent(eventKod, originalIntygsId);
        save(utkast.getIntygsId(), user, eventKod, LocalDateTime.now(), meddelande);
    }


    @Override
    public List<UtkastEvent> getUtkastEvents(String intygsId, String intygsTyp) {

        List<UtkastEvent> events = utkastEventRepository.findByIntygsId(intygsId);
        if (events.isEmpty()) {
            events = addEventsForUtkastIntyg(intygsId, intygsTyp);
        }
        return events;
    }

    private List<UtkastEvent> addEventsForUtkastIntyg(String intygsId, String intygsTyp) {
        List<UtkastEvent> events = new ArrayList<>();
        Optional<Utkast> utkast = utkastService.getOptionalDraft(intygsId, intygsTyp, true);

        if (utkast.isPresent()) {
            events = createEventsForUtkast(utkast.get());
            return events;
        }

        if (events.isEmpty()) {
            IntygContentHolder intygContentHolder = intygService.fetchIntygData(intygsId, intygsTyp, false);
            events = createEventsForIntyg(intygsId, intygContentHolder);
            return events;
        }
        return events;
    }


    private List<UtkastEvent> createEventsForUtkast(Utkast utkast) {
        List<UtkastEvent> events = new ArrayList<>();
        String utkastId = utkast.getIntygsId();

        if (utkast.getSkapad() != null) {
            String skapadAv = utkast.getSkapadAv() != null ? utkast.getSkapadAv().getHsaId() : OKAND_ANVANDARE;
            if (utkast.getRelationKod() != null && utkast.getRelationIntygsId() != null) {
                EventKod kod = getEventKod(utkast.getRelationKod());
                String relationAsMeddelande = getMeddelandeForUtkastEvent(kod, utkast.getRelationIntygsId());
                UtkastEvent savedEvent = save(utkastId, skapadAv, kod, utkast.getSkapad(), relationAsMeddelande);
                events.add(savedEvent);
            } else {
                UtkastEvent savedEvent = save(utkastId, skapadAv, EventKod.SKAPAT, utkast.getSkapad());
                events.add(savedEvent);
            }
        }

        if (utkast.getStatus() == UtkastStatus.DRAFT_COMPLETE) {
            UtkastEvent savedEvent = save(utkastId, WEBCERT_ANVANDARE, EventKod.KFSIGN, utkast.getKlartForSigneringDatum());
            events.add(savedEvent);
        } else if (utkast.getStatus() == UtkastStatus.SIGNED) {
            Signatur signatur = utkast.getSignatur();
            UtkastEvent savedEvent = save(utkastId, signatur.getSigneradAv(), EventKod.SIGNAT, signatur.getSigneringsDatum());
            events.add(savedEvent);
            if (utkast.getAterkalladDatum() != null) {
                savedEvent = save(utkastId, WEBCERT_ANVANDARE, EventKod.MAKULERAT, utkast.getAterkalladDatum());
                events.add(savedEvent);
            }
        }

        if (utkast.getSkickadTillMottagareDatum() != null) {
            UtkastEvent savedEvent = save(utkastId, WEBCERT_ANVANDARE, EventKod.SKICKAT, utkast.getSkickadTillMottagareDatum(),
                "Mottagare: " + utkast.getSkickadTillMottagare());
            events.add(savedEvent);
        }

        events.addAll(createEventsFromArende(utkast.getIntygsId()));

        return events;
    }

    private List<UtkastEvent> createEventsForIntyg(String intygsId, IntygContentHolder intyg) {
        List<UtkastEvent> events = new ArrayList<>();
        GrundData grundData = intyg.getUtlatande().getGrundData();

        if (intyg.getCreated() != null) {
            if (intyg.getRelations() != null && intyg.getRelations().getParent() != null) {
                WebcertCertificateRelation parent = intyg.getRelations().getParent();
                EventKod kod = getEventKod(parent.getRelationKod());
                String relation = getMeddelandeForUtkastEvent(kod, parent.getIntygsId());
                UtkastEvent savedEvent = save(intygsId, grundData.getSkapadAv().getPersonId(), kod, intyg.getCreated(), relation);
                events.add(savedEvent);
            } else {
                String skapadAv = grundData.getSkapadAv() != null ? grundData.getSkapadAv().getPersonId() : OKAND_ANVANDARE;
                EventKod kod = EventKod.SKAPAT;
                UtkastEvent savedEvent = save(intygsId, skapadAv, kod, intyg.getCreated(), kod.getKlartext());
                events.add(savedEvent);
            }
        }

        if (grundData.getSigneringsdatum() != null) {
            String signeratAv = grundData.getSkapadAv().getPersonId();
            EventKod kod = EventKod.SIGNAT;
            UtkastEvent savedEvent = save(intygsId, signeratAv, kod, grundData.getSigneringsdatum(), kod.getKlartext());
            events.add(savedEvent);
        }

        if (!CollectionUtils.isEmpty(intyg.getStatuses())) {
            for (Status status : intyg.getStatuses()) {
                if (status.getType().name().equals("SENT") && status.getTimestamp() != null) {
                    String meddelande = "Mottagare: " + status.getTarget();
                    UtkastEvent savedEvent = save(intygsId, OKAND_ANVANDARE, EventKod.SKICKAT, status.getTimestamp(), meddelande);
                    events.add(savedEvent);
                }
            }
        }

        events.addAll(createEventsFromArende(intygsId));

        return events;
    }

    private List<UtkastEvent> createEventsFromArende(String id) {

        List<Arende> arenden = arendeService.getArendenInternal(id);
        List<UtkastEvent> events = new ArrayList<>();
        if (!arenden.isEmpty()) {

            for (Arende arende : arenden) {
                if (arende.getStatus() == se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION) {
                    if (arende.getAmne() == ArendeAmne.KOMPLT) {
                        String skickatAv = arende.getSkickatAv() != null ? arende.getSkickatAv() : OKAND_ANVANDARE;
                        events
                            .add(save(id, skickatAv, EventKod.NYFRFM, arende.getTimestamp(), "Komplettering"));
                    }
                    if (arende.getAmne() == ArendeAmne.PAMINN) {
                        String skickatAv = arende.getSkickatAv() != null ? arende.getSkickatAv() : OKAND_ANVANDARE;
                        events
                            .add(save(id, skickatAv, EventKod.NYFRFM, arende.getTimestamp(), "Påminnelse"));
                    }
                }
            }
        }
        return events;
    }

    private UtkastEvent save(String intygsId, String user, EventKod eventKod, LocalDateTime eventTimestamp) {
        return save(intygsId, user, eventKod, eventTimestamp, eventKod.getKlartext());
    }

    private UtkastEvent save(String intygsId, String user, EventKod eventKod, LocalDateTime eventTimestamp, String meddelande) {
        UtkastEvent event = new UtkastEvent();
        event.setIntygsId(intygsId);
        event.setAnvandare(user);
        event.setEventKod(eventKod);
        event.setTimestamp(eventTimestamp);
        event.setMeddelande(meddelande);

        return utkastEventRepository.save(event);
    }

    private EventKod getEventKod(RelationKod relationKod) {
        switch (relationKod) {
            case ERSATT:
                return EventKod.ERSATTER;
            case KOMPLT:
                return EventKod.KOMPLETTERAR;
            case FRLANG:
                return EventKod.FORLANGER;
            case KOPIA:
                return EventKod.SKAPATFRAN;
        }
        return null;
    }

    private String getMeddelandeForUtkastEvent(EventKod event, String originalId) {
        switch (event) {
            case ERSATTER:
                return "Ersätter " + originalId;
            case KOMPLETTERAR:
                return "Kompletterar " + originalId;
            case FORLANGER:
                return "Förlänger " + originalId;
            case SKAPATFRAN:
                return "Skapat från " + originalId;
        }
        return null;
    }

}
