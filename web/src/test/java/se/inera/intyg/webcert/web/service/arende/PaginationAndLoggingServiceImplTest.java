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

package se.inera.intyg.webcert.web.service.arende;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.PaginationAndLoggingServiceImpl;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class PaginationAndLoggingServiceImplTest {

    private static final WebCertUser WEBCERT_USER = new WebCertUser();
    private static final String TOLVAN_PERSON_ID = "191212121212";
    private static final String LILLTOLVAN_PERSON_ID = "201212121212";
    private static final LocalDateTime DATE = LocalDateTime.now();

    @InjectMocks
    PaginationAndLoggingServiceImpl paginationAndLoggingServiceImpl;

    @Mock
    LogService logService;

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @BeforeEach
    void setUp() {
        WEBCERT_USER.setHsaId("HSA-ID");
    }


    @Nested
    class Sorting {

        @Test
        void shouldSortByAmneIfOrderByIsAmneAscending() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("", Status.PENDING_INTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("amne", true), List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(0).getAmne());
            assertEquals(item2.getAmne(), result.get(1).getAmne());
        }

        @Test
        void shouldSortByAmneIfOrderByIsAmneDescending() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("", Status.PENDING_INTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("amne", false), List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortToDescendingIfAscendingIsNull() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("", Status.PENDING_INTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("amne", null), List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortByFragestallareIfOrderByIsFragestallare() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "FK",
                TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "XX",
                TOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("fragestallare", true),
                List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortByPatientIdIfOrderByIsPatientId() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                LILLTOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                TOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("patientId", false), List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortBySigneratAvNamnIdIfOrderByIsSigneratAvNamn() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                LILLTOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "b",
                TOLVAN_PERSON_ID, DATE, "b");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("signeratAvNamn", false),
                List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortByVidarebefordradIdIfOrderByIsVidarebefordrad() {
            final var item1 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                LILLTOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("KOMPLETTERING_AV_LAKARINTYG", Status.PENDING_INTERNAL_ACTION, "a",
                TOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("vidarebefordrad", false),
                List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(1).getAmne());
            assertEquals(item2.getAmne(), result.get(0).getAmne());
        }

        @Test
        void shouldSortByReceivedDateIfOrderByIsNull() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE.minusDays(1),
                "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(null, true), List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(0).getAmne());
            assertEquals(item2.getAmne(), result.get(1).getAmne());
        }

        @Test
        void shouldSortByReceivedDateIfOrderByIsReceivedDate() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE.minusDays(1),
                "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("receivedDate", true),
                List.of(item2, item1),
                WEBCERT_USER);

            assertEquals(2, result.size());
            assertEquals(item1.getAmne(), result.get(0).getAmne());
            assertEquals(item2.getAmne(), result.get(1).getAmne());
        }
    }

    @Nested
    class Logging {

        private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        @Test
        void shouldOnlyLogListIntygOnceIfSamePatientOccursMultipleTimesInList() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");

            paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("amne", true), List.of(item1, item2), WEBCERT_USER);

            verify(logService, times(1)).logReadLevelTwo(eq(WEBCERT_USER), argumentCaptor.capture());
            assertEquals(TOLVAN_PERSON_ID, argumentCaptor.getValue());
        }

        @Test
        void shouldLogListIntygTwiceIfTwoDifferentPatientsInList() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");

            paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter("amne", true), List.of(item1, item2, item3), WEBCERT_USER);

            verify(logService, times(2)).logReadLevelTwo(eq(WEBCERT_USER), argumentCaptor.capture());
            assertEquals(TOLVAN_PERSON_ID, argumentCaptor.getAllValues().get(0));
            assertEquals(LILLTOLVAN_PERSON_ID, argumentCaptor.getAllValues().get(1));
        }
    }

    @Nested
    class Pagination {

        @Test
        void shouldTakeWholeListIfIncludedInPageSize() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");
            final var itemList = List.of(item1, item2, item3);

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(10, 0), itemList, WEBCERT_USER);

            assertEquals(3, result.size());
            assertTrue(result.containsAll(itemList));
        }

        @Test
        void shouldTakeWholeListIfListSizeIsSameAsPageSize() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "a");
            final var itemList = List.of(item1, item2, item3);

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(3, 0), itemList, WEBCERT_USER);

            assertEquals(3, result.size());
            assertTrue(result.containsAll(itemList));
        }

        @Test
        void shouldSplitOnStartFrom() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "b");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "c");
            final var itemList = List.of(item1, item2, item3);

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(10, 1), itemList, WEBCERT_USER);

            assertEquals(2, result.size());
            assertFalse(result.contains(item1));
        }

        @Test
        void shouldSplitOnPageSize() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "b");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "c");
            final var itemList = List.of(item1, item2, item3);

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(2, 0), itemList, WEBCERT_USER);

            assertEquals(2, result.size());
            assertFalse(result.contains(item3));
        }

        @Test
        void shouldSplitOnPageSizeAndStartFrom() {
            final var item1 = createArendeListItem("a", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "a");
            final var item2 = createArendeListItem("b", Status.PENDING_EXTERNAL_ACTION, "a", TOLVAN_PERSON_ID, DATE, "b");
            final var item3 = createArendeListItem("c", Status.PENDING_EXTERNAL_ACTION, "a", LILLTOLVAN_PERSON_ID, DATE, "c");
            final var itemList = List.of(item1, item2, item3);

            final var result = paginationAndLoggingServiceImpl.get(createQueryFragaSvarParameter(1, 1), itemList, WEBCERT_USER);

            assertEquals(1, result.size());
            assertTrue(result.contains(item2));
        }
    }

    private QueryFragaSvarParameter createQueryFragaSvarParameter(String orderBy, Boolean isAscending) {
        final var queryFragaSvarParameter = new QueryFragaSvarParameter();

        queryFragaSvarParameter.setOrderBy(orderBy);
        queryFragaSvarParameter.setStartFrom(0);
        queryFragaSvarParameter.setPageSize(20);
        queryFragaSvarParameter.setOrderAscending(isAscending);
        queryFragaSvarParameter.setEnhetId("ID");
        queryFragaSvarParameter.setVidarebefordrad(true);

        return queryFragaSvarParameter;
    }

    private QueryFragaSvarParameter createQueryFragaSvarParameter(Integer pageSize, Integer startFrom) {
        final var queryFragaSvarParameter = new QueryFragaSvarParameter();

        queryFragaSvarParameter.setOrderBy("signeratAvNamn");
        queryFragaSvarParameter.setStartFrom(startFrom);
        queryFragaSvarParameter.setPageSize(pageSize);
        queryFragaSvarParameter.setOrderAscending(true);
        queryFragaSvarParameter.setEnhetId("ID");
        queryFragaSvarParameter.setVidarebefordrad(true);

        return queryFragaSvarParameter;
    }

    private ArendeListItem createArendeListItem(String subject, Status status, String askedBy, String patientId, LocalDateTime date,
        String signedBy) {
        final var arendeListItem = new ArendeListItem();

        arendeListItem.setAmne(subject);
        arendeListItem.setStatus(status);
        arendeListItem.setPaminnelse(false);
        arendeListItem.setFragestallare(askedBy);
        arendeListItem.setPatientId(patientId);
        arendeListItem.setReceivedDate(date);
        arendeListItem.setIntygId("certificateId");
        arendeListItem.setSigneratAvNamn(signedBy);
        arendeListItem.setTestIntyg(false);

        return arendeListItem;
    }

}
