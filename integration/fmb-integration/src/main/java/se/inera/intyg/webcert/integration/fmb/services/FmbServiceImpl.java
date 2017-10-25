/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.integration.fmb.consumer.FailedToFetchFmbData;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumer;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Aktivitetsbegransning;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxData;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Funktionsnedsattning;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Markup;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Fmbtillstand;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;
import se.inera.intyg.webcert.integration.fmb.model.typfall.TypfallData;
import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbCallType;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Service
@Transactional("jpaTransactionManager")
@Configuration
@EnableScheduling
@Profile({ "dev", "test", "webcertMainNode" })
public class FmbServiceImpl implements FmbService {

    private static final Logger LOG = LoggerFactory.getLogger(FmbServiceImpl.class);

    public static final String UNKNOWN_TIMESTAMP = "UnknownTimestamp";

    @Autowired
    private FmbConsumer fmbConsumer;

    @Autowired
    private FmbRepository fmbRepository;

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
        LOG.info("Updating FMB information");
        try {
            final FmdxInformation fmdxInfo = fmbConsumer.getForsakringsmedicinskDiagnosinformation();
            final Typfall typfall = fmbConsumer.getTypfall();
            final List<Fmb> updatedDiagnosinfos = getUpdatedDiagnosinfos(fmdxInfo, typfall);
            if (updatedDiagnosinfos.isEmpty()) {
                LOG.warn("Updated diagnos infos is empty. No FMB update will be performed.");
            } else {
                updateFmbDb(updatedDiagnosinfos);
            }
        } catch (FailedToFetchFmbData failedToFetchFmbData) {
            LOG.error("Failed to update FMB information", failedToFetchFmbData);
        }
    }

    private void updateFmbDb(List<Fmb> updatedFmbs) {
        if (!updatedFmbs.isEmpty()) {
            fmbRepository.deleteAllInBatch();
            fmbRepository.save(updatedFmbs);
            LOG.info("Inserted {} rows for FMB", updatedFmbs.size());
        }
    }

    @Nonnull
    private List<Fmb> getUpdatedDiagnosinfos(FmdxInformation fmdxInfo, Typfall typfall) {
        final List<Fmb> fmbs = new ArrayList<>();

        if (fmdxInfo == null) {
            LOG.warn("Diagnosinformation is null");
            return fmbs;
        }
        if (typfall == null) {
            LOG.warn("Typfall info is null");
            return fmbs;
        }

        final String senateAndring = fmdxInfo.getMeta() != null ? fmdxInfo.getMeta().getBuildtimestamp() : UNKNOWN_TIMESTAMP;
        final List<FmdxData> datas = fmdxInfo.getData();
        if (datas == null) {
            LOG.warn("Fmdx datas is null");
            return fmbs;
        }

        for (FmdxData data : datas) {
            final Attributes attributes = data.getAttributes();
            if (attributes != null) {
                final Aktivitetsbegransning ab = attributes.getAktivitetsbegransning();
                final String aktivitetsbegransning = ab != null ? ab.getAktivitetsbegransningsbeskrivning() : null;
                final Funktionsnedsattning fn = attributes.getFunktionsnedsattning();
                final String funktionsnedsattning = fn != null ? fn.getFunktionsnedsattningsbeskrivning() : null;
                final Markup spb = attributes.getSymtomprognosbehandling();
                final String symtomprognosbehandling = spb != null ? spb.getMarkup() : null;
                final Markup fmi = attributes.getForsakringsmedicinskinformation();
                final String beskrivning = fmi != null ? fmi.getMarkup() : null;
                final List<Kod> diagnoskod = attributes.getDiagnoskod();
                final List<String> formatedIcd10Codes = getFormatedIcd10Codes(diagnoskod);
                fmbs.addAll(getFmbs(senateAndring, aktivitetsbegransning, funktionsnedsattning,
                        symtomprognosbehandling, beskrivning, formatedIcd10Codes));
                fmbs.addAll(getTypfallForDx(typfall, formatedIcd10Codes, senateAndring));
            }
        }
        return fmbs;
    }

    private List<Fmb> getTypfallForDx(Typfall typfall, List<String> diagnoskods, String senateAndring) {
        final List<Fmb> fmbs = new ArrayList<>();
        final List<TypfallData> datas = typfall.getData();
        if (datas == null || datas.isEmpty()) {
            LOG.info("Typfall datas is null");
            return fmbs;
        }
        for (TypfallData data : datas) {
            final se.inera.intyg.webcert.integration.fmb.model.typfall.Attributes attributes = data.getAttributes();
            if (attributes != null) {
                final String typfallsmening = attributes.getTypfallsmening();
                final Fmbtillstand fmbtillstand = attributes.getFmbtillstand();
                final List<Kod> dxs = fmbtillstand != null ? fmbtillstand.getDiagnoskod() : Collections.emptyList();
                for (Kod dx : dxs) {
                    final String kod = dx.getKod();
                    if (diagnoskods.contains(kod)) {
                        fmbs.add(new Fmb(kod, FmbType.BESLUTSUNDERLAG_TEXTUELLT, FmbCallType.FMB, typfallsmening, senateAndring));
                    }
                }
            }
        }
        return fmbs;
    }

    private List<Fmb> getFmbs(@Nonnull String senateAndring, String aktivitetsbegransningBeskrivning,
                              String funktionsnedsattningBeskrivning, String symptomPrognosBehandling,
                              String generellInformation, List<String> formatedIcd10Codes) {
        final List<Fmb> fmbs = new ArrayList<>();
        for (String code : formatedIcd10Codes) {
            if (symptomPrognosBehandling != null) {
                fmbs.add(new Fmb(code, FmbType.SYMPTOM_PROGNOS_BEHANDLING, FmbCallType.DIAGNOSINFORMATION, symptomPrognosBehandling,
                        senateAndring));
            }
            if (generellInformation != null) {
                fmbs.add(new Fmb(code, FmbType.GENERELL_INFO, FmbCallType.DIAGNOSINFORMATION, generellInformation, senateAndring));
            }
            if (funktionsnedsattningBeskrivning != null) {
                fmbs.add(new Fmb(code, FmbType.FUNKTIONSNEDSATTNING, FmbCallType.DIAGNOSINFORMATION, funktionsnedsattningBeskrivning,
                        senateAndring));
            }
            if (aktivitetsbegransningBeskrivning != null) {
                fmbs.add(new Fmb(code, FmbType.AKTIVITETSBEGRANSNING, FmbCallType.DIAGNOSINFORMATION, aktivitetsbegransningBeskrivning,
                        senateAndring));
            }
        }
        return fmbs;
    }

    @Nonnull
    private List<String> getFormatedIcd10Codes(@Nullable List<Kod> huvuddxs) {
        final List<String> codes = new ArrayList<>();
        if (huvuddxs == null) {
            LOG.info("Missing huvuddiagnos");
            return codes;
        }
        for (Kod huvuddx : huvuddxs) {
            final String code = getFormatedIcd10Code(huvuddx);
            if (code != null) {
                codes.add(code);
            }
        }
        return codes;
    }

    @Nullable
    private String getFormatedIcd10Code(@Nullable Kod huvuddx) {
        if (huvuddx == null) {
            return null;
        }
        final String kod = huvuddx.getKod();

        if (kod == null) {
            return null;
        }
        return kod.replaceAll("\\.", "").toUpperCase(Locale.ENGLISH);
    }

}
