package se.inera.intyg.webcert.web.service.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-08-30.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatisticsGroupByUtilTest {

    private static final String HSA1 = "hsa-1";
    private static final String HSA2 = "hsa-2";
    private static final String PNR1 = "191212121212";
    private static final String PNR2 = "191313131313";
    private static final String PNR3 = "191414141414";
    @Mock
    private PUService puService;

    @InjectMocks
    private StatisticsGroupByUtil testee;

    @Test
    public void testFilterAndGroupForTwoResultsOfSameUnitOneIsSekr() {

        Personnummer pnr1 = Personnummer.createValidatedPersonnummerWithDash(PNR1).get();
        when(puService.getPerson(pnr1)).thenReturn(buildPersonSvar(pnr1, false));

        Personnummer pnr2 = Personnummer.createValidatedPersonnummerWithDash(PNR2).get();
        when(puService.getPerson(pnr2)).thenReturn(buildPersonSvar(pnr2, true));

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"id-1", HSA1, PNR1});
        queryResult.add(new Object[]{"id-2", HSA1, PNR2});

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(1, result.size());
        assertEquals(new Long(1L), result.get(HSA1));
    }


    @Test
    public void testFilterAndGroupForMultipleUnits() {

        Personnummer pnr1 = Personnummer.createValidatedPersonnummerWithDash(PNR1).get();
        when(puService.getPerson(pnr1)).thenReturn(buildPersonSvar(pnr1, false));

        Personnummer pnr2 = Personnummer.createValidatedPersonnummerWithDash(PNR2).get();
        when(puService.getPerson(pnr2)).thenReturn(buildPersonSvar(pnr2, true));

        Personnummer pnr3 = Personnummer.createValidatedPersonnummerWithDash(PNR3).get();
        when(puService.getPerson(pnr3)).thenReturn(buildPersonSvar(pnr3, false));

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"id-1", HSA1, PNR1});
        queryResult.add(new Object[]{"id-2", HSA1, PNR2});
        queryResult.add(new Object[]{"id-3", HSA1, PNR3});
        queryResult.add(new Object[]{"id-4", HSA2, PNR3});
        queryResult.add(new Object[]{"id-5", HSA2, PNR1});
        queryResult.add(new Object[]{"id-6", HSA2, PNR3});

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(2, result.size());
        assertEquals(new Long(2L), result.get(HSA1));
        assertEquals(new Long(3L), result.get(HSA2));
    }

    @Test
    public void testAssumeNotSekrWhenPUNotResponding() {

        Personnummer pnr1 = Personnummer.createValidatedPersonnummerWithDash(PNR1).get();
        when(puService.getPerson(pnr1)).thenReturn(buildPersonSvar(pnr1, false, PersonSvar.Status.ERROR));

        Personnummer pnr2 = Personnummer.createValidatedPersonnummerWithDash(PNR2).get();
        when(puService.getPerson(pnr2)).thenReturn(buildPersonSvar(pnr2, true, PersonSvar.Status.ERROR));

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"id-1", HSA1, PNR1});
        queryResult.add(new Object[]{"id-2", HSA1, PNR2});

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(1, result.size());
        assertEquals(new Long(2L), result.get(HSA1));
    }

    private PersonSvar buildPersonSvar(Personnummer pnr, boolean sekretessmarkering) {
        return buildPersonSvar(pnr, sekretessmarkering, PersonSvar.Status.FOUND);
    }

    private PersonSvar buildPersonSvar(Personnummer pnr, boolean sekretessmarkering, PersonSvar.Status status) {
        Person person = new Person(pnr, sekretessmarkering, false, "Förnamn", "", "Efternamn", "Postgatan 1", "12345", "Postort");
        return new PersonSvar(person, status);
    }

    @Test
    public void testFilterEmptyMap() {

        Map<String, Long> result = testee.toSekretessFilteredMap(new ArrayList<>());
        assertEquals(0, result.size());
    }
}
