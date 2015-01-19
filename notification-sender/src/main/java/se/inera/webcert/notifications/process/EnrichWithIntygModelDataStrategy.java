package se.inera.webcert.notifications.process;

import iso.v21090.dt.v1.PQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.ArbetsformagaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.DiagnosType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.DateInterval;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;

public class EnrichWithIntygModelDataStrategy {

    public static final JsonPath DIAGNOS_KOD_JSONP = JsonPath.compile("$.diagnosKod");
    public static final JsonPath DIAGNOS_BESKR_JSONP = JsonPath.compile("$.diagnosBeskrivning1");

    public static final JsonPath NEDSATT_25_JSONP = JsonPath.compile("$.nedsattMed25");
    public static final JsonPath NEDSATT_50_JSONP = JsonPath.compile("$.nedsattMed50");
    public static final JsonPath NEDSATT_75_JSONP = JsonPath.compile("$.nedsattMed75");
    public static final JsonPath NEDSATT_100_JSONP = JsonPath.compile("$.nedsattMed100");

    private static final String ARBETSFORMAGA_UNIT = "%";

    private static final String DIAGNOS_CODESYSTEM = "1.2.752.116.1.1.1.1.1";
    private static final String DIAGNOS_CODESYSTEM_NAME = "ICD-10";

    private static final Logger LOG = LoggerFactory.getLogger(EnrichWithIntygModelDataStrategy.class);

    private static final Configuration PARSER_JACKSON_CONFIGURATION = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonProvider())
            .build();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private Map<String, JsonPath> jsonPaths = new HashMap<String, JsonPath>();

    public EnrichWithIntygModelDataStrategy() {
        initJsonPaths();
    }

    private void initJsonPaths() {
        jsonPaths.put("25", NEDSATT_25_JSONP);
        jsonPaths.put("50", NEDSATT_50_JSONP);
        jsonPaths.put("75", NEDSATT_75_JSONP);
        jsonPaths.put("100", NEDSATT_100_JSONP);
    }

    public ReadContext setupJsonContext(String json) {
        return JsonPath.using(PARSER_JACKSON_CONFIGURATION).parse(json);
    }

    public CertificateStatusUpdateForCareType enrichWithArbetsformagorAndDiagnos(CertificateStatusUpdateForCareType statusUpdateType, String jsonModel) {
        LOG.debug("Enriching with data from JSON model");
        UtlatandeType utlatandeType = statusUpdateType.getUtlatande();
        ReadContext jsonCtx = setupJsonContext(jsonModel);

        List<ArbetsformagaType> arbetsFormagor = extractArbetsformagor(jsonCtx);
        utlatandeType.getArbetsformaga().addAll(arbetsFormagor);

        DiagnosType diagnosType = extractDiagnos(jsonCtx);

        if (diagnosType != null) {
            utlatandeType.setDiagnos(diagnosType);
        }

        return statusUpdateType;
    }

    public DiagnosType extractDiagnos(ReadContext jsonCtx) {

        LOG.debug("Extracting diagnos info from JSON model");

        String diagnosKod = extractString(DIAGNOS_KOD_JSONP, jsonCtx);

        if (diagnosKod == null) {
            LOG.debug("Diagnos code was not found in model");
            return null;
        }

        String diagnosBeskr = extractString(DIAGNOS_BESKR_JSONP, jsonCtx);

        DiagnosType dt = new DiagnosType();
        dt.setCode(diagnosKod);
        dt.setCodeSystem(DIAGNOS_CODESYSTEM);
        dt.setCodeSystemName(DIAGNOS_CODESYSTEM_NAME);
        dt.setDisplayName(diagnosBeskr);

        return dt;
    }

    public List<ArbetsformagaType> extractArbetsformagor(ReadContext jsonCtx) {

        List<ArbetsformagaType> arbetsFormagor = new ArrayList<ArbetsformagaType>();

        for (Entry<String, JsonPath> afEntry : jsonPaths.entrySet()) {
            ArbetsformagaType arbetsformagaType = extractToArbArbetsformagaType(afEntry.getKey(), afEntry.getValue(), jsonCtx);
            if (arbetsformagaType != null) {
                arbetsFormagor.add(arbetsformagaType);
            }
        }

        LOG.debug("Extracted {} ArbetsformagaType from JSON model", arbetsFormagor.size());

        return arbetsFormagor;
    }

    public ArbetsformagaType extractToArbArbetsformagaType(String enhet, JsonPath jsonPath, ReadContext jsonCtx) {

        NedsattningsPeriod nedsattningsPeriod = extractToNedsattningsPeriod(jsonPath, jsonCtx);

        if (nedsattningsPeriod == null) {
            LOG.debug("Could not found ArbetsformagaType for {}%", enhet);
            return null;
        }

        LOG.debug("Found ArbetsformagaType for {}%", enhet);

        ArbetsformagaType arbTyp = new ArbetsformagaType();
        arbTyp.setPeriod(convertNedsattningsPeriodToDateInterval(nedsattningsPeriod));
        arbTyp.setVarde(buildPQForArbetsformaga(enhet));
        return arbTyp;
    }

    private DateInterval convertNedsattningsPeriodToDateInterval(NedsattningsPeriod period) {
        DateInterval di = new DateInterval();
        di.setFrom(parseToLocalDate(period.from));
        di.setTom(parseToLocalDate(period.tom));
        return di;
    }

    private PQ buildPQForArbetsformaga(String varde) {
        PQ pq = new PQ();
        pq.setUnit(ARBETSFORMAGA_UNIT);
        pq.setValue(Double.parseDouble(varde));
        return pq;
    }

    private LocalDate parseToLocalDate(String str) {
        return LocalDate.parse(str, DATE_FORMAT);
    }

    public String extractString(JsonPath jsonPath, ReadContext jsonCtx) {
        try {
            return jsonCtx.read(jsonPath);
        } catch (PathNotFoundException e) {
            LOG.trace("JsonPath '{}' evaluates to null", jsonPath);
            return null;
        }
    }

    public NedsattningsPeriod extractToNedsattningsPeriod(JsonPath jsonPath, ReadContext jsonCtx) {
        try {
            return jsonCtx.read(jsonPath, NedsattningsPeriod.class);
        } catch (PathNotFoundException e) {
            LOG.trace("JsonPath '{}' evaluates to null", jsonPath);
            return null;
        }
    }

    public static class NedsattningsPeriod {
        private String from;
        private String tom;

        public String getFrom() {
            return from;
        }
        public void setFrom(String from) {
            this.from = from;
        }
        public String getTom() {
            return tom;
        }
        public void setTom(String tom) {
            this.tom = tom;
        }
    }
}
