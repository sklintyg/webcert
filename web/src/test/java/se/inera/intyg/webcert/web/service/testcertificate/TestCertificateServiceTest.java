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

package se.inera.intyg.webcert.web.service.testcertificate;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.TestCertificateEraseResult;

@RunWith(MockitoJUnitRunner.class)
public class TestCertificateServiceTest {
    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private EraseTestCertificateService eraseTestCertificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private TestCertificateService testCertificateService;

    private final static LocalDateTime FROM = null;
    private final static LocalDateTime TO = LocalDateTime.of(2020, 04, 30, 0, 0);


    final Utkast testCertificateRoot = mock(Utkast.class);
    private final static String TEST_CERTIFICATE_ROOT_ID = "TEST_CERTIFICATE_ROOT_ID";
    private final static String TEST_CERTIFICATE_ROOT_UNIT_ID = "TEST_CERTIFICATE_ROOT_UNIT_ID";
    private final static String TEST_CERTIFICATE_ROOT_STAFF_ID = "TEST_CERTIFICATE_ROOT_STAFF_ID";

    final Utkast testCertificateBranch = mock(Utkast.class);
    private final static String TEST_CERTIFICATE_BRANCH_ID = "TEST_CERTIFICATE_BRANCH_ID";
    private final static String TEST_CERTIFICATE_BRANCH_UNIT_ID = "TEST_CERTIFICATE_BRANCH_UNIT_ID";
    private final static String TEST_CERTIFICATE_BRANCH_STAFF_ID = "TEST_CERTIFICATE_BRANCH_STAFF_ID";

    final Utkast testCertificateLeaf = mock(Utkast.class);
    private final static String TEST_CERTIFICATE_LEAF_ID = "TEST_CERTIFICATE_LEAF_ID";
    private final static String TEST_CERTIFICATE_LEAF_UNIT_ID = "TEST_CERTIFICATE_LEAF_UNIT_ID";
    private final static String TEST_CERTIFICATE_LEAF_STAFF_ID = "TEST_CERTIFICATE_LEAF_STAFF_ID";

    final Utkast testCertificateSingle = mock(Utkast.class);
    private final static String TEST_CERTIFICATE_SINGLE_ID = "TEST_CERTIFICATE_SINGLE_ID";
    private final static String TEST_CERTIFICATE_SINGLE_UNIT_ID = "TEST_CERTIFICATE_SINGLE_UNIT_ID";
    private final static String TEST_CERTIFICATE_SINGLE_STAFF_ID = "TEST_CERTIFICATE_SINGLE_STAFF_ID";

    private void setupTestCertificatesWithRelations() {
        doReturn(TEST_CERTIFICATE_ROOT_ID).when(testCertificateRoot).getIntygsId();
        doReturn(TEST_CERTIFICATE_ROOT_UNIT_ID).when(testCertificateRoot).getEnhetsId();
        final VardpersonReferens rootStaffReference = mock(VardpersonReferens.class);
        doReturn(rootStaffReference).when(testCertificateRoot).getSkapadAv();
        doReturn(TEST_CERTIFICATE_ROOT_STAFF_ID).when(rootStaffReference).getHsaId();
        doReturn(testCertificateRoot).when(utkastRepository).getOne(TEST_CERTIFICATE_ROOT_ID);
        final WebcertCertificateRelation rootChildrenRelation = mock(WebcertCertificateRelation.class);
        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(rootChildrenRelation).getIntygsId();
        doReturn(Collections.emptyList()).when(utkastRepository).findParentRelation(TEST_CERTIFICATE_ROOT_ID);
        doReturn(Arrays.asList(rootChildrenRelation)).when(utkastRepository).findChildRelations(TEST_CERTIFICATE_ROOT_ID);

        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(testCertificateBranch).getIntygsId();
        doReturn(TEST_CERTIFICATE_BRANCH_UNIT_ID).when(testCertificateBranch).getEnhetsId();
        final VardpersonReferens branchStaffReference = mock(VardpersonReferens.class);
        doReturn(branchStaffReference).when(testCertificateBranch).getSkapadAv();
        doReturn(TEST_CERTIFICATE_BRANCH_STAFF_ID).when(branchStaffReference).getHsaId();
        doReturn(testCertificateBranch).when(utkastRepository).getOne(TEST_CERTIFICATE_BRANCH_ID);
        final WebcertCertificateRelation branchParentRelation = mock(WebcertCertificateRelation.class);
        doReturn(TEST_CERTIFICATE_ROOT_ID).when(branchParentRelation).getIntygsId();
        final WebcertCertificateRelation branchChildrenRelation = mock(WebcertCertificateRelation.class);
        doReturn(TEST_CERTIFICATE_LEAF_ID).when(branchChildrenRelation).getIntygsId();
        doReturn(Arrays.asList(branchParentRelation)).when(utkastRepository).findParentRelation(TEST_CERTIFICATE_BRANCH_ID);
        doReturn(Arrays.asList(branchChildrenRelation)).when(utkastRepository).findChildRelations(TEST_CERTIFICATE_BRANCH_ID);

        doReturn(TEST_CERTIFICATE_LEAF_ID).when(testCertificateLeaf).getIntygsId();
        doReturn(TEST_CERTIFICATE_LEAF_UNIT_ID).when(testCertificateLeaf).getEnhetsId();
        final VardpersonReferens leafStaffReference = mock(VardpersonReferens.class);
        doReturn(leafStaffReference).when(testCertificateLeaf).getSkapadAv();
        doReturn(TEST_CERTIFICATE_LEAF_ID).when(leafStaffReference).getHsaId();
        doReturn(testCertificateLeaf).when(utkastRepository).getOne(TEST_CERTIFICATE_LEAF_ID);
        final WebcertCertificateRelation leafParentRelation = mock(WebcertCertificateRelation.class);
        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(leafParentRelation).getIntygsId();
        doReturn(Arrays.asList(leafParentRelation)).when(utkastRepository).findParentRelation(TEST_CERTIFICATE_LEAF_ID);
        doReturn(Collections.emptyList()).when(utkastRepository).findChildRelations(TEST_CERTIFICATE_LEAF_ID);
    }

