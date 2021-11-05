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

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventTypeDTO;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@ExtendWith(MockitoExtension.class)
class GetCertificateEventsFacadeServiceImplTest {

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private CertificateEventService certificateEventService;

    @InjectMocks
    private GetCertificateEventsFacadeServiceImpl getCertificateEventsFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";

    private CertificateEvent certificateEvent;
    private Relations relations;
    private List<CertificateEvent> certificateEvents;

    @BeforeEach
    void setup() {
        certificateEvent = new CertificateEvent();
        certificateEvent.setCertificateId(CERTIFICATE_ID);
        certificateEvent.setId(1000L);
        certificateEvent.setMessage("Messsage");
        certificateEvent.setTimestamp(LocalDateTime.now());
        certificateEvent.setUser("UserId");
        certificateEvent.setEventCode(EventCode.SKAPAT);

        certificateEvents = new ArrayList<>(Arrays.asList(certificateEvent));

        doReturn(certificateEvents)
            .when(certificateEventService)
            .getCertificateEvents(CERTIFICATE_ID);

        relations = new Relations();

        doReturn(relations)
            .when(certificateRelationService)
            .getRelations(CERTIFICATE_ID);
    }

    @Test
    void shallIncludeOneEvent() {

        final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

        assertEquals(1, actualEvents.length);
    }

    @Test
    void shallIncludeTimestamp() {

        final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

        assertEquals(certificateEvent.getTimestamp(), actualEvents[0].getTimestamp());
    }

    @Test
    void shallIncludeCertificateId() {

        final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

        assertEquals(certificateEvent.getCertificateId(), actualEvents[0].getCertificateId());
    }

    @Test
    void shallReturnInSortedOrder() {
        final var expectedFirst = CertificateEventTypeDTO.CREATED;
        final var expectedSecond = CertificateEventTypeDTO.LOCKED;
        final var expectedThird = CertificateEventTypeDTO.REVOKED;

        certificateEvents.clear();
        certificateEvents.addAll(Arrays.asList(
            getEvent(2000L, LocalDateTime.now().plus(14, ChronoUnit.DAYS), EventCode.LAST),
            getEvent(3000L, LocalDateTime.now().plus(15, ChronoUnit.DAYS), EventCode.MAKULERAT),
            getEvent(1000L, LocalDateTime.now(), EventCode.SKAPAT)
        ));

        final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

        assertAll(
            () -> assertEquals(expectedFirst, actualEvents[0].getType()),
            () -> assertEquals(expectedSecond, actualEvents[1].getType()),
            () -> assertEquals(expectedThird, actualEvents[2].getType())
        );
    }

