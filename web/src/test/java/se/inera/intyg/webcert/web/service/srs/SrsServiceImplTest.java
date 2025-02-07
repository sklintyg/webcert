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
package se.inera.intyg.webcert.web.service.srs;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.infra.integration.srs.model.SrsPrediction;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsRecommendation;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.services.SrsInfraService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.class)
public class SrsServiceImplTest {

    private static Diagnos buildDiagnosis(String code, String description) {
        Diagnos diagnosis = new Diagnos();
        diagnosis.setKod(code);
        diagnosis.setBeskrivning(description);
        return diagnosis;
    }

    private static final Diagnos DIAGNOSIS_F438A = buildDiagnosis("F438A", "Utmattningssyndrom");
    private static final Diagnos DIAGNOSIS_F438 = buildDiagnosis("F438", "Andra specificerade reaktioner på svår stress");
    private static final Diagnos DIAGNOSIS_F43 = buildDiagnosis("F43", "Anpassningsstörningar och reaktion på svår stress");

    private static Utdatafilter buildUtdataFilter(boolean prediction, boolean measure, boolean statistics) {
        Utdatafilter f = new Utdatafilter();
        f.setAtgardsrekommendation(measure);
        f.setPrediktion(prediction);
        f.setStatistik(statistics);
        return f;
    }

    public static Utlatande buildUtlatande(String certificateId, String diagnosis, String parentCertificateId) {
        GrundData grundData = new GrundData();
        if (StringUtils.isNotBlank(parentCertificateId)) {
            Relation parentRelation = new Relation();
            parentRelation.setRelationKod(RelationKod.FRLANG);
            parentRelation.setRelationIntygsId(parentCertificateId);
            grundData.setRelation(parentRelation);
        }
        return LisjpUtlatandeV1.builder()
            .setId(certificateId)
            .setDiagnoser(Arrays.asList(se.inera.intyg.common.fkparent.model.internal.Diagnos.create(diagnosis, "TEST", "TEST", "TEST")))
            .setGrundData(grundData)
            .setTextVersion("1.1")
            .build();
    }

    private static IntygContentHolder buildIntygContentHolder(String certificateId, String diagnosisCode, String extendsCertificateId,
        boolean signed) {
        IntygContentHolder certHolder = IntygContentHolder.builder()
            .contents("DUMMY-MODEL-" + certificateId)
            .revoked(false)
            .deceased(false)
            .sekretessmarkering(false)
            .patientAddressChangedInPU(false)
            .patientNameChangedInPU(false)
            .testIntyg(false) // It's a kind of testintyg but we want to unit test as if it was a real one
            .relations(new Relations())
            .latestMajorTextVersion(true)
            .build();
        return certHolder;
    }

    @Mock
    private WebCertUser user;

    @Mock
    private SrsInfraService srsInfraService;

    @Mock
    private LogService logService;

    @Mock
    private DiagnosService diagnosService;

    @Mock
    private IntygService intygService;

    @Mock
    private IntygModuleFacade intygModuleFacade;

    @InjectMocks
    private SrsServiceImpl srsServiceUnderTest;

