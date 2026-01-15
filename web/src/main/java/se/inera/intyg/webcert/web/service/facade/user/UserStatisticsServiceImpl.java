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
package se.inera.intyg.webcert.web.service.facade.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceStatisticService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
@Slf4j
public class UserStatisticsServiceImpl implements UserStatisticsService {

    @Value("${max.number.of.commissions.for.statistics:15}")
    private Integer maxCommissionsForStatistics;

    private static final Logger LOG = LoggerFactory.getLogger(UserStatisticsServiceImpl.class);

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;
    private final AuthoritiesHelper authoritiesHelper;
    private final ArendeService arendeService;
    private final CertificateServiceStatisticService certificateServiceStatisticService;

    @Autowired
    public UserStatisticsServiceImpl(WebCertUserService webCertUserService, UtkastService utkastService,
        AuthoritiesHelper authoritiesHelper, ArendeService arendeService,
        CertificateServiceStatisticService certificateServiceStatisticService) {
        this.webCertUserService = webCertUserService;
        this.utkastService = utkastService;
        this.authoritiesHelper = authoritiesHelper;
        this.arendeService = arendeService;
        this.certificateServiceStatisticService = certificateServiceStatisticService;
    }

    @Override
    public UserStatisticsDTO getUserStatistics() {
        final var user = webCertUserService.getUser();

        if (!validateUser(user)) {
            return null;
        }

        final var careUnitIds = getCareUnitIds(user);

        if (careUnitIds.isEmpty()) {
            return null;
        }

        final var maxCommissionsExceeded = careUnitIds.size() > maxCommissionsForStatistics;

        if (maxCommissionsExceeded && user.getValdVardenhet() == null) {
            log.info("Number of commissions ({}) exceeds maxCommissionsForStatistics ({}) without selected unit. No statistics will "
                + "be collected.", careUnitIds.size(), maxCommissionsForStatistics);
            return null;
        }

        final var unitIds = maxCommissionsExceeded ? user.getValdVardenhet().getHsaIds() : getUnitIds(user);

        if (maxCommissionsExceeded) {
            log.info("Number of commissions ({}) exceeds maxCommissionsForStatistics ({}) with selected unit. Statistics will be collected "
                + "for selected care unit only.", careUnitIds.size(), maxCommissionsForStatistics);
        }

        final var statistics = new UserStatisticsDTO();
        final var certificateTypes = getCertificateTypesAllowedForUser(user);
        final var questionsMap = arendeService.getNbrOfUnhandledArendenForCareUnits(unitIds, certificateTypes);
        final var draftsMap = utkastService.getNbrOfUnsignedDraftsByCareUnits(unitIds);

        if (user.getValdVardenhet() != null) {
            statistics.setNbrOfDraftsOnSelectedUnit(
                getNumberOfDraftsOnSelectedUnit(user, draftsMap)
            );
            statistics.setNbrOfUnhandledQuestionsOnSelectedUnit(
                getNumberOfUnhandledQuestionsOnSelectedUnit(user.getIdsOfSelectedVardenhet(), questionsMap)
            );
            statistics.setTotalDraftsAndUnhandledQuestionsOnOtherUnits(
                getTotalDraftsAndUnhandledQuestionsOnOtherUnits(unitIds, user, draftsMap, questionsMap)
            );
        }

        if (!maxCommissionsExceeded) {
            addCareProviderStatistics(statistics, user.getVardgivare(), draftsMap, questionsMap);
        }

        certificateServiceStatisticService.add(statistics, unitIds, user, maxCommissionsExceeded);
        return statistics;
    }

    private void addCareProviderStatistics(UserStatisticsDTO statistics, List<Vardgivare> careProviders, Map<String, Long> draftMap,
        Map<String, Long> questionMap) {
        for (Vardgivare careProvider : careProviders) {
            for (Vardenhet unit : careProvider.getVardenheter()) {
                final var subUnitIds = unit.getHsaIds();
                subUnitIds.remove(unit.getId());
                addUnitStatistics(statistics, unit.getId(), subUnitIds, draftMap, questionMap, careProvider.getId());
                addSubUnitsStatistics(statistics, subUnitIds, draftMap, questionMap, careProvider.getId(), unit.getId());
            }
        }
    }

