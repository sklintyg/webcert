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
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKodTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Referens;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.web.controller.api.IcfRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.AktivitetsBegransningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.FunktionsNedsattningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfDiagnoskodResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfResponse;

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

    @Override
    public Optional<IcfResponse> findIcfInformationByIcd10Koder(final IcfRequest icfRequest) {

        Preconditions.checkArgument(Objects.nonNull(icfRequest));
        Preconditions.checkArgument(Objects.nonNull(icfRequest.getIcd10Code1()));

        return convertToResponse(
                Tuple.of(icfRequest.getIcd10Code1(), repository.findByIcd10KodList_kod(icfRequest.getIcd10Code1())),
                Tuple.of(icfRequest.getIcd10Code2(), repository.findByIcd10KodList_kod(icfRequest.getIcd10Code2())),
                Tuple.of(icfRequest.getIcd10Code3(), repository.findByIcd10KodList_kod(icfRequest.getIcd10Code3())));
    }

    private Optional<IcfResponse> convertToResponse(
            final Tuple2<String, Optional<DiagnosInformation>> kod1Respons,
            final Tuple2<String, Optional<DiagnosInformation>> kod2Respons,
            final Tuple2<String, Optional<DiagnosInformation>> kod3Respons) {

        final List<Tuple2<String, Optional<DiagnosInformation>>> responsList = List.of(kod1Respons, kod2Respons, kod3Respons);

        final IcfDiagnoskodResponse gemensammaKoder = getGemensammaKoder(responsList);
        final List<IcfDiagnoskodResponse> unikaKoder = getUnikaKoder(responsList, gemensammaKoder);

        return Optional.of(IcfResponse.of(
                gemensammaKoder,
                unikaKoder.toJavaList()));
    }

    private List<IcfDiagnoskodResponse> getUnikaKoder(
            final List<Tuple2<String, Optional<DiagnosInformation>>> responsList, final IcfDiagnoskodResponse gemensammaKoder) {

        return responsList.filter(tuple -> tuple._2.isPresent())
                .filter(hasIcfCodes())
                .map(pair -> IcfDiagnoskodResponse.of(
                        pair._1,
                        pair._2.map(getDiagnosKoder(BeskrivningTyp.FUNKTIONSNEDSATTNING, gemensammaKoder.getFunktionsNedsattningsKoder()))
                                .orElse(null),
                        pair._2.map(getDiagnosKoder(BeskrivningTyp.AKTIVITETSBEGRANSNING, gemensammaKoder.getAktivitetsBegransningsKoder()))
                                .orElse(null)
                ));
    }

    private IcfDiagnoskodResponse getGemensammaKoder(final List<Tuple2<String, Optional<DiagnosInformation>>> responsList) {

        List<IcfKoder> funktionsKoder = responsList
                .map(pair -> pair._2.orElse(null))
                .filter(Objects::nonNull)
                .map(getDiagnosKoder(BeskrivningTyp.FUNKTIONSNEDSATTNING));

        List<IcfKoder> aktivitetsKoder = responsList
                .map(pair -> pair._2.orElse(null))
                .filter(Objects::nonNull)
                .map(getDiagnosKoder(BeskrivningTyp.AKTIVITETSBEGRANSNING));

        return IcfDiagnoskodResponse.of(
                findGemensammaKoder(funktionsKoder, BeskrivningTyp.FUNKTIONSNEDSATTNING),
                findGemensammaKoder(aktivitetsKoder, BeskrivningTyp.AKTIVITETSBEGRANSNING));
    }

    private IcfKoder findGemensammaKoder(final List<IcfKoder> icfKoderList, final BeskrivningTyp typ) {

        final List<IcfKoder> allaKoder = List.ofAll(icfKoderList);

        List<IcfKod> gemensammaCentralKoder = List.empty();
        List<IcfKod> gemensammaKompletterandeKoder = List.empty();

        if (allaKoder.length() > 1) {
            for (int i = 0; i < allaKoder.length() - 1; i++) {

                gemensammaCentralKoder = gemensammaCentralKoder.appendAll(Sets.intersection(
                        Sets.newHashSet(allaKoder.get(i).getCentralaKoder()),
                        Sets.newHashSet(allaKoder.get(i + 1).getCentralaKoder())).immutableCopy());

                gemensammaKompletterandeKoder = gemensammaKompletterandeKoder.appendAll(Sets.intersection(
                        Sets.newHashSet(allaKoder.get(i).getKompletterandeKoder()),
                        Sets.newHashSet(allaKoder.get(i + 1).getKompletterandeKoder())).immutableCopy());
            }
        }

        if (typ == BeskrivningTyp.FUNKTIONSNEDSATTNING) {
            return FunktionsNedsattningsKoder.of(
                    gemensammaCentralKoder.toJavaList(),
                    gemensammaKompletterandeKoder.toJavaList());
        } else if (typ == BeskrivningTyp.AKTIVITETSBEGRANSNING) {
            return AktivitetsBegransningsKoder.of(
                    gemensammaCentralKoder.toJavaList(),
                    gemensammaKompletterandeKoder.toJavaList());
        } else {
            throw new IllegalArgumentException("Incorrect Type for variable typ");
        }
    }

    private Predicate<Tuple2<String, Optional<DiagnosInformation>>> hasIcfCodes() {
        return pair -> {
            if (!pair._2.isPresent()) {
                return false;
            }

            final List<se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKod> icfKods = List.ofAll(pair._2.get().getBeskrivningList())
                    .flatMap(Beskrivning::getIcfKodList)
                    .orElse(List.empty());

            return !icfKods.isEmpty();
        };
    }

    private Function<DiagnosInformation, IcfKoder> getDiagnosKoder(final BeskrivningTyp typ) {
        return info -> info.getBeskrivningList().stream()
                .filter(filterBeskrivning(typ))
                .collect(toOptional())
                .map(toIcfKoder(null))
                .orElse(null);
    }

    private Function<DiagnosInformation, IcfKoder> getDiagnosKoder(final BeskrivningTyp typ, final IcfKoder exkludera) {
        return info -> info.getBeskrivningList().stream()
                .filter(filterBeskrivning(typ))
                .collect(toOptional())
                .map(toIcfKoder(exkludera))
                .orElse(null);
    }

    private Predicate<Beskrivning> filterBeskrivning(final BeskrivningTyp beskrivningTyp) {
        return beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), beskrivningTyp);
    }

    private Function<Beskrivning, IcfKoder> toIcfKoder(final IcfKoder exkludera) {
        return beskrivning -> {
            final java.util.List<IcfKod> centralKoder = beskrivning.getIcfKodList().stream()
                    .filter(kod -> kod.getIcfKodTyp() == IcfKodTyp.CENTRAL)
                    .map(kod -> IcfKod.of(kod.getKod(), "temp-beskrivning"))
                    .filter(kod -> exkludera == null || !exkludera.getCentralaKoder().contains(kod))
                    .collect(Collectors.toList());

            final java.util.List<IcfKod> kompletterandeKoder = beskrivning.getIcfKodList().stream()
                    .filter(kod -> kod.getIcfKodTyp() == IcfKodTyp.KOMPLETTERANDE)
                    .map(kod -> IcfKod.of(kod.getKod(), "temp-beskrivning"))
                    .filter(kod -> exkludera == null || !exkludera.getKompletterandeKoder().contains(kod))
                    .collect(Collectors.toList());

            IcfKoder icfKoder = null;
            switch (beskrivning.getBeskrivningTyp()) {
                case FUNKTIONSNEDSATTNING:
                    icfKoder = FunktionsNedsattningsKoder.of(centralKoder, kompletterandeKoder);
                    break;
                case AKTIVITETSBEGRANSNING:
                    icfKoder = AktivitetsBegransningsKoder.of(centralKoder, kompletterandeKoder);
                    break;
            }
            return icfKoder;
        };
    }

    private FmbResponse convertToResponse(
            final String icd10,
            final String icd10CodeDeskription,
            final DiagnosInformation diagnosInformation) {

        final String upperCaseIcd10 = icd10.toUpperCase();

        final Icd10Kod kod = diagnosInformation.getIcd10KodList().stream()
                .filter(icd10Kod -> StringUtils.equalsIgnoreCase(icd10Kod.getKod(), upperCaseIcd10))
                .collect(onlyElement());

        final Optional<Beskrivning> aktivitetsBegransing = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.AKTIVITETSBEGRANSNING))
                .collect(toOptional());

        final Optional<Beskrivning> funktionsNedsattning = diagnosInformation.getBeskrivningList().stream()
                .filter(beskrivning -> Objects.equals(beskrivning.getBeskrivningTyp(), BeskrivningTyp.FUNKTIONSNEDSATTNING))
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
        } else {
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

    private String getDiagnoseDescriptionForIcd10Code(String icd10Kod) {

        final int minCharCount = 3;

        String icd10TrimmedCode = icd10Kod;
        while (icd10TrimmedCode.length() >= minCharCount) {
            final DiagnosResponse response = diagnosService.getDiagnosisByCode(icd10TrimmedCode, Diagnoskodverk.ICD_10_SE);
            if (nonNull(response) && nonNull(response.getResultat()) && response.getResultat().equals(DiagnosResponseType.OK)) {
                final Diagnos first = Iterables.getFirst(response.getDiagnoser(), null);
                return first != null ? first.getBeskrivning() : null;
            }
            // Make the icd10-code one position shorter, and thus more general.
            icd10TrimmedCode = StringUtils.chop(icd10TrimmedCode);
        }
        return icd10Kod;
    }
}
