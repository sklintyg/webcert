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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.GetCopyFromCriteria;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

/**
 * @author Magnus Ekstrand on 2019-08-27.
 */
@Service
public class UtkastCandidateServiceImpl {

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

        try {
            // Lookup certificate candidate
            Optional<Utkast> candidate = findCandidate(moduleApi, user, patient.getPersonId());

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
                logService.logReadIntyg(logRequestFactory.createLogRequestFromUtkast(utkast, isCoherentJournaling), createLogUser(user));
            }

            return Optional.ofNullable(metaData);

        } catch (Exception e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                "Failed to lookup a candidate to copy certificate information from", e);
        }

    }

    private Optional<Utkast> findCandidate(ModuleApi moduleApi, IntygUser user, Personnummer personnummer) {
        final Optional<GetCopyFromCriteria> copyFromCriteria = moduleApi.getCopyFromCriteria();
        if (!copyFromCriteria.isPresent()) {
            return Optional.empty();
        }

        Set<String> validIntygType = new HashSet<>();
        validIntygType.add(copyFromCriteria.get().getIntygType());

        List<Utkast> toFilter = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(),
            validIntygType);

        LocalDateTime earliestValidDate = LocalDateTime.now().minusDays(copyFromCriteria.get().getMaxAgeDays());

        // This is the candidate to present
        final Optional<Utkast> candidate = toFilter.stream()
            .filter(utkast -> utkast.getStatus() == UtkastStatus.SIGNED)
            .filter(utkast -> utkast.getEnhetsId().equals(user.getValdVardenhet().getId()))
            .filter(utkast -> utkast.getAterkalladDatum() == null)
            .filter(utkast -> sameMajorVersion(utkast.getIntygTypeVersion(), copyFromCriteria.get().getIntygTypeMajorVersion()))
            .filter(utkast -> utkast.getSignatur().getSigneringsDatum().isAfter(earliestValidDate))
            .filter(utkast -> utkast.getSignatur().getSigneradAv().equals(user.getHsaId()))
            .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum(), Comparator.reverseOrder()))
            .findFirst();

        return candidate;
    }

    private LogUser createLogUser(IntygUser intygUser) {
        SelectableVardenhet valdVardenhet = intygUser.getValdVardenhet();
        SelectableVardenhet valdVardgivare = intygUser.getValdVardgivare();

        return new LogUser.Builder(intygUser.getHsaId(), valdVardenhet.getId(), valdVardgivare.getId())
            .userName(intygUser.getNamn())
            .userAssignment(intygUser.getSelectedMedarbetarUppdragNamn())
            .userTitle(intygUser.getTitel())
            .enhetsNamn(valdVardenhet.getNamn())
            .vardgivareNamn(valdVardgivare.getNamn())
            .build();
    }

    private boolean sameMajorVersion(String intygTypeVersion, String intygTypeMajorVersion) {
        return !Strings.isNullOrEmpty(intygTypeVersion) && !Strings.isNullOrEmpty(intygTypeMajorVersion)
            && intygTypeVersion.startsWith(intygTypeMajorVersion + ".");
    }


}