    private void setupTestCertificatesForSingle() {
        doReturn(TEST_CERTIFICATE_SINGLE_ID).when(testCertificateSingle).getIntygsId();
        doReturn(TEST_CERTIFICATE_SINGLE_UNIT_ID).when(testCertificateSingle).getEnhetsId();
        final VardpersonReferens singleStaffReference = mock(VardpersonReferens.class);
        doReturn(singleStaffReference).when(testCertificateSingle).getSkapadAv();
        doReturn(TEST_CERTIFICATE_SINGLE_STAFF_ID).when(singleStaffReference).getHsaId();
        doReturn(testCertificateSingle).when(utkastRepository).getOne(TEST_CERTIFICATE_SINGLE_ID);
        doReturn(Collections.emptyList()).when(utkastRepository).findParentRelation(TEST_CERTIFICATE_SINGLE_ID);
        doReturn(Collections.emptyList()).when(utkastRepository).findChildRelations(TEST_CERTIFICATE_SINGLE_ID);
    }

    @Test
    public void testEraseTestCertificateSingleCertificate() throws Exception {
        setupTestCertificatesForSingle();

        final List<String> certificateIds = new ArrayList<>(1);
        certificateIds.add(TEST_CERTIFICATE_SINGLE_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(1, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(1)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseTestCertificateSingleCertificateFailed() throws Exception {
        setupTestCertificatesForSingle();

        final List<String> certificateIds = new ArrayList<>(1);
        certificateIds.add(TEST_CERTIFICATE_SINGLE_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(0, actualEraseResult.getErasedCount());
        assertEquals(1, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(0)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphWhenAllMatchFindQuery() throws Exception {
        setupTestCertificatesWithRelations();

        final List<String> certificateIds = new ArrayList<>(3);
        certificateIds.add(TEST_CERTIFICATE_ROOT_ID);
        certificateIds.add(TEST_CERTIFICATE_BRANCH_ID);
        certificateIds.add(TEST_CERTIFICATE_LEAF_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphWhenRootMatchFindQuery() throws Exception {
        setupTestCertificatesWithRelations();

        final List<String> certificateIds = new ArrayList<>(1);
        certificateIds.add(TEST_CERTIFICATE_ROOT_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphWhenBranchMatchFindQuery() throws Exception {
        setupTestCertificatesWithRelations();

        final List<String> certificateIds = new ArrayList<>(1);
        certificateIds.add(TEST_CERTIFICATE_BRANCH_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphWhenLeafMatchFindQuery() throws Exception {
        setupTestCertificatesWithRelations();

        final List<String> certificateIds = new ArrayList<>(1);
        certificateIds.add(TEST_CERTIFICATE_LEAF_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3 , actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphAndSingleCertificate() throws Exception {
        setupTestCertificatesWithRelations();
        setupTestCertificatesForSingle();

        final List<String> certificateIds = new ArrayList<>(2);
        certificateIds.add(TEST_CERTIFICATE_LEAF_ID);
        certificateIds.add(TEST_CERTIFICATE_SINGLE_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(4 , actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(4)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphSucessAndSingleCertificateFailed() throws Exception {
        setupTestCertificatesWithRelations();
        setupTestCertificatesForSingle();

        final List<String> certificateIds = new ArrayList<>(2);
        certificateIds.add(TEST_CERTIFICATE_LEAF_ID);
        certificateIds.add(TEST_CERTIFICATE_SINGLE_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(argThat(new ArgumentMatcher<List<String>>() {
            @Override
            public boolean matches(List<String> argument) {
                return argument.size() == 1;
            }
        }));

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3 , actualEraseResult.getErasedCount());
        assertEquals(1, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any(), any());
    }

    @Test
    public void testEraseFullGraphFailedAndSingleCertificateSuccess() throws Exception {
        setupTestCertificatesWithRelations();
        setupTestCertificatesForSingle();

        final List<String> certificateIds = new ArrayList<>(2);
        certificateIds.add(TEST_CERTIFICATE_LEAF_ID);
        certificateIds.add(TEST_CERTIFICATE_SINGLE_ID);

        doReturn(certificateIds).when(utkastRepository).findTestCertificatesByCreatedBefore(any());

        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(argThat(new ArgumentMatcher<List<String>>() {
            @Override
            public boolean matches(List<String> argument) {
                return argument.size() == 3;
            }
        }));

        final TestCertificateEraseResult actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(1 , actualEraseResult.getErasedCount());
        assertEquals(3, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(1)).logTestCertificateErased(any(), any(), any());
    }
}
