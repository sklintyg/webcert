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

package se.inera.intyg.webcert.web.service.access;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@RunWith(MockitoJUnitRunner.class)
public class DraftAccessServiceHelperTest {

    @Mock
    private DraftAccessService draftAccessService;

    @Mock
    private AccessResultExceptionHelper accessResultExceptionHelper;

    @InjectMocks
    private DraftAccessServiceHelper draftAccessServiceHelper;

    private final static String INTYGSTYP = "intygstyp";
    private final static Personnummer PERSONNUMMER = mock(Personnummer.class);
    private final static Utkast UTKAST = mock(Utkast.class);
    private final static Vardenhet VARDENHET = mock(Vardenhet.class);

    @Test
    public void testIsAllowToCreateUtkastHasAccess() {
        when(draftAccessService.allowToCreateDraft(any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToCreateUtkast(INTYGSTYP, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToCreateUtkastAccessDenied() {
        when(draftAccessService.allowToCreateDraft(any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToCreateUtkast(INTYGSTYP, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAllowToCreateUtkastHasAccess() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = mock(Personnummer.class);
        draftAccessServiceHelper.validateAllowToCreateUtkast(intygsTyp, personnummer);
    }

    @Test
    public void testValidateAllowToCreateUtkastAccessDenied() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = mock(Personnummer.class);

        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToCreateUtkast(intygsTyp, personnummer);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testIsAllowToReadUtkastHasAccess() {
        when(draftAccessService.allowToReadDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToReadUtkast(UTKAST, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToReadUtkastAccessDenied() {
        when(draftAccessService.allowToReadDraft(any(), any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToReadUtkast(UTKAST, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAccessToReadUtkastHasAccess() {
        final Utkast utkast = mock(Utkast.class);
        final Personnummer personnummer = mock(Personnummer.class);
        draftAccessServiceHelper.validateAllowToReadUtkast(utkast, personnummer);
    }

    @Test
    public void testValidateAccessToReadUtkastAccessDenied() {
        final Utkast utkast = mock(Utkast.class);
        final Personnummer personnummer = mock(Personnummer.class);

        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToReadUtkast(utkast, personnummer);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testValidateAccessToEditUtkastHasAccess() {
        draftAccessServiceHelper.validateAllowToEditUtkast(UTKAST);
    }

    @Test
    public void testValidateAccessToEditUtkastAccessDenied() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToEditUtkast(UTKAST);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testIsAllowToEditUtkast() {
        when(draftAccessService.allowToEditDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToEditUtkast(INTYGSTYP, VARDENHET, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToEditUtkastHasAccess() {
        when(draftAccessService.allowToEditDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToEditUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToEditUtkastAccessDenied() {
        when(draftAccessService.allowToEditDraft(any(), any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToEditUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToDeleteUtkast() {
        when(draftAccessService.allowToDeleteDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToDeleteUtkast(INTYGSTYP, VARDENHET, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToDeleteUtkastHasAccess() {
        when(draftAccessService.allowToDeleteDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToDeleteUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToDeleteUtkastAccessDenied() {
        when(draftAccessService.allowToDeleteDraft(any(), any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToDeleteUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAccessToDeleteUtkastHasAccess() {
        draftAccessServiceHelper.validateAllowToDeleteUtkast(UTKAST);
    }

    @Test
    public void testValidateAccessToDeleteUtkastAccessDenied() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToDeleteUtkast(UTKAST);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testIsAllowToPrintUtkast() {
        when(draftAccessService.allowToPrintDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToPrintUtkast(INTYGSTYP, VARDENHET, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToPrintUtkastHasAccess() {
        when(draftAccessService.allowToPrintDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToPrintUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToPrintUtkastAccessDenied() {
        when(draftAccessService.allowToPrintDraft(any(), any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToPrintUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAccessToPrintUtkastHasAccess() {
        draftAccessServiceHelper.validateAllowToPrintUtkast(UTKAST);
    }

    @Test
    public void testValidateAccessToPrintUtkastAccessDenied() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToPrintUtkast(UTKAST);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testIsAllowToForwardUtkast() {
        when(draftAccessService.allowToForwardDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(INTYGSTYP, VARDENHET, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToForwardUtkastHasAccess() {
        when(draftAccessService.allowToForwardDraft(any(), any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToForwardUtkastAccessDenied() {
        when(draftAccessService.allowToForwardDraft(any(), any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAccessToForwardUtkastHasAccess() {
        draftAccessServiceHelper.validateAllowToForwardDraft(UTKAST);
    }

    @Test
    public void testValidateAccessToForwardUtkastAccessDenied() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToForwardDraft(UTKAST);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }

    @Test
    public void testIsAllowToToCopyFromCandidate() {
        when(draftAccessService.allowToCopyFromCandidate(any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(INTYGSTYP, VARDENHET, PERSONNUMMER);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToToCopyFromCandidateHasAccess() {
        when(draftAccessService.allowToCopyFromCandidate(any(), any())).thenReturn(AccessResult.noProblem());

        final boolean expectedResult = true;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testIsAllowToToCopyFromCandidateAccessDenied() {
        when(draftAccessService.allowToCopyFromCandidate(any(), any())).thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access"));

        final boolean expectedResult = false;
        final boolean actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(UTKAST);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testValidateAccessToCopyFromCandidateHasAccess() {
        draftAccessServiceHelper.validateAllowToCopyFromCandidate(UTKAST);
    }

    @Test
    public void testValidateAccessToCopyFromCandidateAccessDenied() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "No Access")).when(accessResultExceptionHelper).throwExceptionIfDenied(any());

        try {
            draftAccessServiceHelper.validateAllowToCopyFromCandidate(UTKAST);
            fail();
        } catch(Throwable th) {
            assertEquals(WebCertServiceException.class, th.getClass());
        }
    }
}
