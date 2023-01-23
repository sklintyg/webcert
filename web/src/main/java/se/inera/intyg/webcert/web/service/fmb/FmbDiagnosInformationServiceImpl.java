/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import io.vavr.Tuple2;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.integration.fmb.model.TidEnhet;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.model.dto.MaximalSjukskrivningstidDagar;
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
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;

@Service
public class FmbDiagnosInformationServiceImpl extends FmbBaseService implements FmbDiagnosInformationService {

    private final DiagnosService diagnosService;
    private final FmbSjukfallService sjukfallService;

    public FmbDiagnosInformationServiceImpl(
        final DiagnosService diagnosService,
        final FmbSjukfallService sjukfallService,
        final DiagnosInformationRepository repository) {
        super(repository);
        this.diagnosService = diagnosService;
        this.sjukfallService = sjukfallService;
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FmbResponse> findFmbDiagnosInformationByIcd10Kod(final String icd10Kod) {
        Preconditions.checkArgument(Objects.nonNull(icd10Kod));
        return getFmbContent(icd10Kod);
    }

    @Override
    public MaximalSjukskrivningstidResponse validateSjukskrivningtidForPatient(
        final MaximalSjukskrivningstidRequest maximalSjukskrivningstidRequest) {

        final Personnummer personnummer = maximalSjukskrivningstidRequest.getPersonnummer();
        final List<Period> periods = maximalSjukskrivningstidRequest.getPeriods();
        final Icd10KoderRequest icd10Koder = maximalSjukskrivningstidRequest.getIcd10Koder();

        final int total = sjukfallService.totalSjukskrivningstidForPatientAndCareUnit(personnummer, periods);
        final Collection<String> validIcd10Codes = getValidIcd10Codes(icd10Koder.getIcd10Codes());
        final Optional<MaximalSjukskrivningstidDagar> maxRek = findMaximalSjukrivningstidDagarByIcd10Koder(validIcd10Codes);

        return maxRek
            .map(rek -> MaximalSjukskrivningstidResponse.fromFmbRekommendation(
                total, rek.getMaximalSjukrivningstidDagar(), rek.getIcd10Kod(),
                toDisplayFormat(rek.getMaximalSjukrivningstidSourceValue(), rek.getMaximalSjukrivningstidSourceUnit())))
            .orElseGet(() -> MaximalSjukskrivningstidResponse.ingenFmbRekommendation(
                total));
    }

    private Optional<MaximalSjukskrivningstidDagar> findMaximalSjukrivningstidDagarByIcd10Koder(Collection<String> icd10Codes) {
        if (!icd10Codes.isEmpty()) {
            return repository.findMaximalSjukrivningstidDagarByIcd10Koder(new HashSet<>(icd10Codes)).stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    private Optional<FmbResponse> getFmbContent(final String icd10Kod) {

        final Tuple2<String, Optional<DiagnosInformation>> diagnosInformation = searchDiagnosInformationByIcd10Kod(icd10Kod);

        if (diagnosInformation._2.isPresent()) {
            final DiagnosResponse response = diagnosService.getDiagnosisByCode(diagnosInformation._1, Diagnoskodverk.ICD_10_SE);
            String beskrivning = null;
            if (nonNull(response) && nonNull(response.getResultat()) && response.getResultat().equals(DiagnosResponseType.OK)) {
                final Diagnos first = response.getDiagnoser().stream().findFirst().orElse(null);
                beskrivning = first != null ? first.getBeskrivning() : null;
            }
            return Optional.of(convertToResponse(diagnosInformation._1, beskrivning, diagnosInformation._2.get()));
        }

        return Optional.empty();
    }

    private List<String> getValidIcd10Codes(final Collection<String> icd10Codes) {
        return Optional.ofNullable(icd10Codes).orElseGet(Collections::emptyList).stream()
            .map(this::searchDiagnosInformationByIcd10Kod)
            .filter(tuple -> tuple._2.isPresent())
            .map(tuple -> tuple._1)
            .distinct()
            .collect(Collectors.toList());
    }

    private FmbResponse convertToResponse(
        final String icdTrimmed,
        final String icd10CodeDeskription,
        final DiagnosInformation diagnosInformation) {

        final String upperCaseIcd10 = icdTrimmed.toUpperCase();

        final Icd10Kod kod = diagnosInformation.getIcd10KodList().stream()
            .filter(icd10Kod -> StringUtils.equalsIgnoreCase(icd10Kod.getKod(), upperCaseIcd10))
            .collect(onlyElement());

        final String relatedDiagnoses = diagnosInformation.getIcd10KodList().stream().map(Icd10Kod::getKod)
            .collect(Collectors.joining(", "));

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

        final String rehabilitering = diagnosInformation.getInformationOmRehabilitering();

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

        if (rehabilitering != null) {
            fmbFormList.add(
                new FmbForm(
                    FmbFormName.INFORMATIONOMREHABILITERING,
                    ImmutableList.of(new FmbContent(FmbType.INFORMATIONOMREHABILITERING, rehabilitering))));
        }

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
                    Lists.newArrayList(new FmbContent(FmbType.BESLUTSUNDERLAG_TEXTUELLT, typfallList))));
        }

        final Optional<Referens> referens = diagnosInformation.getReferensList().stream().findFirst();
        final String referensDescription = referens.map(Referens::getText).orElse(null);
        final String referensLink = referens.map(Referens::getUri).orElse(null);
        final String diagnosRubrik = diagnosInformation.getDiagnosrubrik();

        return FmbResponse.of(
            upperCaseIcd10,
            icd10CodeDeskription,
            diagnosRubrik,
            relatedDiagnoses,
            referensDescription,
            referensLink,
            fmbFormList);
    }

    private String toDisplayFormat(String maximalSjukrivningstidSourceValue, String maximalSjukrivningstidSourceUnit) {
        return TidEnhet.of(maximalSjukrivningstidSourceUnit)
            .map(te -> te.getUnitDisplayValue(Ints.tryParse(maximalSjukrivningstidSourceValue))).orElse("");
    }

}
