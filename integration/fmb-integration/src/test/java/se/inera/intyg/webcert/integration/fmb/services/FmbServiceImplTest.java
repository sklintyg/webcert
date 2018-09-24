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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MoreCollectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import se.inera.intyg.webcert.persistence.fmb.model.icf.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;

public class FmbServiceImplTest {

    @InjectMocks
    private FmbServiceImpl fmbServiceImpl;

    @Mock
    private FmbRepository fmbRepository;

    @Mock
    private DiagnosInformationRepository diagnosInformationRepository;

    @Mock
    private FmbConsumer fmbConsumer;

    @Captor
    private ArgumentCaptor<List<DiagnosInformation>> fmbCaptor;

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
        Mockito.verify(diagnosInformationRepository, times(1)).save(fmbCaptor.capture());
        List<DiagnosInformation> fmbCaptorAllValues = fmbCaptor.getValue();
        assertEquals(111, fmbCaptorAllValues.size());
    }

    private long getCount(List<Fmb> fmbCaptorAllValues, FmbType aktivitetsbegransning) {
        return fmbCaptorAllValues.stream().filter(fmb -> fmb.getTyp().equals(aktivitetsbegransning)).count();
    }

    @Test
    public void testThatDotsInIcd10CodesAreRemoved() throws Exception {
        //Given
//        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation("", "J22.2", "J22.4"));

        doReturn(createFmdxInformation("", "J22.2", "J22.4")).when(fmbConsumer).getForsakringsmedicinskDiagnosinformation();
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(createTypfall("", "J22.2", "J22.4"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(diagnosInformationRepository, times(1)).save(fmbCaptor.capture());
        List<List<DiagnosInformation>> fmbCaptorAllValues = fmbCaptor.getAllValues();
        assertEquals("J222", fmbCaptorAllValues.get(0).get(0).getIcd10KodList().get(0).getKod());
        assertEquals("J224", fmbCaptorAllValues.get(0).get(0).getIcd10KodList().get(1).getKod());
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
        Mockito.verify(diagnosInformationRepository, times(1)).deleteAll();
        Mockito.verify(diagnosInformationRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, fmbCaptor.getValue().get(0).getBeskrivningList().stream()
                .filter(besk -> besk.getBeskrivningTyp() == BeskrivningTyp.FUNKTIONSNEDSATTNING)
                .collect(MoreCollectors.onlyElement()).getBeskrivningText());
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
        Mockito.verify(diagnosInformationRepository, times(1)).deleteAll();
        Mockito.verify(diagnosInformationRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, fmbCaptor.getValue().get(0).getBeskrivningList().stream()
                .filter(besk -> besk.getBeskrivningTyp() == BeskrivningTyp.FUNKTIONSNEDSATTNING)
                .collect(MoreCollectors.onlyElement()).getBeskrivningText());    }

    @Test
    public void testUpdateDiagnosInfoWillNotBeDoneIfTypfallCouldNotBeFetched() throws Exception {
        //Given
        final String beskrivning = "test";
        Mockito.when(fmbConsumer.getForsakringsmedicinskDiagnosinformation()).thenReturn(createFmdxInformation(beskrivning, "J22"));
        Mockito.when(fmbConsumer.getTypfall()).thenReturn(null);

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(diagnosInformationRepository, times(0)).deleteAll();
        Mockito.verify(diagnosInformationRepository, times(0)).save(fmbCaptor.capture());

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
        Mockito.verify(diagnosInformationRepository, times(0)).deleteAllInBatch();
        Mockito.verify(diagnosInformationRepository, times(0)).save(fmbCaptor.capture());
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
