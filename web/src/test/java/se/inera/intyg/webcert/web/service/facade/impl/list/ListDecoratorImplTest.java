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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;
import se.inera.intyg.webcert.web.service.facade.list.ListDecoratorImpl;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@ExtendWith(MockitoExtension.class)
class ListDecoratorImplTest {

    @Mock
    private IntygDraftDecorator intygDraftDecorator;
    @Mock
    private EmployeeNameService employeeNameService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private ListDecoratorImpl listDecorator;

    @Nested
    class CertificateTypeName {

        final List<ListIntygEntry> list = new ArrayList<ListIntygEntry>(
            List.of(ListTestHelper.createListIntygEntry("STATUS", true, true))
        );

        @Test
        public void shouldDecorateWithCertificateTypeName() {
            listDecorator.decorateWithCertificateTypeName(list);
            assertEquals(1, list.size());
        }
    }

    @Nested
    class StaffName {

        public void setupStaffWithOnlyLastName() {
            when(employeeNameService.getEmployeeHsaName(any()))
                .thenReturn("EXAMPLE_NAME");
        }

        public void setupStaffWithCompleteName() {
            when(employeeNameService.getEmployeeHsaName(any()))
                .thenReturn("FIRST MIDDLE LAST");
        }

        public void setupStaffWithNoLastName() {
            when(employeeNameService.getEmployeeHsaName(any()))
                .thenReturn(null);
        }

        @Test
        public void shouldDecorateWithStaffLastName() {
            setupStaffWithOnlyLastName();
            final List<ListIntygEntry> list = new ArrayList<ListIntygEntry>(
                List.of(ListTestHelper.createListIntygEntry("STATUS", true, true))
            );

            listDecorator.decorateWithStaffName(list);
            assertEquals(1, list.size());
            assertEquals("EXAMPLE_NAME", list.get(0).getUpdatedSignedBy());
        }

        @Test
        public void shouldDecorateWithStaffFullName() {
            setupStaffWithCompleteName();
            final List<ListIntygEntry> list = new ArrayList<ListIntygEntry>(
                List.of(ListTestHelper.createListIntygEntry("STATUS", true, true))
            );

            listDecorator.decorateWithStaffName(list);
            assertEquals(1, list.size());
            assertEquals("FIRST MIDDLE LAST", list.get(0).getUpdatedSignedBy());
        }

        @Test
        public void shouldNotDecorateIfStaffHasNoLastName() {
            setupStaffWithNoLastName();
            final List<ListIntygEntry> list = new ArrayList<ListIntygEntry>(
                List.of(ListTestHelper.createListIntygEntry("STATUS", true, true))
            );

            listDecorator.decorateWithStaffName(list);
            assertEquals(1, list.size());
            assertNull(list.get(0).getUpdatedSignedBy());
        }
    }

    @Nested
    class ProtectedPersonStatus {

        final List<ListIntygEntry> list = new ArrayList<ListIntygEntry>(
            List.of(ListTestHelper.createListIntygEntry("STATUS", true, true))
        );

        public void setupPatientStatus(SekretessStatus status, boolean flag) {
            final var statusMap = mock(Map.class);
            PatientDetailsResolverResponse response = new PatientDetailsResolverResponse();
            response.setTestIndicator(flag);
            response.setDeceased(flag);
            response.setProtectedPerson(status);
            when(statusMap.get(any(Personnummer.class))).thenReturn(response);
            Mockito.when(patientDetailsResolver.getPersonStatusesForList(any())).thenReturn(statusMap);
        }

        public void setupHandleProtectedPatientPrivilege() {
            ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        }

        @Test
        public void shouldIncludeNotProtectedPerson() {
            setupPatientStatus(SekretessStatus.FALSE, false);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertEquals(1, result.size());
        }

        @Test
        public void shouldSetFalseForDeceasedPatient() {
            setupPatientStatus(SekretessStatus.FALSE, false);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertFalse(result.get(0).isAvliden());
        }

        @Test
        public void shouldSetFalseForTestIndicatedPatient() {
            setupPatientStatus(SekretessStatus.FALSE, false);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertFalse(result.get(0).isTestIntyg());
        }

        @Test
        public void shouldSetFalseForProtectedPatient() {
            setupPatientStatus(SekretessStatus.FALSE, false);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertFalse(result.get(0).isSekretessmarkering());
        }

        @Test
        public void shouldIncludeProtectedPerson() {
            setupHandleProtectedPatientPrivilege();
            setupPatientStatus(SekretessStatus.TRUE, true);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertEquals(1, result.size());
        }

        @Test
        public void shouldSetTrueForDeceasedPatient() {
            setupHandleProtectedPatientPrivilege();
            setupPatientStatus(SekretessStatus.TRUE, true);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertTrue(result.get(0).isAvliden());
        }

        @Test
        public void shouldSetTrueForTestIndicatedPatient() {
            setupHandleProtectedPatientPrivilege();
            setupPatientStatus(SekretessStatus.TRUE, true);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertTrue(result.get(0).isTestIntyg());
        }

        @Test
        public void shouldSetTrueForProtectedPatient() {
            setupHandleProtectedPatientPrivilege();
            setupPatientStatus(SekretessStatus.TRUE, true);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertTrue(result.get(0).isSekretessmarkering());
        }

        @Test
        public void shouldNotIncludeProtectedPersonIfUserHasNoAccess() {
            setupPatientStatus(SekretessStatus.TRUE, true);
            ListTestHelper.setupUser(webCertUserService, LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertEquals(0, result.size());
        }

        @Test
        public void shouldNotIncludePatientWithUndefinedProtectedStatus() {
            setupPatientStatus(SekretessStatus.UNDEFINED, true);
            setupHandleProtectedPatientPrivilege();

            final var result = listDecorator.decorateAndFilterProtectedPerson(list);

            assertEquals(0, result.size());
        }
    }
}