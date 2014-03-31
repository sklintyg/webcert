package se.inera.webcert.web.controller.moduleapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.web.controller.moduleapi.dto.StatsResponse;
import se.inera.webcert.web.controller.moduleapi.dto.VardenhetStats;
import se.inera.webcert.web.controller.moduleapi.dto.VardgivareStats;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author marced
 */
public class StatModuleApiController {

    private static final String SEPARATOR = " - ";

    @Autowired
    private IntygService intygService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private WebCertUserService webCertUserService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getStatistics() {
        
        StatsResponse statsResponse = new StatsResponse();
        
        WebCertUser user = webCertUserService.getWebCertUser();
        List<String> allUnitIds = user.getIdsOfAllVardenheter();
        
        Map<String, Long> fragaSvarStats = fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(allUnitIds); 
                       
        populateStatsResponseWithStats(statsResponse, user.getVardgivare(), fragaSvarStats);
        
        List<String> unitIdsOfSelected = user.getIdsOfSelectedVardenhet();
        
        long fragaSvarOnOtherUnitThanTheSelected = calcTotalOfUnhandledFragaSvarOnOtherUnitThanTheSelected(unitIdsOfSelected, allUnitIds, fragaSvarStats);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnOtherThanSelected(fragaSvarOnOtherUnitThanTheSelected);
        
        long fragaSvarOnSelected = calcTotalOfUnhandledFragaSvarOnSelected(unitIdsOfSelected, fragaSvarStats);
        statsResponse.setTotalNbrOfUnhandledFragaSvarOnSelected(fragaSvarOnSelected);
        
        return Response.ok(statsResponse).build();
    }

    private long calcTotalOfUnhandledFragaSvarOnSelected(List<String> allUnitIds, Map<String, Long> fragaSvarStats) {
        
        long sum = 0;
                
        for (String unitId : allUnitIds) {
            sum += getSafeStatValueFromMap(unitId, fragaSvarStats);
        }
        
        return sum;
    }

    private long calcTotalOfUnhandledFragaSvarOnOtherUnitThanTheSelected(List<String> unitIdsOfSelected, List<String> allUnitIds,
            Map<String, Long> fragaSvarStats) {
        
        long sum = 0;
        
        @SuppressWarnings("unchecked")
        List<String> unitIdsOfNotSelected = (List<String>) CollectionUtils.subtract(allUnitIds, unitIdsOfSelected);
        
        for (String unitId : unitIdsOfNotSelected) {
            sum += getSafeStatValueFromMap(unitId, fragaSvarStats);
        }
                
        return sum;
    }

    private void populateStatsResponseWithStats(StatsResponse statsResponse, List<Vardgivare> vardgivare, Map<String, Long> fragaSvarStats) {
        
        VardgivareStats vgStats;
        
        for(Vardgivare vg : vardgivare) {
            vgStats = new VardgivareStats(vg.getNamn(), vg.getId());
            vgStats.getVardenheter().addAll(createAndPopulateVardenheterWithStats(vg.getVardenheter(), fragaSvarStats));
            statsResponse.getVardgivare().add(vgStats);
        }
    }

    private List<VardenhetStats> createAndPopulateVardenheterWithStats(List<Vardenhet> vardenheter,  Map<String, Long> fragaSvarStats) {
        
        List<VardenhetStats> veStatsList = new ArrayList<VardenhetStats>();
        
        VardenhetStats veStats;
        
        for(Vardenhet ve : vardenheter) {
            String veNamn = ve.getNamn();
            
            veStats = new VardenhetStats(veNamn, ve.getId());
            veStats.setOhanteradeFragaSvar(getSafeStatValueFromMap(ve.getId(), fragaSvarStats));
            veStatsList.add(veStats);
            
            addStatsForMottagningar(ve, veStatsList, fragaSvarStats);
        }
        
        return veStatsList;
    }
    
    private void addStatsForMottagningar(Vardenhet vardenhet, List<VardenhetStats> veStatsList,
            Map<String, Long> fragaSvarStats) {
        
        List<Mottagning> mottagningar = vardenhet.getMottagningar();
        
        if (mottagningar == null || mottagningar.isEmpty()) {
            return;
        }
        
        VardenhetStats moStats;
        
        for (Mottagning mo : mottagningar) {
            String moNamn = StringUtils.join(new Object[] {vardenhet.getNamn(), mo.getNamn()}, SEPARATOR);
            moStats = new VardenhetStats(moNamn , mo.getId());
            moStats.setOhanteradeFragaSvar(getSafeStatValueFromMap(mo.getId(), fragaSvarStats));
            veStatsList.add(moStats);
        }
        
    }
    
    private static long getSafeStatValueFromMap(String id, Map<String, Long> statsMap) {
        Long statValue = statsMap.get(id);
        return (statValue != null) ? statValue.longValue() : 0L;
    }
    
}
