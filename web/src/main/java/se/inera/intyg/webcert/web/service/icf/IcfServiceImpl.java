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
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import com.google.common.base.Preconditions;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.apache.commons.collections.CollectionUtils;
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
import se.inera.intyg.webcert.web.service.icf.resource.IcfTextResource;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.AktivitetsBegransningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.FunktionsNedsattningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfCentralKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfDiagnoskodResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKompletterandeKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfResponse;


@Service
public class IcfServiceImpl implements IcfService {

    private static final int THREE_MATCHES = 3;
    private static final int TWO_MATCHES = 2;

    private final DiagnosInformationRepository repository;
    private final IcfTextResource textResource;

    public IcfServiceImpl(final DiagnosInformationRepository repository, final IcfTextResource textResource) {
        this.repository = repository;
        this.textResource = textResource;
    }

    @Override
    public IcfResponse findIcfInformationByIcd10Koder(final Icd10KoderRequest icd10KoderRequest) {

        Preconditions.checkArgument(Objects.nonNull(icd10KoderRequest), "Icd10KoderRequest can not be null");
        Preconditions.checkArgument(Objects.nonNull(icd10KoderRequest.getIcd10Kod1()), "Icd10KoderRequest must have an icfCode1");

        return convertToResponse(
                Tuple.of(icd10KoderRequest.getIcd10Kod1(), repository.findFirstByIcd10KodList_kod(icd10KoderRequest.getIcd10Kod1())),
                Tuple.of(icd10KoderRequest.getIcd10Kod2(), repository.findFirstByIcd10KodList_kod(icd10KoderRequest.getIcd10Kod2())),
                Tuple.of(icd10KoderRequest.getIcd10Kod3(), repository.findFirstByIcd10KodList_kod(icd10KoderRequest.getIcd10Kod3())));
    }

