/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
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
    private DraftAccessService draftAccessService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;


    public Optional<UtkastCandidateMetaData> getCandidateMetaData(ModuleApi moduleApi, Patient patient, boolean isCoherentJournaling) {
        UtkastCandidateMetaData metaData = null;
        WebCertUser user = webCertUserService.getUser();

        // Finns det några urvalskriterier?
        final Optional<GetCopyFromCriteria> copyFromCriteria = moduleApi.getCopyFromCriteria();
        if (!copyFromCriteria.isPresent()) {
            return Optional.empty();
        }

        // Kontrollera användarens rättigheter
        AccessResult accessResult =
            draftAccessService.allowToCopyFromCandidate(copyFromCriteria.get().getIntygType(), patient.getPersonId());
        if (accessResult.isDenied()) {
            return Optional.empty();
        }

        try {
            // Lookup certificate candidate
            Optional<Utkast> candidate = findCandidate(copyFromCriteria.get(), user, patient.getPersonId());

            if (candidate.isPresent()) {
                Utkast utkast = candidate.get();
                metaData = new UtkastCandidateMetaData.Builder()
                    .with(builder -> {
                        builder.intygId = utkast.getIntygsId();
                        builder.intygType = utkast.getIntygsTyp();
                        builder.intygTypeVersion = utkast.getIntygTypeVersion();
                        builder.intygCreated = utkast.getSkapad();
                        builder.signedByHsaId = utkast.getSkapadAv().getHsaId();
                        builder.enhetHsaId = utkast.getEnhetsId();
                    })
                    .create();

                // PDL Log read access to copyFromCandidate Utlatande
                logService.logReadIntyg(
                    logRequestFactory.createLogRequestFromUtkast(
                        utkast, isCoherentJournaling), LogUtil.getLogUser(user));
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
    private Optional<Utkast> findCandidate(GetCopyFromCriteria copyFromCriteria, IntygUser user, Personnummer personnummer) {
        if (copyFromCriteria == null) {
            return Optional.empty();
        }

        // Är patienten i fråga sekretessmarkerad?
        // Endast läkare och tandläkare har rätt att söka efter intyg i så fall.
        /*
        try {
            if (isSekretessmarkerad(personnummer)) {
                if (!hasRole(AuthoritiesConstants.ROLE_LAKARE, user) && !hasRole(AuthoritiesConstants.ROLE_TANDLAKARE, user)) {
                    return Optional.empty();
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            return Optional.empty();
        }
        */

        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.getIntygType());

        List<Utkast> candidates = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(),
            validIntygType);

        LocalDateTime earliestValidDate = LocalDateTime.now().minusDays(copyFromCriteria.getMaxAgeDays());

        // This is the candidate to present
        return candidates.stream()
            .filter(candidate -> candidate.getStatus() == UtkastStatus.SIGNED)
            .filter(candidate -> candidate.getAterkalladDatum() == null)
            .filter(candidate -> candidate.getSignatur().getSigneringsDatum().isAfter(earliestValidDate))
            .filter(candidate -> filterOnMajorVersion(candidate.getIntygTypeVersion(), copyFromCriteria.getIntygTypeMajorVersion()))
            .filter(candidate -> filterOnUnit(candidate, user))
            .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum(), Comparator.reverseOrder()))
            .findFirst();
    }

    private boolean filterOnMajorVersion(String intygTypeVersion, String intygTypeMajorVersion) {
        return !Strings.isNullOrEmpty(intygTypeVersion) && !Strings.isNullOrEmpty(intygTypeMajorVersion)
            && intygTypeVersion.startsWith(intygTypeMajorVersion + ".");
    }

    private boolean filterOnUnit(Utkast candidate, IntygUser user) {
        if (hasRole(AuthoritiesConstants.ROLE_LAKARE, user) || hasRole(AuthoritiesConstants.ROLE_TANDLAKARE, user)) {
            // För läkare och tandläkare så räcker det med att vara inloggad på enhet eller någon av dess underenheter
            return webCertUserService.isUserLoggedInOnEnhetOrUnderenhet(candidate.getEnhetsId());
        } else if (hasRole(AuthoritiesConstants.ROLE_ADMIN, user)) {
            // En våradministratör måste vara inloaggad på samma enhet som intyget
            return candidate.getEnhetsId().equals(user.getValdVardenhet().getId());
        }
        return false;
    }

    private boolean hasRole(String role, IntygUser user) {
        if (user.getRoles() == null || user.getRoles().size() == 0) {
            return false;
        }
        return user.getRoles().containsKey(role);
    }

}
