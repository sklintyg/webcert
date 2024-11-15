/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import com.google.common.base.Joiner;
import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsHelper;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.StatsResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.VardenhetStats;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.VardgivareStats;

/**
 * @author marced
 */
@Path("/stat")
@Api(value = "stat", produces = MediaType.APPLICATION_JSON)
public class StatModuleApiController extends AbstractApiController {

    private static final String SEPARATOR = " - ";

    private static final Logger LOG = LoggerFactory.getLogger(StatModuleApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private UtkastService intygDraftService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "stat-module-get-statistics", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getStatistics() {

        StatsResponse statsResponse = new StatsResponse();

        WebCertUser user = getWebCertUserService().getUser();
        if (user == null) {
            LOG.warn("getStatistics was called, but webcertUser was null!");
            return Response.ok(statsResponse).build();
        } else if (UserOriginType.DJUPINTEGRATION.name().equals(user.getOrigin())) {
            LOG.debug("getStatistics was called, but webcertUser origin is DJUPINTEGRATION - returning empty answer");
            return Response.ok(statsResponse).build();
        }

        List<String> allUnitIds = user.getIdsOfAllVardenheter();
        if (allUnitIds == null || allUnitIds.isEmpty()) {
            LOG.warn("getStatistics was called by user {} that have no id:s of vardenheter present in the user context: {}",
                user.getHsaId(), user.getAsJson());
            return Response.ok(statsResponse).build();
        }

        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
        Map<String, Long> fragaSvarStatsMap = fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(allUnitIds, intygsTyper);
        Map<String, Long> arendeStatsMap = arendeService.getNbrOfUnhandledArendenForCareUnits(allUnitIds, intygsTyper);

        Map<String, Long> mergedMap = StatisticsHelper.mergeArendeAndFragaSvarMaps(fragaSvarStatsMap, arendeStatsMap);
        Map<String, Long> intygStatsMap = intygDraftService.getNbrOfUnsignedDraftsByCareUnits(allUnitIds);

        List<String> unitIdsOfSelected = user.getIdsOfSelectedVardenhet();

        List<String> unitIdsOfNotSelected = new ArrayList<>(allUnitIds);
        unitIdsOfNotSelected.removeAll(unitIdsOfSelected);

        long fragaSvarOnOtherUnitThanTheSelected = calcSumFromSelectedUnits(unitIdsOfNotSelected, mergedMap);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnOtherThanSelected(fragaSvarOnOtherUnitThanTheSelected);

        long fragaSvarOnSelected = calcSumFromSelectedUnits(unitIdsOfSelected, mergedMap);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnSelected(fragaSvarOnSelected);

        long unsignedDraftsOnOtherThanSelected = calcSumFromSelectedUnits(unitIdsOfNotSelected, intygStatsMap);
        statsResponse.setTotalNbrOfUnsignedDraftsOnOtherThanSelected(unsignedDraftsOnOtherThanSelected);

        long unsignedDraftsOnSelected = getSafeStatValueFromMap(user.getValdVardenhet().getId(), intygStatsMap);
        statsResponse.setTotalNbrOfUnsignedDraftsOnSelected(unsignedDraftsOnSelected);

        populateStatsResponseWithVardgivarStats(statsResponse, user.getVardgivare(), intygStatsMap, mergedMap);
        return Response.ok(statsResponse).build();
    }

    private long calcSumFromSelectedUnits(List<String> unitIdsList, Map<String, Long> statsMap) {

        long sum = 0;
        for (String unitId : unitIdsList) {
            sum += getSafeStatValueFromMap(unitId, statsMap);
        }

        return sum;
    }

    private void populateStatsResponseWithVardgivarStats(StatsResponse statsResponse, List<Vardgivare> vardgivare,
        Map<String, Long> intygStats, Map<String, Long> fragaSvarStats) {

        VardgivareStats vgStats;

        for (Vardgivare vg : vardgivare) {
            vgStats = new VardgivareStats(vg.getNamn(), vg.getId());
            vgStats.getVardenheter().addAll(createAndPopulateVardenheterWithStats(vg.getVardenheter(), intygStats, fragaSvarStats));
            statsResponse.getVardgivare().add(vgStats);
        }
    }

    private List<VardenhetStats> createAndPopulateVardenheterWithStats(List<Vardenhet> vardenheter,
        Map<String, Long> intygStats, Map<String, Long> fragaSvarStats) {

        List<VardenhetStats> veStatsList = new ArrayList<>();

        VardenhetStats veStats;

        for (Vardenhet ve : vardenheter) {
            String veNamn = ve.getNamn();

            veStats = new VardenhetStats(veNamn, ve.getId());
            veStats.setOhanteradeFragaSvar(getSafeStatValueFromMap(ve.getId(), fragaSvarStats));
            veStats.setOsigneradeIntyg(getSafeStatValueFromMap(ve.getId(), intygStats));
            veStatsList.add(veStats);

            addStatsForMottagningar(ve, veStatsList, intygStats, fragaSvarStats);
        }

        return veStatsList;
    }

    private void addStatsForMottagningar(Vardenhet vardenhet, List<VardenhetStats> veStatsList,
        Map<String, Long> intygStats, Map<String, Long> fragaSvarStats) {

        List<Mottagning> mottagningar = vardenhet.getMottagningar();

        if (mottagningar == null || mottagningar.isEmpty()) {
            return;
        }

        VardenhetStats moStats;

        for (Mottagning mo : mottagningar) {
            String moNamn = Joiner.on(SEPARATOR).join(vardenhet.getNamn(), mo.getNamn());
            moStats = new VardenhetStats(moNamn, mo.getId());
            moStats.setOhanteradeFragaSvar(getSafeStatValueFromMap(mo.getId(), fragaSvarStats));
            moStats.setOsigneradeIntyg(getSafeStatValueFromMap(mo.getId(), intygStats));
            veStatsList.add(moStats);
        }

    }

    private static long getSafeStatValueFromMap(String id, Map<String, Long> statsMap) {
        Long statValue = statsMap.get(id);
        return (statValue != null) ? statValue : 0L;
    }

}
