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
package se.inera.intyg.webcert.integration.fmb.services;

import static se.inera.intyg.webcert.persistence.fmb.model.icf.Beskrivning.BeskrivningBuilder.aBeskrivning;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation.DiagnosInformationBuilder.aDiagnosInformation;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.Icd10Kod.Icd10KodBuilder.anIcd10Kod;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.IcfKod.IcfKodBuilder.anIcfKod;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.Referens.ReferensBuilder.aReferens;
import static se.inera.intyg.webcert.persistence.fmb.model.icf.TypFall.TypFallBuilder.aTypFall;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumer;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.Meta;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxData;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmxBeskrivning;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Markup;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Fmbtillstand;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Rekommenderadsjukskrivning;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;
import se.inera.intyg.webcert.integration.fmb.model.typfall.TypfallData;
import se.inera.intyg.webcert.persistence.fmb.model.icf.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.icf.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.icf.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.icf.IcfKod;
import se.inera.intyg.webcert.persistence.fmb.model.icf.IcfKodTyp;
import se.inera.intyg.webcert.persistence.fmb.model.icf.Referens;
import se.inera.intyg.webcert.persistence.fmb.model.icf.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;

@Service
@Transactional("jpaTransactionManager")
@Configuration
@EnableScheduling
@Profile({"dev", "test", "webcertMainNode"})
public class FmbServiceImpl implements FmbService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FmbConsumer fmbConsumer;
    private final DiagnosInformationRepository repository;

    public FmbServiceImpl(final FmbConsumer fmbConsumer, final DiagnosInformationRepository repository) {
        this.fmbConsumer = fmbConsumer;
        this.repository = repository;
    }

    @Override
    @Scheduled(cron = "${fmb.dataupdate.cron}")
    public void updateData() {
        try {
            LOG.info("FMB data update started");
            performUpdate();
            LOG.info("FMB data update done");
        } catch (Exception e) {
            LOG.error("Failed to update FMB", e);
        }
    }

    private void performUpdate() {
        final Try<Void> result = Try.run(() -> {
            final FmdxInformation diagnosinformation = fmbConsumer.getForsakringsmedicinskDiagnosinformation();
            final Typfall typfall = fmbConsumer.getTypfall();
            final List<DiagnosInformation> diagnosInformationList = convertResponseToDiagnosInformation(diagnosinformation, typfall);

            if (!CollectionUtils.isEmpty(diagnosInformationList)) {
                repository.deleteAll();
                repository.save(diagnosInformationList);
            }
        });

        if (result.isFailure()) {
            throw new RuntimeException(MessageFormat.format("Failed to fetch FMB information: {0}", result.getCause()));
        }
    }

    private List<DiagnosInformation> convertResponseToDiagnosInformation(final FmdxInformation diagnosinformation, final Typfall typfall) {
        validateResponse(diagnosinformation, typfall);

        final Optional<LocalDateTime> senasteAndring = diagnosinformation.getMeta()
                .map(Meta::getBuildtimestamp)
                .map(timeStampString -> OffsetDateTime.parse(timeStampString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")))
                .map(OffsetDateTime::toLocalDateTime);

        return diagnosinformation.getData().stream()
                .map(FmdxData::getAttributes)
                .map(attributes -> {
                    List<Beskrivning> beskrivningList = Lists.newArrayList();
                    attributes.getAktivitetsbegransning().ifPresent(begransning -> beskrivningList.add(convertToBeskrivning(begransning, BeskrivningTyp.AKTIVITETSBEGRANSNING)));
                    attributes.getFunktionsnedsattning().ifPresent(begransning -> beskrivningList.add(convertToBeskrivning(begransning, BeskrivningTyp.FUNKTIONSNEDSATTNING)));

                    final List<Icd10Kod> icd10KodList = convertToIcd10KodList(attributes, typfall);

                    return aDiagnosInformation()
                            .forsakringsmedicinskInformation(attributes.getForsakringsmedicinskinformation().map(Markup::getMarkup).orElse(null))
                            .symptomPrognosBehandling(attributes.getSymtomprognosbehandling().map(Markup::getMarkup).orElse(null))
                            .beskrivningList(beskrivningList)
                            .icd10KodList(icd10KodList)
                            .referensList(convertToReferensList(attributes))
                            .senastUppdaterad(senasteAndring.orElse(null))
                            .build();
                }).collect(Collectors.toList());
    }

    private void validateResponse(final FmdxInformation diagnosinformation, final Typfall typfall) {
        Preconditions.checkArgument(Objects.nonNull(diagnosinformation));
        Preconditions.checkArgument(Objects.nonNull(diagnosinformation.getData()));
        Preconditions.checkArgument(Objects.nonNull(typfall));
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
                        .kod(kod.getKod())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TypFall> convertToTypFallList(final Typfall typfall, final Kod kod) {
        return typfall.getData().stream()
                .map(TypfallData::getAttributes)
                .filter(filterTypfall(kod))
                .map(attributes -> aTypFall()
                        .typfallsMening(attributes.getTypfallsmening())
                        .maximalSjukrivningstid(attributes.getRekommenderadsjukskrivning()
                                .map(Rekommenderadsjukskrivning::getMaximalsjukskrivningstid)
                                .map(Ints::tryParse)
                                .orElse(0))
                        .build())
                .collect(Collectors.toList());
    }

    private Predicate<se.inera.intyg.webcert.integration.fmb.model.typfall.Attributes> filterTypfall(final Kod kod) {
        return typFall -> typFall.getFmbtillstand().map(Fmbtillstand::getDiagnoskod).orElse(Collections.emptyList()).contains(kod);
    }

    private List<Icd10Kod> convertToIcd10KodList(final Attributes attributes, final Typfall typfallList) {

        return attributes.getDiagnoskod().stream()
                .map(kod -> anIcd10Kod()
                        .kod(kod.getKod())
                        .beskrivning(kod.getBeskrivning())
                        .typFallList(convertToTypFallList(typfallList, kod))
                        .build())
                .collect(Collectors.toList());
    }

    private List<Referens> convertToReferensList(final Attributes attributes) {
        return attributes.getReferens().stream()
                .map(referens -> aReferens()
                        .text(referens.getText())
                        .uri(referens.getUri())
                        .build())
                .collect(Collectors.toList());
    }
}
