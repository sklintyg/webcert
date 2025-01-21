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
package se.inera.intyg.webcert.web.service.utkast;

import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.LogUtil;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

/**
 * @author Magnus Ekstrand on 2019-08-27.
 */
@Service
public class UtkastCandidateServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastCandidateServiceImpl.class);

    @Autowired
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;


    public Optional<UtkastCandidateMetaData> getCandidateMetaData(
        ModuleApi moduleApi, String intygType, String intygTypeVersion, Patient patient, boolean isCoherentJournaling) {

        UtkastCandidateMetaData metaData = null;

        // Finns det några urvalskriterier?
        final Optional<GetCopyFromCriteria> copyFromCriteria = moduleApi.getCopyFromCriteria();
        if (!copyFromCriteria.isPresent()) {
            return Optional.empty();
        }

        // Kontrollera användarens rättigheter
        final var isAllowedToCopy = draftAccessServiceHelper.isAllowedToCopyFromCandidate(
            AccessEvaluationParameters.create(intygType, intygTypeVersion, null, patient.getPersonId(), false)
        );

        if (!isAllowedToCopy) {
            return Optional.empty();
        }

        // Är användaren vårdadministratör får denne inte hantera patienter
        // som har sekretessmarkering
        if (!getUser().isLakare() && patient.isSekretessmarkering()) {
            return Optional.empty();
        }

        try {
            // Lookup certificate candidate
            Optional<Utkast> candidate = findCandidate(copyFromCriteria.get(), patient);

            if (candidate.isPresent()) {
                Utkast utkast = candidate.get();

                // True if the enhet or vardenhet of the currently logged in user is same vardenhet or a subunit
                // of the vardenhet where the candidate certificate was issued.
                boolean sameVardenhet = webCertUserService.isUserAllowedAccessToUnit(utkast.getEnhetsId());

                metaData = new UtkastCandidateMetaData.Builder()
                    .with(builder -> {
                        builder.intygId = utkast.getIntygsId();
                        builder.intygType = utkast.getIntygsTyp();
                        builder.intygTypeVersion = utkast.getIntygTypeVersion();
                        builder.intygCreated = utkast.getSkapad();
                        builder.signedByHsaId = utkast.getSkapadAv().getHsaId();
                        builder.enhetHsaId = utkast.getEnhetsId();
                        builder.enhetName = utkast.getEnhetsNamn();
                        builder.sameVardenhet = sameVardenhet;
                    })
                    .create();

                // PDL Log read access to copyFromCandidate Utlatande
                logService.logReadIntyg(
                    logRequestFactory.createLogRequestFromUtkast(
                        utkast, isCoherentJournaling), LogUtil.getLogUser(getUser()));
            }

            return Optional.ofNullable(metaData);

        } catch (Exception e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                "Failed to lookup a candidate to copy certificate information from", e);
        }
    }

    /*
     * The requirements to select an candidate was implemented while working on JIRA-number INTYGFV-10834.
     */
    private Optional<Utkast> findCandidate(GetCopyFromCriteria copyFromCriteria, Patient patient) {
        if (copyFromCriteria == null) {
            return Optional.empty();
        }

        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.getIntygType());

        List<Utkast> candidates = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(
            patient.getPersonId().getPersonnummerWithDash(),
            validIntygType);

        switch (copyFromCriteria.getIntygType()) {
            case "lisjp":
                return filterLisjpCandidates(candidates, copyFromCriteria);
            case "db":
                return filterDbCandidates(candidates, copyFromCriteria);
            default:
                return Optional.empty();
        }
    }

    private Optional<Utkast> filterLisjpCandidates(List<Utkast> candidates, GetCopyFromCriteria copyFromCriteria) {

        // This is the candidate to present
        return candidates.stream()
            .filter(candidate -> candidate.getStatus() == UtkastStatus.SIGNED)
            .filter(candidate -> candidate.getAterkalladDatum() == null)
            .filter(candidate -> filterOnMajorVersion(candidate.getIntygTypeVersion(), copyFromCriteria.getIntygTypeMajorVersion()))
            .filter(candidate -> webCertUserService.isUserAllowedAccessToUnit(candidate.getEnhetsId()))
            .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum(), Comparator.reverseOrder()))
            .findFirst();
    }

    private Optional<Utkast> filterDbCandidates(List<Utkast> candidates, GetCopyFromCriteria copyFromCriteria) {

        return candidates.stream()
            .filter(candidate -> candidate.getStatus() == UtkastStatus.SIGNED)
            .filter(candidate -> candidate.getAterkalladDatum() == null)
            .filter(candidate -> filterDbOnReplaced(candidate, candidates))
            .filter(candidate -> filterOnMajorVersion(candidate.getIntygTypeVersion(), copyFromCriteria.getIntygTypeMajorVersion()))
            .filter(candidate -> candidate.getIntygsTyp().equals(copyFromCriteria.getIntygType()))
            .filter(candidate -> getUser().getValdVardgivare().getId().equals(candidate.getVardgivarId()))
            .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum(), Comparator.reverseOrder()))
            .findFirst();
    }

    private WebCertUser getUser() {
        return webCertUserService.getUser();
    }

    private boolean filterOnMajorVersion(String intygTypeVersion, String intygTypeMajorVersion) {
        return !Strings.isNullOrEmpty(intygTypeVersion) && !Strings.isNullOrEmpty(intygTypeMajorVersion)
            && intygTypeVersion.startsWith(intygTypeMajorVersion + ".");
    }

    private boolean filterDbOnReplaced(Utkast candidateForChecking, List<Utkast> candidates) {
        for (Utkast candidate : candidates) {
            if (candidateForChecking.getIntygsId().equals(candidate.getRelationIntygsId())
                && candidate.getRelationKod().equals(RelationKod.ERSATT)) {
                return false;
            }
        }
        return true;
    }
}
