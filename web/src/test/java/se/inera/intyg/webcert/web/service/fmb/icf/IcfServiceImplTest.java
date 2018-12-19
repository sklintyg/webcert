/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.fmb.icf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKod.IcfKodBuilder.anIcfKod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Referens.ReferensBuilder.aReferens;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDateTime;
import java.util.Optional;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKodTyp;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.fmb.icf.resource.IcfTextResource;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfResponse;

@RunWith(MockitoJUnitRunner.class)
public class IcfServiceImplTest {

    @Mock
    private DiagnosInformationRepository repository;

    @Mock
    private IcfTextResource icfTextResource;

    @InjectMocks
    private IcfServiceImpl icfService;

    @Test
    public void testNoRequest() {
        assertThatThrownBy(() -> icfService.findIcfInformationByIcd10Koder(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Icd10KoderRequest can not be null");
    }

    @Test
    public void testNoIcd10Code1() {
        assertThatThrownBy(() -> icfService.findIcfInformationByIcd10Koder(Icd10KoderRequest.of(null, null, null)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Icd10KoderRequest must have an icfCode1");
    }

    @Test
    public void testNoMatchingIcfCodes() {

        doReturn(Optional.empty())
                .when(repository)
                .findFirstByIcd10KodList_kod(anyString());

        final IcfResponse response = icfService.findIcfInformationByIcd10Koder(Icd10KoderRequest.of("code", null, null));

        assertThat(response.getGemensamma().getIcd10Kod()).isNull();
        assertThat(response.getGemensamma().getAktivitetsBegransningsKoder()).isNull();
        assertThat(response.getGemensamma().getFunktionsNedsattningsKoder()).isNull();
        assertThat(response.getUnika()).isEmpty();
    }

    @Test
    public void testOneMatchingCode() {

        final String noMatch1 = "no-match-code1";
        final String noMatch2 = "no-match-code2";
        final String match1 = "a-match-code";

        final String icd10Kod = "icd10";

        doReturn(Optional.empty())
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(noMatch1));

        doReturn(Optional.empty())
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(noMatch2));

        doReturn(Optional.of(buildDiagnosInformation(icd10Kod, match1)))
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(match1));

        final IcfResponse icfInformationByIcd10Koder = icfService.findIcfInformationByIcd10Koder(
                Icd10KoderRequest.of(noMatch1, noMatch2, match1));


        assertThat(icfInformationByIcd10Koder).isNotNull();

        assertThat(icfInformationByIcd10Koder.getUnika()).isNotNull();
        assertThat(icfInformationByIcd10Koder.getUnika()).hasSize(1);
        assertThat(icfInformationByIcd10Koder.getUnika().get(0).getIcd10Kod()).isEqualTo(match1);
        assertThat(icfInformationByIcd10Koder.getUnika().get(0).getFunktionsNedsattningsKoder().getIcfKoder().get(0).getKod()).isEqualTo(match1);
        assertThat(icfInformationByIcd10Koder.getUnika().get(0).getFunktionsNedsattningsKoder().getIcfKoder().get(0).getKod()).isEqualTo(match1);

        assertThat(icfInformationByIcd10Koder.getGemensamma()).isNotNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getIcd10Kod()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getFunktionsNedsattningsKoder()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getAktivitetsBegransningsKoder()).isNull();
    }

    @Test
    public void testTwoMatchingCodes() {

        final String noMatch1 = "no-match-code1";
        final String match1 = "a-match-code1";
        final String match2 = "a-match-code2";

        final String icd10Kod1 = "icd10";
        final String icd10Kod2 = "icd10";

        doReturn(Optional.empty())
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(noMatch1));

