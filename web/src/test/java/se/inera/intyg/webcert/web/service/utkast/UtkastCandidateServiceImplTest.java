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

package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import se.inera.intyg.common.ag7804.v1.rest.Ag7804ModuleApiV1;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceImpl;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

/**
 * @author Magnus Ekstrand on 2019-08-28.
 */
@RunWith(MockitoJUnitRunner.class)
public class UtkastCandidateServiceImplTest {

    private WebCertUser webCertUser;

    @Mock
    DraftAccessServiceImpl draftAccessService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private Ag7804ModuleApiV1 ag7804ModuleApiV1Mock;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private PUService puService;

    @Mock
    private LogService logService;

    @Mock
    private LogRequestFactory logRequestFactory;

    @InjectMocks
    private UtkastCandidateServiceImpl utkastCandidateService;

    @Before
    public void setup()  {
        webCertUser = mock(WebCertUser.class);
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(final InvocationOnMock invocation) {
                final Object[] args = invocation.getArguments();
                return isUserAllowedAccessToUnit((String) args[0]);
            }
        }).when(webCertUserService).isUserAllowedAccessToUnit(anyString());
    }

    @Test
    public void getCandidateMetaDataWhenMatchingCriterias() {
        String intygIdCandidate = "correct-candidate-intygid";
        String intygType = "lisjp";
        String intygTypeVersion = "1.0";

        when(webCertUser.getValdVardgivare()).thenReturn(createSelectableVardenhet("correct-vg-hsaid"));
        when(webCertUser.getValdVardenhet()).thenReturn(createSelectableVardenhet("correct-ve-hsaid"));
        when(webCertUser.getHsaId()).thenReturn("correct-user-hsaid");

        when(draftAccessService.allowToCopyFromCandidate(anyString(), any(Personnummer.class))).
            thenReturn(AccessResult.create(AccessResultCode.NO_PROBLEM, ""));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), anyBoolean())).thenReturn(new LogRequest());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Läkare
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(true);

        // Signed by user itself on the same care unit
        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                intygIdCandidate, intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Signed by other user on the same care unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Signed by other user and on a care sub unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-veunder-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Vårdadministratör
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(false);

        // Signed by 'läkare' on the same care unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // PDL-logging shall be invoked
        verify(logService, times(4)).logReadIntyg(any(LogRequest.class), any(LogUser.class));
    }

    @Test
    public void getCandidateMetaDataWhenMissingCopyFromCriteria() {
        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(Optional.empty());

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // No PDL-logging shall be invoked
        verify(logService, times(0)).logReadIntyg(any(LogRequest.class));
    }

    @Test
    public void getCandidateMetaDataWhenAccessIsDenied() {
        String intygType = "lisjp";
        String intygTypeVersion = "1.0";

        when(draftAccessService.allowToCopyFromCandidate(anyString(), any(Personnummer.class))).
            thenReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS,
                "User missing required privilege or cannot handle sekretessmarkerad patient"));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // No PDL-logging shall be invoked
        verify(logService, times(0)).logReadIntyg(any(LogRequest.class));
    }

    @Test
    public void getCandidateMetaDataWhenNoMatchingCriterias() {
        String intygType = "lisjp";
        String intygTypeVersion = "1.0";

        when(draftAccessService.allowToCopyFromCandidate(anyString(), any(Personnummer.class))).
            thenReturn(AccessResult.create(AccessResultCode.NO_PROBLEM, ""));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Läkare
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(true);

        // Not Signed
        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.DRAFT_COMPLETE,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Wrong enhetsId - candidate is written on a unit that is not users current unit or a sub unit.
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "INcorrect-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Is revoked
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", LocalDateTime.now(),
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // To old
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now().minusDays(20), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Incompatible majorversion
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, "2.0", LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Vårdadministratör
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(false);

        // Wrong enhetsId - user and candidate must be on the same unit.
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "INcorrect-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // No PDL-logging shall be invoked
        verify(logService, times(0)).logReadIntyg(any(LogRequest.class), any(LogUser.class));
    }

    @Test
    public void getCandidateMetaDataShouldSelectCorrectCandidateIntyg() {
        String intygType = "ag7804";
        String intygTypeVersion = "1.0";

        when(webCertUser.getValdVardgivare()).thenReturn(createSelectableVardenhet("correct-vg-hsaid"));
        when(webCertUser.getValdVardenhet()).thenReturn(createSelectableVardenhet("correct-ve-hsaid"));
        when(webCertUser.getHsaId()).thenReturn("correct-user-hsaid");

        when(draftAccessService.allowToCopyFromCandidate(anyString(), any(Personnummer.class))).
            thenReturn(AccessResult.create(AccessResultCode.NO_PROBLEM, ""));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), anyBoolean())).thenReturn(new LogRequest());

        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Läkare
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(true);

        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.DRAFT_COMPLETE, // Revoked status
                "correct-ve-hsaid",
                null,
                "bad-id-1",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(1),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // Wrong version
                "correct-ve-hsaid",
                null,
                "bad-id-2",
                intygType,
                "2.0",
                LocalDateTime.now().minusDays(1),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // Other enhetsid
                "bad-ve-hsa-id",
                null,
                "bad-id-2",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(4),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // The one that should be selected
                "correct-ve-hsaid",
                null,
                "expected-intygsid",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(2),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // Revoked yesterday
                "correct-ve-hsaid",
                LocalDateTime.now().minusDays(1),
                "bad-id-5",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(2),
                "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        Optional<UtkastCandidateMetaData> metaData = utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false);

        assertTrue(metaData.isPresent());
        assertEquals(metaData.get().getIntygId(), "expected-intygsid");

        // PDL-logging shall be invoked
        verify(logService, times(1)).logReadIntyg(any(LogRequest.class), any(LogUser.class));
    }

    @Test
    public void getCandidateMetaDataWhenPatientIsSekretessmarkerad() {
        String intygIdCandidate = "correct-candidate-intygid";
        String intygType = "ag7804";
        String intygTypeVersion = "1.0";

        when(webCertUser.getValdVardgivare()).thenReturn(createSelectableVardenhet("correct-vg-hsaid"));
        when(webCertUser.getValdVardenhet()).thenReturn(createSelectableVardenhet("correct-ve-hsaid"));
        when(webCertUser.getHsaId()).thenReturn("correct-user-hsaid");

        when(draftAccessService.allowToCopyFromCandidate(anyString(), any(Personnummer.class))).
            thenReturn(AccessResult.create(AccessResultCode.NO_PROBLEM, ""));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), anyBoolean())).thenReturn(new LogRequest());

        Patient patient = createPatient("Lilltolvan", "Tolvansson", createPnr("20121212-1212"), true);
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Läkare
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(true);

        // Signed by user herself on the same care unit
        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                intygIdCandidate, intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Signed by other user on the same care unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Signed by user herself but on different unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-veunder-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Signed by other user and on different unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-veunder-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertTrue(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // - - - - - - - - - - - - - - - - - - -
        // Run tests as Vårdadministratör
        // - - - - - - - - - - - - - - - - - - -
        when(webCertUser.isLakare()).thenReturn(false);

        // Signed by a doctor on same unit
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "other-user-hsaid"));

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // PDL-logging shall be invoked
        verify(logService, times(4)).logReadIntyg(any(LogRequest.class), any(LogUser.class));
    }

    // CHECKSTYLE:OFF ParameterNumber
    private Utkast createCandidate(UtkastStatus utkastStatus, String enhetsId, LocalDateTime revokedDate, String intygId,
        String intygType, String intygTypeVersion, LocalDateTime signDate, String signedBy) {

        VardpersonReferens skapadAv = new VardpersonReferens();
        skapadAv.setHsaId(signedBy);
        skapadAv.setNamn(signedBy);

        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygType);
        utkast.setIntygsId(intygId);
        utkast.setStatus(utkastStatus);
        utkast.setEnhetsId(enhetsId);
        utkast.setAterkalladDatum(revokedDate);
        utkast.setIntygTypeVersion(intygTypeVersion);
        utkast.setSkapadAv(skapadAv);
        utkast.setSignatur(new Signatur(signDate, signedBy, intygId, "", "", "", SignaturTyp.XMLDSIG));

        return utkast;
    }
    // CHECKSTYLE:ON ParameterNumber

    private Patient createPatient(String fornamn, String efternamn, Personnummer personnummer) {
        Patient patient = new Patient();
        patient.setFornamn("Lilltolvan");
        patient.setEfternamn("Tolvansson");
        patient.setPersonId(personnummer);
        return patient;
    }

    private Patient createPatient(String fornamn, String efternamn, Personnummer personnummer, boolean sekretessmarkering) {
        Patient patient = createPatient(fornamn, efternamn, personnummer);
        patient.setSekretessmarkering(sekretessmarkering);
        return patient;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + pnr));
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

    private Boolean isUserAllowedAccessToUnit(String enhetsId) {
        if ("correct-ve-hsaid".equals(enhetsId)) {
            return true;
        }
        if ("correct-veunder-hsaid".equals(enhetsId)) {
            return true;
        }
        return false;
    }

}