    private IcfResponse convertToResponse(
            final Tuple2<String, Optional<DiagnosInformation>> kod1Respons,
            final Tuple2<String, Optional<DiagnosInformation>> kod2Respons,
            final Tuple2<String, Optional<DiagnosInformation>> kod3Respons) {

        final List<Tuple2<String, Optional<DiagnosInformation>>> responsList = List.of(kod1Respons, kod2Respons, kod3Respons)
                .filter(resp -> resp._1 != null && resp._2.isPresent());

        final IcfDiagnoskodResponse gemensammaKoder = getGemensammaKoder(responsList);
        final List<IcfDiagnoskodResponse> unikaKoder = getUnikaKoder(responsList, gemensammaKoder);

        return IcfResponse.of(
                gemensammaKoder,
                unikaKoder.toJavaList());
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
                )).filter(Objects::nonNull);
    }

    private IcfDiagnoskodResponse getGemensammaKoder(final List<Tuple2<String, Optional<DiagnosInformation>>> responsList) {

        List<Tuple2<String, IcfKoder>> funktionsKoder = responsList
                .map(getDiagnosKoder(BeskrivningTyp.FUNKTIONSNEDSATTNING))
                .filter(Objects::nonNull);

        List<Tuple2<String, IcfKoder>> aktivitetsKoder = responsList
                .map(getDiagnosKoder(BeskrivningTyp.AKTIVITETSBEGRANSNING))
                .filter(Objects::nonNull);

        final Optional<IcfKoder> gemensammaNedsattning = findGemensammaKoder(funktionsKoder, BeskrivningTyp.FUNKTIONSNEDSATTNING);
        final Optional<IcfKoder> gemensammaAktivitet = findGemensammaKoder(aktivitetsKoder, BeskrivningTyp.AKTIVITETSBEGRANSNING);

        if (!(gemensammaNedsattning.isPresent() && gemensammaAktivitet.isPresent())) {
            return IcfDiagnoskodResponse.empty();
        } else {
            return IcfDiagnoskodResponse.of(gemensammaNedsattning.get(), gemensammaAktivitet.get());
        }
    }

    private Optional<IcfKoder> findGemensammaKoder(final List<Tuple2<String, IcfKoder>> icfKoderList, final BeskrivningTyp typ) {

        final String central = "central";
        final String kompletterande = "kompletterande";

        HashSet<String> icd10KoderMedGemensammaIcf = HashSet.empty();

        final List<Tuple3<String, IcfKod, String>> koder = icfKoderList.flatMap(diagnos -> {
            final List<Tuple3<String, IcfKod, String>> tempList =
                    List.ofAll(emptyIfNull(diagnos._2.getIcfKoder()))
                            .filter(IcfCentralKod.class::isInstance)
                            .map(kod -> Tuple.of(diagnos._1, kod, central));

            return tempList.appendAll(
                    List.ofAll(emptyIfNull(diagnos._2.getIcfKoder()))
                            .filter(IcfKompletterandeKod.class::isInstance)
                            .map(kod -> Tuple.of(diagnos._1, kod, kompletterande)));
        });

        List<IcfKod> gemensammaCentralKoder = List.empty();
        List<IcfKod> gemensammaBlandadeKoder = List.empty();
        List<IcfKod> gemensammaKompletterandeKoder = List.empty();

        final Map<String, List<Tuple3<String, IcfKod, String>>> byIcfKod = koder.groupBy(kod -> kod._2.getKod());

        for (Tuple2<String, List<Tuple3<String, IcfKod, String>>> icfKod : byIcfKod) {
            List<IcfKod> tempGemensammaCentralKoder = List.empty();
            List<IcfKod> tempGemensammaBlandadeKoder = List.empty();
            List<IcfKod> tempGemensammaKompletterandeKoder = List.empty();

            final List<Tuple3<String, IcfKod, String>> machesPerIcfKategori = icfKod._2;

            if (machesPerIcfKategori.size() == TWO_MATCHES) { //gemensamma för 2 Diagnoser

                final String typ0 = machesPerIcfKategori.get(0)._3;
                final String typ1 = machesPerIcfKategori.get(1)._3;

                final IcfKod icfKategori = machesPerIcfKategori.get(0)._2;
                final List<String> icd10Koder = machesPerIcfKategori.map(Tuple3::_1);

                if (typ0.equals(typ1)) {

                    if (typ0.equals(central)) {
                        tempGemensammaCentralKoder = tempGemensammaCentralKoder.append(icfKategori);
                    } else {
                        tempGemensammaKompletterandeKoder = tempGemensammaKompletterandeKoder.append(icfKategori);
                    }

                } else {
                    tempGemensammaBlandadeKoder = tempGemensammaBlandadeKoder.append(icfKategori);
                }

                icd10KoderMedGemensammaIcf = icd10KoderMedGemensammaIcf.addAll(icd10Koder);

            } else if (machesPerIcfKategori.size() == THREE_MATCHES) { //gemensamma för alla 3 Diagnoser

                final String typ0 = machesPerIcfKategori.get(0)._3;
                final String typ1 = machesPerIcfKategori.get(1)._3;
                final String typ2 = machesPerIcfKategori.get(2)._3;

                final IcfKod icfKategori = machesPerIcfKategori.get(0)._2;
                final List<String> icd10Koder = machesPerIcfKategori.map(Tuple3::_1);

                if (typ0.equals(typ1) && typ0.equals(typ2)) {

                    if (typ0.equals(central)) {
                        tempGemensammaCentralKoder = tempGemensammaCentralKoder.append(icfKategori);
                    } else {
                        tempGemensammaKompletterandeKoder = tempGemensammaKompletterandeKoder.append(icfKategori);
                    }

                } else {
                    tempGemensammaBlandadeKoder = tempGemensammaBlandadeKoder.append(icfKategori);
                }

                icd10KoderMedGemensammaIcf = icd10KoderMedGemensammaIcf.addAll(icd10Koder);
            }

            // lägg ihop svar i respektive kategori och sortera efter alfabetisk ordning på benämning.
            gemensammaCentralKoder = gemensammaCentralKoder.appendAll(tempGemensammaCentralKoder);
            gemensammaCentralKoder = gemensammaCentralKoder.sortBy(IcfKod::getBenamning);
            gemensammaBlandadeKoder = gemensammaBlandadeKoder.appendAll(tempGemensammaBlandadeKoder);
            gemensammaBlandadeKoder = gemensammaBlandadeKoder.sortBy(IcfKod::getBenamning);
            gemensammaKompletterandeKoder = gemensammaKompletterandeKoder.appendAll(tempGemensammaKompletterandeKoder);
            gemensammaKompletterandeKoder = gemensammaKompletterandeKoder.sortBy(IcfKod::getBenamning);
        }

        if (gemensammaCentralKoder.isEmpty() && gemensammaBlandadeKoder.isEmpty() && gemensammaKompletterandeKoder.isEmpty()) {
            return Optional.empty();
        }


        List<IcfKod> all = List.ofAll(gemensammaCentralKoder);
        all = all.appendAll(gemensammaBlandadeKoder);
        all = all.appendAll(gemensammaKompletterandeKoder);

        if (typ == BeskrivningTyp.FUNKTIONSNEDSATTNING) {
            return Optional.of(FunktionsNedsattningsKoder.of(icd10KoderMedGemensammaIcf.toJavaList(), all.toJavaList()));
        } else if (typ == BeskrivningTyp.AKTIVITETSBEGRANSNING) {
            return Optional.of(AktivitetsBegransningsKoder.of(icd10KoderMedGemensammaIcf.toJavaList(), all.toJavaList()));
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
                    .map(getIcfKodFromResource())
                    .filter(kod -> exkludera == null
                            || (exkludera.getIcfKoder() != null && !exkludera.getIcfKoder().contains(kod)))
                    .collect(Collectors.toList());

            final java.util.List<IcfKod> kompletterandeKoder = beskrivning.getIcfKodList().stream()
                    .filter(kod -> kod.getIcfKodTyp() == IcfKodTyp.KOMPLETTERANDE)
                    .map(getIcfKodFromResource())
                    .filter(kod -> exkludera == null
                            || (exkludera.getIcfKoder() != null && !exkludera.getIcfKoder().contains(kod)))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(centralKoder) && CollectionUtils.isEmpty(kompletterandeKoder)) {
                return null;
            }

            List<IcfKod> sortedCentralKoder = List.ofAll(centralKoder);
            sortedCentralKoder = sortedCentralKoder.sortBy(IcfKod::getBenamning);

            List<IcfKod> sortedKompletterandeKoder = List.ofAll(kompletterandeKoder);
            sortedKompletterandeKoder = sortedKompletterandeKoder.sortBy(IcfKod::getBenamning);

            List<IcfKod> allaKoder = List.empty();
            allaKoder = allaKoder.appendAll(sortedCentralKoder);
            allaKoder = allaKoder.appendAll(sortedKompletterandeKoder);

            IcfKoder icfKoder = null;
            switch (beskrivning.getBeskrivningTyp()) {
                case FUNKTIONSNEDSATTNING:
                    icfKoder = FunktionsNedsattningsKoder.of(allaKoder.toJavaList());
                    break;
                case AKTIVITETSBEGRANSNING:
                    icfKoder = AktivitetsBegransningsKoder.of(allaKoder.toJavaList());
                    break;
            }
            return icfKoder;
        };
    }

    private Function<se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKod, IcfKod> getIcfKodFromResource() {
        return kod -> {

            final Optional<IcfKod> lookup = textResource.lookupTextByIcfKod(kod.getKod());

            if (lookup.isPresent()) {

                final IcfKod found = lookup.get();
                return kod.getIcfKodTyp() == IcfKodTyp.CENTRAL
                        ? IcfCentralKod.of(kod.getKod(), found.getBenamning(), found.getBeskrivning(), found.getInnefattar())
                        : IcfKompletterandeKod.of(kod.getKod(), found.getBenamning(), found.getBeskrivning(), found.getInnefattar());
            }

            return kod.getIcfKodTyp() == IcfKodTyp.CENTRAL
                    ? IcfCentralKod.of(kod.getKod(), "", "", "")
                    : IcfKompletterandeKod.of(kod.getKod(), "", "", "");
        };
    }

}
