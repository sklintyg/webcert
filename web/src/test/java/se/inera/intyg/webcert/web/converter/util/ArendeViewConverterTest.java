/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import se.inera.intyg.common.support.model.*;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.model.internal.LisjpUtlatande;
import se.inera.intyg.common.luse.model.internal.LuseUtlatande;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;

@RunWith(MockitoJUnitRunner.class)
public class ArendeViewConverterTest {
    private static final String PATIENT_PERSON_ID = "19121212-1212";
    private static final String SKAPADAV_PERSON_ID = "19121212-1212";
    private static final String ENHETS_ID = "enhetsId";
    private static final String ENHETS_NAMN = "enhetsNamn";
    private static final String intygsId = "intyg-1";
    private static final String VARDAKTOR_NAMN = "vardaktor namn";
    private static final String VARDGIVARE_NAMN = "vardgivare namn";

    @InjectMocks
    private ArendeViewConverter converter;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private IntygServiceImpl intygService;

    @Mock
    private ModuleApi moduleApi;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        Map<String, List<String>> map = new HashMap<>();
        map.put("1", Arrays.asList(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_JSON_ID_1, RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TELEFONKONTAKT_PATIENT_SVAR_JSON_ID_1));
        map.put("2", Arrays.asList(RespConstants.KANNEDOM_SVAR_JSON_ID_2));
        map.put("4", Arrays.asList("", "", RespConstants.UNDERLAG_SVAR_JSON_ID_4));
        when(moduleApi.getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class))).thenReturn(map);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToArendeForLuse() throws ModuleNotFoundException {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean())).thenReturn(new IntygContentHolder("", buildLuseUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                SKAPADAV_PERSON_ID,
                LocalDateTime.now().minusDays(2)), Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))), false, null));

        ArendeView result = converter.convert(buildArende("luse"));

        assertNotNull(result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(RespConstants.KANNEDOM_SVAR_JSON_ID_2,
                result.getKompletteringar().get(1).getJsonPropertyHandle());
        assertEquals(RespConstants.UNDERLAG_SVAR_JSON_ID_4,
                result.getKompletteringar().get(2).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(new Integer(0), result.getKompletteringar().get(1).getPosition());
        assertEquals(new Integer(1), result.getKompletteringar().get(2).getPosition());
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @Test
    public void convertToJson() throws JsonGenerationException, JsonMappingException, IOException{
        Arende arende = buildArende("lisjp");
        StringWriter jsonWriter = new StringWriter();
        CustomObjectMapper objectMapper = new CustomObjectMapper();
        objectMapper.writeValue(jsonWriter, arende);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToArendeForLisjp() throws ModuleNotFoundException {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean())).thenReturn(new IntygContentHolder("", buildLisjpUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                SKAPADAV_PERSON_ID,
                LocalDateTime.now().minusDays(2)), Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))), false, null));

        ArendeView result = converter.convert(buildArende("lisjp"));

        assertEquals(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TELEFONKONTAKT_PATIENT_SVAR_JSON_ID_1,
                result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(RespConstants.KANNEDOM_SVAR_JSON_ID_2,
                result.getKompletteringar().get(1).getJsonPropertyHandle());
        assertEquals(RespConstants.UNDERLAG_SVAR_JSON_ID_4,
                result.getKompletteringar().get(2).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(new Integer(0), result.getKompletteringar().get(1).getPosition());
        assertEquals(new Integer(1), result.getKompletteringar().get(2).getPosition());
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertKompletteringWithoutInstans() throws ModuleNotFoundException {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean())).thenReturn(new IntygContentHolder("", buildLisjpUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                SKAPADAV_PERSON_ID,
                LocalDateTime.now().minusDays(2)), Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))), false, null));

        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("1", null, "arende1")));
        ArendeView result = converter.convert(arende);

        assertEquals(1, result.getKompletteringar().size());
        assertEquals(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_JSON_ID_1, result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertKompletteringInstansTooHigh() throws ModuleNotFoundException {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean())).thenReturn(new IntygContentHolder("", buildLisjpUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                SKAPADAV_PERSON_ID,
                LocalDateTime.now().minusDays(2)), Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))), false, null));

        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("1", 3, "arende1")));
        ArendeView result = converter.convert(arende);

        assertEquals(1, result.getKompletteringar().size());
        assertEquals(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_JSON_ID_1, result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(new Integer(2), result.getKompletteringar().get(0).getPosition());
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertKompletteringUnknownQuestionId() throws ModuleNotFoundException {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean())).thenReturn(new IntygContentHolder("", buildLisjpUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                SKAPADAV_PERSON_ID,
                LocalDateTime.now().minusDays(2)), Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))), false, null));

        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("10", 1, "arende1")));
        ArendeView result = converter.convert(arende);

        assertEquals(1, result.getKompletteringar().size());
        assertEquals("", result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToArendeWithoutKomplettering() throws ModuleNotFoundException {
        ArendeView result = converter.convert(buildArende("meddelandeId", LocalDateTime.now(), LocalDateTime.now()));

        assertTrue(CollectionUtils.isEmpty(result.getKompletteringar()));
        assertEquals(VARDAKTOR_NAMN, result.getVardaktorNamn());
        assertEquals(ENHETS_NAMN, result.getEnhetsnamn());
        assertEquals(VARDGIVARE_NAMN, result.getVardgivarnamn());
        verify(moduleApi, never()).getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class));
    }

    @Test
    public void convertToArendeConversationViewTest() {
        final String fragaMeddelandeId = "fragaId";
        final LocalDateTime senasteHandelseFraga = LocalDateTime.now();
        final String svarMeddelandeId = "svarId";
        final String paminnelse1MeddelandeId = "paminnelse1Id";
        final LocalDateTime paminnelse1Timestamp = LocalDateTime.now();
        final String paminnelse2MeddelandeId = "paminnelse2Id";
        final LocalDateTime paminnelse2Timestamp = paminnelse1Timestamp.minusDays(2);

        ArendeConversationView res = converter.convertToArendeConversationView(buildArende(fragaMeddelandeId, senasteHandelseFraga, LocalDateTime.now()),
                buildArende(svarMeddelandeId, null, LocalDateTime.now()), Arrays.asList(buildArende(paminnelse2MeddelandeId, null, paminnelse2Timestamp),
                        buildArende(paminnelse1MeddelandeId, null, paminnelse1Timestamp)));
        assertNotNull(res);
        assertNotNull(res.getFraga());
        assertEquals(fragaMeddelandeId, res.getFraga().getInternReferens());
        assertNotNull(res.getSvar());
        assertEquals(svarMeddelandeId, res.getSvar().getInternReferens());
        assertNotNull(res.getPaminnelser());
        assertEquals(2, res.getPaminnelser().size());

        // assert paminnelser are sorted
        assertEquals(paminnelse1MeddelandeId, res.getPaminnelser().get(0).getInternReferens());
        assertEquals(paminnelse2MeddelandeId, res.getPaminnelser().get(1).getInternReferens());
    }

    @Test
    public void buildArendeConversationsTest() {
        final LocalDateTime january = LocalDateTime.parse("2013-01-12T11:22:11");
        final LocalDateTime february = LocalDateTime.parse("2013-02-12T11:22:11");
        final LocalDateTime decemberYear9999 = LocalDateTime.parse("9999-12-11T10:22:00");
        List<Arende> arendeList = new ArrayList<>();

        arendeList.add(buildArende(UUID.randomUUID().toString(), decemberYear9999, february));
        arendeList.add(buildArende(UUID.randomUUID().toString(), january, january));
        arendeList.get(1).setSvarPaId(arendeList.get(0).getMeddelandeId()); // svar
        arendeList.add(buildArende(UUID.randomUUID().toString(), decemberYear9999, decemberYear9999));
        arendeList.get(2).setAmne(ArendeAmne.PAMINN);
        arendeList.get(2).setPaminnelseMeddelandeId(arendeList.get(0).getMeddelandeId()); // paminnelse
        arendeList.add(buildArende(UUID.randomUUID().toString(), february, february));
        arendeList.add(buildArende(UUID.randomUUID().toString(), decemberYear9999, decemberYear9999));
        arendeList.add(buildArende(UUID.randomUUID().toString(), january, january));

        List<ArendeConversationView> result = converter.buildArendeConversations(arendeList);

        assertEquals(4, result.size());
        assertEquals(1, result.get(0).getPaminnelser().size());
        assertEquals(arendeList.get(0).getMeddelandeId(), result.get(0).getFraga().getInternReferens());
        assertEquals(arendeList.get(1).getMeddelandeId(), result.get(0).getSvar().getInternReferens());
        assertEquals(arendeList.get(2).getMeddelandeId(), result.get(0).getPaminnelser().get(0).getInternReferens());
        assertEquals(arendeList.get(3).getMeddelandeId(), result.get(2).getFraga().getInternReferens());
        assertEquals(arendeList.get(4).getMeddelandeId(), result.get(1).getFraga().getInternReferens());
        assertEquals(arendeList.get(5).getMeddelandeId(), result.get(3).getFraga().getInternReferens());
        assertEquals(decemberYear9999, result.get(0).getSenasteHandelse());
        assertEquals(decemberYear9999, result.get(1).getSenasteHandelse());
        assertEquals(february, result.get(2).getSenasteHandelse());
        assertEquals(january, result.get(3).getSenasteHandelse());
    }

    private LisjpUtlatande buildLisjpUtlatande(String intygsid2, String enhetsId, String enhetsNamn, String patientPersonId,
            String skapadAvNamn, String skapadavPersonId, LocalDateTime timeStamp) {

        LisjpUtlatande.Builder template = LisjpUtlatande.builder();

        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsNamn, patientPersonId, skapadavPersonId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setTelefonkontaktMedPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private LuseUtlatande buildLuseUtlatande(String intygsId, String enhetsId, String enhetsnamn, String patientPersonId,
            String string, String skapadAvId, LocalDateTime timeStamp) {
        LuseUtlatande.Builder template = LuseUtlatande.builder();
        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsnamn, patientPersonId, skapadAvId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setAnhorigsBeskrivningAvPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private GrundData buildGrundData(String enhetsId, String enhetsNamn, String patientPersonId, String skapadavPersonId, LocalDateTime timeStamp) {
        GrundData grundData = new GrundData();
        HoSPersonal skapadAv = new HoSPersonal();
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(enhetsId);
        vardenhet.setEnhetsnamn(enhetsNamn);
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid("vardgivarid");
        vardgivare.setVardgivarnamn("vardgivarnamn");
        vardenhet.setVardgivare(vardgivare);
        skapadAv.setVardenhet(vardenhet);
        skapadAv.setFullstandigtNamn(skapadavPersonId);
        skapadAv.setPersonId(skapadavPersonId);
        grundData.setSkapadAv(skapadAv);
        Patient patient = new Patient();
        Personnummer personId = new Personnummer(patientPersonId);
        patient.setPersonId(personId);
        grundData.setPatient(patient);
        grundData.setSigneringsdatum(timeStamp);
        return grundData;
    }

    private Arende buildArende(String intygstyp) {
        Arende arende = new Arende();
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setIntygsId(intygsId);
        arende.setStatus(se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION);
        arende.setMeddelandeId("meddelandeId");
        arende.setPatientPersonId("191212121212");
        arende.setTimestamp(LocalDateTime.now());
        arende.setIntygTyp(intygstyp);
        arende.setVardaktorName(VARDAKTOR_NAMN);
        arende.setEnhetId(ENHETS_ID);
        arende.setEnhetName(ENHETS_NAMN);
        arende.setVardgivareName(VARDGIVARE_NAMN);

        arende.setSkickatAv("Fragestallare");
        arende.setRubrik("rubrik");
        arende.setSistaDatumForSvar(LocalDateTime.now().plusDays(4).toLocalDate());
        MedicinsktArende medArende1 = buildMedicinsktArende("1", 1, "arende1");
        MedicinsktArende medArende2 = buildMedicinsktArende("2", 1, "arende1");
        MedicinsktArende medArende4 = buildMedicinsktArende("4", 2, "arende1");
        arende.setKomplettering(Arrays.asList(medArende1, medArende2, medArende4));

        return arende;
    }

    private Arende buildArende(String meddelandeId, LocalDateTime senasteHandelse, LocalDateTime timestamp) {
        Arende arende = new Arende();
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setIntygsId(intygsId);
        arende.setStatus(se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION);
        arende.setMeddelandeId(meddelandeId);
        arende.setPatientPersonId("191212121212");
        arende.setTimestamp(timestamp);
        arende.setSenasteHandelse(senasteHandelse != null ? senasteHandelse : timestamp);
        arende.setIntygTyp("luse");
        arende.setVardaktorName(VARDAKTOR_NAMN);
        arende.setEnhetId(ENHETS_ID);
        arende.setEnhetName(ENHETS_NAMN);
        arende.setVardgivareName(VARDGIVARE_NAMN);
        arende.setSkickatAv("Fragestallare");
        arende.setRubrik("rubrik");
        arende.setSistaDatumForSvar(LocalDateTime.now().plusDays(4).toLocalDate());
        return arende;
    }

    private MedicinsktArende buildMedicinsktArende(String frageId, Integer instansId, String text) {
        MedicinsktArende med1 = new MedicinsktArende();
        med1.setFrageId(frageId);
        med1.setInstans(instansId);
        med1.setText(text);
        return med1;
    }

}