        doReturn(Optional.of(buildDiagnosInformation(icd10Kod1, match1)))
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(match1));

        doReturn(Optional.of(buildDiagnosInformation(icd10Kod2, match2)))
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(match2));

        final IcfResponse icfInformationByIcd10Koder = icfService.findIcfInformationByIcd10Koder(
                Icd10KoderRequest.of(noMatch1, match1, match2));


        assertThat(icfInformationByIcd10Koder).isNotNull();

        assertThat(icfInformationByIcd10Koder.getUnika()).isNotNull();
        assertThat(icfInformationByIcd10Koder.getUnika()).hasSize(2);
    }

    @Test
    public void testGeneralizeSearch() {
        final String matchingCode = "matching-code";

        final String icd10KodTooSpecific = "M160";
        final String icd10KodGeneralized = "M16";

        doReturn(Optional.of(buildDiagnosInformation(icd10KodGeneralized, matchingCode)))
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(icd10KodGeneralized));

        final IcfResponse icfInformationByIcd10Koder = icfService.findIcfInformationByIcd10Koder(
                Icd10KoderRequest.of(icd10KodTooSpecific, null, null));

        assertThat(icfInformationByIcd10Koder).isNotNull();

        assertThat(icfInformationByIcd10Koder.getUnika()).isNotNull();
        assertThat(icfInformationByIcd10Koder.getUnika()).hasSize(1);
        assertThat(icfInformationByIcd10Koder.getUnika().get(0).getIcd10Kod()).isEqualTo(icd10KodGeneralized);
        assertThat(icfInformationByIcd10Koder.getGemensamma().getIcd10Kod()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getAktivitetsBegransningsKoder()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getFunktionsNedsattningsKoder()).isNull();
    }

    @Test
    public void testGeneralizeSearchWithTwoSpecificCodedResultingInSameParentIcd10Kod() {
        final String matchingCode = "matching-code";

        final String icd10KodTooSpecific1 = "M160";
        final String icd10KodTooSpecific2 = "M161";
        final String icd10KodGeneralized = "M16";

        doReturn(Optional.of(buildDiagnosInformation(icd10KodGeneralized, matchingCode)))
                .when(repository)
                .findFirstByIcd10KodList_kod(eq(icd10KodGeneralized));

        final IcfResponse icfInformationByIcd10Koder = icfService.findIcfInformationByIcd10Koder(
                Icd10KoderRequest.of(icd10KodTooSpecific1, icd10KodTooSpecific2, null));

        assertThat(icfInformationByIcd10Koder).isNotNull();
        assertThat(icfInformationByIcd10Koder.getUnika()).isNotNull();
        assertThat(icfInformationByIcd10Koder.getUnika()).hasSize(1);
        assertThat(icfInformationByIcd10Koder.getUnika().get(0).getIcd10Kod()).isEqualTo(icd10KodGeneralized);
        assertThat(icfInformationByIcd10Koder.getGemensamma().getIcd10Kod()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getAktivitetsBegransningsKoder()).isNull();
        assertThat(icfInformationByIcd10Koder.getGemensamma().getFunktionsNedsattningsKoder()).isNull();
    }

    private DiagnosInformation buildDiagnosInformation(final String icd10Kod, final String icfKod) {
        return aDiagnosInformation()
                .forsakringsmedicinskInformation("forsakringsmedicinskInformation")
                .symptomPrognosBehandling("symptomPrognosBehandling")
                .beskrivningList(ImmutableList.of(
                        aBeskrivning()
                                .beskrivningText("beskrivningsText")
                                .beskrivningTyp(BeskrivningTyp.FUNKTIONSNEDSATTNING)
                                .icfKodList(ImmutableList.of(
                                        anIcfKod()
                                                .kod(icfKod)
                                                .icfKodTyp(IcfKodTyp.CENTRAL)
                                                .build(),
                                        anIcfKod()
                                                .kod(icfKod)
                                                .icfKodTyp(IcfKodTyp.KOMPLETTERANDE)
                                                .build()))
                                .build()))
                .icd10KodList(ImmutableList.of(
                        anIcd10Kod()
                                .kod(icd10Kod)
                                .beskrivning("beskrivning")
                                .typFallList(ImmutableList.of())
                                .build(),
                        anIcd10Kod()
                                .kod(icd10Kod)
                                .beskrivning("beskrivning")
                                .typFallList(ImmutableList.of())
                                .build()))
                .referensList(ImmutableList.of(
                        aReferens()
                                .text("text")
                                .uri("www.uri.com")
                                .build()))
                .senastUppdaterad(LocalDateTime.of(2018, 11, 11, 11, 11))
                .build();

    }
}