    private void addUnitStatistics(UserStatisticsDTO statistics, String unitId, List<String> subUnitIds, Map<String, Long> draftMap,
        Map<String, Long> questionMap, String careProviderId) {
        if (unitId == null) {
            LOG.warn("Care provider with id '{}' includes care unit without id. Statistics will not be included for unit.", careProviderId);
            return;
        }
        final var draftsOnSubUnits = sumStatisticsForUnits(subUnitIds, draftMap);
        final var questionsOnSubUnits = sumStatisticsForUnits(subUnitIds, questionMap);
        final var draftsOnUnit = getFromMap(unitId, draftMap);
        final var questionsOnUnit = getFromMap(unitId, questionMap);
        statistics.addUnitStatistics(unitId, new UnitStatisticsDTO(draftsOnUnit, questionsOnUnit, draftsOnSubUnits, questionsOnSubUnits));
    }

    private void addSubUnitsStatistics(UserStatisticsDTO statistics, List<String> unitIds, Map<String, Long> draftMap,
        Map<String, Long> questionMap, String careProviderId, String careUnitId) {
        for (String unitId : unitIds) {
            if (unitId == null) {
                LOG.warn(
                    "Care provider with id '{}' & care unit with id '{}' includes sub unit without id. Statistics will not be included for"
                        + " sub unit.", careProviderId, careUnitId);
                continue;
            }
            final var nbrOfDrafts = getFromMap(unitId, draftMap);
            final var nbrOfQuestions = getFromMap(unitId, questionMap);
            statistics.addUnitStatistics(unitId, new UnitStatisticsDTO(nbrOfDrafts, nbrOfQuestions));
        }
    }

    private Set<String> getCertificateTypesAllowedForUser(WebCertUser user) {
        return authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
    }

    private List<String> getNotSelectedUnitIds(WebCertUser user, List<String> unitIds) {
        final var selectedUnitIds = user.getIdsOfSelectedVardenhet();
        final var notSelectedUnitIds = new ArrayList<>(unitIds);
        notSelectedUnitIds.removeAll(selectedUnitIds);
        return notSelectedUnitIds;
    }

    private long getTotalDraftsAndUnhandledQuestionsOnOtherUnits(List<String> unitIds, WebCertUser user, Map<String, Long> draftStats,
        Map<String, Long> questionsStats) {
        final var notSelectedUnitIds = getNotSelectedUnitIds(user, unitIds);
        return sumStatisticsForUnits(notSelectedUnitIds, draftStats) + sumStatisticsForUnits(notSelectedUnitIds, questionsStats);
    }

    private long getNumberOfDraftsOnSelectedUnit(WebCertUser user, Map<String, Long> draftsMap) {
        return getFromMap(user.getValdVardenhet().getId(), draftsMap);
    }

    private long getNumberOfUnhandledQuestionsOnSelectedUnit(List<String> unitIds, Map<String, Long> statisticsMap) {
        return sumStatisticsForUnits(unitIds, statisticsMap);
    }

    private long sumStatisticsForUnits(List<String> unitIds, Map<String, Long> statistics) {
        long sum = 0;
        for (String unitId : unitIds) {
            sum += getFromMap(unitId, statistics);
        }
        return sum;
    }

    private boolean validateUser(WebCertUser user) {
        if (user == null) {
            LOG.warn("getStatistics was called, but webcertUser was null!");
            return false;
        } else if (UserOriginType.DJUPINTEGRATION.name().equals(user.getOrigin())) {
            LOG.debug("getStatistics was called, but webcertUser origin is DJUPINTEGRATION - returning empty answer");
            return false;
        } else if (user.isUnauthorizedPrivatePractitioner()) {
            LOG.debug("getStatistics was called, but webcertUser is unauthorized private practitioner - returning empty answer");
            return false;
        }
        return true;
    }

    private List<String> getUnitIds(WebCertUser user) {
        final var units = user.getIdsOfAllVardenheter();
        if (units == null || units.isEmpty()) {
            LOG.warn("getStatistics was called by user {} that have no id:s of vardenheter present in the user context: {}",
                user.getHsaId(), user.getAsJson());
            return null;
        }
        return units;
    }

    private List<String> getCareUnitIds(WebCertUser user) {
        List<String> allIds = new ArrayList<>();
        for (Vardgivare v : user.getVardgivare()) {
            for (Vardenhet ve : v.getVardenheter()) {
                allIds.add(ve.getId());
            }
        }
        return allIds;
    }

    private long getFromMap(String id, Map<String, Long> statsMap) {
        final var value = statsMap.get(id);
        return value != null ? value : 0L;
    }

}