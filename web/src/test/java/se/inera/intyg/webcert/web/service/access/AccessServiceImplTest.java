/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.tstrk1062.support.TsTrk1062EntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceImplTest {

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private AccessServiceImpl accessService;

    public static String PERSON_NUMMER_TOLVAN = "191212121212";

    @Test
    public void allowToCreateTstrk1062Utkast() {
        final String intygsType = TsTrk1062EntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithUtkastAuthority(intygsType))
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(personnummer);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = true;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateTstrk1062UtkastOnMissingFeature() {
        final String intygsType = TsTrk1062EntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithoutUtkastFeature(intygsType))
                .when(webCertUserService).getUser();
        // doReturn(true)
        // .when(patientDetailsResolver).isAvliden(personnummer);
        // doReturn(SekretessStatus.FALSE)
        // .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateTstrk1062UtkastOnMissingAuthority() {
        final String intygsType = TsTrk1062EntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithoutUtkastAuthority(intygsType))
                .when(webCertUserService).getUser();
        // doReturn(true)
        // .when(patientDetailsResolver).isAvliden(personnummer);
        // doReturn(SekretessStatus.FALSE)
        // .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateTstrk1062UtkastOnDeceasedPatient() {
        final String intygsType = TsTrk1062EntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithUtkastAuthority(intygsType))
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(personnummer);
        // doReturn(SekretessStatus.FALSE)
        // .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateTstrk1062UtkastOnInactiveCareUnit() {
        final String intygsType = TsTrk1062EntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithUtkastAuthorityOnInactiveUnit(intygsType))
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(personnummer);
        // doReturn(SekretessStatus.FALSE)
        // .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void allowToCreateDOIUtkastOnDeceasedPatient() {
        final String intygsType = DoiModuleEntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        doReturn(AccessServiceTestToolkit.createUserWithUtkastAuthority(intygsType))
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(personnummer);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(personnummer);

        final boolean expectedReturn = true;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateDOIUtkastOnSameCareProviderWhenUtkastSameVGExists() {
        final String intygsType = DoiModuleEntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        final WebCertUser user = AccessServiceTestToolkit.createUserWithUtkastAuthorityForUniqueUtkastWithinVG(intygsType);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(personnummer);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(personnummer);
        doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsType))
                .when(utkastService).checkIfPersonHasExistingIntyg(personnummer, user);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateDOIUtkastOnDifferentCareProviderWhenIntygSameVGExists() {
        final String intygsType = DoiModuleEntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        final WebCertUser user = AccessServiceTestToolkit.createUserWithUtkastAuthorityForUniqueIntygWithinVG(intygsType);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(personnummer);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(personnummer);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsType, true))
                .when(utkastService).checkIfPersonHasExistingIntyg(personnummer, user);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }

    @Test
    public void dontAllowToCreateDOIUtkastOnSameCareProviderWhenIntygExists() {
        final String intygsType = DoiModuleEntryPoint.MODULE_ID;
        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_NUMMER_TOLVAN).get();

        final WebCertUser user = AccessServiceTestToolkit.createUserWithUtkastAuthorityForUniqueIntyg(intygsType);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(personnummer);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(personnummer);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsType, false))
                .when(utkastService).checkIfPersonHasExistingIntyg(personnummer, user);

        final boolean expectedReturn = false;
        final boolean actualReturn = accessService.allowedToCreateUtkast(intygsType, personnummer);

        assertEquals("Should have access", expectedReturn, actualReturn);
    }
}
