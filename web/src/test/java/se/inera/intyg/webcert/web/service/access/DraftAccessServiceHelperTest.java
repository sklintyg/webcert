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
package se.inera.intyg.webcert.web.service.access;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelperImpl;

@RunWith(MockitoJUnitRunner.class)
public class DraftAccessServiceHelperTest {

    @Mock
    private DraftAccessService draftAccessService;

    @Spy
    private AccessResultExceptionHelper accessResultExceptionHelper = new AccessResultExceptionHelperImpl();

    @InjectMocks
    private DraftAccessServiceHelper draftAccessServiceHelper;

    private Utkast draft;

    @Before
    public void setup() {
        draft = new Utkast();
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").get());
        draft.setTestIntyg(false);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToCreateCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToCreateDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToCreateUtkast(draft.getIntygsTyp(), draft.getPatientPersonnummer());
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToCreateCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToCreateDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToCreateUtkast(draft.getIntygsTyp(), draft.getPatientPersonnummer());
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToCreateCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToCreateDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToCreateUtkast(draft.getIntygsTyp(), draft.getPatientPersonnummer());
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToCreateCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToCreateDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToCreateUtkast(draft.getIntygsTyp(), draft.getPatientPersonnummer());
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToReadCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToReadDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToReadUtkast(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToReadCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToReadDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToReadUtkast(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToReadCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToReadDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToReadUtkast(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToReadCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToReadDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToReadUtkast(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToEditCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToEditUtkast(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToEditCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToEditUtkast(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToEditCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToEditUtkast(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToEditCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToEditUtkast(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallAllowIfAllowAccessToEditCertificatePassingAccessEvaluationParameters() {
        doReturn(createAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToEditUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToEditCertificatePassingAccessEvaluationParameters() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToEditDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToEditUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToDeleteCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToDeleteUtkast(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToDeleteCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToDeleteUtkast(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToDeleteCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToDeleteUtkast(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToDeleteCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToDeleteUtkast(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallAllowIfAllowAccessToDeleteCertificatePassingAccessEvaluationParameters() {
        doReturn(createAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToDeleteUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToDeleteCertificatePassingAccessEvaluationParameters() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToDeleteDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToDeleteUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToPrintCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToPrintUtkast(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToPrintCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToPrintUtkast(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToPrintCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToPrintUtkast(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToPrintCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToPrintUtkast(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallAllowIfAllowAccessToPrintCertificatePassingAccessEvaluationParameters() {
        doReturn(createAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToPrintUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToPrintCertificatePassingAccessEvaluationParameters() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToPrintDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToPrintUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToForwardCertificate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToForwardDraft(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToForwardCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToForwardDraft(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToForwardCertificate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToForwardUtkast(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToForwardCertificate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToForwardUtkast(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallAllowIfAllowAccessToForwardCertificatePassingAccessEvaluationParameters() {
        doReturn(createAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToForwardCertificatePassingAccessEvaluationParameters() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToForwardDraft(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(mock(AccessEvaluationParameters.class));
        assertEquals(false, actualResult);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToCopyFromCandidate() {
        try {
            doReturn(createNoAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
            draftAccessServiceHelper.validateAllowToCopyFromCandidate(draft);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToCopyFromCandidate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
        draftAccessServiceHelper.validateAllowToCopyFromCandidate(draft);
        assertTrue(true);
    }

    @Test
    public void shallAllowIfAllowAccessToCopyFromCandidate() {
        doReturn(createAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToCopyFromCandidate(draft);
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToCopyFromCandidate() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper
            .isAllowedToCopyFromCandidate(draft);
        assertEquals(false, actualResult);
    }

    @Test
    public void shallAllowIfAllowAccessToCopyFromCandidatePassingAccessEvaluationParameters() {
        doReturn(createAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(mock(AccessEvaluationParameters.class));
        assertEquals(true, actualResult);
    }

    @Test
    public void shallNotAllowIfNoAccessToCopyFromCandidatePassingAccessEvaluationParameters() {
        doReturn(createNoAccessResult()).when(draftAccessService).allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
        final var actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(mock(AccessEvaluationParameters.class));
        assertEquals(false, actualResult);
    }

    private AccessResult createAccessResult() {
        return AccessResult.noProblem();
    }

    private AccessResult createNoAccessResult() {
        return AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No Access");
    }
}
