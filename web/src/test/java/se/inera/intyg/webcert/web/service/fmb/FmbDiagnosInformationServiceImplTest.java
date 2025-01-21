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
package se.inera.intyg.webcert.web.service.fmb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall.TypFallBuilder.aTypFall;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fmb.model.dto.MaximalSjukskrivningstidDagar;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.auth.AuthorityAsserter;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.fmb.sjukfall.FmbSjukfallService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;

@RunWith(MockitoJUnitRunner.class)
public class FmbDiagnosInformationServiceImplTest {

    protected static final String DIAGNOSRUBRIK_PREFIX = "diagnosrubrik-";
    @Mock
    private DiagnosService diagnosService;

    @Mock
    private DiagnosInformationRepository diagnosInformationRepository;

    @Mock
    private AuthorityAsserter authorityAsserter;

    @Mock
    private FmbSjukfallService sjukfallService;

    @InjectMocks
    private FmbDiagnosInformationServiceImpl diagnosInformationService;

    private List<Period> PERIODS = Collections.emptyList();

    @Test
    public void validateSjukskrivningtidForPatientShouldReturnOK() {

        final int rekommenderad = 14;
        final int total = 12;
        final String sourceValue = "2";
        final String sourceUnit = "wk";

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test", "test", "A10", "B20");
        final List<MaximalSjukskrivningstidDagar> max = Lists
            .newArrayList(MaximalSjukskrivningstidDagar.of("kod1", rekommenderad, sourceValue, sourceUnit));

        doReturn(max)
            .when(diagnosInformationRepository)
            .findMaximalSjukrivningstidDagarByIcd10Koder(anySet());

        doReturn(total)
            .when(sjukfallService)
            .totalSjukskrivningstidForPatientAndCareUnit(any(Personnummer.class), any(List.class));

        doReturn(Optional.of(diagnosInformation))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final MaximalSjukskrivningstidRequest request = MaximalSjukskrivningstidRequest.of(
            Icd10KoderRequest.of("kod1", "kod2", "kod3"), Personnummer.createPersonnummer("191212121212").get(), PERIODS);

        final MaximalSjukskrivningstidResponse response = diagnosInformationService.validateSjukskrivningtidForPatient(request);

        assertThat(response).isNotNull();
        assertThat(response.isOverskriderRekommenderadSjukskrivningstid()).isFalse();
        assertThat(response.getTotalSjukskrivningstid()).isEqualTo(total);
        assertThat(response.getMaximaltRekommenderadSjukskrivningstidSource()).isEqualTo("2 veckor");
    }

    @Test
    public void validateSjukskrivningtidForPatientShouldReturnNotOK() {

        final int rekommenderad = 7;
        final int total = 12;
        final String sourceValue = "1";
        final String sourceUnit = "wk";

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test", "test", "A10", "B20");
        final List<MaximalSjukskrivningstidDagar> max = Lists
            .newArrayList(MaximalSjukskrivningstidDagar.of("kod1", rekommenderad, sourceValue, sourceUnit));

        doReturn(max)
            .when(diagnosInformationRepository)
            .findMaximalSjukrivningstidDagarByIcd10Koder(anySet());

        doReturn(total)
            .when(sjukfallService)
            .totalSjukskrivningstidForPatientAndCareUnit(any(Personnummer.class), any(List.class));

        doReturn(Optional.of(diagnosInformation))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final MaximalSjukskrivningstidRequest request = MaximalSjukskrivningstidRequest.of(
            Icd10KoderRequest.of("kod1", "kod2", "kod3"), Personnummer.createPersonnummer("191212121212").get(), PERIODS);

        final MaximalSjukskrivningstidResponse response = diagnosInformationService.validateSjukskrivningtidForPatient(request);

        assertThat(response).isNotNull();
        assertThat(response.isOverskriderRekommenderadSjukskrivningstid()).isTrue();
        assertThat(response.getTotalSjukskrivningstid()).isEqualTo(total);
        assertThat(response.getMaximaltRekommenderadSjukskrivningstidSource()).isEqualTo("1 vecka");
    }

    @Test
    public void testDuplicateTexts() {

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test", "test", "A10", "B20");

        doReturn(Optional.of(diagnosInformation))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10");

        final long count = response.get().getForms().stream()
            .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
            .map(FmbForm::getContent)
            .mapToLong(Collection::size)
            .sum();

        final List<FmbContent> contentList = response.get().getForms().stream()
            .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
            .map(FmbForm::getContent)
            .findAny()
            .orElse(Lists.emptyList());

        assertEquals(DIAGNOSRUBRIK_PREFIX + "A10", response.get().getDiagnosTitle());
        assertEquals("A10, B20", response.get().getRelatedDiagnoses());
        assertEquals(1, count);
        assertEquals("test", contentList.get(0).getText());
        assertNull(contentList.get(0).getList());
    }

