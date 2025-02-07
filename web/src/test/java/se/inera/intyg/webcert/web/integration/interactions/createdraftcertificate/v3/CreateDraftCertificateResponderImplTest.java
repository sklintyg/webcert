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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.webcert.web.csintegration.aggregate.CreateDraftCertificateAggregator;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderImplTest extends BaseCreateDraftCertificateTest {

    private static final String LOGICAL_ADDR = "1234567890";
    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String FULL_NAME = "fullName";
    protected static final String AUTH_METHOD = "http://id.sambi.se/loa/loa3";

    @Mock
    private CreateDraftCertificateValidator createDraftCertificateValidator;
    @Mock
    private MonitoringLogService mockMonitoringLogService;
    @Mock
    private CreateDraftCertificateAggregator createDraftCertificateAggregator;
    @InjectMocks
    private CreateDraftCertificateResponderImpl responder;

    @Before
    public void setup() throws ModuleNotFoundException {
        super.setup();
    }

    @Test
    public void shallReturnMIUErrorIfWebcertUserDetailsServiceThrows() {
        doThrow(IllegalStateException.class).when(webcertUserDetailsService).buildUserPrincipal(anyString(), anyString());
        final var result = responder.createDraftCertificate(LOGICAL_ADDR, createCertificateType());
        assertEquals(ErrorIdType.VALIDATION_ERROR, result.getResult().getErrorId());
        assertTrue(result.getResult().getResultText().contains("No valid MIU was found for person"));
    }

    @Test
    public void shallReturnValidationErrorIfDraftParametersValidationHasErrors() {
        final var expectedErrorMessage = "expected error message";
        final var resultValidator = mock(ResultValidator.class);
        final var certificateType = createCertificateType();

        doReturn(resultValidator).when(createDraftCertificateValidator).validate(certificateType.getIntyg());
        doReturn(true).when(resultValidator).hasErrors();
        doReturn(expectedErrorMessage).when(resultValidator).getErrorMessagesAsString();

        final var result = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertEquals(ErrorIdType.VALIDATION_ERROR, result.getResult().getErrorId());
        assertEquals(expectedErrorMessage, result.getResult().getResultText());
    }

    @Test
    public void shallReturnMIUErrorIfHealthPersonalDontHaveMIURightsOnCareUnit() {
        final var certificateType = createCertificateType();
        final var resultValidator = mock(ResultValidator.class);
        final var webCertUser = buildWebCertUser();
        webCertUser.setVardgivare(Collections.emptyList());

        when(webcertUserDetailsService.buildUserPrincipal(anyString(), anyString())).thenReturn(webCertUser);
        doReturn(resultValidator).when(createDraftCertificateValidator).validate(certificateType.getIntyg());
        doReturn(false).when(resultValidator).hasErrors();

        final var result = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertEquals(ErrorIdType.VALIDATION_ERROR, result.getResult().getErrorId());
        assertTrue(result.getResult().getResultText().contains("No valid MIU was found for person"));
    }

    @Test
    public void shallReturnApplicationErrorsIfHasErrorsIsTrue() {
        final var expectedErrorMessage = "expected error message";

        final var certificateType = createCertificateType();
        final var validationErrors = mock(ResultValidator.class);
        final var applicationErrors = mock(ResultValidator.class);
        final var webCertUser = buildWebCertUser();

        when(webcertUserDetailsService.buildUserPrincipal(anyString(), anyString())).thenReturn(webCertUser);
        doReturn(validationErrors).when(createDraftCertificateValidator).validate(any(Intyg.class));
        doReturn(applicationErrors).when(createDraftCertificateValidator)
            .validateApplicationErrors(certificateType.getIntyg(), webCertUser);
        doReturn(false).when(validationErrors).hasErrors();
        doReturn(true).when(applicationErrors).hasErrors();
        doReturn(expectedErrorMessage).when(applicationErrors).getErrorMessagesAsString();

        final var result = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertEquals(ErrorIdType.APPLICATION_ERROR, result.getResult().getErrorId());
        assertEquals(expectedErrorMessage, result.getResult().getResultText());
    }

    @Test
    public void shallReturnCreateDraftCertificateResponseType() {
        final var expectedResponse = new CreateDraftCertificateResponseType();
        final var certificateType = createCertificateType();
        final var validationErrors = mock(ResultValidator.class);
        final var applicationErrors = mock(ResultValidator.class);
        final var webCertUser = buildWebCertUser();

        when(webcertUserDetailsService.buildUserPrincipal(anyString(), anyString())).thenReturn(webCertUser);
        doReturn(validationErrors).when(createDraftCertificateValidator).validate(any(Intyg.class));
        doReturn(applicationErrors).when(createDraftCertificateValidator)
            .validateApplicationErrors(certificateType.getIntyg(), webCertUser);
        doReturn(false).when(validationErrors).hasErrors();
        doReturn(false).when(applicationErrors).hasErrors();
        doReturn(expectedResponse).when(createDraftCertificateAggregator).create(certificateType.getIntyg(), webCertUser);

        final var result = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertEquals(expectedResponse, result);
    }

    private CreateDraftCertificateType createCertificateType() {
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");

        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");

        Enhet hosEnhet = new Enhet();
        hosEnhet.setEnhetsId(unitHsaId);

        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setFullstandigtNamn(FULL_NAME);
        hosPerson.setPersonalId(userHsaId);
        hosPerson.setEnhet(hosEnhet);

        Intyg utlatande = new Intyg();
        utlatande.setSkapadAv(hosPerson);

        CreateDraftCertificateType certificateType = new CreateDraftCertificateType();
        certificateType.setIntyg(utlatande);
        return certificateType;
    }
}
