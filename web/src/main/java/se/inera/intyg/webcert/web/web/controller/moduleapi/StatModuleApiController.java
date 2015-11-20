package se.inera.intyg.webcert.web.web.controller.moduleapi;

import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.StatsResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.VardenhetStats;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.VardgivareStats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author marced
 */
@Path("/stat")
@Api(value = "stat", description = "REST API - moduleapi - stat", produces = MediaType.APPLICATION_JSON)
public class StatModuleApiController extends AbstractApiController {

    private static final String SEPARATOR = " - ";

    private static final Logger LOG = LoggerFactory.getLogger(StatModuleApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private UtkastService intygDraftService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getStatistics() {

        StatsResponse statsResponse = new StatsResponse();

        WebCertUser user = getWebCertUserService().getUser();

        if (user == null) {
            LOG.warn("getStatistics was called, but webcertUser was null!");
            return Response.ok(statsResponse).build();
        }

        List<String> allUnitIds = user.getIdsOfAllVardenheter();

        if (allUnitIds == null || allUnitIds.isEmpty()) {
            LOG.warn("getStatistics was called by user {} that have no id:s of vardenheter present in the user context: {}", user.getHsaId(),
                    user.getAsJson());
            return Response.ok(statsResponse).build();
        }

        Map<String, Long> fragaSvarStatsMap = fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(allUnitIds);

        Map<String, Long> intygStatsMap = intygDraftService.getNbrOfUnsignedDraftsByCareUnits(allUnitIds);

        List<String> unitIdsOfSelected = user.getIdsOfSelectedVardenhet();

        @SuppressWarnings("unchecked")
        List<String> unitIdsOfNotSelected = (List<String>) CollectionUtils.subtract(allUnitIds, unitIdsOfSelected);

        long fragaSvarOnOtherUnitThanTheSelected = calcSumFromSelectedUnits(unitIdsOfNotSelected,
                fragaSvarStatsMap);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnOtherThanSelected(fragaSvarOnOtherUnitThanTheSelected);

        long fragaSvarOnSelected = calcSumFromSelectedUnits(unitIdsOfSelected, fragaSvarStatsMap);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnSelected(fragaSvarOnSelected);

        long unsignedDraftsOnOtherThanSelected = calcSumFromSelectedUnits(unitIdsOfNotSelected,
                intygStatsMap);
        statsResponse.setTotalNbrOfUnsignedDraftsOnOtherThanSelected(unsignedDraftsOnOtherThanSelected);

        long unsignedDraftsOnSelected = getSafeStatValueFromMap(user.getValdVardenhet().getId(), intygStatsMap);

        statsResponse.setTotalNbrOfUnsignedDraftsOnSelected(unsignedDraftsOnSelected);

        populateStatsResponseWithVardgivarStats(statsResponse, user.getVardgivare(), intygStatsMap, fragaSvarStatsMap);

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
            vgStats.getVardenheter().addAll(
                    createAndPopulateVardenheterWithStats(vg.getVardenheter(), intygStats, fragaSvarStats));
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
            String moNamn = StringUtils.join(new Object[] { vardenhet.getNamn(), mo.getNamn() }, SEPARATOR);
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
