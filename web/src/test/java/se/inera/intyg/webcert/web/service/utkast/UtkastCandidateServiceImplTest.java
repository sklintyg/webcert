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
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.ag7804.v1.rest.Ag7804ModuleApiV1;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
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
    private WebCertUserService webCertUserService;

    @Mock
    private Ag7804ModuleApiV1 ag7804ModuleApiV1Mock;

    @Mock
    private UtkastRepository utkastRepository;

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
    }

    @Test
    public void getCandidateMetaDataWhenMatchingCriterias() {
        String intygIdCandidate = "correct-candidate-intygid";
        String intygType = "ag7804";
        String intygTypeVersion = "1.0";

        when(webCertUser.getValdVardgivare()).thenReturn(createSelectableVardenhet("correct-vg-hsaid"));
        when(webCertUser.getValdVardenhet()).thenReturn(createSelectableVardenhet("correct-ve-hsaid"));
        when(webCertUser.getHsaId()).thenReturn("correct-user-hsaid");

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lill-Tolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "correct-but-to-old-intygId", intygType, intygTypeVersion, LocalDateTime.now().minusDays(20), "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                intygIdCandidate, intygType, intygTypeVersion, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), anyBoolean())).thenReturn(new LogRequest());

        Optional<UtkastCandidateMetaData> metaData = utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false);

        // Assert
        assertTrue(metaData.isPresent());
        assertEquals(intygIdCandidate, metaData.get().getIntygId());
        assertEquals(intygTypeVersion, metaData.get().getIntygTypeVersion());

        // PDL-logging shall be invoked
        verify(logService).logReadIntyg(any(LogRequest.class), any(LogUser.class));
    }

    @Test
    public void getCandidateMetaDataWhenNoCriteria() {
        Patient patient = createPatient("Lill-Tolvan", "Tolvansson", createPnr("20121212-1212"));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(Optional.empty());

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // No PDL-logging shall be invoked
        verify(logService, times(0)).logReadIntyg(any(LogRequest.class));
    }

    @Test
    public void getCandidateMetaDataWhenNoMatchingCriterias() {
        String intygType = "ag7804";
        String intygTypeVersion = "1.0";

        when(webCertUser.getValdVardenhet()).thenReturn(createSelectableVardenhet("correct-user-hsaid"));

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lill-Tolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        // Signed by other user
        List<Utkast> candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygTypeVersion, LocalDateTime.now(), "INcorrect-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Not Signed
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.DRAFT_COMPLETE,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygType, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Wrong enhetsId
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "INcorrect-ve-hsaid", null,
                "intygId", intygType, intygType, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // Is revoked
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", LocalDateTime.now(),
                "intygId", intygType, intygType, LocalDateTime.now(), "correct-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        assertFalse(utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false).isPresent());

        // To old
        candidates = Arrays.asList(
            createCandidate(UtkastStatus.SIGNED,
                "correct-ve-hsaid", null,
                "intygId", intygType, intygType, LocalDateTime.now().minusDays(20), "correct-user-hsaid"));

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

        Optional<GetCopyFromCriteria> copyFromCriteria = Optional.of(new GetCopyFromCriteria(intygType, "1", 10));
        when(ag7804ModuleApiV1Mock.getCopyFromCriteria()).thenReturn(copyFromCriteria);

        Patient patient = createPatient("Lill-Tolvan", "Tolvansson", createPnr("20121212-1212"));
        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

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
            createCandidate(UtkastStatus.SIGNED, // Revoked yesterday
                "correct-ve-hsaid",
                LocalDateTime.now().minusDays(1),
                "bad-id-5",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(2),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // The one that should be selected
                "correct-ve-hsaid",
                null,
                "expected-intygsid",
                intygType,
                intygTypeVersion,
                LocalDateTime.now().minusDays(2),
                "correct-user-hsaid"),
            createCandidate(UtkastStatus.SIGNED, // Signed by other user
                "correct-ve-hsaid",
                null,
                "bad-id-6",
                intygType,
                intygTypeVersion,
                LocalDateTime.now(),
                "BAD-user-hsaid"));

        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patient.getPersonId().getPersonnummerWithDash(), validIntygType))
            .thenReturn(candidates);

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), anyBoolean())).thenReturn(new LogRequest());

        Optional<UtkastCandidateMetaData> metaData = utkastCandidateService.getCandidateMetaData(ag7804ModuleApiV1Mock, patient, false);

        // Assert
        assertTrue(metaData.isPresent());
        assertEquals("expected-intygsid", metaData.get().getIntygId());
        assertEquals(intygTypeVersion, metaData.get().getIntygTypeVersion());

        // PDL-logging shall be invoked
        verify(logService).logReadIntyg(any(LogRequest.class), any(LogUser.class));
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
        patient.setFornamn("Tolvan");
        patient.setEfternamn("Tolvansson");
        patient.setPersonId(personnummer);
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

}
