package se.inera.intyg.webcert.integration.fmb.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Service
@Transactional("jpaTransactionManager")
@Configuration
@EnableScheduling
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
        LOG.info("Latest diagnosInfo version is '{}'. 'The database is up to date with this version'='{}'", diagnosInfoDate, diagnosInfoIsUpToDate);
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
        final GetDiagnosInformationResponseType diagnosInformation = getDiagnosInformationResponder.getDiagnosInformation(logicalAddress, new GetDiagnosInformationType());
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
        final String falt5 = information.getAktivitetsbegransningBeskrivning();
        final String falt4 = information.getFunktionsnedsattningBeskrivning();
        final OvrigFmbInformationType ovrigFmbInformation = information.getOvrigFmbInformation();
        final String falt2Spb = ovrigFmbInformation != null ? ovrigFmbInformation.getSymtomPrognosBehandling() : null;
        final String falt2General = ovrigFmbInformation != null ? ovrigFmbInformation.getGenrellInformation() : null;
        final List<HuvuddiagnosType> huvuddxs = information.getHuvuddiagnos();
        final List<String> formatedIcd10Codes = getFormatedIcd10Codes(huvuddxs);
        for (String code : formatedIcd10Codes) {
            if (falt2Spb != null) {
                fmbs.add(new Fmb(code, FmbType.FALT2_SPB, FmbCallType.DIAGNOSINFORMATION, falt2Spb, senateAndring));
            }
            if (falt2General != null) {
                fmbs.add(new Fmb(code, FmbType.FALT2_GENERAL, FmbCallType.DIAGNOSINFORMATION, falt2General, senateAndring));
            }
            if (falt4 != null) {
                fmbs.add(new Fmb(code, FmbType.FALT4, FmbCallType.DIAGNOSINFORMATION, falt4, senateAndring));
            }
            if (falt5 != null) {
                fmbs.add(new Fmb(code, FmbType.FALT5, FmbCallType.DIAGNOSINFORMATION, falt5, senateAndring));
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
                fmbs.add(new Fmb(code, FmbType.FALT8B, FmbCallType.FMB, falt8b, senateAndring));
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
