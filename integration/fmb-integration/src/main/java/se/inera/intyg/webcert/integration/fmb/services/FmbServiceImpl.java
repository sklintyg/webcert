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
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// CHECKSTYLE:OFF LineLength
import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbCallType;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.BeslutsunderlagType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.DiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.HuvuddiagnosType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.ICD10SEType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.OvrigFmbInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionerType;
// CHECKSTYLE:ON LineLength

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
    private GetDiagnosInformationResponderInterface getDiagnosInformationResponder;

    @Autowired
    private GetFmbResponderInterface getFmbResponder;

    @Autowired
    private GetVersionsResponderInterface getVersionsResponder;

    @Autowired
    private FmbRepository fmbRepository;

    @Value("${fmb.logicaladdress}")
    private String logicalAddress;

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
        final FmbVersionStatus versionStatus = getVersionStatus();

        if (!versionStatus.isDiagnosInfoIsUpToDate()) {
            LOG.info("Updating diagnosInfo");
            final List<Fmb> updatedDiagnosinfos = getUpdatedDiagnosinfos();
            updateDbForCallType(FmbCallType.DIAGNOSINFORMATION, updatedDiagnosinfos);
        }

        if (!versionStatus.isFmbIsUpToDate()) {
            LOG.info("Updating fmbInfo");
            final List<Fmb> updatedFmbs = getUpdatedFmbs();
            updateDbForCallType(FmbCallType.FMB, updatedFmbs);
        }
    }

    private void updateDbForCallType(FmbCallType callType, List<Fmb> updatedFmbs) {
        if (!updatedFmbs.isEmpty()) {
            removeAll(callType);
            fmbRepository.save(updatedFmbs);
            LOG.info("Added {} rows for {}", updatedFmbs.size(), callType);
        }
    }

    private void removeAll(@Nullable FmbCallType callType) {
        if (callType == null) {
            return;
        }
        final List<Fmb> fmbs = fmbRepository.findByUrsprung(callType);
        LOG.info("Removing all '{}' rows from FMB of calltype '{}'", fmbs.size(), callType);
        fmbRepository.deleteInBatch(fmbs);
    }

    @Nonnull
    private FmbVersionStatus getVersionStatus() {
        GetVersionsResponseType versions = getVersionsResponder.getVersions(logicalAddress, new GetVersionsType());
        if (versions == null) {
            LOG.warn("Versions is null. FMB data will not be updated.");
            return new FmbVersionStatus(true, true);
        }

        final VersionerType versioner = versions.getVersioner();
        if (versioner == null) {
            LOG.warn("Versioner is null. FMB data will not be updated.");
            return new FmbVersionStatus(true, true);
        }

        final boolean diagnosInfoIsUpToDate = isDiagnosInfoUpToDate(versioner);
        final boolean fmbIsUpToDate = isFmbInfoUpToDate(versioner);
        return new FmbVersionStatus(fmbIsUpToDate, diagnosInfoIsUpToDate);
    }

    private boolean isFmbInfoUpToDate(@Nonnull VersionerType versioner) {
        final String fmbDate = versioner.getFmbSenateAndring();
        final List<Fmb> fmbs = fmbRepository.findByUrsprung(FmbCallType.FMB);
        final boolean fmbIsUpToDate = isUpToDate(fmbDate, fmbs);
        LOG.info("Latest FMB version is '{}'. 'The database is up to date with this version'='{}'", fmbDate, fmbIsUpToDate);
        return fmbIsUpToDate;
    }

    private boolean isDiagnosInfoUpToDate(@Nonnull VersionerType versioner) {
        final String diagnosInfoDate = versioner.getDiagnosInformationSenateAndring();
        final List<Fmb> diagnosInfos = fmbRepository.findByUrsprung(FmbCallType.DIAGNOSINFORMATION);
        final boolean diagnosInfoIsUpToDate = isUpToDate(diagnosInfoDate, diagnosInfos);
        LOG.info("Latest diagnosInfo version is '{}'. 'The database is up to date with this version'='{}'", diagnosInfoDate,
                diagnosInfoIsUpToDate);
        return diagnosInfoIsUpToDate;
    }

    private boolean isUpToDate(@Nullable String lastUpdate, @Nullable List<Fmb> fmbs) {
        if (fmbs == null || fmbs.isEmpty()) {
            return false;
        }
        if (lastUpdate == null) {
            return true;
        }
        for (Fmb fmb : fmbs) {
            if (fmb != null && !lastUpdate.equals(fmb.getLastUpdate())) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    private List<Fmb> getUpdatedDiagnosinfos() {
        final List<Fmb> fmbs = new ArrayList<>();
        final GetDiagnosInformationResponseType diagnosInformation = getDiagnosInformationResponder.getDiagnosInformation(logicalAddress,
                new GetDiagnosInformationType());
        if (diagnosInformation == null) {
            LOG.warn("Diagnosinformation is null");
            return fmbs;
        }
        final VersionType version = diagnosInformation.getVersion();
        final String senateAndring = version != null ? version.getSenateAndring() : UNKNOWN_TIMESTAMP;
        final List<DiagnosInformationType> diagnosInformations = diagnosInformation.getDiagnosInformation();
        if (diagnosInformations == null) {
            LOG.warn("Diagnosinformation does not contain any data");
            return fmbs;
        }
        for (DiagnosInformationType information : diagnosInformations) {
            if (information != null) {
                fmbs.addAll(createFmbsForDiagnosInfo(senateAndring, information));
            }
        }
        return fmbs;
    }

    private List<Fmb> createFmbsForDiagnosInfo(@Nonnull String senateAndring, @Nonnull DiagnosInformationType information) {
        final List<Fmb> fmbs = new ArrayList<>();
        final String aktivitetsbegransningBeskrivning = information.getAktivitetsbegransningBeskrivning();
        final String funktionsnedsattningBeskrivning = information.getFunktionsnedsattningBeskrivning();
        final OvrigFmbInformationType ovrigFmbInformation = information.getOvrigFmbInformation();
        final String symptomPrognosBehandling = ovrigFmbInformation != null ? ovrigFmbInformation.getSymtomPrognosBehandling() : null;
        final String generellInformation = ovrigFmbInformation != null ? ovrigFmbInformation.getGenrellInformation() : null;
        final List<HuvuddiagnosType> huvuddxs = information.getHuvuddiagnos();
        final List<String> formatedIcd10Codes = getFormatedIcd10Codes(huvuddxs);
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
    private List<Fmb> getUpdatedFmbs() {
        final List<Fmb> fmbs = new ArrayList<>();
        GetFmbResponseType fmb = getFmbResponder.getFmb(logicalAddress, new GetFmbType());
        if (fmb == null) {
            LOG.warn("FMB response is null");
            return fmbs;
        }
        final VersionType version = fmb.getVersion();
        final String senateAndring = version != null ? version.getSenateAndring() : UNKNOWN_TIMESTAMP;
        final List<BeslutsunderlagType> beslutsunderlags = fmb.getBeslutsunderlag();
        if (beslutsunderlags == null) {
            LOG.warn("FMB beslutsunderlag is null");
            return fmbs;
        }
        for (BeslutsunderlagType beslutsunderlag : beslutsunderlags) {
            if (beslutsunderlag != null) {
                fmbs.addAll(createFmbsForFmbInfo(senateAndring, beslutsunderlag));
            }
        }
        return fmbs;
    }

    private List<Fmb> createFmbsForFmbInfo(@Nonnull String senateAndring, @Nonnull BeslutsunderlagType beslutsunderlag) {
        final List<Fmb> fmbs = new ArrayList<>();
        final String falt8b = beslutsunderlag.getTextuelltUnderlag();
        if (falt8b != null) {
            final List<HuvuddiagnosType> huvuddxs = beslutsunderlag.getHuvuddiagnos();
            final List<String> formatedIcd10Codes = getFormatedIcd10Codes(huvuddxs);
            for (String code : formatedIcd10Codes) {
                fmbs.add(new Fmb(code, FmbType.BESLUTSUNDERLAG_TEXTUELLT, FmbCallType.FMB, falt8b, senateAndring));
            }
        }
        return fmbs;
    }

    @Nonnull
    private List<String> getFormatedIcd10Codes(@Nullable List<HuvuddiagnosType> huvuddxs) {
        final List<String> codes = new ArrayList<>();
        if (huvuddxs == null) {
            LOG.info("Missing huvuddiagnos");
            return codes;
        }
        for (HuvuddiagnosType huvuddx : huvuddxs) {
            final String code = getFormatedIcd10Code(huvuddx);
            if (code != null) {
                codes.add(code);
            }
        }
        return codes;
    }

    @Nullable
    private String getFormatedIcd10Code(@Nullable HuvuddiagnosType huvuddx) {
        if (huvuddx == null) {
            return null;
        }
        final ICD10SEType kod = huvuddx.getKod();

        if (kod == null) {
            return null;
        }
        final String codeRaw = kod.getCode();

        if (codeRaw == null) {
            return null;
        }
        return codeRaw.replaceAll("\\.", "").toUpperCase(Locale.ENGLISH);
    }

}
