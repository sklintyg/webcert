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
package se.inera.intyg.webcert.integration.fmb.services;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static se.inera.intyg.webcert.logging.MdcLogConstants.SPAN_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKod.IcfKodBuilder.anIcfKod;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.Referens.ReferensBuilder.aReferens;
import static se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall.TypFallBuilder.aTypFall;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumer;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.Meta;
import se.inera.intyg.webcert.integration.fmb.model.TidEnhet;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxData;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmxBeskrivning;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Markup;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Fmbtillstand;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Rekommenderadsjukskrivning;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;
import se.inera.intyg.webcert.integration.fmb.model.typfall.TypfallData;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKod;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.IcfKodTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Referens;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;

@Slf4j
@Service
@Transactional
@EnableScheduling
@RequiredArgsConstructor
public class FmbServiceImpl implements FmbService {

    private static final String JOB_NAME = "FmbServiceUpdate.run";

    private final MdcHelper mdcHelper;
    private final FmbConsumer fmbConsumer;
    private final DiagnosInformationRepository repository;

    @Override
    @Scheduled(cron = "${fmb.dataupdate.cron}")
    @SchedulerLock(name = JOB_NAME)
    @PerformanceLogging(eventAction = "job-update-fmb-data", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public void updateData() {
        try {
            MDC.put(TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(SPAN_ID_KEY, mdcHelper.spanId());

            log.info("FMB data update started");
            performUpdate();
            log.info("FMB data update done");
        } catch (Exception e) {
            log.error("Failed to update FMB", e);
        } finally {
            MDC.clear();
        }
    }

    private void performUpdate() {
        final Try<Void> result = Try.run(() -> {
            final FmdxInformation diagnosinformation = fmbConsumer.getForsakringsmedicinskDiagnosinformation();
            final Typfall typfall = fmbConsumer.getTypfall();
            final List<DiagnosInformation> diagnosInformationList = convertResponseToDiagnosInformation(diagnosinformation, typfall);

            if (!isEmpty(diagnosInformationList)) {
                repository.deleteAll();
                repository.saveAll(diagnosInformationList);
            }
        });

        if (result.isFailure()) {
            log.warn("Failed to fetch FMB information");
            result.getCause().printStackTrace();
        }
    }

    private List<DiagnosInformation> convertResponseToDiagnosInformation(final FmdxInformation diagnosinformation, final Typfall typfall) {
        validateResponse(diagnosinformation, typfall);

        final Optional<LocalDateTime> senasteAndring = diagnosinformation.getOptionalMeta()
            .map(Meta::getBuildtimestamp)
            .map(timeStampString -> OffsetDateTime.parse(timeStampString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")))
            .map(OffsetDateTime::toLocalDateTime);

        return diagnosinformation.getData().stream()
            .map(FmdxData::getAttributes)
            .map(attributes -> {
                List<Beskrivning> beskrivningList = Lists.newArrayList();
                attributes.getOptionalAktivitetsbegransning().ifPresent(begransning ->
                    beskrivningList.add(convertToBeskrivning(begransning, BeskrivningTyp.AKTIVITETSBEGRANSNING)));
                attributes.getOptionalFunktionsnedsattning().ifPresent(begransning ->
                    beskrivningList.add(convertToBeskrivning(begransning, BeskrivningTyp.FUNKTIONSNEDSATTNING)));

                final List<Icd10Kod> icd10KodList = convertToIcd10KodList(attributes, typfall);

                return aDiagnosInformation()
                    .diagnosRubrik(attributes.getOptionalDiagnosrubrik().orElse(null))
                    .forsakringsmedicinskInformation(attributes.getOptionalForsakringsmedicinskinformation()
                        .map(Markup::getMarkup)
                        .orElse(null))
                    .symptomPrognosBehandling(attributes.getOptionalSymtomprognosbehandling()
                        .map(Markup::getMarkup)
                        .orElse(null))
                    .informationOmRehabilitering(attributes.getOptionalInformationomrehabilitering()
                        .map(Markup::getMarkup)
                        .orElse(null))
                    .beskrivningList(beskrivningList)
                    .icd10KodList(icd10KodList)
                    .referensList(convertToReferensList(attributes))
                    .senastUppdaterad(senasteAndring.orElse(null))
                    .build();
            }).toList();
    }

    private void validateResponse(final FmdxInformation diagnosinformation, final Typfall typfall) {
        Preconditions.checkArgument(nonNull(diagnosinformation));
        Preconditions.checkArgument(nonNull(diagnosinformation.getData()));
        Preconditions.checkArgument(nonNull(typfall));
    }

    private Beskrivning convertToBeskrivning(final FmxBeskrivning beskrivning, final BeskrivningTyp beskrivningTyp) {
        final List<IcfKod> icfKodList = Lists.newArrayList();
        icfKodList.addAll(convertToIcfKodList(beskrivning.getCentralkod(), IcfKodTyp.CENTRAL));
        icfKodList.addAll(convertToIcfKodList(beskrivning.getKompletterandekod(), IcfKodTyp.KOMPLETTERANDE));

        return aBeskrivning()
            .beskrivningTyp(beskrivningTyp)
            .beskrivningText(beskrivning.getBeskrivning() != null ? beskrivning.getBeskrivning() : "")
            .icfKodList(icfKodList)
            .build();
    }

    private List<IcfKod> convertToIcfKodList(final List<Kod> kodList, IcfKodTyp kodTyp) {
        return kodList.stream()
            .map(kod -> anIcfKod()
                .icfKodTyp(kodTyp)
                .kod(kod.getOptionalKod().isPresent()
                    ? kod.getOptionalKod().get().replace(".", "").toUpperCase(Locale.ENGLISH)
                    : null)
                .build())
            .toList();
    }

    private List<TypFall> convertToTypFallList(final Typfall typfall, final Kod kod) {
        return typfall.getData().stream()
            .map(TypfallData::getAttributes)
            .filter(filterTypfall(kod))
            .map(attributes -> aTypFall()
                .typfallsMening(attributes.getTypfallsmening())
                .maximalSjukrivningstidDagar(convertToAntalDagar(attributes.getRekommenderadsjukskrivning()))
                .maximalSjukrivningstidSourceValue(attributes.getRekommenderadsjukskrivning().getMaximalsjukskrivningstid())
                .maximalSjukrivningstidSourceUnit(attributes.getRekommenderadsjukskrivning().getMaximalsjukskrivningsenhet())
                .build())
            .toList();
    }

    private Integer convertToAntalDagar(final Rekommenderadsjukskrivning rekommenderadsjukskrivning) {
        if (!isValidRekommenderadSjukskrivning(rekommenderadsjukskrivning)) {
            return null;
        }

        final int antal = Integer.parseInt(rekommenderadsjukskrivning.getMaximalsjukskrivningstid());
        final TidEnhet enhet = TidEnhet.of(rekommenderadsjukskrivning.getMaximalsjukskrivningsenhet()).orElseThrow();
        return antal * enhet.getInDays();
    }

    private boolean isValidRekommenderadSjukskrivning(final Rekommenderadsjukskrivning rekommenderadsjukskrivning) {
        return nonNull(rekommenderadsjukskrivning)
            && nonNull(rekommenderadsjukskrivning.getMaximalsjukskrivningstid())
            && nonNull(Ints.tryParse(rekommenderadsjukskrivning.getMaximalsjukskrivningstid()))
            && nonNull(rekommenderadsjukskrivning.getMaximalsjukskrivningsenhet())
            && TidEnhet.of(rekommenderadsjukskrivning.getMaximalsjukskrivningsenhet()).isPresent();
    }

    private Predicate<se.inera.intyg.webcert.integration.fmb.model.typfall.Attributes> filterTypfall(final Kod kod) {
        return typFall -> typFall.getOptionalFmbtillstand().map(Fmbtillstand::getDiagnoskod).orElse(Collections.emptyList()).contains(kod);
    }

    private List<Icd10Kod> convertToIcd10KodList(final Attributes attributes, final Typfall typfallList) {

        return attributes.getDiagnoskod().stream()
            .map(kod -> anIcd10Kod()
                .kod(kod.getOptionalKod().isPresent()
                    ? kod.getOptionalKod().get().replace(".", "").toUpperCase(Locale.ENGLISH)
                    : null)
                .beskrivning(kod.getBeskrivning())
                .typFallList(convertToTypFallList(typfallList, kod))
                .build())
            .toList();
    }

    private List<Referens> convertToReferensList(final Attributes attributes) {
        return attributes.getReferens().stream()
            .map(referens -> aReferens()
                .text(referens.getText())
                .uri(referens.getUri())
                .build())
            .toList();
    }
}
