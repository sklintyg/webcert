package se.inera.webcert.notifications.process;

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

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.Arbetsformaga;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.Diagnos;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.PQ;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.DatumPeriod;
import se.inera.certificate.codes.Diagnoskodverk;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;
import se.inera.certificate.modules.service.WebcertModuleService;

public class EnrichWithIntygModelDataStrategy {

    @Autowired(required = false)
    private WebcertModuleService moduleService;

    public static final JsonPath DIAGNOS_KOD_JSONP = JsonPath.compile("$.diagnosKod");
    public static final JsonPath DIAGNOS_BESKR_JSONP = JsonPath.compile("$.diagnosBeskrivning1");
    public static final JsonPath DIAGNOS_CODESYSTEM_JSONP = JsonPath.compile("$.diagnosKodsystem1");

    public static final JsonPath NEDSATT_25_JSONP = JsonPath.compile("$.nedsattMed25");
    public static final JsonPath NEDSATT_50_JSONP = JsonPath.compile("$.nedsattMed50");
    public static final JsonPath NEDSATT_75_JSONP = JsonPath.compile("$.nedsattMed75");
    public static final JsonPath NEDSATT_100_JSONP = JsonPath.compile("$.nedsattMed100");

    private static final String ARBETSFORMAGA_UNIT = "%";

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

        List<Arbetsformaga> arbetsFormagor = extractArbetsformagor(jsonCtx);
        utlatandeType.getArbetsformaga().addAll(arbetsFormagor);

        Diagnos diagnosType = extractDiagnos(jsonCtx);

        if (diagnosType != null) {
            utlatandeType.setDiagnos(diagnosType);
        }

        return statusUpdateType;
    }

    public Diagnos extractDiagnos(ReadContext jsonCtx) {

        LOG.debug("Extracting diagnos info from JSON model");

        String diagnosKod = extractString(DIAGNOS_KOD_JSONP, jsonCtx);
        if (diagnosKod == null) {
            LOG.debug("Diagnos code was not found in model");
            return null;
        }

        String diagnosBeskr = extractString(DIAGNOS_BESKR_JSONP, jsonCtx);

        String kodverk = extractString(DIAGNOS_CODESYSTEM_JSONP, jsonCtx);
        Diagnoskodverk diagnoskodverk;
        if (kodverk == null) {
            // Default is ICD-10-SE
            diagnoskodverk = Diagnoskodverk.ICD_10_SE;
        } else {
            diagnoskodverk = Diagnoskodverk.valueOf(kodverk);
        }

        if (!moduleService.validateDiagnosisCode(diagnosKod, diagnoskodverk.getCodeSystemName())) {
            LOG.debug("Diagnos code is not valid.");
            return null;
        }

        Diagnos dt = new Diagnos();
        dt.setCode(diagnosKod);
        dt.setCodeSystem(diagnoskodverk.getCodeSystem());
        dt.setCodeSystemName(diagnoskodverk.getCodeSystemName());
        dt.setDisplayName(diagnosBeskr != null ? diagnosBeskr : "");

        return dt;
    }

    public List<Arbetsformaga> extractArbetsformagor(ReadContext jsonCtx) {

        List<Arbetsformaga> arbetsFormagor = new ArrayList<Arbetsformaga>();

        for (Entry<String, JsonPath> afEntry : jsonPaths.entrySet()) {
            Arbetsformaga arbetsformagaType = extractToArbArbetsformaga(afEntry.getKey(), afEntry.getValue(), jsonCtx);
            if (arbetsformagaType != null) {
                arbetsFormagor.add(arbetsformagaType);
            }
        }

        LOG.debug("Extracted {} ArbetsformagaType from JSON model", arbetsFormagor.size());

        return arbetsFormagor;
    }

    public Arbetsformaga extractToArbArbetsformaga(String nedsattningArbetsformaga, JsonPath jsonPath, ReadContext jsonCtx) {

        NedsattningsPeriod nedsattningsPeriod = extractToNedsattningsPeriod(jsonPath, jsonCtx);

        if (nedsattningsPeriod == null) {
            LOG.debug("Could not found NedsattningsPeriod for {}%", nedsattningArbetsformaga);
            return null;
        }

        LOG.debug("Found NedsattningsPeriod for nedsattning by {}%", nedsattningArbetsformaga);

        Arbetsformaga arbTyp = new Arbetsformaga();
        arbTyp.setPeriod(convertNedsattningsPeriodToDatumPeriod(nedsattningsPeriod));
        arbTyp.setVarde(buildPQForArbetsformaga(nedsattningArbetsformaga));
        return arbTyp;
    }

    private DatumPeriod convertNedsattningsPeriodToDatumPeriod(NedsattningsPeriod period) {
        DatumPeriod di = new DatumPeriod();
        di.setFrom(parseToLocalDate(period.from));
        di.setTom(parseToLocalDate(period.tom));
        return di;
    }

    /**
     * Calculates the REMAINING arbetsformaga based on the nedsattning of arbetsformaga.
     *
     * @param nedsattningArbetsformaga
     * @return
     */
    private PQ buildPQForArbetsformaga(String nedsattningArbetsformaga) {
        PQ pq = new PQ();
        pq.setUnit(ARBETSFORMAGA_UNIT);
        pq.setValue(100 - Double.parseDouble(nedsattningArbetsformaga));
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
