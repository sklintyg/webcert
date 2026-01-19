package se.inera.intyg.webcert.web.service.facade.user;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceStatisticService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
public class CalculateQuestionsForUnitService {

    private final AuthoritiesHelper authoritiesHelper;
    private final ArendeService arendeService;
    private final CertificateServiceStatisticService certificateServiceStatisticService;

    public UserStatisticsDTO calculate(WebCertUser user, List<AbstractVardenhet> units) {
        final var unitIds = units.stream()
            .map(AbstractVardenhet::getId)
            .toList();

        final var statistics = new UserStatisticsDTO();
        final var certificateTypes = getCertificateTypesAllowedForUser(user);
        final var questionsMap = arendeService.getNbrOfUnhandledArendenForCareUnits(unitIds, certificateTypes);

        for (AbstractVardenhet unit : units) {
            final var subUnitIds = getSubUnits(unit);
            final var unitId = unit.getId();

            final var questionsOnSubUnits = sumStatisticsForUnits(subUnitIds, questionsMap);
            final var questionsOnUnit = getFromMap(unitId, questionsMap);
            statistics.addUnitStatistics(unitId, new UnitStatisticsDTO(0L, questionsOnUnit, 0L, questionsOnSubUnits));
        }

        certificateServiceStatisticService.add(statistics, unitIds, user, false);
        return statistics;
    }

    private List<String> getSubUnits(AbstractVardenhet unit) {
        if (unit instanceof Vardenhet careUnit) {
            return careUnit.getMottagningar().stream()
                .map(Mottagning::getHsaIds)
                .flatMap(List::stream)
                .toList();
        }

        return Collections.emptyList();
    }

    private long sumStatisticsForUnits(Iterable<String> unitIds, Map<String, Long> statistics) {
        long sum = 0;
        for (String unitId : unitIds) {
            sum += getFromMap(unitId, statistics);
        }
        return sum;
    }

    private long getFromMap(String id, Map<String, Long> statistics) {
        return statistics.getOrDefault(id, 0L);
    }

    private Set<String> getCertificateTypesAllowedForUser(WebCertUser user) {
        return authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
    }
}