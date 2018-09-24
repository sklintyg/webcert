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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MoreCollectors;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.model.icf.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.icf.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.icf.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.icf.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

@Service
public class FmbDiagnosInformationServiceImpl implements FmbDiagnosInformationService {

    private final DiagnosInformationRepository repository;

    private final DiagnosService diagnosService;

    public FmbDiagnosInformationServiceImpl(final DiagnosInformationRepository repository, final DiagnosService diagnosService) {
        this.repository = repository;
        this.diagnosService = diagnosService;
    }

    @Override
    public Optional<FmbResponse> findFmbDiagnosInformationByIcd10Kod(final String icd10Kod) {
        Preconditions.checkArgument(Objects.nonNull(icd10Kod));

        final String icd10CodeDeskription = getDiagnoseDescriptionForIcd10Code(icd10Kod);

        return repository.findByIcd10KodList_kod(icd10Kod)
                .map(diagnosInformation -> convertToResponse(icd10Kod, icd10CodeDeskription, diagnosInformation));
    }

    private FmbResponse convertToResponse(
            final String icd10Kodfinal,
            final String icd10CodeDeskription,
            final DiagnosInformation diagnosInformation) {

        final Icd10Kod kod = diagnosInformation.getIcd10KodList().stream().filter(icd10Kod -> Objects.equals(icd10Kod.getKod(), icd10Kodfinal))
                .collect(MoreCollectors.onlyElement());

        final Optional<Beskrivning> aktivitetsBegransing = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.AKTIVITETSBEGRANSNING))
                .collect(MoreCollectors.toOptional());

        final Optional<Beskrivning> funktionsNedsattning = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.FUNKTIONSNEDSATTNING))
                .collect(MoreCollectors.toOptional());

        final List<String> typfallList = kod.getTypFallList().stream().map(TypFall::getTypfallsMening).collect(Collectors.toList());

        final String generell = diagnosInformation.getForsakringsmedicinskInformation();

        final String symptom = diagnosInformation.getSymptomPrognosBehandling();

        final List<FmbForm> fmbFormList = Lists.newArrayList();

        fmbFormList.add(
                new FmbForm(
                        FmbFormName.DIAGNOS,
                        ImmutableList.of(new FmbContent(FmbType.GENERELL_INFO, diagnosInformation.getForsakringsmedicinskInformation()))));

        aktivitetsBegransing.ifPresent(beskrivning -> fmbFormList.add(
                new FmbForm(
                        FmbFormName.AKTIVITETSBEGRANSNING,
                        ImmutableList.of(new FmbContent(FmbType.AKTIVITETSBEGRANSNING, beskrivning.getBeskrivningText())))));

        funktionsNedsattning.ifPresent(beskrivning -> fmbFormList.add(
                new FmbForm(
                        FmbFormName.FUNKTIONSNEDSATTNING,
                        ImmutableList.of(new FmbContent(FmbType.FUNKTIONSNEDSATTNING, beskrivning.getBeskrivningText())))));
        fmbFormList.add(
                new FmbForm(
                        FmbFormName.ARBETSFORMAGA,
                        typfallList.stream().map(typfall -> new FmbContent(FmbType.BESLUTSUNDERLAG_TEXTUELLT, typfall)).collect(Collectors.toList())));

        return new FmbResponse(
                icd10Kodfinal,
                icd10CodeDeskription,
                fmbFormList);
    }

    private String getDiagnoseDescriptionForIcd10Code(String icd10WithFmb) {
        DiagnosResponse response = diagnosService.getDiagnosisByCode(icd10WithFmb, Diagnoskodverk.ICD_10_SE);
        if (!response.getResultat().equals(DiagnosResponseType.OK)) {
//            LOG.info("Failed to get diagnose description for {} with result {}", icd10WithFmb, response.getResultat().name());
            return null;
        } else {
            return response.getDiagnoser().get(0).getBeskrivning();
        }
    }
}
