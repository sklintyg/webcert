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
package se.inera.intyg.webcert.web.service.icf;

import static com.google.common.collect.MoreCollectors.toOptional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKodTyp;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.web.web.controller.api.IcfRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.AktivitetsBegransningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.FunktionsNedsattningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfDiagnoskodResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfResponse;

@Service
public class IcfServiceImpl implements IcfService {

    private final DiagnosInformationRepository repository;

    public IcfServiceImpl(final DiagnosInformationRepository repository) {
        this.repository = repository;
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

        final List<Tuple2<String, Optional<DiagnosInformation>>> responsList = List.of(kod1Respons, kod2Respons, kod3Respons)
                .filter(resp -> resp._1 != null && resp._2.isPresent());

        final IcfDiagnoskodResponse gemensammaKoder = getGemensammaKoder(responsList);
        final List<IcfDiagnoskodResponse> unikaKoder = getUnikaKoder(responsList, gemensammaKoder);

        return Optional.of(IcfResponse.of(
                gemensammaKoder,
                unikaKoder.toJavaList()));
    }

    private List<IcfDiagnoskodResponse> getUnikaKoder(
            final List<Tuple2<String, Optional<DiagnosInformation>>> responsList, final IcfDiagnoskodResponse gemensammaKoder) {

        return responsList
                .filter(tuple -> tuple._2.isPresent())
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

        List<Tuple2<String, IcfKoder>> funktionsKoder = responsList
                .map(getDiagnosKoder(BeskrivningTyp.FUNKTIONSNEDSATTNING));

        List<Tuple2<String, IcfKoder>> aktivitetsKoder = responsList
                .map(getDiagnosKoder(BeskrivningTyp.AKTIVITETSBEGRANSNING));


        final Optional<IcfKoder> gemensammaNedsattning = findGemensammaKoder(funktionsKoder, BeskrivningTyp.FUNKTIONSNEDSATTNING);
        final Optional<IcfKoder> gemensammaAktivitet = findGemensammaKoder(aktivitetsKoder, BeskrivningTyp.AKTIVITETSBEGRANSNING);

        if (!(gemensammaNedsattning.isPresent() && gemensammaAktivitet.isPresent())) {
            return IcfDiagnoskodResponse.empty();
        } else {
            return IcfDiagnoskodResponse.of(gemensammaNedsattning.get(), gemensammaAktivitet.get());
        }
    }

    private Optional<IcfKoder> findGemensammaKoder(final List<Tuple2<String, IcfKoder>> icfKoderList, final BeskrivningTyp typ) {

        HashSet<String> icd10KoderMedGemensammaIcf = HashSet.empty();

        final List<Tuple2<String, IcfKoder>> allaKoder = List.ofAll(icfKoderList);
        List<IcfKod> gemensammaCentralKoder = List.empty();
        List<IcfKod> gemensammaKompletterandeKoder = List.empty();

        if (allaKoder.length() > 1) {
            for (int i = 0; i < allaKoder.length() - 1; i++) {

                List<IcfKod> tempCentral = List.empty();
                List<IcfKod> tempKompletterande = List.empty();
                tempCentral = tempCentral.appendAll(Sets.intersection(
                        Sets.newHashSet(allaKoder.get(i)._2.getCentralaKoder()),
                        Sets.newHashSet(allaKoder.get(i + 1)._2.getCentralaKoder())).immutableCopy());

                tempKompletterande = tempKompletterande.appendAll(Sets.intersection(
                        Sets.newHashSet(allaKoder.get(i)._2.getKompletterandeKoder()),
                        Sets.newHashSet(allaKoder.get(i + 1)._2.getKompletterandeKoder())).immutableCopy());

                if (!tempCentral.isEmpty() || !tempKompletterande.isEmpty()) {
                    icd10KoderMedGemensammaIcf = icd10KoderMedGemensammaIcf.add(allaKoder.get(i)._1);
                    icd10KoderMedGemensammaIcf = icd10KoderMedGemensammaIcf.add(allaKoder.get(i + 1)._1);
                }

                gemensammaCentralKoder = gemensammaCentralKoder.appendAll(tempCentral);
                gemensammaKompletterandeKoder = gemensammaKompletterandeKoder.appendAll(tempKompletterande);
            }
        }

        if (gemensammaCentralKoder.isEmpty() && gemensammaKompletterandeKoder.isEmpty()) {
            return Optional.empty();
        }

        if (typ == BeskrivningTyp.FUNKTIONSNEDSATTNING) {
            return Optional.of(FunktionsNedsattningsKoder.of(
                    icd10KoderMedGemensammaIcf.toJavaList(),
                    gemensammaCentralKoder.toJavaList(),
                    gemensammaKompletterandeKoder.toJavaList()));
        } else if (typ == BeskrivningTyp.AKTIVITETSBEGRANSNING) {
            return Optional.of(AktivitetsBegransningsKoder.of(
                    icd10KoderMedGemensammaIcf.toJavaList(),
                    gemensammaCentralKoder.toJavaList(),
                    gemensammaKompletterandeKoder.toJavaList()));
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

    private Function<Tuple2<String, Optional<DiagnosInformation>>, Tuple2<String, IcfKoder>> getDiagnosKoder(final BeskrivningTyp typ) {
        return pair -> pair._2
                .map(info -> info.getBeskrivningList().stream()
                        .filter(filterBeskrivning(typ))
                        .collect(toOptional())
                        .map(toIcfKoder(null))
                        .map(koder -> Tuple.of(pair._1, koder))
                        .orElse(null))
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

}
