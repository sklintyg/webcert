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
package se.inera.intyg.webcert.web.service.certificate;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@RunWith(MockitoJUnitRunner.class)
public class GetCertificateServiceTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygService intygService;

    @InjectMocks
    private GetCertificateServiceImpl getCertificateService;

    @Test
    public void shallReturnIntygFromWebcertIfExists() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";

        final var expectedCertificate = mock(Intyg.class);
        final var utlatande = mock(Utlatande.class);

        setupMockToReturnFromWebcert(expectedCertificate, utlatande, true);

        final var actualCertificate = getCertificateService.getCertificateAsIntyg(certificateId, certificateType);

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shallReturnIntygFromIntygstjanstIfItDoesntExistsInWebcert() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";

        final var expectedCertificate = mock(Intyg.class);
        final var utlatande = mock(Utlatande.class);

        setupMockToThrowExceptionFromWebcert();
        setupMockToReturnFromIntygstjanst(expectedCertificate, utlatande, true);

        final var actualCertificate = getCertificateService.getCertificateAsIntyg(certificateId, certificateType);

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shallReturnUtlatandeFromWebcertIfExists() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";

        final var expectedCertificate = mock(Utlatande.class);

        setupMockToReturnFromWebcert(null, expectedCertificate, false);

        final var actualCertificate = getCertificateService.getCertificateAsUtlatande(certificateId, certificateType);

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shallReturnUtlatandeFromIntygstjanstIfItDoesntExistsInWebcert() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";

        final var expectedCertificate = mock(Utlatande.class);

        setupMockToThrowExceptionFromWebcert();
        setupMockToReturnFromIntygstjanst(null, expectedCertificate, false);

        final var actualCertificate = getCertificateService.getCertificateAsUtlatande(certificateId, certificateType);

        assertEquals(expectedCertificate, actualCertificate);
    }

    private void setupMockToReturnFromWebcert(Intyg certificate, Utlatande utlatande, boolean asIntyg)
        throws Exception {
        final var utkast = mock(Utkast.class);
        final var modelJson = "MODEL_JSON";
        doReturn(modelJson).when(utkast).getModel();
        doReturn("CERTIFICATE_VERSION").when(utkast).getIntygTypeVersion();
        doReturn(utkast).when(utkastService).getDraft("CERTIFICATE_ID", "CERTIFICATE_TYPE", false);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(intygModuleRegistry).getModuleApi("CERTIFICATE_TYPE", "CERTIFICATE_VERSION");
        doReturn("CERTIFICATE_TYPE").when(utlatande).getTyp();
        doReturn("CERTIFICATE_VERSION").when(utlatande).getTextVersion();
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(modelJson);
        if (asIntyg) {
            doReturn(certificate).when(moduleApi).getIntygFromUtlatande(utlatande);
        }
    }

    private void setupMockToThrowExceptionFromWebcert() {
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Missing!")).when(utkastService)
            .getDraft("CERTIFICATE_ID", "CERTIFICATE_TYPE", false);
    }

    private void setupMockToReturnFromIntygstjanst(Intyg certificate, Utlatande utlatande, boolean asIntyg)
        throws Exception {
        final var certificateContentHolder = mock(IntygContentHolder.class);
        doReturn(certificateContentHolder).when(intygService).fetchIntygDataForInternalUse("CERTIFICATE_ID", true);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(intygModuleRegistry).getModuleApi("CERTIFICATE_TYPE", "CERTIFICATE_VERSION");
        doReturn("CERTIFICATE_TYPE").when(utlatande).getTyp();
        doReturn("CERTIFICATE_VERSION").when(utlatande).getTextVersion();
        doReturn(utlatande).when(certificateContentHolder).getUtlatande();
        if (asIntyg) {
            doReturn(certificate).when(moduleApi).getIntygFromUtlatande(utlatande);
        }
    }
}
