/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.fmb.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumer;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxData;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Funktionsnedsattning;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Attributes;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Fmbtillstand;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;
import se.inera.intyg.webcert.integration.fmb.model.typfall.TypfallData;
import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbCallType;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

public class FmbServiceImplTest {

    @InjectMocks
    private FmbServiceImpl fmbServiceImpl;

    @Mock
    private FmbRepository fmbRepository;

    @Mock
    private FmbConsumer fmbConsumer;

    @Captor
    private ArgumentCaptor<List<Fmb>> fmbCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGoldenMaster() throws Exception {
        //Given
        ObjectMapper mapper = new ObjectMapper();
        final URL typfallJson = getClass().getResource("/TypfallStubResponse.json");
        final Typfall typfall = mapper.readValue(typfallJson, Typfall.class);
        final URL fmdxInfoJson = getClass().getResource("/FmdxInfoStubResponse.json");
        final FmdxInformation fmdxInformation = mapper.readValue(fmdxInfoJson, FmdxInformation.class);
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(fmdxInformation);
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(typfall);

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        List<Fmb> fmbCaptorAllValues = fmbCaptor.getValue();
        assertEquals(1865, fmbCaptorAllValues.size());
        assertEquals(100, getCount(fmbCaptorAllValues, FmbType.AKTIVITETSBEGRANSNING));
        assertEquals(863, getCount(fmbCaptorAllValues, FmbType.BESLUTSUNDERLAG_TEXTUELLT));
        assertEquals(292, getCount(fmbCaptorAllValues, FmbType.FUNKTIONSNEDSATTNING));
        assertEquals(305, getCount(fmbCaptorAllValues, FmbType.GENERELL_INFO));
        assertEquals(305, getCount(fmbCaptorAllValues, FmbType.SYMPTOM_PROGNOS_BEHANDLING));

    }

    private long getCount(List<Fmb> fmbCaptorAllValues, FmbType aktivitetsbegransning) {
        return fmbCaptorAllValues.stream().filter(fmb -> fmb.getTyp().equals(aktivitetsbegransning)).count();
    }

    @Test
    public void testThatDotsInIcd10CodesAreRemoved() throws Exception {
        //Given
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation("", "J22.2", "J22.4"));
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(createTypfall("", "J22.2", "J22.4"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        List<List<Fmb>> fmbCaptorAllValues = fmbCaptor.getAllValues();
        assertEquals("J222", fmbCaptorAllValues.get(0).get(0).getIcd10());
        assertEquals("J224", fmbCaptorAllValues.get(0).get(1).getIcd10());
    }

    @Test
    public void testUpdateDiagnosInfoUpdatesCorrectlyOnEmptyDb() throws Exception {
        //Given
        final String beskrivning = "test";
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation(beskrivning, "J22"));
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(createTypfall("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteAllInBatch();
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, findFmbType(FmbType.FUNKTIONSNEDSATTNING, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateDiagnosInfoUpdatesCorrectlyOnNonEmptyDb() throws Exception {
        //Given
        final String beskrivning = "test";
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation(beskrivning, "J22"));
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(createTypfall("Testunderlag", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteAllInBatch();
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, findFmbType(FmbType.FUNKTIONSNEDSATTNING, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateDiagnosInfoWillNotBeDoneIfTypfallCouldNotBeFetched() throws Exception {
        //Given
        final String beskrivning = "test";
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation(beskrivning, "J22"));
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(null);

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(0)).deleteAllInBatch();
        Mockito.verify(fmbRepository, times(0)).save(fmbCaptor.capture());
    }

    @Test
    public void testUpdateDiagnosInfoWillNotBeDoneIfFmdxInfoCouldNotBeFetched() throws Exception {
        //Given
        final String beskrivning = "test";
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(null);
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(createTypfall("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(0)).deleteAllInBatch();
        Mockito.verify(fmbRepository, times(0)).save(fmbCaptor.capture());
    }

    private Fmb createFmbDi(String icd10, String text) {
        return new Fmb(icd10, FmbType.FUNKTIONSNEDSATTNING, FmbCallType.DIAGNOSINFORMATION, text, "unknown");
    }

    private Fmb findFmbType(FmbType fmbType, List<Fmb> fmbs) {
        for (Fmb fmb : fmbs) {
            if (fmbType.equals(fmb.getTyp())) {
                return fmb;
            }
        }
        throw new RuntimeException("Could not find Fmb with type: " + fmbType);
    }

    private FmdxInformation createFmdxInformation(String funktionsnedsattningBeskrivning, String... icd10Codes) {
        final FmdxInformation fmdxInformation = new FmdxInformation();
        final ArrayList<FmdxData> data = new ArrayList<>();
        fmdxInformation.setData(data);
        final FmdxData fmdxData = new FmdxData();
        data.add(fmdxData);
        final se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes attributes = new se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes();
        fmdxData.setAttributes(attributes);
        attributes.setDiagnoskod(Arrays.stream(icd10Codes).map(dx -> {
            final Kod kod = new Kod();
            kod.setKod(dx);
            return kod;
        }).collect(Collectors.toList()));
        final Funktionsnedsattning funktionsnedsattning = new Funktionsnedsattning();
        funktionsnedsattning.setBeskrivning(funktionsnedsattningBeskrivning);
        attributes.setFunktionsnedsattning(funktionsnedsattning);
        return fmdxInformation;
    }

    private Typfall createTypfall(String underlag, String... icd10Codes) {
        final Typfall typfall = new Typfall();
        final ArrayList<TypfallData> data = new ArrayList<>();
        typfall.setData(data);
        final TypfallData typfallData = new TypfallData();
        final Attributes attributes = new Attributes();
        typfallData.setAttributes(attributes);
        attributes.setTypfallsmening(underlag);
        final Fmbtillstand fmbtillstand = new Fmbtillstand();
        attributes.setFmbtillstand(fmbtillstand);
        fmbtillstand.setDiagnoskod(Arrays.stream(icd10Codes).map(dx -> {
            final Kod kod = new Kod();
            kod.setKod(dx);
            return kod;
        }).collect(Collectors.toList()));
        return typfall;
    }

}
