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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventTypeDTO;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@Service("getCertificateEventsFromWebcert")
public class GetCertificateEventsFacadeServiceImpl implements GetCertificateEventsFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateEventsFacadeServiceImpl.class);

    private final CertificateRelationService certificateRelationService;

    private final CertificateEventService certificateEventService;

    private final IntygService intygService;

    private final List<EventCode> eventCodesToRemove = Arrays.asList(
        EventCode.RELINTYGMAKULE,
        EventCode.NYFRFM,
        EventCode.NYFRFV,
        EventCode.HANFRFV,
        EventCode.HANFRFM,
        EventCode.NYSVFM,
        EventCode.PAMINNELSE,
        EventCode.KFSIGN
    );

    @Autowired
    public GetCertificateEventsFacadeServiceImpl(
        CertificateRelationService certificateRelationService,
        CertificateEventService certificateEventService,
        GetCertificateFacadeService getCertificateFacadeService, IntygService intygService) {
        this.certificateRelationService = certificateRelationService;
        this.certificateEventService = certificateEventService;
        this.intygService = intygService;
    }

    @Override
    public CertificateEventDTO[] getCertificateEvents(String certificateId) {
        LOG.debug("Retrieve events to convert for certificate '{}'", certificateId);
        final var eventsToConvert = certificateEventService.getCertificateEvents(certificateId);

        LOG.debug("Retrieve relations to other certificates for certificate '{}'", certificateId);
        final var relations = certificateRelationService.getRelations(certificateId);

        final var certificateType = intygService.getIntygTypeInfo(certificateId).getIntygType();

        LOG.debug("Convert events for certificate '{}'", certificateId);
        return convert(certificateId, eventsToConvert, relations, certificateType);
    }

    private CertificateEventDTO[] convert(String certificateId, List<CertificateEvent> eventsToConvert,
        Relations relations, String certificateType) {
        final var events = getCertificateEvents(eventsToConvert);

        decorateCertificateEventsWithParentInfo(events, relations.getParent());

        addAvailableForPatientIfNeeded(events, certificateType);

        addEventsBasedOnChildRelations(events, relations.getLatestChildRelations(), certificateId);

        return sortAndReturnArray(events);
    }

    private CertificateEventDTO[] sortAndReturnArray(List<CertificateEventDTO> events) {
        return events.stream()
            .sorted(Comparator.comparing(CertificateEventDTO::getTimestamp))
            .toArray(CertificateEventDTO[]::new);
    }

    private void addEventsBasedOnChildRelations(List<CertificateEventDTO> events, FrontendRelations childRelations, String certificateId) {
        addEventForChildRelation(events, childRelations.getReplacedByUtkast(), CertificateEventTypeDTO.REPLACED, certificateId);
        addEventForChildRelation(events, childRelations.getReplacedByIntyg(), CertificateEventTypeDTO.REPLACED, certificateId);
        addEventForChildRelation(events, childRelations.getUtkastCopy(), CertificateEventTypeDTO.COPIED_BY, certificateId);
        addEventForChildRelation(events, childRelations.getComplementedByUtkast(), CertificateEventTypeDTO.COMPLEMENTED, certificateId);
        addEventForChildRelation(events, childRelations.getComplementedByIntyg(), CertificateEventTypeDTO.COMPLEMENTED, certificateId);
    }

    private void addEventForChildRelation(List<CertificateEventDTO> events, WebcertCertificateRelation relation,
        CertificateEventTypeDTO type, String certificateId) {
        if (relation == null) {
            return;
        }

        final var event = new CertificateEventDTO();
        event.setCertificateId(certificateId);
        event.setType(type);
        event.setTimestamp(relation.getSkapad());
        event.setRelatedCertificateId(relation.getIntygsId());
        event.setRelatedCertificateStatus(
            getRelatedCertificateStatus(relation)
        );

        events.add(event);
    }

    private CertificateStatus getRelatedCertificateStatus(WebcertCertificateRelation relation) {
        if (relation.isMakulerat()) {
            if (relation.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                return CertificateStatus.LOCKED_REVOKED;
            }
            return CertificateStatus.REVOKED;
        }

        switch (relation.getStatus()) {
            case SIGNED:
                return CertificateStatus.SIGNED;
            case DRAFT_LOCKED:
                return CertificateStatus.LOCKED;
            case DRAFT_INCOMPLETE:
            case DRAFT_COMPLETE:
                return CertificateStatus.UNSIGNED;
            default:
                throw new IllegalArgumentException("Cannot convert status: " + relation.getStatus());
        }
    }

    private void addAvailableForPatientIfNeeded(List<CertificateEventDTO> events, String certificateType) {
        if (certificateType.equals(DbModuleEntryPoint.MODULE_ID) || certificateType.equals(DoiModuleEntryPoint.MODULE_ID)) {
            return;
        }

        final var signedEvent = events.stream()
            .filter(certificateEventDTO -> certificateEventDTO.getType() == CertificateEventTypeDTO.SIGNED)
            .findAny();

        if (signedEvent.isEmpty()) {
            return;
        }

        final var availableForPatientEvent = new CertificateEventDTO();
        availableForPatientEvent.setType(CertificateEventTypeDTO.AVAILABLE_FOR_PATIENT);
        availableForPatientEvent.setTimestamp(signedEvent.get().getTimestamp());
        availableForPatientEvent.setCertificateId(signedEvent.get().getCertificateId());

        events.add(availableForPatientEvent);
    }

    private List<CertificateEventDTO> getCertificateEvents(List<CertificateEvent> events) {
        return events.stream()
            .filter(this::shouldBeIncluded)
            .map(this::convertCertificateEvent)
            .collect(Collectors.toList());
    }

    private boolean shouldBeIncluded(CertificateEvent event) {
        return !eventCodesToRemove.contains(event.getEventCode());
    }

    private CertificateEventDTO convertCertificateEvent(CertificateEvent eventToConvert) {
        final var event = new CertificateEventDTO();
        event.setCertificateId(eventToConvert.getCertificateId());
        event.setTimestamp(eventToConvert.getTimestamp());
        event.setType(
            getCertificateEventType(eventToConvert.getEventCode())
        );
        return event;
    }

    private CertificateEventTypeDTO getCertificateEventType(EventCode eventCode) {
        switch (eventCode) {
            case SKAPAT:
                return CertificateEventTypeDTO.CREATED;
            case LAST:
                return CertificateEventTypeDTO.LOCKED;
            case SIGNAT:
                return CertificateEventTypeDTO.SIGNED;
            case SKICKAT:
                return CertificateEventTypeDTO.SENT;
            case MAKULERAT:
                return CertificateEventTypeDTO.REVOKED;
            case ERSATTER:
                return CertificateEventTypeDTO.REPLACES;
            case FORLANGER:
                return CertificateEventTypeDTO.EXTENDED;
            case KOPIERATFRAN:
                return CertificateEventTypeDTO.COPIED_FROM;
            case KOMPLBEGARAN:
                return CertificateEventTypeDTO.REQUEST_FOR_COMPLEMENT;
            case KOMPLETTERAR:
                return CertificateEventTypeDTO.COMPLEMENTS;
            case SKAPATFRAN:
                return CertificateEventTypeDTO.CREATED_FROM;
            default:
                throw new IllegalArgumentException("Cannot map the EventCode: " + eventCode);
        }
    }

    private void decorateCertificateEventsWithParentInfo(List<CertificateEventDTO> events, WebcertCertificateRelation parentRelation) {
        events.forEach(certificateEventDTO -> decorateCertificateEventWithParentInfo(certificateEventDTO, parentRelation));
    }

    private void decorateCertificateEventWithParentInfo(CertificateEventDTO event, WebcertCertificateRelation parentRelation) {
        if (parentRelation == null) {
            return;
        }

        if (!shouldDecorateWithParent(event.getType())) {
            return;
        }

        event.setRelatedCertificateId(parentRelation.getIntygsId());
        event.setRelatedCertificateStatus(
            getRelatedCertificateStatus(parentRelation)
        );
    }

    private boolean shouldDecorateWithParent(CertificateEventTypeDTO eventType) {
        final var eventTypesToDecorateWithParent = Arrays.asList(
            CertificateEventTypeDTO.REPLACES,
            CertificateEventTypeDTO.EXTENDED,
            CertificateEventTypeDTO.COPIED_FROM,
            CertificateEventTypeDTO.COMPLEMENTS
        );
        return eventTypesToDecorateWithParent.contains(eventType);
    }
}
