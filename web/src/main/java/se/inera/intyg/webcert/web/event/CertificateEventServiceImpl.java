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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
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
        if (message.isEmpty()) {
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

        List<CertificateEvent> events = certificateEventRepository.findByCertificateId(certificateId);
        if (events.isEmpty()) {
            events = addEventsForCertificate(certificateId);
        }
        return events;
    }

    private List<CertificateEvent> addEventsForCertificate(String certificateId) {
        List<CertificateEvent> events = new ArrayList<>();
        Utkast certificate = utkastRepository.findById(certificateId).orElse(null);

        if (certificate != null) {
            events = createEventsFromUtkast(certificate);
            return events;
        }

        if (events.isEmpty()) {
            IntygContentHolder intygContentHolder = intygService.fetchIntygDataForInternalUse(certificateId, true);
            events = createEventsFromIntygContentHolder(certificateId, intygContentHolder);
            return events;
        }
        return events;
    }


    private List<CertificateEvent> createEventsFromUtkast(Utkast certificate) {
        List<CertificateEvent> events = new ArrayList<>();
        String certificateId = certificate.getIntygsId();

        if (certificate.getSkapad() != null) {
            String createdBy = certificate.getSkapadAv() != null ? certificate.getSkapadAv().getHsaId() : UNKNOWN_USER;
            if (certificate.getRelationKod() != null && certificate.getRelationIntygsId() != null) {
                EventCode code = getEventCode(certificate.getRelationKod());
                String relationAsMessage = getMessageForCertificateEvent(code, certificate.getRelationIntygsId());
                CertificateEvent savedEvent = save(certificateId, createdBy, code, certificate.getSkapad(), relationAsMessage);
                events.add(savedEvent);
            } else {
                CertificateEvent savedEvent = save(certificateId, createdBy, EventCode.SKAPAT, certificate.getSkapad());
                events.add(savedEvent);
            }
        }

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

        if (certificate.getSkickadTillMottagareDatum() != null) {
            CertificateEvent savedEvent = save(certificateId, WEBCERT_USER, EventCode.SKICKAT, certificate.getSkickadTillMottagareDatum(),
                "Recipient: " + certificate.getSkickadTillMottagare());
            events.add(savedEvent);
        }

        events.addAll(createEventsFromArende(certificate.getIntygsId()));

        return events;
    }

    private List<CertificateEvent> createEventsFromIntygContentHolder(String certificateId, IntygContentHolder certificate) {
        List<CertificateEvent> events = new ArrayList<>();
        GrundData grundData = certificate.getUtlatande().getGrundData();

        if (certificate.getCreated() != null) {
            if (certificate.getRelations() != null && certificate.getRelations().getParent() != null) {
                WebcertCertificateRelation parent = certificate.getRelations().getParent();
                EventCode code = getEventCode(parent.getRelationKod());
                String relation = getMessageForCertificateEvent(code, parent.getIntygsId());
                CertificateEvent savedEvent = save(certificateId, grundData.getSkapadAv().getPersonId(), code, certificate.getCreated(),
                    relation);
                events.add(savedEvent);
            } else {
                String skapadAv = grundData.getSkapadAv() != null ? grundData.getSkapadAv().getPersonId() : UNKNOWN_USER;
                EventCode code = EventCode.SKAPAT;
                CertificateEvent savedEvent = save(certificateId, skapadAv, code, certificate.getCreated(), code.getDescription());
                events.add(savedEvent);
            }
        }

        if (grundData.getSigneringsdatum() != null) {
            String signedBy = grundData.getSkapadAv().getPersonId();
            CertificateEvent savedEvent = save(certificateId, signedBy, EventCode.SIGNAT, grundData.getSigneringsdatum(),
                certificate.getUtlatande().getTyp());
            events.add(savedEvent);
        }

        if (!CollectionUtils.isEmpty(certificate.getStatuses())) {
            for (Status status : certificate.getStatuses()) {
                if (status.getType().name().equals("SENT") && status.getTimestamp() != null) {
                    String message = "Recipient: " + status.getTarget();
                    CertificateEvent savedEvent = save(certificateId, UNKNOWN_USER, EventCode.SKICKAT, status.getTimestamp(), message);
                    events.add(savedEvent);
                }
            }
        }

        events.addAll(createEventsFromArende(certificateId));

        return events;
    }

    private List<CertificateEvent> createEventsFromArende(String id) {

        List<Arende> arenden = arendeService.getArendenInternal(id);
        List<CertificateEvent> events = new ArrayList<>();
        if (!arenden.isEmpty()) {

            for (Arende arende : arenden) {
                if (arende.getStatus() == se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION) {
                    if (arende.getAmne() == ArendeAmne.KOMPLT) {
                        String sentBy = arende.getSkickatAv() != null ? arende.getSkickatAv() : UNKNOWN_USER;
                        events
                            .add(save(id, sentBy, EventCode.NYFRFM, arende.getTimestamp(), ArendeAmne.KOMPLT.getDescription()));
                    }
                    if (arende.getAmne() == ArendeAmne.PAMINN) {
                        String sentBy = arende.getSkickatAv() != null ? arende.getSkickatAv() : UNKNOWN_USER;
                        events
                            .add(save(id, sentBy, EventCode.NYFRFM, arende.getTimestamp(), ArendeAmne.PAMINN.getDescription()));
                    }
                }
            }
        }
        return events;
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
                return EventCode.SKAPATFRAN;
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
        }
        return null;
    }

}
