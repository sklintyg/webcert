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
package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterDateRangeValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterSelectValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

class CertificatesQueryCriteriaFactoryTest {

    private CertificatesQueryCriteriaFactory certificatesQueryCriteriaFactory;
    private ListFilter listFilter;
    private QueryIntygParameter queryIntygParameter;

    @Nested
    class CreateCriteriaFromListFilter {

        @BeforeEach
        void setUp() {
            certificatesQueryCriteriaFactory = new CertificatesQueryCriteriaFactory();
            listFilter = new ListFilter();
        }

        @Test
        void shallIncludeFrom() {
            final var from = LocalDateTime.now(ZoneId.systemDefault());
            listFilter.addValue(new ListFilterDateRangeValue(null, from), "SAVED");

            assertEquals(from, certificatesQueryCriteriaFactory.create(listFilter).getFrom());
        }

        @Test
        void shallIncludeTo() {
            final var to = LocalDateTime.now(ZoneId.systemDefault());
            listFilter.addValue(new ListFilterDateRangeValue(to, null), "SAVED");

            assertEquals(to.plusDays(1), certificatesQueryCriteriaFactory.create(listFilter).getTo());
        }

        @Test
        void shallIncludeStatusUnsignedIfUtkastStatusIsDraftIncomplete() {
            final var statuses = List.of(CertificateStatus.UNSIGNED);
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.INCOMPLETE.name()), "STATUS");

            assertEquals(statuses, certificatesQueryCriteriaFactory.create(listFilter).getStatuses());
        }

        @Test
        void shallIncludeStatusUnsignedIfUtkastStatusIsDraftComplete() {
            final var statuses = List.of(CertificateStatus.UNSIGNED);
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.COMPLETE.name()), "STATUS");

            assertEquals(statuses, certificatesQueryCriteriaFactory.create(listFilter).getStatuses());
        }

        @Test
        void shallIncludeStatusUnsignedIfAllStatuses() {
            final var statuses = List.of(CertificateStatus.UNSIGNED);
            listFilter.addValue(new ListFilterSelectValue(""), "STATUS");

            assertEquals(statuses, certificatesQueryCriteriaFactory.create(listFilter).getStatuses());
        }

        @Test
        void shallIncludeStatusLockedIfUtkastStatusIsLocked() {
            final var statuses = List.of(CertificateStatus.LOCKED);
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.LOCKED.name()), "STATUS");

            assertEquals(statuses, certificatesQueryCriteriaFactory.create(listFilter).getStatuses());
        }

        @Test
        void shallIncludeValidForSignTrueIfUtkastStatusIsComplete() {
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.COMPLETE.name()), "STATUS");

            assertTrue(certificatesQueryCriteriaFactory.create(listFilter).getValidForSign());
        }

        @Test
        void shallIncludeValidForSignFalseIfUtkastStatusIsIncomplete() {
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.INCOMPLETE.name()), "STATUS");

            assertFalse(certificatesQueryCriteriaFactory.create(listFilter).getValidForSign());
        }

        @Test
        void shallNotIncludeValidForSignIfUtkastStatusIsAll() {
            listFilter.addValue(new ListFilterSelectValue(CertificateListItemStatus.SHOW_ALL.name()), "STATUS");

            assertNull(certificatesQueryCriteriaFactory.create(listFilter).getValidForSign());
        }

        @Test
        void shallIncludePatientId() {
            final var personId = PersonIdDTO.builder()
                .id("19121212-1212")
                .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                .build();
            listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");

            assertEquals(personId, certificatesQueryCriteriaFactory.create(listFilter).getPersonId());
        }

        @Test
        void shallNotIncludePatientIdIfEmpty() {
            listFilter.addValue(new ListFilterPersonIdValue(""), "PATIENT_ID");

            assertNull(certificatesQueryCriteriaFactory.create(listFilter).getPersonId());
        }

        @Test
        void shallIncludeIssuedByStaffId() {
            final var issuedByStaffId = "HSA-ID";
            listFilter.addValue(new ListFilterSelectValue("HSA-ID"), "SAVED_BY");

            assertEquals(issuedByStaffId, certificatesQueryCriteriaFactory.create(listFilter).getIssuedByStaffId());
        }

        @Test
        void shallNotIncludeIssuedByStaffIdIfEmpty() {
            listFilter.addValue(new ListFilterSelectValue(""), "SAVED_BY");

            assertNull(certificatesQueryCriteriaFactory.create(listFilter).getIssuedByStaffId());
        }
    }

    @Nested
    class CreateCriteriaFromQueryIntygParameter {

        @BeforeEach
        void setUp() {
            certificatesQueryCriteriaFactory = new CertificatesQueryCriteriaFactory();
            queryIntygParameter = new QueryIntygParameter();
        }

        @Test
        void shallIncludeFrom() {
            final var from = LocalDateTime.now(ZoneId.systemDefault());
            queryIntygParameter.setSignedFrom(from);

            assertEquals(from, certificatesQueryCriteriaFactory.create(queryIntygParameter).getFrom());
        }

        @Test
        void shallIncludeTo() {
            final var to = LocalDateTime.now(ZoneId.systemDefault());
            queryIntygParameter.setSignedTo(to);

            assertEquals(to, certificatesQueryCriteriaFactory.create(queryIntygParameter).getTo());
        }

        @Test
        void shallIncludeStatusSigned() {
            final var statuses = List.of(CertificateStatus.SIGNED);

            assertEquals(statuses, certificatesQueryCriteriaFactory.create(queryIntygParameter).getStatuses());
        }

        @Test
        void shallIncludePatientId() {
            final var personId = PersonIdDTO.builder()
                .id("19121212-1212")
                .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                .build();
            queryIntygParameter.setPatientId(personId.getId());

            assertEquals(personId, certificatesQueryCriteriaFactory.create(queryIntygParameter).getPersonId());
        }

        @Test
        void shallNotIncludePatientIdIfEmpty() {
            queryIntygParameter.setPatientId("");

            assertNull(certificatesQueryCriteriaFactory.create(queryIntygParameter).getPersonId());
        }

        @Test
        void shallIncludeIssuedByStaffId() {
            final var issuedByStaffId = "HSA-ID";
            queryIntygParameter.setHsaId(issuedByStaffId);

            assertEquals(issuedByStaffId, certificatesQueryCriteriaFactory.create(queryIntygParameter).getIssuedByStaffId());
        }

        @Test
        void shallNotIncludeIssuedByStaffIdIfEmpty() {
            queryIntygParameter.setHsaId("");

            assertNull(certificatesQueryCriteriaFactory.create(queryIntygParameter).getIssuedByStaffId());
        }
    }
}