    @Before
    public void init() throws Exception {
        SrsResponse srsResponse = new SrsResponse(
            asList(SrsRecommendation.create("please observe", "text")),
            asList(SrsRecommendation.create("recommended measure", "text")),
            asList(SrsRecommendation.create("extension measure", "text")),
            asList(SrsRecommendation.create("rehab measure", "text")),
            asList(new SrsPrediction("intyg-id-123", "F438", null,
                "OK", 1, "desc",
                0.68, 0.54,
                asList(SrsQuestionResponse.create("question1", "answer1")),
                "KORREKT", LocalDateTime.now(), 15, "2.2")
            ),
            "F438A", "OK",
            "F43", "OK",
            asList(13432, 37494, 50517, 62952, 71240)
        );
        // Initialize descriptions to null when returned from infra mock, they are decorated in SrsService.getSrs
        srsResponse.setStatistikDiagnosisDescription(null);
        srsResponse.setAtgarderDiagnosisDescription(null);

        SrsResponse srsResponseWithoutPrediction = new SrsResponse(
            asList(SrsRecommendation.create("please observe", "text")),
            asList(SrsRecommendation.create("recommended measure", "text")),
            asList(SrsRecommendation.create("extension measure", "text")),
            asList(SrsRecommendation.create("rehab measure", "text")),
            asList(new SrsPrediction("certId", "F438", null,
                "OK", 1, "desc",
                null, 0.54, null, null,
                LocalDateTime.now(), null, "2.2")
            ),
            "F438A", "OK",
            "F43", "OK",
            asList(13432, 37494, 50517, 62952, 71240));

        // Initilize mock responses
        // full SRS response with prediction
        when(srsInfraService.getSrs(any(WebCertUser.class), any(Personnummer.class), anyList(),
            refEq(buildUtdataFilter(true, true, true)), anyList(), anyInt()))
            .thenReturn(srsResponse);
        // SRS response without prediction
        when(srsInfraService.getSrs(any(WebCertUser.class), any(Personnummer.class), anyList(),
            refEq(buildUtdataFilter(false, true, true)), anyList(), anyInt()))
            .thenReturn(srsResponseWithoutPrediction);

        when(diagnosService.getDiagnosisByCode("F438A", Diagnoskodverk.ICD_10_SE))
            .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F438A), false));

        when(diagnosService.getDiagnosisByCode("F438", Diagnoskodverk.ICD_10_SE))
            .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F438), false));

        when(diagnosService.getDiagnosisByCode("F43", Diagnoskodverk.ICD_10_SE))
            .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F43), false));

        when(intygService.fetchIntygDataWithRelations("intyg-id-123", LisjpEntryPoint.MODULE_ID))
            .thenReturn(buildIntygContentHolder("intyg-id-123", "F438A", "parent-intyg-id-1", false));

        when(intygService.fetchIntygDataWithRelations("parent-intyg-id-1", LisjpEntryPoint.MODULE_ID))
            .thenReturn(buildIntygContentHolder("parent-intyg-id-1", "F438A", "parent-intyg-id2", true));

        when(intygService.fetchIntygDataWithRelations("parent-intyg-id-2", LisjpEntryPoint.MODULE_ID))
            .thenReturn(buildIntygContentHolder("parent-intyg-id-2", "F438A", null, true));

        when(intygService.fetchIntygDataWithRelations("parent-intyg-id-3", LisjpEntryPoint.MODULE_ID))
            .thenReturn(buildIntygContentHolder("parent-intyg-id-3", "F438A", null, true));

        // Match dummy models to generate different utlatande
        when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "DUMMY-MODEL-intyg-id-123"))
            .thenReturn(buildUtlatande("intyg-id-123", "F438A", "parent-intyg-id-1"));

        when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "DUMMY-MODEL-parent-intyg-id-1"))
            .thenReturn(buildUtlatande("parent-intyg-id-1", "F438A", "parent-intyg-id-2"));

        when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "DUMMY-MODEL-parent-intyg-id-2"))
            .thenReturn(buildUtlatande("parent-intyg-id-2", "F438A", "parent-intyg-id-3"));

        when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "DUMMY-MODEL-parent-intyg-id-3"))
            .thenReturn(buildUtlatande("parent-intyg-id-3", "F438A", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSrsMissingPersonalIdentityNumberShouldThrowException() throws Exception {
        srsServiceUnderTest.getSrs(user, "intyg-id-123", "", "F438A",
            true, true, true, new ArrayList<SrsQuestionResponse>(), null);
    }

    @Test(expected = InvalidPersonNummerException.class)
    public void getSrsIllFormedPersonalIdentityNumberShouldThrowException() throws Exception {
        srsServiceUnderTest.getSrs(user, "intyg-id-123", "incorrectform1912-12-12-1212", "F438A",
            true, true, true, new ArrayList<SrsQuestionResponse>(), 15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSrsMissingDiagnosisCodeShouldThrowException() throws Exception {
        srsServiceUnderTest.getSrs(user, "intyg-id-123", "191212121212", "",
            true, true, true, new ArrayList<SrsQuestionResponse>(), 15);
    }

    @Test
    public void getSrsShouldLogShowPredictionIfPredictionIsIncluded() throws Exception {
        srsServiceUnderTest.getSrs(user, "intyg-id-123", "191212121212", "F438A",
            true, true, true, new ArrayList<SrsQuestionResponse>(), 15);
        verify(logService, times(1)).logShowPrediction("191212121212", "intyg-id-123");
    }

    @Test
    public void getSrsShouldNotLogShowPredictionIfPredictionIsNotIncluded() throws Exception {
        srsServiceUnderTest.getSrs(user, "intyg-id-123", "191212121212", "F438A",
            false, true, true, new ArrayList<SrsQuestionResponse>(), 15);
        verify(logService, times(0)).logShowPrediction("191212121212", "intyg-id-123");
    }

    @Test
    public void getSrsShouldAddDiagnosisDescriptions() throws Exception {
        SrsResponse resp = srsServiceUnderTest.getSrs(user, "intyg-id-123", "191212121212",
            "F438A", true, true, true, new ArrayList<SrsQuestionResponse>(), 15);
        assertNotNull(resp);
        assertEquals("Andra specificerade reaktioner på svår stress", resp.getPredictions().get(0).getDiagnosisDescription());
        assertEquals("Utmattningssyndrom", resp.getAtgarderDiagnosisDescription());
        assertEquals("Anpassningsstörningar och reaktion på svår stress", resp.getStatistikDiagnosisDescription());
    }

    @Test
    public void getSrsShouldAddCertificateExtensionChainWithMaxThreeEntries() throws Exception {
        SrsResponse resp = srsServiceUnderTest.getSrs(user, "intyg-id-123", "191212121212",
            "F438A", true, true, true, new ArrayList<SrsQuestionResponse>(), 15);
        assertNotNull(resp);
        assertNotNull(resp.getExtensionChain());
        assertEquals(3, resp.getExtensionChain().size());
        assertEquals("intyg-id-123", resp.getExtensionChain().get(0).getCertificateId());
        assertEquals("parent-intyg-id-1", resp.getExtensionChain().get(1).getCertificateId());
        assertEquals("parent-intyg-id-2", resp.getExtensionChain().get(2).getCertificateId());
    }

    @Test
    public void getSrsShouldAddCertificateExtensionChainEvenIfNoExtension() throws Exception {
        SrsResponse resp = srsServiceUnderTest.getSrs(user, "parent-intyg-id-3", "191212121212",
            "F438A", false, true, true, new ArrayList<SrsQuestionResponse>(), null);
        assertNotNull(resp);
        assertNotNull(resp.getExtensionChain());
        assertEquals(1, resp.getExtensionChain().size());
        assertEquals("parent-intyg-id-3", resp.getExtensionChain().get(0).getCertificateId());
    }

}
