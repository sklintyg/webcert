package se.inera.intyg.webcert.web.converter.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-08-21.
 */
@Component
public class ArendeStatisticsUtil {

    @Autowired
    private PUService puService;

    /**
     * Takes a list of object[] where each object[] is one fraga/svar or arende represented as:
     *
     * [0] id
     * [1] enhetsId
     * [2] personnummer
     *
     * This method will filter out any items belonging to a patient having sekretessmarkering and return the result as a
     * map: EnhetsId -> number of id for that unit.
     *
     * @param results
     *      Each item is an array of: id, enhetsId, personnummer.
     * @return
     *      Map with enhetsId -> count, with personummer being sekretessmarkerade has been removed.
     */
    public Map<String, Long> toSekretessFilteredMap(List<Object[]> results) {
        List<QAItem> tmpList = new ArrayList<>();
        for (Object[] resArr : results) {
            tmpList.add(new QAItem(resArr[0], (String) resArr[1], (String) resArr[2]));
        }
        return tmpList.stream()
                .filter(qaItem -> !isSekretessMarkerad(qaItem.personnummer))
                .collect(Collectors.groupingBy(qaItem -> qaItem.enhetsId, Collectors.counting()));
    }

    private boolean isSekretessMarkerad(String personnummer) {
        Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personnummer)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse personnummer"));
        PersonSvar personSvar = puService.getPerson(pnr);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            return personSvar.getPerson().isSekretessmarkering();
        } else {
            // For statistics, we can assume NOT sekretessmarkering when the PU-service cannot respond properly.
            return false;
        }
    }

    private static final class QAItem {
        public String id;
        public String enhetsId;
        public String personnummer;
        public boolean sekretessmarkering;

        private QAItem(Object id, String enhetsId, String personnummer) {
            this.id = id instanceof String ? (String) id : Long.toString((long) id);
            this.enhetsId = enhetsId;
            this.personnummer = personnummer;
        }
    }

}
