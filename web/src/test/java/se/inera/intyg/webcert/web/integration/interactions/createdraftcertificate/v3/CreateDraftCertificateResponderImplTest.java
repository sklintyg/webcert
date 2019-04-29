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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCandidate;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.stub.IntygTestDataBuilder;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.integration.tak.model.TakResult;
import se.inera.intyg.webcert.integration.tak.service.TakService;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderImplTest extends BaseCreateDraftCertificateTest {

    private static final String LOGICAL_ADDR = "1234567890";

    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";

    private static final String UTKAST_ID = "abc123";
    private static final String UTKAST_VERSION = "1";
    private static final String UTKAST_TYPE = "fk7263";
    private static final String UTKAST_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE_VERSION = "1.0";

    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private UtkastService mockUtkastService;
    @Mock
    private CreateNewDraftRequestBuilder mockRequestBuilder;
    @Mock
    private CreateDraftCertificateValidator mockValidator;
    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterService;
    @Mock
    private MonitoringLogService mockMonitoringLogService;
    @Mock
    private TakService takService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private ModuleApi Ag7804ModuleApiV1Mock;
    @Mock
    private IntygModuleFacade moduleFacade;
    @Mock
    private IntygUser intygUserMock;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private LogService logService;
    @Mock
    private LogRequestFactory logRequestFactory;

    @InjectMocks
    private CreateDraftCertificateResponderImpl responder;

    @Before
    public void setup() throws ModuleNotFoundException {
        super.setup();
        when(mockValidator.validateApplicationErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(ResultValidator.newInstance());
        when(moduleRegistry.getModuleIdFromExternalId(any())).thenReturn(UTKAST_TYPE);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(Ag7804ModuleApiV1Mock);
        when(Ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(Optional.empty());
        when(mockUtkastService.checkIfPersonHasExistingIntyg(any(), any())).thenReturn(ImmutableMap.of(
                "utkast", ImmutableMap.of(),
                "intyg", ImmutableMap.of()));
        when(intygTextsService.getLatestVersion(any(String.class))).thenReturn(INTYG_TYPE_VERSION);
    }

    @Test
    public void testCreateDraftCertificateGetCopyCandidateWhenNoCriteria() {
        when(Ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(Optional.empty());
        Personnummer personnummer = Personnummer.createPersonnummer("19121212-1212").get();

        final Optional<GetCopyFromCandidate> copyFromCandidate = responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock,
                personnummer);

        assertFalse(copyFromCandidate.isPresent());
    }

    @Test
    public void testCreateDraftCertificateGetCopyCandidateTestNonMatchingCriterias() {
        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria("lisjp", "1", 10));
        when(Ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);
        Personnummer personnummer = Personnummer.createPersonnummer("20121212-1212").get();
        SelectableVardenhet selectableVardenhetMock = createSelectableVardenhet("correct-ve-hsa-id");
        when(intygUserMock.getValdVardenhet()).thenReturn(selectableVardenhetMock);
        when(intygUserMock.getHsaId()).thenReturn("correct-user-hsaid");
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        // Signed by other user
        List<Utkast> candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", null,
                        "intygId", "1.0", LocalDateTime.now(), "INcorrect-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // Not Signed
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.DRAFT_COMPLETE,
                        "correct-ve-hsa-id", null,
                        "intygId", "1.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // Wrong enhetsId
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "INcorrect-ve-hsa-id", null,
                        "intygId", "1.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // Is revoked
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", LocalDateTime.now(),
                        "intygId", "1.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // To old
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", null,
                        "intygId", "1.0", LocalDateTime.now().minusDays(20), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // Incompatible majorversion
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", null,
                        "intygId", "2.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);
        assertFalse(responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock, personnummer).isPresent());

        // Match!
        candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", null,
                        "correct-but-to-old-intygId", "1.0", LocalDateTime.now().minusDays(20), "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED,
                        "correct-ve-hsa-id", null,
                        "correct-intygId", "1.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);

        final Optional<GetCopyFromCandidate> copyFromCandidate = responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock,
                personnummer);

        assertTrue(copyFromCandidate.isPresent());
        assertEquals("correct-intygId", copyFromCandidate.get().getIntygId());
        assertEquals("1.0", copyFromCandidate.get().getIntygTypeVersion());
    }

    @Test
    public void testCreateDraftCertificateGetCopyCandidateSelectsCorrectCandidateIntyg() {
        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria("lisjp", "1", 10));
        when(Ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);
        Personnummer personnummer = Personnummer.createPersonnummer("20121212-1212").get();
        SelectableVardenhet selectableVardenhetMock = createSelectableVardenhet("correct-ve-hsa-id");
        when(intygUserMock.getValdVardenhet()).thenReturn(selectableVardenhetMock);
        when(intygUserMock.getHsaId()).thenReturn("correct-user-hsaid");
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        List<Utkast> candidates = Arrays.asList(
                createUtkastCandidate(UtkastStatus.DRAFT_COMPLETE, // Revoked status
                        "correct-ve-hsa-id",
                        null,
                        "bad-id-1",
                        "1.0",
                        LocalDateTime.now().minusDays(1),
                        "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED, // Wrong version
                        "correct-ve-hsa-id",
                        null,
                        "bad-id-2",
                        "2.0",
                        LocalDateTime.now().minusDays(1),
                        "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED, // Other enhetsid
                        "bad-ve-hsa-id",
                        null,
                        "bad-id-2",
                        "1.0",
                        LocalDateTime.now().minusDays(4),
                        "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED, // Revoked yesterday
                        "correct-ve-hsa-id",
                        LocalDateTime.now().minusDays(1),
                        "bad-id-5",
                        "1.0",
                        LocalDateTime.now().minusDays(2),
                        "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED, // The one that should be selected
                        "correct-ve-hsa-id",
                        null,
                        "expected-intygsid",
                        "1.0",
                        LocalDateTime.now().minusDays(2),
                        "correct-user-hsaid"),
                createUtkastCandidate(UtkastStatus.SIGNED, // Signed by other user
                        "correct-ve-hsa-id",
                        null,
                        "bad-id-6",
                        "1.0",
                        LocalDateTime.now(),
                        "BAD-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(), validIntygType))
                .thenReturn(candidates);

        final Optional<GetCopyFromCandidate> copyFromCandidate = responder.getCopyFromCandidate(Ag7804ModuleApiV1Mock, intygUserMock,
                personnummer);

        assertTrue(copyFromCandidate.isPresent());
        assertEquals("expected-intygsid", copyFromCandidate.get().getIntygId());
        assertEquals("1.0", copyFromCandidate.get().getIntygTypeVersion());
    }

    private Utkast createUtkastCandidate(UtkastStatus utkastStatus, String enhetsId, LocalDateTime revokedDate, String intygsId,
            String intygTypeVersion, LocalDateTime signDate, String signedBy) {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("lisjp");
        utkast.setIntygsId(intygsId);
        utkast.setStatus(utkastStatus);
        utkast.setEnhetsId(enhetsId);
        utkast.setAterkalladDatum(revokedDate);
        utkast.setIntygTypeVersion(intygTypeVersion);
        utkast.setSignatur(new Signatur(signDate, signedBy, intygsId, "", "", "", SignaturTyp.XMLDSIG));
        return utkast;
    }

    private SelectableVardenhet createSelectableVardenhet(String veId) {
        return new SelectableVardenhet() {
            @Override
            public String getId() {
                return veId;
            }

            @Override
            public String getNamn() {
                return null;
            }

            @Override
            public List<String> getHsaIds() {
                return null;
            }
        };
    }

    @Test
    public void testCreateDraftCertificateCopyFromCandidateSuccess() throws IOException, ModuleException, IntygModuleFacadeException {

        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast newUtkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), "ag7804", "1.0", UtkastStatus.DRAFT_INCOMPLETE,
                UTKAST_JSON,
                vardperson);
        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria("lisjp", "1", 10));
        when(Ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Utlatande copyFromCriteriaUtlatande = IntygTestDataBuilder.getUtlatande();
        when(Ag7804ModuleApiV1Mock.getUtlatandeFromJson(anyString())).thenReturn(copyFromCriteriaUtlatande);
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString()))
                .thenReturn(new CertificateResponse("{}", copyFromCriteriaUtlatande, new CertificateMetaData(), false));
        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
                .thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(newUtkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        Utkast utkastCandidate = createUtkastCandidate(UtkastStatus.SIGNED,
                "SE0987654321",
                null,
                "expected-intygsid",
                "1.0",
                LocalDateTime.now().minusDays(2),
                "only-for-test-use");

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(anyString(), anySet())).thenReturn(Arrays.asList(utkastCandidate));

        LogRequest logRequest = new LogRequest();
        when(logRequestFactory.createLogRequestFromUtlatande(copyFromCriteriaUtlatande, false)).thenReturn(logRequest);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));
        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));
        verify(logService).logReadIntyg(eq(logRequest), any(LogUser.class));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getRoot(), UNIT_HSAID);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateSuccess() {

        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, INTYG_TYPE_VERSION,
                UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
                .thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(eq(SekretessStatus.FALSE));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));
        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getRoot(), UNIT_HSAID);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateTakningNotOK() {
        CreateDraftCertificateType certificateType = createCertificateType();

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(false, Lists.newArrayList("Den angivna enheten går ej att adressera för ärendekommunikation.")));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.APPLICATION_ERROR);
        assertEquals("Den angivna enheten går ej att adressera för ärendekommunikation.", response.getResult().getResultText());
    }

    @Test
    public void testCreateDraftCertificateValidationError() {
        ResultValidator resultValidator = mock(ResultValidator.class);
        CreateDraftCertificateType certificateType = createCertificateType();

        when(resultValidator.hasErrors()).thenReturn(true);
        when(mockValidator.validate(any(Intyg.class))).thenReturn(resultValidator);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateNoMIUs() {
        WebCertUser userWithoutMiu = buildWebCertUser();
        userWithoutMiu.setVardgivare(new ArrayList<>());
        when(webcertUserDetailsService.loadUserByHsaId(USER_HSAID)).thenReturn(userWithoutMiu);

        CreateDraftCertificateType certificateType = createCertificateType();

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockMonitoringLogService).logMissingMedarbetarUppdrag(USER_HSAID, UNIT_HSAID);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateMultipleMIUs() {

        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, INTYG_TYPE_VERSION,
                UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
                .thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateVardenhetAlredyExistsInRegistry() {
        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, INTYG_TYPE_VERSION,
                UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
                .thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    private VardpersonReferens createVardpersonReferens(String hsaId, String name) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hsaId);
        vardperson.setNamn(name);
        return vardperson;
    }

    private CreateNewDraftRequest createCreateNewDraftRequest(Vardenhet vardenhet) {
        CreateNewDraftRequest draftRequest = new CreateNewDraftRequest(UTKAST_ID, null, INTYG_TYPE_VERSION, null, new HoSPersonal(), null);
        draftRequest.getHosPerson().setVardenhet(vardenhet);
        return draftRequest;
    }

    private Vardenhet createVardenhet(Vardgivare vardgivare) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid("SE1234567890-1A01");
        vardenhet.setEnhetsnamn("Vardenheten");
        vardenhet.setVardgivare(vardgivare);
        return vardenhet;
    }

    private Vardgivare createVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid("SE1234567890-2B01");
        vardgivare.setVardgivarnamn("Vardgivaren");
        return vardgivare;
    }

    private CreateDraftCertificateType createCertificateType() {

        // Type
        TypAvIntyg utlTyp = new TypAvIntyg();
        utlTyp.setCode("fk7263");

        // HoSPerson
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");

        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");

        Enhet hosEnhet = new Enhet();
        hosEnhet.setEnhetsId(unitHsaId);

        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setFullstandigtNamn("Abel Baker");
        hosPerson.setPersonalId(userHsaId);
        hosPerson.setEnhet(hosEnhet);

        // Patient
        PersonId personId = new PersonId();
        personId.setRoot("PERSNR");
        personId.setExtension("19121212-1212");

        Patient patType = new Patient();
        patType.setPersonId(personId);
        patType.setFornamn("Adam");
        patType.setMellannamn("Cesarsson");
        patType.setEfternamn("Eriksson");

        Intyg utlatande = new Intyg();
        utlatande.setTypAvIntyg(utlTyp);
        utlatande.setSkapadAv(hosPerson);
        utlatande.setPatient(patType);
        utlatande.setRef("Test-ref");

        CreateDraftCertificateType certificateType = new CreateDraftCertificateType();
        certificateType.setIntyg(utlatande);

        return certificateType;
    }

    private Utkast createUtkast(String intygId, long version, String type, String intygTypeVersion, UtkastStatus status, String model,
            VardpersonReferens vardperson) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setIntygTypeVersion(intygTypeVersion);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }
}
