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
package se.inera.intyg.webcert.web.converter.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.luse.v1.model.internal.LuseUtlatandeV1;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.InternalDate;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.AnsweredWithIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        Map<String, List<String>> map = new HashMap<>();
        map.put("1", Arrays.asList(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_JSON_ID_1,
                RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TELEFONKONTAKT_PATIENT_SVAR_JSON_ID_1));
        map.put("2", Arrays.asList(RespConstants.KANNEDOM_SVAR_JSON_ID_2));
        map.put("4", Arrays.asList("", "", RespConstants.UNDERLAG_SVAR_JSON_ID_4));
        when(moduleApi.getModuleSpecificArendeParameters(any(Utlatande.class), any(List.class))).thenReturn(map);
    }

    @Before
    public void setupDefaultMocksForIntygService() {
        when(intygService.fetchIntygData(any(String.class), any(String.class), Mockito.anyBoolean()))
                .thenReturn(
                        IntygContentHolder.builder()
                                .setContents("")
                                .setUtlatande(buildLuseUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, "Test Testsson",
                                        SKAPADAV_PERSON_ID, LocalDateTime.now().minusDays(2)))
                                .setStatuses(
                                        Arrays.asList(new Status(CertificateState.RECEIVED, intygsId, LocalDateTime.now().minusDays(2))))
                                .setRevoked(false)
                                .setRelations(new Relations())
                               // .setReplacedByRelation(null)
                               // .setComplementedByRelation(null)
                                .setDeceased(false)
                                .setSekretessmarkering(false)
                                .setPatientNameChangedInPU(false)
                                .setPatientAddressChangedInPU(false)
                                .build());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToArendeForLuse() throws ModuleNotFoundException, ModuleException {
        ArendeView result = converter.convertToDto(buildArende("luse"));

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
    public void convertToJson() throws JsonGenerationException, JsonMappingException, IOException {
        Arende arende = buildArende("lisjp");
        StringWriter jsonWriter = new StringWriter();
        CustomObjectMapper objectMapper = new CustomObjectMapper();
        objectMapper.writeValue(jsonWriter, arende);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToArendeForLisjp() throws ModuleNotFoundException, ModuleException {
        ArendeView result = converter.convertToDto(buildArende("lisjp"));

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
    public void testConvertKompletteringWithoutInstans() throws ModuleNotFoundException, ModuleException {
        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("1", null, "arende1")));
        ArendeView result = converter.convertToDto(arende);

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
    public void testConvertKompletteringInstansTooHigh() throws ModuleNotFoundException, ModuleException {
        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("1", 3, "arende1")));
        ArendeView result = converter.convertToDto(arende);

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
    public void testConvertKompletteringUnknownQuestionId() throws ModuleNotFoundException, ModuleException {
        Arende arende = buildArende("lisjp");
        arende.setKomplettering(Arrays.asList(buildMedicinsktArende("10", 1, "arende1")));
        ArendeView result = converter.convertToDto(arende);

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
    public void testConvertToArendeWithoutKomplettering() throws ModuleNotFoundException, ModuleException {
        ArendeView result = converter.convertToDto(buildArende("meddelandeId", LocalDateTime.now(), LocalDateTime.now()));

        assertTrue(result.getKompletteringar().isEmpty());
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

        ArendeConversationView res = converter.convertToArendeConversationView(
                buildArende(fragaMeddelandeId, senasteHandelseFraga, LocalDateTime.now()),
                buildArende(svarMeddelandeId, null, LocalDateTime.now()), null,
                Arrays.asList(buildArende(paminnelse2MeddelandeId, null, paminnelse2Timestamp),
                        buildArende(paminnelse1MeddelandeId, null, paminnelse1Timestamp)),
                null);

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

        List<ArendeConversationView> result = converter.buildArendeConversations(intygsId, arendeList, Collections.emptyList(),
                Collections.emptyList());

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

    @Test
    public void paminnelserInArendeConversionViewShouldBeReverseSortedOnDate() {
        // Given
        LocalDateTime fragaTimestamp = LocalDateTime.parse("2016-03-01T11:22:11");
        Arende fraga = createValidArendeForLuse("fraga", fragaTimestamp, "fraga1-id", null);
        Arende p1 = createValidArendeForLuse("paminnelse", fragaTimestamp.plusHours(2), "paminnelse-1.1-id", fraga);
        Arende p2 = createValidArendeForLuse("paminnelse", fragaTimestamp.plusDays(1), "paminnelse-1.2-id", fraga);
        Arende p3 = createValidArendeForLuse("paminnelse", fragaTimestamp.plusDays(3), "paminnelse-1.3-id", fraga);
        Arende svar = createValidArendeForLuse("svar", fragaTimestamp.plusDays(4), "svar-1-id", fraga);

        // When
        ArendeConversationView messageThread = converter.convertToArendeConversationView(fraga, svar, null,
                ImmutableList.of(p3, p1, p2), null);

        // Then
        List<ArendeView> expectedPaminnelserOrder = Stream.of(p3, p2, p1)
                .map(converter::convertToDto)
                .collect(Collectors.toList());
        Assertions.assertThat(messageThread.getPaminnelser())
                .hasSize(3)
                .containsExactlyElementsOf(expectedPaminnelserOrder);
    }

    @Test
    public void differentMessageThreadsShouldBeSeparatedAndSortedOnSenasteHandelse() {
        // Given
        LocalDateTime startOfFirstThread = LocalDateTime.parse("2016-03-01T11:22:11");
        Arende fraga1 = createValidArendeForLuse("fraga", startOfFirstThread, "fraga1-id", null);
        Arende p11 = createValidArendeForLuse("paminnelse", startOfFirstThread.plusHours(2), "paminnelse-1.1-id", fraga1);
        Arende p12 = createValidArendeForLuse("paminnelse", startOfFirstThread.plusDays(1), "paminnelse-1.2-id", fraga1);
        Arende p13 = createValidArendeForLuse("paminnelse", startOfFirstThread.plusDays(3), "paminnelse-1.3-id", fraga1);
        Arende svar1 = createValidArendeForLuse("svar", startOfFirstThread.plusDays(4), "svar-1-id", fraga1);

        LocalDateTime startOfSecondThread = LocalDateTime.parse("2016-03-02T11:22:11");
        Arende fraga2 = createValidArendeForLuse("fraga", startOfSecondThread, "fraga2-id", null);
        Arende p21 = createValidArendeForLuse("paminnelse", startOfFirstThread.plusHours(2), "paminnelse-2.1-id", fraga2);
        Arende p22 = createValidArendeForLuse("paminnelse", startOfFirstThread.plusDays(1), "paminnelse-2.2-id", fraga2);
        Arende svar2 = createValidArendeForLuse("svar", startOfFirstThread.plusDays(4), "svar-2-id", fraga2);

        List<Arende> mixedThreads = ImmutableList.of(fraga1, fraga2, p11, p21, p12, p22, p13, svar2, svar1);

        // When
        List<ArendeConversationView> createdList = converter.buildArendeConversations(intygsId, mixedThreads, Collections.emptyList(),
                Collections.emptyList());

        // Then
        List<ArendeView> expectedPaminnelser1Dtos = Stream.of(p13, p12, p11)
                .map(converter::convertToDto)
                .collect(Collectors.toList());
        List<ArendeView> expectedPaminnelser2Dtos = Stream.of(p22, p21)
                .map(converter::convertToDto)
                .collect(Collectors.toList());
        Assertions.assertThat(createdList)
                .hasSize(2)
                .extracting("fraga", "svar", "paminnelser")
                .contains(tuple(converter.convertToDto(fraga1), converter.convertToDto(svar1), expectedPaminnelser1Dtos),
                        tuple(converter.convertToDto(fraga2), converter.convertToDto(svar2), expectedPaminnelser2Dtos));
    }

    @Test
    public void emptyListOfKompletterandeIntygIsOk() {
        // Given
        LocalDateTime fragaTimestamp = LocalDateTime.parse("2016-03-01T11:22:11");
        Arende fraga = createValidArendeForLuse("fraga", fragaTimestamp, "unique id of fraga", null);
        Arende svar = createValidArendeForLuse("svar", fragaTimestamp.plusMinutes(10), "unique id of svar", fraga);

        // When
        List<ArendeConversationView> res = converter.buildArendeConversations(fraga.getIntygsId(), ImmutableList.of(fraga, svar),
                Collections.emptyList(), Collections.emptyList());

        // Then
        Assertions.assertThat(res)
                .hasSize(1)
                .extracting("fraga", "svar", "answeredWithIntyg")
                .contains(tuple(converter.convertToDto(fraga), converter.convertToDto(svar), null));
    }

    @Test
    public void nullListOfKompltIntygShouldThrowException() {
        // Given
        LocalDateTime fragaTimestamp = LocalDateTime.parse("2016-03-01T11:22:11");
        Arende fraga = createValidArendeForLuse("fraga", fragaTimestamp, "unique id of fraga", null);
        Arende svar = createValidArendeForLuse("svar", fragaTimestamp.plusMinutes(10), "unique id of svar", fraga);

        // When
        Throwable thrown = catchThrowable(() -> {
            converter.buildArendeConversations(
                    fraga.getIntygsId(),
                    ImmutableList.of(fraga, svar),
                    null,
                    Collections.emptyList());
        });

        // Then
        Assertions.assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void ifMultipleKompltIntygClosestInTimeShouldBeChoosen() {
        // Given
        LocalDateTime fragaDate = LocalDateTime.parse("2016-03-01T11:22:11");
        Arende fraga = createValidArendeForLuse("fraga", fragaDate, "fraga-id", null);
        List<AnsweredWithIntyg> komplt = ImmutableList.of(
                createMatchingAnsweredWithIntyg(fraga, fragaDate.plusDays(3)),
                createMatchingAnsweredWithIntyg(fraga, fragaDate.plusDays(1)),
                createMatchingAnsweredWithIntyg(fraga, fragaDate.plusDays(2)));

        // When
        List<ArendeConversationView> messageThreads = converter.buildArendeConversations(fraga.getIntygsId(), ImmutableList.of(fraga),
                komplt, Collections.emptyList());

        // Then
        Assertions.assertThat(messageThreads)
                .hasSize(1)
                .extracting(ArendeConversationView::getAnsweredWithIntyg)
                .contains(komplt.get(1));
    }

    private AnsweredWithIntyg createMatchingAnsweredWithIntyg(Arende fraga, LocalDateTime signDate) {
        LocalDateTime sendDate = signDate.plusMinutes(5);
        if (fraga.getSenasteHandelse().compareTo(sendDate) < 0) {
            fraga.setSenasteHandelse(sendDate);
        }
        return AnsweredWithIntyg.create(fraga.getIntygsId(), "signeratAv", signDate, sendDate, "namnetpaskapareavintyg");
    }

    private Arende createValidArendeForLuse(String typeOfArende, LocalDateTime timestamp, String meddelandeId, Arende relatedFraga) {
        /*
         * A Arende (message) has different fields (corresponding to columns in the database) set, depending whether
         * it is a 'fraga', 'svar' or 'paminnelse'.
         *
         * timestamp: when entry was created in the database
         * skickatTidpunkt: the date and time written into the original request from FK
         * senasteHandelse: timestamp of last message related to a 'thread of messages' (which consists of 1 fraga, 0..1
         * svar 0..n paminnelser
         * referensId: FK's internal tracking id
         */
        if (typeOfArende == null || timestamp == null) {
            throw new NullPointerException();
        }
        if ((!typeOfArende.equals("fraga")) && relatedFraga == null) {
            throw new IllegalArgumentException("No related fraga provided.");
        }
        Arende arende = new Arende();
        // First, set the fields all three types have in common:
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setIntygsId(intygsId);
        arende.setStatus(se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION);
        arende.setMeddelandeId(meddelandeId); // Unique for each Arende
        arende.setMeddelande("This is the text of a auto generated Arende.");
        arende.setPatientPersonId("191212121212");
        arende.setTimestamp(timestamp);
        arende.setIntygTyp("luse");
        arende.setVardaktorName(VARDAKTOR_NAMN);
        arende.setEnhetId(ENHETS_ID);
        arende.setEnhetName(ENHETS_NAMN);
        arende.setVardgivareName(VARDGIVARE_NAMN);
        arende.setSenasteHandelse(timestamp);
        arende.setSkickatAv("Fragestallare");

        // Then, set fields which are specific per type
        switch (typeOfArende) {
        case "fraga":
            arende.setRubrik("Fraga");
            arende.setSistaDatumForSvar(LocalDateTime.now().plusDays(4).toLocalDate());
            arende.setAmne(ArendeAmne.KOMPLT);
            break;
        case "svar":
            arende.setRubrik("Svar");
            arende.setSvarPaId(relatedFraga.getMeddelandeId()); // Contains the thread root's meddelandeId.
            arende.setAmne(ArendeAmne.KOMPLT);
            break;
        case "paminnelse":
            arende.setRubrik("Paminnelse");
            arende.setSistaDatumForSvar(LocalDateTime.now().plusDays(4).toLocalDate());
            // Contains the thread root's meddelandeId.
            arende.setPaminnelseMeddelandeId(relatedFraga.getMeddelandeId());
            arende.setAmne(ArendeAmne.PAMINN);
            break;
        }

        // Finally, update the root message (fraga) of the thread
        if (!typeOfArende.equals("fraga")) {
            relatedFraga.setSenasteHandelse(timestamp);
        }

        return arende;
    }

    private LisjpUtlatandeV1 buildLisjpUtlatande(String intygsid2, String enhetsId, String enhetsNamn, String patientPersonId,
                                                 String skapadAvNamn, String skapadavPersonId, LocalDateTime timeStamp) {

        LisjpUtlatandeV1.Builder template = LisjpUtlatandeV1.builder();

        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsNamn, patientPersonId, skapadavPersonId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setTelefonkontaktMedPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private LuseUtlatandeV1 buildLuseUtlatande(String intygsId, String enhetsId, String enhetsnamn, String patientPersonId,
                                               String string, String skapadAvId, LocalDateTime timeStamp) {
        LuseUtlatandeV1.Builder template = LuseUtlatandeV1.builder();
        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsnamn, patientPersonId, skapadAvId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setAnhorigsBeskrivningAvPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private GrundData buildGrundData(String enhetsId, String enhetsNamn, String patientPersonId, String skapadavPersonId,
            LocalDateTime timeStamp) {
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
        Personnummer personId = Personnummer.createPersonnummer(patientPersonId).get();
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