    @Test
    /**
     * EventCode.RELINTYGMAKULE is not relevant for the user and will be filtered out.
     */
    void shallExcludeRelIntygMakuleEvent() {
        certificateEvent.setEventCode(EventCode.RELINTYGMAKULE);

        final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

        assertEquals(0, actualEvents.length);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class EventTypes {

        void setup() {
            final var parentRelation = new WebcertCertificateRelation(
                "ParentCertificateId",
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            relations.setParent(parentRelation);
        }

        Stream<Arguments> eventTypes() {
            return Stream.of(
                Arguments.of(EventCode.SKAPAT, CertificateEventTypeDTO.CREATED),
                Arguments.of(EventCode.LAST, CertificateEventTypeDTO.LOCKED),
                Arguments.of(EventCode.SIGNAT, CertificateEventTypeDTO.SIGNED),
                Arguments.of(EventCode.SKICKAT, CertificateEventTypeDTO.SENT),
                Arguments.of(EventCode.MAKULERAT, CertificateEventTypeDTO.REVOKED),
                Arguments.of(EventCode.ERSATTER, CertificateEventTypeDTO.REPLACES),
                Arguments.of(EventCode.FORLANGER, CertificateEventTypeDTO.EXTENDED),
                Arguments.of(EventCode.KOPIERATFRAN, CertificateEventTypeDTO.COPIED_FROM),
                Arguments.of(EventCode.NYFRFM, CertificateEventTypeDTO.INCOMING_MESSAGE),
                Arguments.of(EventCode.NYFRFV, CertificateEventTypeDTO.OUTGOING_MESSAGE),
                Arguments.of(EventCode.NYSVFM, CertificateEventTypeDTO.INCOMING_ANSWER),
                Arguments.of(EventCode.HANFRFM, CertificateEventTypeDTO.INCOMING_MESSAGE_HANDLED),
                Arguments.of(EventCode.HANFRFV, CertificateEventTypeDTO.OUTGOING_MESSAGE_HANDLED),
                Arguments.of(EventCode.PAMINNELSE, CertificateEventTypeDTO.INCOMING_MESSAGE_REMINDER),
                Arguments.of(EventCode.KOMPLBEGARAN, CertificateEventTypeDTO.REQUEST_FOR_COMPLEMENT),
                Arguments.of(EventCode.KOMPLETTERAR, CertificateEventTypeDTO.COMPLEMENTS),
                Arguments.of(EventCode.SKAPATFRAN, CertificateEventTypeDTO.CREATED_FROM)
            );
        }

        /**
         * Event types that should be decorated are specifically tested else-where.
         */
        Stream<EventCode> eventTypesNotToDecorate() {
            return Stream.of(
                EventCode.SKAPAT,
                EventCode.LAST,
                EventCode.SIGNAT,
                EventCode.SKICKAT,
                EventCode.MAKULERAT,
                EventCode.NYFRFM,
                EventCode.NYFRFV,
                EventCode.HANFRFV,
                EventCode.HANFRFM,
                EventCode.NYSVFM,
                EventCode.PAMINNELSE,
                EventCode.KOMPLBEGARAN,
                EventCode.SKAPATFRAN
            );
        }

        @ParameterizedTest
        @MethodSource("eventTypes")
        void shallIncludeCorrectType(EventCode eventCode, CertificateEventTypeDTO expectedType) {
            certificateEvent.setEventCode(eventCode);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedType, actualEvents[0].getType());
        }

        @ParameterizedTest
        @MethodSource("eventTypesNotToDecorate")
        void shallNotDecorateWithParentInformation(EventCode eventCode) {
            certificateEvent.setEventCode(eventCode);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertAll(
                () -> assertNull(actualEvents[0].getRelatedCertificateId(), "Should not include related certificate id"),
                () -> assertNull(actualEvents[0].getRelatedCertificateStatus(), "Should not include related certificate status")
            );
        }
    }

    @Nested
    class AvailableForPatient {

        @Test
        void shallIncludeEventWhenSigned() {
            certificateEvent.setEventCode(EventCode.SIGNAT);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            final var optionalCertificateEventDTO = Arrays.stream(actualEvents)
                .filter(event -> event.getType() == CertificateEventTypeDTO.AVAILABLE_FOR_PATIENT)
                .findAny();

            assertTrue(optionalCertificateEventDTO.isPresent());
        }

        @Test
        void shallIncludeSameTimestampAsSignedEvent() {
            certificateEvent.setEventCode(EventCode.SIGNAT);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            final var availableForPatientEvent = getEvent(actualEvents, CertificateEventTypeDTO.AVAILABLE_FOR_PATIENT);

            final var signedEvent = getEvent(actualEvents, CertificateEventTypeDTO.SIGNED);

            assertEquals(signedEvent.getTimestamp(), availableForPatientEvent.getTimestamp());
        }

        @Test
        void shallIncludeSameCertificateIdAsSignedEvent() {
            certificateEvent.setEventCode(EventCode.SIGNAT);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            final var availableForPatientEvent = getEvent(actualEvents, CertificateEventTypeDTO.AVAILABLE_FOR_PATIENT);

            final var signedEvent = getEvent(actualEvents, CertificateEventTypeDTO.SIGNED);

            assertEquals(signedEvent.getCertificateId(), availableForPatientEvent.getCertificateId());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReplacesEvent {

        private static final String PARENT_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation parentRelation;

        @BeforeEach
        void setup() {
            certificateEvent.setEventCode(EventCode.ERSATTER);

            parentRelation = new WebcertCertificateRelation(
                PARENT_CERTIFICATE_ID,
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            relations.setParent(parentRelation);
        }

        @Test
        void shallIncludeParentCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(PARENT_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        Stream<Arguments> parentStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> parentRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);
            relations.getParent().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ExtendedEvent {

        private static final String PARENT_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation parentRelation;

        @BeforeEach
        void setup() {
            certificateEvent.setEventCode(EventCode.FORLANGER);

            parentRelation = new WebcertCertificateRelation(
                PARENT_CERTIFICATE_ID,
                RelationKod.FRLANG,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            relations.setParent(parentRelation);
        }

        @Test
        void shallIncludeParentCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(PARENT_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        Stream<Arguments> parentStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> parentRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);
            relations.getParent().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CopiedFromEvent {

        private static final String PARENT_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation parentRelation;

        @BeforeEach
        void setup() {
            certificateEvent.setEventCode(EventCode.KOPIERATFRAN);

            parentRelation = new WebcertCertificateRelation(
                PARENT_CERTIFICATE_ID,
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            relations.setParent(parentRelation);
        }

        @Test
        void shallIncludeParentCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(PARENT_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        Stream<Arguments> parentStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> parentRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);
            relations.getParent().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReplacedByCertificateEvent {

        private static final String CHILD_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation childRelation;

        @BeforeEach
        void setup() {
            certificateEvents.clear();

            childRelation = new WebcertCertificateRelation(
                CHILD_CERTIFICATE_ID,
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            final var latestChildRelation = new FrontendRelations();
            latestChildRelation.setReplacedByIntyg(childRelation);

            relations.setLatestChildRelations(latestChildRelation);
        }

        @Test
        void shallIncludeChildCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CHILD_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        @Test
        void shallIncludeTimestampFromChild() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(childRelation.getSkapad(), actualEvents[0].getTimestamp());
        }

        @Test
        void shallIncludeTypeOfReplaced() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CertificateEventTypeDTO.REPLACED, actualEvents[0].getType());
        }

        Stream<Arguments> childStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("childStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getReplacedByIntyg().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> childRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("childRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getReplacedByIntyg().setStatus(parentStatus);
            relations.getLatestChildRelations().getReplacedByIntyg().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReplacedByDraftEvent {

        private static final String CHILD_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation childRelation;

        @BeforeEach
        void setup() {
            certificateEvents.clear();

            childRelation = new WebcertCertificateRelation(
                CHILD_CERTIFICATE_ID,
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.DRAFT_INCOMPLETE,
                false);

            final var latestChildRelation = new FrontendRelations();
            latestChildRelation.setReplacedByUtkast(childRelation);

            relations.setLatestChildRelations(latestChildRelation);
        }

        @Test
        void shallIncludeChildCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CHILD_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        @Test
        void shallIncludeTimestampFromChild() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(childRelation.getSkapad(), actualEvents[0].getTimestamp());
        }

        @Test
        void shallIncludeTypeOfReplaced() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CertificateEventTypeDTO.REPLACED, actualEvents[0].getType());
        }

        Stream<Arguments> childStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("childStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getReplacedByUtkast().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> childRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("childRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getReplacedByUtkast().setStatus(parentStatus);
            relations.getLatestChildRelations().getReplacedByUtkast().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CopiedByEvent {

        private static final String CHILD_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation childRelation;

        @BeforeEach
        void setup() {
            certificateEvents.clear();

            childRelation = new WebcertCertificateRelation(
                CHILD_CERTIFICATE_ID,
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.DRAFT_INCOMPLETE,
                false);

            final var latestChildRelation = new FrontendRelations();
            latestChildRelation.setUtkastCopy(childRelation);

            relations.setLatestChildRelations(latestChildRelation);
        }

        @Test
        void shallIncludeChildCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CHILD_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        @Test
        void shallIncludeTimestampFromChild() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(childRelation.getSkapad(), actualEvents[0].getTimestamp());
        }

        @Test
        void shallIncludeTypeOfCopiedBy() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CertificateEventTypeDTO.COPIED_BY, actualEvents[0].getType());
        }

        Stream<Arguments> childStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("childStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getUtkastCopy().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> childRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("childRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getUtkastCopy().setStatus(parentStatus);
            relations.getLatestChildRelations().getUtkastCopy().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ComplementedCertificateEvent {

        private static final String CHILD_CERTIFICATE_ID = "ChildCertificateId";
        private WebcertCertificateRelation childRelation;

        @BeforeEach
        void setup() {
            certificateEvents.clear();

            childRelation = new WebcertCertificateRelation(
                CHILD_CERTIFICATE_ID,
                RelationKod.KOMPLT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            final var latestChildRelation = new FrontendRelations();
            latestChildRelation.setComplementedByIntyg(childRelation);

            relations.setLatestChildRelations(latestChildRelation);
        }

        @Test
        void shallIncludeChildCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CHILD_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        @Test
        void shallIncludeTimestampFromChild() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(childRelation.getSkapad(), actualEvents[0].getTimestamp());
        }

        @Test
        void shallIncludeTypeOfComplemented() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(CertificateEventTypeDTO.COMPLEMENTED, actualEvents[0].getType());
        }

        Stream<Arguments> childStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("childStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getComplementedByIntyg().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> childRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("childRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getLatestChildRelations().getComplementedByIntyg().setStatus(parentStatus);
            relations.getLatestChildRelations().getComplementedByIntyg().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ComplementsEvent {

        private static final String PARENT_CERTIFICATE_ID = "ParentCertificateId";
        private WebcertCertificateRelation parentRelation;

        @BeforeEach
        void setup() {
            certificateEvent.setEventCode(EventCode.ERSATTER);

            parentRelation = new WebcertCertificateRelation(
                PARENT_CERTIFICATE_ID,
                RelationKod.KOMPLT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false);

            relations.setParent(parentRelation);
        }

        @Test
        void shallIncludeParentCertificateId() {
            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(PARENT_CERTIFICATE_ID, actualEvents[0].getRelatedCertificateId());
        }

        Stream<Arguments> parentStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.SIGNED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED),
                Arguments.of(UtkastStatus.DRAFT_INCOMPLETE, CertificateStatus.UNSIGNED),
                Arguments.of(UtkastStatus.DRAFT_COMPLETE, CertificateStatus.UNSIGNED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentStatuses")
        void shallIncludeParentStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }

        Stream<Arguments> parentRevokedStatuses() {
            return Stream.of(
                Arguments.of(UtkastStatus.SIGNED, CertificateStatus.REVOKED),
                Arguments.of(UtkastStatus.DRAFT_LOCKED, CertificateStatus.LOCKED_REVOKED)
            );
        }

        @ParameterizedTest
        @MethodSource("parentRevokedStatuses")
        void shallIncludeParentRevokedStatus(UtkastStatus parentStatus, CertificateStatus expectedStatus) {
            relations.getParent().setStatus(parentStatus);
            relations.getParent().setMakulerat(true);

            final var actualEvents = getCertificateEventsFacadeService.getCertificateEvents(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualEvents[0].getRelatedCertificateStatus());
        }
    }

    private CertificateEventDTO getEvent(CertificateEventDTO[] actualEvents, CertificateEventTypeDTO type) {
        return Arrays.stream(actualEvents)
            .filter(event -> event.getType() == type)
            .findAny()
            .get();
    }

    private CertificateEvent getEvent(long id, LocalDateTime timestamp, EventCode eventCode) {
        final var event = new CertificateEvent();
        event.setCertificateId(CERTIFICATE_ID);
        event.setId(id);
        event.setMessage("Messsage");
        event.setTimestamp(timestamp);
        event.setUser("UserId");
        event.setEventCode(eventCode);
        return event;
    }
}