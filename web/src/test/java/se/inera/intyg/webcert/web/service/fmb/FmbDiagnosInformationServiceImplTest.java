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
package se.inera.intyg.webcert.web.service.fmb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall.TypFallBuilder.aTypFall;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

@RunWith(MockitoJUnitRunner.class)
public class FmbDiagnosInformationServiceImplTest {

    @Mock
    private DiagnosService diagnosService;

    @Mock
    private DiagnosInformationRepository diagnosInformationRepository;

    @InjectMocks
    private FmbDiagnosInformationServiceImpl diagnosInformationService;

    @Test
    public void testDuplicateTexts() {

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test", "test", "A10");

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

        assertEquals(1, count);
        assertEquals("test", contentList.get(0).getText());
        assertNull(contentList.get(0).getList());
    }

    @Test
    public void testMultipleTypFall() {

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test1", "test2", "A10");

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


    private DiagnosInformation createDiagnosInformation(final String firstTypFallText, final String secondTypfalltext, final String... icd10Koder) {
        return aDiagnosInformation()
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
