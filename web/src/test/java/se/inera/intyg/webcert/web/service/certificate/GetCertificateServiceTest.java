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
    private GetCertificateService getCertificateService;

    @Test
    public void shallReturnFromWebcertIfExists() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";
        final var certificateVersion = "CERTIFICATE_VERSION";

        final var expectedCertificate = mock(Intyg.class);

        setupMockToReturnFromWebcert(certificateId, certificateType, certificateVersion, expectedCertificate);

        final var actualCertificate = getCertificateService.getCertificate(certificateId, certificateType, certificateVersion);

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shallReturnFromIntygstjanstIfItDoesntExistsInWebcert() throws Exception {
        final var certificateId = "CERTIFICATE_ID";
        final var certificateType = "CERTIFICATE_TYPE";
        final var certificateVersion = "CERTIFICATE_VERSION";

        final var expectedCertificate = mock(Intyg.class);

        setupMockToThrowExceptionFromWebcert(certificateId, certificateType);
        setupMockToReturnFromIntygstjanst(certificateId, certificateType, certificateVersion, expectedCertificate);

        final var actualCertificate = getCertificateService.getCertificate(certificateId, certificateType, certificateVersion);

        assertEquals(expectedCertificate, actualCertificate);
    }

    private void setupMockToReturnFromWebcert(String certificateId, String certificateType, String certificateVersion, Intyg certificate)
        throws Exception {
        final var moduleId = "MODULE_ID";
        doReturn(moduleId).when(intygModuleRegistry).getModuleIdFromExternalId(certificateType);
        final var utkast = mock(Utkast.class);
        final var modelJson = "MODEL_JSON";
        doReturn(modelJson).when(utkast).getModel();
        doReturn(utkast).when(utkastService).getDraft(certificateId, moduleId, false);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(intygModuleRegistry).getModuleApi(moduleId, certificateVersion);
        final var utlatande = mock(Utlatande.class);
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(modelJson);
        doReturn(certificate).when(moduleApi).getIntygFromUtlatande(utlatande);
    }

    private void setupMockToThrowExceptionFromWebcert(String certificateId, String certificateType) {
        final var moduleId = "MODULE_ID";
        doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Missing!")).when(utkastService)
            .getDraft(certificateId, moduleId, false);
    }
    
    private void setupMockToReturnFromIntygstjanst(String certificateId, String certificateType, String certificateVersion,
        Intyg certificate)
        throws Exception {
        final var certificateContentHolder = mock(IntygContentHolder.class);
        doReturn(certificateContentHolder).when(intygService).fetchIntygDataForInternalUse(certificateId, true);
        final var moduleId = "MODULE_ID";
        doReturn(moduleId).when(intygModuleRegistry).getModuleIdFromExternalId(certificateType);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(intygModuleRegistry).getModuleApi(moduleId, certificateVersion);
        final var utlatande = mock(Utlatande.class);
        doReturn(utlatande).when(certificateContentHolder).getUtlatande();
        doReturn(certificate).when(moduleApi).getIntygFromUtlatande(utlatande);
    }
}