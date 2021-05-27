package se.inera.intyg.webcert.web.service.access;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelperImpl;

@RunWith(MockitoJUnitRunner.class)
public class CertificateAccessServiceHelperTest {

    @Mock
    private CertificateAccessService certificateAccessService;

    @Spy
    private AccessResultExceptionHelper accessResultExceptionHelper = new AccessResultExceptionHelperImpl();

    @InjectMocks
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    private Utlatande certificate;

    @Before
    public void setup() {
        certificate = mock(Utlatande.class);
        doReturn("certificateType").when(certificate).getTyp();
        doReturn("certificateTypeVersion").when(certificate).getTextVersion();
        doReturn(createGrundData()).when(certificate).getGrundData();
    }

    @Test
    public void shallThrowExceptionIfNoAccessToRenewCertificate() {
        try {
            doReturn(createNoAccessResult()).when(certificateAccessService).allowToRenew(any(AccessEvaluationParameters.class));
            certificateAccessServiceHelper.validateAccessToRenewCertificate(certificate);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToRenewCertificate() {
        doReturn(createAccessResult()).when(certificateAccessService).allowToRenew(any(AccessEvaluationParameters.class));
        certificateAccessServiceHelper.validateAccessToRenewCertificate(certificate);
        assertTrue(true);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToComplementCertificate() {
        try {
            doReturn(createNoAccessResult()).when(certificateAccessService)
                .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));
            certificateAccessServiceHelper.validateAccessToComplementCertificate(certificate);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToComplementCertificate() {
        doReturn(createAccessResult()).when(certificateAccessService)
            .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));
        certificateAccessServiceHelper.validateAccessToComplementCertificate(certificate);
        assertTrue(true);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToReplaceCertificate() {
        try {
            doReturn(createNoAccessResult()).when(certificateAccessService).allowToReplace(any(AccessEvaluationParameters.class));
            certificateAccessServiceHelper.validateAccessToReplaceCertificate(certificate);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToReplaceCertificate() {
        doReturn(createAccessResult()).when(certificateAccessService).allowToReplace(any(AccessEvaluationParameters.class));
        certificateAccessServiceHelper.validateAccessToReplaceCertificate(certificate);
        assertTrue(true);
    }

    @Test
    public void shallThrowExceptionIfNoAccessToCreateDraftFromSignedTemplate() {
        try {
            doReturn(createNoAccessResult()).when(certificateAccessService)
                .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
            certificateAccessServiceHelper.validateAllowCreateDraftFromSignedTemplate(certificate);
            assertTrue("Should throw exception if no access", false);
        } catch (AuthoritiesException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallNotThrowExeptionIfAllowAccessToCreateDraftFromSignedTemplate() {
        doReturn(createAccessResult()).when(certificateAccessService)
            .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
        certificateAccessServiceHelper.validateAllowCreateDraftFromSignedTemplate(certificate);
        assertTrue(true);
    }

    private AccessResult createAccessResult() {
        return AccessResult.noProblem();
    }

    private AccessResult createNoAccessResult() {
        return AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No Access");
    }

    private GrundData createGrundData() {
        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer("191212121212").get());
        final var vardenhet = new Vardenhet();
        final var skapadAv = new HoSPersonal();
        skapadAv.setVardenhet(vardenhet);
        final var grundData = new GrundData();
        grundData.setPatient(patient);
        grundData.setSkapadAv(skapadAv);
        return grundData;
    }
}