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
package se.inera.intyg.webcert.web.service.fmb;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Objects.nonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Referens;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.service.fmb.sjukfall.FmbSjukfallService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidRequest;

@Service
public class FmbDiagnosInformationServiceImpl implements FmbDiagnosInformationService {

    private final DiagnosInformationRepository repository;
    private final DiagnosService diagnosService;
    private final FmbSjukfallService sjukfallService;

    public FmbDiagnosInformationServiceImpl(
            final DiagnosInformationRepository repository,
            final DiagnosService diagnosService,
            final FmbSjukfallService sjukfallService) {
        this.repository = repository;
        this.diagnosService = diagnosService;
        this.sjukfallService = sjukfallService;
    }

    @Override
    public FmbResponse validateSjukskrivningtidForPatient(final MaximalSjukskrivningstidRequest maximalSjukskrivningstidRequest) {
        final Optional<Integer> max = findMaximalSjukrivningstidDagarByIcd10Koder(maximalSjukskrivningstidRequest.getIcd10Koder());
        sjukfallService.beraknaSjukfallForPatient(maximalSjukskrivningstidRequest.getPersonnummer());
        return null;
    }

    private Optional<Integer> findMaximalSjukrivningstidDagarByIcd10Koder(final Icd10KoderRequest icd10KoderRequest) {
        return repository.findMaximalSjukrivningstidDagarByIcd10Koder(icd10KoderRequest.getIcd10Codes().toJavaSet());
    }

    @Override
    public Optional<FmbResponse> findFmbDiagnosInformationByIcd10Kod(final String icd10Kod) {
        Preconditions.checkArgument(Objects.nonNull(icd10Kod));
        return getFmbContent(icd10Kod);
    }

    private Optional<FmbResponse> getFmbContent(final String icd10Kod) {

        final int minCharCount = 3;

        String icd10TrimmedCode = icd10Kod;
        Optional<DiagnosInformation> diagnosInformation = Optional.empty();
        while (icd10TrimmedCode.length() >= minCharCount) {
            diagnosInformation = repository.findFirstByIcd10KodList_kod(icd10TrimmedCode);

            if (diagnosInformation.isPresent()) {
                break;
            } else {
                // Make the icd10-code one position shorter, and thus more general.
                icd10TrimmedCode = StringUtils.chop(icd10TrimmedCode);
            }
        }

        if (diagnosInformation.isPresent()) {
            final DiagnosResponse response = diagnosService.getDiagnosisByCode(icd10TrimmedCode, Diagnoskodverk.ICD_10_SE);
            String beskrivning = null;
            if (nonNull(response) && nonNull(response.getResultat()) && response.getResultat().equals(DiagnosResponseType.OK)) {
                final Diagnos first = Iterables.getFirst(response.getDiagnoser(), null);
                beskrivning = first != null ? first.getBeskrivning() : null;
            }
            return Optional.of(convertToResponse(icd10TrimmedCode, beskrivning, diagnosInformation.get()));
        }

        return Optional.empty();
    }

    private FmbResponse convertToResponse(
            final String icdTrimmed,
            final String icd10CodeDeskription,
            final DiagnosInformation diagnosInformation) {

        final String upperCaseIcd10 = icdTrimmed.toUpperCase();

        final Icd10Kod kod = diagnosInformation.getIcd10KodList().stream()
                .filter(icd10Kod -> StringUtils.equalsIgnoreCase(icd10Kod.getKod(), upperCaseIcd10))
                .collect(onlyElement());

        final Optional<Beskrivning> aktivitetsBegransing = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.AKTIVITETSBEGRANSNING))
                .filter(beskrivning -> StringUtils.isNotEmpty(beskrivning.getBeskrivningText()))
                .collect(toOptional());

        final Optional<Beskrivning> funktionsNedsattning = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.FUNKTIONSNEDSATTNING))
                .filter(beskrivning -> StringUtils.isNotEmpty(beskrivning.getBeskrivningText()))
                .collect(toOptional());

        final java.util.List<String> typfallList = kod.getTypFallList().stream()
                .sorted(Comparator.comparing(TypFall::getMaximalSjukrivningstid, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(TypFall::getTypfallsMening)
                .distinct()
                .collect(Collectors.toList());

        final String generell = diagnosInformation.getForsakringsmedicinskInformation();

        final String symptom = diagnosInformation.getSymptomPrognosBehandling();

        final java.util.List<FmbForm> fmbFormList = Lists.newArrayList();

        //mapping these codes for now to be backwards compatible with current apis
        fmbFormList.add(
                new FmbForm(
                        FmbFormName.DIAGNOS,
                        ImmutableList.of(
                                new FmbContent(FmbType.GENERELL_INFO, generell),
                                new FmbContent(FmbType.SYMPTOM_PROGNOS_BEHANDLING, symptom))));

        aktivitetsBegransing.ifPresent(beskrivning -> fmbFormList.add(
                new FmbForm(
                        FmbFormName.AKTIVITETSBEGRANSNING,
                        ImmutableList.of(new FmbContent(FmbType.AKTIVITETSBEGRANSNING, beskrivning.getBeskrivningText())))));

        funktionsNedsattning.ifPresent(beskrivning -> fmbFormList.add(
                new FmbForm(
                        FmbFormName.FUNKTIONSNEDSATTNING,
                        ImmutableList.of(new FmbContent(FmbType.FUNKTIONSNEDSATTNING, beskrivning.getBeskrivningText())))));


        if (typfallList.size() == 1) {
            fmbFormList.add(
                    new FmbForm(
                            FmbFormName.ARBETSFORMAGA,
                            Lists.newArrayList(new FmbContent(FmbType.BESLUTSUNDERLAG_TEXTUELLT, typfallList.get(0)))));
        } else if (CollectionUtils.isNotEmpty(typfallList)) {
            fmbFormList.add(
                    new FmbForm(
                            FmbFormName.ARBETSFORMAGA,
                            Lists.newArrayList(new FmbContent(
                                    FmbType.BESLUTSUNDERLAG_TEXTUELLT, typfallList))));
        }

        final Optional<Referens> referens = diagnosInformation.getReferensList().stream().findFirst();
        final String referensDescription = referens.map(Referens::getText).orElse(null);
        final String referensLink = referens.map(Referens::getUri).orElse(null);

        return FmbResponse.of(
                upperCaseIcd10,
                icd10CodeDeskription,
                referensDescription,
                referensLink,
                fmbFormList);
    }
}
