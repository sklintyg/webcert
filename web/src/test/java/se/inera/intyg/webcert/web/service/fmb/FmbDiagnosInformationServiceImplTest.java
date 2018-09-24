package se.inera.intyg.webcert.web.service.fmb;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.TypFall.TypFallBuilder.aTypFall;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.persistence.fmb.model.icf.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
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
                .findByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10");

        final long count = response.get().getForms().stream()
                .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
                .map(FmbForm::getContent)
                .mapToLong(Collection::size)
                .sum();

        assertEquals(1, count);
    }

    @Test
    public void testMultipleTypFall() {

        final DiagnosInformation diagnosInformation = createDiagnosInformation("test1", "test2", "A10");

        doReturn(Optional.of(diagnosInformation))
                .when(diagnosInformationRepository)
                .findByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10");

        final long count = response.get().getForms().stream()
                .filter(fmbForm -> Objects.equals(fmbForm.getName(), FmbFormName.ARBETSFORMAGA))
                .map(FmbForm::getContent)
                .mapToLong(Collection::size)
                .sum();

        assertEquals(2, count);
    }

    @Test
    public void faultySearch() {

        doReturn(Optional.empty())
                .when(diagnosInformationRepository)
                .findByIcd10KodList_kod(anyString());

        final Optional<FmbResponse> response = diagnosInformationService.findFmbDiagnosInformationByIcd10Kod("A10M");

        assertEquals(Optional.empty(), response);
    }

    @Test
    public void testGetFmbForIcd10IsReturningCorrectIcdCode() throws Exception {
        final String icd10 = "asdf";
        final DiagnosInformation diagnosInformation = createDiagnosInformation("test1", "test2", icd10);

        doReturn(Optional.of(diagnosInformation))
                .when(diagnosInformationRepository)
                .findByIcd10KodList_kod(anyString());

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
                                                .maximalSjukrivningstid(1)
                                                .build(),
                                        aTypFall()
                                                .typfallsMening(secondTypfalltext)
                                                .maximalSjukrivningstid(1)
                                                .build()))
                                .build())
                        .collect(Collectors.toList()))
                .referensList(Collections.emptyList())
                .senastUppdaterad(LocalDateTime.of(2018, 12, 12, 12, 12))
                .build();
    }

}