    @Test
    public void testMultipleTypFall() {
        final DiagnosInformation diagnosInformation =
            createDiagnosInformation("test1", "test2", "A10");

        doReturn(Optional.of(diagnosInformation))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10");

        final long count = response.get().getForms().stream()
            .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
            .map(FmbForm::getContent)
            .mapToLong(Collection::size)
            .sum();

        final List<FmbContent> contentList = response.get().getForms().stream()
            .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
            .map(FmbForm::getContent)
            .findAny()
            .orElse(null);

        assertEquals(1, count);
        assertEquals(2, contentList.get(0).getList().size());
    }

    @Test
    public void faultySearch() {
        doReturn(Optional.empty())
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10M");

        assertEquals(Optional.empty(), response);
    }

    @Test
    public void testGetFmbForIcd10IsReturningCorrectIcdCode() throws Exception {
        final String icd10 = "asdf";
        final DiagnosInformation diagnosInformation = createDiagnosInformation("test1", "test2", icd10);

        doReturn(Optional.of(diagnosInformation))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod(icd10);

        assertEquals(icd10.toUpperCase(), response.get().getIcd10Code());
    }

    @Test
    public void testWithSpecificIcd10CodeResultingInItsParentIcd10Kod() {

        final int foreslagen = 1;
        final int tidigare = 12;

        final int f31Rekommenderad = 744;
        final String f31SourceValue = "24";
        final String f31SourceUnit = "mo";

        final String icd10KodSpecific = "F314";
        final String icd10KodParent = "F31";

        final List<MaximalSjukskrivningstidDagar> max = Lists
            .newArrayList(MaximalSjukskrivningstidDagar.of("F31", f31Rekommenderad, f31SourceValue, f31SourceUnit));

        doReturn(max)
            .when(diagnosInformationRepository)
            .findMaximalSjukrivningstidDagarByIcd10Koder(anySet());

        doReturn(tidigare)
            .when(sjukfallService)
            .totalSjukskrivningstidForPatientAndCareUnit(any(Personnummer.class), any(List.class));

        final MaximalSjukskrivningstidRequest request = MaximalSjukskrivningstidRequest.of(
            Icd10KoderRequest.of("F314", null, null),
            Personnummer.createPersonnummer("191212121212").get(),
            PERIODS);

        doReturn(Optional.of(createDiagnosInformation("typfall1", "typfall2", icd10KodParent)))
            .when(diagnosInformationRepository)
            .findFirstByIcd10KodList_kod(eq(icd10KodSpecific));

        MaximalSjukskrivningstidResponse response =
            diagnosInformationService.validateSjukskrivningtidForPatient(request);

        assertThat(response).isNotNull();
        assertThat(response.getAktuellIcd10Kod()).isEqualTo(icd10KodParent);
    }

    private DiagnosInformation createDiagnosInformation(final String firstTypFallText, final String secondTypfalltext,
        final String... icd10Koder) {
        return aDiagnosInformation()
            .diagnosRubrik(DIAGNOSRUBRIK_PREFIX + Arrays.stream(icd10Koder).findFirst().orElse("NONE"))
            .forsakringsmedicinskInformation("info")
            .symptomPrognosBehandling("behandling x")
            .beskrivningList(ImmutableList.of(
                aBeskrivning()
                    .beskrivningTyp(BeskrivningTyp.AKTIVITETSBEGRANSNING)
                    .beskrivningText("begränsad")
                    .icfKodList(Collections.emptyList())
                    .build()))
            .icd10KodList(Arrays.stream(icd10Koder)
                .map(kod -> anIcd10Kod()
                    .kod(kod)
                    .beskrivning("besk")
                    .typFallList(ImmutableList.of(
                        aTypFall()
                            .typfallsMening(firstTypFallText)
                            .maximalSjukrivningstidDagar(1)
                            .build(),
                        aTypFall()
                            .typfallsMening(secondTypfalltext)
                            .maximalSjukrivningstidDagar(1)
                            .build()))
                    .build())
                .collect(Collectors.toList()))
            .referensList(Collections.emptyList())
            .senastUppdaterad(LocalDateTime.of(2018, 12, 12, 12, 12))
            .build();
    }

}
